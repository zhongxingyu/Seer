 package at.friki.aufgabe1;
 
 import java.net.MalformedURLException;
 import java.net.URL;
import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
import java.util.Locale;
 
 /**
  * Created by Mathias on 26.09.13.
  */
 public class RssItem {
    static SimpleDateFormat FORMATTER = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US);
    //static DateFormat format = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM, Locale.US);
    
     private String title;
     private URL link;
     private String description;
     private Date date;
 
     public String getTitle() {
         return this.title;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
     public String getDescription() {
         return this.description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     public URL getLink() {
         return this.link;
     }
 
     public void setLink(String link) {
         try {
             this.link = new URL(link);
         } catch (MalformedURLException e) {
             throw new RuntimeException(e);
         }
     }
 
     public String getDate() {
        return FORMATTER.format(this.date); //format.format(this.date);// 
     }
 
     public void setDate(String date) {
         // pad the date if necessary
         while (!date.endsWith("00")){
             date += "0";
         }
         try {
            this.date = FORMATTER.parse(date.trim());//format.parse(date.trim());//
         } catch (ParseException e) {
             throw new RuntimeException(e);
         }
     }
 
     public RssItem copy() {
         RssItem copyItem = new RssItem();
 
         copyItem.title = this.title;
         copyItem.link = this.link;
         copyItem.description = this.description;
         copyItem.date = this.date;
 
         return copyItem;
     }
 
     // sort by date
     public int compareTo(RssItem another) {
         if (another == null) return 1;
         // sort descending, most recent first
         return another.date.compareTo(date);
     }
 }
