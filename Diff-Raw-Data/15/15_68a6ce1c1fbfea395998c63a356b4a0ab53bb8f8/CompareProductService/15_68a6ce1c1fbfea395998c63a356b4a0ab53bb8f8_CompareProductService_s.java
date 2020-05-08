 package com.orange.groupbuy.api.service.product;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.jsoup.Jsoup;
 
 import net.sf.json.JSONArray;
 import net.sf.json.JSONObject;
 
 import com.orange.groupbuy.api.service.CommonGroupBuyService;
 import com.orange.groupbuy.constant.ErrorCode;
 import com.orange.groupbuy.constant.ServiceConstant;
 import com.taobao.api.ApiException;
 import com.taobao.api.DefaultTaobaoClient;
 import com.taobao.api.TaobaoClient;
 import com.taobao.api.request.ItemsSearchRequest;
 import com.taobao.api.response.ItemsSearchResponse;
 
 public class CompareProductService extends CommonGroupBuyService {
 	
 	String keywords;
 
 	@Override
 	public String toString() {
 		return "CompareProductService [keywords=" + keywords + "]";
 	}
 
 	@Override
 	public boolean setDataFromRequest(HttpServletRequest request) {
 		keywords = request.getParameter(ServiceConstant.PARA_KEYWORDS);
 		if (!check(keywords, ErrorCode.ERROR_PARAMETER_COMPAREWORD_EMPTY, ErrorCode.ERROR_PARAMETER_COMPAREWORD_NULL)) {
 			return false;
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
 		try{
 			final String url = "http://gw.api.taobao.com/router/rest";
 			final String appkey = "12362612";
 			final String secret = "918cf5f4f2d5d387a622da0257821bd0";
 			final String basicSite = "http://a.m.taobao.com/i";
 			
 			TaobaoClient client = new DefaultTaobaoClient(url, appkey, secret);
 			ItemsSearchRequest req = new ItemsSearchRequest();
 			req.setFields("num_iid,title,nick,pic_url,price,post_fee");
 			req.setQ(keywords);
 			req.setOrderBy("popularity:desc");
 			ItemsSearchResponse response = client.execute(req);
 			
 			// delete the html tag
 			org.jsoup.nodes.Document doc = Jsoup.parse(response.getBody());
 			String content = doc.text();
 			content = content.replaceAll("<\\\\/span>", "");
 			// add product site
 			JSONObject object = JSONObject.fromObject(content);
 			object = object.getJSONObject("items_search_response");
 			if (object == null || !object.containsKey("item_search")){
 				log.info("search taobao product but no result found");
 				return;
 			}
 
 			object = object.getJSONObject("item_search");
 			if (object == null || !object.containsKey("items")){
 				log.info("search taobao product but no result found");
 				return;
 			}
 			
 			object = object.getJSONObject("items");
 			if (object == null || !object.containsKey("item")){
 				log.info("search taobao product but no result found");
 				return;
 			}
 			
 			JSONArray taobaoItems = object.getJSONArray("item");
 			for (int i=0; i<taobaoItems.size(); i++) {
 				JSONObject taobaoItem = (JSONObject) taobaoItems.get(i);
				Long id = (Long) taobaoItem.get("num_iid");
 				String site = basicSite.concat(""+id).concat(".htm");
 				taobaoItem.accumulate("product_site", site);
 				taobaoItems.set(i, taobaoItem);
 			}
 			// add to response result
 			resultData = taobaoItems;	
 			
 		}
 		catch (ApiException e) {
 			log.error("<CompareProductService> catch exception = " + e.toString(), e);
 		}				
 	}
 
 }
