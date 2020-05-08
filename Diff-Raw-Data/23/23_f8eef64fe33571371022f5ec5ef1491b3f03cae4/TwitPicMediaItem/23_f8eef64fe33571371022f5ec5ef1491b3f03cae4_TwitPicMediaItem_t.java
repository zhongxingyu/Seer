 package eu.socialsensor.framework.abstractions.socialmedia.twitpic;
 
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import eu.socialsensor.framework.common.domain.MediaItem;
 
 /**
  * Class that holds the information regarding the twitpic media item
  * @author manosetro
  * @email  manosetro@iti.gr
  */
 public class TwitPicMediaItem extends MediaItem {
 
 	private static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 	
 	private static String urlBase = "http://d3j5vwomefv46c.cloudfront.net/photos/large/";
 	private static String thumbBase = "http://d3j5vwomefv46c.cloudfront.net/photos/thumb/";
 	private static String pageBase = "http://twitpic.com/";
 	
 	public TwitPicMediaItem(TwitPicImage image) throws Exception {
 		super(new URL(urlBase + image.id + "." + image.type));
 		
 		//Id
		this.setId("Twitpic#"+image.id);
 		//SocialNetwork Name
 		this.setStreamId("Twitpic");
 		//Type 
 		this.setType("image");
 		//Time of publication
 		try {
 			Date date = formatter.parse(image.timestamp);
 			this.setPublicationTime(date.getTime());
 		}
 		catch(Exception e){}
 		//PageUrl
 		this.setPageUrl(pageBase + image.short_id);
 		//Thumbnail
 		this.setThumbnail(thumbBase + image.id + "." + image.type);
 		//Title
 		this.setTitle(image.message);
 		//Tags
 		if(image.tags != null) {
 			this.setTags(image.tags.split(","));
 		}
 		//Popularity
 		comments = new Long(image.number_of_comments);
 		views = new Long(image.views);
 		//Size
 		this.setSize(image.width, image.height);
 		
 	}
 
 	
 }
