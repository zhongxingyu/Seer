 /*******************************************************************************
  * Copyright (c) 2011 Petri Tuononen and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Petri Tuononen - Initial implementation
  *******************************************************************************/
 package org.eclipse.cdt.managedbuilder.pkgconfig.util;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.cdt.core.model.CoreModel;
 import org.eclipse.cdt.core.settings.model.ICProjectDescription;
 import org.eclipse.cdt.managedbuilder.core.BuildException;
 import org.eclipse.cdt.managedbuilder.core.IConfiguration;
 import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
 import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
 import org.eclipse.cdt.managedbuilder.core.IManagedProject;
 import org.eclipse.cdt.managedbuilder.core.IOption;
 import org.eclipse.cdt.managedbuilder.core.IOptionCategory;
 import org.eclipse.cdt.managedbuilder.core.ITool;
 import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 
 /**
  * Add include and library search paths and libraries to Tool's (compiler, linker) options.
  * 
  * TODO: Modify so that the Tool Options are added to the selected build configuration.
  * 		 IF all configurations is selected then add Tool Options to all configurations.
  */
 public class PathToToolOption {
 
 	//tool input extensions
 	private static final String linkerInputType = "o"; //$NON-NLS-1$
 	private static final String[] inputTypes = {"cpp", "c"};  //$NON-NLS-1$ //$NON-NLS-2$
 	//tool option values
 	public static final int INCLUDE = 1;
 	public static final int LIB = 2;
 	public static final int LIB_PATH = 3;
 
 	/**
 	 * Adds new include path to Compiler's Include path option.
 	 * 
 	 * @param includePath Include path to be added to Compiler's Include Option 
 	 */
 	public static void addIncludePath(String includePath, IProject proj) {
 		if (proj != null) {
 			addPathToToolOption(includePath, INCLUDE, proj);
 		}
 	}
 
 	/**
 	 * Removes an include path from Compiler's Include path option. 
 	 * 
 	 * @param includePath Include path to be removed from Compiler's Include Option 
 	 */
 	public static void removeIncludePath(String includePath, IProject proj) {
 		if (proj != null) {
 			removePathFromToolOption(includePath, INCLUDE, proj);
 		}
 	}
 
 	/**
 	 * Adds a new Library to Linker's Libraries Option.
 	 * 
 	 * @param lib Library name to be added to Linker's Libraries Option
 	 */
 	public static void addLib(String lib, IProject proj) {
 		if (proj != null) {
 			addPathToToolOption(lib, LIB, proj);	
 		}
 	}
 
 	/**
 	 * Removes a Library from Linker's Libraries Option.
 	 * 
 	 * @param lib Library name to be removed from Linker's Libraries Option
 	 */
 	public static void removeLib(String lib, IProject proj) {
 		if (proj != null) {
 			removePathFromToolOption(lib, LIB, proj);
 		}
 	}
 
 	/**
 	 * Adds a new Library search path to Linker's Library search path Option.
 	 * 
 	 * @param libDir Library search path to be added to Linker's Library search path Option
 	 */
 	public static void addLibraryPath(String libDir, IProject proj) {
 		if (proj != null) {
 			addPathToToolOption(libDir, LIB_PATH, proj);
 		}
 	}
 
 	/**
 	 * Removes a Library search path from Linker's Library search path Option.
 	 * 
 	 * @param libDir Library search path to be removed from Linker's Library search path Option
 	 */	
 	public static void removeLibraryPath(String libDir, IProject proj) {
 		if (proj != null) {
 			removePathFromToolOption(libDir, LIB_PATH, proj);
 		}
 	}
 
 	/**
 	 * Adds a path to Tool's option.
 	 * 
 	 * @param path Path to be added to Tool's option
 	 * @param var Tool option's value
 	 */
 	private static void addPathToToolOption(String path, int var, IProject proj) {
 		//check if the given path exists
 		if (path.length()>0 && (pathExists(path) || var==LIB)) {
 			boolean success = false;
 //			IConfiguration[] configs;
 			//get all build configurations of the IProject
 //			configs = getAllBuildConfigs(proj);
 			//if build configurations found
 //			if (configs.length>0) {
 //				for (IConfiguration cf : configs) {
 			IConfiguration cf = getCurrentBuildConf(proj);
 			if (cf != null) {
 				//Add path for the Tool's option
 				if (addPathToSelectedToolOptionBuildConf(cf, path, var)) {
 					success = true;
 				} else {
 					success = false;
 				}						
 			}
 //				}
 				//if the path was added successfully
 			if (success) {
 				//save project build info
 //				ManagedBuildManager.saveBuildInfo(proj, true); //call moved to property ptab class to reduce overhead
 			}
 //			}
 		}
 	}
 
 	/**
 	 * Removes a path from Tool's option.
 	 * 
 	 * @param path Path to be removed from Tool's option
 	 * @param var Tool option's value
 	 */
 	private static void removePathFromToolOption(String path, int var, IProject proj) {
 		//check if the given path exists
		if (path.length()>0 && pathExists(path)) {
 			boolean success = false;
 //			IConfiguration[] configs;
 			//get all build configurations of the IProject
 //			configs = getAllBuildConfigs(proj);
 			//if build configurations found
 //			if (configs.length>0) {
 //				for (IConfiguration cf : configs) {
 			IConfiguration cf = getCurrentBuildConf(proj);
 			//remove a path from the Tool's option
 			if (removePathFromSelectedToolOptionBuildConf(cf, path, var)) {
 				success = true;
 			} else {
 				success = false;
 			}
 //				}
 			//if the path was removed successfully
 			if (success) {
 				//save project build info
 //				ManagedBuildManager.saveBuildInfo(proj, true); //call moved to property ptab class to reduce overhead
 			}
 //			}
 		}
 	}
 
 	/**
 	 * Add a path to specific build configuration's Tool option. 
 	 * 
 	 * @param cf Build configuration
 	 * @param path Path or file name to add
 	 * @param var Value of the option type
 	 * @return boolean True if path was added successfully
 	 */
 	private static boolean addPathToSelectedToolOptionBuildConf(IConfiguration cf, String path, int var) {
 		switch (var) {
 		case INCLUDE:
 			return addIncludePathToToolOption(cf, path);
 		case LIB:
 			return addLibToToolOption(cf, path);
 		case LIB_PATH:
 			return addLibSearchPathToToolOption(cf, path);
 		default:
 			return false;
 		}
 	}
 
 	/**
 	 * Removes a path from specific build configuration's Tool option. 
 	 * 
 	 * @param cf Build configuration
 	 * @param path Path or file name to remove
 	 * @param var Value of the option type
 	 * @return boolean True if path was removed successfully
 	 */
 	private static boolean removePathFromSelectedToolOptionBuildConf(IConfiguration cf, String path, int var) {
 		switch (var) {
 		case INCLUDE:
 			return removeIncludePathFromToolOption(cf, path);
 		case LIB:
 			return removeLibFromToolOption(cf, path);
 		case LIB_PATH:
 			return removeLibSearchPathFromToolOption(cf, path);
 		default:
 			return false;
 		}
 	}
 
 	/**
 	 * Returns all projects in the workspace.
 	 * 
 	 * @return IProject[]
 	 */
 	public static IProject[] getProjectsInWorkspace() {
 		//get workspace
 		IWorkspace root = ResourcesPlugin.getWorkspace();
 		//get all projects in the workspace
 		return root.getRoot().getProjects();
 	}
 
 	/**
 	 * Returns all build configurations of the project.
 	 * 
 	 * @param proj IProject Project
 	 * @return IConfiguration[] Build configurations
 	 */
 	private static IConfiguration[] getAllBuildConfigs(IProject proj) {
 		IConfiguration[] configurations = new IConfiguration[] {};
 		IManagedBuildInfo info = null;
 		//try to get Managed build info
 		try {
 			info = ManagedBuildManager.getBuildInfo(proj); //null if doesn't exists
 		} catch (Exception e) { //if not a managed build project
 			//print error
 			e.printStackTrace();
 			return configurations;
 		}
 		//info can be null for projects without build info. For example, when creating a project
 		//from Import -> C/C++ Executable
 		if(info == null) {
 			return configurations;
 		}
 		//get ManagedProject associated with build info
 		IManagedProject mProj = info.getManagedProject();
 
 		//get all build configurations of the project
 		configurations = mProj.getConfigurations();
 		return configurations;
 	}
 
 	/**
 	 * Adds an include path to Compiler's Include path Option.
 	 * 
 	 * @param cf IConfiguration Build configuration
 	 * @param newIncludePath Include path to be added to Compiler's Include path Option
 	 */
 	private static boolean addIncludePathToToolOption(IConfiguration cf, String newIncludePath) {
 		//get front-end
 		ITool frontEnd = getCompiler(cf);
 		//If the front-end is found from the given build configuration
 		if (frontEnd != null) {
 			//get front-end's Include paths option.
 			IOption frontEndIncPathOption = getCompilerIncludePathOption(cf);
 			//add a new include path to front-end's Include paths option.
 			boolean val = addIncludePathToToolOption(cf, frontEnd, frontEndIncPathOption, newIncludePath);
 			return val;
 		} 
 		return false;
 	}
 
 	/**
 	 * Removes an include path from Compiler's Include path Option.
 	 * 
 	 * @param cf IConfiguration Build configuration
 	 * @param removeIncludePath Include path to be removed from Compiler's Include path Option
 	 */
 	private static boolean removeIncludePathFromToolOption(IConfiguration cf, String removeIncludePath) {
 		//get front-end
 		ITool frontEnd = getCompiler(cf);
 		//If the front-end is found from the given build configuration
 		if (frontEnd != null) {
 			//get front-end's Include paths option.
 			IOption frontEndIncPathOption = getCompilerIncludePathOption(cf);
 			//remove an include path from front-end's Include paths option.
 			removeIncludePathFromToolOption(cf, frontEnd, frontEndIncPathOption, removeIncludePath);
 			return true;
 		} 
 		return false;
 	}
 
 	/**
 	 * Adds a Library to Linker's Libraries Option.
 	 * 
 	 * @param cf IConfiguration Build configuration
 	 * @param lib Library name
 	 * @return boolean Returns true if Library Option was added successfully to the Linker.
 	 */
 	private static boolean addLibToToolOption(IConfiguration cf, String lib) {
 		//get linker
 		ITool linker = getLinker(cf);
 		//If the linker is found from the given build configuration
 		if (linker != null) {
 			//get Linker's Libraries option
 			IOption librariesOption = getLinkerLibrariesOption(cf);
 			//add library to Linker's Libraries Option type
 			boolean val = addLibraryToToolOption(cf, linker, librariesOption, lib);
 			return val;
 		} 
 		//adding the library failed
 		return false;
 	}
 
 	/**
 	 * Removes a Library from Linker's Libraries Option.
 	 * 
 	 * @param cf IConfiguration Build configuration
 	 * @param removeLib Library name
 	 * @return boolean Returns true if Library Option was removed successfully from the Linker.
 	 */
 	private static boolean removeLibFromToolOption(IConfiguration cf, String removeLib) {
 		//get linker
 		ITool linker = getLinker(cf);
 		//If the Linker is found from the given build configuration
 		if (linker != null) {
 			//get Linker's Libraries option
 			IOption librariesOption = getLinkerLibrariesOption(cf);
 			//remove a library from linker's Libraries Option type
 			removeLibraryFromToolOption(cf, linker, librariesOption, removeLib);
 			return true;
 		} 
 		//removing the library failed
 		return false;
 	}
 
 	/**
 	 * Adds a Library search path to Linker's Library search path Option.
 	 * 
 	 * @param cf IConfiguration Build configuration
 	 * @param libDir Library search path
 	 * @return boolean Returns true if Library search path Option was added successfully to the Linker.
 	 */
 	private static boolean addLibSearchPathToToolOption(IConfiguration cf, String libDir) {
 		//get linker
 		ITool linker = getLinker(cf);
 		//If the linker is found from the given build configuration
 		if (linker != null) {
 			//get Linker's Library search path option
 			IOption libDirOption = getLinkerLibrarySearchPathOption(cf);
 			//add library search path to linker's Library Search Path Option type
 			boolean val = addLibrarySearchPathToToolOption(cf, linker, libDirOption, libDir);
 			return val;
 		} 
 		//adding library failed
 		return false;
 	}
 
 	/**
 	 * Removes a Library search path from Linker's Library search path Option.
 	 * 
 	 * @param cf IConfiguration Build configuration
 	 * @param removeLibDir Library search path
 	 * @return boolean Returns true if Library search path Option was removed successfully from the Linker.
 	 */
 	private static boolean removeLibSearchPathFromToolOption(IConfiguration cf, String removeLibDir) {
 		//get linker
 		ITool linker = getLinker(cf);
 		//If the linker is found from the given build configuration
 		if (linker != null) {
 			//get Linker's Library search path option
 			IOption libDirOption = getLinkerLibrarySearchPathOption(cf);
 			//remove a library search path from linker's Library Search Path Option type
 			removeLibrarySearchPathFromToolOption(cf, linker, libDirOption, removeLibDir);
 			return true;
 		} 
 		//removing the library search path failed
 		return false;
 	}
 
 	/**
 	 * Adds include path for given Build configuration's Tool's Include path Option.
 	 * 
 	 * @param cf IConfiguration Build configuration
 	 * @param cfTool ITool Tool
 	 * @param option Tool Option type
 	 * @param newIncludePath Include path to be added to Tool's Include path option
 	 */
 	private static boolean addIncludePathToToolOption(IConfiguration cf, ITool cfTool, IOption option, String newIncludePath) {
 		try {
 			//add path only if it does not exists
 			String[] incPaths = option.getIncludePaths();
 			for (String inc : incPaths) {
 				if (inc.equalsIgnoreCase(newIncludePath)) {
 					return false;
 				}
 			}
 			//add a new include path to linker's Include paths option.
 			addInputToToolOption(cf, cfTool, option, newIncludePath, incPaths);
 		} catch (BuildException e) {
 			//show error
 			e.printStackTrace();
 		}
 		return true;
 	}
 
 	/**
 	 * Removes an include path from given Build configuration's Tool's Include path Option.
 	 * 
 	 * @param cf IConfiguration Build configuration
 	 * @param cfTool ITool Tool
 	 * @param option Tool Option type
 	 * @param removeIncludePath Include path to be removed from Tool's Include path option
 	 */
 	private static void removeIncludePathFromToolOption(IConfiguration cf, ITool cfTool, IOption option, String removeIncludePath) {
 		try {
 			//remove an include path from linker's Include paths option.
 			removeInputFromToolOption(cf, cfTool, option, removeIncludePath, option.getIncludePaths());
 		} catch (BuildException e) {
 			//show error
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Adds new Library for the Linker's Libraries Option.
 	 * 
 	 * @param cf IConfiguration Build configuration
 	 * @param cfTool ITool Tool
 	 * @param option Tool Option type
 	 * @param newLibrary Library
 	 */
 	private static boolean addLibraryToToolOption(IConfiguration cf, ITool cfTool, IOption option, String newLibrary) {
 		try {
 			//add library only if it does not exists
 			String[] libraries = option.getLibraries();
 			for (String lib : libraries) {
 				if (lib.equalsIgnoreCase(newLibrary)) {
 					return false;
 				}
 			}
 			//add a new library to linker's Libraries option.
 			addInputToToolOption(cf, cfTool, option, newLibrary, libraries);
 		} catch (BuildException e) {
 			//show error
 			e.printStackTrace();
 		}
 		return true;
 	}
 
 	/**
 	 * Removes a new Library from the Linker's Libraries Option.
 	 * 
 	 * @param cf IConfiguration Build configuration
 	 * @param cfTool ITool Tool
 	 * @param option Tool Option type
 	 * @param removeLibrary Library
 	 */
 	private static void removeLibraryFromToolOption(IConfiguration cf, ITool cfTool, IOption option, String removeLibrary) {
 		try {
 			//remove a library from linker's Libraries option.
 			removeInputFromToolOption(cf, cfTool, option, removeLibrary, option.getLibraries());
 		} catch (BuildException e) {
 			//show error
 			e.printStackTrace();
 		}
 	}
 
 	//Works only if Eclipse Bugzilla Bug 321040 fix is applied (since CDT 8.0)
 	/**
 	 * Adds new Library search path for the Linker's Library search path Option.
 	 * 
 	 * @param cf IConfiguration Build configuration
 	 * @param cfTool ITool Tool
 	 * @param option Tool Option type
 	 * @param newLibraryPath Library search path
 	 */
 	private static boolean addLibrarySearchPathToToolOption(IConfiguration cf, ITool cfTool, IOption option, String newLibraryPath) {
 		try {
 			//add path only if it does not exists
 			String[] libPaths = option.getLibraryPaths();
 			for (String libPath : libPaths) {
 				if (libPath.equalsIgnoreCase(newLibraryPath)) {
 					return false;
 				}
 			}
 			//add a new library path to linker's Library search path option.
 			addInputToToolOption(cf, cfTool, option, newLibraryPath, libPaths);
 		} catch (BuildException e) {
 			//show error
 			e.printStackTrace();
 		}
 		return true;
 	}
 
 	//Works only if Eclipse Bugzilla Bug 321040 fix is applied (since CDT 8.0)
 	/**
 	 * Removes a Library search path from the Linker's Library search path Option.
 	 * 
 	 * @param cf IConfiguration Build configuration
 	 * @param cfTool ITool Tool
 	 * @param option Tool Option type
 	 * @param removeSearchPath Library search path
 	 */
 	private static void removeLibrarySearchPathFromToolOption(IConfiguration cf, ITool cfTool, IOption option, String removeSearchPath) {
 		try {
 			//remove a library path from linker's Library search path option.
 			removeInputFromToolOption(cf, cfTool, option, removeSearchPath, option.getLibraryPaths());
 		} catch (BuildException e) {
 			//show error
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Add other flag to compiler Option.
 	 * TODO: Added flags vanish after a restart,
 	 * 
 	 * @param otherFlag String
 	 * @param proj IProject
 	 */
 	public static void addOtherFlag(String otherFlag, IProject proj) {
 		IConfiguration cf = getCurrentBuildConf(proj);
 		if (cf != null) {
 			ITool frontEnd = getCompiler(cf);
 			IOption option = frontEnd.getOptionById("gnu.c.compiler.option.misc.other");
 		
 			//if option type for other flags found from the compiler
 			if (option!=null) {
 				String flags = (String) option.getValue();
 				if (flags == null) {
 					flags = "";
 				}
 				
 				//append the new flag to existing flags
 				flags = flags+" "+otherFlag;
 				
 				try {
 					option.setValue(flags);
 				} catch (BuildException e) {
 					e.printStackTrace();
 				}
 				ManagedBuildManager.setOption(cf, frontEnd, option, flags);
 				ManagedBuildManager.saveBuildInfo(proj, true);
 			}
 		}
 	}
 	
 	/**
 	 * Add other flag to compiler Option.
 	 * TODO: Added flags vanish after a restart,
 	 * 
 	 * @param otherFlag String
 	 * @param proj IProject
 	 */
 	public static void addOtherFlag2(String otherFlag, IProject proj) {
 		IConfiguration cf = getCurrentBuildConf(proj);
 		if (cf != null) {
 			ITool frontEnd = getCompiler(cf);
 			IOption option = frontEnd.getOptionById("gnu.c.compiler.option.misc.other");
 			IOptionCategory category = frontEnd.getOptionCategory("gnu.c.compiler.category.other");
 		
 			//if option type for other flags found from the compiler
 			if (option!=null) {
 				String flags = (String) option.getValue();
 				if (flags == null) {
 					flags = "";
 				}
 				
 				//append the new flag to existing flags
 				flags = flags+" "+otherFlag;
 				
 				try {
 					option.setValue(flags);
 				} catch (BuildException e) {
 					e.printStackTrace();
 				}
 				ICProjectDescription desc = CoreModel.getDefault().getProjectDescription(proj);
 				IConfiguration configuration = ManagedBuildManager.getConfigurationForDescription(desc.getActiveConfiguration());
 				IHoldsOptions optionsHolder = category.getOptionHolder();
 				try {
 					configuration.setOption(optionsHolder, option, flags);
 				} catch (BuildException e1) {
 					e1.printStackTrace();
 				}
 				try {
 					CoreModel.getDefault().setProjectDescription(proj, desc);
 				} catch (CoreException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Remove other flag from compiler Option.
 	 * 
 	 * @param otherFlag String
 	 * @param proj IProject
 	 */
 	public static void removeOtherFlag(String otherFlag, IProject proj) {
 		IConfiguration cf = getCurrentBuildConf(proj);
 		if (cf != null) {
 			ITool frontEnd = getCompiler(cf);
 			IOption option = frontEnd.getOptionById("gnu.c.compiler.option.misc.other");
 
 			String flags = (String) option.getValue();
 			if (flags == null) {
 				flags = "";
 			}
 
 			//remove otherFlag String if found
 			if (flags.contains(otherFlag)) {
 				flags = flags.replace(" "+otherFlag, "");
 			}
 			
 			try {
 				option.setValue(flags);
 			} catch (BuildException e) {
 				e.printStackTrace();
 			}
 			ManagedBuildManager.setOption(cf, frontEnd, option, flags);
 		}
 	}
 	
 	/**
 	 * Adds a new value to specific Option.
 	 * 
 	 * @param cf IConfiguration Build configuration
 	 * @param cfTool ITool Tool
 	 * @param option Tool Option type
 	 * @param newValue New value to be added to the Option type
 	 * @param existingValues Existing Option type values
 	 */
 	private static void addInputToToolOption(IConfiguration cf, ITool cfTool, IOption option, String newValue, String[] existingValues) {
 		//if Option type is found
 		if (option != null) {
 			//append new value with existing values
 			String[] newValues = addNewPathToExistingPathList(existingValues, newValue);
 			//set new values array for the option for the given build configuration
 			ManagedBuildManager.setOption(cf, cfTool, option, newValues);
 		} else{
 			//log error
 		}
 	}
 
 	/**
 	 * Removes a value from a specific Option.
 	 * 
 	 * @param cf IConfiguration Build configuration
 	 * @param cfTool ITool Tool
 	 * @param option Tool Option type
 	 * @param removeValue Value to be removed from the Option type
 	 * @param existingValues Existing Option type values
 	 */
 	private static void removeInputFromToolOption(IConfiguration cf, ITool cfTool, IOption option, String removeValue, String[] existingValues) {
 		//if Option type is found
 		if (option != null) {
 			//check that list has values
 			if(existingValues.length>0) {
 				//remove value from existing values
 				String[] newValues = removePathFromExistingPathList(existingValues, removeValue);
 				//set new values array for the option for the given build configuration
 				ManagedBuildManager.setOption(cf, cfTool, option, newValues);
 			}
 		} else{
 			//log error
 		}
 	}
 
 	/**
 	 * Return compiler according to the input type.
 	 * @param cf IConfiguration Build configuration
 	 * @return ITool Compiler
 	 */
 	private static ITool getCompiler(IConfiguration cf) {
 		//get compiler according to the input type
 		for(int i=0; i<inputTypes.length; i++) {
 			ITool tool = getIToolByInputType(cf, inputTypes[i]);
 			if (tool != null) {
 				return tool;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Returns linker.
 	 * 
 	 * @param cf IConfiguration Build configuration
 	 * @return ITool linker
 	 */
 	private static ITool getLinker(IConfiguration cf) {
 		return getIToolByInputType(cf, linkerInputType);
 	}
 
 	/**
 	 * Returns ITool associated with the input extension.
 	 * 
 	 * @param cf IConfiguration Build configuration
 	 * @param ext input extension associated with ITool
 	 * @return ITool Tool that matches input extension
 	 */
 	private static ITool getIToolByInputType(IConfiguration cf, String ext) {
 		//get ITool associated with the input extension
 		return cf.getToolFromInputExtension(ext);
 	}
 
 	/**
 	 * Returns compiler Include path Option type.
 	 * 
 	 * @param cf IConfiguration Project build configuration
 	 * @return IOption Tool option type
 	 */
 	private static IOption getCompilerIncludePathOption(IConfiguration cf) {
 		//get compiler
 		ITool cfTool = getCompiler(cf);
 		//get option id for include paths
 		String includeOptionId = getOptionId(cfTool, IOption.INCLUDE_PATH);
 		return getToolOptionType(cfTool, includeOptionId);
 	}
 
 	/**
 	 * Returns Linker Libraries Option type.
 	 * 
 	 * @param cf IConfiguration Project build configuration
 	 * @return IOption Tool option type
 	 */
 	private static IOption getLinkerLibrariesOption(IConfiguration cf) {
 		//get linker
 		ITool cfTool = getLinker(cf);
 		//get option id for libraries
 		String libOptionId = getOptionId(cfTool, IOption.LIBRARIES);
 		return getToolOptionType(cfTool, libOptionId);
 	}
 
 	/**
 	 * Returns Linker Library search path Option type.
 	 * 
 	 * @param cf IConfiguration Project build configuration
 	 * @return IOption Tool option type
 	 */
 	private static IOption getLinkerLibrarySearchPathOption(IConfiguration cf) {
 		//get ITool associated with the input extension
 		ITool cfTool = getLinker(cf);
 		//get option id for library paths
 		String libDirOptionId = getOptionId(cfTool, IOption.LIBRARY_PATHS);
 		return getToolOptionType(cfTool, libDirOptionId);
 	}
 
 	/**
 	 * Returns Tool's option id.
 	 * 
 	 * @param cfTool ITool Tool
 	 * @param optionValueType Option's value type.
 	 * @return optionId Tool's option id.
 	 */
 	private static String getOptionId(ITool cfTool, int optionValueType) {
 		String optionId = null;
 		//get all Tool options.
 		IOption[] options = cfTool.getOptions();
 		for (IOption opt : options) {
 			try {
 				//try to match option value type
 				if(opt.getValueType()==optionValueType) {
 					//get option id
 					optionId = opt.getId();
 					break;
 				}
 			} catch (BuildException e) {
 				//log error
 			}
 		}	
 		return optionId;
 	}
 
 	/**
 	 * Returns Tool's Option type by Id.
 	 * 
 	 * @param cfTool ITool Tool
 	 * @param optionId String Tool option type id
 	 * @return IOption Tool option type
 	 */
 	private static IOption getToolOptionType(ITool cfTool, String optionId) {
 		//get option type with specific id for the ITool
 		return cfTool.getOptionById(optionId);
 	}
 
 	/**
 	 * Adds one or more paths to the list of paths.
 	 * 
 	 * @param existingPaths Existing list of paths to add to
 	 * @param newPath New path to add. May include multiple directories with a path delimiter java.io.File.pathSeparator 
 	 * (usually semicolon (Win) or colon (Linux/Mac), OS specific)
 	 * @return String[] List that includes existing paths as well as new paths.
 	 */
 	public static String[] addNewPathToExistingPathList(String[] existingPaths, String newPath) {
 		String pathSep = java.io.File.pathSeparator;  // semicolon for windows, colon for Linux/Mac
 		List<String> newPathList = new ArrayList<String>();
 		String path;
 		//adds existing paths to new paths list
 		for (int i = 0; i < existingPaths.length; i++) {
 			path = existingPaths[i];
 			newPathList.add(path);
 		}
 		//separates new path if it has multiple paths separated by a path separator
 		String[] newPathArray = newPath.split(pathSep);
 		for (int i = 0; i < newPathArray.length; i++) {
 			path = newPathArray[i];
 			newPathList.add(path);
 		}
 		//creates a new list that includes all existing paths as well as new paths
 		String[] newArray = newPathList.toArray(new String[0]);
 		return newArray;
 	}
 
 	/**
 	 * Removes one path from the list of paths.
 	 * 
 	 * @param existingPaths Existing list of paths to remove from
 	 * @param removePath Path to be removed.
 	 * @return String[] List that includes existing paths without the path that was removed.
 	 */
 	public static String[] removePathFromExistingPathList(String[] existingPaths, String removePath) {
 		List<String> newPathList = new ArrayList<String>();
 		String path;
 		//adds existing paths to new paths list
 		for (int i = 0; i < existingPaths.length; i++) {
 			path = existingPaths[i];
 			newPathList.add(path);
 		}
 		newPathList.remove(removePath);
 		//creates a new list that includes all existing paths except the removed path
 		String[] newArray = newPathList.toArray(new String[0]);
 		return newArray;
 	}
 
 	/**
 	 * Checks if a file path exists.
 	 * 
 	 * @return boolean True if the file exists.
 	 */
 	private static boolean pathExists(String path) {
 		//return true if path exists.
 		return new File(path).exists();
 	}
 
 	//TODO: FIX. Currently always returns Debug config.
 	private static IConfiguration getCurrentBuildConf(IProject proj) {
 		IConfiguration conf = null;
 		IManagedBuildInfo info = null;
 		//try to get Managed build info
 		try {
 			info = ManagedBuildManager.getBuildInfo(proj); //null if doesn't exists
 		} catch (Exception e) { //if not a managed build project
 			//print error
 			e.printStackTrace();
 			return conf;
 		}
 		//info can be null for projects without build info. For example, when creating a project
 		//from Import -> C/C++ Executable
 		if(info == null) {
 			return conf;
 		}
 		conf = info.getDefaultConfiguration();
 		return conf;
 	}
 	
 }
