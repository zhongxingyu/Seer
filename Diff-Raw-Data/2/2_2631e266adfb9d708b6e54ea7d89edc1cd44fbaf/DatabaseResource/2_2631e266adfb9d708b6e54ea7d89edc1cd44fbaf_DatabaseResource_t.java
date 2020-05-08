 /***************************************************************************
 *                                                                          *
 *  Organization: Lawrence Livermore National Lab (LLNL)                    *
 *   Directorate: Computation                                               *
 *    Department: Computing Applications and Research                       *
 *      Division: S&T Global Security                                       *
 *        Matrix: Atmospheric, Earth and Energy Division                    *
 *       Program: PCMDI                                                     *
 *       Project: Earth Systems Grid (ESG) Data Node Software Stack         *
 *  First Author: Gavin M. Bell (gavin@llnl.gov)                            *
 *                                                                          *
 ****************************************************************************
 *                                                                          *
 *   Copyright (c) 2009, Lawrence Livermore National Security, LLC.         *
 *   Produced at the Lawrence Livermore National Laboratory                 *
 *   Written by: Gavin M. Bell (gavin@llnl.gov)                             *
 *   LLNL-CODE-420962                                                       *
 *                                                                          *
 *   All rights reserved. This file is part of the:                         *
 *   Earth System Grid Federation (ESGF) Data Node Software Stack           *
 *                                                                          *
 *   For details, see http://esgf.org/esg-node/                             *
 *   Please also read this link                                             *
 *    http://esgf.org/LICENSE                                               *
 *                                                                          *
 *   * Redistribution and use in source and binary forms, with or           *
 *   without modification, are permitted provided that the following        *
 *   conditions are met:                                                    *
 *                                                                          *
 *   * Redistributions of source code must retain the above copyright       *
 *   notice, this list of conditions and the disclaimer below.              *
 *                                                                          *
 *   * Redistributions in binary form must reproduce the above copyright    *
 *   notice, this list of conditions and the disclaimer (as noted below)    *
 *   in the documentation and/or other materials provided with the          *
 *   distribution.                                                          *
 *                                                                          *
 *   Neither the name of the LLNS/LLNL nor the names of its contributors    *
 *   may be used to endorse or promote products derived from this           *
 *   software without specific prior written permission.                    *
 *                                                                          *
 *   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS    *
 *   "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT      *
 *   LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS      *
 *   FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL LAWRENCE    *
 *   LIVERMORE NATIONAL SECURITY, LLC, THE U.S. DEPARTMENT OF ENERGY OR     *
 *   CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,           *
 *   SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT       *
 *   LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF       *
 *   USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND    *
 *   ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,     *
 *   OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT     *
 *   OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF     *
 *   SUCH DAMAGE.                                                           *
 *                                                                          *
 ***************************************************************************/
 
 /**
    Description:
 
 **/
 package esg.common.db;
 
 import java.util.Properties;
 import javax.sql.DataSource;
 
 import org.apache.commons.pool.ObjectPool;
 import org.apache.commons.pool.impl.GenericObjectPool;
 import org.apache.commons.dbcp.ConnectionFactory;
 import org.apache.commons.dbcp.PoolingDataSource;
 import org.apache.commons.dbcp.PoolableConnectionFactory;
 import org.apache.commons.dbcp.DriverManagerConnectionFactory;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.logging.impl.*;
 
 //Singleton class for getting database DataSources
 public class DatabaseResource {
 
     private static Log log = LogFactory.getLog(DatabaseResource.class);
     private static DatabaseResource instance = null;
     private ObjectPool connectionPool = null;
     private PoolingDataSource dataSource = null;
     private String driverName = null;
 
     public static DatabaseResource init(String driverName) {
         log.trace("Initializing... with Driver: ["+driverName+"]");
         if(instance == null) {
             instance = new DatabaseResource(driverName);
         }else {
             log.trace("fetching instance: ["+instance+"]");
         }
         return instance;
     }
     public static DatabaseResource getInstance() { 
         if(instance == null) log.warn("Instance is NULL!!! \"init\" must be called prior to calling this method!!");
         return instance; 
     }
 
     //Private Singleton Constructor...
     private DatabaseResource(String driverName) { 
         log.trace("Instantating DatabaseResource object...");
         try {
             log.debug("Loading JDBC driver: ["+driverName+"]");
             Class.forName(driverName);
             this.driverName = driverName;
         } catch (ClassNotFoundException e) {
             log.error(e);
         }
     }
     
     public DatabaseResource setupDataSource(Properties props) {
         log.info("Setting up data source: props = "+props);
         if(props == null) { log.error("Property object is ["+props+"]: Cannot setup up data source"); return this; }
         //Ex: jdbc:postgresql://pcmdi3.llnl.gov:5432/esgcet
         String protocol = props.getProperty("db.protocol","jdbc:postgresql:");
         String host =     props.getProperty("db.host","localhost");
         String port =     props.getProperty("db.port","5432");
         String database = props.getProperty("db.database","esgcet");
         String user =     props.getProperty("db.user","dbsuper");
         String password = props.getProperty("db.password");
 
         //If the password is not directly available in the properties
         //object then try to read it via the code provided in the
         //ESGFProperties type...iff props is actually of the type
         //ESGFProperties.
         if(password == null) {
             try{
                 password = ((esg.common.util.ESGFProperties)props).getDatabasePassword();
             }catch(Throwable t) {
                 t.printStackTrace();
             }
         }
 
        String connectURI = protocol+"//"+host+":"+port+"/"+database;
         log.info("Connection URI = "+connectURI);
         connectionPool = new GenericObjectPool(null);
         ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(connectURI,user,password);
         PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory,connectionPool,null,null,false,true);
         dataSource = new PoolingDataSource(connectionPool);
         return this;
     }
 
     public String getDriverName() { return driverName; }
     
     public DataSource getDataSource() {
         if(null == dataSource) log.error("Data Source Is NULL!!!");
         return dataSource;
     }
 
     public void showDriverStats() {
         System.out.println(" NumActive: " + (connectionPool == null ? "X" : connectionPool.getNumActive()));
         System.out.println(" NumIdle:   " + (connectionPool == null ? "X" : connectionPool.getNumIdle()));
     }
 
     public void shutdownResource() {
         log.info("Shutting Down Database Resource! ("+driverName+")");
         try{
             connectionPool.close();
         }catch(Exception ex) {
             log.error("Problem with closing connection Pool!",ex);
         }
         dataSource = null;
         instance = null;
     }
 }
