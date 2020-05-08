 package sage.entity;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.OneToOne;
 
 @Entity(name="Tweet")
 public class Tweet {
 	private long id;
 	private String content;
 	private User author;
 	private Date time;
 	private Tweet origin = null;
 	private Long blogId = null;
 	private Set<Tag> tags = new HashSet<>();
 	private Collection<Comment> comments = new ArrayList<>();
 
 	public Tweet() {
 	}
 
 	public Tweet(String content, User author, Date time, Set<Tag> tags) {
 		this.content = content;
 		this.author = author;
 		this.time = time;
 		this.tags.addAll(tags);
 	}
 	
 	public Tweet(String content, User author, Date time, Tweet origin) {
 		this.content = content;
 		this.author = author;
 		this.time = time;
		tags.addAll(origin.getTags());
 		this.origin = origin;
 		if (origin.getOrigin() != null) {//XXX need better approach
 			throw new IllegalArgumentException("tweet's origin should not be nested!");
 		}
 	}
 	
 	public Tweet(String content, User author, Date time, Blog sourceBlog) {
 		this.content = content;
 		this.author = author;
 		this.time = time;
 		tags.addAll(sourceBlog.getTags());
 		blogId =  sourceBlog.getId();
 	}
 
 	@Id
 	@GeneratedValue
 	public long getId() {return id;}
 	public void setId(long id) {this.id = id;}
 
 	public String getContent() {return content;}
 	public void setContent(String content) {this.content = content;}
 
 	@ManyToOne(optional=false)
 	public User getAuthor() {return author;}
 	public void setAuthor(User author) {this.author = author;}
 
 	public Date getTime() {return time;}
 	public void setTime(Date time) {this.time = time;}
 
 	@OneToOne
 	public Tweet getOrigin() {return origin;}
 	public void setOrigin(Tweet origin) {this.origin = origin;}
 
 	public Long getBlogId() {return blogId;}
 	public void setBlogId(Long blogId) {this.blogId = blogId;}
 	
 	@ManyToMany(fetch=FetchType.EAGER)
 	public Set<Tag> getTags() {return tags;}
 	public void setTags(Set<Tag> tags) {this.tags = tags;}
 	
 	@OneToMany(mappedBy="source")
 	public Collection<Comment> getComments() {return comments;}
 	public void setComments(Collection<Comment> comments) {this.comments = comments;}
 	
 	@Override
 	public String toString() {
 		return author + ": " + content + tags;
 	}
 
 	@Override
 	public int hashCode() {
 		return Long.valueOf(id).hashCode();
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Tweet other = (Tweet) obj;
 		if (id != other.id)
 			return false;
 		return true;
 	}
 }
