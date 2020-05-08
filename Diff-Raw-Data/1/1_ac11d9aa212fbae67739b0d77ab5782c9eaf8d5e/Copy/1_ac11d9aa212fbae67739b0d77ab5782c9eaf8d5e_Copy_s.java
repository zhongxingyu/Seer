 package com.emjaay.mdb.data;
 
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class Copy {
 	
 	private static final String API_IMDB_ID = "imdbID";
 	private static final String API_MEDIA_TYPE = "mediaType";
 	private static final String API_ADDED = "added";
 	
 	public static final int TYPE_VHS = 1;
 	public static final int TYPE_DVD = 2;
 	public static final int TYPE_BLURAY = 3;
 	public static final int TYPE_DIGITAL = 4;
 	
 	private int id;
 	private String imdbId;
 	private int mediaType;
 	private long added;
 	private int synced;
 
 	public void setId(int id){
 		this.id = id;
 	}
 	
 	public int getId(){
 		return this.id;
 	}
 
 	public void setImdbId(String imdbId){
 		this.imdbId = imdbId;
 	}
 	
 	public String getImdbId(){
 		return this.imdbId;
 	}
 	
 	public void setMediaType(int mediaType){
 		this.mediaType = mediaType;
 	}
 	
 	public int getMediaType(){
 		return this.mediaType;
 	}
 	
 	public void setAdded(long added) {
 		this.added = added;
 	}
 	
 	public long getAdded() {
 		return added;
 	}
 	
 	public void setSynced(boolean synced) {
 		this.synced = synced ? 1 : 0;
 	}
 	
 	public void setSynced(int synced) {
 		this.synced = synced;
 	}
 	
 	public boolean getSynced() {
 		return synced == 1;
 	}
 	
 	public static Copy fromJson(JSONObject jo){
 		Copy copy = new Copy();
 		try{
 			if (jo.has(API_IMDB_ID)){
 				copy.setImdbId(jo.getString(API_IMDB_ID));
 			}
 			if (jo.has(API_MEDIA_TYPE)){
 				copy.setMediaType(jo.getInt(API_MEDIA_TYPE));
 			}
 			if (jo.has(API_ADDED)){
 				copy.setAdded(jo.getLong(API_ADDED));
 			}
 		} catch (JSONException e){
 			e.printStackTrace();
 		}
 		
 		return copy;
 	}
 	
 	public static ArrayList<Copy> fromJson(JSONArray ja){
 		ArrayList<Copy> copies = new ArrayList<Copy>();
 		try{
 			for(int i=0; i<ja.length(); i++){
 				JSONObject jo = ja.getJSONObject(i);
 				copies.add(fromJson(jo));
 			}
 		}catch (JSONException e){
 			e.printStackTrace();
 		}
 		return copies;
 	}
 	
 }
