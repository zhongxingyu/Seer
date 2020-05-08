 /*
  * Launcher.java
  *
  * Created on April 19, 2004, 1:59 PM
  */
 
 package goDiet.Utils;
 
 import goDiet.Model.*;
 
 import java.io.*;
 import java.util.*;
 import java.text.SimpleDateFormat;
 
 /**
  *
  * @author  hdail
  */
 public class Launcher {
     
     /** Creates a new instance of Launcher */
     public Launcher() {
     }
     
     public String createLocalScratch(String scratchBase,RunConfig runCfg) {
         if(runCfg.debugLevel >= 1){
            System.out.println("Preparing local scratch directory in " + scratchBase);
         }
         String dirName = null;
         String runLabel = null;
         
         SimpleDateFormat formatter = new SimpleDateFormat("yyMMMdd_HHmm");
         java.util.Date today = new Date();
         String dateString = formatter.format(today);
          
         runLabel = "run_" + dateString;
         
         File dirHdl = new File(scratchBase,runLabel);
         if( runCfg.useUniqueDirs && dirHdl.exists() ) {
             int i = 0;
             do {
                 i++;
                 dirHdl = new File(scratchBase,runLabel + "_r" + i);
             } while (dirHdl.exists());
             runLabel += "_r" + i;
         }
         if(runCfg.useUniqueDirs){
             dirHdl.mkdirs();
         }
         return runLabel;
     }
     
     /* launchElement is the primary method for launching components of the DIET
      * hierarchy.  This method performs the following actions:
      *      - check that element, compRes, & scratch base are non-null
      *      - create the config file locally
      *      - stage the config file to remote host
      *      - run the element on the remote host
      */
     public void launchElement(Elements element,
                               String localScratchBase,
                               String runLabel,
                               boolean useLogService,
                               RunConfig runConfig){
         if(element == null){
             System.err.println("Launcher.launchElement called with null element.\n" +
                 "Launch request ignored.");
             return;
         }
         if(element.getComputeResource() == null){
             System.err.println("Launcher.launchElement called with null resource.\n" +
                 "Launch request ignored.");
             return;
         }
         if(localScratchBase == null){
             System.err.println("launchElement: Scratch space is not ready.  Need to run createLocalScratch.");
             return;
         }
         if(runLabel == null){
             System.err.println("launchElement: RunLabel undefined.\n");
             return;
         }
         if(runConfig.debugLevel >= 1){
             System.out.println("\n** Launching element " + element.getName() +
                 " on " + element.getComputeResource().getName());
         } 
         try {
             if(runConfig.useUniqueDirs){
                 createCfgFile(element,localScratchBase + "/" + runLabel,
                     useLogService,runConfig);
             } else {
                 createCfgFile(element,localScratchBase,
                     useLogService,runConfig);
             }
         }
         catch (IOException x) {
             System.err.println("Exception writing cfg file for " + element.getName());
             System.err.println("Exception: " + x);
             System.err.println("Exiting");
             System.exit(1);
         }
         StorageResource storeRes = element.getComputeResource().getStorageResource();
         stageFile(localScratchBase,runLabel,element.getCfgFileName(),
             storeRes,runConfig);
         runElement(element,runConfig);
     }
     
     // TODO: incorporate Elagi usage
     private void stageFile(String localBase, String runLabel, 
                            String filename,StorageResource storeRes,
                            RunConfig runConfig) {
         if(runConfig.debugLevel >= 1){
            System.out.println("Staging file " + filename + " to " + storeRes.getName());
         }
         //AccessMethod access = storeRes.getAccessMethod("scp");
         
         SshUtils sshUtil = new SshUtils();
         sshUtil.stageWithScp(localBase,runLabel,filename,storeRes,runConfig);
     }
     
     // TODO: incorporate Elagi usage
     private void runElement(Elements element, RunConfig runConfig) {
         ComputeResource compRes = element.getComputeResource();
         StorageResource storage = compRes.getStorageResource();
         if(runConfig.debugLevel >= 1){
            System.out.println("Executing element " + element.getName() + 
                 " on resource " + compRes.getName());
         }
         AccessMethod access = compRes.getAccessMethod("ssh");
         if(access == null){
             System.err.println("runElement: compRes does not have ssh access " +
                 "method. Ignoring launch request");
             return;
         }
         
         SshUtils sshUtil = new SshUtils();
         sshUtil.runWithSsh(element,runConfig);
     }
     
     public void stopElement(Elements element,
                              RunConfig runConfig){
        if(runConfig.debugLevel >= 1){                          
           System.out.println("Trying to stop element " + element.getName());
        }
        SshUtils sshUtil = new SshUtils();
        sshUtil.stopWithSsh(element,runConfig);
     }
     
     private void createCfgFile(Elements element,
                                String localScratch,
                                boolean useLogService,
                                RunConfig runConfig) throws IOException {
         if( element.getName().compareTo("TestTool") == 0){
             return;
         }
                 
         if(element instanceof goDiet.Model.OmniNames){
             element.setCfgFileName("omniORB4.cfg");
         } else {
             element.setCfgFileName(element.getName() + ".cfg");
         }
  
         File cfgFile = new File(localScratch, element.getCfgFileName());
         
         if(runConfig.debugLevel >= 1){
             System.out.println("Writing config file " + element.getCfgFileName());
         }
         
         try {
             cfgFile.createNewFile();
             FileWriter out = new FileWriter(cfgFile);      
             
             if( element.getName().compareTo("LogCentral") ==  0){
                 writeCfgFileLogCentral(element,out);
             } else if(element instanceof goDiet.Model.OmniNames){
                 writeCfgFileOmniNames((OmniNames)element,out);
             } else {
                 writeCfgFileDiet(element,out,useLogService);
             }
             out.close();
         }
         catch (IOException x) {
             System.err.println("Failed to write " + cfgFile.getPath());
             throw x;
         }
     }  
     
     private void writeCfgFileLogCentral(Elements element,FileWriter out) throws IOException {
         out.write("[General]\n\n");
         out.write("[DynamicTagList]\n");
         out.write("[StaticTagList]\n");
         out.write("[UniqueTagList]\n");
         out.write("[VolatileTagList]\n");
     }
     
     private void writeCfgFileOmniNames(OmniNames omni,FileWriter out) throws IOException {        
         out.write("InitRef = NameService=corbaname::" + 
             omni.getContact() + ":" + omni.getPort() + "\n"); 
         out.write("giopMaxMsgSize = 33554432\n");
         out.write("supportBootstrapAgent = 1\n");        
     }
     
     private void writeCfgFileDiet(Elements element,FileWriter out,
             boolean useLogService) throws IOException {
         ComputeResource compRes = element.getComputeResource();
         if(element instanceof goDiet.Model.MasterAgent) {
             out.write("name = " + element.getName() + "\n");
             out.write("agentType = DIET_MASTER_AGENT\n");
         } else if( element instanceof goDiet.Model.LocalAgent) {
             out.write("name = " + element.getName() + "\n");
             out.write("agentType = DIET_LOCAL_AGENT\n");
             LocalAgent agent = (LocalAgent)element;
             out.write("parentName = " + (agent.getParent()).getName() + "\n");
         } else if(element instanceof goDiet.Model.ServerDaemon){
             ServerDaemon sed = (ServerDaemon)element;
             out.write("parentName = " + (sed.getParent()).getName() + "\n");
         }
 
         if(element.isTraceLevelSet()) {
             out.write("traceLevel = " + element.getTraceLevel() + "\n");
         }
        int port = compRes.allocateEndPointPort();
        // port will be -1 if we don't need to use port, or if all ports
        // have been allocated (in which case we try without specifying port)
        if(port > 0){
            out.write("dietPort = " + port + "\n");
        }
         if(compRes.getEndPointContact() != null){
            out.write("dietHostname = " + compRes.getEndPointContact() +
                 "\n");
         }
         // TODO: properly handle port range here for firewalls
         out.write("fastUse = 0\n"); // TODO: support config in xml
         out.write("ldapUse = 0\n"); // TODO: support config in xml
         out.write("nwsUse = 0\n");  // TODO: support config in xml
         if(useLogService){ 
             out.write("useLogService = 1\n");
         } else {
             out.write("useLogService = 0\n");
         }
         out.write("lsOutbuffersize = 0\n");
         out.write("lsFlushinterval = 10000\n");        
     }
 }
