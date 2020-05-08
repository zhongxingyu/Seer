 package backend;
 
import gui.Gui;

 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.StringTokenizer;
 
 import main.Main;
 
 public class FileReadWrite {
 	Main window = null;
 
 	DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss");
 	Date date = new Date();
 
 	//String outputFileNameExperOne 	= "C:\\andres\\Code\\eclipseWorkspace\\SimpleGUI\\guiMain\\data\\experOneLog" + dateFormat.format(date) + ".txt";
 	String outputFileNameExperTwo 	= "C:\\andres\\Code\\eclipseWorkspace\\SimpleGUI\\guiMain\\data\\experTwoLog" + dateFormat.format(date) + ".txt";
 	//String outputFileNameExperThree = "C:\\andres\\Code\\eclipseWorkspace\\SimpleGUI\\guiMain\\data\\experThreeLog" + dateFormat.format(date) + ".txt";
 	File outputFile = new File(outputFileNameExperTwo); 
 	Writer output = null; 
 	BufferedReader input = null;
 
 	boolean firstWrite = true;
 
 	public FileReadWrite(Main main){
 		this.window = main;
 		initialization();
 	}
 
 	public void initialization()
 	{
 		/*if (window.isExpOneSelected()) {
 			outputFile = new File(outputFileNameExperOne);
 		} else if (window.isExpTwoSelected()) {
 			outputFile = new File(outputFileNameExperTwo);
 		} else if (window.isExpThreeSelected()) {
 			outputFile = new File(outputFileNameExperThree);
 		}*/
 		
 		try {
 			output = new BufferedWriter(new FileWriter(outputFile, true));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		System.out.println("Logging data to: " + outputFile.getAbsolutePath());
 		/*
 		// This is done here so the header files are done once 
 		//without checking if there was a previous version of this file.
 		try {
 			output = new BufferedWriter(new FileWriter(outputFile));
 			output.write("Network Data\t" +dateFormat.format(date) + "\n");
 			output.append("botID\tParent\tTime Slot\t\n");			
 
 			output.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}	*/
 	}
 
 	public void WriteHeader(){
 		System.out.println("Logging data to: " + outputFile.getAbsolutePath());
 
 		// This is done here so the header files are done once 
 		//without checking if there was a previous version of this file.
 		try {
 			output = new BufferedWriter(new FileWriter(outputFile, true));
 			output.write("BER Experimental Data\t" +dateFormat.format(date) + "\n");
 			output.append("Received data\t\n");			
 
 			output.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	// Writes the contents of packageData to the desired outputFile
 	public void WriteData(Package packageData){
 
 		try {
 
 			if (firstWrite == true) {
 				output = new BufferedWriter(new FileWriter(outputFile, true));
 				output.write("BER Experimental Data\t" + dateFormat.format(date) + "\n");
 				output.append("Received data\t\n");
 				output.close();
 				firstWrite = false;
 			}
 
 			output.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	// Writes the contents of packageData to the desired outputFile
 	public void WriteData(byte[] data){
 
 		String str;
 		try {
 			//output = new BufferedWriter(new FileWriter(outputFile));
 			output = new BufferedWriter(new FileWriter(outputFile, true));
 			for (int i = 0; i < window.serialPortManager.numBytesSent; i++) {
 				str = new String(new byte[] { (byte) data[i] });
 				output.append(str);
 			}
 			output.append('\n');
 			//output.flush();
 			output.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	// Writes the contents of packageData to the desired outputFile
 	public void WriteData(String text){
 
 		try {
 			//output = new BufferedWriter(new FileWriter(outputFile));
 			//output = new BufferedWriter(new FileWriter(outputFile, true));
 			output.append(text);
 			//output.append('\n');
 			output.flush();
 			//output.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	// Example of reading data line by line from a given input file 
 	public void ReadData(String fileToRead){
 
 		String line;
 
 		try {
 			BufferedReader input = new BufferedReader(new FileReader(new File(fileToRead)));
 
 			while ((line = input.readLine()) != null) {
 				StringTokenizer token = new StringTokenizer(line, "\t");
 				String value1 = token.nextToken();
 				System.out.println(value1);
 			}
 
 			input.close();
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 }
