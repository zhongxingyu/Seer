 package com.hp.hpl.jena.sparql.serializer;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.jena.atlas.io.IndentedLineBuffer;
 
 import com.hp.hpl.jena.query.Query;
 import com.hp.hpl.jena.sparql.util.NodeToLabelMapBNode;
 
 import eu.play_project.play_platformservices.api.HistoricalQuery;
 
 
 public class PlaySerializer extends Serializer{
 	
	  /**
     * Generate historical querys dependent on the destination cloud Id.
     * @return Key: CloudID, Value: String representation of historical select query.
     */
     public static List<HistoricalQuery> serializeToMultipleSelectQueries(Query query){
         // For the query pattern
         SerializationContext cxt1 = new SerializationContext(query, new NodeToLabelMapBNode("b", false) ) ;
         // For the construct pattern
         SerializationContext cxt2 = new SerializationContext(query, new NodeToLabelMapBNode("c", false)  ) ;
         IndentedLineBuffer writer = new IndentedLineBuffer() ;
         
         HistoricalGraphFormaterElement e = new HistoricalGraphFormaterElement(writer, cxt1);
         
         serializeARQ(query, writer,
                      e,
                      new FmtExprSPARQL(writer, cxt1),
                      new FmtTemplate(writer, cxt2)) ;
 		// TODO make it more efficient.
 
         List<HistoricalQuery> result = new LinkedList<HistoricalQuery>();
         HistoricalQuery hq = new HistoricalQuery();
         
 		Map<String, String> queries = e.getHistoricalCloudQueries();
 		List<String> vars = query.getResultVars(); // FIXME different values for historical query.
 		vars.add("firstEvent");
 		String selectString = null;
 
 		// Add select variables
 		for (String key : queries.keySet()) {
 			hq = new HistoricalQuery();
 			// Add preifix
 			selectString = e.getPrefixNames() + "\n";
 			selectString += "SELECT DISTINCT ";
 			// Variables in current query.
 			for (String var : vars) {
 				if (queries.get(key).contains("?" + var)) {
 					selectString += " ?" + var;
 					//Set variables.
 					hq.getVariables().add("?" + var);
 				}
 			}
 			// Set values.
 			hq.setQuery(selectString +" \n WHERE { \n" + queries.get(key) + "} ");
 			hq.setCloudId(key);
 			result.add(hq);
 		}
 		return result;
 		
 
 	}
     
  
 
 }
