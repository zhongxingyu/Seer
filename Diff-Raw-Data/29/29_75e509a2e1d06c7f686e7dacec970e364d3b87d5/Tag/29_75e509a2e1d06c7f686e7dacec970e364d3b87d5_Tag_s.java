 package grids.entity;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 
 @Entity(name="Tag")
 public class Tag {
 	public static final long ROOT_ID = 1; 
 	private long id;
 	private String name;
 	private Set<Tag> children;
 	private Tag parent;
 	
 	private Collection<Tweet> tweets;
 	private Collection<Blog> blogs;
 	private Collection<Follow> follows;
 
 	public Tag() {
 	}
 	
 	public Tag(String name, Tag parent) {
 		this.name = name;
 		this.parent = parent;
 	}
 	
 	@Id
 	@GeneratedValue
 	public long getId() {return id;}
 	public void setId(long id) {this.id = id;}
 	
 	public String getName() {return name;}
 	public void setName(String name) {this.name = name;}
 	
 	@OneToMany(mappedBy="parent")
 	public Set<Tag> getChildren() {return children;}
 	public void setChildren(Set<Tag> children) {this.children = children;}
 	
 	@ManyToOne
 	public Tag getParent() {return parent;}
 	public void setParent(Tag parent) {this.parent = parent;}
 
 	@ManyToMany(mappedBy="tags")
 	public Collection<Tweet> getTweets() {return tweets;}
 	public void setTweets(Collection<Tweet> tweets) {this.tweets = tweets;}
 	
 	@ManyToMany(mappedBy="tags")
 	public Collection<Blog> getBlogs() {return blogs;}
 	public void setBlogs(Collection<Blog> blogs) {this.blogs = blogs;}
 	
 	@ManyToMany(mappedBy="tags")
 	public Collection<Follow> getFollows() {return follows;}
 	public void setFollows(Collection<Follow> follows) {this.follows = follows;}
 	
 	/**
 	 * @return a chain from itself to ancestors
 	 */
 	public List<Tag> chainUp() {
 		List<Tag> chain = new LinkedList<>();
 		Tag current = this;
 		while (current.getId() != ROOT_ID) {
 			chain.add(current);
 			current = current.getParent();
 		}
 		return chain;
 	}
 	
	public Set<Tag> descandants() {
 		Set<Tag> descanants = new HashSet<>();
 		for (Tag child : getChildren()) {
 			descanants.add(child);
			descanants.addAll(child.descandants());
 		}
 		return descanants;
 	}
 	
 	@Override
 	public String toString() {
 		return name;
 	}
 }
