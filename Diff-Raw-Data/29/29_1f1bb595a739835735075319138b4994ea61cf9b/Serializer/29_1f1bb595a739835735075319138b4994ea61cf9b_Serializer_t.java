 package edu.wheaton.simulator.statistics;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 
 public class Serializer {
 
 	public static void serializer(String serializable) {
 		String filename = "tester.txt"; // TODO: Change if necessary
 
 		try {
 			BufferedWriter writer = new BufferedWriter(
 					new FileWriter(filename));
 			writer.write(serializable);
 			writer.close();
 		} catch (IOException e) {
			System.err.println("Serializer.java: IOException");
 			e.printStackTrace();
 		}
 
 		// TODO: Move to a separate method
 		try {
 			BufferedReader reader = new BufferedReader(
 					new FileReader(filename));
 
 			StringBuilder sb = new StringBuilder();
 			String line = reader.readLine();
 			String savefile;
 
 			while (line != null) {
 				sb.append(line);
 				sb.append("\n");
 				line = reader.readLine();
 			}
 
 			savefile = sb.toString();
 			
 			reader.close();
 			
 			System.out.println(savefile + "\nDone."); //TODO Console tester.
 			
 		} catch (FileNotFoundException e) {
			System.err.println("Serializer.java: FileNotFoundException");
 			e.printStackTrace();
 		} catch (IOException e) {
			System.err.println("Serializer.java: IOException");
 			e.printStackTrace();
 		}
 	}
 
 }
