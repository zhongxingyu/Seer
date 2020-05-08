 /*
  * Copyright (c) 2011 Marco Aurélio Graciotto Silva <magsilva@ironiacorp.com>
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 
 package com.ironiacorp.statistics.r;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.ironiacorp.computer.ComputerSystem;
 import com.ironiacorp.computer.OperationalSystem;
 
 
 /**
  * Rserve (http://www.rforge.net/Rserve/) is a TCP/IP server which allows
  * other programs to use facilities of R (www.r-project.org) from various
  * languages without the need to initialize R or link against R library.
  * Every connection has a separate workspace and working directory.
  */
 public class RServe
 {
 	private static final String DEFAULT_R_EXECUTABLE = "R";
 
 	private static final String DEFAULT_RSERVE_EXECUTABLE = "Rserve";
 
 	private static final String[] subdirs = {"library", "site-library"};
 	
 	private String home;
 	
 	public String getHome()
 	{
 		return home;
 	}
 
 	public void setHome(String home)
 	{
 		this.home = home;
 	}
 
 	protected File getRServeExecutable()
 	{
 		OperationalSystem os = ComputerSystem.getCurrentOperationalSystem();
 		String rHome = System.getenv("R_HOME");
 		File rserveExecutable;
 		
 		if (rHome != null) {
 			StringBuilder sb = new StringBuilder();
 			for (String subdir : subdirs) {
 				sb.append(rHome);
 				sb.append(File.separator);
 				sb.append(subdir);
 				sb.append(File.separator);
 				sb.append("Rserve");
 				os.addExecutableSearchPath(new File(sb.toString()));
 				sb.setLength(0);
 			}
 		}
 			
 		rserveExecutable = os.findExecutable(DEFAULT_RSERVE_EXECUTABLE);
 
 		return rserveExecutable;
 	}
 
 	
 	protected File getRExecutable()
 	{
 		ComputerSystem computer = new ComputerSystem();
 		OperationalSystem os = computer.getCurrentOperationalSystem();
 		File rExecutable = os.findExecutable(DEFAULT_R_EXECUTABLE);
 
 		return rExecutable;
 	}
 
 	
	// TODO: Fix it. I don't know how and why, but this is not working.
 	public Process start()
 	{
		ComputerSystem computer = new ComputerSystem();
		OperationalSystem os = computer.getCurrentOperationalSystem();
 		ProcessBuilder pb;
 		File file;
 
 			
 		file = getRServeExecutable();
 		if (file != null) {
			pb = os.exec(file);
 			if 	(home != null) {
 				pb.environment().put("R_HOME", home);
 			}
 			try { 
 				return pb.start();
 			} catch (Exception e) {
 			}
 		}
 		
 		file = getRExecutable();
 		if (file != null) {
 			List<String> parameters = new ArrayList<String>();
 			parameters.add("CMD");
 			parameters.add("Rserve");
 			pb = os.exec(file, parameters);
 			if 	(home != null) {
 				pb.environment().put("R_HOME", home);
 			}
 			try { 
 				return pb.start();
 			} catch (Exception e) {
 			}
 		}
 		
 		throw new UnsupportedOperationException("Could not start the R server");
     }
 }
