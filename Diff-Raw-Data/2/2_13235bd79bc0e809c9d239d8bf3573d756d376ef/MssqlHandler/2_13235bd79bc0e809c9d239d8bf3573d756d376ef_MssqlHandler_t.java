 /*
  * Copyright (c) 2013, Tripwire, Inc.
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are
  * met:
  *
  *  o Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  *
  *  o Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
  * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package org.jmxdatamart.common;
 
 import org.slf4j.LoggerFactory;
 
 import java.sql.*;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Xiao Han
  * To change this template use File | Settings | File Templates.
  */
 public class MssqlHandler extends DBHandler {
     private final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());
    private final String driver = "net.sourceforge.jtds.jdbc.Driver";
     private String jdbcurl ;
     private final String tableSchem = "dbo";
     public String getJdbcurl() {
         return jdbcurl;
     }
     public void setJdbcurl(String jdbcurl) {
         this.jdbcurl = jdbcurl;
     }
     @Override
     public String getTableSchema() {
         return tableSchem;
     }
 
     @Override
     public boolean connectServer( Properties p){
         try {
             Class.forName(this.driver);
             DriverManager.getConnection(this.jdbcurl, p);
         }
         catch (ClassNotFoundException ce){
             logger.error("Can't connect to the server. Check JDBC driver.", ce);
             return false;
         }
         catch (SQLException se){
             logger.error("Can't connect to the server. Check username/password and connection", se);
             return false;
         }
         return true;
     }
 
     @Override
     public Connection connectDatabase(String databasename, Properties p){
         Connection conn =null;
         PreparedStatement ps = null;
         ResultSet rs =  null;
         try {
             Class.forName(this.driver);
 
             conn = DriverManager.getConnection(this.jdbcurl, p);
             String sql = "Select count(*) from master.sys.databases where name=? ";
             ps = conn.prepareStatement(sql);
             ps.setString(1, databasename);
             rs = ps.executeQuery();
             if (rs.next() && rs.getInt(1)==0){
                 sql = "CREATE DATABASE " + databasename;
                 ps = conn.prepareStatement(sql);
                 ps.executeUpdate();
             }
             return DriverManager.getConnection(this.jdbcurl+";database="+databasename, p);
         }
         catch (ClassNotFoundException ce){
             logger.error("Can't loader the JDBC driver." + ce.getMessage(), ce);
             throw new RuntimeException(ce);
         }
         catch (SQLException se){
             logger.error(se.getMessage(), se);
             throw new RuntimeException(se);
         }
         finally {
             DBHandler.releaseDatabaseResource(rs,null,ps,null);
         }
     }
 
 
     @Override
     public boolean databaseExists(String databaseName,Properties p){
         Connection conn =null;
         PreparedStatement ps = null;
         ResultSet rs =  null;
         try {
             Class.forName(this.driver);
             conn = DriverManager.getConnection(this.jdbcurl, p);
             String sql = "Select count(*) from master.sys.databases where name=? ";
             ps =conn.prepareStatement(sql);
             ps.setString(1,databaseName);
             rs = ps.executeQuery();
             return (rs.next() && rs.getInt(1)==1);
         }
         catch (ClassNotFoundException ce){
             logger.error("Can't loader the JDBC driver", ce);
             return false;
         }
         catch (SQLException e){
             return false;
         }
         finally {
             DBHandler.releaseDatabaseResource(rs,null,ps,conn);
         }
     }
 
 
 }
