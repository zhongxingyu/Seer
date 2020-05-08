 package edu.utep.cybershare.DerivAUI.components;
 
 import java.util.ArrayList;
 import java.util.Vector;
 
 import java.util.HashMap;
 
 import com.hp.hpl.jena.query.QuerySolution;
 import com.hp.hpl.jena.query.ResultSet;
 import com.hp.hpl.jena.query.ResultSetFactory;
 
 import edu.utep.cybershare.DerivAUI.components.IndividualList.Individual;
 
 public class InferenceRulesComboBox extends IndividualComboBox {
 	
 	private static final long serialVersionUID = 1L;
 	private HashMap<String,Integer> prettyNames = new HashMap<String,Integer>();
 	
 	private String Ontology = null;
 	
 	public void setOntology(String ont) {
 		Ontology = ont;
 	}
 	
 	public InferenceRulesComboBox() {
 		super();
 		queryInferenceRules();
 	}
 	
 	public static String stripURI(String formatURI)
 	{
 		int start = formatURI.indexOf('#') + 1;
 		String name = formatURI.substring(start);
 		
 		return name;
 	}
 
 	public ArrayList<String> queryInferenceRulesByWorkflow(String conclusionTypeURI, String workflow){
 		Vector<Individual> individuals = new Vector<Individual>();
 
		String getInferenceRuleQuery = "SELECT DISTINCT ?methodRule ?methodName " + 
 				"WHERE { " +
 				"?data <http://www.w3.org/2000/01/rdf-schema#subClassOf> <" + conclusionTypeURI + "> . " + 
				"?data <http://trust.utep.edu/2.0/wdo.owl#isInputTo> ?method . " +
				"?method <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?methodRule . " + 
				"?methodRule <http://www.w3.org/2000/01/rdf-schema#label> ?methodName . " +
				"FILTER regex(str(?methodRule),\"http://rio.cs.utep.edu/ciserver/ciprojects/wdo/.*\", \"i\")" +
 				"}";
 
 		edu.utep.trust.provenance.RDFStore_Service service = new edu.utep.trust.provenance.RDFStore_Service();
 		edu.utep.trust.provenance.RDFStore proxy = service.getRDFStoreHttpPort();
 		
 		String qResult = proxy.doQuery(getInferenceRuleQuery);
 		ResultSet rSet = ResultSetFactory.fromXML(qResult);
 
 		//		System.out.println(qResult);
 
 		ArrayList<String> URIs = new ArrayList<String>();
 		String inferenceRuleLabel, inferenceRuleURI;
 
 		if(rSet != null)
 			while(rSet.hasNext()){
 
 				QuerySolution QS = rSet.nextSolution();
				inferenceRuleURI = QS.get("methodRule").toString();
 				URIs.add(inferenceRuleURI);
				inferenceRuleLabel = QS.get("methodName").toString();
 
 				if(inferenceRuleLabel.contains("@")){
 					inferenceRuleLabel = inferenceRuleLabel.substring(0, inferenceRuleLabel.indexOf('@'));
 				}
 
 				individuals.add(new Individual(inferenceRuleURI, inferenceRuleLabel, inferenceRuleURI));
 
 			}
 
 		this.setIndividuals(individuals);
 		return URIs;
 	}
 	
 	public void queryInferenceRulesByInferenceEngine(String inferenceEngine) {
 		Vector<Individual> individuals = new Vector<Individual>();
 
 		edu.utep.trust.provenance.RDFStore_Service service = new edu.utep.trust.provenance.RDFStore_Service();
 		edu.utep.trust.provenance.RDFStore proxy = service.getRDFStoreHttpPort();
 		//		String pml_j = proxy.getNodeSetsForProject(project);
 		//		ResultSet results = ResultSetFactory.fromXML(pml_j);
 
 		
 		String rules = "", query = "";
 		query = "SELECT DISTINCT ?inferenceRule ?ruleLabel ?ruleClass " +
 				"WHERE {" +
 				"?inferenceRule <http://www.w3.org/2000/01/rdf-schema#subClassOf> <http://inference-web.org/2.0/pml-provenance.owl#MethodRule> . " +
 				"<" + inferenceEngine + "> <http://inference-web.org/2.0/pml-provenance.owl#hasInferenceEngineRule> ?inferenceRule . " +
 				"?inferenceRule <http://www.w3.org/2000/01/rdf-schema#label> ?ruleLabel . " +
 				"?inferenceRule a ?ruleClass . " +
 				"FILTER (! regex(str(?ruleClass), \".*Thing.*\",\"i\")) . " +
 				"FILTER (! regex(str(?ruleClass), \".*Rule.*\",\"i\")) . }" +
 				"ORDER BY ?ruleLabel";
 
 		rules = proxy.doQuery(query);
 
 //		System.out.println(query);
 //		System.out.println(rules);
 
 		ResultSet results = ResultSetFactory.fromXML(rules);
 
 
 		String rule, IRClass, IRName;
 
 		individuals.add(new Individual("Choose Inference Rule", " -- Choose Inference Rule -- ", "Choose Inference Rule"));
 
 		// try web service and update local store	
 		if(results != null)
 			while(results.hasNext())
 			{
 				QuerySolution QS = results.nextSolution();
 
 				rule = QS.get("?inferenceRule").toString();
 				IRName = QS.get("?ruleLabel").toString();
 				IRClass = QS.get("?ruleClass").toString();
 
 				String prettyName = IRName;
 
 				if(!IRClass.substring(IRClass.lastIndexOf('/') + 1).equalsIgnoreCase("rdf-schema#Class")){
 					rule = null;
 				}
 
 				if(prettyName.contains("@")){
 					prettyName = prettyName.substring(0, prettyName.indexOf('@'));
 				}
 
 				if(prettyName.startsWith("\"")){
 					prettyName = prettyName.substring(1);
 				}
 
 				//				if(prettyName.equalsIgnoreCase("null") || prettyName.equalsIgnoreCase(""))
 				//					prettyName = stripURI(rule);
 
 				if(rule != null && prettyName != null)
 				{
 					individuals.add(new Individual(rule, prettyName, rule));
 				}
 
 				//			System.out.println(results.nextSolution().get("?x").toString());
 			}
 		// if web service not reached, populate from local store
 
 		this.setIndividuals(individuals);
 	}
 	
 	public void queryInferenceRules() {
 		Vector<Individual> individuals = new Vector<Individual>();
 
 		edu.utep.trust.provenance.RDFStore_Service service = new edu.utep.trust.provenance.RDFStore_Service();
 		edu.utep.trust.provenance.RDFStore proxy = service.getRDFStoreHttpPort();
 
 		String rules = "", query = "";
 		if(Ontology == null){
 			query = "SELECT DISTINCT ?inferenceRule ?ruleLabel ?ruleClass " +
 					"WHERE {?inferenceRule <http://www.w3.org/2000/01/rdf-schema#subClassOf> " +
 					"<http://inference-web.org/2.0/pml-provenance.owl#MethodRule> . " +
 					"?inferenceRule <http://www.w3.org/2000/01/rdf-schema#label> ?ruleLabel . " +
 					"?inferenceRule a ?ruleClass . " +
 					"FILTER (! regex(str(?ruleClass), \".*Thing.*\",\"i\")) . " +
 					"FILTER (! regex(str(?ruleClass), \".*Rule.*\",\"i\")) .}" +
 					"ORDER BY ?ruleLabel";
 			
 		}else{			
 			query = "SELECT DISTINCT ?inferenceRule ?ruleLabel ?ruleClass " +
 					"WHERE {" +
 					"?inferenceRule <http://www.w3.org/2000/01/rdf-schema#subClassOf> <http://inference-web.org/2.0/pml-provenance.owl#MethodRule> . " +
 					"?inferenceRule <http://www.w3.org/2000/01/rdf-schema#label> ?ruleLabel . " +
 					"?inferenceRule a ?ruleClass . " +
 					"FILTER (! regex(str(?ruleClass), \".*Thing.*\",\"i\")) . " +
 					"FILTER (! regex(str(?ruleClass), \".*Rule.*\",\"i\")) ." +
 					"FILTER regex(str(?inferenceRule),\"" + Ontology + ".*\", \"i\") .}" +
 					"ORDER BY ?ruleLabel";
 		}
 		
 		rules = proxy.doQuery(query);
 		//rules = proxy.getInferenceRulesWithInfo();
 		ResultSet results = ResultSetFactory.fromXML(rules);
 		
 //		System.out.println(rules);
 		
 		String rule, IRClass, IRName;
 
 		individuals.add(new Individual("Choose Inference Rule", " -- Choose Inference Rule -- ", "Choose Inference Rule"));
 		
 		// try web service and update local store	
 		if(results != null)
 			while(results.hasNext())
 			{
 				QuerySolution QS = results.nextSolution();
 				
 				rule = QS.get("?inferenceRule").toString();
 				IRName = QS.get("?ruleLabel").toString();
 				IRClass = QS.get("?ruleClass").toString();
 //				String prettyName = IRClass.substring(IRClass.lastIndexOf('#') + 1) + " : " + IRName;
 				String prettyName = IRName;
 				
 //				System.out.println(prettyName);
 				
 				if(IRClass.substring(IRClass.lastIndexOf('#') + 1).equalsIgnoreCase("resource")){
 					rule = null;
 				}
 				
 				if(prettyName.contains("@")){
 					prettyName = prettyName.substring(0, prettyName.indexOf('@'));
 				}
 				
 				if(prettyName.startsWith("\"")){
 					prettyName = prettyName.substring(1);
 				}
 				
 //				if(prettyName.equalsIgnoreCase("null") || prettyName.equalsIgnoreCase(""))
 //					prettyName = stripURI(rule);
 
 				if(rule != null && prettyName != null)
 				{
 					individuals.add(new Individual(rule, prettyName, rule));
 				}
 
 				//			System.out.println(results.nextSolution().get("?x").toString());
 			}
 		// if web service not reached, populate from local store
 
 		this.setIndividuals(individuals);
 	}
 }
