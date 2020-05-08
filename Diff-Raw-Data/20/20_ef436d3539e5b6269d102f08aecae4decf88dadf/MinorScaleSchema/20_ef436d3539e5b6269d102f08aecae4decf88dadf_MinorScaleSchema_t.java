 package com.rogthefrog.music;
 
 public final class MinorScaleSchema extends ScaleSchema {
 
   // remember schema starts from the right (root = 1)
  public static final int MINOR_SCHEMA  = 0b010110101101;
   public static final String NAME       = "Minor scale";
   private static MinorScaleSchema instance = null;
 
   private MinorScaleSchema() {
     super(MINOR_SCHEMA, NAME);
   }
 
   public static MinorScaleSchema getInstance() {
     if (instance == null) {
       instance = new MinorScaleSchema();
     }
     return instance;
   }
 }
