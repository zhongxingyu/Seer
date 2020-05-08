 package org.caesarj.compiler;
 
import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
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
		        	InputStream stream = zip.getInputStream(entry);
		        	String str = stream.toString();
		        	classes.addClassFile(entry.getName(), str.getBytes());		        	
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
	
 	public static void traverseDir(File file, ByteCodeMap classes) {
 		// TODO
 	}
 }
