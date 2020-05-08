 package parser;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 
 import dataClasses.Constants;
 import dataClasses.PatientCase;
 
 public class CaseReader {
 	
 	private static String result;
 	private ArrayList<PatientCase> patients;
 	private String output = "patientCases/stopwords";
 	
 	public CaseReader(){
 		patients = new ArrayList<PatientCase>();
 		addCases();
 	}
 	
 	
 	public ArrayList<PatientCase> getPatients(){
 		return patients;
 	}
 	
 	
 	private void addCases(){
 		
 		for(int i = 1; i < 9; i++){
			File input = new File(Constants.STEMMEDPATIENTCASES+"/case"+i+"stemmed"+".txt");
 			
 			try{
 				readFile(input);
 				
 			} catch (IOException e){
 				e.printStackTrace();
 			}
 			PatientCase pc = new PatientCase(1, getResult());
 			patients.add(pc);
 		}
 	}
 	
 	
 	private String removeStopWords(String file){
 		
 		try{
 			FileInputStream fileStream = new FileInputStream("resources/norwegianStopWords.txt");
 			DataInputStream in = new DataInputStream(fileStream);
 			BufferedReader stopWords = new BufferedReader(new InputStreamReader(in));
 			
 			String stopWordString;
			while ((stopWordString = stopWordString.readLine()) != null){
 				file = file.replaceAll(" " + stopWordString + " ", " ");
 			}
 			
 		} catch (FileNotFoundException e){
 			System.out.println("Stop words file not found");
 		} catch (IOException e) {
 			System.out.println("Error reading line");
 		}
 		
 		return file;
 	}
 
 	
 	public String readFile(File filename) throws IOException{
 		
 		BufferedReader in = new BufferedReader(new FileReader(filename));
 		String file = "";
 		String temp;
 		while ((temp = in.readLine()) != null) {
 			file += temp;
 			file += " ";
 		}
 		
 		result = file;
 		
 		return file;
 	}
 	
 	
 	public void makeFile(File inputFile, String output) throws FileNotFoundException{
 		
 		String fileContent = "";
 		try {
 			fileContent = readFile(inputFile);
 			
 		} catch (IOException e){
 			e.printStackTrace();
 		}
 		PrintWriter out = new PrintWriter(new FileOutputStream(new File(output)));
 		out.println(fileContent);
 		out.println();
 		out.flush();
 		out.close();
 	}
 	
 	
 	public String getResult(){
 		return result;
 	}
 }
