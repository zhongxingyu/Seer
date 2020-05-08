 package com.computas.sublima.app.service;
 
 import com.computas.sublima.query.SparqlDispatcher;
 import com.computas.sublima.query.SparulDispatcher;
 import com.computas.sublima.query.impl.DefaultSparqlDispatcher;
 import com.computas.sublima.query.impl.DefaultSparulDispatcher;
 import com.computas.sublima.query.service.DatabaseService;
 import com.computas.sublima.query.service.MappingService;
 import com.computas.sublima.query.service.SettingsService;
 import static com.computas.sublima.query.service.SettingsService.getProperty;
 import com.hp.hpl.jena.sparql.util.StringUtils;
 import org.apache.cocoon.components.flow.apples.AppleRequest;
 import org.apache.log4j.Logger;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathFactory;
 import java.io.ByteArrayInputStream;
 import java.io.UnsupportedEncodingException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashSet;
 import java.util.LinkedHashSet;
 
 /**
  * A class to support the administration of Sublima
  * Has methods for getting topics, statuses, languages, media types, audience etc.
  *
  * @author: mha
  * Date: 13.mar.2008
  */
 //todo Use selected interface language for all labels
 public class AdminService {
 
   private static Logger logger = Logger.getLogger(AdminService.class);
   private SparqlDispatcher sparqlDispatcher = new DefaultSparqlDispatcher();
   private SparulDispatcher sparulDispatcher = new DefaultSparulDispatcher();
   private MappingService ms = new MappingService();
 
   /**
    * Method to get all relation types
    *
    * @return RDF XML result
    */
   public String getAllRelationTypes() {
     String queryResult;
     String queryString = StringUtils.join("\n", new String[]{
             "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>",
             "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
             "DESCRIBE ?relation WHERE {",
             "?relation rdfs:subPropertyOf skos:semanticRelation .",
             "}"});
 
     logger.trace("AdminService.getAllRelationTypes() executing");
     queryResult = sparqlDispatcher.query(queryString).toString();
 
     return queryResult;
   }
 
   /**
    * Method to get all publishers
    *
    * @return A String RDF/XML containing all the publishers
    */
   public String getAllPublishers() {
 
     String queryString = StringUtils.join("\n", new String[]{
             "PREFIX dct: <http://purl.org/dc/terms/>",
             "PREFIX foaf: <http://xmlns.com/foaf/0.1/>",
             "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
             "CONSTRUCT {",
             "    ?publisher a foaf:Agent ;",
             "    foaf:name ?name .",
             "}",
             "WHERE {",
             "?publisher a foaf:Agent ;",
             "foaf:name ?name .",
             "}"});
 
     logger.trace("AdminService.getAllPublishers() executing");
     Object queryResult = sparqlDispatcher.query(queryString);
 
     return queryResult.toString();
   }
 
   /**
    * Method to get all statuses
    *
    * @return A String RDF/XML containing all the statuses
    */
   public String getAllStatuses() {
     String queryString = StringUtils.join("\n", new String[]{
             "PREFIX wdr: <http://www.w3.org/2007/05/powder#>",
             "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
             "CONSTRUCT {",
             "    ?status a wdr:DR ;",
             "    rdfs:label ?label .",
             "}",
             "WHERE {",
             "    ?status a wdr:DR ;",
             "    rdfs:label ?label .",
             "}"});
 
 
     logger.trace("AdminService.getAllStatuses() executing");
     Object queryResult = sparqlDispatcher.query(queryString);
 
     return queryResult.toString();
   }
 
   /**
    * Method to get all statuses valid for user administration
    *
    * @return A String RDF/XML containing all the statuses
    */
   public String getAllStatusesForUser() {
     String queryString = StringUtils.join("\n", new String[]{
             "PREFIX wdr: <http://www.w3.org/2007/05/powder#>\n" +
                     "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                     "CONSTRUCT {\n" +
                     "    <http://sublima.computas.com/status/godkjent_av_administrator> a wdr:DR ;\n" +
                     "    rdfs:label ?label1 .\n" +
                     " <http://sublima.computas.com/status/inaktiv> a wdr:DR ;\n" +
                     "    rdfs:label ?label2 .\n" +
                     "}\n" +
                     "WHERE {\n" +
                     " OPTIONAL {\n" +
                     " <http://sublima.computas.com/status/godkjent_av_administrator> a wdr:DR ;\n" +
                     "    rdfs:label ?label1 .}\n" +
                     " OPTIONAL {\n" +
                     " <http://sublima.computas.com/status/inaktiv> a wdr:DR ;\n" +
                     "    rdfs:label ?label2 .}\n" +
                     "}"});
 
     logger.trace("AdminService.getAllStatusesForUser() executing");
     Object queryResult = sparqlDispatcher.query(queryString);
 
     return queryResult.toString();
   }
 
 
   /**
    * Method to get all languages
    *
    * @return A String RDF/XML containing all the languages
    */
   public String getAllLanguages() {
     String queryString = StringUtils.join("\n", new String[]{
             "PREFIX lingvoj: <http://www.lingvoj.org/ontology#>",
             "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
             "CONSTRUCT {",
             "?language a lingvoj:Lingvo ;",
             "          rdfs:label ?label .",
             "}",
             "WHERE {",
             "?language a lingvoj:Lingvo ;",
             "          rdfs:label ?label .",
             "}"});
 
     logger.trace("AdminService.getAllLanguages() executing");
     Object queryResult = sparqlDispatcher.query(queryString);
 
     return queryResult.toString();
   }
 
   /**
    * Method to get distinct labels for different properties
    * NOTE: This doesn't anymore check what is actually used. It became such a performance liability that we had to skip it.
    *
    * @param rdfType  The full URI (unless it is in the dct or rdfs namespaces) with pointy brackets for the type of subject that you want.
    * @param property The full URI (unless it is in the dct or rdfs namespaces) with pointy brackets for the property that connects the resource to the subject. Not used at the moment.
    * @return A String containing SPARQL Result Set XML with the languages
    */
   public String getDistinctAndUsedLabels(String rdfType, String property) {
     String queryString = StringUtils.join("\n", new String[]{
             "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
             "PREFIX dct: <http://purl.org/dc/terms/>",
             "SELECT DISTINCT ?uri ?label",
             "WHERE {",
             "?uri a " + rdfType + " ;",
             "          rdfs:label ?label .",
             "?resource " + property + " ?uri .",
             "}"});
 
     logger.trace("AdminService.getDistinctAndUsedLabels() executing");
     Object queryResult = sparqlDispatcher.query(queryString, "SELECT");
     logger.trace("AdminService.getDistinctAndUsedLabels() got " + queryResult.toString());
 
     return queryResult.toString();
   }
 
 
   /**
    * Method to get all media types
    *
    * @return A String RDF/XML containing all the media types
    */
   public String getAllMediaTypes() {
 
     String queryString = StringUtils.join("\n", new String[]{
             "PREFIX dct: <http://purl.org/dc/terms/>",
             "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
             "CONSTRUCT {",
             "    ?mediatype a dct:MediaType ;",
             "             rdfs:label ?label .",
             "}",
             "WHERE {",
             "    ?mediatype a dct:MediaType ;",
             "             rdfs:label ?label .",
             "}"});
 
     logger.trace("AdminService.getAllMediaTypes() executing");
     Object queryResult = sparqlDispatcher.query(queryString);
 
     return queryResult.toString();
   }
 
   /**
    * Method to get all audiences
    *
    * @return A String RDF/XML containing all the audiences
    */
   public String getAllAudiences() {
 
     String queryString = StringUtils.join("\n", new String[]{
             "PREFIX dct: <http://purl.org/dc/terms/>",
             "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
             "CONSTRUCT {",
             "    ?audience a dct:AgentClass ;",
             "             rdfs:label ?label .",
             "}",
             "WHERE {",
             "    ?audience a dct:AgentClass ;",
             "             rdfs:label ?label .",
             "}"});
 
     logger.trace("AdminService.getAllAudiences() executing");
     Object queryResult = sparqlDispatcher.query(queryString);
 
     return queryResult.toString();
   }
 
   /**
    * Method to get a resource by its URI
    *
    * @return A String RDF/XML containing the resource
    */
   public Object getResourceByURI(String uri) {
 
     try {
       uri = "<" + uri + ">";
 
     } catch (Exception e) {
       e.printStackTrace();
     }
 
     String queryString = StringUtils.join("\n", new String[]{
             "PREFIX dct: <http://purl.org/dc/terms/>",
             "PREFIX sub: <http://xmlns.computas.com/sublima#>",
             "PREFIX sioc: <http://rdfs.org/sioc/ns#>",
             "DESCRIBE " + uri + " ?comment ?commentcreator",
             "WHERE {",
             "  OPTIONAL { " + uri + " sub:comment ?comment . ",
             "  ?comment sioc:has_creator ?commentcreator .",
             "}",
             "}"});
 
     logger.trace("AdminService.getResourceByURI() executing");
     Object queryResult = sparqlDispatcher.query(queryString);
 
     return queryResult.toString();
   }
 
   public String insertPublisher(String publishername, String language) {
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
                     "foaf:name \"" + publishername + "\"@" + language + " .\n" +
                     "}";
 
     logger.info("updatePublisherByURI() executing");
     boolean success = false;
     success = sparulDispatcher.query(insertPublisherByName);
     logger.info("updatePublisherByURI() ---> " + publisherURI + " -- INSERT NEW NAME --> " + success);
     if (success) {
       return publisherURI;
     } else {
       return "";
     }
   }
 
   /**
    * Method to get all topics
    *
    * @return A String RDF/XML containing all the topics
    */
   public String getAllTopics() {
 
     String queryString = StringUtils.join("\n", new String[]{
             "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>",
             "PREFIX wdr: <http://www.w3.org/2007/05/powder#>",
             "CONSTRUCT {",
             "    ?topic a skos:Concept ;",
             "        skos:prefLabel ?label ;",
             "        wdr:describedBy ?status .",
             "}",
             "WHERE {",
             "    ?topic a skos:Concept ;",
             "        skos:prefLabel ?label ;",
             "        wdr:describedBy ?status .",
             "}"});
 
     logger.trace("AdminService.getAllTopics() executing");
     Object queryResult = sparqlDispatcher.query(queryString);
 
     return queryResult.toString();
   }
 
   public String getTopicByURI(String uri) {
     String queryString = StringUtils.join("\n", new String[]{
             "PREFIX dct: <http://purl.org/dc/terms/>",
             "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>",
             "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
             "PREFIX wdr: <http://www.w3.org/2007/05/powder#>",
             "DESCRIBE <" + uri + ">",
             "WHERE {",
             "<" + uri + "> a skos:Concept . }"});
 
     logger.trace("AdminService.getTopicByURI() executing");
     Object queryResult = sparqlDispatcher.query(queryString);
 
     return queryResult.toString();
   }
 
   public String getTopicsAsJSON() {
     String queryString = StringUtils.join("\n", new String[]{
             "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>",
             "PREFIX wdr: <http://www.w3.org/2007/05/powder#>",
            "SELECT ?label <" + SettingsService.getProperty("sublima.basegraph") + ">",
             "WHERE {",
             "    ?topic a skos:Concept .",
             "   {?topic skos:prefLabel ?label .}",
             "   UNION {",
             "       ?topic skos:altLabel ?label . }",
             "    ?topic wdr:describedBy <http://sublima.computas.com/status/godkjent_av_administrator> .",
             "}",
             "ORDER BY ?label"});
 
     logger.trace("AdminService.getTopicByPartialName() executing");
     Object queryResult = sparqlDispatcher.getResultsAsJSON(queryString);
 
     return queryResult.toString();
   }
 
   public String getPublishersAsJSON() {
     String queryString = StringUtils.join("\n", new String[]{
             "PREFIX dct: <http://purl.org/dc/terms/>",
             "PREFIX foaf: <http://xmlns.com/foaf/0.1/>",
             "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
             "SELECT ?label FROM <" + SettingsService.getProperty("sublima.basegraph") + ">",
             "WHERE {",
             "?o a foaf:Agent ; ",
             "       foaf:name ?label .",
             "?s ?p ?o .",
             "}",
             "ORDER BY ?label"});
 
     logger.trace("AdminService.getPublishersAsJSON() executing");
     Object queryResult = sparqlDispatcher.getResultsAsJSON(queryString);
 
     return queryResult.toString();
   }
 
   public String getTopicResourcesByURI(String uri) {
     String queryString = StringUtils.join("\n", new String[]{
             "PREFIX dct: <http://purl.org/dc/terms/>",
             "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>",
             "DESCRIBE ?resource",
             "WHERE {",
             "    ?resource dct:subject <" + uri + "> . ",
             "}"});
 
     logger.trace("AdminService.getTopicResourcesByURI() executing");
     Object queryResult = sparqlDispatcher.query(queryString);
 
     return queryResult.toString();
   }
 
   public String getThemeTopics() {
     String queryString = StringUtils.join("\n", new String[]{
             "PREFIX sub: <http://xmlns.computas.com/sublima#>",
             "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>",
             "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>",
             "PREFIX wdr: <http://www.w3.org/2007/05/powder#>",
             "DESCRIBE ?theme",
             "WHERE {",
             "    ?theme a skos:Concept .",
             "    ?theme sub:theme \"true\"^^xsd:boolean .",
             "    ?theme wdr:describedBy <http://sublima.computas.com/status/godkjent_av_administrator> .",
             "}"});
 
     logger.trace("AdminService.getTopicByURI() executing");
     Object queryResult = sparqlDispatcher.query(queryString);
 
     return queryResult.toString();
   }
 
   public String getAllUsers() {
     String queryString = StringUtils.join("\n", new String[]{
             "PREFIX sioc: <http://rdfs.org/sioc/ns#>",
             "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
             "DESCRIBE ?user",
             "WHERE {",
             "    ?user a sioc:User ;",
             "        rdfs:label ?label .",
             "}"});
 
     logger.trace("AdminService.getAllUsers() executing");
     Object queryResult = sparqlDispatcher.query(queryString);
 
     return queryResult.toString();
   }
 
   public String getUserByURI(String uri) {
     if (uri == null) {
       return "";
     }
     String queryString = StringUtils.join("\n", new String[]{
             "PREFIX sioc: <http://rdfs.org/sioc/ns#>",
             "DESCRIBE <" + uri + ">"});
 
     logger.trace("AdminService.getUserbyURI() executing");
     Object queryResult = sparqlDispatcher.query(queryString);
 
     return queryResult.toString();
   }
 
   public String getRelationByURI(String uri) {
     String queryString = StringUtils.join("\n", new String[]{
             "DESCRIBE <" + uri + ">"});
 
     logger.trace("AdminService.getRelationByURI() executing");
     Object queryResult = sparqlDispatcher.query(queryString);
 
     return queryResult.toString();
   }
 
   private static String convertToHex(byte[] data) {
     StringBuilder buf = new StringBuilder();
     for (int i = 0; i < data.length; i++) {
       int halfbyte = (data[i] >>> 4) & 0x0F;
       int two_halfs = 0;
       do {
         if ((0 <= halfbyte) && (halfbyte <= 9))
           buf.append((char) ('0' + halfbyte));
         else
           buf.append((char) ('a' + (halfbyte - 10)));
         halfbyte = data[i] & 0x0F;
       } while (two_halfs++ < 1);
     }
     return buf.toString();
   }
 
   public String generateSHA1(String text)
           throws NoSuchAlgorithmException, UnsupportedEncodingException {
     MessageDigest md;
     md = MessageDigest.getInstance("SHA-1");
     byte[] sha1hash = new byte[40];
     md.update(text.getBytes("UTF-8"), 0, text.length());
     //todo simplify
     sha1hash = md.digest();
     return convertToHex(sha1hash);
   }
 
 
   public String getTopicsByLetter(String letter) {
     letter = letter.toUpperCase();
     if (letter.equalsIgnoreCase("0-9")) {
       letter = "[0-9]";
     }
 
     String queryString = StringUtils.join("\n", new String[]{
             "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>",
             "PREFIX wdr: <http://www.w3.org/2007/05/powder#>",
             "PREFIX dct: <http://purl.org/dc/terms/>",
             "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
             "CONSTRUCT { ?topic a skos:Concept ; rdfs:label ?label . }",
             "WHERE {",
             "    ?topic a skos:Concept .",
             "   {?topic skos:prefLabel ?label .}",
             "   UNION {",
             "       ?topic skos:altLabel ?label . }",
             "    ?topic wdr:describedBy <http://sublima.computas.com/status/godkjent_av_administrator> .",
             "FILTER regex(str(?label), \"^" + letter + "\", \"i\")",
             "}"});
 
     logger.trace("AdminService.getTopicResourcesByURI() executing");
     Object queryResult = sparqlDispatcher.query(queryString);
 
     if (queryResult == null) {
       return "<rdf:RDF></rdf:RDF>";
     } else {
       return queryResult.toString();
     }
   }
 
   public boolean validateURL(String url) {
     String ourcode;
     try {
       // Do a URL check so that we know we have a valid URL
       URLActions urlAction = new URLActions(url);
       ourcode = urlAction.getCode();
     }
     catch (NullPointerException e) {
       e.printStackTrace();
       return false;
     }
 
     return "302".equals(ourcode) ||
             "303".equals(ourcode) ||
             "304".equals(ourcode) ||
             "305".equals(ourcode) ||
             "307".equals(ourcode) ||
             ourcode.startsWith("2");
   }
 
   public String getAllRoles() {
     String queryString = StringUtils.join("\n", new String[]{
             "PREFIX sioc: <http://rdfs.org/sioc/ns#>",
             "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
             "DESCRIBE ?role",
             "WHERE {",
             "    ?role a sioc:Role ;",
             "        rdfs:label ?label .",
             "}"});
 
     logger.trace("AdminService.getAllRoles() executing");
     Object queryResult = sparqlDispatcher.query(queryString);
 
     return queryResult.toString();
   }
 
   public String getRoleByURI(String uri) {
     String queryString = StringUtils.join("\n", new String[]{
             "DESCRIBE <" + uri + ">"});
 
     logger.trace("AdminService.getRoleByURI() --> executing");
     Object queryResult = sparqlDispatcher.query(queryString);
 
     return queryResult.toString();
   }
 
   /**
    * Method to get the role privileges based on the given URI.
    * Returns the privileges as XML.
    *
    * @param roleuri
    * @return
    */
   public String getRolePrivilegesAsXML(String roleuri) {
     DatabaseService dbService = new DatabaseService();
     Statement statement;
     ResultSet resultSet;
     StringBuilder xmlBuffer = new StringBuilder();
 
     String getRolePrivilegesString = "SELECT privilege FROM DB.DBA.roleprivilege WHERE \"role\" = '" + roleuri + "'";
 
     xmlBuffer.append("<c:privileges xmlns:c=\"http://xmlns.computas.com/cocoon\">\n");
 
 
     logger.trace("AdminService.getRolePrivilegesAsXML --> " + getRolePrivilegesString);
 
     Connection connection = null;
 
     try {
       connection = dbService.getJavaSQLConnection();
 
       statement = connection.createStatement();
       resultSet = statement.executeQuery(getRolePrivilegesString);
 
       while (resultSet.next()) {
         xmlBuffer.append("<c:privilege>" + resultSet.getString(1) + "</c:privilege>");
       }
 
       xmlBuffer.append("</c:privileges>\n");
     } catch (SQLException e) {
       xmlBuffer.append("</c:privileges>\n");
       e.printStackTrace();
       logger.trace("AdminService.getRolePrivilegesAsXML --> FAILED\n");
 
     }
 
     finally {
       if (connection != null) {
         try {
           connection.close();
         } catch (SQLException e) {
           logger.error("Could not close Database connection");
         }
       }
     }
     return xmlBuffer.toString();
   }
 
   /**
    * Method to get the role privileges based on the given URI.
    * Returns the privileges as XML.
    *
    * @return
    */
   public String getIndexStatisticsAsXML() {
     DatabaseService dbService = new DatabaseService();
     Statement statement;
     ResultSet resultSet;
     StringBuilder xmlBuffer = new StringBuilder();
 
     String getIndexStatisticsString = "SELECT type, \"date\" FROM DB.DBA.indexstatistics";
 
     xmlBuffer.append("<c:statistics xmlns:c=\"http://xmlns.computas.com/cocoon\">\n");
 
 
     logger.trace("AdminService.getIndexStatisticsAsXML --> " + getIndexStatisticsString);
 
     Connection connection = null;
 
     try {
       connection = dbService.getJavaSQLConnection();
 
       statement = connection.createStatement();
       resultSet = statement.executeQuery(getIndexStatisticsString);
 
       while (resultSet.next()) {
         xmlBuffer.append("<c:statistic><c:type>" + resultSet.getString(1) + "</c:type><c:date>" + resultSet.getString(2) + "</c:date></c:statistic>");
       }
 
       xmlBuffer.append("</c:statistics>\n");
     } catch (SQLException e) {
       xmlBuffer.append("</c:statistics>\n");
       e.printStackTrace();
       logger.trace("AdminService.getIndexStatisticsAsXML --> FAILED\n");
 
     }
 
     finally {
       if (connection != null) {
         try {
           connection.close();
         } catch (SQLException e) {
           logger.error("Could not close Database connection");
         }
       }
     }
     return xmlBuffer.toString();
   }
 
   /**
    * Method to get the user role based on the username
    * This method use JAXP to perform a XPATH operation on the results from Joseki.
    *
    * @param name
    * @return role
    */
   public String getUserRole(String name) {
     String queryString = StringUtils.join("\n", new String[]{
             "PREFIX sioc: <http://rdfs.org/sioc/ns#>",
             "SELECT ?role",
             "WHERE {",
             "?user a sioc:User ;",
             "    sioc:email " + name + " ;",
             "    sioc:has_function ?role .",
             "?role a sioc:Role . }"});
 
     logger.trace("AdminService.getUserRole() executing");
     Object queryResult = sparqlDispatcher.query(queryString);
     try {
 
       DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
       Document doc = builder.parse(new ByteArrayInputStream(queryResult.toString().getBytes("UTF-8")));
       XPathExpression expr = XPathFactory.newInstance().newXPath().compile("//td");
       NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
       return getTextContent(nodes.item(0));
 
     } catch (Exception e) {
       e.printStackTrace();
       return "<empty/>";
     }
   }
 
   /**
    * Recursive implementation of the method
    * org.w3c.dom.Node.getTextContent which is present in JDK 1.5 but not 1.4
    *
    * @param node Node that you need to get the text content of
    * @return
    * @author Tobias Hinnerup
    */
   private static String getTextContent(Node node) {
     Node child;
     String sContent = node.getNodeValue() != null ? node.getNodeValue() : "";
 
     NodeList nodes = node.getChildNodes();
     for (int i = 0; i < nodes.getLength(); i++) {
       child = nodes.item(i);
       sContent += child.getNodeValue() != null ? child.getNodeValue() : "";
       if (nodes.item(i).getChildNodes().getLength() > 0) {
         sContent += getTextContent(nodes.item(i));
       }
     }
 
     return sContent;
   }
 
   /**
    * Method to get the user based on the e-mail
    *
    * @param email
    * @return role
    */
   public String getUserByEmail(String email) {
     String queryString = StringUtils.join("\n", new String[]{
             "PREFIX sioc: <http://rdfs.org/sioc/ns#>",
             "DESCRIBE ?user",
             "WHERE {",
             "?user a sioc:User ;",
             "    sioc:email " + email + " . }"});
 
     logger.trace("AdminService.getUserByName() executing");
     return (String) sparqlDispatcher.query(queryString);
   }
 
   /**
    * Method to check if a given URL already exists as and URI in the data. Checks both with and without a trailing /.
    *
    * @param url
    * @return
    */
   public boolean checkForDuplicatesByURI(String url) {
     String resourceWithEndingSlash;
     String resourceWithoutEndingSlash;
 
     try {
 
       // We have to check the url both with and without an ending /
       if (url.endsWith("/")) {
         resourceWithEndingSlash = (String) getResourceByURI(url);
         url = url.substring(0, url.length() - 1);
         resourceWithoutEndingSlash = (String) getResourceByURI(url);
 
       } else {
         resourceWithoutEndingSlash = (String) getResourceByURI(url);
         resourceWithEndingSlash = (String) getResourceByURI(url + "/");
       }
 
       if (resourceWithEndingSlash.contains(url)
               || resourceWithoutEndingSlash.contains(url)) {
         return true;
       } else {
         return false;
       }
     } catch (Exception e) {
       return false;
     }
   }
 
   public StringBuilder getMostOfTheRequestXML(AppleRequest req) {
     // This is such a 1999 way of doing things. There should be a generic SAX events generator
     // or something that would serialise this data structure automatically in a one-liner,
     // but I couldn't find it. Arguably a TODO.
     StringBuilder params = new StringBuilder();
     String uri = req.getCocoonRequest().getRequestURI();
     int paramcount = 0;
     params.append("  <request justbaseurl=\"" + uri + "\" ");
     if (req.getCocoonRequest().getQueryString() != null) {
       uri += "?" + req.getCocoonRequest().getQueryString();
       uri = uri.replace("&", "&amp;");
       uri = uri.replace("<", "%3C");
       uri = uri.replace(">", "%3E");
       uri = uri.replace("#", "%23"); // A hack to get the hash alive through a clickable URL
       paramcount = req.getCocoonRequest().getParameters().size();
     }
     params.append("paramcount=\"" + paramcount + "\" ");
     params.append("requesturl=\"" + uri);
     params.append("\">\n");
     for (Enumeration keys = req.getCocoonRequest().getParameterNames(); keys.hasMoreElements();) {
       String key = keys.nextElement().toString();
       params.append("\n    <param key=\"" + key.replace("<", "%3C").replace(">", "%3E") + "\">");
       String[] values = req.getCocoonRequest().getParameterValues(key);
       for (String value : values) {
         value = value.replace("&", "&amp;");
         value = value.replace("<", "%3C");
         value = value.replace(">", "%3E");
         value = value.replace("#", "%23"); // A hack to get the hash alive through a clickable URL
         params.append("\n      <value>" + value + "</value>");
       }
       params.append("\n    </param>");
     }
     return params;
   }
 
   public StringBuilder getMostOfTheRequestXMLWithPrefix(AppleRequest req) {
     // This is such a 1999 way of doing things. There should be a generic SAX events generator
     // or something that would serialise this data structure automatically in a one-liner,
     // but I couldn't find it. Arguably a TODO.
     StringBuilder params = new StringBuilder();
     String uri = req.getCocoonRequest().getRequestURI();
     int paramcount = 0;
     params.append("  <c:request xmlns:c=\"http://xmlns.computas.com/cocoon\" justbaseurl=\"" + uri + "\" ");
     if (req.getCocoonRequest().getQueryString() != null) {
       uri += "?" + req.getCocoonRequest().getQueryString();
       uri = uri.replace("&", "&amp;");
       uri = uri.replace("&", "&amp;");
       uri = uri.replace("<", "%3C");
       uri = uri.replace(">", "%3E");
       uri = uri.replace("#", "%23"); // A hack to get the hash alive through a clickable URL
       paramcount = req.getCocoonRequest().getParameters().size();
     }
     params.append("paramcount=\"" + paramcount + "\" ");
     params.append("requesturl=\"" + uri);
     params.append("\">\n");
     for (Enumeration keys = req.getCocoonRequest().getParameterNames(); keys.hasMoreElements();) {
       String key = keys.nextElement().toString();
       params.append("\n    <c:param key=\"" + key + "\">");
       String[] values = req.getCocoonRequest().getParameterValues(key);
       for (String value : values) {
         value = value.replace("&", "&amp;");
         value = value.replace("<", "%3C");
         value = value.replace(">", "%3E");
         value = value.replace("#", "%23"); // A hack to get the hash alive through a clickable URL
         params.append("\n      <c:value>" + value + "</c:value>");
       }
       params.append("\n    </c:param>");
     }
     return params;
   }
 
   public String getAllTopicsByStatus(String status) {
     String queryString = StringUtils.join("\n", new String[]{
             "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>",
             "PREFIX wdr: <http://www.w3.org/2007/05/powder#>",
             "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
             "CONSTRUCT { ?topic a skos:Concept ; rdfs:label ?label . }",
             "WHERE {",
             "    ?topic a skos:Concept .",
             "   {?topic skos:prefLabel ?label .}",
             "   UNION {",
             "       ?topic skos:altLabel ?label . }",
             "    ?topic wdr:describedBy <" + status + "> .",
             "}"});
 
     logger.trace("AdminService.getAllTopics() executing");
     Object queryResult = sparqlDispatcher.query(queryString);
 
     return queryResult.toString();
   }
 
   /**
    * Method to get all topics
    *
    * @return A String RDF/XML containing all the topics
    */
   public String getAllTopicsWithPrefAndAltLabel() {
 
     String queryString = StringUtils.join("\n", new String[]{
             "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>",
             "PREFIX wdr: <http://www.w3.org/2007/05/powder#>",
             "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
             "CONSTRUCT { ?topic a skos:Concept ; rdfs:label ?label . }",
             "WHERE {",
             "    ?topic a skos:Concept .",
             "   {?topic skos:prefLabel ?label .}",
             "   UNION {",
             "       ?topic skos:altLabel ?label . }",
             "}"});
 
 
     logger.trace("AdminService.getAllTopics() executing");
     Object queryResult = sparqlDispatcher.query(queryString);
 
     return queryResult.toString();
   }
 
   /**
    * Method to return a boolean if the expected number of hits for the given query exceeds a threshold
    *
    * @param query A query counting the number of hits
    * @return boolean if above the given treshold.
    */
   public boolean isAboveMaxNumberOfHits(String query) {
     logger.trace("AdminService.isAboveMaxNumberOfHits() executing");
     if (query == null) {
       return true;
     }
     if (!query.contains("bif:contains")) {
       return false;
     }
     try {
       return sparqlDispatcher.query(query).toString().contains("<uri>");
 
     } catch (Exception e) {
       logger.warn("AdminService.isAboveMaxNumberOfHits() returned an error: " + e.getMessage());
       e.printStackTrace();
       return false;
     }
   }
 
   public ArrayList<String[]> createArrayOfTopics() {
     String result = getTopicsAsJSON();
     ArrayList<String[]> topicList = new ArrayList<String[]>();
 
     try {
       JSONObject json = new JSONObject(result);
       json = json.getJSONObject("results");
       JSONArray jsonArray = json.getJSONArray("bindings");
 
       for (int i = 0; i < jsonArray.length(); i++) {
         JSONObject obj2 = (JSONObject) jsonArray.get(i);
         obj2 = (JSONObject) obj2.get("label");
         String language;
         try {
           language = obj2.get("xml:lang").toString();
         } catch (Exception e) {
           language = "";
         }
         topicList.add(new String[]{obj2.get("value").toString(), language});
       }
 
     } catch (JSONException e) {
 
       e.printStackTrace();
     }
     return topicList;
   }
 
   public LinkedHashSet<String> createArrayOfPublishers() {
     String result = getPublishersAsJSON();
     LinkedHashSet<String> publisherSet = new LinkedHashSet<String>();
 
     try {
       JSONObject json = new JSONObject(result);
       json = json.getJSONObject("results");
       JSONArray jsonArray = json.getJSONArray("bindings");
 
       for (int i = 0; i < jsonArray.length(); i++) {
         JSONObject obj2 = (JSONObject) jsonArray.get(i);
         obj2 = (JSONObject) obj2.get("label");
         publisherSet.add(obj2.get("value").toString());
       }
 
     } catch (JSONException e) {
       e.printStackTrace();
     }
     return publisherSet;
   }
 
   public ArrayList<String> getTopicsByPartialName(String name, String language) {
     ArrayList<String> results = new ArrayList<String>();
 
     String comparename = ms.charactermapping(name);
 
     for (String[] s : AutocompleteCache.getTopicList()) {
       String s0 = ms.charactermapping(s[0]);
       if (s0.toLowerCase().startsWith(comparename.toLowerCase()) && (language.equalsIgnoreCase(s[1]) || language.equals(""))) {
         results.add(s[0]);
       }
     }
 
     return results;
   }
 
   public ArrayList<String> getPublishersByPartialName(String name) {
     ArrayList<String> results = new ArrayList<String>();
 
     String comparename = ms.charactermapping(name);
 
     for (String s : AutocompleteCache.getPublisherSet()) {
       String s0 = ms.charactermapping(s);
       if (s0 != null && s0.toLowerCase().startsWith(comparename.toLowerCase())) {
         results.add(s);
       }
     }
     return results;
   }
 
   public String describeResource(String identifier) {
     String query = "DESCRIBE <" + identifier + "> ?rest WHERE {\n" +
             "<" + identifier + "> ?p ?rest . \n}";
 
     Object result = sparqlDispatcher.query(query);
     return result.toString();
 
   }
 
   public String getAllComments() {
     String queryString = StringUtils.join("\n", new String[]{
             "PREFIX sioc: <http://rdfs.org/sioc/ns#>\n" +
                     "PREFIX sub: <http://xmlns.computas.com/sublima#>\n" +
                     "PREFIX dct: <http://purl.org/dc/terms/>\n" +
                     "\n" +
                     "CONSTRUCT {\n" +
                     "    ?comment a sioc:Item ;\n" +
                     "             sioc:content ?content ;\n" +
                     "             dct:dateAccepted ?date ;\n" +
                     "             sioc:has_creator ?creator ;\n" +
                     "             sioc:has_owner ?owner .\n" +
                     "    ?owner a sub:Resource ;\n" +
                     "           dct:title ?title ; \n" +
                     "           dct:identifier ?identifier .\n" +
                     "}\n" +
                     "WHERE {\n" +
                     "    ?comment a sioc:Item ;\n" +
                     "             sioc:content ?content ;\n" +
                     "             dct:dateAccepted ?date ;\n" +
                     "             sioc:has_creator ?creator ;\n" +
                     "             sioc:has_owner ?owner .\n" +
                     "    ?owner a sub:Resource ;\n" +
                     "           dct:title ?title ; \n" +
                     "           dct:identifier ?identifier .\n" +
                     "}"});
 
     logger.trace("AdminService.getAllComments() executing");
     Object queryResult = sparqlDispatcher.query(queryString);
 
     return queryResult.toString();
   }
 
   public String getTopicDetailsForTopicPage(String subject) {
 
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
 
     logger.trace("AdminService.getTopicDetailsForTopicPage() executing");
     Object queryResult = sparqlDispatcher.query(queryString);
 
     return queryResult.toString();
   }
 
   public String getTopicDetailsForTopicPageFromAdmin(String subject) {
 
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
             "                ?p ?rest .}"});
     logger.trace("AdminService.getTopicDetailsForTopicPage() executing");
     Object queryResult = sparqlDispatcher.query(queryString);
 
     return queryResult.toString();
   }
 
   public String getNavigationDetailsForTopicPage(String subject) {
 
     // This query, relies on that all relations are explicitly stated.
     // I.e. a triple for both skos:broader and skos:narrower must exist.
     // Small fix added allowing concepts to not have relations at all.
     String sparqlConstructQuery =
             "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
                     "prefix skos: <http://www.w3.org/2004/02/skos/core#> \n" +
                     "prefix owl: <http://www.w3.org/2002/07/owl#> \n" +
                     "CONSTRUCT {\n" +
                     subject + "\n" +
                     "   a skos:Concept ;\n" +
                     "   skos:prefLabel ?preflabel ; \n" +
                     "   skos:altLabel ?altlabel ;  \n" +
                     "   skos:definition ?definition ; \n" +
                     "   ?semrelation ?object . \n" +
                     "?semrelation rdfs:subPropertyOf skos:semanticRelation ;\n" +
                     "   rdfs:label ?semrellabel ;\n" +
                     "   a owl:ObjectProperty .\n" +
                     "?object skos:prefLabel ?preflabel2 ; \n" +
                     "   a skos:Concept .\n" +
                     "}\n" +
                     "WHERE {\n" +
                     subject + "\n" +
                     "   skos:prefLabel ?preflabel ;\n" +
                     "   a skos:Concept .\n" +
                     "OPTIONAL {\n" +
                     subject + "\n" +
                     "   skos:altLabel ?altlabel .\n" +
                     "}\n" +
                     "OPTIONAL {\n" +
                     subject + "\n" +
                     "   skos:definition ?definition .\n" +
                     "}\n" +
                     "OPTIONAL {\n" +
                     subject + "\n" +
                     "   ?semrelation ?object .\n" +
                     "?semrelation rdfs:subPropertyOf skos:semanticRelation ;\n" +
                     "   rdfs:label ?semrellabel ;\n" +
                     "   a owl:ObjectProperty .\n" +
                     "?object  a skos:Concept ;\n" +
                     "   skos:prefLabel ?preflabel2 .\n" +
                     "}\n" +
                     "}";
 
 
     logger.trace("AdminService.getNavigationDetailsForTopicPage() executing");
     Object queryResult = sparqlDispatcher.query(sparqlConstructQuery);
 
     return queryResult.toString();
   }
 
   /**
    * This method returns the uri of the inverse relation if such a relation exists
    *
    * @param relationUri The uri of the relation to check wether has an inverse relation or not
    * @return a String with the uri of the inverse relation if any
    */
   public String getInverseRelationUriIfAny(String relationUri) {
 
     if (!relationUri.startsWith("<") && !relationUri.endsWith(">")) {
       relationUri = "<" + relationUri + ">";
     }
 
     String sparqlConstructQuery = "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
             "\n" +
             "SELECT ?uri \n" +
             "WHERE {\n" +
             relationUri + " owl:inverseOf ?uri .\n" +
 
             "}";
 
     logger.trace("AdminService.getInverseRelationUriIfAny() executing");
     Object queryResult = sparqlDispatcher.query(sparqlConstructQuery);
 
     try {
 
       DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
       Document doc = builder.parse(new ByteArrayInputStream(queryResult.toString().getBytes("UTF-8")));
       XPathExpression expr = XPathFactory.newInstance().newXPath().compile("//td");
       NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
       return nodes.item(0) == null ? "" : getTextContent(nodes.item(0));
 
     } catch (Exception e) {
       e.printStackTrace();
       return "";
     }
   }
 
   public boolean isSymmetricProperty(String relationUri) {
 
     if (!relationUri.startsWith("<") && !relationUri.endsWith(">")) {
       relationUri = "<" + relationUri + ">";
     }
     String sparqlConstructQuery = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
             "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
             "\n" +
             "ASK { " + relationUri + " a owl:SymmetricProperty  . \n" +
             "}";
 
     logger.trace("AdminService.isSymmetricProperty() executing");
     Object queryResult = sparqlDispatcher.query(sparqlConstructQuery, "ASK");
 
     return queryResult.toString().contains("true");
 
   }
 
   public boolean isRelation(String relationUri) {
 
     if (!relationUri.startsWith("<") && !relationUri.endsWith(">")) {
       relationUri = "<" + relationUri + ">";
     }
 
     String sparqlConstructQuery = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
             "\n" +
             "ASK { " + relationUri + " rdfs:subPropertyOf <http://www.w3.org/2004/02/skos/core#semanticRelation>  .\n}";
 
 
     logger.trace("AdminService.isRelation() executing");
     Object queryResult = sparqlDispatcher.query(sparqlConstructQuery, "ASK");
 
     if (queryResult != null) {
       return queryResult.toString().contains("true");
     } else {
       return false;
     }
 
   }
 
   public boolean isInactiveUser(String name) {
     String sparqlAsk = "PREFIX wdr: <http://www.w3.org/2007/05/powder#>" +
             "PREFIX sioc: <http://rdfs.org/sioc/ns#>" +
             "ASK { ?s sioc:email <mailto:" + name + "> ;" +
             "         wdr:describedBy <http://sublima.computas.com/status/inaktiv> . }";
 
     logger.trace("AdminService.isInactiveUser() executing");
     Object queryResult = sparqlDispatcher.query(sparqlAsk, "ASK");
 
     if (queryResult != null) {
       return queryResult.toString().contains("true");
     } else {
       return false;
     }
   }
 
   public String query(String query) {
 
     logger.trace("AdminService.query() executing");
     Object queryResult = sparqlDispatcher.query(query);
 
     return queryResult.toString();
   }
 
   public boolean insertSubjectOf() {
     String sparul = "PREFIX dct: <http://purl.org/dc/terms/>\n" +
             "PREFIX sub: <http://xmlns.computas.com/sublima#>\n" +
             "INSERT INTO <" + SettingsService.getProperty("sublima.basegraph") + "> {?s sub:isSubjectOf ?o}\n" +
             "WHERE {?o dct:subject ?s}";
 
     return sparulDispatcher.query(sparul);
   }
 
   public boolean deleteSubjectOf() {
     String sparul = "PREFIX sub: <http://xmlns.computas.com/sublima#>\n" +
             "DELETE FROM <" + SettingsService.getProperty("sublima.basegraph") + "> {?s sub:isSubjectOf ?o}\n" +
             "WHERE {?s sub:isSubjectOf ?o} ";
 
     return sparulDispatcher.query(sparul);
   }
 
 
   public boolean deleteComment(String uri) {
     String sparqlDelete = "DELETE FROM <" + SettingsService.getProperty("sublima.basegraph") + "> {<" + uri + "> ?p1 ?o1 . ?s ?p2 <" + uri + "> .} WHERE {<" + uri + "> ?p1 ?o1 . ?s ?p2 <" + uri + "> .}";
     return sparulDispatcher.query(sparqlDelete);
   }
 
   public boolean hasPublisherResources(String uri) {
     String sparqlAsk = "ASK { ?s ?p <" + uri + "> . }";
 
     logger.trace("AdminService.hasPublisherResources() executing");
     Object queryResult = sparqlDispatcher.query(sparqlAsk, "ASK");
 
     if (queryResult != null) {
       return queryResult.toString().contains("true");
     } else {
       return false;
     }
   }
 
   public String getPublisherByURI(String publisherURI) {
     String findPublisherByURIQuery = StringUtils.join("\n", new String[]{
             "PREFIX dct: <http://purl.org/dc/terms/>",
             "DESCRIBE <" + publisherURI + "> ?resource ?subject",
             "WHERE {",
             "OPTIONAL { ?resource dct:publisher <" + publisherURI + "> .",
             "?resource dct:subject ?subject . }",
             "}"});
 
 
     logger.trace("AdminController.showPublisherByURI() --> SPARQL query sent to dispatcher: \n" + findPublisherByURIQuery);
     Object queryResult = sparqlDispatcher.query(findPublisherByURIQuery);
     return queryResult.toString();
   }
 
 
   public HashSet<String> getAllExternalResourcesURLs() {
     HashSet<String> results = new HashSet<String>();
     String query = "PREFIX sub: <http://xmlns.computas.com/sublima#>\n" +
             "SELECT ?uri WHERE { ?uri a sub:Resource }";
 
     logger.trace("AdminService.getAllExternalResourcesURLs() executing");
     Object queryResult = sparqlDispatcher.query(query);
     try {
 
       DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
       Document doc = builder.parse(new ByteArrayInputStream(queryResult.toString().getBytes("UTF-8")));
       XPathExpression expr = XPathFactory.newInstance().newXPath().compile("//td");
       NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
 
       for (int i = 0; i < nodes.getLength(); i++) {
         results.add(getTextContent(nodes.item(i)));
       }
 
     } catch (Exception e) {
       e.printStackTrace();
     }
 
     return results;
 
   }
 }
