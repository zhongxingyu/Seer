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
 
 import com.sun.grid.reporting.dbwriter.db.Database;
 import com.sun.grid.util.SQLUtil;
 import com.sun.grid.util.sqlutil.Command;
 import java.util.Map;
 import java.util.logging.Level;
 
 /**
  * Database object for a test database
  */
 public class TestDB {
    
    private static final String [] TABLES = new String[] {
       "sge_job_usage", "sge_job_log", "sge_job_request",
       "sge_job", "sge_queue_values", "sge_queue",
       "sge_host_values", "sge_host", "sge_department_values",
       "sge_department", "sge_project_values", "sge_project",
       "sge_user_values", "sge_user", "sge_group_values", "sge_group",
       "sge_share_log", "sge_version", "sge_statistic_values", "sge_statistic",
       "sge_checkpoint", "sge_ar_attribute", "sge_ar_log",
       "sge_ar_resource_usage", "sge_ar_usage", "sge_ar"
    };
    
    private static final String [] VIEWS = new String [] {
      "view_ar_time_usage", "view_job_times_subquery", "view_job_times", "view_jobs_completed",
       "view_job_log", "view_department_values", "view_group_values",
       "view_host_values",  "view_project_values", "view_queue_values",
       "view_user_values", "view_ar_attribute", "view_ar_log", "view_ar_usage",
       "view_ar_resource_usage", "view_accounting", "view_statistic"
    };
 
    public static final String DEFAULT_DEBUG_LEVEL = Level.INFO.toString();
    private SQLUtil sqlUtil = new SQLUtil();
    
    private DBWriterTestConfig config;
    private String debugLevel;
    
    /** Creates a new instance of TestDB */
    public TestDB(DBWriterTestConfig config) {
       
       this.config = config;
       
       setDebugLevel(DEFAULT_DEBUG_LEVEL);      
    }
 
    protected String getDBIdentifier() {
       return config.getIdentifier();      
    }
    
    
    protected String getJDBCDriver() {
       return config.getDriver();      
    }
    
    protected String getJDBCUrl() {
       return config.getUrl();      
    }
    
    protected String getJDBCUser() {
       return config.getUser();  
    }
 
    protected String getJDBCPassword() {
       return config.getPassword();  
    }
    
    protected String getReadOnlyUser() {
       return config.getReadOnlyUser();
    }
    
    protected String getReadOnlyUserPwd() {
       return config.getReadOnlyUserPwd();
    }
    
    protected String getDbHost() {
       return config.getDbHost();
    }
    
    protected String getDbName() {
        return config.getDbName();
    }
    
    protected String getSchema() {
       return config.getSchema();
    }
    
    protected String getTablespace() {
        return config.getTablespace();
    }
    
    protected String getIndexTablespace() {
        return config.getIndexTablespace();
    }
    
    int dbversion = -1;
    protected int getDBVersion() {
       if( dbversion < 0 ) {
          dbversion = config.getDbversion();
       }
       return dbversion;
    }
    
    private int dbType = -1;
    
    public int getDBType() {
       if ( dbType < 0 ) {
          String driver = getJDBCDriver();   
          dbType = Database.getDBType(driver);
          if ( dbType < 0 ) {
             throw new IllegalStateException("Can not determine dbtype for jdbc driver " + driver); 
          }   
       }
       return dbType;
    }
    
    protected String getDBDefinition() {
       return config.getDbdefinition();
    }
 
    /**
     * Installs the ARCO database.
     * <b>!!Attention!!</b> 
     * All existing tables and views of the database will be dropped.
     * @throws Exception
     */   
    protected int installDB() throws Exception {
 
       connect();
       // for Oracle database we create synonyms as a read user
       if (getDBType() == Database.TYPE_ORACLE) {
          connectReadUser();
       }
       dropDB();
       
       setEnv("READ_USER", getReadOnlyUser() );
       setEnv("DB_SCHEMA", getSchema());
       setEnv("DB_USER", getJDBCUser() );
       setEnv("DB_HOST", getDbHost());
       setEnv("DB_NAME", getDbName());
       setEnv("TABLESPACE", getTablespace());
       setEnv("TABLESPACE_INDEX", getIndexTablespace());
       
       Command cmd = sqlUtil.getCommand( "install" );
       
       return cmd.run( getDBVersion() + " " + getDBDefinition() + " " + getSchema() );      
    }
    
    private void connect() {
       
       if( sqlUtil.getConnection() == null ) {      
          Command cmd = sqlUtil.getCommand( "connect" );
          int result = cmd.run( getJDBCDriver() + " " + getJDBCUrl() 
                              + " " + getJDBCUser() + " " + getJDBCPassword() );
       }
    }
    
    private void connectReadUser() {
       
       if( sqlUtil.getConnection2() == null ) {      
          setEnv("SYNONYMS", "1");
          Command cmd = sqlUtil.getCommand( "connect" );
          int result = cmd.run( getJDBCDriver() + " " + getJDBCUrl() 
                              + " " + getReadOnlyUser() + " " + getReadOnlyUserPwd() );
          setEnv("SYNONYMS", "0");
       }
    }
       
    private void dropStandardDB() {
       int result = 0;
       Command cmd = sqlUtil.getCommand( "drop" );
       Command debugCmd = sqlUtil.getCommand("debug");
       String orgLevel = getDebugLevel();
       try {
          setDebugLevel("FINE");
          for( int i = 0; i < VIEWS.length; i++ ) {
             result = cmd.run( " VIEW " + VIEWS[i] );
          }
          for( int i = 0; i < TABLES.length; i++ ) {
             result = cmd.run( " TABLE " + TABLES[i] );
          }      
       } finally {
          setDebugLevel(orgLevel);
       }
    }
    
    private void dropOracleDB() {
       
       String rdUser = getReadOnlyUser();
       int result = 0;
       Command cmd = sqlUtil.getCommand( "drop" );
       String orgLevel = getDebugLevel();
       setDebugLevel("FINE");
       try {
          setEnv("SYNONYMS", "1");
          for( int i = 0; i < VIEWS.length; i++ ) {
             result = cmd.run( " SYNONYM " + rdUser + "." + VIEWS[i] );
          }
          for( int i = 0; i < TABLES.length; i++ ) {
             result = cmd.run( " SYNONYM " + rdUser + "." + TABLES[i] );
          }
          setEnv("SYNONYMS", "0");
          for( int i = 0; i < VIEWS.length; i++ ) {
             result = cmd.run( " VIEW " + VIEWS[i] + " CASCADE CONSTRAINTS" );
          }
          for( int i = 0; i < TABLES.length; i++ ) {
             result = cmd.run( " TABLE " + TABLES[i]+ " CASCADE CONSTRAINTS" );
          }      
       } finally {
          setDebugLevel(orgLevel);
       }
    }
    
    protected void dropDB() {
       
       connect();
 
       switch( getDBType() ) {
          case Database.TYPE_POSTGRES:
          case Database.TYPE_MYSQL:
             dropStandardDB();
             break;
          case Database.TYPE_ORACLE:
             dropOracleDB();
             break;
          default:
             throw new IllegalStateException("Unknown DB type (" + dbType +")");
       }
    }
    
    
    protected void cleanDB() {
       
       connect();
       
       Command cmd = sqlUtil.getCommand( "delete" );
       
       int result;
       for( int i = 0; i < TABLES.length; i++ ) {
          if( !TABLES[i].equalsIgnoreCase("SGE_VERSION") && !TABLES[i].equalsIgnoreCase("SGE_CHECKPOINT")) {
             result = cmd.run( "from " + TABLES[i] );
          }
       }      
    }
    
    public String getDebugLevel() {
       return debugLevel;
    }
    
    public void setDebugLevel(String debugLevel) {
       Command cmd = sqlUtil.getCommand( "debug" );
       if( cmd.run(debugLevel) != 0 ) {
          throw new IllegalStateException("debug command failed");
       }
       this.debugLevel = debugLevel;
    }
    
    private void setEnv( String name, String value ) {
       Command cmd = sqlUtil.getCommand( "set" );
       if( cmd.run( name + " " + value ) != 0 ) {
          throw new IllegalStateException("set env command failed");
       }      
    }
    
 }
