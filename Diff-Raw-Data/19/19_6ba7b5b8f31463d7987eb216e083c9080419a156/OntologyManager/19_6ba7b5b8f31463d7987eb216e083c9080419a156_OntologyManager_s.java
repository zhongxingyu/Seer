 package com.admin.owlmanager;
 
 import java.util.ArrayList;
 
 import com.admin.config.ConfigManager;
 import com.admin.domain.Triplet;
 import com.hp.hpl.jena.query.QuerySolution;
 import com.hp.hpl.jena.query.ResultSet;
 import com.hp.hpl.jena.rdf.model.RDFNode;
 
 
 /**
  * This class manages all that has to do with the ontology
  * 
  * @author israelord
  */
 public class OntologyManager {
 	
 	public void addTriplet() {
 		
 	}
 
 	public void deleteTriplet() {
 		
 	}
 	
 	public boolean deleteOntology(String graphName) {
 		return VirtuosoActor.getTheInstance().deleteGraph(graphName);
 	}
 	
 	public boolean loadOntology() {
 		OWLActor reasoner = new OWLActor();
 		ConfigManager cm = ConfigManager.getInstance();
 		String sourcePath = cm.getProperty("baseModelPath");
 		String targetPath = cm.getProperty("reasonedModelPath");
 		String graphName = cm.getProperty("graphName");
 		String graphLabel = cm.getProperty("graphLabel");
 		
 		boolean result = reasoner.generateReasonedModel(sourcePath, targetPath);
 		result = result && VirtuosoActor.getTheInstance().toVirtuoso(targetPath, graphName, graphLabel);
 
 		return result;
 	}
 	
 	
 	private ArrayList<Triplet> populate(ResultSet res) {
 		ArrayList<Triplet> result = new ArrayList<Triplet>();
 		
 		while (res.hasNext()) {
 			QuerySolution qs = res.nextSolution();
 			RDFNode subject = qs.get("s");
 			RDFNode predicate = qs.get("p");
 			RDFNode object = qs.get("o");
 			
 			Triplet t = new Triplet(subject.toString(), predicate.toString(), object.toString());
 			result.add(t);
 		}
 		
 		return result;
 	}
 	
 	public ArrayList<Triplet> getModelAsString() {
 		ResultSet res = VirtuosoActor.getTheInstance()
 							.getAllModel(ConfigManager
 							.getInstance().getProperty("graphName"));
 		
 		ArrayList<Triplet> result = this.populate(res);
 		
 		return result;
 	}
 	
 	public void getTriplets() {
 		
 	}
 	
	public void getClasses() {
 		
 	}
 	
 	public void addResource() {
 		
 	}
 	
 	public void removeResource() {
 		
 	}
 }
