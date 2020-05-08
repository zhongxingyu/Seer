 package br.com.entropie.hellocuriosity.news;
 
 public class Asset {
 
 	private final String media;
 	private final String credit;
 	private final String caption;
 
 	public Asset(String media) {
 		this.media = media;
 		this.credit = "";
 		this.caption = "";
 	}
 
 	public Asset(String media, String credit, String caption) {
 		this.media = media;
 		this.credit = credit;
 		this.caption = caption;
 	}
 
 	public String getMedia() {
 		return media;
 	}
 
 	public String getCredit() {
 		return credit;
 	}
 
 	public String getCaption() {
 		return caption;
 	}
 }
