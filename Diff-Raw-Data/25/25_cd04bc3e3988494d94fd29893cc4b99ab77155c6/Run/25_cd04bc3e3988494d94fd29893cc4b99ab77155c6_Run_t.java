 /* Simple code to extract some quiz questions and answers.
  * Comments added liberally to aid readers who are new to Java or to programming
  * 
  * To use this program:
  * Copy and paste your questions and solutions into a text file and save. Put ! on its own line to separate each question.
  * Run program and navigate to your file and select it. Program will automatically run and generate two new files: 
  * A list of questions and an answer key.
  * 
  * 
  * Imports
  * IO imports file input/output for reading and writing.
  * Scanner is used to read command line if file selector option fails.
  * awt and swing handle file opening GUI elements
  */
 
 import java.awt.Panel;
 import java.io.*;
 import javax.swing.JFileChooser;
 
 // As a natively OOP language, everything that executes must run inside of a class
 public class Run {
 
 	// main is required and always executes first. args are not used in this program, but they allow for command line inputs if used.
 	public static void main(String[] args) {
 		convert(selector());	// this main code is pretty short, it just calls two static methods.
 								// a static method is just a function that doesn't require an object.
 	}
 	
 	//append to a given file name
 	static String appendname(String name, String append) {
 		  return name.replaceFirst(".txt", append+".txt");	// in java, strings are objects too so we have to call a method to regex (replace text with some other text)
 	}
 	static String selector() {
 		String filename;	// this is the filename of our selected file. we have to declare variables in Java. This variable is currently null. it points to nothing.
 		JFileChooser fc = new JFileChooser();			// Java's libraries allow us to easily create premade GUI elements. 
 		int returnVal = fc.showOpenDialog(new Panel()); // We create a basic panel to build our dialog on top of. This is the only element in our panel but panels can hold more than one thing.
 
 		File file;
 		filename = "";
 		if (returnVal == JFileChooser.APPROVE_OPTION) {		// this allows the code to only run if the user selects and accepts a file.
 		    file = fc.getSelectedFile();
 		    												//Now you have your file to do whatever you want to do
 		    filename = file.getAbsolutePath();				
 		} else {
 			System.exit(0);
 		}
 		return filename;
 		
 	}
 	static void convert(String filename) {	
 				// built in IO classes help us read and write from our files
 			  FileReader infile = null;
 			  FileWriter outfile = null;
 			  FileWriter ansfile = null;
 			  
 			  try {
 				  // create new files with similar names to the original file
 				  String outfilename = appendname(filename, "_qst");
 				  String ansfilename = appendname(filename, "_ans");
 				  
 				  // more IO initialization
 			      outfile = new FileWriter(outfilename);
 			      ansfile = new FileWriter(ansfilename);
 			      BufferedWriter writer = new BufferedWriter(outfile);		
 			      BufferedWriter awriter = new BufferedWriter(ansfile);
 			      
 			      // checking for existing directory.. strictly speaking this isn't necessary here but it is included for the sake of example.
 			      File file = new File(outfilename);
 			      File parent = file.getParentFile();
 			      
 			      // attempt to make the directory if it does not exist.
 			      if(!parent.exists() && !parent.mkdirs()){									// !parent.mkdirs() -> if it fails to create the directory
 			          throw new IllegalStateException("Couldn't create dir: " + parent);
 			      } 
 			      
 				// more IO initialization
 			    infile = new FileReader(filename);
 			    BufferedReader reader = new BufferedReader(infile);
 			    
 			    String line = "";
 			    int w1 = 0,w2 = 0;
 			    int qNum = 1;
	    		String q = "Question #"+qNum;
	    		String s = "Solution #"+qNum;
				writer.newLine();
    			writer.write(q);
    			writer.newLine();
    			writer.newLine();
    			
    			awriter.newLine();
	    		awriter.write(s);
	    		awriter.newLine();
    			awriter.newLine();
 			    // read in one line at a time until we reach the end of the file
 			    while ((line = reader.readLine()) != null) {
 			    	
 			    	// ignore empty lines
 			    	if(!line.isEmpty()) {
 			    		
 			    		// strip out leading space
 				    	while(line.charAt(0)==' '&&line.length()>1) {		
 				    		line = line.substring(1);
 				    	}
 				    	
 				    	// store comments as questions
 				    	if(line.charAt(0)=='#') {
 				    		
 				    		// count the number of lines being written to the file. ++ is the same as w = w+1 or w += 1
 				    		w1++;
 				    		// write the line and start a new one: exclude the # tag
 				    		writer.write("    "+line.substring(1));
 				    		writer.newLine();
 				    	}	// write to the answer key if the line has none of the tags to indicate a new problem
 				    	else if(line.charAt(0)!='!'&&!line.startsWith("Quiz")&&!line.startsWith("Problem")) {
 				    		// count the number of lines being written to the file
 				    		w2++;
 				    		// write the line and start a new one
 				    		awriter.write("    "+line);
 				    		awriter.newLine();
 				    	}	// write the answers
 				    	
 				    	//recognize a new question if the line starts with 'Quiz' or '!' or 'Problem'
 				    	else {
 				    		// count the number of lines being written to the file
				    		q = "Question #"+qNum;
				    		s = "Solution #"+qNum;
 				    		w1++;
 				    		w2++;
				    		qNum++;
 				    			// write the line and start a new one. Add whitespace
 			    				writer.newLine();
 				    			writer.write(q);
 				    			writer.newLine();
 				    			writer.newLine();
 				    			
 				    			awriter.newLine();
 					    		awriter.write(s);
 					    		awriter.newLine();
 				    			awriter.newLine();
 				    			
 				    	}
 			    	}
 			    }
 			    awriter.close();
 			    writer.close();			// files, io streams, and buffers must be closed
 			    

 			    System.out.println(qNum+ " questions were written in "+w1+ " lines. ");
 			    System.out.println(qNum+ " answers were written in "+w2+" lines. ");
 			    
 			  } catch (Exception e) {
 			      throw new RuntimeException(e);	// I/O create exceptions that Java requires us to catch. 
 			  } finally {							// These will display to the user if something fails during runtime.
 			    if (infile != null) {
 			      try {
 			        infile.close();
 			      } catch (IOException e) {
 			        System.out.println("Error: did not close file properly.");	// we can also choose to create our own messages during an exception.
 			      }
 			    }
 			    if (outfile != null) {
 		        try {
 		            outfile.close();
 		          } catch (IOException e) {
 		            // Ignores issues during closing
 		          }
 			  }
 			    if (ansfile != null) {
 			        try {
 			            outfile.close();
 			          } catch (IOException e) {
 			            // Ignores issues during closing
 			          }
 				  }
 			    
 			  }
 	}
 
 }
