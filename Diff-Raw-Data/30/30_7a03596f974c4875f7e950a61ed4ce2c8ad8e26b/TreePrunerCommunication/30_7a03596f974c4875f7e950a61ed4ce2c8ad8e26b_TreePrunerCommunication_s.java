 package com.lanl.application.treePruner.applet;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import javax.swing.JOptionPane;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.lanl.application.TPTD.applet.AppletParams;
 import com.lanl.application.TPTD.applet.ControlPanelAdditions;
 import com.lanl.application.treePruner.custom.data.WorkingSet;
 
 public class TreePrunerCommunication {
 	
 	private WorkingSet ws = new WorkingSet();
 	public static String lastAction="";
 	static URL postURL;
 	static String TPpostURL = AppletParams.URLprefix + "/id";
 	
 	
 	private static String createJSONString(JSONArray jsonArray, String action, String filename){
 		
 		JSONObject jsonObject = new JSONObject();
 		JSONObject innerJO = new JSONObject();
 		try {
 			innerJO.put(TreePrunerCommunicationNames.ACTION.getName(), action);
 			innerJO.put(TreePrunerCommunicationNames.FILENAME.getName(), filename);
 			if(jsonArray!=null){
 				innerJO.put(TreePrunerCommunicationNames.SEQ_ACC_TO_REMOVE.getName(), jsonArray);
 			}
 			jsonObject.put(TreePrunerCommunicationNames.PRUNER.getName(), innerJO);
 			
 		} catch (JSONException e) {
 			e.printStackTrace();
 			ControlPanelAdditions.destroyWarningWindow();
 		}
 		return jsonObject.toString();
 		
 	}
 	
 	private static String connectToServer(JSONArray jsonArray,String action){
 		String returnedString ="";
 		String filename = AppletParams.filename;
 		try{
 			postURL  = new URL(TPpostURL);
 			HttpURLConnection postConn = (HttpURLConnection) postURL.openConnection();
 			postConn.addRequestProperty("Content-Type","text/JSON" );
 			postConn.setRequestMethod("POST");
 			postConn.setDoOutput(true);
 	       OutputStreamWriter wr = new OutputStreamWriter(postConn.getOutputStream());
 	       if(action.equals(TreePrunerCommunicationNames.DISCARD.getName())){
 	    	   wr.write(createJSONString(null, action,filename));
 	       }
 	       
 	       else if(action.equals(TreePrunerCommunicationNames.LOCK_WS.getName())){
 	    	   wr.write(createJSONString(null, action,filename));
 	       }
 	       
 	       else if(action.equals(TreePrunerCommunicationNames.UNLOCK_WS.getName())){
 	    	   wr.write(createJSONString(null, action,filename));
 	       }
 	       
 	       else{
 	    	   wr.write(createJSONString(jsonArray, action,filename));
 	       }
 	        
 	        wr.flush();
 	        BufferedReader rd;
 	        rd = new BufferedReader(new InputStreamReader(postConn.getInputStream()));
 	        String line;
 	        while ((line = rd.readLine()) != null) {
 	        	returnedString += line;
 	        }
 	        wr.close();
 	        rd.close();
 	        postConn.disconnect();
 		} catch (MalformedURLException e1) {
 			ControlPanelAdditions.destroyWarningWindow();
 			e1.printStackTrace();
 		} catch (IOException e) {
 			ControlPanelAdditions.destroyWarningWindow();
 			System.err.println("Communication Failure");
 			e.printStackTrace();
 		}
 		return returnedString;
 	}
 	
 	public static void deleteFromDbComm (JSONArray accToRemove){
 		lastAction = TreePrunerCommunicationNames.COMMIT.getName();
 		String returnedString = "";
 		String action = TreePrunerCommunicationNames.COMMIT.getName();
 		returnedString = connectToServer(accToRemove,action);
 		ControlPanelAdditions.destroyWarningWindow();
 	    if(returnedString.equals(TreePrunerCommunicationNames.COMMIT_SUCCESS.getName())){
 	    	JOptionPane.showMessageDialog( null, "Your sequences were successfully deleted","Delete Confirmation",JOptionPane.INFORMATION_MESSAGE);
 	    }
 	    else{
 	    	JOptionPane.showMessageDialog( null, "Your sequences were not deleted.\n Please make sure " +
         			"that the sequences are not already deleted. \n " +
         			"Please contact flu@lanl.gov if your problem persists.","Delete Confirmation",JOptionPane.INFORMATION_MESSAGE );
 	    }
 	}
 	
 	public static void discardComm(){
 		lastAction = TreePrunerCommunicationNames.DISCARD.getName();
 		String returnedString = "";
 		String action = TreePrunerCommunicationNames.DISCARD.getName();
 		returnedString = connectToServer(null, action);
 	    if(returnedString.equals(TreePrunerCommunicationNames.DISCARD_SUCCESS.getName())){
 	    	System.out.println("Discard PRESSED \n");
         	System.out.println(" Server reurned: file successfully deleted");
 	    }
 	    else{
 	    	System.out.println("Discard PRESSED \n");
         	System.out.println(" Server reurned: Failed to delete the file because no file was present or permissions");
 	    }
 	}
 	
 	public static void saveToFileComm(JSONArray accToRemove){
 		lastAction = TreePrunerCommunicationNames.SAVE.getName();
 		String returnedString = "";
 		String action = TreePrunerCommunicationNames.SAVE.getName();
 		returnedString = connectToServer(accToRemove, action);
 	    if(returnedString.equals(TreePrunerCommunicationNames.SAVE_SUCCESS.getName())){
 	    	System.out.println("SAVE PRESSED / AUTOSAVE \n");
         	System.out.println(" Server reurned: File successfully opened and written");
 	    }
 	    else{
 	    	System.out.println("SAVE PRESSED / AUTOSAVE \n");
         	System.out.println(" Server reurned: Can't open file");
 	    }
 	}
 	
 	public static void lockWSComm(){  //BHB only
 		String returnedString = "";
 		String action = TreePrunerCommunicationNames.LOCK_WS.getName();
 		returnedString = connectToServer(null, action);
 	    if(returnedString.equals(TreePrunerCommunicationNames.LOCK_SUCCESS.getName())){
 	    	System.out.println("WorkingSet Lock \n");
         	System.out.println(" Server reurned: Working Set locked successfully");
 	    }
 	    else{
 	    	System.out.println("WorkingSet Lock \n");
         	System.out.println(" Server reurned:  Can't lock Working Set");
 	    }
 	}
 		
 	public static void unlockWSComm(){  //BHB only
 		String returnedString = "";
 		String action = TreePrunerCommunicationNames.UNLOCK_WS.getName();
 		returnedString = connectToServer(null, action);
 	    if(returnedString.equals(TreePrunerCommunicationNames.UNLOCK_SUCCESS.getName())){
 	    	System.out.println("WorkingSet Unlock \n");
         	System.out.println(" Server reurned: Working Set unlocked successfully");
 	    }
 	    else{
 	    	System.out.println("WorkingSet Unlock \n");
         	System.out.println(" Server reurned:  Can't unlock Working Set");
 	    }
 	}
 	
 	
 }
 	
