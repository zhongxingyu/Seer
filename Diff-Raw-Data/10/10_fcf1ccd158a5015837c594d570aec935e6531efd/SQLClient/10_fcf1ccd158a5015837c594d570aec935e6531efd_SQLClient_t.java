 package github.hfdiao.sqlclient;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 public abstract class SQLClient {
     public abstract void loadDriver() throws ClassNotFoundException;
 
     public abstract boolean driverLoaded();

     abstract String makeConnectUrl(String host, int port, String database);
 
     public Connection getConnection(String host, int port, String database,
             String username, String password) throws SQLException {
         String connectUrl = makeConnectUrl(host, port, database);
         return DriverManager.getConnection(connectUrl, username, password);
     }
 
     public Connection getConnection(String host, int port, String database,
             Properties properties) throws SQLException {
         String connectUrl = makeConnectUrl(host, port, database);
         return DriverManager.getConnection(connectUrl, properties);
     }
 
     public Object doSql(Connection conn, String sql) throws SQLException {
         if (null == sql) {
             return null;
         }
 
         sql = sql.trim();
         Statement statement = null;
         try {
             statement = conn.createStatement();
             if (sql.toLowerCase().startsWith("select ")
                     || sql.toLowerCase().startsWith("show ")) {
                 List<Map<String, Object>> query = doSelect(statement, sql);
                 return query;
             } else {
                 int affected = doUpdate(statement, sql);
                 return affected;
             }
         } finally {
            if (null != statement) {
                 statement.close();
             }
         }
     }
 
     List<Map<String, Object>> doSelect(Statement statement, String sql)
             throws SQLException {
         ResultSet rs = null;
         try {
             rs = statement.executeQuery(sql);
             ResultSetMetaData rsmd = rs.getMetaData();
             int numberOfColumns = rsmd.getColumnCount();
             List<Map<String, Object>> list = new LinkedList<Map<String, Object>>();
             while (rs.next()) {
                 Map<String, Object> row = new HashMap<String, Object>();
                 for (int i = 1; i <= numberOfColumns; i++) {
                     String label = rsmd.getColumnLabel(i);
                     Object value = rs.getObject(i);
                     row.put(label, value);
                 }
                 list.add(row);
             }
             return list;
         } finally {
            if (null != rs) {
                rs.close();
            }
         }
 
     }
 
     int doUpdate(Statement statement, String sql) throws SQLException {
         int affected = statement.executeUpdate(sql);
         return affected;
     }
 }
