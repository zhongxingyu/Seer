 package com.mirantis.aminakov.bigdatacourse;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
 
 public class ManagementTables {
 	public static final Logger LOG = Logger.getLogger(ManagementTables.class);
 	String driverName = "com.mysql.jdbc.Driver";
 	String url = "jdbc:mysql://localhost:1234/bigdata?user=aminakov&password=bigdata";
 	String jdbcutf8 = "&useUnicode=true&characterEncoding=UTF-8";
 	protected Connection con = null;
 	Statement st = null;
 	ResultSet rs = null;
 	
 	public ManagementTables() throws DaoException {
		//FIXME
		DOMConfigurator.configure("log4j.xml");
 		try {
 			try {
 				Class.forName(driverName).newInstance();
 			} catch (InstantiationException e) {
 				LOG.error("Instantiation exception driver.");
 				throw new DaoException(e);
 			} catch (IllegalAccessException e) {	
 				LOG.error("Illegal access to driver class.");
 				throw new DaoException(e);
 			}
 			try {
 				con = DriverManager.getConnection(url + jdbcutf8);
 			} catch (SQLException e) {
 				throw new DaoException(e);
 			}
 		} catch (ClassNotFoundException e) {
 			LOG.error("Driver not found.");
 			throw new DaoException(e);
 		}
 	}
 	
 	
 	public void createTables() throws DaoException {
 		try {
 			con.setAutoCommit(false);
 			st = con.createStatement();
 			st.executeUpdate("CREATE TABLE IF NOT EXISTS Authors (id int NOT NULL AUTO_INCREMENT, " +
 							  "author varchar(40) NOT NULL, " +
 							  "PRIMARY KEY (id)) ENGINE=INNODB CHARACTER SET utf8;");
 			st.executeUpdate("CREATE TABLE IF NOT EXISTS Genres (id int NOT NULL AUTO_INCREMENT, " +
 					  	"genre varchar(40) NOT NULL, " +
 					  	"PRIMARY KEY (id)) ENGINE=INNODB CHARACTER SET utf8;");
 			st.executeUpdate("CREATE TABLE IF NOT EXISTS Texts (id int NOT NULL AUTO_INCREMENT, " +
 						"text TEXT NOT NULL, " +
 					  	"PRIMARY KEY (id)) ENGINE=INNODB CHARACTER SET utf8;");
 			st.executeUpdate("CREATE TABLE IF NOT EXISTS Books (id int NOT NULL AUTO_INCREMENT, " +
 					  	"title varchar(200) NOT NULL, book_id int NOT NULL, " +
 					  	"author_id int NOT NULL, genre_id int NOT NULL, " +
 					  	"PRIMARY KEY (id), FOREIGN KEY (book_id) REFERENCES Texts (id), " +
 					  	"FOREIGN KEY (author_id) REFERENCES Authors (id), " +
 					  	"FOREIGN KEY (genre_id) REFERENCES Genres (id)) ENGINE=INNODB CHARACTER SET utf8;");
 			con.commit();
 			con.setAutoCommit(true);
 		} catch (SQLException e) {
 			try {
 				con.rollback();
 				throw new DaoException("Transaction filed " + e.getMessage());
 			} catch (SQLException e1) {
 				throw new DaoException("Rollback filed " + e1.getMessage());
 			}
 		} finally {
             if (rs != null) {
                 try {
 					rs.close();
 				} catch (SQLException e) {
 					throw new DaoException(e);
 				}
             }
             if (st != null) {
                 try {
 					st.close();
 				} catch (SQLException e) {
 					throw new DaoException(e);
 				}
             }
         }
 	}
 	
 	public void dropTables() throws DaoException {
 		try {
 			con.setAutoCommit(false);
 			st = con.createStatement();
 			st.executeUpdate("DROP TABLE IF EXISTS Books;");
 			st.executeUpdate("DROP TABLE IF EXISTS Authors;");
 			st.executeUpdate("DROP TABLE IF EXISTS Genres;");
 			st.executeUpdate("DROP TABLE IF EXISTS Texts;");
 			con.commit();
 			con.setAutoCommit(true);
 		} catch (SQLException e) {
 			try {
 				con.rollback();
 				throw new DaoException("Transaction filed " + e.getMessage());
 			} catch (SQLException e1) {
 				throw new DaoException("Rollback filed " + e1.getMessage());
 			}
 		} finally {
             if (rs != null) {
                 try {
 					rs.close();
 				} catch (SQLException e) {
 					throw new DaoException(e);
 				}
             }
             if (st != null) {
                 try {
 					st.close();
 				} catch (SQLException e) {
 					throw new DaoException(e);
 				}
             }
         }
 	}
 	
 	public void closeConnection() throws DaoException {
 		if (con != null) {
 			try {
 				con.close();
 			} catch (SQLException e) {
 				throw new DaoException(e);
 			}
 		}
 	}
 }
