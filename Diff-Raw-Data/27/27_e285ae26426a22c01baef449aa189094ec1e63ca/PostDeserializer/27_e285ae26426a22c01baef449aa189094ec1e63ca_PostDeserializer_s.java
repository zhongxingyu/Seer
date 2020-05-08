 /**@author Yeison Rodriguez */
 package tumblib;
 
 
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import com.google.gson.*;
 
 /**
  * This is an implementation of the deserializer interface in the Gson library.  
  * It essentially converts tumblr posts into the Java Post objects defined by 
  * tumblib.  First, the program determines the type of a tumblr post.  Depending 
  * on the type, the appropriate kind of tumblib Post is instantiated.
  * <br><br>
  * The PostDeserializer implements the JsonDeserializer from the Gson library,
  * and implements the method "deserialize" defined by that interface.
  * @author Yeison Rodriguez
  *
  */
 
 public class PostDeserializer implements JsonDeserializer<Post> {
	//Hide the default constructor from the JavaDoc.
	PostDeserializer(){
		;
	}
 
 	/**
 	 * This is a special callback method that is utilized by the Gson
 	 * library to deserialize Json.  This method should not be used by the
 	 * user, and must be Public.  The JsonElement parameter "post" in this case 
 	 * must be a Json Array containing all the posts of interest.
 	 * @param post A Json Array containing tumblr posts.
 	 * @param typeOfT The class of Post.
 	 * @param context Special callback parameter used by Gson.
 	 * **/
 	public Post deserialize(JsonElement post, Type typeOfT,
 			JsonDeserializationContext context) throws JsonParseException {
 		
 		//jpo: JSON Post Object
 		JsonObject jpo =  post.getAsJsonObject();
 		
 		//Ugly approach at saving some space.
 		long id = id(jpo); String url = url(jpo); 
 		String urlWithSlug = urlWithSlug(jpo); long date = date(jpo); 
 		String format = format(jpo); int bookmarklets = bookmarklets(jpo); 
 		int mobiles = mobiles(jpo); String reblogKey = reblogKey(jpo);
 		String slug = slug(jpo); String[] tags = tags(jpo);
 		
 		/*The method "type" extracts the type of the post from its Json, and then
 		 * calls the method TypeCheck to determine the corresponding PostType
 		 * Enum of the tumblr post.*/
 		PostType postType = type(jpo);
 		
 		/*This switch uses the Enum to instantiate the appropriate Post subclass.
 		 *Any special data fields that are specific to one particular subclass
 		 *are handled under the corresponding case statement.*/
 		switch(postType){
 		case audio: 
 			String caption = AudioPost.captionFromJson(jpo);
 			String player = AudioPost.playerFromJson(jpo);
 			int plays = AudioPost.playsFromJson(jpo);
 			return new AudioPost(id, url, urlWithSlug, postType, date, format, bookmarklets, 
 					mobiles, reblogKey, slug, tags, caption, player, plays);
 		case conversation:
 			String convoTitle = ConvoPost.titleFromJson(jpo);
 			String convoText = ConvoPost.textFromJson(jpo);
 			return new ConvoPost(id, url, urlWithSlug, postType, date, format, bookmarklets, 
 					mobiles, reblogKey, slug, tags, convoTitle, convoText);
 		case link:
 			String linkText = LinkPost.textFromJson(jpo);
 			String linkUrl = LinkPost.urlFromJson(jpo);
 			String linkDescription = LinkPost.descriptionFromJson(jpo);
 			return new LinkPost(id, url, urlWithSlug, postType, date, format, bookmarklets, 
 					mobiles, reblogKey, slug, tags, linkText, linkUrl, linkDescription);
 		case photo:
 			String photoCaption = PhotoPost.captionFromJson(jpo);
 			String[] photoUrls = PhotoPost.urlsFromJson(jpo);
 			TumblrPhoto[] photoArray = PhotoPost.photosFromJson(jpo);
 			return new PhotoPost(id, url, urlWithSlug, postType, date, format, bookmarklets, 
 					mobiles, reblogKey, slug, tags, photoCaption, photoUrls, photoArray);
 		case quote:
 			String quoteText = QuotePost.textFromJson(jpo);
 			String quoteSource = QuotePost.sourceFromJson(jpo);
 			return new QuotePost(id, url, urlWithSlug, postType, date, format, bookmarklets, 
 					mobiles, reblogKey, slug, tags, quoteText, quoteSource);
 		case regular:
 			String title = RegularPost.titleFromJson(jpo);
 			String body = RegularPost.bodyFromJson(jpo);
 			return new RegularPost(id, url, urlWithSlug, postType, date, format, bookmarklets, 
 					mobiles, reblogKey, slug, tags, title, body);
 		case video: 
 			String videoSource = VideoPost.sourceFromJson(jpo);
 			String videoCaption = VideoPost.captionFromJson(jpo);
 			String videoPlayer = VideoPost.playerFromJson(jpo);
 			return new VideoPost(id, url, urlWithSlug, postType, date, format, bookmarklets, 
 					mobiles, reblogKey, slug, tags, videoSource, videoCaption, videoPlayer);
 		default: return new Post(id, url, urlWithSlug, postType, date, format, bookmarklets, mobiles, 
 					reblogKey, slug, tags);
 		}
 	}
 	
 	/**
 	 * Extracts the type of the post from its Json, and then
 	 * calls the method TypeCheck to determine the corresponding PostType
 	 * Enum of the tumblr post.
 	 * @return The type of the post as a PostType object.**/
 	PostType type(JsonObject post){
 		String typeString = post.getAsJsonPrimitive("type").getAsString();
 		return typeCheck(typeString);
 	}
 	
 	/**
 	 * Determines the PostType of the post.
 	 * @param type The json string corresponding to the post-type.
 	 * @return The PostType Enum that represents the tumblr post-type.
 	 */
 	PostType typeCheck(String type){
 		return PostType.valueOf(type);
 	}
 	
 	/**
 	 * Parses the Json post object for the unique id assigned by tumblr.
 	 * @return The unique id number of the post as a long.**/
 	long id(JsonObject post){
 		return post.getAsJsonPrimitive("id").getAsLong();
 	}
 	
 	/**
 	 * Parses the Json post object for the url of tumblr post.
 	 * @return The URL of the post as a String.**/
 	String url(JsonObject post){
 		return post.getAsJsonPrimitive("url").getAsString();
 	}
 	
 	/**
 	 * Parses the Json post object for the url of the post including the slug.
 	 * @return The URL of the post with its slug as a string.**/
 	String urlWithSlug(JsonObject post){
 		return post.getAsJsonPrimitive("url-with-slug").getAsString();
 	}
 	
 	
 	/**
 	 * Parses the Json post object for unix-timestamp of the post.
 	 * @return The date as a long.  The long represents a unix-timestamp: 
 	 * milliseconds since January 1, 1970, 00:00:00 GMT**/
 	long date(JsonObject post){
 		//Dates from the tumblr read api are represented in seconds not millis.
 		return post.getAsJsonPrimitive("unix-timestamp").getAsLong()*1000;
 	}
 	
 	/**
 	 * Parses the Json post object for the format of the tumblr post.
 	 * @param post A tumblr post in Json form.
 	 * @return The format of this post as a String.
 	 */
 	String format(JsonObject post){
 		return post.getAsJsonPrimitive("format").getAsString();
 	}
 	
 	/**@return The number of times this post has been bookmarklet as int. **/
 	int bookmarklets(JsonObject post){
 		return post.getAsJsonPrimitive("bookmarklet").getAsInt();
 	}
 	
 	/**@return The number of times thist post has been read via the mobile
 	 * access point.**/
 	int mobiles(JsonObject post){
 		return post.getAsJsonPrimitive("mobile").getAsInt();
 	}
 	
 	/**@return The reblog-key of the post as a String.**/
 	String reblogKey(JsonObject post){
 		return post.getAsJsonPrimitive("reblog-key").getAsString();
 	}
 	
 	/**@return The slug for the url as string.**/
 	String slug(JsonObject post){
 		return post.getAsJsonPrimitive("slug").getAsString();
 	}
 	
 	/**@return The tags that this post has been categorized under, as an array 
 	 * of strings.**/
 	String [] tags(JsonObject post){
 		Iterator<JsonElement> tagIterator = null;
 		try{
 			tagIterator = post.getAsJsonArray("tags").iterator();
 		}catch(NullPointerException e){
 			//System.err.println("  Post " + post.getAsJsonPrimitive("id") +" does not have tags.");
 			return null;
 		}
 		ArrayList<String> tags = new ArrayList<String>();
 		
 		while(tagIterator.hasNext()){
 			tags.add(tagIterator.next().getAsString());
 		}
 		
 		String[] tagsArray = {};
 		tagsArray = tags.toArray(tagsArray);
 		
 		return tagsArray;
 	}
 	
 }
