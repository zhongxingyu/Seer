 package com.bigvisible.kanbansimulator;
 
 import java.io.OutputStream;
 import java.io.PrintWriter;
 
 public class CommandLine {
 
	private static final int NUM_OF_STORIES = 0;
	private static final int BA_CAPACITY = 1;
	private static final int DEV_CAPACITY = 2;
	private static final int WEBDEV_CAPACITY = 3;
	private static final int QA_CAPACITY = 4;
	private static final int BATCH_SIZE = 5;
	private static final int NUM_OF_ITERATIONS = 6;
 
 	private static OutputStream out = System.out;
 
 	/**
 	 * Must be called prior to calling {@link #main(String[])} to take effect.
 	 * 
 	 * @param newOut
 	 *            the {@link OutputStream} to send simulator output to instead
 	 *            of the default.
 	 */
 	public static void redirectOutputTo(OutputStream newOut) {
 		out = newOut;
 	}
 
 	public static void main(String[] args) {
 		if (args.length < 7) {
 			printUsage();
 			return;
 		}
 
 		Simulator stimulator = new SimulatorEngine();
 
 		stimulator.addStories(argAsInt(args, NUM_OF_STORIES));
 		stimulator.setBusinessAnalystCapacity(argAsInt(args, BA_CAPACITY));
 		stimulator.setDevelopmentCapacity(argAsInt(args, DEV_CAPACITY));
 		stimulator.setWebDevelopmentCapacity(argAsInt(args, WEBDEV_CAPACITY));
 		stimulator.setQualityAssuranceCapacity(argAsInt(args, QA_CAPACITY));
 		stimulator.setBatchSize(argAsInt(args, BATCH_SIZE));
 		stimulator.setNumberOfIterationsToRun(argAsInt(args, NUM_OF_ITERATIONS));
 		stimulator.run(out);
 	}
 
 	private static void printUsage() {
 		PrintWriter output = new PrintWriter(out);
 
 		output.println("Kanban Simulator (KBS)         Version 0.01                           2012-05-24");
 		output.println("\nUsage:\n");
 		output.println("   java -jar stimulator.jar (total stories) (BA) (DEV) (WEBDEV) (QA) (BATCH)  (iterations)\n");
 		output.println("where...");
 		output.println("  total stories = total number of stories in the backlog.");
 		output.println("  BA = capacity of Business Analysis step.");
 		output.println("  DEV = capacity of Development step.");
 		output.println("  WEBDEV = capacity of Web Development step.");
 		output.println("  QA = capacity of Quality Assurance step.");
 		output.println("  BATCH = total number of stories to release into the kanban board each iteration.");
 		output.println("  iterations = the total number of iterations that simulator will run.  (Use 0 to indicate that the simulator is to stop when there are no more stories in the workflow.)");
 
 		output.flush();
 	}
 
 	private static int argAsInt(String[] args, int position) {
 		return Integer.parseInt(args[position]);
 	}
 }
