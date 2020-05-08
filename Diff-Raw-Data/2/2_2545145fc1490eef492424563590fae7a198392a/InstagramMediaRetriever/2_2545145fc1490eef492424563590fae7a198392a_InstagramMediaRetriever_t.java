 package eu.socialsensor.framework.retrievers.socialmedia.instagram;
 
 import java.net.URL;
 import java.util.List;
 
 import org.jinstagram.Instagram;
 import org.jinstagram.InstagramOembed;
 import org.jinstagram.auth.model.Token;
 import org.jinstagram.entity.common.Caption;
 import org.jinstagram.entity.common.ImageData;
 import org.jinstagram.entity.common.Images;
 import org.jinstagram.entity.media.MediaInfoFeed;
 import org.jinstagram.entity.oembed.OembedInformation;
 import org.jinstagram.entity.users.feed.MediaFeedData;
 import org.jinstagram.exceptions.InstagramException;
 
 import eu.socialsensor.framework.common.domain.Location;
 import eu.socialsensor.framework.common.domain.MediaItem;
 import eu.socialsensor.framework.retrievers.socialmedia.MediaRetriever;
 
 
 /**
  * The retriever that implements the Instagram simplified retriever
  * @author manosetro
  * @email  manosetro@iti.gr
  */
 public class InstagramMediaRetriever  implements MediaRetriever {
 
 	private Instagram instagram = null;
 	private InstagramOembed instagramOembed = null;
 	
 	public InstagramMediaRetriever(String secret, String token) {
 		Token instagramToken = new Token(token,secret); 
 		this.instagram = new Instagram(instagramToken);
 		this.instagramOembed = new InstagramOembed();
 	}
 	
 	private String getMediaId(String url) {
 		try {
 			OembedInformation info = instagramOembed.getOembedInformation(url);
 			return info.getMediaId();
 		} catch (InstagramException e) {
 
 		}
 		return null;
 	}
 	
 	public MediaItem getMediaItem(String sid) {
 		try {
 			String id = getMediaId("http://instagram.com/p/"+sid);
 			
 			MediaInfoFeed mediaInfo = instagram.getMediaInfo(id);
 			if(mediaInfo != null) {
 				MediaFeedData mediaData = mediaInfo.getData();
 				Images images = mediaData.getImages();
 				
 				ImageData standardUrl = images.getStandardResolution();
 				String url = standardUrl.getImageUrl();
 				
 				MediaItem mediaItem = new MediaItem(new URL(url));
 				
 				ImageData thumb = images.getThumbnail();
 				String thumbnail = thumb.getImageUrl();
 				
 				String mediaId = "Instagram#" + mediaData.getId();
 				List<String> tags = mediaData.getTags();
 				
 				Caption caption = mediaData.getCaption();
 				String title = caption.getText();
 				
				Long publicationTime = new Long(1000*Long.parseLong(mediaData.getCreatedTime()));
 				
 				//id
 				mediaItem.setId(mediaId);
 				//SocialNetwork Name
 				mediaItem.setStreamId("Instagram");
 				//Reference
 				mediaItem.setRef(id);
 				//Type 
 				mediaItem.setType("image");
 				//Time of publication
 				mediaItem.setPublicationTime(publicationTime);
 				//PageUrl
 				mediaItem.setPageUrl(url);
 				//Thumbnail
 				mediaItem.setThumbnail(thumbnail);
 				//Title
 				mediaItem.setTitle(title);
 				//Tags
 				mediaItem.setTags(tags.toArray(new String[tags.size()]));
 				//Popularity
 				mediaItem.setLikes(new Long(mediaData.getLikes().getCount()));
 				mediaItem.setComments(new Long(mediaData.getComments().getCount()));
 				//Location
 				org.jinstagram.entity.common.Location geoLocation = mediaData.getLocation();
 				if(geoLocation != null) {
 					double latitude = geoLocation.getLatitude();
 					double longitude = geoLocation.getLongitude();
 					
 					Location location = new Location(latitude, longitude);
 					location.setName(geoLocation.getName());
 					mediaItem.setLocation(location);
 				}
 				//Size
 				ImageData standard = images.getStandardResolution();
 				if(standard!=null) {
 					int height = standard.getImageHeight();
 					int width = standard.getImageWidth();
 					mediaItem.setSize(width, height);
 				}
 				
 				return mediaItem;
 			}
 		} catch (Exception e) {
 		
 		} 
 
 		
 		return null;
 	}
 
 }
