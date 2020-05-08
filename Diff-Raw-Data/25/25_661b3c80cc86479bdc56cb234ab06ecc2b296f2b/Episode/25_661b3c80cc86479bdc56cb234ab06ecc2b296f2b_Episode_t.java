 package de.binaervarianz.holopod.db;
 
 import java.io.Serializable;
 
 public class Episode implements Serializable {
 	private static final long serialVersionUID = -7311381002303717513L;
 
 	long _id = 0;
 	long channel;
 	String title;
 	String subtitle;
 	String description;
 	String link;
	long image = 0;
 	String author;
 	String keywords;
 	long lastupdated;
	long playCount = 0;
 
 	String enc_url;
 	long enc_size; // tag:length
 	long enc_rcvsize; // downloaded bytes
 	long enc_duration;
 	String enc_type;
 	String enc_filepath;
	Boolean enc_onDevice = false; // file present on device?
 	long enc_pausedTime; // time in enclosure file where last paused
 	long enc_dlDate; // timestamp when enclosure was downloaded completely
 	long pubDate;
 	Boolean archive; // not available in rss-feed anymore but on device
 	long downloadId; // not stored in database
 
 	// Constructor
 	public Episode() {
 
 	}
 	
 	// Constructor
 	public Episode(long channel) {
 		this.channel = channel;
 	}
 
 	// Constructor
 	public Episode(long id, long channel, String title, String subtitle,
 			String description, String link, long image, String author,
 			String keywords, long lastupdated, String enc_url, long enc_size,
 			long enc_rcvsize, long enc_duration, String enc_type,
 			String enc_filepath, Boolean enc_onDevice, long enc_pausedTime,
 			long enc_dlDate, long pubDate, Boolean archive) {
 		this._id = id;
 		this.channel = channel;
 		this.title = title;
 		this.subtitle = subtitle;
 		this.description = description;
 		this.link = link;
 		this.image = image;
 		this.author = author;
 		this.keywords = keywords;
 		this.lastupdated = lastupdated;
 		this.enc_url = enc_url;
 		this.enc_size = enc_size;
 		this.enc_rcvsize = enc_rcvsize;
 		this.enc_duration = enc_duration;
 		this.enc_type = enc_type;
 		this.enc_filepath = enc_filepath;
 		this.enc_onDevice = enc_onDevice;
 		this.enc_pausedTime = enc_pausedTime;
 		this.enc_dlDate = enc_dlDate;
 		this.pubDate = pubDate;
 		this.archive = archive;
 	}
 
 	// Constructor
 	public Episode(long id, long channel, String title, String subtitle,
 			String description, String link, String enc_url, long enc_size,
 			String enc_type, String enc_filepath, Boolean enc_onDevice) {
 		this._id = id;
 		this.channel = channel;
 		this.title = title;
 		this.subtitle = subtitle;
 		this.description = description;
 		this.link = link;
 		this.enc_url = enc_url;
 		this.enc_size = enc_size;
 		this.enc_type = enc_type;
 		this.enc_filepath = enc_filepath;
 		this.enc_onDevice = enc_onDevice;
 	}
 	
 	// Constructor
 	public Episode(long channel, String title, String subtitle,
 			String description, String link, String author, String keywords,
 			long lastupdated, String enc_url, long enc_size, String enc_type,
 			long pubDate) {
 		this.channel = channel;
 		this.title = title;
 		this.subtitle = subtitle;
 		this.description = description;
 		this.link = link;
 		this.author = author;
 		this.keywords = keywords;
 		this.lastupdated = lastupdated;
 		this.enc_url = enc_url;
 		this.enc_size = enc_size;
 		this.enc_type = enc_type;
 		this.pubDate = pubDate;
 	}
 
 	// public methods
 	public long getId() {
 		return this._id;
 	}
 
 	public void setId(long id) {
 		this._id = id;
 	}
 
 	public long getChannel() {
 		return this.channel;
 	}
 
 	public void setChannel(long channel) {
 		this.channel = channel;
 	}
 
 	public String getTitle() {
 		return this.title;
 	}
 
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	public String toString() {
 		return this.getTitle();
 	}
 
 	public String getSubtitle() {
 		return this.subtitle;
 	}
 
 	public void setSubtitle(String subtitle) {
 		this.subtitle = subtitle;
 	}
 
 	public String getDescription() {
 		return this.description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public String getLink() {
 		return this.link;
 	}
 
 	public void setLink(String link) {
 		this.link = link;
 	}
 
 	public long getImage() {
 		return this.image;
 	}
 	
 	public void setImage(long image_key) {
 		this.image = image_key;
 	}
 	
 	public String getAuthor() {
 		return this.author;
 	}
 	
 	public void setAuthor(String author) {
 		this.author = author;
 	}
 	
 	public String getKeywords() {
 		return this.keywords;
 	}
 	
 	public void setKeywords(String keywords) {
 		this.keywords = keywords;
 	}
 	
 	public long getLastUpdated() {
 		return lastupdated;
 	}
 	
 	public void setLastUpdated(long lastupdated) {
 		this.lastupdated = lastupdated;
 	}
 	
 	public String getEncUrl() {
 		return this.enc_url;
 	}
 
 	public void setEncUrl(String enc_url) {
 		this.enc_url = enc_url;
 	}
 	
 	public long getEncSize() {
 		return this.enc_size;
 	}
 
 	public void setEncSize(long enc_size) {
 		this.enc_size = enc_size;
 	}
 	
 	public String getEncType() {
 		return this.enc_type;
 	}
 
 	public void setEncType(String enc_type) {
 		this.enc_type = enc_type;
 	}
 	public String getEncFilepath() {
 		return this.enc_filepath;
 	}
 
 	public void setEncFilepath(String enc_filepath) {
 		this.enc_filepath = enc_filepath;
 	}
 	
 	public Boolean getEncOnDevice() {
 		return this.enc_onDevice;
 	}
 	
 	public void setEncOnDevice(Boolean enc_onDevice) {
 		this.enc_onDevice = enc_onDevice;
 	}
 	
 	public long getDownloadId() {
 		return this.downloadId;
 	}
 
 	public void setDownloadId(long downloadId) {
 		this.downloadId = downloadId;
 	}
 	
 }
