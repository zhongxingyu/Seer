 package com.computas.sublima.app.controller;
 
 import com.computas.sublima.app.service.Form2SparqlService;
 import com.computas.sublima.app.adhoc.ImportData;
 import com.computas.sublima.query.SparqlDispatcher;
 import com.computas.sublima.query.SparulDispatcher;
 import com.computas.sublima.query.service.AdminService;
 import com.hp.hpl.jena.sparql.util.StringUtils;
 import org.apache.cocoon.components.flow.apples.AppleRequest;
 import org.apache.cocoon.components.flow.apples.AppleResponse;
 import org.apache.cocoon.components.flow.apples.StatelessAppleController;
 import org.apache.cocoon.environment.Request;
 import org.apache.log4j.Logger;
 
 import java.io.IOException;
 import java.net.URLEncoder;
 import java.util.*;
 
 /**
  * @author: mha
  * Date: 31.mar.2008
  */
 public class AdminController implements StatelessAppleController {
 
     private SparqlDispatcher sparqlDispatcher;
     private SparulDispatcher sparulDispatcher;
     AdminService adminService = new AdminService();
     private String mode;
     private String submode;
     String[] completePrefixArray = {
             "PREFIX dct: <http://purl.org/dc/terms/>",
             "PREFIX foaf: <http://xmlns.com/foaf/0.1/>",
             "PREFIX sub: <http://xmlns.computas.com/sublima#>",
             "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
             "PREFIX wdr: <http://www.w3.org/2007/05/powder#>",
             "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>",
             "PREFIX lingvoj: <http://www.lingvoj.org/ontology#>",
             "PREFIX portal-topic: <http://sublima.computas.com/topic-instance/>"};
     String completePrefixes = StringUtils.join("\n", completePrefixArray);
     String[] prefixArray = {
             "dct: <http://purl.org/dc/terms/>",
             "foaf: <http://xmlns.com/foaf/0.1/>",
             "sub: <http://xmlns.computas.com/sublima#>",
             "rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
             "wdr: <http://www.w3.org/2007/05/powder#>",
             "skos: <http://www.w3.org/2004/02/skos/core#>",
             "lingvoj: <http://www.lingvoj.org/ontology#>",
             "portal-topic: <http://sublima.computas.com/topic-instance/>"};
     String prefixes = StringUtils.join("\n", prefixArray);
 
     private static Logger logger = Logger.getLogger(AdminController.class);
 
     @SuppressWarnings("unchecked")
     public void process(AppleRequest req, AppleResponse res) throws Exception {
 
         this.mode = req.getSitemapParameter("mode");
         this.submode = req.getSitemapParameter("submode");
 
         if ("".equalsIgnoreCase(mode)) {
             res.sendPage("xml2/admin", null);
         } else if ("testsparql".equalsIgnoreCase(mode)) {
             if ("".equalsIgnoreCase(submode)) {
                 res.sendPage("xhtml/testsparql", null);
             } else {
                 String query = req.getCocoonRequest().getParameter("query");
                 res.redirectTo(req.getCocoonRequest().getContextPath() + "/sparql?query=" + URLEncoder.encode(query, "UTF-8"));
             }
         }
 
         // Linkcheck. Send the user to a page that displays a list of all resources affected.
         else if ("lenkesjekk".equalsIgnoreCase(mode)) {
             AdminService adminService = new AdminService();
             showLinkcheckResults(res, req);
 
             return;
         } else if ("insertpublisher".equalsIgnoreCase(mode)) {
             insertPublisherByName(res, req);
             return;
         } else if ("database".equalsIgnoreCase(mode)) {
             uploadForm(res,req);
         }
         // Publishers. Send the user to a page that displays a list of all publishers.
         else if ("utgivere".equalsIgnoreCase(mode)) {
             if ("".equalsIgnoreCase(submode) || submode == null) {
                 showPublishersIndex(res, req, null);
                 return;
             } else if ("updatepublisher".equalsIgnoreCase(submode)) {
                 updatePublisherByURI(res, req);
                 return;
             } else if ("insertpublisher".equalsIgnoreCase(submode)) {
                 insertPublisherByName(res, req);
                 return;
             } else {
                 showPublisherByURI(res, req, null, null);
                 return;
             }
         }
         // Resources.
         else if ("ressurser".equalsIgnoreCase(mode)) {
             if ("".equalsIgnoreCase(submode) || submode == null) {
                 showResourcesIndex(res, req);
                 return;
             } else if ("foreslaatte".equalsIgnoreCase(submode)) {
                 showSuggestedResources(res, req);
                 return;
             } else if ("ny".equalsIgnoreCase(submode)) {
                 editResource(res, req, "ny", null);
                 return;
             } else if ("edit".equalsIgnoreCase(submode)) {
                 editResource(res, req, "edit", null);
                 return;
             } else {
                 return;
             }
         } else if ("brukere".equalsIgnoreCase(mode)) {
             res.sendPage("xml2/brukere", null);
             return;
         } else if ("emner".equalsIgnoreCase(mode)) {
             res.sendPage("xml2/emner", null);
             return;
         } else {
             res.sendStatus(404);
             return;
         }
     }
 
   private void uploadForm(AppleResponse res, AppleRequest req) {
     if (req.getCocoonRequest().getMethod().equalsIgnoreCase("GET")) {
       res.sendPage("xml2/upload", null);
     } else if (req.getCocoonRequest().getMethod().equalsIgnoreCase("POST")) {
       String location = req.getCocoonRequest().getParameter("location");
       String type = req.getCocoonRequest().getParameter("type");
 
       ImportData importData = new ImportData();
       importData.load(location, type);
       res.sendPage("xml2/upload", null);
     }
   }
 
   /**
      * Method to add new resources and edit existing ones
      * Sparql queries for all topics, statuses, languages, media types and audience
      * is done and the results forwarded to the JX Template and XSLT.
      * <p/>
      * A query for the resource is done when the action is "edit". In case of "new" a blank
      * form is presented.
      *
      * @param res - AppleResponse
      * @param req - AppleRequest
      */
     private void editResource(AppleResponse res, AppleRequest req, String type, String messages) {
 
         boolean validated = true;
 
         // Get all list values
         String allTopics = adminService.getAllTopics();
         String allLanguages = adminService.getAllLanguages();
         String allMediatypes = adminService.getAllMediaTypes();
         String allAudiences = adminService.getAllAudiences();
         String allStatuses = adminService.getAllStatuses();
         String allPublishers = adminService.getAllPublishers();
 
         if (messages == null) {
             messages = "";
         }
 
         Map<String, Object> bizData = new HashMap<String, Object>();
         bizData.put("topics", allTopics);
         bizData.put("languages", allLanguages);
         bizData.put("mediatypes", allMediatypes);
         bizData.put("audience", allAudiences);
         bizData.put("status", allStatuses);
         bizData.put("publishers", allPublishers);
 
         // When GET present a blank form with listvalues or prefilled with resource
         if (req.getCocoonRequest().getMethod().equalsIgnoreCase("GET")) {
             bizData.put("tempvalues", "<empty></empty>");
 
             if ("ny".equalsIgnoreCase(type)) {
                 bizData.put("resource", "<empty></empty>");
                 bizData.put("mode", "new");
             } else {
                 bizData.put("resource", adminService.getResourceByURI(req.getCocoonRequest().getParameter("uri")));
                 bizData.put("mode", "edit");
             }
 
             bizData.put("messages", "<empty></empty>");
             res.sendPage("xml2/ressurs", bizData);
 
             // When POST try to save the resource. Return error messages upon failure, and success message upon great success
         } else if (req.getCocoonRequest().getMethod().equalsIgnoreCase("POST")) {
 
             StringBuffer tempValues = getTempValues(req);
             String tempPrefixes = "<c:tempvalues \n" +
                     "xmlns:topic=\"http://sublima.computas.com/topic/\"\n" +
                     "xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\"\n" +
                     "xmlns:wdr=\"http://www.w3.org/2007/05/powder#\"\n" +
                     "xmlns:lingvoj=\"http://www.lingvoj.org/ontology#\"\n" +
                     "xmlns:sioc=\"http://rdfs.org/sioc/ns#\"\n" +
                     "xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n" +
                     "xmlns:foaf=\"http://xmlns.com/foaf/0.1/\"\n" +
                     "xmlns:owl=\"http://www.w3.org/2002/07/owl#\"\n" +
                     "xmlns:dct=\"http://purl.org/dc/terms/\"\n" +
                     "xmlns:ti=\"http://sublima.computas.com/topic-instance/\"\n" +
                     "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\"\n" +
                     "xmlns:dcmitype=\"http://purl.org/dc/dcmitype/\"\n" +
                     "xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"\n" +
                     "xmlns:c=\"http://xmlns.computas.com/cocoon\"\n" +
                     "xmlns:sub=\"http://xmlns.computas.com/sublima#\">\n";
 
             // Check if all required fields are filled out, if not return error messages
             String validationMessages = validateRequest(req);
             if (!"".equalsIgnoreCase(validationMessages)) {
                 bizData.put("resource", "<empty></empty>");
                 bizData.put("tempvalues", tempPrefixes + tempValues.toString() + "</c:tempvalues>");
                 bizData.put("messages", "<messages>\n" + validationMessages + "\n</messages>\n");
                 bizData.put("mode", "temp");
 
                 res.sendPage("xml2/ressurs", bizData);
 
             } else {
                 Map<String, String[]> parameterMap = new TreeMap<String, String[]>(createParametersMap(req.getCocoonRequest()));
 
                 // If the user names a new publisher we persist this and use the new publisher URI in our INSERT later
                 if ("".equalsIgnoreCase(req.getCocoonRequest().getParameter("dct:publisher")) && (req.getCocoonRequest().getParameter("dct:publisher/foaf:Agent/foaf:name") != null || "".equalsIgnoreCase(req.getCocoonRequest().getParameter("dct:publisher/foaf:Agent/foaf:name")))) {
                     String publisheruri = adminService.insertPublisher(req.getCocoonRequest().getParameter("dct:publisher/foaf:Agent/foaf:name"));
                     if ("".equalsIgnoreCase(publisheruri)) {
                         messages += "<message>Feil ved tillegging av ny utgiver</message>";
                         validated = false;
 
                         /* //todo OO this
                        bizData.put("resource", "<empty></empty>");
                        res.sendPage("xml2/ressurs", bizData);*/
                     } else {
                         messages += "<message>" + req.getCocoonRequest().getParameter("dct:publisher/foaf:Agent/foaf:name") + " lagt til som ny utgiver.</message>";
                         // Remove empty publisher reference and add a new with the publisher uri
                         parameterMap.remove("dct:publisher");
                         parameterMap.remove("dct:publisher/foaf:Agent/foaf:name");
                         parameterMap.put("dct:publisher", new String[]{publisheruri});
                     }
                 }
 
                 parameterMap.put("interface-language", new String[]{"no"});
                 parameterMap.put("the-resource", new String[]{req.getCocoonRequest().getParameter("sub:url")});
 
                 // Generate a dct:identifier | dct:identifier		<http://sublima.computas.com/resource/samliv_net_001084> ;
                 String dctIdentifier = req.getCocoonRequest().getParameter("dct:title").replace(" ", "_");
                 dctIdentifier = dctIdentifier.replace(",", "_");
                 dctIdentifier = dctIdentifier.replace(".", "_");
                 dctIdentifier = "http://sublima.computas.com/resource/" + dctIdentifier;
 
                 parameterMap.put("dct:identifier", new String[]{dctIdentifier});
                 parameterMap.put("prefix", prefixArray);
 
                 //todo Fix med autorisasjon
                 parameterMap.put("dct:dateAccepted", new String[]{"2008-18-09T13:39:38"});
                 parameterMap.put("sub:committer", new String[]{"http://sublima.computas.com/user/det_usr00057"});
 
                 Form2SparqlService form2SparqlService = new Form2SparqlService(parameterMap.get("prefix"));
                 parameterMap.remove("prefix"); // The prefixes are magic variables
 
                 // If no validation failures at this point
                 if (validated) {
                     try {
                         String sparqlQuery = form2SparqlService.convertForm2Sparul(parameterMap);
                         logger.trace("AdminController.editResource --> INSERT QUERY:\n" + sparqlQuery);
                         validated = sparulDispatcher.query(sparqlQuery);
                         logger.trace("AdminController.editResource --> INSERT QUERY RESULT: " + validated);
                         if (validated) {
                             messages += "<message>Ny ressurs lagt til!</message>";
 
                         } else {
                             messages += "<message>Feil ved lagring av ny ressurs</message>";
                             bizData.put("resource", "<empty></empty>");
                         }
 
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                 }
 
                 if (validated) {
                     bizData.put("resource", adminService.getResourceByURI(req.getCocoonRequest().getParameter("sub:url")));
                     bizData.put("tempvalues", "<empty></empty>");
                     bizData.put("mode", "edit");
                 } else {
                     bizData.put("resource", "<empty></empty>");
                     bizData.put("tempvalues", "<temp>" + tempValues.toString() + "</temp>");
                     bizData.put("mode", "temp");
                 }
 
                 bizData.put("messages", messages);
 
                 res.sendPage("xml2/ressurs", bizData);
             }
 
 
         }
     }
 
     /**
      * Method to validate the request upon insert of new resource.
      * Checks all parameters and gives error message if one or more required values are null
      *
      * @param req
      * @return
      */
     private String validateRequest(AppleRequest req) {
         StringBuffer validationMessages = new StringBuffer();
 
         if ("".equalsIgnoreCase(req.getCocoonRequest().getParameter("dct:title")) || req.getCocoonRequest().getParameter("dct:title") == null) {
             validationMessages.append("<message>Tittel kan ikke vre blank</message>\n");
         }
 
         if ("".equalsIgnoreCase(req.getCocoonRequest().getParameter("sub:url")) || req.getCocoonRequest().getParameter("sub:url") == null) {
             validationMessages.append("<message>URL kan ikke vre blank</message>\n");
         }
 
         if (("".equalsIgnoreCase(req.getCocoonRequest().getParameter("dct:publisher")) || req.getCocoonRequest().getParameter("dct:publisher") == null) &&
                 ("".equalsIgnoreCase(req.getCocoonRequest().getParameter("dct:publisher/foaf:Agent/foaf:name")) || req.getCocoonRequest().getParameter("dct:publisher/foaf:Agent/foaf:name") == null)) {
             validationMessages.append("<message>En utgiver m velges, eller et nytt utgivernavn angis</message>\n");
         }
 
         if ("".equalsIgnoreCase(req.getCocoonRequest().getParameter("sub:url")) || req.getCocoonRequest().getParameter("sub:url") == null) {
             validationMessages.append("<message>URL kan ikke vre blank</message>\n");
         }
 
         if (req.getCocoonRequest().getParameterValues("dct:language") == null) {
             validationMessages.append("<message>Minst ett sprk m vre valgt</message>\n");
         }
 
         if (req.getCocoonRequest().getParameterValues("dct:MediaType") == null) {
             validationMessages.append("<message>Minst en mediatype m vre valgt</message>\n");
         }
 
         if (req.getCocoonRequest().getParameterValues("dct:audience") == null) {
             validationMessages.append("<message>Minst en mlgruppe m vre valgt</message>\n");
         }
 
         if (req.getCocoonRequest().getParameterValues("dct:subject") == null) {
             validationMessages.append("<message>Minst ett emne m vre valgt</message>\n");
         }
 
         if (req.getCocoonRequest().getParameter("wdr:DR") == null) {
             validationMessages.append("<message>En status m velges</message>\n");
         }
 
         return validationMessages.toString();
     }
 
     private StringBuffer getTempValues(AppleRequest req) {
         //Keep all selected values in case of validation error
         String temp_title = req.getCocoonRequest().getParameter("dct:title");
         String temp_uri = req.getCocoonRequest().getParameter("sub:url");
         String temp_description = req.getCocoonRequest().getParameter("dct:description");
         String temp_publisher = req.getCocoonRequest().getParameter("dct:publisher");
         String temp_added_publisher = req.getCocoonRequest().getParameter("dct:publisher/foaf:Agent/foaf:name");
         String[] temp_languages = req.getCocoonRequest().getParameterValues("dct:language");
         String[] temp_mediatypes = req.getCocoonRequest().getParameterValues("dct:MediaType");
         String[] temp_audiences = req.getCocoonRequest().getParameterValues("dct:audience");
         String[] temp_subjects = req.getCocoonRequest().getParameterValues("dct:subject");
         String temp_comment = req.getCocoonRequest().getParameter("rdfs:comment");
         String temp_status = req.getCocoonRequest().getParameter("wdr:DR");
 
         //Create an XML structure for the selected values, to use in the JX template
         StringBuffer xmlStructureBuffer = new StringBuffer();
         xmlStructureBuffer.append("<dct:title>" + temp_title + "</dct:title>\n");
         xmlStructureBuffer.append("<sub:url>" + temp_uri + "</sub:url>\n");
         xmlStructureBuffer.append("<dct:description>" + temp_description + "</dct:description>\n");
         xmlStructureBuffer.append("<dct:publisher>" + temp_publisher + "</dct:publisher>\n");
         xmlStructureBuffer.append("<foaf:Agent>" + temp_added_publisher + "</foaf:Agent>\n");
 
         if (temp_languages != null) {
             for (String s : temp_languages) {
                 //xmlStructureBuffer.append("<language>" + s + "</language>\n");
               xmlStructureBuffer.append("<dct:language rdf:description=\"" + s + "\"/>\n");
             }
         }
 
         if (temp_mediatypes != null) {
 
             for (String s : temp_mediatypes) {
                 xmlStructureBuffer.append("<dct:MediaType rdf:description=\"" + s + "\"/>\n");
             }
 
         }
 
         if (temp_audiences != null) {
 
             for (String s : temp_audiences) {
                 xmlStructureBuffer.append("<dct:audience rdf:description=\"" + s + "\"/>\n");
             }
 
         }
 
         if (temp_subjects != null) {
             for (String s : temp_subjects) {
                 xmlStructureBuffer.append("<dct:subject rdf:description=\"" + s + "\"/>\n");
             }
         }
 
         xmlStructureBuffer.append("<rdfs:comment>" + temp_comment + "</rdfs:comment>\n");
         xmlStructureBuffer.append("<wdr:DR>" + temp_status + "</wdr:DR>\n");
 
         return xmlStructureBuffer;
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
 
         String messages = "";
         String publishername = req.getCocoonRequest().getParameter("new_publisher");
 
         if ("".equalsIgnoreCase(publishername) || publishername == null) {
             messages = "<message>\nUtgivernavn m angis\n</message>";
         } else {
 
             // Check if a publisher by that name already exists
             //Find the publisher URI based on name
             String findPublisherByNameQuery = StringUtils.join("\n", new String[]{
                    prefixes,
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
                 messages = "<message>\nEn utgiver med det navnet finnes allerede registrert\n</message>";
                 showPublishersIndex(res, req, messages);
                 return;
             }
 
             String success = adminService.insertPublisher(publishername);
 
             if (!"".equalsIgnoreCase(success)) {
                 messages = "<message>\nLagring av ny utgiver vellykket\n</message>";
             } else {
                 messages = "<message>\nFeil ved lagring av ny utgiver\n</message>";
             }
         }
 
         showPublishersIndex(res, req, messages);
     }
 
     /**
      * Method to update a publisher
      *
      * @param res - AppleResponse
      * @param req - AppleRequest
      */
     private void updatePublisherByURI
             (AppleResponse
                     res, AppleRequest
                     req) {
         String publisheruri = req.getCocoonRequest().getParameter("uri");
         String publisherNewLang = req.getCocoonRequest().getParameter("new_lang");
         String publisherNewName = req.getCocoonRequest().getParameter("new_name");
         String messages = "";
 
 // Delete statement
         StringBuffer deleteStringBuffer = new StringBuffer();
         deleteStringBuffer.append(prefixes);
         deleteStringBuffer.append("DELETE {\n");
         deleteStringBuffer.append("<" + publisheruri + "> a foaf:Agent ;\n");
         deleteStringBuffer.append("foaf:name ?oldname .\n");
 
 // Where statement for the delete
         StringBuffer whereStringBuffer = new StringBuffer();
         whereStringBuffer.append("WHERE {\n");
         whereStringBuffer.append("<" + publisheruri + "> a foaf:Agent ;\n");
         whereStringBuffer.append("foaf:name ?oldname .\n");
         whereStringBuffer.append("FILTER ( ");
 
 // Insert statement
         StringBuffer insertStringBuffer = new StringBuffer();
         insertStringBuffer.append(prefixes);
         insertStringBuffer.append("INSERT {\n");
 
 // If user has added a new name for a new language
         if (!"".equalsIgnoreCase(publisherNewLang)
                 && publisherNewLang != null
                 && !"".equalsIgnoreCase(publisherNewName)
                 && publisherNewName != null) {
 
             insertStringBuffer.append("<" + publisheruri + "> a foaf:Agent ;\n");
             insertStringBuffer.append("foaf:name" + " \"" + publisherNewName + "\"" + "@" + publisherNewLang + " .\n");
         }
 
         Map<String, String> parameterMap = req.getCocoonRequest().getParameters();
         ArrayList filterList = new ArrayList();
 
         boolean emptyName = false;
 
         for (String s : parameterMap.keySet()) {
             if (s.contains("@")) {
                 String[] partialString = s.split("@");
 
 //If no name is provided, return error message
                 if ("".equalsIgnoreCase(parameterMap.get(s)) || parameterMap.get(s) == null) {
                     emptyName = true;
                 }
 
                 filterList.add("lang(?oldname) = \"" + partialString[1] + "\"");
 
                 insertStringBuffer.append("<" + publisheruri + "> a foaf:Agent ;\n");
                 insertStringBuffer.append("foaf:name" + " \"" + parameterMap.get(s) + "\"" + "@" + partialString[1] + " .\n");
             }
         }
 
         if (emptyName) {
             messages = "<message>\nNavn kan ikke vre blankt\n</message>";
         } else {
 
             for (int i = 0; i < filterList.size(); i++) {
                 whereStringBuffer.append(filterList.get(i));
                 if (filterList.size() - i != 1) {
                     whereStringBuffer.append(" || ");
                 }
             }
 
             deleteStringBuffer.append("}\n");
             whereStringBuffer.append(")\n}\n");
             insertStringBuffer.append("}\n");
 
             String deleteString = deleteStringBuffer.toString() + whereStringBuffer.toString();
             String insertString = insertStringBuffer.toString();
 
             logger.info("updatePublisherByURI() ---> " + publisheruri + " -- SPARUL DELETE  --> " + deleteString);
 
             boolean success = false;
             success = sparulDispatcher.query(deleteString);
             logger.info("updatePublisherByURI() ---> " + publisheruri + " -- DELETE OLD NAME --> " + success);
 
             logger.info("updatePublisherByURI() ---> " + publisheruri + " -- SPARUL INSERT  --> " + insertString);
             success = false;
             success = sparulDispatcher.query(insertString);
             logger.info("updatePublisherByURI() ---> " + publisheruri + " -- INSERT NEW NAME --> " + success);
 
             if (success) {
                 messages = "<message>\nUtgiveren oppdatert\n</message>";
             } else {
                 messages = "<message>\nFeil ved oppdatering\n</message>";
             }
         }
 
         showPublisherByURI(res, req, messages, publisheruri);
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
         if ("".equalsIgnoreCase(publisherURI) || publisherURI == null) {
             publisherURI = req.getCocoonRequest().getParameter("uri");
         }
 
         if ("".equals(messages) || messages == null) {
             messages = "<empty></empty>";
         }
 
         //Find the publisher URI based on name
         String findPublisherByURIQuery = StringUtils.join("\n", new String[]{
                prefixes,
                 "DESCRIBE <" + publisherURI + "> ?resource ?subject",
                 "WHERE {",
                 "OPTIONAL { ?resource dct:publisher <" + publisherURI + "> .",
                 "?resource dct:subject ?subject . }",
                 "}"});
 
 
         logger.trace("AdminController.showPublisherByURI() --> SPARQL query sent to dispatcher: \n" + findPublisherByURIQuery);
         Object queryResult = sparqlDispatcher.query(findPublisherByURIQuery);
 
         Map<String, Object> bizData = new HashMap<String, Object>();
         bizData.put("messages", messages);
         bizData.put("publisherdetails", queryResult);
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
 
         if ("".equals(messages) || messages == null) {
             messages = "<empty></empty>";
         }
 
         String queryString = StringUtils.join("\n", new String[]{
                prefixes,
                 "SELECT DISTINCT ?publisher ?name",
                 "WHERE {",
                 "?publisher foaf:name ?name ;",
                 "           a foaf:Agent .",
                 "}",
                 "ORDER BY ?name"});
 
         logger.trace("AdminController.showPublishersIndex() --> SPARQL query sent to dispatcher: \n" + queryString);
         Object queryResult = sparqlDispatcher.query(queryString);
 
         Map<String, Object> bizData = new HashMap<String, Object>();
         bizData.put("messages", messages);
         bizData.put("publisherlist", queryResult);
         res.sendPage("xml2/utgivere", bizData);
     }
 
     /**
      * Method to displaty a list of all resources suggested by users
      *
      * @param res - AppleResponse
      * @param req - AppleRequest
      */
     private void showSuggestedResources
             (AppleResponse
                     res, AppleRequest
                     req) {
         String queryString = StringUtils.join("\n", new String[]{
                 completePrefixes,
                 "CONSTRUCT {",
                 "    ?resource dct:title ?title ;" +
                         //"              dct:identifier ?identifier ;" +
                         "              a sub:Resource . }",
                 "    WHERE {",
                 "        ?resource wdr:DR <http://sublima.computas.com/status/til_godkjenning> ;",
                 "                  dct:title ?title .",
                 //"                  dct:identifier ?identifier .",
                 "}"});
 
         logger.trace("AdminController.showSuggestedResources() --> SPARQL query sent to dispatcher: \n" + queryString);
         Object queryResult = sparqlDispatcher.query(queryString);
 
         Map<String, Object> bizData = new HashMap<String, Object>();
 
         if ("".equalsIgnoreCase( (String) queryResult) || queryResult == null) {
           bizData.put("suggestedresourceslist", "<empty></empty>");
         } else {
           bizData.put("suggestedresourceslist", queryResult);
         }
         res.sendPage("xml2/foreslaatte", bizData);
     }
 
     /**
      * Method to display the initial page for administrating resources
      *
      * @param res - AppleResponse
      * @param req - AppleRequest
      */
     private void showResourcesIndex
             (AppleResponse
                     res, AppleRequest
                     req) {
         res.sendPage("xml2/ressurser", null);
     }
 
     /**
      * Method that displays a list of all resources tagged to be checked
      *
      * @param res - AppleResponse
      * @param req - AppleRequest
      */
     private void showLinkcheckResults
             (AppleResponse
                     res, AppleRequest
                     req) {
 
         // CHECK
         String queryStringCHECK = StringUtils.join("\n", new String[]{
                prefixes,
                 "CONSTRUCT {",
                 "    ?resource dct:title ?title ;" +
                         "              dct:identifier ?identifier ;" +
                         "              a sub:Resource . }",
                 "    WHERE {",
                 "        ?resource sub:status <http://sublima.computas.com/status/CHECK> ;",
                 "                  dct:title ?title ;",
                 "                  dct:identifier ?identifier .",
                 "}"});
 
         logger.trace("AdminController.showLinkcheckResults() --> SPARQL query sent to dispatcher: \n" + queryStringCHECK);
         Object queryResultCHECK = sparqlDispatcher.query(queryStringCHECK);
 
 // INACTIVE
         String queryStringINACTIVE = StringUtils.join("\n", new String[]{
                prefixes,
                 "CONSTRUCT {",
                 "    ?resource dct:title ?title ;" +
                         "              dct:identifier ?identifier ;" +
                         "              a sub:Resource . }",
                 "    WHERE {",
                 "        ?resource sub:status <http://sublima.computas.com/status/INACTIVE> ;",
                 "                  dct:title ?title ;",
                 "                  dct:identifier ?identifier .",
                 "}"});
 
         logger.trace("AdminController.showLinkcheckResults() --> SPARQL query sent to dispatcher: \n" + queryStringINACTIVE);
         Object queryResultINACTIVE = sparqlDispatcher.query(queryStringINACTIVE);
 
 // GONE
         String queryStringGONE = StringUtils.join("\n", new String[]{
                prefixes,
                 "CONSTRUCT {",
                 "    ?resource dct:title ?title ;" +
                         "              dct:identifier ?identifier ;" +
                         "              a sub:Resource . }",
                 "    WHERE {",
                 "        ?resource sub:status <http://sublima.computas.com/status/GONE> ;",
                 "                  dct:title ?title ;",
                 "                  dct:identifier ?identifier .",
                 "}"});
 
         logger.trace("AdminController.showLinkcheckResults() --> SPARQL query sent to dispatcher: \n" + queryStringGONE);
         Object queryResultGONE = sparqlDispatcher.query(queryStringGONE);
 
         Map<String, Object> bizData = new HashMap<String, Object>();
         bizData.put("lenkesjekklist_check", queryResultCHECK);
         bizData.put("lenkesjekklist_inactive", queryResultINACTIVE);
         bizData.put("lenkesjekklist_gone", queryResultGONE);
         res.sendPage("xml2/lenkesjekk", bizData);
 
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
 
