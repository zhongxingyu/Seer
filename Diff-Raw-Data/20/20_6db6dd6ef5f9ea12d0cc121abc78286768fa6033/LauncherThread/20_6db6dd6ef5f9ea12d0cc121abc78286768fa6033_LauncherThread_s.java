 // The Grinder
 // Copyright (C) 2000  Paco Gomez
 // Copyright (C) 2000  Philip Aston
 
 // This program is free software; you can redistribute it and/or
 // modify it under the terms of the GNU General Public License
 // as published by the Free Software Foundation; either version 2
 // of the License, or (at your option) any later version.
 
 // This program is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 // GNU General Public License for more details.
 
 // You should have received a copy of the GNU General Public License
 // along with this program; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 
 package net.grinder.engine;
 
 import java.io.*;
 
 import net.grinder.util.FilenameFactory;
 
 /**
  * This class knows how to start a Java Virtual Machine with parameters.
  * The virtual machine will execute the class GrinderProcess.
  * It redirects the standard ouput and error to disk files.
  *
  * @see net.grinder.Grinder
  * @see net.grinder.engine.Redirector
 * @see net.grinder.engine.GrinderProcess
  *
  * @author Paco Gomez
  * @author Philip Aston
  * @version $Revision$
  */
 public class LauncherThread implements java.lang.Runnable {
 
     private final String m_processID;
     private final String m_commandLine;
     private final FilenameFactory m_filenameFactory;
     private final boolean m_appendLog;
     
     /**
      * The constructor.
      * It starts a new thread that will execute the run method.
      */    
     public LauncherThread(String processID,
 			  String commandLine,
 			  boolean appendLog)
     {
 	m_processID = processID;
 	m_commandLine = commandLine;
 	m_filenameFactory = new FilenameFactory(processID, null);
 	m_appendLog = appendLog;
 
 	Thread t = new Thread(this, m_commandLine);
 	t.start(); 
     }
   
     /**
      * This method will start a process with the JVM.
      * It redirects standard output and error to disk files.
      */    
     public void run(){
 
 	try{
 	    logMessage("started with command line: " + m_commandLine);
 	    
 	    final Process process = Runtime.getRuntime().exec(m_commandLine);
       
 	    final BufferedReader outputReader =
 		new BufferedReader(
 		    new InputStreamReader(process.getInputStream()));
 
 	    final BufferedReader errorReader =
 		new BufferedReader(
 		    new InputStreamReader(process.getErrorStream()));
       
 	    final PrintWriter outputFile =
 		new PrintWriter(
 		    new BufferedOutputStream(
 			new FileOutputStream(
 			    m_filenameFactory.createFilename("out"),
 			    m_appendLog)),
 		    true);  
 
 	    final PrintWriter errorFile =
 		new PrintWriter(
 		    new BufferedOutputStream(
 			new FileOutputStream(
 			    m_filenameFactory.createFilename("error"),
 			    m_appendLog)),
 		    true);
       
 	    final Redirector r1 = new Redirector(outputFile, outputReader);
 	    final Redirector r2 = new Redirector(errorFile, errorReader);
 
 	    process.waitFor();
 	    int completionStatus = process.exitValue(); 
 
 	    logMessage("exited with status " + completionStatus); 
                
 	}
 	catch(Exception e){
 	    e.printStackTrace();
 	}
     }
 
     private void logMessage(String message)
     {
 	System.out.println("Process " + m_processID + " " + message);
     }
 }
