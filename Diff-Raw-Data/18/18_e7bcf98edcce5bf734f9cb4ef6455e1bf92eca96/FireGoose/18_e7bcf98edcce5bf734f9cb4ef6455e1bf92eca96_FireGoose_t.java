 /*
  * Copyright (C) 2007 by Institute for Systems Biology,
  * Seattle, Washington, USA.  All rights reserved.
  *
  * This source code is distributed under the GNU Lesser
  * General Public License, the text of which is available at:
  *   http://www.gnu.org/copyleft/lesser.html
  */
 
 package org.systemsbiology.gaggle.geese.firegoose;
 
 import net.sf.json.JSONObject;
 import netscape.javascript.JSObject;
import org.systemsbiology.gaggle.core.Boss;
import org.systemsbiology.gaggle.core.Boss3;
import org.systemsbiology.gaggle.core.Goose3;
import org.systemsbiology.gaggle.core.GooseWorkflowManager;
 import org.systemsbiology.gaggle.core.datatypes.*;
 import org.systemsbiology.gaggle.geese.common.GaggleConnectionListener;
 import org.systemsbiology.gaggle.geese.common.GooseShutdownHook;
import org.systemsbiology.gaggle.geese.common.RmiGaggleConnector;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
 
 
 /**
  * The FireGoose class is the java side of the Firefox Gaggle
  * toolbar.
  *
  * @author cbare
  */
 
 
 public class FireGoose implements Goose3, GaggleConnectionListener {
     String activeGooseNames[] = new String[0];
     RmiGaggleConnector connector = new RmiGaggleConnector(this);
     final static String defaultGooseName = "Firegoose";
     String gooseName = defaultGooseName;
     Boss boss;
     Signal hasNewDataSignal = new Signal();
     Signal hasNewWorkflowDataSignal = new Signal();
     Signal hasTargetUpdateSignal = new Signal();
 
     String species = "unknown";
     String[] nameList;
     String size;
     String type = null;
     String workingDir = null;
     Tuple metadata;
 
     GooseWorkflowManager workflowManager = new GooseWorkflowManager();
 
     public FireGoose() {
         System.out.println("created Firegoose instance");
         connector.setAutoStartBoss(true);
         connector.addListener(this);
         //workingDir = System.getProperty("user.dir");
 
         // Send the application info to boss
         reportApplicationInfo();
 
         // this has no effect. Firefox probably doesn't wait for the JVM to shut down properly.
         new GooseShutdownHook(connector);
     }
 
     private void reportApplicationInfo()
     {
         try
         {
             String query = "Exe.Name.ct=Firefox";
             System.out.println("=====>Query string for process " + query);
             ((org.systemsbiology.gaggle.core.Boss3)boss).recordAction("Firegoose", null, query, -1, null, null, null);
         }
         catch (Exception e)
         {
             System.out.println("Failed to record app name " + e.getMessage());
         }
     }
 
     public String getSpecies() {
         return species;
     }
 
     public void setSpecies(String species) {
         this.species = species;
     }
 
     public String getWorkflowDataSpecies(String requestID)
     {
         return this.workflowManager.getSpecies(requestID);
     }
 
     public String[] getNameList() {
         return nameList;
     }
 
     public String[] getWorkflowDataNameList(String requestID)
     {
         System.out.println("Get workflow namelist");
         return this.workflowManager.getNameList(requestID);
     }
 
     public String getWorkflowDataSubAction(String requestID)
     {
         System.out.println("Get subaction for " + requestID);
         return this.workflowManager.getSubAction(requestID);
     }
 
     /**
      * Used to implement a FG_GaggleData object that represents the
      * broadcast from the Gaggle.
      * See FG_GaggleDataFromGoose in firegoose.js
      * @return a type
      */
     public String getType() {
 		return type;
 	}
 
     public String getWorkflowDataType(String requestID)
     {
         return this.workflowManager.getType(requestID);
     }
 
     public String getSize() {
     	return size;
     }
 
     public String getWorkflowDataSize(String requestID)
     {
         return this.workflowManager.getSize(requestID);
     }
 
     public String getWorkflowRequest()
     {
         return this.workflowManager.getCurrentRequest();
     }
 
     public void removeWorkflowRequest(String requestID)
     {
         if (requestID != null)
         {
             System.out.println("Remove " + requestID + " workflow requests");
             this.workflowManager.removeRequest(requestID);
         }
     }
 
     public WorkflowAction getWorkflowAction(String requestID)
     {
         return this.workflowManager.getWorkflowAction(requestID);
     }
 
     public void test(Object object) {
     	// this finally worked somehow:
     	// it's an example of calling into javascript from java
     	// this works w/ the apple MRJPlugin implementation of JSObject
     	// but not with the sun implementation found on windows.
     	if (object == null) {
     		System.out.println("Hey that tickles! It's a null!");
     	}
     	else {
     		System.out.println("I got a " + object.getClass().getName());
     		System.out.println("This object has a name: " + ((JSObject)object).getMember("name"));
     		((JSObject)object).call("test",new Object[] {});
     		System.out.println("did that do anything?");
     	}
     }
 
 
     /**
      * a hacky way to signal that we have received a new broadcast
      * from the Gaggle, so we don't have to keep updating. The idea is to
      * compare the return value with the value you got last time. If the
      * value has changed, we got a broadcast since last time you checked.
      * @return an integer that increases every time we get a broadcast.
      */
     public int checkNewDataSignal() {
     	return hasNewDataSignal.check();
     }
     
     public int checkTargetUpdateSignal() {
     	return hasTargetUpdateSignal.check();
     }
 
     public String[] getGooseNames() {
         List<String> results = new ArrayList<String>();
             for (String name : activeGooseNames) {
                 if (!this.gooseName.equals(name)) {
                     results.add(name);
                 }
             }
 
         return results.toArray(new String[0]);
     }
 
     public void broadcastNameList(String targetGoose, String name, String species, String[] names) {
         try {
             Namelist namelist = new Namelist();
             namelist.setName(name);
             namelist.setSpecies(species);
             namelist.setNames(names);
             boss.broadcastNamelist(gooseName, targetGoose, namelist);
         }
         catch (RemoteException e) {
         	System.out.println("FireGoose: rmi error calling boss.broadcastNamelist");
         }
         catch (Exception e) {
             System.out.println(e);        	
         }
     }
 
     public void broadcastNetwork(String targetGoose, Network network) {
         try {
             boss.broadcastNetwork(gooseName, targetGoose, network);
         }
         catch (RemoteException e) {
         	System.out.println("FireGoose: rmi error calling boss.broadcastNetwork");
             System.out.println(e);
         }
         catch (Exception e) {
             System.out.println(e);        	
         }
     }
 
     public void broadcastDataMatrix(String targetGoose, DataMatrix matrix) {
         try {
             boss.broadcastMatrix(gooseName, targetGoose, matrix);
         }
         catch (RemoteException e) {
         	System.out.println("FireGoose: rmi error calling boss.broadcastMatrix");
             System.out.println(e);
         }
         catch (Exception e) {
             System.out.println(e);        	
         }
     }
 
     public void broadcastMap(String targetGoose, String species, String name, HashMap<String, String> map) {
     	System.out.println("broadcastMap not implemented");
         /*try {
             boss.broadcast(gooseName, targetGoose, species, name, map);
         }
         catch (RemoteException e) {
             System.err.println("SampleGoose: rmi error calling boss.broadcast (map)");
             System.out.println(e);
         } */
     }
 
     public void broadcastCluster(String targetGoose, String species, String name, String [] rowNames, String [] columnNames) {
         try {
         	Cluster cluster = new Cluster(name, species, rowNames, columnNames);
             boss.broadcastCluster(gooseName, targetGoose, cluster);
         }
         catch (RemoteException e) {
             System.err.println("FireGoose: rmi error calling boss.broadcast (map)");
             System.out.println(e);
         }
     }
 
     public void showGoose(String gooseName) {
         try {
             boss.show(gooseName);
         }
         catch (RemoteException e) {
         	System.out.println("FireGoose: rmi error calling boss.show (gooseName)");
             System.out.println(e);
         }
         catch (Exception e) {
             System.out.println(e);
         }
     }
 
     public void hideGoose(String gooseName) {
         try {
             boss.hide(gooseName);
         }
         catch (RemoteException e) {
         	System.out.println("FireGoose: rmi error calling boss.hide (gooseName)");
             System.out.println(e);
         }
         catch (Exception e) {
             System.out.println(e);
         }
     }
 
 
     public void disconnectFromGaggle() {
         connector.disconnectFromGaggle(true);
     }
 
 
     public void setAutoStartBoss(boolean autoStartBoss) {
     	this.connector.setAutoStartBoss(autoStartBoss);
     }
     
     public boolean getAutoStartBoss() {
     	return this.connector.getAutoStartBoss();
     }
 
 
     // Goose methods ---------------------------------------------------------
 
     public void connectToGaggle() throws Exception {
     	try {
             System.out.println("connectToGaggle...");
 	    	if (!connector.isConnected()) {
 	    		gooseName = defaultGooseName;
 	    		connector.connectToGaggle();
 	    	}
     	}
     	catch (Exception e) {
     		System.out.println("Exception trying to connect to Boss:");
     		e.printStackTrace();
     	}
     }
 
     /**
      * Try to connect to Gaggle without autostarting Boss.
      */
     public void connectToGaggleIfAvailable() throws Exception {
     	boolean autostart = connector.getAutoStartBoss();
         System.out.println("ConnectToGaggleIfAvailable...");
 		try {
 			connector.setAutoStartBoss(false);
 			connector.connectToGaggle();
 		}
 		catch (Exception e) {
 			System.out.println("Firegoose tried and failed to connect to Gaggle Boss: " + e.getClass().getName() + ": " + e.getMessage() );
 		}
 		finally {
 			connector.setAutoStartBoss(autostart);
 		}
 
     }
 
     public void handleNameList(String sourceGooseName, Namelist namelist) throws RemoteException {
         this.species = namelist.getSpecies();
         this.nameList = namelist.getNames();
         this.type = "NameList";
         this.size = String.valueOf(nameList.length);
         System.out.println("incoming broadcast: " + type + "(" + size + ")");
         System.out.println("Current signal value: " + hasNewDataSignal.check());
         hasNewDataSignal.increment();
         System.out.println("New signal value: " + hasNewDataSignal.check());
     }
 
     public void handleMatrix(String sourceGooseName, DataMatrix simpleDataMatrix) throws RemoteException {
         //TODO
         System.out.println("incoming broadcast: DataMatrix");
     }
 
 
     public void handleTuple(String string, GaggleTuple gaggleTuple) throws RemoteException {
         //TODO
         System.out.println("incoming broadcast: gaggleTuple");
     }
 
     public void handleCluster(String sourceGooseName, Cluster cluster) throws RemoteException {
     	// we handle clusters by translating them to namelists
         this.species = cluster.getSpecies();
         this.nameList = cluster.getRowNames();
         this.type = "NameList";
         this.size = String.valueOf(nameList.length);
         hasNewDataSignal.increment();
         System.out.println("incoming broadcast: cluster translated to " + type + "(" + size + ")");
     }
 
     public void handleNetwork(String sourceGooseName, Network network) throws RemoteException {
         System.out.println("incoming broadcast: network");
     }
 
     public GaggleGooseInfo getGooseInfo() throws RemoteException
     {
         System.out.println("Firgoose retrieving goose info...");
         return this.workflowManager.getGooseInfo();
     }
 
     public void saveState(String directory, String filePrefix)
     {
 
     }
 
     public void loadState(String filename)
     {
 
     }
 
     // Received workflow request from another component
     // We store the action in the workflowManager and then call the corresponding handle functions
     // to store properties of the data
     public void handleWorkflowAction(org.systemsbiology.gaggle.core.datatypes.WorkflowAction workflowAction)
     {
         if (workflowAction != null)
         {
             System.out.println("Received workflow action request!!");
             this.workflowManager.addSession(workflowAction);
         }
     }
 
     public void handleWorkflowInformation(java.lang.String s, java.lang.String s1)
     {
 
     }
 
     public void handleTable(java.lang.String s, org.systemsbiology.gaggle.core.datatypes.Table table)
     {
 
     }
 
 
     // Submit a NameList to the workflow manager
     // names is a delimited string of all the names
     public void submitNameList(String requestID, int targetIndex, String name, String species, String names, String delimit)
     {
         System.out.println("Got Namelist..." + names + " " + delimit);
         try
         {
             Namelist namelist = new Namelist();
             namelist.setName(name);
             namelist.setSpecies(species);
             String[] splittedstrings = names.split(delimit);
             namelist.setNames(splittedstrings);
             this.workflowManager.addSessionTargetData(requestID, targetIndex, namelist);
             System.out.println("Added namelist to workflow manager " + requestID);
         }
         catch (Exception e)
         {
             System.out.println("Failed to submit Namelist to workflow: " + e.getMessage());
         }
     }
 
     public void submitNetwork(String requestID, int targetIndex, Network network) {
         this.workflowManager.addSessionTargetData(requestID, targetIndex, network);
         System.out.println("Added network to workflow manager " + requestID);
     }
 
     public void submitDataMatrix(String requestID, int targetIndex, DataMatrix matrix) {
         this.workflowManager.addSessionTargetData(requestID, targetIndex, matrix);
         System.out.println("Added Matrix to workflow manager " + requestID);
     }
 
     public void submitMap(String requestID, int targetIndex, String species, String name, HashMap<String, String> map) {
         System.out.println("Map not implemented");
 //        try {
 //            boss.broadcast(gooseName, targetGoose, species, name, map);
 //        }
 //        catch (RemoteException e) {
 //            System.err.println("SampleGoose: rmi error calling boss.broadcast (map)");
 //            System.out.println(e);
 //        }
     }
 
     public void submitCluster(String requestID, int targetIndex, String species, String name,
                               String [] rowNames, String [] columnNames)
     {
         Cluster cluster = new Cluster(name, species, rowNames, columnNames);
         this.workflowManager.addSessionTargetData(requestID, targetIndex, cluster);
         System.out.println("Added cluster to workflow manager " + requestID);
     }
 
     // All the data are ready, we submit the response to the boss
     public boolean CompleteWorkflowAction(String requestID)
     {
         return this.workflowManager.CompleteWorkflowAction(boss, requestID);
     }
 
     public boolean AllDataCommittedForRequest(String requestID)
     {
         if (requestID != null)
         {
             System.out.println("Verifying all data committed for request " + requestID);
             WorkflowAction request = this.workflowManager.getWorkflowAction(requestID);
             int submitted = this.workflowManager.dataSubmittedForSession(requestID);
             int targets = 0;
             if (request != null && request.getTargets() != null)
             {
                 targets = request.getTargets().length;
             }
             return submitted == targets;
         }
         return false;
     }
 
     /**
      * Record the broadcast to web handlers (e.g. DAVID, EGRIN, etc)
      * @param sourceUrl: the url of the current tab
      * @param subaction: the name of the web handler
      * @param jsonData
      */
     public void recordWorkflow(String targetGoose, String sourceUrl, String subaction, String jsonData)
     {
         if (boss != null && (boss instanceof Boss3))
         {
             try
             {
                 if (targetGoose == null)
                     targetGoose = this.gooseName;
                 System.out.println("Recording workflow from " + this.gooseName + " to " + targetGoose + " " + subaction + " data: " + jsonData);
                 Boss3 boss3 = (Boss3)boss;
                 JSONObject obj = JSONObject.fromObject(jsonData);
                 HashMap<String, String> sourceparams = new HashMap<String, String>();
                 if (sourceUrl != null)
                 {
                     System.out.println("Data uri: " + sourceUrl);
                     sourceparams.put(JSONConstants.WORKFLOW_COMPONENT_DATAURI, sourceUrl);
                 }
                 HashMap<String, String> targetparams = new HashMap<String, String>();
                 if (subaction != null)
                 {
                     System.out.println("Subaction: " + subaction);
                     targetparams.put(JSONConstants.WORKFLOW_COMPONENT_SUBACTION, subaction);
                 }
                 HashMap<String, String> edgeparams = new HashMap<String, String>();
                 for (Object key: obj.keySet())
                 {
                     edgeparams.put((String)key, (String)obj.get(key));
                 }
 
                 boss3.recordAction(this.gooseName, targetGoose, "", -1, sourceparams, targetparams, edgeparams);
             }
             catch (Exception e)
             {
                 System.out.println("Failed to record: " + e.getMessage());
             }
         }
     }
 
     public void saveWorkflowData(String requestId, String componentname, String url)
     {
         System.out.println("Save workflow data for " + requestId + " " + componentname + " " + url);
         if (boss instanceof Boss3)
         {
             WorkflowAction workflowAction = this.workflowManager.getWorkflowAction(requestId);
             if (workflowAction != null)
             {
                 System.out.println("Obtained workflowaction " + workflowAction.getWorkflowID());
                 ArrayList<Single> paramList = new ArrayList<Single>();
                 Single data = new Single("workflowid", workflowAction.getWorkflowID());
                 paramList.add(data);
                 data = new Single("componentid", workflowAction.getComponentID());
                 paramList.add(data);
                 data = new Single("component-name", componentname);
                 paramList.add(data);
                 data = new Single("type", new String("url"));
                 paramList.add(data);
                 data = new Single("url", url);
                 paramList.add(data);
                 Tuple tuple = new Tuple("Cytoscape Report Data", paramList);
                 WorkflowData wfdata = new WorkflowData(tuple);
                 GaggleData[] gdata = new GaggleData[1];
                 gdata[0] = wfdata;
                 WorkflowAction reportaction = new WorkflowAction(workflowAction.getWorkflowID(),
                         workflowAction.getSessionID(),
                         workflowAction.getComponentID(),
                         WorkflowAction.ActionType.Request,
                         workflowAction.getSource(),
                         null,
                         WorkflowAction.Options.WorkflowReportData.getValue(),
                         gdata
                 );
 
                 System.out.println("Sending report workflowaction to boss...");
                 try
                 {
                     ((Boss3)boss).handleWorkflowAction(reportaction);
                 }
                 catch (Exception e)
                 {
                     System.out.println("Failed to send report to boss: " + e.getMessage());
                 }
             }
         }
     }
 
     public void update(String[] gooseNames) throws RemoteException {
         this.activeGooseNames = gooseNames;
         this.hasTargetUpdateSignal.increment();
     }
 
     public String getName() {
         return gooseName;
     }
 
     public void setName(String gooseName) {
         this.gooseName = gooseName;
         System.out.println("Set firegoose name to: " + this.gooseName);
     }
 
 
     public Tuple getMetadata() {
         return metadata;
     }
 
     public void setMetadata(Tuple metadata) {
         this.metadata = metadata;
     }
 
     public void doBroadcastList() {
         // TODO Auto-generated method stub
     }
 
     public void doExit() throws RemoteException {
         // TODO Auto-generated method stub
     }
 
     public void doHide() {
         // TODO Auto-generated method stub
         // could use window.focus() and window.blur() to implement these, if
         // we had a method of calling javascript from java.
     	System.out.println("FireGoose.doHide()");
     }
 
     public void doShow() {
     	System.out.println("FireGoose.doShow()");
     }
 
     public String[] getSelection() {
         // TODO Auto-generated method stub
         return null;
     }
 
     public int getSelectionCount() {
         // TODO Auto-generated method stub
         return 0;
     }
 
     public void clearSelections() {
         // TODO Auto-generated method stub
     }
 
     //implements GaggleConnectionListener
 	public void setConnected(boolean connected, Boss boss) {
 		if (connected) {
 			this.boss = boss;
             System.out.println("Connected to boss.");
             reportApplicationInfo();
 		}
 		else {
 			this.boss = null;
 		}
 		System.out.println("set connected: " + connected);
 		System.out.println("isConnected: " + connector.isConnected());
 	}
     
     public boolean isConnected() {
         return connector.isConnected();
     }
 
 
 
 
 
 
     /*
     public void handleMap(String species, String dataTitle, HashMap hashMap) {
         this.species = species;
         this.dataTitle = dataTitle;
         this.map = hashMap;
         this.incomingDataMsg = "Map(" + hashMap.size() + ")";
     }
 
     public void handleMatrix(DataMatrix matrix) throws RemoteException {
         this.species = matrix.getSpecies();
         this.dataMatrix = matrix;
         this.incomingDataMsg = "Matrix(" + matrix.getRowCount() + "x" + matrix.getColumnCount() + ")";
     }
 
     public void handleNetwork(String species, Network network) throws RemoteException {
         this.species = species;
         this.network = network;
         this.incomingDataMsg = "Network(" + network.nodeCount() + "x" + network.edgeCount() + ")";
     }
     */
 
 
     // end Goose methods -----------------------------------------------------
 
 	/**
 	 * A signal to tell when a new broadcast from the Gaggle has arrived. Dunno
 	 * if this really helps thread safety much, but it's an effort in that direction.
 	 */
 	private static class Signal {
 		private int value = 0;
 
 		/**
 		 * @return the value of the signal.
 		 */
 		public synchronized int check() {
 			return value;
 		}
 
 		public synchronized void reset() {
 			value = 0;
 		}
 
 		/**
 		 * increment the value of the signal.
 		 */
 		public synchronized void increment() {
 			value++;
 		}
 	}
 }
