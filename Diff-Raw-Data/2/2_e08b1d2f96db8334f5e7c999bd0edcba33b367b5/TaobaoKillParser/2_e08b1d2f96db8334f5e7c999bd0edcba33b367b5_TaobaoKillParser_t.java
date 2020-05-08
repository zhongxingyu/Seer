 package com.orange.groupbuy.parser;
 
 import java.util.Date;
 import java.util.concurrent.ConcurrentHashMap;
 
 import net.sf.json.JSONArray;
 import net.sf.json.JSONObject;
 
 import org.jdom.Element;
 import org.jsoup.Jsoup;
 
 import com.mongodb.DBObject;
 import com.orange.common.solr.SolrClient;
 import com.orange.common.utils.DateUtil;
 import com.orange.common.utils.StringUtil;
 import com.orange.groupbuy.addressparser.CommonAddressParser;
 import com.orange.groupbuy.constant.DBConstants;
 import com.orange.groupbuy.dao.Product;
 import com.orange.groupbuy.manager.ProductManager;
 import com.taobao.api.ApiException;
 import com.taobao.api.DefaultTaobaoClient;
 import com.taobao.api.TaobaoClient;
 import com.taobao.api.request.ItemsSearchRequest;
 import com.taobao.api.request.SellercatsListGetRequest;
 import com.taobao.api.request.ShopGetRequest;
 import com.taobao.api.request.UserGetRequest;
 import com.taobao.api.response.ItemsSearchResponse;
 import com.taobao.api.response.SellercatsListGetResponse;
 import com.taobao.api.response.ShopGetResponse;
 import com.taobao.api.response.UserGetResponse;
 
 public class TaobaoKillParser extends CommonGroupBuyParser {
 
 	final static String url = "http://gw.api.taobao.com/router/rest";
 	
 	final static String TAOBAO_MIAOSHA_APPKEY = "12426200";
 	final static String TAOBAO_MIAOSHA_SECRET = "a673eb7d8a117ea5f24ce60d42fdf972";
 	
 	final static String TAOBAO_ZHEKOU_APPKEY = "12428257";
 	final static String TAOBAO_ZHEKOU_SECRET = "7f2ce34d3e564b1cf257d07ba751faae";
 	
 	final String basicWapSite = "http://a.m.taobao.com/i";
 	final String basicWebSite = "http://item.taobao.com/item.htm?id=";
 	final String SITE_NAME_TAOBAO = "http://www.taobao.com";
 	final String SITE_URL = "http://www.taobao.com";	
 		
 	TaobaoClient client; //= new DefaultTaobaoClient(url, appkey, secret);
 	
 	static final ConcurrentHashMap<String, JSONObject> taobaoShopMap = new ConcurrentHashMap<String, JSONObject>();
 	
 	public static TaobaoClient getTaobaoMiaoshaConfig(){
 		return new DefaultTaobaoClient(url, TAOBAO_MIAOSHA_APPKEY, TAOBAO_MIAOSHA_SECRET);
 	}
 	
 	public static TaobaoClient getTaobaoZhekouConfig(){
 		return new DefaultTaobaoClient(url, TAOBAO_ZHEKOU_APPKEY, TAOBAO_ZHEKOU_SECRET);		
 	}
 
 	public TaobaoKillParser(TaobaoClient client){
 		this.client = client;
 	}
 	
 	@Override
 	public int convertCategory(String category) {
 		return 0;
 	}
 
 	@Override
 	public boolean disableAddressParsing() {
 		return true;
 	}
 
 	@Override
 	public String generateWapLoc(String webURL, String imageURL) {
 		return null;
 	}
 
 	@Override
 	public boolean parseElement(Element root, CommonAddressParser addressParser) {
 		return false;
 	}
 
 	public static String TAOBAO_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
 	public static Date parseTaobaoDate(String dateString){				
 		return DateUtil.dateFromStringByFormat(dateString, TAOBAO_DATE_FORMAT, DateUtil.CHINA_TIMEZONE);
 	}
 	
 	public boolean userHasShop(String userNick) {
 		UserGetRequest req = new UserGetRequest();
 		req.setFields("has_shop");
 		req.setNick(userNick);
 		UserGetResponse response;
 		try {
 			response = client.execute(req , null);
 			org.jsoup.nodes.Document doc = Jsoup.parse(response.getBody());
 			String content = doc.text();
 			JSONObject object = JSONObject.fromObject(content);
 			object = object.getJSONObject("user_get_response");
 			log.debug("get taobao user = "+object.toString());
 			if (object != null && object.containsKey("user")){					
 				JSONObject userInfo = object.getJSONObject("user");
 				boolean hasShop = userInfo.getBoolean("has_shop");
 				return hasShop;
 			}
 		} 
 		catch (ApiException e) {
 			log.error("userHasShop but catch exception = "+e.toString(), e);
 		}
 		
 		return false;
 	}
 	
 	public JSONObject getTaobaoShop(String shopNick){
 		
 		if (shopNick == null || shopNick.trim().length() == 0){
 			log.warn("getTaobaoShop but shop nickname is null or empty");
 			return null;
 		}
 		
 		JSONObject shopInfo = null;
 		if (taobaoShopMap.containsKey(shopNick)){
 			shopInfo= taobaoShopMap.get(shopNick);
 		}
 		else{
 			if (userHasShop(shopNick) == false){
 				log.warn("user " + shopNick + " found but has no shop");
 				return null;
 			}
 			
 			try {
 				log.info("getTaobaoShop, nick="+shopNick);
 				ShopGetRequest req=new ShopGetRequest();
 				req.setNick(shopNick);
 				req.setFields("sid,cid,title,nick,created,modified");
 				ShopGetResponse response = client.execute(req);
 				
 				org.jsoup.nodes.Document doc = Jsoup.parse(response.getBody());
 				String content = doc.text();
 				JSONObject object = JSONObject.fromObject(content);
 				object = object.getJSONObject("shop_get_response");
 				if (object != null && object.containsKey("shop")){					
 					shopInfo = object.getJSONObject("shop");
 				}
 				
 				if (shopInfo != null){
 					taobaoShopMap.put(shopNick, shopInfo);
 				}
 				
 			} catch (ApiException e) {
 				log.error("getTaobaoShopWapUrl but catch exception = "+e.toString(), e);
 			}
 			
 		}			
 		
 		return shopInfo;
 	}
 	
 	public String getTaobaoShopURL(JSONObject shopObj){
 		if (shopObj == null)
 			return null;
 		else
 			return "http://shop".concat(shopObj.getString("sid")).concat(".taobao.com");
 	}
 
 	public String getTaobaoShopWAPURL(JSONObject shopObj){
 		if (shopObj == null)
 			return null;
 		else
 			return "http://shop".concat(shopObj.getString("sid")).concat(".m.taobao.com");
 	}
 
 	public String getTaobaoShopTitle(JSONObject shopObj){
 		if (shopObj == null)
 			return null;
 		else
 			return shopObj.getString("title");
 	}
 
 	
 	@Override
 	public boolean parse(DBObject task){
 			
 		String query = (String)task.get(DBConstants.F_TAOBAO_QUERY);
 		int category = ((Double)task.get(DBConstants.F_TAOBAO_CATEGORY)).intValue();		
 		
 		log.info("Start parsing taobao products site, query = "+query+", category = "+category);		
 		
 		ItemsSearchRequest req = new ItemsSearchRequest();
 		req.setFields("num_iid,title,nick,pic_url,cid,price,type,list_time,delist_time,post_fee,score,volume");
 		req.setQ(query);
 		req.setOrderBy("popularity:desc");
 		req.setPageSize(200L);
 		try {
 			log.info("[SEND] taobao request, req = "+req.toString());
 			ItemsSearchResponse response = client.execute(req);
 			
 			// delete the html tag
 			org.jsoup.nodes.Document doc = Jsoup.parse(response.getBody());
 			String content = doc.text();
 			log.debug("[RECV] taobao response = "+content);
			content = content.replaceAll("<\\\\/span>", "");
 			// add product site
 			JSONObject object = JSONObject.fromObject(content);
 			object = object.getJSONObject("items_search_response");
 			if (object == null || !object.containsKey("item_search")){
 				log.info("search taobao product but no result found");
 				return true;
 			}			
 
 			object = object.getJSONObject("item_search");
 			if (object == null || !object.containsKey("items")){
 				log.info("search taobao product but no result found");
 				return true;
 			}
 			
 			object = object.getJSONObject("items");
 			if (object == null || !object.containsKey("item")){
 				log.info("search taobao product but no result found");
 				return true;
 			}
 			
 			JSONArray taobaoItems = object.getJSONArray("item");
 			for (int i=0; i<taobaoItems.size(); i++) {
 				JSONObject taobaoItem = (JSONObject) taobaoItems.get(i);
 				Object id = taobaoItem.get("num_iid");
 				String image = taobaoItem.getString("pic_url");
 				String title = taobaoItem.getString("title");
 
 				// set Web & WAP URL
 				String wapURL = basicWapSite.concat(""+id).concat(".htm");
 				String webURL = basicWebSite.concat(""+id);
 
 				// set start date & end date
 				String startDateString = taobaoItem.getString("list_time");
 				String endDateString = taobaoItem.getString("delist_time");				
 				Date startDate = parseTaobaoDate(startDateString);
 				Date endDate = parseTaobaoDate(endDateString);
 				
 				// set price & bought info
 				Double price = taobaoItem.getDouble("price");
 				Double value = taobaoItem.getDouble("price");
 				int bought = taobaoItem.getInt("volume");
 								
 				Product product = null;
 				product = ProductManager.findProduct(mongoClient, webURL, DBConstants.C_NATIONWIDE);
 				if (product == null){
 					
 					// set shop information here to reduce some traffic on invoking taobao shop API
 					String nick = taobaoItem.getString("nick");
 					JSONObject shopObject = getTaobaoShop(nick);
 					String siteName = getTaobaoShopTitle(shopObject);
 					String siteURL = getTaobaoShopWAPURL(shopObject);	
 					
 					if (StringUtil.isEmpty(siteName) || StringUtil.isEmpty(siteURL)){
 						log.info("fail to create taobao product due to site name/URL empty, nick = "+nick);
 						continue;						
 					}
 
 					boolean result = true;
 					product = new Product();
 					result = product.setMandantoryFields(DBConstants.C_NATIONWIDE, webURL, image, title, startDate, endDate, 
 							price, value, bought, siteId, siteName, siteURL);
 					product.setWapLoc(wapURL);
 					product.setCategory(category);
 					product.setProductType(DBConstants.C_PRODUCT_TYPE_TAOBAO);
 						
 					if (!result){
 						log.info("fail to create taobao product on setMandantoryFields, product = "+product.toString());
 						continue;
 					}
 
 					log.info("Create taobao product = " + product.toString());
 					result = ProductManager.createProduct(mongoClient, product);
 					if (!result){
 						log.info("fail to create taobao product on final creation, product = "+product.toString());
 						continue;
 					}
 					
 					ProductManager.createSolrIndex(product, true);
 				}
 				else{
 					// update product if it's changed
 					boolean hasChange = false;
 					if (price != product.getPrice()){
 						product.setPrice(price);
 						product.setValue(value);
 						hasChange = true;
 					}
 					if (bought != product.getBought()){
 						product.setBought(bought);
 						product.calcAndSetTopScore(bought, startDate);
 						hasChange = true;
 					}
 					if (hasChange){
 						log.debug("product "+product.getTitle()+" change, update DB");
 						ProductManager.save(mongoClient, product);
 					}
 					else{
 						log.debug("product "+product.getTitle()+" found, no change");
 					}
 				}
 			}		
 			
 			SolrClient.commit();
 			
 		} catch (ApiException e) {
 			log.error("execute taobao kill parser, but catch taobao API exception = " + e.toString(), e);
 		} catch (Exception e) {
 			log.error("execute taobao kill parser, but catch exception = " + e.toString(), e);			
 		}
 		
 		log.info("Finish parsing all taobao products site, query = "+query+", category = "+category);		
 		return true;
 	}
 }
