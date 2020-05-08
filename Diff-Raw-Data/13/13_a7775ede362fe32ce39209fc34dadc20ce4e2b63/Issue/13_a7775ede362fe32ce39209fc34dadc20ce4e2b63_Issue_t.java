 package com.ijg.darklight.sdk.core;
 
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
 * Issues should be subclassed from this, and implement the following methods:<br />
 * <ul><li>install</li><li>isFixed</li></ul>
 * loadSettings may also be implemented <strong>IF</strong> the issue being created
 * will load settings from the config.json file, and should be designed accordingly.
 * @author Isaac Grant
 */

 public abstract class Issue {
 	private String name;
 	private String description;
 	
	protected Issue(String name, String description) {
 		this.name = name;
 		this.description = description;
 	}
 	
 	
 	/**
 	 * Method to install/create this issue on target machine,
 	 * should be overriden.
 	 */
 	public static void install() {}
 	
 	/**
 	 * If this issue implements any settings found in the config.json
 	 * file, then they are loaded here
	 * @see com.ijg.darklight.sdk.core.Settings
 	 */
 	protected abstract void loadSettings();
 	
 	/**
 	 * Check whether or not this issue is fixed, the content
 	 * of this function will depend on how to check the status
 	 * of a given issue
 	 * @return If this issue has been fixed or not
 	 */
 	public abstract boolean isFixed();
 	
 	/**
 	 * 
 	 * @return The name of this issue
 	 */
 	public String getName() {
 		return name;
 	}
 	
 	/**
 	 * 
 	 * @return The description of this issue
 	 */
 	public String getDescription() {
 		return description;
 	}
 }
