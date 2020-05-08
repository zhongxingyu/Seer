 /*-
  * Copyright © 2011 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.dawb.common.python.rpc;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.dawb.common.util.eclipse.BundleUtils;
 import org.dawb.common.util.net.NetUtils;
 
 import uk.ac.diamond.scisoft.analysis.rpc.AnalysisRpcClient;
 import uk.ac.gda.util.OSUtils;
 
 import com.isencia.util.commandline.ManagedCommandline;
 import org.eclipse.core.variables.VariablesPlugin;
 
 /**
  * This class encapsulates a system command to python used with the RPC service
  * and drives python using the diamond RPC link.
  * 
  * It is a subset of the RPC interface availble by diamond pertinent to running
  * python commands in the way jep does.
  */
 public class PythonService {
 	
 	private ManagedCommandline command;
 	private AnalysisRpcClient  client;
 	private Thread             stopThread;
 	
 	/**
 	 * Must use openConnection()
 	 */
 	private PythonService() {
 		
 	}
 
 	/**
 	 * Each call to this method starts a python process with 
 	 * a server waiting for commands. An RCP client is 
 	 * attached to it.
 	 * 
 	 * @param command to start a python with numpy in. For instance
 	 * 'python', 'python2.6', or the full path
 	 * 
 	 * The port is started at 8613 and a free one is searched for.
 	 * The property org.dawb.passerelle.actors.scripts.python.free.port
 	 * many be used to change the start port if needed.
 	 * 
 	 * This method also adds a shutdown hook to ensure that the service
 	 * is stopped cleanly when the vm is shutdown. Calling the stop()
 	 * method removes this shutdown hook.
 	 * 
 	 * @return
 	 */
 	public static synchronized PythonService openConnection(final String pythonInterpreter) throws Exception {
 		
 		final PythonService service = new PythonService();
 		
 	    final String scisoftRpcPort = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution("${scisoft_rpc_port}");
 
 	    final int    port   = NetUtils.getFreePort(getServiceStartPort());
 		final File   path   = BundleUtils.getBundleLocation("org.dawb.common.python");
 		final String script;
 		if (System.getProperty("eclipse.debug.session")!=null || System.getProperty("org.dawb.test.session")!=null) {
 			script = path.getAbsolutePath()+"/src/org/dawb/common/python/rpc/python_service.py";
 		} else {
 			script = path.getAbsolutePath()+"/org/dawb/common/python/rpc/python_service.py";
 		}
 		
 		service.command = new ManagedCommandline();
		service.command.addArguments(new String[]{pythonInterpreter, "-u", script, String.valueOf(port), scisoftRpcPort});
 		
 		// Ensure uk.ac.diamond.scisoft.python in PYTHONPATH
 		final Map<String,String> env = new HashMap<String,String>(System.getenv());
 		String pythonPath = env.get("PYTHONPATH");
 		StringBuilder pyBuf = pythonPath==null ? new StringBuilder() : new StringBuilder(pythonPath);
 		if (OSUtils.isWindowsOS()) pyBuf.append(";"); else pyBuf.append(":");
 		if (System.getProperty("eclipse.debug.session")!=null || System.getProperty("org.dawb.test.session")!=null) {
 			pyBuf.append(BundleUtils.getBundleLocation("uk.ac.diamond.scisoft.python").getAbsolutePath()+"/src");
 		} else {
 			pyBuf.append(BundleUtils.getBundleLocation("uk.ac.diamond.scisoft.python").getAbsolutePath());
 		}
 		env.put("PYTHONPATH", pyBuf.toString());
 		service.command.setEnv(env);
 		
 		// Currently log back python output directly to the log file.
 		service.command.setStreamLogsToLogging(true);
 		service.command.setWorkingDir(path);
 		service.command.execute();
 		
 		service.stopThread = new Thread("Stop Python Service") {
         	public void run() {
         		service.stop();
         	}
 		};
 		Runtime.getRuntime().addShutdownHook(service.stopThread);
         	
 		service.client = service.getActiveClient(port);
 
 		return service;
 	}
 	
 	/**
 	 * This call opens a client to the service already running. If you
 	 * want you can run the python_serice.py in pydev then debug the commands
 	 * as they come it. Use this method to get the PythonService Java side in
 	 * that case. It will look for the running RPC service on the port passed
 	 * in and allow commands to be run and debugged.
 	 * 
 	 * @param port
 	 * @return
 	 * @throws Exception
 	 */
 	public static PythonService openClient(final int port) throws Exception {
 		
 		final PythonService service = new PythonService();
 		        	
 		service.client = service.getActiveClient(port);
 
 		return service;
 	}
 
 	/**
 	 * Tries to connect to the service, only returning when connected.
 	 * This is more reliable than waiting for a given time.
 	 * @param port
 	 * @return
 	 * @throws InterruptedException 
 	 */
 	private AnalysisRpcClient getActiveClient(int port) throws Exception {
 
 		if (!isRunning()) throw new Exception("The remote python process did not start!");
 		
 		int count=0;
 		final int time = System.getProperty("org.dawb.common.python.rpc.service.timeout") !=null ?
 				         Integer.parseInt(System.getProperty("org.dawb.common.python.rpc.service.timeout")) :
 				         5000;
 				          
 				
 		while(count<=time) {
 			try {
 				final AnalysisRpcClient client = new AnalysisRpcClient(port);
 				final Object active = client.request("isActive", new Object[]{"unused"}); // Calls the method 'run' in the script with the arguments
 			    if ((((Boolean)active)).booleanValue()) return client;
 			    Thread.sleep(100);
 			    count+=100;
 			    continue;
 			} catch (Exception ne) {
 			    count+=100;
 			    Thread.sleep(100);
 				continue;
 			}
 		}
 		throw new Exception("RPC connect to python timed out after "+time+"ms! Are you sure the python server is going?");
 	}
 
 	/**
 	 * Will be null when openClient(port) is used.
 	 * @return
 	 */
 	public ManagedCommandline getCommand() {
 		return command;
 	}
 
 	public AnalysisRpcClient getClient() {
 		return client;
 	}
 
 	public void stop() {
 		if (command==null)              return;
 		if (command.getProcess()==null) return;
 		command.getProcess().destroy();
 		if (stopThread!=null) {
 			try {
 				Runtime.getRuntime().removeShutdownHook(stopThread);
 			} catch (Throwable ne) {
 				// We try to remove it but errors are not required if we fail because this method may
 				// be called during shutdown, when it will.
 			}
 			stopThread = null;
 		}
 
 	}
 	
 	public boolean isRunning() {
 		if (command==null) return true; // Probably in debug mode
 		return !command.hasTerminated();
 	}
 
 	/**
 	 * Convenience method for calling
 	 * @param methodName
 	 * @param arguments
 	 * @param outputNames - names of global variables to read back from python
 	 * @return
 	 * @throws Exception
 	 */
 	public Map<String,? extends Object> runScript(String scriptFullPath, Map<String, ?> data, Collection<String> outputNames) throws Exception {
 		
 		final List<String> additionalPaths = new ArrayList<String>(1);
 		additionalPaths.add(BundleUtils.getEclipseHome());
 		if (System.getenv("PYTHONPATH")!=null) {
 			additionalPaths.addAll(Arrays.asList(System.getenv("PYTHONPATH").split(":")));
 		}
         
 		final Object out = client.request("runScript", new Object[]{scriptFullPath, data, outputNames, additionalPaths}); 
 		// Calls the method 'runScript' in the script with the arguments
 	    return (Map<String,? extends Object>)out;
 	}
 	
     /**
      * Run an edna plugin with some xml and get some xml back.
      * 
      * @param execDir
      * @param pluginName
      * @param ednaDebugMode
      * @param xmlInputString
      * @return
      */
 	public String runEdnaPlugin(final String  execDir, 
 			                    final String  pluginName,
 			                    final boolean ednaDebugMode, 
 			                    final String  xmlInputString) throws Exception {
 		
 		// We add fabio as an additional path to the service.
 		final List<String> additionalPaths = new ArrayList<String>(1);
 		additionalPaths.add(BundleUtils.getEclipseHome());
 		if (System.getenv("PYTHONPATH")!=null) {
 			additionalPaths.addAll(Arrays.asList(System.getenv("PYTHONPATH").split(":")));
 		}
 		
 		final Object out = client.request("runEdnaPlugin", new Object[]{execDir, pluginName, ednaDebugMode, xmlInputString, additionalPaths}); 
 		// Calls the method 'runEdnaPlugin' in the script with the arguments
 	    return (String)out;
 	}
 
 
 	public static int getDebugPort() {
 		int port = 8613;
 		if (System.getProperty("org.dawb.passerelle.actors.scripts.python.debug.port")!=null) {
 			// In an emergency allow the port to be changed for the debug session.
 			port = Integer.parseInt(System.getProperty("org.dawb.passerelle.actors.scripts.python.debug.port"));
 		}
 		return port;
 	}
 	
 	/**
 	 * Returns the port used to start the search for a free port in non-debug mode
 	 * @return
 	 */
 	private static int getServiceStartPort() {
 		int port = 8613;
 		if (System.getProperty("org.dawb.passerelle.actors.scripts.python.free.port")!=null) {
 			// In an emergency allow the port to be changed for the debug session.
 			port = Integer.parseInt(System.getProperty("org.dawb.passerelle.actors.scripts.python.free.port"));
 		}
 		return port;
 	}
 
 }
