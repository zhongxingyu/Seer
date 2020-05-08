 import freemarker.template.Configuration;
 import util.GenerateUtil;
 import util.YamlUtil;
 
 import java.io.IOException;
 import java.util.HashMap;
 
 /**
  * Author: Vlaaad
  * Date: 07.11.12
  */
 public class Generator {
     private static String pathToYaml;
     private static String targetSourcePath;
     private static Configuration cfg;
     private static String templateDir;
 
     public static void main(String[] args) throws IOException {
         validateArgs(args);
         initGenerator();
         generate();
     }
 
     private static void initGenerator() throws IOException {
         GenerateUtil.init(templateDir);
     }
 
     private static void validateArgs(String[] args) {
         if (args.length != 3) {
             log("Required three arguments: relative path to yaml, as3 project source root and template directory");
             System.exit(-1);
         }
         pathToYaml = args[0];
         targetSourcePath = args[1];
         templateDir = args[2];
     }
 
     private static void generate() {
         HashMap<String, Object> yamlData = YamlUtil.load(pathToYaml);
         for (String signalKey : yamlData.keySet()) {
             createSignal(signalKey, yamlData.get(signalKey));
         }
     }
 
     private static void createSignal(String signalKey, Object params) {
         String pathFromClassName = createPathFromClassName(signalKey);
         String classNameWithoutPackage = getClassNameFromFullClassName(signalKey);
         HashMap<String, Object> root = new HashMap<String, Object>();
         root.put("params", params);
         root.put("name", classNameWithoutPackage);
         root.put("package", getPackage(signalKey));
        GenerateUtil.generate(targetSourcePath + pathFromClassName + classNameWithoutPackage + "Signal.as", "signal.as.ftl", root);
         GenerateUtil.generate(targetSourcePath + pathFromClassName + "I" + classNameWithoutPackage + "Handler.as", "handler.as.ftl", root);
     }
 
     private static String getPackage(String signalKey) {
         String[] arr = signalKey.split("\\.");
         String res = "";
         for (int i = 0; i < arr.length - 1; i++) {
             res += arr[i];
             if (i < arr.length - 2) {
                 res += ".";
             }
         }
         return res;
     }
 
     private static String getClassNameFromFullClassName(String signalKey) {
         String[] arr = signalKey.split("\\.");
         String name = arr[arr.length - 1];
         return name.substring(0, 1).toUpperCase() + name.substring(1);
     }
 
     private static String createPathFromClassName(String signalKey) {
         String[] arr = signalKey.split("\\.");
         String res = "";
         for (int i = 0; i < arr.length - 1; i++) {
             res += arr[i] + "/";
         }
         return res;
     }
 
     private static void log(String message) {
         System.out.println(message);
     }
 }
