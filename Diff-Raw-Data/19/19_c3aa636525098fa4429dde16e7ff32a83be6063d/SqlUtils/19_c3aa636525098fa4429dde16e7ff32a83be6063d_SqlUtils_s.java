 package ch.ethz.inf.utils;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 public class SqlUtils {
 	public static String[] parseSqlScript(String path) throws Exception {
 		return parseSqlScript(new FileInputStream(path));
 	}
 	
 	public static String[] parseSqlScript(InputStream is) throws Exception {
 		// we use the string buffer to store the content of the given SQL file
 		StringBuffer sb = new StringBuffer();
 		
 		// now iterate over the content of the given file and store all the info in the StringBuffer
 		try(BufferedReader in = new BufferedReader(new InputStreamReader(is));) {
 			String str;
 			
 			while((str = in.readLine()) != null) {
 				// we skip the empty lines and '\n'
				if(StringUtils.isNotNullOrEmpty(str)) {
 					sb.append(str);
 					sb.append("\n");
 				}
 			}
 		}
 		
 		// finally we parse the content by ';\n', so that we have a entry in the array for every single SQL command
 		return sb.toString().split(";\n");
 	}
 }
