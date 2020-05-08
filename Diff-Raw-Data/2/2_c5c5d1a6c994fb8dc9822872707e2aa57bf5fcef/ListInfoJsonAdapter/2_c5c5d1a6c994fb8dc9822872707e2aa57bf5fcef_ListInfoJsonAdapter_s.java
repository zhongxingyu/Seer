 /**
  * 
  */
 package com.blumenthal.listey;
 
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Map.Entry;
 
 import com.google.gson.JsonArray;
 import com.google.gson.JsonDeserializationContext;
 import com.google.gson.JsonDeserializer;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParseException;
 import com.google.gson.JsonSerializationContext;
 import com.google.gson.JsonSerializer;
 import com.google.gson.reflect.TypeToken;
 
 public class ListInfoJsonAdapter implements JsonDeserializer<ListInfo>, JsonSerializer<ListInfo> {
 	/**
 	 * Deserialize the list info.  Note, the ListInfo uniqueID is actually the key of the
 	 * parent map and not included in this at all.
 	 */
 	@Override
 	public ListInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
 			throws JsonParseException {
 		ListInfo listInfo = new ListInfo();
 
 		JsonObject topMap = json.getAsJsonObject();
 		listInfo.lastUpdate = topMap.get(ListInfo.LAST_UPDATE).getAsLong();
 		listInfo.name = topMap.get(ListInfo.NAME).getAsString();
 		listInfo.status = ListInfo.ListInfoStatus.valueOf(topMap.get(ListInfo.STATUS).getAsString());
 
 		if (topMap.has(ListInfo.ITEMS)) {
 			JsonArray itemsJson = topMap.get(ListInfo.ITEMS).getAsJsonArray();
 			for (JsonElement itemJsonElement : itemsJson) {
 				ItemInfo item = context.deserialize(itemJsonElement, ItemInfo.class);
 
 				//Add it to the map.  Note, order is lost, but since it's always alphabetical order it's ok
 				listInfo.items.put(item.uniqueId, item);
 			}//for itemsJson
 		}//if has items
 
 		if (topMap.has(ListInfo.CATEGORIES)) {
 			JsonArray categoriesJson = topMap.get(ListInfo.CATEGORIES).getAsJsonArray();
 			//Hack needed to make it deserialize to an ArrayList correctly.
 			//http://stackoverflow.com/questions/5554217/google-gson-deserialize-listclass-object-generic-type
 			Type listType = new TypeToken<ArrayList<CategoryInfo>>() {
             }.getType();
 			listInfo.categories = context.deserialize(categoriesJson, listType);
 		}//if has categories
 		
 		if (topMap.has(ListInfo.SELECTED_CATEGORIES)) {
 			JsonArray selCatJson = topMap.get(ListInfo.SELECTED_CATEGORIES).getAsJsonArray();
 			Type listType = new TypeToken<HashSet<String>>() {
             }.getType();
 			listInfo.selectedCategories = context.deserialize(selCatJson, listType);
 		}//if has selectedCategories
 
 		return listInfo;
 	}//deserialize
 
 
 	/* (non-Javadoc)
 	 * @see com.google.gson.JsonSerializer#serialize(java.lang.Object, java.lang.reflect.Type, com.google.gson.JsonSerializationContext)
 	 */
 	@Override
 	public JsonElement serialize(ListInfo listInfo, Type type,
 			JsonSerializationContext context) {
 		JsonObject rv = new JsonObject();
 		rv.addProperty(ListInfo.LAST_UPDATE, listInfo.lastUpdate);
 		rv.addProperty(ListInfo.NAME, listInfo.name);
		rv.addProperty(ListInfo.STATUS, ListInfo.STATUS.toString());
 		
 		if (!listInfo.items.isEmpty()){
 			JsonArray itemsJson = new JsonArray();
 			for (Entry<String, ItemInfo> entry : listInfo.items.entrySet()) {
 				ItemInfo item = entry.getValue();
 				JsonObject itemJson = context.serialize(item).getAsJsonObject();
 				itemJson.addProperty(ItemInfo.UNIQUE_ID, item.uniqueId);
 				itemsJson.add(itemJson);
 			}//for each entry
 			rv.add(ListInfo.ITEMS, itemsJson);
 		}//if items
 		
 		if (!listInfo.categories.isEmpty()) {
 			JsonElement categoriesJson = context.serialize(listInfo.categories);
 			rv.add(ListInfo.CATEGORIES, categoriesJson);			
 		}
 		
 		if (!listInfo.selectedCategories.isEmpty()) {
 			JsonElement selectedCategoriesJson = context.serialize(listInfo.selectedCategories);
 			rv.add(ListInfo.SELECTED_CATEGORIES, selectedCategoriesJson);			
 		}
 		
 		return rv;
 	}//serialize
 }//ListInfoJsonAdapter
