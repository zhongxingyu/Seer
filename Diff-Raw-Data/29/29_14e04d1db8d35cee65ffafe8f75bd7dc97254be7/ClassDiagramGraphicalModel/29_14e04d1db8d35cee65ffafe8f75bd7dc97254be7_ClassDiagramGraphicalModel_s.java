 package edu.ysu.onionuml.ui.graphics.graphicalmodels;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.eclipse.draw2d.geometry.Point;
 
 import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
 import com.mxgraph.model.mxGeometry;
 import com.mxgraph.view.mxGraph;
 
 import edu.ysu.onionuml.compact.DiagramGraph;
 import edu.ysu.onionuml.core.RelationshipType;
 import edu.ysu.onionuml.core.UmlClassElement;
 import edu.ysu.onionuml.core.UmlClassModel;
 import edu.ysu.onionuml.core.UmlPackageElement;
 import edu.ysu.onionuml.core.UmlRelationshipElement;
 import edu.ysu.onionuml.ui.graphics.IEventListener;
 import edu.ysu.onionuml.ui.graphics.IEventRegistrar;
 import edu.ysu.onionuml.ui.graphics.editparts.ClassDiagramEditPart;
 
 
 /**
  * Represents the model of a uml class diagram to be displayed with the
  * Eclipse Graphical Editing Framework (GEF).
  */
 public class ClassDiagramGraphicalModel implements IEventListener, IEventRegistrar {
 	
 	private static final int NUM_CLASSES_FOR_HYPEREDGES = 3;	// minimum number of child classes for inserting hyperedges
 	
 	// PRIVATE MEMBER VARIABLES ----------------------------------
 	
 	private IEventListener mListener = null;
 	private List<IElementGraphicalModel> mElements;
 	private Map<String,ClassElementGraphicalModel> mClassIdMap =
 			new HashMap<String,ClassElementGraphicalModel>();
 	private Map<String,RelationshipElementGraphicalModel> mRelationshipIdMap =
 			new HashMap<String,RelationshipElementGraphicalModel>();
 	private UmlClassModel mClassModel;
 	private DiagramGraph mDiagramGraph = new DiagramGraph();
 	
 	private boolean mClassSizeChanged = false;
 	
 	
 	
 	// PUBLIC METHODS ----------------------------------------
 	
 	/**
 	 * Constructs a new ClassDiagramGraphicalModel object from the
 	 * given UmlClassModel.
 	 */
 	public ClassDiagramGraphicalModel(UmlClassModel classModel){
 		mElements = new ArrayList<IElementGraphicalModel>();
 		mClassModel = classModel;
 		
 		Iterator<Entry<String,UmlPackageElement>> itPackages = classModel.getPackages().entrySet().iterator();
 		while (itPackages.hasNext()) {
 			Entry<String,UmlPackageElement> packagePairs = (Entry<String,UmlPackageElement>)itPackages.next();
 			
 			Iterator<Entry<String,UmlClassElement>> itClasses =
 					packagePairs.getValue().getClasses().entrySet().iterator();
 			while (itClasses.hasNext()) {
 				Entry<String,UmlClassElement> classPairs = (Entry<String,UmlClassElement>)itClasses.next();
 				ClassElementGraphicalModel n = new ClassElementGraphicalModel(classPairs.getValue(), packagePairs.getValue());
 				n.registerEventListener(this);
 				mElements.add(n);
 				mClassIdMap.put(classPairs.getKey(), n);
 				mDiagramGraph.addElement(n);
 			}
 		}
 		
 		initEdges();
 	}
 	
 	@Override
 	public void registerEventListener(IEventListener listener){
 		mListener = listener;
 	}
 	
 	@Override
 	public void unregisterEventListener(){
 		mListener = null;
 	}
 
 	@Override
 	public void eventOccured(String evt) {
 		
 		if(evt.equals(ClassElementGraphicalModel.EVENT_SIZE_CHANGED)){
 			mClassSizeChanged = true;
 		}
 		else if(evt.equals(ClassDiagramEditPart.EVENT_ACTIVATED)
 				&& mClassSizeChanged && mListener != null){
 			layout();
 			mClassSizeChanged = false;
 			mListener.eventOccured(ClassDiagramEditPart.EVENT_REFRESH_REQUIRED);
 		}
 	}
 	
 	/**
 	 * Returns the collection of elements managed by the DiagramModel.
 	 */
 	public List<IElementGraphicalModel> getElements(){
 		return mElements;
 	}
 	
 	/**
 	 * Gets a reference to the underlying UmlClassModel object.
 	 */
 	public UmlClassModel getClassModel(){
 		return mClassModel;
 	}
 	
 	/**
 	 * Finds the id assigned to the specified element graphical model, or null if the
 	 * element cannot be found.
 	 */
 	public String lookupIdByGraphicalModel(IElementGraphicalModel element){
 		
 		Iterator<Entry<String,ClassElementGraphicalModel>> itClasses =
 				mClassIdMap.entrySet().iterator();
 		while (itClasses.hasNext()) {
 			Entry<String,ClassElementGraphicalModel> pairs =
 					(Entry<String,ClassElementGraphicalModel>)itClasses.next();
 			if(pairs.getValue().equals(element)){
 				return pairs.getKey();
 			}
 		}
 		
 		Iterator<Entry<String,RelationshipElementGraphicalModel>> itRelationships =
 				mRelationshipIdMap.entrySet().iterator();
 		while (itRelationships.hasNext()) {
 			Entry<String,RelationshipElementGraphicalModel> pairs =
 					(Entry<String,RelationshipElementGraphicalModel>)itRelationships.next();
 			if(pairs.getValue().equals(element)){
 				return pairs.getKey();
 			}
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * Finds the element graphical model associated with the specified id, or null if an
 	 * element cannot be found.
 	 */
 	public IElementGraphicalModel lookupGraphicalModelById(String id){
 		
 		IElementGraphicalModel c = mClassIdMap.get(id);
 		return (c != null ? c : mRelationshipIdMap.get(id));
 	}
 	
 	
 	/**
 	 * Updates the entire diagram after changes have been made to individual elements.
 	 */
 	public void update(){
 		mDiagramGraph.update();
 		mListener.eventOccured(ClassDiagramEditPart.EVENT_REFRESH_REQUIRED);
 	}
 	
 	
 	
 	// PRIVATE METHODS ------------------------------
 	
 	/*
 	 * Lays out the classes and relationships using the JGraphX library.
 	 */
 	private void layout(){
 		
 		mxGraph graph = new mxGraph();
 		mxHierarchicalLayout layout = new mxHierarchicalLayout(graph);
 		Object defaultParent = graph.getDefaultParent();
 		Map<String,Object> vertexIdMap = new HashMap<String,Object>();
 		Map<String,Object> edgeIdMap = new HashMap<String,Object>();
 		
 		graph.getModel().beginUpdate();
 		try{
 			Iterator<Entry<String,RelationshipElementGraphicalModel>> itRelationships =
 					mRelationshipIdMap.entrySet().iterator();
 			while (itRelationships.hasNext()) {
 				Entry<String,RelationshipElementGraphicalModel> pairs =
 						(Entry<String,RelationshipElementGraphicalModel>)itRelationships.next();
 				
 				// get head and tail classes for each relationship
 				RelationshipElementGraphicalModel rel = pairs.getValue();
 				String relId = pairs.getKey();
 				String headId = rel.getRelationshipElement().getHeadId();
 				String tailId = rel.getRelationshipElement().getTailId();
 				IElementGraphicalModel head = lookupGraphicalModelById(headId);
 				IElementGraphicalModel tail = lookupGraphicalModelById(tailId);
 				
 				// add head and tail classes to layout graph
 				Object headVertex;
 				Object tailVertex;
 				if(vertexIdMap.containsKey(headId)){
 					headVertex = vertexIdMap.get(headId);
 				}
 				else{
 					headVertex = graph.insertVertex(defaultParent, null, headId,
 							head.getPosition().x, head.getPosition().y, 
 							head.getSize().width, head.getSize().height);
 					vertexIdMap.put(headId, headVertex);
 				}
 				
 				if(vertexIdMap.containsKey(tailId)){
 					tailVertex = vertexIdMap.get(tailId);
 				}
 				else{
 					tailVertex = graph.insertVertex(defaultParent, null, tailId,
 							tail.getPosition().x, tail.getPosition().y, 
 							tail.getSize().width, tail.getSize().height);
 					vertexIdMap.put(tailId, tailVertex);
 				}
 				Object edge = graph.insertEdge(defaultParent, null, relId, headVertex, tailVertex);
 				edgeIdMap.put(relId, edge);
 			}
 			
 			// add remaining classes to layout graph
 			Iterator<Entry<String,ClassElementGraphicalModel>> itClasses =
 					mClassIdMap.entrySet().iterator();
 			while (itClasses.hasNext()) {
 				Entry<String,ClassElementGraphicalModel> pairs =
 						(Entry<String,ClassElementGraphicalModel>)itClasses.next();
 				String id = pairs.getKey();
 				ClassElementGraphicalModel c = pairs.getValue();
 				if(!vertexIdMap.containsKey(id)){
 					Object v = graph.insertVertex(defaultParent, null, id,
 							c.getPosition().x, c.getPosition().y, 
 							c.getSize().width, c.getSize().height);
 					vertexIdMap.put(id, v);
 				}
 			}
 			
 			layout.execute(graph.getDefaultParent());
 		}
 		finally{
 			graph.getModel().endUpdate();
 		}
 		
 		// get new position of each class and find its children for each relationship
 		Iterator<Entry<String,Object>> itVertices = vertexIdMap.entrySet().iterator();
 		while (itVertices.hasNext()) {
 			Entry<String,Object> pairs = (Entry<String,Object>)itVertices.next();
 			IElementGraphicalModel gm = lookupGraphicalModelById(pairs.getKey());
 			mxGeometry geom = graph.getModel().getGeometry(pairs.getValue());
 			gm.setPosition(new Point((int)geom.getX(), (int)geom.getY()));
 			mElements.add(gm);
 		}
 	}
 	
 	// initializes the edges in the graphical model
 	private void initEdges(){
 		
 		// maps of each class to its children for joining relationship arcs with hypervertices
 		Map<String, ArrayList<String>> aggregationChildren = new HashMap<String, ArrayList<String>>();
 		Map<String, ArrayList<String>> associationChildren = new HashMap<String, ArrayList<String>>();
 		Map<String, ArrayList<String>> realizationChildren = new HashMap<String, ArrayList<String>>();
 		Map<String, ArrayList<String>> compositionChildren = new HashMap<String, ArrayList<String>>();
 		Map<String, ArrayList<String>> dependencyChildren = new HashMap<String, ArrayList<String>>();
 		Map<String, ArrayList<String>> dirAssociationChildren = new HashMap<String, ArrayList<String>>();
 		Map<String, ArrayList<String>> generalizationChildren = new HashMap<String, ArrayList<String>>();
 		
 		
 		Iterator<Entry<String,UmlRelationshipElement>> itRel =
 				mClassModel.getRelationships().entrySet().iterator();
 		while (itRel.hasNext()) {
 			Entry<String,UmlRelationshipElement> pairs = (Entry<String,UmlRelationshipElement>)itRel.next();
 			UmlRelationshipElement rel = pairs.getValue();
 			String headId = rel.getHeadId();
 			String tailId = rel.getTailId();
 			
 			// get the children of each relationship type for each class
 			Map<String, ArrayList<String>> relMap = null;
 			switch(rel.getType()){
 			case AGGREGATION:
 				relMap = aggregationChildren;
 				break;
 			case ASSOCIATION:
 				relMap = associationChildren;
 				break;
 			case DIRECTEDASSOCIATION:
 				relMap = dirAssociationChildren;
 				break;
 			case COMPOSITION:
 				relMap = compositionChildren;
 				break;
 			case DEPENDENCY:
 				relMap = dependencyChildren;
 				break;
 			case REALIZATION:
 				relMap = realizationChildren;
 				break;
 			case GENERALIZATION:
 				relMap = generalizationChildren;
 				break;
 			}
 			
 			if(relMap.containsKey(headId)){
 				relMap.get(headId).add(tailId);
 			}
 			else{
 				ArrayList<String> children = new ArrayList<String>();
 				children.add(tailId);
 				relMap.put(headId, children);
 			}
 			
 		}
 		
 		// insert hypervertices for each edge
 		layoutEdges(aggregationChildren, RelationshipType.AGGREGATION);
 		layoutEdges(associationChildren, RelationshipType.ASSOCIATION);
 		layoutEdges(realizationChildren, RelationshipType.REALIZATION);
 		layoutEdges(compositionChildren, RelationshipType.COMPOSITION);
 		layoutEdges(dependencyChildren, RelationshipType.DEPENDENCY);
 		layoutEdges(dirAssociationChildren, RelationshipType.DIRECTEDASSOCIATION);
 		layoutEdges(generalizationChildren, RelationshipType.GENERALIZATION);
 	}
 	
 	// inserts hyperedges for classes with many children
 	private void layoutEdges(Map<String, ArrayList<String>> childrenIdMap, RelationshipType relType){
 		Iterator<Entry<String, ArrayList<String>>> itChildren = childrenIdMap.entrySet().iterator();
 		while (itChildren.hasNext()) {
 			Entry<String, ArrayList<String>> pairs =
 					(Entry<String, ArrayList<String>>)itChildren.next();
 			String headId = pairs.getKey();
 			ArrayList<String> children = pairs.getValue();
 			
 			
 			if(children.size() >= NUM_CLASSES_FOR_HYPEREDGES){
 				
 				RelationshipType hyperRelType = null;
 				switch(relType){
 				case AGGREGATION:
 					hyperRelType = RelationshipType.HYPER_AGGREGATION;
 					break;
 				case ASSOCIATION:
 					hyperRelType = RelationshipType.HYPER_ASSOCIATION;
 					break;
 				case DIRECTEDASSOCIATION:
 					hyperRelType = RelationshipType.HYPER_DIRECTEDASSOCIATION;
 					break;
 				case COMPOSITION:
 					hyperRelType = RelationshipType.HYPER_COMPOSITION;
 					break;
 				case DEPENDENCY:
 					hyperRelType = RelationshipType.HYPER_DEPENDENCY;
 					break;
 				case REALIZATION:
 					hyperRelType = RelationshipType.HYPER_REALIZATION;
 					break;
 				case GENERALIZATION:
 					hyperRelType = RelationshipType.HYPER_GENERALIZATION;
 					break;
 				}
 				
 				ClassElementGraphicalModel headElement = (ClassElementGraphicalModel)lookupGraphicalModelById(headId);
 				String hyperClassId = "HYPER-" + headId + relType.toString();
 				ClassElementGraphicalModel hyperClass = new ClassElementGraphicalModel(null, null);
 				hyperClass.setIsHyper(true);
 				mDiagramGraph.addElement(hyperClass);
 				mClassIdMap.put(hyperClassId, hyperClass);
 				mElements.add(hyperClass);
 				
 				
 				for(String tailId : children){
					try{
						UmlRelationshipElement newRel = new UmlRelationshipElement(null, headId, hyperClassId, relType);
						UmlRelationshipElement newHyperRel = new UmlRelationshipElement(null, hyperClassId, tailId, hyperRelType);
						RelationshipElementGraphicalModel r = new RelationshipElementGraphicalModel(newRel);
						RelationshipElementGraphicalModel hr = new RelationshipElementGraphicalModel(newHyperRel);
						mElements.add(r);
						mElements.add(hr);
						mRelationshipIdMap.put("EDGE-" + headId + ":" + hyperClassId, r);
						mRelationshipIdMap.put("EDGE-" + hyperClassId + ":" + tailId, hr);
						mDiagramGraph.addRelationship(r, hyperClass, headElement);
						mDiagramGraph.addRelationship(hr, mClassIdMap.get(tailId), hyperClass);
					}
					catch(RuntimeException rte){
						// TODO figure out why duplicate relationships are being added to graph
					}
 				}
				
 			}
 			else{
 				for(String tailId : children){
 					UmlRelationshipElement newRel = new UmlRelationshipElement(null, headId, tailId, relType);
 					RelationshipElementGraphicalModel r = new RelationshipElementGraphicalModel(newRel);
 					mElements.add(r);
 					mRelationshipIdMap.put(pairs.getKey(), r);
 					mDiagramGraph.addRelationship(r, mClassIdMap.get(tailId), mClassIdMap.get(headId));
 				}
 			}
 		}
 	}
 }
