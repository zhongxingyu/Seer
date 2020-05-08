 package tumblib;
 
 import com.google.gson.JsonObject;
 
 /**
  * A regular post contains a title and a body.
  * @author Yeison Rodriguez
  *
  */
 public class RegularPost extends Post {
 	static int count;
 	String title;
 	String body;
 	
 	/**This wacky constructor is meant to be used by the {@link PostDeserializer}.*/
 	RegularPost(long id, String url, String urlWithSlug, PostType type, long date, String format,
 			int bookmarklets, int mobiles, String reblogKey, String slug, String[] tags,
 			String title, String body){
 		
		super(id, url, urlWithSlug, type, date, format, bookmarklets, mobiles, reblogKey, slug, tags);
 		count++;
 	}
 	
 	/**
 	 * Returns the title and body of this post.
 	 */
 	public String getContent(){
 		return getTitle() + "\n" + getBody();
 	}
 	
 	public String getTitle() {
 		return title;
 	}
 
 	void setTitle(String title) {
 		this.title = title;
 	}
 
 	public String getBody() {
 		return body;
 	}
 
 	void setBody(String body) {
 		this.body = body;
 	}
 	
 	/******Static methods for deserializing QuotePosts from JSON.******/
 	static String titleFromJson(JsonObject post){
 		return post.getAsJsonPrimitive("regular-title").getAsString();
 	}
 	
 	static String bodyFromJson(JsonObject post){
 		return post.getAsJsonPrimitive("regular-body").getAsString();
 	}
 
 }
