 /**
  * 
  */
 package io;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import model.Corpus;
 
 /**
  * @author muelleml
  * 
  */
 public class ConllWriter {
 
 	public static void write(Corpus c, String fileName) {
 
 		try {
 		String path ="";
 		String[] parts = fileName.split("/");
 		for(int i=0; i<parts.length-1; i++) {
 			String dir = parts[i];
 			path += dir + "/";
 			File temp = new File(path);
			if(!temp.exists()) temp.mkdir();
 		}
 		
 		File f = new File(fileName);
 
 			if(f.exists() || f.createNewFile()) { 
 				FileOutputStream fos = new FileOutputStream(f);
 	
 				fos.write(c.toString().trim().getBytes());
 	
 				fos.close();
 			}
 		}
 		catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 }
