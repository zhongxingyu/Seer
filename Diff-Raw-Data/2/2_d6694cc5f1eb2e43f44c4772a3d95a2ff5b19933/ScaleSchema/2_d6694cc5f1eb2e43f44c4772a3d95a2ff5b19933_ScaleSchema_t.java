 package com.rogthefrog.music;
 
 public class ScaleSchema {
   
   public static final int ROTATOR_AND_MASK    = 0b111111111111;
   
   protected int     schema;
   protected String  name;
 
   // ************
   // housekeeping
   // ************
   public ScaleSchema() {
     schema = 0;
   }
   
   public ScaleSchema(int schema) {
     setSchema(schema);
   }
   
   public ScaleSchema(int schema, String name) {
     setSchema(schema);
     setName(name);
   }
   
   public String getName() {
     return name;
   }
   
   protected void setName(String name) {
     this.name = name;
   }
   
   public int getSchema() {
     return schema;
   }
   
   protected void setSchema(int schema) {
     this.schema = schema;
   }
   
   public String toString() {
     String prettyName = zeroPadSchema(this.schema);
     if (this.name != "") {
       prettyName = this.name + " = " + prettyName;
     }
     return prettyName;
   }
 
   /**
    * checks whether a scale schema contains a given interval
    * e.g. a major scale contains a AbsInterval.MAJ_3 but not a AbsInterval.MIN_7
    * @param interval
    * @return true if that interval is found in the scale
    */
   public Boolean contains(AbsInterval interval) {
     int intervalBinaryValue = (int)Math.pow(2, interval.getInterval());
     int andResult           = (schema & intervalBinaryValue);
     
     return (andResult == intervalBinaryValue);
   }
 
   /**
    * zero-pads the binary representation of the scale
    */
   public static String zeroPadSchema(int schema) {
     return String.format("%" + Music.SEMITONES_IN_OCTAVE + "s", 
       Integer.toBinaryString(schema)).replace(" ", "0");
   }
 
   // ****************
   // end housekeeping
   // ****************
 
   /**
    * zero-based schemas can be bad (rootless scales)
    * @return true if a schema has no root
    */
   public Boolean hasNoRoot() {
     return schema % 2 == 0;
   }
   
   /**
    * flips a schema (e.g. from 10101 to 01010)
    * used when flipping zero-based, rootless schemas
    * @param schema any integer version of a schema
    * @return its flipped schema
    */
   public static int flipSchema(int schema) {
     int flipped = 0;
     int power   = 0;
     for (int i = 0; i < Music.SEMITONES_IN_OCTAVE; ++i) {
       power = (int)Math.pow(2,  i);
      if ((schema & power) != power) {
         flipped += power;
       }
     }
     return flipped;
   }
   /**
    * flips a schema, using a ScaleSchema object
    * @param scale a ScaleSchema object
    * @return the flipped schema
    */
   public static int flipSchema(ScaleSchema scale) {
     return flipSchema(scale.getSchema());
   }
 
   /**
    * a valid schema must have a root (1 as lowest bit)
    * if you shift a scale by an interval not in that scale
    * e.g. shift a major scale by a m6 (G#)
    * you wind up with a 0-based schema that needs flipping
    * This method flips the schema if necessary
    * @return
    */
   protected int ensureSchemaHasRoot() {
     if (hasNoRoot()) {
       setSchema(flipSchema(schema));
     }
     return schema;
   }
 
   /**
    * shift a scale schema by an arbitrary number of semitones
    * @param numSteps number of semitones to shift by
    */
   public ScaleSchema shiftUp(int numSemitones) {
     if (numSemitones < 0 || numSemitones > Music.SEMITONES_IN_OCTAVE) {
       return this;
     }
     // the rotator is a concat of the schema to itself
     // so we can take a slice
     // e.g. if the schema is 101 the rotator is 101101
     int rotator = schema + (schema << Music.SEMITONES_IN_OCTAVE);
 
     // now you push the rotator to the right by however many semitones
     int rotated = rotator >>> numSemitones;
 
     // and you grab the 12 rightmost digits
     this.setSchema(rotated & ROTATOR_AND_MASK);
     this.setName(this.name + " + " + numSemitones + " semitone(s)");
 
     // we like our schemas to have a root, so flip the schema if necessary
     this.ensureSchemaHasRoot();
 
     return this;
   }
 
   /**
    * 
    * @param interval AbsInterval object to shift by (e.g. "shift up a maj third")
    */
   public ScaleSchema shiftUp(AbsInterval interval) {
     String oldName = name;
     shiftUp(interval.getInterval());
     setName(oldName + " + 1 " + interval.longName());
 
     return this;
   }
 }
