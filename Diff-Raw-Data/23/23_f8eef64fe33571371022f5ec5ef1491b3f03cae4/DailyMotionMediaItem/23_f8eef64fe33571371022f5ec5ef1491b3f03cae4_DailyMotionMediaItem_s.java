 package eu.socialsensor.framework.abstractions.socialmedia.dailymotion;
 
 import java.net.URL;
 
 import eu.socialsensor.framework.common.domain.Location;
 import eu.socialsensor.framework.common.domain.MediaItem;
 
 /**
  * Class that holds the information regarding the dailymotion video
  * @author manosetro
  * @email  manosetro@iti.gr
  *
  */
 public class DailyMotionMediaItem extends MediaItem {
 
 	public DailyMotionMediaItem(DailyMotionVideo video) throws Exception {
 		super(new URL(video.embed_url));
 		
 		//id
		this.setId("Dailymotion::" + video.id);
 		//SocialNetwork Name
 		this.setStreamId("Dailymotion");
 		//Type 
 		this.setType("video");
 		//Time of publication
 		this.setPublicationTime(1000 * video.created_time);
 		//PageUrl
 		this.setPageUrl(video.url);
 		//Thumbnail
 		this.setThumbnail(video.thumbnail_url);
 		//Title
 		this.setTitle(video.title);
 		//Tags
 		this.setTags(video.tags);
 		//Popularity
 		comments = new Long(video.comments_total);
 		views = new Long(video.views_total);
 		ratings = new Float(video.ratings_total);
 		//Location
 		double[] geoloc = video.geoloc;
 		if(geoloc != null && geoloc.length>0) {
 			Location location = new Location(geoloc[0], geoloc[1]);
 			this.setLocation(location);
 		}
 	
 	}
 
 }
