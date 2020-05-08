 /*-
  * Copyright © 2010 Diamond Light Source Ltd.
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
 
 package uk.ac.diamond.scisoft.analysis.rcp.util;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.python.core.PyString;
 import org.python.core.PyStringMap;
 import org.python.core.PySystemState;
 import org.python.util.PythonInterpreter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.gda.common.rcp.util.BundleUtils;
 
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
 		PySystemState     state       = new PySystemState();
 		
 		final ClassLoader classLoader = uk.ac.diamond.scisoft.analysis.PlotServer.class.getClassLoader();
 		state.setClassLoader(classLoader);
 		
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
 			state.path.append(new PyString(new File(pythonPlugin, "bin").getAbsolutePath()));
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
