 package com.orange.groupbuy.api.service;
 
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 
 import com.orange.common.utils.StringUtil;
 import com.orange.groupbuy.constant.ErrorCode;
 import com.orange.groupbuy.constant.ServiceConstant;
 import com.orange.groupbuy.dao.Product;
 import com.orange.groupbuy.manager.ProductManager;
 
 public class FindProductByTopScoreService extends CommonGroupBuyService {
 
 	String appId;
 	String city;
 	int maxCount = 25;			
 	int startOffset = 0;
 	int startPrice = -100;
 	int endPrice = 10;
 	int category = -1;
 	
 	@Override
 	public String toString() {
 		return "FindProductByTopScoreService [appId=" + appId + ", city="
 				+ city + ", maxCount=" + maxCount + ", startOffset="
 				+ startOffset + ", startPrice=" + startPrice + ", endPrice="
 				+ endPrice + ", category=" + category + "]";
 	}
 
 	@Override
 	public boolean setDataFromRequest(HttpServletRequest request) {
 		appId = request.getParameter(ServiceConstant.PARA_APPID);
 		if (!check(appId, ErrorCode.ERROR_PARAMETER_APPID_EMPTY, ErrorCode.ERROR_PARAMETER_APPID_NULL)) {
 			return false;
 		}
 		String startOffsetStr = request.getParameter(ServiceConstant.PRAR_START_OFFSET);
 		if (!StringUtil.isEmpty(startOffsetStr)){
 			startOffset = Integer.parseInt(startOffsetStr);
 		}		
 
 		String maxCountStr = request.getParameter(ServiceConstant.PARA_MAX_COUNT);
 		if (!StringUtil.isEmpty(maxCountStr)){
 			maxCount = Integer.parseInt(maxCountStr);
 		}	
 	    city= request.getParameter(ServiceConstant.PARA_CITY);
 		
 	    String startPriceStr = request.getParameter(ServiceConstant.PARA_START_PRICE);
 		if (!StringUtil.isEmpty(startPriceStr)){
			startPrice = Integer.parseInt(startPriceStr);
 		}
 		String endPriceStr = request.getParameter(ServiceConstant.PARA_END_PRICE);
 		if (!StringUtil.isEmpty(endPriceStr)){
			endPrice = Integer.parseInt(endPriceStr);
 		}	
 		String categoryStr = request.getParameter(ServiceConstant.PARA_CATEGORIES);
 		if (!StringUtil.isEmpty(categoryStr)){
 			category = Integer.parseInt(categoryStr);
 		}	
 		
 		return true;
 	}
 
 	@Override
 	public boolean needSecurityCheck() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public void handleData() {
 		// TODO Auto-generated method stub
 		List<Product> productList = ProductManager.getTopScoreProducts(mongoClient, city, 
 				category, startOffset, maxCount, startPrice, endPrice);
 		
 		for (Product p : productList){
 			log.info("product id="+p.getId()+
 					", top score="+p.getTopScore()+
 					", title="+p.getTitle());
 		}
 		
 		resultData = CommonServiceUtils.productListToJSONArray(productList);	
 	}
 
 }
