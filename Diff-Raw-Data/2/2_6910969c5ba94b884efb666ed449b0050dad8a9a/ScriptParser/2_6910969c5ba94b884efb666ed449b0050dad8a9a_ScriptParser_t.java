 package edu.cmu.scripting;
 
 import java.util.List;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 import org.slf4j.Logger;
 
 import com.google.gson.Gson;
 
 import edu.cmu.logger.EmulatorLogger;
 
 
 /**
  * This class provides one static method to help parse script
  * with the given name. The result is stored and returned in a 
  * HashMap containing the status of the parsing (error or success) and 
  * the message (error message or JSON String).
  * @author ziw
  *
  */
 public class ScriptParser {
 
 	public static final String SUCCESS = "success";
 	public static final String ERROR = "error";
 	
 	public static final String STATUS_KEY = "status";
 	public static final String MESSAGE_KEY = "message";
 
 	private static final char COMMENT_SIGN = '#';
 	private static final String WAIT_COMMAND = "wait";
 
 	private static final String CLICK = "click";
 	private static final String HOLD = "hold";
 	private static final String RELEASE = "release";
 	
 	private static final int MIN_WAITING_TIME =50;
 	private static List<String> allButtonNames;
 	
 	//Initialization block. Read all button codes from input mapping.
 	static{
 		Logger logger = EmulatorLogger.getEmulatorInfoLogger();
 		allButtonNames = new ArrayList<String>();
		File inputMapping = new File("./BWT_SCRIPTS/input_mapping.csv");
 		try {
 			BufferedReader reader = new BufferedReader(new FileReader(inputMapping));
 			String line;
 			while((line = reader.readLine()) != null ){
 				String[] words = line.split(",");
 				if(words.length>0){
 					//trim the leading _ and the quotes
 					String name = words[0].substring(2, words[0].length()-1);
 					if(!name.equals("initialize") && !name.equals("uninitialize")){
 						allButtonNames.add(name);
 					}
 				}
 			}
 		} catch (FileNotFoundException e) {
 			logger.error("input_mapping.csv is not found. Fatal. Script parsing may not work.");
 			EmulatorLogger.logException(logger, e);
 		} catch (IOException e) {
 			logger.error("IOException when reading from input_mapping.csv. Fatal. Script parsing may not work.");
 			EmulatorLogger.logException(logger, e);
 
 		}
 		
 	}
 	
 	
 	
 	/**
 	 * Try to parse/compile the script with the given file name.
 	 * The function return a JSON string representing the result of 
 	 * compilation. The corresponding JSON object contains two fields,
 	 * "status" and "message". If status is success, message stores
 	 * an array of {@link ButtonAction}. Otherwise it stores the error messages.
 	 * @param scriptName
 	 * @return
 	 */
 	public static String parseScript(String scriptName){
 		Map<String, Object> result = new HashMap<String,Object>();
 		//a set of buttons that are held down
 		Set<String> heldDown = new HashSet<String>();
 		//all compilation errors
 		List<String> errors = new ArrayList<String>();
 		//the resulting recording queue
 		List<ButtonAction> recordingQueue = new ArrayList<ButtonAction>();
 
 		int lineNumber = 1;
 		int waitTime = 0;
 		try {
 			BufferedReader reader = new BufferedReader(new FileReader(scriptName));
 			String line;
 			while((line = reader.readLine()) != null ){
 				line = line.trim();
 				if(line.length()>0 && line.charAt(0) != COMMENT_SIGN){			
 					StringTokenizer st = new StringTokenizer(line, " \t\n");
 					int count = st.countTokens();
 					if(count<2){
 						errors.add(prepareErrorMessage(lineNumber,"Too few arguments"));
 					}
 					else if(count > 2){
 						errors.add(prepareErrorMessage(lineNumber,"Too many arguments"));
 					}
 					else{
 						String action = st.nextToken().toLowerCase();
 						String argument = st.nextToken().toLowerCase();
 						//check to see if action is wait or button command
 						if(isWaitCommand(action)){								
 							int milliSeconds =0;
 							try{
 								milliSeconds = Integer.parseInt(argument);
 								if(milliSeconds <=0){
 									errors.add(prepareErrorMessage(lineNumber, 
 											"Invalid argument for wait. Must be positive"));
 								}
 								else{
 									waitTime += milliSeconds;
 								}
 							}
 							catch(NumberFormatException e){
 								errors.add(prepareErrorMessage(lineNumber, 
 										"Invalid argument for wait. Must be an integer"));
 							}
 						}
 						else if(isButtonCommand(action)){
 							if(!isValidButtonCode(argument)){
 								errors.add(prepareErrorMessage(lineNumber, "Invalid button code: " + argument));
 							}
 							else{
 								if(action.equals(HOLD) && !heldDown.contains(argument)){
 									heldDown.add(argument);
 									recordingQueue.add(new ButtonAction(argument,action, 
 											recordingQueue.size()==0? 
 											0 :Math.max(MIN_WAITING_TIME, waitTime)));
 									waitTime = 0;
 									
 								}
 								else if(action.equals(RELEASE) && heldDown.contains(argument)){
 									heldDown.remove(argument);
 									recordingQueue.add(new ButtonAction(argument,action, 
 											recordingQueue.size()==0? 
 											0 :Math.max(MIN_WAITING_TIME, waitTime)));
 									waitTime = 0;
 								}
 								else if(action.equals(CLICK)){
 									recordingQueue.add(new ButtonAction(argument,action, 
 											recordingQueue.size()==0? 
 											0 :Math.max(MIN_WAITING_TIME, waitTime)));
 									waitTime = 0;
 								}
 							}
 							
 						}
 						else{
 							errors.add(prepareErrorMessage(lineNumber, "Unrecognized command: " + action));
 							
 						}
 						
 					}
 				}
 				lineNumber ++;
 			}
 			
 			
 		} catch (FileNotFoundException e) {
 			errors.add(scriptName +" file not found.");
 		} catch (IOException e) {
 			errors.add("IOException occurred when reading script " + scriptName);
 		}
 		if(heldDown.size()>0){
 			StringBuffer sb = new StringBuffer();
 			for(String button : heldDown){
 				sb.append(button+" ");
 			}
 			errors.add("Invalid script. The following buttons are never released.");
 			errors.add(sb.toString());
 		}
 		
 		if(errors.size()>0){
 			StringBuffer sb = new StringBuffer();
 			for(String error : errors){
 				sb.append(error+"<br />");
 			}
 			result.put(STATUS_KEY, ERROR);
 			result.put(MESSAGE_KEY, sb.toString());
 		}
 		else if(recordingQueue.size()==0){
 			result.put(STATUS_KEY, ERROR);
 			result.put(MESSAGE_KEY, "Script is empty.");
 		}
 		else{
 			result.put(STATUS_KEY, SUCCESS);
 			result.put(MESSAGE_KEY, recordingQueue);
 		}
 		
 		Gson g = new Gson();
 		return g.toJson(result);
 	}
 	
 	private static boolean isButtonCommand(String action){
 		return action!=null && (
 					action.equalsIgnoreCase(CLICK)||
 					action.equalsIgnoreCase(HOLD)||
 					action.equalsIgnoreCase(RELEASE)
 				); 
 	}
 	
 	private static boolean isWaitCommand(String action){
 		return action!=null && action.equals(WAIT_COMMAND); 
 	}
 
 	private static boolean isValidButtonCode(String argument){
 		return argument!=null && allButtonNames.contains(argument);
 	}
 	
 	private static String prepareErrorMessage(int lineNumber, String message){
 		return "Error at line "+ lineNumber+ ". " + message.trim();
 	}
 	
 	
 	/**
 	 * A private class used to temporarily store a frame in a recording queue and 
 	 * serves as a wrapper for the Gson library to use to generate JSON string.
 	 * @author ziw
 	 *
 	 */
 	private static class ButtonAction{
 		
 		private String button;
 		private String eventType;
 		private int timeStamp;
 		
 		public ButtonAction(String button, String eventType, int timeStamp){
 			this.button = button;
 			this.eventType = eventType;
 			this.timeStamp = timeStamp;
 		}
 
 		public String toString(){
 			return button +" " + eventType +" " + timeStamp;
 		}
 	}
 
 
 	
 	
 }
