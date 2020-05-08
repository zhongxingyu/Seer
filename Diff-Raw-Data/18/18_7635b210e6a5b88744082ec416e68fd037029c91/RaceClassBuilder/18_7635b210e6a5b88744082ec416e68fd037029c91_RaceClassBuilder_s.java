 package result;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import main.Driver;
 
 import util.Time;
 
 public class RaceClassBuilder {
 
 	private int plac = 0;
 	private int maxNbrOfLaps;
 	private String raceClass;
 	private StringBuilder sb;
 	private ArrayList<Driver> driverList;
	ArrayList<String> lapTimes;
 
 
 	public RaceClassBuilder(int nbrOfLaps) {
 		maxNbrOfLaps = nbrOfLaps;
 		sb = new StringBuilder();
 	}
 
 	public void writeResult(ArrayList<Driver> allDriversInAClass) {
 		this.raceClass = allDriversInAClass.get(0).getRaceClass();
 		driverList = allDriversInAClass;
 		addRaceClass();
 		addHeader();
 		writeEachDriverResult();
 	}
 
 	private void writeEachDriverResult() {
 		int plac = 1;
 		for (Driver driver : driverList) {
 			// Writes the id, info and result of each ---driver--- on one line
 			// #################EXAMPLE##########################
 			// Plac; StartNr; Namn; #Varv; Totaltid; Varv1; Varv2
 
 			// //ANVÄNDS INTE LÄNGRE, VAR TÄNKT TILL DEN NYA DESIGNEN: CheckError checkError = new CheckError(driver); ////HÄR MÅSTE
 			// ALLA EVENTUELLA FEL FIXAS!
 			sb.append(plac + "; ");
 			plac++;
 			sb.append(driver.getId());
 			sb.append("; ");
 			sb.append(driver.getName());
 			sb.append("; ");
 
 			lapTimes = driver.listOfLapTimes();
 			sb.append(lapTimes.size() + 1);
 			sb.append("; ");
 			sb.append(Time.totalTimeString(driver.totalTime()) + "; ");
 			printLapTimes();
 			sb.append("\n");
 		}
 
 	}
 
 	private void printLapTimes() {
 		for (String time : lapTimes) {
 			sb.append(time.toString());
 			sb.append("; ");
 		}
 		addSemiColon(maxNbrOfLaps - lapTimes.size());
 	}
 	
 	private void addSemiColon(int nbrOfSemiColon) {
 		for (int i = 0; i < nbrOfSemiColon; i++) {
 			sb.append("; ");
 		}
 	}
 
 	private void addRaceClass() {
 		sb.append(raceClass + "\n");
 	}
 
 	/**
 	 * Adds a header to the resultFile
 	 */
 	private void addHeader() {
 
 		// StartNr; Namn; #Varv; Totaltid; Varv1; Varv2; Varv3; Start;
 		// Varvning1; Varvning2; Mål
 
 		sb.append("Plac; StartNr; Namn; #Varv; Totaltid; ");
 		for (int i = 0; i < maxNbrOfLaps; i++) {
 			sb.append("Varv");
 			sb.append(i + 1 + "; ");
 		}
 		sb.append("\n");
 	}
 	
 	public String toString(){
 		return sb.toString();
 	}
 
 }
