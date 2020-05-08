 package com.orange.groupbuy.api.service;
 
 import javax.servlet.http.HttpServletRequest;
 
 import net.sf.json.JSONObject;
 
 import com.orange.common.utils.StringUtil;
 import com.orange.groupbuy.constant.ErrorCode;
 import com.orange.groupbuy.constant.ServiceConstant;
 import com.orange.groupbuy.dao.Product;
 import com.orange.groupbuy.manager.ProductManager;
 
 public class ActionOnProductService extends CommonGroupBuyService {
 
 	String appId;
 	String userId;
 	String productId;
 	String actionName;	
 	int    actionValue;
 
 	boolean hasLocation;
 	double longitude;
 	double latitude;
 	
 	
 	
 	@Override
 	public String toString() {
 		return "ActionOnProductService [actionName=" + actionName
 				+ ", actionValue=" + actionValue + ", appId=" + appId
 				+ ", hasLocation=" + hasLocation + ", latitude=" + latitude
 				+ ", longitude=" + longitude + ", productId=" + productId
 				+ ", userId=" + userId + "]";
 	}
 
 	@Override
 	public void handleData() {				
 		// write counter into product
 		ProductManager.incActionCounter(mongoClient, productId, actionName, actionValue);
 		Product product = ProductManager.findProductById(mongoClient, productId);
 		if (product == null){
 			resultCode = ErrorCode.ERROR_PRODUCT_NOT_FOUND;
 			return;
 		}
 		
 		int counterValue = product.getActionCounterValueByName(actionName);
 		JSONObject retJSON = new JSONObject();
 		retJSON.put(actionName, counterValue);
 		resultData = retJSON;
 	}
 
 	@Override
 	public boolean needSecurityCheck() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean setDataFromRequest(HttpServletRequest request) {
 		productId = request.getParameter(ServiceConstant.PARA_ID);
 		userId = request.getParameter(ServiceConstant.PARA_USERID);
 		actionName = request.getParameter(ServiceConstant.PARA_ACTION_NAME);	
 		String actionValueStr = request.getParameter(ServiceConstant.PARA_ACTION_VALUE);
 		appId = request.getParameter(ServiceConstant.PARA_APPID);
 		String latitudeStr = request.getParameter(ServiceConstant.PARA_LATITUDE);
 		String longitudeStr = request.getParameter(ServiceConstant.PARA_LONGITUDE);
 		
 		if (!check(productId, ErrorCode.ERROR_PARAMETER_PRODUCTID_EMPTY,
 				ErrorCode.ERROR_PARAMETER_PRODUCTID_NULL))
 			return false;
 
 		if (!check(userId, ErrorCode.ERROR_PARAMETER_USERID_EMPTY,
 				ErrorCode.ERROR_PARAMETER_USERID_NULL))
 			return false;
 
 		if (!check(actionName,
 				ErrorCode.ERROR_PARAMETER_ACTIONNAME_EMPTY,
 				ErrorCode.ERROR_PARAMETER_ACTIONNAME_NULL))
 			return false;
 
 		if (!check(appId, ErrorCode.ERROR_PARAMETER_APPID_EMPTY,
 				ErrorCode.ERROR_PARAMETER_APPID_NULL))
 			return false;
 		
 		if (actionValueStr != null && !actionValueStr.isEmpty()){
 			actionValue = Integer.parseInt(actionValueStr);
 		}
 		else{
 			actionValue = 1;
 		}
 		
 		if (!StringUtil.isEmpty(longitudeStr) && !StringUtil.isEmpty(latitudeStr)){
 			hasLocation = true;
 			longitude = Double.parseDouble(longitudeStr);
 			latitude = Double.parseDouble(latitudeStr);
 		}
 		else{
 			hasLocation = false;
 		}
 		
 		return true;				
 	}
 
 }
