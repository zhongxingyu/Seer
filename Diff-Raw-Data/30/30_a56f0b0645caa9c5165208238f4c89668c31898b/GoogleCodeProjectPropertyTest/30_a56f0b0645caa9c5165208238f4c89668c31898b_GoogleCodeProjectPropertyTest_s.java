 package hudson.plugins.googlecode;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 public class GoogleCodeProjectPropertyTest {
     @Test
     public void testGoogleCodeProjectPropertyNotEndingWithSlash() {
         GoogleCodeProjectProperty property = new GoogleCodeProjectProperty("http://code.google.com/p/project");
         assertEquals("http://code.google.com/p/project/", property.googlecodeWebsite);
     }
 
     @Test
     public void testGoogleCodeProjectPropertyEndingWithSlash() {
         GoogleCodeProjectProperty property = new GoogleCodeProjectProperty("http://code.google.com/p/project/");
         assertEquals("http://code.google.com/p/project/", property.googlecodeWebsite);
     }
 
     @Test
     public void testGetProjectName() {
         GoogleCodeProjectProperty property = new GoogleCodeProjectProperty("http://code.google.com/p/project");
         assertEquals("project", property.getProjectName());
     }
 
     @Test
     public void testGetProjectNameWithDash() {
         GoogleCodeProjectProperty property = new GoogleCodeProjectProperty("http://code.google.com/p/py-stones");
         assertEquals("py-stones", property.getProjectName());
     }
 
     @Test
     public void testGetSubversionRootUrl() {
         GoogleCodeProjectProperty property = new GoogleCodeProjectProperty("http://code.google.com/p/project");
        assertEquals("http://project.googlecode.com/svn/", property.getSubversionRootUrl());
     }    
 }
