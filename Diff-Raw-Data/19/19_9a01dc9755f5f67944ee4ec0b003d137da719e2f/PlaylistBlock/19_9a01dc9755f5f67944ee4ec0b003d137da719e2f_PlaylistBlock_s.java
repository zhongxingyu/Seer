 package com.ngdb.web.components.article;
 
 import com.google.common.io.ByteStreams;
 import com.ngdb.entities.ArticleFactory;
 import com.ngdb.entities.article.Article;
 import com.ngdb.entities.article.Game;
 import com.ngdb.entities.article.element.Note;
 import org.apache.tapestry5.annotations.Parameter;
 import org.apache.tapestry5.annotations.Property;
 import org.apache.tapestry5.ioc.annotations.Inject;
 import org.apache.tapestry5.json.JSONArray;
 import org.apache.tapestry5.json.JSONObject;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 public class PlaylistBlock {
 
     @Property
     @Parameter
     private Article article;
 
     @Property
     private Note note;
 
     private static final Logger LOG = LoggerFactory.getLogger(PlaylistBlock.class);
 
     @Inject
     private ArticleFactory articleFactory;
 
     public List<Note> getNotes() {
         List<Note> notes = new ArrayList<Note>();
         try {
             insertPlaylistContentIn(notes);
         } catch (IOException e) {
             LOG.error("Impossible d'obtenir les videos youtube", e);
         }
         return notes;
     }
 
     private void insertPlaylistContentIn(List<Note> notes) throws IOException {
         String playlistUrl = article.getPlaylist();
        String playlistId = getPlaylistIdOf(playlistUrl);
        List<Video> videos = fetchVideosOf(playlistId);
        for (Video video : videos) {
            String label = "<a href='http://www.youtube.com/watch?v=" + video.id + "'>" + video.title + "</a>";
            String value = "<a href='" + video.uploader.uploaderUrl + "'>" + video.uploader.displayName + "</a>";
            notes.add(new Note(label, value, article));
         }
     }
 
     public String getCssClass() {
         if (getNotes().isEmpty()) {
             return "";
         }
         return "table table-striped table-bordered table-condensed";
     }
 
     private String getPlaylistIdOf(String playlistUrl) {
         return playlistUrl.split("=")[1];
     }
 
     private List<Video> fetchVideosOf(String playlist) throws
             IOException {
         List<Video> videos = new ArrayList<Video>();
         String content = readContentOfPlaylist(playlist);
         JSONObject jsonObject = new JSONObject(content);
         JSONObject data1 = (JSONObject) jsonObject.get("data");
         JSONArray items = (JSONArray) data1.get("items");
         for (Object o : items) {
             JSONObject video = (JSONObject) ((JSONObject) o).get("video");
             String uploaderId = video.getString("uploader");
             String id = video.getString("id");
             String title = video.getString("title");
             Uploader uploader = getUploaderById(uploaderId);
             videos.add(new Video(uploader, id, title));
         }
         return videos;
     }
 
     private Uploader getUploaderById(String uploaderId) throws IOException {
         String content =
                 readContentOf("http://gdata.youtube.com/feeds/api/users/" + uploaderId
                         + "?v=2&alt=json");
         JSONObject jsonObject = new JSONObject(content);
         JSONObject entry = (JSONObject) jsonObject.get("entry");
         JSONObject yt$username = (JSONObject) entry.get("yt$username");
         String displayName = yt$username.getString("display");
         JSONArray link = (JSONArray) entry.get("link");
         String uploaderUrl = ((JSONObject) link.get(0)).getString("href");
         return new Uploader(uploaderId, displayName, uploaderUrl);
     }
 
     static class Uploader {
 
         private String uploaderId;
         private String displayName;
         private String uploaderUrl;
 
         public Uploader(String uploaderId, String displayName, String
                 uploaderUrl) {
             this.uploaderId = uploaderId;
             this.displayName = displayName;
             this.uploaderUrl = uploaderUrl;
         }
 
         @Override
         public String toString() {
             return "Uploader{" +
                     "displayName='" + displayName + '\'' +
                     ", uploaderId='" + uploaderId + '\'' +
                     ", uploaderUrl='" + uploaderUrl + '\'' +
                     '}';
         }
     }
 
     static class Video {
         private Uploader uploader;
         private String id;
         private String title;
 
         public Video(Uploader uploader, String id, String title) {
             this.uploader = uploader;
             this.id = id;
             this.title = title;
         }
 
         @Override
         public String toString() {
             return "Video{" +
                     "id='" + id + '\'' +
                     ", uploader='" + uploader + '\'' +
                     ", title='" + title + '\'' +
                     '}';
         }
     }
 
     private static String readContentOfPlaylist(String playlist)
             throws IOException {
         return readContentOf("http://gdata.youtube.com/feeds/api/playlists/"
                 + playlist + "?v=2&alt=jsonc");
     }
 
     private static String readContentOf(String sUrl) throws IOException {
         URL url = new URL(sUrl);
         InputStream in = url.openStream();
         byte[] bytes = ByteStreams.toByteArray(in);
         return new String(bytes);
     }
 
     public String getPlaylist() {
        if(article.isGame()) {
            return articleFactory.findPlaylistOf((Game)article);
         }
         return null;
     }
 
 }
