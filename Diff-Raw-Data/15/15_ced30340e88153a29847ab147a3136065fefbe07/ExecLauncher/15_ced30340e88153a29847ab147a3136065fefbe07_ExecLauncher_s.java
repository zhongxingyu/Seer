 /*****************************************************************************
  * This source file is part of SBS (Screen Build System),                    *
  * which is a component of Screen Framework                                  *
  *                                                                           *
  * Copyright (c) 2008-2011 Ratouit Thomas                                    *
  *                                                                           *
  * This program is free software; you can redistribute it and/or modify it   *
  * under the terms of the GNU Lesser General Public License as published by  *
  * the Free Software Foundation; either version 3 of the License, or (at     *
  * your option) any later version.                                           *
  *                                                                           *
  * This program is distributed in the hope that it will be useful, but       *
  * WITHOUT ANY WARRANTY; without even the implied warranty of                *
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser   *
  * General Public License for more details.                                  *
  *                                                                           *
  * You should have received a copy of the GNU Lesser General Public License  *
  * along with this program; if not, write to the Free Software Foundation,   *
  * Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA, or go to   *
  * http://www.gnu.org/copyleft/lesser.txt.                                   *
  *****************************************************************************/
 
 package screen.tools.sbs.utils;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import screen.tools.sbs.objects.EnvironmentVariables;
 import screen.tools.sbs.objects.ErrorList;
 import screen.tools.sbs.objects.GlobalSettings;
 import screen.tools.sbs.objects.Pack;
 
 public class ExecLauncher {
 	private Pack pack;
 	
 	public ExecLauncher(Pack pack) {
 		this.pack = pack;
 	}
 	
 	public void launch(){
 		ErrorList err = GlobalSettings.getGlobalSettings().getErrorList();
 		EnvironmentVariables variables = GlobalSettings.getGlobalSettings().getEnvironmentVariables();
 		
 		if(!variables.contains("REPOSITORY_ROOT")){
 			err.addError("undefined variable : REPOSITORY_ROOT");
 		}
 		String repoRoot = variables.getValue("REPOSITORY_ROOT");
 		
 		if(!variables.contains("ENV_NAME")){
 			err.addError("undefined variable : ENV_NAME");
 		}
 		String envName = variables.getValue("ENV_NAME");
 
 		if(!variables.contains("_COMPILE_MODE")){
 			err.addError("undefined variable : _COMPILE_MODE");
 		}
 		String compileMode = variables.getValue("_COMPILE_MODE");
 
 		String path = repoRoot+"/"+envName+"/"+compileMode;
 
 		if(!variables.contains("LAUNCH_COMMAND")){
 			err.addError("undefined variable : LAUNCH_COMMAND");
 		}
 		
 		EnvironmentVariables addVars = new EnvironmentVariables();
 		addVars.put("EXE_NAME", pack.getProperties().getName().getString().replaceAll("/", ""));
 		addVars.put("EXE_VERSION", pack.getProperties().getVersion().getString());
 		String launchCommand = variables.getValue("LAUNCH_COMMAND",addVars);
 		
         try {
 			List<String> command = new ArrayList<String>();
			command.add(launchCommand);
         	ProcessLauncher p = new ProcessLauncher();
			p.execute(command.toArray(new String[command.size()]),null,new File(path));
 			
 			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
 
 	        BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
 
             String s;
 	        while ((s = stdInput.readLine()) != null) {
             	Logger.info(s);
             }
             while ((s = stdError.readLine()) != null) {
                 err.addError(s);
             }
             
         }
         catch (IOException e) {
         	err.addError(e.getMessage());
         }
 	}
 }
