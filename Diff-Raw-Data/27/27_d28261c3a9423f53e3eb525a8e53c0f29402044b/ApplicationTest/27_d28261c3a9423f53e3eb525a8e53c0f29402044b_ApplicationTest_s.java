 /* vim: set ts=2 et sw=2 cindent fo=qroca: */
 
 package com.globant.katari.shindig.domain;
 
import static org.junit.Assert.*;
 import org.junit.Test;
 
 import java.io.File;
 
 public class ApplicationTest {
 
   private String gadgetXmlUrl = "file:///" + new File(
       "target/test-classes/SampleGadget.xml").getAbsolutePath();
 
   @Test
   public void testConstructor() {
     Application app = new Application(gadgetXmlUrl);
    assertTrue(app.getUrl().equals(gadgetXmlUrl));
    assertTrue(app.getTitle().equals("Test title"));
    assertTrue(app.getId() == 0);
   }
 
   @Test
   public void testConstructor_nullGadgetUrl() {
     try {
       new Application(null);
       fail("Should be an illegal argument exception because gadget url is null");
     } catch (IllegalArgumentException e) {
     }
   }
 
   @Test
   public void testConstructor_emptyGadgetUrl() {
     try {
       new Application("");
       fail("Should be an illegal argument exception because gadget url is empty");
     } catch (IllegalArgumentException e) {
     }
   }
 }
 
