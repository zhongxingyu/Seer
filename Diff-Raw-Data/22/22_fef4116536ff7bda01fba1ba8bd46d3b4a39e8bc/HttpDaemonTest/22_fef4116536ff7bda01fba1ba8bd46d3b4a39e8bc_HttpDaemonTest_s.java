 /*
  * Copyright (C) 2008 Laurent Caillette
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation, either
  * version 3 of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package novelang.daemon;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.StringWriter;
 import java.net.URL;
 import java.nio.charset.Charset;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpMethod;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.ClassUtils;
 import org.junit.After;
 import org.junit.Assert;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.NameAwareTestClassRunner;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import novelang.ScratchDirectoryFixture;
 import novelang.TestResourceTools;
 import novelang.TestResources;
 import novelang.configuration.ConfigurationTools;
 import novelang.produce.RequestTools;
 import novelang.rendering.RenditionMimeType;
 import novelang.system.DefaultCharset;
 
 /**
  * End-to-end tests with {@link HttpDaemon} and the download of some generated documents.
  *
  * @author Laurent Caillette
  */
 @RunWith( value = NameAwareTestClassRunner.class )
 public class HttpDaemonTest {
 
   @Test
   public void nlpOk() throws Exception {
     setUp( "nlpOk", ConfigurationTools.BUNDLED_STYLE_DIR, ISO_8859_1 ) ;
     final String generated = readAsString(
         new URL( "http://localhost:" + HTTP_DAEMON_PORT + GOOD_NLP_RESOURCE_NAME ) ) ;
     final String shaved = shaveComments( generated ) ;
     save( "generated.nlp", generated ) ;
     Assert.assertEquals( goodNlpSource, shaved ) ;
 
   }
 
   @Test
   public void pdfOk() throws Exception {
     setUp( "pdfOk" ) ;
     final byte[] generated = readAsBytes(
         new URL( "http://localhost:" + HTTP_DAEMON_PORT + GOOD_PDF_DOCUMENT_NAME ) ) ;
     save( "generated.pdf", generated ) ;
     assertTrue( generated.length > 100 ) ;
   }
 
   @Test
   public void fontListingMakesNoSmoke() throws Exception {
     setUp( "fontListingMakesNoSmoke" ) ;
     final byte[] generated = readAsBytes(
         new URL( "http://localhost:" + HTTP_DAEMON_PORT + FontDiscoveryHandler.DOCUMENT_NAME ) ) ;
     save( "generated.pdf", generated ) ;
     assertTrue( generated.length > 100 ) ;
   }
 
   @Test
   public void htmlOk() throws Exception {
     setUp( "htmlOk" ) ;
     final byte[] generated = readAsBytes(
         new URL( "http://localhost:" + HTTP_DAEMON_PORT + GOOD_HTML_DOCUMENT_NAME ) ) ;
     save( "generated.html", generated ) ;
     assertTrue( generated.length > 100 ) ;
   }
 
   @Test
   public void htmlBrokenCausesRedirection() throws Exception {
     setUp( "htmlBrokenCausesRedirection" ) ;
 
     final HttpMethod method = followRedirection(
         "http://localhost:" + HTTP_DAEMON_PORT + BROKEN_HTML_DOCUMENT_NAME ) ;
     final String responseBody = method.getResponseBodyAsString() ;
 
     assertTrue( responseBody.contains( "Requested:" ) ) ;
 
     assertTrue(
         "Expected link to requested page",
         responseBody.contains( BROKEN_HTML_DOCUMENT_NAME )
     ) ;
 
     assertTrue(
         "Expected path '" + BROKEN_PATH_AFTER_REDIRECTION + "'",
         method.getPath().contains( BROKEN_PATH_AFTER_REDIRECTION )
     ) ;
   }
 
   @Test
   public void htmlNotBrokenCausesRedirection() throws Exception {
     setUp( "htmlNotBroken" ) ;
 
     final HttpMethod method = followRedirection(
         "http://localhost:" + HTTP_DAEMON_PORT + GOOD_HTML_DOCUMENT_NAME +
         RequestTools.ERRORPAGE_SUFFIX
     ) ;
 
     String responseBody = method.getResponseBodyAsString() ;
     assertFalse( responseBody.contains( "Requested:" ) ) ;
 
   }
 
   @Test
   public void listDirectoryContentNoTrailingSolidus() throws Exception {
     setUp( "listDirectoryContent" ) ;
 
     final HttpMethod method = followRedirection( "http://localhost:" + HTTP_DAEMON_PORT ) ;
 
     checkDirectoryListing( method );
 
   }
 
   @Test
   public void listDirectoryContentWithTrailingSolidus() throws Exception {
     setUp( "listDirectoryContent" ) ;
 
     final HttpMethod method = followRedirection( "http://localhost:" + HTTP_DAEMON_PORT + "/" ) ;
 
     checkDirectoryListing( method );
 
   }
 
   private void checkDirectoryListing( HttpMethod method ) throws IOException {
     String responseBody = method.getResponseBodyAsString() ;
     assertTrue( responseBody.contains( "<a href=\"served/\">served/</a>" ) ) ;
     assertTrue( responseBody.contains( "<a href=\"served/good.html\">served/good.html</a>" ) ) ;
   }
 
   @Test
   public void listDirectoryContentWithSafari() throws Exception {
     setUp( "listDirectoryContent" ) ;
 
     final HttpMethod method = followRedirection(
         "http://localhost:" + HTTP_DAEMON_PORT + "/",
         SAFARI_USER_AGENT
     ) ;
 
     Assert.assertTrue( method.getPath().endsWith( "/" + DirectoryScanHandler.MIME_HINT ) ) ;
 
     checkDirectoryListing( method );
 
   }
 
   @Test
   public void testAlternateStylesheetInQueryParameter() throws Exception {
     setUp( "alternateStylesheetInQuery", SERVED_DIRECTORYNAME, DefaultCharset.RENDERING ) ;
     final byte[] generated = readAsBytes( new URL(
         "http://localhost:" + HTTP_DAEMON_PORT + BOOK_ALTERNATESTYLESHEET_DOCUMENT_NAME
     ) ) ;
 
     save( "generated.html", generated ) ;
     assertTrue( new String( generated ).contains( "this is the void stylesheet" ) ) ;
 
   }
 
   @Test
   public void testAlternateStylesheetInBook() throws Exception {
     setUp( "alternateStylesheetInBook", SERVED_DIRECTORYNAME, DefaultCharset.RENDERING ) ;
     final byte[] generated = readAsBytes( new URL(
         "http://localhost:" + HTTP_DAEMON_PORT +
             GOOD_HTML_DOCUMENT_NAME + ALTERNATE_STYLESHEET_QUERY
     ) ) ;
 
     save( "generated.html", generated ) ;
     assertTrue( new String( generated ).contains( "this is the void stylesheet" ) ) ;
   }
 
   
 // =======
 // Fixture
 // =======
 
   private static final Logger LOGGER = LoggerFactory.getLogger( HttpDaemonTest.class ) ;
   private static final Charset ISO_8859_1 = Charset.forName( "ISO_8859_1" );
 
 
   private static final int TIMEOUT = 5000 ;
 
   public static final String VOID_STYLESHEET = "void.xsl" ;
   private static final String ALTERNATE_STYLESHEET_QUERY =
       "?" + RequestTools.ALTERNATE_STYLESHEET_PARAMETER_NAME + "=" + VOID_STYLESHEET ;
 
 
   @Before
   public void before() {
     LOGGER.info( "Test name doesn't work inside IDEA-7.0.3: {}",
         NameAwareTestClassRunner.getTestName() ) ;
   }
 
   private static final Pattern STRIP_COMMENTS_PATTERN = Pattern.compile( "%.*\\n" ) ;
 
   private static String shaveComments( String s ) {
     final Matcher matcher = STRIP_COMMENTS_PATTERN.matcher( s ) ;
     final StringBuffer buffer = new StringBuffer() ;
     while( matcher.find() ) {
       matcher.appendReplacement( buffer, "" ) ;
     }
     matcher.appendTail( buffer ) ;
     return buffer.toString() ;
   }
 
   private static String readAsString( URL url ) throws IOException {
     final StringWriter stringWriter = new StringWriter() ;
     IOUtils.copy( url.openStream(), stringWriter ) ;
     return stringWriter.toString() ;
   }
 
   private static byte[] readAsBytes( URL url ) throws IOException {
     final ByteArrayOutputStream outputStream = new ByteArrayOutputStream() ;
     IOUtils.copy( url.openStream(), outputStream ) ;
     return outputStream.toByteArray() ;
   }
 
   private void save( String name, String document ) throws IOException {
     final File file = new File( contentDirectory, name ) ;
     FileUtils.writeStringToFile( file, document ) ;
     LOGGER.info( "Wrote file '{}'", file.getAbsolutePath() ) ;
   }
 
   private void save( String name, byte[] document ) throws IOException {
     final File file = new File( contentDirectory, name ) ;
     FileUtils.writeByteArrayToFile( new File( contentDirectory, name ), document ) ;
     LOGGER.info( "Wrote file '{}'", file.getAbsolutePath() ) ;
   }
 
   private static final int HTTP_DAEMON_PORT = 8081 ;
 
   private static final String SERVED_DIRECTORYNAME = TestResources.SERVED_DIRECTORY_NAME ;
 
   private static final String GOOD_NLP_RESOURCE_NAME = TestResources.SERVED_PARTSOURCE_GOOD;
 
   private static final String PDF = "." + RenditionMimeType.PDF.getFileExtension() ;
   private static final String HTML = "." + RenditionMimeType.HTML.getFileExtension() ;
 
   private static final String GOOD_PDF_DOCUMENT_NAME =
       TestResources.SERVED_PART_GOOD_NOEXTENSION + PDF ;
 
   private static final String GOOD_HTML_DOCUMENT_NAME =
       TestResources.SERVED_PART_GOOD_NOEXTENSION + HTML ;
 
   private static final String BROKEN_HTML_DOCUMENT_NAME =
       TestResources.SERVED_PART_BROKEN_NOEXTENSION + HTML ;
 
   private static final String BROKEN_PATH_AFTER_REDIRECTION =
       BROKEN_HTML_DOCUMENT_NAME + RequestTools.ERRORPAGE_SUFFIX ;
 
   private static final String BOOK_ALTERNATESTYLESHEET_DOCUMENT_NAME =
       TestResources.SERVED_BOOK_WITHALTERNATESTYLESHEET_NOEXTENSION + HTML ;
 
   private HttpDaemon httpDaemon ;
   private File contentDirectory;
   private String goodNlpSource;
 
   private void setUp( String testHint ) throws Exception {
     setUp( testHint, ConfigurationTools.BUNDLED_STYLE_DIR, DefaultCharset.RENDERING ) ;
   }
 
 
   /**
    * We don't use standard {@code Before} annotation because crappy JUnit 4 doesn't
    * let us know about test name so we have to pass it explicitely for creating
    * different directories (avoiding one erasing the other).
    * @link http://twgeeknight.googlecode.com/svn/trunk/JUnit4Playground/src/org/junit/runners/NameAwareTestClassRunner.java
    *     doesn't work with IDEA-7.0.3 (while it works with Ant-1.7.0 alone).
    */
   private void setUp(
       String testHint,
       String styleDirectoryName,
       Charset renderingCharset
   ) throws Exception {
 
     final String testName =
         ClassUtils.getShortClassName( getClass() + "-" + testHint ) ;
     final ScratchDirectoryFixture scratchDirectoryFixture =
         new ScratchDirectoryFixture( testName ) ;
     contentDirectory = scratchDirectoryFixture.getTestScratchDirectory() ;
 
     goodNlpSource = TestResourceTools.readStringResource(
         getClass(), GOOD_NLP_RESOURCE_NAME, DefaultCharset.SOURCE ) ;
 
     TestResources.copyServedResources( contentDirectory ) ;
 
     httpDaemon = new HttpDaemon( TestResources.createDaemonConfiguration( 
         HTTP_DAEMON_PORT, 
         contentDirectory, 
         styleDirectoryName,
         renderingCharset
     ) ) ;
     httpDaemon.start() ;
   }
 
   @After
   public void tearDown() throws Exception {
     httpDaemon.stop() ;
   }
 
 
   private static final String CAMINO_USER_AGENT =
       "Mozilla/5.0 (Macintosh; U; Intel Mac OS X; en; rv:1.8.1.14) " +
       "Gecko/20080512 Camino/1.6.1 (like Firefox/2.0.0.14)"
   ;
   
   private static final String SAFARI_USER_AGENT =
       "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_4_11; en) " +
       "AppleWebKit/525.18 (KHTML, like Gecko) " +
       "Version/3.1.2 Safari/525.22"
   ;
 
   private static final String DEFAULT_USER_AGENT = CAMINO_USER_AGENT ;
 
   private HttpMethod followRedirection( String originalUrlAsString ) throws IOException {
     return followRedirection( originalUrlAsString, DEFAULT_USER_AGENT ) ;
   }
 
   private HttpMethod followRedirection(
       String originalUrlAsString,
       String userAgent
   ) throws IOException {
     final HttpClient httpClient = new HttpClient() ;
     httpClient.getHttpConnectionManager().getParams().setConnectionTimeout( TIMEOUT ) ;
     final HttpMethod method = new GetMethod( originalUrlAsString ) ;
     method.setRequestHeader( "User-Agent", userAgent ) ;
     method.setFollowRedirects( true ) ;
     httpClient.executeMethod( method ) ;
 
     final String responseBody = method.getResponseBodyAsString() ;
     save( "generated.html", responseBody ) ;
 
     return method;
   }
 
 
 }
