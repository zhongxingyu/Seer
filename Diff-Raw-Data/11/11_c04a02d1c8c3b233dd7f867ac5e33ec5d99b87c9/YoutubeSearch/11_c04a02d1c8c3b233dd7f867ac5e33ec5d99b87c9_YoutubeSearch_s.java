 package de.zigapeda.flowspring.data;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.LinkedList;
 import java.util.List;
 
 import de.zigapeda.flowspring.interfaces.TreeRow;
 
 public class YoutubeSearch implements TreeRow {
 	
 	private String search = null;
 	
 	public YoutubeSearch(String searchstring) {
 		this.search = searchstring;
 	}
 
 	public int getId() {
 		return 0;
 	}
 	
 	public Integer getInt() {
 		return null;
 	}
 	
 	public String getName() {
 		return "Youtube";
 	}
 
 	public String getArtist() {
 		return null;
 	}
 
 	public String getAlbum() {
 		return null;
 	}
 
 	public String getGenre() {
 		return null;
 	}
 
 	public String getTrack() {
 		return null;
 	}
 
 	public String getYear() {
 		return null;
 	}
 
 	public Integer getDuration() {
 		return null;
 	}
 
 	public String getComment() {
 		return null;
 	}
 
 	public String getRating() {
 		return null;
 	}
 
 	public String getPlaycount() {
 		return null;
 	}
 
 	public Integer getType() {
 		return TreeRow.YoutubeSearch;
 	}
 
 	public List<DataNode> getYoutubeTracks() {
 		List<DataNode> list = new LinkedList<>();
 		search = search.replace(' ', '+');
 		String searchUrl = "http://www.youtube.com/results?search_query="
 				+ search;
 		try {
 			URL u = new URL(searchUrl);
 			InputStream is = u.openConnection().getInputStream();
 			BufferedReader br = new BufferedReader(new InputStreamReader(is));
 			String line = "";
 			while ((line = br.readLine()) != null) {
 				if (line.contains("<li class=\"yt-grid-box result-item-video")) {
 					int pos = line.indexOf("data-context-item-title=") + 25;
 					String name = line.substring(pos, line.indexOf("\"", pos));
 					pos = line.indexOf("data-context-item-id=") + 22;
 					String url = "http://www.youtube.com/watch?v="
 							+ line.substring(pos, line.indexOf("\"", pos));
 					pos = line.indexOf("data-context-item-time=") + 24;
 					String time = line.substring(pos, line.indexOf("\"", pos));
 					list.add(new DataNode(new YoutubeVideo(name, url, time),null,null));
 				}
 			}
 			is.close();
			return list;
 		} catch (MalformedURLException e1) {
 		} catch (IOException e1) {
 		}
		return null;
 	}
 
 }
