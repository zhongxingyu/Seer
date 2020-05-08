 /**
  * File: TaggerThread.java
  * Date: Apr 17, 2012
  * Author: Morteza Ansarinia <ansarinia@me.com>
  */
 package fuschia.tagger.common;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import opennlp.tools.cmdline.postag.POSModelLoader;
 import opennlp.tools.postag.POSModel;
 import opennlp.tools.postag.POSTaggerME;
 import opennlp.tools.tokenize.WhitespaceTokenizer;
 
 import fuschia.tagger.common.DocumentRepository;
 
 public class RepositoryCreator extends Thread {
 
 	private String strWorkingDirectory;
 	public DocumentRepository results;
 
 	public static void main(String[] args) {
 		
 		try {
 			RepositoryCreator creator = new RepositoryCreator("/Volumes/Personal HD/Friends/Salar/");
 			creator.start();
 			while(creator.isAlive());
 			
 			creator.results.saveToFile("/Users/morteza/911.cmap.gz");
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public RepositoryCreator(String strWorkingDirectory) {
 		super();
 		this.strWorkingDirectory = new String(strWorkingDirectory);
 		results = null;
 	}
 
 	public List<File> getAllFiles(String rootPath) {
 
 		List<File> result = new ArrayList<File>();
 
 		File[] files = new File(rootPath).listFiles();
 
 		String filenameRegex = "[A-Z]{2,4}\\d{1,3}[BC]?(\\sunsure)?-Q[0-9]*\\.txt";
 
 		for (File file : files) {
 			// Directories
 			if (file.isDirectory() && file.exists() && file.canRead()) {
 				List<File> children = getAllFiles(file.getPath());
 				result.addAll(children);
 				continue;
 			}
 
 			// Files
 			if (file.isFile() && file.exists() && file.canRead()) {
 				if (Pattern.matches(filenameRegex, file.getName())) {
 					result.add(file);
 				}
 			}
 		}
 
 		return result;
 	}
 
 	public void run() {
 				try {
 
 					results = new DocumentRepository();
 					POSModel model = new POSModelLoader().load(new File("resources/en-pos-maxent.bin"));
 					POSTaggerME tagger = new POSTaggerME(model);
 
 					List<File> files = getAllFiles(strWorkingDirectory);
 
 					System.out.println("Num of files: " + files.size());
 
 					if (files == null || files.size() == 0) {
 						results = null;
 						return;
 					}
 
 					sleep(10); // FIXME: Just to update logs view
 
 					int index = 0;
 					
 					int surveyId = 0;
 					
 					int[][] verbCounts =new int[56][7];
 					int[][] adjectiveCounts =new int[56][7];
 					
 					Pattern questionNumberPattern = Pattern.compile("[^0-9]+[0-9]+[^0-9]+([0-9]+)[^0-9]+");
 					int questionNumber = 0;
 					for (File file : files) {
 
 						index++;
 						if(index%1000==0)
 							System.out.println("processing "+ (index) + " of " +files.size());
 						Scanner lineScanner = new Scanner(new FileReader(file));
 
 						Matcher m = questionNumberPattern.matcher(file.getName());
 						
 						if(m.find()){
 							questionNumber = Integer.valueOf(m.group(1));
 							// mark question for writing to the output file
 						}
 						m.reset();
 						
 						if (file.getPath().indexOf("SURVEY 1") != -1)
 							surveyId = 1;
 						else if (file.getPath().indexOf("SURVEY 2") != -1)
 							surveyId = 2;
 						else if (file.getPath().indexOf("SURVEY 3") != -1)
 							surveyId = 3;
 						else
 							surveyId = 0;
 
 						String txt = new String();
 						while (lineScanner.hasNextLine()) {
 							txt = txt + lineScanner.nextLine();
 						}
 
 						lineScanner.close();
 						lineScanner = null;
 						String tokens[] = WhitespaceTokenizer.INSTANCE.tokenize(txt);
 						String[] tags = tagger.tag(tokens);
 
 
 						if (surveyId>0) {
 
 							verbCounts[questionNumber][surveyId*2-1] += tags.length;
 							adjectiveCounts[questionNumber][surveyId*2-1] += tags.length;
 							for (int i=0;i<tags.length;i++) {
 								if (tags[i].startsWith("VB")) {
 									verbCounts[questionNumber][surveyId*2]++;
 								} else if (tags[i].startsWith("JJ")
 											|| tags[i].startsWith("RB")
 											|| tags[i].startsWith("WRB")) {
 									adjectiveCounts[questionNumber][surveyId*2]++;
 								}
 							}							
 						}
 						
 						// Create and add appropriate document object
						String documentId = ((surveyId==0)?".":"s"+String.valueOf(surveyId)+".")
 								+ file.getName().substring(0,file.getName().length() - 4);
 						results.addDocument(documentId, new Document(file.getName(), tokens, tags));
 
 					}
 
 					System.out.println("Writing Construal CSV output...");
 					
 					BufferedWriter fVerbs = new BufferedWriter(new FileWriter(new File("/Users/morteza/verbs.csv")));
 					BufferedWriter fAdjectives = new BufferedWriter(new FileWriter(new File("/Users/morteza/adjectives.csv")));
 
 					for (int q=0; q< 56 ; q++) {
 						String vLine = "";
 						String aLine="";
 						if (verbCounts[q][1]+verbCounts[q][3]+verbCounts[q][5]>0) {
 							vLine +=verbCounts[q][1]+","; // tag count (s1)
 							vLine +=verbCounts[q][2]+",";
 							vLine +=verbCounts[q][3]+","; // tag count (s2)
 							vLine +=verbCounts[q][4]+",";
 							vLine +=verbCounts[q][5]+","; // tag count (s3)
 							vLine +=verbCounts[q][6];							
 
 							fVerbs.write(String.valueOf(q) + "," + vLine);
 							fVerbs.newLine();
 							fVerbs.flush();
 						}
 						if (adjectiveCounts[q][1]+adjectiveCounts[q][3]+adjectiveCounts[q][5]>0) {
 							aLine +=adjectiveCounts[q][1]+",";
 							aLine +=adjectiveCounts[q][2]+",";
 							aLine +=adjectiveCounts[q][3]+",";
 							aLine +=adjectiveCounts[q][4]+",";
 							aLine +=adjectiveCounts[q][5]+",";
 							aLine +=adjectiveCounts[q][6];
 							fAdjectives.write(String.valueOf(q) + "," + aLine);
 							fAdjectives.newLine();
 							fAdjectives.flush();
 						}
 					}
 					
 					fVerbs.flush();
 					fAdjectives.flush();
 					fVerbs.close();
 					fAdjectives.close();
 
 					System.out.println("Finished!");
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 	}
 }
