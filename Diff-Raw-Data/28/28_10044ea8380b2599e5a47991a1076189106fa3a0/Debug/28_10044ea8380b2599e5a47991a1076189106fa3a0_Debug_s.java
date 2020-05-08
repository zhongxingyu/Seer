 /*
  * Copyright (c) 2009 Timothy Bourke
  * All rights reserved.
  *
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the "BSD License" which is distributed with the
  * software in the file ../../LICENSE.
  *
  * This program is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the BSD
  * License for more details.
  */
 
 package mnemogogo.mobile.hexcsv;
 
 import java.lang.*;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.Enumeration;
 import javax.microedition.io.Connector;
 import javax.microedition.io.file.FileSystemRegistry;
 
 public class Debug
 {
     public static boolean open = false;
     public static PrintStream logFile = null;
 
     public static void open() {
 	StringBuffer path = new StringBuffer("file://");
 	Enumeration roots = FileSystemRegistry.listRoots();
 
 	while (roots.hasMoreElements()) {
 	    try {
 		path.delete(7, path.length());
 		path.append("/");
 		path.append((String)roots.nextElement());
 		path.append("mnemogogo.log");
 
		logFile = new PrintStream(
		    Connector.openOutputStream(path.toString()));
 
 		if (logFile != null) {
 		    break;
 		}
 	    } catch (SecurityException e) {
	    } catch (IOException e) {}
 	}
 
 	open = true;
     }
 
     public static void log(String msg) {
 	if (!open) open();
 
 	if (logFile != null) {
 	    logFile.print(msg);
 	    logFile.flush();
 	}
     }
 
     public static void log(int msg) {
 	if (!open) open();
 
 	if (logFile != null) {
 	    logFile.print(msg);
 	    logFile.flush();
 	}
     }
 
     public static void log(long msg) {
 	if (!open) open();
 
 	if (logFile != null) {
 	    logFile.print(msg);
 	    logFile.flush();
 	}
     }
 
     public static void stopLog() {
 	if (logFile != null) {
 	    logFile.close();
 	    logFile = null;
 	    open = false;
 	}
     }
 }
 
