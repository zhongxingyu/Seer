 package com.lanl.application.treeDecorator.applet.communication;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import javax.swing.JOptionPane;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 
 import com.lanl.application.TPTD.applet.AppletParams;
 import com.lanl.application.TPTD.applet.CommunicationMessageWarningWindow;
 import com.lanl.application.TPTD.applet.ControlPanelAdditions;
 import com.lanl.application.treeDecorator.dataStructures.DecoratorTable;
 import com.lanl.application.treeDecorator.enumeration.CommunicationEnum;
 import com.lanl.application.treePruner.applet.TreePrunerCommunicationNames;
 
 
 public class TreeDecoratorCommunication {
 	static URL seqDetailsURL,crashRecoveryURL,postURL;
 	static String seqDetailsURLString,crashRecoveryURLString ;
 	static String postURLString = AppletParams.URLprefix+"/id";
 	public static boolean isCommError = false;
 	static CommunicationMessageWarningWindow warningWindow;
 	
 	private static String connectToServerForGET(CommunicationEnum getWhat){
 		isCommError = false;
 		if(getWhat == CommunicationEnum.GET_WHAT_SEQ_DETAILS){
 			String seqDetailsJSONString="";
 			try {
 				seqDetailsURL= new URL(seqDetailsURLString);
 				HttpURLConnection seqDetailsConn = (HttpURLConnection) seqDetailsURL.openConnection();
 				seqDetailsConn.setRequestMethod("GET");
 				BufferedReader rd;
 				seqDetailsConn.setDoInput (true);
 		        rd = new BufferedReader(new InputStreamReader(seqDetailsConn.getInputStream()));
 		        String line;
 		        while ((line = rd.readLine()) != null) {
 		        	seqDetailsJSONString += line; 
 		        }
 		        rd.close();
 		        seqDetailsConn.disconnect();
 		        return seqDetailsJSONString;
 		    } catch (MalformedURLException e1) {
 		    	showServerErrorJOptionPane(CommunicationEnum.SERVER_ERROR_SEQ_DETAILS.getName(), 
 		    			CommunicationEnum.SERVER_ERROR.getName(), e1);
 		    	isCommError = true;
 		    } catch (IOException e) {
 		    	System.err.println("Communication Failure");
 				showServerErrorJOptionPane(CommunicationEnum.SERVER_ERROR_SEQ_DETAILS.getName(), 
 		    			CommunicationEnum.SERVER_ERROR.getName(), e);
 				isCommError = true;
 		    } 
 		}
 		else if(getWhat == CommunicationEnum.GET_WHAT_SAVED_DECORATIONS){
 			String savedDecorationsJSONString="";
 			try {
 				crashRecoveryURL= new URL(crashRecoveryURLString);
 				HttpURLConnection crashRecoveryConn = (HttpURLConnection)crashRecoveryURL.openConnection();
 				crashRecoveryConn.setRequestMethod("GET");
 				BufferedReader rd;
 				crashRecoveryConn.setDoInput (true);
 		        rd = new BufferedReader(new InputStreamReader(crashRecoveryConn.getInputStream()));
 		        String line;
 		        while ((line = rd.readLine()) != null) {
 		        	savedDecorationsJSONString += line; 
 		        }
 		        rd.close();
 		        crashRecoveryConn.disconnect();
 		        return savedDecorationsJSONString;
 		    } catch (MalformedURLException e1) {
 		    	e1.printStackTrace();
 		    } catch (IOException e) {
 		    	System.err.println("Communication Failure");
 		    	e.printStackTrace();
 		    }
 			
 		}
 		return "";
 	}
 	
 	private static String connectToServerForPOST(String JSONStringToPass){
 		String returnedString ="";
 		try {
 			postURL = new URL(postURLString);
 			HttpURLConnection postConn = (HttpURLConnection) postURL
 					.openConnection();
 			postConn.addRequestProperty("Content-Type", "text/JSON");
 			postConn.setRequestMethod("POST");
 			postConn.setDoOutput(true);
 			OutputStreamWriter wr = new OutputStreamWriter(postConn
 					.getOutputStream());
 			wr.write(JSONStringToPass);
 			wr.flush();
 			BufferedReader rd;
 			rd = new BufferedReader(new InputStreamReader(postConn
 					.getInputStream()));
 			String line;
 			while ((line = rd.readLine()) != null) {
 				returnedString += line;
 			}
 			wr.close();
 			rd.close();
 			postConn.disconnect();
 		} catch (MalformedURLException e1) {
 			destroyWarningWindow();
 			e1.printStackTrace();
 		} catch (IOException e) {
 			destroyWarningWindow();
 			System.err.println("Communication Failure");
			if(e.getMessage().contains("500")){
				JOptionPane.showMessageDialog( null, 
						"Your action could not be completed.\n"+
						"Please try to relaunch the applet and try again.\n" +
						"If the problem persists, please contact flu@lanl.gov.","Error",JOptionPane.ERROR_MESSAGE);
			}
			if(e.getMessage().contains("403") && AppletParams.isEitherTPorTDForLANL()){
				JOptionPane.showMessageDialog( null, 
						"Your action could not be completed.\n"+
						"Your session has either expired or is invalid. \n" +
						"Please re-login to your account and try again.\n" +
						"If the problem persists, please contact flu@lanl.gov.","Error",JOptionPane.ERROR_MESSAGE);
			}
 			e.printStackTrace();
 		}
 
 		return returnedString;
 	}
 	
 	public static void getSequenceDetailsComm(){
 		seqDetailsURLString  = AppletParams.URLprefix + "/sd/"+AppletParams.filename;
 		String seqDetailsJSONString = connectToServerForGET(CommunicationEnum.GET_WHAT_SEQ_DETAILS);
 		if(!seqDetailsJSONString.equals("")){
 			DecoratorJSONHandler.storeCharacteristicValues(seqDetailsJSONString);
 		}
 	}
 	
 	public static void getSavedDecorationsComm(){
 		crashRecoveryURLString = AppletParams.URLprefix+ "/id/"+AppletParams.filename;
 		String savedDecoraionsJSONString = connectToServerForGET(CommunicationEnum.GET_WHAT_SAVED_DECORATIONS);
 		if(!savedDecoraionsJSONString.equals("")){
 			DecoratorJSONHandler.storeSavedStyles(savedDecoraionsJSONString);
 		}
 	}
 	public static void postSaveDecorationsComm(boolean isAutoSave){
 		warningWindow = new CommunicationMessageWarningWindow();
 		String serverResponse = "";
 		String JSONString = DecoratorJSONHandler.getSavedStyles();
 		if(!JSONString.equals("")){
 			serverResponse = connectToServerForPOST(JSONString);
 		}
 		if(serverResponse.equals(CommunicationEnum.SAVE_SUCCESS.getName())){
 			System.out.println("SAVE PRESSED / AUTOSAVE \n");
         	System.out.println(" Server reurned: File successfully opened and written");
         	if(!isAutoSave){
         		DecoratorTable.copyStuffToSavedStuff();
         	}
 		}
 		else{
 			System.out.println("SAVE PRESSED / AUTOSAVE \n");
         	System.out.println(" Server reurned: Can't open file");
 		}
 		destroyWarningWindow();
 	}
 	
 	public static void postDiscardDecorationsComm(){
 		warningWindow = new CommunicationMessageWarningWindow();
 		String serverResponse = "";
 		String JSONString = DecoratorJSONHandler.getDiscardJSONString();
 		if(!JSONString.equals("")){
 			serverResponse = connectToServerForPOST(JSONString);
 		}
 		if(serverResponse.equals(CommunicationEnum.DISCARD_SUCCESS.getName())){
 			System.out.println("Discard PRESSED \n");
         	System.out.println(" Server reurned: file successfully deleted");
 		}
 		else{
 			System.out.println("Discard PRESSED \n");
         	System.out.println(" Server reurned: Failed to delete the file because no file was present or permissions");
 		}
 		destroyWarningWindow();
 	}
 	protected static void showServerErrorJOptionPane(String msg,String title,Exception e){
 		JOptionPane.showMessageDialog( null, msg+"\n\n"+e.toString(),
     			title,JOptionPane.ERROR_MESSAGE);
 	}
 	
 	protected static void destroyWarningWindow(){
         if(warningWindow!=null){
         	warningWindow.close();
                 
         }
     }
 }
