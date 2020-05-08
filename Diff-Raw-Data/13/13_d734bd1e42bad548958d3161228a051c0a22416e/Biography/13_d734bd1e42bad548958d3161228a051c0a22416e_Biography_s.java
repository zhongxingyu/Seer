 package common;
 
 import java.io.Serializable;
 import java.util.Date;
 
 import org.json.simple.JSONObject;
 
 public class Biography implements Serializable {
 	private static final long serialVersionUID = 2767462670848472356L;
 	public long id;
 	public String name;
 	public String screenName;
 	public String location;
 	public String description;
 	public String profileImageUrl;
 	public String websiteUrl;
 	
 	public long followersCount;
 	public long friendCount;
 	public long favoritesCount;
 	public long statusCount;
 	
 	public String profileBackgroundColor;
 	public String profileTextColor;
 	public String profileLinkColor;
 	public String profileSidebarFillColor;
 	public String profileSidebarBorderColor;
 	public String profileBackgroundImageUrl;
 	public Boolean profileBackgroundTile;
 	public Boolean verified;
 	public Boolean notifications;
 	
 	public Date createdAt;
 	public long utcOffset;
 	public String timeZone;
 	
 	public Date crawledAt; // Set at crawl time
 	
 	public JSONObject toJSON() {
 		JSONObject object = new JSONObject();
 		object.put("id", id);
 		object.put("name", name);
 		object.put("screenName", screenName);
 		object.put("location", location);
 		object.put("description", description);
 		object.put("profileImageUrl", profileImageUrl);
 		object.put("websiteUrl", websiteUrl);
 		object.put("followersCount", followersCount);
 		object.put("friendCount", friendCount);
 		object.put("favoritesCount", favoritesCount);
 		object.put("statusCount", statusCount);
 		object.put("profileBackgroundColor", profileBackgroundColor);
 		object.put("profileTextColor", profileTextColor);
 		object.put("profileLinkColor", profileLinkColor);
 		object.put("profileSidebarFillColor", profileSidebarFillColor);
 		object.put("profileBackgroundImageUrl", profileBackgroundImageUrl);
 		object.put("profileBackgroundTile", profileBackgroundTile);
 		object.put("verified", verified);
 		object.put("notifications", notifications);
 		object.put("createdAt", createdAt.getTime());
 		object.put("utcOffset", utcOffset);
 		object.put("timeZone", timeZone);
 		return object;
 	}
 }
