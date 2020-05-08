 package com.id.misc.assorted;
 
 import java.io.File;
 
 /**
  * Be careful when converting File to URL.
  * See Sun's bugs :
  * <br> 
  * 4273532,
  * <br>
  * 6179468 - File.toURL() should be deprecated
  * @author idanilov
  * 
  */
 public class ConvertFile2URLDemo {
 
 	public static void main(String[] args) throws Exception {
 		File file = new File("C:\\Documents and Settings");
 		// works improperly.
 		System.out.println("file.toURL() ----> " + file.toURL());
		// works normally. escaping all special characters.
 		System.out.println("file.toURI().toURL() ----> " + file.toURI().toURL());
 	}
 }
