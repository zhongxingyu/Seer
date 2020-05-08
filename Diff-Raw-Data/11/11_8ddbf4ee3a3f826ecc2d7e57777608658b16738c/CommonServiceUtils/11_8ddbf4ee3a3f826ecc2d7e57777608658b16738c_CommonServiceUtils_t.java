 package com.orange.groupbuy.api.service;
 
 import java.util.List;
 
 import net.sf.json.JSONArray;
 import net.sf.json.JSONObject;
 
 import com.mongodb.DBObject;
 import com.orange.common.mongodb.MongoDBClient;
 import com.orange.groupbuy.constant.DBConstants;
 import com.orange.groupbuy.constant.ServiceConstant;
 import com.orange.groupbuy.dao.App;
 import com.orange.groupbuy.dao.Product;
 
 public class CommonServiceUtils {
 
 	public static JSONObject userToJSON(DBObject user) {
 		JSONObject obj = new JSONObject();
 		obj.put(ServiceConstant.PARA_USERID, user.get(DBConstants.F_ID).toString());
 		return obj;
 	}
 	
 	private static void safePut(JSONObject object, String key, Object value){
 		if (value == null)
 			return;
 		
 		object.put(key, value);		
 	}
 	
 	public static JSONArray productListToJSONArray(List<Product> products){
 		
 		if (products == null)
 			return null;
 		
 		JSONArray jsonArray = new JSONArray();
 		for(Product product : products){
 			JSONObject object = new JSONObject();
 			
 			safePut(object, ServiceConstant.PARA_ID, product.getId());
 			safePut(object, ServiceConstant.PARA_LOC, product.getLoc());
 			safePut(object, ServiceConstant.PARA_CITY, product.getCity());
 			safePut(object, ServiceConstant.PARA_IMAGE, product.getImage());
 			safePut(object, ServiceConstant.PARA_TITLE, product.getTitle());
 			safePut(object, ServiceConstant.PARA_START_DATA, product.getStartDateString());
 			safePut(object, ServiceConstant.PARA_END_DATA, product.getEndDateString());
 			safePut(object, ServiceConstant.PARA_PRICE, product.getPrice());
 			safePut(object, ServiceConstant.PARA_VALUE, product.getValue());
 			safePut(object, ServiceConstant.PARA_BOUGHT, product.getBought());
 //			safePut(object, ServiceConstant.PARA_SITE_ID, product.getSiteId());
 			safePut(object, ServiceConstant.PARA_SITE_NAME, product.getSiteName());
 //			safePut(object, ServiceConstant.PARA_SITE_URL, product.getSiteUrl());
 			safePut(object, ServiceConstant.PARA_REBATE, product.getRebate());
 			safePut(object, ServiceConstant.PARA_GPS, product.getGPS());
 			safePut(object, ServiceConstant.PARA_ADDRESS, product.getAddress());
 			safePut(object, ServiceConstant.PARA_TEL, product.getTel());
 			safePut(object, ServiceConstant.PARA_WAP_URL, product.getWapLoc());
 			safePut(object, ServiceConstant.PARA_UP, product.getUp());
 			safePut(object, ServiceConstant.PARA_DOWN, product.getDown());
			safePut(object, ServiceConstant.PARA_TOP_SCORE, product.getTopScore());
 			
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
 		for(String keyword : keywordList){
 			jsonArray.add(keyword);
 		}
 		
 		return jsonArray;
 	}
 
 }
