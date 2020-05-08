 package com.cn.server;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.PrintWriter;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Scanner;
 
 import com.cn.constants.Constants;
 import com.cn.constants.ProtocolConstants;
 import com.cn.protocol.Protocol;
 
 /**
  * This class is non-static by design.  Since the authentication server
  * is multi threaded, there should be separate instances of the authentication
  * server helper to differentiate between the different users who want to login.
  * 
  * Each instance created is intended to be thread-specific.
  * 
  * @author Michael
  *
  */
 public class AuthenticationServerHelper {
 	
 	private Scanner inputStream = null;
 	private boolean userFound = false;
 	private boolean authenticated = false;
 	private String username = null;
 	private String password = null;
 	private Map<String, String[]> characterMap = null;
 	private String saveFile = null;
 	
 	/**
 	 * Uses the inputStream to scan for the saved file.  If found, read it
 	 * and initialize the object for the current user in session.
 	 * 
 	 * @param username
 	 * @param password
 	 */
 	public AuthenticationServerHelper(String username, String password) {
 		init(username, password);
 	}
 	
 	public boolean isAuthenticated() {
 		return authenticated;
 	}
 	
 	public boolean isUserFound() {
 		return userFound;
 	}
 	
 	public Map<String, String[]> getCharacterMap() {
 		return characterMap;
 	}
 	
 	private String getSaveFile(String username) {
 		String file = "src/users/"+username;
 		if(saveFile == null) {
 			saveFile = file;
 		}
 		return file;
 	}
 	
 	/**
 	 * This method overwrites the current save file in src/usrers/<userName>
 	 */
 	public void overWriteChar() {
 		if(!userFound || !authenticated) {
 			return;
 		}
 		PrintWriter outputStream = null;
 		try {
 			outputStream = new PrintWriter(new FileOutputStream(saveFile));
 			outputStream.println(ProtocolConstants.PASSWORD + Protocol.createSimpleRequest(password));
 			for(String character : characterMap.keySet()) {
				outputStream.println(Protocol.createResponseSimple(characterMap.get(character)));
 			}
			outputStream.close();
			outputStream.flush();
 			//re-init the character data structure
 			reInit();
 		}
 		catch(FileNotFoundException e) {
 			e.printStackTrace();
 		}
 		
 	}
 	
 	private void init(String username, String password) {
 		try {
 			characterMap = new HashMap<String, String[]>();
 			inputStream = new Scanner(new FileInputStream(getSaveFile(username)));
 			userFound = true;
 			while(inputStream.hasNextLine()) {
 				String line = inputStream.nextLine();
 				String[] args = Protocol.getRequestArgsSimple(line);
 				String cmd = Protocol.getRequestCmdSimple(line);
 				if(cmd.equals(Constants.PASSWORD)) {
 					if(args[1].equals(password)) {
 						authenticated = true;
 						this.password = password;
 						this.username = username;
 					}
 				}
				else if(authenticated){  //they are stats
 					for(int i=1; i<5; i++) {
 						characterMap.put(cmd, Protocol.getRequestArgsSimple(line));
 					}
 				}
 			}
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * This method is only intended to be used after init(String, String) has
 	 * been called by the instructor.
 	 */
 	private void reInit() {
 		init(username, password);
 	}
 
 
 }
