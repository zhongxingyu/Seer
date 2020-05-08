 /**
  * Copyright (C) 2013 Colorado School of Mines
  *
  * This file is part of the Interface Software Development Kit (SDK).
  *
  * The InterfaceSDK is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * The InterfaceSDK is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with the InterfaceSDK.  If not, see <http://www.gnu.org/licenses/>.
  */
 package edu.mines.acmX.exhibit.module_management.module_executors;
 
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

 import edu.mines.acmX.exhibit.module_management.metas.ModuleMetaData;
 
 public abstract class ModuleExecutor {
 
	static Logger logger = LogManager.getLogger(ModuleExecutor.class.getName());
 
	protected String moduleParentDirectory;
	protected ModuleMetaData moduleData;
	protected String fullyQualifiedModuleName;
	protected String jarPath;
 	
 	public ModuleExecutor(String fullyQualifiedModuleName, String jarPath) {
 		this.fullyQualifiedModuleName = fullyQualifiedModuleName;
 		this.jarPath = jarPath;
 	}
 
 	public abstract void run() throws ModuleRuntimeException;
 
 }
 
 
