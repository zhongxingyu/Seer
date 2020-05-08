 package org.systemsbiology.gaggle.boss;
 
 import java.io.*;
 import java.lang.reflect.Field;
 import java.net.ConnectException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 import java.rmi.server.*;
 import java.rmi.registry.*;
 import java.rmi.Naming;
 import java.rmi.RemoteException;
 
 import net.sf.json.JSONObject;
 import org.hyperic.sigar.Sigar;
 import org.hyperic.sigar.ptql.ProcessFinder;
 import org.systemsbiology.gaggle.core.*;
 import org.systemsbiology.gaggle.geese.DeafGoose;
 import org.systemsbiology.gaggle.core.datatypes.*;
 import org.systemsbiology.gaggle.util.*;
 import org.hyperic.sigar.ProcExe;
 import sun.awt.AppContext;
 
 import javax.swing.*;
 import java.util.logging.*;
 
 class ProxyGooseMessage
 {
     private String type;
     private String message;
 
     public String getType() { return type; }
     public String getMessage() { return message; }
 
     public ProxyGooseMessage(String type, String msg)
     {
         this.type = type;
         this.message = msg;
     }
 }
 
 /**
  *  When we call a proxy goose, we need to spawn a thread to do so.
  *  This is because when a goose calls the broadcast* interface of Boss, Boss in turn
  *  calls the GetGooseInfo of the goose, which will hang the goose due to reentrance.
  *  The problem is addressed by wrapping the calls in a thread
  */
 class ProxyCallbackThread extends Thread
 {
     BossImpl bossimpl;
     Goose3 proxyGoose;
     List<ProxyGooseMessage> processingQueue = Collections.synchronizedList(new ArrayList<ProxyGooseMessage>());
     boolean cancel = false;
     private static Logger Log = Logger.getLogger("Boss");
 
     public ProxyCallbackThread(BossImpl bossimpl, Goose3 pg)
     {
         this.bossimpl = bossimpl;
         this.proxyGoose = pg;
     }
 
     public void setProxyGoose(Goose3 proxyGoose) { this.proxyGoose = proxyGoose; }
 
     public void AddMessage(ProxyGooseMessage msg)
     {
         this.processingQueue.add(msg);
     }
 
     public void run()
     {
         //int readahead = stepsize;
         while (!cancel)
         {
            try {
                if (!processingQueue.isEmpty())
                {
                    // Handle msg for the proxy goose
                    ProxyGooseMessage msg = processingQueue.get(0);
                    if (proxyGoose != null) {
                        Log.info("Passing " + msg.getType() + " " + msg.getMessage() + " to ProxyGoose");
                        proxyGoose.handleWorkflowInformation(msg.getType(), msg.getMessage());
                    }
                    processingQueue.remove(0);
                }
 
                Thread.sleep(3000);
            }
            catch (Exception e) {
                Log.warning("Failed to process proxy goose callback: " + e.getMessage());
                processingQueue.remove(0);
            }
         }
     }
 }
 
 class RestoreStateThread extends Thread
 {
     private static Logger Log = Logger.getLogger("Boss");
     private String goosename;
     private String serviceurl;
     private ArrayList<String> fileinfo;
     private WorkflowManager workflowManager;
 
     public RestoreStateThread(WorkflowManager wm, String goosename, String serviceurl, ArrayList<String> fileinfo)
     {
         this.workflowManager = wm;
         this.goosename = goosename;
         this.serviceurl = serviceurl;
         this.fileinfo = fileinfo;
     }
 
     public void run()
     {
         if (workflowManager != null && serviceurl != null && serviceurl.length() > 0)
         {
             // download all state files to local temp dir
             try
             {
                 // Start the goose and parse the restore file
                 Log.info("Starting goose " + goosename);
                 WorkflowComponent c = new WorkflowComponent("", "", "", goosename, goosename, "", serviceurl, "", null);
                 Object syncObj = new Object();
                 Goose3 goose = workflowManager.PrepareGoose(c, syncObj);
                 if (goose != null)
                 {
                     String toplevelfn = "";
                     for (int i = 0; i < fileinfo.size(); i++)
                     {
                         String fileurl = fileinfo.get(i);
                         String restorefilename = fileurl.substring(fileurl.lastIndexOf("/") + 1);
 
                         if (restorefilename != null && restorefilename.length() > 0)
                         {
                             restorefilename = workflowManager.getMyTempFolder().getAbsolutePath() + File.separator + restorefilename;
                             if (restorefilename.indexOf(".dat") < 0)
                                 // this is a "top" level restore file. Lower level files are used to store serialized objects
                                 // and their file ext are ".dat"
                                 toplevelfn = restorefilename;
 
                             Log.info("Temp restore file name: " + restorefilename + " download from " + fileurl);
                             workflowManager.downloadFileFromUrl(restorefilename, fileurl);
                             if (goosename.equals("Multiple Array Viewer"))
                             {
                                 // MeV can have multiple anl files
                                 workflowManager.Report(WorkflowManager.InformationMessage, ("Load state for MeV from " + restorefilename));
                                 goose.loadState(restorefilename);
                             }
                         }
                     }
 
                     if (!goosename.equals("Multiple Array Viewer"))
                     {
                         Log.info("Load state from " + toplevelfn);
                         workflowManager.Report(WorkflowManager.InformationMessage, ("Load state from " + toplevelfn));
                         goose.loadState(toplevelfn);
                     }
                 }
             }
             catch (Exception e)
             {
                 Log.severe("Failed to process goose " + goosename + " " + e.getMessage());
                 workflowManager.Report(WorkflowManager.ErrorMessage, ("Failed to load state for goose " + goosename + " " + e.getMessage()));
             }
         }
     }
 }
 
 public class BossImpl extends UnicastRemoteObject implements Boss3 {
     public static final String SERVICE_NAME = "gaggle";
     private NewNameHelper nameHelper;
     private BossUI ui;
     private GooseManager gooseManager;
     private boolean isRecording = false;
     private HashMap<String, String> savedNodes = new HashMap<String, String>();
     private HashMap<String, HashMap<String, String>> dictNodes = new HashMap<String, HashMap<String, String>>();
     private HashMap<String, HashMap<String, String>> dictEdges = new HashMap<String, HashMap<String, String>>();
     private String startNode = null;
     private int edgeCount = 0;
     private int nodeCount = 0;
     private WorkflowManager workflowManager;
     private HashMap<String, String> applicationInfo = new HashMap<String, String>();
     private Sigar sigar;
     private Goose3 proxyGoose;
     private ProxyCallbackThread proxyCallbackThread;
     private String stateTempFolderName = "StateFiles";
     private Object syncObj = null;
     private String submitWorkflowResult = "";
 
     private static Logger Log = Logger.getLogger("Boss");
 
     public BossImpl(BossUI ui, String nameHelperURI)
         throws Exception {
         this.ui = ui;
         this.gooseManager = new GooseManager(ui);
         this.workflowManager = new WorkflowManager(this, this.gooseManager);
 
         if (nameHelperURI != null && nameHelperURI.length() > 0) {
             nameHelper = new NewNameHelper(nameHelperURI);
         }
 
         /*String os = System.getProperty("os.name");
         String arch = System.getProperty("os.arch");
         String javalibpath = System.getProperty("java.library.path");
         Log.info("SSIIGGAARRRRRRR ===== loading " + os + " " + arch + " " + javalibpath + " SSIIGGAARRRRR...");
 
         boolean libloaded = false;
         //System.setProperty( "java.library.path", "." );
         if(os.startsWith("Windows"))
         {
             Log.info("Windows OS detected...");
             if (arch.startsWith("amd64"))
             {
                 Log.info("Loading windows sigar-amd64-winnt.dll");
                 libloaded = loadSigarLibrary("sigar-amd64-winnt");
             }
             else
             {
                 Log.info("Loading windows sigar-x86-winnt.dll");
                 libloaded = loadSigarLibrary("sigar-x86-winnt");
             }
         }
         if (os.startsWith("Mac"))
         {
             Log.info("Mac OS detected");
             if (arch.equals("x86_64"))
             {
                 Log.info("Loading Mac libsigar-universal64-macosx.dylib");
                 libloaded = loadSigarLibrary("libsigar-universal64-macosx.dylib");
             }
             else if (arch.startsWith("i386") || arch.startsWith("x86"))
             {
                 Log.info("Loading Mac libsigar-universal-macosx.dylib");
                 libloaded = loadSigarLibrary("libsigar-universal-macosx.dylib");
             }
         }
         else if (os.startsWith("Linux"))
         {
             Log.info("Linux OS detected");
             if (arch.startsWith("x86"))
             {
                 Log.info("Loading linux libsigar-x86-linux.so");
                 libloaded = loadSigarLibrary("libsigar-x86-linux");
             }
             else if (arch.startsWith("amd64"))
             {
                 Log.info("Loading linux libsigar-x64-linux.so");
                 libloaded = loadSigarLibrary("libsigar-x64-linux");
             }
         }
         if (libloaded) {
             try
             {
                 Log.info("Now loading SIGAR...");
                 System.setProperty("org.hyperic.sigar.path", "-");
                 //System.setProperty( "java.library.path", "." );
                 Field fieldSysPath = ClassLoader.class.getDeclaredField( "sys_paths" );
                 fieldSysPath.setAccessible( true );
                 fieldSysPath.set( null, null );
                 sigar = new Sigar();
             }
             catch (Exception e)
             {
                 Log.severe("Failed to load SIGAR class: " + e.getMessage());
             }
         }
         */
 
 
         proxyCallbackThread = new ProxyCallbackThread(this, null);
         proxyCallbackThread.start();
     }
 
     private void loadJarLib(InputStream in, String libName) throws IOException {
         //InputStream in = MyClass.class.getResourceAsStream(name);
         Log.info("Load " + libName + " from inputstream");
         if (in != null && libName != null)
         {
             byte[] buffer = new byte[1024];
             int read = -1;
             File temp = File.createTempFile(libName, "");
             Log.info("Created temp file: " + temp.getAbsolutePath());
 
             FileOutputStream fos = new FileOutputStream(temp);
             while((read = in.read(buffer)) != -1) {
                 fos.write(buffer, 0, read);
             }
             fos.close();
             in.close();
 
             Log.info("Loading library...");
             System.load(temp.getAbsolutePath());
         }
     }
 
     private boolean loadSigarLibrary(String libName)
     {
         if (libName != null)
         {
             try
             {
                 System.loadLibrary(libName);
                 return true;
             }
             catch (UnsatisfiedLinkError ule)
             {
                 Log.severe("Cannot find library: " + ule.getMessage());
                 try
                 {
                     Log.info("Trying to load " + libName + "using getContextClassLoader().loadClass");
                     Thread.currentThread().getContextClassLoader().loadClass(libName);
                     return true;
                 }
                 catch (Exception e0)
                 {
                     Log.severe("Failed to load " + libName + " using getContextClassLoader().loadClass " + e0.getMessage());
                 }
 
                 try
                 {
                     Log.info("Trying to load " + libName + "using getClass().getClassLoader.getResourceAsStream");
                     InputStream ins0 = this.getClass().getClassLoader().getResourceAsStream(libName);
                     if (ins0 != null)
                     {
                         Log.info("Successfully loaded " + libName);
                         loadJarLib(ins0, libName);
                         return true;
                     }
                     else
                     {
                         Log.info("Trying to load " + libName + "using getClass().getResourceAsStream");
                         InputStream ins1 = this.getClass().getResourceAsStream(libName);
                         if (ins1 != null)
                         {
                             Log.info("Successfully loaded " + libName);
 
                             return true;
                         }
                         else
                             Log.warning("Failed to load " + libName);
                     }
                 }
                 catch (Exception ex)
                 {
                     Log.severe("Failed to load " + libName + " using getResourceAsStream " + ex.getMessage());
                 }
             }
             catch (Exception e)
             {
                 Log.severe("Failed to load " + e.getMessage());
             }
         }
         return false;
     }
 
     public void addMessage(String type, String message)
     {
         ProxyGooseMessage msg = new ProxyGooseMessage(type, message);
         this.proxyCallbackThread.AddMessage(msg);
     }
 
     public NewNameHelper getNameHelper() { return nameHelper; }
 
     public String getAppInfo(String appName)
     {
         Log.info("???? Getting executable for " + appName);
         for (String key : applicationInfo.keySet())
         {
             if (key.toLowerCase().equals(appName.toLowerCase()))
                 return applicationInfo.get(key);
         }
 
         for (String key : applicationInfo.keySet())
         {
             if (key.toLowerCase().indexOf(appName.toLowerCase()) >= 0)
                 return applicationInfo.get(key);
         }
         return null;
     }
 
     public void bind() throws Exception {
         //if (System.getSecurityManager() == null) {
         //    System.setSecurityManager(new SecurityManager());
         //}
 
         LocateRegistry.createRegistry(1099);
         Naming.rebind(SERVICE_NAME, this);
         Log.info("Boss Service bound");
     }
     public void unbind() throws Exception {
         Naming.unbind(SERVICE_NAME);
         Log.info("Boss Service unboudnd");
     }
 
     // ***** Goose Management *****
     public Goose getGoose(String name) { return gooseManager.getGoose(name); }
     public String[] getGooseNames() { return gooseManager.getGooseNames(); }
     public String[] getListeningGooseNames() { return ui.getListeningGeese(); }
     public String renameGoose(String oldName, String proposedName) {
         return gooseManager.renameGoose(oldName, proposedName);
     }
     public String renameGooseDirectly(String oldName,
                                       String proposedName) throws RemoteException {
         return gooseManager.renameGooseDirectly(oldName, proposedName);
     }
     public String register(Goose goose) throws RemoteException {
         Log.info("BossImpl.register(Goose)");
         return gooseManager.register(goose);
     }
     public String register(JSONGoose goose) throws RemoteException {
         Log.info("BossImpl.register(JSONGoose)");
         return gooseManager.register(goose);
     }
     public String register(DeafGoose deafGoose) { return ""; }
     public void unregister(String gooseName) { gooseManager.unregister(gooseName); }
     public void unregisterIdleGeeseAndUpdate() {
         gooseManager.unregisterIdleGeeseAndUpdate();
     }
     public void setRecording(boolean recording) { isRecording = recording; }
 
     // ***** Broadcasting *****
     public void broadcastNamelist(final String sourceGoose, final String targetGoose,
                                   final Namelist nameList) {
         Runnable broadcastTask = new Runnable()
         {
             public void run()
             {
                 ui.broadcastToPlugins(nameList.getNames());
 
                 String[] gooseNames;
                 if (targetGoose == null || targetGoose.equalsIgnoreCase("boss") ||
                         targetGoose.equalsIgnoreCase("all")) {
                     gooseNames = ui.getListeningGeese();
                 } else {
                     gooseNames = new String[]{targetGoose};
                 }
 
                 for (int i = 0; i < gooseNames.length; i++) {
                     String gooseName = gooseNames[i];
                    if (gooseName.equals(sourceGoose)) continue;
                     Goose goose = getGoose(gooseName);
                     if (goose == null) continue;
 
                     try {
                         if (isRecording)
                             recordAction(sourceGoose, gooseName, nameList, -1, null, null, null);
                         goose.handleNameList(sourceGoose, nameList);
                     } catch (Exception ex0) {
                         Log.severe("error in select request to " + gooseName + ": " +
                                 ex0.getMessage());
                         ex0.printStackTrace();
                     }
                 }
             }
         };
         ((GuiBoss)ui).invokeLater2(broadcastTask);
     }
 
     public void broadcastMatrix(final String sourceGoose, final String targetGoose,
                                 final DataMatrix matrix) {
         Runnable broadcastTask = new Runnable()
         {
             public void run()
             {
                 ui.broadcastToPlugins(matrix.getRowTitles());
 
                 String[] gooseNames;
                 if (targetGoose == null || targetGoose.equalsIgnoreCase("boss") ||
                     targetGoose.equalsIgnoreCase("all")) {
                     gooseNames = ui.getListeningGeese();
                 } else {
                     gooseNames = new String[]{targetGoose};
                 }
 
                 for (int i = 0; i < gooseNames.length; i++) {
                     String gooseName = gooseNames[i];
                    if (gooseName.equals(sourceGoose)) continue;
                     Goose goose = getGoose(gooseName);
                     if (goose == null) continue;
                     try {
                         if (isRecording)
                             recordAction(sourceGoose, gooseName, matrix, -1, null, null, null);
                         goose.handleMatrix(sourceGoose, matrix);
                     } catch (Exception ex0) {
                         Log.severe("error in handleMatrix request to " + gooseName + ": " +
                                    ex0.getMessage());
                         ex0.printStackTrace();
                     }
                 }
             }
         };
 
         ((GuiBoss)ui).invokeLater2(broadcastTask);
     }
 
     public void broadcastTuple(final String sourceGoose, final String targetGoose,
                                final GaggleTuple gaggleTuple) {
 
         Runnable broadcastTask = new Runnable()
         {
             public void run()
             {
                 String[] gooseNames;
                 if (targetGoose == null || targetGoose.equalsIgnoreCase("boss") ||
                     targetGoose.equalsIgnoreCase("all")) {
                     gooseNames = ui.getListeningGeese();
                 } else {
                     gooseNames = new String[]{targetGoose};
                 }
                 for (int i = 0; i < gooseNames.length; i++) {
                     String gooseName = gooseNames[i];
                    if (gooseName.equals(sourceGoose)) continue;
                     Goose goose = getGoose(gooseName);
                     if (goose == null) continue;
                     try {
                         System.out.println("broadcastTuple to " + gooseName);
                         if (isRecording)
                             recordAction(sourceGoose, gooseName, gaggleTuple, -1, null, null, null);
                         goose.handleTuple(sourceGoose, gaggleTuple);
                     } catch (Exception ex0) {
                         Log.severe("error in broadcastTuple to " + gooseName + ": " +
                                    ex0.getMessage());
                         ex0.printStackTrace();
                     }
                 }
             }
         };
         ((GuiBoss)ui).invokeLater2(broadcastTask);
     }
 
     public void broadcastCluster(final String sourceGoose, final String targetGoose,
                                  final Cluster cluster) {
         Runnable broadcastTask = new Runnable()
         {
             public void run()
             {
                 ui.broadcastToPlugins(cluster.getRowNames());
 
                 String[] gooseNames;
                 if (targetGoose == null || targetGoose.equalsIgnoreCase("boss") ||
                     targetGoose.equalsIgnoreCase("all")) {
                     gooseNames = ui.getListeningGeese();
                 } else {
                     gooseNames = new String[]{targetGoose};
                 }
                 for (int i = 0; i < gooseNames.length; i++) {
                     String gooseName = gooseNames[i];
                    if (gooseName.equals(sourceGoose)) continue;
                     if (!ui.isListening(gooseName)) continue;
                     Goose goose = getGoose(gooseName);
                     if (goose == null) continue;
                     try {
                         Log.info("Check recording flag " + isRecording);
                         if (isRecording)
                         {
                             // Record the action
                             recordAction(sourceGoose, gooseName, cluster, -1, null, null, null);
                         }
                         goose.handleCluster(sourceGoose, cluster);
                     } catch (Exception ex0) {
                         Log.severe("error in broadcastCluster () to " + gooseName + ": " +
                                    ex0.getMessage());
                         ex0.printStackTrace();
                     }
                 }
             }
         };
         ((GuiBoss)ui).invokeLater2(broadcastTask);
     }
 
     public void broadcastNetwork(final String sourceGoose, final String targetGoose,
                                  final Network network) {
         Runnable broadcastTask = new Runnable()
         {
             public void run()
             {
                 String[] gooseNames;
                 if (targetGoose == null || targetGoose.equalsIgnoreCase("boss") ||
                     targetGoose.equalsIgnoreCase("all")) {
                     gooseNames = ui.getListeningGeese();
                 } else {
                     gooseNames = new String[]{targetGoose};
                 }
 
                 for (int i = 0; i < gooseNames.length; i++) {
                     String gooseName = gooseNames[i];
                    if (gooseName.equals(sourceGoose)) continue;
                     Goose goose = getGoose(gooseName);
                     if (goose == null) continue;
                     try {
                         if (isRecording)
                         {
                             // Record the action
                             recordAction(sourceGoose, gooseName, network, -1, null, null, null);
                             //this.proxyGoose.handleWorkflowInformation("Recording", ("Network;" + sourceGoose + ";" + gooseName));
                         }
                         goose.handleNetwork(sourceGoose, network);
                     } catch (Exception ex0) {
                         System.err.println("error in broadcastNetwork () to " + gooseName + ": " +
                                            ex0.getMessage());
                         ex0.printStackTrace();
                     }
                 }
             }
         };
         ((GuiBoss)ui).invokeLater2(broadcastTask);
     }
 
     private String processWorkflow(Goose3 proxyGoose, String jsonWorkflow, Object syncObj)
     {
         workflowManager.Report(WorkflowManager.InformationMessage, ("Boss received JSON workflow string " + jsonWorkflow));
         JSONReader jsonReader = new JSONReader();
         submitWorkflowResult = "";
         try {
             Workflow w = jsonReader.createWorkflowFromJSONString(jsonWorkflow);
 
             // Now we hand over to the manager, which will spawn a thread to process the workflow
             workflowManager.SubmitWorkflow(proxyGoose, w);
             JSONObject json = new JSONObject();
 
             if (!w.getIsReset())
             {
                 // This is the submission of a workflow (not a resetting command)
                 HashMap<String, String> nodeInfoMap = w.getNodeInfoMap();
                 Log.info("Key set size: " + nodeInfoMap.keySet().size());
 
                 for (String key : nodeInfoMap.keySet())
                 {
                     // Key is the ID of the component
                     String goosename = nodeInfoMap.get(key);
                     String exepath = getAppInfo(goosename);
                     Log.info("Path for " + goosename + ": " + exepath);
                     if (exepath != null)
                     {
                         Log.info("Exec path for " + key + ": " + exepath);
                         nodeInfoMap.put(key, exepath);
                     }
                     else {
                         Log.info("Removing " + key);
                         nodeInfoMap.put(key, "");
                     }
                 }
 
                 Log.info("Generating goose json string...");
                 json.putAll(nodeInfoMap);
                 submitWorkflowResult = json.toString();
             }
 
             Log.info("Workflow goose json string: " + json.toString());
             if (syncObj != null)
             {
                 Log.info("Releasing syncObj...");
             }
         }
         catch (Exception e)
         {
             Log.severe("Failed to generate goose json string " + e.getMessage());
             workflowManager.Report(WorkflowManager.ErrorMessage, "Failed to parse workflow json string " + e.getMessage());
         }
         finally
         {
             if (syncObj != null)
             {
                 synchronized (syncObj) {
                     workflowManager.Report(WorkflowManager.InformationMessage, "Workflow Submit Sync Object released.");
                     syncObj.notify();
                 }
             }
             return submitWorkflowResult;
         }
     }
 
     /**
      * Accepts a workflow (usually from a Proxy Applet, but we do not assume so)
      * @param proxyGoose: A goose that communicates workflow related info with the boss.
      *               Typically it is the ProxyApplet goose.
      * @param jsonWorkflow: a workflow in JSON format submitted by goose
      */
     public String submitWorkflow(final Goose3 proxyGoose, final String jsonWorkflow)
     {
         if (jsonWorkflow != null && jsonWorkflow.length() > 0)
         {
             this.proxyGoose = proxyGoose;
             if (this.proxyCallbackThread != null) {
                 this.proxyCallbackThread.setProxyGoose(proxyGoose);
             }
 
             Log.info("JSON workflow string: " + jsonWorkflow);
             AppContext appContext = AppContext.getAppContext();
             if (appContext == null)
             {
                 workflowManager.Report(WorkflowManager.InformationMessage, "Submitting workflow to handle with valid appContext");
                 syncObj = new Object();
                 Runnable workflowTask = new Runnable() {
                     public void run() {
                          processWorkflow(proxyGoose, jsonWorkflow, syncObj);
                     }
                 };
 
                 GuiBoss guiBoss = (GuiBoss)this.ui;
                 guiBoss.invokeLater2(workflowTask);
 
                 try
                 {
                     synchronized (syncObj) {
                         syncObj.wait();
                         Log.info("Submit workflow result " + submitWorkflowResult);
                         return submitWorkflowResult;
                     }
                 }
                 catch (Exception e)
                 {
                     Log.warning("Failed to wait on syncObj " + e.getMessage());
                     return null;
                 }
             }
             else
             {
                 workflowManager.Report(WorkflowManager.InformationMessage, "Processing workflow with proper appContext");
                 return processWorkflow(proxyGoose, jsonWorkflow, null);
             }
         }
         return null;
     }
 
 
     /**
      * Handles workflow action received from components
      * @param action A workflow action needs to be carried out.
      */
     public void handleWorkflowAction(WorkflowAction action)
     {
         System.out.println("Processing workflow action: " + action.getSource().getName());
         this.workflowManager.HandleWorkflowAction(action);
     }
 
 
     private String generateRecordedWorkflow()
     {
         JSONObject jsonObj = new JSONObject();
         HashMap<String, Object> finalObj = new HashMap<String, Object>();
         finalObj.put("nodes", this.dictNodes);
         finalObj.put("edges", this.dictEdges);
         finalObj.put("id", "");
         finalObj.put("name", "");
         finalObj.put("desc", "");
         finalObj.put("startNode", this.startNode);
         jsonObj.putAll(finalObj);
         Log.info("Generated workflow json string: " + jsonObj.toString());
         return jsonObj.toString();
     }
 
     // Some goose's name is appName + data name (e.g., cygoose), we extract the appName
     private String processGooseName(String gooseName)
     {
         if (gooseName != null)
         {
             String[] splitted = gooseName.split(";");
             if (splitted != null)
                 return splitted[0];
         }
         return null;
     }
 
     /**
      * Start recording a workflow
      * @return
      */
     public UUID startRecordingWorkflow()
     {
         if (isRecording)
             return null;
         isRecording = true;
 
         // Create an array for recording the actions
         UUID rid = UUID.randomUUID();
         this.dictNodes.clear();
         this.dictEdges.clear();
         this.savedNodes.clear();
         this.edgeCount = 0;
         this.nodeCount = 0;
         // Inform all the goose
         return rid;
     }
 
     /**
      * Stop recording workflow
      * @return
      */
     public String terminateRecordingWorkflow(UUID rid)
     {
         isRecording = false;
         return generateRecordedWorkflow();
     }
 
     public String pauseRecordingWorkflow(UUID rid)
     {
         this.isRecording = false;
         return generateRecordedWorkflow();
     }
 
     public void resumeRecordingWorkflow(UUID rid)
     {
         this.isRecording = true;
     }
 
     private String getGooseWorkflowComponentID(Goose goose, String gooseName)
     {
         if (goose instanceof Goose3)
         {
             try {
                 String result = null;
                 GaggleGooseInfo gooseInfo = ((Goose3)goose).getGooseInfo();
                 Log.info("GooseInfo: " + gooseInfo);
                 if (gooseInfo != null){
                     result = gooseInfo.getWorkflowComponentID();
                     Log.info(gooseName + " component ID: " + result);
                 }
                 if (result == null || result.length() == 0)
                 {
                     result = gooseName;
                 }
                 return result;
             }
             catch (Exception e) {
                 Log.warning("Failed to get goose info for " + gooseName + " " + e.getMessage());
             }
         }
         return gooseName;
     }
 
     private void sendBroadcastToProxyGoose(Goose srcGoose, String sourceGoose, Goose trgtGoose, String targetGoose, String dataType)
     {
         if (this.proxyGoose != null)
         {
             String srcGooseComponentID = getGooseWorkflowComponentID(srcGoose, sourceGoose);
             String trgtGooseComponentID = getGooseWorkflowComponentID(trgtGoose, targetGoose);
             String msg = dataType + ";" + srcGooseComponentID + ";" + trgtGooseComponentID;
             Log.info("Sending goose info back to proxy goose. " + msg);
             try {
                 //this.proxyGoose.handleWorkflowInformation("Recording", msg);
                 ProxyGooseMessage m = new ProxyGooseMessage("Recording", msg);
                 this.proxyCallbackThread.AddMessage(m);
             }
             catch (Exception e)
             {
                 Log.severe("Failed to connect to proxy goose " + e.getMessage());
             }
         }
     }
 
 
     /**
      * Record a broadcast action. The API can be used to update source goose and target goose parameters
      * as well as edge parameters
      * @param sourceGoose
      * @param targetGoose
      * @param data
      * @param edgeIndex: The index of the edge to be updated, -1 if it's a new edge
      * @param sourceParams
      * @param targetParams
      * @param edgeParams
      */
     public void recordAction(String sourceGoose, String targetGoose,
                              Object data,
                              int edgeIndex,
                              HashMap<String, String> sourceParams,
                              HashMap<String, String> targetParams,
                              HashMap<String, String> edgeParams
                              )
     {
         Log.info("***** Recording action from " + sourceGoose + "********");
         if (sourceGoose != null && targetGoose == null && data != null)
         {
             // Application information report
             /*try
             {
                 Log.info("Goose " + sourceGoose + " query: " + (String)data);
                 ProcessFinder procFinder = new ProcessFinder(sigar);
                 long[] pids = procFinder.find((String)data);
                 if (pids != null && pids.length > 0)
                 {
                     Log.info("Getting info for process " + pids[0]);
                     ProcExe procExe = new ProcExe();
                     procExe.gather(sigar, pids[0]);
                     String workdir = procExe.getCwd();
                     String exename = procExe.getName();
                     Log.info("Work dir: " + workdir + " Executable: " + exename);
                     this.applicationInfo.put(sourceGoose, exename);
                 }
                 else
                     Log.warning("Couldn't find the process.");
             }
             catch (Exception e0)
             {
                 Log.severe("Failed to get path of the process for " + sourceGoose + " " + e0.getMessage());
             } */
 
         }
         else if (isRecording)
         {
             Log.info("Recording source: " + sourceGoose + " target:" + targetGoose + " data: " + data.toString());
 
             String sourceGooseName = processGooseName(sourceGoose);
             String targetGooseName = processGooseName(targetGoose);
 
            Goose srcGoose = getGoose(sourceGoose);
            Goose trgtGoose = getGoose(targetGoose);
 
             // Add nodes to the nodes dict
             if (this.dictNodes.isEmpty())
             {
                 // This is the starting node
                 if (sourceGoose != null)
                 this.startNode = sourceGooseName;
             }
 
             String sourceid = "";
             String targetid = "";
             if (sourceGooseName != null)
             {
                 HashMap<String, String> node = null;
                 if (!this.savedNodes.containsKey(sourceGooseName))
                 {
                     node = new HashMap<String, String>();
                     node.put(JSONConstants.WORKFLOW_ID, String.valueOf(nodeCount));
                     node.put(JSONConstants.WORKFLOW_NAME, sourceGooseName);
                     this.dictNodes.put(String.valueOf(this.nodeCount), node);
                     this.savedNodes.put(sourceGooseName, String.valueOf(nodeCount));
                     sourceid = String.valueOf(nodeCount);
                     nodeCount++;
                 }
                 else {
                     sourceid = savedNodes.get(sourceGooseName);
                     node = this.dictNodes.get(sourceid);
                 }
                 if (sourceParams != null && node != null)
                 {
                     for (String key: sourceParams.keySet())
                     {
                         String value = sourceParams.get(key);
                         if (value != null && value.length() > 0)
                         {
                             String nodevalue = "";
                             if (node.containsKey(key))
                                 // If the parameter already exists, we concatenate it
                                 // with the new value. This is especially important for
                                 // some parameters such as subactions ( A node can accept multiple
                                 // subactions). For example, users might want to pass the data to
                                 // Firegoose and trigger multiple webhandlers. Concatenating
                                 // subactions achieve this purpose.
                                 nodevalue = (node.get(key) + ";");
                             nodevalue += value;
                             node.put(key, nodevalue);
                         }
                     }
                 }
 
                 // Pass it to workflow manager
                 //workflowManager.addNode(srcGoose);
             }
 
             if (targetGoose != null)
             {
                 HashMap<String, String> node = null;
                 if (!this.savedNodes.containsKey(targetGooseName))
                 {
                     node = new HashMap<String, String>();
                     node.put(JSONConstants.WORKFLOW_ID, String.valueOf(nodeCount));
                     node.put(JSONConstants.WORKFLOW_NAME, targetGooseName);
                     this.dictNodes.put(String.valueOf(this.nodeCount), node);
                     this.savedNodes.put(targetGooseName, String.valueOf(nodeCount));
                     targetid = String.valueOf(nodeCount);
                     nodeCount++;
                 }
                 else {
                     targetid = savedNodes.get(targetGooseName);
                     node = this.dictNodes.get(targetid);
                 }
                 if (targetParams != null && node != null)
                 {
                     for (String key: targetParams.keySet())
                     {
                         node.put(key, targetParams.get(key));
                     }
                 }
             }
 
 
             // Add to the edge dict
             if (sourceGoose != null && targetGoose != null)
             {
                 Log.info("Source node id: " + sourceid + " target node id: " + targetid);
                 HashMap<String, String> edge = null;
                 if (edgeIndex >= 0)
                     edge = this.dictEdges.get(String.valueOf(edgeIndex));
                 else
                     edge = new HashMap<String, String>();
                 if (edge != null)
                 {
                     edge.put(JSONConstants.WORKFLOW_EDGE_SOURCEID, sourceid);
                     edge.put(JSONConstants.WORKFLOW_EDGE_TARGETID, targetid);
 
                     String dataType = "Data";
                     if (data instanceof DataMatrix)
                         dataType = "Matrix";
                     else if (data instanceof GaggleTuple)
                         dataType = "Tuple";
                     else if (data instanceof Network)
                         dataType = "Network";
                     else if (data instanceof Cluster)
                         dataType = "Cluster";
                     edge.put(JSONConstants.WORKFLOW_EDGE_DATATYPE, dataType);
                     Log.info("Data type: " + dataType);
                     edge.put(JSONConstants.WORKFLOW_EDGE_PARALLELTYPE, "true");
 
                     if (edgeParams != null)
                     {
                         Log.info("Adding edge params...");
                         for (String key: edgeParams.keySet())
                         {
                             edge.put(key, edgeParams.get(key));
                         }
                     }
                     if (edgeIndex >= 0)
                     {
                         this.dictEdges.put(String.valueOf(edgeIndex), edge);
                     }
                     else
                     {
                         this.dictEdges.put(String.valueOf(this.edgeCount), edge);
                         this.edgeCount++;
                     }
 
                     sendBroadcastToProxyGoose(srcGoose, sourceGoose, trgtGoose, targetGoose, dataType);
                 }
             }
         }
     }
 
 
     class SaveStateThread extends Thread
     {
         Goose3 proxyGoose;
         String userid;
         String name;
         String desc;
         String filePrefix;
 
         public SaveStateThread(Goose3 proxyGoose, String userid, String name, String desc, String filePrefix)
         {
             this.proxyGoose = proxyGoose;
             this.userid = userid;
             this.name = name;
             this.desc = desc;
             this.filePrefix = filePrefix;
         }
 
         public void run()
         {
             String[] gooseNames = ui.getListeningGeese();
             if (gooseNames != null && gooseNames.length > 0)
             {
                 String tempDir = System.getProperty("java.io.tmpdir");
                 Log.info("Temp dir: " + tempDir);
                 if (tempDir.toLowerCase().startsWith("/var/folders/"))
                     tempDir = "/tmp/";
                 tempDir += ("Gaggle" + File.separator + stateTempFolderName);
                 Log.info("Upload file path: " + tempDir);
                 File temp = new File(tempDir);
                 if (!temp.exists())
                 {
                     Log.info("Create temp dir: " + tempDir);
                     try
                     {
                         temp.mkdirs();
                     }
                     catch (Exception fe)
                     {
                         Log.warning("Failed to create temp directory " + fe.getMessage());
                     }
                 }
 
                 // if fileprefix is empty, we use current datetime
                 if (filePrefix == null || filePrefix.length() == 0)
                 {
                     SimpleDateFormat df = new SimpleDateFormat("MMddyyyy-HHmmss");
                     Date date = new Date();
                     filePrefix = df.format(date);
                 }
 
                 for (int i = 0; i < gooseNames.length; i++) {
                     String gooseName = gooseNames[i];
                     Log.info("Getting goose " + gooseName);
                     SuperGoose goose = gooseManager.getGoose(gooseName);
                     if (goose == null || gooseName.contains("ProxyAppletGoose"))
                         continue;
                     try {
                         Log.info("Call goose " + gooseName + " to save state with prefix " + filePrefix);
                         workflowManager.Report(WorkflowManager.InformationMessage, ("Call goose " + gooseName + " to save state with prefix " + filePrefix));
                         goose.saveState(tempDir, filePrefix);
                     }
                     catch (Exception e) {
                         Log.severe("Failed to save state for goose " + gooseName + " " + e.getMessage());
                         workflowManager.Report(WorkflowManager.ErrorMessage, ("Failed to save state for goose " + gooseName + " " + e.getMessage()));
                     }
                 }
 
                 // Now we upload the goose state files to server
                 URL url = null;
                 try {
                     String server = System.getProperty("server");
                     Log.info("Web server: " + server);
                     if (server == null || server.length() == 0)
                         server = "http://networks.systemsbiology.net";
                     url = new URL((server + "/workflow/savestate"));
                     //url = new URL("http://localhost:8000/workflow/savereport/");
                 } catch (MalformedURLException ex) {
                     Log.warning("Malformed URL " + ex.getMessage());
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
                     workflowManager.Report(WorkflowManager.ErrorMessage, ("Failed to create connection to " + url.toString()));
                 }
 
                 try
                 {
                     ClientHttpRequest httpRequest = new ClientHttpRequest(urlConn);
                     InputStream responseStream = null;
 
                     if (temp.exists())
                     {
                         // send the component name
                         httpRequest.setParameter("userid", userid);
                         httpRequest.setParameter("name", name);
                         httpRequest.setParameter("desc", desc);
                         Log.info("Searching for state files with prefix " + filePrefix);
                         //final String fpre = filePrefix;
 
                         Thread.sleep(25000); // wait for geese to save the state files
                         String filenames[] = temp.list();
                         /*(new FilenameFilter() {
                            @Override
                            public boolean accept(File dir, String name) {
                                if (name.startsWith(fpre))
                                    return true;
                                return false;  //To change body of implemented methods use File | Settings | File Templates.
                            }
                        }); */
 
                         ArrayList<File> statefiles = new ArrayList<File>();
                         for (String fn : filenames) {
                             Log.info("Processing " + fn);
                             if (fn.startsWith(filePrefix)) {
                                 Log.info("Adding file " + fn + " to httprequest");
                                 File f = new File(tempDir + File.separator + fn);
                                 statefiles.add(f);
                                 //FileInputStream is = new FileInputStream((String)value);
                                 httpRequest.setParameter(fn, f);
                             }
                         }
                         responseStream = httpRequest.post();
                         BufferedReader reader = new BufferedReader(new InputStreamReader(responseStream));
                         StringBuilder builder = new StringBuilder();
                         String line;
                         while ((line = reader.readLine()) != null)
                         {
                             builder.append(line);
                         }
                         String jsonresponse = builder.toString();
                         Log.info("Save state json response " + jsonresponse);
 
                         // Remove the temp state files
                         for (int i = 0; i < statefiles.size(); i++)
                         {
                             File f = statefiles.get(i);
                             try {
                                 if (f != null)
                                     f.delete();
                             }
                             catch (Exception e2) {
                                 Log.warning("Failed to delete temp state file " + e2.getMessage());
                             }
                         }
 
                         // Parse the JSON string
                         if (proxyGoose != null)
                         {
                             Log.info("Save state return json: " + jsonresponse);
                             ProxyGooseMessage m = new ProxyGooseMessage("SaveStateResponse", jsonresponse);
                             proxyCallbackThread.setProxyGoose(proxyGoose);
                             proxyCallbackThread.AddMessage(m);
                         }
                     }
                 }
                 catch (Exception e)
                 {
                     Log.severe("Failed to upload state file to server " + e.getMessage());
                 }
             }
         }
     }
 
     public void saveState(final Goose3 proxyGoose, final String userid, final String name,
                           final String desc, final String filePrefix)
     {
         Log.info("Saving state...");
 
         if (AppContext.getAppContext() == null) {
             Runnable saveStateTask = new Runnable() {
                 public void run() {
                     SaveStateThread sst = new SaveStateThread(proxyGoose, userid, name, desc, filePrefix);
                     sst.start();
                 }
             };
 
             GuiBoss guiBoss = (GuiBoss)this.ui;
             guiBoss.invokeLater2(saveStateTask);
 
         }
         else {
             SaveStateThread sst = new SaveStateThread(proxyGoose, userid, name, desc, filePrefix);
             sst.start();
         }
     }
 
 
     private void handleLoadState(String stateid)
     {
         URL url = null;
         try {
             String server = System.getProperty("server");
             Log.info("Web server: " + server);
             if (server == null || server.length() == 0)
                 server = "http://networks.systemsbiology.net";
             url = new URL((server + "/workflow/getstateinfo/" + stateid + "/"));
             workflowManager.Report(WorkflowManager.InformationMessage, ("Load state url " + url.toString()));
             //url = new URL("http://localhost:8000/workflow/savereport/");
         } catch (MalformedURLException ex) {
             Log.warning("Malformed URL " + ex.getMessage());
         }
 
         HttpURLConnection urlConn = null;
         try {
             // URL connection channel.
             urlConn = (HttpURLConnection) url.openConnection();
             ClientHttpRequest httpRequest = new ClientHttpRequest(urlConn);
 
             InputStream responseStream = httpRequest.post();
             byte data[] = new byte[1024];
             int count;
             BufferedReader reader = new BufferedReader(new InputStreamReader(responseStream));
             StringBuilder builder = new StringBuilder();
             String line;
             while ((line = reader.readLine()) != null)
             {
                 Log.info("Got line: " + line);
                 builder.append(line);
             }
             String jsonresponse = builder.toString();
             workflowManager.Report(WorkflowManager.InformationMessage, ("Received state json string: " + jsonresponse));
 
             // Parse the JSON string
             Log.info("Parsing JSON state info: " + jsonresponse);
             JSONObject jsonobj = JSONObject.fromObject(jsonresponse);
             Iterator iter = jsonobj.keys();
             while (iter.hasNext())
             {
                 String key = (String)iter.next();
                 Log.info("Retrieving json key " + key);
                 JSONObject nodeJSONObj = jsonobj.getJSONObject(key);
                 if (nodeJSONObj != null)
                 {
                     String goosename = nodeJSONObj.getString("goosename");
                     String serviceurl = nodeJSONObj.getString("serviceurl");
                     int filecnt = Integer.parseInt(nodeJSONObj.getString("files"));
                     Log.info("Starting goose " + goosename + " to restore state service url: " + serviceurl + " with " + filecnt + " files");
                     workflowManager.Report(WorkflowManager.InformationMessage, ("Starting goose " + goosename + " to restore state service url: " + serviceurl + " with " + filecnt + " files"));
 
                     ArrayList<String> fileinfo = new ArrayList<String>();
                     JSONObject fileobj = nodeJSONObj.getJSONObject("fileobj");
                     for (int i = 0; i < filecnt; i++)
                     {
                         JSONObject fobj = fileobj.getJSONObject(Integer.toString(i));
                         String fileurl = fobj.getString("fileurl");
                         fileinfo.add(fileurl);
                         Log.info("Add " + fileurl + " for " + goosename);
                         workflowManager.Report(WorkflowManager.InformationMessage, ("Add " + fileurl + " for " + goosename));
                     }
 
                     // Put goose restore in threads
                     RestoreStateThread rst = new RestoreStateThread(workflowManager, goosename, serviceurl, fileinfo);
                     rst.start();
                 }
             }
 
         }
         catch (Exception e1)
         {
             Log.severe("Failed to load state " + e1.getMessage());
         }
     }
 
     public void loadState(final String stateid) throws RemoteException
     {
         // Get the state information
         if (AppContext.getAppContext() == null) {
             Runnable loadStateTask = new Runnable() {
                 public void run() {
                     handleLoadState(stateid);
                 }
             };
 
             GuiBoss guiBoss = (GuiBoss)this.ui;
             guiBoss.invokeLater2(loadStateTask);
 
         } else {
             handleLoadState(stateid);
         }
     }
 
     public void hide(String targetGoose) {
         String[] gooseNames;
         if (targetGoose == null) {
             gooseNames = ui.getListeningGeese();
         } else if (targetGoose.equalsIgnoreCase("boss")) {
             ui.hide();
             return;
         } else gooseNames = new String[]{targetGoose};
 
         for (int i = 0; i < gooseNames.length; i++) {
             Goose goose = getGoose(gooseNames[i]);
             if (goose == null) continue;
             try {
                 goose.doHide();
             } catch (Exception ex0) {
                 Log.severe("error in hide request to " + targetGoose + ": " +
                                    ex0.getMessage());
                 ex0.printStackTrace();
             }
         }
     }
 
     public void show(String gooseName) {
         if (gooseName.equalsIgnoreCase("boss")) ui.show();
         else {
             final Goose goose = getGoose(gooseName);
             if (goose == null) return;
 
             try {
                 goose.doShow();
             } catch (Exception ex0) {
                 Log.severe("error in show request to " + gooseName + ": " +
                            ex0.getMessage());
                 ex0.printStackTrace();
             }
         }
     }
 
     public void terminate(String gooseName) {
         Goose goose = getGoose(gooseName);
         if (goose == null) return;
         try {
             goose.doExit();
         } catch (java.rmi.UnmarshalException ignore0) {
             ignore0.printStackTrace();
         } catch (Exception ex1) {
             Log.severe("error in terminate request to " + gooseName + ": " + ex1.getMessage());
         }
     }
 
     public void broadcastJSON(String source, String target, String json) {
         String[] gooseNames;
         if (target == null || target.equalsIgnoreCase("boss") ||
             target.equalsIgnoreCase("all")) {
             gooseNames = ui.getListeningGeese();
         } else {
             gooseNames = new String[]{target};
         }
 
         for (int i = 0; i < gooseNames.length; i++) {
             String gooseName = gooseNames[i];
             if (gooseName.equals(source)) continue;
             SuperGoose goose = gooseManager.getGoose(gooseName);
             if (goose == null) continue;
             try {
                 goose.handleJSON(source, json);
             } catch (Exception ex0) {
                 System.err.println("error in broadcastJSON() to " + gooseName + ": " +
                                    ex0.getMessage());
                 ex0.printStackTrace();
             }
         }
     }
 
     public void broadcastTable(String source, String target, Table table) {
         throw new UnsupportedOperationException("TODO");
     }
 
 
 
 }
