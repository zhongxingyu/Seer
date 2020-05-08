 package enduro;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import enduro.racedata.RaceClass;
 import enduro.racedata.Time;
 import enduro.racedata.RacerData;
 
 /**
  * Superclass for all sorters. Merges data from input files and ouputs them in a
  * readable format according to the formatting rules (Subclass) used.
  * 
  * @author Rick
  * 
  */
 public abstract class Sorter {
 
 	protected RacerData racerData;
 	protected Time startTime;
 	protected Time finishTime;
 	protected StringBuilder trail;
 
 	public Sorter() {
 		racerData = new RacerData();
 		trail = new StringBuilder();
 	}
 
 	/**
 	 * Reads a file with start times and populates the data structure..
 	 * 
 	 * @param fileName
 	 *            The name of the file.
 	 */
 	public void readStartFile(String fileName) throws Exception {
 		ArrayList<String[]> startingTimes = readFile(fileName);
 		for (int i = 0; i < startingTimes.size(); i++) {
 			int startNbr = Integer.parseInt(startingTimes.get(i)[0]);
 			racerData.addStartTime(startNbr, new Time(startingTimes.get(i)[1]));
 		}
 	}
 
 	/**
 	 * Reads a file with finish times and populates the data structure.
 	 * 
 	 * @param fileName
 	 *            The name of the file.
 	 */
 	public void readFinishFile(String fileName) throws Exception {
 		ArrayList<String[]> finishTimes = readFile(fileName);
 		for (int i = 0; i < finishTimes.size(); i++) {
 			int startNbr = Integer.parseInt(finishTimes.get(i)[0]);
 			racerData.addFinishTime(startNbr, new Time(finishTimes.get(i)[1]));
 		}
 	}
 
 	/**
 	 * Reads a file with a start number and a time and returns and ArrayList
 	 * containing String arrays containing the start number and the time.
 	 * 
 	 * @param fileName
 	 *            The name of the file.
 	 * @return ArrayList<String[]> containing start numbers and time.
 	 * @throws Exception
 	 *             when something goes wrong.
 	 */
 	private ArrayList<String[]> readFile(String fileName) throws Exception {
 		ArrayList<String[]> list = new ArrayList<String[]>();
 
 		BufferedReader in = new BufferedReader(new FileReader(fileName));
 		while (in.ready()) {
 			list.add(in.readLine().split("; "));
 		}
 		return list;
 	}
 
 	/**
 	 * Reads a file containing names of racers and adds them to the data
 	 * structure.
 	 * 
 	 * @param fileName
 	 *            the name of the file to be read.
 	 * @throws Exception
 	 *             when something goes wrong.
 	 */
 	public void readNameFile(String fileName) throws Exception {
 		ArrayList<String[]> names = readFile(fileName);
 		RaceClass currentClass = new RaceClass("");
 		for (int i = 0; i < names.size(); i++) {
 			try {
 				int startNbr = Integer.parseInt(names.get(i)[0]);
 				racerData.addName(startNbr, names.get(i)[1]);
 				currentClass.registerContestant(startNbr);
 			} catch (NumberFormatException e) {
 				currentClass = new RaceClass(names.get(i)[0]);
 				racerData.addClass(currentClass);
 			}
 
 		}
		if(!racerData.containsClass(currentClass)){
			racerData.addClass(currentClass);
		}
 	}
 
 	/**
 	 * Method used for testing Returns an ArrayList of registered classes
 	 * 
 	 * @return
 	 */
 	public ArrayList<RaceClass> getClasses() {
 		return racerData.getClasses();
 	}
 
 	/**
 	 * Creates a readable results file from the gathered data.
 	 * 
 	 * @param fileName
 	 *            the name of the output file.
 	 * @throws IOException
 	 *             when something goes wrong.
 	 */
 	public void createResultFile(String fileName) throws IOException {
 		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(
 				fileName)));
 		out.println(titleRow());
 		Iterator<RaceClass> itr = racerData.iterator();
 		while (itr.hasNext()) {
 			RaceClass currentClass = itr.next();
 			if(!currentClass.getName().equals(""))out.println(currentClass.getName());
 			Iterator<Integer> nbrItr = currentClass.iterator();
 			while (nbrItr.hasNext()) {
 				int i = nbrItr.next();
 				String name = racerData.getName(i);
 				String start;
 				String finish = null;
 				String total = null;
 				try {
 					startTime = racerData.getStartTime(i).poll();
 					start = startTime.toString();
 					if (racerData.getStartTime(i).size() > 0) {
 						trail.append("; Flera starttider?");
 						while (racerData.getStartTime(i).size() > 0) {
 							trail.append(' ');
 							trail.append(racerData.getStartTime(i).poll());
 						}
 					}
 				} catch (NullPointerException e) {
 					start = "Start?";
 				}
 				try {
 					finishTime = getFinishTime(i);
 				} catch (NullPointerException e) {
 					finish = "Slut?";
 				}
 				if (finish == null || !finish.equals("Slut?")) {
 					finish = finishTime(i);
 				}
 				if (!finish.equals("Slut?") && !start.equals("Start?")) {
 					total = totalTime(i);
 				} else {
 					total = "--.--.--";
 				}
 				out.println(i + "; " + name + "; " + total + "; " + start
 						+ "; " + finish + trail.toString());
 				trail.delete(0, trail.length());
 			}
 		}
 		out.close();
 	}
 
 	protected Time getFinishTime(int i) throws NullPointerException {
 		return racerData.getFinishTime(i).poll();
 	}
 
 	/**
 	 * Returns the title row formatted according to the current race type.
 	 * 
 	 * @return the title row.
 	 */
 	protected abstract String titleRow();
 
 	/**
 	 * Calculates and returns the value for the total time column. How this is
 	 * calculated differs between race types.
 	 * 
 	 * @param i
 	 *            the racer's number.
 	 * @return a <code>String</code> containing the total time on the format
 	 *         "hh.mm.ss".
 	 */
 	protected String totalTime(int i) {
 		String total;
 		try {
 			Time totalTime = startTime.getTotalTime(finishTime);
 			total = totalTime.toString();
 			Time fastTime = new Time(0, 15, 0);
 			if (fastTime.compareTo(totalTime) > 0)
 				trail.append("; Omöjlig Totaltid?");
 		} catch (NullPointerException e) {
 			total = "--.--.--";
 		}
 		return total;
 	}
 
 	/**
 	 * Formats the finish time according to how it's supposed to be in the
 	 * current race type.
 	 * 
 	 * @param i
 	 *            the racer's number.
 	 * @return a <code>String</code> containing the racer's finish time on the
 	 *         format "hh.mm.ss".
 	 */
 	protected abstract String finishTime(int i);
 
 }
