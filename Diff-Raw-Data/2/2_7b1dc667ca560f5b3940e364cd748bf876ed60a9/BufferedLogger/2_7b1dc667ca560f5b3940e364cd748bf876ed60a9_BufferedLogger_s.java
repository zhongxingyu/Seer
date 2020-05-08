 package eu.icecraft.iceauth;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 
 public class BufferedLogger {
 
 	private static PrintWriter theWriter;
 	private static ArrayList<String> buffer = new ArrayList<String>();
 
 	private static int bfSize;
 
 	public BufferedLogger(File logPath, int bufferSize) {
 		bfSize = bufferSize;
 		load(logPath);
 	}
 
 	public void log(String text) {
 		buffer.add(text);
 		if (buffer.size() >= bfSize) commit();
 	}
 
 	public void disable() {
 		commit();
 		theWriter.close();
 	}
 
 	public void commit() {
 		for (String s : buffer) {
 			theWriter.println(s);
 		}
 
 		buffer.clear();
 		theWriter.flush();
 	}
 
 	public void load(File logPath) {
 		try {
			theWriter = new PrintWriter(logPath, true);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 }
