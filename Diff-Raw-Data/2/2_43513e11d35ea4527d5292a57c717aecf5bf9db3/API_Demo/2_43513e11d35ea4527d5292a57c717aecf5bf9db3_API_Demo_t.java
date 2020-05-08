 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package nl.maastro.eureca.aida.search.zylabpatisclient;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Arrays;
 import java.util.List;
 import javax.xml.rpc.ServiceException;
 import nl.maastro.eureca.aida.search.zylabpatisclient.config.Config;
 import org.apache.lucene.search.Query;
 
 /**
  *
  * @author kasper2
  */
 public class API_Demo {
 	
 	static public void main(String[] args) {
 		try {
 			// Read config file
 			// InputStream s = new FileInputStream("/home/kasper2/git/aida.git/Search/zylabPatisClient/src/main/webapp/WEB-INF/zpsc-config.xml");
 
 			InputStream s = new FileInputStream("/home/administrator/aida.git/Search/zylabPatisClient/src/main/webapp/WEB-INF/zpsc-config.xml");
 			Config config = Config.init(s);
 
 			// Use config to initialise a searcher
 			Searcher searcher = config.getSearcher();
 
 			// Dummy list of patients; reading a list of patisnumbers is not yet in API
 			List<PatisNumber> patients = Arrays.<PatisNumber>asList(
 //					PatisNumber.create("12345"),
 //					PatisNumber.create("11111"),
 					PatisNumber.create("71358"), // Exp 0
 					PatisNumber.create("71314"),
 					PatisNumber.create("71415"), // Exp 0
 					PatisNumber.create("71539"),
 					PatisNumber.create("71586"),
 					PatisNumber.create("70924"),
 					PatisNumber.create("71785"),
 					PatisNumber.create("71438"),
 					PatisNumber.create("71375"),
 					PatisNumber.create("71448"),
 					
 					PatisNumber.create("71681"), // Exp 1
 					PatisNumber.create("71692"),
 					PatisNumber.create("71757"),
 					PatisNumber.create("70986"),
 					PatisNumber.create("46467"));
 
 			// Get a QueryPattern; normally the Query is retrieved via its
 			// URI and not via an internal enum constant
 			Query queryPattern = PreconstructedQueries.instance().getQuery(
 					PreconstructedQueries.LocalParts.METASTASIS_IV);
 			Iterable<SearchResult> result = searcher.searchForAll(
 					queryPattern,
 					patients);
 
 			// Do something with the results
 			for (SearchResult searchResult : result) {
 				System.out.printf("PatisNr: %s found in %d documents\nSnippets:\n",
 						searchResult.patient.value, searchResult.nHits);
 				
 				for (String docId : searchResult.snippets.keySet()) {
 					System.out.printf("\tDocument: %s\n", docId);
 					for (String snippet : searchResult.snippets.get(docId)) {
						System.out.printf("\t\t<snippet>%s</snippet>\n", snippet);
 					}
 				}
 			}
 			
 			
 			
 		} catch (ServiceException | IOException ex) {
 			throw new Error(ex);
 		}
 		
 	}
 }
