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
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import screen.tools.sbs.context.ContextException;
 import screen.tools.sbs.context.ContextHandler;
 import screen.tools.sbs.context.defaults.ContextKeys;
 import screen.tools.sbs.context.defaults.EnvironmentVariablesContext;
 import screen.tools.sbs.objects.EnvironmentVariables;
 import screen.tools.sbs.objects.ErrorList;
 import screen.tools.sbs.objects.Pack;
 
 public class ExecLauncher {
 	private Pack pack;
 	private ContextHandler contextHandler;
 	private final List<FieldPath> runtimePaths;
 	
 	public ExecLauncher(ContextHandler contextHandler, Pack pack, List<FieldPath> runtimePaths) {
 		this.contextHandler = contextHandler;
 		this.pack = pack;
 		this.runtimePaths = runtimePaths;
 	}
 	
 	public void launch() throws ContextException, FieldException{
 		EnvironmentVariables variables = contextHandler.<EnvironmentVariablesContext>get(ContextKeys.ENV_VARIABLES).getEnvironmentVariables();
 		
 		StringBuffer paths = new StringBuffer();
 		if(runtimePaths.isEmpty()){
 			paths.append(File.pathSeparator);
 		}
 		else{
 			Iterator<FieldPath> iterator = runtimePaths.iterator();
 			while(iterator.hasNext()){
 				FieldPath fieldPath = iterator.next();
 				paths.append(fieldPath.getString());
 				if(iterator.hasNext())
 					paths.append(File.pathSeparator);
 			}
 		}
 		
 		Logger.debug(paths.toString());
 				
 		FieldString fieldRepoRoot = variables.getFieldString("REPOSITORY_ROOT");
 		String repoRoot = fieldRepoRoot.getString();
 		
 		FieldString fieldEnvName = variables.getFieldString("ENV_NAME");
 		String envName = fieldEnvName.getString();
 
 		FieldString fieldCompileMode = variables.getFieldString("_COMPILE_MODE");
 		String compileMode = fieldCompileMode.getString();
 
 		String path = repoRoot+"/"+pack.getProperties().getName().getString()+
 						"/"+pack.getProperties().getVersion().getString()+
 						"/exe/"+envName+"/"+compileMode;
 
 		EnvironmentVariables addVars = new EnvironmentVariables();
 		addVars.put("EXE_NAME", pack.getProperties().getName().getString().replaceAll("/", ""));
 		addVars.put("EXE_VERSION", pack.getProperties().getVersion().getString());
 		FieldString launchCommand = variables.getFieldString("LAUNCH_COMMAND");
 	    List<String> command = new ArrayList<String>();
 		command.add(path+"/"+launchCommand.getString(addVars));
 		
 		Logger.info(ProcessLauncher.getCommand(command));
 		
 		ProcessHandler processHandler = new ProcessHandler(command) {
 			
 			@Override
 			public void processOutLine(String line) {
 				Logger.info(line);					
 			}
 			
 			@Override
 			public void processErrLine(String line) {
 				Logger.error(line);
 		    	ErrorList.instance.addError(line);					
 			}
 		};
 		processHandler.getProcessBuilder().directory(new File(path));
 		Map<String, String> environment = processHandler.getProcessBuilder().environment();
		String variableName = null;
		if(Utilities.isWindows()){
			variableName = "Path";
		}
		else{
			variableName = "LD_LIBRARY_PATH";
		}
		String envPath = environment.get(variableName);
		environment.put(variableName,envPath+File.pathSeparator+paths.toString());
 		processHandler.exec();
 		}
 }
