 /*
  * Copyright 2002-2005 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */	
 package net.sf.sojo.util;
 
 import java.io.InputStream;
 import java.sql.Timestamp;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.TreeMap;
 import java.util.logging.LogManager;
 import java.util.logging.Logger;
 
 import net.sf.sojo.core.NonCriticalExceptionHandler;
 import net.sf.sojo.core.reflect.ReflectionMethodHelper;
 
 /**
  * Helper/Util - class. This are functions, which can't be assigned, clearly ;-)
  * 
  * @author linke
  *
  */
 public final class Util {
 
 	public static final String DEFAULT_KEY_WORD_CLASS = "class";
 	
 	private static String keyWordClass = DEFAULT_KEY_WORD_CLASS;
 	private final static Map<String, DateFormat> dateFormats = new HashMap<String, DateFormat>();
 	
 	static {
 		registerDateFormat("EEE MMM dd HH:mm:ss z yyyy"); // java.util.Date.toString() format
		registerDateFormat("yyyy-MM-dd"); // SQL date format
 	}
 	
 	public static DateFormat registerDateFormat(String key, DateFormat pvDateFormat) {
 		return dateFormats.put(key, pvDateFormat);
 	}
 
 	/**
 	 * Add the format string as {@link SimpleDateFormat}. The format can be removed by the format string.
 	 * @param format {@link SimpleDateFormat#SimpleDateFormat(String)}
 	 */
 	public static DateFormat registerDateFormat(String format) {
 		return registerDateFormat(format, new SimpleDateFormat(format));
 	}
 	
 	public static void unregisterDateFormat(String key) {
 	  synchronized(dateFormats) {
 		dateFormats.remove(key);
 	}
 	}
 	
 	public static void setDateFormats(Map<String, DateFormat> map) {
 	  synchronized(dateFormats) {
   	  dateFormats.clear();
   	  dateFormats.putAll(map);
 	  }
 	}
 
 	private static Collection<DateFormat> getDateFormats() {
 	  synchronized(dateFormats) {
 	    return Collections.unmodifiableCollection(dateFormats.values());
 	  }
 	}
 
 	/**
 	 * Removes all registered {@link DateFormat DateFormats}.
 	 * 
 	 * @param copy if not null, the old values are copied to this map
 	 * @return an unmodifiable version of the original registrations.
 	 */
 	public static void clearDateFormats(Map<String, DateFormat> copy) {
 	  synchronized(dateFormats) {
 	    if (copy != null) {
 	      copy.putAll(dateFormats);
 	    }
 		dateFormats.clear();
 	  }
 	}
 	
 	private Util() {}
 	
 	public static void setKeyWordClass(String pvKeyWordClass) { 
 		if (pvKeyWordClass != null && pvKeyWordClass.length() > 0) {
 			keyWordClass = pvKeyWordClass;
 			ReflectionMethodHelper.clearPropertiesCache();
 		}
 	}
 	public static String getKeyWordClass() {
 		return keyWordClass; 
 	}
 	public static void resetKeyWordClass() {
 		setKeyWordClass(DEFAULT_KEY_WORD_CLASS); 
 	}
 	
 	
 	public static boolean initJdkLogger () {
     	return initJdkLogger (Util.class.getResourceAsStream("jdk14-logging.properties"));
 	}
 
 	public static boolean initJdkLogger (InputStream pvInputStream) {
 		try {
 			LogManager.getLogManager().readConfiguration(pvInputStream);
 			Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME); 
 			LogManager.getLogManager().addLogger(logger);
 			logger.finest("Jdk14Logger initialisiert ...");
 			return true;
 		} catch (Exception e) {
 			e.printStackTrace();
 		}		
 		return false;
 	}
 	
 	public static String createNumberOfBlank(int pvNumberOfBlank, int pvMultiplier ) {
 		StringBuffer sb =  new StringBuffer();
 		for (int i=0; i<pvNumberOfBlank; i++) {
 			for (int j=0; j<pvMultiplier; j++) {
 				sb.append(" ");
 			}
 		}
 		return sb.toString();
 	}
 
 	/**
 	 * Convert a Date-String to a Date. The Converter <b>ignored the Millisecond</b>.
 	 * Example: Thu Aug 11 19:30:57 CEST 2005
 	 * 
 	 * @param pvDateString The Date-String (unequal null).
 	 * @return Valid <code>java.util.Date</code>.
 	 */
 	public static Date string2Date (String pvDateString) {
 		if (pvDateString == null) { 
 			throw new IllegalArgumentException(pvDateString); 
 		}
 		
 		// 1. pass: pvDateString is a long as milliseconds after Jan 1st 1970
 		try {
 			return new Date(Long.parseLong(pvDateString));
 		} catch (Exception e) {
 			if (NonCriticalExceptionHandler.isNonCriticalExceptionHandlerEnabled()) {
 				NonCriticalExceptionHandler.handleException(Util.class, e, "1st pass string2Date: " + pvDateString);
 			}
 		}
 		
 		// 2. pass: Timestamp format. The Timestamp format with two digits og fractional seconds are not parsed correctly by SimpleDateFormat,
 		//          hence we have to use Timestamp.valueOf, which handles this correctly
 			try {
 		  return Timestamp.valueOf(pvDateString);
     } catch (Exception e) {
       if (NonCriticalExceptionHandler.isNonCriticalExceptionHandlerEnabled()) {
         NonCriticalExceptionHandler.handleException(Util.class, e, "2nd pass string2Date: " + pvDateString);
       }
     }
 		
 		// 3. pass, iterate through all registered DateFormats
 		Collection<DateFormat> dfList = getDateFormats();
 		for (DateFormat df : dfList) {
 			try {
 				return df.parse(pvDateString); 
 			} catch (ParseException e) {
 				if (NonCriticalExceptionHandler.isNonCriticalExceptionHandlerEnabled()) {
 					NonCriticalExceptionHandler.handleException(Util.class, e, "3nd pass string2Date: " + pvDateString);
 				}
 			}
 		}
 		
 		throw new IllegalArgumentException (pvDateString); 
 	}
 
 	
     /**
      * Analysed the objects in an Array/Collection. If all Object from the same class type,
      * then is the return value this class. If are several class types in the Array,
      * then ist the return class from type Object.
      * @param pvListObj
      * @return Class, that are in the Array or Collection
      */
     public static Class<?> getArrayType (Object pvListObj) {
     	Class<?> lvType = Object.class;
     	if (pvListObj == null) { return lvType; }
     	if (pvListObj.getClass().isArray()) {
     		Object o[] = (Object[]) pvListObj;
     		if (o.length > 0) {
 	    		Class<?> lvClass = o[0].getClass(); 
 	    		
 	    		// !!!!! Specialfall ?????
 	    		if(Map.class.isAssignableFrom(lvClass)) {
 	    			return Object.class;
 	    		}
 	    		
 	    		for (int i = 0; i < o.length; i++) {
 					if (!lvClass.equals(o[i].getClass())) {
 						return lvType;
 					}
 				}
 	    		return lvClass;
     		} else {
     			return o.getClass().getComponentType();
     		}
     	}
     	else if (pvListObj instanceof Collection) {
     		Collection<?> coll = (Collection<?>) pvListObj;
     		if (coll.size() > 0) {
     			Class<?> lvClass = coll.iterator().next().getClass();
     			Iterator<?> it = coll.iterator();
     			while (it.hasNext()) {
 					if (!lvClass.equals(it.next().getClass())) {
 						return lvType;
 					}
     			}
     			return lvClass;
     		} else {
     			return lvType;
     		}
     	}
     	return lvType;
     }
     
 	public static void delLastComma (StringBuffer s) {
 		int lvPos = s.length() - 1;
 		if (lvPos > 0 && s.charAt(lvPos) == ',') {
 			s.deleteCharAt(lvPos);
 		}
 	}
 
 	public static boolean isStringInArray (String[] pvList, String pvSearchString) {
 		for (String pvi : pvList) {
 			if (pvi.equals(pvSearchString)) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public static Map<String, Object> filterMapByKeys (Map<String, Object> pvSearchMap, String[] pvList) {
 		if (pvList == null || pvList.length == 0) {
 			return pvSearchMap;
 		} else {
 			Map<String,Object> lvReturnMap = new TreeMap<String, Object>();
 			for (Entry<String, Object> entry : pvSearchMap.entrySet()) {
 				
 		        if (isStringInArray(pvList, entry.getKey())) {
 		        	lvReturnMap.put(entry.getKey(), entry.getValue());
 		        }
 		     
 		    }
 			return lvReturnMap;
 		}
 	}
 }
