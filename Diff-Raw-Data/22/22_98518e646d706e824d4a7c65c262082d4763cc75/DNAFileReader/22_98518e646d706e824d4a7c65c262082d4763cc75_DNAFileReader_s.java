 package bioUtils;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
 
 public class DNAFileReader {
 
 	public static String readFASTA(String pathname) throws Exception {
 		File f = new File(pathname);
 		FileReader in = new FileReader(f);
 		BufferedReader reader = new BufferedReader(in);
 				
 		//Skip the first line
 		reader.readLine();
 		
 		StringBuilder builder = new StringBuilder();
 		
 		String line = reader.readLine();
 		while(line != null) {
 			builder.append(line.trim());
 			
 			line = reader.readLine();
 		}
 		
 		reader.close();
 		
 		return builder.toString();
 	}
 
 }
