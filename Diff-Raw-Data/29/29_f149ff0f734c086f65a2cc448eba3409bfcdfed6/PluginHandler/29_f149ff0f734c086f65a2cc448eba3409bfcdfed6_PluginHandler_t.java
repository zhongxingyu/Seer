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
 
 public class PluginHandler {
 	private Plugin[] plugins;
 	
 	/**
 	 * @param plugins Loaded plugins
 	 */
 	void setPlugins(Plugin[] plugins) {
 		this.plugins = plugins;
 	}
 	
 	/**
 	 * Start all loaded plugins
 	 */
 	public void startAll() {
 		for (Plugin plugin : plugins) {
 			plugin.start();
 		}
 	}
 	
 	/**
 	 * Kill all loaded plugins
 	 */
 	public void killAll() {
 		for (Plugin plugin : plugins) {
 			plugin.kill();
 		}
 	}
 }
