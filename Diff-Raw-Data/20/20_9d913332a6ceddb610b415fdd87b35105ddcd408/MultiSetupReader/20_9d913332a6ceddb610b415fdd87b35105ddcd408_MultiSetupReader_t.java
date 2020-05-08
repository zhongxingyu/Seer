 /*
  * Copyright (C) 2008 - 2012 OpenSubsystems.com/net/org and its owners. All rights reserved.
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
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 /**
  * Class that enables reading related properties from property files that can be
  * overridden multiple times in a defined priority order. It allows to specify 
  * set of default values and then override the values for a set of properties 
  * with specified name multiple times in a defined priority order. If an 
  * overridden property with the specified name doesn't exist, the next name 
  * specified in priority order is used until the value is found or until the 
  * prioritized names are all used and then the default property will be used.
  * 
  * Each property name consist from three parts. 
  * 1. base path, for example oss.my.application
  * 2. Prioritized reader names, for example 
  *    {specific.subsystem, specific, subsystem}
  * 3. parameter name, for example version
  *  
  * Property name looks like <basepath>.<readername>.<parametername> and are 
  * looked for example in this order
  * 1. oss.my.application.specific.subsystem.version
  * 2. oss.my.application.specific.version
  * 3. oss.my.application.subsystem.version
  * 4. oss.my.application.version (default value)
  * 
  * @author bastafidli
  */
 public abstract class MultiSetupReader extends SetupReader
 {
    // Attributes ///////////////////////////////////////////////////////////////
    
    /**
     * Prioritized list of name to be used by this reader. These names are added 
     * to base path to get full parameter names if using the previous name didn't
     * lead to result.
     */
    protected List<String> m_lstReaderNames;
    
    // Constructors /////////////////////////////////////////////////////////////
    
    /**
     * @param strBasePath - base path of reader
     * @param lstReaderNames - prioritized list of name to be used by this reader. 
     *                         These names are added to base path to get full 
     *                         parameter names if using the previous name didn't
     *                         lead to result.
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
    public MultiSetupReader(
       String                         strBasePath,
       List<String>                   lstReaderNames,
       Map<String, ThreeObjectStruct> mpRegisteredParameters
    )
    {
       // Pass the first reader name to the base class since that will simulate 
       // the default functionality (most important name or default value)
      super(strBasePath, lstReaderNames.get(0), mpRegisteredParameters);
       
       m_lstReaderNames = lstReaderNames;
    }
 
    /**
     * {@inheritDoc}
     */ 
   @Override
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
            StringBuilder sbFullPropertyName = new StringBuilder();
            Integer       iParameterType;
            Object        objDefaultValue;
            String        strDisplayName;
            String        strPropertyValue;
            String        strReaderName;
             
             iParameterType = (Integer)parameter.getFirst();
             objDefaultValue = parameter.getSecond();
             strDisplayName = (String)parameter.getThird();
 
             for (Iterator<String> names = m_lstReaderNames.iterator(); 
                  (names.hasNext()) && (objRetval == null);)
             {
                strReaderName = names.next();
                sbFullPropertyName.delete(0, sbFullPropertyName.length());
                sbFullPropertyName.append(m_strBasePath);
                sbFullPropertyName.append(".");
                sbFullPropertyName.append(strReaderName);
                sbFullPropertyName.append(".");
                sbFullPropertyName.append(strParameterName);
             
                // Read value from properties
                Properties prpSettings = Config.getInstance().getProperties();
                strPropertyValue = PropertyUtils.getStringProperty(
                                      prpSettings, sbFullPropertyName.toString(), 
                                      null, "TODO", true);
             
                if (strPropertyValue != null)
                {
                   objRetval = parseValue(iParameterType.intValue(), 
                                          strPropertyValue, objDefaultValue,
                                          sbFullPropertyName.toString(),
                                          strDisplayName);
                   if (objRetval != null)
                   {
                      // Remember the value
                      m_mpRegisteredParametersValues.put(strParameterName, 
                                                         objRetval);
                   }
                }
             }
             
             if (objRetval == null)
             {
                objRetval = objDefaultValue;
             }
          }
       }
       
       return objRetval;
 
    }
 }
