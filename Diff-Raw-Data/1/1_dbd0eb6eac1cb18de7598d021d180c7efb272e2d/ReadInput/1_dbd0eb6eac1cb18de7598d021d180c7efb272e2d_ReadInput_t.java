 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.*;
 
 public class ReadInput{
 	public static Vector<LineInterface> readLine(){
 		//Get number of lines to read
 		System.out.printf("Number of lines to enter: ");
 		Scanner myScan = new Scanner(System.in);
 		int numOfLines = myScan.nextInt();
		myScan.nextLine();
 		
 		Vector<LineInterface> myLineVector = new Vector<LineInterface>();
 		
 		//loop through all lines and add it to myLineVector.
 		for(int i=0; i<numOfLines; i++){
 			String currLine = myScan.nextLine();
 			LineInterface currLineInterface = new Line(currLine);
 			myLineVector.add(currLineInterface);
 		}
 		
 		return myLineVector;
 	}
 	
 	public static Vector<LineInterface> readLine(String fileName){
 		Vector<LineInterface> myLineVector = new Vector<LineInterface>();
 		File lineFile = new File(fileName);
 		
 		FileReader myFileReader = null;
 		try{
 			myFileReader = new FileReader(lineFile);
 		}
 		catch(FileNotFoundException fnfe){
 			//crash out here
 		}
 		
 		BufferedReader myBufferedReader = new BufferedReader(myFileReader);
 		
 		try{
 			String currLine = myBufferedReader.readLine();
 			while(currLine != null){
 				LineInterface myLineInterface = new Line(currLine);
 				myLineVector.add(myLineInterface);
 				
 				currLine = myBufferedReader.readLine();
 			}
 		}
 		catch(IOException ioe){
 			//crashed out here
 		}
 		finally{
 			try{
 				myBufferedReader.close();
 			}
 			catch(IOException ioe){
 				//do nothing here
 			}
 		}
 		
 		return myLineVector;
 	}
 }
