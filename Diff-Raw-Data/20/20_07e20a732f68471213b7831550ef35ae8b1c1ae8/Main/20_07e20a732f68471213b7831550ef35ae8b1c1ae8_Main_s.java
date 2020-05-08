 package com.rockwellautomation.verification.framework;
 
 import java.io.File;
 import java.io.FileOutputStream;
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
 	private static OperationType[] operationTypes = OperationType.values();
 	private static DictionaryType[] dictionaryTypes = DictionaryType.values();
 	private static ElementType[] elementTypes = ElementType.values();
 	
 	/**
 	 * Main entry point of the program
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		
 		// Process the supplied arguments
 		processArgs(args);
 
 		try {
 		    // Create the operations
 		    createOperations();
 		    
 		    // Create the data
 			createData();
 		}
 		catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	/**
 	 * This method will process the program arguments
 	 * @param args
 	 */
 	private static void processArgs(String[] args) {
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
 	}
 	
 	/**
 	 * This method will create the test data
 	 * @throws Exception
 	 */
 	private static void createData() throws Exception {
 		FileOutputStream out = null;
 		
 		// Make sure that data should be created
 		if (max <= 0) {
 			System.out.println("No test data elements were generated");
 			return;
 		}
 		
 		try {
 			// Create the root builder and add elements to it
 			Element.Builder root = Element.newBuilder();
 			// TODO the root has to have data - make this more random
 			root.setExtension(Element.stringValue, "ROOT!!");
 			while (total < max)
 			    createElement(root, -1, -1);
 			
 			// Write the data to the output file
 			String file = "out" + File.separator + "testdata.dat";
 			out = new FileOutputStream(file);
 			Element e = root.build();
 			e.writeTo(out);
 			
 			System.out.println("Created " + total + " elements: ");
 			System.out.println(e);
 		}
 		finally {
 			try { if (out != null) out.close(); } catch (Exception ignore) { ignore.printStackTrace(); }
 		}
 		
 		System.out.println(total + " elements were created");
 	}
 	
 	private static void createElement(Element.Builder parent, int currentDepth, int currentBranches) {
 		if ( (total >= max) ||
 			 (currentDepth >= depth) ||
 			 (currentBranches >= branches) )
 			return;
 		
 		int randDepth = rand.nextInt(depth);
 		int randBranches = rand.nextInt(branches);
 		
 		// Do a bit of random-ness to ensure that not
 		// all nodes are equal in size.
 		if ( (currentDepth > randDepth) ||
 			 (currentBranches > randBranches) )
 			return;
 		
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
				String value = getDictionaryValue();
				if (value == null)
					value = DictionaryType.NULL.name();
				child.setExtension(Element.stringValue, value);
 		}
 		
 		// Increment the counters
 		total++;
 		
 		// Capture a random flag used to determine if a sibling or child
 		// should be created
 		boolean sibling = rand.nextBoolean();
 		
 		// Determine if more children should be added
 		if (!sibling)
 			createElement(child, currentDepth + 1, currentBranches);
 		else
 			createElement(parent, currentDepth, currentBranches + 1);
 		
 		// Add the child to the parent and increment the total
 		parent.addChildren(child);
 	}
 
 	/**
 	 * This method will create the operational data used to perform operations
 	 * on the test data.
 	 * @throws Exception
 	 */
 	private static void createOperations() throws Exception {
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
				if (type == OperationType.SEARCH) {
					String search = getDictionaryValue();
					if (search != null)
					    b.setSearch(search);
				}
 				
 				// Add the operation to the parent
 				builder.addOperations(b.build());
 			}
 			
 			// Write the main operation to the output file
 			String file = "out" + File.separator + "operations.dat";
 			out = new FileOutputStream(file);
 			Operations o = builder.build();
 			o.writeTo(out);
 			System.out.println("Created " + i + " opertaions:");
 			System.out.println(o);
 		}
 		finally {
 			try { if (out != null) out.close(); } catch (Exception ignore) { ignore.printStackTrace(); }
 		}
 	}
 	
 	/**
 	 * This method will get the string name of the word from the dictionary.
 	 * @return
 	 */
 	private static String getDictionaryValue() {
		DictionaryType dict = dictionaryTypes[rand.nextInt(dictionaryTypes.length)];
		// If the enum has a name of NULL, then return a null string
		if (dict == DictionaryType.NULL)
			return null;
		return dict.name();
 	}
 }
