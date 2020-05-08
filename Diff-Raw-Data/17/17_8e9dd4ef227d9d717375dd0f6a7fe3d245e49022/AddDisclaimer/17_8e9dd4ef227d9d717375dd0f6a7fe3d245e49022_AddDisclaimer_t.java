 /* *********************************************************************
  *
  * This demo was developed by John Tubert.
  * This was a port from a flash game I developed a few years back.
  *
  * ********************************************************************/
 
 package com.rga;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.util.Date;
 import java.io.*;
 import java.util.*;
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Task;
 import org.apache.tools.ant.Project;
import com.rga.FileListing;
 
 /**
  * An ANT task class to add disclaimer to all files. 
  * Currently it supports HTML, JS, and CSS
  * @author  John Tubert
  * @version 1.0
  * @see     org.apache.tools.ant.Task
  */
 public class AddDisclaimer extends Task{
 	
 	private String path=null;
 
 	public void setPath(String path) {
 	  this.path = path;
 	}
 
 	public String getPath() {
 	  return path;
 	}
 	
 	// The method executing the task
 	public void execute()
 	{
 		String HTML_disclaimerText = "<!--\n";
 		HTML_disclaimerText+="    This demo was developed by John Tubert.\n";
 		HTML_disclaimerText+="    This was a port from a flash game I developed a few years back.\n"; 
 		HTML_disclaimerText+="-->\n\n";
 		
 		//Stars (CSS, C#, Java, etc)
 		String CSS_disclaimerText = "/* *********************************************************************\n";
 		CSS_disclaimerText+=" *\n";
 		CSS_disclaimerText+=" * This demo was developed by John Tubert.\n";
 		CSS_disclaimerText+=" * This was a port from a flash game I developed a few years back.\n"; 
 		CSS_disclaimerText+=" *\n";
 		CSS_disclaimerText+=" * ********************************************************************/\n\n";
 		
 		//Slashes (JS, C#, Java, etc)
 		String JS_disclaimerText = "////////////////////////////////////////////////////////////////////////\n";
 		JS_disclaimerText+="//\n";
 		JS_disclaimerText+="// This demo was developed by John Tubert.\n";
 		JS_disclaimerText+="// This was a port from a flash game I developed a few years back.\n"; 
 		JS_disclaimerText+="//\n";
 		JS_disclaimerText+="////////////////////////////////////////////////////////////////////////\n\n";
 		
 		//Hashes/Pounds (Python)
 		String Python_disclaimerText = "# ######################################################################\n";
 		Python_disclaimerText+="#\n";
 		Python_disclaimerText+="# This demo was developed by John Tubert.\n";
 		Python_disclaimerText+="# This was a port from a flash game I developed a few years back. \n";
 		Python_disclaimerText+="#\n";
 		Python_disclaimerText+="# ######################################################################\n\n";
 		
 		
 		try {
 			List<File> listOfFiles = FileListing.getFileListing(new File(getPath()));
 
 	    	for(File file : listOfFiles ){
 				String ext=file.getAbsolutePath();
 				int dotIndex=ext.lastIndexOf('.');
 				if(dotIndex>=0) { // to prevent exception if there is no dot
 				  ext=ext.substring(dotIndex+1,ext.length());
 				}//if
 			
 			
 				if (ext.equals("js") || ext.equals("html") || ext.equals("css")){
 					String disclaimerText = "";
 					//System.out.println(file);
 					if(ext.equals("js")){
 						disclaimerText = JS_disclaimerText;
 					}else if(ext.equals("html") || ext.equals("htm")){
 						disclaimerText = HTML_disclaimerText;
 					}else if(ext.equals("css")){
 						disclaimerText = CSS_disclaimerText;
 					}					
 				
 					String outputTxt = "";
 					String fileContent = fileToString(file.getAbsolutePath());
 				
 					if(fileContent.indexOf(disclaimerText) == -1){
 						System.out.println(file+" - needs disclaimer");
 						
 						if(ext.equals("html")){
 							String temp = fileContent.replace("<!DOCTYPE html>","");						
 							outputTxt = "<!DOCTYPE html>\n"+disclaimerText+temp;
 						}else{
 							outputTxt = disclaimerText+fileContent;
 						}
 						
 						
 					}else{
 						System.out.println(file+" - already has disclaimer. So will not add"+fileContent.indexOf(disclaimerText));
 						continue;
 					}//else
 				
 				
 					try {
 					    //FileWriter fw = new FileWriter(file.getAbsolutePath());
 					    //BufferedWriter bw = new BufferedWriter(fw);						
 						BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file.getAbsolutePath()),"UTF8"));
 						
 						bw.write(outputTxt);
 						bw.close();
 						System.out.println("Done");
 					}//try
 					catch(Exception e) {
 					      System.out.println("Exception: " + e);
 					}//catch
 				}
 		
 			}
 		}
 		catch(Exception e){
 	        System.out.println(e.toString());
 	    }//catch
 	}//execute
 	
 	/**
       * @param string  Name of the file
       * @return string  content of the file      
       */
 	public static String fileToString (String fileName) throws IOException{
 		try {
 			FileInputStream input = new FileInputStream(fileName);
 
 			byte[] fileData = new byte[input.available()];
 
 			input.read(fileData);
 			input.close();
 
 			return new String(fileData, "UTF-8");
 		}//try
 		catch(Exception e){
 	        System.out.println(e.toString());
 	    }//catch
 	
 		return "";
 		
 	}//fileToString	
 } //class
