 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 
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
  * This is the class that contains the main entry point for the heapsort
  * program. It is also where the sort function lives. This class sets up the
  * arguments as well as makes the calls that set everything in motion.
  * 
  * @author Reese Moore
  * @author Tyler Kahn
  * @version 2011.10.26
  */
 public class heapsort {
 	/**
 	 * The entry point for this program.
 	 * @param arg The array of command line arguments passed in.
 	 */
 	public static void main(String[] arg)
 	{
 		// Make sure we have the correct number of arguments, if not, print an
 		// error message and exit.
 		if (arg.length != 3) {
 			String msg = "Usage: heapsort <data-file-name> <numb-buffers> " +
 						 "<stat-file-name>";
 			System.out.println(msg);
 			System.exit(2);
 		}
 		
 		// Set up the argument names to be more meaningful.
 		String datafile = arg[0];
 		int num_buffers = Integer.parseInt(arg[1]);
 		String statfile = arg[2];
 		
 		// Inform Stats of the datafile name
 		Stats.dataFileName = datafile;
 		
 		// Load up the Record Array
 		RecordArray array = null;
 		try {
 			array = new RecordArray(datafile, num_buffers);
 		} catch (FileNotFoundException e) {
 			System.err.println("File not found: " + datafile);
 			System.exit(-1);
 		} catch (IOException e) {
 			e.printStackTrace();
 			System.exit(-1);
 		}
 		
 		// Perform the Sort! (and time it
 		long t1 = System.currentTimeMillis();
		// perform the sort.
 		sort(array);
 		// Flush the array to disk so everything works nicely
 		array.flush();
 		Stats.sortExecutionTime = System.currentTimeMillis() - t1;
 		
 		//Append stats file
 		try {
 			// Make sure the file exists.
 			File stats = new File(statfile);
 			stats.createNewFile();
 			
 			// Write the data.
 			FileWriter statfileWriter = new FileWriter(stats, true);
 			BufferedWriter statOut = new BufferedWriter(statfileWriter);
 			statOut.write(Stats.formattedOutput());
 			statOut.flush();
 			
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
		
		// Print the output of the sort. The first record of every block.
		final int RECS_PER_ARRAY = 1024;
		for (int i = 0; i < (array.size() / RECS_PER_ARRAY); i++) {
			// Print the first record of the i-th block.
			System.out.printf("%5d %5d ", array.getKey(i * RECS_PER_ARRAY), 
										  array.getValue(i * RECS_PER_ARRAY));
			
			if (((i + 1) % 8 == 0)) { System.out.print("\n"); } // new line
		}
 	}
 
 	/**
 	 * Perform the heap sort on the record array passed in.
 	 * @param array The array to sort.
 	 */
 	public static void sort(RecordArray array)
 	{
 		int size = array.size();
 		RecordMaxHeap heap = new RecordMaxHeap(array);
 		for (int i = 0; i < size; i++) {
 			heap.removeMax();
 		}
 	}
 }
