 package enduro.racer;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.HashMap;
 
 import enduro.racer.comparators.RunnerCheckTotalTimeMax;
 import enduro.racer.comparators.RunnerLapseComparator;
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
 	private ArrayList<String> nameFileLocations = new ArrayList<String>();
 	private ArrayList<String> finishFileLocations = new ArrayList<String>();
 	private ArrayList<String> startFileLocations = new ArrayList<String>();
 	HashMap<Integer, Racer> racerList;
 	ArrayList<RacerSorter> groups;
 	
 	public void addNameFile(String location) {
 		nameFileLocations.add(location);
 	}
 	
 	public void addFinishFile(String location) {
 		finishFileLocations.add(location);
 	}
 	
 	public void addStartFile(String location) {
 		startFileLocations.add(location);
 	}
 	
 	public String print() {
 		RacerPrinter printer;
 		Comparator<Racer> comp;
 		
 		String compare = ConfigParser.getInstance().getStringConf("sorting");
 		String sorted = ConfigParser.getInstance().getStringConf("sorted");
 		
 		if(compare.equals("number")) {
 			System.out.println("sorting based on number");
 			comp = new RunnerNumberComparator();
 		} else if(compare.equals("laps")){
 			System.out.println("sorting based on laps");
 			comp = new RunnerCheckTotalTimeMax(new RunnerLapseComparator(new RunnerTotalTimeComparator(new RunnerNumberComparator())));
 		} else if(compare.equals("time")){
 			System.out.println("sorting based on time");
 			comp = new RunnerStageComparator(new RunnerCheckTotalTimeMax(new RunnerTotalTimeComparator(new RunnerNumberComparator())));
 		} else {
 			System.out.println("sorting based on number");
 			comp = new RunnerNumberComparator();
 		}
 		
 		String printerType = ConfigParser.getInstance().getStringConf("race");
 		
 		if(printerType.equals("laps")) {
 			if(sorted.equals("true")) {
 				printer = new SortedLapRacePrinter();
 				System.out.println("printing a sorted lap list");
 			} else {
 				printer = new LapRacePrinter();
 				System.out.println("printing a lap list");
 			}
 		} else if(printerType.equals("stages")){
 			if(sorted.equals("true")) {
 				printer = new SortedStageRacePrinter();
 				System.out.println("printing a sorted stage list");
 			} else {
 				printer = new StageRacePrinter();
 				System.out.println("printing a stage list");
 			}
 		} else {
 			System.out.println("printing a lap list");
 			printer = new LapRacePrinter();
 		}
 		
 		String error =  this.preparePrint(printer, comp);
 		StringBuilder out = new StringBuilder();
 		
 		for(RacerSorter sorter : groups) {
 			out.append(sorter.print());
 		}
		
 		if(error.length() > 0)
 			out.append("\n\n\nerror: \n");
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
 			String[] names = this.getLines(nameFileLocations.get(0));
 			this.headerInformation = names[0].split("; ");
 			
 			rp.setHeaderInformation(this.headerInformation);
 			unnamedGroup = new RacerSorter("ungrouped people", rc, rp);
 			unregisteredRacers = new RacerSorter("Icke existerande startnummer", rc, rp);
 			
 			for(int i = 1; i < names.length; i++) {
 				String[] lineSplit = names[i].split("; ");
 				
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
 
 	private void readStartFile(StringBuilder error,
 			RacerSorter unregisteredRacers) {
 		try {
 			for(String file: startFileLocations) {
 				String[] lineSplit = this.getLines(file);
 				
 				for(String line: lineSplit) {
 					String[] lineInfo = line.split("; ");
 					
 					if(lineInfo.length == 2) {
 						try {
 							Racer relevantRacer = this.racerList.get(Integer.parseInt(lineInfo[0]));
 							
 							if(relevantRacer == null) {
 								//racer does not exist
 								Racer r = new Racer(new String[]{lineInfo[0]});
 								r.addStartTime(new Time(lineInfo[1]));
 								unregisteredRacers.addRacer(r);
 								racerList.put(Integer.parseInt(lineInfo[0]), r);
 							} else {
 								//racer exists
 								relevantRacer.addStartTime(new Time(lineInfo[1]));
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
 		} catch(IOException e) {
 			error.append("error reading a start time file"  + "\n");
 		}
 	}
 
 	private void readFinishFile(StringBuilder error,
 			RacerSorter unregisteredRacers) {
 		try {
 			for(String file: finishFileLocations) {
 				String[] lineSplit = this.getLines(file);
 				
 				for(String line: lineSplit) {
 					String[] lineInfo = line.split("; ");
 					
 					if(lineInfo.length == 2) {
 						try {
 							Racer relevantRacer = this.racerList.get(Integer.parseInt(lineInfo[0]));
 							
 							if(relevantRacer == null) {
 								//racer does not exist
 								Racer r = new Racer(new String[]{lineInfo[0]});
 								r.addFinishTime(new Time(lineInfo[1]));
 								unregisteredRacers.addRacer(r);
 								racerList.put(Integer.parseInt(lineInfo[0]), r);
 							} else {
 								//racer exists
 								relevantRacer.addFinishTime(new Time(lineInfo[1]));
 							}
 						} catch(Exception E) {
 							error.append("integer parse error in finish file: " + file + " line reads:: " + line + "\n");
 						}
 						
 						
 					} else {
 						error.append("error reading in a finish file: " + file + " line reads:: " + line + "\n");
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
 }
