 package se.tla.mavenversionbumper;
 
 import org.junit.Assert;
 import org.junit.Test;
 import se.tla.mavenversionbumper.vcs.VersionControl;
 
 import java.io.File;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 /**
  * Unit tests for the Module class.
  */
 public class ModuleTest {
 
     @Test
     public void testSimple() throws Exception {
         Module subject = new Module("target/test-classes/sources", "simple", null);
 
         Assert.assertEquals("se.tla.maven", subject.groupId());
         Assert.assertEquals("versionbumper", subject.artifactId());
         Assert.assertEquals("1.0-SNAPSHOT", subject.version());
         Assert.assertEquals("se.tla.maven:versionbumper:1.0-SNAPSHOT", subject.gav());
     }
 
     @Test
     public void testParentVersion() throws Exception {
         Module subject = new Module("target/test-classes", "withparent", null);
 
         Assert.assertEquals("0.1-SNAPSHOT", subject.parentVersion());
     }
 
     @Test
     public void testItemsFromParent() throws Exception {
         Module subject = new Module("target/test-classes", "withparent", null);
 
         Assert.assertEquals("se.tla.maven", subject.groupId());
         Assert.assertEquals("0.1-SNAPSHOT", subject.version());
     }
 
     @Test
     public void testParentVersionWithNoParent() throws Exception {
         Module subject = new Module("target/test-classes", "simple", null);
 
         Assert.assertNull(subject.parentVersion());
 
         try {
             subject.parentVersion("1.0");
             Assert.fail();
         } catch (IllegalArgumentException e) {
             // Expected
         }
 
         try {
             Module parent = new Module("target/test-classes", "simple", null);
             subject.parentVersion(parent);
             Assert.fail();
         } catch (IllegalArgumentException e) {
             // Expected
         }
     }
 
     @Test
     public void testParentMissingVersion() throws Exception {
         Module subject = new Module("target/test-classes/withparent", "missingversion", null);
 
         try {
             subject.parentVersion("1.0");
             Assert.fail();
         } catch (IllegalStateException e) {
             // Expected
         }
     }
 
     @Test
     public void testSetParentVersion() throws Exception {
         // TODO
     }
 
     @Test
     public void testSimpleSave() throws Exception {
         ModuleTestTemplate.template("simple", "simple.xml", new ModuleTinker() {
             @Override
             public void tink(Module subject) {
                 subject.groupId("se.tla");
                 subject.artifactId("kaffekokare");
                 subject.version("1.0");
             }
         });
     }
 
     @Test
     public void testUpdateDependency() throws Exception {
         // TODO
     }
 
     @Test
     public void testUpdatePluginDependency() throws Exception {
         // TODO
     }
 
     @Test
     public void testUpdateProperty() throws Exception {
        // TODO
     }
 
     @Test
     public void testLabel() throws Exception {
         final String LABELMESSAGE = "labelmsg";
         final AtomicBoolean labelWasCalled = new AtomicBoolean(false);
         Module subject = new Module("target/test-classes", "simple", new VersionControl() {
             @Override
             public void prepareSave(File file) {
             }
 
             @Override
             public void commit(File file, String message) {
             }
 
             @Override
             public void label(String label, File... targets) {
                 Assert.assertEquals(LABELMESSAGE, label);
                 labelWasCalled.set(true);
             }
         });
 
         subject.label(LABELMESSAGE);
         subject.save();
 
         Assert.assertTrue(labelWasCalled.get());
     }
 
     @Test
     public void testCommitDefaultMessage() throws Exception {
         final AtomicBoolean commitWasCalled = new AtomicBoolean(false);
         Module subject = new Module("target/test-classes", "simple", new VersionControl() {
             @Override
             public void prepareSave(File file) {
             }
 
             @Override
             public void commit(File file, String message) {
                 Assert.assertTrue(message.contains("Bump"));
                 Assert.assertTrue(message.contains("1.0-SNAPSHOT"));
                 Assert.assertTrue(message.contains("99"));
                 commitWasCalled.set(true);
             }
 
             @Override
             public void label(String label, File... targets) {
             }
         });
 
         subject.version("99");
         subject.save();
 
         Assert.assertTrue(commitWasCalled.get());
     }
 
     @Test
     public void testCommitCustomMessage() throws Exception {
         final String COMMITMESSAGE = "commitmsg";
         final AtomicBoolean commitWasCalled = new AtomicBoolean(false);
         Module subject = new Module("target/test-classes", "simple", new VersionControl() {
             @Override
             public void prepareSave(File file) {
             }
 
             @Override
             public void commit(File file, String message) {
                 Assert.assertEquals(COMMITMESSAGE, message);
                 commitWasCalled.set(true);
             }
 
             @Override
             public void label(String label, File... targets) {
             }
         });
 
         subject.commitMessage(COMMITMESSAGE);
         subject.save();
 
         Assert.assertTrue(commitWasCalled.get());
     }
 
     @Test
     public void testLabelOnlyPomXml() throws Exception {
         // TODO
     }
 }
