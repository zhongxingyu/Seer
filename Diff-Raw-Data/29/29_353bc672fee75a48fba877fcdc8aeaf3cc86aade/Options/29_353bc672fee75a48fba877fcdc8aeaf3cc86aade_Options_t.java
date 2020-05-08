 /**
  *  Andromeda, a galaxy extension language.
  *  Copyright (C) 2010 J. 'gex' Finis  (gekko_tgh@gmx.de, sc2mod.com)
  * 
  *  Because of possible Plagiarism, Andromeda is not yet
  *	Open Source. You are not allowed to redistribute the sources
  *	in any form without my permission.
  *  
  */
 package com.sc2mod.andromeda.program;
 
 import java.io.File;
 
 import com.sc2mod.andromeda.parsing.FileSource;
 
 public class Options {
 
 	//Code gen
 	public boolean shortenVarNames;
 	public boolean useIndent;
 	public boolean newLines;
 	public boolean whitespacesInExprs;
 	public boolean ownLineForOpenBraces;
 	public boolean insertDescriptionComments;
 	
 	public boolean checkNativeFunctionBodies;
 	
 	//Exceptions
 	public int handleDeadCode;
 	public int handleUnusedGlobals;
 	public int handleUnusedFields;
 	public int handleUnusedStaticFields;
 	public int handleUnreadLocals;
 	public int handleUnwrittenLocals;
 	public int handleUncalledMethods;
 	public int handleUncalledFunctions;
 	
 	
 	//Optimization
 	public boolean resolveConstantExpressions;
 	public int inlineStringConstsUpToXChars;	
 	
 	public static final int EXCEPTION_IGNORE = 0;
 	public static final int EXCEPTION_REMOVE = 1;
 	public static final int EXCEPTION_WARNING = 2;
 	public static final int EXCEPTION_ERROR = 3;
 	
 	//Map handling
 	public File mapIn;
 	public File mapOut;
 	public String runConfiguration;
 	public File xmlStructure;
 	public File mapScriptIn;
 	public File triggersIn;
 	
 	
 	//Folders
 	public File nativeLibFolder;
 	public File libFolder;
 	public String nativeList;
 	public boolean runMap;
 	public String runParams;
 	public File xmlErrors;
 	public boolean noCodegen;
 	public boolean outputNatives;
 	public File outDir;
 	
 	public Options(ConfigHandler configHandler, Parameters params) throws InvalidParameterException{
 		this(configHandler,params,null);
 	}
 	
 	public Options(ConfigHandler configHandler, Parameters params, String forceRunConf) throws InvalidParameterException {
 		String curSection;
 		
 		readParams(params);
 		if(runConfiguration==null){
 			runConfiguration = configHandler.getPropertyString("GENERAL", "defaultRunConfig", "debug");
 		}
 		if(forceRunConf!=null){
 			runConfiguration = forceRunConf;
 		}
 		//Check if the run configuration exists
 		String[] runConfs = configHandler.getPropertyCommaString("GENERAL", "runConfigs", "debug");
 		boolean exists = false;
 		for(String s: runConfs){
 			if(runConfiguration.equals(s)) exists = true;
 		}
 		if(!exists){
 			throw new InvalidParameterException("The run configuration '" + runConfiguration + "' does not exist.");
 		}
 		
 		runConfiguration = "RC_" + runConfiguration.toUpperCase();
 
 		//Code gen
 		curSection = runConfiguration + ".CODE_GEN";		
 		shortenVarNames = configHandler.getPropertyBool(curSection, "shortenVarNames", false);
 		useIndent = configHandler.getPropertyBool(curSection, "useIndent", true);
 		newLines = configHandler.getPropertyBool(curSection, "newLines", true);
 		whitespacesInExprs = configHandler.getPropertyBool(curSection, "whitespacesInExprs", true);
 		ownLineForOpenBraces = configHandler.getPropertyBool(curSection, "ownLineForOpenBraces", false);
 		insertDescriptionComments = configHandler.getPropertyBool(curSection, "insertDescriptionComments", false);
 		if(!newLines){
 			this.useIndent = false;
 			this.insertDescriptionComments = false;
 			this.ownLineForOpenBraces = false;
 		}
 		
 		//Exceptions
 		String[] options = {"IGNORE","REMOVE","WARNING","ERROR"};
 		curSection = runConfiguration + ".EXCEPTIONS";
 		handleDeadCode = configHandler.getPropertyEnum(curSection, "unreachableCode", "ERROR", options);
 		handleUnusedGlobals = configHandler.getPropertyEnum(curSection, "unusedGlobals", "ERROR", options);
 		handleUnusedFields = configHandler.getPropertyEnum(curSection, "unusedFields", "ERROR", options);
 		handleUnusedStaticFields = configHandler.getPropertyEnum(curSection, "unusedStaticFields", "ERROR", options);
 		handleUnreadLocals = configHandler.getPropertyEnum(curSection, "unreadLocals", "ERROR", options);
 		handleUnwrittenLocals = configHandler.getPropertyEnum(curSection, "unwrittenLocals", "ERROR", options);
 		handleUncalledMethods = configHandler.getPropertyEnum(curSection, "uncalledMethods", "ERROR", options);
 		handleUncalledFunctions = configHandler.getPropertyEnum(curSection, "uncalledFunctions", "ERROR", options);
 		
 		
 		//Optimization
 		resolveConstantExpressions = configHandler.getPropertyBool(curSection, "resolveConstantExpressions", true);
 		inlineStringConstsUpToXChars = configHandler.getPropertyInt(curSection, "inlineStringConstsUpToXChars", 20);
 
 		
 		//Testing
 		curSection = runConfiguration + ".TEST";		
 		runParams = configHandler.getPropertyString(curSection, "sc2params", "");
 		
 		
 		
 		//Folders
 		nativeLibFolder = new File(Program.appDirectory,configHandler.getPropertyString("GENERAL", "nativeLibFolder", "./nativeLib"));
 		Util.checkForDir(nativeLibFolder,true);
 		libFolder = new File(Program.appDirectory,configHandler.getPropertyString("GENERAL", "libFolder", "./lib"));
		if(!libFolder.exists()) {
			libFolder = new File(configHandler.getPropertyString("GENERAL", "libFolder", "./lib"));
		}
 		Util.checkForDir(libFolder,true);
 		nativeList = configHandler.getPropertyString("GENERAL", "nativeLibs", "missing_natives.a,NativeLib_beta.galaxy");
 		
 	}
 	
 	private void readParams(Parameters params) throws InvalidParameterException {
 		runConfiguration = params.getParamString(Parameters.STRING_RUN_CONF);
 		mapIn = params.getParamFile(Parameters.FILE_MAP_IN);
 		mapOut = params.getParamFile(Parameters.FILE_MAP_OUT);
 		xmlStructure = params.getParamFile(Parameters.FILE_XML_STRUCTURE);
 		xmlErrors = params.getParamFile(Parameters.FILE_XML_ERRORS);
 		noCodegen = params.getParamFlag(Parameters.FLAG_NO_CODE);
 		outputNatives = params.getParamFlag(Parameters.FLAG_OUTPUT_NATIVES);
 		runMap = params.getParamFlag(Parameters.FLAG_RUN_MAP);
 		outDir = params.getParamFile(Parameters.FILE_DIR_OUT);
 		mapScriptIn = params.getParamFile(Parameters.FILE_MAPSCRIPT_IN);
 		triggersIn = params.getParamFile(Parameters.FILE_TRIGGERS_IN);
 		
 		if(outDir == null){
 			outDir = new File(Program.appDirectory,"out");
 		}
 		if(mapIn == null && mapOut != null){
 			throw new InvalidParameterException("You may not specify an output map (-o) if no input map (-i) was specified.");
 		}
 		if(mapIn != null){
 			if(triggersIn != null){
 				throw new InvalidParameterException("You may not specify an input map (-i) and an input trigger file (-g) together.");
 			}
 			if(mapScriptIn != null){
 				throw new InvalidParameterException("You may not specify an input map (-i) and an input map scipt file (-m) together.");
 			}
 		}
 	}
 	
 	
 }
