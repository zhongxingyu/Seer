 /*
  *  __  __      
  * /\ \/\ \  __________   
  * \ \ \_\ \/_______  /\   
  *  \ \  _  \  ____/ / /  
  *   \ \_\ \_\ \ \/ / / 
  *    \/_/\/_/\ \ \/ /  
  *             \ \  /
  *              \_\/
  *
  * -----------------------------------------------------------------------------
  * @author: Herbert Veitengruber 
  * @version: 1.0.0
  * -----------------------------------------------------------------------------
  *
  * Copyright (c) 2013 Herbert Veitengruber 
  *
  * Licensed under the MIT license:
  * http://www.opensource.org/licenses/mit-license.php
  */
 package jhv.util.script;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.script.ScriptEngine;
 import javax.script.ScriptEngineFactory;
 import javax.script.ScriptEngineManager;
 
 import jhv.util.debug.logger.ApplicationLogger;
 
 /**
  * JXScriptFactory
  * 
  * Class to work with the javax.script API
  * 
  */
 public class JXScriptFactory 
 {
 	// ============================================================================
 	//  Variables
 	// ============================================================================
 	
 	/**
 	 * the script engine
 	 */
 	private ScriptEngine engine;
 
 	
 	// ============================================================================
 	//  Constructor
 	// ============================================================================
 
 	/**
 	 * Constructor 1.
 	 * Default with JavaScript.
 	 */
 	public JXScriptFactory() 
 	{
 		ScriptEngineManager manager = new ScriptEngineManager();
 		engine = manager.getEngineByName("JavaScript");
 	}
 
 	/**
 	 * Constructor 2.
 	 * With custom script engine name.
 	 * 
 	 * @param scriptname
 	 */
 	public JXScriptFactory(String scriptname) 
 	{
 		ScriptEngineManager manager = new ScriptEngineManager();
 		engine = manager.getEngineByName(scriptname);
 	}
 	
 	// ============================================================================
 	//  Functions
 	// ============================================================================
 	
 	/**
 	 * availableScriptEngines
 	 * 
 	 * returns an array of available script engines.
 	 * @return
 	 */
 	public static ArrayList<String> availableScriptEngines()
 	{
 		ScriptEngineManager manager = new ScriptEngineManager();
         ArrayList<String> arr = new ArrayList<String>();
         
 		List<ScriptEngineFactory> factories = manager.getEngineFactories();
         for( ScriptEngineFactory factory : factories )
         	arr.add( factory.getEngineName() );
         	
         return arr;
 	}
 	
 	/**
 	 * getEngine
 	 * 
 	 * @return
 	 */
 	public ScriptEngine getEngine()
 	{
 		return engine;
 	}
 	
 	/**
 	 * getEngineName
 	 * 
 	 * @return
 	 */
 	public String getEngineName()
 	{
 		return engine.getFactory().getEngineName();
 	}
 	
 	/**
 	 * getEngineVersion
 	 * 
 	 * @return
 	 */
 	public String getEngineVersion()
 	{
 		return engine.getFactory().getEngineVersion();
 	}
 	
 	/**
 	 * getLanguageName
 	 * 
 	 * @return
 	 */
 	public String getLanguageName()
 	{
 		return engine.getFactory().getLanguageName();
 	}
 	
 	/**
 	 * getLanguageVersion
 	 * 
 	 * @return
 	 */
 	public String getLanguageVersion()
 	{
 		return engine.getFactory().getLanguageVersion();
 	}
 	
 	/**
 	 * logs the Engine
 	 */
 	public void logEngineProperties()
 	{
 		String msg = "javax.script API\n"
 				+ "Engine: " + getEngineName() + " " + getEngineVersion() + "\n"
 				+ "Language: " + getLanguageName() + " " + getLanguageVersion();
 		
 		ApplicationLogger.separator();
		ApplicationLogger.logInfo(msg);
 		ApplicationLogger.separator();
 	}
 	
 	/**
 	 * loadScriptResource
 	 * 
 	 * loads a script from the jar resource or from an external location.
 	 *
 	 * In most cases you will try to access a file from jar resource
 	 * first. In the case of scripting it is more suitable to access
 	 * the external file first since we want to give the user the
 	 * possibility to customize his/her scripts.
 	 * 
 	 * So are script inside a jar is just for fall back.
 	 * 
 	 * @param filename
 	 */
 	public String loadScriptResource(String filename, String charset)
 	{
 		BufferedReader br = null;
 		try 
 		{
 			br = new BufferedReader(new InputStreamReader(
 					new FileInputStream(filename),
 					charset
 				));
 		
 		} catch( IOException | NullPointerException e1 ) {
 			// no file found try to load from jar
 			try 
 			{
 				br = new BufferedReader(new InputStreamReader(
 						getClass().getClassLoader().getResourceAsStream(filename),
 						charset
 					));
 				
 			} catch( Exception e2 ) {
 				// both loading tries failed
 				// so no script file is available
 				ApplicationLogger.logError("Script " + filename + " not found!");
 				return null;
 			}
 		}
 		try 
 		{
 			StringBuilder sb = new StringBuilder();
 			String line;
 		
 			while(( line = br.readLine()) != null ) 
 			{
 				sb.append(line);
 				sb.append("\n");
 			}
 			br.close();
 			return sb.toString();
 			
 		} catch( IOException e ) {
 			ApplicationLogger.logError(e);
 		}
 		return null;
 	}
 	
 }
