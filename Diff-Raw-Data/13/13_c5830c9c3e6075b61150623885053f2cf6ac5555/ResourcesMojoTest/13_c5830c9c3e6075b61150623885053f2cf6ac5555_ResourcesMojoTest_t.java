 package org.charless.qxmaven.mojo.qooxdoo;
 
 import java.io.File;
 
 import org.apache.maven.plugin.Mojo;
 import org.apache.maven.plugin.testing.AbstractMojoTestCase;
 import org.qooxdoo.charless.build.config.Config;
 import org.qooxdoo.charless.build.config.Manifest;
 
 public class ResourcesMojoTest extends AbstractMojoTestCase
 {
     public void testConfig()
         throws Exception
     {
         File testPom = new File( getBasedir(), "src/test/resources/resources.pom" );
         ResourcesMojo mojo = (ResourcesMojo) lookupMojo( "resources", testPom );
         assertNotNull( "Failed to configure the plugin", mojo );
 
         
         mojo.execute();
         
         // Check if resources have been copied
         assertTrue(mojo.getApplicationTarget().exists());
         assertTrue(mojo.getConfigTarget().exists());
         assertTrue(new File(mojo.getApplicationTarget(),"Manifest.json").exists());
         // Test the filtering for config.json
         Config cfg = Config.read(mojo.getConfigTarget());
         assertEquals("resources_app",cfg.getApplication());
        assertEquals("..",cfg.getRoot());
        assertEquals("../cache",cfg.getCache());
        assertEquals("../../qooxdoo-sdk",cfg.getQooxdooPath());
         // Test the filtering for Manifest.json
         Manifest m = Manifest.read(new File(mojo.getApplicationTarget(),"Manifest.json"));
         assertEquals("resources_app",m.getNamespace());
         assertEquals("UTF-8",m.getEncoding());
        assertEquals("../../../../src/main/qooxdoo",m.getKlass());
        assertEquals("../../../../src/test/resources",m.getResource());
        assertEquals("../../../../src/test/resources/translation",m.getTranslation());
         assertEquals("[1.5]",m.info("qooxdoo-versions").toString());
     }
 }
 
