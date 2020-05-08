 /*
  * Copyright (C) 2003 - 2012 OpenSubsystems.com/net/org and its owners. All rights reserved.
  * 
  * This file is part of OpenSubsystems.
  *
  * OpenSubsystems is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
  */
 
 package org.opensubsystems.core.util;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 /**
  * Class that enables reading related properties from property files. It allows 
  * to specify set of default values and then override the values for a set of 
  * properties with specified name. If an overridden property with the  specified 
  * name doesn't exist, the default property will be used.
  * 
  * Each property name consist from three parts. 
  * 1. base path, for example oss.my.application
  * 2. reader name, for example subsystem
  * 3. parameter name, for example version
  *  
  * Property name looks like <basepath>.<readername>.<parametername> for example
  * oss.my.application.subsystem.version
  * 
  * @author OpenSubsystems
  */
 public abstract class SetupReader extends OSSObject
 {
    // Constants ////////////////////////////////////////////////////////////////
    
    /**
     * Constant for parameter data type
     */
    public static final int PARAMETER_TYPE_UNKNOWN = 0;
    
    /**
     * Constant for parameter data type object
     */
    public static final Integer PARAMETER_TYPE_UNKNOWN_OBJ 
                                   = new Integer(PARAMETER_TYPE_UNKNOWN);
 
    /**
     * Constant for parameter data type
     */
    public static final int PARAMETER_TYPE_STRING = 1;
    
    /**
     * Constant for parameter data type object
     */
    public static final Integer PARAMETER_TYPE_STRING_OBJ 
                                   = new Integer(PARAMETER_TYPE_STRING);
    
    /**
     * Constant for parameter data type
     */
    public static final int PARAMETER_TYPE_INTEGER = 2;
    
    /**
     * Constant for parameter data type object
     */
    public static final Integer PARAMETER_TYPE_INTEGER_OBJ 
                                   = new Integer(PARAMETER_TYPE_INTEGER);
    
    /**
     * Constant for parameter data type object
     */
    public static final int PARAMETER_TYPE_DOUBLE = 3;
    
    /**
     * Constant for parameter data type object
     */
    public static final Integer PARAMETER_TYPE_DOUBLE_OBJ 
                                   = new Integer(PARAMETER_TYPE_DOUBLE);
    
    /**
     * Constant for parameter data type object
     */
    public static final int PARAMETER_TYPE_FLOAT = 4;
    
    /**
     * Constant for parameter data type object
     */
    public static final Integer PARAMETER_TYPE_FLOAT_OBJ 
                                   = new Integer(PARAMETER_TYPE_FLOAT);
 
    /**
     * Constant for parameter data type object
     */
    public static final int PARAMETER_TYPE_BOOLEAN = 5;
    
    /**
     * Constant for parameter data type object
     */
    public static final Integer PARAMETER_TYPE_BOOLEAN_OBJ 
                                   = new Integer(PARAMETER_TYPE_BOOLEAN);
 
    /**
     * Constant for parameter data type object
     */
    public static final int PARAMETER_TYPE_LONG = 6;
    
    /**
     * Constant for parameter data type object
     */
    public static final Integer PARAMETER_TYPE_LONG_OBJ 
                                   = new Integer(PARAMETER_TYPE_LONG);
 
    /**
     * Constant for parameter data type object
     */
    public static final int PARAMETER_TYPE_PATH = 7;
    
    /**
     * Constant for parameter data type object
     */
    public static final Integer PARAMETER_TYPE_PATH_OBJ 
                                   = new Integer(PARAMETER_TYPE_PATH);
 
    // Attributes ///////////////////////////////////////////////////////////////
    
    /**
     * Name of this reader. This name is added to base path to get full parameter 
     * name.
     */
    protected String m_strReaderName;
    
    /**
     * Base path of this reader. Each property name consist from three parts. 
     * base path, reader name and parameter name. Property name looks like 
     * <xxx>.<yyy>.<zzz> where xxx is base path (for example oss.receiver) yyy 
     * is reader name (for example defaultfax) and zzz is parameter name (for 
     * example priority). Result will be oss.receiver.defaultfax.priority
     */
    protected String m_strBasePath;
 
    /**
     * Map with all registered parameters. The key is String parameter name and 
     * the value is an a ThreeObjectStruct where the element 
     * 1. is Integer constant (one of the PARAMETER_TYPE_XXX constants) 
     *    representing the type. If this map is null of empty, the constructor 
     *    will invoke the registerParameters method to register the parameters 
     *    that will inserted to this map. This allows you to pass in static 
     *    variable that can be shared between all instances.
     * 2. is Object representing the default value. This can be static variable 
     *    that can be shared between all instances.
     * 3. is String representing user friendly name of the property
     */
    protected  Map<String, ThreeObjectStruct> m_mpRegisteredParameters;
 
    /**
     * Map with parameters values. Parameters values are read only once and  
     * then the value from this map is returned after each subsequent request. 
     * Key is parameter name and value is Object representing the value.
     */
    protected Map<String, Object> m_mpRegisteredParametersValues;
 
    // Constructors /////////////////////////////////////////////////////////////
    
    /**
     * @param strBasePath - base path of reader
     * @param strReaderName - name of reader
     * @param mpRegisteredParameters - map to cache all registered parameters. The 
     *                                 key is String parameter name and the value 
     *                                 is an a ThreeObjectStruct where the element 
     *                                 1. is Integer constant (one of the 
     *                                 PARAMETER_TYPE_XXX constants) representing 
     *                                 the type. 
     *                                 2. is Object representing the default 
     *                                 value. This can be static variable that 
     *                                 can be shared between all instances.
     *                                 3. is String representing user friendly 
     *                                 name of the property
     *                                 If this map is empty, the constructor will 
     *                                 invoke the registerParameters method to 
     *                                 register the parameters that will inserted 
     *                                 to this map. This allows you to pass in 
     *                                 static variable that can be shared between 
     *                                 all instances. If this map is not empty, 
     *                                 it is expected to contain all registered 
     *                                 parameters.
     */
    public SetupReader(
       String                         strBasePath,
       String                         strReaderName,
       Map<String, ThreeObjectStruct> mpRegisteredParameters
    )
    {
       super();
       
       if (GlobalConstants.ERROR_CHECKING)
       {
          assert strBasePath != null : "Base path cannot be null";
          assert strReaderName != null : "Reader name cannot be null";
          assert mpRegisteredParameters != null 
                : "Registered parameter map cannot be null";
       }
       
       m_strBasePath = strBasePath;
       m_strReaderName = strReaderName;
       m_mpRegisteredParameters = mpRegisteredParameters;
       // Here will be cached configuration values for current reader
      m_mpRegisteredParametersValues = new HashMap<String, Object>();
 
       if (mpRegisteredParameters.isEmpty())
       {
          // No parameters were registered yet so register them
          registerParameters();
       }
    }
    
    // Logic ////////////////////////////////////////////////////////////////////
    
    /**
     * Read parameter with specified name. The parameter name has to be 
     * registered using registerParameter. If such parameter is not registered it 
     * returns null.
     * 
     * @param strParameterName - name of parameter
     * @return Object - object with parameter value or null of parameter name is 
     *                  not registered. If you pass in correct parameter name, 
     *                  this method guarantees to return not null value. In case 
     *                  of failure it will return the hardcoded value specified 
     *                  parameter registration during.
     */ 
    public Object getParameterValue(
       String strParameterName
    )
    {
       Object            objRetval = null;
       ThreeObjectStruct parameter;
       
       parameter = m_mpRegisteredParameters.get(strParameterName);
       if (parameter != null)
       {
          // Try to get parameter value from cached values
          objRetval = m_mpRegisteredParametersValues.get(strParameterName);
          if (objRetval == null)
          {
            StringBuffer sbFullPropertyName = new StringBuffer(m_strBasePath);
            Integer      iParameterType = null;
            Object       objDefaultValue = null;
            String       strDisplayName;
            String       strPropertyValue;
             
             iParameterType = (Integer)parameter.getFirst();
             objDefaultValue = parameter.getSecond();
             strDisplayName = (String)parameter.getThird();
             
             sbFullPropertyName.append(".");
             sbFullPropertyName.append(m_strReaderName);
             sbFullPropertyName.append(".");
             sbFullPropertyName.append(strParameterName);
             
             // Read value from properties
             Properties prpSettings = Config.getInstance().getProperties();
             strPropertyValue = PropertyUtils.getStringProperty(
                                   prpSettings, sbFullPropertyName.toString(), 
                                   null, "TODO", true);
             if (strPropertyValue == null)
             {
                objRetval = objDefaultValue;
             }
             else
             {
                objRetval = parseValue(iParameterType.intValue(), 
                                       strPropertyValue, objDefaultValue,
                                       sbFullPropertyName.toString(),
                                       strDisplayName);
                if (objRetval != null)
                {
                   // Remember the value
                   m_mpRegisteredParametersValues.put(strParameterName, objRetval);
                }
             }
          }
       }
       
       return objRetval;
    }
    
    /**
     * Get parameter value as string
     * 
     * @param parameterName - name of parameter
     * @return String - value of parameter. It will be default value if 
     *                  parameter was not found or null if parameter name is 
     *                  not registered
     */
    public String getStringParameterValue(
       String parameterName
    )
    {
       return (String)getParameterValue(parameterName);
    }
 
    /**
     * Get parameter value as Integer
     * 
     * @param parameterName - name of parameter
     * @return Integer - value of parameter. It will be default value if 
     *                  parameter was not found or null if parameter name is 
     *                  not registered
     */
    public Integer getIntegerParameterValue(
       String parameterName
    )
    {
       return (Integer)getParameterValue(parameterName);
    }
 
    /**
     * Get parameter value as Double
     * 
     * @param parameterName - name of parameter
     * @return Double - value of parameter. It will be default value if 
     *                  parameter was not found or null if parameter name is 
     *                  not registered
     */
    public Double getDoubleParameterValue(
       String parameterName
    )
    {
       return (Double)getParameterValue(parameterName);
    }
 
    /**
     * Get parameter value as Float
     * 
     * @param parameterName - name of parameter
     * @return Float - value of parameter. It will be default value if 
     *                  parameter was not found or null if parameter name is 
     *                  not registered
     */
    public Float getFloatParameterValue(
       String parameterName
    )
    {
       return (Float)getParameterValue(parameterName);
    }   
    
    /**
     * Get parameter value as Boolean
     * 
     * @param parameterName - name of parameter
     * @return Boolean - value of parameter. It will be default value if 
     *                  parameter was not found or null if parameter name is 
     *                  not registered
     */
    public Boolean getBooleanParameterValue(
       String parameterName
    )
    {
       return (Boolean)getParameterValue(parameterName);
    }
    
    /**
     * Get parameter value as Long
     * 
     * @param parameterName - name of parameter
     * @return Long - value of parameter. It will be default value if 
     *                  parameter was not found or null if parameter name is 
     *                  not registered
     */
    public Long getLongParameterValue(
       String parameterName
    )
    {
       return (Long)getParameterValue(parameterName);
    }
 
    // Helper methods ///////////////////////////////////////////////////////////
 
    /**
     * Register parameters which will be read from the property file with this
     * reader. This method will try to read from the property file value common
     * to all readers and if it doesn't find one it will use specified default 
     * value.
     * 
     * @param strParameterName - parameter string name. This is just the part of 
     *                           the name which follows the base path and 
     *                           optionally reader name
     * @param iParameterType - parameter type, one of PARAMETER_TYPE_XXX 
     *                         constants
     * @param strDefaultValue - parameter default value - as string, this should 
     *                          be always valid hardcoded string that can be 
     *                          parsed into Object representation matching the
     *                          parameter type
     * @param strDisplayName - user friendly name of the property
     * @param strBasePath - base path of this reader
     * @param prpSettings - properties containing configuration settings from 
     *                      which to read the values
     * @param mpRegisteredParameters - map to cache all registered parameters. The 
     *                                 key is String parameter name and the value 
     *                                 is an a ThreeObjectStruct where the element 
     *                                 1. is Integer constant (one of the 
     *                                 PARAMETER_TYPE_XXX constants) representing 
     *                                 the type. 
     *                                 2. is Object representing the default 
     *                                 value. This can be static variable that 
     *                                 can be shared between all instances.
     *                                 3. is String representing user friendly 
     *                                 name of the property
     */
    protected static void registerParameter(
       String                         strParameterName,
       Integer                        iParameterType,
       String                         strDefaultValue,
       String                         strDisplayName,
       String                         strBasePath,
       Properties                     prpSettings,
       Map<String, ThreeObjectStruct> mpRegisteredParameters
    )
    {
      StringBuffer      sbFullPropertyName = new StringBuffer(strBasePath);
       String            strNewDefaultValue;
       Object            objDefault;
       Object            objHardcodedDefault;
       ThreeObjectStruct parameter;
       
       objHardcodedDefault = parseValue(iParameterType.intValue(), 
                                        strDefaultValue, null, strParameterName,
                                        strDisplayName);
       if (GlobalConstants.ERROR_CHECKING)
       {
          assert objHardcodedDefault != null 
                 : "Hardcoded default value should be always parsable.";
       }
 
       // Try to read what is the default value for this property (common value 
       // for all readers without the reader name) and only if it is not there
       // only then use the passed in default value
       sbFullPropertyName.append(".");
       sbFullPropertyName.append(strParameterName);         
       // Empty value is usually allowed
       strNewDefaultValue = PropertyUtils.getStringProperty(
                               prpSettings, sbFullPropertyName.toString(), 
                               strDefaultValue, strDisplayName, true, false);      
       objDefault = parseValue(iParameterType.intValue(), 
                               strNewDefaultValue,
                               objHardcodedDefault, 
                               sbFullPropertyName.toString(),
                               strDisplayName);
 
       parameter = new ThreeObjectStruct(iParameterType, objDefault, 
                                         strDisplayName);
       mpRegisteredParameters.put(strParameterName, parameter);
    }
    
    /**
     * Parse specified value according to its type and log config message for the
     * value that will be used.
     * 
     * @param iParameterType - parameter type, one of PARAMETER_TYPE_XXX constants
     * @param strPropertyValue - value to parse
     * @param objDefaultValue - default value to return if the specified value
     *                          cannot be parsed
     * @param strPropertyName - name of the property to use in error reporting
     * @param strDisplayName - user friendly name of the property
     * @return Object - parsed object or null if it cannot be parsed
     */
    protected static Object parseValue(
       int    iParameterType,
       String strPropertyValue,
       Object objDefaultValue,
       String strPropertyName,
       String strDisplayName
    )
    {
       Object objRetval = null;
       String strReason = "";
    
       if (strPropertyValue != null)
       {
          switch (iParameterType)
          {
             case PARAMETER_TYPE_STRING :
             {
                objRetval = strPropertyValue;
                break;
             }
             case PARAMETER_TYPE_INTEGER :
             {
                try
                {
                   objRetval = new Integer(strPropertyValue);
                }
                catch (NumberFormatException exec)
                {
                   strReason = " cannot be parsed into an integer";
                }
                break;
             }
             case PARAMETER_TYPE_DOUBLE :
             {
                try
                {
                   objRetval = new Double(strPropertyValue);
                }
                catch (NumberFormatException exec)
                {
                   strReason = " cannot be parsed into a double";
                }
                break;
             }
             case PARAMETER_TYPE_FLOAT :
             {
                try
                {
                   objRetval = new Float(strPropertyValue);
                }
                catch (NumberFormatException exec)
                {
                   strReason = " cannot be parsed into a float";
                }
                break;
             }
             case PARAMETER_TYPE_BOOLEAN :
             {
                objRetval = GlobalConstants.isBoolean(strPropertyValue);
                if (objRetval == null)
                {
                   strReason = " cannot be parsed into a boolean";
                }
                break;
             }
             case PARAMETER_TYPE_LONG :
             {
                try
                {
                   objRetval = new Long(strPropertyValue);
                }
                catch (NumberFormatException exec)
                {
                   strReason = " cannot be parsed into a long";
                }
                break;
             }
             case PARAMETER_TYPE_PATH :
             {
                char   cTemp;
                String strTrim;
                
                strTrim = strPropertyValue.trim(); 
                cTemp = strTrim.charAt(strTrim.length() - 1);
                if ((cTemp != File.separatorChar)
                    // On Windows it is allowed to end with / since java will handle it
                    && (GlobalConstants.isWindows() && (cTemp != '/')))
                {
                   strReason = "  does not end with path separator character";
                }
                else
                {
                   objRetval = strPropertyValue;
                }
                break;
             }
             default:
             {
                assert false : "Unknown property value type";
             }
          }
       }
 
       if ((objRetval == null) && (objDefaultValue != null))
       {
          objRetval = objDefaultValue;
          PropertyUtils.printConfigMessage(
                           strPropertyName, String.valueOf(objDefaultValue), 
                           "Value " + String.valueOf(strPropertyValue)
                           + " for " + strDisplayName + " specified in property " 
                           + strPropertyName + strReason 
                           + ", using default value "
                           + String.valueOf(objDefaultValue));
       }
       
       return objRetval;
    }
 
    /**
     * Register parameters which will be read from the property file with this
     * reader. This method will try to read from the property file value common
     * to all readers and if it doesn't find one it will use specified default 
     * value.
     * 
     * @param strParameterName - parameter string name. This is just the part of 
     *                           the name which follows the base path and 
     *                           optionally reader name
     * @param iParameterType - parameter type, one of PARAMETER_TYPE_XXX 
     *                         constants
     * @param strDefaultValue - parameter default value - as string, this should 
     *                          be always valid hardcoded string that can be 
     *                          parsed into Object representation matching the
     *                          parameter type
     * @param strDisplayName - user friendly name of the property
     */
    protected void registerParameter(
       String  strParameterName,
       Integer iParameterType,
       String  strDefaultValue,
       String  strDisplayName
    )
    {
       Properties prpSettings = Config.getInstance().getProperties();
       registerParameter(strParameterName, iParameterType, strDefaultValue,
                         strDisplayName, m_strBasePath, prpSettings,
                         m_mpRegisteredParameters);
    }
 
    /**
     * Implementation can register all parameters in this function.
     */
    protected abstract void registerParameters();
 }
