 package com.computas.sublima.app.controller;
 
 import com.computas.sublima.app.service.Form2SparqlService;
 import com.computas.sublima.app.service.SearchService;
 import com.computas.sublima.query.service.SettingsService;
 import com.computas.sublima.query.SparqlDispatcher;
 import com.computas.sublima.query.SparulDispatcher;
 import com.computas.sublima.query.service.DatabaseService;
 import com.hp.hpl.jena.db.IDBConnection;
 import com.hp.hpl.jena.db.ModelRDB;
 import com.hp.hpl.jena.query.Query;
 import com.hp.hpl.jena.query.QueryExecution;
 import com.hp.hpl.jena.query.QueryExecutionFactory;
 import com.hp.hpl.jena.query.QueryFactory;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.sparql.util.StringUtils;
 import org.apache.cocoon.components.flow.apples.AppleRequest;
 import org.apache.cocoon.components.flow.apples.AppleResponse;
 import org.apache.cocoon.components.flow.apples.StatelessAppleController;
 import org.apache.cocoon.environment.Request;
 import org.apache.log4j.Logger;
 
 import java.io.ByteArrayOutputStream;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.TreeMap;
 import java.sql.SQLException;
 
 public class Search implements StatelessAppleController {
   private SparqlDispatcher sparqlDispatcher;
   private SparulDispatcher sparulDispatcher;
   private String mode;
 
   private static Logger logger = Logger.getLogger(Search.class);
 
   @SuppressWarnings("unchecked")
   public void process(AppleRequest req, AppleResponse res) throws Exception {
 
     this.mode = req.getSitemapParameter("mode");
 
     // The initial advanced search page
     if ("advancedsearch".equalsIgnoreCase(mode)) {
       res.sendPage("xhtml/search-form", null);
       return;
     }
 
     // If it's search-results for advanced search, topic instance or resource
     if ("resource".equalsIgnoreCase(mode) || "search-result".equalsIgnoreCase(mode)) {
       doAdvancedSearch(res, req);
       return;
     }
 
     if ("freetext-result".equalsIgnoreCase(mode)) {
       doFreeTextSearch(res, req);
       return;
     }
 
     if ("topic-instance".equalsIgnoreCase(mode)) {
       doGetTopic(res, req);
       return;
     }
   }
 
   private void doGetTopic(AppleResponse res, AppleRequest req) {
 
     String subject = "<http://sublima.computas.com/topic-instance/" + req.getSitemapParameter("topic") + ">";
     String queryString = StringUtils.join("\n", new String[]{
             "PREFIX dct: <http://purl.org/dc/terms/>",
             "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
             "DESCRIBE ?resource " + subject + " ?publisher ?subjects",
             "WHERE {",
             "        ?resource dct:language ?lang;",
             "				 dct:publisher ?publisher ;",
             "                dct:subject " + subject + ", ?subjects .}"});
 
     logger.trace("SPARQL query sent to dispatcher: " + queryString);
     Object queryResult = sparqlDispatcher.query(queryString);
    
     Map<String, Object> bizData = new HashMap<String, Object>();
     bizData.put("result-list", queryResult);
 
     String sparqlConstructQuery =
               "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
               "prefix owl: <http://www.w3.org/2002/07/owl#>\n" +
 	      "prefix dct: <http://purl.org/dc/terms/>\n" +
               "prefix sub: <http://xmlns.computas.com/sublima#>\n" +
 	      "prefix skos: <http://www.w3.org/2004/02/skos/core#>\n" +
               "CONSTRUCT {\n" +
                       subject + " skos:prefLabel ?label ; \n" +
                       " a skos:Concept;\n" + 
                       " skos:altLabel ?synLabel ;\n" +
                       " skos:related ?relSub ;\n" +
                       " skos:broader ?btSub ;\n" +
                       " skos:narrower ?ntSub .\n" +
                       " ?relSub skos:prefLabel ?relLabel ;\n" +
                       " a skos:Concept .\n" +
                       " ?btSub skos:prefLabel ?btLabel ;\n" +
                       " a skos:Concept .\n" +
                       " ?ntSub skos:prefLabel ?ntLabel ;\n" +
                       " a skos:Concept .\n" +
                       " }\n" +
                       " WHERE {\n" +
                       subject + " rdfs:label ?label .\n" +
                       subject + " a ?class .\n" +
                       " OPTIONAL { " + subject + " sub:synonym ?synLabel  . }\n" +
                       " OPTIONAL { " + subject + " ?prop ?relSub .\n" +
                       " ?relSub rdfs:label ?relLabel . }\n" +
                       " OPTIONAL { ?class rdfs:subClassOf ?btClass .\n" +
                       " ?btSub a ?btClass ;\n" +
                       " rdfs:label ?btLabel . }\n" +
                       " OPTIONAL { ?ntClass rdfs:subClassOf ?class .\n" +
                       " ?ntSub a ?ntClass .\n" +
                       " ?ntClass rdfs:label ?ntLabel . } }";
 
     logger.trace("SPARQL query sent to dispatcher: " + sparqlConstructQuery);
     queryResult = sparqlDispatcher.query(sparqlConstructQuery);
    
     bizData.put("navigation", queryResult);
     bizData.put("mode", mode);
     res.sendPage("xml/sparql-result", bizData);
   }
 
 
   private void doFreeTextSearch(AppleResponse res, AppleRequest req) {
     String defaultBooleanOperator = SettingsService.getProperty("sublima.default.boolean.operator");
     String chosenOperator = req.getCocoonRequest().getParameter("booleanoperator");
     boolean deepsearch = false;
 
     SearchService searchService;
     DatabaseService myDbService = new DatabaseService();
     IDBConnection connection = myDbService.getConnection();
 
     //Use user chosen boolean operator when it doesn't equal the default
     if ( !chosenOperator.equalsIgnoreCase(defaultBooleanOperator)) {
       searchService = new SearchService(chosenOperator);
       logger.debug("SUBLIMA: Use " + chosenOperator + " as boolean operator for search");
     }
     else {
       searchService = new SearchService(defaultBooleanOperator);
       logger.debug("SUBLIMA: Use " + defaultBooleanOperator + " as boolean operator for search");
     }
 
     String searchstring = searchService.buildSearchString(req.getCocoonRequest().getParameter("searchstring"));
 
     //Do deep search in external resources or not
     if ( req.getCocoonRequest().getParameterValues("deepsearch") != null && "deepsearch".equalsIgnoreCase(req.getCocoonRequest().getParameterValues("deepsearch")[0])) {
       deepsearch = true;
       logger.debug("SUBLIMA: Deep search enabled");
     }
 
     //Create a model based on the one in the DB
     ModelRDB model = ModelRDB.open(connection);
     try {
       connection.close();
     } catch (SQLException e) {
       e.printStackTrace();
     }
 
     String queryString = StringUtils.join("\n", new String[]{
             "PREFIX pf: <http://jena.hpl.hp.com/ARQ/property#>",
             "PREFIX dct: <http://purl.org/dc/terms/>",
             "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
             "DESCRIBE ?resource ?subject ?publisher",
             "WHERE {",
             "        ?lit pf:textMatch ( '+" + searchstring + "' 10 ) .",
             "        ?resource ?p ?lit; ",
             "                dct:title ?title;",
             "                dct:description ?desc;",
             "                dct:language ?lang;",
             "				 dct:publisher ?publisher ;",
             "                dct:subject ?subject .}"});
 
     Query query = QueryFactory.create(queryString);
     QueryExecution qExec = QueryExecutionFactory.create(query, model);
     Model m = qExec.execDescribe();
     qExec.close();
 
     // If the model is empty, thus no results, return the zero-results-strategy-page
     if( m.isEmpty() ) {
       res.sendPage("tips", null);  
     }
     else {
       ByteArrayOutputStream bout = new ByteArrayOutputStream();
       m.write(bout, "RDF/XML-ABBREV");
 
       Map<String, Object> bizData = new HashMap<String, Object>();
       bizData.put("result-list", bout.toString());
      bizData.put("navigation", "<empty></empty>");
       bizData.put("mode", mode);
       bizData.put("configuration", new Object());
       res.sendPage("xml/sparql-result", bizData);
     }
   }
 
   public void doAdvancedSearch(AppleResponse res, AppleRequest req) {
     // Get all parameteres from the HTML form as Map
     Map<String, String[]> parameterMap = new TreeMap<String, String[]>(createParametersMap(req.getCocoonRequest()));
 
 
     if ("resource".equalsIgnoreCase(mode)) {
       parameterMap.put("prefix", new String[]{"dct: <http://purl.org/dc/terms/>", "rdfs: <http://www.w3.org/2000/01/rdf-schema#>"});
       parameterMap.put("interface-language", new String[]{req.getSitemapParameter("interface-language")});
       parameterMap.put("dct:identifier", new String[]{"http://sublima.computas.com/resource/"
               + req.getSitemapParameter("name")});
       parameterMap.put("dct:subject/rdfs:label", new String[]{""});
     }
 
     // sending the result
     String sparqlQuery = null;
     // Check for magic prefixes
     if (parameterMap.get("prefix") != null) {
       // Calls the Form2SPARQL service with the parameterMap which returns
       // a SPARQL as String
       Form2SparqlService form2SparqlService = new Form2SparqlService(parameterMap.get("prefix"));
       parameterMap.remove("prefix"); // The prefixes are magic variables
       sparqlQuery = form2SparqlService.convertForm2Sparql(parameterMap);
     } else {
       res.sendStatus(400);
     }
     // FIXME hard-wire the query for testing!!!
     // sparqlQuery = "DESCRIBE <http://the-jet.com/>";
 
     logger.trace("SPARQL query sent to dispatcher: " + sparqlQuery);
     Object queryResult = sparqlDispatcher.query(sparqlQuery);   	
     
     Map<String, Object> bizData = new HashMap<String, Object>();
     bizData.put("result-list", queryResult);
     bizData.put("navigation", "<empty></empty>");
     bizData.put("mode", mode);
     bizData.put("request", req.getCocoonRequest()); // TODO: Must loop
     res.sendPage("xml/sparql-result", bizData);
   }
 
   private Map<String, String[]> createParametersMap(Request request) {
     Map<String, String[]> result = new HashMap<String, String[]>();
     Enumeration parameterNames = request.getParameterNames();
     while (parameterNames.hasMoreElements()) {
       String paramName = (String) parameterNames.nextElement();
       result.put(paramName, request.getParameterValues(paramName));
     }
     return result;
   }
 
   public void setSparqlDispatcher(SparqlDispatcher sparqlDispatcher) {
     this.sparqlDispatcher = sparqlDispatcher;
   }
 
   public void setSparulDispatcher(SparulDispatcher sparulDispatcher) {
     this.sparulDispatcher = sparulDispatcher;
   }
 }
