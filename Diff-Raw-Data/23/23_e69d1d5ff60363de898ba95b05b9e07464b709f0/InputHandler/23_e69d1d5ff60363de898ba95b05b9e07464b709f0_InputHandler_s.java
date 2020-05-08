 package enduro.racer;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.regex.Pattern;
 
 import enduro.racer.comparators.RunnerCheckTotalTimeMax;
 import enduro.racer.comparators.RunnerLapsComparator;
 import enduro.racer.comparators.RunnerNumberComparator;
 import enduro.racer.comparators.RunnerStageComparator;
 import enduro.racer.comparators.RunnerTotalTimeComparator;
 import enduro.racer.configuration.ConfigParser;
 import enduro.racer.printer.LapRacePrinter;
 import enduro.racer.printer.RacerPrinter;
 import enduro.racer.printer.SortedLapRacePrinter;
 import enduro.racer.printer.StageRacePrinter;
 import enduro.racer.printer.SortedStageRacePrinter;
 
 /**
  * This class has a number of public void functions that adds name files, finish time files and start time files
  * and adds them to internal lists. This class parses these files and gives as output the final output file content.
  */
 public class InputHandler {
 
 	String[] headerInformation;
 	private TreeMap<Integer, ArrayList<String>> nameFileLocations = new TreeMap<Integer, ArrayList<String>>();
 	private TreeMap<Integer, ArrayList<String>> finishFileLocations = new TreeMap<Integer, ArrayList<String>>();
 	private TreeMap<Integer, ArrayList<String>> startFileLocations = new TreeMap<Integer, ArrayList<String>>();
 	HashMap<Integer, Racer> racerList;
 	ArrayList<RacerSorter> groups;
 	
 	//these two variables are for debugging purposes
 	private String error = null;
 	private RacerPrinter debugPrinter;
 	/**
 	 * Adds a new name file to the handler.
 	 * The name file has an optional parameter "stage" (should be 1).
 	 * 
 	 * This file is only handled by print. No errors are passed in any form and no file checkups if the file exists is done.
 	 * 
 	 * !!This application only reads a single name file from stage 1 at this moment - nothing else is read or accepted.
 	 * 
 	 * @param location the file location
 	 * @param stage what stage the file is in
 	 */
 	public void addNameFile(String location, int stage) {
 		ArrayList<String> nameList = nameFileLocations.get(stage);
 		if(nameList == null) {
 			nameList = new ArrayList<String>();
 			nameFileLocations.put(stage, nameList);
 		}
 		nameList.add(location);
 	}
 	
 	/**
 	 * adds a finish file to the handler. The optional "stage" number should be 1 unless the printer supports the functionality
 	 * (currently only the "stage"-kind of printers are able to do anything such.
 	 * 
 	 * This file is only handled by print. No errors are passed in any form and no file checkups if the file exists is done.
 	 * 
 	 * @param location the file location
 	 * @param stage what stage the file is in.
 	 */
 	public void addFinishFile(String location, int stage) {
 		ArrayList<String> finishList = finishFileLocations.get(stage);
 		if(finishList == null) {
 			finishList = new ArrayList<String>();
 			finishFileLocations.put(stage, finishList);
 		}
 		finishList.add(location);
 	}
 	
 	/**
 	 * adds a start file to the handler. The optional "stage" number should be 1 unless the printer supports the functionality
 	 * (currently only the "stage"-kind of printers are able to do anything such.
 	 * 
 	 * This file is only handled by print. No errors are passed in any form and no file checkups if the file exists is done.
 	 * 
 	 * @param location
 	 * @param stage
 	 */
 	public void addStartFile(String location, int stage) {
 		ArrayList<String> startList = startFileLocations.get(stage);
 		if(startList == null) {
 			startList = new ArrayList<String>();
 			startFileLocations.put(stage, startList);
 		}
 		startList.add(location);
 	}
 	
 	/**
 	 * Prints data based on the input files and configuration offered.
 	 * 
 	 * @return the output which should be written to a file.
 	 */
 	public String print() {
 		RacerPrinter printer;
 		Comparator<Racer> comp;		
 		
 		String printerType = ConfigParser.getInstance().getStringConf("race");
 		
 		if(printerType.equals("laps")) {
 			debugPrinter = new LapRacePrinter();
 			comp = new RunnerCheckTotalTimeMax(new RunnerLapsComparator(new RunnerTotalTimeComparator(new RunnerNumberComparator())));
 			printer = new SortedLapRacePrinter();
 			System.out.println("printing a sorted lap list");
 		} else {
 			debugPrinter = new StageRacePrinter();
 			comp = new RunnerStageComparator(new RunnerCheckTotalTimeMax(new RunnerTotalTimeComparator(new RunnerNumberComparator())));
 			printer = new SortedStageRacePrinter();
 			System.out.println("printing a sorted stage list");
 		}
 		
 		StringBuilder out = new StringBuilder();
 		
 		error =  this.preparePrint(printer, comp);
 		if(error.length() != 0) {
			Log.log(error);
 		}
 		
 		
 		for(RacerSorter sorter : groups) {
 			out.append(sorter.print());
 		}
 
 		return out.toString();
 	}
 	
 	public String debugPrint() {
 		StringBuilder out = new StringBuilder();
 		if(this.error == null)
 			return new String("FATAL ERROR: initialization: print() must be called in the InputHandler class before a debug print is possible!!");
 		for(RacerSorter sorter : groups) {
 			out.append(sorter.debugPrint(this.debugPrinter));
 		}
 		
 		if(error.length() != 0) {
 			out.append("\n\n\nerror: "+error+" \n");
 		}
 		
 		return out.toString();
 	}
 	
 	/**
 	 * reads all files in the arraylists and populates them into the hashmap and groups arrays.
 	 * this function is relatively huge and should be broken up into more logical pieces. alas not at this moment.
 	 * @return a trail of errors.
 	 */
 	private String preparePrint(RacerPrinter rp, Comparator<Racer> rc) {
 		StringBuilder error = new StringBuilder();
 		
 		groups = new ArrayList<RacerSorter>();
 		RacerSorter currentGroup = null;
 		RacerSorter unnamedGroup;
 		RacerSorter unregisteredRacers;
 		racerList = new HashMap<Integer, Racer>();
 		
 		//if(nameFileLocations.size() == 0 || finishFileLocations.size() == 0 || startFileLocations.size() == 0)
 		//	return "input data files missing. check your input, a name, start and finish file must all be supplanted for this function to work.";
 		
 		//assumes there is only one name file at this time. customer has not requested anything else
 		try {
 			String[] names = this.getLines(nameFileLocations.get(1).get(0));
 			this.headerInformation = names[0].split("; ");
 			if(!this.correctNameFileLine(names[0])) {
 				Log.log("in the name file " + nameFileLocations.get(1).get(0) + " incorrect header line");
 			}
 			rp.setHeaderInformation(this.headerInformation);
 			this.debugPrinter.setHeaderInformation(this.headerInformation);
 			
 			unnamedGroup = new RacerSorter("Ogrupperade förare", rc, rp);
 			unregisteredRacers = new RacerSorter("Icke existerande startnummer", rc, rp);
 			
 			for(int i = 1; i < names.length; i++) {
 				if(!(correctNameFileLine(names[i]) || correctCategory(names[i]))) {
 					Log.log("in the in the name file " + nameFileLocations.get(1).get(0) + " on line: " + (i+1) + " incorrect line:\n\t" + names[i] + "\n");
 					continue;
 				}
 				
 				String[] lineSplit = names[i].split("; ");
 				
 				if(this.headerInformation.length != lineSplit.length && lineSplit.length != 1) {
 					Log.log("in the in the name file " + nameFileLocations.get(1).get(0) + " on line: " + (i+1) + " incorrect line:\n\t" + names[i] + "\n");
 					continue;
 				}
 				
 				if(lineSplit.length == 1) {
 					//new group
 					if(currentGroup != null)
 						this.groups.add(currentGroup);
 					
 					currentGroup = new RacerSorter(lineSplit[0], rc, rp);
 				} else {
 					Racer temp = new Racer(lineSplit);
 					racerList.put(temp.startNbr, temp);
 					
 					if(currentGroup != null) {
 						currentGroup.addRacer(temp);
 					} else {
 						unnamedGroup.addRacer(temp);
 					}
 					
 				}
 			}
 		} catch (IOException e) {
 			error.append("FATAL ERROR: error reading the name file: " + nameFileLocations.get(0) + "\n");
 			return error.toString();
 		}
 		
 		readStartFile(error, unregisteredRacers);
 		readFinishFile(error, unregisteredRacers);
 
 		this.groups.add(currentGroup);
 		this.groups.add(unnamedGroup);
 		this.groups.add(unregisteredRacers);
 		return error.toString();
 	}
 
 	/**
 	 * reads and parses the start time files.
 	 * @param error a stringbuilder where errors will be printed.
 	 * @param unregisteredRacers a group of unregistered racers which unexisting racers are added.
 	 */
 	private void readStartFile(StringBuilder error, RacerSorter unregisteredRacers) {
 		try {
 			Set<Integer> stages = this.startFileLocations.keySet();
 
 			for(int stage: stages) {
 				for(String file: startFileLocations.get(stage)) {
 					String[] lineSplit = this.getLines(file);
 					int numLine = 0;
 					for(String line: lineSplit) {
 						numLine++;
 						if(!correctInputLine(line)) {
 							Log.log("in the start file: " + file + " on line: " + numLine + " incorrect line:\n\t" + line + "\n");
 							continue;
 						}
 						String[] lineInfo = line.split("; ");
 						
 						if(lineInfo.length == 2) {
 							try {
 								Racer relevantRacer = this.racerList.get(Integer.parseInt(lineInfo[0]));
 								
 								if(relevantRacer == null) {
 									//racer does not exist
 									Racer r = new Racer(new String[]{lineInfo[0]});
 									r.addStartTime(new Time(lineInfo[1]), stage);
 									unregisteredRacers.addRacer(r);
 									racerList.put(Integer.parseInt(lineInfo[0]), r);
 								} else {
 									//racer exists
 									relevantRacer.addStartTime(new Time(lineInfo[1]), stage);
 								}
 							} catch(Exception E) {
 								error.append("integer parse error in start file: " + file + " line reads:: " + line + "\n");
 							}
 							
 							
 						} else {
 							if(line.length() != 0)
 								error.append("error reading in a start file: " + file + " line reads:: " + line + "\n");
 						}
 					}
 				}
 			}
 		} catch(IOException e) {
 			error.append("error reading a start time file"  + "\n");
 		}
 	}
 
 	/**
 	 * reads and parses the finish time files.
 	 * @param error a stringbuilder where errors will be printed.
 	 * @param unregisteredRacers a group of unregistered racers which unexisting racers are added.
 	 */
 	private void readFinishFile(StringBuilder error,
 			RacerSorter unregisteredRacers) {
 		try {
 			Set<Integer> stages = this.startFileLocations.keySet();
 			for(int stage: stages) {
 				for(String file: finishFileLocations.get(stage)) {
 					String[] lineSplit = this.getLines(file);
 					int numLine = 0;
 					for(String line: lineSplit) {
 						numLine++;
 						if(!correctInputLine(line)) {
 							Log.log("in the finish file: " + file + " on line: " + numLine + " incorrect line:\n\t" + line + "\n");
 							continue;
 						}
 						String[] lineInfo = line.split("; ");
 						
 						if(lineInfo.length == 2) {
 							try {
 								Racer relevantRacer = this.racerList.get(Integer.parseInt(lineInfo[0]));
 								
 								if(relevantRacer == null) {
 									//racer does not exist
 									Racer r = new Racer(new String[]{lineInfo[0]});
 									r.addFinishTime(new Time(lineInfo[1]), stage);
 									unregisteredRacers.addRacer(r);
 									racerList.put(Integer.parseInt(lineInfo[0]), r);
 								} else {
 									//racer exists
 									relevantRacer.addFinishTime(new Time(lineInfo[1]), stage);
 								}
 							} catch(Exception E) {
 								error.append("integer parse error in finish file: " + file + " line reads:: " + line + "\n");
 							}
 							
 							
 						} else {
 							if(line.length() != 0)
 								error.append("error reading in a finish file: " + file + " line reads:: " + line + "\n");
 						}
 					}
 				}
 			}
 		} catch(IOException e) {
 			error.append("error reading a finish time file"  + "\n");
 		}
 	}
 	
 	/**
 	 * a very basic filereader which reads a file and returns linewise what it reads as an array of Strings.
 	 * 
 	 * @param fileLocation the relative / absolute path to the file to be read.
 	 * @return an array of strings, the linewise content of the file.
 	 * @throws IOException
 	 */
 	private String[] getLines(String fileLocation) throws IOException {
 		
 		BufferedReader reader = new BufferedReader(new FileReader(fileLocation));
 		
 		ArrayList<String> res = new ArrayList<String>();
 		
 		while(reader.ready())
 			res.add(reader.readLine());
 		
 		return res.toArray(new String[res.size()]);
 	}
 	
 	private boolean correctInputLine(String line) {
 		if(line.length()==0)
 			return true;
 		return Pattern.matches("\\d+;( )?\\d\\d.\\d\\d.\\d\\d", line);
 	}
 	
 	private boolean correctNameFileLine(String line) {
 		if(line.length()==0)
 			return true;
 		return Pattern.matches("([åäöÅÄÖa-zA-Z0-9. \"øØüÜ-]+; )+([åäöÅÄÖa-zA-Z0-9. \"øØüÜ-]+)?", line);
 	}
 	
 	private boolean correctCategory(String line) {
 		if(line.length()==0)
 			return true;
 		return Pattern.matches("[a-zA-Z0-9åäöÅÄÖ]+", line);
 	}
 }
