 package net.wagnerism.podcast;
 
 /**
  * Simple java object for encaspulating a data
  * for a podcast episode.
  * 
  * @author Sean Wagner
  *
  */
 public class Episode {
 	
 	private String title;
 	private String description;
 	private String blogUrl;
 	private String audioUrl;
 	
 	public Episode() {
 		// empty constructor
 	}
 	
 	public Episode(String title, String description, String blogUrl, String audioUrl) {
 		this.title = title;
 		this.description = description;
 		this.blogUrl = blogUrl;
 		this.audioUrl = audioUrl;
 	}
 
 	public String getTitle() {
 		return title;
 	}
 
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public String getBlogUrl() {
 		return blogUrl;
 	}
 
 	public void setBlogUrl(String blogUrl) {
 		this.blogUrl = blogUrl;
 	}
 
 	public String getAudioUrl() {
 		return audioUrl;
 	}
 
 	public void setAudioUrl(String audioUrl) {
 		this.audioUrl = audioUrl;
 	}
 	
	public Episode copy() {
		return new Episode(this.title, this.description, this.blogUrl, this.audioUrl);
	}
	
 	public String toString() {
 		return title;
 	}
 	
 	public String[] toStringArray() {
 		return new String[] {title, description, blogUrl, audioUrl};
 	}
 
 }
