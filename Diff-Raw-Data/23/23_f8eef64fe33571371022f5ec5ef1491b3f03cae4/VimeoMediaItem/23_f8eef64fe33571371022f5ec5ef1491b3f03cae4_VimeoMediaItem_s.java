 package eu.socialsensor.framework.abstractions.socialmedia.vimeo;
 
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import eu.socialsensor.framework.common.domain.MediaItem;
 
 /**
  * Class that holds the information regarding the vimeo media item
  * @author manosetro
  * @email  manosetro@iti.gr
  */
 public class VimeoMediaItem extends MediaItem {
 	private static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 	
 	public VimeoMediaItem(VimeoVideo video) throws Exception {
 		//url
 		super(new URL("http://vimeo.com/moogaloop.swf?clip_id="+video.id));
 		
 		//Id
		this.setId("Vimeo::"+video.id);
 		//SocialNetwork Name
 		this.setStreamId("Vimeo");
 		//Type 
 		this.setType("video");
 		//Time of publication
 		try {
 			Date date = formatter.parse(video.upload_date);
 			this.setPublicationTime(date.getTime());
 		}
 		catch(Exception e) {}
 		//PageUrl
 		this.setPageUrl(video.url);
 		//Thumbnail
 		this.setThumbnail(video.thumbnail_large);
 		//Title
 		this.setTitle(video.title);
 		//Description
 		this.setDescription(video.description);
 		//Tags
 		String tags = video.tags;
 		if(tags != null) {
 			this.setTags(tags.split(","));
 		}
 		//Description
 		this.setDescription(video.description);
 		//Popularity
 		likes = new Long(video.stats_number_of_likes);
 		views = new Long(video.stats_number_of_plays);
 		comments = new Long(video.stats_number_of_comments);
 		//Size
 		this.setSize(video.width, video.height);
 
 	}
 }
