 package output;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintStream;
 
 /**
  * This class can be used to output a schedule to an IPE file.
  * 
  * @author Thom Castermans
  */
 public class OutputIpe {
 	
 	private PrintStream output;
 	
 	public static void main(String[] args) {
 		OutputIpe oi = new OutputIpe();
 		oi.outputHeader();
 		oi.outputFooter();
 	}
 	
 	/**
 	 * Create a new object capable of outputting to the default output.
 	 */
 	public OutputIpe() {
 		output = System.out;
 	}
 	
 	private void outputFromFile(String path) {
		InputStream is = getClass().getResourceAsStream(path);
 		// Read header from file and output it to the stream
 		byte[] buffer = new byte[4096]; // tweaking this number may increase performance  
 		int len;  
 		try {
 			while ((len = is.read(buffer)) != -1)  
 			{  
 			    output.write(buffer, 0, len);  
 			}
 			output.flush();
 			output.println();
 			is.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}  
 	}
 	
 	private void outputFooter() {
		outputFromFile("/ipe_footer.txt");
 	}
 	
 	private void outputHeader() {
		outputFromFile("/ipe_header.txt");
 	}
 	
 }
