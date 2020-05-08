 package com.example.folio.shared.model;
 
 /**
 * Foreground or background image.
  */
 public class Image {
 
 	/**
 	 * A name based on which the URL to an image will be generated.
 	 */
 	private String name;
 	
 	/**
 	 * URL to an image.
 	 */
 	private String url;
 	
 	public String getName() {
 		return name;
 	}
 	
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 	public String getUrl() {
 		return url;
 	}
 	
 	public void setUrl(String url) {
 		this.url = url;
 	}
 	
 }
