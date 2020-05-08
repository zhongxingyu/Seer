 package benzawacki.dao;
 
 import com.google.appengine.api.users.User;
 import com.google.gson.annotations.Expose;
 
 public class Article extends AbstractDatastore<Article>{
 	
 	public static final String KIND = "Articles";
 	
 	@Expose private String authorName = "";
 	@Expose private User author;	
 	@Expose private String displayDate = "";
 	@Expose private String title;
 	@Expose private String summary;
 	@Expose private String content;
 	@Expose private String tags;
 	@Expose private String titleImageKey;
 	
 	
 	public Article(){
 		super(Article.class, KIND);
		setAuthor();
 	}
 
 	public Article(String title, String summary, String content, String tags, String titleImageKey){
 		super(Article.class, KIND);
 		this.title = title;
 		this.summary = summary;
 		this.content = content;
 		this.tags = tags;
 		this.titleImageKey = titleImageKey;	
 		setAuthor();
 	}
 	
 	public Article(String title, String summary, String content, String tags, String titleImageKey, String authorName){		
 		super(Article.class, KIND);
 		this.title = title;
 		this.summary = summary;
 		this.content = content;
 		this.tags = tags;
 		this.titleImageKey = titleImageKey;
 		this.authorName = authorName;	
 		setAuthor();
 	}
 	
 	
 	public void setAuthor(){		
 		this.author = this.createdBy;
 		if (this.authorName.isEmpty()){
 			this.authorName = this.author.getNickname().split("@")[0];
 		}
 	}
 	
 	public String getAuthorName() {
 		return authorName;
 	}
 
 	public void setAuthorName(String authorName) {
 		this.authorName = authorName;
 	}
 	
 	public User getAuthor() {
 		return author;
 	}
 
 	public void setAuthor(User author) {
 		this.author = author;
 	}
 
 	public String getTitle() {
 		return title;
 	}
 
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	public String getSummary() {
 		return summary;
 	}
 
 	public void setSummary(String summary) {
 		this.summary = summary;
 	}
 
 	public String getContent() {
 		return content;
 	}
 
 	public void setContent(String content) {
 		this.content = content;
 	}
 
 	public String getTags() {
 		return tags;
 	}
 
 	public void setTags(String tags) {
 		this.tags = tags;
 	}
 
 	public String getTitleImageKey() {
 		return titleImageKey;
 	}
 
 	public void setTitleImageKey(String titleImageKey) {
 		this.titleImageKey = titleImageKey;
 	}
 
 	public String getDisplayDate() {
 		return displayDate;
 	}
 
 	public void setDisplayDate(String displayDate) {
 		this.displayDate = displayDate;		
 	}
 
 //	public Key getKey(String id){
 //		return KeyFactory.createKey("Articles", id);
 //	}
 }
