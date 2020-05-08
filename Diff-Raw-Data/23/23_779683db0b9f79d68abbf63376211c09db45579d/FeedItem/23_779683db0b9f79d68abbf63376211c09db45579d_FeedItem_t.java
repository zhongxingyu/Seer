 package nmd.rss.collector.feed;
 
 import java.util.Date;
 
 import static nmd.rss.collector.util.Assert.*;
 import static nmd.rss.collector.util.Parameter.isValidString;
 
 /**
  * Author : Igor Usenko ( igors48@gmail.com )
  * Date : 28.04.13
  */
 public class FeedItem {
 
    private static final int ONE_SECOND = 1000;

     public final String title;
     public final String description;
     public final String link;
     public final Date date;
 
     public FeedItem(final String title, final String description, final String link, final Date date) {
         assertNotNull(title);
         assertNotNull(description);
         assertTrue(isValidString(title) || isValidString(description));
         this.title = title;
         this.description = description;
 
         assertValidUrl(link);
         this.link = link;
 
         assertNotNull(date);
         this.date = date;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         FeedItem feedItem = (FeedItem) o;
 
         if (!description.equals(feedItem.description)) return false;
         if (!link.equals(feedItem.link)) return false;

        // this is important
        if (Math.abs(date.getTime() - feedItem.date.getTime()) > ONE_SECOND) return false;

         if (!title.equals(feedItem.title)) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = title.hashCode();
         result = 31 * result + description.hashCode();
         result = 31 * result + link.hashCode();
         result = 31 * result + date.hashCode();
         return result;
     }
 }
