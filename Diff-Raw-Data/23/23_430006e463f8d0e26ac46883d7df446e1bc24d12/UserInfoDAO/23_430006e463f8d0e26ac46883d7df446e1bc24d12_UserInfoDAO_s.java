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
  *   Earth System Grid (ESG) Data Node Software Stack, Version 1.0          *
  *                                                                          *
  *   For details, see http://esgf.org/esg-node/                    *
  *   Please also read this link                                             *
  *    http://esgf.org/LICENSE                                      *
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
    Perform sql query to find out all the people who
    Return Tuple of info needed (dataset_id, recipients/(user), names of updated files)
    
 **/
 package esg.node.components.security;
 
 import java.util.Set;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Properties;
 import java.io.Serializable;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import javax.sql.DataSource;
 
 import org.apache.commons.dbutils.QueryRunner;
 import org.apache.commons.dbutils.ResultSetHandler;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.logging.impl.*;
 
 import esg.common.db.DatabaseResource;
 
 public class UserInfoDAO implements Serializable {
 
     //-------------------
     //Selection queries
     //-------------------
     private static final String idQuery = 
        "SELECT esgf.id, esgf.firstname, esgf.lastname, esgf.openid, esgf.email "+
        "FROM user "+
        "WHERE esgf.openid = ?";
 
     private static final String groupQuery = 
         "SELECT g.name, r.name from esgf.group as g, esgf.role as r, esgf.permission as p, esgf.user as u "+
         "WHERE p.user_id = u.id and u.openid = ? and p.group_id = g.id and p.role_id = r.id "+
         "ORDER BY g.name";
 
     //-------------------
     //Insertion queries
     //-------------------
     
     //User Queries...
     private static final String hasUserOpenidQuery =
         "SELECT * form esgf.user "+
         "WHERE openid = ?";
     private static final String updateUserQuery = 
         "UPDATE esgf.user "+
         "SET openid=?, firstname=?, middlename=?, lastname=?, username=?, email=?, organization=?, organization_type=?, city=? state=?, country=? "+
         "WHERE id = ? ";
     private static final String getNextUserPrimaryKeyValQuery = 
         "SELECT NEXTVAL('esgf.seq_user')";
     private static final String addUserQuery = 
         "INSERT INTO esgf.user (id, openid, firstname, middlename, lastname, username, email, organization, organization_type, city, state, country) "+
         "VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
 
     //Permission Queries...
     private static final String addPermissionQuery = 
         "INSERT INTO esgf.permission (user_id, grou_id, role_id) "+
         "VALUES ( ?, ? )";
 
     //-------------------
 
     
     private static final Log log = LogFactory.getLog(UserInfoDAO.class);
 
     private Properties props = null;
     private DataSource dataSource = null;
     private QueryRunner queryRunner = null;
     private ResultSetHandler<UserInfo> userInfoResultSetHandler = null;
     private ResultSetHandler<Map<String,Set<String>>> userGroupsResultSetHandler = null;
     private ResultSetHandler<Integer> idResultSetHandler = null;
 
     //uses default values in the DatabaseResource to connect to database
     public UserInfoDAO() {
         this(new Properties());
     }
 
     public UserInfoDAO(Properties props) {
         if (props == null) {
             log.warn("Input Properties parameter is: ["+props+"] - creating empty Properties obj");
             props = new Properties();
         }
         
         //This is kind of tricky because the DatabaseResource is meant
         //to be set up once in the earliest part of this application.
         //Subsequent to it's initialziation and setup then any program
         //that needs to use the database can call getInstance.  Since
         //I am not sure where in the codebase I can initialize the
         //DatabaseResource I am doing it here but guarding repeated
         //calls to setupDatasource so that it is ostensibly in the
         //singleton as well.
         
         if (DatabaseResource.getInstance() == null) {
             DatabaseResource.init(props.getProperty("db.driver","org.postgresql.Driver")).setupDataSource(props);
         }
         
         this.setDataSource(DatabaseResource.getInstance().getDataSource());
         this.setProperties(props);
         init();
     }
 
     public void init() {
         this.idResultSetHandler = new ResultSetHandler<Integer>() {
 		    public Integer handle(ResultSet rs) throws SQLException {
                 if(!rs.next()) { return -1; }
                 return rs.getInt(1);
 		    }
 		};
         
         //To handle the single record result
         userInfoResultSetHandler =  new ResultSetHandler<UserInfo>() {
             public UserInfo handle(ResultSet rs) throws SQLException {
                 UserInfo userInfo = null;
                 while(rs.next()) {
                     userInfo = new UserInfo();
                    userInfo.setFirstName(rs.getString(1))
                        .setLastName(rs.getString(2))
                        .setOpenid(rs.getString(3))
                        .setEmail(rs.getString(4));
                 }
                 return userInfo;
             }
         };
         
         userGroupsResultSetHandler = new ResultSetHandler<Map<String,Set<String>>>() {
             Map<String,Set<String>> groups = null;    
             Set<String> roleSet = null;
             
             public Map<String,Set<String>> handle(ResultSet rs) throws SQLException{
                 while(rs.next()) {
                     addGroup(rs.getString(1),rs.getString(2));
                 }
                 return groups;
             }
             
             public void addGroup(String name, String value) {
                 //lazily instantiate groups map
                 if(groups == null) {
                     groups = new HashMap<String,Set<String>>();
                 }
                 
                 //lazily instantiate the set of values for group if not
                 //there
                 if((roleSet = groups.get(name)) == null) {
                     roleSet = new HashSet<String>();
                 }
                 
                 //enter group associated with group value set
                 roleSet.add(value);
                 groups.put(name, roleSet);
             }
 
 
         };
     }
     
     public void setProperties(Properties props) { this.props = props; }
 
     public void setDataSource(DataSource dataSource) {
         log.trace("Setting Up UserInfoDAO's Pooled Data Source");
         this.dataSource = dataSource;
         this.queryRunner = new QueryRunner(dataSource);
     }
     
     //------------------------------------
     //Query function calls... 
     //(NOTE: synchronized since there are two calls to database - can optimize around later)
     //------------------------------------
     public synchronized UserInfo getUserById(String openid) {
         UserInfo userInfo = null;
         int affectedRecords = 0;
         try{
             log.trace("Issuing Query for info associated with id: ["+openid+"], from database");
             if (openid==null) { return null; }
             userInfo = queryRunner.query(idQuery,userInfoResultSetHandler,openid);
             userInfo.setGroups(queryRunner.query(groupQuery,userGroupsResultSetHandler,openid));
             
             //A bit of debugging and sanity checking...
             System.out.println(userInfo);
             
         }catch(SQLException ex) {
             log.error(ex);      
         }
         return userInfo;
     }
 
     public synchronized boolean addUserInfo(UserInfo userInfo) {
         int userid = -1;
         int groupid = -1;
         int roleid = -1;
         int numRowsAffected = -1;
         try{
             log.trace("Inserting UserInfo associated with id: ["+userInfo.getOpenid()+"], into database");
             if (userInfo.getOpenid() == null) { return false; }
             
             //Check to see if there is an entry by this openid already....
             userid = queryRunner.query(hasUserOpenidQuery,idResultSetHandler,userInfo.getOpenid());
             
             //If there *is*... then UPDATE that record
             if(userid > 0) {
                 assert (userid == userInfo.getid()) : "The database id ("+userid+") for this openid ("+userInfo.getOpenid()+") does NOT match this object's ("+userInfo.getid()+")";
                 numRowsAffected = queryRunner.update(updateUserQuery,
                                                      userInfo.getOpenid(),
                                                      userInfo.getFirstName(),
                                                      userInfo.getMiddleName(),
                                                      userInfo.getLastName(),
                                                      userInfo.getUserName(),
                                                      userInfo.getEmail(),
                                                      userInfo.getOrganization(),
                                                      userInfo.getOrgType(),
                                                      userInfo.getCity(),
                                                      userInfo.getState(),
                                                      userInfo.getCountry(),
                                                      userid
                                                      );
                 return (numRowsAffected > 0);
             }
             
             //If this user does not exist in the database then add (INSERT) a new one
             userid = queryRunner.query(getNextUserPrimaryKeyValQuery ,idResultSetHandler);
             numRowsAffected = queryRunner.update(addUserQuery,
                                                  userid,
                                                  userInfo.getOpenid(),
                                                  userInfo.getFirstName(),
                                                  userInfo.getMiddleName(),
                                                  userInfo.getLastName(),
                                                  userInfo.getUserName(),
                                                  userInfo.getEmail(),
                                                  userInfo.getOrganization(),
                                                  userInfo.getOrgType(),
                                                  userInfo.getCity(),
                                                  userInfo.getState(),
                                                  userInfo.getCountry()
                                                  );
             
             
             //A bit of debugging and sanity checking...
             System.out.println(userInfo);
             
         }catch(SQLException ex) {
             log.error(ex);      
         }
         return (numRowsAffected > 0);
     }
     
     public synchronized int addPermission(int userid, int groupid,int roleid) {
         int numRowsAffected = -1;
         try{
             numRowsAffected = queryRunner.update(addPermissionQuery,
                                                  userid, groupid, roleid);
         }catch(SQLException ex) {
             log.error(ex);
         }
         return numRowsAffected;
     }
     
     
     //------------------------------------
     
     public String toString() {
         StringBuilder out = new StringBuilder();
         out.append("DAO:["+this.getClass().getName()+"] - "+((dataSource == null) ? "[OK]" : "[INVALID]\n"));
         return out.toString();
     }
 }
