 package edu.android.podcast_listener.db;
 
 public class Podcast {
 	private long id;
 	private String name;
 	private String url;
 	private String img;
 	private String category;
 		
 	public long getId() {
 		return id;
 	}
 	public void setId(long id) {
 		this.id = id;
 	}
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
 	public String getImg() {
 		return img;
 	}
 	public void setImg(String img) {
 		this.img = img;
 	}
 	public String getCategory() {
 		return category;
 	}
 	public void setCategory(String category) {
 		this.category = category;
 	}
 	
 	@Override
 	public String toString() {
		return name + ";" + url;
 	}
 	
 }
