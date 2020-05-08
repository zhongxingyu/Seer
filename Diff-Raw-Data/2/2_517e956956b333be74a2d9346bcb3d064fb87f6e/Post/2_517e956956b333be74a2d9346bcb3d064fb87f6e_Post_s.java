 package blog;
 
 import java.util.Date;
 import com.ibm.icu.text.DateFormat;
 import com.ibm.icu.text.SimpleDateFormat;
 
 public class Post {
 
 	private int id;
 	private String content;
 	private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
 	private Date date = new Date();
 	private String title;
 	private Author author;
 	private Category category;
 
 	public void setValues(String title_in, String content_in, Date date_in) {
 		title = title_in;
 		content = content_in;
 		date = date_in;
 	}
 	
 	//set the author of this post
 	public void setAuthor(Author author_in){
 		author = author_in;
 	}
 	
 	public void setCategory(Category category_in){
 		category = category_in;
 	}
 	
 	//return the id
 	public int getId(){
 		return id;
 	}
 	
 	public void setId(int id_in){
 		id = id_in;
 	}
 	
 	//return the Author object of this post
 	public Author getAuthor(){
 		return author;
 	}
 	
 	//return the Category object of this post
 	public Category getCategory(){
 		return category;
 	}
 
 	public String getTitle() {
 		return title;
 	}
 
 	/**
 	 * @param title
 	 *            the super title
 	 */
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	public String getContent() {
 		return content;
 	}
 
 	public String getDate() {
		return dateFormat.format(date);
 	}
 
 	public String getBlogPost() {
 		return "Frfattare: " + author.getName() + "\r\n" + "Kategori: "
 				+ category.getCategory() + "\r\n" + title + "\r\n" + getDate()
 				+ "\r\n" + content;
 	}
 
 	@Override
 	public String toString() {
 		return "Post [id=" + id + ", content=" + content + ", dateFormat="
 				+ dateFormat + ", date=" + date + ", title=" + title
 				+ ", author=" + author + ", category=" + category + "]";
 	}
 	
 
 }
