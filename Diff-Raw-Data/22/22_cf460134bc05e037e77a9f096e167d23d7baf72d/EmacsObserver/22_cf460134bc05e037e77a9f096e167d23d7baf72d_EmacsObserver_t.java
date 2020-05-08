 /*
     EmacsObserver.java
 
     @author: <a href="mailto:ovidiu@cup.hp.com">Ovidiu Predescu</a>
     Date: March 29, 2001
 
     Copyright (C) 2001 Ovidiu Predescu
 
     This program is free software; you can redistribute it and/or
     modify it under the terms of the GNU General Public License as
     published by the Free Software Foundation; either version 2 of the
     License, or (at your option) any later version.
    
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
     General Public License for more details.
    
     You should have received a copy of the GNU General Public License
     along with this program; if not, write to the Free Software
     Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
     02111-1307, USA.
  */
 
 package xslt.debugger.cmdline;
 
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.lang.StringBuffer;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.Stack;
import javax.xml.transform.TransformerException;
 import xslt.debugger.AbstractXSLTDebugger;
 import xslt.debugger.Manager;
 import xslt.debugger.Observer;
 import xslt.debugger.SourceFrame;
 import xslt.debugger.StyleFrame;
 import xslt.debugger.Type;
import xslt.debugger.Utils;
 import xslt.debugger.Value;
 import xslt.debugger.Variable;
import java.util.HashSet;
 
 public class EmacsObserver implements Observer
 {
  HashSet reportedExceptions = new HashSet();
   Controller controller;
   StringBuffer buffer = new StringBuffer(2000);
   
   public EmacsObserver(Controller controller)
   {
     this.controller = controller;
 
     // Create a server socket, send the port number back to Emacs, and
     // wait for Emacs to connect to it. Upon connection, setup an
     // output stream on the client socket and use it to serialize the
     // XSLT processing results to Emacs.
     try {
       InetAddress localhost = InetAddress.getByName("localhost");
       ServerSocket serverSocket = new ServerSocket(0, 5, localhost);
       int port = serverSocket.getLocalPort();
       System.out.println("^(xslt-process-set-output-port " + port + ")$");
       System.out.flush();
 
       AbstractXSLTDebugger debugger = controller.getDebugger();
       Manager manager = controller.getManager();
       Socket clientSocket;
 
       // Wait for an incoming connection. The first client socket
       // created is used for the XSLT generated output.
       clientSocket = serverSocket.accept();
       OutputStream outputStream = clientSocket.getOutputStream();
       manager.setOutStream(outputStream);
 
       // Wait for another incoming connection. This second client
       // socket connection is for outputing xsl:message.
       clientSocket = serverSocket.accept();
       OutputStream messageStream = clientSocket.getOutputStream();
       manager.setMessageStream(messageStream);
 
       // Close the server socket
       serverSocket.close();
     }
     catch (Exception e) {
       caughtException(e);
     }
   }
 
   public void debuggerProcessStarted()
   {
     System.out.println("^(xslt-process-debugger-process-started)$");
   }
 
   /**
    * Invoked by the debugger to print a descriptive message showing
    * where the debugger stopped.
    *
    * @param filename a <code>String</code> value
    * @param line an <code>int</code> value
    * @param column an <code>int</code> value
    * @param message a <code>String</code> value, additional
    * informative message
    */
   public void debuggerStopped(String filename,
                               int line,
                               int column,
                               String message)
   {
     if (filename.startsWith("file:"))
       filename = filename.substring(5);
     System.out.println("^(xslt-process-debugger-stopped-at "
                        + "\"" + filename
                        + "\" " + line
                        + " " + column
                        + " \"" + message + "\")$");
   }
 
   public void breakpointSetAt(String filename, int line) {}
   
   public void breakpointDeletedAt(String filename, int line) {}
   
   public void breakpointEnabledAt(String filename, int line) {}
   
   public void breakpointDisabledAt(String filename, int line) {}
 
   public void sourceStackChanged()
   {
     Manager manager = controller.getManager();
     Stack sourceFrames = manager.getSourceFrames();
 
     buffer.delete(0, buffer.length());
     buffer.append("^(xslt-process-source-frames-stack-changed [");
     for (int i = 0; i < sourceFrames.size(); i++) {
       SourceFrame frame = (SourceFrame)sourceFrames.get(i);
       String filename = frame.getFilename();
 
       if (filename.startsWith("file:"))
         filename = filename.substring(5);
 
       buffer.append("[\"" + frame.getName()
                     + "\" \"" + filename
                     + "\" " + frame.getLine()
                     + " " + (frame.isExiting() ? "t" : "nil")
                     + " " + i
                     + "]");
     }
     buffer.append("])$");
     System.out.println(buffer);
     System.out.flush();
   }
 
   public void styleStackChanged()
   {
     Manager manager = controller.getManager();
     Stack styleFrames = manager.getStyleFrames();
 
     buffer.delete(0, buffer.length());
     buffer.append("^(xslt-process-style-frames-stack-changed [");
     for (int i = 0; i < styleFrames.size(); i++) {
       StyleFrame frame = (StyleFrame)styleFrames.get(i);
       String filename = frame.getFilename();
 
       if (filename.startsWith("file:"))
         filename = filename.substring(5);
 
       buffer.append("[\"" + frame.getName()
                     + "\" \"" + filename
                     + "\" " + frame.getLine()
                     + " " + (frame.isExiting() ? "t" : "nil")
                     + " " + i
                     + "]");
     }
     buffer.append("])$");
     System.out.println(buffer);
     System.out.flush();
   }
 
   public void stackFramesChanged(int sourceFrameNo, int styleFrameNo)
   {
     System.out.println("stackFramesChanged " + sourceFrameNo + ", " + styleFrameNo);
     
     buffer.delete(0, buffer.length());
     buffer.append("^(xslt-process-stack-frames-changed ");
     buffer.append(sourceFrameNo);
     buffer.append(" ");
     buffer.append(styleFrameNo);
     buffer.append(")$");
     System.out.println(buffer);
     System.out.flush();
   }
   
   public void localVariablesChanged(ArrayList localVariables)
   {
     buffer.delete(0, buffer.length());
     buffer.append("^(xslt-process-local-variables-changed [");
     for (int i = 0; i < localVariables.size(); i++) {
       Variable variable = (Variable)localVariables.get(i);
       String name = variable.getName();
       Value value = variable.getValue();
 
       buffer.append(" [\"" + name + "\"");
       if (value != null) {
         Type type = value.getType();
         String typeName = null;
         if (type != null)
           typeName = type.getTypeName();
 
         // Append the type
         buffer.append(" \"" + (typeName != null ? typeName : "") + "\"");
 
         // Now append the value
         String stringValue = Utils.escape(value.getValue());
         buffer.append(" \"" + (stringValue != null ? stringValue : "") + "\"");
       }
       else {
         // We have no value and no type
         buffer.append(" \"\" \"\"");
       }
 
       buffer.append("]");
     }
 
     buffer.append("])$");
     System.out.println(buffer);
     System.out.flush();
   }
 
  public void processorStarted()
  {
    reportedExceptions.clear();
  }

   public void processorFinished()
   {
     System.out.println("^(xslt-process-processor-finished)$");
   }
 
   public void caughtException(Exception e)
   {
    // Report an exception; if the exception was already reported,
    // ignore it.
    if (reportedExceptions.contains(e.getMessage()))
      return;
    
    reportedExceptions.add(e.getMessage());
     StringWriter strWriter = new StringWriter();
     PrintWriter pWriter = new PrintWriter(strWriter);
     e.printStackTrace(pWriter);
     pWriter.flush();
     if (e instanceof TransformerException)
       System.out.println("^(xslt-process-report-error \""
                          + Utils.escape(e.getMessage())
                          + "\" \""
                          + Utils.escape(strWriter.getBuffer().toString())
                          + "\")$");
     else
       System.out.println("^(xslt-process-report-error \""
                          + Utils.escape(e.getMessage())
                          + "\" \""
                          + Utils.escape(strWriter.getBuffer().toString())
                          + "\")$");
   }
 }
