 package com.core.database;
 
 import java.util.Date;
 import java.util.List;
 
 import android.content.Context;
 import android.util.Log;
 
 import com.core.InventoryLineItem;
 import com.core.Item;
 import com.core.ItemDescription;
 import com.database.InventoryDB;
 import com.database.InventoryLineItemBookDB;

 public class Inventory {
 	private InventoryDB db;
 	
 	public Item getByID(Context context , int id){
 		db = new InventoryDB(context);
 		Item i = db.findByID(id);
 		db.close();
 		return i;
 	}
 	
 	public List<Item> getAllStock(Context con){
 		db = new InventoryDB(con);
 		List<Item> i = db.findByDescriptionID(Item.SALE_STOCK_ID);
 		db.close();
 		return i;
 	}
 	
 	public List<Item> getItemsByInventoryLineItem(Context con,InventoryLineItem inventoryLineItem)
 	{
 		db = new InventoryDB(con);
 		List<Item> i = db.findByInventoryLineItemID(inventoryLineItem.getID());
 		db.close();
 		return i;
 	}
 	
 	public InventoryLineItem addInventoryLineItem(Context con , InventoryLineItem inventoryLineItem){
 		db = new InventoryDB(con);
 		InventoryLineItem i = db.insert(inventoryLineItem);
 		db.close();
 		return i;
 	}
 	
 	public List<Item> getItemsByItemDescription(Context con , ItemDescription itemDescription){
 		db = new InventoryDB(con);
 		List<Item> i = db.findByDescriptionID(itemDescription.getId());
 		db.close();
 		return i;
 	}
 	
 	public InventoryLineItem getInventoryLineItemByID(Context con , int id){
 		InventoryLineItemBookDB inventoryLineItemBookDB = new InventoryLineItemBookDB(con);
 		InventoryLineItem inventoryLineItem = inventoryLineItemBookDB.findByID(id);
 		inventoryLineItemBookDB.close();
 		return inventoryLineItem;
 	}
 	
 	public boolean remove(Context con , InventoryLineItem inventoryLineItem){
 		InventoryLineItemBookDB inventoryLineItemBookDB = new InventoryLineItemBookDB(con);
 		inventoryLineItemBookDB.delete(inventoryLineItem.getID());
 		inventoryLineItemBookDB.close();
 		return true;
 	}
 	
 	public boolean remove(Context con , Item item){
 		db = new InventoryDB(con);
 		db.deleteItemByID(item.getID());
 		db.close();
 		return true;
 	}
 	
 	public boolean isContains(Context con , Item item){
 		Item i = getByID(con, item.getID());
 		return i != null;
 	}
 	
 	public int getQuantity(Context con){
 		InventoryDB inventoryDB = new InventoryDB(con);
 		int i = inventoryDB.findAll().size();
 		inventoryDB.close();
 		return i;
 	}
 	
 	public List<InventoryLineItem> getInventoryLineItemBetween(Context con , Date from , Date to){
 
 		InventoryLineItemBookDB inventoryLineItemBookDB = new InventoryLineItemBookDB(con);
 		List<InventoryLineItem> inventoryLineItems = inventoryLineItemBookDB.findByDate(from, to);
 		inventoryLineItemBookDB.close();
 		return inventoryLineItems;
 	}
 }
