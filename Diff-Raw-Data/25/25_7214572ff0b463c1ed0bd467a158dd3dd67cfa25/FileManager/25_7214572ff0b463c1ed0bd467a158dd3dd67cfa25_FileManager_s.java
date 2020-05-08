 package mo.umac.umsu.UMSUDemocrat.VoteCounting;
 
 import java.io.*;
 import java.util.*;
 
 public class FileManager {
 	
	private static String fileName = System.getProperty("user.dir") + "/data/groups.txt";
 
 	public Vector<String> Read() {
 
 		Vector<String> groups = new Vector<String>(9);
 		String line = null;
 
 		try {
 
 			FileReader fileStream = new FileReader(fileName);
 			BufferedReader in = new BufferedReader(fileStream);
 
 			while ((line = in.readLine()) != null) {
 
 				System.out.println(line);
 				groups.add(line);
 
 			}
 
 			in.close();
 			
 		} catch (FileNotFoundException ex) {
 			System.out.println("Unable to open file '" + fileName + "'");
 		} catch (IOException ex) {
 			System.out.println("Error reading file '" + fileName + "'");
 
 		}
 		
 		return groups;
 		
 	}
 	
 	public boolean Write(Vector<String> groups){
 	
 		try{
 			
 			FileWriter fileStream = new FileWriter(fileName);
 			BufferedWriter out = new BufferedWriter(fileStream);
 			int size = groups.size();
 			
 			for(int i = 0; i < size; ++i){
 				
 				out.write(groups.get(i) + "\n");
 				
 			}
 			
 			out.close();
 		
 		}catch (FileNotFoundException ex) {
 			System.out.println("Unable to open file '" + fileName + "'");
 			return false;
 		} catch (IOException ex) {
 			System.out.println("Error reading file '" + fileName + "'");
 			return false;
 		}
 		
 		return true;
 
 	}
 
 }
