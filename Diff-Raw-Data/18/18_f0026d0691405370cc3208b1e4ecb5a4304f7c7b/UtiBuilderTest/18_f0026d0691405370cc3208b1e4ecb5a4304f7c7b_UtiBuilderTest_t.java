 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.psexton.libuti;
 
 import org.junit.Ignore;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author PSexton
  */
 public class UtiBuilderTest {
     
     /**
      * Test of fromFile method, of class UtiBuilder.
      */
     @Ignore
     @Test
     public void testFromFile() {
         fail("The test case is a prototype.");
     }
 
     /**
      * Test of fromSuffix method, of class UtiBuilder.
      */
     @Test
     public void testFromSuffixMatches() {
         String fileSuffix = "docx";
         UtiBuilder instance = new UtiBuilder();
         Uti expResult = new Uti("com.microsoft.word.docx");
         Uti result = instance.fromSuffix(fileSuffix);
         assertEquals(expResult, result);
     }
     
     @Test
     public void testFromSuffixDoesNotMatch() {
         String fileSuffix = "dog";
         UtiBuilder instance = new UtiBuilder();
         Uti expResult = new Uti("dyn." + fileSuffix);
         Uti result = instance.fromSuffix(fileSuffix);
         assertEquals(expResult, result);
     }
 
     /**
      * Test of fromString method, of class UtiBuilder.
      */
     @Test
     public void testFromStringMatches() {
         String utiName = "com.microsoft.word.docx";
         UtiBuilder instance = new UtiBuilder();
         Uti expResult = new Uti(utiName);
         Uti result = instance.fromString(utiName);
         assertEquals(expResult, result);
     }
     
     @Test
     public void testFromStringDoesNotMatch() {
         String utiName = "this.is.not.a.uti";
         UtiBuilder instance = new UtiBuilder();
         Uti expResult = null;
         Uti result = instance.fromString(utiName);
         assertEquals(expResult, result);
     }
    
    @Test
    public void testFromSuffixUppercase() {
        String fileSuffix = "DOCX";
        UtiBuilder instance = new UtiBuilder();
        Uti expResult = new Uti("com.microsoft.word.docx");
        Uti result = instance.fromSuffix(fileSuffix);
        assertEquals(expResult, result);
    }
    
 }
