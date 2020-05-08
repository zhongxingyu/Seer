 package br.com.superseniordevelopers.html5videotest.web.dto;
 
 import br.com.superseniordevelopers.html5videotest.web.entity.VideoHTML5;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Fake implementation of VideoDTO just to illustrate the use of HTML5 Video
  *
  * @author luan
  */
 public class VideoDTOMock implements VideoDTO {
 
     @Override
     public boolean hasError() {
         return false;
     }
 
     @Override
     public String getErrorMessage() {
         return "Error ocurred!";
     }
 
     @Override
     public List<VideoHTML5> getVideoURLs() {
         List<VideoHTML5> urls = new ArrayList<VideoHTML5>();
         /*
          * The first videos (http://www.w3schools.com/html5/movie.mp4 and
          * http://www.w3schools.com/html5/movie.ogg ) are owned by W3Schools
          * (website http://www.w3schools.com/)
          *
          * The other are from http://www.808.dk/
          *
          * I'm using the links just for demonstration of HTML5 Video, no
          * commercial propose.
          *
          */
         urls.add(new VideoHTML5("http://www.w3schools.com/html5/movie.ogg", "http://www.w3schools.com/html5/movie.mp4"));
         urls.add(new VideoHTML5("http://www.808.dk/pics/video/gizmo.ogv", "http://www.808.dk/pics/video/gizmo.mp4"));
         return urls;
     }
 }
