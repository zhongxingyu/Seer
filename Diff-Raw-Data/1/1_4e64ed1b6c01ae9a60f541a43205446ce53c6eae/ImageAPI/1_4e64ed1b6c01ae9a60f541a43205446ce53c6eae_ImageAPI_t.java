 package com.github.jkschoen.jsma;
 
 import java.io.File;
 import java.io.IOException;
 import java.nio.file.Files;
 import java.security.InvalidKeyException;
 import java.security.NoSuchAlgorithmException;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.github.jkschoen.jsma.misc.JSMAUtils;
 import com.github.jkschoen.jsma.misc.JsmaLoggingFilter;
 import com.github.jkschoen.jsma.misc.SmugMugException;
 import com.github.jkschoen.jsma.model.Album;
 import com.github.jkschoen.jsma.model.Comment;
 import com.github.jkschoen.jsma.model.Image;
 import com.github.jkschoen.jsma.model.ImageEXIF;
 import com.github.jkschoen.jsma.model.ImageStats;
 import com.github.jkschoen.jsma.response.AlbumResponse;
 import com.github.jkschoen.jsma.response.CommentResponse;
 import com.github.jkschoen.jsma.response.ImageEXIFResponse;
 import com.github.jkschoen.jsma.response.ImageResponse;
 import com.github.jkschoen.jsma.response.ImageStatsResponse;
 import com.github.jkschoen.jsma.response.SMResponse;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.oauth.client.OAuthClientFilter;
 import com.sun.jersey.oauth.signature.OAuthParameters;
 import com.sun.jersey.oauth.signature.OAuthSecrets;
 
 public class ImageAPI extends BaseAPI{
 	static final Logger logger = LoggerFactory.getLogger(ImageAPI.class);
 	
 	private static final String UPLOAD_URL = "http://upload.smugmug.com/";
 	
 	private SmugMugAPI smugmug;
 	
 	protected ImageAPI(SmugMugAPI smugmug){
 		this.smugmug = smugmug;
 	}
 	
 	/**
 	 * Applies a watermark to the image.
 	 * 
 	 * @param imageId (required) The id for a specific album.
 	 * @param watermarkId (required) The id for a specific watermark.
 	 * @param pretty return formatted JSON that is easier to read
 	 * @param strict Enable strict error handling.
 	 * @return returns true if watermark was set
 	 * @throws SmugMugException
 	 */
 	public boolean applyWatermark(long imageId, long watermarkId, boolean pretty, boolean strict) throws SmugMugException {
 		logger.debug("applyWatermark() called");
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("ImageID", Long.toString(imageId));
 		params.put("WatermarkID", Long.toString(watermarkId));
 		SMResponse requestToken = SMResponse.callMethod(this.smugmug,SMResponse.class, "smugmug.images.applyWatermark", params, null, pretty, false, strict, false);
 		logger.debug("applyWatermark() result: "+(requestToken == null ? "null" : requestToken.toString()));
 		//if it does not throw an exception than it worked, so return true
 		return true;
 	}
 	
 	/**
 	 * Change the position of an image within an album.
 	 * 
 	 * @param imageId (required) The id for a specific album.
 	 * @param position (required) The position of the image (or video) within the album.
 	 * @param pretty return formatted JSON that is easier to read
 	 * @param strict Enable strict error handling.
 	 * @return true id the position is changed
 	 * @throws SmugMugException
 	 */
 	public boolean changePosition(long imageId, int position, boolean pretty, boolean strict) throws SmugMugException {
 		logger.debug("changePosition() called");
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("ImageID", Long.toString(imageId));
 		params.put("Position", Integer.toString(position));
 		SMResponse requestToken = SMResponse.callMethod(this.smugmug,SMResponse.class, "smugmug.images.changePosition", params, null, pretty, false, strict, false);
 		logger.debug("changePosition() result: "+(requestToken == null ? "null" : requestToken.toString()));
 		//if it does not throw an exception than it worked, so return true
 		return true;
 	}
 	
 	/**
 	 * Change the settings of an image.
 	 * 
 	 * Changable settings:
 	 * <ul>
 	 * 	<li>Album</li>
 	 * 	<li>Altitude</li>
 	 * 	<li>Caption</li>
 	 * 	<li>FileName</li>
 	 * 	<li>Hidden</li>
 	 * 	<li>Keywords</li>
 	 * 	<li>Latitude</li>
 	 * 	<li>Longitude</li>
 	 * </ul>
 	 * 
 	 * @param image the new image settings.
 	 * @param extras A comma separated string of additional attributes to return in the response. 
 	 *               NOTE: I do not think this does anything at all.
 	 * @param pretty return formatted JSON that is easier to read
 	 * @param sandboxed Forces URLs to a location with a crossdomain.xml file.
 	 * @param strict Enable strict error handling.
 	 * @return true if the settngs are successfully changed.
 	 * @throws SmugMugException
 	 * @throws IllegalAccessException 
 	 * @throws IllegalArgumentException 
 	 * @throws SecurityException 
 	 * @throws NoSuchFieldException 
 	 */
 	public Image changeSettings(Image image, String[] extras, boolean pretty, boolean sandboxed, boolean strict) throws SmugMugException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException{
 		logger.debug("changeSettings() called");
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("ImageID", Long.toString(image.getId()));
 		if(image.getAlbum() != null){
 			params.put("AlbumID", image.getAlbum().getId().toString());
 		}
 		if(image.getAltitude() != null){
 			params.put("Altitude", image.getAltitude().toString());
 		}
 		if(image.getCaption() != null){
 			params.put("Caption", image.getCaption());
 		}
 		if(image.getFileName() != null){
 			params.put("FileName", image.getFileName());
 		}
 		if(image.getHidden() != null){
 			params.put("Hidden", (image.getHidden() ? "true" : "false"));
 		}
 		if(image.getKeywords() != null){
 			params.put("Keywords", image.getKeywords());
 		}
 		if(image.getLatitude() != null){
 			params.put("Latitude", image.getLatitude().toString());
 		}
 		if(image.getLongitude() != null){
 			params.put("Longitude", image.getLongitude().toString());
 		}
 		
 		ImageResponse requestToken = SMResponse.callMethod(this.smugmug,ImageResponse.class, "smugmug.images.changeSettings", params, extras, pretty, sandboxed, strict, false);
 		logger.debug("changeSettings() result: "+(requestToken == null ? "null" : requestToken.toString()));
 		//if it does not throw an exception than it worked, so return true
 		if(image != null){
 			this.setExtras(image, requestToken.getImage(), extras);
 		}
 		return requestToken.getImage();
 	}
 	
 	/**
 	 * Collect an image into an album.
 	 * 
 	 * @param albumId The id for a specific album.
 	 * @param imageId The id for a specific image.
 	 * @param imageKey The key for a specific image.
 	 * @param extras A comma separated string of additional attributes to return in the response.
 	 * @param pretty return formatted JSON that is easier to read
 	 * @param strict Enable strict error handling.
 	 * @return return the collected image
 	 * @throws SmugMugException
 	 */
 	public Image collect(int albumId, long imageId, String imageKey, String[] extras, boolean pretty, boolean strict) throws SmugMugException {
 		logger.debug("collect() called");
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("AlbumID", Integer.toString(albumId));
 		params.put("ImageID", Long.toString(imageId));
 		params.put("ImageKey", imageKey);
 		ImageResponse requestToken = SMResponse.callMethod(this.smugmug,ImageResponse.class, "smugmug.images.collect", params, extras, pretty, false, strict, false);
 		logger.debug("collect() result: "+(requestToken == null ? "null" : requestToken.toString()));
 		return requestToken.getImage();
 	}
 	
 	/**
 	 * Add a comment to an image.
 	 * 
 	 * @param comment The comment to be added.
 	 * @param imageId The id for a specific image.
 	 * @param imageKey The key for a specific image.
 	 * @param extras array of extra fields to be populated.
 	 * @param pretty return formatted JSON that is easier to read
 	 * @param strict Enable strict error handling.
 	 * @return the added comment
 	 * @throws IllegalAccessException 
 	 * @throws IllegalArgumentException 
 	 * @throws SecurityException 
 	 * @throws NoSuchFieldException 
 	 */
 	public Comment commentAdd(Comment comment, long imageId, String imageKey, String[] extras, boolean pretty, boolean strict) throws SmugMugException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException{
 		logger.debug("commentAdd() called");
 		
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("ImageID" , Long.toString(imageId));
 		params.put("ImageKey" , imageKey);
 		params.put("Text", comment.getText());
 		if(comment.getRating() != null){
 			params.put("Rating", comment.getRating().toString());
 		}
 		
 		CommentResponse requestToken = SMResponse.callMethod(this.smugmug,CommentResponse.class, "smugmug.images.comment.add", params, extras, pretty, false, strict, false);
 		logger.debug("commentAdd() result: "+(requestToken == null ? "null" : requestToken.toString()));
 		comment.setId(requestToken.getComment().getId());
 		this.setExtras(comment, requestToken.getComment(), extras);
 		return comment;
 	}
 	
 	/**
 	 * Retrieve a list of comments for an image.
 	 * 
 	 * @param imageId The id for a specific album.
 	 * @param imageKey The key for a specific album.
 	 * @param password The password for the album.
 	 * @param sitePassword The site password for a specific user.
 	 * @param lastUpdated Return results where LastUpdated is after
 	 * @param pretty return formatted JSON that is easier to read
 	 * @param strict Enable strict error handling.
 	 * @return
 	 * @throws SmugMugException
 	 */
 	public List<Comment> commentsGet(long imageId, String imageKey, String password, String sitePassword, Date lastUpdated, boolean pretty, boolean strict) throws SmugMugException{
 		logger.debug("commentGet() called");
 		
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("ImageID" , Long.toString(imageId));
 		params.put("ImageKey" , imageKey);
 		
 		if(password != null){
 			params.put("Password", password);
 		}
 		if(sitePassword != null){
 			params.put("SitePassword", sitePassword);
 		}
 		if(lastUpdated != null){
 			params.put("LastUpdated", Long.toString(lastUpdated.getTime()));
 		}
 		
 		ImageResponse requestToken = SMResponse.callMethod(this.smugmug,ImageResponse.class, "smugmug.images.comments.get", params, null, pretty, false, strict, false);
 		logger.debug("commentGet() result: "+(requestToken == null ? "null" : requestToken.toString()));
 		return requestToken.getImage().getComments();
 	}
 
 	/**
 	 * Crop an image.
 	 * 
 	 * @param imageId (required) The id for a specific image.
 	 * @param height (required) The height of the crop.
 	 * @param width (required) The width of the crop.
 	 * @param x The x coordinate of the starting point.
 	 * @param y The y coordinate of the starting point.
 	 * @param pretty Return a more human friendly response.
 	 * @param strict Enable strict error handling.
 	 * @return
 	 * @throws SmugMugException  
 	 */
 	public boolean crop(long imageId, int height, int width, Integer x, Integer y, boolean pretty, boolean strict) throws SmugMugException {
 		logger.debug("crop() called");
 		
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("ImageID" , Long.toString(imageId));
 		params.put("Height" , Integer.toString(height));
 		params.put("Width" , Integer.toString(width));
 		
 		if(x != null){
 			params.put("X", x.toString());
 		}
 		if(y != null){
 			params.put("Y", y.toString());
 		}
 		
 		SMResponse requestToken = SMResponse.callMethod(this.smugmug,SMResponse.class, "smugmug.images.crop", params, null, pretty, false, strict, false);
 		logger.debug("crop() result: "+(requestToken == null ? "null" : requestToken.toString()));
 		return true;
 	}
 
 	/**
 	 * Delete an image.
 	 * 
 	 * @param imageId (required) The id for a specific image.
 	 * @param albumId The id for a specific album.
 	 * @param pretty Return a more human friendly response.
 	 * @param strict Enable strict error handling.
 	 * @return
 	 * @throws SmugMugException
 	 */
 	public boolean delete(long imageId, Long albumId, boolean pretty, boolean strict) throws SmugMugException {
 		logger.debug("delete() called");
 		
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("ImageID" , Long.toString(imageId));
 		
 		if(albumId != null){
 			params.put("AlbumID", albumId.toString());
 		}
 		
 		SMResponse requestToken = SMResponse.callMethod(this.smugmug,SMResponse.class, "smugmug.images.delete", params, null, pretty, false, strict, false);
 		logger.debug("delete() result: "+(requestToken == null ? "null" : requestToken.toString()));
 		return true;
 	}
 
 	/**
 	 * Retrieve a list of images for an album.
 	 * 
 	 * @param albumId The id for a specific album.
 	 * @param albumKey The key for a specific album.
 	 * @param customSize A custom size image to return.
 	 * @param password The password for the album.
 	 * @param sitePassword The site password for a specific user.
 	 * @param lastUpdated return results that where last updated after given time
 	 * @param extras array of extra fields to be populated.
 	 * @param pretty return formatted JSON that is easier to read
 	 * @param sandboxed Forces URLs to a location with a crossdomain.xml file.
 	 * @param strict Enable strict error handling.
 	 * @param heavy Returns a heavy response for this method.
 	 * @return
 	 * @throws SmugMugException
 	 */
 	public Album get(long albumId, String albumKey, String customSize, String password, String sitePassword, Date lastUpdated, String[] extras, boolean pretty, boolean sandboxed, boolean strict, boolean heavy) throws SmugMugException {
 		logger.debug("get() called");
 		
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("AlbumID" , Long.toString(albumId));
 		params.put("AlbumKey" , albumKey);
 		if(customSize != null){
 			params.put("CustomSize ", customSize);
 		}
 		if(password != null){
 			params.put("Password ", password);
 		}
 		if(sitePassword != null){
 			params.put("SitePassword ", sitePassword);
 		}
 		if(lastUpdated != null){
 			params.put("LastUpdated ", Long.toString(lastUpdated.getTime()));
 		}
 		
 		AlbumResponse requestToken = SMResponse.callMethod(this.smugmug,AlbumResponse.class, "smugmug.images.get", params, extras, pretty, sandboxed, strict, heavy);
 		logger.debug("get() result: "+(requestToken == null ? "null" : requestToken.toString()));
 		return requestToken.getAlbum();
 	}
 	
 	/**
 	 * Retrieve the EXIF data for an image.
 	 * 
 	 * @param imageId (required) The id for a specific image.
 	 * @param imageKey (required) The key for a specific image.
 	 * @param password The password for the album.
 	 * @param sitePassword The site password for a specific user.
 	 * @param pretty return formatted JSON that is easier to read
 	 * @param strict Enable strict error handling.
 	 * @return
 	 * @throws SmugMugException
 	 */
 	public ImageEXIF getEXIF(long imageId, String imageKey, String password, String sitePassword, boolean pretty, boolean strict) throws SmugMugException {
 		logger.debug("getEXIF() called");
 		
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("ImageID" , Long.toString(imageId));
 		params.put("ImageKey" , imageKey);
 		if(password != null){
 			params.put("Password ", password);
 		}
 		if(sitePassword != null){
 			params.put("SitePassword ", sitePassword);
 		}
 		
 		ImageEXIFResponse requestToken = SMResponse.callMethod(this.smugmug,ImageEXIFResponse.class, "smugmug.images.getEXIF", params, null, pretty, false, strict, false);
 		logger.debug("getEXIF() result: "+(requestToken == null ? "null" : requestToken.toString()));
 		return requestToken.getImage();
 	}
 	
 	/**
 	 * Retrieve the information for an image.
 	 * 
 	 * @param imageId (required) The id for a specific image.
 	 * @param imageKey (required) The key for a specific image.
 	 * @param customSize A custom size image to return.
 	 * @param password The password for the album.
 	 * @param sitePassword The site password for a specific user.
 	 * @param pretty return formatted JSON that is easier to read
 	 * @param sandboxed Forces URLs to a location with a crossdomain.xml file.
 	 * @param strict Enable strict error handling.
 	 * @return
 	 * @throws SmugMugException
 	 */
 	public Image getInfo(long imageId, String imageKey, String customSize, String password, String sitePassword, boolean pretty, boolean sandboxed, boolean strict) throws SmugMugException {
 		logger.debug("getInfo() called");
 		
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("ImageID" , Long.toString(imageId));
 		params.put("ImageKey" , imageKey);
 		if(customSize != null){
 			params.put("CustomSize ", customSize);
 		}
 		if(password != null){
 			params.put("Password ", password);
 		}
 		if(sitePassword != null){
 			params.put("SitePassword ", sitePassword);
 		}
 		
 		ImageResponse requestToken = SMResponse.callMethod(this.smugmug,ImageResponse.class, "smugmug.images.getInfo", params, null, pretty, sandboxed, strict, false);
 		logger.debug("getInfo() result: "+(requestToken == null ? "null" : requestToken.toString()));
 		return requestToken.getImage();
 	}
 
 	/**
 	 * Retrieve the statistics for an image.
 	 * 
 	 * @param imageId The id for a specific image.
 	 * @param month The month to retrieve statistics for.
 	 * @param year The year to retrieve statistics for.
 	 * @param pretty return formatted JSON that is easier to read
 	 * @param strict Enable strict error handling.
 	 * @return
 	 * @throws SmugMugException
 	 */
 	public ImageStats getStats(long imageId, int month, int year, boolean pretty, boolean strict) throws SmugMugException {
 		logger.debug("getStats() called");
 		
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("ImageID", Long.toString(imageId));
 		params.put("Month", Integer.toString(month));
 		params.put("Year", Integer.toString(year));
 		
 		ImageStatsResponse requestToken = SMResponse.callMethod(this.smugmug,ImageStatsResponse.class, "smugmug.images.getStats", params, null, pretty, false, strict, false);
 		logger.debug("getStats() result: "+(requestToken == null ? "null" : requestToken.toString()));
 		return requestToken.getStats();
 	}
 	
 	/**
 	 * Retrieve the URLs for an image.
 	 * 
 	 * @param imageId (required) The id for a specific image.
 	 * @param imageKey (required) The key for a specific image.
 	 * @param customSize A custom size image to return.
 	 * @param password The password for the album.
 	 * @param sitePassword The site password for a specific user.
 	 * @param pretty return formatted JSON that is easier to read
 	 * @param sandboxed Forces URLs to a location with a crossdomain.xml file.
 	 * @param strict Enable strict error handling.
 	 * @return
 	 * @throws SmugMugException
 	 */
 	public Image getURLs(long imageId, String imageKey, String customSize, String password, String sitePassword, boolean pretty, boolean sandboxed, boolean strict) throws SmugMugException {
 		logger.debug("getURLs() called");
 		
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("ImageID" , Long.toString(imageId));
 		params.put("ImageKey" , imageKey);
 		if(customSize != null){
 			params.put("CustomSize ", customSize);
 		}
 		if(password != null){
 			params.put("Password ", password);
 		}
 		if(sitePassword != null){
 			params.put("SitePassword ", sitePassword);
 		}
 		
 		ImageResponse requestToken = SMResponse.callMethod(this.smugmug,ImageResponse.class, "smugmug.images.getURLs", params, null, pretty, sandboxed, strict, false);
 		logger.debug("getURLs() result: "+(requestToken == null ? "null" : requestToken.toString()));
 		return requestToken.getImage();
 	}
 
 	/**
 	 * Remove a watermark from an image.
 	 * 
 	 * @param imageId The id for a specific album.
 	 * @param pretty return formatted JSON that is easier to read
 	 * @param strict Enable strict error handling.
 	 * @return true if the watermark is removed
 	 * @throws SmugMugException
 	 */
 	public boolean removeWatermark(long imageId, boolean pretty, boolean strict) throws SmugMugException {
 		logger.debug("removeWatermark() called");
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("ImageID", Long.toString(imageId));
 		SMResponse requestToken = SMResponse.callMethod(this.smugmug,SMResponse.class, "smugmug.images.removeWatermark", params, null, pretty, false, strict, false);
 		logger.debug("removeWatermark() result: "+(requestToken == null ? "null" : requestToken.toString()));
 		//if it does not throw an exception than it worked, so return true
 		return true;
 	}
 	
 	/**
 	 * Rotates an image.
 	 * 
 	 * @param imageId The id for a specific image.
 	 * @param degrees The degrees of rotation.
 	 *        Values:
 	 *        90 - Left
 	 *        180 - Down
 	 *        270 - Right
 	 * @param flip Mirror the image in the horizontal direction.
 	 * @param pretty return formatted JSON that is easier to read
 	 * @param strict Enable strict error handling.
 	 * @return
 	 * @throws SmugMugException
 	 */
 	public boolean rotate(long imageId, Integer degrees, Boolean flip, boolean pretty, boolean strict) throws SmugMugException {
 		logger.debug("rotate() called");
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("ImageID", Long.toString(imageId));
 		if(degrees != null){
 			params.put("Degrees", degrees.toString());
 		}
 		if (flip != null){
 			params.put("Flip", flip ? "true" : "false");
 		}
 		SMResponse requestToken = SMResponse.callMethod(this.smugmug,SMResponse.class, "smugmug.images.removeWatermark", params, null, pretty, false, strict, false);
 		logger.debug("rotate() result: "+(requestToken == null ? "null" : requestToken.toString()));
 		//if it does not throw an exception than it worked, so return true
 		return true;
 	}
 	
 	/**
 	 * Upload an image from a URL to an album.
 	 * 
 	 * @param albumId (required) The id for a specific album.
 	 * @param downloadURL (required) The URL for the image.
 	 * @param imageSettings various settings that can be set for the image
 	 *        Changable settings:
 	 *        <ul>
 	 *        	<li>Altitude</li>
 	 *          <li>ByteCount</li>
 	 *        	<li>Caption</li>
 	 *        	<li>FileName</li>
 	 *        	<li>Hidden</li>
 	 *        	<li>Keywords</li>
 	 *        	<li>Latitude</li>
 	 *        	<li>Longitude</li>
 	 *          <li>MD5Sum</li>
 	 *        </ul>
 	 * @param extras array of extra fields to be populated.
 	 * @param pretty return formatted JSON that is easier to read
 	 * @param strict Enable strict error handling.
 	 * @return
 	 * @throws SmugMugException
 	 */
 	public Image uploadFromURL(long albumId, String downloadURL, Image imageSettings, String[] extras, boolean pretty, boolean strict) throws SmugMugException {
 		logger.debug("uploadFromURL() called");
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("ImageID", Long.toString(albumId));
 		params.put("URL", downloadURL);
 		if (imageSettings != null){
 			if(imageSettings.getAltitude() != null){
 				params.put("Altitude", imageSettings.getAltitude().toString());
 			}
 			if (imageSettings.getSize() != null){
 				params.put("ByteCount", imageSettings.getSize().toString());
 			}
 			if (imageSettings.getCaption() != null){
 				params.put("Caption", imageSettings.getCaption());
 			}
 			if (imageSettings.getFileName() != null){
 				params.put("FileName", imageSettings.getFileName());
 			}
 			if (imageSettings.getHidden() != null){
 				params.put("Hidden", imageSettings.getHidden() ? "true" : "false");
 			}
 			if (imageSettings.getKeywords() != null){
 				params.put("Keywords", imageSettings.getKeywords());
 			}
 			if (imageSettings.getLatitude() != null){
 				params.put("Latitude", imageSettings.getLatitude().toString());
 			}
 			if (imageSettings.getLongitude() != null){
 				params.put("Longitude", imageSettings.getLongitude().toString());
 			}
 			if (imageSettings.getMd5Sum() != null){
 				params.put("MD5Sum", imageSettings.getMd5Sum());
 			}
 		}
 		ImageResponse requestToken = SMResponse.callMethod(this.smugmug,ImageResponse.class, "smugmug.images.uploadFromURL", params, null, pretty, false, strict, false);
 		logger.debug("uploadFromURL() result: "+(requestToken == null ? "null" : requestToken.toString()));
 		return requestToken.getImage();
 	}
 
 	/**
 	 * Crop the thumbnail of an image.
 	 * 
 	 * @param imageId The id for a specific image.
 	 * @param height The height of the crop.
 	 * @param width The width of the crop.
 	 * @param x The x coordinate of the starting point.
 	 * @param y The y coordinate of the starting point.
 	 * @param pretty return formatted JSON that is easier to read
 	 * @param strict Enable strict error handling.
 	 * @return
 	 * @throws SmugMugException
 	 */
 	public boolean zoomThumbnail(long imageId, int height, int width, Integer x, Integer y, boolean pretty, boolean strict) throws SmugMugException{
 		logger.debug("zoomThumbnail() called");
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("ImageID", Long.toString(imageId));
 		params.put("Height", Integer.toString(height));
 		params.put("Width", Integer.toString(width));
 		if(x != null){
 			params.put("X", x.toString());
 		}
 		if (y != null){
 			params.put("Y", y.toString());
 		}
 		SMResponse requestToken = SMResponse.callMethod(this.smugmug,SMResponse.class, "smugmug.images.zoomThumbnail", params, null, pretty, false, strict, false);
 		logger.debug("zoomThumbnail() result: "+(requestToken == null ? "null" : requestToken.toString()));
 		//if it does not throw an exception than it worked, so return true
 		return true;
 	}
 	
 	/**
 	 * Uploads an image to a specific album. If the imageId is replaced,
 	 * it will upload the image in place of the image with the 
 	 * corresponding imageId.
 	 * 
 	 * The MD5 calculated by us will be set in the returned image. This can be
 	 * useful if the caller wants to then do a second call getInfo() to see if
 	 * SmugMug made some modifications to the image changed it. While it does
 	 * not seem intuitive that they would make changes, it seems to happen
 	 * consistently with photo's that have the Orientation bit set. They alter
 	 * the image they store, which changes the MD5Sum. This causes an issue
 	 * if you are wanting to check if an image exists before you upload. 
 	 * 
 	 * The only solution that seems fool proof is to check the MD5Sum against
 	 * SmugMug's and if different download from SmugMug overwriting the one 
 	 * on the file system.
 	 * 
 	 * See http://www.dgrin.com/showthread.php?t=205411 for more.
 	 * 
 	 * @param image (Required) The image file to be uploaded.
 	 * @param albumId (Required) The id of the album to upload the photo (or video) to.
 	 * @param caption The caption for the image (or video).
 	 * @param keywords The keyword string for the image (or video).
 	 * @param hidden Hide the image (or video). Default false.
 	 * @param imageId The id of the image to replace. Can be null.
 	 * @param altitude The altitude at which the image (or video) was taken.
 	 * @param latitude The latitude at which the image (or video) was taken.
 	 * @param longitude The longitude at which the image (or video) was taken.
 	 * @param pretty Return a more human friendly response.
 	 * @return the details of the uploaded image
 	 * @throws IOException 
 	 * @throws NoSuchAlgorithmException 
 	 * @throws InvalidKeyException 
 	 * @throws SmugMugException 
 	 */
 	public Image upload(File image, long albumId, String caption, String keywords, 
 			Boolean hidden, Long imageId, Integer altitude, Float latitude, 
 			Float longitude, boolean pretty) throws IOException, InvalidKeyException, NoSuchAlgorithmException, SmugMugException{
 		logger.debug("upload() called");
 		byte[] imageBytes = Files.readAllBytes(image.toPath());
 		
 		WebResource resource = SmugMugAPI.CLIENT.resource(UPLOAD_URL);
 		
 		JsmaLoggingFilter logFilter = new JsmaLoggingFilter();
 	    resource.addFilter(logFilter);
 		
 		OAuthSecrets secrets = new OAuthSecrets().consumerSecret(smugmug.getConsumerSecret());
 	    OAuthParameters oauthParams = new OAuthParameters().consumerKey(smugmug.getCosumerKey()).
 	            signatureMethod("HMAC-SHA1").version("1.0");
 	    // Create the OAuth client filter
 	    OAuthClientFilter filter = new OAuthClientFilter(SmugMugAPI.CLIENT.getProviders(), oauthParams, secrets);
 	    // Add the filter to the resource
 	    if (smugmug.getToken() != null){
 	        secrets.setTokenSecret(smugmug.getToken().getSecret());
 	        oauthParams.token(smugmug.getToken().getId());
 	    }
 	    resource.addFilter(filter);
 	    String md5 = JSMAUtils.md5(imageBytes);
 		WebResource.Builder builder = resource.getRequestBuilder();
 		//User agent
 		builder = builder.header("User-Agent", smugmug.getAppName());
 		//API Version header
 		builder = builder.header("X-Smug-Version", "1.3.0");
 		//Response Type header
 		builder = builder.header("X-Smug-ResponseType", "JSON");
 		//Content-Length header
 		builder = builder.header("Content-Length", Long.toString(image.length()));
 		//Content-MD5 header
 		builder = builder.header("Content-MD5", md5);
 		//X-Smug-FileName header
 		builder = builder.header("X-Smug-FileName", image.getName());
 		//X-Smug-AlbumID header
 		builder = builder.header("X-Smug-AlbumID", Long.toString(albumId));
 		//X-Smug-Caption header
 		if(caption != null){
 			builder = builder.header("X-Smug-Caption", caption);
 		}
 		//X-Smug-Caption header
 		if(keywords != null){
 			builder = builder.header("X-Smug-Keywords", keywords);
 		}
 		//X-Smug-Hidden header
 		if(hidden != null){
 			builder = builder.header("X-Smug-Hidden", hidden.toString());
 		}
 		//X-Smug-ImageID header
 		if(imageId != null){
 			builder = builder.header("X-Smug-ImageID", imageId.toString());
 		}
 		//X-Smug-Altitude header
 		if(altitude != null){
 			builder = builder.header("X-Smug-Altitude", altitude.toString());
 		}
 		//X-Smug-Latitude header
 		if(latitude != null){
 			builder = builder.header("X-Smug-Latitude", latitude.toString());
 		}
 		//X-Smug-Latitude header
 		if(longitude != null){
 			builder = builder.header("X-Smug-Longitude", longitude.toString());
 		}
 		//X-Smug-Pretty header
 		if(pretty){
 			builder = builder.header("X-Smug-Pretty", Boolean.toString(pretty));
 		}
 	    
 		ImageResponse response = builder.post(ImageResponse.class, imageBytes);
 	    if (!"ok".equals(response.getStat())) {
 	        throw new SmugMugException(response);
 	    }
 	    response.getImage().setMd5Sum(md5);
	    logger.debug("upload() result: "+response.toString());
 		return response.getImage();
 	}
 
 }
