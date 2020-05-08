 package com.Zolli.EnderCore.File;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Reader;
 import java.io.StringWriter;
 import java.io.Writer;
 
 import org.bukkit.craftbukkit.libs.jline.internal.InputStreamReader;
 
 public class fileUtils {
 	
 	/**
 	 * File object
 	 */
 	private File file;
 	
 	/**
 	 * BufferedReader object
 	 */
 	private BufferedReader bReader;
 	
 	/**
 	 * BufferedWriter object
 	 */
 	private BufferedWriter bWriter;
 	
 	/**
 	 * Constructor of this object
 	 * @param file The file to be parsed
 	 */
 	public fileUtils (File file) {
 		this.file = file;
 		this.bWriter = this.write();
 		this.bReader = this.read();
 	}
 	
 	/**
 	 * Write the give string to end of the file and add a new line for future addition
 	 * @param s The string to be written
 	 */
 	public void writeLineToEnd(String s) {
 		try {
 			this.bWriter.write(s);
 			this.bWriter.newLine();
 			this.bWriter.flush();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Read the file defined in constructor
 	 * @return BufferedReader object if successful, null when open fails
 	 */
 	private BufferedReader read() {
 		try {
 			BufferedReader bReader = new BufferedReader(new FileReader(this.file));
 			return bReader;
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	/**
 	 * Open file defined in constructor for writing
 	 * @return BufferedWriter object if successful, null when open fails
 	 */
 	private BufferedWriter write() {
 		try {
 			BufferedWriter bWriter = new BufferedWriter(new FileWriter(this.file, true));
 			return bWriter;
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	/**
 	 * Parse inputStreams to a string
 	 * @param in The input stream
 	 * @return A string contains inputStram content
 	 */
 	public static String streamToString(InputStream in) {
 		if (in != null) {
 		    Writer writer = new StringWriter();
 
 		    char[] buffer = new char[1024];
 		    try {
		        @SuppressWarnings("resource")
				Reader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
 		        int n;
 		        while ((n = reader.read(buffer)) != -1)
 		            writer.write(buffer, 0, n);
 		    } catch (IOException e) {
 				e.printStackTrace();
 			} finally {
 		        try {
 					in.close();
 				} catch (IOException e) {}
 		    }
 		    return writer.toString();
 		} else     
 		    return "";
 	}
 	
 	public void finalize() {
 		try {
 			this.bReader.close();
 			this.bWriter.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 }
