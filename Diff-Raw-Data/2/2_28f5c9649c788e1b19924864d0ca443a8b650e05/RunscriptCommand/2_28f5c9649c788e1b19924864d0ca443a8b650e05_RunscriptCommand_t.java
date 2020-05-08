 package eggPlant;
 
 import hudson.model.AbstractBuild;
 import java.io.File;
 
 /** This class contains data and methods for constructing the runscript command
  *  that will be passed to Jenkins for execution.
  *
  * @author afisher
  * @version 1.0
  */
 public class RunscriptCommand {
 
     //runscript parameters
     private String runscript;
     private String scripts;
     private String host;
     private int port;
     private String password;
     private int colorDepth;
     private int repeatCount;
     private String globalResultsFolder;
     private String defaultDocumentDirectory;
     private boolean commandLineOutput;
     private boolean reportFailures;
     private String params;
     private AbstractBuild build;
      
     private String runscriptCommandString; //final constructed runscript command
    
     public RunscriptCommand(AbstractBuild b, 
                             String exeLoc, 
                             String scriptList){
         runscript = exeLoc;
         build = b;
         colorDepth = 0;
         repeatCount = 0;
         port = 0;
         scripts = "";
         params = "";
         
         String [] scriptArray = scriptList.split(",");
         
         for (int i = 0; i < scriptArray.length; i++){
                 scripts += " "+build.getWorkspace()
                         +File.separator
                         +scriptArray[i].trim();  
         }
         
         runscriptCommandString =  runscript+" "+scripts;
     }
     
     public void buildRunscriptCommandString(){
         if (host != null) runscriptCommandString += " -host " + host;
         if (port != 0) runscriptCommandString += " -port " + port;
         if (password != null) runscriptCommandString += " -password " + password;
        if (colorDepth != 0) runscriptCommandString += " -colorDepth " + colorDepth;
         if (repeatCount != 0) runscriptCommandString += " -repeat " + repeatCount;
         if (globalResultsFolder != null) runscriptCommandString += " -GlobalResultsFolder " + globalResultsFolder;
         if (defaultDocumentDirectory != null) runscriptCommandString += " -DefaultDocumentDirectory " + defaultDocumentDirectory;
         if (commandLineOutput == true) runscriptCommandString += " -CommandLineOutput YES ";
         if (reportFailures == true) runscriptCommandString += " -ReportFailures YES ";
         
         //params
         if (!params.isEmpty()) {
             runscriptCommandString += " -params "+params;
         } 
     }
     
     public String getRunScriptCommandString(){
         return runscriptCommandString;
     }
 
     public int getColorDepth() {
         return colorDepth;
     }
 
     public void setColorDepth(int colorDepth) {
         this.colorDepth = colorDepth;
     }
 
     public boolean isCommandLineOutput() {
         return commandLineOutput;
     }
 
     public void setCommandLineOutput(boolean commandLineOutput) {
         this.commandLineOutput = commandLineOutput;
     }
 
     public String getDefaultDocumentDirectory() {
         return defaultDocumentDirectory;
     }
 
     public void setDefaultDocumentDirectory(String defaultDocumentDirectory) {
         this.defaultDocumentDirectory = defaultDocumentDirectory;
     }
 
     public String getGlobalResultsFolder() {
         return globalResultsFolder;
     }
 
     public void setGlobalResultsFolder(String globalResultsFolder) {
         this.globalResultsFolder = globalResultsFolder;
     }
 
     public String getHost() {
         return host;
     }
 
     public void setHost(String host) {
         this.host = host;
     }
 
     public String getParams() {
         return params;
     }
 
     public void setParams(String prms) {
         String[] parameters = prms.split(",");
 
         for (int i = 0; i < parameters.length; i++) {
             params += parameters[i] + " ";
         }
     }
 
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     public int getPort() {
         return port;
     }
 
     public void setPort(int port) {
         this.port = port;
     }
 
     public int getRepeatCount() {
         return repeatCount;
     }
 
     public void setRepeatCount(int repeatCount) {
         this.repeatCount = repeatCount;
     }
 
     public boolean isReportFailures() {
         return reportFailures;
     }
 
     public void setReportFailures(boolean reportFailures) {
         this.reportFailures = reportFailures;
     }
 
     public String getRunscript() {
         return runscript;
     }
 
     public void setRunscript(String runscript) {
         this.runscript = runscript;
     }
 
     public String getRunscriptCommandString() {
         return runscriptCommandString;
     }
 
     public String getScripts() {
         return scripts;
     }
 
     public void setScripts(String scripts) {
         this.scripts = scripts;
     }
 }
