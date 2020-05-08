 package net.teamwraith.npctalk;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 
 public class Files {
 	
 	public static void writeRawFile (int lines, String[] str, String fileName) {
 		writeRawFile(lines, str, new File(fileName + ".txt"));
 	}
 	
 	public static void writeRawFile (int lines, String[] str, File file) {
 		BufferedWriter writer;
 
 		try {
 			writer = new BufferedWriter(new FileWriter(file));
 			
 			file.createNewFile();
 		for(int l=0; l < lines; l++){
			writer.append(str[l]);
 		}
 			//FOR DEBUGGING
 			//System.out.println("wrote in file: " + file.getAbsolutePath());
 			writer.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 }
