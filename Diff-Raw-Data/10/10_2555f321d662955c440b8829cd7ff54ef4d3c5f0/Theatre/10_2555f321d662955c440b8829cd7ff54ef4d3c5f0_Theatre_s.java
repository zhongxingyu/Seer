 package com.cinemar.phoneticket.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class Theatre {
 	String id;
 	String name;
 	int latitude;
 	int longitude;
 	String address;
 	String PhotoUrl;
 	List<Show> shows;
 
 	public Theatre(JSONObject cinema) throws JSONException {
 		this.id = cinema.getString("id");
 		this.name = cinema.getString("name");
 		this.latitude = cinema.getInt("latitude");
 		this.longitude = cinema.getInt("longitude");
 		this.address = cinema.getString("address");
		this.PhotoUrl = cinema.getString("id");
 		shows = new ArrayList<Show>();
 
 		if (cinema.has("shows")) {
 			JSONArray showsArray = cinema.getJSONArray("shows");
 
 			for (int j = 0; j < showsArray.length(); j++) {
 				JSONObject showObject = showsArray.optJSONObject(j);
 				shows.add(new Show(showObject));
 			}
 		}
 
 	}
 
 	public String getId() {
 		return id;
 	}
 
 	public void setId(String id) {
 		this.id = id;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public int getLatitude() {
 		return latitude;
 	}
 
 	public void setLatitude(int latitude) {
 		this.latitude = latitude;
 	}
 
 	public int getLongitude() {
 		return longitude;
 	}
 
 	public void setLongitude(int longitude) {
 		this.longitude = longitude;
 	}
 
 	public String getAddress() {
 		return address;
 	}
 
 	public void setAddress(String address) {
 		this.address = address;
 	}
 
 	public String getPhotoUrl() {
 		return PhotoUrl;
 	}
 
 	public void setPhotoUrl(String photoUrl) {
 		PhotoUrl = photoUrl;
 	}
 
 	public List<Show> getShows() {
 		return shows;
 	}
 
 	public void setShows(List<Show> shows) {
 		this.shows = shows;
 	}
 
 	public void addShow(Show show) {
 		this.addShow(show);
 	}
 
 }
