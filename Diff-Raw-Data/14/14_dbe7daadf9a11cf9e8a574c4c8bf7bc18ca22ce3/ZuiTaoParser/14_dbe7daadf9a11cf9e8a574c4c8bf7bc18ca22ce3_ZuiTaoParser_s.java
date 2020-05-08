 package com.orange.groupbuy.parser;
 
 
 import org.jdom.Element;
 
 import com.orange.groupbuy.constant.DBConstants;
 
 public class ZuiTaoParser extends Hao123Parser {
 	
 	@Override
 	Element getDataElement(Element productElement){
 		Element e = getFieldElement(productElement, "data");
 		if (e == null){
 			return getFieldElement(productElement, "data", "display");
 		}
 		else {
 			return e;
 		}
 	}
 	
 	@Override
 	String getStartTimeString(Element data){
 		String value = getFieldValue(data, "starttime");
 		if (value == null)
 			return getFieldValue(data, "startTime");
 		else
 			return value;
 	}
 
 	@Override
 	String getEndTimeString(Element data){
 		String value = getFieldValue(data, "endtime");
 		if (value == null)
 			return getFieldValue(data, "endTime");
 		else
 			return value;
 	}
 
 	@Override
 	String getImage(Element data){
 		String image = getFieldValue(data, "image");
 		if (image == null)
 			return null;
 		
		image = image.replaceAll("http://www.zuitao.com", "");
		image = image.replaceAll("http://img.zuitao.com", "");
 		return image;
 	}
 	
 	@Override
 	public boolean disableAddressParsing() {
 		return true;
 	}
 	
 	@Override
 	public int convertCategory(String category) {
 		return DBConstants.C_CATEGORY_COUPON;
 	}
 
 }
