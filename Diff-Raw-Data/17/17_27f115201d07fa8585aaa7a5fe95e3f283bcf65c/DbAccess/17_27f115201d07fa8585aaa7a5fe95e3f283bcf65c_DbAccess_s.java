 package com.homework.hw3.hsql;
 
 import java.sql.*;
 import java.util.ArrayList;
 
 import org.apache.commons.dbutils.DbUtils;
 
 public class DbAccess {
 
    private static final String DB_URL = "jdbc:hsqldb:file:${user.home}/i377/fpoobus/db;shutdown=true";
 
     static {
         try {
             Class.forName("org.hsqldb.jdbcDriver");
         } catch (ClassNotFoundException e) {
             throw new RuntimeException(e);
         }
     };
 
     public static void main(String[] args) throws Exception {
 
     } 
 
     public static void setupDatabase() {
     	try {
     		DbAccess db = new DbAccess();
     		db.deleteTable();
 			db.createTable();
 			db.createSequence();
 			//db.getAll();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     }
 
     private void createTable() throws SQLException {
 
         Connection conn = DriverManager.getConnection(DB_URL);
         Statement stmt = null;
         try {
             stmt = conn.createStatement();
             stmt.executeUpdate(
                     "CREATE TABLE unit ("+
 					"id BIGINT NOT NULL PRIMARY KEY,"+
 					"name VARCHAR(255) NOT NULL,"+
 					"code VARCHAR(255) NOT NULL,"+
 					"superior_id BIGINT,"+
 					"FOREIGN KEY (superior_id)"+
 					"REFERENCES unit ON DELETE RESTRICT);"
 					);
         } catch(Exception e) { 
         	System.out.println("Table already exists, TODO: fix duplicate insert");
         	e.printStackTrace();
         } finally {
             DbUtils.closeQuietly(stmt);
             DbUtils.closeQuietly(conn);
         }
     }
 
     
     private void createSequence() throws SQLException {
     	Connection conn = DriverManager.getConnection(DB_URL);
         Statement stmt = null;
         try {
             stmt = conn.createStatement();
             stmt.executeUpdate("CREATE SEQUENCE sequence AS INTEGER START WITH 1");
         } catch(Exception e) { 
         	System.out.println("Sequence already exists, TODO: fix duplicate insert");
         } finally {
             DbUtils.closeQuietly(stmt);
             DbUtils.closeQuietly(conn);
         }
     }
     
     
     public ArrayList<DbItem> getByLike(String str) throws SQLException {
     	ArrayList list = new ArrayList();
         Connection conn = DriverManager.getConnection(DB_URL);
         PreparedStatement ps = null;
         Statement stmt = null;
         ResultSet rset = null;
         try {
             ps = conn.prepareStatement("SELECT * FROM unit " + "WHERE name LIKE '%"+str+"%'");
             rset = ps.executeQuery();
             while (rset.next()) {
             	
             	long id = rset.getLong(1);
             	String name = rset.getString(2);
             	String code = rset.getString(3);
             	long superior = rset.getLong(4);
             	
             	DbItem item = new DbItem(id, name, code, superior);
             	list.add(item);
             	
                 System.out.println(rset.getInt(1) + ", " + rset.getString(2));
             }
         } catch(Exception e) {
         	e.getStackTrace();
         } finally {
             DbUtils.closeQuietly(rset);
             DbUtils.closeQuietly(stmt);
             DbUtils.closeQuietly(conn);
         }
 		return list;
     }
 
     public ArrayList getAll() throws SQLException {
     	ArrayList list = new ArrayList();
         Connection conn = DriverManager.getConnection(DB_URL);
         Statement stmt = null;
         ResultSet rset = null;
         try {
             stmt = conn.createStatement();
             rset = stmt.executeQuery("SELECT * FROM unit");
             while (rset.next()) {
             	
             	long id = rset.getLong(1);
             	String name = rset.getString(2);
             	String code = rset.getString(3);
             	long superior = rset.getLong(4);
             	
             	DbItem item = new DbItem(id, name, code, superior);
             	list.add(item);
             	
                 System.out.println(rset.getLong(1) + ", " + rset.getString(2));
             }
         } finally {
             DbUtils.closeQuietly(rset);
             DbUtils.closeQuietly(stmt);
             DbUtils.closeQuietly(conn);
         }
         
         return list;
     }
 
  
     public void deleteTable() throws SQLException {
     	Connection conn = DriverManager.getConnection(DB_URL);
         Statement stmt = null;
         try {
             stmt = conn.createStatement();
             stmt.executeUpdate("DROP SCHEMA public CASCADE");
             System.out.println("Schema dropped");
         } catch(Exception e) { 
         	System.out.println("Error deleting table");
         } finally {
             DbUtils.closeQuietly(stmt);
             DbUtils.closeQuietly(conn);
         }
     }
     
     public void clearTable() throws SQLException {
     	Connection conn = DriverManager.getConnection(DB_URL);
         Statement stmt = null;
         try {
             stmt = conn.createStatement();
             stmt.executeUpdate("TRUNCATE SCHEMA public AND COMMIT");
             System.out.println("Schema dropped");
         } catch(Exception e) { 
         	System.out.println("Error emptying table");
         } finally {
             DbUtils.closeQuietly(stmt);
             DbUtils.closeQuietly(conn);
         }
     }
     
 
     public void insertData(String name, String code) {
     	System.out.println("Inserting item. Name: " + name + " Code: " + code);
         executeQuery("INSERT INTO unit (id, name, code) VALUES(NEXT VALUE FOR sequence, '"+ name +"', '"+ code +"')");
     } 
     
     public void insertDummyData() {
     	insertData("Name 1", "Code 1");
     	insertData("Name 2", "Code 2");
     	insertData("Name 3", "Code 3");
     	insertData("Name 4", "Code 4");
     	insertData("Name 5", "Code 5");
     } 
     
 
     public void deleteItem(String key) {
     	int delkey = Integer.valueOf(key);
     	executeQuery("DELETE FROM unit WHERE id = " + delkey);
     }
     
     private void executeQuery(String queryString) {
         Statement stmt = null;
         Connection conn = null;
         try {
             conn = DriverManager.getConnection(DB_URL);
             stmt = conn.createStatement();
             stmt.executeUpdate(queryString);
          } catch (Exception e) {
              throw new RuntimeException(e);
          } finally {
              DbUtils.closeQuietly(stmt);
              DbUtils.closeQuietly(conn);
          }
     }
 }
 
