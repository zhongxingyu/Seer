 package com.computas.sublima.app.controller;
 
 import com.computas.sublima.app.service.AdminService;
 import com.computas.sublima.app.service.Form2SparqlService;
 import com.computas.sublima.app.service.LanguageService;
 import com.computas.sublima.query.SparqlDispatcher;
 import com.computas.sublima.query.service.CachingService;
 import com.computas.sublima.query.service.MappingService;
 import com.computas.sublima.query.service.SearchService;
 import com.computas.sublima.query.service.SettingsService;
 import static com.computas.sublima.query.service.SettingsService.getProperty;
 import net.spy.memcached.MemcachedClient;
 import org.apache.cocoon.auth.ApplicationManager;
 import org.apache.cocoon.components.flow.apples.AppleRequest;
 import org.apache.cocoon.components.flow.apples.AppleResponse;
 import org.apache.cocoon.components.flow.apples.StatelessAppleController;
 import org.apache.cocoon.environment.Request;
 import org.apache.log4j.Logger;
 
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.TreeMap;
 
 public class SearchController implements StatelessAppleController {
   private SparqlDispatcher sparqlDispatcher;
   private ApplicationManager appMan;
   private AdminService adminService = new AdminService();
   private MappingService ms = new MappingService();
   private String mode;
   private String format;
   boolean loggedIn;
 
   private static Logger logger = Logger.getLogger(SearchController.class);
   private static Logger heavylogger = Logger.getLogger("HeavyLogger");
 
   @SuppressWarnings("unchecked")
   public void process(AppleRequest req, AppleResponse res) throws Exception {
     loggedIn = appMan.isLoggedIn("Sublima");
 
     this.mode = req.getSitemapParameter("mode");
     this.format = req.getSitemapParameter("format");
     if (format == null || "html".equalsIgnoreCase(format) || "".equalsIgnoreCase(format)) {
       format = "xml";
     }
 
     LanguageService langServ = new LanguageService();
     String language = langServ.checkLanguage(req, res);
 
     logger.trace("SearchController: Language from sitemap is " + req.getSitemapParameter("interface-language"));
     logger.trace("SearchController: Language from service is " + language);
 
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
       bizData.put("mediatypes", adminService.getDistinctAndUsedLabels("dct:MediaType", "dct:type"));
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
       doAdvancedSearch(res, req);
       return;
     }
 
     if ("topic".equalsIgnoreCase(mode)) {
       doGetTopic(res, req);
     }
   }
 
   private void doGetTopic(AppleResponse res, AppleRequest req) {
 
     String subject = "<" + getProperty("sublima.base.url")
             + "topic/" + req.getSitemapParameter("topic") + ">";
 
     Map<String, Object> bizData = new HashMap<String, Object>();
     bizData.put("result-list", adminService.getTopicDetailsForTopicPage(subject));
 
     bizData.put("navigation", adminService.getNavigationDetailsForTopicPage(subject));
     bizData.put("mode", mode);
 
     StringBuilder params = adminService.getMostOfTheRequestXML(req);
 
     // These will not be brought along unless we add it as request parameters, which is hackish.
     params.append("\n    <param key=\"wdr:describedBy\">");
     params.append("\n      <value>http://sublima.computas.com/status/godkjent_av_administrator</value>");
     params.append("\n    </param>");
     params.append("\n    <param key=\"prefix\">");
     params.append("\n      <value>wdr: &lt;http://www.w3.org/2007/05/powder%23&gt;</value>");
     params.append("\n      <value>dct: &lt;http://purl.org/dc/terms/&gt;</value>");
     params.append("\n      <value>rdfs: &lt;http://www.w3.org/2000/01/rdf-schema%23&gt;</value>");
     params.append("\n      <value>skos: &lt;http://www.w3.org/2004/02/skos/core%23&gt;</value>");
     params.append("\n    </param>");
     params.append("\n  </request>\n");
 
     bizData.put("request", params.toString());
     bizData.put("loggedin", loggedIn);
     bizData.put("searchparams", "<empty/>");
     bizData.put("messages", "<empty/>");
     bizData.put("abovemaxnumberofhits", "false");
     bizData.put("comment", "<empty/>");
 
     System.gc();
     res.sendPage(format + "/sparql-result", bizData);
   }
 
 
   private String freeTextSearchString(String searchstring, String booleanoperator, boolean exactmatch, boolean advancedsearch) {
     String defaultBooleanOperator = getProperty("sublima.default.boolean.operator");
 
     SearchService searchService;
 
     //Use user chosen boolean operator when it doesn't equal the default
     if (!booleanoperator.equalsIgnoreCase(defaultBooleanOperator)) {
       searchService = new SearchService(booleanoperator);
       logger.debug("SUBLIMA: Use " + booleanoperator + " as boolean operator for search");
     } else {
       searchService = new SearchService(defaultBooleanOperator);
       logger.debug("SUBLIMA: Use " + defaultBooleanOperator + " as boolean operator for search");
     }
 
     String result = searchService.buildSearchString(searchstring, !exactmatch, advancedsearch);
     searchService = null;
 
     return result;
   }
 
 
   public void doAdvancedSearch(AppleResponse res, AppleRequest req) {
     // Get all parameteres from the HTML form as Map
     Map<String, String[]> parameterMap = new TreeMap<String, String[]>(createParametersMap(req.getCocoonRequest()));
     Map<String, Object> bizData = new HashMap<String, Object>();
     boolean abovemaxnumberofhits = false;
 
     StringBuilder messageBuffer = new StringBuilder();
     messageBuffer.append("<c:messages xmlns:i18n=\"http://apache.org/cocoon/i18n/2.1\" xmlns:c=\"http://xmlns.computas.com/cocoon\">\n");
 
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
 
         searchStringOverriden = freeTextSearchString(req.getCocoonRequest().getParameter("searchstring"), req.getCocoonRequest().getParameter("booleanoperator"), (req.getCocoonRequest().getParameter("exactmatch") != null), false);
         parameterMap.put("searchstring", new String[]{searchStringOverriden});
         parameterMap.remove("booleanoperator");
         parameterMap.remove("sort");
         parameterMap.remove("sortorder");
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
 
       boolean truncate = Boolean.valueOf(SettingsService.getProperty("sublima.advancedsearch.truncate"));
 
       // Test if some of the free text fields in the advanced search form exists, if so we build a proper search string
       if (req.getCocoonRequest().getParameter("dct:title") != null && !"".equals(req.getCocoonRequest().getParameter("dct:title"))) {
         parameterMap.remove("dct:title");
         form2SparqlService.setTruncate(truncate);
         searchStringOverriden = freeTextSearchString(req.getCocoonRequest().getParameter("dct:title"), "AND", true, true);
         parameterMap.put("dct:title", new String[]{searchStringOverriden});
       }
 
       if (req.getCocoonRequest().getParameter("dct:subject/sub:literals") != null && !"".equals(req.getCocoonRequest().getParameter("dct:subject/sub:literals"))) {
         parameterMap.remove("dct:subject/sub:literals");
         form2SparqlService.setTruncate(truncate);
         searchStringOverriden = freeTextSearchString(req.getCocoonRequest().getParameter("dct:subject/sub:literals"), "AND", true, true);
         parameterMap.put("dct:subject/sub:literals", new String[]{searchStringOverriden});
       }
 
       if (req.getCocoonRequest().getParameter("dct:description") != null && !"".equals(req.getCocoonRequest().getParameter("dct:description"))) {
         parameterMap.remove("dct:description");
         form2SparqlService.setTruncate(truncate);
         searchStringOverriden = freeTextSearchString(req.getCocoonRequest().getParameter("dct:description"), "AND", true, true);
         parameterMap.put("dct:description", new String[]{searchStringOverriden});
 
       }
 
       if (req.getCocoonRequest().getParameter("dct:publisher/foaf:name") != null && !"".equals(req.getCocoonRequest().getParameter("dct:publisher/foaf:name"))) {
         parameterMap.remove("dct:publisher/foaf:name");
         form2SparqlService.setTruncate(truncate);
         searchStringOverriden = freeTextSearchString(req.getCocoonRequest().getParameter("dct:publisher/foaf:name"), "AND", true, true);
         parameterMap.put("dct:publisher/foaf:name", new String[]{searchStringOverriden});
       }
 
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
           abovemaxnumberofhits = false;
         } else {
           Request r = req.getCocoonRequest();
           String uri = r.getScheme() + "://" + r.getServerName();
           if (r.getServerPort() != 80) {
             uri = uri + ":" + r.getServerPort();
           }
           uri = uri + r.getRequestURI() + "?dobigsearchanyway=true&" + r.getQueryString();
           heavylogger.info(uri);
           queryResult = "<empty/>";
           abovemaxnumberofhits = true;
         }
       } else {
         // We are below the threshold, do the search. Cache will be checked
         queryResult = sparqlDispatcher.query(sparqlQuery);
         abovemaxnumberofhits = false;
       }
     } else {
       queryResult = "<empty/>";
     }
 
     Object navigationResults = "<empty></empty>";
     if (searchStringOverriden != null && searchStringOverriden.length() > 0) {
       // Now get the matching topics
       String sparqlTopicsQuery = null;
 
       if (!searchStringOverriden.isEmpty() && !"''".equals(searchStringOverriden)) {
         sparqlTopicsQuery = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
                 "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> \n" +
                 "PREFIX owl: <http://www.w3.org/2002/07/owl#> \n" +
                 "PREFIX wdr: <http://www.w3.org/2007/05/powder#>\n" +
                 "PREFIX sub: <http://xmlns.computas.com/sublima#>\n" +
                 "DESCRIBE ?subject WHERE {\n" +
                 "  ?subject sub:literals ?lit .\n" +
                 "  ?lit <bif:contains> \"\"\"" + searchStringOverriden + "\"\"\" . \n" +
                 "  ?subject a skos:Concept .\n" +
                 "  ?subject wdr:describedBy <http://sublima.computas.com/status/godkjent_av_administrator> . \n" +
                 "\n}\n";
 
         //logger.trace("doAdvanced: SPARQL query to get topics sent to dispatcher:\n" + sparqlTopicsQuery);
         navigationResults = sparqlDispatcher.query(sparqlTopicsQuery);
       } else {
         navigationResults = "<empty/>";
       }
     }
 
     bizData.put("result-list", queryResult);
     bizData.put("navigation", navigationResults);
     bizData.put("mode", mode);
 
     bizData.put("searchparams", xmlSearchParametersBuffer.toString());
     StringBuilder params = adminService.getMostOfTheRequestXML(req);
     params.append("\n  </request>\n");
 
     messageBuffer.append("</c:messages>");
 
     bizData.put("request", params.toString());
     bizData.put("loggedin", loggedIn);
     bizData.put("messages", messageBuffer.toString());
     bizData.put("abovemaxnumberofhits", abovemaxnumberofhits);
     bizData.put("comment", "<empty/>");
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
