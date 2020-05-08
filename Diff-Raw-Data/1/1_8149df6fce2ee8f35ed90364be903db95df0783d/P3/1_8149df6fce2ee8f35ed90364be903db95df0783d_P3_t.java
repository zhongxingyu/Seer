 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Scanner;
 
 // On my honor:
 //
 // - I have not used source code obtained from another student,
 // or any other unauthorized source, either modified or
 // unmodified.
 //
 // - All source code and documentation used in my program is
 // either my original work, or was derived by me from the
 // source code published in the textbook for this course.
 //
 // - I have not discussed coding details about this project with
 // anyone other than my partner (in the case of a joint
 // submission), instructor, ACM/UPE tutors or the TAs assigned
 // to this course. I understand that I may discuss the concepts
 // of this program with other students, and that another student
 // may help me debug my program so long as neither of us writes
 // anything during the discussion or modifies any computer file
 // during the discussion. I have violated neither the spirit nor
 // letter of this restriction.
 
 /**
  * Main P3 class for Project 3.
  * 
  * This class contains the main method for this project, which does
  * several things.  First, it deals with the command line parameter
  * and usage.  Second, it attempts to open and read lines from the
  * input file.  Third, it handles the commands by using the DNATree
  * class and database manager.  Fourth, it outputs appropriate errors
  * and prints.
  * 
  * @author Chris Schweinhart (schwein)
  * @author Nate Kibler (nkibler7)
  */
 public class P3 {
 
 	/**
 	 * Constant string patterns for command matching.  These are
 	 * used for regular expression matching with the commands
 	 * given by the input file.  They all allow for uppercase or
 	 * lowercase commands, but require uppercase sequences, with any
 	 * amount of spacing between arguments.
 	 */
 	private static final String INSERT_PATTERN = "^ *(insert|INSERT) *[ACGT]+ *[1-9][0-9]* *$";
 	private static final String REMOVE_PATTERN = "^ *(remove|REMOVE) *[ACGT]+ *$";
 	private static final String PRINT_PATTERN = "^ *(print|PRINT) *$";
 	private static final String SEARCH_PATTERN = "^ *(search|SEARCH) *[ACGT]+[$]? *$";
 	
 	/**
 	 * Additional string patterns for result matching.  These are
 	 * used for parsing the search results from the DNA Tree to use
 	 * with the database manager.
 	 */
 	private static final String KEY_PATTERN = "^Key: [ACGT]+$";
 	private static final String HANDLE_PATTERN = "^\\[[0-9]+, [1-9][0-9]*\\]$";
 	
 	/**
 	 * Member field for DNATree tree.  This tree represents the
 	 * sequences to be stored in memory, with each branch for
 	 * one letter of a DNA sequence.  For more information, look
 	 * in the DNATree.java file.
 	 */
 	private static DNATree tree;
 	
 	/**
 	 * Member field for DatabaseManager.  This memory manager will
 	 * keep track of the bytes in memory for each DNA sequence.
 	 * Also keeps track of the free memory blocks.  For more
 	 * information, look in the DatabaseManager.java file.
 	 */
 	private static DatabaseManager dbm;
 	
 	/**
 	 * Main method to control data flow.  This function takes
 	 * the command line parameter as input and uses it to read
 	 * from the input file, executing and outputting commands
 	 * along the way.
 	 * 
 	 * @param args - the command line arguments
 	 */
 	public static void main(String[] args) {
 				
 		// Check for proper usage
 		if (args.length != 1) {
 			System.out.println("Usage:");
 			System.out.println("P3 COMMAND_FILE");
 			System.exit(0);
 		}
 		
 		String fileName = args[0];
 		
 		tree = new DNATree();
 		dbm = new DatabaseManager();
 		
 		// Main command line reading
 		try {
 			
 			// Attempt to open the input file into a buffered reader
 			BufferedReader in = new BufferedReader(new FileReader(fileName));
 			
 			// Keep reading in commands until we reach the EOF
 			String line;
 			while ((line = in.readLine()) != null) {
 				if (line.matches(INSERT_PATTERN)) {
 					
 					// Parse out the sequence id from the command line
 					int index = Math.max(line.indexOf("r"), line.indexOf("R")) + 2;
 					String sequence = line.substring(index);
 					sequence = sequence.trim();
 					
 					// Get the next line for the sequence
 					String entry = in.readLine();
 					
 					// Add to the dbm
 					Handle handle = dbm.insert(entry);
 					
 					// Add to tree
 					int result = tree.insert(sequence, handle);
 					
 					if(result < 0) {
						dbm.remove(handle);
 						System.out.println("Sequence " + sequence + " already in tree.");
 					} else {
 						System.out.println("Sequence " + sequence + " inserted at level " + result + ".");
 					}
 				} else if (line.matches(REMOVE_PATTERN)) {
 					
 					// Parse out the sequence id from the command line
 					int index = Math.max(line.indexOf("v"), line.indexOf("V")) + 2;
 					String sequence = line.substring(index);
 					sequence = sequence.trim();
 					
 					// Remove sequence id from tree
 					Handle handle = tree.remove(sequence);
 					
 					// Remove sequence from dbm
 					if(handle == null) {
 						System.out.println("Sequence " + sequence + " not found in tree.");
 					} else {
 						dbm.remove(handle);
 					}
 				} else if (line.matches(PRINT_PATTERN)) {
 					
 					// Output the tree
 					System.out.println(tree);
 					
 					// Output free blocks
 					System.out.println(dbm);
 				} else if (line.matches(SEARCH_PATTERN)) {
 					
 					// Parse out the sequence id from the command line
 					int index = Math.max(line.indexOf("h"), line.indexOf("H")) + 1;
 					String sequence = line.substring(index);
 					sequence = sequence.trim();
 					
 					// Search the tree for handles
 					String results = tree.search(sequence);
 					Scanner scan = new Scanner(results);
 					String output = scan.nextLine() + "\n";
 					
 					// Augment output with results from dbm
 					String entry;
 					int offset, length;
 					Handle handle;
 					while (scan.hasNextLine()) {
 						entry = scan.nextLine();
 						
 						if (entry.matches(KEY_PATTERN)) {
 							output += entry + "\n";
 						} else if (entry.matches(HANDLE_PATTERN)) {
 							
 							// Parse out offset and length
 							offset = Integer.parseInt(entry.substring(entry.indexOf('['), entry.indexOf(',')));
 							length = Integer.parseInt(entry.substring(entry.indexOf(','), entry.indexOf(']')));
 							
 							// Create handle and use it to query database
 							handle = new Handle(offset, length);
 							output += "Sequence:\n" + dbm.getEntry(handle) + "\n";
 						} else {
 							continue;
 						}
 					}
 					
 					scan.close();
 					
 					System.out.println(output);
 				} else {
 					continue;
 				}
 			}
 			
 			in.close();
 		}  catch (FileNotFoundException e) {
 			System.out.println("The input file could not be found.");
 			System.exit(0);
 		} catch (IOException e) {
 			System.out.println("Error reading from file.");
 			System.exit(0);
 		} catch (Exception e) {
 			System.out.println("Incorrect file formatting.");
 			e.printStackTrace();
 			System.exit(0);
 		}
 	}
 }
