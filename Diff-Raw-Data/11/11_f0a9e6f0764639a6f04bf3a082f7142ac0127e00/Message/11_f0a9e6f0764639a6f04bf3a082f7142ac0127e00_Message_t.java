 package model;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
import javax.persistence.FetchType;
 import javax.persistence.Id;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.validation.constraints.Size;
 import org.hibernate.validator.constraints.Length;
 import play.Logger;
 import play.data.validation.Constraints.Required;
 import play.db.ebean.Model;
 
 @Entity(name = "messages")
 public class Message extends Model {
 
 	public static Finder<Long, Message> find = new Finder<Long, Message>(Long.class, Message.class); 	
 
 	@Id
 	private Long id;
 	
 	private Date timestamp = new Date(System.currentTimeMillis());
 	
 	@Column(length=1024)
 	String url;
 	
 	String contentType;
 	String contentEncoding;
 	
 	@Column(length=1024)
 	String description;
 	
 	@Column(length=1024)
 	String title;
 	
 	@JoinTable(name="messages_tags")
	@ManyToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER)
 	Set<Tag> tags;
 	
 	public Set<Tag> getTags() {
 		return tags;
 	}
 	
 	public void setTags(String tags) {
 		this.tags = new HashSet<Tag>();
 		final Message _this = this;
 		for(String tag: tags.split("\\s*\\,\\s*")) {
 			Tag t = Tag.find.where().eq("name", tag).findUnique();
 			if(t == null) {
 				t = new Tag(tag);
 			}
 			// t.save();
 			final Set m = t.getMessage();
 			t.setMessage(new HashSet<Message>(){{
 				addAll(m);
 				add(_this);
 			}});
 			this.tags.add(t);
 		}
 	}
 	
 	public void setTags(Set<Tag> tags1) {
 		for(Tag t: tags1) {
 			String tagsString = t.getName();
			if(!tagsString.trim().equals("")) {
				setTags(tagsString);
			}
 		}
 	}
 
 	int clicks = 0;
 	
 	private byte[] data;
 	
 	@ManyToOne
 	private Message parent;
 	
 	@OneToMany(mappedBy="parent")
 	List<Message> dependencies;
 
 	public Message() {
 		// TODO Auto-generated constructor stub
 	}
 	
 	public Message(String url, String contentType, byte[] data) {
 		super();
 		this.url = url;
 		this.contentType = contentType;
 		this.data = data;
 	}
 
 
 
 	public Long getId() {
 		return id;
 	}
 
 	
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	
 	public Date getTimestamp() {
 		return timestamp;
 	}
 
 	
 	public void setTimestamp(Date timestamp) {
 		this.timestamp = timestamp;
 	}
 
 	
 	public String getUrl() {
 		return url;
 	}
 
 	
 	public void setUrl(String url) {
 		this.url = url;
 	}
 
 	
 	public String getContentType() {
 		return contentType;
 	}
 
 	
 	public void setContentType(String contentType) {
 		this.contentType = contentType;
 	}
 
 	
 	public byte[] getData() {
 		return data;
 	}
 
 	
 	public void setData(byte[] data) {
 		this.data = data;
 	}
 
 	
 	public Message getParent() {
 		return parent;
 	}
 
 	
 	public void setParent(Message parent) {
 		this.parent = parent;
 	}
 
 	
 	public List<Message> getDependencies() {
 		return dependencies;
 	}
 
 	
 	public void setDependencies(List<Message> dependencies) {
 		this.dependencies = dependencies;
 	}
 	
 	public String getContentEncoding() {
 		return contentEncoding;
 	}
 	
 	public void setContentEncoding(String contentEncoding) {
 		this.contentEncoding = contentEncoding;
 	}
 	
 	
 	public String getDescription() {
 		return description;
 	}
 	
 	
 	public void setDescription(String description) {
 		this.description = description;
 	}
 	
 	
 	public String getTitle() {
 		return title;
 	}
 	
 	
 	public void setTitle(String title) {
 		this.title = title;
 	}
 	
 	
 	public int getClicks() {
 		return clicks;
 	}
 	
 	
 	public void setClicks(int clicks) {
 		this.clicks = clicks;
 	}
 
 	public String validate() {
 		if(url.isEmpty() || (!url.startsWith("http://") && !url.startsWith("file://") && !url.startsWith("https://") && !url.startsWith("ftp://"))) {
 			return "URL not valid";
 		}
 		return null;
 	}
 
 	@Override
 	public String toString() {
 		return "Message [id=" + id + ", timestamp=" + timestamp + ", url=" + url + ", contentType=" + contentType + ", data=byte[]" + ", parent=" + parent + ", dependencies=" + dependencies + "]";
 	}
 
 	private transient Map<String,List<String>> headerFields;
 	public Map<String, List<String>> getHeaderFields() {
 		return headerFields;
 	}
 	
 	public void setHeaderFields(Map<String, List<String>> headerFields) {
 		this.headerFields = headerFields;
 	}
 
 	public String getHostName() {
 		try {
 			return new URL(url).getHost();
 		} catch (MalformedURLException e) {
 			return "";
 		}
 	}	
 }
