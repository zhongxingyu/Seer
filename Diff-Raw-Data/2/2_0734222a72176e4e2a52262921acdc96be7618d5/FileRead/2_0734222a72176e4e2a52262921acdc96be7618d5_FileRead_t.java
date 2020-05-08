 package server.file;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 
 import server.JServer;
 
 public class FileRead {
 	public static void Read() {
 		if (!JServer.debug) {
 			ReadFile("history");
 		}
 	}
 	
 	public static void ReadFile(String f) {
 		String file = JServer.rootDir + f;
 		String cLine;
 		
 		try (BufferedReader bReader = new BufferedReader(new FileReader(file))) {
 			while ((cLine = bReader.readLine()) != null) {
 				if (cLine.trim().indexOf('#') == 0)	continue;
 			}
 		} catch(IOException e) { }
 	}
 	
 	public static boolean ReadConfig() {
 		String file = JServer.rootDir + "config";
 		String cLine;
 		String[][] str = new String[2][2];
 		
 		try (BufferedReader bReader = new BufferedReader(new FileReader(file))) {
 			int i = 0;
 			while ((cLine = bReader.readLine()) != null) {
 				if (cLine.trim().indexOf('#') == 0) continue;
 				str[i] = cLine.split(": ");
 				i++;
 			}
 			
			if (str[0][0].equals("Port")) JServer.serverPort = Integer.parseInt(str[0][1]);
 			
 			return true;
 		} catch (IOException e) {
 			return false;
 		}
 	}
 }
