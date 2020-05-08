 package lapd.databases.neo4j;
 
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.imp.pdb.facts.IMapWriter;
 import org.eclipse.imp.pdb.facts.IValue;
 import org.eclipse.imp.pdb.facts.IValueFactory;
 import org.eclipse.imp.pdb.facts.io.IValueTextReader;
 import org.eclipse.imp.pdb.facts.io.StandardTextReader;
 import org.eclipse.imp.pdb.facts.type.ITypeVisitor;
 import org.eclipse.imp.pdb.facts.type.Type;
 import org.eclipse.imp.pdb.facts.type.TypeFactory;
 import org.eclipse.imp.pdb.facts.type.TypeStore;
 import org.neo4j.graphdb.Direction;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Relationship;
 
 public class GraphDbValueRetrievalVisitor implements ITypeVisitor<IValue, GraphDbMappingException>{
 	
 	private final Node node;
 	private final IValueFactory valueFactory;
 	private final TypeStore typeStore;
 	
 	public GraphDbValueRetrievalVisitor(Node node, IValueFactory valueFactory, TypeStore typeStore) {
 		this.node = node;
 		this.valueFactory = valueFactory;
 		this.typeStore = typeStore;
 	}
 	
 	@Override
 	public IValue visitString(Type type) throws GraphDbMappingException {
 		return valueFactory.string(node.getProperty(PropertyNames.STRING).toString());
 	}
 	
 	@Override
 	public IValue visitInteger(Type type) throws GraphDbMappingException {
 		return valueFactory.integer(node.getProperty(PropertyNames.INTEGER).toString());
 	}
 
 	@Override
 	public IValue visitReal(Type type) throws GraphDbMappingException {
 		return valueFactory.real(node.getProperty(PropertyNames.REAL).toString());
 	}
  
 	@Override
 	public IValue visitBool(Type type) throws GraphDbMappingException {
 		return valueFactory.bool((Boolean)node.getProperty(PropertyNames.BOOLEAN));
 	}
 
 	@Override
 	public IValue visitRational(Type type) throws GraphDbMappingException {
 		String numerator = node.getProperty(PropertyNames.NUMERATOR).toString();
 		String denominator = node.getProperty(PropertyNames.DENOMINATOR).toString();
 		return valueFactory.rational(numerator + "r" + denominator);
 	}
 	
 	@Override
 	public IValue visitSourceLocation(Type type) throws GraphDbMappingException {
 		String locString = node.getProperty(PropertyNames.SOURCE_LOCATION).toString();
 		IValueTextReader reader = new StandardTextReader();
 		try {
 			return reader.read(valueFactory, TypeFactory.getInstance().sourceLocationType(), 
 					new StringReader(locString));
 		} catch (Exception e) {
 			throw new GraphDbMappingException(e.getMessage());
 		}
 	}
 	
 	@Override
 	public IValue visitDateTime(Type type) throws GraphDbMappingException {
		// datetime deviates by one hour, hence the -3600000
		return valueFactory.datetime((Long)node.getProperty(PropertyNames.DATE_TIME) - 3600000);
 	}
 
 	@Override
 	public IValue visitList(Type type) throws GraphDbMappingException {
 		if (!hasHead())
 			return valueFactory.list(TypeFactory.getInstance().voidType());
 		List<IValue> elementList = getElementValues(type);
 		return valueFactory.list(elementList.toArray(new IValue[elementList.size()]));
 	}
 	
 	@Override
 	public IValue visitSet(Type type) throws GraphDbMappingException {
 		if (!hasHead())
 			return valueFactory.set(TypeFactory.getInstance().voidType());
 		List<IValue> elementList = getElementValues(type);
 		return valueFactory.set(elementList.toArray(new IValue[elementList.size()]));
 	}
 	
 	@Override
 	public IValue visitNode(Type type) throws GraphDbMappingException {
 		String nodeName = node.getProperty(PropertyNames.NODE).toString();
 		if (!node.hasRelationship(Direction.OUTGOING, RelTypes.HEAD)) 
 			return valueFactory.node(nodeName);		
 		List<IValue> valueList = new ArrayList<IValue>();
 		Node currentNode = node.getSingleRelationship(RelTypes.HEAD, Direction.OUTGOING).getEndNode();
 		valueList.add(new TypeDeducer(currentNode, typeStore).getType().accept(new GraphDbValueRetrievalVisitor(currentNode, valueFactory, typeStore)));
 		while (currentNode.hasRelationship(Direction.OUTGOING, RelTypes.NEXT_ELEMENT)) {
 			currentNode = currentNode.getSingleRelationship(RelTypes.NEXT_ELEMENT, Direction.OUTGOING).getEndNode();
 			valueList.add(new TypeDeducer(currentNode, typeStore).getType().accept(new GraphDbValueRetrievalVisitor(currentNode, valueFactory, typeStore)));
 		}
 		if (!hasAnnotations()) 
 			return valueFactory.node(nodeName, valueList.toArray(new IValue[valueList.size()]));
 		Map<String, IValue> annotations = getAnnotations();
 		return valueFactory.node(nodeName, annotations, valueList.toArray(new IValue[valueList.size()]));
 	}		
 	
 	@Override
 	public IValue visitConstructor(Type type) throws GraphDbMappingException {		
 		if (!hasHead()) 
 			return valueFactory.constructor(type);		
 		List<IValue> valueList = getFields(type);
 		if (!hasAnnotations())
 			return valueFactory.constructor(type, valueList.toArray(new IValue[valueList.size()]));
 		Map<String, IValue> annotations = getAnnotations();
 		return valueFactory.constructor(type, annotations, valueList.toArray(new IValue[valueList.size()]));
 	}
 	
 	@Override
 	public IValue visitTuple(Type type) throws GraphDbMappingException {
 		if (!hasHead())
 			return valueFactory.tuple();
 		List<IValue> valueList = getFields(type);
 		return valueFactory.tuple(valueList.toArray(new IValue[valueList.size()]));
 	}
 
 	@Override
 	public IValue visitMap(Type type) throws GraphDbMappingException {
 		IMapWriter mapWriter = valueFactory.mapWriter();
 		if (!hasHead())		
 			return mapWriter.done();
 		Node currentKeyNode = node.getSingleRelationship(RelTypes.HEAD, Direction.OUTGOING).getEndNode();
 		Node currentValueNode = currentKeyNode.getSingleRelationship(RelTypes.MAP_KEY_VALUE, Direction.OUTGOING).getEndNode();
 		mapWriter.put(new TypeDeducer(currentKeyNode, typeStore).getType().accept(new GraphDbValueRetrievalVisitor(currentKeyNode, valueFactory, typeStore)),
 				new TypeDeducer(currentValueNode, typeStore).getType().accept(new GraphDbValueRetrievalVisitor(currentValueNode, valueFactory, typeStore)));
 		while (currentKeyNode.hasRelationship(Direction.OUTGOING, RelTypes.NEXT_ELEMENT)) {
 			currentKeyNode = currentKeyNode.getSingleRelationship(RelTypes.NEXT_ELEMENT, Direction.OUTGOING).getEndNode();
 			currentValueNode = currentKeyNode.getSingleRelationship(RelTypes.MAP_KEY_VALUE, Direction.OUTGOING).getEndNode();
 			mapWriter.put(new TypeDeducer(currentKeyNode, typeStore).getType().accept(new GraphDbValueRetrievalVisitor(currentKeyNode, valueFactory, typeStore)),
 					new TypeDeducer(currentValueNode, typeStore).getType().accept(new GraphDbValueRetrievalVisitor(currentValueNode, valueFactory, typeStore)));
 		}
 		return mapWriter.done();
 	}
 	
 	@Override
 	public IValue visitExternal(Type type) throws GraphDbMappingException {
 		throw new GraphDbMappingException("Cannot handle external types.");
 	}
 
 	@Override
 	public IValue visitNumber(Type type) throws GraphDbMappingException {
 		return new TypeDeducer(node, typeStore).getType().accept(this);
 	}
 
 	@Override
 	public IValue visitAlias(Type type) throws GraphDbMappingException {
 		return type.getAliased().accept(this);
 	}
 
 	@Override
 	public IValue visitAbstractData(Type type) throws GraphDbMappingException {
 		String name = node.getProperty(PropertyNames.NODE).toString();
 		return visitConstructor(typeStore.lookupConstructor(type, name).iterator().next());
 	}
 
 	@Override
 	public IValue visitValue(Type type) throws GraphDbMappingException {
 		return new TypeDeducer(node, typeStore).getType().accept(this);
 	}
 
 	@Override
 	public IValue visitVoid(Type type) throws GraphDbMappingException {
 		throw new GraphDbMappingException("Cannot handle void types.");
 	}	
 
 	@Override
 	public IValue visitParameter(Type type) throws GraphDbMappingException {
 		return type.getBound().accept(this);
 	}
 	
 	private List<IValue> getElementValues(Type type) throws GraphDbMappingException {
 		List<IValue> valueList = new ArrayList<IValue>();		
 		Node currentNode = node.getSingleRelationship(RelTypes.HEAD, Direction.OUTGOING).getEndNode();
 		valueList.add(new TypeDeducer(currentNode, typeStore).getType().accept(new GraphDbValueRetrievalVisitor(currentNode, valueFactory, typeStore)));
 		while (currentNode.hasRelationship(Direction.OUTGOING, RelTypes.NEXT_ELEMENT)) {
 			currentNode = currentNode.getSingleRelationship(RelTypes.NEXT_ELEMENT, 
 					Direction.OUTGOING).getEndNode();
 			valueList.add(new TypeDeducer(currentNode, typeStore).getType().accept(new GraphDbValueRetrievalVisitor(currentNode, 
 					valueFactory, typeStore)));
 		}
 		return valueList;
 	}
 
 	private boolean hasHead() {
 		return node.hasRelationship(Direction.OUTGOING, RelTypes.HEAD);
 	}
 	
 	private boolean hasAnnotations() {
 		return node.hasRelationship(Direction.OUTGOING, RelTypes.ANNOTATION);
 	}
 	
 	private Map<String, IValue> getAnnotations() throws GraphDbMappingException {
 		Map<String, IValue> annotations = new HashMap<String, IValue>();
 		for (Relationship rel : node.getRelationships(Direction.OUTGOING, RelTypes.ANNOTATION)) {
 			Node annotationNode = rel.getEndNode();
 			String annotationName = annotationNode.getProperty(PropertyNames.ANNOTATION).toString();
 			IValue annotationValue = new TypeDeducer(annotationNode, typeStore).getType().accept(new GraphDbValueRetrievalVisitor(annotationNode, valueFactory, typeStore));
 			annotations.put(annotationName, annotationValue);
 		}
 		return annotations;
 	}
 	
 	private List<IValue> getFields(Type type) throws GraphDbMappingException {
 		List<IValue> valueList = new ArrayList<IValue>();
 		Node currentNode = node.getSingleRelationship(RelTypes.HEAD, Direction.OUTGOING).getEndNode();
 		valueList.add(new TypeDeducer(currentNode, typeStore).getType().accept(new GraphDbValueRetrievalVisitor(currentNode, valueFactory, typeStore)));
 		while (currentNode.hasRelationship(Direction.OUTGOING, RelTypes.NEXT_ELEMENT)) {
 			currentNode = currentNode.getSingleRelationship(RelTypes.NEXT_ELEMENT, Direction.OUTGOING).getEndNode();
 			valueList.add(new TypeDeducer(currentNode, typeStore).getType().accept(new GraphDbValueRetrievalVisitor(currentNode, valueFactory, typeStore)));
 		}
 		return valueList;
 	}
 
 }
