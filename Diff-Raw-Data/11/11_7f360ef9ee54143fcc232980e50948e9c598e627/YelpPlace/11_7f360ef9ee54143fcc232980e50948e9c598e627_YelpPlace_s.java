 package com.where.place;
 
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.where.atlas.feed.yellowpages.YPRawDataParser;
 import com.where.atlas.feed.yelp.olddata.YelpParserUtils.InnerRating;
 import com.where.atlas.feed.yelp.olddata.YelpParserUtils.Listing;
 
 public class YelpPlace extends Place
 {
 	String yelpName;
 	ArrayList<InnerRating> ratings;
 	boolean phoneMatch;
 
 	//new stuff (March 28)
 	String avg_rating;
 	ArrayList<JSONObject> reviews;
 	String biz_url;
 	String price;   //ex: "$$$"
 	ArrayList<String> categories;
 	String type;   //ex: restaurant
 	String parking;
 	String wheelchair;
 	String yelp_url;
 
 	
 	public YelpPlace()
 	{
 		setSource(Source.YELP);    
 		phoneMatch=false;
 		ratings = new ArrayList<InnerRating>();
 		reviews = new ArrayList<JSONObject>();
 		categories = new ArrayList<String>();
 	}
 
 	public String getYelpName()
 	{
 		return yelpName;
 	}
 	public void setYelpName(String name)
 	{
 		yelpName = name;
 	}
 	public void setPhoneMatch(boolean bool)
 	{
 		phoneMatch=bool;
 	}
 	public boolean getPhoneMatch()
 	{
 		return phoneMatch;
 	}
 	public void setRatings(ArrayList<InnerRating> Yratings)
 	{
 		ratings = Yratings;
 	}
 	public ArrayList<InnerRating> getRatings()
 	{
 		return ratings;
 	}
 
 	public String toString()
 	{
 		return "Yelp Place:Name: " + getYelpName() + "\nCS Name: " + getName() 
 		+ "\ncsid: "+getNativeId()+"  Phone: "+getPhone() + "\nRatings:\t" + getRatings();
 	}
 
 	public Listing toListing()
 	{
 		Listing listing = new Listing();
 		Address address = getAddress();
 		double[] latlng = getLatlng();
 
 
 		listing.name_=getYelpName();
 		listing.csId_ = Long.parseLong(getNativeId());
 		listing.csName_ = getName();
 		listing.phoneMatch_ = getPhoneMatch();
 		listing.lat_ = latlng[0];
 		listing.lng_ = latlng[1];
 		listing.geoHash_ = getGeohash();
 		listing.street_ = address.getAddress1();
 		listing.city_ = address.getCity();
 		listing.state_ = address.getState();
 		listing.zip_ = Integer.parseInt(address.getZip());
 		listing.phone_ = getPhone();
 		listing.ratings_ = getRatings();
 
 		return listing;
 	}
 
 	public String getAvg_rating() {
 		return avg_rating;
 	}
 
 	public void setAvg_rating(String avg_rating) {
 		this.avg_rating = avg_rating;
 	}
 
 	public ArrayList<JSONObject> getReviews() {
 		return reviews;
 	}
 
 	public void addReview(JSONObject rev) throws JSONException
 	{
 		reviews.add(new JSONObject(YPRawDataParser.cleanReview(rev.toString())));
 	}
 
 	public String getBiz_url() {
 		return biz_url;
 	}
 
 	public void setBiz_url(String biz_url) {
 		this.biz_url = biz_url;
 	}
 
 	public String getPrice() {
 		return price;
 	}
 
 	public void setPrice(String price) {
 		this.price = price;
 	}
 
 	public ArrayList<String> getCategories() {
 		return categories;
 	}
 
 	public void addCategory(String cat)
 	{
 		categories.add(cat);
 	}
 
 	public String getType() {
 		return type;
 	}
 
 	public void setType(String type) {
 		this.type = type;
 	}
 
 	public String getParking() {
 		return parking;
 	}
 
 	public void setParking(String parking) {
 		this.parking = parking;
 	}
 
 	public String getWheelchair() {
 		return wheelchair;
 	}
 
 	public void setWheelchair(String wheelchair) {
 		this.wheelchair = wheelchair;
 	}
 
 	public String getYelp_url() {
 		return yelp_url;
 	}
 
 	public void setYelp_url(String yelp_url) {
 		this.yelp_url = yelp_url;
 	}
 
 
 	public JSONObject toJSON(){
 		return toJSON(false);
 	}
 
 	public JSONObject toJSON(boolean useStreet1) {
 		JSONObject json = new JSONObject();
 		try{
 			json.put("source", "yelp");
 			
 			if(categories.size() > 0)
 			{
 				JSONArray jarray = new JSONArray();
 				for(String str:categories)
 				{
 					jarray.put(str);
 				}
 				json.put("categories", jarray);
 			}
 			if(getNativeId() != null && getNativeId().length() > 0)
 				json.put("place_hash", getNativeId());
 			if(getName() != null)
 				json.put("name", getName());
 

 			if(getBiz_url() != null && getBiz_url().length() > 0)
 				json.put("biz_url", getBiz_url());
 			if(getPrice() != null && getPrice().length() > 0)
 				json.put("price", getPrice());
 			if(getType() != null && getType().length() > 0)
 				json.put("type", getType());
 			if(getParking() != null && getParking().length() > 0)
 				json.put("parking", getParking());
 			if(getWheelchair() != null && getWheelchair().length() > 0)
 				json.put("wheelchair", getWheelchair());
 
 
 			if(getAvg_rating() != null && getAvg_rating().length() > 0)
 				json.put("avg_rating", getAvg_rating());
 
 			if(getWhereId() != null)
 				json.put("whereid", getWhereId());
 			if(getAddress() != null)
 			{
 				Address addy = getAddress();
 				if(useStreet1){
 					json.put("location", addy.toJSON(true));
 				}else{
 					json.put("location", addy.toJSON());
 				}
 			}
 
 			if(getLatlng() != null){
 				json.put("lat", getLatlng()[0]);
 				json.put("long", getLatlng()[1]);
 			}
 
 
 			if(yelp_url != null)
 				json.put("ypurl",yelp_url);
 
 
 			if(reviews.size() > 0)
 			{
 				JSONArray jarray = new JSONArray();
 				for(JSONObject jobj:reviews)
 				{
 					jarray.put(jobj);
 				}
 				json.put("reviews", jarray);
 			}
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 		return json;
 	}
 
 }
