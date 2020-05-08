 package commandLineApp;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.Map;
 
 import quickfix.ConfigError;
 import quickfix.InvalidMessage;
 
 import com.corrigal.fixCracker.MessageReader;
 
 public class FixSringInterpretor {
 
 	public static void main(String[] args) throws IOException {
 		System.out.println("Paste FIX message up to last SOH before checkSum field (pipe delimited):");
 		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
 		String fixMessageString = input.readLine();
 		
 		try {
 			Map<Integer, String> fixFieldMap = parseString(fixMessageString);
 			displayFixFieldMap(fixFieldMap);
 		} catch (InvalidMessage e) {
 			System.out.println("Fix string invalid: " + e.getMessage());
 		} catch (ConfigError e) {
 			System.out.println(e.getMessage());
 		}
 	}
 	
 	private static Map<Integer, String> parseString(String fixString) throws ConfigError, InvalidMessage {
 		MessageReader reader = new MessageReader();
 		return reader.parseFixString(fixString);
 	}
 	
 	private static void displayFixFieldMap(Map<Integer, String> fixFieldMap) throws ConfigError {
 		MessageReader reader = new MessageReader();
 		for (Integer tag : fixFieldMap.keySet()) {
 			String tagName = reader.fieldNameForTag(tag);
 			String fieldValue = reader.meaningfulFieldValue(tag, fixFieldMap.get(tag));
			String outputRow = String.format("%-10s %-20s %-20s", tag, tagName, fieldValue);
			System.out.println(outputRow);
 		}
 	}
 	
 	
 
 }
