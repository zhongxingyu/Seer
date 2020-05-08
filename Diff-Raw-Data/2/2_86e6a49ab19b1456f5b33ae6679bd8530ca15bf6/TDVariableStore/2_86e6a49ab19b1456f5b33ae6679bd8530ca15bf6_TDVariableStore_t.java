 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.client.core.businessprocess;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.exception.Assert;
 
 
 /**
  * @author BREDEX GmbH
  * @created 18.07.2006
  */
 public class TDVariableStore {
     /** Prefix for a user defined variable of the system environment */
    public static final String USERDEF_ENV_VAR_PREFIX = "TEST_UDV_"; //$NON-NLS-1$
         
     /** Prefix for a pre-defined test variable */
     public static final String PREDEF_VAR_PREFIX = "GD_"; //$NON-NLS-1$
     
     /** id for the predefined language variable */
     public static final String VAR_LANG = PREDEF_VAR_PREFIX + "LANGUAGE"; //$NON-NLS-1$
 
     /** id for the predefined testsuite name variable */
     public static final String VAR_TS = PREDEF_VAR_PREFIX + "TESTSUITE"; //$NON-NLS-1$
     
     /** id for the predefined username variable */
     public static final String VAR_USERNAME = PREDEF_VAR_PREFIX + "USERNAME"; //$NON-NLS-1$
     
     /** id for the predefined db username variable */
     public static final String VAR_DBUSERNAME = 
         PREDEF_VAR_PREFIX + "DBUSERNAME"; //$NON-NLS-1$
 
     /** id for the predefined AutStarter hostname variable */
     public static final String VAR_AUTSTARTER = 
         PREDEF_VAR_PREFIX + "AUTSTARTER"; //$NON-NLS-1$
 
     /** id for the predefined AutStarter port variable */
     public static final String VAR_PORT = PREDEF_VAR_PREFIX + "PORTNUMBER"; //$NON-NLS-1$
 
     /** id for the predefined AUT name variable */
     public static final String VAR_AUT = PREDEF_VAR_PREFIX + "AUT"; //$NON-NLS-1$
     
     /** id for the predefined AUT configuration name variable */
     public static final String VAR_AUTCONFIG = 
         PREDEF_VAR_PREFIX + "AUTCONFIG"; //$NON-NLS-1$
     
     /** id for the predefined Jubula client version variable */
     public static final String VAR_CLIENTVERSION = 
         PREDEF_VAR_PREFIX + "CLIENTVERSION"; //$NON-NLS-1$
     
     
     /** Holds the key value pairs */
     private Map<String, String> m_variables;
     
     /**
      * Constructor
      */
     public TDVariableStore() {
         m_variables = new HashMap<String, String>();
     }
 
    
     /**
      * Stores the given value with the given key
      * @param varName the name of the variable to store
      * @param value the value of the variable to store
      */
     public void store(String varName, String value) {
         m_variables.put(varName, value);
     }
     
     /**
      * Gets the value of the given variable name
      * @param varName the name of the variable which value to get
      * @return the value of the given variable name
      */
     public String getValue(String varName) {
         return m_variables.get(varName);
     }
     
     /**
      * Clears the VariableStore, removes all variables and their
      * values from this store.
      */
     public void clear() {
         m_variables.clear();
     }
     
     
     /**
      * Reads and stores the user defined variables set via environment
      * into the variable store.
      */
     public void storeEnvironmentVariables() {
         Map< ? extends Object, ? extends Object > vars = System.getenv();
         storeEnvVars(vars);
         vars = System.getProperties();
         storeEnvVars(vars);
     }
 
 
     /**
      * @param map the map of environment variables
      */
     private void storeEnvVars(Map< ? extends Object, ? extends Object > map) {
         final Set< ? extends Object > keys = map.keySet();
         for (Object oKey : keys) {
             Assert.verify(oKey instanceof String, "Key: '"  //$NON-NLS-1$
                     + String.valueOf(oKey) + "' is not a String"); //$NON-NLS-1$
             final String key = (String)oKey;
             if (key.startsWith(USERDEF_ENV_VAR_PREFIX)) {
                 String value = System.getenv(key);
                 if (value == null) {
                     value = System.getProperty(key);
                 }
                 final String varName = 
                     key.substring(USERDEF_ENV_VAR_PREFIX.length());
                 store(varName, value);
             }
         }
     }
     
     /**
      * 
      * {@inheritDoc}
      */
     public String toString() {
         return super.toString() + StringConstants.SPACE 
             + m_variables.toString();
     }
 }
