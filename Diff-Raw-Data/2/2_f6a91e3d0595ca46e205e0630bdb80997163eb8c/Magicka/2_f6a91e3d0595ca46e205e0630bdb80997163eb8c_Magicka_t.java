 package hueper.codejam2011.magicka;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.PrintWriter;
 
 public class Magicka {
 	
 	private static int[] baseLookup = new int[255];
 	
 	static {
 		baseLookup['Q'] = 1;
 		baseLookup['W'] = 2;
 		baseLookup['E'] = 4;
 		baseLookup['R'] = 8;
 		baseLookup['A'] = 16;
 		baseLookup['S'] = 32;
 		baseLookup['D'] = 64;
 		baseLookup['F'] = 128;
 	}
 	
 	public static void parseInput(String filenameIn, String filenameOut) throws Exception {
 		BufferedReader r = new BufferedReader(new FileReader(filenameIn));
 		PrintWriter w = new PrintWriter(new FileWriter(filenameOut));
 		
 		if (r.ready()) {
 			String numTestCasesLine = r.readLine();
 			int numTestCases = Integer.parseInt(numTestCasesLine);
 			for (int testCaseIndex = 0; testCaseIndex < numTestCases; testCaseIndex++) {
 				// handle each case
 				String testCaseLine = r.readLine();
 				String[] testCaseInput = testCaseLine.split(" ");
 				int lineIndex = 0; // next element in line
 				int numCombination = Integer.parseInt(testCaseInput[lineIndex++]);
 				char[] combinationLookup = new char[257];
 				for (int combinersIndex = 0; combinersIndex < numCombination; combinersIndex++) {
 					String combination = testCaseInput[lineIndex++];
 					// each combination has 3 chars
 					char base1 = combination.charAt(0);
 					char base2 = combination.charAt(1);
 					char result = combination.charAt(2);
 					combinationLookup[baseLookup[base1] + baseLookup[base2]] = result;
 				}
 				int numOpposing = Integer.parseInt(testCaseInput[lineIndex++]);
 				boolean[] opposingLookup = new boolean[257];
 				for (int opposingIndex = 0; opposingIndex < numOpposing; opposingIndex++) {
 					String opossing = testCaseInput[lineIndex++];
 					// each opposing has 2 chars
 					char base1 = opossing.charAt(0);
 					char base2 = opossing.charAt(1);
 					opposingLookup[baseLookup[base1] + baseLookup[base2]] = true;
 				}
 				
 				int numInvokation = Integer.parseInt(testCaseInput[lineIndex++]);
 				String invokation = testCaseInput[lineIndex++];
 				
 				String result = "";
 				
 				for (int invokationIndex = 0; invokationIndex < numInvokation; invokationIndex++) {
 					result += invokation.charAt(invokationIndex);
 					int resultLength = result.length();
 					if (resultLength >= 2) {
 						// check last to chars for combination
 						int lastElemValue = baseLookup[result.charAt(resultLength - 1)];
 						int secondLastElemValue = baseLookup[result.charAt(resultLength - 2)];
						if (lastElemValue != 0 && secondLastElemValue != 0 && combinationLookup[lastElemValue + secondLastElemValue] != 0) {
 							// if combination found, replace them with combination result and done
 							result = result.substring(0, resultLength - 2);
 							result += combinationLookup[lastElemValue + secondLastElemValue];
 						} else {
 							// else, check for opposing
 							for (int resultIndex = 0; resultIndex < resultLength; resultIndex++) {
 								int currentElemValue = baseLookup[result.charAt(resultIndex)];
 								if (currentElemValue != 0 && opposingLookup[lastElemValue + currentElemValue]) {
 									// clear the result
 									result = "";
 									break;
 								}
 							}
 						}
 					}
 				}
 				
 				// ... and output result
 				String outString = "Case #" + (testCaseIndex + 1) + ": ";
 				//debug:
 				//outString += "C: " + numCombination + ", D: " + numOpposing + ", N: " + numInvokation + ", result: ";
 				
 				outString += "[";
 				int resultLength = result.length();
 				for (int resultIndex = 0; resultIndex < resultLength; resultIndex++) {
 					outString += result.charAt(resultIndex);
 					if (resultIndex < resultLength - 1) {
 						outString += ", ";
 					}
 				}
 				outString += "]";
 				System.out.println(outString);
 				w.println(outString);
 			}
 		}
 		w.flush();
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) throws Exception {
 		Magicka.parseInput(args[0], args[1]);
 	}
 
 }
