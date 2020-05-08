 // The Grinder
 // Copyright (C) 2001  Paco Gomez
 // Copyright (C) 2001  Philip Aston
 // Copyright (C) 2001  David Freels
 
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
 
 package net.grinder.plugin.socket;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.Socket;
 import java.util.Date;
 import java.util.Set;
 
 import net.grinder.common.GrinderException;
 import net.grinder.common.GrinderProperties;
 import net.grinder.common.Test;
 import net.grinder.plugininterface.GrinderPlugin;
 import net.grinder.plugininterface.PluginException;
 import net.grinder.plugininterface.PluginProcessContext;
 import net.grinder.plugininterface.PluginThreadContext;
 import net.grinder.plugininterface.ThreadCallbacks;
 
 
 /**
  * Grinder Plugin that allows testing of socket based programs
  *
  * @author  David Freels
  * @version $Revision$
  */
 public class SocketPlugin implements GrinderPlugin
 {
     private Set m_testsFromPropertiesFile;
     private String m_host = "localhost";
     private int m_port = 7080;
   
     public void initialize(PluginProcessContext processContext,
 			   Set testsFromPropertiesFile)
 	throws PluginException
     {
 	m_testsFromPropertiesFile = testsFromPropertiesFile;
 
 	final GrinderProperties parameters =
 	    processContext.getPluginParameters();
     
 	try {
 	    m_host = parameters.getMandatoryProperty("host");
 	    m_port = parameters.getMandatoryInt("port");
 	}
 	catch(GrinderException ge) {
 	    throw new PluginException("Missing property", ge);
 	}
     }
 
     public Set getTests()
     {
 	return m_testsFromPropertiesFile;
     }
 
     public ThreadCallbacks createThreadCallbackHandler()
 	throws PluginException
     {
 	return new SocketPluginThreadCallbacks();
     }
 
     private class SocketPluginThreadCallbacks implements ThreadCallbacks
     {
 	private PluginThreadContext m_pluginThreadContext = null;
 	private Socket m_socket = null;
 	private BufferedReader m_reader = null;
 	private BufferedWriter m_writer = null;
 
 	public void initialize(PluginThreadContext threadContext) 
 	{
 	    m_pluginThreadContext = threadContext;
 	}
 
 	/**
 	 * This is called for each method name in grinder.plugin.methods.
 	 */
 	public boolean doTest(Test testDefinition) throws PluginException
 	{ 
 	    //Get the test parameters
 	    final GrinderProperties parameters =
 		testDefinition.getParameters();
 
 	    //Get the test number
 	    final int testName = testDefinition.getNumber();
     
 	    /**
 	     * Multiple request/response operations could happen
 	     * during a single test, so we need to cycle through
 	     * each operation.
 	     */
     
 	    //Message count
 	    int i = 0;
 
 	    //Look for the first request
 	    String requestFile = parameters.getProperty("request" + i,
 							null);
     
 	    //If no request is found, abort
 	    if (requestFile == null) {
 		throw new PluginException(
 		    "No request parameters have been set!");
 	    }
     
 	    while (requestFile != null) {
 		//Call method to load the next request and save the response
 		sendRequest(requestFile,
 			    parameters.getProperty("response"+ i,
 						   "test" + testName +
 						   "response" + i + ".txt"));
 		i++;
 
 		//Get the next request file
		requestFile = parameters.getProperty("request"+i, null);
 	    }
 
 	    return true;
 	}
   
 	/**
 	 * This method sends a request and records it the response the the file responseFile
 	 */
 	private void sendRequest(String requestFile, String responseFile)
 	    throws PluginException
 	{
 	    try {
 		//Read the contents of the file and send them to the server
 		BufferedReader fis =
 		    new BufferedReader(
 			new InputStreamReader(
 			    new FileInputStream(requestFile)));
 
 		StringBuffer message = new StringBuffer("");
 		String line = "";
       
 		while( (line = fis.readLine()) != null) {
 		    message.append(line+"\n");
 		}
 
 		m_writer.write(message.toString());
 		m_writer.flush();
 		fis.close();
 
 		//Read the response from the server and write it to a file      
 		message.delete(0, message.length());
 		line = "";
 
 		while((line = m_reader.readLine()) != null) {
 		    message.append(line+System.getProperty("line.separator"));
 		}
 
 		java.io.RandomAccessFile fos =
 		    new java.io.RandomAccessFile(responseFile, "rw");
 
 		fos.seek(fos.length());
 		fos.writeBytes(message.toString() + 
 			       System.getProperty("line.separator"));
 		fos.close();
 	    }
 	    catch(java.io.IOException ioe) {
 		throw new PluginException(
 		    "Error communicating with server", ioe);
 	    }
 	}
   
 	/**
 	 * This method is executed at the beginning of evey cycle.
 	 */
 	public void beginCycle() throws PluginException
 	{
 	    //open the socket connection
 	    try {
 		Date d = new Date();
 		m_socket = new Socket(m_host, m_port);
 		m_pluginThreadContext.logMessage(
 		    "Time to connect to " + m_host + " took " + 
 		    (new Date().getTime() - d.getTime())+" ms");
 		
 		m_reader =
 		    new BufferedReader(
 			new InputStreamReader(m_socket.getInputStream()));
 
 		m_writer =
 		    new BufferedWriter(
 			new OutputStreamWriter(m_socket.getOutputStream()));
 
 	    }
 	    catch(java.net.UnknownHostException uhe) {
 		throw new PluginException("Cannot locate host "+
 					  m_host, uhe);
 	    }
 	    catch(java.io.IOException ioe) {
 		throw new PluginException("Unable to connect to host "+
 					  m_host, ioe);
 	    }
 	}
 
 	/**
 	 * This method is executed at the end of every cycle.
 	 */
 	public void endCycle() throws PluginException
 	{
 	    //close the socket connection
 	    if(m_socket != null) {
 		try {
 		    m_socket.close();
 		    m_reader.close();
 		    m_writer.close();
 		}
 		catch(java.io.IOException ioe) {
 		    throw new PluginException(
 			"Error closing socket connection", ioe);
 		}
 	    }
 	}
     }
 }
