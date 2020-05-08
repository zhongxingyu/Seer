 package com.mattbertolini.jxr.ant;
 
 import junit.framework.TestSuite;
 import org.apache.ant.antunit.junit3.AntUnitSuite;
 import org.apache.ant.antunit.junit4.AntUnitSuiteRunner;
 import org.junit.runner.RunWith;
 
 import java.io.File;
 import java.net.URISyntaxException;
 import java.net.URL;
 
 /**
  * @author Matt Bertolini
  */
 @RunWith(AntUnitSuiteRunner.class)
 public class JxrTaskTest {
     public static TestSuite suite() throws URISyntaxException {
         URL resource = JxrTaskTest.class.getResource("/com/mattbertolini/jxr/ant/JxrTaskTest.xml");
         File file = new File(resource.toURI());
         return new AntUnitSuite(file, JxrTaskTest.class);
     }
 }
