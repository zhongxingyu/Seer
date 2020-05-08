 package com.lanl.application.TPTD.applet;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 
 
 import com.lanl.application.treeDecorator.applet.communication.TreeDecoratorCommunication;
 import com.lanl.application.treeDecorator.dataStructures.DecoratorTable;
 import com.lanl.application.treeDecorator.enumeration.DecorationEnumHelper;
 import com.lanl.application.treePruner.applet.TreePrunerCommunication;
 import com.lanl.application.treePruner.applet.TreePrunerCommunicationNames;
 import com.lanl.application.treePruner.custom.data.WorkingSet;
 
 public class CrashRevovery {
 	ArrayList<String> crashRecoveryAccessions = new ArrayList<String>();
 	WorkingSet ws = new WorkingSet();
 	URL savedAccURL;
 	String TPgetURL = AppletParams.URLprefix + "/id/"+AppletParams.filename;
 	public void treePrunerCrashRecoveryInit() {
 		String TPgetJSON = "";
 		JSONObject crashRecoveryJO;
 		JSONArray recoveredAccessions = null;
 		// String savedAcc = "file:////Users/kmohan/Desktop/2.txt";
 		if(AppletParams.isTreePrunerForBHB()){
 			TreePrunerCommunication.lockWSComm();
 		}
 		if (AppletParams.savedAccFlag.equals("true")) {
 			// wait till all the nodes have been successfully painted on the
 			// applet. After 2 secs hoping that all tree branches would have
 			// successfully painted by then
 			// we will call the crash recovery and repaint the tree(see below).
 			// START
 			long wait, time;
 			wait = System.currentTimeMillis();
 			while (true) {
 				time = System.currentTimeMillis() - wait;
 				if (time >= 2000) // 2 seconds wait
 					break;
 			}
 			// wait till all the nodes have been successfully painted on the
 			// applet. After 2 secs hoping that all tree branches would have
 			// successfully painted by then
 			// we will call the crash recovery and repaint the tree (see below).
 			// END
 			try {
 				savedAccURL = new URL(TPgetURL);
 				HttpURLConnection crashRecConn = (HttpURLConnection) savedAccURL.openConnection();
 				crashRecConn.setRequestMethod("GET");
 				BufferedReader rd;
 				crashRecConn.setDoInput (true);
 		        rd = new BufferedReader(new InputStreamReader(crashRecConn.getInputStream()));
 		        String line;
 		        while ((line = rd.readLine()) != null) {
 		        	TPgetJSON += line; 
 		        }
 		        rd.close();
 		        crashRecConn.disconnect();
 		        
 		        if(!TPgetJSON.equals("")){
 		        	JSONTokener jt = new JSONTokener(TPgetJSON);
 		        	crashRecoveryJO = new JSONObject(jt);
 		        	JSONObject innerJO = crashRecoveryJO.getJSONObject(TreePrunerCommunicationNames.PRUNER.getName());
 		        	recoveredAccessions = innerJO.getJSONArray(TreePrunerCommunicationNames.SEQ_ACC_TO_REMOVE.getName());
 		        	for (int i = 0; i < recoveredAccessions.length(); i++) {
 		    			crashRecoveryAccessions.add(recoveredAccessions.getString(i));
 		    		}
 		        }
 		        else {
 		        	throw new IOException();
 		        }
 			} catch (MalformedURLException e1) {
 				e1.printStackTrace();
 			} catch (IOException e) {
 				System.err.println("Unable to read from file of saved accession");
 				e.printStackTrace();
 			} catch (JSONException e) {
 				System.err.println("JSON Exception has occured");
 				e.printStackTrace();
 			}
 			ws.crashRecovery(crashRecoveryAccessions);
 			TreePrunerCommunication.lastAction = TreePrunerCommunicationNames.SAVE.getName();
 			ws.copyAccToRememberAcc();
 			ws.clear("removeActive");
 			// Repaint the tree. START
 			SubTreePanel.refreshAllWindows();
 			// atvappletframe.getATVpanel().getATVtreePanel().repaint();
 			// Repaint the tree. END
 		} // end of if(saved_session_before.equals("true")){
 	} // end of crashRecoveryInit
 	
 	public void treeDecoratorCrashRecoveryInit(){
 		if (AppletParams.savedAccFlag.equals("true")) {
 			// wait till all the nodes have been successfully painted on the
 			// applet. After 2 secs hoping that all tree branches would have
 			// successfully painted by then
 			// we will call the crash recovery and repaint the tree(see below).
 			// START
 			long wait, time;
 			wait = System.currentTimeMillis();
 			while (true) {
 				time = System.currentTimeMillis() - wait;
 				if (time >= 2000) // 2 seconds wait
 					break;
 			}
 			// wait till all the nodes have been successfully painted on the
 			// applet. After 2 secs hoping that all tree branches would have
 			// successfully painted by then
 			// we will call the crash recovery and repaint the tree (see below).
 			// END
 			AutoSave.resetAutoSave();
 			TreeDecoratorCommunication.getSavedDecorationsComm();
 			DecoratorTable.copyStuffToSavedStuff();
 			DecorationEnumHelper.populateBranchColorNodes();
 			DecoratorTable.updateLegend();
 			//refresh all windows
 			SubTreePanel.refreshAllWindows();
 			//	print(DecoratorTable.decoratorTable);
 		}
 	}
 }
