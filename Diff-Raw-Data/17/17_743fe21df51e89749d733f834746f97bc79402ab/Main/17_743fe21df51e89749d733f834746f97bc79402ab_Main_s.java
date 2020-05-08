 package versionThreads;
 
 import java.io.File;
 
 /**
  * Malware Capture Facility CVUT University, Prague, Czech Republic
  * 
  * These files were generated as part of a research project in the CVUT
  * University, Prague, Czech Republic. The goal is to store long-lived real
  * botnet traffic and to generate labeled netflows files. Any question feel free
  * to contact us: Sebastian Garcia, sebastian.garcia@agents.fel.cvut.cz Vojtech
  * Uhlir <vojtech.uhlir@agents.fel.cvut.cz
  * 
  */
 public class Main {
     public static void main(String[] args) {
 
 	if (args.length != 4) {
 	    System.err.println("Arguments have to be: \n\t-t X, where X is number of minutes for output. \n\t-f File.rrd to be updated.");
 	    System.exit(0);
 	}
	DataSummary.timeWindowInMillis = Long.parseLong(args[1]) * 59 * 1000;
 
 	// Current time is created only when the app. is launched
 	DataSummary.timeToAdd = System.currentTimeMillis();
 
 	DataSummary.rrdFileToUpdate = args[3];
 	if (!new File(DataSummary.rrdFileToUpdate).exists()) {
 	    System.out.println("Cannot find: " + DataSummary.rrdFileToUpdate);
 	    System.exit(0);
 	}
 
 	Thread readParseAndUpdateVariablesThread = new ReadParseAndUpdateVariablesThread();
 
 	readParseAndUpdateVariablesThread.start();
 
 	Thread outputAndResetThread = new OutputAndResetThread();
 
 	outputAndResetThread.start();
     }
 }
