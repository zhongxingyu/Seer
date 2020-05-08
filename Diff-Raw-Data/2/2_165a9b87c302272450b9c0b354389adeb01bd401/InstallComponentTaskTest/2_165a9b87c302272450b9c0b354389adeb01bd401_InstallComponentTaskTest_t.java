 /*
  * Created on Jul 13, 2005
  *
  * To change the template for this generated file go to
  * Window - Preferences - Java - Code Generation - Code and Comments
  */
 package org.servicemix.jbi.management.task;
 
 import org.servicemix.jbi.util.FileUtil;
 
 import java.io.File;
 import java.net.URL;
 
 /**
  *
  * InstallComponentTaskTest
  */
 public class InstallComponentTaskTest extends JbiTaskSupport {
     
     
     private InstallComponentTask installComponentTask;
     private File rootDir = new File("testWDIR");
     /*
      * @see TestCase#setUp()
      */
     protected void setUp() throws Exception {
         this.container.setRootDir(rootDir.getPath());
         super.setUp();        
         installComponentTask = new InstallComponentTask(){};
         installComponentTask.init();
     }
 
     /*
      * @see TestCase#tearDown()
      */
     protected void tearDown() throws Exception {
         installComponentTask.close();
         super.tearDown();
     }
     
     public void testInstallation() throws Exception {
         URL url = getClass().getClassLoader().getResource("org/servicemix/jbi/installation/testarchive.jar");
         if (url != null) {
             String file = url.getFile();
            installComponentTask.setFile(file);
             installComponentTask.init();
             installComponentTask.execute();
             File testFile = new File(rootDir, container.getName() + File.separator + "components" + File.separator
                     + "ComponentTest");
             assertTrue(testFile.exists());
             FileUtil.deleteFile(rootDir);
         }
     }
 }
