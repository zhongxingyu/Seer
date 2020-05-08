 package me.deal.server;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import me.deal.client.servlets.DealService;
 import me.deal.shared.BusinessInfo;
 import me.deal.shared.Category;
 import me.deal.shared.Deal;
 import me.deal.shared.LatLngCoor;
 import me.deal.shared.json.JSONYipitDeals;
 import me.deal.shared.json.JSONYelp;
 import me.deal.shared.json.JSONYipitDeals.JSONYipitDeal;
 import me.deal.shared.json.JSONYipitDeals.JSONYipitDeal.JSONTag;
 import me.deal.shared.Location;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 
 /**
  * The server side implementation of the RPC service.
  */
 @SuppressWarnings("serial")
 public class DealServiceImpl extends RemoteServiceServlet implements
 		DealService {
 
 	final String apiKey = "tvw7u8QvGqetCNUf";
 	final String apiKeyYelp="ijeImBEdt2K2hS2Abub6FQ";
 	
 	/*
 	 *  Implemented using the Yipit API.  Returns deals relating to the tags within radius distance of
 	 *  the given coordinate.  Returns null if no deals found.
 	 */
 	public ArrayList<Deal> getYipitDeals(LatLngCoor coor, Double radius, Integer limit, ArrayList<Category> tags) {
 		/*
 		 * TODO: Implement this method.  Get the deals using the Yipit API and parse the result into
 		 * Deal java items, returning them to the user.  See http://yipit.com/about/api/documentation/
 		 * for reference.
 		 * Yipit API Key = tvw7u8QvGqetCNUf
 		 */
 		
 		String endPoint = "http://api.yipit.com/v1/deals/";
 		String requestParameters = generateParamterStr(coor, radius, limit, tags);
 		String response = HttpSender.sendGetRequest(endPoint, requestParameters);
 		response = formatResponse(response);
 		
 		Gson gson = new GsonBuilder().create();
 		JSONYipitDeals yipitDeals = gson.fromJson(response, JSONYipitDeals.class);
 		ArrayList<Deal> deals = convertYipitDeals(yipitDeals);
 		
 		return deals;
 	}
 	
 	private ArrayList<Deal> convertYipitDeals(JSONYipitDeals yipitDeals) {
 		
 		ArrayList<Deal> deals = new ArrayList<Deal>();
 		
 		for(JSONYipitDeal jsonDeal : yipitDeals.deals) {
 			
 			ArrayList<Category> tags = new ArrayList<Category>();
 			for(JSONTag tag : jsonDeal.tags) {
 				tags.add(Category.getCategory(tag.slug));
 			}
 			
 			LatLngCoor latLng = null;
 			if(jsonDeal.business.locations.get(0).lat != null &&
 				jsonDeal.business.locations.get(0).lon != null) {
 				latLng = new LatLngCoor(new Double(jsonDeal.business.locations.get(0).lat),
 						new Double(jsonDeal.business.locations.get(0).lon));
 			}
 			
			//Temporary fix for discount.
 			BusinessInfo dealBusinessInfo=lookupYelpByPhone(jsonDeal.business.locations.get(0).phone);
 			Deal deal = new Deal(
 					jsonDeal.id,
 					jsonDeal.date_added,
 					jsonDeal.end_date,
					jsonDeal.value.raw == null ? null : new Double(jsonDeal.value.raw),
 					jsonDeal.price.raw == null? null : new Double(jsonDeal.price.raw),
	//			    jsonDeal.discount.raw == null ? null : new Double(jsonDeal.discount.raw),
					jsonDeal.discount.raw == null ? null : new Double(0),
 					jsonDeal.title,
 					jsonDeal.yipit_title,
 					jsonDeal.yipit_url,
 					jsonDeal.mobile_url,
 					jsonDeal.images.image_big,
 					jsonDeal.images.image_small,
 					new Location(jsonDeal.business.locations.get(0).address, "",
 							jsonDeal.business.locations.get(0).state,
 							jsonDeal.business.locations.get(0).zip_code, latLng),
 					jsonDeal.business.locations.get(0).phone,
 					new Boolean(jsonDeal.source.paid != 0),
 					tags,
 					dealBusinessInfo);
 			
 			deals.add(deal);
 		}
 		return deals;
 	}
 	
 	private String formatResponse(String response) {
 		String preBegin = "\"response\": {        ";
 		String preEnd = "</pre>";
 		return "{ " + response.substring(response.indexOf(preBegin) + preBegin.length(),
 				response.indexOf(preEnd)-6) + " }";
 	}
 	
 	private String generateParamterStr(LatLngCoor coor, Double radius, Integer limit, ArrayList<Category> tags) {
 		String parameterStr = "";
 		parameterStr += "key=" + apiKey + "&";
 		parameterStr += coor.getLatitude().isNaN() ? "" : "lat=" + coor.getLatitude() + "&";
 		parameterStr += coor.getLongitude().isNaN() ? "" : "lon=" + coor.getLongitude() + "&";
 		parameterStr += radius.isNaN() ? "" : "radius=" + radius + "&";
 		parameterStr += limit.equals(null) ? "" : "limit=" + limit + "&";
 		parameterStr += "tag="+getCategoryParams(tags);
 		
 		while(parameterStr.endsWith("&"))
 			parameterStr.substring(0, parameterStr.length() - 2);
 		
 		return parameterStr;
 	}
 	// overloaded generateParamterStr for Yelp
 	private String generateParamterStr(String phoneNumber) 
 	{
 		String parameterStr = "";
 		parameterStr += "ywsid=" + apiKeyYelp + "&";
 		parameterStr += "phone="+phoneNumber;
 		return parameterStr;
 	}
 	
 	private String getCategoryParams(ArrayList<Category> tags) {
 		
 		String parameterStr = "";
 		for(Category tag : tags) {
 			parameterStr += tag.getSlug() + ",";
 		}
 		
 		while(parameterStr.endsWith(","))
 			parameterStr = parameterStr.substring(0, parameterStr.length() - 2);
 		
 		return parameterStr;
 	}
 	
 	public BusinessInfo lookupYelpByPhone(String phoneNumber) {
 		/*
 		 * TODO: Implement this method.  Given a phone number of the business use the Yelp API to
 		 * find information about that business and load that info into a BusinessInfo object that
 		 * is returned to the user.  See http://www.yelp.com/developers/documentation/phone_api
 		 * Yelp API Key = ijeImBEdt2K2hS2Abub6FQ
 		 */
 		
 		String endPoint = "http://api.yelp.com/phone_search";
 		String requestParameters = generateParamterStr(phoneNumber);
 		
 		Gson gson = new GsonBuilder().create();
 		String response = HttpSender.sendGetRequest(endPoint, requestParameters);
 		System.out.println(response);
 		//JSONYelp yelp = gson.fromJson(response, JSONYelp.class);
 		
 		
 		SearchResponse searchResponse = new Gson().fromJson(response, SearchResponse.class);
 		ArrayList<JSONYelp> yelp= searchResponse.businesses;//response = formatResponse(response);
 		// if there does not exists any record for the business on yelp 
 		if(yelp.size()==0)
 			return null;
 		
 		return convertYelp(yelp);
 	
 	}
 	private class SearchResponse {
 		public ArrayList<JSONYelp> businesses;
 	}
 	
 	private BusinessInfo convertYelp(ArrayList<JSONYelp> yelp) {
 		
 		BusinessInfo instance =new BusinessInfo();
 		Iterator<JSONYelp> i = yelp.iterator();
 		if(yelp==null)
 			return null;
 		JSONYelp oneRecord;
 		while(i.hasNext())
 		{
 			oneRecord= i.next();
 		
 			
 			instance.setAvgRating(Double.parseDouble(oneRecord.avg_rating));
 			instance.setAvgRatingImageUrl(oneRecord.rating_img_url_small);
 			instance.setName(oneRecord.name);
 			instance.setWebUrl(oneRecord.url);
 			instance.setMobileUrl(oneRecord.mobile_url);
 			instance.setYelpID(oneRecord.id);
 			instance.setNumReviews(Integer.parseInt(oneRecord.review_count));
 			instance.setImageUrl(oneRecord.photo_url_small);
 			
 			
 		}
 		
 		// for debugging
 		System.out.println("inside yelp");
 		System.out.println(instance.getImageUrl()+instance.getAvgRatingImageUrl()+instance.getMobileUrl()+instance.getName()+instance.getWebUrl()+instance.getYelpID()+instance.getAvgRating()+instance.getNumReviews());
 		
 		return instance;
 		
 		
 	}
 }
