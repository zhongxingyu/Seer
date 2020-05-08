 package org.systemsbiology.gaggle.boss;
 
 import org.apache.commons.collections.iterators.ArrayListIterator;
 import org.systemsbiology.gaggle.core.Goose;
 import org.systemsbiology.gaggle.core.Goose3;
 import org.systemsbiology.gaggle.core.datatypes.*;
 import org.systemsbiology.gaggle.util.ClientHttpRequest;
 
 import java.io.*;
 import java.net.*;
 import java.rmi.RemoteException;
 import java.util.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Ning Jiang
  * Date: 10/12/12
  * Time: 1:24 PM
  *
  * A boss can handle multiple workflows. Each workflow is executed in a thread.
  * This class manages the execution and resources of workflow threads.
  */
 public class WorkflowManager {
     protected GooseManager gooseManager = null;
     protected BossImpl bossImpl = null;
     private long timerInterval = 200L; //milliseconds
     private long timerTimeout = 3000000L; // 15 seconds (for verifying if a goose is started correctly)
     private static Logger Log = Logger.getLogger("Boss");
     private Thread resourceManagementThread;
     private Map<UUID, WorkflowThread> threadMap;
     private ShutdownHookThread shutdownHook;
 
     class ShutdownHookThread extends Thread
     {
         public void run()
         {
             Cleanup(true);
         }
     }
 
     /// This thread cleans up workflow threads that have finished
     class ResourceManagementThread extends Thread
     {
         public void run()
         {
             while (true)
             {
                 try {
                     Thread.sleep(5000);
                     Cleanup(false);
                 }
                 catch (Exception e)
                 {
                     Log.severe("Workflow resource management thread failed: " + e.getMessage());
                     Cleanup(true);
                 }
             }
         }
     }
 
     private synchronized void Cleanup(boolean force)
     {
         for ( UUID key : threadMap.keySet())
         {
             WorkflowThread t = threadMap.get(key);
             if (force || t.cancel)
             {
                 t.cancel = true;
                 threadMap.remove(key);
                 Log.info("Removed thread for workflow " + key);
             }
         }
     }
 
     public WorkflowManager(BossImpl bossImpl, GooseManager gm)
     {
         this.gooseManager = gm;
         this.bossImpl = bossImpl;
         this.threadMap = Collections.synchronizedMap(new HashMap<UUID, WorkflowThread>());
         this.shutdownHook = new ShutdownHookThread();
         Runtime.getRuntime().addShutdownHook(this.shutdownHook);
         this.resourceManagementThread = new ResourceManagementThread();
         this.resourceManagementThread.start();
     }
 
     public void SubmitWorkflow(Goose3 goose, Workflow w)
     {
         if (w != null)
         {
             Log.info("Workflow submitted");
             UUID sessionID = UUID.randomUUID();
             WorkflowThread t = new WorkflowThread(goose, sessionID, w);
             this.threadMap.put(sessionID, t);
             t.start();
         }
     }
 
     /**
      * Handle workflow action received from children of a source goose
      * If it is a Response, we need to inform the source goose to
      * proceed for the next geese
      * @param action
      */
     public void HandleWorkflowAction(WorkflowAction action)
     {
         if (action != null)
         {
             Log.info("Handle workflow action from: "
                     + action.getSource().getName() + " " + action.getSessionID());
             switch (action.getActionType())
             {
                 case Response:
                     WorkflowThread t = threadMap.get(UUID.fromString(action.getSessionID()));
                     if (t != null)
                     {
                         if ((action.getOption() & WorkflowAction.Options.ErrorMessage.getValue()) > 0)
                         {
                             // we need to pass the error back to the proxy goose
                         }
                         else if ((action.getOption() & WorkflowAction.Options.SuccessMessage.getValue()) > 0)
                         {
                             // received acknowledge data from a goose
                             t.Acknowledge(action);
                         }
                     }
                     break;
 
                 case Request:
                     Log.info("Workflow Action Request received with option " + action.getOption());
                     if ((action.getOption() & WorkflowAction.Options.WorkflowReportData.getValue()) > 0)
                     {
                         // This is report data, we need to send it to web server
                         reportData(action);
                     }
                     break;
 
                 default:
                     Log.warning("Unexpected action type received by Workflow Manager.");
                     break;
             }
         }
     }
 
     /**
      * Send the report data to server to generate report html page
      * @param action
      */
     private void reportData(WorkflowAction action)
     {
         if (action.getData() != null)
         {
             Log.info("Handle report data for Workflow " + action.getWorkflowID() + " session " + action.getSessionID() + " component " + action.getComponentID());
             GaggleData data = action.getData()[0];
             if (data != null && data instanceof WorkflowData)
             {
                 Tuple tpl = (Tuple)(((WorkflowData)data).getData());
                 Log.info("====> Processing " + tpl.getSingleList().size() + " report parameters");
                 String type = "";
                 Object value = null;
                 String componentname = "";
                 for (int i = 0; i < tpl.getSingleList().size(); i++)
                 {
                     Single s = tpl.getSingleList().get(i);
                     if (s != null)
                     {
                         if (s.getName().toLowerCase().equals("type"))
                         {
                             type = (String)s.getValue();
                             Log.info("Type: " + type);
                         }
                        else if (s.getName().toLowerCase().equals("file"))
                         {
                             value = s.getValue();
                             Log.info("Value: " + value);
                         }
                         else if (s.getName().toLowerCase().equals("component-name"))
                         {
                             componentname = (String)s.getValue();
                             Log.info("Component name: " + componentname);
                         }
                         else {
                             Log.warning("Encountered unknown property name: " + s.getName());
                         }
                     }
                     else
                         Log.warning("Encountered a null Single");
                 }
 
                 if (!type.isEmpty() && value != null)
                 {
                     URL url = null;
                     try {
                        //url = new URL("http://networks.systemsbiology.net/workflow/savereport/" + action.getSessionID());
                        url = new URL("http://localhost:8000/workflow/savereport/");
                     } catch (MalformedURLException ex) {
                         Log.warning("Malformed URL");
                     }
 
                     HttpURLConnection urlConn = null;
                     try {
                         // URL connection channel.
                         urlConn = (HttpURLConnection) url.openConnection();
 
 
                         // Let the run-time system (RTS) know that we want input.
                         urlConn.setDoInput (true);
 
                         // Let the RTS know that we want to do output.
                         urlConn.setDoOutput (true);
 
                         // No caching, we want the real thing.
                         urlConn.setUseCaches (false);
                     }
                     catch (IOException ex) {
                         Log.severe("Failed to create url Connection " + ex.getMessage());
                     }
 
                     try
                     {
                         ClientHttpRequest httpRequest = new ClientHttpRequest(urlConn);
                         InputStream responseStream = null;
                         if (type.equals("file"))
                         {
                             Log.info("Upload file path: " + (String)value + " of Component " + componentname + " component workflow node id " + action.getSource().getComponentWorkflowNodeID() + " for Workflow " + action.getWorkflowID());
                             // send the component name
                             httpRequest.setParameter("sessionid", action.getSessionID());
                             httpRequest.setParameter("component-name", componentname);
                             httpRequest.setParameter("workflowid", action.getWorkflowID());
                             httpRequest.setParameter("componentid", action.getComponentID());
                             httpRequest.setParameter("componentworkflownodeid", action.getSource().getComponentWorkflowNodeID());
                             File f = new File((String)value);
                             //FileInputStream is = new FileInputStream((String)value);
                             httpRequest.setParameter("file", f);
                             responseStream = httpRequest.post();
                         }
                         else if (type.equals("url"))
                         {
                             Log.info("Upload url: " + (String)value + " of Component " + componentname + " component workflow node id " + action.getSource().getComponentWorkflowNodeID() + " for Workflow " + action.getWorkflowID());
                             httpRequest.setParameter("sessionid", action.getSessionID());
                             httpRequest.setParameter("component-name", componentname);
                             httpRequest.setParameter("workflowid", action.getWorkflowID());
                             httpRequest.setParameter("componentid", action.getComponentID());
                             httpRequest.setParameter("componentworkflownodeid", action.getSource().getComponentWorkflowNodeID());
 
                             //String content =
                             //        "name=" + URLEncoder.encode((String)value, "utf-8");
                             httpRequest.setParameter("url", (String)value);
                             responseStream = httpRequest.post();
                         }
                     }
                     catch (Exception e)
                     {
                         Log.severe("Failed to post workflow data to server: " + e.getMessage());
                     }
                 }
 
             }
         }
     }
 
     /**
      *  State machine
      */
     public enum ProcessingState
     {
         Initial,
         ParallelProcessed,
         ParallelAcknowledged,
         SequentialProcessed,
         SequentialAcknowledged,
         Finished,
         Error
     }
 
     class WorkflowNode
     {
         public ProcessingState state;
         public WorkflowComponent component;
         public Goose3 goose;
         // The number of sequential children that has acknowledged
         // to this node
         public int acknowledgedSequentials = 0;
         public int errorRetries = 0;
         public String gooseName;
 
         public WorkflowNode(WorkflowComponent c)
         {
             this.component = c;
             this.state = ProcessingState.Initial;
         }
     }
 
     class WorkflowThread extends Thread
     {
         String workflowID;
         UUID sessionID; // sessionID is needed for goose to communicate with the boss
         //int myIndex;
         //WorkflowSessionManager sessionManager;
         Workflow myWorkflow = null;
         HashMap<String, ArrayList<ArrayList<WorkflowComponent>>> workflowMap = null;
 
         // nodes that have been acknowledged by the processing component
         Map<String, String> acknowledgedParallelNodes = Collections.synchronizedMap(new HashMap<String, String>());
         Map<String, String> acknowledgedSequentialNodes = Collections.synchronizedMap(new HashMap<String, String>());
 
         // stores the data received from children component
         Map<String, GaggleData> stagingParallelData = Collections.synchronizedMap(new HashMap<String, GaggleData>());
         Map<String, GaggleData> stagingSequentialData = Collections.synchronizedMap(new HashMap<String, GaggleData>());
 
         // contains nodes currently being processed
         List<WorkflowNode> processingQueue = Collections.synchronizedList(new ArrayList<WorkflowNode>());
 
         // contains nodes that will be processed after all the nodes in the processingQueue are finished
         //ArrayList<WorkflowNode> pendingQueue;
 
         public boolean cancel = false;
         String[] snapshotBeforeStartingGoose;
 
         Goose3 proxyGoose = null;
         String messageType;
         String message;
         int MAX_ERROR_RETRIES = 3;
 
         //Object threadSyncObj = new Object();
         //int stepsize = 1;
 
         public WorkflowThread(Goose3 proxyGoose, UUID sessionID, Workflow w)
         {
             Log.info("Initiating workflow thread " + sessionID.toString());
             //this.myIndex = indx;
             //this.sessionManager = sessionManager;
             this.workflowID = w.getWorkflowID();
             this.sessionID = sessionID;
             this.proxyGoose = proxyGoose;
             this.myWorkflow = w;
             this.workflowMap = w.getWorkflow();
             //this.processingQueue = new ArrayList<WorkflowNode>();
             //this.pendingQueue = new ArrayList<WorkflowNode>();
             //this.acknowledgedNodes = new HashedMap();
             //this.stagingData = new HashedMap<String, GaggleData>();
             //this.stepsize = stepsize;
 
             // add start nodes into the processingQueue
             ArrayList<String> startnodes = w.getStartNodeIDs();
             for (int i = 0; i < startnodes.size(); i++)
             {
                 Log.info("Adding start node " + startnodes.get(i));
                 try{
                     ArrayList parallelcomponents = this.workflowMap.get(startnodes.get(i)).get(0);
                     // Push the start node to the processing queue
                     System.out.println("Pushing start node " + startnodes.get(i));
                     WorkflowComponent c = (WorkflowComponent)parallelcomponents.get(0);
                     WorkflowNode node = new WorkflowNode(c);
                     this.processingQueue.add(node);
                 }
                 catch (Exception e)
                 {
                     Log.severe(e.getMessage());
                 }
             }
         }
 
         public void HandleError(WorkflowAction action)
         {
            try {
                 this.proxyGoose.handleWorkflowAction(action);
            }
            catch (Exception e)
            {
                Log.info("Failed to pass error info to proxy goose");
            }
         }
 
         /**
          * Process acknowledgement from the source and children components
          * Store the data to the target geese
          * @param action
          */
         public void Acknowledge(WorkflowAction action)
         {
             Log.info("Acknowledging workflow thread: " + sessionID.toString());
             if ((action.getOption() & WorkflowAction.Options.Parallel.getValue()) > 0
                     && action.getTargets() != null)
             {
                 GaggleData[] data = action.getData();
                 WorkflowComponent[] targets = action.getTargets();
 
                 Log.info("Storing parallel acknowledgement " + action.getSource().getComponentID() + " " + data.length);
                 this.acknowledgedParallelNodes.put(action.getSource().getComponentID(), action.getSource().getComponentID());
 
                 if (data != null && targets != null)
                 {
                     for (int i = 0; i < data.length; i++)
                     {
                         this.stagingParallelData.put(targets[i].getComponentID(), data[i]);
                     }
                 }
             }
             else if ((action.getOption() & WorkflowAction.Options.Sequential.getValue()) > 0
                     && action.getTargets() != null)
             {
                 this.acknowledgedSequentialNodes.put(action.getSource().getComponentID(), action.getSource().getComponentID());
                 GaggleData[] data = action.getData();
                 WorkflowComponent[] targets = action.getTargets();
                 Log.info("Storing sequential acknowledgement " + action.getSource().getComponentID() + " " + data.length);
 
                 if (data != null && targets != null)
                 {
                     for (int i = 0; i < data.length; i++)
                     {
                         this.stagingSequentialData.put(targets[i].getComponentID(), data[i]);
                     }
                 }
             }
         }
 
         private void Report(String type, String msg, boolean passToProxy)
         {
             Log.info(type + " " + msg);
             this.messageType = type;
             this.message = msg;
             if (passToProxy)
             {
                 try{
                     this.proxyGoose.handleWorkflowInformation(type, msg);
                 }
                 catch(Exception e)
                 {
                     Log.severe("Failed to call handleWorkflowInformation on Proxy Goose");
                 }
             }
         }
 
         public void run()
         {
             //int readahead = stepsize;
             try {
                 while (!cancel)
                 {
                     boolean hasPendingNodes = false;
                     if (processingQueue.size() > 0)
                     {
                         //Log.info("New round of processing nodes");
                         for (int i = 0; i < processingQueue.size(); i++)
                         {
                             WorkflowNode c = processingQueue.get(i);
                             if (c.state != ProcessingState.Finished)
                             {
                                 ProcessWorkflowNode(c);
                                 hasPendingNodes = true;
                             }
                         }
 
                         if (!hasPendingNodes)
                             cancel = true;
                     }
                     /*else if (pendingQueue.size() > 0)
                     {
                         // fetch pending nodes to the processingQueue
                         readahead = (stepsize < 0 ? this.pendingQueue.size() : stepsize);
                         for (int i = readahead - 1; i >= 0; i--)
                         {
                             WorkflowComponent c = this.processingQueue.get(i);
                             processingQueue.add(c);
                             pendingQueue.remove(i);
                         }
                     } */
                     else
                     {
                         Log.info("Finished processing session " + this.sessionID.toString());
                         cancel = true;
                     }
                     Thread.sleep(2000);
                 }
             }
             catch (Exception e)
             {
                 Log.severe("Error processing nodes for session " + this.sessionID.toString() + ": " + e.getMessage());
             }
         }
 
 
         /**
          * Process a workflow. It's a state machine. Basically, for each component,
          * we first start its corresponding goose
          * and then process its parallel and sequential children in turn
          * @param nodeID
          */
         private void ProcessWorkflowNode(WorkflowNode c)
         {
             Goose3 sourceGoose = c.goose;
             WorkflowComponent source = c.component;
             if (c.state == ProcessingState.Initial)
             {
                 c.errorRetries++;
                 Log.info("Handling workflow node " + source.getComponentID() + " state: initial");
                 boolean sourceStarted = false;
 
                 // Find or create the goose corresponding to the source component
                 if ((sourceGoose = PrepareGoose(source)) != null)
                 {
                     Log.info("Goose " + source.getGooseName() + " started.");
                     sourceStarted = true;
                     c.goose = sourceGoose;
                     try {
                         c.gooseName = sourceGoose.getName();
                     }
                     catch (Exception e)
                     {
                         Log.severe("Failed to get goose name...");
                         c.state = ProcessingState.Error;
                     }
                 }
                 else
                 {
                     this.Report(this.messageType, this.message, true);
 
                     // we cannot start the goose, so let's mark this node as error
                     // we allow other errors so that the workflow won't be broken by one single node
                     c.state = ProcessingState.Error;
                 }
 
                 if (sourceStarted)
                 {
                     ArrayList<WorkflowComponent> parallelcomponents = workflowMap.get(source.getComponentID()).get(0);
                     Log.info("Component " + source.getComponentID() + " has " + parallelcomponents.size() + " parallel children");
                     WorkflowComponent[] targets = null;
                     if (parallelcomponents != null && parallelcomponents.size() > 1)
                     {
                         // Pass the parallel children info to the source
                         targets = new WorkflowComponent[parallelcomponents.size() - 1];
                         for (int i = 1; i < parallelcomponents.size(); i++)
                         {
                             targets[i - 1] = parallelcomponents.get(i);
                         }
                     }
 
                     // We need to do this even if there is no parallel child
                     // because the user might just want to show some data using
                     // the source component
                     try {
                         Log.info("Pass data to source for parallel children");
                         Log.info(source.getParams().get(WorkflowComponent.ParamNames.Data.getValue()).toString());
                         Log.info("SessionID: " + sessionID.toString());
                         Log.info("ComponentID: " + source.getComponentID());
                         WorkflowAction action = new WorkflowAction(
                                 this.workflowID,
                                 sessionID.toString(),
                                 source.getComponentID(),
                                 WorkflowAction.ActionType.Request,
                                 source,
                                 targets,
                                     WorkflowAction.Options.Parallel.getValue(),
                                 //WorkflowAction.DataType.WorkflowData,
                                 null  // Data is contained in the source node's params property
                         );
 
                         //threadSyncObj = new Object();
                         sourceGoose.handleWorkflowAction(action);
                         // if there is no parallel child, we skip to the ParallelAcknowledged state
                         c.state = (parallelcomponents != null && parallelcomponents.size() > 1) ?
                                 ProcessingState.ParallelProcessed : ProcessingState.ParallelAcknowledged;
                     }
                     catch (Exception e0)
                     {
                         this.Report("Error", "Failed to process parallel action for node "
                                 + c.component.getComponentID() + " " + e0.getMessage(), true);
 
 
                         Log.info(c.component.getComponentID() + " failed");
                         c.state = ProcessingState.Error;
                     }
                 }
             }
             else if (c.state == ProcessingState.ParallelProcessed)
             {
                 //Log.info("Handling workflow node " + source.getComponentID() + " state: ParallelProcessed");
                 if (this.acknowledgedParallelNodes.containsKey(source.getComponentID()))
                 {
                     // Add all the parallel components to processingQueue
                     Log.info("Found acknowledgement!");
                     ArrayList<WorkflowComponent> parallelcomponents = workflowMap.get(source.getComponentID()).get(0);
                     for (int i = 1; i < parallelcomponents.size(); i++)
                     {
                         WorkflowComponent pc = parallelcomponents.get(i);
                         GaggleData dataForChild = this.stagingParallelData.get(pc.getComponentID());
                         if (dataForChild != null)
                         {
                             Log.info("Adding ack data " + dataForChild.getName());
                             pc.addParam(WorkflowComponent.ParamNames.Data.getValue(), dataForChild);
                         }
                         WorkflowNode pn = new WorkflowNode(pc);
                         this.processingQueue.add(pn);
                     }
                     // remove acknowledge data
                     this.acknowledgedParallelNodes.remove(source.getComponentID());
                     c.state = ProcessingState.ParallelAcknowledged;
                 }
             }
             else if (c.state == ProcessingState.ParallelAcknowledged)
             {
                 Log.info("Handling workflow node " + source.getComponentID() + " state: ParallelAcknowledged");
                 // Now we process sequential nodes
                 Log.info("Pass data to source for sequential children of " + source.getComponentID());
 
                 ArrayList<WorkflowComponent> sequentialcomponents = workflowMap.get(source.getComponentID()).get(1);
                 if (sequentialcomponents != null && sequentialcomponents.size() > 0)
                 {
                     try {
                         WorkflowComponent[] targets = null;
                         targets = new WorkflowComponent[1];
                         targets[1] = sequentialcomponents.get(0);
                         WorkflowAction action = new WorkflowAction(
                                 this.workflowID,
                                 sessionID.toString(),
                                 targets[1].getComponentID(),
                                 WorkflowAction.ActionType.Request,
                                 source,
                                 targets,
                                 WorkflowAction.Options.Sequential.getValue(),
                                 //WorkflowAction.DataType.WorkflowData,
                                 null
                         );
                         sourceGoose.handleWorkflowAction(action);
                         c.state = ProcessingState.SequentialProcessed;
                     }
                     catch (Exception e1)
                     {
                         this.Report("Error", "Failed to process sequential action for node "
                                 + c.component.getComponentID() + " " + e1.getMessage(), true);
                         c.state = ProcessingState.Error;
                     }
                 }
                 else {
                     Log.info("No sequential child for " + source.getComponentID());
                     c.state = ProcessingState.Finished;
                 }
             }
             else if (c.state == ProcessingState.SequentialProcessed)
             {
                 Log.info("Handling workflow node " + source.getComponentID() + " state: SequentialProcessed");
                 Log.info("Currently acknowledged: " + c.acknowledgedSequentials);
                 ArrayList<WorkflowComponent> sequentialcomponents = workflowMap.get(source.getComponentID()).get(1);
                 if (this.acknowledgedSequentialNodes.containsKey(source.getComponentID())) //, sequentialcomponents.get(c.acknowledgedSequentials)))
                 {
                     WorkflowComponent sc = sequentialcomponents.get(c.acknowledgedSequentials);
                     GaggleData dataForChild = this.stagingSequentialData.get(sc.getComponentID());
                     if (dataForChild != null)
                     {
                         Log.info("Adding ack data for sequential child " + dataForChild.getName());
                         sc.addParam(WorkflowComponent.ParamNames.Data.getValue(), dataForChild);
                     }
                     // Now we add the child sequential node to the processing queue
                     this.processingQueue.add(new WorkflowNode(sc));
                     c.acknowledgedSequentials++;
 
                     if (c.acknowledgedSequentials >= sequentialcomponents.size())
                     {
                         // all sequential nodes are processed
                         c.state = ProcessingState.SequentialAcknowledged;
                         this.acknowledgedSequentialNodes.remove(source.getComponentID());
                     }
                     else
                     {
                         // Start an action for the next component
                         try {
                             WorkflowComponent[] targets = null;
                             targets = new WorkflowComponent[1];
                             targets[0] = sequentialcomponents.get(c.acknowledgedSequentials);
                             Log.info("Prepare for sequential node " + targets[0].getComponentID());
                             WorkflowAction action = new WorkflowAction(
                                     this.workflowID,
                                     sessionID.toString(),
                                     targets[0].getComponentID(),
                                     WorkflowAction.ActionType.Request,
                                     source,
                                     targets,
                                     //WorkflowAction.DataType.WorkflowData,
                                     WorkflowAction.Options.Sequential.getValue(),
                                     null
                             );
 
                             // Remove the node from the acknowledgement map because we are
                             // waiting for the ack for the current target
                             this.acknowledgedSequentialNodes.remove(source.getComponentID());
 
                             sourceGoose.handleWorkflowAction(action);
                         }
                         catch (Exception e1)
                         {
                             this.Report("Error", "Failed to process sequential action for node "
                                     + source.getComponentID() + " " + e1.getMessage(), true);
                             c.state = ProcessingState.Error;
                         }
                     }
                 }
             }
             else if (c.state == ProcessingState.SequentialAcknowledged)
             {
                 Log.info("Handling workflow node " + source.getComponentID() + " state: SequentialAcknowledged");
 
                 // All parallel and sequential nodes are processed, we can remove c from processingQueue
                 c.state = ProcessingState.Finished;
             }
             else if (c.state == ProcessingState.Error)
             {
                 if (c.errorRetries <= MAX_ERROR_RETRIES)
                 {
                     // We failed to connect to the goose, remove the goose from Boss and try again
                     try {
                         Thread.sleep(2000);
                         Log.warning("Trying to unregister the goose...");
                         bossImpl.unregister(c.gooseName);
                         c.state = ProcessingState.Initial;
                     }
                     catch (Exception e)
                     {
                         Log.info("RMI failed to unregister goose" + e.getMessage());
                         c.state = ProcessingState.Initial;
                     }
                 }
                 else
                 {
                     Log.severe("All retries failed. Terminate the processing of node " + c.component.getGooseName());
                     c.state = ProcessingState.Finished;
                 }
             }
         }
 
 
         private boolean InSnapshot(String gooseName)
         {
             if (snapshotBeforeStartingGoose != null)
             {
                 for (int i = 0; i < snapshotBeforeStartingGoose.length; i++)
                 {
                     Log.info(snapshotBeforeStartingGoose[i] + " " + gooseName);
                     if (snapshotBeforeStartingGoose[i].equals(gooseName))
                     {
                         return true;
                     }
                 }
             }
             return false;
         }
 
         private Goose3 PrepareGoose(WorkflowComponent source)
         {
             String[] geeseNames = bossImpl.getListeningGooseNames();
             boolean foundGoose = false;
             Goose goose = null;
             if (geeseNames != null)
             {
                 for (int i = 0; i < geeseNames.length; i++)
                 {
                     String currentGooseName = geeseNames[i];
                     Log.info("Current goose name: " + currentGooseName);
                     String[] gooseNameSplitted = currentGooseName.split(";");
                     Log.info("Splitted goose name: " + gooseNameSplitted.length);
                     if (gooseNameSplitted.length > 1)
                         Log.info("Splitted goose name: " + gooseNameSplitted[0] + " " + gooseNameSplitted[1]);
                     if ((gooseNameSplitted.length > 1 && gooseNameSplitted[0].equals(gooseNameSplitted[1]))
                             || gooseNameSplitted.length == 1)
                     {
                         // We always append the name of the application for Cytoscape, we want to broadcast
                         // to the original goose with the name similar to Cytoscape v2.8.3;Cytoscape v2.8.3
                         if (currentGooseName.trim().toLowerCase().contains(source.getGooseName().toLowerCase()))
                         {
                             goose = bossImpl.getGoose(geeseNames[i]);
                             if (goose instanceof Goose3)
                             {
                                 Log.info("Found existing goose " + geeseNames[i]);
                                 return (Goose3)goose;
                             }
                         }
                     }
                 }
             }
 
             if (goose == null)
             {
                 goose = tryToStartGoose(source);
                 if (goose != null && goose instanceof Goose3)
                     return (Goose3)goose;
             }
             Log.info("No goose found for " + source.getGooseName());
             return null;
         }
 
         class WaitForGooseStart extends TimerTask {
             long startTime = System.currentTimeMillis();
             String gooseName;
             boolean gooseStarted = false;
 
             public WaitForGooseStart(String gooseName)
             {
                 super();
                 this.gooseName = gooseName;
             }
 
             public boolean IsGooseStarted() { return gooseStarted; }
 
             public void run() {
                 long elapsed = System.currentTimeMillis() - startTime;
                 if (elapsed > timerTimeout) {
                     Log.info("Didn't hear from the goose for 15 seconds, timing out.");
                     this.cancel();
                 }
 
                 try {
                     // we get the listening geese, non-listening geese could be "phantom"
                     // (e.g. Close Firefox and firegoose might still lingering there
                     String[] gooseNames = bossImpl.getListeningGooseNames();
                     for (int i = 0; i < gooseNames.length; i++) {
                         String currentGooseName = gooseNames[i];
                         //Log.info("Retrieve a goose: " + currentGooseName + " " + this.gooseName);
                         String[] gooseNameSplitted = currentGooseName.split(";");
                         if ((gooseNameSplitted.length > 1 && gooseNameSplitted[0].equals(gooseNameSplitted[1]))
                             || gooseNameSplitted.length == 1)
                         {
                             // We always append the name of the application for Cytoscape, we want to broadcast
                             // to the original goose
                             if (currentGooseName.toLowerCase().contains(this.gooseName.toLowerCase())
                                     && !InSnapshot(currentGooseName))
                             {
                                 // The goose has been started correctly!
                                 Log.info("Our goose is started!");
                                 gooseStarted = true;
                                 this.gooseName = currentGooseName;
                                 synchronized (syncObj) {
                                     syncObj.notify();
                                 }
                                 this.cancel();
                             }
                         }
                     }
                 } catch (Exception ex) {
                     Log.log(Level.WARNING, "unknown Exception in WaitForGooseStart: " + ex.getMessage());
                     message = "general exception trying to start goose: " + this.gooseName + " " + ex.getMessage();
                     messageType = "Error";
                     System.out.println(message);
                     ex.printStackTrace();
                 }
             }
         }
 
         private Object syncObj = null;
         public Goose tryToStartGoose(WorkflowComponent goose) {
             if (goose != null)
             {
                 //String command = System.getProperty("java.home");
                 //command += File.separator +  "bin" + File.separator + "javaws " + GaggleConstants.BOSS_URL;
                 try {
                     System.out.println("Starting goose " + goose.getGooseName());
                     syncObj = new Object();
 
                     String cmdToRun = goose.getCommandUri();
                     if (goose.getArguments() != null && goose.getArguments().length() > 0)
                         Log.info("Arguments: " + goose.getArguments());
                     if (cmdToRun != null && cmdToRun.trim().length() > 0)
                     {
                         cmdToRun = cmdToRun.trim().toLowerCase();
                         // Somehow JSON Stringify wrapped the command uri with "".
                         if (cmdToRun.endsWith(".bat\"") || cmdToRun.endsWith("bat"))
                         {
                             Log.info("Command line is a batch file... " + cmdToRun);
                             // if it is a batch file, we need to do something more...
                             // Remove the quotations
                             if (cmdToRun.endsWith(".bat\""))
                                 cmdToRun = cmdToRun.substring(1, cmdToRun.length() - 1);
 
                             File f = new File(cmdToRun);
                             String path = f.getParent();
 
                             String[] cmdsToRun = new String[5];
                             cmdsToRun[0] = "cmd.exe";
                             cmdsToRun[1] = "/C";
                             cmdsToRun[2] = "start";
                             cmdsToRun[3] = cmdToRun;
                             cmdsToRun[4] = goose.getArguments();
                             Runtime.getRuntime().exec(cmdsToRun, null, new File(path));
                         }
                         else if (cmdToRun.endsWith(".sh"))
                         {
                             Log.info("Shell script: " + cmdToRun);
 
                             File f = new File(cmdToRun);
                             String path = f.getParent();
 
                             String[] cmdsToRun = new String[3];
                             cmdsToRun[0] = "sh";
                             cmdsToRun[1] = cmdToRun;
                             cmdsToRun[2] = goose.getArguments();
                             Runtime.getRuntime().exec(cmdsToRun, null, new File(path));
                         }
                         else
                         {
                             Log.info("Goose command line: " + cmdToRun);
                             String[] cmdsToRun = null;
                             if (goose.getArguments() == null || goose.getArguments() == "")
                             {
                                 cmdsToRun = new String[1];
                                 cmdsToRun[0] = cmdToRun;
                             }
                             else
                             {
                                 cmdsToRun = new String[2];
                                 cmdsToRun[0] = cmdToRun;
                                 cmdsToRun[1] = goose.getArguments();
                             }
                             File f = new File(cmdToRun);
                             String path = f.getParent();
                             Runtime.getRuntime().exec(cmdsToRun, null, new File(path)); // Goose will register itself with boss once it starts
                         }
                         Timer timer = new Timer();
                         WaitForGooseStart wfg = new WaitForGooseStart(goose.getGooseName()); //.getName());
                         timer.schedule(wfg, 0, timerInterval);
                         synchronized (syncObj) {
                             syncObj.wait();
                         }
                         if (wfg.IsGooseStarted())
                             return gooseManager.getGoose(wfg.gooseName);
                     }
                     else
                         Log.warning("Empty command line for " + goose.getGooseName() + "encountered!");
                 } catch (IOException e) {
                     message = "Failed to start goose: " + goose.getGooseName() + " " + e.getMessage();
                     messageType = "Error";
                     Log.severe(message);
                     e.printStackTrace();
                 }
                 catch (InterruptedException e1) {
                     message = "Failed to wait for goose: " + goose.getGooseName() + " " + e1.getMessage();
                     messageType = "Error";
                     Log.severe(message);
                     e1.printStackTrace();
                 }
             }
             return null;
         }
     }
 
 
 }
