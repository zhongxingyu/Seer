 package org.geoserver.test;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.lang.reflect.Field;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.StringTokenizer;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpServletResponseWrapper;
 import javax.xml.XMLConstants;
 import javax.xml.namespace.QName;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.validation.Schema;
 import javax.xml.validation.SchemaFactory;
 import javax.xml.validation.Validator;
 
 import junit.framework.TestCase;
 
 import org.geoserver.catalog.Catalog;
 import org.geoserver.config.GeoServerLoader;
 import org.geoserver.data.test.TestData;
 import org.geoserver.ows.util.ResponseUtils;
 import org.geoserver.platform.GeoServerExtensions;
 import org.geoserver.platform.GeoServerResourceLoader;
 import org.geotools.data.FeatureSource;
 import org.geotools.factory.Hints;
 import org.geotools.util.logging.Log4JLoggerFactory;
 import org.geotools.util.logging.Logging;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.simple.SimpleFeatureType;
 import org.springframework.web.context.WebApplicationContext;
 import org.springframework.web.servlet.DispatcherServlet;
 import org.springframework.web.servlet.HandlerInterceptor;
 import org.vfny.geoserver.global.Data;
 import org.vfny.geoserver.global.FeatureTypeInfo;
 import org.vfny.geoserver.global.GeoServer;
 import org.vfny.geoserver.global.GeoserverDataDirectory;
 import org.vfny.geoserver.global.NameSpaceInfo;
 import org.vfny.geoserver.global.FeatureTypeInfo;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.ErrorHandler;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 
 import com.mockrunner.mock.web.MockHttpServletRequest;
 import com.mockrunner.mock.web.MockHttpServletResponse;
 import com.mockrunner.mock.web.MockHttpSession;
 import com.mockrunner.mock.web.MockServletConfig;
 import com.mockrunner.mock.web.MockServletContext;
 import com.mockrunner.mock.web.MockServletOutputStream;
 
 /**
  * Base test class for GeoServer unit tests.
  * <p>
  * This test case provides a spring application context which loads the
  * application contexts from all modules on the classpath.
  * </p>
  * <p>
  * Subclasses should provide a data directory location, that will be inserted
  * in the mock servlet context for GeoServer to pick up
  * </p>
  * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
  * @author Andrea Aime, The Open Planning Project
  */
 public abstract class GeoServerAbstractTestSupport extends OneTimeSetupTest {
     /**
      * Common logger for test cases
      */
     protected static final Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.geoserver.test"); 
     
     /**
      * Application context
      */
     protected static GeoServerTestApplicationContext applicationContext;
 
     protected static TestData testData;
     
     /**
      * Returns a test data instance
      * 
      * @return
      */
     protected abstract TestData buildTestData() throws Exception;
     
     public TestData getTestData() {
         return testData;
     }
 
     /**
      * Override runTest so that the test will be skipped if the TestData is not
      * available
      */
     protected void runTest() throws Throwable {
         if (getTestData().isTestDataAvailable()) {
             super.runTest();
         } else {
             LOGGER.warning("Skipping " + getClass() + "." + getName()
                     + " since test data is not available");
         }
     }
 
     /**
      * If subclasses override they *must* call super.setUp() first.
      */
     @Override
     protected void oneTimeSetUp() throws Exception {
         if (System.getProperty("org.geotools.referencing.forceXY") == null) {
             System.setProperty("org.geotools.referencing.forceXY", "true");
         }
         if (Boolean.TRUE.equals(Hints.getSystemDefault(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER))) {
             Hints.putSystemDefault(Hints.FORCE_AXIS_ORDER_HONORING, "http");
         }
 
         // setup quiet logging (we need to to this here because Data
         // is loaded before GoeServer has a chance to setup logging for good)
         try {
             Logging.ALL.setLoggerFactory(Log4JLoggerFactory.getInstance());
         } catch (Exception e) {
             LOGGER.log(Level.SEVERE, "Could not configure log4j logging redirection", e);
         }
         GeoServer.suppressLoggingConfiguration();
         GeoServer.configureGeoServerLogging(getClass().getResourceAsStream(getLogConfiguration()), false, true, null);
 
         // set up test data and, if succeeds, create a mock servlet context and start up the spring configuration
         testData = buildTestData();
         testData.setUp();
         if (testData.isTestDataAvailable()) {
             MockServletContext servletContext = new MockServletContext();
             servletContext.setInitParameter("GEOSERVER_DATA_DIR", testData.getDataDirectoryRoot()
                     .getPath());
             servletContext.setInitParameter("serviceStrategy", "PARTIAL-BUFFER2");
 
             applicationContext = new GeoServerTestApplicationContext(getSpringContextLocations(),
                     servletContext);
             applicationContext.refresh();
 
             // set the parameter after a refresh because it appears a refresh
             // wipes
             // out all parameters
             servletContext.setAttribute(
                     WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE,
                     applicationContext);
         }
     }
     
     /**
      * Returns the spring context locations to be used in order to build the GeoServer Spring
      * context. Subclasses might want to provide extra locations in order to test extension points.
      * @return
      */
     protected String[] getSpringContextLocations() {
         return new String[] {
                 "classpath*:/applicationContext.xml",
                 "classpath*:/applicationSecurityContext.xml"
             };
     }
 
     /**
      * Returns the logging configuration path. The default value is "/TEST_LOGGING.properties", which
      * is a pretty quiet configuration. Should you need more verbose logging override this method
      * in subclasses and choose a different configuration, for example "/DEFAULT_LOGGING.properties".
      * @return
      */
     protected String getLogConfiguration() {
         return "/TEST_LOGGING.properties";
     }
 
     /**
      * Returns a default services.xml file with WMS, WFS and WCS enabled. Subclasses may
      * need to override this in order to test extra services or specific configurations
      * @return
      */
     protected URL getServicesFile() {
         return GeoServerAbstractTestSupport.class.getResource("services.xml");
     }
 
     /**
      * Subclasses may override this method to force memory cleaning before the 
      * test data dir is cleaned up. This is necessary on windows if coverages are used in the
      * test, since readers might still be around in the heap as garbage without having
      * been disposed of
      * @return
      */
     protected boolean isMemoryCleanRequired() {
         return false;
     }
 
     /**
      * If subclasses overide they *must* call super.tearDown() first.
      */
     @Override
     protected void oneTimeTearDown() throws Exception {
         if(getTestData().isTestDataAvailable()) {
             try {
                 //kill the context
                 applicationContext.destroy();
         
                 if(isMemoryCleanRequired()) {
                     System.gc(); 
                     System.runFinalization();
                 }
                 
                 if(getTestData() != null) {
                     // this cleans up the data directory static loader, if we don't the next test
                     // will keep on running on the current data dir
                     GeoserverDataDirectory.destroy();
                     getTestData().tearDown();
                 }
             } finally {
                 applicationContext = null;
                 testData = null;
             }
         }
     }
      
     /**
      * Reloads the catalog and configuration from disk.
      * <p>
      * This method can be used by subclasses from a test method after they have
      * changed the configuration on disk.
      * </p>
      */
     protected void reloadCatalogAndConfiguration() throws Exception {
         GeoServerLoader loader = GeoServerExtensions.bean( GeoServerLoader.class , applicationContext );
         loader.reload();
     }
     
     /**
      * Accessor for global catalog instance from the test application context.
      */
     protected Data getCatalog() {
         return (Data) applicationContext.getBean("data");
     }
 
     /**
      * Accessor for global geoserver instance from the test application context.
      */
     protected GeoServer getGeoServer() {
         return (GeoServer) applicationContext.getBean("geoServer");
     }
     
     /**
      * Accessor for global resource loader instance from the test application context.
      */
     protected GeoServerResourceLoader getResourceLoader() {
         return (GeoServerResourceLoader) applicationContext.getBean( "resourceLoader" );
     }
 
     /**
      * Loads a feature source from the catalog.
      *
      * @param typeName The qualified type name of the feature source.
      */
     @SuppressWarnings("unchecked")
     protected FeatureSource<SimpleFeatureType, SimpleFeature> getFeatureSource(QName typeName)
         throws IOException {
         return getCatalog().getFeatureSource(typeName.getPrefix(), typeName.getLocalPart());
     }
 
     /**
      * Get the FeatureTypeInfo for a featuretype to allow configuration tweaks for tests.
      *
      * @param typename the QName for the type
      */
     protected FeatureTypeInfo getFeatureTypeInfo(QName typename){
         return getCatalog().getFeatureTypeInfo(typename);
     }
 
     /**
      * Get the FeatureTypeInfo for a featuretype by the layername that would be used in a request.
      *
      * @param typename the layer name for the type
      */
     protected FeatureTypeInfo getFeatureTypeInfo(String typename){
         return getFeatureTypeInfo(resolveLayerName(typename));
     }
 
     /**
      * Get the QName for a layer specified by the layername that would be used in a request.
      * @param typename the layer name for the type
      */
     protected QName resolveLayerName(String typename){
         int i = typename.indexOf(":");
         String prefix = typename.substring(0, i);
         String name = typename.substring(i + 1);
         NameSpaceInfo ns = getCatalog().getNamespaceMetaData(prefix);
         QName qname = new QName(ns.getUri(), name, ns.getPrefix());
         return qname;
     }
 
     /**
      * @deprecated use {@link #getLayerId(QName)}.
      */
     public final String layerId(QName layerName) {
         return getLayerId( layerName );
     }
     
     /**
      * Given a qualified layer name returns a string in the form "prefix:localPart" if prefix
      * is available, "localPart" if prefix is null
      * @param layerName
      * @return
      */
     public String getLayerId(QName layerName) {
         if(layerName.getPrefix() != null)
             return layerName.getPrefix() + ":" + layerName.getLocalPart();
         else
             return layerName.getLocalPart();
     }
 
     /**
      * Convenience method for subclasses to create mock http servlet requests.
      * <p>
      * Examples of using this method are:
      * <pre>
      * <code>
      *   createRequest( "wfs?request=GetCapabilities" );  //get
      *   createRequest( "wfs" ); //post
      * </code>
      * </pre>
      * </p>
      * @param path The path for the request and optional the query string.
      * @return
      */
     protected MockHttpServletRequest createRequest(String path) {
         MockHttpServletRequest request = new MockHttpServletRequest();
 
         request.setScheme("http");
         request.setServerName("localhost");
         request.setContextPath("/geoserver");
         request.setRequestURI(ResponseUtils.stripQueryString(ResponseUtils.appendPath(
                     "/geoserver/", path)));
         request.setRequestURL(ResponseUtils.appendPath("http://localhost/geoserver", path ) );
         request.setQueryString(ResponseUtils.getQueryString(path));
         request.setRemoteAddr("127.0.0.1");
         request.setServletPath(ResponseUtils.makePathAbsolute( ResponseUtils.stripRemainingPath(path)) );
         request.setPathInfo(ResponseUtils.makePathAbsolute( ResponseUtils.stripBeginningPath( path))); 
         
         kvp(request, path);
 
         MockHttpSession session = new MockHttpSession();
         session.setupServletContext(new MockServletContext());
         request.setSession(session);
 
         request.setUserPrincipal(null);
 
         return request;
     }
 
     /**
      * Convenience method for subclasses to create mock http servlet requests.
      * <p>
      * Examples of using this method are:
      * <pre>
      * <code>
      *   Map kvp = new HashMap();
      *   kvp.put( "service", "wfs" );
      *   kvp.put( "request", "GetCapabilities" );
      *   
      *   createRequest( "wfs", kvp );
      * </code>
      * </pre>
      * </p>
      * @param path The path for the request, minus any query string parameters.
      * @param kvp The key value pairs to be put in teh query string. 
      * 
      */
     protected MockHttpServletRequest createRequest( String path, Map kvp ) {
         StringBuffer q = new StringBuffer();
         for ( Iterator e = kvp.entrySet().iterator(); e.hasNext(); ) {
             Map.Entry entry = (Map.Entry) e.next();
             q.append( entry.getKey() ).append("=").append( entry.getValue() );
             q.append( "&" );
         }
         q.setLength(q.length()-1);
         
         return createRequest( ResponseUtils.appendQueryString(path, q.toString() ) );
     }
     
     /**
      * Executes an ows request using the GET method.
      *
      * @param path The porition of the request after hte context, 
      *      example: 'wms?request=GetMap&version=1.1.1&..."
      * 
      * @return An input stream which is the result of the request.
      * 
      * @throws Exception
      */
     protected InputStream get( String path ) throws Exception {
         MockHttpServletResponse response = getAsServletResponse(path);
         return new ByteArrayInputStream( response.getOutputStreamContent().getBytes() );
     }
     
     /**
      * Executes an ows request using the GET method.
      *
      * @param path The porition of the request after hte context, 
      *      example: 'wms?request=GetMap&version=1.1.1&..."
      * 
      * @return the mock servlet response
      * 
      * @throws Exception
      */
     protected MockHttpServletResponse getAsServletResponse( String path ) throws Exception {
         MockHttpServletRequest request = createRequest( path ); 
         request.setMethod( "GET" );
         request.setBodyContent(new byte[]{});
         
         return dispatch( request );
     }
         
     /**
      * Executes an ows request using the POST method with key value pairs 
      * form encoded. 
      *
      * @param path The porition of the request after hte context, 
      *      example: 'wms?request=GetMap&version=1.1.1&..."
      * 
      * @return An input stream which is the result of the request.
      * 
      * @throws Exception
      */
     protected InputStream post( String path ) throws Exception {
         MockHttpServletRequest request = createRequest( path ); 
         request.setMethod( "POST" );
         request.setContentType( "application/x-www-form-urlencoded" );
         
         MockHttpServletResponse response = dispatch( request );
         return new ByteArrayInputStream( response.getOutputStreamContent().getBytes() );
     }
 
     /**
      * Executes a request with an empty body using the PUT method.
      *
      * @param path the portion of the request after the context, for example:
      *      "api/datastores.xml"
      *
      * @throws Exception
      */
     protected InputStream put(String path) throws Exception{
         return put(path, "");
     }
 
     /**
      * Executes a request with a default mimetype using the PUT method.
      *
      * @param path the portion of the request after the context, for example:
      *      "api/datastores.xml"
      * @param body the content to send as the body of the request
      *
      * @throws Exception
      */
     protected InputStream put(String path, String body) throws Exception{
         return put(path, body, "text/plain");
     }
 
     /**
      * Executes a request using the PUT method.
      *
      * @param path the portion of the request after the context, for example:
      *      "api/datastores.xml"
      * @param body the content to send as the body of the request
      * @param contentType the mime-type to set for the request being sent
      *
      * @throws Exception
      */
     protected InputStream put(String path, String body, String contentType) throws Exception {
         MockHttpServletRequest request = createRequest(path);
         request.setMethod("PUT");
         request.setContentType(contentType);
         request.setBodyContent(body);
 
         MockHttpServletResponse response = dispatch(request);
         return new ByteArrayInputStream(response.getOutputStreamContent().getBytes());
     }
 
     /**
      * Executes a request using the PUT method.
      *
      * @param path the portion of the request after the context, for example:
      *      "api/datastores.xml"
      * @param body the content to send as the body of the request
      * @param contentType the mime-type to set for the request being sent
      *
      * @throws Exception
      */
     protected InputStream put(String path, byte[] body, String contentType) throws Exception {
         MockHttpServletRequest request = createRequest(path);
         request.setMethod("PUT");
         request.setContentType(contentType);
         request.setBodyContent(body);
 
         MockHttpServletResponse response = dispatch(request);
         return new ByteArrayInputStream(response.getOutputStreamContent().getBytes());
     }
     
     /**
      * Executes an ows request using the POST method.
      * <p>
      * 
      * </p>
      * @param path The porition of the request after the context ( no query string ), 
      *      example: 'wms'. 
      * 
      * @return An input stream which is the result of the request.
      * 
      * @throws Exception
      */
     protected InputStream post( String path , String xml ) throws Exception {
         MockHttpServletResponse response = postAsServletResponse(path, xml);
         return new ByteArrayInputStream(response.getOutputStreamContent().getBytes());
     }
 
     /**
      * Executes an ows request using the POST method.
      * <p>
      * 
      * </p>
      * 
      * @param path
      *            The porition of the request after the context ( no query
      *            string ), example: 'wms'.
      * 
      * @return the servlet response
      * 
      * @throws Exception
      */
     protected MockHttpServletResponse postAsServletResponse(String path, String xml)
             throws Exception {
         MockHttpServletRequest request = createRequest(path);
         request.setMethod("POST");
         request.setContentType("application/xml");
         request.setBodyContent(xml);
 
         MockHttpServletResponse response = dispatch(request);
         return response;
     }
 
     /**
      * Extracts the true binary stream out of the response. The usual way (going
      * thru {@link MockHttpServletResponse#getOutputStreamContent()}) mangles
      * bytes if the content is not made of chars.
      * 
      * @param response
      * @return
      */
     protected InputStream getBinaryInputStream(MockHttpServletResponse response) {
         try {
             MockServletOutputStream os = (MockServletOutputStream) response.getOutputStream();
             final Field field = os.getClass().getDeclaredField("buffer");
             field.setAccessible(true);
             ByteArrayOutputStream bos = (ByteArrayOutputStream) field.get(os);
             return new ByteArrayInputStream(bos.toByteArray());
         } catch (Exception e) {
             throw new RuntimeException("Whoops, did you change the MockRunner version? "
                     + "If so, you might want to change this method too");
         }
     }
             
             
     /**
      * Executes an ows request using the POST method.
      * 
      * @param path
      *            The porition of the request after the context ( no query
      *            string ), example: 'wms'.
      * 
      * @param body
      *            the body of the request
      * @param contentType
      *            the mimetype to set for the request
      * 
      * @return An input stream which is the result of the request.
      * 
      * @throws Exception
      */
     protected InputStream post(String path, String body, String contentType) throws Exception{
         MockHttpServletRequest request = createRequest(path);
         request.setMethod("POST");
         request.setContentType(contentType);
         request.setBodyContent(body);
 
         MockHttpServletResponse response = dispatch(request);
         return new ByteArrayInputStream(response.getOutputStreamContent().getBytes());
     }
     
     /**
      * Executes an ows request using the GET method and returns the result as an 
      * xml document.
      * 
      * @param path The portion of the request after the context, 
      *      example: 'wms?request=GetMap&version=1.1.1&..."
      * @param the list of validation errors encountered during document parsing (validation
      *        will be activated only if this list is non null)
      * 
      * @return A result of the request parsed into a dom.
      * 
      * @throws Exception
      */
     protected Document getAsDOM(final String path)
             throws Exception {
         return dom(get(path));
     }
 
     
     /**
      * Executes an ows request using the POST method with key value pairs 
      * form encoded, returning the result as a dom.
      *
      * @param path The porition of the request after hte context, 
      *      example: 'wms?request=GetMap&version=1.1.1&..."
      * @param the list of validation errors encountered during document parsing (validation
      *        will be activated only if this list is non null)     
      * 
      * @return An input stream which is the result of the request.
      * 
      * @throws Exception
      */
     protected Document postAsDOM( String path ) throws Exception {
         return postAsDOM(path, (List<Exception>) null);
     }
     
     /**
      * Executes an ows request using the POST method with key value pairs 
      * form encoded, returning the result as a dom.
      *
      * @param path The porition of the request after hte context, 
      *      example: 'wms?request=GetMap&version=1.1.1&..."
      * 
      * @return An input stream which is the result of the request.
      * 
      * @throws Exception
      */
     protected Document postAsDOM( String path, List<Exception> validationErrors ) throws Exception {
         return dom( post( path ));
     }
     
     /**
      * Executes an ows request using the POST method and returns the result as an
      * xml document.
      * <p>
      * 
      * </p>
      * @param path The porition of the request after the context ( no query string ), 
      *      example: 'wms'. 
      * 
      * @return An input stream which is the result of the request.
      * 
      * @throws Exception
      */
     protected Document postAsDOM( String path, String xml ) throws Exception {
             return postAsDOM(path, xml, null);
     }
     
     /**
      * Executes an ows request using the POST method and returns the result as an
      * xml document.
      * <p>
      * 
      * </p>
      * @param path The porition of the request after the context ( no query string ), 
      *      example: 'wms'. 
      * 
      * @return An input stream which is the result of the request.
      * 
      * @throws Exception
      */
     protected Document postAsDOM( String path, String xml, List<Exception> validationErrors ) throws Exception {
             return dom( post( path, xml ));
     }
     
     protected String getAsString(String path) throws Exception {
         return string(get(path));
     }
     
     /**
      * Parses a stream into a dom.
      * @throws IOException 
      * @throws SAXException 
      */
     protected Document dom(InputStream is) throws ParserConfigurationException, SAXException, IOException {
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         factory.setNamespaceAware(true);
         DocumentBuilder builder = factory.newDocumentBuilder();
         return builder.parse(is);
     }
             
     protected void checkValidationErorrs(Document dom, String schemaLocation) throws SAXException, IOException {
         final SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
         Schema schema = factory.newSchema(new File(schemaLocation));
         checkValidationErrors(dom, schema);
     }
 
     /**
      * Given a dom and a schema, checks that the dom validates against the schema 
      * of the validation errors instead
      * @param validationErrors
      * @throws IOException 
      * @throws SAXException 
      */
     protected void checkValidationErrors(Document dom, Schema schema) throws SAXException, IOException {
         final Validator validator = schema.newValidator();
         final List<Exception> validationErrors = new ArrayList<Exception>();
         validator.setErrorHandler(new ErrorHandler() {
             
             public void warning(SAXParseException exception) throws SAXException {
                 System.out.println(exception.getMessage());
             }
 
             public void fatalError(SAXParseException exception) throws SAXException {
                 validationErrors.add(exception);
             }
 
             public void error(SAXParseException exception) throws SAXException {
                 validationErrors.add(exception);
             }
 
           });
         validator.validate(new DOMSource(dom));
         if (validationErrors != null && validationErrors.size() > 0) {
             StringBuilder sb = new StringBuilder();
             for (Exception ve : validationErrors) {
                 sb.append(ve.getMessage()).append("\n");
             }
             fail(sb.toString());
         }
     }
         
     
     /**
      * Parses a stream into a String
      */
     protected String string(InputStream input) throws Exception {
         BufferedReader reader = null;
         StringBuffer sb = new StringBuffer();
         char[] buf = new char[8192];
         try {
             reader = new BufferedReader(new InputStreamReader(input));
             String line = null;
             while((line = reader.readLine()) != null)
                 sb.append(line);
         } finally {
             if(reader != null)
                 reader.close();
         }
         return sb.toString();
     }
     
     /**
      * Utility method to print out a dom.
      */
     protected void print( Document dom ) throws Exception {
         TransformerFactory txFactory = TransformerFactory.newInstance();
         try {
             txFactory.setAttribute("{http://xml.apache.org/xalan}indent-number", new Integer(2));
         } catch(Exception e) {
             // some 
         }
         
         Transformer tx = txFactory.newTransformer();
         tx.setOutputProperty(OutputKeys.METHOD,"xml");
         tx.setOutputProperty( OutputKeys.INDENT, "yes" );
           
         tx.transform( new DOMSource( dom ), new StreamResult(new OutputStreamWriter(System.out, "utf-8") ));
     }
 
     /**
      * Convenience method for element.getElementsByTagName() to return the 
      * first element in the resulting node list.
      */
     protected Element getFirstElementByTagName( Element element, String name ) {
         NodeList elements = element.getElementsByTagName(name);
         if ( elements.getLength() > 0 ) {
             return (Element) elements.item(0);
         }
         
         return null;
     }
     
     /**
      * Convenience method for element.getElementsByTagName() to return the 
      * first element in the resulting node list.
      */
     protected Element getFirstElementByTagName( Document dom, String name ) {
         return getFirstElementByTagName( dom.getDocumentElement(), name );
     }
     
     /*
      * Helper method to create the kvp params from the query string.
      */
     private void kvp(MockHttpServletRequest request, String path) {
         int index = path.indexOf('?');
 
         if (index == -1) {
             return;
         }
 
         String queryString = path.substring(index + 1);
         StringTokenizer st = new StringTokenizer(queryString, "&");
 
         while (st.hasMoreTokens()) {
             String token = st.nextToken();
             String[] keyValuePair = token.split("=");
             
             //check for any special characters
             if ( keyValuePair.length > 1 ) {
                 //replace any equals or & characters
                 keyValuePair[1] = keyValuePair[1].replaceAll( "%3D", "=" );
                 keyValuePair[1] = keyValuePair[1].replaceAll( "%3d", "=" );
                 keyValuePair[1] = keyValuePair[1].replaceAll( "%23", "&" );
             }
             request.setupAddParameter(keyValuePair[0], keyValuePair.length > 1 ?  keyValuePair[1]: "");
         }
     }
 
     private MockHttpServletResponse dispatch( HttpServletRequest request ) throws Exception {
         MockHttpServletResponse response = new MockHttpServletResponse() {
             public void setCharacterEncoding( String encoding ) {
                     
             }
         };
 
         dispatch(request, response);
         return response;
     } 
  
     private void dispatch(HttpServletRequest request, HttpServletResponse response) throws Exception{
         //create an instance of the spring dispatcher
         ServletContext context = applicationContext.getServletContext();
         
         MockServletConfig config = new MockServletConfig();
         config.setServletContext(context);
         config.setServletName("dispatcher");
         
         DispatcherServlet dispatcher = new DispatcherServlet();
         
         dispatcher.setContextConfigLocation(GeoServerAbstractTestSupport.class.getResource("dispatcher-servlet.xml").toString());
         dispatcher.init(config);
         
         
         //look up the handler
 //        Dispatcher dispatcher = 
 //                (Dispatcher) applicationContext.getBean( "dispatcher" ); 
         //dispatcher.setApplicationContext( getGeoServer().getApplicationContext() );
         
         //excute the pre handler step
         Collection interceptors = 
             GeoServerExtensions.extensions(HandlerInterceptor.class, applicationContext );
         for ( Iterator i = interceptors.iterator(); i.hasNext(); ) {
             HandlerInterceptor interceptor = (HandlerInterceptor) i.next();
             interceptor.preHandle( request, response, dispatcher );
         }
         
         //execute 
         //dispatcher.handleRequest( request, response );
         dispatcher.service(request, response);
         
         //execute the post handler step
         for ( Iterator i = interceptors.iterator(); i.hasNext(); ) {
             HandlerInterceptor interceptor = (HandlerInterceptor) i.next();
             interceptor.postHandle( request, response, dispatcher, null );
         }
     }
 
     /**
      * Assert that a GET request to a path will have a particular status code for the response.
      * @param code the number of the HTTP status code that is expected
      * @param path the path to which a GET request should be made, without the protocol, server and servlet context.
      * For example, to make a request to "http://localhost:8080/geoserver/ows" the path would be "ows"
      *
      * @throws Exception on test failure
      */
     protected void assertStatusCodeForGet(int code, String path) throws Exception{
         assertStatusCodeForRequest(code, "GET", path, "", "");
     }
 
     /**
      * Assert that a POST request to a path will have a particular status code for the response.
      * @param code the number of the HTTP status code that is expected
      * @param path the path to which a POST request should be made, without the protocol, server and servlet context.
      * For example, to make a request to "http://localhost:8080/geoserver/ows" the path would be "ows"
      * @param body the body to send with the request. May be empty, but must not be null.
      * @param type the mimetype to report for the body
      *
      * @throws Exception on test failure
      */
     protected void assertStatusCodeForPost(int code, String path, String body, String type) throws Exception {
         assertStatusCodeForRequest(code, "POST", path, body, type);
     }
 
     /**
      * Assert that a PUT request to a path will have a particular status code for the response.
      * @param code the number of the HTTP status code that is expected
      * @param path the path to which a PUT request should be made, without the protocol, server and servlet context.
      * For example, to make a request to "http://localhost:8080/geoserver/ows" the path would be "ows"
      * @param body the body to send with the request. May be empty, but must not be null.
      * @param type the mimetype to report for the body
      *
      * @throws Exception on test failure
      */
     protected void assertStatusCodeForPut(int code, String path, String body, String type) throws Exception {
         assertStatusCodeForRequest(code, "PUT", path, body, type);
     }
 
     /**
      * Assert that an HTTP request will have a particular status code for the response.
      * @param code the number of the HTTP status code that is expected
      * @param method the HTTP method for the request (eg, GET, PUT)
      * @param path the path for the request, excluding the protocol, server, port, and servlet context.
      * For example, to make a request to "http://localhost:8080/geoserver/ows" the path would be "ows"
      * @param body the body for the request.  May be empty, but must not be null.
      * @param type the mimetype for the request.
      */
     protected void assertStatusCodeForRequest(int code, String method, String path, String body, String type) throws Exception {
         MockHttpServletRequest request = createRequest(path);
         request.setMethod(method);
         request.setBodyContent(body);
         request.setContentType(type);
 
        CodeExpectingHttpServletResponse response = new CodeExpectingHttpServletResponse(new MockHttpServletResponse());
         dispatch(request, response);
        assertEquals(code, response.getError());
     }
 
     /**
      * HttpServletResponse wrapper to help in making assertions about expected status codes.
      */
     private class CodeExpectingHttpServletResponse extends HttpServletResponseWrapper{
        private int myErrorCode;
 
        protected CodeExpectingHttpServletResponse (HttpServletResponse req){
             super(req);
            myErrorCode = 200;
         }
         
         public void setStatus(int sc){
            myErrorCode = sc;
             super.setStatus(sc);
         }
 
         public void setStatus(int sc, String sm){
            myErrorCode = sc;
             super.setStatus(sc, sm);
         }
 
         public void sendError(int sc) throws IOException {
            myErrorCode = sc;
             super.sendError(sc);
         }
 
         public void sendError(int sc, String sm) throws IOException {
            myErrorCode = sc;
             super.sendError(sc, sm);
         }

        public int getError(){
            return myErrorCode;
        }
     }
 }
