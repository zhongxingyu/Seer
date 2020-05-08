 package my.triviagame.xmcd;
 
 /**
  * 
  */
 public class TrackTitle extends Title {
     
     public TrackTitle(String title, String artist) {
         super(title, artist);
     }
 
     /**
      * Constructs a {@link TrackTitle} from an xmcd-formatted title.
      * 
      * @param xmcdTitle string from an xmcd file, e.g. "My Song / My Artist" or "My Song"
      * @param defaultArtist default artist in case the artist is not present in the xmcd title
      */
    public static TrackTitle fromXmcdTitle(String xmcdTitle, String defaultArtist) {
         String[] split = xmcdTitle.split(" / ", 2);
         String title = split[0];
         String artist;
         if (split.length > 1) {
             artist = split[1];
         } else {
             artist = defaultArtist;
         }
         return new TrackTitle(title, artist);
     }
 }
