 package at.yawk.fimfiction.operation;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.Reader;
 import java.io.Writer;
 import java.net.URL;
 import java.net.URLConnection;
 
import lombok.Cleanup;
 import lombok.EqualsAndHashCode;
 import lombok.NonNull;
 import lombok.Value;
 import at.yawk.fimfiction.Chapter;
 import at.yawk.fimfiction.FimFiction;
 
 /**
  * Operation to toggle read status of a chapter. Currently it is impossible to
  * set it to <code>true</code> or <code>false</code> specifically.
  * 
  * @author Yawkat
  */
 @Value
 @EqualsAndHashCode(callSuper = false)
 public class ToggleReadStatusOperation extends AbstractRequest<Chapter> {
     @NonNull Chapter chapter;
     
     @Override
     protected Chapter request(final FimFiction session) throws Exception {
         final URL rateUrl = new URL(Util.BASE_URL + "/ajax/toggle_read.php");
         final URLConnection connection = rateUrl.openConnection();
         Util.preparePost(connection);
         connection.setRequestProperty("Cookie", Util.getCookies(session));
         connection.connect();
        @Cleanup final Writer post = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
        post.write("chapter=");
        post.write(Integer.toString(this.getChapter().getId()));
         
        @Cleanup final Reader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
        final char[] buffer = new char[100];
        final int length = reader.read(buffer);
        final String resultingImageSource = new String(buffer, 0, length);
        
        return this.chapter.withUnread(resultingImageSource.endsWith("tick.png"));
     }
 }
