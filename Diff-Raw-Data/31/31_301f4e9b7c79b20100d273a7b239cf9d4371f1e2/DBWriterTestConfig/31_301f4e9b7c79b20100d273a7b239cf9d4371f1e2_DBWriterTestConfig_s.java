 /*___INFO__MARK_BEGIN__*/
 /*************************************************************************
  *
  *  The Contents of this file are made available subject to the terms of
  *  the Sun Industry Standards Source License Version 1.2
  *
  *  Sun Microsystems Inc., March, 2001
  *
  *
  *  Sun Industry Standards Source License Version 1.2
  *  =================================================
  *  The contents of this file are subject to the Sun Industry Standards
  *  Source License Version 1.2 (the "License"); You may not use this file
  *  except in compliance with the License. You may obtain a copy of the
  *  License at http://gridengine.sunsource.net/Gridengine_SISSL_license.html
  *
  *  Software provided under this License is provided on an "AS IS" basis,
  *  WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING,
  *  WITHOUT LIMITATION, WARRANTIES THAT THE SOFTWARE IS FREE OF DEFECTS,
  *  MERCHANTABLE, FIT FOR A PARTICULAR PURPOSE, OR NON-INFRINGING.
  *  See the License for the specific provisions governing your rights and
  *  obligations concerning the Software.
  *
  *   The Initial Developer of the Original Code is: Sun Microsystems, Inc.
  *
  *   Copyright: 2001 by Sun Microsystems, Inc.
  *
  *   All Rights Reserved.
  *
  ************************************************************************/
 /*___INFO__MARK_END__*/
 package com.sun.grid.reporting.dbwriter;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Properties;
 
 public class DBWriterTestConfig {
    
    private final static String CONFIG_FILE = "test/DBWriterTestConfig.properties";
    private final static String PRIVATE_CONFIG_FILE = "test/DBWriterTestConfig_private.properties";
    
    private String identifier;
    private String driver;
    private String url;
    private String user;
    private String password;
    private String readOnlyUser;
    private String schema;
    private int dbversion;
    private String dbdefinition;
    static private String debugLevel;
    private String dbHost;
    private String dbName;
   
    static private String testDebugLevel;
    static private String testCalculationFile;
    static private String testRawVariableSQL;
    static private String testHourVariableSQL;
    static private int testTimeout = 10;
    
    
    /**
     * Creates a new instance of DBWriterTestConfig
     */
    public DBWriterTestConfig() {
    }
    
    private static DBWriterTestConfig newInstance(Properties props, String prefix) {
       DBWriterTestConfig ret  = null;
       String str = props.getProperty(prefix + ".identifier");
       if (str != null) {
          ret = new DBWriterTestConfig();
          ret.identifier = str;
          ret.url = props.getProperty(prefix + ".url");
          ret.driver = props.getProperty(prefix + ".driver");
          ret.dbHost = props.getProperty(prefix + ".dbHost");
          ret.dbName = props.getProperty(prefix + ".dbName");
          ret.dbversion = Integer.parseInt(props.getProperty(prefix + ".dbversion"));
          ret.user = props.getProperty(prefix + ".user");
          ret.password = props.getProperty(prefix + ".password");
          ret.readOnlyUser = props.getProperty(prefix + ".readOnlyUser");
          ret.schema = props.getProperty(prefix + ".schema");
          ret.dbdefinition = props.getProperty(prefix + ".dbdefinition");
          ret.debugLevel = props.getProperty(prefix + ".debugLevel");
          
 //         System.out.println(prefix + ".identifier: " + ret.getIdentifier());
 //         System.out.println(prefix + ".driver: " + ret.getDriver());
 //         System.out.println(prefix + ".url: " + ret.getUrl());
 //         System.out.println(prefix + ".dbversion: " + ret.getDbversion());
 //         System.out.println(prefix + ".user: " + ret.getUser());
 //         System.out.println(prefix + ".password: " + ret.getPassword());
 //         System.out.println(prefix + ".readOnlyUser: " + ret.getReadOnlyUser());
 //         System.out.println(prefix + ".schema: " + ret.getSchema());
 //         System.out.println(prefix + ".dbdefinition: " + ret.getDbdefinition());
 //         System.out.println(prefix + ".debugLevel: " + ret.getDebugLevel());
 //         System.out.flush();
          
       }
       
       return ret;
    }
    
    public static DBWriterTestConfig[] getDatabases(String configFile, String testPrefix) throws IOException {
       
       Properties props = new Properties();
       
       InputStream in = null;
       
       File file = new File(configFile);
       if(file.exists()) {
           in = new FileInputStream(file);
       } else {
           ClassLoader cl = DBWriterTestConfig.class.getClassLoader();
           in = cl.getResourceAsStream(configFile);
           if (in == null) {
              in = cl.getResourceAsStream(PRIVATE_CONFIG_FILE);
              if(in == null) {
                 in = cl.getResourceAsStream(CONFIG_FILE);
              }
           }
       }      
       props.load(in);
       
       if (testPrefix != null) {
          testCalculationFile = props.getProperty(testPrefix + ".testDebugLevel");
          testCalculationFile = props.getProperty(testPrefix + ".calculationFile");
          testHourVariableSQL = props.getProperty(testPrefix + ".testHourVariableSQL");
          testRawVariableSQL = props.getProperty(testPrefix + ".testRawVariableSQL");
          String tout = props.getProperty(testPrefix + ".timeout");
          System.out.println("testPrefix: " + testPrefix);
          System.out.flush();
          if (tout != null) {
             testTimeout = Integer.parseInt(tout);
          }   
       }
       
       int i = 0;
       ArrayList list = new ArrayList();
       
       while(true) {
          DBWriterTestConfig conf = newInstance(props, "database[" + list.size() + "]");
          if(conf == null) {
             break;
          }
          list.add(conf);
       }
       
       DBWriterTestConfig[] ret = new DBWriterTestConfig[list.size()];
       list.toArray(ret);
       return ret;
    }
    
    public static DBWriterTestConfig[] getDatabases(String configFile) throws IOException {
       return getDatabases(configFile, null);
    }
    
    public String getIdentifier() {
       return identifier;
    }
    
    public String getDriver() {
       return driver;
    }
    
    public String getUrl() {
       return url;
    }
    
    public String getUser() {
       return user;
    }
    
    public String getPassword() {
       return password;
    }
    
    public String getReadOnlyUser() {
       return readOnlyUser;
    }
    
    public String getSchema() {
       return schema;
    }
    
    public int getDbversion() {
       return dbversion;
    }
    
    public String getDbdefinition() {
       return dbdefinition;
    }
    
    static public String getDebugLevel() {
       return debugLevel;
    }
    
    static public String getTestDebugLevel() {
       return testDebugLevel;
    }
    
    static public String getTestCalculationFile() {
       return testCalculationFile;
    }
    
    static public String getTestRawVariableSQL() {
       return testRawVariableSQL;
    }
    
    static public String getTestHourVariableSQL() {
       return testHourVariableSQL;
    }
    
    static public int getTestTimeout() {
       return testTimeout;
    }
    
    public String getDbHost() {
        return dbHost;
    }
    
    public String getDbName () {
        return dbName;
    }
 }
