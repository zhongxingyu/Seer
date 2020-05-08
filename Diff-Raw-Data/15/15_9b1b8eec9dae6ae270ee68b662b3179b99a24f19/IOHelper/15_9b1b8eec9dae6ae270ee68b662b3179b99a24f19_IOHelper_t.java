 package rpg;
 
 import java.io.*;
 
 public class IOHelper {	
 	
 	/**Reads from the specified file to System.out
 	 * 
 	 * @param file: The file to be read from
 	 */
 	public static void read(File file) {
 		try {
 			@SuppressWarnings("resource")
 			FileReader fr = new FileReader(file);
 			int ch = fr.read();
 			
 			while(ch != -1) {
 				System.out.println((char) ch);
 				ch = fr.read();
 			}
 			
 		}catch(Exception exc) {
 			exc.printStackTrace();
 		}
 	}
 	
    public static String readFile(String file) throws IOException {
    	@SuppressWarnings("resource")
        BufferedReader reader = new BufferedReader(new FileReader(file));
	    String line = null;
	    StringBuilder stringBuilder = new StringBuilder();
	    String ls = System.getProperty("line.separator");
 
 	    while( ( line = reader.readLine() ) != null ) {
 	        stringBuilder.append( line );
 	        stringBuilder.append( ls );
 	    }
	    
 	    return stringBuilder.toString();
 	}
 	
 	/** Writes the specified message to the specified file
 	 * 
 	 * @param file: The file message will be written to
 	 * @param message: The message to be written in the file
 	 */
 	public static void write(File file, String message) {
 		try {
 			FileWriter fw = new FileWriter(file);
 			fw.write(message);
 			fw.close();
 		} catch(Exception exc) {
 			exc.printStackTrace();
 		}
 	}
 }
