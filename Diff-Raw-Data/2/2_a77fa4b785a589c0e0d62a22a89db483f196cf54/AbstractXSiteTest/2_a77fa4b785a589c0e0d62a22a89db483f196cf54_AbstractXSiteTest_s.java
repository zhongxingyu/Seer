 package org.codehaus.xsite;
 
 import java.io.File;
 
 import org.junit.Before;
 
 /**
  * @author Mauro Talevi
  */
 public abstract class AbstractXSiteTest {
 
     protected  String testSrcDir;
 
     @Before
     public void setTestSrcDir() throws Exception {
         testSrcDir =  System.getProperty("test.src.dir");
         if ( testSrcDir == null ){
            testSrcDir = "xsite-core/src/test/site"; 
         } else if ( !testSrcDir.endsWith(File.separator) ){
             testSrcDir = testSrcDir + File.separator; 
         }        
     }
     
 }
