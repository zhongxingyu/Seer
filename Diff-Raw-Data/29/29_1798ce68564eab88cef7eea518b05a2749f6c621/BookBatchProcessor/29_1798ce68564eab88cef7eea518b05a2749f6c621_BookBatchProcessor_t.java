 package ie.cit.patrick.service.impl;
 
 import ie.cit.patrick.service.BatchProcessor;
 
 import java.io.BufferedReader;
import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import org.springframework.beans.factory.annotation.Autowired;
 
 
 public class BookBatchProcessor implements BatchProcessor {
 	
 	String fileLocation;
 	String delineator;
 	
 	
 	public BookBatchProcessor(){
 	}
 	
 	public BookBatchProcessor(String fileLocation, String delineator) {
 		this.fileLocation = fileLocation;
 		this.delineator = delineator;
 	}
 	public String getFileLocation() {
 		return fileLocation;
 	}
 
 	public void setFileLocation(String fileLocation) {
 		this.fileLocation = fileLocation;
 	}
 	public String getDelineator() {
 		return delineator;
 	}
 
 	public void setDelineator(String delineator) {
 		this.delineator = delineator;
 	}
 
 	public ArrayList<String> convertFiletoStrings(){		
 		ArrayList<String> lines = new ArrayList<String>();
 		
 			try{
				 File file = new File("src\\test\\resources\\batchFile.txt");
				 System.out.println(file.getCanonicalPath());
				
				
 			BufferedReader reader = new BufferedReader(new FileReader(fileLocation));
			String line;
			while ((line = reader.readLine())!=null){
 				lines.add(line);
 			}
 			reader.close();
 			} catch(FileNotFoundException fNFE){
 				System.out.print(fNFE.getMessage());
 			} catch (IOException iOE){
 				System.out.print(iOE.getMessage());
 			} 
 		
 		return lines;
 	}
 	
 	//Should validate all strings and look at addSlashes
 	public void processLines(ArrayList<String> bookParts){
 		
 		for ( String x : bookParts ){
 		
 		String[] parts = x.split(delineator);
 		
 		if(parts[0]=="A"){
 			//TODO the add book code
 		}
 		else{
 			if (parts[3]=="*U"){
 				//TODO add the make unavailable code
 			} else if (parts[3]=="*A"){
 				//TODO add the make available code
 			} else{
 			//TODO the update book code
 			}
 		}
 		
 		}
 		
 		
 		
 	}
 
 	@Override
 	public String toString() {
 		return "BookBatchProcessor [fileLocation=" + fileLocation
 				+ ", delineator=" + delineator + "]";
 	}
 
 	
 
 }
