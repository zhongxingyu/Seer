 package ch.rotscher.mavenext;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.fail;
 
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashMap;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.maven.model.Model;
 import org.codehaus.plexus.logging.console.ConsoleLogger;
 import org.junit.Before;
 import org.junit.Test;
 
 public class GeneratedVersionModelReaderTest {
 
     @Before
     public void before() {
         System.clearProperty(VersionOverrideModelReader.MAVENEXT_CHECK_SNAPSHOT_DEP);
         System.clearProperty(VersionOverrideModelReader.MAVENEXT_CHECK_SNAPSHOT_DEP_FAIL_ON_ERROR);
         VersionOverrideModelReader.reset();
         
 		try {
 			FileOutputStream fos = new FileOutputStream(VersionOverrideModelReader.MAVENEXT_BUILDNUMBER_FILE);
 			IOUtils.write("0", fos);
 			IOUtils.closeQuietly(fos);
 		} catch (FileNotFoundException e) {
 			fail(e.getMessage());
 		} catch (IOException e) {
 			fail(e.getMessage());
 		}
     }
 
     @Test
     public void testWithNoPropertySet() {
     	System.clearProperty(VersionOverrideModelReader.MAVENEXT_RELEASE_VERSION);
         InputStream correctPomFile = GeneratedVersionModelReaderTest.class.getResourceAsStream("valid/single/pom.xml");
 
         VersionOverrideModelReader customVersionModelReader = new VersionOverrideModelReader();
         customVersionModelReader.setLogger(new ConsoleLogger());
         try {
             Model model = customVersionModelReader.read(correctPomFile, new HashMap<String, Object>());
             assertNotNull(model);
             assertEquals("0.0.1-SNAPSHOT", model.getVersion());
         } catch (IOException e) {
             fail(e.getMessage());
         }
     }
     
     @Test
     public void testWithEmptyPropertySet() {
     	System.setProperty(VersionOverrideModelReader.MAVENEXT_RELEASE_VERSION, "");
         InputStream correctPomFile = GeneratedVersionModelReaderTest.class.getResourceAsStream("valid/single/pom.xml");
 
         VersionOverrideModelReader customVersionModelReader = new VersionOverrideModelReader();
         customVersionModelReader.setLogger(new ConsoleLogger());
         try {
             Model model = customVersionModelReader.read(correctPomFile, new HashMap<String, Object>());
             assertNotNull(model);
            assertEquals("0.0.1-S-1", model.getVersion());
         } catch (IOException e) {
             fail(e.getMessage());
         }
     }
     
     @Test
     public void testWithPropertySet() {
     	System.setProperty(VersionOverrideModelReader.MAVENEXT_RELEASE_VERSION, "1.2.3");
         InputStream correctPomFile = GeneratedVersionModelReaderTest.class.getResourceAsStream("valid/single/pom.xml");
 
         VersionOverrideModelReader customVersionModelReader = new VersionOverrideModelReader();
         customVersionModelReader.setLogger(new ConsoleLogger());
         try {
             Model model = customVersionModelReader.read(correctPomFile, new HashMap<String, Object>());
             assertNotNull(model);
             assertEquals("1.2.3", model.getVersion());
         } catch (IOException e) {
             fail(e.getMessage());
         }
     }
 }
