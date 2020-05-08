 package com.orange.groupbuy.api.service;
 
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.jetty.util.log.Log;
 
 import net.sf.json.JSONArray;
 import net.sf.json.JSONObject;
 
 import com.mongodb.BasicDBList;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBObject;
 import com.orange.groupbuy.constant.DBConstants;
 import com.orange.groupbuy.constant.ServiceConstant;
 import com.orange.groupbuy.dao.App;
 import com.orange.groupbuy.dao.Category;
 import com.orange.groupbuy.dao.Product;
 import com.orange.groupbuy.dao.ShoppingCategory;
 
 public class CommonServiceUtils {
 
 	public static JSONObject userToJSON(DBObject user) {
 		JSONObject obj = new JSONObject();
 		obj.put(ServiceConstant.PARA_USERID, user.get(DBConstants.F_ID)
 				.toString());
 		return obj;
 	}
 
 	private static void safePut(JSONObject object, String key, Object value) {
 		if (value == null)
 			return;
 
 		object.put(key, value);
 	}
 
 	public static JSONArray productListToJSONArray(List<Product> products) {
 
 		if (products == null)
 			return null;
 
 		JSONArray jsonArray = new JSONArray();
 		for (Product product : products) {
 			JSONObject object = new JSONObject();
 
 			safePut(object, ServiceConstant.PARA_ID, product.getId());
 			safePut(object, ServiceConstant.PARA_LOC, product.getLoc());
 			safePut(object, ServiceConstant.PARA_CITY, product.getCity());
 			safePut(object, ServiceConstant.PARA_IMAGE, product.getImage());
 			safePut(object, ServiceConstant.PARA_TITLE, product.getTitle());
 			safePut(object, ServiceConstant.PARA_START_DATA,
 					product.getStartDateString());
 			safePut(object, ServiceConstant.PARA_END_DATA,
 					product.getEndDateString());
 			safePut(object, ServiceConstant.PARA_PRICE, product.getPrice());
 			safePut(object, ServiceConstant.PARA_VALUE, product.getValue());
 			safePut(object, ServiceConstant.PARA_BOUGHT, product.getBought());
 			// safePut(object, ServiceConstant.PARA_SITE_ID,
 			// product.getSiteId());
 			safePut(object, ServiceConstant.PARA_SITE_NAME,
 					product.getSiteName());
 			// safePut(object, ServiceConstant.PARA_SITE_URL,
 			// product.getSiteUrl());
 			safePut(object, ServiceConstant.PARA_REBATE, product.getRebate());
 			safePut(object, ServiceConstant.PARA_GPS, product.getGPS());
 			safePut(object, ServiceConstant.PARA_ADDRESS, product.getAddress());
 			safePut(object, ServiceConstant.PARA_TEL, product.getTel());
 			safePut(object, ServiceConstant.PARA_WAP_URL, product.getWapLoc());
 			safePut(object, ServiceConstant.PARA_UP, product.getUp());
 			safePut(object, ServiceConstant.PARA_DOWN, product.getDown());
 			
 			safePut(object, ServiceConstant.PARA_TOP_SCORE,
 					product.getTopScore());
 
 			jsonArray.add(object);
 		}
 		return jsonArray;
 	}
 
 	public static Object AppToJSON(App app) {
 		JSONObject obj = new JSONObject();
 		obj.put(ServiceConstant.PARA_VERSION, app.getVersion());
 		obj.put(ServiceConstant.PARA_APPURL, app.getAppUrl());
 		return obj;
 	}
 
 	public static Object appKeywordToJSON(App app) {
 
 		List<String> keywordList = app.getAppKeywordList();
 		if (keywordList == null)
 			return null;
 
 		JSONArray jsonArray = new JSONArray();
 		for (String keyword : keywordList) {
 			jsonArray.add(keyword);
 		}
 
 		return jsonArray;
 	}
 
 	public static JSONArray categoryListToJSONArray(List<Category> categoryList) {
 		if (categoryList == null)
 			return null;
 
 		JSONArray jsonArray = new JSONArray();
 		for (Category category : categoryList) {
 			JSONObject object = new JSONObject();
 
 			safePut(object, ServiceConstant.PARA_CATEGORY_NAME,
 					category.getCategoryName());
 			jsonArray.add(object);
 		}
 		return jsonArray;
 	}
 
 	public static JSONArray shoppingcategoryListToJSONArray(
 			List<ShoppingCategory> shoppingcategoryList) {
 		if (shoppingcategoryList == null)
 			return null;
 		
 		
 
 		JSONArray jsonArray = new JSONArray();
 		for (ShoppingCategory category : shoppingcategoryList) {
 			JSONObject object = new JSONObject();
 			JSONArray subjsonArray = subShoppingCategoryListToJSONArray(category.getSubCategoryList());
 
 			
 			safePut(object, ServiceConstant.PARA_CATEGORY_NAME,	category.getCategoryName());
 			safePut(object, ServiceConstant.PARA_SUB_CATEGORY,subjsonArray);
 			
 			jsonArray.add(object);
 		}
 		return jsonArray;
 	}
 	
 	public static JSONArray subShoppingCategoryListToJSONArray(
 			BasicDBList list) {
 		if(list == null)
 			return null;
 
 		JSONArray jsonArray = new JSONArray();
 		for (Object subCategory : list) {
 			String categoryId = ((BasicDBObject)subCategory).getString(DBConstants.F_SUB_CATEGORY_ID);
 			String categoryName = ((BasicDBObject)subCategory).getString(DBConstants.F_SUB_CATEGORY_NAME);
 			Object categoryKey = ((BasicDBObject) subCategory).get(DBConstants.F_SUB_CATEGORY_KEYS);
 			
 			JSONObject object = new JSONObject();
 			safePut(object, ServiceConstant.PARA_SUB_CATEGORY_ID,	categoryId);
 			safePut(object, ServiceConstant.PARA_SUB_CATEGORY_NAME,categoryName);
 			safePut(object, ServiceConstant.PARA_SUB_CATEGORY_KEYS,categoryKey);
 			jsonArray.add(object);
 		}
 		return jsonArray;
 	}
 
 	public static JSONArray userShoppingItemListToJSONArray(
 			Object list) {
 		if(list == null)
 			return null;
 
 		JSONArray jsonArray = new JSONArray();
 		jsonArray.add(list);
 	
 		return jsonArray;
 	}
 	
 	public static JSONArray userShoppingItemProductCountToJSONArray(Map<String, Integer> map) {
 	    JSONArray jsonArray = new JSONArray();
         for(String itemId : map.keySet()) {
             JSONObject object = new JSONObject();
             object.put(ServiceConstant.PARA_ITEMID, itemId);
             object.put(ServiceConstant.PARA_MATCH_ITEM_COUNT, map.get(itemId));
             jsonArray.add(object);
         }
         return jsonArray;
 	}
 }
