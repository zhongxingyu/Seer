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
 import org.eclipse.core.runtime.preferences.InstanceScope;
 import org.eclipse.ui.preferences.ScopedPreferenceStore;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.rpc.AnalysisRpcClient;
 
 import com.isencia.util.commandline.ManagedCommandline;
 
 /**
  * This class encapsulates a system command to python used with the RPC service
  * and drives python using the diamond RPC link.
  * 
  * It is a subset of the RPC interface available by diamond pertinent to running
  * python commands in the way jep does.
  */
 public class PythonService {
 	
 	private static final Logger logger = LoggerFactory.getLogger(PythonService.class);
 	
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
 		
 	    int scisoftRpcPort = getSciSoftPlottingPort();
 	    if (scisoftRpcPort<=0) {
 	    	logger.error("Cannot identify scisoft plotting port.\nShow view part 'Plot 1' if you would like to start the plotting.");
 	    }
 
 		// Find the location of python_service.py and
 		// ensure uk.ac.diamond.scisoft.python in PYTHONPATH
 		final Map<String,String> env = new HashMap<String,String>(System.getenv());
 		String pythonPath = env.get("PYTHONPATH");
 
 		StringBuilder pyBuf;
 		if (pythonPath==null) {
 			pyBuf = new StringBuilder();
 		} else {
 			pyBuf = new StringBuilder(pythonPath);
 			pyBuf.append(File.pathSeparatorChar);
 		}
 	    final int    port   = NetUtils.getFreePort(getServiceStartPort());
 		final File   path   = BundleUtils.getBundleLocation("org.dawb.common.python");
 		String script = path.getAbsolutePath()+"/org/dawb/common/python/rpc/python_service.py";;
 		if (new File(script).exists()) {
 			pyBuf.append(BundleUtils.getBundleLocation("uk.ac.diamond.scisoft.python").getAbsolutePath());
 		} else {
 			// Check if we are running a development version
 			script = path.getAbsolutePath()+"/src/org/dawb/common/python/rpc/python_service.py";
 			if (new File(script).exists()) {
 				pyBuf.append(BundleUtils.getBundleLocation("uk.ac.diamond.scisoft.python").getAbsolutePath()+"/src");
 			} else {
 				throw new RuntimeException("Couldn't find path to python_service.py!");
 			}
 		}
 		
 		service.command = new ManagedCommandline();
 		service.command.addArguments(new String[]{pythonInterpreter, "-u", script, String.valueOf(port), String.valueOf(scisoftRpcPort)});
 		
 		env.put("PYTHONPATH", pyBuf.toString());
 		service.command.setEnv(env);
 		
 		// Currently log back python output directly to the log file.
 		service.command.setStreamLogsToLogging(true);
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
 	 * Tries to get a dir in the same place as the script, otherwise
 	 * it tries to get a dir in the user home.
 	 * 
 	 * @param scriptPath
 	 * @return
 	 */
 	private static File getWorkingDir(final String scriptPath) {
 		
 		File path = null;
 		try {
 			final File dir  = (new File(scriptPath)).getParentFile();
 			path = getUniqueDir(dir, "python_tmp");
			if (!path.canWrite() || !path.isDirectory() || !canTouch(path)) {
 				path = null;
 			}
 		} catch (Throwable ne) {
 			path = null;
 		}
 		
 		if (path==null) {
 			File home = new File(System.getProperty("user.home")+"/.dawn/");
 			home.mkdirs();
 			path = getUniqueDir(home, "python_tmp");
 		}
 		
 		return path;
 		
 	}
 
 	private static File getUniqueDir(File dir, String name) {
 		
 		int i = 1;
 		File ret = new File(dir, name+i);
 		while(ret.exists()) {
 			if (ret.list()==null || ret.list().length<1) break; // Use the same empty one.
 			++i;
 			ret = new File(dir, name+i);
 		}
 		ret.mkdirs();
 		return ret;
 	}
 
 	private static boolean canTouch(File path) {
 		try {
 			path.mkdirs();
 			File touch = new File(path, "touch");
 			touch.createNewFile();
 			touch.delete();
 		} catch (Throwable ne) {
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Call to get the scisoft plotting port, may be "", null or 0.
 	 * Will check temp variable set to dynamic port.
 	 * @return
 	 */
 	public static int getSciSoftPlottingPort() {
 	    
 		final ScopedPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, "uk.ac.diamond.scisoft.analysis.rcp");
 		int scisoftRpcPort=0; 
 	    try {
 	    	scisoftRpcPort = store.getInt("analysisrpc.server.port");
 	    	if (scisoftRpcPort>0) {
 	    		logger.info("Found RPC plotting port set to value of "+scisoftRpcPort);
 	    		return scisoftRpcPort;
 	    	}
 	    } catch (Exception ne) {
 	    	scisoftRpcPort = 0;
 	    }
 	    
 	    
 	    if (scisoftRpcPort<=0) {
 		    try {
 		    	scisoftRpcPort = store.getInt("analysisrpc.server.port.auto");
 		    	if (scisoftRpcPort>0) {
 		    		logger.info("Found RPC plotting port set to temporary value of "+scisoftRpcPort);
 		    		return scisoftRpcPort;
 		    	}
 		    } catch (Exception ne) {
 		    	scisoftRpcPort = 0;
 		    }
 	    }
 
     	// TODO Ensure plotting is started programmatically in the GUI.
 	    return scisoftRpcPort;
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
 	@SuppressWarnings("unchecked")
 	public Map<String,? extends Object> runScript(String scriptFullPath, Map<String, ?> data, Collection<String> outputNames) throws Exception {
 		
 		final File dir = getWorkingDir(scriptFullPath);
 		command.setWorkingDir(dir);
 		final List<String> additionalPaths = new ArrayList<String>(1);
 		additionalPaths.add(BundleUtils.getEclipseHome());
 		additionalPaths.add(new File(scriptFullPath).getParent().toString());
 		if (System.getenv("PYTHONPATH")!=null) {
 			additionalPaths.addAll(Arrays.asList(System.getenv("PYTHONPATH").split(File.pathSeparator)));
 		}
         
 		final Object out = client.request("runScript", new Object[]{scriptFullPath, data, outputNames, additionalPaths}); 
 		
 		if (dir.exists() && (dir.list()==null || dir.list().length<1)) {
 			dir.delete();
 		}
 		
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
 		
 		//TODO Should AbstractEdnaPlugin set the working dir on the command so that
 		//streams from the process are logged correctly?
 		
 		// We add fabio as an additional path to the service.
 		final List<String> additionalPaths = new ArrayList<String>(1);
 		additionalPaths.add(BundleUtils.getEclipseHome());
 		if (System.getenv("PYTHONPATH")!=null) {
 			additionalPaths.addAll(Arrays.asList(System.getenv("PYTHONPATH").split(File.pathSeparator)));
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
