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
 
 package net.grinder.engine.process;
 
 import java.util.StringTokenizer;
 import java.util.Vector;
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.*;
 import java.lang.reflect.Method;
 
 import net.grinder.engine.EngineException;
 import net.grinder.plugininterface.GrinderPlugin;
 import net.grinder.util.FilenameFactory;
 import net.grinder.util.GrinderException;
 import net.grinder.util.GrinderProperties;
 
 /**
  * The class executed by the main thread of each JVM.
  * The total number of JVM is specified in the property "grinder.jvms".
  * This class is responsible for creating as many threads as configured in the
  * property "grinder.threads". Each thread is an object of class "CycleRunnable".
  * It is responsible for storing the statistical information from the threads
  * and also for send it to the console and print it at the end.
  * 
  * @author Paco Gomez
  * @author Philip Aston
  * @version $Revision$
 * @see net.grinder.engine.GrinderThread
 * @see net.grinder.engine.CycleRunnable
  */
 public class GrinderProcess
 {
     /**
      * The application's entry point.
      * 
      */    
     public static void main(String args[]){
 
 	GrinderProcess grinderProcess = null;
 
 	try {
 	    grinderProcess = new GrinderProcess();
 	}
 	catch (GrinderException e) {
 	    System.err.println("Error initialising grinder process: " + e);
 	    e.printStackTrace();
 	    System.exit(1);
 	}
 
 	try {
 	    grinderProcess.run();
 	}
 	catch (GrinderException e) {
 	    System.err.println("Error initialising grinder process: " + e);
 	    e.printStackTrace();
 	    System.exit(2);
 	}
     }
 
     private final String m_hostID;
     private final String m_jvmID;
     private final FilenameFactory m_filenameFactory;
     private final int m_numberOfThreads;
     private final boolean m_waitForConsoleSignal;
     private final boolean m_reportToConsole;
     private InetAddress m_grinderAddress = null;
     private int m_grinderPort = 0;
     private InetAddress m_consoleAddress = null;
     private int m_consolePort = 0;
     private DatagramSocket m_datagramSocket = null;
     private final int m_reportToConsoleInterval;
     private final String m_logDirectory;
     private final boolean m_appendToLog;
     private Class m_pluginClass;
     private final GrinderProperties m_pluginParameters;
     private Method[] m_methods;
     private String[] m_methodNames;
 
     public GrinderProcess() throws GrinderException
     {
 	final GrinderProperties properties = GrinderProperties.getProperties();
 
 	m_hostID = properties.getProperty("grinder.hostID", "UNNAMED HOST");
 	m_jvmID = properties.getMandatoryProperty("grinder.jvmID");
 
 	m_filenameFactory = new FilenameFactory(m_jvmID, null);
 
 	m_numberOfThreads = properties.getInt("grinder.threads", 1);
 	m_waitForConsoleSignal =
 	    properties.getBoolean("grinder.waitForConsoleSignal", false);
 
 	if (m_waitForConsoleSignal) {
 	    try{
 		m_grinderAddress =
 		    InetAddress.getByName(
 			properties.getMandatoryProperty(
 			    "grinder.multicastAddress"));
 
 		m_grinderPort =
 		    properties.getMandatoryInt("grinder.multicastPort");
 	    }
 	    catch(Exception e) {
 		throw new EngineException("Couldn't resolve grinder address",
 					  e);
 	    }
 	}
 
 	m_reportToConsole =
 	    properties.getBoolean("grinder.reportToConsole", false);
 	m_reportToConsoleInterval =
 	    properties.getInt("grinder.reportToConsole.interval", 500);
 
 	m_logDirectory =
 	    properties.getMandatoryProperty("grinder.logDirectory");
 	m_appendToLog = properties.getBoolean("grinder.appendLog", false);
 
 	if (m_reportToConsole) {
 	    try{
 		m_consoleAddress =
 		    InetAddress.getByName(
 			properties.getMandatoryProperty(
 			    "grinder.console.multicastAddress"));
 
 		m_consolePort =
 		    properties.getMandatoryInt(
 			"grinder.console.multicastPort");
 
 		m_datagramSocket = new DatagramSocket();
 	    }
 	    catch(Exception e) {
 		throw new EngineException("Couldn't resolve console address",
 					  e);
 	    }
 	}
 
 	// Parse plug-in class.
 	try{
 	    m_pluginClass =
 		Class.forName(properties.getProperty("grinder.plugin"));
 
 	    if (!GrinderPlugin.class.isAssignableFrom(m_pluginClass)) {
 		throw new EngineException(
 		    "The specified plug-in class ('" +
 		    m_pluginClass.getName() +
 		    "') does not implement the interface: '" +
 		    GrinderPlugin.class.getName() + "'");
 	    }
 	}
 	catch(ClassNotFoundException e){
 	    throw new EngineException(
 		"The specified plug-in class was not found.", e);
 	}
 
 	// Plugin parameters.
 	m_pluginParameters =
 	    properties.getPropertySubset("grinder.plugin.parameter.");
 
 	// Parse plug-in methods.
 	final String pluginMethods =
 	    properties.getMandatoryProperty("grinder.plugin.methods");
 
 	final StringTokenizer methodTokeniser =
 	    new StringTokenizer(pluginMethods,"\t\n\r,");
 
 	final Vector methodNames = new java.util.Vector();
 
 	while(methodTokeniser.hasMoreElements()) {
 	    methodNames.addElement(methodTokeniser.nextElement());
 	}
 
 	m_methods = new Method[methodNames.size()];
 	m_methodNames = new String[methodNames.size()];
             
 	for (int i=0; i<methodNames.size(); i++){
 	    m_methodNames[i] = (String)methodNames.get(i);
 
 	    try{
 		m_methods[i] = m_pluginClass.getMethod(m_methodNames[i], null);
 	    }
 	    catch(Exception e){
 		throw new EngineException(
 		    "Could not initialize the plug-in method: " +
 		    m_methodNames[i], e);
 	    }
 	}
     }
     
 
     /**
      * The application's main loop. This is split from the constructor
      * as theoretically it might be called multiple times. The
      * consturctor sets up the static configuration, this does a
      * single execution.
      */        
     protected void run() throws GrinderException
     {
 	final String dataFilename = m_filenameFactory.createFilename("data");
 	PrintWriter dataPrintWriter = null;
 
 	try {
 	    dataPrintWriter =
 		new PrintWriter(
 		    new BufferedWriter(
 			new FileWriter(dataFilename, m_appendToLog)));
 
 	    if (!m_appendToLog) {
 		dataPrintWriter.println("Thread, Cycle, Method, Time");
 	    }
 	}
 	catch(Exception e){
 	    throw new EngineException("Cannot open process data file '" +
 				      dataFilename + "'", e);
 	}
 
 	final MethodStatistics[]  methodStatistics =
 	    new MethodStatistics[m_methods.length];
 	final MethodStatistics[]  lastMethodStatistics =
 	    new MethodStatistics[m_methods.length];
 
 	for (int i=0; i<m_methods.length; i++){
 	    methodStatistics[i] = new MethodStatistics();
 	    lastMethodStatistics[i] = new MethodStatistics();
 	}
 
 	final GrinderThread runnable[] = new GrinderThread[m_numberOfThreads];
 
 	for (int i=0; i<m_numberOfThreads; i++) {
 	    final GrinderContextImplementation grinderContext =
 		new GrinderContextImplementation(m_pluginParameters,
 						 m_hostID, m_jvmID, i);
 
 	    runnable[i] = new GrinderThread(m_pluginClass, grinderContext,
 					    dataPrintWriter, m_methods,
 					    methodStatistics);
 	}
         
 	if (m_waitForConsoleSignal) {
 	    System.out.println("Grinder (" + getContextString() +
 			       ") waiting for console signal");
 	    waitForSignal();
 	}
 
 	System.out.println("Grinder (" + getContextString() +
 			   ") starting threads");
 
 	//   Start the threads
 	for (int i=0; i<m_numberOfThreads; i++) {
 	    final Thread t = new Thread(runnable[i], "Grinder thread " + i);
 	    t.start();             
 	}
 
 	while (GrinderThread.numberOfUncompletedThreads() > 0)
 	{
 	    try {
 		Thread.sleep(m_reportToConsoleInterval);
 	    }
 	    catch (InterruptedException e) {
 		continue;
 	    }
 
 	    if (m_reportToConsole) {
 		for (int i=0; i<m_methods.length; i++) {
 		    final MethodStatistics delta =
 			methodStatistics[i].getDelta(
 			    lastMethodStatistics[i], true);
 
 		    final String msg =
 			m_hostID + "," + 
 			m_jvmID + "," + 
 			m_numberOfThreads + "," + 
 			i + "," +
 			delta.getTotalTime() + "," +
 			delta.getTransactions() + "," +
 			delta.getAverageTransactionTime();
 		    
 		    //Java 1.1 (it doesn't work)
 		    // b = msg.getBytes();
 		    // Java 1.0 Deprecated in Java 1.1, but it works:
 		    final byte[] b = new byte[msg.length() + 1];
 		    msg.getBytes(0, msg.length(), b, 0);
 
 		    try{
 			final DatagramPacket packet =
 			    new DatagramPacket(b, b.length, m_consoleAddress,
 					       m_consolePort);
 			m_datagramSocket.send(packet);
 		    }
 		    catch(SocketException e) {
 			System.err.println(e);
 		    } 
 		    catch(IOException e) {
 			System.err.println(e);
 		    }
 		} 
 	    }
 	}
 
         if (dataPrintWriter != null) {
 	    dataPrintWriter.close();
 	}
 
 	System.out.println("Grinder (" + getContextString() +
 			   ") finished");
 
  	System.out.println("Final statistics for this process:");
 
 	final MethodStatisticsTable statisticsTable =
 	    new MethodStatisticsTable(m_methodNames, methodStatistics);
 
 	statisticsTable.print(System.out);
     }
     
     private void waitForSignal() {
 	byte[] inbuf = new byte[1024];
 	try{
 	    final MulticastSocket msocket = new MulticastSocket(m_grinderPort);
 	    msocket.joinGroup(m_grinderAddress);
 
 	    DatagramPacket packet = new DatagramPacket(inbuf, inbuf.length);
 
  	    msocket.receive(packet);
 	}
 	catch(SocketException e){
 	    System.err.println(e);
 	    e.printStackTrace();
 	}
 	catch(IOException e){
 	    System.err.println(e);
 	    e.printStackTrace();
 	}
     }	
 
     private String getContextString()
     {
 	return "Host " + m_hostID + " JVM " + m_jvmID;
     }
 }
 
