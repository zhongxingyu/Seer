 package org.hubiquitus.hubotsdk.adapters.HtwitterAdapter;
 
 /*
  * Copyright (c) Novedia Group 2012.
  *
  *     This file is part of Hubiquitus.
  *
  *     Hubiquitus is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  *
  *     Hubiquitus is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  *
  *     You should have received a copy of the GNU General Public License
  *     along with Hubiquitus.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 
 /**
  * 
  */
 
 import java.net.URL;
 
 import org.joda.time.DateTime;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 /**
  * @version 0.5
  * Describes a twitter author
  */
 public class HTweetAuthor extends JSONObject {
 
 	final Logger log = LoggerFactory.getLogger(HTweetAuthor.class);
 
 	public HTweetAuthor() {
 		super();
 	};
 
 	public HTweetAuthor(JSONObject jsonObj) throws JSONException{
 		super(jsonObj, JSONObject.getNames(jsonObj));
 	}
 
 	
 	/* Getters & Setters */
 	
 	/**
 	 * @return The twitter name of the author. 
 	 */
 	public String getScrName() {
 		String scrName;
 		try {
 			scrName = this.getString("scrName");
 		} catch (Exception e) {
 			scrName = null;			
 		}
 		return scrName;
 	}
 
 	/**
 	 * Set the twitter name of the author.
 	 * @param scrName
 	 */
 	public void setScrName(String scrName) {
 		try {
			if(scrName == null & scrName.length()== 0) {
 				log.error("message: scrName attibute is mandatoty;");
 				return;
 			} else {
 				this.put("scrName", scrName);
 			}
 		} catch (JSONException e) {
 			log.error("Can't update the scrName attribut",e);
 		}
 	}
 	
 	
 	/**
 	 * @return The number of tweet the author has sent. 0 if not provided.
 	 */
 	public int getStatus() {
 		int status;
 		try {
 			status = this.getInt("status");
 		} catch (Exception e) {
 			status = 0;			
 		}
 		return status;
 	}
 	
 	/**
 	 * Set the number of tweet the author has sent. Not provided if 0.
 	 * @param status
 	 */
 	public void setStatus(int status) {
 		try {
 			if(status == 0) {
 				this.remove("status");
 			} else {
 				this.put("status", status);
 			}
 		} catch (JSONException e) {
 			log.error("Can't update the status attribut",e);
 		}
 	}
 	
 	/**
 	 * @return The number of followers of the author. 0 if not provided.
 	 */
 	public int getFollowers() {
 		int followers;
 		try {
 			followers = this.getInt("followers");
 		} catch (Exception e) {
 			followers = 0;		
 		}
 		return followers;
 	}
 
 	/**
 	 * Set the number of followers of the author. Not provided if 0.
 	 * @param followers
 	 */
 	public void setFollowers(int followers) {
 		try {
 			if(followers== 0) {
 				this.remove("followers");
 			} else {
 				this.put("followers", followers);
 			}
 		} catch (JSONException e) {
 			log.error("Can't update the followers attribut",e);
 		}
 
 
 	}
 	
 	
 	/** 
 	 * @return The name of the author. Null if not provided.
 	 */
 	public String getName() {
 		String name;
 		try {
 			name = this.getString("name");
 		} catch (Exception e) {
 			name = null;			
 		}
 		return name;
 	}
 
 	/**
 	 * Set the name of the author. Not provided if empty
 	 * @param name
 	 */
 	public void setName(String name) {
 		try {
			if(name == null & name.length()== 0) {
 				this.remove("name");
 			} else {
 				this.put("name", name);
 			}
 		} catch (JSONException e) {
 			log.error("Can't update the name attribut",e);
 		}
 	}
 	
 	
 	/**
 	 * @return The number of account followed by the author. O if not provided. 
 	 */
 	public int 	getFriends() {
 		int friends;
 		try {
 			friends = this.getInt("friends");
 		} catch (Exception e) {
 			friends = 0;			
 		}
 		return friends;
 	}
 	
 	/**
 	 * Set the number of account followed by the author. Not provided if 0
 	 * @param friends
 	 */
 	public void setFriends(int friends) {
 		try {
 			if(friends == 0 ) {
 				this.remove("friends");
 			} else {
 				this.put("friends", friends);
 			}
 		} catch (JSONException e) {
 			log.error("Can't update the friends attribut",e);
 			
 		}
 	}
 	
 	
 
 	/**
 	 * @return The location provided by the author in its profile. Null if not provided.
 	 */
 	public String getLocation() {
 		String location;
 		try {
 			location = this.getString("location");
 		} catch (Exception e) {
 			location = null;		
 		}
 		return location;
 	}
 
 	/**
 	 * Set the location provided by the author in its profile. Not provided if empty.
 	 * @param location
 	 */
 	public void setLocation(String location) {
 		try {
			if(location == null & location.length()== 0) {
 				this.remove("location");
 			} else {
 				this.put("location", location);
 			}
 		} catch (JSONException e) {
 			log.error("Can't update the location attribut",e);
 		}
 
 
 	}
 	
 	
 	
 	/**
 	 * @return The author’s description provided in its profile. Null if not provided.
 	 */
 	public String getDescription() {
 		String description;
 		try {
 			description = this.getString("description");
 		} catch (Exception e) {
 			description = null;			
 		}
 		return description;
 	}
 	
 	/**
 	 * Set the author’s description. Not provided if empty.
 	 * @param description
 	 */
 	public void setDescription(String description) {
 		try {
			if(description == null & description.length()==0 ) {
 				this.remove("description");
 			} else {
 				this.put("description", description);
 			}
 		} catch (JSONException e) {
 			log.error("Can't update the description attribut",e);
 		}
 	}
 	
 	
 	/**
 	 * @return The http url of the author’s profile avatar. Null if not provided.
 	 */
 	public String getProfileImg() {
 		String profileImg;
 		try {
 			profileImg = this.getString("profileImg");
 		} catch (Exception e) {
 			profileImg = null;			
 		}
 		return profileImg;
 	}
 	/**
 	 * Set the http url of the authoer's profile avatar.
 	 * @param profileImg
 	 */
 	public void setProfileImg(String profileImg) {
 		try {
			if(profileImg == null & profileImg.length()== 0) {
 				this.remove("profileImg");
 			} else {
 				this.put("profileImg", profileImg);
 			}
 		} catch (JSONException e) {
 			log.error("Can't update the profile Image attribut",e);
 		}
 	}
 	
 	
 	/**
 	 * The URL
 	 * @return The http url of the author’s web site. Null if not provided.
 	 */
 	public String getURL() {
 		String url;
 		try {
 			url = this.getString("url").toString();
 		} catch (Exception e) {
 			url = null;			
 		}
 		return url;
 	}
 	
 	/**
 	 * Set the http url of the author's web site.
 	 * @param url
 	 */
 	public void setUrl(URL url) {
 		try {
 			if(url == null  ) {
 				this.remove("url");
 			} else {
 				this.put("url", url);
 			}
 		} catch (JSONException e) {
 			log.error("Can't update the url attribut",e);
 		}
 	}
 	
 	
 	
 	/**
 	 * @return The date of profile creation.
 	 */
 	public DateTime getCreatedAt() {
 		DateTime createdAt;
 		try {
 			createdAt = (DateTime) this.get("createdAt");;
 		} catch (JSONException e) {
 			createdAt = null;
 		}
 		return createdAt;
 	}
 
 	/**
 	 * Set the date of profile creation.
 	 * @param createdAt
 	 */
 	public void setCreatedAt(DateTime createdAt) {
 		try {
 			if(createdAt == null ) {
 				log.error("message: createdAt attribute is mandatory.");
 			} else {
 				this.put("createdAt", createdAt);
 			}
 		} catch (JSONException e) {
 			log.error("Can't update the createdAt  attribut",e);
 		}
 	}
 	
 	
 	/**
 	 * @return The preferred language code defined by the author in its profile. Null if not provided.
 	 */	
 	public String getLang() {
 		String lang;
 		try {
 			lang = this.getString("lang");
 		} catch (Exception e) {
 			lang = null;		
 		}
 		return lang;
 	}
 
 	/**
 	 * Set the preferred language code defined by the author in its profile.
 	 * @param lang
 	 */
 	public void setLang(String lang) {
 		try {
 			if(lang == null) {
 				this.remove("lang");
 			} else {
 				this.put("lang", lang);
 			}
 		} catch (JSONException e) {
 			log.error("Can't update the languge attribut",e);
 		}
 	}
 	
 	
 	/**
 	 * @return True means the author may use the geolocalisation. False if not provided.
 	 */	
 	public boolean getGeo() {
 		boolean geo;
 		try {
 			geo = this.getBoolean("geo");
 		} catch (Exception e) {
 			geo = false;		
 		}
 		return geo;
 	}
 
 	/**
 	 * True means the author may use the geolocalisation. Not provided if false.
 	 * @param geo
 	 */
 	public void setGeo(boolean geo) {
 		try {
 			if(geo == false) {
 				this.remove("geo");
 			} else {
 				this.put("geo", geo);
 			}
 		} catch (JSONException e) {
 			log.error("Can't update the Geolocation attribut",e);
 		}
 	}
 	
 	
 
 	/**
 	 * @return True means the author has been verified by twitter, false if not provided.
 	 */	
 	public boolean getVerified() {
 		boolean verified;
 		try {
 			verified = this.getBoolean("verified");
 		} catch (Exception e) {
 			verified = false;		
 		}
 		return verified;
 	}
 
 	/**
 	 * True means the author has been verified by twitter, not provided if false
 	 * @param verified
 	 */
 	public void setVerified(boolean verified) {
 		try {
 			if(verified == false) {
 				this.remove("verified");
 			} else {
 				this.put("verified", verified);
 			}
 		} catch (JSONException e) {
 			log.error("Can't update the verified attribut",e);
 		}
 	}
 
 	
 	
 	/**
 	 * @return The number of times the author is listed by other member of twitter. 0 if not provided.
 	 */
 	public int getListeds() {
 		int listeds;
 		try {
 			listeds = this.getInt("listeds");
 		} catch (Exception e) {
 			listeds = 0;			
 		}
 		return listeds;
 	}
 	
 	/**
 	 * Set the number of times the author is listed by other members of twitter.
 	 * @param listeds
 	 */
 	public void setListeds(int listeds) {
 		try {
 			if(listeds == 0) {
 				this.remove("listed");
 			} else {
 				this.put("listed", listeds);
 			}
 		} catch (JSONException e) {
 			log.error("Can't update the Listed  attribut",e);
 		}
 	}
 
 }
 
