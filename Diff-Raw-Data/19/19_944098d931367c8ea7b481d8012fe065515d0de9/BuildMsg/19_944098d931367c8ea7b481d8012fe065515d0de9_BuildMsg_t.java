 package org.nitlanguage.ndt.core;
 
 public class BuildMsg {	
 	/*PARSER*/
 	
 	public static final String PARSING = "Parsing";
 	public static final String PARSING_TASK = "Parsing nit files";
 	public static final String PARSING_PROJECT = "Parsing project ";
 	public static final String ERROR_COMPILER = "Error with nit compiler";
 
 	/*COMPILER*/
 	
 	public static final String COMPILATION_JOB = "Nit Compiler";
 	public static final String COMPILATION_TASK = "Compiling Nit";
 	// public static final String BUILDER_ID = "org.uqam.nit.ndt.builder";
 	public static final String MSG_CAT_COMPILER = "Nit Compiling Process";
 	public static final String ERROR_COMPILER_MISSING = "You need to set the Nit compiler location in Window/Properties/Nit";
	public static final String PREFERENCE_KEY_COMPILER = "nitc";
 	// Do not use color to display errors and warnings
 	public static final String COMPILER_ARG_NO_COLOR = "--no-color";
 	// Output file
 	public static final String COMPILER_ARG_OUTPUT = "-o";	
 	//Stop after meta-model processing
 	public static final String COMPILER_ARG_ONLY_MM = "--only-metamodel";
 	
 	public static final String COMPILATION_ERROR= "Error with nit compiler";
 	public static final String COMPILATION_OK = "All OK";
 	public static final String COMPILATION_ABORTED = "Error : Compilation aborted";
 	public static final String BINARY_NOT_FOUND = "Error : Binary cannot be found at specified path";
 	public static final String FILE_PATH_MISSING = "Error : No file path specified";
 	public static final String ERROR_COMPILER_NOT_FOUND = "Nit compiler cannot be found or cannot be run, are you sure the path you have set is valid ?";
 	
 	/*EXECUTION*/
 	public static final String EXECUTION_TASK = "Executing program";
 	public static final String LAUNCHING_TASK = "Compile and Run Nit";
 	public static final String EXECUTION_JOB = "Execution";
 }
