 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Calendar;
 import java.util.Date;
 
 import javax.microedition.lcdui.Image;
 import javax.microedition.media.Manager;
 import javax.microedition.media.MediaException;
 import javax.microedition.media.Player;
 import javax.microedition.media.PlayerListener;
 
 public class Utils {
     /** Formats seconds as a duration description. */
     static String secToDuration(int n) {
         if (n <= 90) {
             return n + " seconds";
         }
         int min = n / 60;
         int sec = n % 60;
         if (sec == 0) {
           return min + " minutes";
         }
         // Spare seconds in Couch-to-5k intervals all round nicely
         return (min + (float)sec/60) + " minutes";
     }
 
     /** Formats seconds as a time in MM:SS format. */
     static String secToTime(int n) {
         int minutes = n / 60;
         int seconds = n % 60;
         return pad(minutes) + ":" + pad(seconds);
     }
 
     static final String[] DAYS = new String[] {
        "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
     static final String[] MONTHS = new String[] {
         "Jan", "Feb", "Mar", "Apr", "May", "June", "July", "Aug", "Sep", "Oct", "Nov", "Dec"};
     static final String[] ORDINALS = new String[] {
         "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th"};
     static String formatDate(Date date) {
         Calendar cal = Calendar.getInstance();
         int currentYear = cal.get(Calendar.YEAR);
         cal.setTime(date);
         StringBuffer sb = new StringBuffer();
         sb.append(pad(cal.get(Calendar.HOUR_OF_DAY))).append(":")
           .append(pad(cal.get(Calendar.MINUTE))).append(", ")
          .append(DAYS[cal.get(Calendar.DAY_OF_WEEK) - 1]).append(" ")
           .append(cal.get(Calendar.DAY_OF_MONTH))
           .append(ORDINALS[cal.get(Calendar.DAY_OF_MONTH) % 10]).append(" ")
           .append(MONTHS[cal.get(Calendar.MONTH)]);
         if (cal.get(Calendar.YEAR)!= currentYear ) {
             sb.append(" ").append(cal.get(Calendar.YEAR));
         }
         return sb.toString();
     }
 
     /** Pads a number with a leading zero if necessary. */
     static String pad(int n) {
         if (n < 10) {
             return "0" + n;
         }
         return "" + n;
     }
 
     /** Formats a String with $1, $2 style placeholders. */
     static String format(String template, String[] values) {
         StringBuffer sb = new StringBuffer();
         int nextIndex = 0;
         for (int i = 0; i < values.length; i++) {
             int placeholderIndex = template.indexOf("$" + (i + 1), nextIndex);
             sb.append(template.substring(nextIndex, placeholderIndex));
             sb.append(values[i]);
             // Advance to the index beyond the placeholder
             nextIndex = (placeholderIndex + 2 + ((i + 1) / 10));
         }
         if (nextIndex <= template.length() - 1) {
             sb.append(template.substring(nextIndex));
         }
         return sb.toString();
     }
 
     /** Plays an MP3 file once. */
     static void playSound(String action) {
         try {
             InputStream in = Utils.class.getResourceAsStream("/" + action + ".mp3");
             Player player = Manager.createPlayer(in, "audio/mpeg");
             player.addPlayerListener(new ClosePlayerListener());
             player.start();
         } catch (MediaException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     /** Loads an image file as an immutable Image. */
     static Image loadImage(String name) {
         Image result = null;
         try {
             InputStream in = Utils.class.getResourceAsStream("/" + name + ".png");
             result = Image.createImage(in);
         } catch (IOException e) {
             e.printStackTrace();
         }
         return result;
     }
 
     static class ClosePlayerListener implements PlayerListener {
         /**
          * Close every Player we create once it reaches the end of its media, to
          * free up resources.
          */
         public void playerUpdate(Player player, String event, Object eventData) {
             if (event == PlayerListener.END_OF_MEDIA) {
                 player.close();
             }
         }
     }
 }
