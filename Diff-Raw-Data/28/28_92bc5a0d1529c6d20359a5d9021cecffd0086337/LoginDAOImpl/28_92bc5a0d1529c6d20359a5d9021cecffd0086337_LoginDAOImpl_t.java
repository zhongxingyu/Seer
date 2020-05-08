 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.pos.dao;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 /**
  *
  * @author Josh
  */
public class LoginDAOImpl extends BaseDAO implements LoginDAO{
     private String password;
    private final String GET_USERS_PASS_SQL;
    private final String GET_USERS_ORGANIZATION_SQL; 
    private final String GET_USERS_ROLE_SQL;
     
     public LoginDAOImpl(String username, String password){
         this.password = password;
        GET_USERS_PASS_SQL = "SELECT password FROM users WHERE username=\"" + username + "\"";
        GET_USERS_ORGANIZATION_SQL = "SELECT organization FROM users WHERE username=\"" + username + "\"";
        GET_USERS_ROLE_SQL = "SELECT role FROM users WHERE username=\"" + username + "\""; 
     }
     
     @Override
     public int authenticate() throws DAOException {
         int result = Result.NO_USER;
         Connection connection = null;
         PreparedStatement pStatement = null;
         ResultSet resultSet = null;
         try{
             connection = getConnection();
             pStatement = connection.prepareStatement(GET_USERS_PASS_SQL);
             resultSet = pStatement.executeQuery();
                 if (resultSet.next()){
                     if(resultSet.getString("password").equals(password)){
                             result = Result.PASS;
                         }
                     else{
                         result = Result.WRONG_PASSWORD;
                     }
                 }  
         }catch (SQLException e){
             throw new DAOException("Error getting user info. " + e.getMessage());
         }finally {
             closeDBObjects(resultSet, pStatement, connection);
         }
         return result;
     }
 
     @Override
     public String getOrganization() throws DAOException {
         String organization = null;
         Connection connection = null;
         PreparedStatement pStatement = null;
         ResultSet resultSet = null;
         try{
             connection = getConnection();
             pStatement = connection.prepareStatement(GET_USERS_ORGANIZATION_SQL);
             resultSet = pStatement.executeQuery();
             //Only calling after authentication so no need to test resultSet.next()
             resultSet.next();
             organization = resultSet.getString("organization");
         }catch(SQLException e){
             throw new DAOException("Error accessing user's organization." + e.getMessage());
         }finally{
             closeDBObjects(resultSet, pStatement, connection);
         }
         return organization;
     }
     
     @Override
     public String getRole() throws DAOException {
         String role = null;
         Connection connection = null;
         PreparedStatement pStatement = null;
         ResultSet resultSet = null;
         try{
             connection = getConnection();
             pStatement = connection.prepareStatement(GET_USERS_ROLE_SQL);
             resultSet = pStatement.executeQuery();
             //Only calling after authentication so no need to test resultSet.next()
             resultSet.next();
             role = resultSet.getString("role");
         }catch(SQLException e){
             throw new DAOException("Error accessing user's role." + e.getMessage());
         }finally{
             closeDBObjects(resultSet, pStatement, connection);
         }
         return role;
     }
 }
