 package de.tu_berlin.textmining.translator.prototypes;
 
 import java.io.FileWriter;
 import java.io.FileReader;
 import java.io.IOException;
import java.util.List;
import java.util.Locale;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.StringTokenizer;
 
 import com.google.gson.Gson;
 import java.io.BufferedReader;
 import java.util.ArrayList;
 
 public class Dictionary {
 	/** holds the dictionary german->english: HashMap<German, English> */
 	private Map<String, ArrayList<String>> translationData = new HashMap<String, ArrayList<String>>();
 	/** holds the additional information for german: HashMap<German, Attribute> */
 	private Map<String, ArrayList<String>> attributeData = new HashMap<String, ArrayList<String>>();
 
 	/**
 	 * Parses a dictionary file from dict.cc into HashMap NOTE: Ignores any
 	 * Phrases in (..) [..]; Stores Phrases in {..} as Attribute
 	 * 
 	 * @param path
 	 *          file path
 	 */
 	public void parseDictFile(String path) {
 		BufferedReader br = null;
 		try {
 			br = new BufferedReader(new FileReader(path));
 			String aLine;
 			String[] aTempLine;
 			String pattern1 = "\\([^\\)]*\\)"; // replace (..) with empty string
 			String pattern2 = "\\[[^\\]]*\\]"; // replace [..)]with empty string
 			String pattern3 = "\\{[^\\}]*\\}"; // replace {..} with empt string
 			String pattern4 = "[^\\{^\\}]*\\{"; // replace ..{ with empty string
 			String pattern5 = "\\}[^\\{]*"; // replace }.. with empty string
 			String eng = "";
 			String att = "";
 			String ger = "";
 			ArrayList<String> tempEngList;
 			ArrayList<String> tempAttList;
 			String[] attList;
 			int engIndex = 0;
 			while (br.ready()) {
 				engIndex = 0;
 				att = "";
 				aLine = br.readLine();
 				// split a line from dict.cc dictionary into german and english part
 				aTempLine = aLine.split("::", 2);
 				if (aTempLine.length == 2) {
 					ger = aTempLine[0].replaceAll(pattern1, ""); // remove all (..)
 					ger = ger.replaceAll(pattern2, ""); // remove all [..]
 					ger = ger.replaceAll(pattern3, ""); // remove all {..}
 					ger = ger.trim(); // remove leading and tailing whitespaces
 					ger = ger.toLowerCase();
 					eng = aTempLine[1].replaceAll(pattern1, ""); // remove all (..)
 					eng = eng.replaceAll(pattern2, ""); // remove all [..]
 					eng = eng.replaceAll(pattern3, ""); // remove all {..}
 					eng = eng.trim(); // remove leading and tailing whitespaces
 					if ((ger.compareToIgnoreCase("") != 0) && (eng.compareToIgnoreCase("") != 0)) {
 						if ((aTempLine[0].contains("{") == true)
 								&& (aTempLine[0].substring(aTempLine[0].indexOf('{')).contains("}") == true)) {
 							att = aTempLine[0].replaceAll(pattern4, "{"); // replace "...{"
 																														// with "{"
 							att = att.replaceAll(pattern5, ""); // replace "}.." with ""
 							attList = att.substring(1).split("\\{");
 							for (int i = 0; i < attList.length; i++) {
 								att = attList[i].trim(); // remove leading and tailing
 																					// whitespaces
 								if (translationData.containsKey(ger) == true) {
 									tempEngList = translationData.get(ger);
 									tempAttList = attributeData.get(ger);
 									if (tempEngList.contains(eng) == true) {
 										engIndex = tempEngList.indexOf(eng);
 										if (tempAttList.get(engIndex).compareTo(att) != 0) {
 											tempEngList.add(eng);
 											tempAttList.add(att);
 											translationData.put(ger, tempEngList);
 											attributeData.put(ger, tempAttList);
 										}
 									} else {
 										tempEngList.add(eng);
 										tempAttList.add(att);
 										translationData.put(ger, tempEngList);
 										attributeData.put(ger, tempAttList);
 									}
 								} else {
 									tempEngList = new ArrayList<String>();
 									tempAttList = new ArrayList<String>();
 									tempEngList.add(eng);
 									tempAttList.add(att);
 									translationData.put(ger, tempEngList);
 									attributeData.put(ger, tempAttList);
 								}
 							}
 						} else {
 							if (translationData.containsKey(ger) == true) {
 								tempEngList = translationData.get(ger);
 								tempAttList = attributeData.get(ger);
 								if (tempEngList.contains(eng) == true) {
 									engIndex = tempEngList.indexOf(eng);
 									if (tempAttList.get(engIndex).compareTo(att) != 0) {
 										tempEngList.add(eng);
 										tempAttList.add(att);
 										translationData.put(ger, tempEngList);
 										attributeData.put(ger, tempAttList);
 									}
 								} else {
 									tempEngList.add(eng);
 									tempAttList.add(att);
 									translationData.put(ger, tempEngList);
 									attributeData.put(ger, tempAttList);
 								}
 							} else {
 								tempEngList = new ArrayList<String>();
 								tempAttList = new ArrayList<String>();
 								tempEngList.add(eng);
 								tempAttList.add(att);
 								translationData.put(ger, tempEngList);
 								attributeData.put(ger, tempAttList);
 							}
 						}
 					}
 				}
 			}
 		} catch (Exception ex) {
 			throw new RuntimeException(ex);
 		} finally {
 			try {
 				br.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * reads the Dictionary from a JSON file into HashMap
 	 * 
 	 * @param path
 	 *          : total file path
 	 */
 	public void parseJSONFile(String path) {
 		BufferedReader br = null;
 		try {
 			Gson gsonParser = new Gson();
 			br = new BufferedReader(new FileReader(path));
 			String aLine = br.readLine();
 			while (aLine != null) {
 				DictToken aToken = gsonParser.fromJson(aLine, DictToken.class);
 				translationData.put(aToken.getGerman(), aToken.getEnglish());
 				attributeData.put(aToken.getGerman(), aToken.getAttribute());
 				aLine = br.readLine();
 			}
 			br.close();
 		} catch (IOException ioe) {
 			throw new RuntimeException(ioe);
 		} finally {
 			if (br != null) {
 				try {
 					br.close();
 				} catch (IOException ioe) {
 					ioe.printStackTrace();
 				}
 			}
 		}
 	}
 
 	/**
 	 * Creates a JSON file of the dictionary
 	 * 
 	 * @param path
 	 *          : full file path
 	 */
 	public void createJSONFile(String path) {
 		try {
 			String ger;
 			ArrayList<String> att;
 			ArrayList<String> eng;
 			DictToken aToken;
 			Gson gsonParser = new Gson();
 			FileWriter writer = new FileWriter(path);
 			for (Map.Entry<String, ArrayList<String>> anEntry : translationData.entrySet()) {
 				ger = anEntry.getKey();
 				att = attributeData.get(ger);
 				eng = anEntry.getValue();
 				aToken = new DictToken(ger, att, eng);
 				writer.write(gsonParser.toJson(aToken) + System.getProperty("line.separator"));
 			}
 			writer.close();
 		} catch (Exception ex) {
 			throw new RuntimeException(ex);
 		}
 	}
 	
 	public String translateSentence(String sentence) {
 		StringTokenizer strTok = new StringTokenizer(sentence);
 		StringBuilder strBld = new StringBuilder();
 		while (strTok.hasMoreTokens()) {
 			String word = strTok.nextToken().toLowerCase();
 			String[] transWords = this.translateWord(word);
 			if (transWords != null) {
 				strBld.append(Dictionary.wordArrayToString(transWords));
 			}
 			else {
 				strBld.append('[');
 				strBld.append(word);
 				strBld.append(']');
 			}
 			strBld.append(" ");
 		}
 		
 		return strBld.toString();
 	}
 	
 	public String[] translateWord(String word) {
 		if (this.translationData.containsKey(word)) {
 			return this.translationData.get(word).toArray(new String[0]);
 		}
 		return null;
 	}
 	
 	private static String wordArrayToString(String[] words) {
 		StringBuilder strBld = new StringBuilder();
 		if (words.length < 2) {
 			strBld.append(words[0]);
 		}
 		else {
 			strBld.append('(');
 			for (int i = 0; i < words.length; i++) {
 				strBld.append(words[i]);
 				if (i < words.length - 1) {
 					strBld.append('/');
 				}
 			}
 			strBld.append(')');
 		}
 		
 		return strBld.toString();
 	}
 }
