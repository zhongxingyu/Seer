 package by.itransition.fanfic.domain;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.CascadeType;
 import javax.persistence.ElementCollection;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 
 import org.apache.solr.analysis.LowerCaseFilterFactory;
 import org.apache.solr.analysis.SnowballPorterFilterFactory;
 import org.apache.solr.analysis.StandardTokenizerFactory;
 import org.hibernate.search.annotations.Analyzer;
 import org.hibernate.search.annotations.AnalyzerDef;
 import org.hibernate.search.annotations.Field;
 import org.hibernate.search.annotations.Indexed;
 import org.hibernate.search.annotations.Parameter;
 import org.hibernate.search.annotations.TokenFilterDef;
 import org.hibernate.search.annotations.TokenizerDef;
 

 @Indexed
 @AnalyzerDef(name = "FanficAnalyzer",
 	tokenizer = @TokenizerDef(factory = StandardTokenizerFactory.class),
 	filters = {
 		@TokenFilterDef(factory = LowerCaseFilterFactory.class),
 		@TokenFilterDef(factory = SnowballPorterFilterFactory.class, 
 			params = @Parameter(name = "language", value = "Russian")),
 		@TokenFilterDef(factory = SnowballPorterFilterFactory.class,
 			params = @Parameter(name = "language", value = "English"))
 })
 public class Fanfic {
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	private int id;
 
 	private double rating;
 
 	@Field
 	@Analyzer(definition="FanficAnalyzer")
 	private String name;
 
 	@Field
 	@Analyzer(definition="FanficAnalyzer")
 	private String description;
 
 	//@Field(bridge = @FieldBridge(impl=CollectionToCSVBridge.class))
 	@ElementCollection
 	private List<String> tags = new ArrayList<String>();
 
 	//@Field(bridge = @FieldBridge(impl=CollectionToCSVBridge.class))
 	@ElementCollection
 	private List<String> categories = new ArrayList<String>();
 
 	@OneToMany(cascade = CascadeType.ALL)
 	private List<Chapter> chapters = new ArrayList<Chapter>();
 
 	@OneToMany(cascade = CascadeType.ALL)
 	private List<Comment> comments = new ArrayList<Comment>();
 
 	@OneToMany(cascade = CascadeType.ALL)
 	private List<Vote> votes = new ArrayList<Vote>();
 
 	@ManyToOne
 	private User author;
 
 	public double getRating() {
 		return rating;
 	}
 
 	public void setRating(double rating) {
 		this.rating = rating;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public List <String> getTags() {
 		return tags;
 	}
 
 	public void setTags(List <String> tags) {
 		this.tags = tags;
 	}
 
 	public List <String> getCategories() {
 		return categories;
 	}
 
 	public void setCategories(List <String> categories) {
 		this.categories = categories;
 	}
 
 	public List <Chapter> getChapters() {
 		return chapters;
 	}
 
 	public void addChapter(Chapter chapter) {
 		chapters.add(chapter);
 	}
 
 	public void addComment(Comment comment) {
 		comments.add(comment);
 	}
 
 	public List<Comment> getComments() {
 		return comments;
 	}
 
 	public User getAuthor() {
 		return author;
 	}
 
 	public void setAuthor(User author) {
 		this.author = author;
 	}
 
 	public int getId() {
 		return id;
 	}
 	
 	public void makeVote(int rating, User user) {
 		Vote newVote = null;
 		for (Vote vote : votes) {
 			if (vote.getUser().equals(user)) {
 				newVote = vote;
 				break;
 			}
 		}
 		if (null == newVote) {
 			newVote = new Vote();
 			votes.add(newVote);
 		}
 		newVote.setUser(user);
 		newVote.setRating(rating);
 		this.rating = 0;
 		for (Vote vote : votes) {
 			this.rating += vote.getRating();
 		}
 		this.rating /= votes.size();
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((author == null) ? 0 : author.hashCode());
 		result = prime * result
 				+ ((categories == null) ? 0 : categories.hashCode());
 		result = prime * result
 				+ ((chapters == null) ? 0 : chapters.hashCode());
 		result = prime * result
 				+ ((comments == null) ? 0 : comments.hashCode());
 		result = prime * result
 				+ ((description == null) ? 0 : description.hashCode());
 		result = prime * result + id;
 		result = prime * result + ((name == null) ? 0 : name.hashCode());
 		long temp;
 		temp = Double.doubleToLongBits(rating);
 		result = prime * result + (int) (temp ^ (temp >>> 32));
 		result = prime * result + ((tags == null) ? 0 : tags.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Fanfic other = (Fanfic) obj;
 		return id == other.getId();
 	}
 
 	public List<Vote> getVotes() {
 		return votes;
 	}
 
 	public void setVotes(List<Vote> votes) {
 		this.votes = votes;
 	}
 
 	public void setChapters(List<Chapter> chapters) {
 		this.chapters = chapters;
 	}
 
 	public void setComments(List<Comment> comments) {
 		this.comments = comments;
 	}
 	
 	public Chapter getChapterById(int id) {
 		for (Chapter chapter : chapters) {
 			if (chapter.getId() == id) {
 				return chapter;
 			}
 		}
 		return null;
 	}
 
 }
