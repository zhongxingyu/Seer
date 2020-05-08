 package at.epu.DataAccessLayer;
 
 import java.lang.reflect.Field;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 
 import at.epu.DataAccessLayer.DataModels.BackofficeTableModel;
 import at.epu.DataAccessLayer.DataObjects.DataObject;
 
 
 public class SQLQueryProvider {
 
 	Connection databaseHandle = null;
 	Statement stm = null;
 	ResultSet rs = null;
 	ResultSet sub_rs = null;
 	String sql = new String();
 	String sql_count = new String();
 	
 	public Object[][] selectAll(BackofficeTableModel model, Connection dbHandle) {
 		Object[][] data_ = null;
 		
 		databaseHandle = dbHandle;
 		
 		try {
 			stm = databaseHandle.createStatement();
 			sql = "SELECT * FROM "+ model.getTableName();
 			sql_count = "SELECT COUNT(*) FROM "+ model.getTableName();
 			
 			rs = stm.executeQuery(sql_count);
 
 			int x = 0;
 			int rowCount = 0;
 			int colCount = model.getColumnCount();
 			while(rs.next()) {
 				rowCount = rs.getInt(1);
 			}
 			
 			rs = stm.executeQuery(sql);
 			
 			int counter = 0;
 			int[] fkindex = null;
 			// determin if there are foreign keys in the table to be replaced with real data
 			if(model.getForeignColumns().length == 1 && model.getForeignColumns()[0].equals("-")) {
 				
 			} else {
 				fkindex = new int[model.getForeignColumns().length];
 				for(int z=0;z<model.getColumnCount();z++) {
 					if(model.getForeignTableColumns().length == counter) {
 						break;
 					}
 					if(model.getColumnNames()[z].equals(model.getForeignTableColumns()[counter])) {
 						fkindex[counter] = z;
 						counter++;
 					}
 				}
 				
 			}
 			data_ = new Object[rowCount][colCount];
 			//fill the data array
 			while(rs.next()) {
 				for(int i=0;i<colCount;i++) {
 					if(model.getForeignColumns().length == 1 && model.getForeignColumns()[0].equals("-")) {
 						data_[x][i] = rs.getString(i+1);
 					} else {
 						for(int c=0;c<model.getForeignColumns().length;c++) {
 							if(i == fkindex[c]) {
 								data_[x][i] = getDataFromForeignKey(rs.getInt(1), model.getTableName(), 
 									model.getMappingTableName()[c], model.getForeignTableName()[c], 
 									model.getForeignColumns()[c], model.getForeignKeyNameFromMappingCol()[c], 
 									model.getDesiredColFromForeignKey()[c]);
 								break;
 							} else {
 								data_[x][i] = rs.getString(i+1);
 							}
 						}
 					}
 				}
 				x++;
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 			System.exit(1);
 		}
 
 		return data_;
 	}
 	
 	public ArrayList<Object> getDataFromForeignKey(int id, String mainTableName, 
 							String mappingTableName, String foreignTableName, 
 							String foreignKeyColumn, String foreignKeyFromMappingCol, 
 							String desiredColFromForeignKey) {
 		ArrayList<Object> data = new ArrayList<Object>();
 		
 		try {
 			if(mappingTableName.equals("-")) {
 				sql = "SELECT b."+desiredColFromForeignKey+" FROM "+mainTableName+" a, "+foreignTableName+" b "+
 						"WHERE a."+foreignKeyColumn+" = b.id " +
 						"AND a.id = "+id;
 			} else {
 				sql = "SELECT c."+desiredColFromForeignKey+" FROM "+mainTableName+" a, "+mappingTableName+" b, "+foreignTableName+" c " +
 					"WHERE a."+foreignKeyColumn+" = b.id " +
 					"AND b."+foreignKeyFromMappingCol+" = c.id " +
 					"AND a.id = "+id;
 			}
 			
 			sub_rs = stm.executeQuery(sql);
 	
 			while(sub_rs.next()) {
 				data.add(sub_rs.getObject(1));
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		return data;
 	}
 	
 	public void insert(BackofficeTableModel model, DataObject obj) throws SQLException {
 		Class<? extends DataObject> objClass = obj.getClass();
 		
 		Field[] fields = objClass.getFields();
 		
 		String valueString = new String();
 		
 		for(int i = 0; i < fields.length; i++) {
 			Object property = null;
 			
 			try {
 				property = fields[i].get(obj);
 			} catch (IllegalArgumentException e) {
 				e.printStackTrace();
 				throw new SQLException("Reflection failure.");
 			} catch (IllegalAccessException e) {
 				e.printStackTrace();
 				throw new SQLException("Reflection failure.");
 			}
 			
 			if(property.getClass() == String.class) {
 				valueString += "\'" + property.toString() + "\'";
 				
 				if(i != fields.length - 1) {
 					valueString += ",";
 				}
 			}
 		}
 		
 		String statement = "INSERT INTO " + model.getTableName() + " VALUES (" + valueString + ")";
 		
 		
 	}
 }
