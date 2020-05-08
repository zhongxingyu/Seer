 /**
  * The contents of this file are subject to the OpenMRS Public License
  * Version 1.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://license.openmrs.org
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  * License for the specific language governing rights and limitations
  * under the License.
  *
  * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
  */
 package org.openmrs.contrib.databaseexporter.util;
 
 import org.apache.commons.dbutils.DbUtils;
 import org.apache.commons.dbutils.ResultSetHandler;
 import org.apache.commons.dbutils.handlers.ArrayHandler;
 import org.apache.commons.dbutils.handlers.ColumnListHandler;
 import org.openmrs.contrib.databaseexporter.ColumnValue;
 import org.openmrs.contrib.databaseexporter.DatabaseCredentials;
 import org.openmrs.contrib.databaseexporter.ExportContext;
 import org.openmrs.contrib.databaseexporter.TableMetadata;
 import org.openmrs.contrib.databaseexporter.TableRow;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 public class DbUtil {
 
 	public static Connection openConnection(DatabaseCredentials credentials) {
 		try {
 			DbUtils.loadDriver(credentials.getDriver());
 			return DriverManager.getConnection(credentials.getUrl(), credentials.getUser(), credentials.getPassword());
 		}
 		catch (Exception e) {
 			throw new IllegalArgumentException("Error retrieving connection to the database", e);
 		}
 	}
 
 	public static List<String> getAllTables(ExportContext context) {
 		String allTableQuery = "select lower(table_name) from information_schema.tables where table_schema = database()";
 		List<String> tables = context.executeQuery(allTableQuery, new ColumnListHandler<String>());
 		return tables;
 	}
 
 	public static Map<String, TableMetadata> getTableMetadata(ExportContext context) {
 		final Map<String, TableMetadata> ret = new LinkedHashMap<String, TableMetadata>();
 
 		// Get all of the tables
 		for (String table : getAllTables(context)) {
 			ret.put(table, new TableMetadata(table));
 		}
 
 		// Retrieve the foreign key relationships for each column in each table
 		StringBuilder foreignKeyQuery = new StringBuilder();
 		foreignKeyQuery.append("select	lower(referenced_table_name), lower(referenced_column_name), lower(table_name), lower(column_name) ");
 		foreignKeyQuery.append("from 	information_schema.key_column_usage ");
 		foreignKeyQuery.append("where 	table_schema = database()");
 		context.executeQuery(foreignKeyQuery.toString(), new ResultSetHandler<Integer>() {
 			public Integer handle(ResultSet rs) throws SQLException {
 				int rowsHandled = 0;
 				while (rs.next()) {
 					TableMetadata tableMetadata = ret.get(rs.getString(1));
 					if (tableMetadata != null) {
 						ListMap<String, String> foreignKeyMap = tableMetadata.getForeignKeyMap();
 						foreignKeyMap.putInList(rs.getString(2), rs.getString(3) + "." + rs.getString(4));
 						rowsHandled++;
 					}
 				}
 				return rowsHandled;
 			}
 		});
 
 		return ret;
 	}
 
 	public static StringBuilder addConstraintToQuery(StringBuilder query, String constraint) {
 		query.append(query.indexOf(" where") == -1 ? " where " : " and ").append(constraint);
 		return query;
 	}
 
 	public static StringBuilder addInClauseToQuery(StringBuilder query, Collection<Object> l) {
 		query.append(" (");
 		for (Iterator<Object> i = l.iterator(); i.hasNext();) {
 			Object columnValue = i.next();
 			if (columnValue instanceof String) {
 				columnValue = "'" + columnValue + "'";
 			}
 			query.append(columnValue).append(i.hasNext() ? "," : "");
 		}
 		query.append(")");
 		return query;
 	}
 
 	/**
 	 * @return the ordered join queries needed to go from "fromTable" to "toTable"
 	 * if there are certain joins you wish to disallow, for example if you don't want
 	 * any joins from the user table to the person table to be included, then you can
 	 * pass these in via the "patternsToIgnore" property in the format "user/person"
 	 */
 	public static List<String> getJoins(String fromTable, String toTable, Set<String> patternsToIgnore, ExportContext context) {
 		return getJoins(fromTable, toTable, patternsToIgnore, new HashSet<String>(), context);
 	}
 
 	private static List<String> getJoins(String fromTable, String toTable, Set<String> patternsToIgnore, Set<String> checkedTables, ExportContext context) {
 		ListMap<String, String> fkMap = context.getTableMetadata(toTable).getForeignKeyMap();
 		for (String toColumn : fkMap.keySet()) {
 			for (String foreignKey : fkMap.get(toColumn)) {
 
 				String[] fkTabCol = foreignKey.split("\\.");
 				if (!checkedTables.contains(foreignKey)) {
 					checkedTables.add(foreignKey);
 
 					boolean ignore = false;
 					if (patternsToIgnore != null && !patternsToIgnore.isEmpty()) {
 						for (String pattern : patternsToIgnore) {
 							String[] fromTo = pattern.split("\\/");
 							ignore = ignore || (Util.matchesPattern(fkTabCol[0], fromTo[0]) && Util.matchesPattern(toTable, fromTo[1]));
 						}
 					}
 
 					if (!ignore) {
 						String joinQuery = "inner join " + toTable + " on " + foreignKey + " = " + toTable + "." + toColumn;
 						if (foreignKey.startsWith(fromTable + ".")) {
 							List<String> joins = new ArrayList<String>();
 							joins.add(joinQuery);
 							return joins;
 						}
 						else {
 							List<String> joins = getJoins(fromTable, fkTabCol[0], patternsToIgnore, checkedTables, context);
 							if (!joins.isEmpty()) {
 								joins.add(joinQuery);
 								return joins;
 							}
 						}
 					}
 				}
 			}
 		}
 		return new ArrayList<String>();
 	}
 
 	public static void closeConnection(Connection connection) {
 		DbUtils.closeQuietly(connection);
 	}
 
 	/**
 	 * Write the DDL Header as mysqldump does
 	*/
 	public static void writeExportHeader(ExportContext context) {
 		context.write("-- ------------------------------------------------------");
 		context.write("-- Create OpenMRS Schema");
 		context.write("-- Generated: " + new Date());
 		context.write("-- ------------------------------------------------------");
 		context.write("");
 		context.write("/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;");
 		context.write("/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;");
 		context.write("/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;");
 		context.write("/*!40101 SET NAMES utf8 */;");
 		context.write("/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;");
 		context.write("/*!40103 SET TIME_ZONE='+00:00' */;");
 		context.write("/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;");
 		context.write("/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;");
 		context.write("/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;");
 		context.write("/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;");
 	}
 
 	public static void writeTableSchema(String table, ExportContext context) {
 		context.write("--");
 		context.write("select 'Creating " + table + " schema...';");
 		context.write("--");
 		context.write("");
 		context.write("DROP TABLE IF EXISTS `" + table + "`;");
 		context.write("/*!40101 SET @saved_cs_client     = @@character_set_client */;");
 		context.write("/*!40101 SET character_set_client = utf8 */;");
 
 		Object[] createTableStatement = context.executeQuery("SHOW CREATE TABLE " + table, new ArrayHandler());
 		context.write(createTableStatement[1] + ";");
 		context.write("/*!40101 SET character_set_client = @saved_cs_client */;");
 	}
 
 	/**
 	 * Write the header that precedes all table data exports
 	 */
 	public static void writeTableExportHeader(String table, ExportContext context) {
 		context.write("");
 		context.write("--");
 		context.write("select 'Inserting data into " + table + "...';");
 		context.write("--");
 		context.write("");
 		context.write("LOCK TABLES `" + table + "` WRITE;");
 		context.write("/*!40000 ALTER TABLE `" + table + "` DISABLE KEYS */;");
 		context.write("");
 	}
 
 	/**
 	 * Write each row of data for a table
 	 */
 	public static void writeInsertRow(TableRow row, long rowIndex, long rowsAdded, ExportContext context) {
 		if (rowIndex == 1) {
 			if (rowsAdded > 1) {
 				context.write(";");
 				context.write("");
 			}
 			context.write("INSERT INTO " + row.getTableName() + " VALUES ");
 		}
 		else {
 			context.write(",");
 		}
 		context.getWriter().print("    (");
 		for (Iterator<ColumnValue> valIter = row.getColumnValueMap().values().iterator(); valIter.hasNext();) {
 			ColumnValue columnValue = valIter.next();
 			context.getWriter().print(columnValue.getValueForExport());
 			if (valIter.hasNext()) {
 				context.getWriter().print(",");
 			}
 		}
 		context.getWriter().print(")");
 	}
 
 	/**
 	 * Write the footer that follows all table data exports
 	 */
 	public static void writeTableExportFooter(String table, ExportContext context) {
 		context.write("");
 		context.write("/*!40000 ALTER TABLE `" + table + "` ENABLE KEYS */;");
 		context.write("UNLOCK TABLES;");
 		context.write("");
 	}
 
 	/**
 	 * Write the DDL Footer as mysqldump does
 	 */
 	public static void writeExportFooter(ExportContext context) {
 		context.write("");
 		context.write("/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;");
 		context.write("/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;");
 		context.write("/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;");
 		context.write("/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;");
 		context.write("/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;");
 		context.write("/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;");
 		context.write("/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;");
 		context.write("/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;");
 	}
 }
 
