 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.HashMap;
 import java.util.Map;
 
 public class SpecParser {
 
 	HashMap<String, String> specDefinitions;
 	HashMap<String, NFA> specNFAs;
 
 	public static void main(String args[]) {
 		SpecParser sp = new SpecParser();
 		sp.parseFile("src/spec2.txt");
 		for (Map.Entry<String, String> specEntry : sp.specDefinitions.entrySet()) {
 			System.out.println("Entry: " + specEntry.getKey() + ". Value: " + specEntry.getValue());
 		}
 	}
 
 	public SpecParser() {
 		specDefinitions = new HashMap<String, String>();
 		specNFAs = new HashMap<String, NFA>();
 	}
 
 	/**
 	 * This method runs through the HashMap and reduces each definition to just
 	 * straight regex and then builds NFA's out of that regex. It requires
 	 * substituting in the regex from other token definitions and calculating
 	 * inclusion principles. (i.e. [^A-Z] IN $ALPHA).
 	 * 
 	 * @param filename
 	 * @return
 	 */
 	public HashMap<String, NFA> parseFile(String filename) {
 		specDefinitions = readFile(filename);
 //		System.out.println("BEGIN PREPROCESSING...");
 		for (Map.Entry<String, String> specEntry : specDefinitions.entrySet()) {
 			String entry = specEntry.getKey();
 			String definition = specEntry.getValue().trim();
 			if (definition.equals("-")) {
 //				System.out.println("Found minus.");
 				definition = "\\-";
 				specDefinitions.put(entry, definition);
 			}
 			if (!definition.contains("$")) {
 				specNFAs.put(entry, new NFA(definition));
 //				System.out.println("Added definition for token \"" + entry + "\": " + definition);
 			}
 		}
 
 		for (Map.Entry<String, String> specEntry : specDefinitions.entrySet()) {
 			String entry = specEntry.getKey();
 			String definition = specEntry.getValue();
 			String target = "";
 			String excludeRule = "";
 			if (definition.contains(" IN ")) {
 				String[] splitDefinition = definition.split(" IN ", 2);
 				excludeRule = splitDefinition[0].trim();
 				target = splitDefinition[1].trim();
 
 				// sanity check
 				assert (specNFAs.containsKey(target));
 				String tokenDefinition = String.valueOf(specDefinitions.get(target));
 				if (tokenDefinition.contains("-") && !(tokenDefinition == ("\\-"))) {
 					int startIndex = 0;
 					int dashIndex = tokenDefinition.indexOf("-", startIndex);
 					while (dashIndex != -1) {
 						char rangeStart = tokenDefinition.charAt(dashIndex - 1);
 						char rangeEnd = tokenDefinition.charAt(dashIndex + 1);
 
 						if (excludeRule.contains("-")) {
 							int excludeDashIndex = excludeRule.indexOf("-");
 							char excludeStart = excludeRule.charAt(excludeDashIndex - 1);
 							char excludeEnd = excludeRule.charAt(excludeDashIndex + 1);
 							if (rangeIsInRange(excludeStart, excludeEnd, rangeStart, rangeEnd)) {
 								String replaceString = excludeRange(excludeStart, excludeEnd, rangeStart, rangeEnd);
 								String newDefinition = tokenDefinition.replace(rangeStart + "-" + rangeEnd,
 										replaceString);
 								specDefinitions.put(entry, newDefinition);
 								specNFAs.put(entry, new NFA(newDefinition));
 
 							}
 						} else {
 							char excludeChar = excludeRule.charAt(excludeRule.indexOf("^") + 1);
 							if (charIsInRange(excludeChar, rangeStart, rangeEnd)) {
 								String replaceString = excludeChar(excludeChar, rangeStart, rangeEnd);
 								String newDefinition = tokenDefinition.replace(rangeStart + "-" + rangeEnd,
 										replaceString);
 								specDefinitions.put(entry, newDefinition);
 								specNFAs.put(entry, new NFA(newDefinition));
 								// System.out.println("Replaced \"" + rangeStart
 								// + "-" + rangeEnd + " with " +replaceString);
 							}
 						}
 
 						dashIndex = tokenDefinition.indexOf("-", dashIndex + 1);
 					}
 				}
 			}
 		}
 		for (Map.Entry<String, NFA> specNFA : specNFAs.entrySet()) {
 			System.out.println("|" + specNFA.getKey() + "|");
 		}
 		for (Map.Entry<String, String> specEntry : specDefinitions.entrySet()) {
 			String entry = specEntry.getKey();
 			String definition = specEntry.getValue();
 			if (!definition.contains(" IN ")) {
 				for (Map.Entry<String, String> specEntryTarget : specDefinitions.entrySet()) {
 					definition = definition.replace(specEntryTarget.getKey(), specEntryTarget.getValue());
 					specDefinitions.put(entry, definition);
 				}
 			}
 		}
 
 		return specNFAs;
 	}
 
 	/*
 	 * This method grabs lines and splits them into (token/spec name,
 	 * definition).
 	 */
 	public HashMap<String, String> readFile(String filename) {
 		// System.out.println("Reading file...");
 		HashMap<String, String> specDefinitions = new HashMap<String, String>();
 		try {
 			FileInputStream fstream = new FileInputStream(filename);
 			DataInputStream in = new DataInputStream(fstream);
 			BufferedReader br = new BufferedReader(new InputStreamReader(in));
 			String strLine;
 
 			while ((strLine = br.readLine()) != null) {
 				String[] splitString = strLine.split(" ", 2);
 				if (splitString.length > 1) {
 					specDefinitions.put(splitString[0], splitString[1]);
 					// System.out.println("Entry: " + splitString[0] +
 					// ". Value: " + splitString[1]);
 				}
 			}
 
 			in.close();
 		} catch (FileNotFoundException e) {
 			System.out.println("File not found!");
 			e.printStackTrace();
 		} catch (IOException e) {
 			System.out.println("Couldn't read file.");
 			e.printStackTrace();
 		}
 		// System.out.println("=========Done reading file==========");
 		return specDefinitions;
 	}
 
 	public static boolean charIsInRange(char c, char rangeStart, char rangeEnd) {
 		return (c >= rangeStart && c <= rangeEnd);
 	}
 
 	public static boolean rangeIsInRange(char testStart, char testEnd, char rangeStart, char rangeEnd) {
 		return (charIsInRange(testStart, rangeStart, rangeEnd) && charIsInRange(testEnd, rangeStart, rangeEnd));
 	}
 
 	public String excludeChar(char c, char rangeStart, char rangeEnd) {
 		String fixedRange = "";
 		if (c == rangeStart) {
 			fixedRange = "" + Character.valueOf((char) (rangeStart + 1)) + "-" + rangeEnd;
 		} else if (c == rangeEnd) {
 			fixedRange = rangeStart + "-" + Character.valueOf((char) (rangeEnd - 1));
 		} else if (charIsInRange(c, rangeStart, rangeEnd)) {
 			fixedRange = rangeStart + "-" + Character.valueOf((char) (c - 1)) + Character.valueOf((char) (c + 1)) + "-"
 					+ rangeEnd;
 		}
 		return fixedRange;
 	}
 
 	public String excludeRange(char excludeStart, char excludeEnd, char rangeStart, char rangeEnd) {
 		String fixedRange = "";
 		if (excludeStart == rangeStart && excludeEnd == rangeEnd) {
 			return fixedRange;
 		}
		if (excludeStart == rangeStart) {
			fixedRange = Character.valueOf((char) (rangeStart + 1)) + "-" + rangeEnd;
 		}
		if (rangeIsInRange(excludeStart, excludeEnd, rangeStart, rangeEnd)) {
			fixedRange = rangeStart + "-" + Character.valueOf((char) (excludeStart - 1))
					+ Character.valueOf((char) (excludeEnd + 1)) + "-" + rangeEnd;
 		}
 		return fixedRange;
	}
 
 }
