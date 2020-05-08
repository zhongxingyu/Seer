 package com.codepath.apps.nommable.models;
 
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
import android.util.Log;

 public class MenuEntry {
 	
 	private String name;
 	private String price;
 	private String description;
 	private Type type;
 	
 	public enum Type {
 		MENU,
 		SECTION,
 		ENTRY
 	}
 	
 	public String getName() {
 		return name;
 	}
 	
 	public String getPrice() {
 		return price;
 	}
 	
 	public String getDescription() {
 		return description;
 	}
 	
 	public Type getType() {
 		return type;
 	}
 	
 	// Might make more sense to use subclasses instead of a separate method for each
 		
 	private static MenuEntry createMenu(JSONObject jsonObject) {
 		MenuEntry m = new MenuEntry();
 		
 		try {
 			m.type = Type.MENU;
 			m.name = jsonObject.getString("name");
 			m.description = jsonObject.getString("description");
 		} catch (JSONException e) {
 			e.printStackTrace();
 			return null;
 		}
 
 		return m;
 	}
 	
 	private static MenuEntry createSection(JSONObject jsonObject) {
 		MenuEntry m = new MenuEntry();
 		
 		try {
 			m.type = Type.SECTION;
 			m.name = jsonObject.getString("name");
 		} catch (JSONException e) {
 			e.printStackTrace();
 			return null;
 		}
 
 		return m;
 	}
 	
 	private static MenuEntry createEntry(JSONObject jsonObject) {
 		MenuEntry m = new MenuEntry();
 		
 		try {
 			m.type = Type.ENTRY;
 			m.name = jsonObject.getString("name");
			
			if (jsonObject.has("price")) {
				m.price = jsonObject.getString("price");
			} else {
				m.price = "";
			}
			
			if (jsonObject.has("description")) {
				m.description = jsonObject.getString("description");
			} else {
				m.description = "";
			}
			
 		} catch (JSONException e) {
 			e.printStackTrace();
 			return null;
 		}
 		
 		return m;
 	}
 	
 	// TODO: can this be cleaned up with recursion?
 	// YEP - make a list of class types or something, and iterate though it - return from the function once it reaches a certain depth
 	// use Class.forName().newInstance
 	
 	public static ArrayList<MenuEntry> fromJson(JSONArray jsonArray) {
 		ArrayList<MenuEntry> items = new ArrayList<MenuEntry>();
 		for (int i = 0; i < jsonArray.length(); i++) {
 			JSONObject jsonMenu = null;
 			try {
 				jsonMenu = jsonArray.getJSONObject(i);
 			} catch (JSONException e) {
 				e.printStackTrace();
 				continue;
 			}
 			
 			MenuEntry item = createMenu(jsonMenu);
 			if (item != null) {
 				items.add(item);
 			}
 			try {
 				JSONArray jsonCategories = jsonMenu.getJSONObject("entries").getJSONArray("items");			
 				for (int j = 0; j < jsonCategories.length(); j++) {
 					JSONObject jsonCategory = null;
 					jsonCategory = jsonCategories.getJSONObject(j);
 					
 					MenuEntry sectionItem = createSection(jsonCategory);
 					if (sectionItem != null) {
 						items.add(sectionItem);
 					}
 					
 					JSONArray jsonEntries = jsonCategory.getJSONObject("entries").getJSONArray("items");
 					for (int k = 0; k < jsonEntries.length(); k++) {
 						JSONObject jsonEntry = null;
 						jsonEntry = jsonEntries.getJSONObject(k);
 						
 						MenuEntry entryItem = createEntry(jsonEntry);
 						if (entryItem != null) {
 							items.add(entryItem);
 						}
 					}
 				}
 			} catch (JSONException e) {
 				e.printStackTrace();
 				continue;
 			}
 		}
 		return items;
 	}
 	
 }
