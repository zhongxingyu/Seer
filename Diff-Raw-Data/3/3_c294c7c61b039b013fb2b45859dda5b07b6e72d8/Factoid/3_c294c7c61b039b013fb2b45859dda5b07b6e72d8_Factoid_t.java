 /*
  * Copyright (C) 2012 CyborgDev <cyborg@alta189.com>
  *
  * This file is part of CyborgREST
  *
  * CyborgREST is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * CyborgREST is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.alta189.cyborg.rest.factoids;
 
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
 public class Factoid {
 	private int id;
 	private String name;
 	private String location;
 	private String handler;
 	private String contents;
 	private String author;
 	private boolean locked = false;
 	private String locker;
 	private boolean forgotten = false;
 	private String forgetter;
 	private long timestamp;
 
 	public Factoid() {
 
 	}
 
 	public Factoid(com.alta189.cyborg.factoids.Factoid factoid) {
 		id = factoid.getId();
 		name = factoid.getName();
 		location = factoid.getLocation();
 		handler = factoid.getHandler();
 		contents = factoid.getContents();
 		author = factoid.getAuthor();
 		locked = factoid.isLocked();
 		locker = factoid.getLocker();
 		forgotten = factoid.isForgotten();
 		forgetter = factoid.getForgetter();
 		timestamp = factoid.getTimestamp();
 	}
 
 	public int getId() {
 		return id;
 	}
 
 	public void setId(int id) {
 		this.id = id;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getLocation() {
 		return location;
 	}
 
 	public void setLocation(String location) {
 		this.location = location;
 	}
 
 	public String getHandler() {
 		return handler;
 	}
 
 	public void setHandler(String handler) {
 		this.handler = handler;
 	}
 
 	public String getContents() {
 		return contents;
 	}
 
 	public void setContents(String contents) {
 		this.contents = contents;
 	}
 
 	public String getAuthor() {
 		return author;
 	}
 
 	public void setAuthor(String author) {
 		this.author = author;
 	}
 
 	public boolean isLocked() {
 		return locked;
 	}
 
 	public void setLocked(boolean locked) {
 		this.locked = locked;
 	}
 
 	public String getLocker() {
 		return locker;
 	}
 
 	public void setLocker(String locker) {
 		this.locker = locker;
 	}
 
 	public boolean isForgotten() {
 		return forgotten;
 	}
 
 	public void setForgotten(boolean forgotten) {
 		this.forgotten = forgotten;
 	}
 
 	public String getForgetter() {
 		return forgetter;
 	}
 
 	public void setForgetter(String forgetter) {
 		this.forgetter = forgetter;
 	}
 
 	public long getTimestamp() {
 		return timestamp;
 	}
 
 	public void setTimestamp(long timestamp) {
 		this.timestamp = timestamp;
 	}
 }
