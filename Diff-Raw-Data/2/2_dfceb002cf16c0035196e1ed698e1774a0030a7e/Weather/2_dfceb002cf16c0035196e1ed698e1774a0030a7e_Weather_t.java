 package com.ifactory.client.openweather;
 
 import java.io.Serializable;
 
 public class Weather implements Serializable {
 	
 	private long id;
 	private String keyword;
 	private String description;
 	private String icon;
 	
 	static final class Builder {
 		
 		private long id;
 		private String keyword;
 		private String description;
 		private String icon;
 		
 		public Builder(long id, String keyword) {
 			this.id = id;
 			this.keyword = keyword;				
 		}
 
 		public Builder description(String description) {
 			this.description = description;
 			return this;
 		}	
 		
 		public Builder icon(String icon) {
 			this.icon = icon;
 			return this;
 		}			
 		
 		public Weather build() {
 			return new Weather(this);
 		}
 	}
 	
 	private Weather(Builder builder) {
 		id = builder.id;
 		keyword = builder.keyword;
 		description = builder.description;
 		icon = builder.icon;			
 	}	
 	
 	public String getKeyword() {
 		return keyword;
 	}
 	
 	public String getDescription() {
 		return description;
 	}
 	
 	public String getIcon() {
 		return icon;
 	}
 	
 	public long getId() {
 		return id;
 	}
 }
