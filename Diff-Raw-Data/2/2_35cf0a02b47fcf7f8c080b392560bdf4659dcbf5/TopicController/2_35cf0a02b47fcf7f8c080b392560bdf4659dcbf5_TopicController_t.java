 package com.computas.sublima.app.controller.admin;
 
 import com.computas.sublima.query.SparqlDispatcher;
 import com.computas.sublima.query.SparulDispatcher;
 import com.computas.sublima.query.service.AdminService;
 import static com.computas.sublima.query.service.SettingsService.getProperty;
 import com.hp.hpl.jena.sparql.util.StringUtils;
 import org.apache.cocoon.components.flow.apples.AppleRequest;
 import org.apache.cocoon.components.flow.apples.AppleResponse;
 import org.apache.cocoon.components.flow.apples.StatelessAppleController;
 import org.apache.cocoon.environment.Request;
 import org.apache.log4j.Logger;
 
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * @author: mha
  * Date: 31.mar.2008
  */
 public class TopicController implements StatelessAppleController {
 
   private SparqlDispatcher sparqlDispatcher;
   private SparulDispatcher sparulDispatcher;
   AdminService adminService = new AdminService();
   private String mode;
   private String submode;
   String[] completePrefixArray = {
           "PREFIX rdf: 		<http://www.w3.org/1999/02/22-rdf-syntax-ns#>",
           "PREFIX rdfs: 		<http://www.w3.org/2000/01/rdf-schema#>",
           "PREFIX owl: 		<http://www.w3.org/2002/07/owl#>",
           "PREFIX foaf: 		<http://xmlns.com/foaf/0.1/>",
           "PREFIX lingvoj: 	<http://www.lingvoj.org/ontology#>",
           "PREFIX dcmitype: 	<http://purl.org/dc/dcmitype/>",
           "PREFIX dct: 		<http://purl.org/dc/terms/>",
           "PREFIX sub: 		<http://xmlns.computas.com/sublima#>",
           "PREFIX wdr: 		<http://www.w3.org/2007/05/powder#>",
           "PREFIX sioc: 		<http://rdfs.org/sioc/ns#>",
           "PREFIX xsd: 		<http://www.w3.org/2001/XMLSchema#>",
           "PREFIX topic: 		<topic/>",
           "PREFIX skos:		<http://www.w3.org/2004/02/skos/core#>"};
 
   String completePrefixes = StringUtils.join("\n", completePrefixArray);
   String[] prefixArray = {
           "dct: <http://purl.org/dc/terms/>",
           "foaf: <http://xmlns.com/foaf/0.1/>",
           "sub: <http://xmlns.computas.com/sublima#>",
           "rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
           "wdr: <http://www.w3.org/2007/05/powder#>",
           "skos: <http://www.w3.org/2004/02/skos/core#>",
           "lingvoj: <http://www.lingvoj.org/ontology#>"};
   String prefixes = StringUtils.join("\n", prefixArray);
 
   private static Logger logger = Logger.getLogger(AdminController.class);
 
   @SuppressWarnings("unchecked")
   public void process(AppleRequest req, AppleResponse res) throws Exception {
 
     this.mode = req.getSitemapParameter("mode");
     this.submode = req.getSitemapParameter("submode");
 
     if ("emner".equalsIgnoreCase(mode)) {
       if ("".equalsIgnoreCase(submode) || submode == null) {
         res.sendPage("xml2/emner", null);
         return;
       } else if ("nytt".equalsIgnoreCase(submode)) {
         editTopic(res, req, "nytt", null);
         return;
       } else if ("alle".equalsIgnoreCase(submode)) {
         showTopics(res, req);
         return;
       } else if ("emne".equalsIgnoreCase(submode)) {
         editTopic(res, req, "edit", null);
         return;
       } else if ("koble".equalsIgnoreCase(submode)) {
         mergeTopics(res, req);
         return;
       } else if ("tema".equalsIgnoreCase(submode)) {
         setThemeTopics(res, req);
         return;
       } else {
         res.sendStatus(404);
         return;
       }
     } else if ("browse".equalsIgnoreCase(mode)) {
       showTopicBrowsing(res, req);
       return;
     }
   }
 
   private void showTopicBrowsing(AppleResponse res, AppleRequest req) {
     Map<String, Object> bizData = new HashMap<String, Object>();
     bizData.put("themetopics", adminService.getThemeTopics());
     bizData.put("mode", "browse");
     res.sendPage("xml/browse", bizData);
   }
 
   private void mergeTopics(AppleResponse res, AppleRequest req) {
     StringBuffer messageBuffer = new StringBuffer();
     messageBuffer.append("<c:messages xmlns:c=\"http://xmlns.computas.com/cocoon\">\n");
     Map<String, Object> bizData = new HashMap<String, Object>();
 
     if ("GET".equalsIgnoreCase(req.getCocoonRequest().getMethod())) {
       bizData.put("tempvalues", "<empty></empty>");
       bizData.put("alltopics", adminService.getAllTopics());
       bizData.put("mode", "topicjoin");
 
       bizData.put("messages", "<empty></empty>");
       res.sendPage("xml2/koble", bizData);
 
     } else if ("POST".equalsIgnoreCase(req.getCocoonRequest().getMethod())) {
 
       // Lage ny URI for emne
       // Legge til URI i alle emner hvor gamle URI finnes
       // Slette alle gamle URIer
       StringBuffer topicBuffer = new StringBuffer();
 
       String uri = req.getCocoonRequest().getParameter("skos:Concept/skos:prefLabel").replace(" ", "_");
       uri = uri.replace(",", "_");
       uri = uri.replace(".", "_");
       uri = getProperty("sublima.base.url") + "topic/" + uri + "_" + uri.hashCode();
 
       String insertNewTopicString = completePrefixes + "\nINSERT\n{\n" +
               "<" + uri + "> a skos:Concept ;\n" +
               " skos:prefLabel \"" + req.getCocoonRequest().getParameter("skos:Concept/skos:prefLabel") + "\"@no .\n" +
               "}";
 
       logger.trace("TopicController.mergeTopics --> INSERT NEW TOPIC QUERY:\n" + insertNewTopicString);
 
       boolean insertNewSuccess = sparulDispatcher.query(insertNewTopicString);
 
       for (String s : req.getCocoonRequest().getParameterValues("skos:Concept")) {
         topicBuffer.append("?resource skos:Concept <" + s + "> .\n");
       }
 
       messageBuffer.append("</c:messages>\n");
 
       bizData.put("messages", messageBuffer.toString());
 
       res.sendPage("xml2/koble", bizData);
     }
   }
 
   private void setThemeTopics(AppleResponse res, AppleRequest req) {
     StringBuffer messageBuffer = new StringBuffer();
     messageBuffer.append("<c:messages xmlns:c=\"http://xmlns.computas.com/cocoon\">\n");
     Map<String, Object> bizData = new HashMap<String, Object>();
 
     if ("GET".equalsIgnoreCase(req.getCocoonRequest().getMethod())) {
       bizData.put("themetopics", adminService.getThemeTopics());
       bizData.put("tempvalues", "<empty></empty>");
       bizData.put("alltopics", adminService.getAllTopics());
       bizData.put("mode", "theme");
 
       bizData.put("messages", "<empty></empty>");
       res.sendPage("xml2/tema", bizData);
 
     } else if ("POST".equalsIgnoreCase(req.getCocoonRequest().getMethod())) {
       Map<String, String[]> requestMap = createParametersMap(req.getCocoonRequest());
 
       StringBuffer deleteString = new StringBuffer();
       StringBuffer whereString = new StringBuffer();
       StringBuffer insertString = new StringBuffer();
 
       deleteString.append(completePrefixes);
       deleteString.append("\nDELETE\n{\n");
       whereString.append("\nWHERE\n{\n");
       deleteString.append("?topic sub:theme ?theme .\n");
       deleteString.append("}\n");
       whereString.append("?topic sub:theme ?theme.\n");
       whereString.append("}\n");
 
       insertString.append(completePrefixes);
       insertString.append("\nINSERT\n{\n");
 
       for (String s : requestMap.keySet()) {
         if (!requestMap.get(s)[0].equalsIgnoreCase("")) {
           insertString.append("<" + requestMap.get(s)[0] + "> sub:theme \"true\"^^xsd:boolean .\n");
         }
       }
 
       insertString.append("}\n");
 
       deleteString.append(whereString.toString());
 
       logger.trace("TopicController.setThemeTopics --> DELETE QUERY:\n" + deleteString.toString());
       logger.trace("TopicController.setThemeTopics --> INSERT QUERY:\n" + insertString.toString());
 
       boolean deleteSuccess = sparulDispatcher.query(deleteString.toString());
       boolean insertSuccess = sparulDispatcher.query(insertString.toString());
 
       logger.trace("TopicController.setThemeTopics --> DELETE QUERY RESULT: " + deleteSuccess);
       logger.trace("TopicController.setThemeTopics --> INSERT QUERY RESULT: " + insertSuccess);
 
       if (deleteSuccess && insertSuccess) {
         messageBuffer.append("<c:message>Emnene satt som temaemner</c:message>\n");
 
       } else {
         messageBuffer.append("<c:message>Feil ved merking av temaemner</c:message>\n");
         bizData.put("themetopics", "<empty></empty>");
       }
 
       if (deleteSuccess && insertSuccess) {
         bizData.put("themetopics", adminService.getThemeTopics());
         bizData.put("tempvalues", "<empty></empty>");
         bizData.put("mode", "theme");
         bizData.put("alltopics", adminService.getAllTopics());
       } else {
         bizData.put("themetopics", adminService.getThemeTopics());
         bizData.put("tempvalues", "<empty></empty>");
         bizData.put("mode", "theme");
         bizData.put("alltopics", adminService.getAllTopics());
       }
 
       messageBuffer.append("</c:messages>\n");
 
       bizData.put("messages", messageBuffer.toString());
 
       res.sendPage("xml2/tema", bizData);
     }
   }
 
   private void showTopics(AppleResponse res, AppleRequest req) {
     Map<String, Object> bizData = new HashMap<String, Object>();
     bizData.put("all_topics", adminService.getAllTopics());
     res.sendPage("xml2/emner_alle", bizData);
   }
 
   private void editTopic(AppleResponse res, AppleRequest req, String type, String messages) {
 
     StringBuffer messageBuffer = new StringBuffer();
     messageBuffer.append("<c:messages xmlns:c=\"http://xmlns.computas.com/cocoon\">\n");
     messageBuffer.append(messages);
     Map<String, Object> bizData = new HashMap<String, Object>();
 
     if (req.getCocoonRequest().getMethod().equalsIgnoreCase("GET")) {
       bizData.put("tempvalues", "<empty></empty>");
 
       if ("nytt".equalsIgnoreCase(type)) {
         bizData.put("topicdetails", "<empty></empty>");
         bizData.put("topicresources", "<empty></empty>");
         bizData.put("tempvalues", "<empty></empty>");
         bizData.put("alltopics", adminService.getAllTopics());
         bizData.put("status", adminService.getAllStatuses());
         bizData.put("mode", "topicedit");
       } else {
         bizData.put("topicdetails", adminService.getTopicByURI(req.getCocoonRequest().getParameter("uri")));
         bizData.put("topicresources", adminService.getTopicResourcesByURI(req.getCocoonRequest().getParameter("uri")));
         bizData.put("alltopics", adminService.getAllTopics());
         bizData.put("status", adminService.getAllStatuses());
         bizData.put("tempvalues", "<empty></empty>");
         bizData.put("mode", "topicedit");
       }
 
       bizData.put("messages", "<empty></empty>");
       res.sendPage("xml2/emne", bizData);
 
       // When POST try to save the resource. Return error messages upon failure, and success message upon great success
     } else if (req.getCocoonRequest().getMethod().equalsIgnoreCase("POST")) {
 
       // 1. Mellomlagre alle verdier
       // 2. Valider alle verdier
       // 3. Forsk  lagre
 
       StringBuffer tempValues = getTempValues(req);
       String tempPrefixes = "<c:tempvalues \n" +
               "xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\"\n" +
               "xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"\n" +
               "xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n" +
               "xmlns:c=\"http://xmlns.computas.com/cocoon\">\n";
 
       String validationMessages = validateRequest(req);
       if (!"".equalsIgnoreCase(validationMessages)) {
         messageBuffer.append(validationMessages + "\n");
         messageBuffer.append("</c:messages>\n");
 
         bizData.put("topicdetails", "<empty></empty>");
         bizData.put("topicresources", adminService.getTopicResourcesByURI(req.getCocoonRequest().getParameter("uri")));
         bizData.put("tempvalues", tempPrefixes + tempValues.toString() + "</c:tempvalues>");
         bizData.put("messages", messageBuffer.toString());
         bizData.put("status", adminService.getAllStatuses());
         bizData.put("alltopics", adminService.getAllTopics());
         bizData.put("mode", "topictemp");
 
         res.sendPage("xml2/emne", bizData);
 
       } else {
         // Generate an identifier if a uri is not given
         String uri;
         if ("".equalsIgnoreCase(req.getCocoonRequest().getParameter("uri")) || req.getCocoonRequest().getParameter("uri") == null) {
           uri = req.getCocoonRequest().getParameter("dct:subject/skos:Concept/skos:prefLabel").replace(" ", "_");
           uri = uri.replace(",", "_");
           uri = uri.replace(".", "_");
           uri = getProperty("sublima.base.url") + "topic/" + uri + "_" + uri.hashCode();
         } else {
           uri = req.getCocoonRequest().getParameter("uri");
         }
 
         StringBuffer deleteString = new StringBuffer();
         StringBuffer whereString = new StringBuffer();
         deleteString.append(completePrefixes);
         deleteString.append("\nDELETE\n{\n");
         whereString.append("\nWHERE\n{\n");
         deleteString.append("<" + uri + "> a skos:Concept .\n");
         deleteString.append("}\n");
         whereString.append("<" + uri + "> a skos:Concept .\n");
         whereString.append("}\n");
 
 
         StringBuffer insertString = new StringBuffer();
         insertString.append(completePrefixes);
         insertString.append("\nINSERT\n{\n");
         insertString.append("<" + uri + "> a skos:Concept ;\n");
         insertString.append("skos:prefLabel \"" + req.getCocoonRequest().getParameter("dct:subject/skos:Concept/skos:prefLabel") + "\"@no ;\n");
         insertString.append("wdr:describedBy <" + req.getCocoonRequest().getParameter("wdr:describedBy") + "> ;\n");
        insertString.append("skos:definition \"" + req.getCocoonRequest().getParameter("dct:subject/skos:Concept/skos:definition") + "\"@no ;\n");
         insertString.append("skos:note \"" + req.getCocoonRequest().getParameter("dct:subject/skos:Concept/skos:note") + "\"@no .\n");
 
         if (req.getCocoonRequest().getParameterValues("dct:subject/skos:Concept/skos:broader/rdf:resource") != null) {
           for (String s : req.getCocoonRequest().getParameterValues("dct:subject/skos:Concept/skos:broader/rdf:resource")) {
             insertString.append("<" + uri + "> skos:broader <" + s + "> .\n");
           }
         }
         insertString.append("}");
 
         deleteString.append(whereString.toString());
 
         boolean deleteSuccess = sparulDispatcher.query(deleteString.toString());
         boolean insertSuccess = sparulDispatcher.query(insertString.toString());
 
 
         logger.trace("TopicController.editTopic --> DELETE QUERY:\n" + deleteString.toString());
         logger.trace("TopicController.editTopic --> INSERT QUERY:\n" + insertString.toString());
 
         logger.trace("TopicController.editTopic --> DELETE QUERY RESULT: " + deleteSuccess);
         logger.trace("TopicController.editTopic --> INSERT QUERY RESULT: " + insertSuccess);
 
         if (deleteSuccess && insertSuccess) {
           messageBuffer.append("<c:message>Nytt emne lagt til!</c:message>\n");
 
         } else {
           messageBuffer.append("<c:message>Feil ved lagring av nytt emne</c:message>\n");
           bizData.put("topicdetails", "<empty></empty>");
         }
 
         if (deleteSuccess && insertSuccess) {
           bizData.put("topicdetails", adminService.getTopicByURI(uri));
           bizData.put("topicresources", adminService.getTopicResourcesByURI(req.getCocoonRequest().getParameter("uri")));
           bizData.put("status", adminService.getAllStatuses());
           bizData.put("tempvalues", "<empty></empty>");
           bizData.put("mode", "topicedit");
           bizData.put("alltopics", adminService.getAllTopics());
         } else {
           bizData.put("topicdetails", adminService.getTopicByURI(uri));
           bizData.put("topicresources", adminService.getTopicResourcesByURI(req.getCocoonRequest().getParameter("uri")));
           bizData.put("status", adminService.getAllStatuses());
           bizData.put("tempvalues", tempPrefixes + tempValues.toString() + "</c:tempvalues>");
           bizData.put("mode", "topictemp");
           bizData.put("alltopics", adminService.getAllTopics());
         }
 
         messageBuffer.append("</c:messages>\n");
 
         bizData.put("messages", messageBuffer.toString());
 
         res.sendPage("xml2/emne", bizData);
       }
     }
   }
 
 
   private StringBuffer getTempValues(AppleRequest req) {
     //Keep all selected values in case of validation error
     String temp_title = req.getCocoonRequest().getParameter("dct:subject/skos:Concept/skos:prefLabel");
     String[] temp_broader = req.getCocoonRequest().getParameterValues("dct:subject/skos:Concept/skos:broader/rdf:resource");
     String temp_status = req.getCocoonRequest().getParameter("wdr:describedBy");
     String temp_description = req.getCocoonRequest().getParameter("dct:subject/skos:Concept/skos:definition");
     String temp_note = req.getCocoonRequest().getParameter("dct:subject/skos:Concept/skos:note");
 
     //Create an XML structure for the selected values, to use in the JX template
     StringBuffer xmlStructureBuffer = new StringBuffer();
     xmlStructureBuffer.append("<skos:prefLabel>" + temp_title + "</skos:prefLabel>\n");
 
     if (temp_broader != null) {
       for (String s : temp_broader) {
         //xmlStructureBuffer.append("<language>" + s + "</language>\n");
         xmlStructureBuffer.append("<skos:broader rdf:resource=\"" + s + "\"/>\n");
       }
     }
 
     xmlStructureBuffer.append("<wdr:describedBy rdf:resource=\"" + temp_status + "\"/>\n");
     xmlStructureBuffer.append("<skos:description>" + temp_description + "</skos:description>\n");
     xmlStructureBuffer.append("<skos:note>" + temp_note + "</skos:note>\n");
 
 
     return xmlStructureBuffer;
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
 
     if ("".equalsIgnoreCase(req.getCocoonRequest().getParameter("dct:subject/skos:Concept/skos:prefLabel")) || req.getCocoonRequest().getParameter("dct:subject/skos:Concept/skos:prefLabel") == null) {
       validationMessages.append("<c:message>Emnets tittel kan ikke vre blank</c:message>\n");
     }
 
     if (req.getCocoonRequest().getParameter("wdr:describedBy") == null) {
       validationMessages.append("<c:message>En status m velges</c:message>\n");
     }
 
     return validationMessages.toString();
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
 
