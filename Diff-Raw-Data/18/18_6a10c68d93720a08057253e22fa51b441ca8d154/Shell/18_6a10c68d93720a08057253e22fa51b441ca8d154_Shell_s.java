 /**
  * This file is part of Atomic Tagging.
  * 
  * Atomic Tagging is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
  * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
  * version.
  * 
  * Atomic Tagging is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
  * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with Atomic Tagging. If not, see
  * <http://www.gnu.org/licenses/>.
  */
 package org.atomictagging.shell;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.atomictagging.core.configuration.Configuration;
 import org.atomictagging.shell.commands.EditCommand;
 import org.atomictagging.shell.commands.HelpCommand;
 import org.atomictagging.shell.commands.ICommand;
 import org.atomictagging.shell.commands.ImportCommand;
 import org.atomictagging.shell.commands.ListCommand;
 import org.atomictagging.shell.commands.NewCommand;
 import org.atomictagging.shell.commands.OpenCommand;
 import org.atomictagging.shell.commands.RemoveCommand;
 import org.atomictagging.shell.commands.SetScopeCommand;
 import org.atomictagging.shell.commands.ShowCommand;
 import org.atomictagging.shell.commands.TestDataCommand;
 
 /**
  * A CLI for Atomic Tagging
  * 
  * @author Stephan Mann
  */
 public class Shell implements IShell {
 	/**
 	 * Main entry point if a CLI is to be executed.
 	 * 
 	 * @param args
 	 */
 	public static void main( String[] args ) {
 		Shell shell = new Shell();
 		shell.run();
 	}
 
 	private final Map<String, ICommand>	commands	= new HashMap<String, ICommand>();
 	private final Map<String, String>	environment	= new HashMap<String, String>();
 
 	private static final String			CONF_NAME	= "atomictagging.conf";
 
 
 	/**
 	 * Default constructor
 	 */
 	public Shell() {
 		initConfiguration();
 		initCommands();
 	}
 
 
 	/**
 	 * Initializes configuration by checking various locations for configuration files.
 	 */
 	private void initConfiguration() {
 		boolean foundAnyConfig = false;
 
 		// Local configuration next to the executed JAR.
 		URL locationUrl = getClass().getProtectionDomain().getCodeSource().getLocation();
 		File location = new File( locationUrl.getPath() );
 
 		if ( !location.isDirectory() ) {
 			location = new File( location.getParent() );
 		}
 
 		File localConf = new File( location.getAbsolutePath() + "/" + CONF_NAME );
 		if ( localConf.canRead() ) {
 			try {
 				Configuration.addFile( localConf );
 				foundAnyConfig = true;
 			} catch ( Exception e ) {
 				System.err.println( "Failed to add configuration from " + localConf.getAbsolutePath() );
 				System.err.println( "Cause: " + e.getMessage() );
 				System.err.println( "Trying to proceed without it." );
 			}
 		}
 
 		// User configuration.
 		File userConf = new File( System.getProperty( "user.home" ) + "/.atomictagging/" + CONF_NAME );
 		if ( userConf.canRead() ) {
 			try {
 				Configuration.addFile( userConf );
 				foundAnyConfig = true;
 			} catch ( Exception e ) {
 				System.err.println( "Failed to add configuration from " + userConf.getAbsolutePath() );
 				System.err.println( "Cause: " + e.getMessage() );
 				System.err.println( "Trying to proceed without it." );
 			}
 		}
 
 		// Global configuration.
 		if ( System.getProperty( "os.name" ) == "Linux" ) {
 			File globalConf = new File( "/etc/atomictagging/" + CONF_NAME );
 			if ( globalConf.canRead() ) {
 				try {
 					Configuration.addFile( globalConf );
 					foundAnyConfig = true;
 				} catch ( Exception e ) {
 					System.err.println( "Failed to add configuration from " + globalConf.getAbsolutePath() );
 					System.err.println( "Cause: " + e.getMessage() );
 					System.err.println( "Trying to proceed without it." );
 				}
 			}
 		}
 
 		if ( !foundAnyConfig ) {
 			System.err
					.println( "Could not load any configuration. Please check the manual on how to configure AtomicTagging Shell." );
 			System.exit( 1 );
 		}
 	}
 
 
 	/**
 	 * Initialize all commands.
 	 */
 	private void initCommands() {
 		register( new HelpCommand( this ) );
 		register( new ListCommand( this ) );
 		register( new TestDataCommand( this ) );
 		register( new ShowCommand( this ) );
 		register( new OpenCommand( this ) );
 		register( new ImportCommand( this ) );
 		register( new SetScopeCommand( this ) );
 		register( new EditCommand( this ) );
 		register( new NewCommand( this ) );
 		register( new RemoveCommand( this ) );
 	}
 
 
 	@Override
 	public String getEnvironment( final String key ) {
 		return environment.get( key );
 	}
 
 
 	@Override
 	public void setEnvironment( final String key, final String value ) {
 		if ( key == null || value == null ) {
 			return;
 		}
 
 		environment.put( key, value );
 	}
 
 
 	@Override
 	public void register( final ICommand command ) {
 		commands.put( command.getCommandString(), command );
 	}
 
 
 	@Override
 	public ICommand getCommand( final String commandString ) {
 		if ( commands.containsKey( commandString ) ) {
 			return commands.get( commandString );
 		}
 		return null;
 	}
 
 
 	@Override
 	public Collection<ICommand> getCommands() {
 		return Collections.unmodifiableCollection( commands.values() );
 	}
 
 
 	private void run() {
 		printWelcomeMessage();
 		boolean run = true;
 
 		while ( run ) {
 			printPrompt();
 			String input = readInput();
 
 			if ( input == null || input.isEmpty() ) {
 				continue;
 
 			} else if ( input.trim().equals( "quit" ) || input.trim().equals( "exit" ) ) {
 				run = false;
 
 			} else {
 				delegate( input );
 			}
 		}
 
 		printGoodByeMessage();
 	}
 
 
 	private void delegate( String input ) {
 		String[] parts = input.trim().split( " ", 2 );
 		String command = parts[0];
 
 		String params = "";
 		if ( parts.length == 2 ) {
 			params = parts[1];
 		}
 
 		if ( command.isEmpty() ) {
 			return;
 		}
 
 		if ( commands.containsKey( command ) ) {
 			commands.get( command ).handleInput( params, System.out );
 		} else {
 			System.out.println( "Command \"" + command + "\" not found." );
 		}
 	}
 
 
 	private static String readInput() {
 		BufferedReader br = new BufferedReader( new InputStreamReader( System.in ) );
 		String input = null;
 
 		try {
 			input = br.readLine();
 		} catch ( IOException ioe ) {
 			System.err.println( "IO error trying to read user input." );
 			System.exit( 1 );
 		}
 
 		return input;
 	}
 
 
 	private void printPrompt() {
 		String scope = getEnvironment( "scope" );
 		scope = ( scope == null ) ? "" : scope;
 		System.out.print( scope + "> " );
 	}
 
 
 	private static void printWelcomeMessage() {
		System.out.println( "Welcome to AtomicTagging Shell version 0.0.4. Type 'help' to get startet.\n\n"
 				+ "This program comes with ABSOLUTELY NO WARRANTY. It is free software,\n"
 				+ "and you are welcome to redistribute it under the terms of GPLv3.\n"
 				+ "For more details see COPYING or <http://www.gnu.org/licenses/>." );
 	}
 
 
 	private static void printGoodByeMessage() {
 		System.out.println( "Bye." );
 	}
 
 }
