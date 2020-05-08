 package edu.stockton;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.ListIterator;
 
 import javax.swing.text.html.HTMLDocument.Iterator;
 
 public class LanguageProcessor {
 	
 	/**
 	 * Interprets a complete ATC command string.
 	 * Parses the string and returns an ATCCommand object.
 	 * @param command The ATC command sentence
 	 * @return An ATCCommand object containing the parsed elements
 	 */
 	public static ATCCommand parse(String command) {
 		ArrayList<String> tokens = new ArrayList<String>();
 		ArrayList<Component> components = new ArrayList<Component>();
 		
 		String[] words = command.split(" ");
 		for(String word : words) {
 			tokens.add(word);
 		}
 		
 		
 		// Tag each token and group
 		String currentTag = "";
 		String componentText = "";
 		for(int i = 0; i < tokens.size(); i++) {
 			
 			String thisTag = tag(tokens.get(i));
 			
 			if(thisTag.equals(currentTag) && i < tokens.size()-1) {
 				componentText += tokens.get(i) + " ";	
 			} else if(i == 0) {
 				currentTag = thisTag;
 				componentText += tokens.get(i) + " ";
 			} else {
				if(i == tokens.size()-1) componentText += tokens.get(i) + " ";
				Component thisComponent = new Component(componentText, currentTag);
				components.add(thisComponent);
				componentText = "";
				componentText += tokens.get(i) + " ";
				currentTag = thisTag;
 			}
 
 		}
 		
 		String recipient = "";
 		
 		// Do conversions for each token
 		for(int i = 0; i < components.size(); i++) {
 			Component current = components.get(i);
 			String type = current.getType();
 			
 			
 			if(type.equals("number")) {				
 				current.setText(NumberEngine.toNumeric(current.getText()));
 				components.set(i, current);
 				
 				// Recognize recipient and build recipient string
 				if(recipient == "" && components.get(i-1).getType() == "callsign") {
 					recipient = components.get(i-1).getText() + components.get(i).getText();
 				}
 				
 			}
 			if(type.equals("callsign")) {
 				current.setText(CallsignEngine.telephonyToDesignator(current.getText()));
 				components.set(i, current);
 			}
 		}
 		
 		String phrase = "";
 		for(Component component : components) {
 			phrase += component.getText() + " ";
 		}
 		ParsedInstruction instruction = InstructionEngine.parse(phrase);
 		
 		ATCCommand parsed = new ATCCommand(recipient, instruction.getType(), instruction.getParam());
 		return parsed;
 
 	}
 	
 	/**
 	 * Tags a passed token string as a number, callsign, or otherwise
 	 * unidentified.
 	 * @param token The string to tag
 	 * @return The tag (either "number", "callsign", or "unidentified")
 	 */
 	private static String tag(String token) {
 		String tag = "unidentified";
 		if(NumberEngine.isNumber(token)) tag = "number";
 		if(CallsignEngine.isCallsign(token)) tag = "callsign";
 		return tag;
 	}
 	
 }
