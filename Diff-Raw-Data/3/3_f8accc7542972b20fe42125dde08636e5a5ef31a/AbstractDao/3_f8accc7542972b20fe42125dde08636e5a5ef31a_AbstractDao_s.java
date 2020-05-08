 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.mosedb.dao;
 
 import com.mosedb.databaseconnection.ConnectionManager;
 import com.mosedb.models.Format;
 import com.mosedb.models.LangId;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Lasse
  */
 public abstract class AbstractDao {
 
     private Connection connection;
 
     public AbstractDao() throws SQLException {
         this.connection = ConnectionManager.getConnection();
     }
 
     /**
      * Prepares and executes the SQL statement, which must be an
      * {@code insert}, {@code update} or {@code delete} statement.
      *
      * @param sql The sql statement to be executed.
      * @param values Values to replace {@code ?} symbols in the {@code sql}
      * string. The amount of variables must match the amount of {@code ?}
      * symbols in the string.
      * @return {@code true} if rows were affected, {@code false} otherwise.
      * @throws SQLException
      */
     protected boolean executeUpdate(String sql, Object... values) throws SQLException {
         PreparedStatement pst = connection.prepareStatement(sql);
         int i = 1;
         for (Object value : values) {
             if (value != null && (value.getClass() == LangId.class || value.getClass() == Format.MediaFormat.class)) {
                 pst.setObject(i++, value.toString());
             } else {
                 pst.setObject(i++, value);
             }
         }
         int result = pst.executeUpdate();
        pst.close();
         return result != 0;
     }
 
     /**
      * Prepares and executes the SQL query.
      *
      * @param sql The sql statement to be executed.
      * @param values Values to replace {@code ?} symbols in the {@code sql}
      * string. The amount of variables must match the amount of {@code ?}
      * symbols in the string.
      * @return The resulting ResultSet.
      * @throws SQLException
      */
     protected ResultSet executeQuery(String sql, Object... values) throws SQLException {
         PreparedStatement pst = connection.prepareStatement(sql);
         int i = 1;
         for (Object value : values) {
             if (value != null && value.getClass() == LangId.class || value.getClass() == Format.MediaFormat.class) {
                 pst.setObject(i++, value.toString());
             } else {
                 pst.setObject(i++, value);
             }
         }
         ResultSet result = pst.executeQuery();
        pst.close();
         return result;
     }
 
     public void closeConnection() {
         try {
             connection.close();
         } catch (SQLException ex) {
             Logger.getLogger(AbstractDao.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 }
