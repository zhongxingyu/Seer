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
 
 import java.util.Calendar;
 import java.util.List;
 
 /**
  * @version 0.3
  * hAPI MessageOption. For more info, see Hubiquitus reference
  */
 
 public class HMessageOptions {
 	
 	private String convid = null;
 	private HMessagePriority priority = null;
 	private Calendar relevance = null;
 	private Boolean _transient = null;
 	private HLocation location = null;
 	private String author = null;
 	private List<HJsonObj> headers = null;
 	private Calendar published = null;
	
 	/**
 	 * @return conversation id. NULL if undefined 
 	 */
 	public String getConvid() {
 		return convid;
 	}
 	public void setConvid(String convid) {
 		this.convid = convid;
 	}
 	
 	/**
 	 * If UNDEFINED, priority lower to 0. 
 	 * @return priority.
 	 */
 	public HMessagePriority getPriority() {
 		return priority;
 	}
 	public void setPriority(HMessagePriority priority) {
 		this.priority = priority;
 	}
 	
 	/**
 	 * Date-time until which the message is considered as relevant.
 	 * @return relevance. NULL if undefined
 	 */
 	public Calendar getRelevance() {
 		return relevance;
 	}
 	public void setRelevance(Calendar relevance) {
 		this.relevance = relevance;
 	}
 	
 	/**
 	 * Persistent if false.
 	 * @return persist message or not. NULL if undefined
 	 */
 	public Boolean getTransient() {
 		return _transient;
 	}
 	public void setTransient(Boolean _transient) {
 		this._transient = _transient;
 	}
 	
 	/**
 	 * The geographical location to which the message refer.
 	 * @return location. NULL if undefined
 	 */
 	public HLocation getLocation() {
 		return location;
 	}
 	public void setLocation(HLocation location) {
 		this.location = location;
 	}
 	
 	/**
 	 * @return author of this message. NULL if undefined 
 	 */
 	public String getAuthor() {
 		return author;
 	}
 	public void setAuthor(String author) {
 		this.author = author;
 	}
 	
 	/**
 	 * The list of headers attached to this message.
 	 * @return Headers. NULL if undefined
 	 */
 	public List<HJsonObj> getHeaders() {
 		return headers;
 	}
 	public void setHeaders(List<HJsonObj> headers) {
 		this.headers = headers;
 	}
 	
 	/**
 	 * Date-time when the message is publish
 	 * @return relevance. NULL if undefined
 	 */
 	public Calendar getPublished() {
 		return published;
 	}
 	public void setPublished(Calendar published) {
 		this.published = published;
 	}
 	
 }
