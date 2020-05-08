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
 
 
 package org.hubiquitus.hapi.hStructures;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import org.hubiquitus.hapi.util.DateISO8601;
 import org.hubiquitus.hapi.util.HJsonDictionnary;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 /**
  * @version 0.3
  * hAPI Command. For more info, see Hubiquitus reference
  */
 
 public class HMessage implements HJsonObj {
 	
 	private JSONObject hmessage = new JSONObject();
 	
 	public HMessage() {
 	}
 	
 	public HMessage(JSONObject jsonObj) {
		this.hmessage = jsonObj;
 	}
 	
 	/* HJsonObj interface */
 	
 	public JSONObject toJSON() {
 		return this.hmessage;
 	}
 	
 	public void fromJSON(JSONObject jsonObj) {
 		if(jsonObj != null){
 			this.hmessage = jsonObj;
 		} else {
 			this.hmessage = new JSONObject();
 		}
 	}
 	
 	public String getHType() {
 		return "hmessage";
 	}
 	
 	@Override
 	public String toString() {
 		return hmessage.toString();
 	}
 	
 	/**
 	 * Check are made on : msgid, chid, convid, type, priority, relevance,
 	 * transient, author, publisher, published and location. 
 	 * @param HAck 
 	 * @return Boolean
 	 */
 	public boolean equals(HMessage obj) {
 		if(obj.getMsgid() != this.getMsgid())
 			return false;
 		if(obj.getChid() != this.getChid())
 			return false;
 		if(obj.getConvid() != this.getConvid())
 			return false;
 		if(obj.getType() != this.getType())
 			return false;
 		if(obj.getPriority().value() != this.getPriority().value())
 			return false;
 		if(obj.getRelevance() != this.getRelevance())
 			return false;
 		if(obj.getTransient() != this.getTransient())
 			return false;
 		if(obj.getAuthor() != this.getAuthor())
 			return false;
 		if(obj.getPublisher() != this.getPublisher())
 			return false;
 		if(obj.getPublished() != this.getPublished())
 			return false;
 		if(obj.getLocation().equals(this.getLocation()))
 			return false;
 		return true;
 	}
 	
 	@Override
 	public int hashCode() {
 		return hmessage.hashCode();
 	}
 	
 	/* Getters & Setters */
 	
 	/**
 	 * Mandatory. Filled by the hApi.
 	 * @return message id. NULL if undefined
 	 */
 	public String getMsgid() {
 		String msgid;
 		try {
 			msgid = hmessage.getString("msgid");
 		} catch (Exception e) {
 			msgid = null;
 		}
 		return msgid;
 	}
 
 	public void setMsgid(String msgid) {
 		try {
 			if(msgid == null) {
 				hmessage.remove("msgid");
 			} else {
 				hmessage.put("msgid", msgid);
 			}
 		} catch (JSONException e) {
 		}
 	}
 
 	/**
 	 * Mandatory
 	 * @return channel id. NULL if undefined 
 	 */
 	public String getChid() {
 		String chid;
 		try {
 			chid = hmessage.getString("chid");
 		} catch (Exception e) {
 			chid = null;
 		}
 		return chid;
 	}
 
 	public void setChid(String chid) {
 		try {
 			if(chid == null) {
 				hmessage.remove("chid");
 			} else {
 				hmessage.put("chid", chid);
 			}
 		} catch (JSONException e) {
 		}
 	}
 
 	/**
 	 * Mandatory. Filled by the hApi if empty.
 	 * @return conversation id. NULL if undefined 
 	 */
 	public String getConvid() {
 		String convid;
 		try {
 			convid = hmessage.getString("convid");
 		} catch (Exception e) {
 			convid = null;
 		}
 		return convid;
 	}
 
 	public void setConvid(String convid) {
 		try {
 			if(convid == null) {
 				hmessage.remove("convid");
 			} else {
 				hmessage.put("convid", convid);
 			}
 		} catch (JSONException e) {
 		}
 	}
 
 	/**
 	 * @return type of the message payload. NULL if undefined 
 	 */
 	public String getType() {
 		String type;
 		try {
 			type = hmessage.getString("type");
 		} catch (Exception e) {
 			type = null;
 		}
 		return type;
 	}
 	
 	public void setType(String type) {
 		try {
 			if(type == null) {
 				hmessage.remove("type");
 			} else {
 				hmessage.put("type", type);
 			}
 		} catch (JSONException e) {
 		}
 	}
 
 	/**
 	 * If UNDEFINED, priority lower to 0. 
 	 * @return priority.
 	 */
 	public HMessagePriority getPriority() {
 		HMessagePriority priority;
 		try {
 			int priorityInt = hmessage.getInt("priority");
 			if(priorityInt < 0 || priorityInt > 5) {
 				priority = null;
 			} else {
 				priority = HMessagePriority.constant(priorityInt);
 			}
 		} catch (Exception e1) {
 			priority = null;
 		}
 		return priority;
 	}
 
 	public void setPriority(HMessagePriority priority) {
 		try {
 			if(priority == null) {
 				hmessage.remove("priority");
 			} else {
 				hmessage.put("priority", priority.value());
 			}
 		} catch (JSONException e) {
 		}
 	}
 
 	
 	/**
 	 * Date-time until which the message is considered as relevant.
 	 * @return relevance. NULL if undefined
 	 */
 	public Calendar getRelevance() {
 		Calendar relevance;
 		try {
 			relevance = (DateISO8601.toCalendar(hmessage.getString("relevance")));;
 		} catch (JSONException e) {
 			relevance = null;
 		}
 		return relevance;
 	}
 
 	public void setRelevance(Calendar relevance) {
 		try {
 			if(relevance == null) {
 				hmessage.remove("relevance");
 			} else {
 				hmessage.put("relevance", DateISO8601.fromCalendar(relevance));
 			}
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * If true, the message is not persistent.
 	 * @return persist message or not. NULL if undefined
 	 */
 	public Boolean getTransient() {
 		Boolean _transient;
 		try {
 			_transient = hmessage.getBoolean("transient");
 		} catch (JSONException e) {
 			_transient = null;
 		}
 		return _transient;
 	}
 
 	public void setTransient(Boolean _transient) {
 		try {
 			if(_transient == null) {
 				hmessage.remove("transient");
 			} else {
 				hmessage.put("transient", _transient);
 			}
 		} catch (JSONException e) {
 		}
 	}	
 	
 	/**
 	 * The geographical location to which the message refer.
 	 * @return location. NULL if undefined
 	 */
 	public HLocation getLocation() {
 		HLocation location;
 		try {
 			location = new HLocation(hmessage.getJSONObject("location"));
 		} catch (JSONException e) {
 			location = null;
 		}
 		return location;
 	}
 
 	public void setLocation(HLocation location) {
 		try {
 			if(location == null) {
 				hmessage.remove("location");
 			} else {
 				hmessage.put("location", location.toJSON());
 			}
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * @return author of this message. NULL if undefined 
 	 */
 	public String getAuthor() {
 		String author;
 		try {
 			author = hmessage.getString("author");
 		} catch (Exception e) {
 			author = null;
 		}
 		return author;
 	}
 	
 	public void setAuthor(String author) {
 		try {
 			if(author == null) {
 				hmessage.remove("author");
 			} else {
 				hmessage.put("author", author);
 			}
 		} catch (JSONException e) {
 		}
 	}
 	
 	/**
 	 * Mandatory
 	 * @return publisher of this message. NULL if undefined 
 	 */
 	public String getPublisher() {
 		String publisher;
 		try {
 			publisher = hmessage.getString("publisher");
 		} catch (Exception e) {
 			publisher = null;
 		}
 		return publisher;
 	}
 	
 	public void setPublisher(String publisher) {
 		try {
 			if(publisher == null) {
 				hmessage.remove("publisher");
 			} else {
 				hmessage.put("publisher", publisher);
 			}
 		} catch (JSONException e) {
 		}
 	}
 	
 	/**
 	 * Mandatory.
 	 * The date and time at which the message has been published.
 	 * @return published. NULL if undefined
 	 */
 	public Calendar getPublished() {
 		Calendar published;
 		try {
 			published = (DateISO8601.toCalendar(hmessage.getString("published")));
 		} catch (JSONException e) {
 			published = null;
 		}
 		return published;
 	}
 
 	public void setPublished(Calendar published) {
 		try {
 			if(published == null) {
 				hmessage.remove("published");
 			} else {
 				hmessage.put("published", DateISO8601.fromCalendar(published));
 			}
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * The list of headers attached to this message.
 	 * @return Headers. NULL if undefined
 	 */
 	public List<HJsonObj> getHeaders() {
 		List<HJsonObj> headers = new ArrayList<HJsonObj>();
 		try {
 			JSONArray headersArray = hmessage.getJSONArray("headers");
 			for(int i = 0; i < headersArray.length() ; i++) {
 				HJsonDictionnary header = new HJsonDictionnary(headersArray.getJSONObject(i)); 
 				headers.add(header);
 			}
 		} catch (JSONException e) {
 			headers = null;
 		}
 		return headers;
 	}
 
 	public void setHeaders(List<HJsonObj> headers) {
 		try {
 			if(headers == null) {
 				hmessage.remove("headers");
 			} else {
 				JSONArray headersArray = new JSONArray();
 				for(HJsonObj header : headers) {
 					headersArray.put(header.toJSON());
 				}
 				hmessage.put("headers", headersArray);
 			}
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * The content of the message.
 	 * @return payload. NULL if undefined
 	 */
 	public HJsonObj getPayload() {
 		HJsonObj payload;
 		try {
 			JSONObject jsonPayload = hmessage.getJSONObject("payload");
 			String type = this.getType().toLowerCase();
 			if (type.equals("hmeasure")) {
 				payload = new HMeasure(jsonPayload);
 			} else if (type.equals("halert")) {
 				payload = new HAlert(jsonPayload);
 			} else if (type.equals("hack")) {
 				payload = new HAck(jsonPayload);
 			} else if (type.equals("hconv")) {
 				payload = new HConv(jsonPayload);
 			} else {
 				payload = new HJsonDictionnary(jsonPayload);
 			}
 		} catch (JSONException e) {
 			payload = null;
 		}
 		return payload;
 	}
 
 	public void setPayload(HJsonObj payload) {
 		try {
 			if(payload == null) {
 				hmessage.remove("payload");
 			} else {
 				hmessage.put("payload", payload.toJSON());
 			}
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 	}
 }
