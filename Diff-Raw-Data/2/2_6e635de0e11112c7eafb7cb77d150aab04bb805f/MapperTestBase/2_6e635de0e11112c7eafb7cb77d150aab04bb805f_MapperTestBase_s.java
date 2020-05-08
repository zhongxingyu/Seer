 /**
  * Copyright (C) 2008 Ivan S. Dubrov
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *         http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.google.code.nanorm.test.common;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.TimeZone;
 
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 
 import com.google.code.nanorm.NanormFactory;
 import com.google.code.nanorm.Session;
 import com.google.code.nanorm.config.NanormConfiguration;
 
 /**
  * 
  * @author Ivan Dubrov
  * @version 1.0 29.05.2008
  */
 public class MapperTestBase {
 
 	/**
 	 * Connection.
 	 */
 	protected static Connection conn;
 
 	/**
 	 * Nanorm factory.
 	 */
 	protected static NanormFactory factory;
 
 	/**
 	 * Current transaction.
 	 */
 	protected static Session transaction;
 
 	/**
 	 * Loads the test data.
 	 * 
 	 * @throws Exception any error
 	 */
 	@BeforeClass
 	public static void beforeClass() throws Exception {
		TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
 		
 		Class.forName("org.h2.Driver");
 		conn = DriverManager.getConnection("jdbc:h2:mem:", "sa", "");
 
 		// Tables for primitive values
 		execute("CREATE TABLE CORE(id INTEGER, primByte TINYINT, wrapByte TINYINT, "
 				+ "primShort SMALLINT, wrapShort SMALLINT, primInt INT, wrapInt INT,"
 				+ "primLong BIGINT, wrapLong BIGINT, primBoolean BOOL, wrapBoolean BOOL,"
 				+ "primChar CHAR(1), wrapChar CHAR(1), primFloat REAL, wrapFloat REAL,"
 				+ "primDouble DOUBLE, wrapDouble DOUBLE, string VARCHAR(50), "
 				+ "date TIMESTAMP, sqldate DATE, sqltime TIME, sqltimestamp TIMESTAMP," 
 				+ "bytearr BLOB, locale VARCHAR(10))");
 
 		execute("INSERT INTO CORE(id, primByte, wrapByte, primShort, wrapShort, "
 				+ "primInt, wrapInt, primLong, wrapLong, primBoolean, wrapBoolean, "
 				+ "primChar, wrapChar, primFloat, wrapFloat, primDouble, wrapDouble, string, " 
 				+  "date, sqldate, sqltime, sqltimestamp, bytearr, locale) VALUES("
 				+ "1, 37, -23, 8723, -6532, "
 				+ "824756237, -123809163, 282347987987234987, -23429879871239879, TRUE, FALSE,"
 				+ "'a', 'H', 34.5, -25.25, "
 				+ "44.5, -47.125, 'Hello, H2!', "
 				+ "'2009-06-07 15:23:34', '2006-12-11', '16:32:01', '2008-07-08 18:08:11'," 
 				+ "'1A5C6F', 'ru_RU')");
 
 		// Create some categories
 		execute("CREATE TABLE CATEGORIES(id INTEGER, title VARCHAR(50), year INTEGER)");
 
 		execute("INSERT INTO CATEGORIES(id, title, year) VALUES (1, 'World', 2006)");
 
 		execute("INSERT INTO CATEGORIES(id, title, year) VALUES (2, 'Science', 2004)");
 
 		// Create some articles
 		execute("CREATE TABLE ARTICLES(id INTEGER, category_id INTEGER, subject VARCHAR(50), body VARCHAR(200), year INTEGER)");
 
 		execute("INSERT INTO ARTICLES(id, category_id, subject, body, year) VALUES (1, 1, 'World Domination', 'Everybody thinks of world domination.', 2007)");
 
 		execute("INSERT INTO ARTICLES(id, category_id, subject, body, year) VALUES (2, 1, 'Saving the Earth', 'To save the earth you need...', 2008)");
 
 		// Create some publications
 		execute("CREATE TABLE PUBLICATIONS(id INTEGER, article_id INTEGER, title VARCHAR(50), year INTEGER)");
 
 		execute("INSERT INTO PUBLICATIONS(id, article_id, title, year) VALUES (543, 1, 'Best Way to World Dominate!', 2008)");
 
 		// Create some labels
 		execute("CREATE TABLE LABELS(id INTEGER, article_id INTEGER, label VARCHAR(50))");
 
 		execute("INSERT INTO LABELS(id, article_id, label) VALUES (1231, 1, 'World')");
 		execute("INSERT INTO LABELS(id, article_id, label) VALUES (1232, 1, 'Dominate')");
 
 		// Create some comments
 		execute("CREATE TABLE COMMENTS(id INTEGER, article_id INTEGER, comment VARCHAR(200), year INTEGER)");
 		execute("INSERT INTO COMMENTS(id, article_id, comment, year) VALUES (101, 1, 'Great!', 2006)");
 		execute("INSERT INTO COMMENTS(id, article_id, comment, year) VALUES (102, 1, 'Always wanted to world-dominate!', 2007)");
 
 		// Sequence for ids
 		execute("CREATE SEQUENCE ids START WITH 123 INCREMENT BY 1");
 		
 		// Some functions to invoke
 		execute("CREATE ALIAS myConcat FOR \"com.google.code.nanorm.test.common.Funcs.concat\"");
 		execute("CREATE ALIAS myConcat2 FOR \"com.google.code.nanorm.test.common.Funcs.concat2\"");
 
 		conn.commit();
 
 		factory = new NanormConfiguration().buildFactory();
 		transaction = factory.openSession(conn);
 	}
 
 	/**
 	 * Execute the statement.
 	 * 
 	 * @param sql sql statement to execute
 	 * @throws SQLException any SQL error
 	 */
 	protected static void execute(String sql) throws SQLException {
 		Statement st = conn.createStatement();
 		try {
 			st.execute(sql);
 		} finally {
 			st.close();
 		}
 	}
 
 	/**
 	 * Rollback the transaction and close the connection.
 	 * 
 	 * @throws SQLException any SQL exception
 	 */
 	@AfterClass
 	public static void afterClass() throws SQLException {
 		if (transaction != null) {
 			transaction.rollback();
 			transaction.end();
 		}
 		if (conn != null) {
 			conn.close();
 		}
 	}
 }
