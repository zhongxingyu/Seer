 package com.orange.groupbuy.parser;
 
 public class DianpingParser extends Hao123Parser {
 
 	@Override
 	public String generateWapLoc(String loc,String imageURL) {
 		
 		final String prefixWapURL = "http://m.t.dianping.com/deal/";
		final String suffixWapURL = "?cityid=*&agent=*&version=*&screen=*&token=*&tag=deal";
 		String id = getIDFromWeb("deal/", null, loc);
 		if(id == null)
 			return null;
 		else 
 			return prefixWapURL.concat(id).concat(suffixWapURL);
 	}
 }
