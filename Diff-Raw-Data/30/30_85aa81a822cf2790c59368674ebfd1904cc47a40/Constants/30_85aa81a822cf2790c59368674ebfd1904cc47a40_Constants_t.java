 package org.levi.engine;
 
 /**
  * @author Ishan Jayawardena
  */
 public class Constants {
     private final static String HOME = "HOME";
    public final static String BPMN_FILE_EXTENSION = ".bpmn";
     public final static String LAR_EXTENSION = ".lar";
     public final static String LOM_EXTENSION = ".lom";
     public final static String EMPTY = "__EMPTY__";
     public static final String SYSTEM_HOME = System.getenv().get(HOME);
     public static final String PWD = System.getenv().get("PWD");
     public static final String LEVI_ENGINE = "LeviEngine";
     public static final String LEVI_HOME = PWD.substring(0, PWD.lastIndexOf('/')) + "/webapps/levi/" + LEVI_ENGINE;
 
     public static final String LOM_PATH = LEVI_HOME + "/serial/";
     public static final String BPMN_PATH = "bpmn-runtime/src/main/java/org/levi/samples/data/";
     public static final String LAR_PATH = "bpmn-runtime/src/main/java/org/levi/samples/data/lars/";
    public static final String LAR_EXTRACT_PATH = LEVI_HOME + "/extract/";
     public static final String ENGINEDATA_PATH = LOM_PATH + "enginedata";
     public static final String ENGINE_CONFIG_PATH = EMPTY;
     public static final String LEVI_VERSION = "0.0.1";
 
     private Constants() {
         throw new AssertionError("Constants class is not intended to be instantiated");
     }
 }
