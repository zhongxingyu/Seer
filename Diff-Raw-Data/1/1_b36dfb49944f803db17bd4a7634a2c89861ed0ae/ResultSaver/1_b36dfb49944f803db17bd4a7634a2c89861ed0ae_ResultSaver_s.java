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
 
 	public static String getIdentifier(double[] prob) {
 		String ident = "";
 		for (Mutation m : Mutation.values()) {
 			if (m.ordinal() > 0) {
 				ident += "|";
 			}
 			ident += String.format("%.2f", prob[m.ordinal()]);
 		}
 		return ident;
 	}
 
 	public ResultSaver(double[] prob) {
 		long id = (System.currentTimeMillis() * Thread.currentThread()
 				.hashCode())
 				& Integer.MAX_VALUE;
 		curDir = RESULTS_DIR + "/" + getIdentifier(prob) + "/" + id + "/";
 		try {
 			new File(curDir).mkdirs();
 			outGen = new PrintWriter(new File(curDir + "generations"));
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void close() {
 		outGen.close();
 	}
 
 	public void saveAutomaton(SimulationResult sr) {
 		try {
 			PrintWriter outAuto = new PrintWriter(new File(curDir + "auto"));
 			outAuto.printf(Locale.US, "Eaten part = %.6f", sr.eatenPartsSum
 					/ sr.fieldsTested);
 			sr.auto.print(outAuto);
 			outAuto.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void saveGeneration(int genNumber, double best, double mean) {
 		outGen.println("Generation " + genNumber);
 		outGen.printf(Locale.US, "Max eaten part = %.6f", best);
 		outGen.println();
 		outGen.printf(Locale.US, "Mean eaten part = %.6f", mean);
 		outGen.println();
 		outGen.println();
 	}
 }
