 package fi.helsinki.cs.koskelo.common;
 
 /**
  * Luokka Koskelon kyttmien vakioiden yhteniseksi sijaintipaikaksi
  */
 
 public final class TTK91Constant {
 
     // common.TTK91TaskOptions/compareMethod:
     public final static int COMPARE_TO_SIMULATED = 1; 
     public final static int COMPARE_TO_STATIC = 0;
     // analyser.TTK91RealAnalyser
     public final static int COMPARE_TO_SIMULATED_PUBLIC = 3;
     public final static int COMPARE_TO_SIMULATED_HIDDEN = 5;
     public final static int COMPARE_TO_STATIC_PUBLIC = 7;
     public final static int COMPARE_TO_STATIC_HIDDEN = 11;
 
     // maksimissaan ajettavien kskyjen mr; "ikuisen silmukan esto"
     public final static int MAX_COMMANDS = 10000; 
 
     // common.TTK91TaskCriteria
     public static final int INVALID = -1; // Alustamaton vertailu.
     public static final int LESS = 0; // <
     public static final int LESSEQ = 1; // <=
     public static final int GREATER = 2; // >
     public static final int GREATEREQ = 3; // >=
     public static final int EQUAL = 4; // =
     public static final int NOTEQUAL = 5; // !=
     public static final int NOTCOMPARABLE = 6; // Tulosteita varten joissa
                                                // ei ole loogista operaattoria
 
     // eAssarin virhekoodit
     public static final int FATAL_ERROR = 2;
    //    public static final int ERROR = 1; <-- tt ei taideta kytt missn, eik taida olla varsinaisesti edes mritelty
     public static final int NO_ERROR = 0;
 
 }
