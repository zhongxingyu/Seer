 package framework.utils;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import com.google.common.io.Files;
 
 /**
  * A set of utility methods for IO manipulation
  * @author elip, sagi
  *
  */
 public class IOUtils {
 	
 	/**
 	 * 
 	 * @param oldFile
 	 * @param newFile
 	 * @throws IOException
 	 */
 	public static void replaceFile(String oldFile, String newFile) throws IOException {
 		File old = new File(oldFile);
 		old.delete();
 		Files.copy(new File(newFile), new File(oldFile));
 	}
 
 	public static void replaceTextInFile(String FileName, String oldText, String newText) throws IOException {
 		HashMap<String, String> toPass = new HashMap<String, String>();
 		toPass.put(oldText, newText);
 		replaceTextInFile(FileName, toPass);
 	}
 	
 	public static void replaceTextInFile(String FileName, Map<String, String> map) throws IOException {
 		File file = new File(FileName);
         BufferedReader reader = new BufferedReader(new FileReader(file));
         String line = "", oldtext = "";
         while((line = reader.readLine()) != null)
             {
             oldtext += line + "\r\n";
         }
         reader.close();
         String newtext = new String(oldtext);
         for (String toReplace : map.keySet()) {
         	newtext = newtext.replaceAll(toReplace, map.get(toReplace));
         }
        
         FileWriter writer = new FileWriter(FileName);
         writer.write(newtext);writer.close();
		
		
 	}
 	
	

 }
