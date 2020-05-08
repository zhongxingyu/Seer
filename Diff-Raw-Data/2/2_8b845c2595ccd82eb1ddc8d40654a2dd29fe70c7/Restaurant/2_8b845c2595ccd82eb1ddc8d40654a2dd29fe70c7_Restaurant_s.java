 package com.codepath.apps.nommable.models;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.activeandroid.Model;
 import com.activeandroid.annotation.Column;
 import com.activeandroid.annotation.Table;
 import com.activeandroid.query.Select;
 
 @Table (name = "Restaurant")
 public class Restaurant extends Model implements Serializable {
 
 	private static final long serialVersionUID = -7256102743331094491L;
 	
 	@Column(name = "name")
 	private String name;
 	@Column(name = "foursquare_id")
 	private String fourSquareId;
 	@Column(name="address")
 	private String address;
 	@Column(name = "latitute")
 	private double latitute;
 	@Column(name = "longitude")
 	private double longitude;
 	@Column(name = "city")
 	private String city;
 	@Column(name = "state")
 	private String state;
 	@Column(name = "zip")
 	private String zip;
 	@Column(name = "formattedphone")
 	private String formattedphone;
 	@Column(name = "image_url")
 	private String image_url;
 
 	/**
 	 * ActiveAndroid requires you to use the superclass constructor.
 	 */
 	public Restaurant() {
 		super();
 	}
 	public String getName() {
 		return name;
 	}
 	public String getFourSquareId() {
 		return fourSquareId;
 	}
 	public String getAddress() {
 		return address;
 	}	
 	public String getState() {
 		return state;
 	}
 	public String getCity() {
 		return city;
 	}
 	public String getDisplayPhone() {
 		return formattedphone;
 	}
 	public double getLatitude() {
 		return latitute;
 	}
 	public double getLongitude() {
 		return longitude;
 	}
 	public String getImageUrl() {
 		return image_url;
 	}
 	
 	public String getFullAddress() {
 		return address + ", " + city + ", " + state;
 	}
 	
 	/**
 	 * Maps JSON response to single a Restaurant object
 	 * @param jsonObject JSON response from FourSquare
 	 * @return Restaurant object
 	 */
 	
 	public static Restaurant fromJson(JSONObject jsonObject) {
 		Restaurant r = new Restaurant();
 		try {
 			
 			JSONObject venue = jsonObject.getJSONObject("venue");
 			JSONObject location = venue.getJSONObject("location");
 			JSONObject photo = venue.getJSONObject("photos").getJSONArray("groups").getJSONObject(0).getJSONArray("items").getJSONObject(0);
 			
 			r.fourSquareId = venue.getString("id");
 			r.name = venue.getString("name");
 			r.formattedphone = venue.getJSONObject("contact").getString("formattedPhone");
 			r.image_url = photo.getString("prefix") + "original" + photo.getString("suffix");
 			r.address = location.getString("address");
 			r.state = location.getString("state");
 			r.city = location.getString("city");
 			r.latitute = location.getDouble("lat");
 			r.longitude = location.getDouble("lng");
 			r.zip = location.getString("postalCode");
 			
 		} catch (JSONException e) {
 			e.printStackTrace();
 			return null;
 		}
 		return r;
 	}
 	/**
 	 * Maps array of restaurant json results into restaurant model objects
 	 * 
 	 * @param jsonArray array of venues from JSON Response
 	 * @return ArrayList of Restaurant objects
 	 */
 	public static ArrayList<Restaurant> fromJson(JSONArray jsonArray) {
 		ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>(jsonArray.length());
 		// Process each result in json array, decode and convert to restaurant object
 		for (int i=0; i < jsonArray.length(); i++) {
 			JSONObject json = null;
 			try {
 				json = jsonArray.getJSONObject(i);
 			} catch (Exception e) {
 				e.printStackTrace();
 				continue;
 			}
 
 			Restaurant restaurant = Restaurant.fromJson(json);
 			if (restaurant != null) {
 				restaurants.add(restaurant);
 			}
 		}
 		return restaurants;
 	}
 	
 	/**
 	 * Return saved restaurants, ordered by most recent
 	 */
 	public static ArrayList<Restaurant> getFavorites() {
		return new Select().from(Restaurant.class).orderBy("mId DESC").execute();
 	}
 }
