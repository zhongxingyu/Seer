 package com.computas.sublima.app.controller;
 
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.TreeMap;
 
 import org.apache.cocoon.components.flow.apples.AppleRequest;
 import org.apache.cocoon.components.flow.apples.AppleResponse;
 import org.apache.cocoon.components.flow.apples.StatelessAppleController;
 import org.apache.cocoon.environment.Request;
 
 import com.computas.sublima.app.Form2SparqlService;
 import com.computas.sublima.app.filter.AuthenticationFilter;
 import com.computas.sublima.query.SparqlDispatcher;
 
 import org.apache.log4j.Logger;
 
 public class Search implements StatelessAppleController {
 
 	private SparqlDispatcher sparqlDispatcher;
 
     	private static Logger logger = Logger.getLogger(Search.class);
 
 	@SuppressWarnings("unchecked")
 	public void process(AppleRequest req, AppleResponse res) throws Exception {
 		// the initial search page
 		String mode = req.getSitemapParameter("mode");
 		if (!"search-result".equalsIgnoreCase(mode)) {
 			res.sendPage("xhtml/search-form", null);
 			return;
 		}
 
 		// sending the result
 
 		String sparqlQuery = null;
 		// Check for magic prefixes
		Request request = req.getCocoonRequest()
		if (request.getParameterValues("prefix") != null) {
 			// Calls the Form2SPARQL service with the parameterMap which returns
 			// a SPARQL as String
 			Form2SparqlService form2SparqlService = new Form2SparqlService(
					request.getParameterValues("prefix"));
			request.removeAttribute("prefix"); // The prefixes are magic variables
			sparqlQuery = form2SparqlService.convertForm2Sparql(request);
 		} else {
 			res.sendStatus(400);
 		}
 		
 
 		// FIXME hard-wire the query for testing!!!
 		//		sparqlQuery = "DESCRIBE <http://the-jet.com/> <http://sublima.computas.com/topic-instance/Jet>";
 		
 		logger.trace("SPARQL query sent to dispatcher: " + sparqlQuery);
 		Object queryResult = sparqlDispatcher.query(sparqlQuery);
 		Map<String, Object> bizData = new HashMap<String, Object>();
 		bizData.put("result-list", queryResult);
 		bizData.put("configuration", new Object());
 		res.sendPage("xml/sparql-result", bizData);
 	}
 
 	public void setSparqlDispatcher(SparqlDispatcher sparqlDispatcher) {
 		this.sparqlDispatcher = sparqlDispatcher;
 	}
 
 
 
 }
