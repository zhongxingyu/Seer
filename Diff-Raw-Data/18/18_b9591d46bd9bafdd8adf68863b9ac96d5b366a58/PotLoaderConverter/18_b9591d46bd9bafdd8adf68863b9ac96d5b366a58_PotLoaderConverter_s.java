 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 
 /**
  * @author pdias
  * 30/07/2013
  *
  */
 public class PotLoaderConverter {
 
     private static final String VERSION = "0.1.3";
 	private static final String COPY_YEARS = "2013";
 
 	public static String replaceFileExtension(String path, String newExtension) {
         int lastDotPostion = path.lastIndexOf('.');
         String st = (lastDotPostion != -1) ? (path.substring(0, lastDotPostion)) : path;
         return st + "." + newExtension;
     }
 
     enum StateEnum {
     	NONE, MSG_ID, MSG_STRING;
     };
 
 	public static void generateFileToGoogleTranslate(String[] args) {
 		StateEnum state = StateEnum.NONE;
 		
 		BufferedReader reader = null;
 		BufferedWriter writer = null;
 		
 		String inFileStr = args[0];
 		
 		File inFile = new File(inFileStr);
 		
 		String sufix = "gen";
 		if (args.length > 1) {
 			sufix = args[1];
 		}
 		
 		String outFileStr = replaceFileExtension(inFile.getName(), sufix + ".po.txt");
 		File outFile = new File(inFile.getParentFile() , outFileStr);
 		
 		try {
 			reader = new BufferedReader(new FileReader(inFile));
 			writer = new BufferedWriter(new FileWriter(outFile));
 			
 			Pattern msgidPat = Pattern.compile("msgid +\"(.+?)\"");
 			// Pattern msgidPatTwo = Pattern.compile("%[\\w\\.]+");
 			String line;
 			boolean headerEmptyMsgId = true;
 			while ((line = reader.readLine()) != null) {
 				// StateEnum pevState = state;
 				String lineToW = "";
 				switch (state) {
 					case NONE:
 						// if (line.startsWith("msgid \"")) {
 						if (line.matches("msgid +\"(.*?)\"")) {
 							// if (line.equals("msgid \"\"") && headerEmptyMsgId) {
 							if (line.matches("msgid +\"\"") && headerEmptyMsgId) {
 								headerEmptyMsgId = false;
 								break;
 							}
 							
 							state = StateEnum.MSG_ID;
 							// if (!line.equals("msgid \"\"")) {
 							if (!line.matches("msgid +\"\"")) {
 								Matcher matcher = msgidPat.matcher(line);
 								matcher.matches();
 								lineToW = matcher.group(1);
 								lineToW = lineToW.replaceAll("%[\\w\\.]+", "€€");
 							}
 							else {
 								lineToW = "";
 							}
 							writer.write("##-##\n");
 							writer.write(lineToW + "\n");
 						}
 						break;
 					case MSG_ID:
 						// if (line.startsWith("\"")) {
						if (line.matches(" +\"(.*?)\"")) {
 							// lineToW = line.substring(1, line.length() - 2);
							lineToW = line.replaceFirst("^ +\"", "").replaceFirst("\"$", "");
 							lineToW = lineToW.replaceAll("%[\\w\\.]+", "€€");
 							writer.write(lineToW + "\n");
 						}
 						// else if (line.startsWith("msgstr \"")) {
 						else if (line.matches("msgid +\"(.*?)\"")) {
 							state = StateEnum.MSG_STRING;
 						}
 						else {
 							state = StateEnum.NONE;
 						}
 						break;
 					case MSG_STRING:
 						// if (line.startsWith("\"")) {
 						if (line.matches(" +\"(.*?)\"")) {
 							// lineToW = line.substring(1, line.length() - 2);
							lineToW = line.replaceFirst("^ +\"", "").replaceFirst("\"$", "");
 							writer.write(lineToW + "\n");
 						}
 						else {
 							state = StateEnum.NONE;
 						}
 						break;
 	
 					default:
 						break;
 				}
 			}
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 		finally {
 			if (reader != null) {
 				try {
 					reader.close();
 				} 
 				catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 			if (writer != null) {
 				try {
 					writer.close();
 				} 
 				catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		
 		System.out.println("Output written to " + outFile.getName() + ".");
 	}
 
 	public static void generatePOFromGoogleTranslate(String[] args) {
 		StateEnum state = StateEnum.NONE;
 		
 		BufferedReader reader = null;
 		BufferedReader reader2 = null;
 		BufferedWriter writer = null;
 		
 		String inFileStr = args[0];
 		String inFile2Str = args[1];
 		
 		File inFile = new File(inFileStr);
 		File inFile2 = new File(inFile2Str);
 		
 		String sufix = "gen";
 		if (args.length > 2) {
 			sufix = args[2];
 		}
 
 		String outFileStr = replaceFileExtension(inFile.getName(), sufix + ".mod.po");
 		File outFile = new File(inFile.getParentFile() , outFileStr);
 		
 		try {
 			reader = new BufferedReader(new FileReader(inFile));
 			reader2 = new BufferedReader(new FileReader(inFile2));
 			writer = new BufferedWriter(new FileWriter(outFile));
 			
 			// Pattern msgidPat = Pattern.compile("msgid \"(.+?)\"");
 			Pattern msgidPatTwo = Pattern.compile("%[\\w\\.]+");
 			Pattern msgidPatThree = Pattern.compile("€ ?€");
 			
 			String line;
 			String line2;
 			boolean headerEmptyMsgId = true;
 			ArrayList<String> replacementsList = new ArrayList<>();
 			ArrayList<String> toReplaceList = new ArrayList<>();
 			int index = 0; 
 			int replacementsCount = 0; 
 			int toReplaceCount = 0; 
 			reader2.readLine();
 
 			while ((line = reader.readLine()) != null) {
 				switch (state) {
 					case NONE:
 						if (line.matches("msgid +\"(.*?)\"")) {
 							if (line.matches("msgid +\"\"") && headerEmptyMsgId) {
 								headerEmptyMsgId = false;
 								writer.write(line + "\n");
 								break;
 							}
 							
 							state = StateEnum.MSG_ID;
 							if (!line.matches("msgid +\"\"")) {
 								Matcher matcher = msgidPatTwo.matcher(line);
 								while (matcher.find()) {
 									String m1 = matcher.group(0);// line.substring(matcher.start(), matcher.end());
 									System.out.println(m1);
 									replacementsList.add(m1);
 									replacementsCount++;
 								}
								line = "#, fuzzy\n" + line;
 							}
 						}
 						writer.write(line + "\n");
 						break;
 					case MSG_ID:
						if (line.matches(" +\"(.*?)\"")) {
 							Matcher matcher = msgidPatTwo.matcher(line);
 							while (matcher.find()) {
 								String m1 = matcher.group(0);// line.substring(matcher.start(), matcher.end());
 								System.out.println(m1);
 								replacementsList.add(m1);
 								replacementsCount++;
 							}
 						}
 						else if (line.matches("msgstr +\"(.*?)\"")) {
 							state = StateEnum.MSG_STRING;
 							// let us read from the translation
 							boolean first = true;
 							line = "";
 							while ((line2 = reader2.readLine()) != null) {
 								if (line2.replaceAll(" ", "").startsWith("##-##")) {
 									break;
 								}
 								Matcher matcher = msgidPatThree.matcher(line2);
 //								System.out.println(line2);
 								while (matcher.find()) {
 									String m1 = matcher.group(0);// line.substring(matcher.start(), matcher.end());
 									toReplaceList.add(m1);
 									toReplaceCount++;
 									System.out.println(m1 + "  " + "  #" + toReplaceList.size() + ":" + replacementsList.size() + ":" + index);
 									index++;
 								}
 								
 								System.out.println(">>  " + "  #" + toReplaceList.size() + ":" + replacementsList.size() + ":" + index);
 								for (int i = 0; i < replacementsList.size(); i++) {
 									line2 = line2.replaceFirst("€ ?€", replacementsList.get(i));
 								}
 								
 								line += (first ? "msgstr ": "\n") + "\"" + line2 + "\"";
 								first = false;
 							}
 						}
 						else {
 							state = StateEnum.NONE;
 						}
 						writer.write(line + "\n");
 						break;
 					case MSG_STRING:
						if (line.matches(" +\"(.*?)\"")) {
 							// let us read from the translation
 							boolean first = true;
 							line = "";
 							while ((line2 = reader2.readLine()) != null) {
 								if (line2.replaceAll(" ", "").startsWith("##-##")) {
 									break;
 								}
 								Matcher matcher = msgidPatThree.matcher(line2);
 //								System.out.println(line2);
 								while (matcher.find()) {
 									String m1 = matcher.group(0);// line.substring(matcher.start(), matcher.end());
 									toReplaceList.add(m1);
 									toReplaceCount++;
 									System.out.println(m1 + "  " + "  #" + toReplaceList.size() + ":" + replacementsList.size() + ":" + index);
 									index++;
 								}
 
 								System.out.println("  " + "  #" + toReplaceList.size() + ":" + replacementsList.size() + ":" + index);
 								for (int i = 0; i < replacementsList.size(); i++) {
 									line2 = line2.replaceFirst("€ ?€", replacementsList.get(i));
 								}
 
 								line += (first ? "": "\n") + "\"" + line2 + "\"";
 								first = false;
 							}
 						}
 						else {
 							state = StateEnum.NONE;
 							replacementsList.clear();
 							toReplaceList.clear();
 							index = 0;
 						}
 						writer.write(line + "\n");
 						break;
 	
 					default:
 						break;
 				}
 			}
 			System.out.println(replacementsCount + " : " + toReplaceCount);
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 		finally {
 			if (reader != null) {
 				try {
 					reader.close();
 				} 
 				catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 			if (reader2 != null) {
 				try {
 					reader2.close();
 				} 
 				catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 			if (writer != null) {
 				try {
 					writer.close();
 				} 
 				catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		System.out.println("Output written to " + outFile.getName() + ".");
 	}
 
 	private static void showHelpAndExit(int exitCode) {
 		StringBuilder sb = new StringBuilder();
 		sb.append(PotLoaderConverter.class.getSimpleName() + " v" + VERSION);
 		sb.append("\n");
 		sb.append("\u00A9" + COPY_YEARS + " FEUP-LSTS, All Rights Reserved");
 		sb.append("\n");
 		sb.append("Usage:");
 		sb.append("\n");
 		sb.append("[help]\tPrints this help.");
 		sb.append("\n");
 		sb.append("to-google POT_FILE [OUTPUT_SUFIX]");
 		sb.append("\n\tCreates a TXT output file to be translated by Google Translate.");
 		sb.append("\n");
 		sb.append("from-google POT_FILE GOOLE_TRANSLATED_FILE [OUTPUT_SUFIX]");
 		sb.append("\n\tUses the Google Translated file and generate a PO file.");
 		sb.append("\n");
 		
 		System.out.println(sb.toString());
 		System.exit(exitCode);
 	}
 	
 	public static void main(String[] args) {
 		
 		if (args.length == 0 || "help".equalsIgnoreCase(args[0])) {
 			showHelpAndExit(0);
 		}
 		
 		String ag1 = args.length == 0 ? "" : args[0];
 
 //		System.out.println(args.length);
 		
 		String[] argsFl = args.length == 0 ? new String[0] : Arrays.copyOfRange(args, 1, args.length);
 		
 		switch (ag1) {
 			case "to-google":
 				if (argsFl.length == 0 || !(new File(argsFl[0])).exists()) {
 					showHelpAndExit(1);
 				}
 				generateFileToGoogleTranslate(argsFl);
 				break;
 			case "from-google":
 				if (argsFl.length < 2 || !(new File(argsFl[0])).exists() || !(new File(argsFl[1])).exists()) {
 					showHelpAndExit(2);
 				}
 				generatePOFromGoogleTranslate(argsFl);
 				break;
 			default:
 //				Pattern msgidPatTwo = Pattern.compile("%[\\w\\.]+");
 //				String line = "df %msg1 sd%msg2 s %msg3";
 //				Matcher matcher = msgidPatTwo.matcher(line);
 //				while (matcher.find()) {
 //					String m1 = matcher.group(0);// line.substring(matcher.start(), matcher.end());
 //					System.out.println(m1);
 //				}
 				showHelpAndExit(0);
 				break;
 		}
 	}
 }
