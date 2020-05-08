 package sim.server.compund;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.ontoware.aifbcommons.collection.ClosableIterator;
 import org.ontoware.rdf2go.model.QueryRow;
 import org.ontoware.rdf2go.model.node.DatatypeLiteral;
 import org.ontoware.rdf2go.model.node.URI;
 import org.ontoware.rdf2go.model.node.impl.URIImpl;
 
 import sim.server.RdfDatabase;
 import sim.server.data.CompoundMetric;
 import sim.server.data.CompoundMetricImpl;
 import sim.server.data.Metric;
 
 
 public class CompundMetricsGenerator {
 
 	public static final URI COUNT = new URIImpl("http://www.larkc.eu/ontologies/IMOntology.rdf#Count");
 	public static final URI AVERAGE = new URIImpl("http://www.larkc.eu/ontologies/IMOntology.rdf#Average");
 	public static final URI SUM = new URIImpl("http://www.larkc.eu/ontologies/IMOntology.rdf#Sum");
 	
 	private String queryPrefixes = 			
 		"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " +
 		"PREFIX owl:<http://www.w3.org/2002/07/owl#> " +
 		"PREFIX xsd:<http://www.w3.org/2001/XMLSchema#> " +
 		"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
 		"PREFIX sim:<http://www.larkc.eu/ontologies/IMOntology.rdf#> " +
 		"PREFIX skos:<http://www.w3.org/2004/02/skos/core#> " +
 		"PREFIX xml:<http://www.w3.org/XML/1998/namespace> ";
 
 	private RdfDatabase rdfDatabase;
 	private HashMap<URI, CompoundMetric> compoundMetricsDefinition;
 	
 	public CompundMetricsGenerator(RdfDatabase rdfDatabase){
 		this.rdfDatabase = rdfDatabase;
 		compoundMetricsDefinition = new HashMap<URI, CompoundMetric>();
 		
 		rdfDatabase.open();
 		readSchemaCompundMetrics();
 		rdfDatabase.close();
 	}
 	
 	private void readSchemaCompundMetrics(){		
 		String queryString = queryPrefixes +
 			"Select distinct ?a ?b "+
 			"Where { "+
 				"?c ?d sim:hasAggregationFunction. "+ 
 				"?c owl:allValuesFrom ?b. "+
 				"?a rdfs:subClassOf ?c. "+
 				"?a rdfs:subClassOf sim:CompoundMetric.}"; 
 
 		ClosableIterator<QueryRow> cr = rdfDatabase.sparqlSelect(queryString).iterator();
 		while(cr.hasNext()){
 			QueryRow qr = cr.next();
 			URI type = qr.getValue("a").asURI();
 			URI aggregationFunction = qr.getValue("b").asURI(); 
 			CompoundMetric compoundMetric = new CompoundMetricImpl(type, aggregationFunction);
 			compoundMetricsDefinition.put(type,compoundMetric);
 		}		
 	}
 
 	public int countCompoundMetrics(){	
 		return compoundMetricsDefinition.size();
 	}
 	
 	public CompoundMetric generateCompundMetric(URI type, List<Metric> metrics){
 		float value = 0;
 		CompoundMetric compoundMetric = compoundMetricsDefinition.get(type);
 		for(Metric m:metrics)
 			compoundMetric.addConstituentMetric(m);
 		
 		if(compoundMetric.getAggregationFunction().equals(COUNT)){
 			value = metrics.size();
 		} 
 		if (compoundMetric.getAggregationFunction().equals(SUM)){
 			float sum = 0;
 			for(Metric m:metrics)
 				sum +=m.getValue();
 			value = sum;
 		}
 		if (compoundMetric.getAggregationFunction().equals(AVERAGE)){
 			float sum = 0;
 			for(Metric m:metrics){
 				sum +=m.getValue();
 			}
 			value = sum/metrics.size();
 		}
 		compoundMetric.setValue(value);
 		
 		return compoundMetric;
 	}
 	
 	/**
 	 * Gets the set of workflow ids and their plugins
 	 * @return
 	 */
 	public Set<String> getRegisteredPlugins(){
 		Set<String> registeredPlugins = new HashSet<String>();
 		
 		String queryString = queryPrefixes +
 		"Select Distinct ?PluginName "+
 		"Where{ "+
 			"?PluginNameInstance rdf:type sim:PluginName . "+
 			"?PluginNameInstance sim:hasDataValue ?PluginName ."+
 		"}";
 		
 		ClosableIterator<QueryRow> cr = rdfDatabase.sparqlSelect(queryString).iterator();
 		while(cr.hasNext()){
 			QueryRow qr = cr.next();
 			registeredPlugins.add(qr.getValue("PluginName").toString());			
 		}		
 			
 		return registeredPlugins;
 	}
 
 	public Set<String> getRegisteredWorkflowIds(){
 		Set<String> registeredWorkflowIds = new HashSet<String>();
 		
 		String queryString = queryPrefixes +
 		
 		"Select Distinct ?WorkflowId "+
 		"Where{ "+
 			"?WorkflowIdInstance rdf:type WorkflowId . "+
 			"?WorkflowIdInstance sim:hasDataValue ?WorkflowId ."+
 		"}";
 		
 		ClosableIterator<QueryRow> cr = rdfDatabase.sparqlSelect(queryString).iterator();
 		while(cr.hasNext()){
 			QueryRow qr = cr.next();
 			registeredWorkflowIds.add(qr.getValue("WorkflowId").toString());			
 		}		
 			
 		return registeredWorkflowIds;
 	}
 
 	/**
 	 * Counts the number of queries that were received in a given time interval
 	 * @param startDateTime
 	 * @param endDateTime
 	 * @return a compound metric of type QueriesPerTimeInterval given the start and end time of the interval
 	 */
 	public CompoundMetric generateQueriesPerTimeInterval(long startDateTime, long endDateTime){
 
 		String queryString = queryPrefixes +
 		"Select ?QueryMethodExecutionInstance "+
 		"Where { "+
 	            "?QueryMethodExecutionInstance rdf:type sim:MethodExecution ."+
 	            "?QueryMethodExecutionInstance sim:isMethodExecutionOf sim:eu.larkc.core.endpoint.sparql.SparqlHandler.handle ."+
 	            "?QueryMethodExecutionInstance sim:hasBeginExecutionTime ?QueryBeginExecutionTime ."+
 	   	              "FILTER(?QueryBeginExecutionTime >= "+ startDateTime +" && " + "?QueryBeginExecutionTime <= "+ endDateTime + ")."+
 			"}"; 
 
 		ClosableIterator<QueryRow> cr = rdfDatabase.sparqlSelect(queryString).iterator();
 		int count = 0;
 		while(cr.hasNext()){
 			cr.next(); count++;
 		}		
 
 		CompoundMetric result =  new CompoundMetricImpl(new URIImpl("http://www.larkc.eu/ontologies/IMOntology.rdf#QueriesPerTimeInterval"), COUNT);
 		result.setValue(count);
 		return result;
 	}
 
 	/**
 	 * Counts the number of queries that finished with success
 	 * @param startDateTime
 	 * @param endDateTime
 	 * @return a compound metric of type QuerySuccessRatePerTimeInterval given the start and end time of the interval
 	 */
 	public CompoundMetric generateQueriesSuccessRatePerTimeInterval(long startDateTime, long endDateTime){
 				
 		String queryString = queryPrefixes +
 		"Select ?QueryMethodExecutionInstance "+
 		"Where { "+
 	            "?QueryMethodExecutionInstance rdf:type sim:MethodExecution ."+
 	            "?QueryMethodExecutionInstance sim:isMethodExecutionOf sim:eu.larkc.core.endpoint.sparql.SparqlHandler.handle ."+
 	            "?QueryMethodExecutionInstance sim:hasBeginExecutionTime ?QueryBeginExecutionTime ."+
 	            "?QueryMethodExecutionInstance sim:hasEndedWithError ?QueryErrorStatus ."+
 	   	              "FILTER(?QueryBeginExecutionTime >= "+ startDateTime +" && " + "?QueryBeginExecutionTime <= "+ endDateTime + 
 	   	              " && xsd:boolean(?QueryErrorStatus) = \"true\"^^xsd:boolean)."+
 			"}"; 
 
 		ClosableIterator<QueryRow> cr = rdfDatabase.sparqlSelect(queryString).iterator();
 		int count = 0;
 		while(cr.hasNext()){
 			cr.next(); count++;
 		}		
 
 		CompoundMetric result =  new CompoundMetricImpl(new URIImpl("http://www.larkc.eu/ontologies/IMOntology.rdf#QuerySuccessRatePerTimeInterval"), COUNT);
 		result.setValue(count);
 		return result;
 	}
 
 	/**
 	 * Counts the number of queries that finished with failure
 	 * @param startDateTime
 	 * @param endDateTime
 	 * @return a compound metric of type QueryFailureRatePerTimeInterval given the start and end time of the interval
 	 */
 	public CompoundMetric generateQueriesFailureRatePerTimeInterval(long startDateTime, long endDateTime){
 				
 		String queryString = queryPrefixes +
 		"Select ?QueryMethodExecutionInstance "+
 		"Where { "+
 	            "?QueryMethodExecutionInstance rdf:type sim:MethodExecution ."+
 	            "?QueryMethodExecutionInstance sim:isMethodExecutionOf sim:eu.larkc.core.endpoint.sparql.SparqlHandler.handle ."+
 	            "?QueryMethodExecutionInstance sim:hasBeginExecutionTime ?QueryBeginExecutionTime ."+
 	            "?QueryMethodExecutionInstance sim:hasEndedWithError ?QueryErrorStatus ."+
 	   	              "FILTER(?QueryBeginExecutionTime >= "+ startDateTime +" && " + "?QueryBeginExecutionTime <= "+ endDateTime + 
 	   	              " && xsd:boolean(?QueryErrorStatus) = \"flase\"^^xsd:boolean)."+
 			"}"; 
 
 		ClosableIterator<QueryRow> cr = rdfDatabase.sparqlSelect(queryString).iterator();
 		int count = 0;
 		while(cr.hasNext()){
 			cr.next(); count++;
 		}		
 
 		CompoundMetric result =  new CompoundMetricImpl(new URIImpl("http://www.larkc.eu/ontologies/IMOntology.rdf#QueryFailureRatePerTimeInterval"), COUNT);
 		result.setValue(count);
 		return result;
 	}
 	
 	/**
 	 * Counts the number of work-flows of a given name (id) that were started in a given time interval
 	 * @param startDateTime
 	 * @param endDateTime
 	 * @param workflowID
 	 * @return a compound metric of type WorkflowsPerTimeInterval given the start and end time of the interval
 	 */
 	public CompoundMetric generateWorkflowsPerTimeInterval(long startDateTime, long endDateTime, String workflowID){
 				
 		String queryString = queryPrefixes +
 		"Select ?WorkflowMethodExecutionInstance "+
 		"Where { "+
 	            "?WorkflowMethodExecutionInstance rdf:type sim:MethodExecution ."+
 	            "{?WorkflowMethodExecutionInstance sim:isMethodExecutionOf sim:eu.larkc.core.executor.Executor.execute .} union "+
 	            "{?WorkflowMethodExecutionInstance sim:isMethodExecutionOf sim:eu.larkc.core.executor.Executor.getNextResults .} "+	            
 	            "?WorkflowMethodExecutionInstance sim:hasMethodMetric ?WorkflowWallClockTimeInstance . "+
 	            "?WorkflowWallClockTimeInstance rdf:type sim:WallClockTime . "+
 	            				
 	            "?WorkflowWallClockTimeInstance sim:hasContext ?WorkflowContextInstance . "+
 	            "?WorkflowContextInstance rdf:type sim:WorkflowExecution . "+
 	    		"?WorkflowContextInstance sim:hasMetrics ?WorkflowContextInstanceMetrics . "+
 	    		"?WorkflowContextInstanceMetrics rdf:li ?WorkflowIdInstance . "+    						
 	            "?WorkflowIdInstance rdf:type sim:WorkflowId . "+
 				"?WorkflowIdInstance sim:hasDataValue ?WorkflowId ."+
 	            
 	            "?WorkflowMethodExecutionInstance sim:hasBeginExecutionTime ?WorkflowBeginExecutionTime ."+
 	   	              "FILTER(?WorkflowBeginExecutionTime >= "+ startDateTime +" && " + "?WorkflowBeginExecutionTime <= "+ endDateTime +
 	   	              		" && ?WorkflowId = "+ workflowID +")."+
 			"}"; 
 
 		ClosableIterator<QueryRow> cr = rdfDatabase.sparqlSelect(queryString).iterator();
 		int count = 0;
 		while(cr.hasNext()){
 			cr.next(); count++;
 		}		
 
 		CompoundMetric result =  new CompoundMetricImpl(new URIImpl("http://www.larkc.eu/ontologies/IMOntology.rdf#WorkflowsPerTimeInterval"), COUNT);
 		result.setValue(count);
 		return result;
 	}
 
 
 	/**
 	 * The average duration of a given work-flow in a given time interval
 	 * @param startDateTime
 	 * @param endDateTime
 	 * @param workflowID
 	 * @return a compound metric of type WorkflowAvgDurationPerTimeInterval given the start and end time of the interval
 	 */
 	public CompoundMetric generateWorkflowAvgDurationPerTimeInterval(long startDateTime, long endDateTime, String workflowID){
 				
 		String queryString = queryPrefixes +
 		"Select ?WorkflowTotalResponseTime "+
 		"Where { "+
 	            "?WorkflowMethodExecutionInstance rdf:type sim:MethodExecution ."+
 	            "{?WorkflowMethodExecutionInstance sim:isMethodExecutionOf sim:eu.larkc.core.executor.Executor.execute .} union "+
 	            "{?WorkflowMethodExecutionInstance sim:isMethodExecutionOf sim:eu.larkc.core.executor.Executor.getNextResults .} "+	            
 	            "?WorkflowMethodExecutionInstance sim:hasMethodMetric ?WorkflowWallClockTimeInstance . "+
 	            "?WorkflowWallClockTimeInstance rdf:type sim:WallClockTime . "+
 	            "?WorkflowWallClockTimeInstance sim:hasDataValue ?WorkflowTotalResponseTime . "+
 
 	            				
 	            "?WorkflowWallClockTimeInstance sim:hasContext ?WorkflowContextInstance . "+
 	            "?WorkflowContextInstance rdf:type sim:WorkflowExecution . "+
 	    		"?WorkflowContextInstance sim:hasMetrics ?WorkflowContextInstanceMetrics . "+
 	    		"?WorkflowContextInstanceMetrics rdf:li ?WorkflowIdInstance . "+    						
 	            "?WorkflowIdInstance rdf:type sim:WorkflowId . "+
 				"?WorkflowIdInstance sim:hasDataValue ?WorkflowId ."+
 	            
 	            "?WorkflowMethodExecutionInstance sim:hasBeginExecutionTime ?WorkflowBeginExecutionTime ."+
 	   	              "FILTER(?WorkflowBeginExecutionTime >= "+ startDateTime +" && " + "?WorkflowBeginExecutionTime <= "+ endDateTime +
 	   	              		" && ?WorkflowId = "+ workflowID +")."+
 			"}"; 
 
 		ClosableIterator<QueryRow> cr = rdfDatabase.sparqlSelect(queryString).iterator();
 		double avg = 0; 	
 		int count = 0;
 		while(cr.hasNext()){
 			QueryRow qr = cr.next();
 			count++;
 			avg += new Double(qr.getValue("WorkflowTotalResponseTime").toString());			
 		}		
 
 		CompoundMetric result =  new CompoundMetricImpl(new URIImpl("http://www.larkc.eu/ontologies/IMOntology.rdf#WorkflowAvgDurationPerTimeInterval"), AVERAGE);
 		result.setValue(avg/count);
 		return result;
 	}
 	
 	/**
 	 * Total execution time of the given  plug-in in a given time interval.
 	 * @param startDateTime
 	 * @param endDateTime
 	 * @param pluginName
 	 * @return a compound metric of type PlatformPluginTotalExecutionTimePerTimeInterval given the start and end time of the interval
 	 */
 	
 	public CompoundMetric generatePlatformPluginTotalExecutionTimePerTimeInterval(long startDateTime, long endDateTime, String pluginName){
 				
 		String queryString = queryPrefixes +
 		"Select ?PluginTotalResponseTime "+
 		"Where { "+			
 				"?PluginMethodExecutionInstance rdf:type sim:MethodExecution ."+
 				"?PluginMethodExecutionInstance sim:isMethodExecutionOf sim:eu.larkc.plugin.Plugin.invoke ."+
 	            "?PluginMethodExecutionInstance sim:hasBeginExecutionTime ?PluginBeginExecutionTime ."+
 	            "?PluginMethodExecutionInstance sim:hasEndExecutionTime ?PluginEndExecutionTime ."+
 	            "?PluginMethodExecutionInstance sim:hasMethodMetric ?PluginWallClockTimeInstance ."+
 	            
 	            "?PluginWallClockTimeInstance rdf:type sim:WallClockTime ."+
 	            "?PluginWallClockTimeInstance sim:hasDataValue ?PluginTotalResponseTime ."+
 	            "?PluginWallClockTimeInstance sim:hasContext ?PluginContextInstance ."+
 	            
 	            "?PluginContextInstance rdf:type sim:PluginExecution ."+
 	            "?PluginContextInstance sim:hasMetrics ?PluginContextInstanceMetrics ."+
 	            "?PluginContextInstanceMetrics rdf:li ?PluginNameInstance ."+	            				
 	            
 	            "?PluginNameInstance rdf:type sim:PluginName ."+
 	            "?PluginNameInstance sim:hasDataValue ?PluginName ."+
 	            
 	   	              "FILTER(?PluginBeginExecutionTime >= "+ startDateTime +" && " + "?PluginEndExecutionTime <= "+ endDateTime +
 	   	              		" && ?PluginName = "+ pluginName +")."+
 			"}"; 
 
 		ClosableIterator<QueryRow> cr = rdfDatabase.sparqlSelect(queryString).iterator();
 		double totalResponseTime = 0;
 		while(cr.hasNext()){
 			totalResponseTime +=new Double(cr.next().getLiteralValue("PluginTotalResponseTime")).doubleValue(); 
 		}		
 
 		CompoundMetric result =  new CompoundMetricImpl(new URIImpl("http://www.larkc.eu/ontologies/IMOntology.rdf#PlatformPluginTotalExecutionTimePerTimeInterval"), SUM);
 		result.setValue(totalResponseTime);
 		return result;
 	}
 
 	/**
 	 * Average execution time of the given  plug-in in a given time interval.
 	 * @param startDateTime
 	 * @param endDateTime
 	 * @param pluginName
 	 * @return a compound metric of type PlatformPluginAvgExecutionTimePerTimeInterval given the start and end time of the interval
 	 */
 	
 	public CompoundMetric generatePlatformPluginAvgExecutionTimePerTimeInterval(long startDateTime, long endDateTime, String pluginName){
 				
 		String queryString = queryPrefixes +
 		"Select ?PluginTotalResponseTime "+
 		"Where { "+			
 				"?PluginMethodExecutionInstance rdf:type sim:MethodExecution ."+
 				"?PluginMethodExecutionInstance sim:isMethodExecutionOf sim:eu.larkc.plugin.Plugin.invoke ."+
 	            "?PluginMethodExecutionInstance sim:hasBeginExecutionTime ?PluginBeginExecutionTime ."+
 	            "?PluginMethodExecutionInstance sim:hasEndExecutionTime ?PluginEndExecutionTime ."+
 	            "?PluginMethodExecutionInstance sim:hasMethodMetric ?PluginWallClockTimeInstance ."+
 	            
 	            "?PluginWallClockTimeInstance rdf:type sim:WallClockTime ."+
 	            "?PluginWallClockTimeInstance sim:hasDataValue ?PluginTotalResponseTime ."+
 	            "?PluginWallClockTimeInstance sim:hasContext ?PluginContextInstance ."+
 	            
 	            "?PluginContextInstance rdf:type sim:PluginExecution ."+
 	            "?PluginContextInstance sim:hasMetrics ?PluginContextInstanceMetrics ."+
 	            "?PluginContextInstanceMetrics rdf:li ?PluginNameInstance ."+	            				
 	            
 	            "?PluginNameInstance rdf:type sim:PluginName ."+
 	            "?PluginNameInstance sim:hasDataValue ?PluginName ."+
 	            
 	   	              "FILTER(?PluginBeginExecutionTime >= "+ startDateTime +" && " + "?PluginEndExecutionTime <= "+ endDateTime +
 	   	              		" && ?PluginName = "+ pluginName +")."+
 			"}"; 
 
 		ClosableIterator<QueryRow> cr = rdfDatabase.sparqlSelect(queryString).iterator();
 		double totalResponseTime = 0;
 		int count = 0;
 		while(cr.hasNext()){
 			totalResponseTime +=new Double(cr.next().getLiteralValue("PluginTotalResponseTime")).doubleValue(); 
 			count++;
 		}		
 
 		CompoundMetric result =  new CompoundMetricImpl(new URIImpl("http://www.larkc.eu/ontologies/IMOntology.rdf#PlatformPluginAvgExecutionTimePerTimeInterval"), AVERAGE);
 		result.setValue(totalResponseTime/count);
 		return result;
 	}
 	
 	/**
 	 * Total execution time of the given plug-in in a given time interval for the given work-flow.
 	 * @param startDateTime
 	 * @param endDateTime
 	 * @param pluginName
 	 * @param workflowID
 	 * @return a compound metric of type WorkflowPluginTotalExecutionTimePerTimeInterval given the start and end time of the interval
 	 */
 	
 	public CompoundMetric generateWorkflowPluginTotalExecutionTimePerTimeInterval(long startDateTime, long endDateTime, String pluginName, String workflowID){
 				
 		String queryString = queryPrefixes +
 		"Select ?PluginTotalResponseTime "+
 		"Where { "+			
 				"?PluginMethodExecutionInstance rdf:type sim:MethodExecution ."+
 				"?PluginMethodExecutionInstance sim:isMethodExecutionOf sim:eu.larkc.plugin.Plugin.invoke ."+
 	            "?PluginMethodExecutionInstance sim:hasBeginExecutionTime ?PluginBeginExecutionTime ."+
 	            "?PluginMethodExecutionInstance sim:hasEndExecutionTime ?PluginEndExecutionTime ."+
 	            "?PluginMethodExecutionInstance sim:hasMethodMetric ?PluginWallClockTimeInstance ."+
 	            
 	            "?PluginWallClockTimeInstance rdf:type sim:WallClockTime ."+
 	            "?PluginWallClockTimeInstance sim:hasDataValue ?PluginTotalResponseTime ."+
 	            "?PluginWallClockTimeInstance sim:hasContext ?PluginContextInstance ."+
 	            
 	            "?PluginContextInstance rdf:type sim:PluginExecution ."+
 	            "?PluginContextInstance sim:hasMetrics ?PluginContextInstanceMetrics ."+
 	            "?PluginContextInstanceMetrics rdf:li ?PluginNameInstance ."+	            				
 	            
 	            "?PluginNameInstance rdf:type sim:PluginName ."+
 	            "?PluginNameInstance sim:hasDataValue ?PluginName ."+
 	         
 	            "?PluginContextInstance sim:hasParentContext ?WorkflowContextInstance ."+
 	            "?WorkflowContextInstance rdf:type sim:WorkflowExecution ."+
 	            "?WorkflowContextInstance sim:hasMetrics ?WorkflowContextInstanceMetrics ."+
 	            "?WorkflowContextInstanceMetrics rdf:li ?WorkflowIdInstance ."+
 	            "?WorkflowIdInstance rdf:type sim:WorkflowId ."+
 	            "?WorkflowIdInstance sim:hasDataValue ?WorkflowId ."+
 
 	   	         "FILTER(?PluginBeginExecutionTime >= "+ startDateTime +" && " + "?PluginEndExecutionTime <= "+ endDateTime +
 	   	              		" && ?PluginName = "+ pluginName +" && ?WorkflowId = "+ workflowID +")."+
 			"}"; 
 
 		ClosableIterator<QueryRow> cr = rdfDatabase.sparqlSelect(queryString).iterator();
 		double totalResponseTime = 0;
 		while(cr.hasNext()){
 			totalResponseTime +=new Double(cr.next().getLiteralValue("PluginTotalResponseTime")).doubleValue(); 
 		}		
 
 		CompoundMetric result =  new CompoundMetricImpl(new URIImpl("http://www.larkc.eu/ontologies/IMOntology.rdf#WorkflowPluginTotalExecutionTimePerTimeInterval"), SUM);
 		result.setValue(totalResponseTime);
 		return result;
 	}
 
 	/**
 	 * Average execution time of the given plug-in in a given time interval for the given work-flow.
 	 * @param startDateTime
 	 * @param endDateTime
 	 * @param pluginName
 	 * @param workflowID
 	 * @return a compound metric of type WorkflowPluginAvgExecutionTimePerTimeInterval given the start and end time of the interval
 	 */
 	
 	public CompoundMetric generateWorkflowPluginAvgExecutionTimePerTimeInterval(long startDateTime, long endDateTime, String pluginName, String workflowID){
 				
 		String queryString = queryPrefixes +
 		"Select ?PluginTotalResponseTime "+
 		"Where { "+			
 				"?PluginMethodExecutionInstance rdf:type sim:MethodExecution ."+
 				"?PluginMethodExecutionInstance sim:isMethodExecutionOf sim:eu.larkc.plugin.Plugin.invoke ."+
 	            "?PluginMethodExecutionInstance sim:hasBeginExecutionTime ?PluginBeginExecutionTime ."+
 	            "?PluginMethodExecutionInstance sim:hasEndExecutionTime ?PluginEndExecutionTime ."+
 	            "?PluginMethodExecutionInstance sim:hasMethodMetric ?PluginWallClockTimeInstance ."+
 	            
 	            "?PluginWallClockTimeInstance rdf:type sim:WallClockTime ."+
 	            "?PluginWallClockTimeInstance sim:hasDataValue ?PluginTotalResponseTime ."+
 	            "?PluginWallClockTimeInstance sim:hasContext ?PluginContextInstance ."+
 	            
 	            "?PluginContextInstance rdf:type sim:PluginExecution ."+
 	            "?PluginContextInstance sim:hasMetrics ?PluginContextInstanceMetrics ."+
 	            "?PluginContextInstanceMetrics rdf:li ?PluginNameInstance ."+	            				
 	            
 	            "?PluginNameInstance rdf:type sim:PluginName ."+
 	            "?PluginNameInstance sim:hasDataValue ?PluginName ."+
 	            "?PluginName sim:hasDataValue "+pluginName+" ."+
 	            
 	         
 	            "?PluginContextInstance sim:hasParentContext ?WorkflowContextInstance ."+
 	            "?WorkflowContextInstance rdf:type sim:WorkflowExecution ."+
 	            "?WorkflowContextInstance sim:hasMetrics ?WorkflowContextInstanceMetrics ."+
 	            "?WorkflowContextInstanceMetrics rdf:li ?WorkflowIdInstance ."+
 	            "?WorkflowIdInstance rdf:type sim:WorkflowId ."+
 	            "?WorkflowIdInstance sim:hasDataValue ?WorkflowId ."+
 
 	            "FILTER(?PluginBeginExecutionTime >= "+ startDateTime +" && " + "?PluginEndExecutionTime <= "+ endDateTime +
               		" && ?PluginName = "+ pluginName +" && ?WorkflowId = "+ workflowID +")."+
 
 			"}"; 
 
 		ClosableIterator<QueryRow> cr = rdfDatabase.sparqlSelect(queryString).iterator();
 		double totalResponseTime = 0;
 		int count = 0;
 		while(cr.hasNext()){
 			totalResponseTime +=new Double(cr.next().getLiteralValue("PluginTotalResponseTime")).doubleValue(); 
 			count++;
 		}		
 
 		CompoundMetric result =  new CompoundMetricImpl(new URIImpl("http://www.larkc.eu/ontologies/IMOntology.rdf#WorkflowPluginAvgExecutionTimePerTimeInterval"), AVERAGE);
 		result.setValue(totalResponseTime/count);
 		return result;
 	}
 
 	
 	/**
 	 * Total number of threads of the given plug-in in a given time interval.
 	 * @param startDateTime
 	 * @param endDateTime
 	 * @param pluginName
 	 * @return a compound metric of type PlatformPluginTotalThreadsStartedPerTimeInterval given the start and end time of the interval
 	 */
 	
 	public CompoundMetric generatePlatformPluginTotalThreadsStartedPerTimeInterval(long startDateTime, long endDateTime, String pluginName){
 				
 		String queryString = queryPrefixes +
 		"Select ?PluginThreadCount "+
 		"Where { "+			
 				"?PluginMethodExecutionInstance rdf:type sim:MethodExecution ."+
 				"?PluginMethodExecutionInstance sim:isMethodExecutionOf sim:eu.larkc.plugin.Plugin.invoke ."+
 	            "?PluginMethodExecutionInstance sim:hasBeginExecutionTime ?PluginBeginExecutionTime ."+
 	            "?PluginMethodExecutionInstance sim:hasEndExecutionTime ?PluginEndExecutionTime ."+
 	            "?PluginMethodExecutionInstance sim:hasMethodMetric ?PluginThreadCountInstance ."+
 	            
 	            "?PluginThreadCountInstance rdf:type sim:ThreadCount ."+
 	            "?PluginThreadCountInstance sim:hasDataValue ?PluginThreadCount ."+
 	            "?PluginThreadCountInstance sim:hasContext ?PluginContextInstance ."+
 	    	            
 	            "?PluginContextInstance rdf:type sim:PluginExecution ."+
 	            "?PluginContextInstance sim:hasMetrics ?PluginContextInstanceMetrics ."+
 	            "?PluginContextInstanceMetrics rdf:li ?PluginNameInstance ."+	            				
 	            
 	            "?PluginNameInstance rdf:type sim:PluginName ."+
 	            "?PluginNameInstance sim:hasDataValue ?PluginName ."+
 	            
 	   	              "FILTER(?PluginBeginExecutionTime >= "+ startDateTime +" && " + "?PluginEndExecutionTime <= "+ endDateTime +
 	   	              		" && ?PluginName = "+ pluginName +")."+
 			"}"; 
 
 		ClosableIterator<QueryRow> cr = rdfDatabase.sparqlSelect(queryString).iterator();
 		int totalThreadsStarted = 0;
 		while(cr.hasNext()){
 			totalThreadsStarted +=new Integer(cr.next().getLiteralValue("PluginThreadCount")).intValue(); 
 		}		
 
 		CompoundMetric result = new CompoundMetricImpl(new URIImpl("http://www.larkc.eu/ontologies/IMOntology.rdf#PlatformPluginTotalThreadsStartedPerTimeInterval"), SUM);
 		result.setValue(totalThreadsStarted);
 		return result;
 	}
 
 	/**
 	 * Average number of threads started by the given plug-in in a given time interval.
 	 * @param startDateTime
 	 * @param endDateTime
 	 * @param pluginName
 	 * @return a compound metric of type PlatformPluginAvgThreadsStartedPerTimeInterval given the start and end time of the interval
 	 */
 	
 	public CompoundMetric generatePlatformPluginAvgThreadsStartedPerTimeInterval(long startDateTime, long endDateTime, String pluginName){
 				
 		String queryString = queryPrefixes +
 		"Select ?PluginThreadCount "+
 		"Where { "+			
 				"?PluginMethodExecutionInstance rdf:type sim:MethodExecution ."+
 				"?PluginMethodExecutionInstance sim:isMethodExecutionOf sim:eu.larkc.plugin.Plugin.invoke ."+
 	            "?PluginMethodExecutionInstance sim:hasBeginExecutionTime ?PluginBeginExecutionTime ."+
 	            "?PluginMethodExecutionInstance sim:hasEndExecutionTime ?PluginEndExecutionTime ."+
 	            "?PluginMethodExecutionInstance sim:hasMethodMetric ?PluginThreadCountInstance ."+
 	            
 	            "?PluginThreadCountInstance rdf:type sim:ThreadCount ."+
 	            "?PluginThreadCountInstance sim:hasDataValue ?PluginThreadCount ."+
 	            "?PluginThreadCountInstance sim:hasContext ?PluginContextInstance ."+
 	    	            
 	            "?PluginContextInstance rdf:type sim:PluginExecution ."+
 	            "?PluginContextInstance sim:hasMetrics ?PluginContextInstanceMetrics ."+
 	            "?PluginContextInstanceMetrics rdf:li ?PluginNameInstance ."+	            				
 	            
 	            "?PluginNameInstance rdf:type sim:PluginName ."+
 	            "?PluginNameInstance sim:hasDataValue ?PluginName ."+
 	            
 	   	              "FILTER(?PluginBeginExecutionTime >= "+ startDateTime +" && " + "?PluginEndExecutionTime <= "+ endDateTime +
 	   	              		" && ?PluginName = "+ pluginName +")."+
 			"}"; 
 
 		ClosableIterator<QueryRow> cr = rdfDatabase.sparqlSelect(queryString).iterator();
 		int totalThreadsStarted = 0;
 		int count = 0;
 		while(cr.hasNext()){
 			totalThreadsStarted +=new Integer(cr.next().getLiteralValue("PluginThreadCount")).intValue(); 
 			count++;
 		}		
 
 		CompoundMetric result = new CompoundMetricImpl(new URIImpl("http://www.larkc.eu/ontologies/IMOntology.rdf#PlatformPluginAvgThreadsStartedPerTimeInterval"), AVERAGE);
 		result.setValue(totalThreadsStarted / count);
 		return result;
 	}
 	
 	/**
 	 * Total number of threads started by the given plug-in in all the runs of a specified work-flow in a given time interval.
 	 * @param startDateTime
 	 * @param endDateTime
 	 * @param pluginName
 	 * @param workflowId
 	 * @return a compound metric of type WorkflowPluginTotalThreadsStartedPerTimeInterval given the start and end time of the interval
 	 */
 	
 	public CompoundMetric generateWorkflowPluginTotalThreadsStartedPerTimeInterval(DatatypeLiteral startDateTime, DatatypeLiteral endDateTime, String pluginName, String workflowId){
 				
 		String queryString = queryPrefixes +
 		"Select ?PluginThreadCount "+
 		"Where { "+			
 				"?PluginMethodExecutionInstance rdf:type sim:MethodExecution ."+
 				"?PluginMethodExecutionInstance sim:isMethodExecutionOf sim:eu.larkc.plugin.Plugin.invoke ."+
 	            "?PluginMethodExecutionInstance sim:hasBeginExecutionTime ?PluginBeginExecutionTime ."+
 	            "?PluginMethodExecutionInstance sim:hasEndExecutionTime ?PluginEndExecutionTime ."+
 	            "?PluginMethodExecutionInstance sim:hasMethodMetric ?PluginThreadCountInstance ."+
 	            
 	            "?PluginThreadCountInstance rdf:type sim:ThreadCount ."+
 	            "?PluginThreadCountInstance sim:hasDataValue ?PluginThreadCount ."+
 	            "?PluginThreadCountInstance sim:hasContext ?PluginContextInstance ."+
 	    	            
 	            "?PluginContextInstance rdf:type sim:PluginExecution ."+
 	            "?PluginContextInstance sim:hasMetrics ?PluginContextInstanceMetrics ."+
 	            "?PluginContextInstanceMetrics rdf:li ?PluginNameInstance ."+	            				
 	            
 	            "?PluginNameInstance rdf:type sim:PluginName ."+
 	            "?PluginNameInstance sim:hasDataValue ?PluginName ."+
 	            
 	            "?PluginContextInstance sim:hasParentContext ?WorkflowContextInstance ."+
 	            "?WorkflowContextInstance rdf:type sim:WorkflowExecution ."+
 	            "?WorkflowContextInstance sim:hasMetrics ?WorkflowContextInstanceMetrics ."+
 	            "?WorkflowContextInstanceMetrics rdf:li ?WorkflowIdInstance ."+
 	            "?WorkflowIdInstance rdf:type sim:WorkflowId ."+
 	            "?WorkflowIdInstance sim:hasDataValue ?WorkflowId ."+
 
 	            
 	   	              "FILTER(?PluginBeginExecutionTime >= "+ startDateTime +" && " + "?PluginEndExecutionTime <= "+ endDateTime +
 	   	              		" && ?PluginName = "+ pluginName +" && ?WorkflowId = "+ workflowId +")."+
 	   	              		
 			"}"; 
 
 		ClosableIterator<QueryRow> cr = rdfDatabase.sparqlSelect(queryString).iterator();
 		int totalThreadsStarted = 0;
 		while(cr.hasNext()){
 			totalThreadsStarted +=new Integer(cr.next().getLiteralValue("PluginThreadCount")).intValue(); 
 		}		
 
 		CompoundMetric result = new CompoundMetricImpl(new URIImpl("http://www.larkc.eu/ontologies/IMOntology.rdf#WorkflowPluginTotalThreadsStartedPerTimeInterval"), SUM);
 		result.setValue(totalThreadsStarted);
 		return result;
 	}
 
 
 	/**
 	 * Average number of threads of the given plug-in in all the runs of a specified work-flow in a given time interval.
 	 * @param startDateTime
 	 * @param endDateTime
 	 * @param pluginName
 	 * @param workflowID
 	 * @return a compound metric of type WorkflowPluginAvgThreadsStartedPerTimeInterval given the start and end time of the interval
 	 */
 	
 	public CompoundMetric generateWorkflowPluginAvgThreadsStartedPerTimeInterval(long startDateTime, long endDateTime, String pluginName, String workflowID){
 				
 		String queryString = queryPrefixes +
 		"Select ?PluginThreadCount "+
 		"Where { "+			
 				"?PluginMethodExecutionInstance rdf:type sim:MethodExecution ."+
 				"?PluginMethodExecutionInstance sim:isMethodExecutionOf sim:eu.larkc.plugin.Plugin.invoke ."+
 	            "?PluginMethodExecutionInstance sim:hasBeginExecutionTime ?PluginBeginExecutionTime ."+
 	            "?PluginMethodExecutionInstance sim:hasEndExecutionTime ?PluginEndExecutionTime ."+
 	            "?PluginMethodExecutionInstance sim:hasMethodMetric ?PluginThreadCountInstance ."+
 	            
 	            "?PluginThreadCountInstance rdf:type sim:ThreadCount ."+
 	            "?PluginThreadCountInstance sim:hasDataValue ?PluginThreadCount ."+
 	            "?PluginThreadCountInstance sim:hasContext ?PluginContextInstance ."+
 	    	            
 	            "?PluginContextInstance rdf:type sim:PluginExecution ."+
 	            "?PluginContextInstance sim:hasMetrics ?PluginContextInstanceMetrics ."+
 	            "?PluginContextInstanceMetrics rdf:li ?PluginNameInstance ."+	            				
 	            
 	            "?PluginNameInstance rdf:type sim:PluginName ."+
 	            "?PluginNameInstance sim:hasDataValue ?PluginName ."+
 	            
 	            "?PluginContextInstance sim:hasParentContext ?WorkflowContextInstance ."+
 	            "?WorkflowContextInstance rdf:type sim:WorkflowExecution ."+
 	            "?WorkflowContextInstance sim:hasMetrics ?WorkflowContextInstanceMetrics ."+
 	            "?WorkflowContextInstanceMetrics rdf:li ?WorkflowIdInstance ."+
 	            "?WorkflowIdInstance rdf:type sim:WorkflowId ."+
 	            "?WorkflowIdInstance sim:hasDataValue ?WorkflowId ."+
 
 	            
 	   	              "FILTER(?PluginBeginExecutionTime >= "+ startDateTime +" && " + "?PluginEndExecutionTime <= "+ endDateTime +
 	   	              		" && ?PluginName = "+ pluginName +" && ?WorkflowId = "+ workflowID +")."+
 	   	              		
 			"}"; 
 
 		ClosableIterator<QueryRow> cr = rdfDatabase.sparqlSelect(queryString).iterator();
 		int totalThreadsStarted = 0;
 		int count = 0;
 		while(cr.hasNext()){
 			totalThreadsStarted +=new Integer(cr.next().getLiteralValue("PluginThreadCount")).intValue(); 
 			count++;
 		}		
 
		CompoundMetric result = new CompoundMetricImpl(new URIImpl("http://www.larkc.eu/ontologies/IMOntology.rdf#WorkflowPluginTotalThreadsStartedPerTimeInterval"), AVERAGE);
 		result.setValue(totalThreadsStarted / count);
 		return result;
 	}
 
 }
