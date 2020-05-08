 package core;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.concurrent.TimeUnit;
 
 import json.SongRespond;
 
 import com.google.gdata.client.youtube.YouTubeQuery;
 import com.google.gdata.client.youtube.YouTubeService;
 import com.google.gdata.data.youtube.VideoEntry;
 import com.google.gdata.data.youtube.VideoFeed;
 import com.google.gdata.data.youtube.YouTubeMediaGroup;
 import com.google.gdata.util.AuthenticationException;
 import com.google.gdata.util.ServiceException;
 import com.google.gdata.data.youtube.YouTubeMediaContent;
 import com.google.gdata.data.media.MediaFeed;
 import com.google.gdata.client.media.*;
 
 
 public class YTServiceSingletone {
 	private static YTServiceSingletone youtubeServ = null;
 	private String dev_key = "AI39si46UMrVM0Ebtm2DiIn9lhwCC87p3W0cpNElF15fdAqVuvWE-ld2ip8Z-EVSuUNCBgSvlRHk6xFhxkknCWuY7TCkfvTrPA";
 	private String URL_VIDEO_FEED = "http://gdata.youtube.com/feeds/api/videos";
 	private YouTubeService service;
 	
 	public static YTServiceSingletone getInstance(){
 		if (youtubeServ == null){
 			youtubeServ = new YTServiceSingletone();
 			try {
 				youtubeServ.InitService();
 			} catch (AuthenticationException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		
 		return youtubeServ;
 	}
 	
 	private void InitService() throws AuthenticationException{
 		service = new YouTubeService("Demo",dev_key);
 		//service.setUserCredentials("list.to.playlist@gmail.com", "prettyeasyone");
 		//there is no need to login into youtube. simple querying is available for everyone
 	}
 	
 	private String GetVideoID(VideoEntry videoEntry){
 		YouTubeMediaGroup mediaGroup = videoEntry.getMediaGroup();
 		System.out.println("Video ID: " + mediaGroup.getVideoId());
 		return mediaGroup.getVideoId();
 	}
 	
 	private VideoFeed SearchVideo(String searchStr) throws IOException, ServiceException{
 		YouTubeQuery query = new YouTubeQuery(new URL(URL_VIDEO_FEED));
 		query.setOrderBy(YouTubeQuery.OrderBy.RELEVANCE);
 		query.setFullTextQuery(searchStr);
 		
 		query.setSafeSearch(YouTubeQuery.SafeSearch.NONE);
 		VideoFeed videoFeed;
 		
 		videoFeed = service.query(query, VideoFeed.class);
 		//TODO ���� ������� ������ ��������� ������, ����� �������� ����������� �� duration
 		//return GetVideoID(videoFeed.getEntries().get(0));
 		return videoFeed;
 	}	
 	
 	public SongRespond GetSongRespong(String searchStr) throws IOException, ServiceException, IndexOutOfBoundsException{
 		SongRespond song = new SongRespond();
 		
 		VideoFeed feed = SearchVideo(searchStr);
 		if (feed.getEntries().size() == 0){
 			throw new IndexOutOfBoundsException();
 		}
 		
 		VideoEntry videoEntry = feed.getEntries().get(0);
 		
 		YouTubeMediaGroup mediaGroup = videoEntry.getMediaGroup();
 		
 		song.setId(mediaGroup.getVideoId());
 		song.setTitle(videoEntry.getTitle().getPlainText());
 		
 		song.setDuration(GetMediaDuration(mediaGroup));		
 		
 		return song;
 	}
 	
 	private String GetMediaDuration(YouTubeMediaGroup mediaGroup){
 		int result;
 		int summ, count;
 		
 	    count 	= 0;
 		summ 	= 0;
 		
		for(YouTubeMediaContent mediaContent : mediaGroup.getYouTubeContents()) {		      
 		      summ += mediaContent.getDuration();
		      count++;	  
		    }	
		
 		try{
 			result = summ/count;
 			//System.out.println("summ: " + summ + " count: " + count);
 			//System.out.println("time: " + Utils.SecondsToString(result));
 	    }
 	    catch(ArithmeticException e){
 	    	System.out.println("ArithmeticException in duration count method!");
 	    	
 	    	result = 0;
 		}
 		
 		return (Utils.SecondsToString(result));
 	}
 
 
 }
