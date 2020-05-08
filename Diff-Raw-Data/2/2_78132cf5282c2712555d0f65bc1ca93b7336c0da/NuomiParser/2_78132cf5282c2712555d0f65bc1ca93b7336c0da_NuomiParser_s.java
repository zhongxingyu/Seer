 package com.orange.groupbuy.parser;
 
 public class NuomiParser extends Tuan800Parser {
 	@Override
 	public String generateWapLoc(String loc, String imageURL) {
 		//loc: http://www.nuomi.com/beijing/jbmg.html
 		//wap: http://m.nuomi.com/?areaId=100010000&id=7530
 		//imageURL:��http://nuomi.xnimg.cn/upload/deal/2011/07/V_H/8232-2.jpg
 		String str = getIDFromWeb("com/", ".html", loc);
 		String strs[] = str.split("/");
 		String city = strs[0];
 		if(city.isEmpty() || city == null)
 			return null;
 		String wapURL = null;
 		String cityCode = "";
 		String id = "";
 		if(city.equals("beijing")){
 			cityCode = "100010000";
 		}
 		if(city.equals("shanghai")){
 			cityCode = "200010000";
 		}
 		if(city.equals("guangzhou")){
 			cityCode = "300110000";
 		}
 		if(city.equals("tianjin")){
 			cityCode = "500010000";
 		}
 		if(city.equals("wuhan")){
 			cityCode = "400010000";
 		}
 		if(city.equals("nanjing")){
 			cityCode = "700010000";
 		}
 		if(city.equals("hangzhou")){
 			cityCode = "600010000";
 		}
 		if(city.equals("chengdu")){
 			cityCode = "800010000";
 		}
 		if(city.equals("xian")){
 			cityCode = "1000010000";
 		}
 		if(city.equals("shenzhen")){
 			cityCode = "300210000";
 		}
 		if(cityCode.isEmpty())
 			return null;
 		// TODO
 		id = getIDFromWeb("V_[a-zA-Z]{1}/", ".jpg", imageURL);
 		if(id == null)
 			return null;
 		int index = id.indexOf("-");
 		if(index == -1)
 			return null;
 		id = id.substring(0, index);
 		wapURL = "http://m.nuomi.com/?areaId=".concat(cityCode).concat("&id=").concat(id);
 		return wapURL;
	}
 }
