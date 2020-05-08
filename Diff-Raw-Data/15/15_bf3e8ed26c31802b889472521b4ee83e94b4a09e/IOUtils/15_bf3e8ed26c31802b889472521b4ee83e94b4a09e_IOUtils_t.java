 package com.scs.security;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.Closeable;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.List;
 
 /***
  * Utility I/O functions
  */
 public class IOUtils {
 
 	public static boolean fileExists(String filePath) {
 		return (new File(filePath)).isFile();
 	}
 
 	public interface Callback {
 		public void processLine(String data);
 	}
 
 	public static void readFile(String filePath, Callback c) {
 		if (!fileExists(filePath)) {
			return;
 		}
 		BufferedReader br = null;
 
 		try {
 			br = new BufferedReader(new FileReader(filePath));
 
 			String line;
 			while ((line = br.readLine()) != null) {
 				/* process the line here */
 				c.processLine(line);
 			}
 
 		} catch (FileNotFoundException e) {
			System.err.println("File at [" + filePath + "] not found");
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			/* close the BufferedReader */
 			close(br);
 		}
 	}
 
 	public static void writeToFile(List<String> lines, String filePath) {
 		File file = new File(filePath);
 
 		if (!file.isFile()) {
 			file = new File(filePath);
 		}
 
 		BufferedWriter bw = null;
 
 		try {
 			bw = new BufferedWriter(new FileWriter(file.getAbsolutePath()));
 
 			for (String str : lines) {
 				bw.write(str);
 				bw.newLine();
 			}
 
 			/* flush the BufferedWriter */
 			bw.flush();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			/* close the BufferedWriter */
 			close(bw);
 		}
 	}
 
 	/* read a line from the console */
 	public static String readFromConsole() {
 		String line = null;
 		BufferedReader br = null;
 		InputStreamReader isr = null;
 		try {
 			isr = new InputStreamReader(System.in);
 			br = new BufferedReader(isr);
 			line = br.readLine();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			// close(br); /* this is causing a IOException : Stream closed on
 			// multiples calls to function */
 		}
 		return line;
 	}
 
 	public static void close(Closeable c) {
 		if (c != null) {
 			try {
 				c.close();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}

	public static void main(String[] args) {

	}

}
