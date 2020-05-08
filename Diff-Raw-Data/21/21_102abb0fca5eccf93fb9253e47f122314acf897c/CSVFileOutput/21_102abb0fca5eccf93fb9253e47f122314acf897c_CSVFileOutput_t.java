 package eu.liveandgov.wp1.backend.sensorLoop;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.PrintStream;
 
 public class CSVFileOutput {
 	private PrintStream old;
 	
 	public CSVFileOutput(String filePath) throws FileNotFoundException {
 		old = System.out;
 	    File file  = new File(filePath);
	    PrintStream printStream = new PrintStream(new FileOutputStream(file, false));
 	    System.setOut(printStream);
 	}
 	
     protected void finalize( ) throws Throwable  {
     	System.setOut(old);
     	super.finalize();
     }
 }
