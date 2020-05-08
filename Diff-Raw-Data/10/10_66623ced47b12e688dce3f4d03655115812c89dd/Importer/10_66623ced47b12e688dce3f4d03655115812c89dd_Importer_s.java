 package parser;
import java.io.*;
 
 import org.apache.log4j.Logger;
 
 
 public class Importer {
 	Logger log = Logger.getLogger(getClass());
 	static private BufferedReader br;
 	static private BufferedReader br2;
	public Importer(String fileName){
		log.debug("New importer created. Filename: "+fileName);
 		try {
 			br = new BufferedReader(new FileReader(fileName));
 			br2 = new BufferedReader(new FileReader(fileName));
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/*
 	 * Reads through the file and stores the number of lines.
 	 */
 	int getNumberOfLines(){
 		int numberOfInstructions = 0;
 		String nextLine;
 		boolean moreInstructions = true;
 		
 		while (moreInstructions){
 			nextLine = getNextLine2();
 			if (nextLine == null){
 				moreInstructions = false;
 			}else{
 				numberOfInstructions++;
 			}
 			
 		}
 		log.debug("Number of lines found: "+ numberOfInstructions);
 		return numberOfInstructions;
 	}
 	
 	/*
 	 * Returns a String[] of all lines in the file
 	 * 
 	 */
 	public String[] getInstructions(){
 		int n = getNumberOfLines();
 		String[] out = new String[n];
 		for (int i = 0; i<n ; i++){
 
 		try {
 			out[i] = br.readLine();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		}
 		return out;
 	}
 	public String getNextLine2(){
 		String out = new String();
 		try {
 			out = br2.readLine();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}   
 		return out;
 	}
 }
