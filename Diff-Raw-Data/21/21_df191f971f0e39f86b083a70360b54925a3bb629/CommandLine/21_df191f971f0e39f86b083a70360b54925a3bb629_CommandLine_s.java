 package com.bigvisible.kanbansimulator;
 
 import java.io.OutputStream;
 import java.io.PrintWriter;
 
 public class CommandLine {
 
	private static int NUM_OF_STEPS = 0;
 	private static int NUM_OF_STORIES = 1;
 	private static int BA_CAPACITY = 2;
 	private static int DEV_CAPACITY = 3;
 	private static int WEBDEV_CAPACITY = 4;
 	private static int QA_CAPACITY = 5;
 	private static int BATCH_SIZE = 6;
 
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
 
 		Stimulator stimulator = new Stimulator();
 
 		stimulator.addStories(argAsInt(args, NUM_OF_STORIES));
 		stimulator.setBusinessAnalystCapacity(argAsInt(args, BA_CAPACITY));
 		stimulator.setDevelopmentCapacity(argAsInt(args, DEV_CAPACITY));
 		stimulator.setWebDevelopmentCapacity(argAsInt(args, WEBDEV_CAPACITY));
 		stimulator.setQualityAssuranceCapacity(argAsInt(args, QA_CAPACITY));
 		stimulator.run(out);
 	}
 
 	private static void printUsage() {
 		PrintWriter output = new PrintWriter(out);
 
 		output.println("Kanban Simulator (KBS)         Version 0.01                           2012-05-24");
 		output.println("\nUsage:\n");
 		output.println("   java -jar stimulator.jar (steps) (total stories) (BA) (DEV) (WEBDEV) (QA) (BATCH)\n");
 		output.println("where...");
 		output.println("  step = total number of steps in the workflow (currently ignored).");
 		output.println("  total stories = total number of stories in the backlog.");
 		output.println("  BA = capacity of Business Analysis step.");
 		output.println("  DEV = capacity of Development step.");
 		output.println("  WEBDEV = capacity of Web Development step.");
 		output.println("  QA = capacity of Quality Assurance step.");
 		output.println("  BATCH = total number of stories to release into the kanban board each iteration.");
 
 		output.flush();
 	}
 
 	private static int argAsInt(String[] args, int position) {
 		return Integer.parseInt(args[position]);
 	}
 }
