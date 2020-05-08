 package mdettlaff.cloudreader.domain;
 
 import java.util.Date;
 
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 
 import com.fasterxml.jackson.annotation.JsonIgnore;
 
 @Entity
public class FeedItem implements Comparable<FeedItem> {
 
 	@Id
 	private String guid;
 	@ManyToOne(optional = false)
 	private Feed feed;
 	private String title;
 	private String link;
 	private String description;
 	private Date date;
 	private Date downloadDate;
 	private String author;
 	private String uri;
 	private boolean read;
 
 	public String getGuid() {
 		return guid;
 	}
 
 	public void setGuid(String guid) {
 		this.guid = guid;
 	}
 
 	public Feed getFeed() {
 		return feed;
 	}
 
 	public void setFeed(Feed feed) {
 		this.feed = feed;
 	}
 
 	public String getTitle() {
 		return title;
 	}
 
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	public String getLink() {
 		return link;
 	}
 
 	public void setLink(String link) {
 		this.link = link;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public Date getDate() {
 		return date;
 	}
 
 	public void setDate(Date date) {
 		this.date = date;
 	}
 
 	public Date getDownloadDate() {
 		return downloadDate;
 	}
 
 	public void setDownloadDate(Date downloadDate) {
 		this.downloadDate = downloadDate;
 	}
 
 	public String getAuthor() {
 		return author;
 	}
 
 	public void setAuthor(String author) {
 		this.author = author;
 	}
 
 	@JsonIgnore
 	public String getUri() {
 		return uri;
 	}
 
 	public void setUri(String uri) {
 		this.uri = uri;
 	}
 
 	public boolean isRead() {
 		return read;
 	}
 
 	public void setRead(boolean read) {
 		this.read = read;
 	}
 
	@Override
	public int compareTo(FeedItem other) {
		if (date != null && other.date != null) {
			return date.compareTo(other.date);
		} else {
			return downloadDate.compareTo(other.downloadDate);
		}
	}

 	@JsonIgnore
 	public String getHashBase() {
 		StringBuilder builder = new StringBuilder();
 		builder.append(feed.getUrl());
 		builder.append(uri);
 		builder.append(link);
 		builder.append(title);
 		builder.append(date);
 		return builder.toString();
 	}
 }
