 package com.examples.gg;
 
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 
 
 public class FeedManager
 {
 
     private JSONObject feed;
     private String theSource;
     private static final String YOUTUBE = "Youtube";
     private static final String TWITCH = "Twitch";
     private String twtichNextApi;
 
 
     public FeedManager(String json, String source) throws JSONException{
     	this.theSource = source;
     	if(source.equals(YOUTUBE))
     		processJSONYoutube(json);
     	else if(source.equals(TWITCH))
     		processJSONTwitch(json);
     }
 
     private void processJSONTwitch(String json) throws JSONException {
 		// TODO Auto-generated method stub
     	twtichNextApi = null;
         JSONTokener jsonParser = new JSONTokener(json);  
         this.feed = (JSONObject) jsonParser.nextValue();  
         twtichNextApi = feed.getJSONObject("_links").getString("next");
 	}
 
 	public ArrayList<Video> getVideoPlaylist(){
         ArrayList<Video> videos = new ArrayList<Video>();
         
         try {
         	if(theSource.equals(YOUTUBE)){
        		System.out.println("Here is youtube");
 	            //get title of the playlist
 	            String plTitle = feed.getJSONObject("title").getString("$t");
 	            //System.out.println(plTitle);
 	            //get the playlist
 	            JSONArray playlist = feed.getJSONArray("entry");
 	            //System.out.println("Length: "+ playlist.length());
 	            
 	            for(int i=0;i<playlist.length();i++){
 	                //get a video in the playlist           
 	                JSONObject oneVideo = playlist.getJSONObject(i);
 	                //get the title of this video
 	                String videoTitle = oneVideo.getJSONObject("title").getString("$t");
 	                String videoLink = oneVideo.getJSONObject("content").getString("src");
 	                String videoId = videoLink.substring(videoLink.indexOf("/v/")+3, videoLink.indexOf("?"));
 	                String videoDesc = oneVideo.getJSONObject("media$group").getJSONObject("media$description").getString("$t");
 	                String thumbUrl = oneVideo.getJSONObject("media$group").getJSONArray("media$thumbnail").getJSONObject(0).getString("url");
 	                String updateTime = oneVideo.getJSONObject("updated").getString("$t");
 	                String author = oneVideo.getJSONArray("author").getJSONObject(0).getJSONObject("name").getString("$t");
 	                //System.out.println(thumbUrl);
 	                //System.out.println(videoDesc);
 	                //store title and link
 	                Video video = new Video();
 	                video.setTitle(videoTitle);
 	                video.setVideoId(videoId);
 	                video.setThumbnailUrl(thumbUrl);
 	                video.setVideoDesc(videoDesc);
 	                video.setUpdateTime(updateTime);
 	                video.setAuthor(author);
 	                //System.out.println(video.getTitle());
 	                //push it to the list
 	                videos.add(video);
 	                //System.out.println(videoTitle+"***"+videoLink);
 	                
 	                
 	            }
 	            
 	            
 	            //check if there are more videos
 	            //String result = getNextApi();
 	            //System.out.println("Result: "+result);
 
         	}else if (theSource.equals(TWITCH)){
 	          	//Json is coming from twitch
        		System.out.println("Here is twitch");
 	    		JSONArray streams = feed.getJSONArray("streams");
 	    		System.out.println("Total: " + streams.length());
 	            for(int i=0;i<streams.length();i++){
	            	 System.out.println("Start+1");
 	                //get a video in the playlist           
 	                JSONObject oneVideo = streams.getJSONObject(i);
	                System.out.println("Start+2");
 	                //get the title of this video
 	                JSONObject videoChannel = oneVideo.getJSONObject("channel");
	                System.out.println("Start+3");
 	                String videoTitle = videoChannel.getString("status");
	                System.out.println("Start+4");
 	                String videoLink = oneVideo.getJSONObject("_links").getString("self");
	                System.out.println("Start+5");
	                String videoId = videoLink.substring(videoLink.indexOf("/streams/")+9, videoLink.length()-1);
	                System.out.println("Start+6");
 	                String videoDesc = "No Desc";
	                System.out.println("Start+7");
 	                String thumbUrl = oneVideo.getJSONObject("preview").getString("small");
	                System.out.println("Start+8");
 	                String updateTime = videoChannel.getString("updated_at");
	                System.out.println("Start+9");
 	                String author = videoChannel.getString("display_name");
	                System.out.println("Thum+++++");
	                System.out.println("Thum: " + thumbUrl);
 	                System.out.println("Stream ID: " + videoId);
 	                //store title and link
 	                
 	                Video video = new Video();
 	                video.setTitle(videoTitle);
 	                video.setVideoId(videoId);
 	                video.setAuthor(author);
 	                video.setThumbnailUrl(thumbUrl);
 	                video.setVideoDesc(videoDesc);
 	                video.setUpdateTime(updateTime);
 	                //video.setAuthor(author);
 	                //System.out.println(video.getTitle());
 	                //push it to the list
 	                videos.add(video);
 	                //System.out.println(videoTitle+"***"+videoLink);
 	                
 	                
 	            }
 	    	}
         } catch (JSONException ex) {  
             
             ex.printStackTrace();
         }  
         
         
         return videos;      
     }
     
     public ArrayList<Video> getVideoPlaylist2(){
         ArrayList<Video> videos = new ArrayList<Video>();
         
         try {
 
         	
             //get title of the playlist
             String plTitle = feed.getJSONObject("title").getString("$t");
             //System.out.println(plTitle);
             //get the playlist
             JSONArray playlist = feed.getJSONArray("entry");
             //System.out.println("Length: "+ playlist.length());
             
             
             for(int i=0;i<playlist.length();i++){
             	Video video = new Video();
                 //get a video in the playlist            
                 JSONObject oneVideo = playlist.getJSONObject(i);
                 //get the title of this video
                 String videoTitle = oneVideo.getJSONObject("title").getString("$t");
                 String videoLink = oneVideo.getJSONObject("content").getString("src");
                 String videoId = videoLink.substring(videoLink.indexOf("/v/")+3, videoLink.indexOf("?"));
                 String author = oneVideo.getJSONArray("author").getJSONObject(0).getJSONObject("name").getString("$t");
                 //String videoDesc = oneVideo.getJSONObject("media$group").getJSONObject("media$description").getString("$t");
                 String videoDesc = oneVideo.getJSONObject("summary").getString("$t");
                 String thumbUrl = oneVideo.getJSONObject("media$group").getJSONArray("media$thumbnail").getJSONObject(0).getString("url");
                 String updateTime = oneVideo.getJSONObject("updated").getString("$t");
                 
                 if(author.toUpperCase().equals("DOTACINEMA")){
                 	video.setUploaderThumUrl("https://i1.ytimg.com/i/NRQ-DWUXf4UVN9L31Y9f3Q/1.jpg?v=5067cf3b");
                 }else if(author.toUpperCase().equals("NOOBFROMUA")){
                 	video.setUploaderThumUrl("https://i1.ytimg.com/i/fsOfLvadg89Bx8Sv_6WERg/1.jpg?v=515d687f");
                 }
                 
                 //System.out.println(thumbUrl);
                // System.out.println(videoDesc);
                 //store title and link
                 
                 video.setTitle(videoTitle);
                 video.setVideoId(videoId);
                 video.setThumbnailUrl(thumbUrl);
                 video.setAuthor(author);
                 video.setPlaylistUrl(videoLink + "&start-index=1&max-results=10&orderby=published&alt=json");
                 video.setVideoDesc(videoDesc);
                 video.setUpdateTime(updateTime);
                 //System.out.println(video.getTitle());
                 //push it to the list
                 videos.add(video);
                 //System.out.println(videoTitle+"***"+videoLink);
                 
                 
             }
             
             
             //check if there are more videos
             //String result = getNextApi();
             //System.out.println("Result: "+result);
 
         } catch (JSONException ex) {  
         	ex.printStackTrace();
         }  
         
         
         return videos;      
     }
     
     public String getNextApi() throws JSONException{
     	if(theSource.equals(YOUTUBE)){
         JSONArray link = feed.getJSONArray("link");
         for(int i=0;i<link.length();i++){
             JSONObject jo = link.getJSONObject(i);
             //System.out.println(jo.getString("rel"));
             if(jo.getString("rel").equals("next")){
                 //there are more videos in this playlist
                 String nextUrl = jo.getString("href");
                 return nextUrl;
             }
         }
     	}else if(theSource.equals(TWITCH)){
     		return twtichNextApi;
     		
     	}
 		return null;
     }
     
 
 
 
 	public JSONObject getFeed() {
 		return feed;
 	}
 
 
 
 
 	public void setFeed(JSONObject feed) {
 		this.feed = feed;
 	}
     
 	
     private void processJSONYoutube(String json) throws JSONException{
         JSONTokener jsonParser = new JSONTokener(json);  
         JSONObject wholeJson = (JSONObject) jsonParser.nextValue();  
         this.feed = wholeJson.getJSONObject("feed");
     }
 
 }
