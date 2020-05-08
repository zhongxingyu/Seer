 package org.meta_environment.eclipse.facts;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import nl.cwi.sen1.relationstores.Factory;
 import nl.cwi.sen1.relationstores.types.IdCon;
 import nl.cwi.sen1.relationstores.types.RElem;
 import nl.cwi.sen1.relationstores.types.RElemElements;
 import nl.cwi.sen1.relationstores.types.RStore;
 import nl.cwi.sen1.relationstores.types.RTuple;
 import nl.cwi.sen1.relationstores.types.RTupleRtuples;
 import nl.cwi.sen1.relationstores.types.RType;
 import nl.cwi.sen1.relationstores.types.RTypeColumnTypes;
 import nl.cwi.sen1.relationstores.types.relem.Bag;
 import nl.cwi.sen1.relationstores.types.relem.Bool;
 import nl.cwi.sen1.relationstores.types.relem.Int;
 import nl.cwi.sen1.relationstores.types.relem.Loc;
 import nl.cwi.sen1.relationstores.types.relem.Set;
 import nl.cwi.sen1.relationstores.types.relem.Str;
 import nl.cwi.sen1.relationstores.types.relem.Tuple;
 import nl.cwi.sen1.relationstores.types.rtype.Relation;
 
 import org.eclipse.imp.pdb.facts.ISetWriter;
 import org.eclipse.imp.pdb.facts.ISourceRange;
 import org.eclipse.imp.pdb.facts.IString;
 import org.eclipse.imp.pdb.facts.ITuple;
 import org.eclipse.imp.pdb.facts.IValue;
 import org.eclipse.imp.pdb.facts.db.FactBase;
 import org.eclipse.imp.pdb.facts.db.FactKey;
 import org.eclipse.imp.pdb.facts.db.IFactContext;
 import org.eclipse.imp.pdb.facts.db.IFactKey;
import org.eclipse.imp.pdb.facts.impl.reference.ValueFactory;
 import org.eclipse.imp.pdb.facts.type.Type;
 import org.eclipse.imp.pdb.facts.type.TypeFactory;
 import org.eclipse.imp.runtime.RuntimePlugin;
 
 import toolbus.adapter.eclipse.EclipseTool;
 import aterm.ATerm;
 
 public class FactsTool extends EclipseTool {
 	private static final TypeFactory types = TypeFactory.getInstance();
 	private static final ValueFactory values = ValueFactory.getInstance();
 
 	private static final IString FALSE_VALUE = values.string("false");
 
 	private static final IString TRUE_VALUE = values.string("true");
 
 	private static final String TOOL_NAME = "facts";
 	
 	private FactBase base = FactBase.getInstance();
 	
 	private static Factory factory;
 
 	private static class InstanceKeeper {
 		private static FactsTool sInstance = new FactsTool();
 		static {
 			sInstance.connect();
 			factory = Factory.getInstance(getFactory());
 		}
 	}
 	
 	private class StringContext implements IFactContext {
 		private String id;
 		
 		public StringContext(String id) {
 			this.id = id;
 		}
 		
 		public String toString() {
 			return id;
 		}
 		
 		public boolean equals(Object obj) {
 			if (obj instanceof StringContext) {
 				return id.equals(((StringContext) obj).id);
 			}
 			return false;
 		}
 	}
 
 	private FactsTool() {
 		super(TOOL_NAME);
 	}
 
 	public static FactsTool getInstance() {
 		return InstanceKeeper.sInstance;
 	}
 	
 	public void loadRstore(String path, ATerm astore) {
 		RStore store = factory.RStoreFromTerm(astore);
 		IFactContext context = new StringContext(path);
 		
 		try {
 		  loadRStore(context, store);
 		}
 		catch (Exception ex) {
 			RuntimePlugin.getInstance().logException("An error occurred while loading an RStore", ex);
 		}
 	}
 
 	private void loadRStore(IFactContext context, RStore store) {
 		RTupleRtuples tuples = store.getRtuples();
 		
 		for ( ; !tuples.isEmpty(); tuples = tuples.getTail()) {
 			loadRTuple(context, tuples.getHead());
 		}
 	}
 
 	private void loadRTuple(IFactContext context, RTuple head) {
 		Type type = convertType(head.getRtype());
 		IValue value = convertValue(head.getValue(), head.getRtype());
 		IFactKey key = convertVariable(head.getVariable(), type, context);
 	
 		base.defineFact(key, value);
 	}
 
 	public IFactKey convertVariable(IdCon variable, Type type, IFactContext context) {
 		return new FactKey(types.namedType(variable.getString(), type), context);
 	}
 
 	public IValue convertValue(RElem value, RType type) {
 		if (value.isBag()) {
 			return convertBag((Bag) value);
 		}
 		else if (value.isBool()) {
 			return convertBool((Bool) value);
 		}
 		else if (value.isInt()) {
 			return convertInt((Int) value);
 		}
 		else if (value.isSet()) {
 			if (type.isRelation()) {
 			  return convertRelation((Set) value, (Relation) type);
 			} 
 			else if (type.isSet()){
 			  return convertSet((Set) value, (nl.cwi.sen1.relationstores.types.rtype.Set) type);
 			}
 		}
 		else if (value.isLoc()) {
 			return convertLocation((Loc) value);
 		}
 		else if (value.isStr()) {
 			return convertString((Str) value);
 		}
 		else if (value.isTuple()) {
 			return convertTuple((Tuple) value, type);
 		}
 		
 	    throw new UnsupportedOperationException("Unknown kind of value: " + value.getClass());
 	}
 
 	private IValue convertTuple(Tuple value, RType type) {
 		RElemElements elems = value.getElements();
 		RTypeColumnTypes columnTypes = type.getColumnTypes();
 		int size = elems.getLength();
 		IValue[] tmpArray = new IValue[size];
 		
 		for (int i = 0; !elems.isEmpty(); elems = elems.getTail(), i++) {
 			tmpArray[i] = convertValue(elems.getHead(), columnTypes.getRTypeAt(i));
 		}
 		
 		return values.tuple(tmpArray);
 	}
 
 	private IValue convertLocation(Loc value) {
 		nl.cwi.sen1.relationstores.types.Location l = value.getLocation();
 		
 		if (l.isArea()) {
 			nl.cwi.sen1.relationstores.types.Area area = l.getArea();
 			ISourceRange range = values.sourceRange(
 					area.getOffset(), 
 					area.getLength(),
 					area.getBeginLine(),
 					area.getEndLine(),
 					area.getBeginColumn(),
 					area.getEndColumn());
 			return values.sourceLocation("<unknown>", range);
 		}
 		else if (l.isFile()) {
 			return values.sourceLocation(l.getFilename(), values.sourceRange(0, 0, 0, 0, 0, 0));
 		}
 		else if (l.isAreaInFile()) {
 			nl.cwi.sen1.relationstores.types.Area area = l.getArea();
 			ISourceRange range = values.sourceRange(
 					area.getOffset(), 
 					area.getLength(),
 					area.getBeginLine(),
 					area.getEndLine(),
 					area.getBeginColumn(),
 					area.getEndColumn());
 			return values.sourceLocation(l.getFilename(), range);
 		}
 		
 		throw new UnsupportedOperationException("Unknown kind of value: " + value.getClass());
 	}
 
 	private IValue convertString(Str value) {
 		return values.string(value.getStrCon());
 	}
 
 	private IValue convertSet(Set value, nl.cwi.sen1.relationstores.types.rtype.Set type) {
 		RElemElements elements = value.getElements();
 		Type elementType = convertType(type.getElementType());
 		ISetWriter w = values.setWriter(elementType);
 		
 		for ( ; !elements.isEmpty(); elements = elements.getTail()) {
 			w.insert(convertValue(elements.getHead(), type.getElementType()));
 		}
 		
 		return w.done();
 	}
 
 	private IValue convertRelation(Set value, Relation type) {
 		RElemElements elements = value.getElements();
		Type tupleType = getTupleType(type);
 		nl.cwi.sen1.relationstores.types.rtype.Tuple rTupleType = factory.makeRType_Tuple(type.getColumnTypes());
 		ISetWriter w = values.relationWriter(tupleType);
 		
 		for ( ; !elements.isEmpty(); elements = elements.getTail()) {
 			w.insert((ITuple) convertValue(elements.getHead(), rTupleType));
 		}
 		
 		return w.done();
 	}
 
	private Type getTupleType(Relation type) {
 	  return convertTupleTypes(type.getColumnTypes());
 	}
 	
 	private IValue convertInt(Int value) {
 		return values.integer(value.getInteger().getNatCon());
 	}
 
 	private IValue convertBool(Bool value) {
 		if (value.getBoolCon().isTrue()) {
 			return TRUE_VALUE;
 		}
 		return FALSE_VALUE;
 	}
 
 	private IValue convertBag(Bag value) {
 		throw new UnsupportedOperationException("Unknown kind of value: " + value.getClass());
 	}
 
 	public Type convertType(RType rtype) {
 		if (rtype.isBag()) {
 			return types.setType(convertType(rtype.getElementType()));
 		}
 		else if (rtype.isBool()) {
 			return types.stringType();
 		}
 		else if (rtype.isInt()) {
 			return types.integerType();
 		}
 		else if (rtype.isRelation()) {
 			return types.relType(convertTupleTypes(rtype.getColumnTypes()));
 		}
 		else if (rtype.isSet()) {
 			return types.setType(convertType(rtype.getElementType()));
 		}
 		else if (rtype.isLoc()) {
 			return types.sourceLocationType();
 		}
 		else if (rtype.isStr()) {
 			return types.stringType();
 		}
 		else if (rtype.isTuple()) {
 			return convertTupleTypes(rtype.getColumnTypes());
 		}
 		else if (rtype.isUserDefined()) {
 			return types.namedType(rtype.getTypeName().getString(), types.valueType());
 		}
 		
 		throw new UnsupportedOperationException("Unknown type: " + rtype);
 	}
 
	private Type convertTupleTypes(RTypeColumnTypes columnTypes) {
 		List<Type> fieldTypes = new LinkedList<Type>();
 		
 		for ( ; !columnTypes.isEmpty(); columnTypes = columnTypes.getTail()) {
 			fieldTypes.add(fieldTypes.size(), convertType(columnTypes.getHead()));
 		}
 		
 		return types.tupleType(fieldTypes);
 	}
 }
