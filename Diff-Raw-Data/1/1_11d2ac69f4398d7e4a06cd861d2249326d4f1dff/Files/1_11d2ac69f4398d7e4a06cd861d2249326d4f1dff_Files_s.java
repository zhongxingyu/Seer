 package net.teamwraith.npctalk;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 
 public class Files {
 	
 	/**
 	 * Calls {@link #writeRawFile(String[], File)}
 	 * but with a string as the filename (.txt 
 	 * appended). Merely for convenience.
 	 */
 	public static void writeRawFile (String[] str, String fileName) {
 		writeRawFile(str, new File(fileName + ".txt"));
 	}
 	
 	/**
 	 * Writes lines provided as raw text. Does NO
 	 * formatting at all, and is (probably)
 	 * temporary.
 	 * 
 	 * @param lines - array of the lines to be
 	 * written to a file.
 	 * @param file - the file to write to; if it
 	 * doesn't exist, it'll be created.
 	 */
 	public static void writeRawFile (String[] lines, File file) {
 		BufferedWriter writer;
 		
 		try {
 			writer = new BufferedWriter(new FileWriter(file));
 			file.createNewFile();
 			
 			for(int i = 0; i < lines.length; i++) {
 				writer.append(lines[i]);
 			}
 		
 			System.out.println("wrote in file: " + file.getAbsolutePath());
 			writer.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 }
