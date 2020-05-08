 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.    
  */
 package org.apache.tuscany.das.rdb.impl;
 
 import java.io.InputStream;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.sql.DataSource;
 
 import org.apache.tuscany.das.rdb.Command;
 import org.apache.tuscany.das.rdb.DAS;
 import org.apache.tuscany.das.rdb.config.Config;
 import org.apache.tuscany.das.rdb.config.ConfigFactory;
 import org.apache.tuscany.das.rdb.config.ConnectionInfo;
 import org.apache.tuscany.das.rdb.config.wrapper.MappingWrapper;
 import org.apache.tuscany.das.rdb.exception.DataSourceInitializationException;
 import org.apache.tuscany.das.rdb.util.ConfigUtil;
 
 import commonj.sdo.DataObject;
 
 /**
  * An ConfiguredCommandFactory produces instances of Command and ApplyChangesCommand. This 
  * factory is initialized with a configuration that defines
  * the commands it produces.
  * 
  */
 public class DASImpl implements DAS {
 
     private MappingWrapper configWrapper;
 
     private Connection connection;
 
     private Map commands = new HashMap();
 
     public DASImpl(InputStream stream) {
         this(ConfigUtil.loadConfig(stream));
 
     }
 
     public DASImpl(Config inConfig) {
         Config cfg = inConfig;
         if (cfg == null) {
             cfg = ConfigFactory.INSTANCE.createConfig();
         }
         this.configWrapper = new MappingWrapper(cfg);
 
         Iterator i = configWrapper.getConfig().getCommand().iterator();
         while (i.hasNext()) {
             org.apache.tuscany.das.rdb.config.Command commandConfig = 
                 (org.apache.tuscany.das.rdb.config.Command) i.next();
             String kind = commandConfig.getKind();
             if (kind.equalsIgnoreCase("select")) {
                 commands.put(commandConfig.getName(), new ReadCommandImpl(commandConfig.getSQL(), 
                                 configWrapper, commandConfig.getResultDescriptor()));
             } else if (kind.equalsIgnoreCase("update")) {
                 commands.put(commandConfig.getName(), new UpdateCommandImpl(commandConfig.getSQL()));
             } else if (kind.equalsIgnoreCase("insert")) {
                 commands.put(commandConfig.getName(), new InsertCommandImpl(commandConfig.getSQL(), new String[0]));
             } else if (kind.equalsIgnoreCase("delete")) {
                 commands.put(commandConfig.getName(), new DeleteCommandImpl(commandConfig.getSQL()));
             } else if (kind.equalsIgnoreCase("procedure")) {
                 commands.put(commandConfig.getName(), new SPCommandImpl(commandConfig.getSQL(), 
                         configWrapper, commandConfig.getParameter()));
             } else {
                 throw new RuntimeException("Invalid kind of command: " + kind);
             }
 
         }
 
     }
 
     public DASImpl(Config inConfig, Connection inConnection) {
         this(inConfig);
         setConnection(inConnection);
     }
 
     public DASImpl(InputStream configStream, Connection inConnection) {
         this(ConfigUtil.loadConfig(configStream), inConnection);
     }
 
     public DASImpl(Connection inConnection) {
         this(ConfigFactory.INSTANCE.createConfig());
         setConnection(inConnection);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.apache.tuscany.das.rdb.CommandGroup#getApplyChangesCommand()
      */
     public ApplyChangesCommandImpl getApplyChangesCommand() {
         ApplyChangesCommandImpl cmd = new ApplyChangesCommandImpl(configWrapper, connection);
         return cmd;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.apache.tuscany.das.rdb.CommandGroup#getCommand(java.lang.String)
      */
     public Command getCommand(String name) {
         if (!commands.containsKey(name)) {
             throw new RuntimeException("CommandGroup has no command named: " + name);
         }
         CommandImpl cmd = (CommandImpl) commands.get(name);
         cmd.setConnection(getConnection(), configWrapper.getConfig());
         return cmd;
     }
 
     public void setConnection(Connection connection) {
         this.connection = connection;
     }
 
     public Connection getConnection() {
         if (connection == null) {
             initializeConnection();
         }
         return connection;
     }
     
     private void initializeConnection() {
         Config config = configWrapper.getConfig();
         if (config == null || config.getConnectionInfo() == null 
                 || config.getConnectionInfo().getDataSource() == null) {
             throw new RuntimeException("No connection has been provided and no data source has been specified");
         }
 
         ConnectionInfo connectionInfo = configWrapper.getConfig().getConnectionInfo();
        if(connectionInfo.isUseDriveManager()){
            initializeDriveManagerConnection(connectionInfo);
         }else{
             initializeDatasourceConnection(connectionInfo);
         }
 
     }
 
     /**
      * Initializes a DB connection on a managed environmet (e.g inside Tomcat)
      */
     private void initializeDatasourceConnection(ConnectionInfo connectionInfo){
         Connection connection = null;
 
         InitialContext ctx;
         try {
             ctx = new InitialContext();
         } catch (NamingException e) {
             throw new RuntimeException(e);
         }
         try {
             DataSource ds = (DataSource) ctx.lookup(connectionInfo.getDataSource());
             try {
                 connection = ds.getConnection();
                 if (connection == null) {
                     throw new RuntimeException("Could not obtain a Connection from DataSource");
                 }
                 connection.setAutoCommit(false);
                 setConnection(connection);
             } catch (SQLException e) {
                 throw new RuntimeException(e);
             }
         } catch (NamingException e) {
             throw new RuntimeException(e);
         }
     }
     
     /**
      * Initialize a DB connection on a J2SE environment
      * For more info, see http://java.sun.com/j2se/1.3/docs/guide/jdbc/getstart/drivermanager.html
      */
    private void initializeDriveManagerConnection(ConnectionInfo connectionInfo) {
 
         Connection connection = null;
         
         if (connectionInfo.getConnectionProperties() == null) {
             throw new DataSourceInitializationException("No existing context and no connection properties");
         }
 
         if (connectionInfo.getConnectionProperties().getDriverClass() == null) {
             throw new DataSourceInitializationException("No jdbc driver class specified!");
         }
 
         try {
             //initialize driver and register it with DriverManager
             Class.forName(connectionInfo.getConnectionProperties().getDriverClass());
             
             //prepare to initialize connection
             String databaseUrl = connectionInfo.getDataSource();
             String userName = connectionInfo.getConnectionProperties().getUserName();
             String userPassword = connectionInfo.getConnectionProperties().getPassword();
             int loginTimeout = connectionInfo.getConnectionProperties().getLoginTimeout();
             
             DriverManager.setLoginTimeout(loginTimeout);
             if( (userName == null || userName.length() ==0) && (userPassword == null || userPassword.length()==0) ){
                 //no username or password suplied
                 connection = DriverManager.getConnection(databaseUrl);
             }else{
                 connection = DriverManager.getConnection(databaseUrl, userName, userPassword);
             }
             
             if(connection == null){
                 throw new DataSourceInitializationException("Error initializing connection : null");
             }
             
             connection.setAutoCommit(false);
             setConnection(connection);
                 
             
         }catch(ClassNotFoundException cnf){
             throw new DataSourceInitializationException("JDBC Driver '" + connectionInfo.getConnectionProperties().getDriverClass() + "' not found", cnf);
         }catch(SQLException sqle){
             throw new DataSourceInitializationException(sqle.getMessage(), sqle);
         }
 
     }
     
     public void releaseResources() {
 
         if (managingConnections()) {
             closeConnection();
         }
     }
 
     private void closeConnection() {
         if (connection != null) {
             try {
                 connection.close();
                 connection = null;
             } catch (SQLException e) {
                 throw new RuntimeException(e);
             }
         }
     }
 
     /**
      * If the config has connection properties then we are "managing" the connection via DataSource
      */
     private boolean managingConnections() {
 
         if (configWrapper.getConfig().getConnectionInfo().getDataSource() == null) {
             return false;
         }
 
         return true;
 
     }
 
     public Command createCommand(String sql) {
         return baseCreateCommand(sql, this.configWrapper);
     }
 
     public Command createCommand(String sql, Config config) {
         return baseCreateCommand(sql, new MappingWrapper(config));
     }
 
     private Command baseCreateCommand(String inSql, MappingWrapper config) {
         CommandImpl returnCmd = null;
         String sql = inSql.trim(); // Remove leading white space
         char firstChar = Character.toUpperCase(sql.charAt(0));
         switch (firstChar) {
             case 'S':
                 returnCmd = new ReadCommandImpl(sql, config, null);
                 break;
             case 'I':
                 returnCmd = new InsertCommandImpl(sql, new String[0]);
                 break;
             case 'U':
                 returnCmd = new UpdateCommandImpl(sql);
                 break;
             case 'D':
                 returnCmd = new DeleteCommandImpl(sql);
                 break;
             case '{':
                 returnCmd = new SPCommandImpl(sql, config, Collections.EMPTY_LIST);
                 break;
             default:
                 throw new RuntimeException("SQL => " + sql + " is not valid");
         }
 
         returnCmd.setConnection(getConnection(), config.getConfig());
         return returnCmd;
     }
 
     public void applyChanges(DataObject root) {
         getApplyChangesCommand().execute(root);
     }
 
 }
