 package model;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import view.MainWindow;
 
 public class FromFileReader {
 	
 	public FromFileReader(File file, MainWindow mainWindow) {
 		FileInputStream stream;
 		try {
 			mainWindow.clearText();
 			mainWindow.resetAllStacks();
 			
 			stream = new FileInputStream(file);
 			DataInputStream fstream = new DataInputStream(stream);
 			BufferedReader reader = new BufferedReader(new InputStreamReader(
 					fstream));
 			String stringLine;
 			while ((stringLine = reader.readLine()) != null) {
 				countTabs(stringLine);
 				mainWindow.processText(stringLine, countTabs(stringLine));
 			}
 			mainWindow.closeAllBrackets();
 			fstream.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 
 		}
 	}
 	int countTabs(String text) {
 		int number = 0;
 		for(int i = 0 ; i<text.length(); ++i) {
 			char c = text.charAt(i);
 			if(c == '\t') number++; 
 		}
		System.out.println(number);
 		return number;
 	}
 }
