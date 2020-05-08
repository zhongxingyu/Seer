 package chameleon.core;
 
 public class Config {
 
 	/**************************
 	 * OPTIMISATION CONSTANTS *
 	 **************************/
 	
 	public static boolean DEBUG=true;
 	
 	public static boolean CACHE_ALL_TYPE_NAMES = false;
 	
 	public static boolean CACHE_DECLARATIONS = false;
 
 	/**
 	 * Turn cache of element references on and off. Default is on.
 	 */
 	public static boolean CACHE_ELEMENT_REFERENCES = false;
 	
 	/**
 	 * Turn cache of expression types on and off. Default is on.
 	 */
 	public static boolean CACHE_EXPRESSION_TYPES = false;
 
 	/**
 	 * Turn cache of language on and off. Default is on.
 	 */
 	public static boolean CACHE_LANGUAGE = false;
 
 	public static void setCaching(boolean bool) {
 		CACHE_ALL_TYPE_NAMES = bool;
 		CACHE_DECLARATIONS = bool;
 		CACHE_ELEMENT_REFERENCES = bool;
 		CACHE_LANGUAGE = bool;
 	}
 }
