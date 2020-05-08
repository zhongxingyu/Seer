 package com.orange.groupbuy.api.service;
 
 import java.util.List;
 
 import net.sf.json.JSONArray;
 import net.sf.json.JSONObject;
 
 import com.mongodb.DBObject;
 import com.orange.common.mongodb.MongoDBClient;
 import com.orange.groupbuy.constant.ServiceConstant;
 import com.orange.groupbuy.dao.App;
 import com.orange.groupbuy.dao.Product;
 
 public class CommonServiceUtils {
 
 	public static JSONObject userToJSON(DBObject user) {
 		JSONObject obj = new JSONObject();
		obj.put(ServiceConstant.PARA_USERID, user.get(MongoDBClient.ID));
 		return obj;
 	}
 	
 	
 	public static JSONArray productListToJSONArray(List<Product> products){
 		
 		if (products == null)
 			return null;
 		
 		JSONArray jsonArray = new JSONArray();
 		for(Product product : products){
 			JSONObject object = new JSONObject();
 			
 			object.put(ServiceConstant.PARA_ID, product.getId());
 			object.put(ServiceConstant.PARA_LOC, product.getLoc());
 			object.put(ServiceConstant.PARA_CITY, product.getCity());
 			object.put(ServiceConstant.PARA_IMAGE, product.getImage());
 			object.put(ServiceConstant.PARA_TITLE, product.getTitle());
 			object.put(ServiceConstant.PARA_START_DATA, product.getStartDateString());
 			object.put(ServiceConstant.PARA_END_DATA, product.getEndDateString());
 			object.put(ServiceConstant.PARA_PRICE, product.getPrice());
 			object.put(ServiceConstant.PARA_VALUE, product.getValue());
 			object.put(ServiceConstant.PARA_BOUGHT, product.getBought());
 			object.put(ServiceConstant.PARA_SITE_ID, product.getSiteId());
 			object.put(ServiceConstant.PARA_SITE_NAME, product.getSiteName());
 			object.put(ServiceConstant.PARA_SITE_URL, product.getSiteUrl());
 			object.put(ServiceConstant.PARA_REBATE, product.getRebate());
 			object.put(ServiceConstant.PARA_GPS, product.getGPS());
 			object.put(ServiceConstant.PARA_ADDRESS, product.getAddress());
 			object.put(ServiceConstant.PARA_TEL, product.getTel());
 			object.put(ServiceConstant.PARA_WAP_URL, product.getWapLoc());
 			
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
 
 }
