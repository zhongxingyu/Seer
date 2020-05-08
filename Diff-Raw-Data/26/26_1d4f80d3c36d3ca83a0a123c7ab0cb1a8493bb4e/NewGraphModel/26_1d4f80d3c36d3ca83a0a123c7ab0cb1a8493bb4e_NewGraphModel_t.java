 package au.uq.dke.comonviz;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import au.uq.dke.comonviz.graph.node.DefaultGraphNode;
 import au.uq.dke.comonviz.model.DatabaseModelListener;
 import ca.uvic.cs.chisel.cajun.graph.DefaultGraphModel;
 import ca.uvic.cs.chisel.cajun.graph.arc.GraphArc;
 import ca.uvic.cs.chisel.cajun.graph.node.GraphNode;
 import database.model.ontology.OntologyClass;
 import database.model.ontology.OntologyRelationship;
 import database.service.OntologyAxiomService;
 import database.service.OntologyClassService;
 import database.service.OntologyRelationshipService;
 
 public class NewGraphModel extends DefaultGraphModel {
 
 	DatabaseModelListener databaseModelListener = new DatabaseModelListener() {
 
 		@Override
 		public void databaseCleared() {
 			// TODO Auto-generated method stub
 			super.databaseCleared();
 		}
 
 		@Override
 		public void databaseRelationshipAdded(OntologyRelationship relationship) {
 			// TODO Auto-generated method stub
 			super.databaseRelationshipAdded(relationship);
			NewGraphModel.this.createArc(relationship);
 		}
 
 		@Override
 		public void databaseRelationshipUpdated(
 				OntologyRelationship relationship) {
 			// TODO Auto-generated method stub
 			super.databaseRelationshipUpdated(relationship);
			//NewGraphModel.this.u(relationship);
 		}
 
 		@Override
 		public void databaseRelationshipRemoved(
 				OntologyRelationship relationship) {
 			// TODO Auto-generated method stub
 			super.databaseRelationshipRemoved(relationship);
			NewGraphModel.this.removeArc(relationship);
 		}
 
 		@Override
 		public void databaseClassAdded(OntologyClass cls) {
 			// TODO Auto-generated method stub
 			super.databaseClassAdded(cls);
			NewGraphModel.this.createNode(cls);
	}
 
 		@Override
 		public void databaseClassUpdated(OntologyClass cls) {
 			// TODO Auto-generated method stub
 			super.databaseClassUpdated(cls);
			NewGraphModel.this.updateNode(cls);
 		}
 
 		@Override
 		public void databaseClassRemoved(OntologyClass cls) {
 			// TODO Auto-generated method stub
 			super.databaseClassRemoved(cls);
			NewGraphModel.this.removeNode(cls);
 		}
 
 		@Override
 		public void databaseAxiomAdded(Object axiom) {
 			// TODO Auto-generated method stub
 			super.databaseAxiomAdded(axiom);
 		}
 
 		@Override
 		public void databaseAxiomUpdated(Object axiom) {
 			// TODO Auto-generated method stub
 			super.databaseAxiomUpdated(axiom);
 		}
 
 		@Override
 		public void databaseAxiomRemoved(Object axiom) {
 			// TODO Auto-generated method stub
 			super.databaseAxiomRemoved(axiom);
 		}
 
 	};
 	EntryPoint entryPoint = new EntryPoint();
 	private static OntologyClassService ontologyClassService = EntryPoint
 			.getOntologyClassService();
 	private static OntologyAxiomService ontologyAxiomService = EntryPoint
 			.getOntologyAxiomService();
 	private static OntologyRelationshipService ontologyRelationshipService = EntryPoint
 			.getOntologyRelationshipService();
 
 	private Map<Object, GraphNode> nodes;
 
 	private Map<Object, GraphArc> arcs;
 
 	public NewGraphModel() {
 		this.nodes = super.getNodes();
 		this.arcs = super.getArcs();
 	}
 
//CRUD of node	
 	// tested
 	private GraphNode findGraphNode(OntologyClass ontologyClass) {
 		for (Map.Entry<Object, GraphNode> entry : nodes.entrySet()) {
 			if (((OntologyClass) entry.getKey()).getId() == ontologyClass
 					.getId()) {
 				return entry.getValue();
 			}
 
 		}
 
 		return null;
 	}
 	// tested
 	private void createNodes() {
 
 		List<OntologyClass> ontologyClassList = this.ontologyClassService
 				.findAll();
 		for (OntologyClass ontologyClass : ontologyClassList) {
 			super.addNode(ontologyClass, ontologyClass.getName(), null);
 		}
 	}
 	
 	private void createNode(OntologyClass ontologyClass){
 		super.addNode(ontologyClass, ontologyClass.getName(), null);
 	}
 	
 	private void removeNode(OntologyClass ontologyClass){
 		GraphNode graphNode = this.findGraphNode(ontologyClass);
 		if(graphNode != null){
 			super.removeArc(graphNode.getUserObject());
 		}
 	}
 	
 	private void updateNode(OntologyClass ontologyClass){
 		DefaultGraphNode graphNode = (DefaultGraphNode) this.findGraphNode(ontologyClass);
 		if(graphNode != null){
 			graphNode.setName(ontologyClass.getName());
 		}
 		
 	}
 
//CRUD of arc
 	
 	// tested
 	private GraphArc findGraphArc(OntologyRelationship ontologyRelationship) {
 		for (Map.Entry<Object, GraphArc> entry : arcs.entrySet()) {
 			if (((OntologyRelationship) entry.getKey()).getId() == ontologyRelationship
 					.getId()) {
 				return entry.getValue();
 			}
 
 		}
 
 		return null;
 	}
 
 	private void createArcs() {
 
 		List<OntologyRelationship> ontologyRelationshipList = this.ontologyRelationshipService
 				.findAll();
 		for (OntologyRelationship ontologyRelationship : ontologyRelationshipList) {
 			createArc(ontologyRelationship);
 		}
 
 	}
 
 	private void createArc(OntologyRelationship ontologyRelationship) {
 		OntologyClass sourceOntologyClass = this.ontologyRelationshipService
 				.findSourceOntologyClass(ontologyRelationship);
 		OntologyClass destinationOntologyClass = this.ontologyRelationshipService
 				.findDestinationOntologyClass(ontologyRelationship);
 		GraphNode sourceNode = findGraphNode(sourceOntologyClass);
 		GraphNode destinationNode = findGraphNode(destinationOntologyClass);
 		super.addArc(ontologyRelationship, sourceNode, destinationNode, null);
 
 	}
 	private void removeArc(OntologyRelationship ontologyRelationship) {
 		GraphArc arc = this.findGraphArc(ontologyRelationship);
 		if(arc != null){
 			super.removeArc(arc.getUserObject());
 		}
 	}
 
/*CRUD of arcTypes, to be added or don't need?
 * since arcTypes are updated by tranverse all arcs and get their types
 * for database, axiom crud is needed, but for model, we may not need it
*/
	
	
	
 
 	private List<GraphNode> getChildren(GraphNode graphNode) {
 		List<OntologyClass> relationSrcClassList = this.ontologyRelationshipService
 				.findChildren((OntologyClass) graphNode.getUserObject());
 		List<GraphNode> graphNodeList = getGraphNodesFromClasses(relationSrcClassList);
 
 		return graphNodeList;
 	}
 
 	private List<GraphNode> getRelationSrcNodes(GraphNode graphNode) {
 		List<OntologyClass> relationSrcClassList = this.ontologyRelationshipService
 				.findRelSrcNeighbourClasses((OntologyClass) graphNode
 						.getUserObject());
 		List<GraphNode> graphNodeList = getGraphNodesFromClasses(relationSrcClassList);
 		return graphNodeList;
 	}
 
 	private List<GraphNode> getRelationDestNodes(GraphNode graphNode) {
 		List<OntologyClass> relationDestClassList = this.ontologyRelationshipService
 				.findRelDestNeighbourClasses((OntologyClass) graphNode
 						.getUserObject());
 		List<GraphNode> graphNodeList = getGraphNodesFromClasses(relationDestClassList);
 		return graphNodeList;
 	}
 
 	private List<GraphNode> getDesendants(GraphNode graphNode) {
 		List<OntologyClass> desendantsClassList = this.ontologyRelationshipService
 				.findDesendants((OntologyClass) graphNode.getUserObject());
 		List<GraphNode> graphNodeList = getGraphNodesFromClasses(desendantsClassList);
 		return graphNodeList;
 	}
 
 	private List<GraphNode> getGraphNodesFromClasses(
 			List<OntologyClass> classList) {
 		List<GraphNode> graphNodeList = new ArrayList<GraphNode>();
 		for (OntologyClass cls : classList) {
 			GraphNode node = this.findGraphNode(cls);
 			graphNodeList.add(node);
 		}
 		return graphNodeList;
 
 	}
 
 	public void test() {
 		createNodes();
 		createArcs();
 		return;
 	}
 
 	public static void main(String args[]) {
 		NewGraphModel ngm = new NewGraphModel();
 		ngm.test();
 
 	}
 
 }
