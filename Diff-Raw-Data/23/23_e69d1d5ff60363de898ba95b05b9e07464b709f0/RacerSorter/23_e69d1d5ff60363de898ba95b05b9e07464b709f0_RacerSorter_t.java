 package enduro.racer;
 
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.TreeSet;
 
 import enduro.racer.comparators.RunnerNumberComparator;
 import enduro.racer.configuration.ConfigParser;
 import enduro.racer.printer.RacerPrinter;
 
 /**
  * This class handles the comparison between Racers based upon a set specific list of comparison critera
  * (represented as a Comparator<Racer> class-instance. The output is corresponding to a "class".
  * It uses the printer classes in enduro.racer.printer to output correct data.
  */
 public class RacerSorter {
 	
 	private TreeSet<Racer> racers = new TreeSet<Racer>();
 	private Comparator<Racer> comp;
 	private RacerPrinter printer;
 	private String groupName;
 	private Time minTotalTime;
 	
 	/**
 	 * creates a new sorter according to a set number of parameters.
 	 * @param comp the comparator according to which the runners will be sorted
 	 * @param printer the output printer
 	 */
 	public RacerSorter(String groupName, Comparator<Racer> comp, RacerPrinter printer) {
 		this.comp = comp;
 		this.printer = printer;
 		this.minTotalTime = new Time(ConfigParser.getInstance().getStringConf("mintime"));
 		this.groupName = groupName;
 	}
 	
 	/**
 	 * Adds a new racer sorted according to the inserted comparator.
 	 * @param r the racer
 	 */
 	public void addRacer(Racer r) {
 		racers.add(r);
 	}
 	
 	/**
 	 * Returns the Racer connected with the runner id.
 	 * If the racer does not exist null is returned instead.
 	 * 
 	 * @param runnerNum the runner id
 	 * @return the racer if available, otherwise null.
 	 */
 	public Racer getRacer(int runnerNum) {
 		Iterator<Racer> list = racers.iterator();
 		while(list.hasNext()) {
 			Racer r = list.next();
 			if (r.startNbr == runnerNum) {
 				list.remove();
 				return r;
 			}
 		}
 		return null;
 	}
 	
 	public String debugPrint(RacerPrinter debugprinter) {
 		Comparator<Racer> savedComp = this.comp;
 		RacerPrinter savedprinter = this.printer;
 		
 		this.comp = new RunnerNumberComparator();
 		this.printer = debugprinter;
 		
 		StringBuilder out = new StringBuilder();
 		
 		out.append(this.print());
 		
 		this.comp = savedComp;
 		this.printer = savedprinter;
 		return out.toString();
 	}
 	
 	/**
 	 * prints the overlaying text line as well as all related runners.
 	 * If there are NO runners nothing gets printed!
 	 * @return all relevant information.
 	 */
 	public String print() {
 		if(this.racers.size() == 0)
 			return "";
 		TreeSet<Racer> sortedRacers = new TreeSet<Racer>(this.comp);
 		sortedRacers.addAll(this.racers);
 		
 		StringBuilder out = new StringBuilder();
 		int position = 1;
 		HashMap<String, String> extraInformation = new HashMap<String, String>();
 		
 		out.append(this.groupName);
 		out.append("\n");
 		
 		out.append(this.printer.printTopInformation());
 		out.append("\n");
 		
 		for(Racer racer: sortedRacers) {
 			//clearing hashmap to save the need for constructing a new one for each racer (==yawn)
 			extraInformation.clear();
 			
 			/*
 			 * from here all possible extra information for printing is added, independent if a printer needs it or not. it does not get printed unless polled.
 			 */
 			
 			try {
 				//tests if the total time running is larger than the minimum time, if that is the case the position attribute is added
 				if(racer.startTimes.size() > 1 || racer.finishTimes.size() > 1) {
 					TreeSet<Integer> keys = new TreeSet<Integer>();
 					keys.addAll(racer.finishTimes.keySet());
 					keys.addAll(racer.startTimes.keySet());
 					
 					Time total = new Time("00.00.00");
 					for (int key : keys) {
 						if(racer.startTimes.get(key) != null && racer.finishTimes.get(key) != null)
 							if(key == 1)
 								if(racer.startTimes.get(1).size() == 0 || racer.finishTimes.get(1).size() == 0)
 									continue;
 							total.increment(racer.startTimes.get(key).first().getTotalTime(racer.finishTimes.get(key).first()));
 					}
 					
 					if(total.compareTo(minTotalTime) > 0) {
 						extraInformation.put("position", position + "");
 						position++;
 					}
 					
 				} else {
					if(racer.startTimes.get(1).size() > 0 && racer.finishTimes.get(1).size() > 0) {
						if(racer.startTimes.get(1).first().getTotalTime(racer.finishTimes.get(1).last()).compareTo(minTotalTime) > 0) {
						
							extraInformation.put("position", position + "");
							
							//position increased
							position++;
						}
 						
 					}
 				}
 				//lets the printer print all the whole line. if it doesn't need the (possible nonexistent) position: who cares? =)
 				
 			} catch(Exception E) {
				Log.log("Exception Racersorter: " + E.toString() + " id: " + racer.startNbr);
 			}
 
 			out.append(printer.print(racer, extraInformation));
 			out.append("\n");
 			
 			
 		}
 		return out.toString();
 	}
 }
