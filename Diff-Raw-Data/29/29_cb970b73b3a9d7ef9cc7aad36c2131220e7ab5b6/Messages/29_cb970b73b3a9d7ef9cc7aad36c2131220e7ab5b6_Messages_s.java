 /**
  * 
  * Copyright 2003 NCHELP
  * 
  * Author:		Tim Bornholtz,  Priority Technologies, Inc.
  * 
  * 
  * This code is part of the Meteor system as defined and specified 
  * by the National Council of Higher Education Loan Programs, Inc. 
  * (NCHELP) and the Meteor Sponsors, and developed by Priority 
  * Technologies, Inc. (PTI). 
  *
  * 
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *	
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *	
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  ********************************************************************************/
 package org.nchelp.meteor.util;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  *  Messages.java
  *
  *   @author  timb
  *   @version $Revision$ $Date$
  *   @since   Apr 29, 2003
  */
 public class Messages {
 
 	private static Properties props = null;
 	private static Log log = LogFactory.getLog(Messages.class);
 	
 	private static void init(){
 		if(props != null) {
 			return;
 		}
 		
 		props = new Properties();
 		
 		InputStream is = Messages.class.getClassLoader().getResourceAsStream("messages.properties");
 		
 		try {
 			props.load(is);
 		} catch (IOException e) {
 			log.error("Error loading properties file 'messages.properties': " + e.getMessage());
 		}
 	}
 	
 	public static String getMessage(String key){
 		init();
 		return getMessage(key, (String)null);
 	}
 	
 	public static String getMessage(String key, String defaultVal){
 		return getMessage(key, defaultVal, (Map)null);
 	}
 	
 	public static String getMessage(String key, Map parameters){
 		return getMessage(key, null, parameters);
 	}
 	
 	public static String getMessage(String key, String defaultVal, Map parameters){
 		init();
 		String value = props.getProperty(key, defaultVal);
 		if(value == null){
 			value = key;
 		}
 		if(parameters == null){
 			parameters = new HashMap();
 		}
 		Set parameterKeys = parameters.keySet();
 		Iterator iter = parameterKeys.iterator();
 		while(iter.hasNext()){
 			String tmp = (String)iter.next();
 			value = replaceTag(value, tmp, (String)parameters.get(tmp));
 		}
 
 		return value;
 	}
 	
 	private static String replaceTag(String original, String tag, String value){
 		StringBuffer buff = new StringBuffer();
 		
 		// find the first occurrence of {tag}
 		int idx = original.indexOf("{" + tag + "}");
 		
 		if(idx >= 0){
 			buff.append(original.substring(0, idx));
 			
 			buff.append(value);
 			
 			// the "2" is for the { and the }
 			int endPos = idx + 2 + tag.length();
 			if(endPos < original.length()){
 				buff.append(original.substring(endPos));
 			}
 			
 		} else {
 			buff.append(original);
 		}
 		
 		return buff.toString();
 	}
 }
