 /**@author Yeison Rodriguez**/
 package tumblib;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Date;
 
 
 /**
  * The Post class encapsulates the data of a tumblr post, as well as accessor
  * methods for the data.  Typically, a tumblr post may be deserialized from
  * JSON to one of the Post subclasses.  However, if the type of the tumblr post 
  * is unknown, the Post class may be utilized instead.
  * @author Yeison Rodriguez
  */
 public class Post {
 	//Total number of Posts created.
 	static int totalCount;
 	
 	private long id;
 	private URL url;
 	private URL urlWithSlug;
 	private PostType type;
 	private Date date;
 	private String format;
 	private int bookmarklets;
 	private int mobiles;
 	private String reblogKey = "";
 	private String slug;
 	private String[] tags;
 	
 	/**
 	 * Very long constructor meant for instantiation by the PostDeserializer 
 	 * class. 
 	 */
 	Post(long id, String url, String urlWithSlug, PostType type, long date, String format, 
 			int bookmarklets, int mobiles, String reblogKey, String slug, String[] tags){
 		
 		setId(id);
 		setUrl(url);
 		setUrlWithSlug(urlWithSlug);
 		setType(type);
 		setDate(date);
 		setFormat(format);
 		setBookmarklets(bookmarklets);
 		setMobiles(mobiles);
 		setReblogKey(reblogKey);
 		setSlug(slug);
 		setTags(tags);
 		
 		totalCount++;	
 	}	
 
 	
 	/*Setter and Getter methods for instance variables. */
 	
 	/**
 	 * Set the unique tumblr id of this post.
 	 */
 	public void setId(long id) {
 		this.id = id;
 	}
 
 	/**
 	 * Retrieve the unique id that tumblr has assigned to this post.
 	 * @return The tumblr id of this post.
 	 */
 	public long getId() {
 		return id;
 	}
 
 	/**
 	 * Set the url where this post may be found.
 	 * @param urlString A string containig the url location of this post.
 	 */
 	public void setUrl(String urlString) {
 		try {
 			this.url = new URL(urlString);
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			System.err.println("Please verify that the URL format is correct.");
			System.err.println(urlString);
 		}
 	}
 
 	/**
 	 * Retrieve the url location of this post.
 	 * @return The url location of this post, as an URL object.
 	 */
 	public URL getUrl() {
 		return url;
 	}
 
 	/**
 	 * Set the alternate URL that uses the post's slug in it's address instead
 	 * of the post's id.
 	 * @param urlWithSlug The slug containing URL of the post.
 	 */
 	public void setUrlWithSlug(String urlWithSlug) {
 		try {
			this.urlWithSlug = new URL(urlWithSlug);
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 			System.err.println("Please verify that the URL format is correct.");
 			System.err.println(urlWithSlug);
 		}
 	}
 
 	/**
 	 * Retrieve the alternate "url with slug" of this post.
 	 * @return The url of this post, as a URL object.
 	 */
 	public URL getUrlWithSlug() {
 		return urlWithSlug;
 	}
 
 	/**
 	 * Set the tumblr type of this post (e.g. video, audio, quote, etc...).
 	 * @param type The PostType Enum.  Please see the {@link PostType} Enum for
 	 * possible options.
 	 */
 	public void setType(PostType type){
 		this.type = type;
 	}
 
 	/**
 	 * Retrieve the type of this post.
 	 * @return The type of this post as a PostType Enum.
 	 */
 	public PostType getType() {
 		return type;
 	}
 
 	/**
 	 * Set the post-date of this post.
 	 * @param date The post-date of this post as a unix time-stamp.
 	 */
 	public void setDate(long date) {
 		this.date = new Date(date);
 	}
 
 	/**
 	 * Retrieve the date of this post.
 	 * @return The date this post was posted on, as a Date object.
 	 */
 	public Date getDate() {
 		return date;
 	}
 
 	/**
 	 * Set the format of this post (e.g. html, plain-text).
 	 * @param format The original format of this post.
 	 */
 	public void setFormat(String format) {
 		this.format = format;
 	}
 
 	/**
 	 * Retrieve the format of this post.
 	 * @return A string indicating the format of this post.
 	 */
 	public String getFormat() {
 		return format;
 	}
 
 	/**
 	 * Set the number of times that this post has been bookmarklet.
 	 * @param bookmarklets The number of bookmarklets this post has received.
 	 */
 	public void setBookmarklets(int bookmarklets) {
 		this.bookmarklets = bookmarklets;
 	}
 
 	/**
 	 * Retrieve the number of times that this post has been bookmarkletted.
 	 * @return The number of times this post has been bookmarklet.
 	 */
 	public int getBookmarklets() {
 		return bookmarklets;
 	}
 
 	/**
 	 * Set the number of times this post has been read through the mobile access
 	 * point.
 	 * @param mobiles The quantity of reads through the mobile access point.
 	 */
 	public void setMobiles(int mobiles) {
 		this.mobiles = mobiles;
 	}
 
 	/**
 	 * Retrieve the number of time this post has been read via the mobile access
 	 * point.
 	 * @return The quantity of reads via the mobile access point.
 	 */
 	public int getMobiles() {
 		return mobiles;
 	}
 
 	/**
 	 * Set the tumblr reblog-key, which may be used to reblog this post via another
 	 * blog.
 	 * @param reblogKey The tumblr reblog-key of the post.
 	 */
 	public void setReblogKey(String reblogKey) {
 		this.reblogKey = reblogKey;
 	}
 
 	/**
 	 * Retrieve the reblog-key of this post, which may be used to reblog this
 	 * post via another tumblr blog.
 	 * @return The tumblr reblog-key of this post.
 	 */
 	public String getReblogKey() {
 		return reblogKey;
 	}
 
 	/**
 	 * Set the slug; a short description that may be used in the url of the 
 	 * post.
 	 * @param slug The slug of this post as a String.
 	 */
 	public void setSlug(String slug) {
 		this.slug = slug;
 	}
 
 	/**
 	 * Retrieve the slug of this post; a short description that may be used in 
 	 * the url of the post.
 	 * @return The slug of this post as a String.
 	 */
 	public String getSlug() {
 		return slug;
 	}
 
 	/**
 	 * Saves an array of Strings as the tags that pertain to this post.
 	 * @param tags An array of Strings that represent the tags of this post.
 	 */
 	public void setTags(String[] tags) {
 		this.tags = tags;
 		
 	}
 
 	/**
 	 * Retrieve that array of Strings that represent the tags of this post.
 	 * @return An array of Strings containing the tags assigned to this post.
 	 */
 	public String[] getTags() {
 		return tags;
 	}
 	
 	/**
 	 * Get the content of this post as a String.  The format will vary depending
 	 * on the post type.
 	 * @return The content of this post as a String.
 	 * @Override This method should be overriden by subclasses.**/
 	public String getContent(){
 		return "\tPost Type is Unknown; no content to return.";
 	}
 	
 }
