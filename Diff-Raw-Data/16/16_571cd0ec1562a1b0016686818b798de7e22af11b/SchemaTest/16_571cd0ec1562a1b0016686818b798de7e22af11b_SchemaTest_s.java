 package org.neurus.data;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertNotSame;
 import static junit.framework.Assert.assertTrue;
 import static junit.framework.Assert.fail;
 import static org.neurus.testing.MoreAsserts.assertStringContains;
 
 import java.util.Arrays;
 
 import org.junit.Test;
 
 public class SchemaTest {
 
   @Test
   public void testSchemaReturnsAttributes() {
     Schema schema = new Schema.Builder()
         .addNumericAttribute("numerical1")
         .addNumericAttribute("numerical2")
         .build();
     assertEquals(2, schema.getAttributeCount());
     assertEquals("numerical1", schema.getAttribute(0).getName());
     assertEquals("numerical2", schema.getAttribute(1).getName());
   }
 
   @Test
   public void testBuilderAddNumericAttribute() {
     Schema schema = new Schema.Builder()
         .addNumericAttribute("aName")
         .build();
     NumericAttribute att = (NumericAttribute) schema.getAttribute(0);
     assertEquals("aName", att.getName());
   }
 
   @Test
   public void testBuilderAddTextAttribute() {
     Schema schema = new Schema.Builder()
         .addTextAttribute("textAttribute")
         .build();
     TextAttribute att = (TextAttribute) schema.getAttribute(0);
     assertEquals("textAttribute", att.getName());
   }
 
   @Test
   public void testBuilderAddNominalAttribute() {
     Schema schema = new Schema.Builder()
         .addNominalAttribute("aName", new String[] { "a", "b" })
         .build();
     NominalAttribute att = (NominalAttribute) schema.getAttribute(0);
     assertEquals("aName", att.getName());
     assertTrue(Arrays.equals(new String[] { "a", "b" }, att.getLabels()));
   }
 
   @Test
   public void testAddingAttributesWithSameNameFails() {
     try {
       new Schema.Builder()
           .addNumericAttribute("sameName")
           .addNominalAttribute("sameName", new String[] { "label" });
       fail();
     } catch (IllegalArgumentException expectedException) {
       assertStringContains("Duplicated attribute",
           expectedException.getMessage());
       assertStringContains("sameName", expectedException.getMessage());
     }
   }
 
   @Test
   public void testAddingNoAttributesFails() {
     try {
       new Schema.Builder().build();
       fail();
     } catch (IllegalStateException expectedException) {
       assertStringContains("at least one attribute",
           expectedException.getMessage());
     }
   }
 
   @Test
   public void testIndexOfAttribute() {
     Schema schema = new Schema.Builder().addNumericAttribute("att_0").addNumericAttribute("att_1")
         .addNumericAttribute("att_2").build();
     assertEquals(0, schema.indexOfAttribute("att_0"));
     assertEquals(1, schema.indexOfAttribute("att_1"));
     assertEquals(2, schema.indexOfAttribute("att_2"));
   }
 
   public void testCopy() {
     Schema original = new Schema.Builder()
         .addNumericAttribute("numerical1")
         .addNumericAttribute("numerical2")
         .build();
     Schema copy = original.copy();
     assertEquals(2, copy.getAttributeCount());
     assertEquals("numerical1", copy.getAttribute(0).getName());
     assertEquals("numerical2", copy.getAttribute(1).getName());
     assertNotSame(copy.getAttribute(0), original.getAttribute(0));
   }
 }
