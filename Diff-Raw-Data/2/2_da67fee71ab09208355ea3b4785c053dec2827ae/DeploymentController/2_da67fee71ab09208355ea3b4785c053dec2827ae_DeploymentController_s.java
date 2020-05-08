 /*
  * DietDeploymentController.java
  *
  * Created on 26 june 2004
  */
 
 package goDiet.Controller;
 
 import goDiet.Utils.*;
 import goDiet.Model.*;
 import goDiet.Events.*;
 import goDiet.Defaults;
 
 //import java.io.IOException;
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 
 /**
  *
  * @author  hdail
  */
 public class DeploymentController extends java.util.Observable
                                   implements Runnable,
                                              java.util.Observer {
     private ConsoleController consoleCtrl;
     private DietPlatformController modelCtrl;
     private LogCentralCommController logCommCtrl;
     
     private goDiet.Model.DietPlatform      dietPlatform;
     private goDiet.Model.ResourcePlatform  resourcePlatform;
     private goDiet.Utils.Launcher          launcher;
     private java.lang.Thread               dcThread;
     
     private java.util.Vector requestQueue;
     private int deployState;
     private Elements waitingOn = null;
 
     public DeploymentController(ConsoleController consoleController,
                                 DietPlatformController modelController){
         this.consoleCtrl    = consoleController;
         this.consoleCtrl.addObserver(this);
         this.modelCtrl      = modelController;
         this.logCommCtrl = new LogCentralCommController(this.consoleCtrl, 
                                                         this.modelCtrl);
         this.logCommCtrl.addObserver(this);
 
         dietPlatform        = modelCtrl.getDietPlatform();
         resourcePlatform    = modelCtrl.getResourcePlatform();
         launcher            = new goDiet.Utils.Launcher(consoleController);
         this.requestQueue   = new java.util.Vector();
         this.deployState    = goDiet.Defaults.DEPLOY_NONE;
         dcThread            = new java.lang.Thread(this);
         dcThread.start();
     }
     
     public void run() {
         consoleCtrl.printOutput("DeploymentController thread starting up.",2);
         String request;
         while(true) {
             try {
                 synchronized(this){
                     this.wait();
                 }
             } catch (InterruptedException x){
                 x.printStackTrace();
             }
             while( (request = deQueueRequest()) != null){
                 if(request.compareTo("launch all") == 0){
                     consoleCtrl.printOutput("got launch all request",2);
                     requestLaunch("all");
                 }
             }
         }
     }
     
     public void update(java.util.Observable observable, Object obj) {
         java.awt.AWTEvent e = (java.awt.AWTEvent)obj;
         String request;
         boolean msgAccept = false;
         if ( e instanceof LaunchRequest){
             request = ((LaunchRequest)e).getLaunchRequest();
             consoleCtrl.printOutput("Got launch request : " + request, 3);
             queueRequest("launch " + request);
             synchronized(this){
                 notifyAll();
             }
         } else if (e instanceof LogStateChange){
             int newState = ((LogStateChange)e).getNewState();
             String elementName = ((LogStateChange)e).getElementName();
             String elementType = ((LogStateChange)e).getElementType();
             if(this.waitingOn != null){
                 // for agents, sufficient to check name
                 // for seds, have to check type
                 if((elementName.compareTo(this.waitingOn.getName()) == 0) ||
                    (elementType.compareTo("SeD") == 0)){
                     consoleCtrl.printOutput("found log verify for stalled agent" +
                         this.waitingOn.getName(), 2);
                     msgAccept = true;
                     this.waitingOn.getLaunchInfo().setLogState(newState);
                     synchronized(this){
                         notifyAll();
                     }
                 } 
             } 
             if(msgAccept == false){
                 consoleCtrl.printError("Warning: received delayed launch" +
                     " verification by log for " + elementName, 1);
                 // TODO: find element and change log state
                 // [TODO: better ID handling for SeDs]
             }
         }
     }
     private void queueRequest(String request){
         synchronized(this.requestQueue) {
             this.requestQueue.add(request);
         }
     }
     private String deQueueRequest(){
         synchronized(this.requestQueue) {
             if(this.requestQueue.size() > 0){
                 return ((String)this.requestQueue.remove(0));
             } else {
                 return null;
             }
         }
     }
 
     /* Interfaces for launching the diet platform, or parts thereof */
     public void requestLaunch(String request){
         boolean deploySuccess = false;
         setChanged();
         consoleCtrl.printOutput("Deployer: Sending deploy state LAUNCHING.",3);
         notifyObservers(new goDiet.Events.DeployStateChange(
             this,goDiet.Defaults.DEPLOY_LAUNCHING));
         clearChanged();
         
         if(request.compareTo("all") == 0){
             deploySuccess = launchPlatform();
         }
         
         setChanged();
         if(deploySuccess){
             consoleCtrl.printOutput("Deployer: Sending deploy state ACTIVE.", 3);
             notifyObservers(new goDiet.Events.DeployStateChange(
                 this,goDiet.Defaults.DEPLOY_ACTIVE));
         } else {
             consoleCtrl.printOutput("Deployer: Sending deploy state INACTIVE.", 3);
             notifyObservers(new goDiet.Events.DeployStateChange(
                 this,goDiet.Defaults.DEPLOY_INACTIVE));
         }
         clearChanged();
     }
     
     public boolean launchPlatform() {
         java.util.Date startTime, endTime;
         double timeDiff;
         startTime = new java.util.Date();
         consoleCtrl.printOutput("* Launching DIET platform at " + 
                 startTime.toString());
 
         prepareScratch();
         if(launchOmniNames() == false){
            return false;
         }
         if(this.dietPlatform.useLogCentral()){
             launchLogCentral();
             if(this.dietPlatform.getLogCentral().useLogToGuideLaunch()){
                 connectLogCentral();
             }
             if(this.dietPlatform.useTestTool()){
                 launchTestTool();
             }
             /*LogCentral logCentral = this.dietPlatform.getLogCentral();
             LaunchInfo logInfo = logCentral.getLaunchInfo();
             if((logInfo.getLaunchState() == goDiet.Defaults.LAUNCH_STATE_RUNNING) &&
                (this.dietPlatform.getTestTool() != null)) {
               if(logCentral.getConnectDuringLaunch() == false){
                 launchTestTool();
               } else if(logCentral.getConnectDuringLaunch() &&
                  (logInfo.getLogState() == goDiet.Defaults.LOG_STATE_RUNNING)){
                  launchTestTool();
               }
             }*/
         }
         launchMasterAgents();
         launchLocalAgents();
         launchServerDaemons();
         endTime = new java.util.Date();
         timeDiff = (endTime.getTime() - startTime.getTime())/1000;
         consoleCtrl.printOutput("* DIET launch done at " + endTime.toString() +
                 " [time= " + timeDiff + " sec]");
         return true;
     }
     
     public void prepareScratch() {
         String runLabel = null;
         RunConfig runCfg = consoleCtrl.getRunConfig();
         if(runCfg.isLocalScratchReady()){
             consoleCtrl.printOutput("Local scratch " + 
                 runCfg.getLocalScratch() + " already ready.", 2);
             return;
         }
         
         // Create physical scratch space and set runCfg variables 
         // runLabel, localScratch, and scratchReady
         launcher.createLocalScratch();
     }
     
     public boolean launchOmniNames() {
         OmniNames omni = this.dietPlatform.getOmniNames();
         launchService(omni);
         if(omni.getLaunchInfo().getLaunchState() != 
                 goDiet.Defaults.LAUNCH_STATE_RUNNING){
             consoleCtrl.printError("OmniNames launch failed. " +
                 "All others will fail.", 0);
             return false;
         }
         return true;
     }
     
     public void launchLogCentral() {
         Elements logger = this.dietPlatform.getLogCentral();
         launchService(logger);
     }
     
     public void connectLogCentral() {
         LogCentral logger = this.dietPlatform.getLogCentral();
         
         if(logger.logCentralConnected()){
             consoleCtrl.printError("* Error: log central already connected.", 1);
             return;
         }
         if (logger.getLaunchInfo().getLaunchState() != 
                 goDiet.Defaults.LAUNCH_STATE_RUNNING) {
           logger.setLogCentralConnected(false);
           return;
         }
         
         OmniNames omni = this.dietPlatform.getOmniNames();
         if(logCommCtrl.connectLogService(omni) == true){
             consoleCtrl.printOutput("* Connected to Log Central.", 1);
             logger.getLaunchInfo().setLogState(
                     goDiet.Defaults.LOG_STATE_RUNNING);
             logger.setLogCentralConnected(true);
         } else {
             consoleCtrl.printError("* Error connecting to log central.", 1);
             logger.getLaunchInfo().setLogState(
                     goDiet.Defaults.LOG_STATE_CONFUSED);
             logger.setLogCentralConnected(false);
         }  
     }
     
     public void launchTestTool() {
         Elements testTool = this.dietPlatform.getTestTool();
         launchService(testTool);
     }
     
     public void launchService(Elements service){
         ComputeResource compRes = service.getComputeResource();
         launchElement(service,compRes);
     }
     
     public void launchMasterAgents() {
         java.util.Vector mAgents = this.dietPlatform.getMasterAgents();
         launchElements(mAgents);
     }
     
     public void launchLocalAgents() {
         java.util.Vector lAgents = this.dietPlatform.getLocalAgents();
         launchElements(lAgents);
     }
     
     public void launchServerDaemons() {
         java.util.Vector seds = this.dietPlatform.getServerDaemons();
         launchElements(seds);
     }
     
     public void launchElements(java.util.Vector elements) {
         Elements currElement = null;
         String hostRef = null;
         LaunchInfo parentLI = null;
         boolean didLaunch = false;
         for( int i = 0; i < elements.size(); i++) {
             currElement = (Elements) elements.elementAt(i);
             ComputeResource compRes = currElement.getComputeResource();
             didLaunch = launchElement(currElement,compRes);
         }
     }
     
     private boolean launchElement(Elements element,
                                   ComputeResource compRes) {
         //boolean userCont = true;
         
         if(checkLaunchReady(element, compRes) == false){
             return false;
         }
         /*if(runConfig.debugLevel >= 3){
             userCont = waitUserReady(element);
         }
         if(userCont){*/
         
         /*** LAUNCH */
         launcher.launchElement(element,dietPlatform.useLogCentral());
         
         waitAfterLaunch(element, compRes);
         
         return true;
     }
     
     /*** ERROR CHECKING FOR VALID LAUNCH CONDITIONS */
     private boolean checkLaunchReady(Elements element,
                                      ComputeResource compRes){
         if(element == null){
             consoleCtrl.printError("Can not launch null element.");
             return false;
         }
         if(compRes == null){
             consoleCtrl.printError("Can not launch on null resource.");
             return false;
         }
         if((element.getLaunchInfo() != null) &&
            (element.getLaunchInfo().getLaunchState() == 
                 goDiet.Defaults.LAUNCH_STATE_RUNNING)){
             consoleCtrl.printError("Element " + element.getName() +
                 " is already running.  Launch request ignored.", 0);
             return false;
         }
         
         if(!(element instanceof OmniNames)){
            // No launch if omniNames is not already running
            // [unless we're currently launching omniNames!]
            if((this.dietPlatform.getOmniNames()).getLaunchInfo().getLaunchState() != 
                 goDiet.Defaults.LAUNCH_STATE_RUNNING){
               consoleCtrl.printError("OmniNames is not running. " + 
                 " Launch for " + element.getName() + " refused.");
               return false;
            }
           
            // No launch if user wants log feedback to guide launch progress
            // and log central is not correctly connected
            if(!(element instanceof LogCentral) &&
                (this.dietPlatform.useLogCentral()) &&
                (this.dietPlatform.getLogCentral().useLogToGuideLaunch())) {
               if(!(this.dietPlatform.getLogCentral().logCentralConnected())){
                  consoleCtrl.printError("LogCentral is not connected. " + 
                         " Launch for " + element.getName() + " refused.");
                  return false;
               }
            }
         }
  
         // For elements with parent in hierarchy, check on run status of parent
         LaunchInfo parentLI = null;
         Agents parent = null;
         if(element instanceof goDiet.Model.LocalAgent){
             parent = ((LocalAgent)element).getParent();
             parentLI = parent.getLaunchInfo();
         } else if (element instanceof goDiet.Model.ServerDaemon){
             parent = ((ServerDaemon)element).getParent();
             parentLI = parent.getLaunchInfo();
         }
         if((element instanceof goDiet.Model.LocalAgent) ||
            (element instanceof goDiet.Model.ServerDaemon)){
             if(parentLI.getLaunchState() != 
                     goDiet.Defaults.LAUNCH_STATE_RUNNING){
                consoleCtrl.printError("Can not launch " + element.getName() +
                   " because parent " + parent.getName() + " is not running.", 1);
                return false;
             }
             if( this.dietPlatform.useLogCentral() &&
                 this.dietPlatform.getLogCentral().useLogToGuideLaunch() &&
                !(this.dietPlatform.getLogCentral().logCentralConnected()) &&
                 (parentLI.getLogState() != goDiet.Defaults.LOG_STATE_RUNNING)){
                consoleCtrl.printError("Can not launch " + element.getName() +
                   " because parent " + parent.getName() + 
                   " did not register with log.", 1);
                return false;         
             }       
         }
         return true;
     }
     
     /*** WAIT FOR PROPER LAUNCH BEFORE RETURNING */
     private void waitAfterLaunch(Elements element,
                                  ComputeResource compRes){
         if(element instanceof goDiet.Model.Services){
             consoleCtrl.printOutput(
                 "Waiting for 3 seconds after service launch",1);
             try {
                 Thread.sleep(3000);
             } catch (InterruptedException x){
                 consoleCtrl.printError("Launch Service: Unexpected sleep " +
                     "interruption.",0);
             }
         } else if(this.dietPlatform.useLogCentral() &&
                   this.dietPlatform.getLogCentral().logCentralConnected()){
             consoleCtrl.printOutput(
                 "Waiting on log service feedback",1);
             try {
                 synchronized(this){
                     this.waitingOn = element;
                     this.wait(10000);
                 }
             } catch (InterruptedException x) {
                 consoleCtrl.printError("LaunchPlatform: Unexpected wait " +
                         "interruption.", 0);
             }
             if(element.getLaunchInfo().getLogState() == 
                     goDiet.Defaults.LOG_STATE_RUNNING){
                 consoleCtrl.printOutput("Element " + element.getName() +
                      " registered with log.", 2);
             } else {
                 consoleCtrl.printOutput("Element " + element.getName() +
                       " did not register with log before deadline.", 1);
                 // TODO: any special launch handling required here?
             }
         } else { 
             consoleCtrl.printOutput(
                 "Waiting for 2 seconds after launch without log service feedback", 1);
             try {
                 Thread.sleep(2000);
             } catch (InterruptedException x){
                 consoleCtrl.printError("Launch Element: Unexpected sleep " +
                     "interruption.",0);
             }
         } 
     }
     
     /*private boolean waitUserReady(Elements element){
         System.out.println("\nType <return> to launch " + element.getName() +
         ", <no> to skip this element, or <stop> to quit ...");
         String userInput = "";
         BufferedReader stdin = new BufferedReader(
             new InputStreamReader(System.in));
         try {
             userInput = stdin.readLine();
         } catch(Exception x) {
             System.err.println("Exception caught while waiting for input. " +
                     "Ignoring exception.");
         }
         userInput = userInput.trim();
         if(userInput.equals("no")){
             System.out.println("Skipping launch of " + element.getName() +
                     ".  The launch of any sub-elements will fail!");
             return false;
         } else if(userInput.equals("stop")){
             stopPlatform();
             System.exit(1);
         }
         return true;
     }*/
     
     /* Interfaces for stopping the diet platform, or parts thereof */
     public void stopPlatform() {
         stopServerDaemons();
         stopLocalAgents();
         stopMasterAgents();
         
         if(this.dietPlatform.getLogCentral() != null){
             if(this.dietPlatform.getTestTool() != null){
                 stopTestTool();
             }
             stopLogCentral();
         }
         stopOmniNames();
     }
     
     public void stopServerDaemons() {
         java.util.Vector seds = this.dietPlatform.getServerDaemons();
         stopElements(seds);
     }
     
     public void stopLocalAgents() {
         java.util.Vector lAgents = this.dietPlatform.getLocalAgents();
         stopElements(lAgents);
     }
     
     public void stopMasterAgents() {
         java.util.Vector mAgents = this.dietPlatform.getMasterAgents();
         stopElements(mAgents);
     }
     
     public void stopOmniNames() {
         Elements omni = this.dietPlatform.getOmniNames();
         stopService(omni);
     }
     
     public void stopLogCentral() {
         Elements logger = this.dietPlatform.getLogCentral();
         stopService(logger);
     }
     
     public void stopTestTool() {
         Elements testTool = this.dietPlatform.getTestTool();
         stopService(testTool);
     }
     
     public void stopService(Elements service){
         ComputeResource compRes = service.getComputeResource();
         if(stopElement(service,compRes)){
             // If stop command was run, sleep afterwards for cleanup time
             try {
                 Thread.sleep(500);
             } catch (InterruptedException x){
                 System.err.println("StopService: Unexpected sleep " +
                 "interruption.  Exiting.");
                 System.exit(1);
             }
         }
     }
     
     public void stopElements(java.util.Vector elements) {
         Elements currElement = null;
         String hostRef = null;
         for( int i = 0; i < elements.size(); i++) {
             currElement = (Elements) elements.elementAt(i);
             ComputeResource compRes = currElement.getComputeResource();
             if( stopElement(currElement,compRes) ){
                 // If stop command was run, sleep afterwards for cleanup time
                 try {
                     Thread.sleep(100);
                 }
                 catch (InterruptedException x) {
                     consoleCtrl.printError("StopElements: Unexpected sleep " +
                         "interruption. Exiting.", 0);
                 }
             }
         }
     }
     
     private boolean stopElement(Elements element,
                                 ComputeResource compRes) {
         if(element == null){
             consoleCtrl.printError("StopElement: Can not run stop on null element.");
             return false;
         }
         if(element.getLaunchInfo() == null){
            consoleCtrl.printError("Element " + element.getName() + " is not " +
                 "running. Ignoring stop command.");
            return false;
         }
         if( (element.getLaunchInfo().getLaunchState() != 
                     goDiet.Defaults.LAUNCH_STATE_RUNNING) && 
             (element.getLaunchInfo().getLaunchState() != 
                 goDiet.Defaults.LAUNCH_STATE_CONFUSED)){
             consoleCtrl.printError("Element " + element.getName() + " is not " +
                 "running. Ignoring stop command.");
             return false;
         }
         launcher.stopElement(element);
         return true;
     }
 }
