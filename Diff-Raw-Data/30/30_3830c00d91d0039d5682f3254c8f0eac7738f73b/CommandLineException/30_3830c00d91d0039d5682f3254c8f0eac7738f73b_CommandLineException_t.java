 /*
 * utils - CommandLineException.java - Copyright © 2008-2009 David Roden
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
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package net.pterodactylus.util.cmdline;
 
 /**
  * Exception that signals an error in command-line argument parsing.
  *
  * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
  */
 public class CommandLineException extends Exception {
 
 	/**
 	 * Creates a new command-line exception.
 	 */
 	public CommandLineException() {
 		super();
 	}
 
 	/**
 	 * Creates a new command-line exception with the given message.
 	 *
 	 * @param message
 	 *            The message of the exception
 	 */
 	public CommandLineException(String message) {
 		super(message);
 	}
 
 	/**
 	 * Creates a new command-line exception with the given cause.
 	 *
 	 * @param cause
 	 *            The cause of the exception
 	 */
 	public CommandLineException(Throwable cause) {
 		super(cause);
 	}
 
 	/**
 	 * Creates a new command-line exception with the given message and cause.
 	 *
 	 * @param message
 	 *            The message of the exception
 	 * @param cause
 	 *            The cause of the exception
 	 */
 	public CommandLineException(String message, Throwable cause) {
 		super(message, cause);
 	}
 
 }
