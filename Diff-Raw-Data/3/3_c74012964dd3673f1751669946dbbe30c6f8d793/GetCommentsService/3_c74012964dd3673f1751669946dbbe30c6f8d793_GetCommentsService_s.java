 package com.orange.groupbuy.api.service;
 
 import java.util.Date;
 import java.util.Iterator;
 
 import javax.servlet.http.HttpServletRequest;
 
 import net.sf.json.JSONArray;
 import net.sf.json.JSONObject;
 
 import com.mongodb.BasicDBList;
 import com.mongodb.BasicDBObject;
 import com.orange.common.utils.DateUtil;
 import com.orange.groupbuy.constant.DBConstants;
 import com.orange.groupbuy.constant.ErrorCode;
 import com.orange.groupbuy.constant.ServiceConstant;
 import com.orange.groupbuy.dao.Product;
 import com.orange.groupbuy.manager.ProductManager;
 
 public class GetCommentsService extends CommonGroupBuyService {
 	String appId;
 	String productId;
 	
 	@Override
 	public String toString() {
 		return "GetCommentsService [appId=" + appId
 				+ ", productId=" + productId + "]";
 	}
 	
 	@Override
 	public void handleData() {	
 		Product product = ProductManager.findProductById(mongoClient, productId);
 		if (product == null){
 			resultCode = ErrorCode.ERROR_PRODUCT_NOT_FOUND;
 			return;
 		}
 		
 		JSONArray commentsArray = new JSONArray();
 		BasicDBList comments = product.getComments();
 		Iterator<Object> iterator = comments.iterator();
 		while (iterator.hasNext()) {
 			BasicDBObject comment = (BasicDBObject)iterator.next();
 			JSONObject commentObject = new JSONObject();
 			commentObject.put(ServiceConstant.PARA_NICKNAME, comment.getString(DBConstants.F_NICKNAME));
 			commentObject.put(ServiceConstant.PARA_COMMENT_CONTENT, comment.getString(DBConstants.F_COMMENT_CONTENT));
 			Date date = (Date)comment.get(DBConstants.F_CREATE_DATE);
 			commentObject.put(ServiceConstant.PARA_CREATE_DATE, date.getTime());
 			commentsArray.add(commentObject);
 		}
 		
 		resultData = commentsArray;
 	}
 	
 	@Override
 	public boolean setDataFromRequest(HttpServletRequest request) {
 		productId = request.getParameter(ServiceConstant.PARA_ID);
 		appId = request.getParameter(ServiceConstant.PARA_APPID);
 		return true;
 	}
 	
 	@Override
 	public boolean needSecurityCheck() {
 		return false;
 	}
 }
