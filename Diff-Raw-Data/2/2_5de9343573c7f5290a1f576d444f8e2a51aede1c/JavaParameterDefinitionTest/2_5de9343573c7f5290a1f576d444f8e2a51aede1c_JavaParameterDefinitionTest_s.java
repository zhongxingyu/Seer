 package com.datalex.jdkparameter;
 
 import hudson.tools.JDKInstaller;
 import org.junit.Test;
 import org.jvnet.hudson.test.JenkinsRule;
 
 import java.io.IOException;
 
 /**
  * Created with IntelliJ IDEA.
  * User: barisbatiege
  * Date: 6/21/13
  * Time: 2:11 PM
  * To change this template use File | Settings | File Templates.
  */
 public class JavaParameterDefinitionTest {
 
     @org.junit.Rule
     public JenkinsRule rule = new JenkinsRule();;
 
     @Test
     public void testGetList() throws IOException {
 
         JDKInstaller.JDKFamilyList jdks = JDKInstaller.JDKList.all().get(JDKInstaller.JDKList.class).toList();
         System.out.println(jdks.data.length);
 //        System.out.println("Dummy");
     }
 }
