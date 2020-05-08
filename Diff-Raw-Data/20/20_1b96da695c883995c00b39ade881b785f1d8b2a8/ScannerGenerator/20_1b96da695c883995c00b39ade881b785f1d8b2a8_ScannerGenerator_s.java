 package Source;
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map.Entry;
 
 public class ScannerGenerator {
 	
 	public ScannerGenerator() {}
 
 	/**
 	 * Generates DFATable from Specification File
 	 * 
 	 * @param specFile
 	 * @return
 	 * @throws Exception 
 	 */
 	public static DFATable generateDFA(String specFile) throws Exception {
 //		FileInputStream in = new FileInputStream(specFile);
 		System.out.println("Parsing specification file '"+specFile+"' to NFAs...");
 		BufferedReader in = new BufferedReader( new FileReader(specFile) );
 		
 		// Read each line
 		String line;
 		NFA partialNFA = new NFA();
 		// Parse Character Classes
 		HashMap<String,HashSet<Character>> tokens = new HashMap<String,HashSet<Character>>();
 		while( (line = in.readLine()) != null && !line.isEmpty() ) {
 			if (isValid(line)) {
 				parseCharClass(line,tokens);
 			}
 		}
 		for (Entry<String, HashSet<Character>> e : tokens.entrySet()) {
 			System.out.println( e.getKey() + " : " + e.getValue().toString());
 		}
 		
 		// Parse Identifiers
 		while( (line = in.readLine()) != null && !line.isEmpty() ) {
 			if (isValid(line)) {
 				parseIdentifier(line);
 			}
 		}
 
 		// For each line, generate an NFA
 
 		in.close();
 		System.out.println("Finished parsing specification file.");
 		
 		// Merge NFA's into BigNFA
 		System.out.println("Merging NFAs...");
 		// TODO :: Merge NFAs
 		System.out.println("Done merging.");
 
 		// Convert BigNFA
 		System.out.println("Converting NFA to DFA...");
 		// TODO :: Convert BigNFA to DFA
 		System.out.println("Done converting.");
 		return new DFATable();
 	}
 	
 	/**
 	 * Checks whether string is valid regex (defined by project)
 	 * @param line
 	 * @return
 	 */
 	private static boolean isValid(String line) {
 		if ( !line.matches("\\$([\\w-]+)\\ (.+)") ) return false;
 //		if ( line.charAt(0) != '$') return false;
 //		line.
 		return true;
 	}
 	
 	
 	public static void parseCharClass(String line, HashMap<String, HashSet<Character>> tokens) throws Exception {
 		line.replaceAll("\\ ", "<SPACE>"); // replace '\ ' with '<SPACE>' so split doesn't affect it
 		String[] chunks = line.split(" ");
 		for (int i=0;i<chunks.length;++i) chunks[i] = chunks[i].replaceAll("<SPACE>", "\\ "); // replace spaceholder with '\ ' again
 		
 		String token = chunks[0];
		HashSet<Character> validChars = new HashSet<Character>(255);
 		
 		if ( tokens.containsKey(token) ) System.out.println("Token repeat Error!");
 		else if (chunks.length == 2) {
 			// Simple X Y
 			String data = chunks[1];
 			parseRule(data, validChars);
 			tokens.put(token, validChars);
 		}
 		else if (chunks.length == 4 && chunks[2].equals("IN") ) {
 			// $X Y IN $Z
 			String data = chunks[1];
 			validChars = new HashSet<Character>( tokens.get( chunks[3] ) );
 			parseNotRule(data,validChars);
 			
 		}
 		else {
 			System.out.println("Wierd Chunks (n="+chunks.length+")! : " + line);
 			for (String s : chunks) System.out.println(":: "+ s);
 		}
 		
 		tokens.put(token, validChars);
 	}
 	private static void parseRule(String data, HashSet<Character> validChars) {		
 		if (data.startsWith("[") && data.endsWith("]")) {
 			data = data.substring(1,data.length()-1);
 		}
 		accumulateChars(data,validChars);
 	}
 	
 	private static void parseNotRule(String data, HashSet<Character> validChars) {
 		if (data.startsWith("[^") && data.endsWith("]")) {
 			// Not
 			data = data.substring(2,data.length()-1);
 		}
 		deccumulateChars(data, validChars);
 	}
 	
 	private static void accumulateChars(String data, HashSet<Character> validChars) {
 		for (int i = 0; i < data.length(); i++) {
 			char c = data.charAt(i);
 			if (c == '-') {
 //				System.out.println(data.charAt(i-1)+"->"+data.charAt(i+1));
 				for (char j = (char) (data.charAt(i-1)+1); j < data.charAt(i+1) - 1; j++) {
 					validChars.add(j);
 				}
 			}
 			else if (c == '\\') continue; 
 			else validChars.add(c);
 		}
 	}
 	private static void deccumulateChars(String data, HashSet<Character> validChars) {
 		for (int i = 0; i < data.length(); i++) {
 			char c = data.charAt(i);
			if (c == '-') for (char j = data.charAt(i-1); j < data.charAt(i-1); j++) validChars.remove(j); 
 			else if (c == '\\') continue; 
 			else validChars.remove(c);
 			
 		}
 	}
 
 	public static void parseIdentifier(String line) {
 		// TODO Auto-generated method stub
 		
 	}
 }
