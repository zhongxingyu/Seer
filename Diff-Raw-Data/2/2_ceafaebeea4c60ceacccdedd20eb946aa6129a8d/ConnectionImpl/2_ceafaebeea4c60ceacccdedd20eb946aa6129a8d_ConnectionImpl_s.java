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
 
 import java.sql.CallableStatement;
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import org.apache.log4j.Logger;
 
 public class ConnectionImpl {
 
     private final Logger logger = Logger.getLogger(ConnectionImpl.class);
 
     private Connection connection;
 
     private boolean managingTransaction = true;
 
     private String generatedKeysSupported = null;
     
     public ConnectionImpl(Connection connection) {
         this.connection = connection;
 
         try {
             if (connection.getAutoCommit()) {
                 throw new RuntimeException("AutoCommit must be off");
             }
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
 
     }
 
     public Connection getJDBCConnection() {
         return connection;
     }
 
     public String getGeneratedKeysSupported() {
         return this.generatedKeysSupported;
     }
     
     public void setGeneratedKeysSupported(String useGetGeneratedKeys){
         this.generatedKeysSupported = useGetGeneratedKeys;
     }    
     
     public boolean isGeneratedKeysSupported() {
         try{
             if(this.generatedKeysSupported == null){
                 DatabaseMetaData dbmsMetadata = this.connection.getMetaData();
                 boolean supportsGetGeneratedKeys = dbmsMetadata.supportsGetGeneratedKeys();
                 if(supportsGetGeneratedKeys){
                     this.generatedKeysSupported = "true";
                 }
                 //currently DERBY partially supports this feature and thus returns FALSE,
                 //this hardcoding is needed as the partial support is enough for DAS
                 //we can remove this later, when DERBY change the behaviour of it's "supportsGetGeneratedKeys"
                 else if(dbmsMetadata.getDatabaseProductName().indexOf("Derby") > 0){                    
                     this.generatedKeysSupported = "true";
                 }
                 else{
                     this.generatedKeysSupported = "false";
                 }
             }           
         }catch(Exception e){//can be from supportsGetGeneratedKeys or due to absense of supportsGetGeneratedKeys
             if (this.logger.isDebugEnabled()) {
                 this.logger.debug("exception setiing useGetGeneratedKeys false");
             }
             this.generatedKeysSupported = "false";
         }
         
         if (this.logger.isDebugEnabled()) {
             this.logger.debug("returning useGetGeneratedKeys():"+ this.generatedKeysSupported);
         }
        return Boolean.parseBoolean(this.generatedKeysSupported);
     }
     
     public void cleanUp() {
         try {
             if (managingTransaction) {
                 if (this.logger.isDebugEnabled()) {
                     this.logger.debug("Committing Transaction");
                 }
                 connection.commit();
             }
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     public void errorCleanUp() {
         try {
             if (managingTransaction) {
                 if (this.logger.isDebugEnabled()) {
                     this.logger.debug("Rolling back Transaction");
                 }
                 connection.rollback();
             }
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
     }
 
     public PreparedStatement prepareStatement(String queryString, String[] returnKeys) throws SQLException {
         if (this.logger.isDebugEnabled()) {
             this.logger.debug("Preparing Statement: " + queryString);
             this.logger.debug("Boolean value for use gen key: " + this.generatedKeysSupported);
         }
 
         if (isGeneratedKeysSupported()) {
             return connection.prepareStatement(queryString, Statement.RETURN_GENERATED_KEYS);
         } else if (returnKeys.length > 0) {
             return connection.prepareStatement(queryString, returnKeys);
         }
 
         return connection.prepareStatement(queryString);
     }
     
     public PreparedStatement preparePagedStatement(String queryString) throws SQLException {
         if (this.logger.isDebugEnabled()) {
             this.logger.debug("Preparing Statement: " + queryString);
         }
 
         return connection.prepareStatement(queryString, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
     }
 
     public void setManageTransactions(boolean manageTransactions) {
         managingTransaction = manageTransactions;
 
     }
 
     public CallableStatement prepareCall(String queryString) throws SQLException {
         return connection.prepareCall(queryString);
     }
 }
