 package com.example.listview;
 
 import java.util.List;
 
 public class VideoItem extends VideoInfo {
 	List<VideoInfo> related;
 	List<VideoInfo> same_artist;
 	List<VideoInfo> best_in_category;
 	List<Episode> episodes;
 	List<Tag> tags;

 	// Use for relate videos
 	public VideoItem(String uniq_id, String artist, String video_title,
 			String description, String yt_id, String yt_thumb, int site_views) {
 		this.uniq_id = uniq_id;
 		this.artist = artist;
 		this.video_title = artist;
 		this.description = description;
 		this.yt_id = yt_id;
 		this.yt_thumb = yt_thumb;
 		this.site_views = site_views;
 	}
 
 	// Use for get video from category
 	public VideoItem(int id, String uniq_id, String artist, String video_title,
			String description, String yt_id, String yt_thumb, int site_views,
 			String mp3) {
 		this.id = id;
 		this.uniq_id = uniq_id;
 		this.artist = artist;
 		this.video_title = video_title;
 		this.description = description;
 		this.yt_id = yt_id;
 		this.yt_thumb = yt_thumb;
 		this.site_views = site_views;
 		this.mp3 = mp3;
 	}
 
 	// Use for full video description
 	public void setRelated(List<VideoInfo> related) {
 		this.related = related;
 	}
 
 	public void setSameArtist(List<VideoInfo> same_artist) {
 		this.same_artist = same_artist;
 	}
 
 	public void setBestInCategory(List<VideoInfo> best_in_category) {
 		this.best_in_category = best_in_category;
 	}
 
 	public void setEpisode(List<Episode> episodes) {
 		this.episodes = episodes;
 	}

	public void setTag(List<Tag> tags) {
 		this.tags = tags;
 	}
 }
