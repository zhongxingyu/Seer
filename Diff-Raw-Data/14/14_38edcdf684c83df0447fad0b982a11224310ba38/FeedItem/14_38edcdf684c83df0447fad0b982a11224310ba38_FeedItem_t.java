 package mdettlaff.cloudreader.domain;
 
 import java.util.Date;
 import java.util.Objects;
 
 import javax.persistence.Entity;
 import javax.persistence.EnumType;
 import javax.persistence.Enumerated;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 
import org.apache.commons.lang3.ObjectUtils;

 import com.fasterxml.jackson.annotation.JsonIgnore;
 
 @Entity
 public class FeedItem {
 
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
 	@Enumerated(EnumType.STRING)
 	private Status status;
 
 	public FeedItem() {
 		status = Status.UNREAD;
 	}
 
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
 
	public Date getSortDate() {
		return ObjectUtils.firstNonNull(date, downloadDate);
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
 
 	public Status getStatus() {
 		return status;
 	}
 
 	public void setStatus(Status status) {
 		this.status = status;
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
 
 	@Override
 	public boolean equals(Object obj) {
 		if (obj == null) {
 			return false;
 		}
 		if (getClass() != obj.getClass()) {
 			return false;
 		}
 		FeedItem other = (FeedItem) obj;
 		return Objects.equals(guid, other.guid);
 	}
 
 	@Override
 	public int hashCode() {
 		return Objects.hashCode(guid);
 	}
 
 	public static enum Status {
 		UNREAD,
 		READ
 	}
 }
