 package org.bonitasoft.connectors.sap;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.bonitasoft.connectors.sap.bos5connector.SAPAbstractConnector;
 import org.bonitasoft.connectors.sap.bos5connector.SAPConstants;
 
 public class SAPUtil {
 
     static {
         final URL librfc32URL = SAPUtil.class.getResource("librfc32.dll");
         final URL sapjcorfcURL = SAPUtil.class.getResource("sapjcorfc.dll");
         if (librfc32URL != null && sapjcorfcURL != null) {
             final File librfc32 = new File(librfc32URL.getFile());
             final File sapjcorfc = new File(sapjcorfcURL.getFile());
             final String key = "java.library.path";
             final String value = System.getProperty(key) + ":" + librfc32.getParent() + ":" + sapjcorfc.getParent();
             System.err.println("Setting " + key + " to " + value);
             System.setProperty(key, value);
         }
     }
     private SAPUtil() { }
 
     public static void addInputRowow(final List<List<Object>> inputParameters, final String parameterType, final String tableName, final String parameterName, final List<Object> parameterValue) {
         final List<Object> row = new ArrayList<Object>();
         row.add(parameterType);row.add(tableName);row.add(parameterName);row.add(parameterValue);
         inputParameters.add(row);
     }
 
     public static void addInputRowow(final List<List<Object>> inputParameters, final String parameterType, final String tableName, final String parameterName, final String parameterValue) {
         final List<Object> row = new ArrayList<Object>();
         row.add(parameterType);row.add(tableName);row.add(parameterName);row.add(parameterValue);
         inputParameters.add(row);
     }
 
     public static void addOutputRow(final List<List<String>> outputParameters, final String parameterType, final String tableName, final String xpath) {
         final List<String> row = new ArrayList<String>();
         row.add(parameterType);row.add(tableName);row.add(xpath);row.add("");
         outputParameters.add(row);
     }
 
     public static Map<String, Object> callFunction(final boolean commitOnSuccess, final boolean rollbackOnFailure, String htmlPath, final String functionName,
                                                    final List<List<Object>> inputParameters, final List<List<String>> outputParameters) throws Exception {
 
         System.err.println("Starting callFunction...");
 
         Map<String, Object> additionalParameters = new HashMap<String, Object>();
         additionalParameters.put(SapCallFunction.COMMIT_ON_SUCCESS, commitOnSuccess);
         additionalParameters.put(SapCallFunction.ROLLBACK_ON_FAILURE, rollbackOnFailure);
         additionalParameters.put(SapCallFunction.REPOSITORY, SAPConstants.DEFAULT_REPOSITORY_NAME);
         additionalParameters.put(SapCallFunction.FUNCTION_NAME, functionName);
         additionalParameters.put(SapCallFunction.HTML_OUTPUT, htmlPath);
         additionalParameters.put(SapCallFunction.INPUT_PARAMETERS, inputParameters);
         additionalParameters.put(SapCallFunction.OUTPUT_PARAMETERS, outputParameters);
 
 
         SapCallFunction connector = initConnector(additionalParameters);
 
         if (!htmlPath.isEmpty()) {
             System.err.println("*********************");
             System.err.println("PRINTING HTML CONTENT");
             System.err.println("*********************");
             System.err.println(readFile(new File(htmlPath)));
             System.err.println("\n\n");
             System.err.println("*********************");
             System.err.println("       END           ");
             System.err.println("*********************");
         }
         System.err.println("Ending callFunction...");
         return connector.execute();
     }
 
     private static String readFile(final File aFile) throws Exception {
         final StringBuilder contents = new StringBuilder();
         final BufferedReader input =  new BufferedReader(new FileReader(aFile));
         try {
             String line = null;
             while (( line = input.readLine()) != null){
                 contents.append(line);
                 contents.append(System.getProperty("line.separator"));
             }
         }
         finally {
             input.close();
         }
         return contents.toString();
     }
 
     private static SapCallFunction initConnector(Map<String, Object> additionalParameters) {
         SapCallFunction connector = new SapCallFunction();
         Map<String, Object> parameters = new HashMap<String, Object>();
         parameters.put(SapCallFunction.SERVER_TYPE, SAPAbstractConnector.SERVER_TYPE_APPLICATION_SERVER);
         parameters.put(SapCallFunction.CLIENT, "000");
         parameters.put(SapCallFunction.USER, "TALEND");
         parameters.put(SapCallFunction.PASSWORD, "FRANCE");
         parameters.put(SapCallFunction.LANGUAGE, "EN");
        parameters.put(SapCallFunction.HOST, "192.168.0.58");
         parameters.put(SapCallFunction.SYSTEM_NUMBER, "00");
         parameters.putAll(additionalParameters);
         connector.setInputParameters(parameters);
         return connector;
     }
 
 
 }
