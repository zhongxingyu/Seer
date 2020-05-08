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
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.ClassUtils;
 import org.junit.After;
 import org.junit.Assert;
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
 import novelang.configuration.ContentConfiguration;
 import novelang.configuration.RenderingConfiguration;
 import novelang.configuration.ServerConfiguration;
 import novelang.loader.ClasspathResourceLoader;
 import novelang.loader.ResourceLoader;
 import novelang.parser.Encoding;
 
 /**
  * End-to-end tests with {@link HttpDaemon} and the download of some generated documents.
  *
  * @author Laurent Caillette
  */
 @RunWith( value = NameAwareTestClassRunner.class )
 public class HttpDaemonTest {
 
   private static final Logger LOGGER = LoggerFactory.getLogger( HttpDaemonTest.class ) ;
 
   @Test
   public void nlpOk() throws Exception {
     setUp( "nlpOk" ) ;
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
         new URL( "http://localhost:" + HTTP_DAEMON_PORT + GOOD_PDF_RESOURCE_NAME ) ) ;
     save( "generated.pdf", generated ) ;
     Assert.assertTrue( generated.length > 100 ) ;
 
   }
 
 // =======
 // Fixture
 // =======
 
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
 
   private static final String GOOD_NLP_RESOURCE_NAME = TestResources.SERVED_GOOD ;
 
   private static final String GOOD_PDF_RESOURCE_NAME =
       TestResources.SERVED_GOOD_NOEXTENSION + ".pdf" ;
 
 
   private HttpDaemon httpDaemon ;
   private File contentDirectory;
   private String goodNlpSource;
 
   /**
    * We don't use standard {@code Before} annotation because crappy JUnit 4 doesn't
    * let us know about test name so we have to pass it explicitely for creating
    * different directories (avoiding one erasing the other).
    * @link http://twgeeknight.googlecode.com/svn/trunk/JUnit4Playground/src/org/junit/runners/NameAwareTestClassRunner.java
   *     doesn't work with IDEA-7.0.3 (while it works with Ant-1.7.0).
    */
   private void setUp( String testHint ) throws Exception {
 
     final String testName =
         ClassUtils.getShortClassName( getClass() + "-" + testHint ) ;
     final ScratchDirectoryFixture scratchDirectoryFixture =
         new ScratchDirectoryFixture( testName ) ;
     contentDirectory = scratchDirectoryFixture.getTestScratchDirectory() ;
     TestResourceTools.copyResourceToFile(
         getClass(), GOOD_NLP_RESOURCE_NAME, contentDirectory ) ;
     goodNlpSource = TestResourceTools.readStringResource(
         getClass(), GOOD_NLP_RESOURCE_NAME, Encoding.DEFAULT ) ;
 
     httpDaemon = new HttpDaemon( HTTP_DAEMON_PORT, createServerConfiguration( contentDirectory ) ) ;
     httpDaemon.start() ;
   }
 
   private ServerConfiguration createServerConfiguration( final File contentDirectory ) {
     return new ServerConfiguration() {
 
       public RenderingConfiguration getRenderingConfiguration() {
         return new RenderingConfiguration() {
           public ResourceLoader getResourceLoader() {
             return new ClasspathResourceLoader( ConfigurationTools.BUNDLED_STYLE_DIR ) ;
           }
         } ;
       }
 
       public ContentConfiguration getContentConfiguration() {
         return new ContentConfiguration() {
           public File getContentRoot() {
             return contentDirectory;
           }
         } ;
       }
     } ;
   }
 
   @After
   public void tearDown() throws Exception {
     httpDaemon.stop() ;
   }
 
 }
