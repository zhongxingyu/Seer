 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Date;
 
 public class Logger {
 
     private static String path = null;
     
     
     public static void log(String key, String value) {
 	
 	File file = new File(path);
 	
 	try {
 	    if (!file.exists())
 		file.createNewFile();
 	    
 	    FileWriter fw = new FileWriter(path, true);
 	    BufferedWriter bw = new BufferedWriter(fw);
 	    bw.write(key+":"+(new Date().getTime()/1000)+" | " + value+"\n");
 	    bw.close();
 	}
 	catch (IOException e) {
 	    System.out.println(e);
 	}
 	
     }
     
     public static void setPath(String filepath) {
 	path = filepath;
     }
     
 }
