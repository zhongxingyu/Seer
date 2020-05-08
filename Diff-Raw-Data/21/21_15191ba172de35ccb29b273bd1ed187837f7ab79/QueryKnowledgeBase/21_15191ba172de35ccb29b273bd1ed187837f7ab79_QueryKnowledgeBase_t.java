 package knowledgeBase;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.hp.hpl.jena.ontology.OntModel;
 import com.hp.hpl.jena.query.Query;
 import com.hp.hpl.jena.query.QueryExecution;
 import com.hp.hpl.jena.query.QueryExecutionFactory;
 import com.hp.hpl.jena.query.QueryFactory;
 import com.hp.hpl.jena.query.QuerySolution;
 import com.hp.hpl.jena.query.ResultSet;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.Resource;
 
 import edu.uci.ics.jung.graph.Graph;
 import edu.uci.ics.jung.graph.SparseMultigraph;
 
 public class QueryKnowledgeBase {
 
 	String fileName;
 	Map<String, KBMachine> machineMap = new HashMap<String, KBMachine>(); 
 	Graph<String, String> graph = new SparseMultigraph<String, String>();
 	
 	public QueryKnowledgeBase(String filename) {
 		this.fileName = filename;
 		computeGraph();
 	}
 
 	public Map<String, List<String>> getLinks() {
 
 		Map<String, List<String>> h = new HashMap<String, List<String>>();
 
 		Graph<String, String> g = graph;
 		List<String> vertices = new ArrayList<String>(g.getVertices());
 
 		for(int i = 0; i < vertices.size(); i++) {
 			String nom = vertices.get(i);
 			List<String> connections = new ArrayList<String>(g.getSuccessors(nom));
 			h.put(nom, connections);
 		}
 
 		return h;
 	}
 
 	public Map<String, KBMachine> getMap() {
 		return machineMap;
 	}	
 	
 
 	public Graph<String, String> computeGraph() {
 		graph = new SparseMultigraph<String, String>();
 
 		OntModel model = ModelFactory.createOntologyModel();
 		model.read("file:"+fileName, null, "N3");
 
		Query qu = QueryFactory.read("requetes/requete_base_sur.rq");
 
 		QueryExecution qexec = QueryExecutionFactory.create(qu, model) ;
 		ResultSet results = qexec.execSelect() ;
 
 		int i = 0;
 		while(results.hasNext()){
 			QuerySolution sol = results.next() ;
 			Resource property = sol.getResource("machine");
 			Resource property2 = sol.getResource("autre_machine") ;			
 
 			String src = property.getLocalName();
 			String dest = property2.getLocalName();
 
 			if(!graph.containsVertex(src)) {
 				graph.addVertex(src);
 				machineMap.put(src, new KBMachine(src));
 			}
 			
 			if(!graph.containsVertex(dest)) {
 				graph.addVertex(dest);
 				machineMap.put(dest, new KBMachine(dest));
 			}
 
 			graph.addEdge("Edge" + i, src, dest);
 			i++;
 			//System.out.println(property.getLocalName() + " -> " + property2.getLocalName());
 		}
 
 		qexec.close() ;
 
		qu = QueryFactory.read("requetes/requete_type_agent.rq");
 
 		qexec = QueryExecutionFactory.create(qu, model) ;
 		results = qexec.execSelect() ;
 
 		i = 0;
 		while(results.hasNext()){
 			QuerySolution sol = results.next() ;
 			Resource property = sol.getResource("machine");
 			Resource property2 = sol.getResource("type") ;
			//Resource pty3 = sol.getResource("locuteur") ;
 
 			String mach = property.getLocalName();
 			String type = property2.getLocalName();
			//String locuteur = pty3.getLocalName();
 			
 			if(type.equals("machine")) {
 				machineMap.get(mach).machineType = KBMachine.type.SWITCH;
 			} else {
 				machineMap.get(mach).machineType =  KBMachine.type.USER;			
				//machineMap.get(mach).interlocuteur = locuteur;
 			}
 			i++;
 			//System.out.println(property.getLocalName() + " -> " + property2.getLocalName());
 		}
 
 		qexec.close() ;
 		return graph;
 	}
 
 
 	public static void main(String[] agrs) {
 		QueryKnowledgeBase q = new QueryKnowledgeBase("./network.n3");
 		//q.getGraph();
 		System.out.println(q.getLinks());
 	}
 
 	public Graph<String, String> getGraph() {
 		return graph;
 	}
 
 	public Map<String, KBMachine> getMachineMap() {
 		return machineMap;
 	}
 
 }
