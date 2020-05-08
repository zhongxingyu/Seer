 /* vim: set ts=2 et sw=2 cindent fo=qroca: */
 
 package com.globant.katari.shindig.domain;
 
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.hamcrest.CoreMatchers.*;
 import org.junit.Test;
 
 import java.io.File;
 
 public class ApplicationTest {
 
   private String gadgetXmlUrl = "file:///" + new File(
       "target/test-classes/SampleGadget.xml").getAbsolutePath();
 
   @Test
   public void testConstructor() {
     Application app = new Application(gadgetXmlUrl);
    assertThat(app.getUrl(), is(gadgetXmlUrl));
    assertThat(app.getTitle(), is("Test title"));
    // getDescription returns an empty string instead of null because the xpath
    // parser implementation does not return null when the xpath element is not
    // found.
    assertThat(app.getDescription(), is(""));
    assertThat(app.getId(), is(0L));
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
 
