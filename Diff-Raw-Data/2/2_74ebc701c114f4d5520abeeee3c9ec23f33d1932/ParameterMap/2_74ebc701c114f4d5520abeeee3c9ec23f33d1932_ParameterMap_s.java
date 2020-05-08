 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator
  * Copyright (C) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * This program and the accompanying materials are dual-licensed under either
  * the terms of the Eclipse Public License v1.0 as published by the Eclipse
  * Foundation
  *  
  *   or (per the licensee's choosing)
  *  
  * under the terms of the GNU General Public License version 2 as published
  * by the Free Software Foundation.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.util;
 
 import java.io.Serializable;
 import java.text.NumberFormat;
 import java.text.ParseException;
 import java.util.HashMap;
 
 import de.tuilmenau.ics.fog.Config;
 import de.tuilmenau.ics.fog.ui.Logging;
 
 
 /**
 * Stores a hash map with parameter values and provides typ-safe
  * method to access them. If allowed, it tries to get the parameter
  * values from the system properties.
  */
 public class ParameterMap implements Configuration
 {
 	public ParameterMap(boolean allowSystemProperties)
 	{
 		this.allowSystemProperties = allowSystemProperties;
 		
 		if(formater == null) formater = NumberFormat.getInstance(Config.LANGUAGE);
 	}
 	
 	public ParameterMap(String[] commandString, int startOffset, boolean allowSystemProperties)
 	{
 		this(allowSystemProperties);
 		
 		for (int i=startOffset; i<commandString.length; i++ ) {
 			String[] kvpart = commandString[i].split("=");
 			
 			put(kvpart[0], kvpart[1]);
 		}
 	}
 	
 	public Object put(String key, Serializable value)
 	{
 		if(parameters == null) parameters = new HashMap<String, Serializable>();
 		
 		return parameters.put(key, value);
 	}
 	
 	@Override
 	public double get(String key, double defaultValue)
 	{
 		Object res = get(key);
 		
 		if(res != null) {
 			try {
 				return formater.parse(res.toString()).doubleValue();
 			}
 			catch(ParseException exc) {
 				Logging.getInstance().err(this, "Unable to convert parameter " + res.toString() + " to a double value", exc);
 			}
 		}
 		
 		return defaultValue;
 	}
 	
 	@Override
 	public int get(String key, int defaultValue)
 	{
 		Object res = get(key);
 		
 		if(res != null) {
 			try {
 				return formater.parse(res.toString()).intValue();
 			}
 			catch(ParseException exc) {
 				// goto end of method
 			}
 		}
 		
 		return defaultValue;
 	}
 	
 	@Override
 	public String get(String key, String defaultValue)
 	{
 		Object res = get(key);
 		
 		if(res != null) {
 			return res.toString();
 		} else {
 			return defaultValue;
 		}
 	}
 	
 	@Override
 	public boolean get(String key, boolean defaultValue)
 	{
 		Object res = get(key);
 		
 		if(res != null) {
 			return Boolean.parseBoolean(res.toString());
 		} else {
 			return defaultValue;
 		}
 	}
 	
 	private Serializable get(String key)
 	{
 		Serializable res = null;
 
 		// is it in hash map?
 		if(parameters != null) {
 			res = parameters.get(key);
 		}
 
 		// do we have to search for system property?
 		if((res == null) && allowSystemProperties) {
 			res = System.getProperty(key);
 		}
 		
 		return res;
 	}
 	
 	public boolean containsKey(String key)
 	{
 		Serializable obj = get(key);
 		
 		return obj != null;
 	}
 	
 	@Override
 	public String toString()
 	{
 		StringBuilder res = new StringBuilder();
 		
 		res.append(getClass().getSimpleName());
 		res.append("{");
 		if(parameters != null) {
 			boolean first = true;
 			for(String key : parameters.keySet()) {
 				if(first) {
 					first = false;
 				} else {
 					res.append(",");
 				}
 				
 				res.append(key);
 				res.append("=");
 				res.append(parameters.get(key));
 			}
 		}
 		res.append("}");
 		
 		return res.toString();
 	}
 	
 	
 	private HashMap<String, Serializable> parameters;
 	private boolean allowSystemProperties;
 	
 	private static NumberFormat formater;
 }
