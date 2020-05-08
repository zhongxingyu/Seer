 package org.caesarj.compiler;
 
import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
import java.io.OutputStream;
 import java.util.Enumeration;
 import java.util.StringTokenizer;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipException;
 import java.util.zip.ZipFile;
 

 public class CollectClassFiles {
 	
 	public static void collectAll(String classPath, ByteCodeMap classes) {
 	    StringTokenizer	entries;
 
 	    entries = new StringTokenizer(classPath, File.pathSeparator);
 	    while (entries.hasMoreTokens()) {
 	    	String name = entries.nextToken();
 
 	    	File file = new File(name);
 	    	if (file.isDirectory()) {
     			traverseDir(file, classes);
     		}
     		else if (file.isFile() && 
     				(file.getName().endsWith(".zip") || file.getName().endsWith(".jar"))) {
     			traverseZip(file, classes); 
     		}
 	    	
 	    }
 	}
 
 	public static void traverseZip(File file, ByteCodeMap classes) {
 		try {
             ZipFile zip = new ZipFile(file);
             
             for (Enumeration e = zip.entries(); e.hasMoreElements(); ) {
 		        ZipEntry entry = (ZipEntry)e.nextElement();
 
 		        if (entry.getName().endsWith(".class")) {
		        	InputStream input = zip.getInputStream(entry);
		        	ByteArrayOutputStream output = new ByteArrayOutputStream();
		        	copy(input, output);		        	
		        	classes.addClassFile(entry.getName(), output.toByteArray());		        	
 		        }
 		    }
         } 
 		catch (ZipException e) {
             // it was not a zip file, ignore it
         }
 		catch (IOException e) {
     		// ignore	    		
     	}
 	}

	private static void copy(InputStream in, OutputStream out) throws IOException {  
		byte[] b = new byte[1024];  
		int read;  
		while ((read = in.read(b)) != -1) {  
			out.write(b, 0, read);  
		}  
	} 

 	public static void traverseDir(File file, ByteCodeMap classes) {
 		// TODO
 	}
 }
