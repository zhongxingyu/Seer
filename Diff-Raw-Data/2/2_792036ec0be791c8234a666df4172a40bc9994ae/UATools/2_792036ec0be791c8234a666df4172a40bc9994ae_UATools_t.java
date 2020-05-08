 package com.ijg.darklight.sdk.utils;
 
 import java.io.IOException;
 
 /*
  * Copyright (C) 2013  Isaac Grant
  * 
  * This file is part of the Darklight Nova Core.
  *  
  * Darklight Nova Core is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Darklight Nova Core is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with the Darklight Nova Core.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 /**
  * User Account Tools for Windows systems
  * @author Isaac Grant
  */
 public class UATools {
 	
 	/**
 	 * Check if an account exists or not using the net user command
 	 * @param account The username to check for
 	 * @return Whether or not a user with the username exists
 	 */
 	public static boolean accountExists(String account) {
 		try {
 			Process p = Runtime.getRuntime().exec("cmd.exe /c net user");
 			p.waitFor();
 			String output = FileLoader.loadFromInputStream(p.getInputStream());
 			if (output.contains("The command completed successfully")) {
 				if (output.contains(account))
 					return true;
 			}
 		} catch (IOException | InterruptedException e) {}
 		return false;
 	}
 	
 	/**
 	 * Get an account property output from the net user <username> command
 	 * @param account The account to check
 	 * @param property The property to retrieve
 	 * @return The value of the given property, or empty string if there was an error
 	 */
 	public static String getAccountProperty(String account, String property) {
 		try {
 			Process p = Runtime.getRuntime().exec("cmd.exe /c net user " + account);
 			p.waitFor();
 			String output = FileLoader.loadFromInputStream(p.getInputStream());
 			
 			int propIndex = output.indexOf(property);
 			String line = output.substring(propIndex, output.substring(propIndex).indexOf("\n") + propIndex);
 			String value = line.substring(property.length()).trim();
 			return value;
 		} catch (IOException | InterruptedException e) {}
 		return "";
 	}
 	
 	/**
 	 * Check if an account is active, using {@link #getAccountProperty(String, String)}
 	 * @param account The account to check
 	 * @return True if the account is active
 	 */
	public static boolean accountActive(String account) {
 		if (getAccountProperty(account, "Account active").equals("Yes"))
 			return true;
 		return false;
 	}
 }
