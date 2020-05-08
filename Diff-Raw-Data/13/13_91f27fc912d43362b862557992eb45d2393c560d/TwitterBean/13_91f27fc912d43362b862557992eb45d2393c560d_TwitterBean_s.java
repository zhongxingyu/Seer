 package twitter;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import javax.jdo.annotations.PersistenceCapable;
 import javax.jdo.annotations.Persistent;
 import javax.jdo.annotations.PrimaryKey;
 
 /**
  * Represents a single tweet stored in memcache
  * 
  * @author peter
  * 
  */
 @PersistenceCapable
 public class TwitterBean {
     @PrimaryKey
     @Persistent
     private String _tweetId;
 
     @Persistent
     private String _text;
 
     @Persistent
     private String _fromUser;
     
     @Persistent
     private String _fromUserName;
     
     @Persistent
     private String _profileImageUrl;
     
     @Persistent
     private Date _createdAt;
     
     /**
      * Creates a new TwitterBean object
      * 
      * @param tweetId	the id of the tweet
      * @param text	the text of the tweet		
      * @param fromUser	the id of the user who posted the tweet
      * @param fromUserName	the name of the user who posted the tweet
      * @param profileImageUrl	url of the user's profile image
      * @param createdAt	the date when the tweet was posted
      */
     public TwitterBean(String tweetId, String text, String fromUser,
     				  	String fromUserName, String profileImageUrl, 
     				  	String createdAt) {
     	this._tweetId = tweetId;
     	this._text = text;
     	this._fromUser = fromUser;
     	this._fromUserName = fromUserName;
     	this._profileImageUrl = profileImageUrl;
     	
     	DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
     	try {
 			this._createdAt = df.parse(createdAt);
 		} catch (ParseException e) {
 			e.printStackTrace();
 		} finally {
 			this._createdAt = new Date();
 		}    	
     }
     
     /**
      * Gets the id of the tweet
      * @return the tweet id
      */
     public String getTweetId() {
     	return this._tweetId;
     }
 
     /**
      * Gets the text of the tweet
      * @return the tweet text
      */
     public String getText() {
     	return this._text;
     }
     
     /**
      * Gets the id of the user who posted the tweet
      * @return the id of the user who posted the tweet 
      */
     public String getFromUser() {
     	return this._fromUser;
     }
     
     /**
      * Gets the name of the user who posted the tweet
      * @return the name of the user who posted the tweet
      */
     public String getFromUserName() {
     	return this._fromUserName;
     }
     
     /**
      * Gets the url of the user's profile image
      * @return the name of the user who posted the tweet
      */
     public String getProfileImageUrl() {
     	return this._profileImageUrl;
     }
     
     /**
      * Gets the url of the user's profile image
      * @return the name of the user who posted the tweet
      */
     public Date getCreatedAt() {
     	return this._createdAt;
     }
     
     public String toString() {
     	DateFormat dt = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
    	return "{id_str:\"" + this._tweetId +"\", text: \"" + this._text.replaceAll("\"", "\\\"") + "\", from_user: \"" + 
     			this._fromUser + "\", from_user_name:\"" + 
     			this._fromUserName + "\", profile_image_url: \"" + this._profileImageUrl + "\", created_at: \"" + dt.format(this._createdAt) + "\"}";
     }
 }
