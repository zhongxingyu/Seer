 package com.adtworker.mail;
 
 public class AdtImage {
 
 	boolean isAsset = false;
 	boolean hasThumb = false;
 	boolean isCached = false;
 
 	String urlFull;
 	String urlThumb;
 
 	public AdtImage() {
 
 	}
 
 	public AdtImage(String url, boolean is_asset) {
 		urlFull = url;
 		isAsset = is_asset;
 	}
 
 	public AdtImage(String url) {
 		urlFull = url;
 	}
 
 	public AdtImage(String url, String urlTb) {
 		urlFull = url;
 		urlThumb = urlTb;
 		hasThumb = true;
 	}
 }
