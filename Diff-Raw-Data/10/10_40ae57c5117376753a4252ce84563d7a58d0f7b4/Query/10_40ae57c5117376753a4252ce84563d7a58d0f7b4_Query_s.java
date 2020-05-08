 /**
  * @author Christian Burelli
 * @version 2.0 Build 19, 07/02/2013
  */
 
 package loveme.db;
 
 import com.sun.rowset.CachedRowSetImpl;
 import java.sql.*;
 import java.util.List;
 import javax.sql.rowset.*;
 
 public class Query {
 
     public Query() {
     }
 
     public static CachedRowSet select(String db, String sqlQuery) throws SQLException, Exception {
         Connection conn = Connessione.getConnection(db);
         ResultSet res;
         try {
             Statement stm = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                                                  ResultSet.CONCUR_READ_ONLY);
             res = stm.executeQuery(sqlQuery);
 
             CachedRowSet crs = new CachedRowSetImpl();
             crs.populate(res);
             res.close();
 
             return crs;
         } finally {
             Connessione.closeConnection(conn);
         }
     }
 
     public static CachedRowSet select(String db, String sqlQuery,String param) throws SQLException, Exception {
         Connection conn = Connessione.getConnection(db);
         ResultSet res;
         try {
             PreparedStatement pstm = conn.prepareStatement(sqlQuery,
                                                            ResultSet.TYPE_SCROLL_INSENSITIVE,
                                                            ResultSet.CONCUR_READ_ONLY);
             pstm.setString(1, param);
             res = pstm.executeQuery();
 
             CachedRowSet crs = new CachedRowSetImpl();
             crs.populate(res);
             res.close();
 
             return crs;
         } finally {
             Connessione.closeConnection(conn);
         }
     }
 
     public static CachedRowSet select(String db, String sqlQuery, int param) throws SQLException, Exception {
         Connection conn = Connessione.getConnection(db);
         ResultSet res;
         try {
             PreparedStatement pstm = conn.prepareStatement(sqlQuery,
                                                            ResultSet.TYPE_SCROLL_INSENSITIVE,
                                                            ResultSet.CONCUR_READ_ONLY);
             pstm.setInt(1, param);
             res = pstm.executeQuery();
 
             CachedRowSet crs = new CachedRowSetImpl();
             crs.populate(res);
             res.close();
 
             return crs;
         } finally {
             Connessione.closeConnection(conn);
         }
     }
 
     public static CachedRowSet select(String sql, List params, String db) throws SQLException {
         Connection conn = Connessione.getConnection(db);
         try {
             SQLCommand command = new SQLCommand();
             command.setConnection(conn);
             command.setSqlValue(sql);
 
             command.setValues(params);
 
             return command.executeQuery();
 
         } finally {
             Connessione.closeConnection(conn);
         }
     }
         
     public static void insert(String db, String sqlQuery) throws Exception {
         Connection conn = Connessione.getConnection(db);
         try {
             try (Statement stm = conn.createStatement()) {
                 stm.executeUpdate(sqlQuery);
             }
         } finally {
             Connessione.closeConnection(conn);
         }
     }
 
     public static boolean insert(String sql, List params, String db) throws SQLException, Exception {
         Connection conn = Connessione.getConnection(db);
         boolean done = false;
 
         try {
             SQLCommand command = new SQLCommand();
             command.setConnection(conn);
             command.setSqlValue(sql);
             command.setValues(params);
             command.executeUpdate();
            conn.commit();
             done = true;
         } catch (Throwable e) {
             try {
                 if (conn != null) {
                    conn.rollback();
                     conn.close();
                 }
                 throw new Exception(e);
             } catch (SQLException e1) {
                 throw new Exception(e1.getMessage());
             }
         } finally {
             Connessione.closeConnection(conn);
         }
         return done;
     }
     
     public static void update(String sqlQuery, String db) throws Exception {
         Connection conn = Connessione.getConnection(db);
         try {
             try (Statement stm = conn.createStatement()) {
                 stm.executeUpdate(sqlQuery);
             }
         } finally {
             Connessione.closeConnection(conn);
         }
     }
 
     public static void delete(String sql, List params, String db) throws Exception {
         Connection conn = Connessione.getConnection(db);
         try {
             SQLCommand command = new SQLCommand();
             command.setConnection(conn);
             command.setSqlValue(sql);
             command.setValues(params);
             command.executeUpdate();
            conn.commit();
         } catch (Throwable e) {
             try {
                 if (conn != null) {
                    conn.rollback();
                     conn.close();
                 }
                 throw new Exception(e);
             } catch (SQLException e1) {
                 throw new Exception(e1.getMessage());
             }
         } finally {
             Connessione.closeConnection(conn);
         }
     }
 
 }
