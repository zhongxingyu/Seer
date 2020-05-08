 package com.rogthefrog.music;
 
 import static org.junit.Assert.*;
 import static org.hamcrest.CoreMatchers.*;
 
 import org.junit.Test;
 
 public class ScaleSchemaTest {
 
   @Test
   public void testEnsureEvenSchema() {
     ScaleSchema schema = new ScaleSchema(0b101010101010);
     assertTrue(schema.hasZeroBasedSchema());
     schema.ensureEvenSchema();
     assertEquals(0b010101010101, schema.getSchema());
   }
   
   @Test
   public void testHasZeroBasedSchema() {
     ScaleSchema schema = new ScaleSchema(0b000000000000, "Zero schema");
     assertTrue(schema.hasZeroBasedSchema());
     schema.setSchema(0b000000000001);
     assertFalse(schema.hasZeroBasedSchema());
   }
   
   @Test
   public void testShiftUp() {
     ScaleSchema major = MajorScaleSchema.getInstance();
     int oldSchema = major.getSchema();
     String oldName = major.getName();
 
     // major scale shifted up 0 whole tones = no change
     major.shiftUp(0);
     assertEquals(oldSchema, major.getSchema());
 
     major.shiftUp(AbsInterval.UNISON);
     assertEquals(oldSchema, major.getSchema());
 
     // if we shift by an octave we should be back to 0
     major.shiftUp(ScaleSchema.SEMITONES_IN_OCTAVE);
     assertEquals(oldSchema, major.getSchema());
 
     // shift by an octave interval
     major.shiftUp(AbsInterval.OCTAVE);
     assertEquals(oldSchema, major.getSchema());
     
     // shift by non-zero, should not match
    /*
     major.shiftUp(1);
     assertThat(oldSchema, not(equalTo(major.getSchema())));
     
     major.shiftUp(AbsInterval.MAJ_3);
     assertThat(oldSchema, not(equalTo(major.getSchema())));
     
    // start from scratch, shift by a 6th, should be a minor scale
     ScaleSchema minor = MinorScaleSchema.getInstance();
     major.setSchema(oldSchema);
     major.setName(oldName);
    System.out.println(major);
    System.out.println(minor);
    major.shiftUp(9);
    System.out.println(major);
     assertEquals(minor.getSchema(), major.getSchema());
    */
   }
 
 }
