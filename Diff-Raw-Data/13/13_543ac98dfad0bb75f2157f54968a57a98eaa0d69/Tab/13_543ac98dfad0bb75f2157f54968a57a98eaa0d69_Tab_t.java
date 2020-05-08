 package com.sjriley.crawler.dao;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Date;
 
 import org.bson.types.ObjectId;
 
 import com.google.code.morphia.annotations.Entity;
 import com.google.code.morphia.annotations.Id;
 import com.google.gson.Gson;
 
 @Entity
 public class Tab {
 	@Id ObjectId id;
 	private String url;
 	private String artist;
 	private String title;
 	private int rating;
 	private String type;
 	private int numberOfVotes;
 	private Date updated;
 	private Date pageProcessed;
 	private Date dateTabStarted;
 	private Date dateTabCompleted;
 	private String originalTabText;
	/**
	 * 
	 */
	private boolean expire;
 	
	public boolean isExpire() {
		return expire;
	}

	public void setExpire(boolean expire) {
		this.expire = expire;
	}

 	public String getOriginalTabText() {
 		return originalTabText;
 	}
 
 	public void setOriginalTabText(String originalTabText) {
 		this.originalTabText = originalTabText;
 	}
 
 	public Date getDateTabStarted() {
 		return dateTabStarted;
 	}
 
 	public void setDateTabStarted(Date dateTabStarted) {
 		this.dateTabStarted = dateTabStarted;
 	}
 
 	public Date getDateTabCompleted() {
 		return dateTabCompleted;
 	}
 
 	public void setDateTabCompleted(Date dateTabCompleted) {
 		this.dateTabCompleted = dateTabCompleted;
 	}
 
 	public String getTitle() {
 		return title;
 	}
 
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	public String getArtist() {
 		return artist;
 	}
 
 	public void setArtist(String artist) {
 		this.artist = artist;
 	}
 
 	public Date getUpdated() {
 		return updated;
 	}
 
 	public void setUpdated(Date updated) {
 		this.updated = updated;
 	}
 
 	public Date getPageProcessed() {
 		return pageProcessed;
 	}
 
 	public void setPageProcessed(Date pageProcessed) {
 		this.pageProcessed = pageProcessed;
 	}
 
 	public int getNumberOfVotes() {
 		return numberOfVotes;
 	}
 
 	public void setNumberOfVotes(int numberOfVotes) {
 		this.numberOfVotes = numberOfVotes;
 	}
 
 	public int getRating() {
 		return rating;
 	}
 
 	public void setRating(int rating) {
 		this.rating = rating;
 	}
 
 	public String getType() {
 		return type;
 	}
 
 	public void setType(String type) {
 		this.type = type;
 	}
 
 	public URL getUrl() {
 		try {
 			return new URL(url);
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	public void setUrl(URL url) {
 		this.url = url.toExternalForm();
 	} 
 	@Override
 	public String toString() {
 		Gson gson = new Gson();
 		return gson.toJson(this, this.getClass());
 	}
 }
