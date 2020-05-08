 package com.shivanshusingh.pluginanalyser.utils.parsing;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 
 /**
  * 
  * extracts related constants used for recording extracted information and also for parsing during restoration from extracts.
  * @author Shivanshu Singh
  * 
  */
 public interface Constants {
 
 	public static final String _OR_ 													= " || ";
 	public static final String _AND_ 													= " && ";
 	
 	public static final String CONFIG_ 													= "CONFIG_";
 	public static final String _FE_ 													= "__";
 
 	public static final String MRKR 													= "==";
 	public static final String MARKER_INTERIM 											= "====";
 	public static final String MARKER_TERMINATOR 										= "----";
 	
 	public static final String BUNDLE_DEPDENDENCY_KEYWORD_OPTIONAL 						= "(optional)";
 
 	public static final String BUNDLE_CLASSPATHENTRIES 									= MRKR +  "Bundle ClassPathEntries"  			  +  MRKR ;
 	public static final String BUNDLE_EXPORTS 											= MRKR + "Bundle Exports"  						  +  MRKR ;
 	public static final String BUNDLE_FRAGMENT_HOST 									= MRKR + "If FRAGMENT then HOSTBUNDLE"  			 +  MRKR ;
 	public static final String BUNDLE_IGNORE 											= MRKR + "IGNORE BUNDLE COZ MANIFEST OR SOME PROPERTY OF IT WAS MALFORMED " 			+ MRKR;
 	public static final String BUNDLE_PACKAGE_IMPORTS 									= MRKR + "Bundle Package Imports"  				  +  MRKR ;
 	public static final String BUNDLE_PLUGIN_XML 										= MRKR + "Bundle Plugin.xml"  					  +  MRKR ;
 	
 	public static final String PROPERTY_VALUE_UNKNOWN_LITERAL							= "PAUNKNOWN";
 	
 	public static final String BUNDLE_REQUIREMENTS 										= MRKR + "Bundle Requirements"  				  +  MRKR ;
 	public static final String BUNDLE_OTHER_BUNDLE_IMPORTS 								= MRKR + "Bundle Other BUNDLE Imports"  			+ MRKR ;
 	public static final String BUNDLE_SYMBOLICNAME 										= MRKR + "Symbolic Name"  						  +  MRKR ;
 	public static final String BUNDLE_VERSION 											= MRKR + "Version"  							  +  MRKR ;
 	public static final String BUNDLE_VERSION_WITHOUT_QUALIFIER 						= MRKR + "Version without qualifier"  			  +  MRKR ;
 
 	public static final String BUNDLE_VERSION_UNKNOWN_WITHOUT_QUALIFIER_LITERAL 		= PROPERTY_VALUE_UNKNOWN_LITERAL + "." + PROPERTY_VALUE_UNKNOWN_LITERAL + "." + PROPERTY_VALUE_UNKNOWN_LITERAL;
 	public static final String BUNDLE_VERSION_UNKNOWN_LITERAL 							= BUNDLE_VERSION_UNKNOWN_WITHOUT_QUALIFIER_LITERAL  + "." + PROPERTY_VALUE_UNKNOWN_LITERAL;
 
 	public static final String DELIM_VERSION_STRING_CLOSE 								= "%>";
 	public static final String DELIM_VERSION_STRING_OPEN 								= "<%";
 	public static final String DELIM_PLUGIN_DEPENDENCY_ELEMENT_SET  					= "####";
 	public static final String DELIM_PLUGIN_ELEMENT_SUPERCLASS_INTERFACE 				= " => ";
 
 	public static final String FM_CONSTRAINTS_FILE_EXTENSION 							= ".fm";
 	public static final String FM_CONSTRAINTS_FILE_NAME 								= "constraints";
 	public static final String FM_CONSTRAINTS_FILE_PREFIX 								= "";
 	public static final String FM_PLUGIN_DEPENDENCIES_FILE_PREFIX 						= "";
 	public static final String FM_PLUGIN_DEPENDENCIES_FILE_NAME 						= "dependencies";
 	public static final String FM_PLUGIN_DEPENDENCIES_FILE_EXTENSION 					= ".fm";
 	
 	public static final String DEPENDENCY_SET_FILE_EXTENSION 							= ".dpst";
 	public static final String DEPENDENCY_SET_FILE_PREFIX 								= "DEPENDENCY-SET-";
 	
 	public static final String EXCEPTION_MSG_EARLY_END_OF_A_PARAMETER 					= "Early end of a parameter";
 
 	public static final String EXTENSION_TXT 											= ".txt";
 	public static final String EXTENSION_JAR 											= ".jar";
 	public static final String EXTENSION_EXTRACT 										= ".extr";
 	public static final String EXTENSION_OBJ 											= ".obj";
 	
 	public static final String EXTRACT_FILE_EXTENSION_EXPPACKAGEMAP 					= Constants.EXTENSION_OBJ;
 	public static final String EXTRACT_FILE_EXTENSION_FEATURE							= Constants.EXTENSION_EXTRACT;
 	public static final String EXTRACT_FILE_EXTENSION_FEATUREMAP 						= Constants.EXTENSION_OBJ;
 	public static final String EXTRACT_FILE_EXTENSION_PLUGIN 							= Constants.EXTENSION_EXTRACT;
 	public static final String EXTRACT_FILE_EXTENSION_PLUGINMAP 						= Constants.EXTENSION_OBJ;
 	public static final String EXTRACT_FILE_NAME_JAVA_CLASSES_SDK 						= "JAVA.CLASSES.SDK";
 	public static final String EXTRACT_FILE_NAME_EXPPACKAGEMAP 							= "EXPORTEDPACKAGES-MAP-OBJECT";
 	public static final String EXTRACT_FILE_NAME_FEATUREMAP 							= "FEATURE-MAP-OBJECT";
 	public static final String EXTRACT_FILE_NAME_PLUGINMAP 								= "PLUGIN-MAP-OBJECT";
 	public static final String EXTRACT_FILE_PREFIX_EXPPACKAGEMAP 						= "EXTRACT-EXPORTEDPACKAGESMAP-";
 	public static final String EXTRACT_FILE_PREFIX_FEATURE 								= "EXTRACT-FEATURE-";
 	public static final String EXTRACT_FILE_PREFIX_FEATUREMAP 							= "EXTRACT-FEATUREMAP-";
 	public static final String EXTRACT_FILE_PREFIX_PLUGIN 								= "EXTRACT-PLUGIN-";
 	public static final String EXTRACT_FILE_PREFIX_PLUGINMAP 							= "EXTRACT-PLUGINMAP-";
 
 	public static final String FEATURE_FEATURE_XML 										= MRKR + "Feature.xml " 				+ MRKR;
 	public static final String FEATURE_LABEL 											= MRKR + "Label " 						+ MRKR;
 	public static final String FEATURE_IMPORTS 											= MRKR + "Imports " 					+ MRKR;
 	public static final String FEATURE_PROVIDED_PLUGINS 								= MRKR + "Provided Plugins " 			+ MRKR;
 	public static final String FEATURE_DESCRIPTION 										= MRKR + "Description " 				+ MRKR;
 	public static final String FEATURE_UPDATE_LABEL 									= MRKR + "UpdateLabel " 				+ MRKR;
 	public static final String FEATURE_URL 												= MRKR + "URL " 						+ MRKR;
 	public static final String FEATURE_PROVIDER_NAME 									= MRKR + "ProviderName " 				+ MRKR;
 	public static final String FEATURE_VERSION_WITHOUT_QUALIFIER 						= MRKR + "Version  Without Qualifier  "	+ MRKR;
 	public static final String FEATURE_VERSION 											= MRKR + "Version " 					+ MRKR;
 	public static final String FEATURE_ID 												= MRKR + "Id OrName  " 					+ MRKR;
 	public static final String FEATURE_OS 												= MRKR + "Feature OS " 					+ MRKR; 
 	public static final String FEATURE_ARCH 											= MRKR + "Feature ARCH " 				+ MRKR; 
 	public static final String FEATURE_WS 												= MRKR + "Feature WS " 					+ MRKR; 
 
 	public static final String PLUGIN_ALL_CLASS_PACKAGES 								= MRKR + "All Class packages " 						+ MRKR;
 	public static final String PLUGIN_ALL_INHERITANCE_AND_INTERFACE_PAIRS   			= MRKR + "All Inheritance AND Interface Impl Pairs Super Set "			+ MRKR;
 	public static final String PLUGIN_ALL_INHERITANCE_HIERARCHIES 						= MRKR + "All Inheritance Hierarchies "  	  		+ MRKR;
 	public static final String PLUGIN_ALL_INHERITANCE_PAIRS 							= MRKR + "All My Inheritance Pairs  "				+ MRKR;
 	public static final String PLUGIN_ALL_INTERFACE_IMPLEMENTATION_LISTS 				= MRKR + "All Interfaces Implementation Lists "		+ MRKR;
 	public static final String PLUGIN_ALL_INTERFACE_IMPLEMENTATION_PAIRS 				= MRKR + "All Interfaces Implementation Pairs "		+ MRKR;
 	public static final String PLUGIN_ALL_INVOKATION_PROXY_PAIRS 						= MRKR + "All Invokation Satisfaction Potential Proxy Pairs"			+ MRKR;
 	public static final String PLUGIN_ALL_JAR_PACKAGES 									= MRKR + "All Jar Packages" 						+ MRKR;
 	public static final String PLUGIN_ALL_MY_METHOD_CALLS 								= MRKR + "All Invokations" 							+ MRKR;
 	public static final String PLUGIN_ALL_MY_METHOD_CALLS_EXTERNAL 						= MRKR + "All External Invokations" 				+ MRKR;
 	public static final String PLUGIN_ALL_MY_METHOD_CALLS_EXTERNAL_AND_NON_JAVA 		= MRKR + "All External and non Java Invokations" 	+ MRKR;
 	public static final String PLUGIN_ALL_MY_METHODS 									= MRKR + "All My Methods" 							+ MRKR;
 	public static final String PLUGIN_ALL_MY_METHODS_DEPRECATED 						= MRKR + "All My Deprecated Methods" 				+ MRKR;
 	public static final String PLUGIN_ALL_MY_METHODS_PUBLIC 							= MRKR + "All My Public Methods" 					+ MRKR;
 	public static final String PLUGIN_ALL_MY_TYPES 										= MRKR + "All My Classes (Types)" 					+ MRKR;
 	public static final String PLUGIN_ALL_MY_TYPES_DEPRECATED 							= MRKR + "All My Deprecated Classes" 				+ MRKR;
 	public static final String PLUGIN_ALL_MY_TYPES_PUBLIC 								= MRKR + "All My Public Classes (Types)" 			+ MRKR;
 	public static final String PLUGIN_ALL_TYPES_DETECTED 								= MRKR + "All Detected Types" 						+ MRKR;
 	public static final String PLUGIN_ALL_TYPES_DETECTED_EXTERNAL 						= MRKR + "All External Detected Types" 				+ MRKR;
 	public static final String PLUGIN_ALL_TYPES_DETECTED_EXTERNAL_AND_NON_JAVA 			= MRKR + "All External Non Java Detected Types" 	+ MRKR;
 	
 	public static final String PLUGIN_DEPENDENCY_ALL_FUNCTIONS  						= MRKR + "All functions " 							+ MRKR;
 	public static final String PLUGIN_DEPENDENCY_ALL_IGNORED_PLUGINS 					= MRKR + "Plugins IGNORED  " 						+ MRKR;
 	public static final String PLUGIN_DEPENDENCY_ALL_TYPES  							= MRKR + "All types (classes) " 					+ MRKR;
 	public static final String PLUGIN_DEPENDENCY_ALL_UNMATCHED_FUNCTION_IMPORTS 		= MRKR + "All UnMatched Invokations" 				+ MRKR;
 	public static final String PLUGIN_DEPENDENCY_ALL_UNMATCHED_TYPE_IMPORTS 			= MRKR + "All UnMatched Types" 						+ MRKR;
 	public static final String PLUGIN_DEPENDENCY_EXPORTERS  							= MRKR + "Exporters " 								+ MRKR;
 	public static final String PLUGIN_DEPENDENCY_EXPORTERS_UNSATISFIED  				= MRKR + "Exporters  UnSatisfied " 					+ MRKR;
 	public static final String PLUGIN_DEPENDENCY_IMPORTERS  							= MRKR + "Importers " 								+ MRKR;
 	public static final String PLUGIN_DEPENDENCY_IMPORTERS_UNSATISFIED  				= MRKR + "Importers  UnSatisfied " 					+ MRKR;
 	public static final String PLUGIN_DEPENDENCY_SATISFYING_PLUGINS_SETS 				= MRKR + "Satisfying Plugins SETS"					+ MRKR;
 
 	public static final String JAVA_LANG_OBJECT 									  	= "java.lang.Object" ;
 
 	public static final List<String> JAVA_LANG_OBJECT_FUNCTIONS = new ArrayList<String>(Arrays.asList(
 			"boolean java.lang.Object.equals (java.lang.Object,)",
 			"int java.lang.Object.hashCode ()",
 			"java.lang.Class java.lang.Object.getClass ()",
 			"java.lang.Object java.lang.Object.clone ()",
 			"java.lang.String java.lang.Object.toString ()",
 			"void java.lang.Object.finalize ()",
 			"void java.lang.Object.notify ()",
 			"void java.lang.Object.notifyAll ()",
 			"void java.lang.Object.wait ()",
 			"void java.lang.Object.wait (long,)",
 			"void java.lang.Object.wait (long,int,)"
 			));
	
 	public static final String IMPLIES_RIGHT = " => ";
 	public static final String IMPLIES_BIDIRECTIONAL = " <=> ";
 		
 	public static final String PLATFORM_PARAM_WS = "____WS_";
 	public static final String PLATFORM_PARAM_ARCH = "____ARCH_";
 	public static final String PLATFORM_PARAM_OS = "____OS_";
 	
 	/**
 	 * the blacklisted plugins list is for ignoring all importes from these.
 	 * The may be edited from time to time as needed.
 	 */
 	public static final List<String> BLACKLISTED_PLUGINS = new ArrayList<String>(Arrays.asList(
 			"org.mozilla.javascript"+DELIM_VERSION_STRING_OPEN+"1.7.2.v201005080400"+DELIM_VERSION_STRING_CLOSE  
 			));
	
	public static final String DELIM_COMMENT = " // ";
 
 
 
 
 }
