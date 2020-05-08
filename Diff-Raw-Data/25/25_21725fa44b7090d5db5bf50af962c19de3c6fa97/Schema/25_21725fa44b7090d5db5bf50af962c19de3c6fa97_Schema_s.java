 package net.eagledb.server.storage;
 
 import java.util.ArrayList;
 
 public class Schema {
 
 	private String name;
 
 	private ArrayList<Table> tables;
 
 	public Schema(String name) {
 		this.name = name;
 		tables = new ArrayList<Table>();
 	}
 
 	/**
 	 * @return the name
 	 */
 	public String getName() {
 		return name;
 	}
 
 	public Table getTable(String tableName) {
 		for(Table table : tables) {
 			if(table.getName().equals(tableName))
 				return table;
 		}
 		return null;
 	}
 
 	public synchronized void addTable(Table table) {
 		tables.add(table);
 	}
 
	public synchronized void dropTable(String tableName) {
 		for(int i = 0; i < tables.size(); ++i) {
 			if(tables.get(i).getName().equals(tableName)) {
 				tables.remove(i);
				return;
 			}
 		}
 	}
 
 	public ArrayList<Table> getTables() {
 		return tables;
 	}
 
 }
