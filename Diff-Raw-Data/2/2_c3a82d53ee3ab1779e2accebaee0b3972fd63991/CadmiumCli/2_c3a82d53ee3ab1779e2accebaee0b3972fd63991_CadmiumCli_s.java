 /**
  *    Copyright 2012 meltmedia
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 package com.meltmedia.cadmium.cli;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.reflections.Reflections;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.beust.jcommander.JCommander;
 import com.beust.jcommander.Parameter;
 import com.beust.jcommander.ParameterException;
 import com.beust.jcommander.Parameters;
 import com.meltmedia.cadmium.core.git.GitService;
 import com.meltmedia.cadmium.core.github.ApiClient;
 
 /**
  * The core class to every Cadmium command line interface commands.
  * 
  * @author Christian Trimble
  * @author John McEntire
  * @author Brian Barr
  *
  */
 public class CadmiumCli {
 
   private static final Logger logger = LoggerFactory.getLogger(CadmiumCli.class);
   
   /**
    * The Object used to parse and populate the command line arguments into the command object instances.
    * 
    * @see <a href="http://jcommander.org/">JCommander</a>
    */
 	public static JCommander jCommander = null;
 
 	/**
 	 * The main entry point to Cadmium cli.
 	 * 
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		try {
 		  jCommander = new JCommander();
 		  	  
 		  jCommander.setProgramName("cadmium");
 		  
 		  HelpCommand helpCommand = new HelpCommand();
 		  jCommander.addCommand("help", helpCommand);
       
       Map<String, CliCommand> commands = wireCommands(jCommander);
 		  try {
 		    jCommander.parse(args);
 		  } catch(ParameterException pe) {
 		    System.err.println(pe.getMessage());
 		    System.exit(1);
 		  }
 		  
 		  String commandName = jCommander.getParsedCommand();
 		  if( commandName == null ) {
 		    System.out.println("Please use one of the following commands:");
 		    for(String command : jCommander.getCommands().keySet() ) {
 		      String desc = jCommander.getCommands().get(command).getObjects().get(0).getClass().getAnnotation(Parameters.class).commandDescription();
 		      System.out.format("   %16s    -%s\n", command, desc);
 		    }
 		  } 
 		  else if( commandName.equals("help") ) {
 			  if( helpCommand.subCommand == null || helpCommand.subCommand.size()==0 ) {
 				  jCommander.usage();
 				  return;
 			  }
 			  else {
 				 JCommander subCommander = jCommander.getCommands().get(helpCommand.subCommand.get(0));
 				 if( subCommander == null ) {
 					 System.out.println("Unknown sub command "+commandName);
 					 return;
 				 }
 				 subCommander.usage();
 				 return;
 			  }
 		  } 
 		  else if(commands.containsKey(commandName)){
 		    CliCommand command = commands.get(commandName);
 		    if(command instanceof AuthorizedOnly) {
 		      setupSsh(((AuthorizedOnly) command).isAuthQuiet());
 		      setupAuth((AuthorizedOnly) command);
 		    }
 		    command.execute();
 		  }
 		
 		}
 		catch( Exception e ) {
 			System.err.println("Error: " + e.getMessage());
 			logger.debug("Cli Failed", e);
 			e.printStackTrace();
 			System.exit(1);
 		}
 
 	}
 
 	/**
 	 * Sets up the ssh configuration that git will use to communicate with the remote git repositories.
 	 * 
 	 * @param noPrompt True if there should be authentication prompts for the users username and password. 
 	 *        If false, the program will fail with an exit code of 1 if not authorized. 
 	 */
 	private static void setupSsh(boolean noPrompt) {
 		File sshDir = new File(System.getProperty("user.home"), ".ssh");
 		if(sshDir.exists()) {
 			GitService.setupLocalSsh(sshDir.getAbsolutePath(), noPrompt);
 		}
 	}
 	
 	/**
 	 * <p>Retrieves and applies whatever previously authorized Github token was used. If the requested command
 	 * requests quiet authentication this will exit with 1 when not authenticated. Otherwise, if the requested 
 	 * command doesn't request quiet authentication this will prompt for and authorize the current user in Github.</p>
 	 * 
 	 * @see {@link AuthorizedOnly.isAuthQuiet}
 	 * @param authCmd
 	 * @throws Exception
 	 */
 	private static void setupAuth(AuthorizedOnly authCmd) throws Exception {
 	  String token = ApiClient.getToken();
 	  if(token != null) {
 	    try {
 	      new ApiClient(token);
 	    } catch(Exception e) {
 	      token = null;
 	    }
 	  }
 	  if(token == null && !authCmd.isAuthQuiet()) {
 	  	if(System.console() == null) {
	  		System.err.println("Please reautenticate with github.");
 	  		System.exit(1);
 	  	} else {
 		    String username = System.console().readLine("Username [github]: ");
 		    String password = new String(System.console().readPassword("Password: "));
 		    List<String> scopes = new ArrayList<String>();
 		    scopes.add("repo");
 		    ApiClient.authorizeAndCreateTokenFile(username, password, scopes);
 		    
 		    token = ApiClient.getToken();
 		  }
 	  }
 	  
 	  if(token != null) {
 	    authCmd.setToken(token);
 	  } else {
 	    if(authCmd.isAuthQuiet()) {
 	      System.err.println("Github auth failed: Please rerun cadmium install script [cli-install.py]");
 	    } else {
 	      System.err.println("Github auth failed");
 	    }
 	    System.exit(1);
 	  }
 	}
 	
 	/**
 	 * <p>Automatically wires in all CliCommand subtypes using Reflections library.</p>
 	 * 
 	 * @see <a href="http://code.google.com/p/reflections/">Reflections</a>
 	 * @see <a href="http://jcommander.org/">JCommander</a>
 	 * 
 	 * @param jCommander The JCommander instance to wire the commands into.
 	 * @return A map of all commands found mapped to their command names.
 	 * @throws Exception
 	 */
 	private static Map<String, CliCommand> wireCommands(JCommander jCommander) throws Exception {
     Map<String, CliCommand> commands = new LinkedHashMap<String, CliCommand>();
     Reflections reflections = new Reflections("com.meltmedia.cadmium");
     Set<Class<? extends CliCommand>> subTypes = reflections.getSubTypesOf(CliCommand.class);
     for(Class<? extends CliCommand> cliCommandClass : subTypes) {
       CliCommand command = cliCommandClass.newInstance();
       commands.put(command.getCommandName(), command);
       jCommander.addCommand(command.getCommandName(), command);
     }
     return commands;
 	}
 }
