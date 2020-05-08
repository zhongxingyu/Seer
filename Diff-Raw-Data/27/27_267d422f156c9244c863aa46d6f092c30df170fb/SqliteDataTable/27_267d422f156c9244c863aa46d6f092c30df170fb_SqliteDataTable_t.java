 /*******************************************************************************
  * Copyright (c) Sam Bell.
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published
  * by the Free Software Foundation, either version 3 of the License,
  * or  any later version.
  * 
  * This program is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See
  * the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * 
  * Contributors:
  *     Sam Bell - initial API and implementation
  ******************************************************************************/
 /**
  * 
  */
 package com.mimpidev.sql.sqlitejdbc;
 
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import com.mimpidev.sql.sqlitejdbc.exceptions.SqliteException;
 import com.mimpidev.sql.sqlitejdbc.schema.SqliteConflictAction;
 
 /**
  * @author bugman
  *
  */
 public class SqliteDataTable {
 
 	private String name;
 	private Database db;
 	
 	public SqliteDataTable(Database db, String name) {
 		setDb(db);
 		setName(name);
 	}
 
	/*TODO: Need to change the insert and update statements
	 * 1. Move conversion of map to string into 1 function
	 * 2. use the table definition to decide how to format the data, rather than doing a test to see if
	 *    its a number (as this may fail if somebody decides to store a number in a text field
	 */
	
 	public Object insert(SqliteConflictAction onConflict,
 			Map<String, Object> values) throws SqliteException {
 		Statement sql;
 		String columnNames = "";
 		String valuesString = "";
 		Set<String> keys = values.keySet();
 		for (String key : keys){
 			if (columnNames.length()>0){
 				columnNames+=", ";
 				valuesString+=", ";
 			}
 			columnNames+=key.toUpperCase();
 			try {
 				Double.parseDouble((String)values.get(key));
 				valuesString+=(String)values.get(key);
 			} catch (NumberFormatException e){
				valuesString+="'"+((String)values.get(key)).replaceAll("\'", "&apos;")+"'";
 			}
 		}
 		String insertCommand="INSERT INTO "+name+"("+columnNames+") VALUES("+valuesString+");";
 		try {
 			synchronized(db){
 				sql = db.getConnection().createStatement();
 				return Long.valueOf((long)sql.executeUpdate(insertCommand));
 			}
 		} catch (SQLException e) {
 			throw new SqliteException("Error inserting record into table: "+name,insertCommand);
 		}
 	}
 
 	public Object clear() throws SqliteException {
 		Statement sql;
 		
 		try {
 			synchronized(db){
 				sql= db.getConnection().createStatement();
 				sql.execute("DELETE FROM "+name);
 				return Boolean.TRUE;
 			}
 		} catch (SQLException e) {
 			throw new SqliteException("Error purging table data in table: "+name);
 		}
 	}
 
 	/**
 	 * @return the name
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * @param name the name to set
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	/**
 	 * @return the db
 	 */
 	public Database getDb() {
 		return db;
 	}
 
 	/**
 	 * @param db the db to set
 	 */
 	public void setDb(Database db) {
 		this.db = db;
 	}
 
 	/**
 	 * command for updating values in the table
 	 * @param datafields
 	 * @param where
 	 * @return
 	 * @throws SqliteException 
 	 */
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	public Object update(Map datafields, String where) throws SqliteException {
 		Statement sql;
 		String setString="";
 		Set<String> keys = datafields.keySet();
 		for (String key : keys){
 			if (setString.length()>0){
 				setString+=", ";
 			}
 			setString+=key.toLowerCase()+"=";
 			try {
 				Double.parseDouble((String)datafields.get(key));
 				setString+=(String)datafields.get(key);
 			} catch (NumberFormatException e) {
				setString+="'"+((String)datafields.get(key)).replaceAll("\'", "&apos;")+"'";
 			}
 		}
 		String updateCommand="UPDATE "+getName()+" SET "+setString+" WHERE "+where+";";
 		try {
 			synchronized(db){
 				sql = db.getConnection().createStatement();
 				return Long.valueOf((long)sql.executeUpdate(updateCommand));
 			}
 		} catch (SQLException e) {
			e.printStackTrace();
 			System.err.println(updateCommand);
 			throw new SqliteException("Error inserting record into table: "+name,updateCommand);
 		}
 	}
 
 	/**
 	 * 
 	 * @param conditions
 	 * @return
 	 * @throws SqliteException 
 	 */
 	public Object delete(HashMap<String, Object> conditions) throws SqliteException {
 		String whereClause = "";
 		Set<String> keys = conditions.keySet();
 		for (String key : keys){
 			if (whereClause.length()>0){
 				whereClause+=" AND ";
 			}
 			whereClause+=(String)key.toLowerCase()+"=";
 			try {
 				Double.parseDouble((String)conditions.get(key));
 				whereClause+=(String)conditions.get(key);
 			} catch (NumberFormatException e) {
 				whereClause+="'"+(String)conditions.get(key)+"'";
 			}
 		}
 		return delete(whereClause);
 	}
 	
 	public Object delete(String whereClause) throws SqliteException{
 		Statement sql;
 		String deleteCommand="DELETE FROM "+getName()+" WHERE "+whereClause+";";
 		try {
 			synchronized(db){
 				sql = db.getConnection().createStatement();
 				return Long.valueOf((long)sql.executeUpdate(deleteCommand));
 			}
 		} catch (SQLException e) {
 			throw new SqliteException("Error inserting record into table: "+name,deleteCommand);
 		}
 	}
 }
