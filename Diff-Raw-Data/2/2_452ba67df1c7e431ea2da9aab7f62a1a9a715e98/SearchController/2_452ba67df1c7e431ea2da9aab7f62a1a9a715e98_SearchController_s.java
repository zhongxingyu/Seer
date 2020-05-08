 package com.computas.sublima.app.controller;
 
 import com.computas.sublima.app.service.Form2SparqlService;
 import com.computas.sublima.app.service.AdminService;
 import com.computas.sublima.query.SparqlDispatcher;
 import com.computas.sublima.query.service.SearchService;
 import com.computas.sublima.query.service.SettingsService;
 import com.computas.sublima.query.service.CachingService;
 import static com.computas.sublima.query.service.SettingsService.getProperty;
 import com.hp.hpl.jena.sparql.util.StringUtils;
 import org.apache.cocoon.auth.ApplicationManager;
 import org.apache.cocoon.components.flow.apples.AppleRequest;
 import org.apache.cocoon.components.flow.apples.AppleResponse;
 import org.apache.cocoon.components.flow.apples.StatelessAppleController;
 import org.apache.cocoon.environment.Request;
 import org.apache.log4j.Logger;
 
 import java.util.*;
 
 import net.spy.memcached.MemcachedClient;
 
 public class SearchController implements StatelessAppleController {
   private SparqlDispatcher sparqlDispatcher;
   private ApplicationManager appMan;
   private AdminService adminService = new AdminService();
   private String mode;
   private String format;
 
   private static Logger logger = Logger.getLogger(SearchController.class);
 
   @SuppressWarnings("unchecked")
   public void process(AppleRequest req, AppleResponse res) throws Exception {
     boolean loggedIn = appMan.isLoggedIn("Sublima");
 
     this.mode = req.getSitemapParameter("mode");
     this.format = req.getSitemapParameter("format");
    if (format == null || "html".equalsIgnoreCase(format) || "".equalsIgnoreCase(format)) {
       format = "xml";
     }
 
     logger.trace("SearchController: Language from sitemap is " + req.getSitemapParameter("interface-language"));
 
     // The initial advanced search page
     if ("advancedsearch".equalsIgnoreCase(mode)) {
         Map<String, Object> bizData = new HashMap<String, Object>();
         bizData.put("loggedin", loggedIn);
         if (loggedIn) {
             bizData.put("statuses", adminService.getDistinctAndUsedLabels("<http://www.w3.org/2007/05/powder#DR>",
                     "<http://www.w3.org/2007/05/powder#describedBy>"));
         } else {
             bizData.put("statuses", "<empty></empty>");
         }
         bizData.put("languages", adminService.getDistinctAndUsedLabels("<http://www.lingvoj.org/ontology#Lingvo>", "dct:language"));
         bizData.put("mediatypes", adminService.getDistinctAndUsedLabels("dct:MediaType", "dct:format"));
         bizData.put("audiences", adminService.getDistinctAndUsedLabels("dct:AgentClass", "dct:audience"));
         bizData.put("committers", adminService.getDistinctAndUsedLabels("<http://rdfs.org/sioc/ns#User>", "<http://xmlns.computas.com/sublima#committer>"));
         StringBuilder params = adminService.getMostOfTheRequestXML(req);
         params.append("\n  </request>\n");
         bizData.put("facets", params.toString());
         System.gc();
         res.sendPage("xml/advancedsearch", bizData);
         return;
     }
 
     // If it's search-results for advanced search, topic instance or resource
     if ("resource".equalsIgnoreCase(mode) || "search-result".equalsIgnoreCase(mode)) {
       doAdvancedSearch(res, req, loggedIn);
       return;
     }
 
     if ("topic".equalsIgnoreCase(mode)) {
       doGetTopic(res, req, loggedIn);
     }
   }
 
   private void doGetTopic(AppleResponse res, AppleRequest req, boolean loggedIn) {
 
     String subject = "<" + getProperty("sublima.base.url")
             + "topic/" + req.getSitemapParameter("topic") + ">";
     String queryString = StringUtils.join("\n", new String[]{
             "PREFIX dct: <http://purl.org/dc/terms/>",
             "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
             "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>",
             "PREFIX wdr: <http://www.w3.org/2007/05/powder#>",
                     "DESCRIBE ?resource " + subject + " ?publisher ?subjects ?rest",
             "WHERE {",
             "        ?resource dct:language ?lang;",
             "				 dct:publisher ?publisher ;",
             "                dct:subject " + subject + ", ?subjects ;",
             "        wdr:describedBy <http://sublima.computas.com/status/godkjent_av_administrator> ;",
             "                ?p ?rest .}"});
 
     //logger.trace("doGetTopic: SPARQL query sent to dispatcher: " + queryString);
     Object queryResult = sparqlDispatcher.query(queryString);
 
     Map<String, Object> bizData = new HashMap<String, Object>();
     bizData.put("result-list", queryResult);
 
     // This query, relies on that all relations are explicitly stated.
     // I.e. a triple for both skos:broader and skos:narrower must exist.
     // Small fix added allowing concepts to not have relations at all.
      String sparqlConstructQuery =
         "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"+
         "prefix skos: <http://www.w3.org/2004/02/skos/core#> \n"+
         "prefix owl: <http://www.w3.org/2002/07/owl#> \n"+
         "CONSTRUCT {\n"+
          subject + "\n" +
         "   a skos:Concept ;\n"+
         "   skos:prefLabel ?preflabel ; \n"+
         "   skos:altLabel ?altlabel ;  \n"+
         "   skos:definition ?definition ; \n" +
         "   ?semrelation ?object . \n"+
         "?semrelation rdfs:subPropertyOf skos:semanticRelation ;\n"+
         "   rdfs:label ?semrellabel ;\n"+
         "   a owl:ObjectProperty .\n"+       
         "?object skos:prefLabel ?preflabel2 ; \n"+
         "   a skos:Concept .\n"+
         "}\n"+
         "WHERE {\n"+ 
          subject + "\n" + 
          "   skos:prefLabel ?preflabel ;\n"+
          "   a skos:Concept .\n"+
          "OPTIONAL {\n"+
          subject + "\n" + 
          "   skos:altLabel ?altlabel .\n" +
          "}\n"+
          "OPTIONAL {\n"+
          subject + "\n" +
          "   skos:definition ?definition ;\n" +
          "}\n"+
          "OPTIONAL {\n" +
          subject + "\n" + 
          "   ?semrelation ?object .\n"+
          "?semrelation rdfs:subPropertyOf skos:semanticRelation ;\n"+
          "   rdfs:label ?semrellabel ;\n"+
          "   a owl:ObjectProperty .\n"+       
          "?object  a skos:Concept ;\n"+
          "   skos:prefLabel ?preflabel2 .\n"+
          "}\n"+ 
          "}";
 
 
 
 
     //logger.trace("doGetTopic: SPARQL CONSTRUCT query sent to dispatcher:\n" + sparqlConstructQuery);
     queryResult = sparqlDispatcher.query(sparqlConstructQuery);
 
     bizData.put("navigation", queryResult);
     bizData.put("mode", mode);
 
     StringBuilder params = adminService.getMostOfTheRequestXML(req);
 
     // These will not be brought along unless we add it as request parameters, which is hackish.
     params.append("\n    <param key=\"prefix\">");
     params.append("\n      <value>dct: &lt;http://purl.org/dc/terms/&gt;</value>");
     params.append("\n      <value>rdfs: &lt;http://www.w3.org/2000/01/rdf-schema%23&gt;</value>");
     params.append("\n      <value>skos: &lt;http://www.w3.org/2004/02/skos/core%23&gt;</value>");
     params.append("\n    </param>");
     params.append("\n  </request>\n");
 
     bizData.put("request", params.toString());
     bizData.put("loggedin", loggedIn);
     bizData.put("searchparams", "<empty/>");
     System.gc();
     res.sendPage(format + "/sparql-result", bizData);
   }
 
 
   private String freeTextSearchString(AppleResponse res, AppleRequest req) {
     String defaultBooleanOperator = getProperty("sublima.default.boolean.operator");
     String chosenOperator = req.getCocoonRequest().getParameter("booleanoperator");
 
     boolean truncate = true;
 
     // Set right truncation or not, based on user choice
     if (req.getCocoonRequest().getParameter("exactmatch") != null) {
       truncate = false;
     }
 
 
     SearchService searchService;
 
     //Use user chosen boolean operator when it doesn't equal the default
     if (!chosenOperator.equalsIgnoreCase(defaultBooleanOperator)) {
       searchService = new SearchService(chosenOperator);
       logger.debug("SUBLIMA: Use " + chosenOperator + " as boolean operator for search");
     } else {
       searchService = new SearchService(defaultBooleanOperator);
       logger.debug("SUBLIMA: Use " + defaultBooleanOperator + " as boolean operator for search");
     }
 
     return searchService.buildSearchString(req.getCocoonRequest().getParameter("searchstring"), truncate);
   }
 
 
   public void doAdvancedSearch(AppleResponse res, AppleRequest req, boolean loggedIn) {
     // Get all parameteres from the HTML form as Map
     Map<String, String[]> parameterMap = new TreeMap<String, String[]>(createParametersMap(req.getCocoonRequest()));
     Map<String, Object> bizData = new HashMap<String, Object>();
 
     // Boolean that can be set to false if we don't want the actual search to be performed. Typically when the search string is empty.
     boolean doSearch = true;
 
     // Temporary to override the sparql query upopn freetext search
     boolean freetext = false;
     String searchStringOverriden = null;
 
     boolean doBigSearchAnyway = (parameterMap.get("dobigsearchanyway") != null);
     parameterMap.remove("dobigsearchanyway");
 
     // Create an XML structure of the search criterias. This could probably be nore generic.
     StringBuilder xmlSearchParametersBuffer = new StringBuilder();
     xmlSearchParametersBuffer.append("<c:searchparams xmlns:c=\"http://xmlns.computas.com/cocoon\">\n");
     if (req.getCocoonRequest().getParameter("searchstring") == null) {
       xmlSearchParametersBuffer.append("\t<c:searchstring></c:searchstring>\n");      
     } else {
       String searchstring = req.getCocoonRequest().getParameter("searchstring");
       searchstring = searchstring.replace("&", "&amp;");
       searchstring = searchstring.replace("<", "&lt;");
       searchstring = searchstring.replace(">", "&gt;");
       xmlSearchParametersBuffer.append("\t<c:searchstring>" + searchstring + "</c:searchstring>\n");
     }
     
     xmlSearchParametersBuffer.append("\t<c:operator>" + req.getCocoonRequest().getParameter("booleanoperator") + "</c:operator>\n");
     xmlSearchParametersBuffer.append("\t<c:deepsearch>" + req.getCocoonRequest().getParameter("deepsearch") + "</c:deepsearch>\n");
     xmlSearchParametersBuffer.append("\t<c:sortby>" + req.getCocoonRequest().getParameter("sort") + "</c:sortby>\n");
     xmlSearchParametersBuffer.append("\t<c:exactmatch>" + req.getCocoonRequest().getParameter("exactmatch") + "</c:exactmatch>\n");
     xmlSearchParametersBuffer.append("</c:searchparams>\n");
 
 
     if ("resource".equalsIgnoreCase(mode)) {
       parameterMap.put("prefix", new String[]{"dct: <http://purl.org/dc/terms/>", "rdfs: <http://www.w3.org/2000/01/rdf-schema#>", "skos: <http://www.w3.org/2004/02/skos/core#>"});
       parameterMap.put("interface-language", new String[]{req.getSitemapParameter("interface-language")});
       parameterMap.put("dct:identifier", new String[]{getProperty("sublima.base.url") + "resource/"
               + req.getSitemapParameter("name")});
       parameterMap.put("dct:subject/skos:prefLabel", new String[]{""});
     }
 
     if (parameterMap.get("searchstring") != null) {
       if (req.getCocoonRequest().getParameter("searchstring").trim().equalsIgnoreCase("")) {
         doSearch = false;
       } else {
         // When true, we override the searchstring later in the code
         freetext = true;
         searchStringOverriden = freeTextSearchString(res, req);
         parameterMap.put("searchstring", new String[]{searchStringOverriden});
         parameterMap.remove("booleanoperator");
         parameterMap.remove("sort");
         parameterMap.remove("exactmatch");
       }
     }
     // sending the result
     String sparqlQuery = null;
     String countNumberOfHitsQuery = null;
     // Check for magic prefixes
     if (parameterMap.get("prefix") != null) {
       // Calls the Form2SPARQL service with the parameterMap which returns
       // a SPARQL as String
       Form2SparqlService form2SparqlService = new Form2SparqlService(parameterMap.get("prefix"), parameterMap.get("freetext-field"));
       parameterMap.remove("prefix"); // The prefixes are magic variables
       parameterMap.remove("freetext-field"); // The freetext-fields are magic variables too
       parameterMap.remove("res-view"); //  As is this
       sparqlQuery = form2SparqlService.convertForm2Sparql(parameterMap);
       countNumberOfHitsQuery = form2SparqlService.convertForm2SparqlCount(parameterMap, Integer.valueOf(SettingsService.getProperty("sublima.search.maxhitsbeforestop")));
     } else {
       res.sendStatus(400);
     }
 
     //logger.trace("doAdvancedSearch: SPARQL query sent to dispatcher:\n" + sparqlQuery);
 
     Object queryResult;
     if (doBigSearchAnyway) {
         // This will do the search despite it being large, thus populating the cache
         queryResult = sparqlDispatcher.query(sparqlQuery);
     } else if (doSearch) {
         if (adminService.isAboveMaxNumberOfHits(countNumberOfHitsQuery)) {
             // We are above the threshold, lets see if we have it cached
             CachingService cache = new CachingService();
             MemcachedClient memcached = cache.connect();
 
             String cacheString = sparqlQuery.replaceAll("\\s+", " ") + SettingsService.getProperty("sublima.base.url");
             String cacheKey = String.valueOf(cacheString.hashCode()); // We could parse the query first to get a better key
             //  logger.trace("SPARQLdispatcher hashing for use as key.\n" + cacheString + "\n");
             if (cache.ask(memcached, cacheKey)) {
                 logger.debug("SearchController found the query in the cache.");
                 queryResult = sparqlDispatcher.query(sparqlQuery); // Cache will be used in here.
             } else {
                 queryResult = "<empty/>";
             }
         } else {
             // We are below the threshold, do the search. Cache will be checked
             queryResult = sparqlDispatcher.query(sparqlQuery);
         }
     }
     else {
         queryResult = "<empty/>";
     }
 
 
     if (searchStringOverriden == null) {
         searchStringOverriden = req.getCocoonRequest().getParameter("dct:subject/all-labels");
     }
 
     if (searchStringOverriden == null) {
         searchStringOverriden = req.getCocoonRequest().getParameter("dct:subject/skos:prefLabel");
     }
 
     Object navigationResults = "<empty></empty>";
     if (searchStringOverriden != null && searchStringOverriden.length() > 0) {
         // Now get the matching topics
         String sparqlTopicsQuery =
                 "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"+
                 "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> \n"+
                 "PREFIX owl: <http://www.w3.org/2002/07/owl#> \n"+
                 "PREFIX wdr: <http://www.w3.org/2007/05/powder#>\n" +
                 "PREFIX pf: <http://jena.hpl.hp.com/ARQ/property#> \n" +
                 "DESCRIBE ?subject WHERE {\n"+
                 "  ?lit pf:textMatch \"\"\"" + searchStringOverriden + "\"\"\" . " +
                 "  ?subject skos:prefLabel ?lit .\n" +
                 "  ?subject wdr:describedBy <http://sublima.computas.com/status/godkjent_av_administrator> . \n" +                        
                 "\n}\n";
         
         //logger.trace("doAdvanced: SPARQL query to get topics sent to dispatcher:\n" + sparqlTopicsQuery);
         navigationResults = sparqlDispatcher.query(sparqlTopicsQuery);
     }
 
     bizData.put("result-list", queryResult);
     bizData.put("navigation", navigationResults);
     bizData.put("mode", mode);
 
     bizData.put("searchparams", xmlSearchParametersBuffer.toString());
     StringBuilder params = adminService.getMostOfTheRequestXML(req);
     params.append("\n  </request>\n");
 
     bizData.put("request", params.toString());
     bizData.put("loggedin", loggedIn);
     System.gc();
     res.sendPage(format + "/sparql-result", bizData);
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
 
   public void setAppMan(ApplicationManager appMan) {
     this.appMan = appMan;
   }
 }
