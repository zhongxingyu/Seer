 package main;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.PrintWriter;
 import java.util.Locale;
 
 import core.Mutation;
 import core.SimulationResult;
 
 public class ResultSaver {
 	private PrintWriter outGen;
 	private final String curDir;
 
 	public static final String RESULTS_DIR = "results";
 	public static final String AUTO_FILENAME = "auto";
 	public static final String GENERATIONS_FILENAME = "generations";
 
 	public static String getDirectoryName(double[] prob) {
 		String name = "";
 		for (Mutation m : Mutation.values()) {
 			if (m.ordinal() > 0) {
 				name += "|";
 			}
 			name += String.format("%.2f", prob[m.ordinal()]);
 		}
 		return name;
 	}
 
 	public ResultSaver(double[] prob) {
 		long id = (System.currentTimeMillis() * Thread.currentThread()
 				.hashCode())
 				& Integer.MAX_VALUE;
 		curDir = RESULTS_DIR + "/" + getDirectoryName(prob) + "/" + id + "/";
 		try {
 			new File(curDir).mkdirs();
 			outGen = new PrintWriter(new File(curDir + GENERATIONS_FILENAME));
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void close() {
 		outGen.close();
 	}
 
 	public void saveAutomaton(SimulationResult sr) {
 		try {
 			PrintWriter outAuto = new PrintWriter(new File(curDir
 					+ AUTO_FILENAME));
 			outAuto.printf(Locale.US, "Eaten part = %.6f", sr.eatenPartsSum
 					/ sr.fieldsTested);
 			outAuto.println();
 			sr.auto.print(outAuto);
 			outAuto.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void saveGeneration(int genNumber, double best, double mean) {
 		outGen.println("Generation " + genNumber);
		outGen.printf(Locale.US, "%.6f = max eaten part", best);
 		outGen.println();
		outGen.printf(Locale.US, "%.6f = mean eaten part", mean);
 		outGen.println();
 		outGen.println();
 	}
 }
