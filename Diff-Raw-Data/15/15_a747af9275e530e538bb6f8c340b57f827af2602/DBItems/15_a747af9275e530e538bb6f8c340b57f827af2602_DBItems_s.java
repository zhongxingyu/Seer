 package database;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.HashMap;
 
 public class DBItems{
 
 	protected static Statement statement;
 	protected static ResultSet result;
 	public static HashMap<Integer, Item> map;
 	
 	// SQL
 	protected final static String sqlInsertItem = "INSERT INTO items (name, slot_type, weight, model_id) VALUES (?, ?, ?, ?)";
 	protected final static String sqlDeleteItem = "DELETE FROM items WHERE id = ?";
 	
 	public DBItems() throws SQLException{
 		map = new HashMap<Integer, Item>();
 		readTable();
 	}
 	
 	private static void readTable() throws SQLException{
 		// id, name, slot_type, weight, model_id
 		Statement statement = DataBase.connectionUnits.createStatement();
 		
 		String sql = "SELECT * FROM items";
 		result = statement.executeQuery(sql);
 		
 		while(result.next()){
 			map.put(result.getInt("id"), new Item(result.getInt("id"),
 												  result.getInt("slot_type"),
 												  result.getString("name"),
 												  result.getInt("weight"),
 												  result.getInt("model_id")
 												  
 					));
 		}
 		System.out.println("DB.Items loaded. Fields: " + map.size());
 	}
 	
 	public static void addItem(int itemType, String name, int weight, int modelId) throws SQLException{
 		map.put(map.size()+1, new Item(map.size()+1, itemType, name, weight, modelId));
 		
 		// Add item to MySQL
		PreparedStatement statement = DataBase.connectionAccounts.prepareStatement(sqlInsertItem);
 		statement.setString(1, name);
 		statement.setInt(2, itemType);
 		statement.setInt(3, weight);
 		statement.setInt(4, modelId);
 		statement.execute();
 		System.out.println("Item added;\n");
 	}
 	
 	public static void deleteItem(int id) throws SQLException{
 		PreparedStatement statement = DataBase.connectionAccounts.prepareStatement(sqlDeleteItem);
 		map.remove(id);
 		statement.setInt(1, id);
 		statement.execute();
 		System.out.println("Item deleted;\n");
 	}
 	
 	protected void finalize() throws SQLException{
 		if(statement != null)
 			statement.close();
 		if(result != null)
 			result.close();
 	}
 	
 	public static void showAllItems(){
 		for(Item item: map.values()){
			System.out.println("id: " + item.id 
 					+ "\tname: " + item.name 
 					+ "\tslot_type: " + item.itemType 
 					+ "\tweight: " + item.weight 
 					+ "\tmodel_id: " + item.modelId);
 		}
 	}
 }
 
 class Item{
 	public int id;
 	public int itemType;
 	public String name;
 	public int weight;
 	public int modelId;
 	
 	Item(int id, int itemType, String name, int weight, int modelId){
 		this.id = id;
 		this.itemType = itemType;
 		this.name = name;
 		this.weight = weight;
 		this.modelId = modelId;
 	}
 }
