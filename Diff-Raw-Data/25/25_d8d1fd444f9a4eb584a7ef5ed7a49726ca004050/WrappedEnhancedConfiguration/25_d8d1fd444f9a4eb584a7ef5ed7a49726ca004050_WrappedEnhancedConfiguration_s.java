 /*******************************************************************************
  * Copyright (c) 2013 Travis Ralston.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser Public License v2.1
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
  * 
  * Contributors:
  * turt2live (Travis Ralston) - initial API and implementation
  ******************************************************************************/
 package com.turt2live.antishare.util;
 
 import java.io.File;
 
 import org.bukkit.plugin.Plugin;
 
 import com.feildmaster.lib.configuration.EnhancedConfiguration;
 
 /**
  * Temporary fix for missing methods
  */
public class WrappedEnhancedConfiguration extends EnhancedConfiguration {
 
 	public WrappedEnhancedConfiguration(File file, Plugin plugin){
 		super(file, plugin);
 	}
 
 	public WrappedEnhancedConfiguration(File file){
 		super(file);
 	}
 
 	/**
 	 * Clears the configuration. This does not save the file.
 	 */
 	public void clearFile(){
 		clearCache();
 		map.clear();
 	}
 
 }
