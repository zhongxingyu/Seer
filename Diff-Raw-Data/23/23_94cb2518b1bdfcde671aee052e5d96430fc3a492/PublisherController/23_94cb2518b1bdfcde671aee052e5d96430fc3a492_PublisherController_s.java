 package com.computas.sublima.app.controller.admin;
 
 import com.computas.sublima.app.service.AdminService;
 import com.computas.sublima.app.service.AutocompleteCache;
 import com.computas.sublima.app.service.Form2SparqlService;
 import com.computas.sublima.app.service.LanguageService;
 import com.computas.sublima.query.SparqlDispatcher;
 import com.computas.sublima.query.SparulDispatcher;
 import static com.computas.sublima.query.service.SettingsService.getProperty;
 import com.hp.hpl.jena.sparql.util.StringUtils;
 import org.apache.cocoon.auth.ApplicationUtil;
 import org.apache.cocoon.auth.User;
 import org.apache.cocoon.components.flow.apples.AppleRequest;
 import org.apache.cocoon.components.flow.apples.AppleResponse;
 import org.apache.cocoon.components.flow.apples.StatelessAppleController;
 import org.apache.cocoon.environment.Request;
 import org.apache.log4j.Logger;
 
 import java.io.IOException;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.TreeMap;
 
 /**
  * @author: mha
  * Date: 31.mar.2008
  */
 public class PublisherController implements StatelessAppleController {
 
   private SparqlDispatcher sparqlDispatcher;
   private SparulDispatcher sparulDispatcher;
   AdminService adminService = new AdminService();
   private ApplicationUtil appUtil = new ApplicationUtil();
   private String mode;
   private String submode;
   private User user;
   String[] completePrefixArray = {
           "PREFIX dct: <http://purl.org/dc/terms/>",
           "PREFIX foaf: <http://xmlns.com/foaf/0.1/>",
           "PREFIX sub: <http://xmlns.computas.com/sublima#>",
           "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
           "PREFIX wdr: <http://www.w3.org/2007/05/powder#>",
           "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>",
           "PREFIX lingvoj: <http://www.lingvoj.org/ontology#>"};
 
   String completePrefixes = StringUtils.join("\n", completePrefixArray);
   private String userPrivileges = "<empty/>";
 
  private static Logger logger = Logger.getLogger(AdminController.class);
 
   @SuppressWarnings("unchecked")
   public void process(AppleRequest req, AppleResponse res) throws Exception {
 
     if (appUtil.getUser() != null) {
       user = appUtil.getUser();
       userPrivileges = adminService.getRolePrivilegesAsXML(user.getAttribute("role").toString());
     }
 
     LanguageService langServ = new LanguageService();
     String language = langServ.checkLanguage(req, res);
 
     logger.trace("PublisherController: Language from sitemap is " + req.getSitemapParameter("interface-language"));
     logger.trace("PublisherController: Language from service is " + language);
 
 
     this.mode = req.getSitemapParameter("mode");
     this.submode = req.getSitemapParameter("submode");
 
     if ("utgivere".equalsIgnoreCase(mode)) {
       if ("".equalsIgnoreCase(submode) || submode == null) {
         Map<String, Object> bizData = new HashMap<String, Object>();
         bizData.put("facets", adminService.getMostOfTheRequestXMLWithPrefix(req) + "</c:request>");
         System.gc();
         res.sendPage("xml2/utgivere", bizData);
       } else if ("ny".equalsIgnoreCase(submode) && req.getCocoonRequest().getParameter("actionbuttondelete") == null) {
         editPublisher(res, req);
       } else if ("ny".equalsIgnoreCase(submode) && req.getCocoonRequest().getParameter("actionbuttondelete") != null) { // delete button pressed
         deletePublisher(res, req);
       } else if ("alle".equalsIgnoreCase(submode)) {
         showPublishersIndex(res, req, null);
       } else {
         showPublisherByURI(res, req, null, null);
       }
     } else {
       res.sendStatus(404);
     }
   }
 
   /**
    * Method to delete a publisher
    *
    * @param res
    * @param req
    */
   private void deletePublisher(AppleResponse res, AppleRequest req) {
     StringBuilder messageBuffer = new StringBuilder();
     messageBuffer.append("<c:messages xmlns:i18n=\"http://apache.org/cocoon/i18n/2.1\" xmlns:c=\"http://xmlns.computas.com/cocoon\">\n");
     String deleteString = "DELETE {\n" +
             "<" + req.getCocoonRequest().getParameter("the-resource").trim() + "> ?a ?o.\n" +
             "} WHERE {\n" +
             "<" + req.getCocoonRequest().getParameter("the-resource").trim() + "> ?a ?o. }";
 
     boolean deletePublisherSuccess = sparulDispatcher.query(deleteString);
 
     logger.trace("PublisherController.deletePublisher --> DELETE RESOURCE QUERY:\n" + deleteString);
     logger.trace("PublisherController.deletePublisher --> DELETE RESOURCE QUERY RESULT: " + deletePublisherSuccess);
 
     if (deletePublisherSuccess) {
       messageBuffer.append("<c:message><i18n:text key=\"publisher.deletedok\">Utgiver slettet!</i18n:text></c:message>\n");
     } else {
       messageBuffer.append("<c:message><i18n:text key=\"publisher.deletefailed\">Feil ved sletting av utgiver</i18n:text></c:message>\n");
     }
 
     messageBuffer.append("</c:messages>");
     Map<String, Object> bizData = new HashMap<String, Object>();
     bizData.put("messages", messageBuffer.toString());
     bizData.put("publisherdetails", "<empty/>");
     bizData.put("languages", adminService.getAllLanguages());
     bizData.put("facets", adminService.getMostOfTheRequestXMLWithPrefix(req) + "</c:request>");
     bizData.put("userprivileges", userPrivileges);
     System.gc();
     res.sendPage("xml2/utgiver", bizData);
   }
 
   /**
    * Method to validate the request upon insert of new resource.
    * Checks all parameters and gives error message if one or more required values are null
    *
    * @param req
    * @return
    */
   private String validateRequest(AppleRequest req) {
     StringBuilder validationMessages = new StringBuilder();
 
 
     return validationMessages.toString();
   }
 
   /**
    * Method to insert a new publisher
    *
    * @param res - AppleResponse
    * @param req - AppleRequest
    */
   private void insertPublisherByName
           (AppleResponse
                   res, AppleRequest
                   req) {
 
     StringBuilder messageBuffer = new StringBuilder();
     String publishername = req.getCocoonRequest().getParameter("new_publisher");
     String description = req.getCocoonRequest().getParameter("dct:description");
 
     if ("".equalsIgnoreCase(publishername) || publishername == null) {
       messageBuffer.append("<c:message><i18n:text key=\"publisher.noname\">Utgivernavn må angis</i18n:text></c:message>\n");
     } else {
 
       // Check if a publisher by that name already exists
       //Find the publisher URI based on name
       String findPublisherByNameQuery = StringUtils.join("\n", new String[]{
               completePrefixes,
               "SELECT ?publisher ?name",
               "WHERE {",
               "?publisher a foaf:Agent ;",
               "           foaf:name ?name  .",
               "FILTER regex(str(?name), \"^" + publishername + "\", \"i\" )",
               //"FILTER langMatches( lang(?name), \"*\" )",
               "}"});
 
       logger.trace("AdminController.insertPublisherByName() --> SPARQL query sent to dispatcher: \n" + findPublisherByNameQuery);
       Object queryResult = sparqlDispatcher.query(findPublisherByNameQuery);
 
       if (queryResult.toString().contains(publishername)) {
         messageBuffer.append("<c:message><i18n:text key=\"publisher.exists\">En utgiver med det navnet finnes allerede registrert</i18n:text></c:message>\n");
         showPublishersIndex(res, req, messageBuffer.toString());
         return;
       }
 
       String publisherURI = publishername.replace(" ", "_");
       publisherURI = publisherURI.replace(".", "_");
       publisherURI = publisherURI.replace(",", "_");
       publisherURI = publisherURI.replace("/", "_");
       publisherURI = publisherURI.replace("-", "_");
       publisherURI = publisherURI.replace("'", "_");
       publisherURI = getProperty("sublima.base.url") + "agent/" + publisherURI;
 
 
       String insertPublisherByName =
               "PREFIX dct: <http://purl.org/dc/terms/>\n" +
                       "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" +
                       "INSERT\n" +
                       "{\n" +
                       "<" + publisherURI + "> a foaf:Agent ;\n" +
                       "foaf:name \"" + publishername + "\"@no ;\n" +
                       "dct:description \"\"\"" + description + "\"\"\"@no . \n" +
                       "}";
 
       logger.info("updatePublisherByURI() ---> " + publisherURI + " -- SPARUL INSERT  --> " + insertPublisherByName);
       boolean success = sparulDispatcher.query(insertPublisherByName);
       logger.info("updatePublisherByURI() ---> " + publisherURI + " -- INSERT NEW NAME --> " + success);
 
       if (success) {
         messageBuffer.append("<c:message><i18n:text key=\"publisher.saveok\">Lagring av ny utgiver vellykket</i18n:text></c:message>\n");
       } else {
         messageBuffer.append("<c:message><i18n:text key=\"publisher.saveerror\">Feil ved lagring av utgiver</i18n:text></c:message>\n");
       }
     }
 
     showPublishersIndex(res, req, messageBuffer.toString());
   }
 
   /**
    * Method to update a publisher
    *
    * @param res - AppleResponse
    * @param req - AppleRequest
    */
   private void editPublisher
           (AppleResponse
                   res, AppleRequest
                   req) {
 
     if (req.getCocoonRequest().getMethod().equalsIgnoreCase("GET")) {
       showPublisherByURI(res, req, null, req.getCocoonRequest().getParameter("the-resource"));
 
     } else if (req.getCocoonRequest().getMethod().equalsIgnoreCase("POST")) {
       StringBuilder messageBuffer = new StringBuilder();
 
       Map<String, String[]> parameterMap = new TreeMap<String, String[]>(createParametersMap(req.getCocoonRequest()));
 
       //logger.info("updatePublisherByURI() ---> " + publisheruri + " -- SPARUL DELETE  --> " + deleteString);
 
       Form2SparqlService form2SparqlService = new Form2SparqlService(parameterMap.get("prefix"));
       parameterMap.remove("prefix"); // The prefixes are magic variables
       parameterMap.remove("actionbutton"); // The name of the submit button
       if (parameterMap.get("subjecturi-prefix") != null) {
         parameterMap.put("subjecturi-prefix", new String[]{getProperty("sublima.base.url") +
                 parameterMap.get("subjecturi-prefix")[0]});
       }
 
       String sparqlQuery = null;
       try {
         sparqlQuery = form2SparqlService.convertForm2Sparul(parameterMap);
         logger.info("editPublisher() ---> SPARUL INSERT  --> " + sparqlQuery);
       }
       catch (IOException e) {
         messageBuffer.append("<c:message><i18n:text key=\"publisher.saveerror\">Feil ved lagring av utgiver</i18n:text></c:message>\n");
       }
 
       boolean success = sparulDispatcher.query(sparqlQuery);
       logger.info("editPublisher() ---> INSERT --> " + success);
 
       if (success) {
         messageBuffer.append("<c:message><i18n:text key=\"publisher.updated\">Utgiveren oppdatert</i18n:text></c:message>\n");
       } else {
         messageBuffer.append("<c:message><i18n:text key=\"publisher.updatefailed\">Feil ved oppdatering</i18n:text></c:message>\n");
       }
 
       //Invalidate the Topic cache for autocompletion
       AutocompleteCache.invalidatePublisherCache();
       showPublisherByURI(res, req, messageBuffer.toString(), form2SparqlService.getURI());
       AutocompleteCache.getPublisherSet();
     }
   }
 
   /**
    * Method to create the individual publisher page based on the publisher name.
    * The page presents the publisher and all resources from that publisher.
    *
    * @param res          - AppleResponse
    * @param req          - AppleRequest
    * @param publisherURI
    */
 
   private void showPublisherByURI
           (AppleResponse
                   res, AppleRequest
                   req, String
                   messages, String
                   publisherURI) {
     //String publisheruri = this.submode;
 
     StringBuilder messageBuffer = new StringBuilder();
     messageBuffer.append("<c:messages xmlns:i18n=\"http://apache.org/cocoon/i18n/2.1\" xmlns:c=\"http://xmlns.computas.com/cocoon\">\n");
     messageBuffer.append(messages);
 
     if ("".equalsIgnoreCase(publisherURI) || publisherURI == null) {
       publisherURI = req.getCocoonRequest().getParameter("uri");
     }
     //Find the publisher URI based on name
     String findPublisherByURIQuery = StringUtils.join("\n", new String[]{
             completePrefixes,
             "DESCRIBE <" + publisherURI + "> ?resource ?subject",
             "WHERE {",
             "OPTIONAL { ?resource dct:publisher <" + publisherURI + "> .",
             "?resource dct:subject ?subject . }",
             "}"});
 
 
     logger.trace("AdminController.showPublisherByURI() --> SPARQL query sent to dispatcher: \n" + findPublisherByURIQuery);
     Object queryResult = sparqlDispatcher.query(findPublisherByURIQuery);
 
     messageBuffer.append("</c:messages>");
     Map<String, Object> bizData = new HashMap<String, Object>();
     bizData.put("messages", messageBuffer.toString());
     bizData.put("publisherdetails", queryResult);
     bizData.put("languages", adminService.getAllLanguages());
     bizData.put("facets", adminService.getMostOfTheRequestXMLWithPrefix(req) + "</c:request>");
     bizData.put("userprivileges", userPrivileges);
     System.gc();
     res.sendPage("xml2/utgiver", bizData);
   }
 
   /**
    * Method to display a list of all publishers. These link to a page where each publisher can edited.
    *
    * @param res - AppleResponse
    * @param req - AppleRequest
    */
   private void showPublishersIndex
           (AppleResponse
                   res, AppleRequest
                   req, String
                   messages) {
 
     StringBuilder messageBuffer = new StringBuilder();
     messageBuffer.append("<c:messages xmlns:i18n=\"http://apache.org/cocoon/i18n/2.1\" xmlns:c=\"http://xmlns.computas.com/cocoon\">\n");
     messageBuffer.append(messages);
 
     String queryString = StringUtils.join("\n", new String[]{
             completePrefixes,
             "SELECT DISTINCT ?publisher ?name",
             "WHERE {",
             "?publisher foaf:name ?name ;",
             "           a foaf:Agent .",
             "}",
             "ORDER BY ?name"});
 
     logger.trace("AdminController.showPublishersIndex() --> SPARQL query sent to dispatcher: \n" + queryString);
     Object queryResult = sparqlDispatcher.query(queryString);
 
     messageBuffer.append("</c:messages>");
 
     Map<String, Object> bizData = new HashMap<String, Object>();
     bizData.put("messages", messageBuffer.toString());
     bizData.put("publisherlist", queryResult);
     bizData.put("facets", adminService.getMostOfTheRequestXMLWithPrefix(req) + "</c:request>");
     System.gc();
     res.sendPage("xml2/utgivere_alle", bizData);
   }
 
   public void setSparqlDispatcher
           (SparqlDispatcher
                   sparqlDispatcher) {
     this.sparqlDispatcher = sparqlDispatcher;
   }
 
   public void setSparulDispatcher
           (SparulDispatcher
                   sparulDispatcher) {
     this.sparulDispatcher = sparulDispatcher;
   }
 
   //todo Move to a Service-class
   private Map<String, String[]> createParametersMap
           (Request
                   request) {
     Map<String, String[]> result = new HashMap<String, String[]>();
     Enumeration parameterNames = request.getParameterNames();
     while (parameterNames.hasMoreElements()) {
       String paramName = (String) parameterNames.nextElement();
       result.put(paramName, request.getParameterValues(paramName));
     }
     return result;
   }
 
 }
 
