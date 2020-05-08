 /*
  * Copyright (c) 2012 European Synchrotron Radiation Facility,
  *                    Diamond Light Source Ltd.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */ 
 
 package org.dawb.gda.extensions.jython;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 import org.dawb.common.util.eclipse.BundleUtils;
 import org.python.core.PyString;
 import org.python.core.PyStringMap;
 import org.python.core.PySystemState;
import org.python.pydev.plugin.PydevPlugin;
import org.python.pydev.ui.interpreters.JythonInterpreterManager;
import org.python.pydev.ui.pythonpathconf.InterpreterInfo;
 import org.python.util.PythonInterpreter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.PlotServer;
 import uk.ac.diamond.scisoft.python.PythonUtils;
 import uk.ac.gda.util.OSUtils;
 
 import com.thoughtworks.xstream.core.util.CompositeClassLoader;
 
 /**
  * SCISOFT - added static method which returns a PythonInterpreter which can run scisoft scripts
 This is for executing a script directly from the workflow tool when you do not want to
 start a separate debug/run process to start the script.
  */
 public class JythonInterpreterUtils {
 
 	private static Logger logger = LoggerFactory.getLogger(JythonInterpreterUtils.class);
 	
 	static {
 		PySystemState.initialize();
 	}
 	
 	/**
 	 * scisoftpy is imported as dnp
 	 * scisoftpy.core as scp
 	 * 
 	 * @return a new PythonInterpreter with scisoft scripts loaded.
 	 * @throws IOException 
 	 * @throws ClassNotFoundException 
 	 */
 	public static PythonInterpreter getInterpreter() throws Exception {
 		
 		final long start = System.currentTimeMillis();
 				
 		logger.debug("Starting new Jython Interpreter.");
 		
 		PySystemState.add_package("uk.ac.diamond.scisoft.analysis.plotserver");
 		PySystemState.add_package("uk.ac.diamond.scisoft.python");
 		// Force class to load
 		PySystemState     state       = new PySystemState();
 		
 		final CompositeClassLoader loader = new CompositeClassLoader();
 		loader.add(PlotServer.class.getClassLoader());
 		loader.add(PythonUtils.class.getClassLoader());
 		state.setClassLoader(loader);
 		File libsLocation;
 		try {
 			libsLocation = BundleUtils.getBundleLocation("uk.ac.gda.libs");
 		} catch (Exception ignored) {
 			libsLocation = null;
 		}
 		if (libsLocation == null) {
 			if (System.getProperty("test.libs.location")==null) throw new Exception("Please set the property 'test.libs.location' for this test to work!");
 			libsLocation = new File(System.getProperty("test.libs.location"));
 		}
 		
 		String jyLib = libsLocation.getAbsolutePath()+"/jython2.5.1/Lib/";
 		state.path.append(new PyString(jyLib));
 		state.path.append(new PyString(jyLib+"dist-utils"));
 		state.path.append(new PyString(jyLib+"site-packages"));
 		state.path.append(new PyString(jyLib+"site-packages/decorator-3.2.0-py2.5.egg"));
 		state.path.append(new PyString(jyLib+"nose-0.11.1-py2.5.egg/nose/ext"));
 
 		try {
 			File pythonPlugin = BundleUtils.getBundleLocation("uk.ac.diamond.scisoft.python");
 			state.path.append(new PyString(pythonPlugin.getAbsolutePath()));
 			state.path.append(new PyString(new File(pythonPlugin, "src").getAbsolutePath()));
 			state.path.append(new PyString(new File(pythonPlugin, "bin").getAbsolutePath()));
 		} catch (Exception e) {
 			logger.error("Could not find Scisoft Python plugin", e);
 		}
 		
 		// If an interpreter called 'jython2.5.1' exists, use that
 		JythonInterpreterManager man = (JythonInterpreterManager)PydevPlugin.getJythonInterpreterManager();
 		InterpreterInfo          inf = null;
 		try {
 			inf = man.getInterpreterInfo("Jython2.5.1", null);
 		} catch (Throwable ignored) {
 			inf = null;
 		}
 		try {
 			if (inf==null) inf =  man.getInterpreterInfo("jython2.5.1", null);
 		} catch (Throwable ignored) {
 			inf = null;
 		}
 		if (inf!=null) {
 			final List<String> paths = inf.getPythonPath();
 			for (String path : paths) state.path.append(new PyString(path));
 		}
 		
 
 		String home = System.getProperty("eclipse.home.location");
 		if (home.startsWith("file:")) home = home.substring("file:".length());
 		if (home.startsWith("/")&&OSUtils.isWindowsOS()) home = home.substring(1);
 		state.path.append(new PyString(home+"/plugins"));
 		
 		try {
 			File analysisPlugin = BundleUtils.getBundleLocation("uk.ac.diamond.scisoft.analysis");
 			state.path.append(new PyString(analysisPlugin.getAbsolutePath()));
 			state.path.append(new PyString(new File(analysisPlugin, "bin").getAbsolutePath()));
 		} catch (Exception e) {
 			logger.error("Could not find Scisoft Python plugin", e);
 		}
 		
 		PythonInterpreter interpreter = new PythonInterpreter(new PyStringMap(), state);
 		interpreter.exec("import scisoftpy as dnp");
 		
 		final long end = System.currentTimeMillis();
 		
 		logger.debug("Created new Jython Interpreter in "+(end-start)+"ms.");
 	
 		return interpreter;
 	}
 
 }
