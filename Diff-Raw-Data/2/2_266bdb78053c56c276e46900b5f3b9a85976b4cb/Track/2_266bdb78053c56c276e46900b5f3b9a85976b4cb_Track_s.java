 package my.triviagame.xmcd;
 
 import com.google.common.base.Objects;
 import org.apache.commons.lang3.builder.ToStringBuilder;
 
 /**
  * A single track in a CD.
  */
 public class Track {
 
     public final String title;
     public final String artist;
     
     public Track(String title, String artist) {
         this.title = title;
         this.artist = artist;
     }
     
     /**
      * Initializes Track from an xmcd-formatted track title.
      * 
      * @param xmcdTitle string from an xmcd file, e.g. "My Song / My Artist"
      * @param defaultArtist default artist in case the artist is not present in the xmcd title
      */
     public static Track fromXmcdTitle(String xmcdTitle, String defaultArtist) {
        String[] split = xmcdTitle.split(" / ", i);
         String title = split[0];
         String artist;
         if (split.length > 1) {
             artist = split[1];
         } else {
             artist = defaultArtist;
         }
         return new Track(title, artist);
     }
     
     @Override
     public boolean equals(Object obj) {
         if (this == obj) {
             return true;
         }
         if (obj == null) {
             return false;
         }
         if (!(obj instanceof Track)) {
             return false;
         }
         Track other = (Track) obj;
         return (Objects.equal(title, other.title) &&
                 Objects.equal(artist, other.artist));
     }
 
     @Override
     public int hashCode() {
         return Objects.hashCode(title);
     }
     
     @Override
     public String toString() {
         return ToStringBuilder.reflectionToString(this);
     }
 }
