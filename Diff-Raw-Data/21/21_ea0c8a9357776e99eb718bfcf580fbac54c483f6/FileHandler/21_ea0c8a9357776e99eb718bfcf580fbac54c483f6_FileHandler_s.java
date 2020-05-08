 package lib;
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 
 public class FileHandler {
 
 	public static boolean searchFile(FileInputStream fis, String searchStr) throws IOException{
 		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
 		String str = null;
 		while((str = br.readLine()) != null) {
 			if(str.startsWith(searchStr)) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public static void writeFile(FileOutputStream fos, String writeString) throws IOException {
		DataOutputStream dos = new DataOutputStream(fos);
		dos.writeChars(writeString);
 	}
 }
