 package com.example.swp_ucd_2013_eule.model;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import android.util.Log;
 
 import com.example.swp_ucd_2013_eule.model.APIModel;
 import com.example.swp_ucd_2013_eule.model.Item;
 import com.example.swp_ucd_2013_eule.net.APIException;
 
 public class MyMarket {
 
 	private static MyMarket INSTANCE = new MyMarket();
 
 	private APIModel<Item, Item> mItemAPI;
 
 	private List<Item> items = new LinkedList<Item>();
 
 	private MyMarket() {
 
 	}
 
	public void loadMarket() {
		mItemAPI = new APIModel<Item, Item>(Item.class);
 		try {
 			items = mItemAPI.getAll(new Item());
 		} catch (APIException e) {
 			Log.e("MyMarket",
 					"Could not retrieve Items! Exception: " + e.getCause());
 		}
 	}
 
 	public static MyMarket getInstance() {
 		return INSTANCE;
 	}
 
 	public List<Item> getItems() {
 		return items;
 	}
 	
 	public List<Item> getItems(String category) {
 		List<Item> result = new LinkedList<Item>();
 		for (Item item : items) {
 			if (item.getCategory().equals(category)) {
 				result.add(item);
 			}
 		}
 		return result;
 	}
 
 }
