 /*******************************************************************************
  * 2011 Ivan Shubin http://mindengine.net
  * 
  * This file is part of MindEngine.net Oculus Grid.
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with Oculus Experior.  If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package net.mindengine.oculus.grid.agent.taskrunner;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.Date;
 import java.util.Random;
 
 import net.mindengine.oculus.experior.suite.XmlSuiteParser;
import net.mindengine.oculus.experior.utils.FileUtils;
 import net.mindengine.oculus.grid.domain.task.SuiteTask;
 import net.mindengine.oculus.grid.storage.Project;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  * Used for running the tests with <b>Oculus Test Run Framework</b>
  * 
  * @author Ivan Shubin
  * 
  */
 public class SuiteTaskRunner extends TaskRunner {
 	private Log logger = LogFactory.getLog(getClass());
 
 	public void downloadProjectFromServer(String projectName, String version) {
 	    
 	}
 	
 	
 	@Override
 	public void run() {
 		try {
 			/*
 			 * Creating the java process with all libraries which are needed to
 			 * run the automation project
 			 */
 			SuiteTask task = (SuiteTask) getTask();
 			
 			if(getProjectSyncNeeded()) {
 			    Project project = getAgent().getServer().downloadProject(task.getProjectName(), task.getProjectVersion());
 			    getAgent().getStorage().putProjectZip(task.getProjectName(), task.getProjectVersion(), project.getBytes(), "agent", project.getControlKey());
 			}
 			
 
 			String projectFolder = task.getProjectName() + File.separator;
 			if (task.getProjectVersion() != null && !task.getProjectVersion().isEmpty()) {
 				projectFolder += task.getProjectVersion();
 			}
 			else {
 				projectFolder += "current";
 			}
 
 			String storagePath = getAgent().getAgentStoragePath();
 			String pathToOculusGrid = getAgent().getAgentOculusGridLibrary();
 			
 			if(!new File(pathToOculusGrid).exists()) {
 			    throw new FileNotFoundException(pathToOculusGrid);
 			}
 			
 			String currentProjectDir = storagePath + File.separator + projectFolder;
 			String oculusRunnerClasspath = getAgent().getAgentOculusRunner();
 			
 			if(storagePath==null) {
 			    throw new IllegalArgumentException("Agent storage path is not specified");
 			}
 			if(pathToOculusGrid==null) {
                 throw new IllegalArgumentException("Path to oculus grid library is not specified");
             }
 			
 			/*
 			 * Determine separator for java classpath
 			 */
 			String jSeparator = ":";
 
 			String osName = System.getProperty("os.name");
 			if (osName != null) {
 				logger.info("OS: " + osName);
 				if (osName.toLowerCase().startsWith("windows")) {
 					jSeparator = ";";
 				}
 			}
 
 			/*
 			 * Going to create a temporary suite where all the received tests
 			 * will be stored. In order to avoid the collision in suite file
 			 * name with another agents on same machine we would need to
 			 * generate suite file name with current date and a random number
 			 */
 			String suiteFileName = "suite" + new Date().getTime() + (new Random().nextInt(6)) + ".xml";
 
 			File file = new File(currentProjectDir + File.separator + suiteFileName);
 
 			/*
 			 * Checking if file already exists
 			 */
 			if (file.exists()) {
 				/*
 				 * Regenerating the suite filename
 				 */
 				suiteFileName = "suite" + new Date().getTime() + (new Random().nextInt(6)) + ".xml";
 				file = new File(currentProjectDir + File.separator + suiteFileName);
 			}
 			
 			logger.info("Saving suite to " + file.getAbsolutePath());
 			XmlSuiteParser.saveSuite(task.getSuite(), file);
			
			/*
			 * Actually here the process command should wrap the classpath in quotes but for some reason java throws error "unable to find jarfile"
			 * 
			 */
			
			String processCommand = "java -classpath " + pathToOculusGrid + jSeparator 
 			    + currentProjectDir + File.separator + "libs" + File.separator + "*" + jSeparator
 			    + currentProjectDir + File.separator + "lib" + File.separator + "*" + jSeparator
			    + currentProjectDir + File.separator + "*" 
 			    + " " + oculusRunnerClasspath 
 			    + " localhost " 
 			    + getAgent().getAgentPort() + " " 
 			    + getAgent().getAgentRemoteName() + " " 
 			    + suiteFileName;
 
 			logger.info("Working directory is: " + currentProjectDir);
 			logger.info("Executing the process: \n" + processCommand);
 
 			Runtime runtime = Runtime.getRuntime();
 			Process process = runtime.exec(processCommand, null, new File(currentProjectDir));
 
 			//TODO create a handler which waits for a response form launched process and in case there is no response - it kills it and reports error.
 			
 			BufferedReader inputStreamReader = new BufferedReader(new InputStreamReader((process.getInputStream())));
 			BufferedReader errorStreamReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
 			String consoleText = null;
 			String errorText = null;
 			while (((consoleText = inputStreamReader.readLine()) != null) || (errorText = errorStreamReader.readLine()) != null) {
 				if (consoleText != null) {
 					System.out.println(consoleText);
 				}
 				else if (errorText != null) {
 					System.out.println(errorText);
 				}
 			}
 			/*
 			 * Removing the temporary suite file
 			 */
 			file.delete();
 		}
 		catch (Throwable error) {
 		    error.printStackTrace();
 			getAgent().onTaskError(error);
 		}
 	}
 }
