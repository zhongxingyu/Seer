 package com.rockwellautomation.verification.framework;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.PrintStream;
 import java.io.Serializable;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 import com.rockwellautomation.verification.Performance.DictionaryType;
 import com.rockwellautomation.verification.Performance.Element;
 import com.rockwellautomation.verification.Performance.ElementType;
 import com.rockwellautomation.verification.Performance.Operation;
 import com.rockwellautomation.verification.Performance.OperationType;
 import com.rockwellautomation.verification.Performance.Operations;
 
 /**
  * This class is used to launch the test data generation process
  * 
  * @author AHall4
  */
 public class Main {
 
 	private static Random rand;
 	private static int max;
 	private static int operations;
 	private static int depth;
 	private static int branches;
 	private static int total;
 	private static boolean print;
 	private static OperationType[] operationTypes = OperationType.values();
 	private static DictionaryType[] dictionaryTypes = DictionaryType.values();
 	private static ElementType[] elementTypes = ElementType.values();
 	private static List<Integer> depthData = new ArrayList<Integer>();
 	private static List<Integer> branchData = new ArrayList<Integer>();
 	private static Map<OperationType, Integer> operationData = new HashMap<OperationType, Integer>();
 	private static Map<ElementType, Integer> elementData = new HashMap<ElementType, Integer>();
 	private Counter counterData = new Counter();
 	
 	/**
 	 * Main entry point of the program
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		
 		Main main = new Main();
 		
 		// Process the supplied arguments
 		main.processArgs(args);
 
 		try {
 		    // Create the operations
 		    main.createOperations();
 		    
 		    // Create the data
 			main.createData();
 			
 			// Generate stats about the data
 			main.reportStats();
 		}
 		catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	/**
 	 * This method will process the program arguments
 	 * @param args
 	 */
 	private void processArgs(String[] args) {
 		if ( (args == null) ||
 			 (args.length < 4) )
 			throw new IllegalArgumentException("A # of data elements, # of operations, max depth of data, and max elements per branch values are required!");
 		
 		// Get the number of data elements and convert to number
 		String tmp = args[0];
 		if ( (tmp == null) ||
 			 (tmp.length() == 0) )
 			throw new IllegalArgumentException("The supplied # of data elements is not valid!");
 		
 		// Allow this exception to bubble up and out
 		max = Integer.parseInt(tmp);
 		System.out.println("Using a # of data elements value of " + max);
 		
 		tmp = args[1];
 		if ( (tmp == null) ||
 			 (tmp.length() == 0) )
 			throw new IllegalArgumentException("The supplied operations is not valid!");
 		
 		operations = Integer.parseInt(tmp);
 		System.out.println("Using # of operations value of " + operations);
 		
 		// Create the random number generator
 		rand = new Random(max * System.currentTimeMillis());
 		
 		// Get the maximum depth for the data set
 		tmp = args[2];
 		if ( (tmp == null) ||
 			 (tmp.length() == 0) )
 			throw new IllegalArgumentException("The supplied maximum depth of data is not valid!");
 		
 		depth = Integer.parseInt(tmp);
 		System.out.println("Using maximum data depth value of " + depth);
 		
 		// Get the maximum elements per branch
 		tmp = args[3];
 		if ( (tmp == null) ||
 			 (tmp.length() == 0) )
 			throw new IllegalArgumentException("The supplied maximum number of elements per brach is not valid!");
 		
 		branches = Integer.parseInt(tmp);
 		System.out.println("Using maximum branches per element value of " + branches);
 		
 		if (args.length >= 4)
 			print = Boolean.parseBoolean(args[4]);
 		System.out.println("The data set will be printed to stdout? : " + print);
 	}
 	
 	/**
 	 * This method will create the test data
 	 * @throws Exception
 	 */
 	private void createData() throws Exception {
 		FileOutputStream out = null;
 		
 		// Make sure that data should be created
 		if (max <= 0) {
 			System.out.println("No test data elements were generated");
 			return;
 		}
 		
 		try {
 			// Create the root builder and add elements to it
 			Element.Builder root = Element.newBuilder();
 			while (total < max) {
 				this.counterData = new Counter();
 			    createElement(root);
 			    this.captureBranchesAndDepth();
 			}
 			
 			// Write the data to the output file
 			String file = "out" + File.separator + "testdata.dat";
 			out = new FileOutputStream(file);
 			Element e = root.build();
 			e.writeTo(out);
 			
 			if (print) {
 				System.out.println("Created " + total + " elements: ");
 				System.out.println(e);
 			}
 		}
 		finally {
 			try { if (out != null) out.close(); } catch (Exception ignore) { ignore.printStackTrace(); }
 		}
 		
 		System.out.println(total + " elements were created");
 	}
 	
 	private void createElement(Element.Builder parent) {
 		if ( (total >= max) ||
 			 (counterData.getDepth() > depth) ||
 			 (counterData.getBranches() > branches) ) {
 			if ( (counterData.getDepth() > 0) &&
 				 (counterData.getDepth() > depth) )
 				counterData.decrementDepth();
 			if ( (counterData.getBranches() > 0) &&
 				 (counterData.getBranches() > branches) )
 				counterData.decrementBranches();
 			return;
 		}
 		
 		int randDepth = rand.nextInt(depth);
 		int randBranches = rand.nextInt(branches);
 		
 		// Do a bit of random-ness to ensure that not
 		// all nodes are equal in size.
 		if ( (counterData.getDepth() > randDepth) ||
 			 (counterData.getBranches() > randBranches) ) {
 			if ( (counterData.getDepth() > 0) &&
 				 (counterData.getDepth() > randDepth) )
 				counterData.decrementDepth();
 			if ( (counterData.getBranches() > 0) &&
 				 (counterData.getBranches() > randBranches) )
 				counterData.decrementBranches();
 			return;
 		}
 		
 		// Create a new builder for the child
 		Element.Builder child = Element.newBuilder();
 		
 		// Determine the type of element
 		ElementType type = elementTypes[rand.nextInt(elementTypes.length)];
 		
 		// Based on the type, create the necessary extension and fill in a 
 		// value
 		switch(type) {
 			case BOOLEAN:
 				child.setExtension(Element.booleanValue, rand.nextBoolean());
 				break;
 			case NUMBER:
 				child.setExtension(Element.numericValue, rand.nextInt());
 				break;
 			default:
 				child.setExtension(Element.stringValue, getDictionaryValue());
 		}
 		
 		// Capture stats about the generated data
 		Integer counter = elementData.get(type);
 		if (counter != null)
 			counter++;
 		else
 			counter = 1;
 		elementData.put(type, counter);
 		
 		// Increment the counters
 		total++;
 		
 		// Capture a random flag used to determine if a sibling or child
 		// should be created
 		boolean sibling = rand.nextBoolean();
 		
 		// Determine if more children should be added
 		if (!sibling) {
 			counterData.incrementDepth();
 			createElement(child);
 		}
 		else {
 			counterData.incrementBranches();
 			createElement(parent);
 		}
 		
 		// Add the child to the parent and increment the total
 		parent.addChildren(child);
 	}
 
 	/**
 	 * This method will create the operational data used to perform operations
 	 * on the test data.
 	 * @throws Exception
 	 */
 	private void createOperations() throws Exception {
 		FileOutputStream out = null;
 		
 		// Make sure that operations should be created
 		if (operations <= 0) {
 			System.out.println("No operations were generated");
 			return;
 		}
 		
 		try {
 			Operations.Builder builder = Operations.newBuilder();
 		
 			// Walk the pre-determine number of operations and populate the
 			// information.
 			int i = 0;
 			for (i = 0; i < operations; i++) {
 				Operation.Builder b = Operation.newBuilder();
 				
 				// Determine the type
 				OperationType type = operationTypes[rand.nextInt(operationTypes.length)];
 				
 				// Add the information to the builder
 				b.setOperationType(type);
 				if (type == OperationType.SEARCH)
 					b.setSearch(getDictionaryValue());
 				
 				// Add the operation to the parent
 				builder.addOperations(b.build());
 				
 				// Capture stats about the generated operations
 				Integer counter = operationData.get(type);
 				if (counter != null)
 					counter++;
 				else
 					counter = 1;
 				operationData.put(type, counter);
 			}
 			
 			// Write the main operation to the output file
 			String file = "out" + File.separator + "operations.dat";
 			out = new FileOutputStream(file);
 			Operations o = builder.build();
 			o.writeTo(out);
 			
 			if (print) {
 				System.out.println("Created " + i + " opertaions:");
 				System.out.println(o);
 			}
 		}
 		finally {
 			try { if (out != null) out.close(); } catch (Exception ignore) { ignore.printStackTrace(); }
 		}
 	}
 	
 	/**
 	 * This method will get the string name of the word from the dictionary.
 	 * @return
 	 */
 	private String getDictionaryValue() {
 		return dictionaryTypes[rand.nextInt(dictionaryTypes.length)].name();
 	}
 	
 	/**
 	 * This method will report stats about the generated data set
 	 */
 	private void reportStats() throws Exception {
 		PrintStream out = new PrintStream(new FileOutputStream("out" + File.separator + "stats.txt"));
 		out.println("Total # of operations generated: " + operations);
 		
 		for (OperationType t : operationTypes) {
 			Integer counter = operationData.get(t);
 			if (counter == null)
 				counter = 0;
 			out.println("\t# of " + t.name() + " operations: " + counter);
 		}
 		
 		out.println("Total # of data elements generated: " + total);
 		out.println("\tMax Depth: " + maximumValue(depthData));
 		out.println("\tMax Branches: " + maximumValue(branchData));
 		Double averageDepth = calculateAverage(depthData);
 		Double averageBranches = calculateAverage(branchData);
 		NumberFormat f = NumberFormat.getInstance();
 		f.setMaximumFractionDigits(2);
 		f.setGroupingUsed(false);
 		
 		out.println("\tAverage Depth: " + f.format(averageDepth));
 		out.println("\tAverage Branches: " + f.format(averageBranches));
 		
 		for (ElementType t : elementTypes) {
 			Integer counter = elementData.get(t);
 			if (counter == null)
 				counter = 0;
 			out.println("\t# of " + t + " elements: " + counter);
 		}
 		
 		out.close();
 	}
 	
 	/**
 	 * This method is used to calculate the average of the supplied list of
 	 * Integer objects.
 	 * @param l The List of Integer objects
 	 * @return The calculated average
 	 */
 	private double calculateAverage(List<Integer> l) {
 		if ( (l == null) ||
 			 (l.size() == 0) )
 			return 0;
 		int size = l.size();
 		int total = 0;
 		for (Integer i : l) {
 			if (i != null)
 				total += i;
 		}
		return (double)((double)total / (double)size);
 	}
 	
 	/**
 	 * This method will return the maximum value from the supplied list
 	 * @param l A List of Integer objects
 	 * @return The max from the supplied list
 	 */
 	private int maximumValue(List<Integer> l) {
 		if ( (l == null) ||
 			 (l.size() == 0) )
 			return 0;
 		Collections.sort(l);
 		return l.get(l.size() - 1);
 	}
 	
 	private void captureBranchesAndDepth() {
 		if (counterData.getDepth() > 0)
 		    depthData.add(counterData.getDepth());
 		if (counterData.getBranches() > 0)
 		    branchData.add(counterData.getBranches());
 	}
 	
 	public class Counter implements Serializable {
 		private static final long serialVersionUID = 383764637309954294L;
 		private int depth;
 		private int branches;
 		
 		public void incrementDepth() { this.depth++; }
 		public void decrementDepth() { this.depth--; }
 		public void incrementBranches() { this.branches++; }
 		public void decrementBranches() { this.branches--; }
 		public int getDepth() { return depth; }
 		public int getBranches() { return branches; }
 	}
 }
