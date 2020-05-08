 package mvc.model.utilities;
 
 import java.io.Serializable;
 import java.net.URL;
 
 public class AudioAlbumTrack implements Serializable,
 		Comparable<AudioAlbumTrack> {
 	private static final long serialVersionUID = -7691994149397894841L;
 	private Long duration;
 	private Integer id;
 	private String name;
 	private URL lastFMUrl;
 	private Double rating;
 
 	public AudioAlbumTrack(Integer id, String name, String dur) {
 		this.id = id;
 		this.name = name;
 
 		this.duration = 0l;
//		if(dur.isEmpty()){
//			this.duration = 0l;
//		}else{
//			this.duration = Long.valueOf(dur);
//		}
 		this.lastFMUrl = null;
 		this.rating = 0.0;
 	}
 
 	public String getDuration() {
		int minutes = (int) (this.duration / 60);
		int leftSeconds = (int) (this.duration - (minutes * 60));
		return minutes + ":" + leftSeconds;
 	}
 
 	public Long getLongDuration() {
 		return this.duration;
 	}
 
 	public void setDuration(String dur) {
 		this.duration = Long.valueOf(dur);
 	}
 
 	public Integer getId() {
 		return id;
 	}
 
 	public void setId(Integer id) {
 		this.id = id;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public URL getLastFMUrl() {
 		return lastFMUrl;
 	}
 
 	public void setLastFMUrl(URL lastFMUrl) {
 		this.lastFMUrl = lastFMUrl;
 	}
 
 	@Override
 	public int compareTo(AudioAlbumTrack o) {
 		int result = this.id.compareTo(o.id);
 		if (result == 0) {
 			result = this.name.compareTo(o.name);
 		}
 		return result;
 	}
 
 	@Override
 	public String toString() {
 		return this.id.toString() + ". " + this.name + "\t("
 				+ this.getDuration() + ")";
 	}
 
 	public Double getRating() {
 		return rating;
 	}
 
 	public void setRating(Double rating) {
 		this.rating = rating;
 	}
 }
