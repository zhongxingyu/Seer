 package br.ufmg.dcc.vod.spiderpig.jobs.youtube.api;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import br.ufmg.dcc.vod.spiderpig.jobs.QuotaException;
 import br.ufmg.dcc.vod.spiderpig.jobs.Requester;
 
 import com.google.gdata.client.youtube.YouTubeService;
 import com.google.gdata.data.DateTime;
 import com.google.gdata.data.Link;
 import com.google.gdata.data.extensions.Comments;
 import com.google.gdata.data.geo.impl.GeoRssWhere;
 import com.google.gdata.data.media.mediarss.MediaDescription;
 import com.google.gdata.data.youtube.CommentEntry;
 import com.google.gdata.data.youtube.CommentFeed;
 import com.google.gdata.data.youtube.VideoEntry;
 import com.google.gdata.data.youtube.YouTubeMediaGroup;
 import com.google.gdata.data.youtube.YtRating;
 import com.google.gdata.data.youtube.YtStatistics;
 import com.google.gdata.util.ServiceException;
 import com.google.gson.Gson;
 
 public class VideoAPIRequester implements Requester<byte[]> {
 	
	private static final int QUOTA_ERR = 403;
 	private YouTubeService service;
 
 	public VideoAPIRequester(String appName, String devKey) {
 		this.service = new YouTubeService(appName, devKey);
 	}
 	
 	@Override
 	public byte[] performRequest(String crawlID) 
 			throws QuotaException, ServiceException, IOException {
 		try {
 			VideoEntry videoEntry = service.getEntry(
 					new URL("http://gdata.youtube.com/feeds/api/videos/" + 
 							crawlID), VideoEntry.class);
 			Map<String, String> videoJson = new LinkedHashMap<String, String>();
 			
 			String title = videoEntry.getTitle().getPlainText();
 			String author = videoEntry.getAuthors().get(0).getName();
 			
 			videoJson.put("title", title);
 			videoJson.put("author", author);
 			
 			YouTubeMediaGroup mediaGroup = videoEntry.getMediaGroup();
 			
 			MediaDescription mediaDesc = mediaGroup.getDescription();
 			String description = mediaDesc == null ? null : 
 				mediaDesc.getPlainTextContent();
 			String category = mediaGroup.getCategories().iterator().
 					next().getLabel();
 			long duration = mediaGroup.getDuration() == null ? 
 					-1 : mediaGroup.getDuration();
 			
 			videoJson.put("description", description);
 			videoJson.put("category", category);
 			videoJson.put("duration", "" + duration);
 			
 			GeoRssWhere location = videoEntry.getGeoCoordinates();
 			double latitude = -1;
 			double longitude = -1;
 			if(location != null) {
 				latitude = location.getLatitude();
 				longitude = location.getLongitude();
 			}
 			
 			videoJson.put("latitude", "" + latitude);
 			videoJson.put("longitude", "" + longitude);
 			
 			YtRating ytRating = videoEntry.getYtRating();
 			YtStatistics stats = videoEntry.getStatistics();
 			
 			long viewCount = -1;
 			long favCount = -1;
 			long likes = -1;
 			long dislikes = -1;
 			
 			if (stats != null) {
 				viewCount = stats.getViewCount();
 				favCount = stats.getFavoriteCount();
 			}
 			if (ytRating != null) {
 				likes = ytRating.getNumLikes();
 				dislikes = ytRating.getNumDislikes();
 			}
 			
 			videoJson.put("view", "" + viewCount);
 			videoJson.put("favorites", "" + favCount);
 			videoJson.put("likes", "" + likes);
 			videoJson.put("dislikes", "" + dislikes);
 			
 			Comments commentsObj = videoEntry.getComments();
 			boolean hasTotalComments = false;
 			long commentNum = 1;
 			if (commentsObj != null) {
 				Link commentLink = commentsObj.getFeedLink();
 				while (commentLink != null) {
 					String commentUrl = commentLink.getHref();
 					CommentFeed commentFeed = service.getFeed(
 							new URL(commentUrl), CommentFeed.class);
 					commentFeed.setItemsPerPage(1000);
 					
 					
 					if (!hasTotalComments) {
 						int totalResults = commentFeed.getTotalResults();
 						hasTotalComments = true;
 						videoJson.put("comments", "" + totalResults);
 					}
 					
 					for(CommentEntry comment : commentFeed.getEntries()) {
 						String[] split = comment.getId().split("\\/");
 						String id = split[split.length - 1];
 						String commentAuthor = comment.getAuthors().
 								get(0).getName();
 						String commentText = comment.getPlainTextContent();
 						DateTime commentDate = comment.getUpdated();
 						boolean isSpam = comment.hasSpamHint();
 						
 						videoJson.put("comment-" + commentNum + "-id", id);
 						videoJson.put("comment-" + commentNum + "-author", 
 								commentAuthor);
 						videoJson.put("comment-" + commentNum + "-text", 
 								commentText);
 						videoJson.put("comment-" + commentNum + "-date", 
 								commentDate.toString());
 						videoJson.put("comment-" + commentNum + "-spam", 
 								""+isSpam);
 						commentNum += 1;
 					}
 					
 					commentLink = commentFeed.getLink("next", 
 							"application/atom+xml");
 				}
 				
 				videoJson.put("comment-numdown", "" + (commentNum - 1));
 			}
 			
 			Gson gson = new Gson();
 			String json = gson.toJson(videoJson);
 			return json.getBytes();
 		} catch (ServiceException e) {
			int code = e.getHttpErrorCodeOverride();
			if (code == QUOTA_ERR) {
 				throw new QuotaException(e);
 			} else {
 				throw e;
 			}
 		} catch (IOException e) {
 			throw e;
 		}
 	}
 }
