 import java.util.HashMap;
 
 public class Issue {
 	
 	private Date pubDate;
 	
 	private HashMap<String, Scholar> editors;
 	
 	private HashMap<String, Scholar> reviewers;
 	
 	private HashMap<String, JournalArticle> articles;
 
 	public Date getPubDate() {
 		return pubDate;
 	}
 
 	public void setPubDate(Date pubDate) {
 		this.pubDate = pubDate;
 	}
 
 	public HashMap<String, Scholar> getEditors() {
 		return editors;
 	}
 
 	public void setEditors(HashMap<String, Scholar> editors) {
 		this.editors = editors;
 	}
 	
 	public void addEditor(String key, Scholar value) {
 		this.editors.put(key, value);
 	}
 	
 	public void removeEditor(String key) {
 		this.editors.remove(key);
 	}
 	
 	public HashMap<String, Scholar> getReviewers() {
 		return reviewers;
 	}
 
 	public void setReviewers(HashMap<String, Scholar> reviewers) {
 		this.reviewers = reviewers;
 	}
 	
 	public void addReviewer(String key, Scholar value) {
 		this.reviewers.put(key, value);
 	}
 	
 	public void removeReviewer(String key) {
 		this.reviewers.remove(key);
 	}
 
 	public HashMap<String, JournalArticle> getArticles() {
 		return articles;
 	}
 
 	public void setArticles(HashMap<String, JournalArticle> articles) {
 		this.articles = articles;
 	}
 	
 	public void addArticle(String key, JournalArticle value) {
 		this.articles.put(key, value);
 	}
 	
 	public void removeArticle(String key) {
 		this.articles.remove(key);
 	}
 	
}
