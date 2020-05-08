 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
 import com.google.gson.internal.LinkedTreeMap;
 
 
 public class EdXLogAnalyzer {
 
 	/**
 	 * args
 	 * - aprs to sepcify what to show
 	 * 		a = include attepmts
 	 *		c = correct or not
 	 *		r = response (student answer)
 	 *		s = summarize response - can only do if the response is a double
 	 *				that is, # of hours spent prepping
 	 *		all are optional 
 	 *  for just time logs, do -rs
 	 *  for attempts & correctnesses -ac
 	 *  
 	 * - folder with files to analyze
 	 * 		each in .csv format
 	 * 
 	 */
 	public static void main(String[] args) {
 
 		if (args.length != 2) {
 			System.out.println("not enough args; need -aprs and foldername");
 			return;
 		}
 
 		boolean showAttempts = false;
 		boolean showCorrect = false;
 		boolean showResponse = false;
 		boolean showSummary = false;
 		int columnNumMultiplier = 0;
 
 		if (args[0].contains("a")) {
 			showAttempts = true;
 			columnNumMultiplier++;
 		}
 
 		if (args[0].contains("c")) {
 			showCorrect = true;
 			columnNumMultiplier++;
 		}
 
 		if (args[0].contains("r")) {
 			showResponse = true;
 			columnNumMultiplier++;
 		}
 
 		if (args[0].contains("s")) {
 			showSummary = true;
 		}
 
 		if (columnNumMultiplier == 0) {
 			System.out.println("nothing to print; exiting");
 			return;
 		}
 
 		File workingFolder = new File(args[1]);
 		if ((!workingFolder.exists()) || (!workingFolder.isDirectory())) {
 			System.out.println(workingFolder.getAbsolutePath() + " is not a directory or does not exist; aborting");
 		}
 
 		// get all working files
 		File[] possibleFiles = workingFolder.listFiles();
 		ArrayList<File>inFiles = new ArrayList<File>();
 		for (int i = 0; i < possibleFiles.length; i++) {
			File file = possibleFiles[i];
			if (file.getName().endsWith(".csv") && !file.getName().startsWith(".")) {
				inFiles.add(file);
 			}
 		}
 		Collections.sort(inFiles, new Comparator<File>() {
 			public int compare(File f1, File f2) {
 				return f1.getName().compareTo(f2.getName());
 			}
 		});
 		int numFiles = inFiles.size();
 
 		String[] columnHeaders = new String[numFiles * columnNumMultiplier];
 		Iterator<File> fileIt = inFiles.iterator();
 		for (int i = 0; i < columnHeaders.length;) {
 			String colBaseName = fileIt.next().getName().replaceAll(".csv", "");
 			if (showAttempts) {
 				columnHeaders[i] = colBaseName + "_A";
 				i++;
 			}
 			if (showCorrect) {
 				columnHeaders[i] = colBaseName + "_C";
 				i++;
 			}
 			if (showResponse) {
 				columnHeaders[i] = colBaseName + "_R";
 				i++;
 			}
 		}
 
 		/*
 		 * read in all the data into monster hash<by name> of arrays[indexed by column #]
 		 */
 		HashMap<String, int[]> attemptsData = new HashMap<String, int[]>();
 		HashMap<String, boolean[]> correctnessData = new HashMap<String, boolean[]>();
 		HashMap<String, String[]> responseData = new HashMap<String, String[]>();
 
 		for (int i = 0; i < inFiles.size(); i++) {
 			try {
 				BufferedReader br = new BufferedReader(new FileReader(inFiles.get(i)));
 				String line = null;
 				while ((line = br.readLine()) != null) {
 					if (line.contains("{")) {
 						line = line.replaceAll("\"\"", "\"");
 						String[] parts = line.split(",", 2);
 						String name = parts[0];
 						String json = parts[1].substring(1, parts[1].length() - 1);		// remove leading and trailing quotes
 						// if new person, set up hashes for them
 						if (!attemptsData.containsKey(name)) {
 							int[] a = new int[numFiles];
 							boolean[] c = new boolean[numFiles];
 							String[] r = new String[numFiles];
 
 							for (int j = 0; j < a.length; j++) {
 								a[j] = -1;
 								c[j] = false;
 								if (showSummary) {
 									r[j] = "-1.0";
 								} else {
 									r[j] = "";
 								}
 							}
 							attemptsData.put(name, a);
 							correctnessData.put(name, c);
 							responseData.put(name, r);
 						}
 
						Map<String, Object> jsonJavaRootObject = null;
						try {
							jsonJavaRootObject = new Gson().fromJson(json, Map.class);
						} catch (JsonSyntaxException e) {
							System.out.println("bad json");
							System.out.println("\t" + json);
						}	
 						Set<String> keys = jsonJavaRootObject.keySet();
 
 						if (showAttempts) {
 							int attempts = -1;
 							if (keys.contains("attempts")) {
 								attempts = (int)Double.parseDouble(jsonJavaRootObject.get("attempts").toString());
 							}
 							attemptsData.get(name)[i] = attempts;
 						}
 
 						if (showCorrect) {
 							boolean correct = false;
 							if (keys.contains("correct_map")) {
 								LinkedTreeMap<String, Map> answer = (LinkedTreeMap<String, Map>)jsonJavaRootObject.get("correct_map");
 								if (answer.keySet().size() > 0) {
 									String probName = answer.keySet().iterator().next();
 									LinkedTreeMap<String, Object> correctMapParts = (LinkedTreeMap<String, Object>)answer.get(probName);
 									if (correctMapParts.containsKey("correctness") && (correctMapParts.get("correctness") != null)) {
 										if (correctMapParts.get("correctness").toString().equals("correct")) correct = true;
 									}
 								}
 							}
 							correctnessData.get(name)[i] = correct;
 						}
 
 						if (showResponse) {
 							String response;
 							if (showSummary) {
 								response = "-1.0";
 							} else {
 								response = "";
 							}
 							if (keys.contains("student_answers")) {
 								LinkedTreeMap<String, String> answer = (LinkedTreeMap<String, String>)jsonJavaRootObject.get("student_answers");
 								if (answer.keySet().size() > 0) {
 									String probName = answer.keySet().iterator().next();
 									response = answer.get(probName).trim();
 								}
 							}
 							responseData.get(name)[i] = response;
 						}
 					}
 				}
 				br.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 
 		// print and summarize data
 		System.out.print("Name,");
 		for (int i = 0; i < columnHeaders.length; i++) {
 			System.out.print(columnHeaders[i] + ",");
 		}
 		System.out.print("\n");
 
 		DataPoint[] summaries = new DataPoint[numFiles];
 		if (showSummary) {
 			for (int i = 0; i < numFiles; i++) {
 				summaries[i] = new DataPoint();
 			}
 		}
 
 		Set<String> names = null;
 		if (showAttempts) {
 			names = attemptsData.keySet();
 		} else if (showCorrect) {
 			names = correctnessData.keySet();
 		} else if (showResponse) {
 			names = responseData.keySet();
 		}
 		if (names != null) {
 			Iterator<String> nameIt = names.iterator();
 			while (nameIt.hasNext()) {
 				String name = nameIt.next();
 				int[] attempts = null;
 				boolean[] correctnesses = null;
 				String[] responses = null;
 				if (showAttempts) attempts = attemptsData.get(name);
 				if (showCorrect) correctnesses = correctnessData.get(name);
 				if (showResponse) responses = responseData.get(name);
 
 				System.out.print(name + ",");
 				for (int i = 0; i < numFiles; i++) {
 					if (showAttempts) System.out.print(attempts[i] + ",");
 					if (showCorrect) {
 						if (correctnesses[i]) {
 							System.out.print("Y,");
 						} else {
 							System.out.print("N,");
 						}
 					}
 					if (showResponse) {
 						/*
 						 * if you're showing summaries, then the responses must be times
 						 * so need to deal with fractions 
 						 */
 						if (showSummary) {
 							System.out.print(parseWithFractions(responses[i].trim()) + ",");
 						} else {
 							System.out.print(responses[i] + ",");
 						}
 					}
 
 					// summarize responses if doubles (prep time estimates)
 					if (showSummary) {
 						double value = parseWithFractions(responses[i].trim());
 						if (value == -1.0f) {
 							summaries[i].numBad++;
 						} else {
 							summaries[i].numValid++;
 							summaries[i].totalValid += value;
 						}
 					}
 				}
 				System.out.print("\n");	
 			}
 
 			if (showSummary) {
 				System.out.print("TotalBad,");
 				for (int i = 0; i < numFiles; i++) {
 					if (showAttempts) System.out.print(",");
 					if (showCorrect) System.out.print(",");
 					System.out.print(summaries[i].numBad + ",");
 				}
 				System.out.print("\n");
 
 				System.out.print("TotalValid,");
 				for (int i = 0; i < numFiles; i++) {
 					if (showAttempts) System.out.print(",");
 					if (showCorrect) System.out.print(",");
 					System.out.print(summaries[i].numValid + ",");
 				}
 				System.out.print("\n");
 
 				System.out.print("AverageValid,");
 				for (int i = 0; i < numFiles; i++) {
 					if (showAttempts) System.out.print(",");
 					if (showCorrect) System.out.print(",");
 					System.out.print(summaries[i].getAverageValid() + ",");
 				}
 				System.out.print("\n");
 			}
 
 		}
 	}
 
 	private static double parseWithFractions(String s) {
 		double value = -1.0f;
 		if (s.contains("/")) {
 			String[] parts = s.split("/");
 			try {
 				double numerator = Double.parseDouble(parts[0]);
 				double denominator = Double.parseDouble(parts[1]);
 				value = numerator/denominator;
 			} catch (NumberFormatException e) {}	// if unreadable, give -1
 		} else {
 			try {
 				value = Double.parseDouble(s);
 			} catch (NumberFormatException e) {}	// if unreadable, give -1
 		}
 		return value;
 	}
 
 }
