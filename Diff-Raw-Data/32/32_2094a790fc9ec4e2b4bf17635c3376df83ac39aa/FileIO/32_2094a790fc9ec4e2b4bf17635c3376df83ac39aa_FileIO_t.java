 package bitter;
 
 import java.io.*;
import java.nio.*;
import java.nio.charset.Charset;
 
public class FileIO  interface Path {
	final public Charset CHARSET;
 	
	public FileIO() {
		CHARSET = Charset.forName("US-ASCII");
	}
	public void writeToFile (String text, File file) {
		try (BufferedWriter writer = File.newBufferedWriter(file, CHARSET)) {
		    writer.write(text, 0, text.length());
		} catch (IOException x) {
		    System.err.format("IOException: %s%n", x);
		}
	}
	public void readFromFile (File file) {
		try (BufferedReader reader = Files.newBufferedReader(file, charset)) {
		    String line = null;
		    while ((line = reader.readLine()) != null) {
		        System.out.println(line);
		    }
		} catch (IOException x) {
		    System.err.format("IOException: %s%n", x);
		}
 	}
 }
