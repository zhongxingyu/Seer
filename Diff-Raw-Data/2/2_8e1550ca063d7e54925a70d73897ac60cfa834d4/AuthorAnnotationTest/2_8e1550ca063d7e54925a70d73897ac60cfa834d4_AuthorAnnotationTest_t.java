 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package uk.ac.ebi.annotation.meta;
 
 import junit.framework.TestCase;
 import uk.ac.ebi.annotation.AuthorAnnotation;
 import uk.ac.ebi.interfaces.Annotation;
 
 
 /**
  *
  * @author johnmay
  */
 public class AuthorAnnotationTest extends TestCase {
 
     public AuthorAnnotationTest(String testName) {
         super(testName);
     }
 
 
     public void testToString() {
 
         System.out.println("testToString()");
 
         Annotation annotation = new AuthorAnnotation("Added during gap filling");
 
        String expected = "Added during gap filling";
 
         assertEquals(expected, annotation.toString());
 
     }
 
 
 }
 
