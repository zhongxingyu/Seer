 package what.sp_parser;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import what.FileHelper;
 
 import forTesting.Localize;
 
 /** 
  * 
  * The LogFile class is the gateway between parser and a log file - it contains the path of the logfile 
  * and an integer  which saves how many lines have been read from this file. 
  * It can read single lines from the logfile and return them to the ParserMediator.
  * 
  * @author Alex
  *
  */
 
 public class Logfile {
 
 	
 	/**
 	 * Number of lines which are already read from the file.
 	 */
 	private int lines;
 	
 	/**
 	 * The type of the csv-file.
 	 */
 	private String type;
 	
 	/**
 	 * The file-object at <code>path</code>
 	 */
 	private File file;
 	
 	/**
 	 * The ParserMediator which is connected to the <code>Logfile</code>
 	 */
 	private ParserMediator pm;
 	
 	/**
 	 * InputStreams and Reader for reading the <code>file</code>
 	 */
 	private FileInputStream fstream = null;
 	private DataInputStream in = null;
 	private BufferedReader br = null;
 	
 	/**
 	 * @return the ParserMediator
 	 */
 	protected ParserMediator getPm() {
 		return pm;
 	}
 
 	/**
 	 * @param pm the ParserMediator to set
 	 */
 	protected void setPm(ParserMediator pm) {
 		this.pm = pm;
 	}
 
 	/**
 	 * @return the number of lines which are already read
 	 */
 	public int getLines() {
 		return lines;
 	}
 
 	/**	
 	 * @return The type of the logfile
 	 */
 	protected String getType() {
 		return type;
 	}
 
 	/**
 	 * The constructor for a new <code>Logfile</code>. It sets the @param path, 
 	 * searches the <code>File</code> at this path and creates a file-object. 
 	 * Then it sets the InputStreams and the BufferedReader and sets the <code>type</code>
 	 * to the first line of the file.
 	 */
 	protected Logfile(String path, ParserMediator pm) {
 		
 		//Initialization and checks if file is correct.
 		this.pm = pm;
 		this.lines = 1;
 		
 		this.file = FileHelper.getFileForExtension(path, FileHelper.CSV);
 		
 		if (file == null) {
 			pm.error(Localize.getString("Error.50"));
 			return;
 		}
 		
 		try {
 			fstream = new FileInputStream(file);
 		} catch (FileNotFoundException e) {
 			//@throws FileNotFoundException - only if the file doesn't exist after checking that it does exist : Shouldn't happen.
 			e.printStackTrace();
 			return;
 		}
 		
 		
 		
 		//Creates BufferedReader which will read the file.
 		in = new DataInputStream(fstream);
 		br = new BufferedReader(new InputStreamReader(in));
 		
 		try {
 			//Type is the first line of the file. (This is standard in the .csv-format)
 			type = br.readLine();
 	
 			if (!VerificationTool.checkConfig(this)) {
 				pm.error(Localize.getString("Error.110"));
 				return;
 			} 
 			
 		} catch (IOException e) {
 			if (!file.canRead()) {
 				pm.error(Localize.getString("Error.71"));
 				return;
 			} else {
 				pm.error(Localize.getString("Error.73"));
 				return;
 			}
 			
 		}
 		
 	}
 	
 	/**
 	 * Returns the next line from <code>file</code> and iterates <code>lines</code>.
 	 * @return the next line from <code>file</code>
 	 */
 	protected synchronized String readLine() {
 		String str = null;
 		String str2;
 		try {
 						
 			
 			str = br.readLine();
 						
 			if (str == null) {
 				return str;
 			}
 			
 			
 			// Those lines of code make sure, that a line is actually a complete line. This is needed because some statements
 			//contain endOfLines which are the same as the endOfLines from the .csv-file. The line gets returned when a line is complete.
 			boolean goOn = true;
 			while (goOn) {
 				br.mark(10000);
 				str2 = br.readLine();
 				if (str2 == null) {
 					return str;
 				}
 				if (!str2.startsWith("20")) {
 					str += str2;
 				} else {
 					br.reset();
 					goOn = false;
 				}
 			}
 			
 		} catch (IOException e) {
 			//I know, try-blocks shouldn't be that big, but there are many readLine()-calls and it doesn't matter in which one the error occurred, the exception-handling is similar.
 			if (!file.canRead()) {
 				pm.error(Localize.getString("Error.72P1") + " " + lines + " " + Localize.getString("Error.72P2"));
 			} else {
 				pm.error(Localize.getString("Error.74P1") + " " + lines + " " + Localize.getString("Error.74P2"));
 			}
 		}
 		lines++;
 		
 		return str;
 	}
	
 }
