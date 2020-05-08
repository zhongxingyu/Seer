 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package in.uglyhunk.jvm.mon;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map.Entry;
 
 /**
  *
  * @author uglyhunk
  */
 public class CSV2JS {
     
     public static void convert(String csvFilename) throws Exception {
         CSV2JS.csvFilename = csvFilename;
         mapProcsToMetrics();
         createJS();
        System.out.println("CSV to JS conversion done");
     }
     
     private static void mapProcsToMetrics() throws Exception {
         BufferedReader br = new BufferedReader(new FileReader(jvmonLogDir + File.separator + CSV2JS.csvFilename));
         br.readLine(); // ignore first line
         while(true){
             String line = br.readLine();
             if(line == null){
                 break;
             }
             String lineItems[] = line.split(",");
             String procId = lineItems[1];
             
             if(procsToMetricsMap.containsKey(procId)){
                 procsToMetricsMap.get(procId).append(";").append(line);
             } else {
                 procsToMetricsMap.put(procId, new StringBuilder().append(line));
             }
         }
         br.close();
     }
   
     private static void createJS() throws Exception {
         String procfunList = createProcListFun();
         String memfuncs = createMemFuns();
         String classfuncs = createClassFuns();
         String thrdFuns = createThrdFuns();
         String compFuns = createCompilationFuns();
         String gcFuns = createGCFuns();
         String memPoolFuns = createMemPoolFuns();
                 
         String jsFileName = CSV2JS.csvFilename.replace(".csv", ".js");
         String jsFilePath = documentRoot + File.separator + "data" + File.separator + jsFileName;
         String indexFilePath = documentRoot + File.separator + "data" + File.separator + "index.js";
         
         BufferedWriter bw = new BufferedWriter(new FileWriter(jsFilePath));
         bw.write(procfunList);
         bw.write(memfuncs);
         bw.write(classfuncs);
         bw.write(thrdFuns);
         bw.write(compFuns);
         bw.write(gcFuns);
         bw.write(memPoolFuns);   
         bw.flush();
         bw.close();
         
         // update index.js file
         updateJSIndex(jsFileName, indexFilePath);
     }
     
     private static String createProcListFun(){
         
         StringBuilder func = new StringBuilder();
         func.append("function _getProcList() {\nreturn \"\" + \n");
         func.append("\"");
         Iterator<Entry<String, StringBuilder>> itrProcsToMetricsMap = procsToMetricsMap.entrySet().iterator();
         while(true){
             Entry<String, StringBuilder> itr = itrProcsToMetricsMap.next();
             func.append("_").append(itr.getKey());
             if(itrProcsToMetricsMap.hasNext()) 
                 func.append(",");
             else
                 break;
         }
         func.append("\";\n}\n\n");
         return func.toString();
     }
     
     private static String createMemFuns(){
         StringBuilder func = new StringBuilder();
         Iterator<Entry<String, StringBuilder>> itrProcsToMetricsMap = procsToMetricsMap.entrySet().iterator();
         while(true){
             Entry<String, StringBuilder> itr = itrProcsToMetricsMap.next();
             String procId = itr.getKey();
             String procIdMetrics[] = procsToMetricsMap.get(procId).toString().split(";");
                      
             func.append("function _").append(procId).append("Mem() {\nreturn \"\" + \n");
             func.append("\"timestamp,used_heap(MB),comm_heap(MB),used_non-heap(MB),comm_non-heap(MB)\\n\" + \n");
             for(int line = 0; line < procIdMetrics.length; line++){
                 String metrics[] = procIdMetrics[line].split(",");
                 func.append("\"").append(metrics[0]).append(",").append(metrics[2]).append(",").append(metrics[3]).append(",").
                                   append(metrics[4]).append(",").append(metrics[5]).append("\\n\"");
                 
                 if(line == procIdMetrics.length-1){
                     func.append(";\n}\n\n");
                 } else {
                     func.append(" + \n");
                 }
             }
             if(!itrProcsToMetricsMap.hasNext()) 
                 break;
         }
         return func.toString();
     }
     
      private static String createClassFuns(){
         StringBuilder func = new StringBuilder();
         Iterator<Entry<String, StringBuilder>> itrProcsToMetricsMap = procsToMetricsMap.entrySet().iterator();
         while(true){
             Entry<String, StringBuilder> itr = itrProcsToMetricsMap.next();
             String procId = itr.getKey();
             String procIdMetrics[] = procsToMetricsMap.get(procId).toString().split(";");
                      
             func.append("function _").append(procId).append("Class() {\nreturn \"\" + \n");
             func.append("\"timestamp,curr_loaded_classes,tot_loaded_classes,tot_unloaded_classes\\n\" + \n");
             for(int line = 0; line < procIdMetrics.length; line++){
                 String metrics[] = procIdMetrics[line].split(",");
                 func.append("\"").append(metrics[0]).append(",").append(metrics[6]).append(",").
                                   append(metrics[7]).append(",").append(metrics[8]).append("\\n\"");
                 
                 if(line == procIdMetrics.length-1){
                     func.append(";\n}\n\n");
                 } else {
                     func.append(" + \n");
                 }
             }
             if(!itrProcsToMetricsMap.hasNext()) 
                 break;
         }
         return func.toString();
     }
      
      private static String createThrdFuns(){
         StringBuilder func = new StringBuilder();
         Iterator<Entry<String, StringBuilder>> itrProcsToMetricsMap = procsToMetricsMap.entrySet().iterator();
         while(true){
             Entry<String, StringBuilder> itr = itrProcsToMetricsMap.next();
             String procId = itr.getKey();
             String procIdMetrics[] = procsToMetricsMap.get(procId).toString().split(";");
                      
             func.append("function _").append(procId).append("Thrd() {\nreturn \"\" + \n");
             func.append("\"timestamp,daemon_thrd_count,peak_thrd_count,current_thrd_count,total_started_thrd_count\\n\" + \n");
             for(int line = 0; line < procIdMetrics.length; line++){
                 String metrics[] = procIdMetrics[line].split(",");
                 func.append("\"").append(metrics[0]).append(",").append(metrics[9]).append(",").append(metrics[10]).append(",").
                                   append(metrics[11]).append(",").append(metrics[12]).append("\\n\"");
                 
                 if(line == procIdMetrics.length-1){
                     func.append(";\n}\n\n");
                 } else {
                     func.append(" + \n");
                 }
             }
             
             if(!itrProcsToMetricsMap.hasNext()) 
                 break;
         }
         return func.toString();
     } 
      
      
     private static String createCompilationFuns(){
         StringBuilder func = new StringBuilder();
         Iterator<Entry<String, StringBuilder>> itrProcsToMetricsMap = procsToMetricsMap.entrySet().iterator();
         while(true){
             Entry<String, StringBuilder> itr = itrProcsToMetricsMap.next();
             String procId = itr.getKey();
             String procIdMetrics[] = procsToMetricsMap.get(procId).toString().split(";");
                      
             func.append("function _").append(procId).append("Comp() {\nreturn \"\" + \n");
             func.append("\"timestamp,tot_compilation_time(ms)\\n\" + \n");
             for(int line = 0; line < procIdMetrics.length; line++){
                 String metrics[] = procIdMetrics[line].split(",");
                 func.append("\"").append(metrics[0]).append(",").append(metrics[13]).append("\\n\"");
                 
                 if(line == procIdMetrics.length-1){
                     func.append(";\n}\n\n");
                 } else {
                     func.append(" + \n");
                 }
             }
             
             if(!itrProcsToMetricsMap.hasNext()) 
                 break;
         }
         return func.toString();
     }  
     
     private static String createGCFuns(){
         StringBuilder func = new StringBuilder();
         Iterator<Entry<String, StringBuilder>> itrProcsToMetricsMap = procsToMetricsMap.entrySet().iterator();
         while(true){
             Entry<String, StringBuilder> itr = itrProcsToMetricsMap.next();
             String procId = itr.getKey();
             String procIdMetrics[] = procsToMetricsMap.get(procId).toString().split(";"); // split all lines of the proc_id
                      
             func.append("function _").append(procId).append("GC() {\nreturn \"\" + \n");
             for(int line = 0; line < procIdMetrics.length; line++){ // iterate all the lines for the select proc_id
                 String metrics[] = procIdMetrics[line].split(", "); //  only gc metrics group will be selected
                 String gcMetrics[] = metrics[1].split(",");
                 int i=0;
                 if(line == 0){
                     func.append("\"timestamp,");
                     while(i < gcMetrics.length){
                         if(i > 0 ){
                             func.append(",");
                         }
                         func.append(gcMetrics[i]).append("-collection_count,");
                         func.append(gcMetrics[i]).append("-collection_time(ms)");
                         i+=3;
                     }
                     func.append("\\n\" + \n");     
                 }
                 String timestamp = metrics[0].split(",")[0];
                 func.append("\"").append(timestamp).append(",");
                 
                 i = 0;
                 while(i < gcMetrics.length){
                     if(i > 0){
                         func.append(",");
                     }
                     func.append(gcMetrics[i+1]).append(",").append(gcMetrics[i+2]);
                     i+=3;   
                 }
                 func.append("\\n\"");
                 
                 if(line == procIdMetrics.length-1){
                     func.append(";\n}\n\n");
                 } else {
                     func.append(" + \n");
                 }
             }
             
             if(!itrProcsToMetricsMap.hasNext()) 
                 break;
         }
         return func.toString();
     }  
     
     private static String createMemPoolFuns(){
         StringBuilder func = new StringBuilder();
         Iterator<Entry<String, StringBuilder>> itrProcsToMetricsMap = procsToMetricsMap.entrySet().iterator();
         while(true){
             Entry<String, StringBuilder> itr = itrProcsToMetricsMap.next();
             String procId = itr.getKey();
             String procIdMetrics[] = procsToMetricsMap.get(procId).toString().split(";"); // split all lines of the proc_id
                      
             func.append("function _").append(procId).append("MemPool() {\nreturn \"\" + \n");
             for(int line = 0; line < procIdMetrics.length; line++){ // iterate all the lines for the select proc_id
                 String metrics[] = procIdMetrics[line].split(", "); //  only memPool metrics group will be selected
                 String memPoolMetrics[] = metrics[2].split(",");
                 
                 int i=0;
                 if(line == 0){
                     func.append("\"timestamp,");
                     while(i < memPoolMetrics.length){
                         if(i>0){
                             func.append(",");
                         }
                         func.append(memPoolMetrics[i]).append("--used_memory(MB),");
                         func.append(memPoolMetrics[i]).append("--comm_memory(MB)");
                         i+=3;
                     }
                     func.append("\\n\" + \n");     
                 }
                 String timestamp = metrics[0].split(",")[0];
                 func.append("\"").append(timestamp).append(",");
                 i = 0;
                 while(i < memPoolMetrics.length){
                     if(i > 0){
                         func.append(",");
                     }
                     func.append(memPoolMetrics[i+1]).append(",").append(memPoolMetrics[i+2]);
                     i+=3;   
                 }
                 func.append("\\n\"");
                 
                 if(line == procIdMetrics.length-1){
                     func.append(";\n}\n\n");
                 } else {
                     func.append(" + \n");
                 }
             }
             
             if(!itrProcsToMetricsMap.hasNext()) 
                 break;
         }
         return func.toString();
     }  
     
     private static void updateJSIndex(String jsReportFileName, String jsIndexFilePath) throws Exception {
         BufferedReader br = null;
         BufferedWriter bw = null;
         StringBuilder jsIndexContents = null;
         try {
             br = new BufferedReader(new FileReader(jsIndexFilePath));
             jsIndexContents = new StringBuilder();
             
             while(true){
                 String line = br.readLine();
                 if(line != null) {
                     if(line.contains("return")){
                         line = line.replace("return", "return \"" + jsReportFileName + ",\" + ");
                     }
                     jsIndexContents.append(line).append(newline);
                 } else {
                     break;
                 }
             }
             br.close();
                                             
             bw = new BufferedWriter(new FileWriter(jsIndexFilePath));
             bw.write(jsIndexContents.toString());
             bw.flush();
         
         } finally {
             if(br != null)
                 br.close();
             if(bw != null)
                 bw.close();
         }
     }
    
     private static String csvFilename;
    public static String newline = System.getProperty("line.separator");
     private static HashMap<String, StringBuilder> procsToMetricsMap = new HashMap<String, StringBuilder>();
     private static final String jvmonLogDir = Main.getJVMONLogDir();
     private static final String documentRoot = Main.getDocucmentRoot();
 }
 
