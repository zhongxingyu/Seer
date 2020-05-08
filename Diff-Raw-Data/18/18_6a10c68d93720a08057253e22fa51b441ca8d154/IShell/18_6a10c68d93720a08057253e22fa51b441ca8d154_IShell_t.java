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
 
 import java.util.Collection;
 
 import org.atomictagging.shell.commands.ICommand;
 
 /**
  * Interface for a CLI running on an Atomic Tagging environment
  * 
  * @author Stephan Mann
  */
 public interface IShell {
 	/**
 	 * Registers a command in the shell. Only registered commands can be executed.
 	 * 
 	 * @param command
 	 */
 	void register( ICommand command );
 
 
 	/**
	 * Returns the current value of the specified environment variable.
 	 * 
 	 * @param key
 	 * @return The value or null if no value was set for this key
 	 */
 	String getEnvironment( String key );
 
 
 	/**
 	 * Write a value to the shells environment. Existing keys will be overwritten.
 	 * 
 	 * @param key
 	 * @param value
 	 */
 	void setEnvironment( String key, String value );
 
 
 	/**
	 * Returns the command that was registered using the given key.
 	 * 
 	 * @param commandString
 	 * @return The command or null if no command was registered under this key
 	 */
 	ICommand getCommand( String commandString );
 
 
 	/**
	 * Returns all registered commands.
 	 * 
 	 * @return An unmodifiable list of all registered commands. Will be a new list every time it is called.
 	 */
 	Collection<ICommand> getCommands();
 }
