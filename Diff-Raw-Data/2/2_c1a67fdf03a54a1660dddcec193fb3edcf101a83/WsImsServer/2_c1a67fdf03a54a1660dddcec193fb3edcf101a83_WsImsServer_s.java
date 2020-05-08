 // BridgeDb,
 // An abstraction layer for identifier mapping services, both local and online.
 //
 // Copyright 2006-2009  BridgeDb developers
 // Copyright 2012-2013  Christian Y. A. Brenninkmeijer
 // Copyright 2012-2013  OpenPhacts
 //
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at
 //
 // http://www.apache.org/licenses/LICENSE-2.0
 //
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.
 //
 package uk.ac.manchester.cs.openphacts.ims.ws.server;
 
 import com.sun.jersey.core.header.FormDataContentDisposition;
 import com.sun.jersey.multipart.FormDataParam;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.util.List;
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import org.apache.log4j.Logger;
 import org.bridgedb.uri.Mapping;
 import org.bridgedb.utils.BridgeDBException;
 import org.bridgedb.ws.WsUriConstants;
 import org.bridgedb.ws.uri.WSUriServer;
 import uk.ac.manchester.cs.openphacts.valdator.bean.StatementBean;
 import uk.ac.manchester.cs.openphacts.valdator.bean.URIBean;
 import uk.ac.manchester.cs.openphacts.valdator.metadata.MetaDataSpecification;
 import uk.ac.manchester.cs.openphacts.valdator.rdftools.ExampleConstants;
 import uk.ac.manchester.cs.openphacts.valdator.rdftools.RdfFactory;
 import uk.ac.manchester.cs.openphacts.valdator.rdftools.RdfInterface;
 import uk.ac.manchester.cs.openphacts.valdator.rdftools.VoidValidatorException;
 import uk.ac.manchester.cs.openphacts.valdator.server.FrameInterface;
 import uk.ac.manchester.cs.openphacts.valdator.server.HtmlWSInterface;
 import uk.ac.manchester.cs.openphacts.valdator.server.WsValidatorServer;
 import uk.ac.manchester.cs.openphacts.valdator.ws.WsValidationConstants;
 import uk.ac.manchester.cs.openphacts.validator.Validator;
 import uk.ac.manchester.cs.openphacts.validator.ValidatorExampleConstants;
 import uk.ac.manchester.cs.openphacts.validator.ValidatorImpl;
 
 /**
  *
  * @author Christian
  */
 public class WsImsServer extends WSUriServer implements FrameInterface, HtmlWSInterface{
     
     private final WsValidatorServer wsValidatorServer;
 
     static final Logger logger = Logger.getLogger(WsImsServer.class);
     
     public WsImsServer()  throws BridgeDBException   {
         super();
         wsValidatorServer = new WsValidatorServer();
         try {
             RdfInterface rdfInterface = RdfFactory.getValidatorFilebase();
             Validator validator = new ValidatorImpl(rdfInterface);
             wsValidatorServer.setUp(rdfInterface, validator, this);
             MetaDataSpecification.LoadSpecification(ValidatorExampleConstants.SIMPLE_FILE, 
                     ValidatorExampleConstants.SIMPLE_NAME, ValidatorExampleConstants.SIMPLE_DESCRIPTION);
             logger.info("WsImsServer setup");    
         } catch (VoidValidatorException ex) {
             logger.error("Initisation of WsImsServer Service failed!", ex);
         }
     }
                
     /**
     * Welcome page for the Serivce.
      * 
      * Expected to be overridden by the QueryExpander
      * 
      * @param httpServletRequest
      * @return
      * @throws BridgeDBException
      * @throws UnsupportedEncodingException 
      */
     @GET
     @Produces(MediaType.TEXT_HTML)
     @Override
     public Response welcomeMessage(@Context HttpServletRequest httpServletRequest) throws BridgeDBException, UnsupportedEncodingException {
         if (logger.isDebugEnabled()){
             logger.debug("welcomeMessage called!");
         }
         return bridgeDbHome(httpServletRequest);
     }
 
     @Override
     protected String serviceName(){
         return "OpenPhacts IMS ";
     }
 
     @Override
     protected void top(StringBuilder sb, String title){
         sb.append("<div id=\"container\">");
         sb.append("<div id=\"top\">");
         sb.append("<a href=\"http://www.cs.manchester.ac.uk/\">" +
         		"<img style=\"float: left; border: none; padding: 0px; margin: 0px;\" " +
         		"src=\"http://assets.manchester.ac.uk/logos/university-1.png\" " +
         		"alt=\"The University of Manchester\" height=\"50\"></img></a>");
         sb.append("<a href=\"http://www.openphacts.org/\">" +
         		"<img style=\"float: right; border: none; padding: 0px; margin: 0px;\" " +
         		"src=\"http://www.openphacts.org/images/stories/banner.jpg\" " +
         		"alt=\"Open PHACTS\" height=\"50\"></img></a>");
         sb.append("<h1>");
         sb.append(title);
         sb.append("</h1>");
         sb.append("</div>");   
     }
 
     /**
      * Allows Super classes to add to the side bar
      */
     @Override
     public void addSideBarMiddle(StringBuilder sb, HttpServletRequest httpServletRequest) {
         super.addSideBarMiddle(sb, httpServletRequest);
         addValidatorSideBar(sb, httpServletRequest);
     }
    
     protected void addValidatorSideBar(StringBuilder sb, HttpServletRequest httpServletRequest) {
         sb.append("<div class=\"menugroup\">OPS Validation Service</div>");
         addSideBarItem(sb, WsValidationConstants.VALIDATE_HOME, "Home", httpServletRequest);
         addSideBarItem(sb, WsValidationConstants.VALIDATE,WsValidationConstants.VALIDATE, httpServletRequest);
         addSideBarItem(sb, WsValidationConstants.STATEMENT_LIST, WsValidationConstants.STATEMENT_LIST,  httpServletRequest);
         addSideBarItem(sb, WsValidationConstants.BY_RESOURCE, WsValidationConstants.BY_RESOURCE,  httpServletRequest);
         addSideBarItem(sb, WsValidationConstants.SPARQL, WsValidationConstants.SPARQL, httpServletRequest);
         addSideBarItem(sb, WsValidationConstants.LOAD_URI, WsValidationConstants.LOAD_URI, httpServletRequest);
     }
 
     @Override
     public void footerAndEnd(StringBuilder sb) {
         sb.append("</div>\n<div id=\"footer\">");
         sb.append("This site is run by <a href=\"https://wiki.openphacts.org/index.php/User:Christian\">Christian Brenninkmeijer</a>.");
         sb.append("\n<div></body></html>");
     }
 
 /*    @Override
     public String getExampleResource() {
         return ExampleConstants.EXAMPLE_RESOURCE;
     }
 
     @Override
     public String getExampleURI() {
         return ExampleConstants.EXAMPLE_CONTEXT;
     }
 
     @Override
     public String getExampleSpecificationName() {
         return ValidatorExampleConstants.SIMPLE_NAME;
     }
 
     @Override
     public String getExampleQuery() {
         return ExampleConstants.EXAMPLE_QUERY;
     }
 
     @Override
     public String getExampleText() {
         return "@prefix : <http://example.com/part1#> .\n"
         + "@prefix ops: <http://openphacts.cs.man.ac.uk:9090/Void/testOntology.owl#> .\n"
         + "@prefix void: <http://rdfs.org/ns/void#> .\n"
         + "@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .\n"
         + "\n"
         + ":person1 a ops:Parent;\n"
         + "    ops:hasName \"John\";\n"
         + "    ops:hasChild :person2.\n"
         + "\n"
         + ":person2 a ops:Person;\n"
         + "    ops:hasName \"Peter\";\n"
         + "    ops:hasPhoneNumber \"1234567\";\n"
         + "    ops:hasBirthdate \"2003-01-17T16:02:27Z\"^^xsd:dateTime;\n"
         + "    ops:hasStreet \"mainStreet\";\n"
         + "    ops:hasHouseNumber \"23\";\n"
         + "    ops:hasWebsite <http://bbc.co.uk>.\n";
     }    
 */    
     @Override
     @GET
     @Produces(MediaType.TEXT_HTML)
     @Path(WsValidationConstants.VALIDATE_HOME)
     public Response validateHome(@Context HttpServletRequest httpServletRequest) throws VoidValidatorException {
         return wsValidatorServer.validateHome(httpServletRequest);
     }
 
     @GET
     @Produces(MediaType.TEXT_HTML)
     @Path(WsValidationConstants.STATEMENT_LIST)
     @Override
     public Response getStatementList(@QueryParam(WsValidationConstants.SUBJECT) String subjectString, 
             @QueryParam(WsValidationConstants.PREDICATE) String predicateString, 
             @QueryParam(WsValidationConstants.OBJECT) String objectString, 
             @QueryParam(WsValidationConstants.CONTEXT) List<String> contextStrings,
             @Context HttpServletRequest httpServletRequest) throws VoidValidatorException {
        return wsValidatorServer.getStatementList(subjectString, predicateString, objectString, contextStrings, httpServletRequest);
     }
 
     @GET
     @Produces(MediaType.TEXT_HTML)
     @Path(WsValidationConstants.BY_RESOURCE)
     @Override
     public Response getByResource(@QueryParam(WsValidationConstants.RESOURCE) String resourceString,
             @Context HttpServletRequest httpServletRequest) throws VoidValidatorException{        
         return wsValidatorServer.getByResource(resourceString, httpServletRequest);
     }
 
     @GET
     @Produces(MediaType.TEXT_HTML)
     @Path(WsValidationConstants.LOAD_URI)
     @Override
     public Response loadURI(@QueryParam(WsValidationConstants.URI) String address, 
             @QueryParam(WsValidationConstants.RDF_FORMAT) String formatName,
             @Context HttpServletRequest httpServletRequest)throws VoidValidatorException {
         return wsValidatorServer.loadURI(address, formatName, httpServletRequest);
     }
 
     @GET
     @Produces(MediaType.TEXT_HTML)
     @Path(WsValidationConstants.SPARQL)
     @Override
     public Response runSparqlQuery(@QueryParam(WsValidationConstants.QUERY)String query, 
             @QueryParam(WsValidationConstants.FORMAT)String formatName,
             @Context HttpServletRequest httpServletRequest)throws VoidValidatorException {
         return wsValidatorServer.runSparqlQuery(query, formatName, httpServletRequest);
     }
 
     @GET
     @Produces(MediaType.TEXT_HTML)
     @Path(WsValidationConstants.VALIDATE)
     @Override
     public Response validate(@QueryParam(WsValidationConstants.TEXT) String text, 
             @QueryParam(WsValidationConstants.URI) String uri, 
             @QueryParam(WsValidationConstants.RDF_FORMAT) String rdfFormat,
             @QueryParam(WsValidationConstants.SPECIFICATION) String specification,
             @QueryParam(WsValidationConstants.INCLUDE_WARNINGS) Boolean includeWarning,
             @Context HttpServletRequest httpServletRequest) throws VoidValidatorException {        
         return wsValidatorServer.validate(text, uri, rdfFormat, specification, includeWarning, httpServletRequest);
     }
 
     @GET
     @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
     @Path(WsValidationConstants.STATEMENT_LIST)
     @Override
     public List<StatementBean> getStatementList(@QueryParam(WsValidationConstants.SUBJECT) String subjectString, 
             @QueryParam(WsValidationConstants.PREDICATE) String predicateString, 
             @QueryParam(WsValidationConstants.OBJECT) String objectString, 
             @QueryParam(WsValidationConstants.CONTEXT) List<String> contextStrings) throws VoidValidatorException {
         return wsValidatorServer.getStatementList(subjectString, predicateString, objectString, contextStrings);
     }
 
     @Override
     @GET
     @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
     @Path(WsValidationConstants.BY_RESOURCE)
     public List<StatementBean> getByResource(@QueryParam(WsValidationConstants.RESOURCE) String resourceString) 
             throws VoidValidatorException{
         return wsValidatorServer.getByResource(resourceString);
     }
 
     @Override
     @GET
     @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
     @Path(WsValidationConstants.LOAD_URI)
     public URIBean loadURI(@QueryParam(WsValidationConstants.URI) String address, 
         @QueryParam(WsValidationConstants.RDF_FORMAT) String formatName)throws VoidValidatorException {
         return wsValidatorServer.loadURI(address, formatName);
     }
 
     @Override
     @GET
     @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
     @Path(WsValidationConstants.SPARQL)
     public String runSparqlQuery(@QueryParam(WsValidationConstants.QUERY)String query, 
             @QueryParam(WsValidationConstants.FORMAT)String formatName) throws VoidValidatorException{
         return wsValidatorServer.runSparqlQuery(query, formatName);
     }
 
     @Override
     public String validate(String text, String uri, String rdfFormat, String specification, Boolean includeWarning) throws VoidValidatorException {
         return wsValidatorServer.validate(text, uri, rdfFormat, specification, includeWarning);
     }
     
     /*@Produces({MediaType.TEXT_PLAIN, MediaType.TEXT_HTML})
     @Path("/" + WsUriConstants.MAPPING + WsImsConstants.RDF)
     public String getMappingRDF() throws BridgeDBException {
         throw new BridgeDBException(WsUriConstants.ID + " parameter missing.");     
     }
     
     @Produces({MediaType.TEXT_PLAIN})
     @Path("/" + WsUriConstants.MAPPING + WsImsConstants.RDF + "/{id}")
     public String getMappingRDF(@PathParam(WsUriConstants.ID) String idString) throws BridgeDBException {
         if (idString == null) throw new BridgeDBException(WsUriConstants.ID + " parameter missing.");
         if (idString.isEmpty()) throw new BridgeDBException(WsUriConstants.ID + " parameter may not be null.");
         int id = Integer.parseInt(idString);
         Mapping mapping = uriMapper.getMapping(id);
         return mapping.toString();
     }
     
     @GET
     @Produces(MediaType.TEXT_HTML)
     @Path("/" + WsUriConstants.MAPPING + WsImsConstants.RDF + "/{id}")
     public Response getMappingRdfHtml(@PathParam(WsUriConstants.ID) String idString) throws BridgeDBException {
         String rdf = getMappingRDF(idString);
         return Response.ok(rdf, MediaType.TEXT_HTML).build();
     }
     */
 }
 
 
