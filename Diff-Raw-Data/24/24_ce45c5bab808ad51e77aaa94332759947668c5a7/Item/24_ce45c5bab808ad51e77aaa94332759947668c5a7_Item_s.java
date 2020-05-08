 /**
  * @author Dirk Bergstrom
  *
  * Keyring for webOS - Easy password management on your phone.
  * Copyright (C) 2009-2010, Dirk Bergstrom, keyring@otisbean.com
  *     
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.otisbean.keyring;
 
 import java.io.UnsupportedEncodingException;
 import java.security.GeneralSecurityException;
 
 import org.json.simple.JSONAware;
 import org.json.simple.JSONObject;
 
 /**
  * A single item on a Keyring.
  *
  * @author Dirk Bergstrom
  */
 public class Item implements JSONAware {
 	
 	// ENCRYPTED_ATTRS: ['username', 'pass', 'url', 'notes'],
 	
	// PLAINTEXT_ATTRS: ['title', 'category', 'created', 'viewed', 'modified'],
 
 	private Ring ring;
 	private String username;
 	private String pass;
 	private String url;
 	private String notes;
 	private String title;
 	private int category;
 	private long created;
 	private long viewed;
	private long modified;
 	
 	public Item(Ring ring, String username, String pass, String url, String notes,
			String title, String categoryName, long created, long viewed, long modified) {
 		super();
 		this.ring = ring;
 		this.username = username;
 		this.pass = pass;
 		this.url = url;
 		this.notes = notes;
 		this.title = title;
 		this.created = created;
 		this.viewed = viewed;
		this.modified = modified;
 		this.category = ring.categoryIdForName(categoryName);
 	}
 	
 	@SuppressWarnings("unchecked")
 	@Override
 	public String toJSONString() {
 		JSONObject crypted = new JSONObject();
 		crypted.put("username", username);
 		crypted.put("pass", pass);
 		crypted.put("url", url);
 		crypted.put("notes", notes);
 		String cryptedJson;
 		try {
 			cryptedJson = ring.encrypt(crypted.toJSONString(), Ring.ITEM_SALT_LENGTH);
 		} catch (UnsupportedEncodingException e) {
 			throw new RuntimeException(e);
 		} catch (GeneralSecurityException e) {
 			throw new RuntimeException(e);
 		}
 
 		JSONObject itemJson = new JSONObject();
 		itemJson.put("title", title);
 		itemJson.put("category", category);
 		// Dates are stored as an empty string if undefined
 		itemJson.put("created", created == 0 ? "" : created);
 		itemJson.put("viewed", viewed == 0 ? "" : viewed);
		itemJson.put("modified", modified == 0 ? "" : modified);
 		itemJson.put("encrypted_data", cryptedJson);
 
 		return itemJson.toJSONString();
 	}
 	
 	public String getUsername() {
 		return username;
 	}
 	public void setUsername(String username) {
 		this.username = username;
 	}
 	public String getPass() {
 		return pass;
 	}
 	public void setPass(String pass) {
 		this.pass = pass;
 	}
 	public String getUrl() {
 		return url;
 	}
 	public void setUrl(String url) {
 		this.url = url;
 	}
 	public String getNotes() {
 		return notes;
 	}
 	public void setNotes(String notes) {
 		this.notes = notes;
 	}
 	public String getTitle() {
 		return title;
 	}
 	public void setTitle(String title) {
 		this.title = title;
 	}
 	public long getCreated() {
 		return created;
 	}
 	public void setCreated(long created) {
 		this.created = created;
 	}
 	public long getViewed() {
 		return viewed;
 	}
 	public void setViewed(long viewed) {
 		this.viewed = viewed;
 	}
	public long getModified() {
		return modified;
 	}
	public void setModified(long modified) {
		this.modified = modified;
 	}
 	public String getCategory() {
 		return ring.categoryNameForId(category);
 	}
 	public void setCategory(String categoryName) {
 		this.category = ring.categoryIdForName(categoryName);
 	}
 
 	/**
 	 * @return the ring
 	 */
 	public Ring getRing() {
 		return ring;
 	}
 
 	/**
 	 * @param ring the ring to set
 	 */
 	public void setRing(Ring ring) {
 		this.ring = ring;
 	}
 }
