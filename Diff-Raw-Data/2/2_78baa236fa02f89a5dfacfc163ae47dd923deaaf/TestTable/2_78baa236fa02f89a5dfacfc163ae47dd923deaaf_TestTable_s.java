 /*******************************************************************************
  * Copyright (c) 2013 - 2014 Maksym Barvinskyi.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v2.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
  * 
  * Contributors:
  *     Maksym Barvinskyi - initial API and implementation
  ******************************************************************************/
 package org.grible.adaptor;
 
 import java.io.File;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.apache.commons.lang3.StringUtils;
 import org.grible.adaptor.helpers.IOHelper;
 import org.grible.adaptor.json.Key;
 import org.grible.adaptor.json.TableJson;
 
 import com.google.gson.Gson;
 
 /**
  * Class that represents Test Table entity from Grible.
  * 
  * @author Maksym Barvinskyi
  * 
  */
 public class TestTable {
 	private String tableName;
 	private String productName;
 	private String productPath;
 
 	public TestTable(String name) {
 		this.tableName = name;
 		this.productName = GribleSettings.getProductName();
 		this.productPath = GribleSettings.getProductPath();
 		if (GribleSettings.getAppType() == AppTypes.POSTGRESQL) {
 			initializeSQLDriver();
 		}
 	}
 
 	/**
 	 * Retrieves data from Preconditions sheet.
 	 * 
 	 * @return HashMap<ParameterName, ParameterValue>.
 	 */
 	public HashMap<String, String> getPreconditionsTable() {
 		return getOneRowTable("precondition");
 	}
 
 	/**
 	 * Retrieves data from Postconditions sheet.
 	 * 
 	 * @return HashMap<ParameterName, ParameterValue>.
 	 */
 	public HashMap<String, String> getPostconditionsTable() {
 		return getOneRowTable("postcondition");
 	}
 
 	private HashMap<String, String> getOneRowTable(String subTableType) {
 		HashMap<String, String> result = new HashMap<String, String>();
 		try {
 			if (GribleSettings.getAppType() == AppTypes.POSTGRESQL) {
 				Connection conn = getConnection();
 				Statement stmt = conn.createStatement();
 
 				String strKeys = null;
 				String strValues = null;
 
				ResultSet rs = stmt.executeQuery("SELECT t.keys, t.values" + "FROM tables t "
 						+ "INNER JOIN tabletypes tt ON t.type = tt.id " + "INNER JOIN tables pt ON t.parentid=pt.id "
 						+ "INNER JOIN categories c ON pt.categoryid=c.id "
 						+ "INNER JOIN products p ON c.productid=p.id "
 						+ "INNER JOIN tabletypes ctt ON c.type = ctt.id " + "WHERE tt.name='" + subTableType
 						+ "' AND pt.name='" + tableName + "' AND p.name='" + productName + "' AND ctt.name='table'");
 				if (rs.next()) {
 					strKeys = rs.getString("keys");
 					strValues = rs.getString("values");
 				}
 
 				conn.close();
 				rs.close();
 				stmt.close();
 
 				if (strKeys != null && strValues != null) {
 					Gson gson = new Gson();
 					Key[] keys = gson.fromJson(strKeys, Key[].class);
 					String[][] values = gson.fromJson(strValues, String[][].class);
 					for (int j = 0; j < values[0].length; j++) {
 						result.put(keys[j].getName(), values[0][j].replace("\\", File.separator));
 					}
 				} else {
 					throw new Exception(StringUtils.capitalize(subTableType) + "s in the table '" + tableName
 							+ "' of product '" + productName + "' not found.");
 				}
 			} else {
 				String fileName = tableName + "_" + subTableType.toUpperCase() + ".json";
 				File file = IOHelper.searchFile(new File(productPath + File.separator + "TestTables"), fileName);
 				if (file == null) {
 					throw new Exception("File '" + fileName + "' not found in directory '"
 							+ new File(productPath + File.separator + "TestTables").getAbsolutePath() + "'.");
 				}
 				TableJson tableJson = IOHelper.parseTableJson(file);
 				Key[] keys = tableJson.getKeys();
 				String[][] values = tableJson.getValues();
 				for (int j = 0; j < values[0].length; j++) {
 					result.put(keys[j].getName(), values[0][j].replace("\\", File.separator));
 				}
 			}
 		} catch (Exception e) {
 			GribleSettings.getErrorsHandler().onAdaptorFail(e);
 		}
 		return result;
 	}
 
 	/**
 	 * Retrieves data from General sheet.
 	 * 
 	 * @return ArrayList of HashMap<ParameterName, ParameterValue>.
 	 */
 	public List<HashMap<String, String>> getGeneralTable() {
 		return getValuesFromGrible("table");
 	}
 
 	List<HashMap<String, String>> getDataStorageValues() {
 		return getValuesFromGrible("storage");
 	}
 
 	HashMap<Integer, HashMap<String, String>> getDataStorageValues(Integer[] iterationNumbers) {
 		return getValuesFromGrible(iterationNumbers);
 	}
 
 	private List<HashMap<String, String>> getValuesFromGrible(String entityType) {
 		List<HashMap<String, String>> result = new ArrayList<HashMap<String, String>>();
 		try {
 			if (GribleSettings.getAppType() == AppTypes.POSTGRESQL) {
 				Connection conn = getConnection();
 				Statement stmt = conn.createStatement();
 
 				String nameColumn = "";
 				if (entityType.equals("table")) {
 					nameColumn = "name";
 				} else {
 					nameColumn = "classname";
 				}
 
 				String strKeys = null;
 				String strValues = null;
 
 				ResultSet rs = stmt.executeQuery("SELECT t.keys, t.values " + "FROM tables t "
 						+ "INNER JOIN tabletypes tt ON t.type = tt.id "
 						+ "INNER JOIN categories c ON t.categoryid=c.id "
 						+ "INNER JOIN products p ON c.productid=p.id "
 						+ "INNER JOIN tabletypes ctt ON c.type = ctt.id " + "WHERE tt.name='" + entityType + "' AND t."
 						+ nameColumn + "='" + tableName + "' AND p.name='" + productName + "'");
 				if (rs.next()) {
 					strKeys = rs.getString("keys");
 					strValues = rs.getString("values");
 				}
 
 				if (strKeys != null && strValues != null) {
 					Gson gson = new Gson();
 					Key[] keys = gson.fromJson(strKeys, Key[].class);
 					String[][] values = gson.fromJson(strValues, String[][].class);
 					for (int i = 0; i < values.length; i++) {
 						HashMap<String, String> row = new HashMap<String, String>();
 						for (int j = 0; j < values[0].length; j++) {
 							row.put(keys[j].getName(), values[i][j].replace("\\", File.separator));
 						}
 						result.add(row);
 					}
 				} else {
 					throw new Exception(StringUtils.capitalize(entityType) + " with name '" + tableName
 							+ "' not found in product '" + productName + "'.");
 				}
 
 				conn.close();
 				rs.close();
 				stmt.close();
 			} else {
 
 				File file = null;
 				if (entityType.equals("table")) {
 					String fileName = tableName + ".json";
 					String sectionDir = "TestTables";
 					file = IOHelper.searchFile(new File(productPath + File.separator + sectionDir), fileName);
 					if (file == null) {
 						throw new Exception("File '" + fileName + "' not found in directory '"
 								+ new File(productPath + File.separator + sectionDir).getAbsolutePath() + "'.");
 					}
 				} else {
 					String className = tableName;
 					String sectionDir = "DataStorages";
 					file = IOHelper.searchFileByClassName(new File(productPath + File.separator + sectionDir),
 							className);
 					if (file == null) {
 						throw new Exception("File with class name '" + className + "' not found in directory '"
 								+ new File(productPath + File.separator + sectionDir).getAbsolutePath() + "'.");
 					}
 				}
 
 				TableJson tableJson = IOHelper.parseTableJson(file);
 				Key[] keys = tableJson.getKeys();
 				String[][] values = tableJson.getValues();
 				for (int i = 0; i < values.length; i++) {
 					HashMap<String, String> row = new HashMap<String, String>();
 					for (int j = 0; j < values[0].length; j++) {
 						row.put(keys[j].getName(), values[i][j].replace("\\", File.separator));
 					}
 					result.add(row);
 				}
 			}
 		} catch (Exception e) {
 			GribleSettings.getErrorsHandler().onAdaptorFail(e);
 		}
 		if (result.isEmpty()) {
 			String message = "Grible error: " + entityType + " '" + tableName + "' is missing.";
 			GribleSettings.getErrorsHandler().onAdaptorFail(new Exception(message));
 		}
 		return result;
 	}
 
 	private HashMap<Integer, HashMap<String, String>> getValuesFromGrible(Integer[] iterationNumbers) {
 		HashMap<Integer, HashMap<String, String>> result = new HashMap<Integer, HashMap<String, String>>();
 		try {
 			if (GribleSettings.getAppType() == AppTypes.POSTGRESQL) {
 				Connection conn = getConnection();
 				Statement stmt = conn.createStatement();
 
 				String strKeys = null;
 				String strValues = null;
 
 				ResultSet rs = stmt.executeQuery("SELECT t.keys, t.values " + "FROM tables t "
 						+ "INNER JOIN tabletypes tt ON t.type = tt.id "
 						+ "INNER JOIN categories c ON t.categoryid=c.id "
 						+ "INNER JOIN products p ON c.productid=p.id "
 						+ "INNER JOIN tabletypes ctt ON c.type = ctt.id " + "WHERE tt.name='storage' AND t.classname='"
 						+ tableName + "' AND p.name='" + productName + "'");
 				if (rs.next()) {
 					strKeys = rs.getString("keys");
 					strValues = rs.getString("values");
 				}
 
 				if (strKeys != null && strValues != null) {
 					Gson gson = new Gson();
 					Key[] keys = gson.fromJson(strKeys, Key[].class);
 					String[][] values = gson.fromJson(strValues, String[][].class);
 					for (int i = 0; i < iterationNumbers.length; i++) {
 						HashMap<String, String> row = new HashMap<String, String>();
 						for (int j = 0; j < values[0].length; j++) {
 							row.put(keys[j].getName(), values[iterationNumbers[i] - 1][j].replace("\\", File.separator));
 						}
 						result.put(iterationNumbers[i], row);
 					}
 				} else {
 					throw new Exception("Storage with name '" + tableName + "' not found in product '" + productName
 							+ "'.");
 				}
 
 				conn.close();
 				rs.close();
 				stmt.close();
 			} else {
 				String className = tableName;
 				String sectionDir = "DataStorages";
 
 				File file = IOHelper.searchFileByClassName(new File(productPath + File.separator + sectionDir),
 						className);
 				if (file == null) {
 					throw new Exception("File with class name '" + className + "' not found in directory '"
 							+ new File(productPath + File.separator + sectionDir).getAbsolutePath() + "'.");
 				}
 				TableJson tableJson = IOHelper.parseTableJson(file);
 				Key[] keys = tableJson.getKeys();
 				String[][] values = tableJson.getValues();
 				for (int i = 0; i < iterationNumbers.length; i++) {
 					HashMap<String, String> row = new HashMap<String, String>();
 					for (int j = 0; j < values[0].length; j++) {
 						row.put(keys[j].getName(), values[iterationNumbers[i] - 1][j].replace("\\", File.separator));
 					}
 					result.put(iterationNumbers[i], row);
 				}
 			}
 		} catch (Exception e) {
 			GribleSettings.getErrorsHandler().onAdaptorFail(e);
 		}
 		return result;
 	}
 
 	private Connection getConnection() throws SQLException {
 		String dbhost = GribleSettings.getDbHost();
 		String dbport = GribleSettings.getDbPort();
 		String dbName = GribleSettings.getDbName();
 		String dblogin = GribleSettings.getDbLogin();
 		String dbpswd = GribleSettings.getDbPswd();
 
 		Connection conn = DriverManager.getConnection("jdbc:postgresql://" + dbhost + ":" + dbport + "/" + dbName,
 				dblogin, dbpswd);
 		return conn;
 	}
 
 	private void initializeSQLDriver() {
 		try {
 			Class.forName("org.postgresql.Driver").newInstance();
 		} catch (Exception e) {
 			GribleSettings.getErrorsHandler().onAdaptorFail(e);
 		}
 	}
 }
