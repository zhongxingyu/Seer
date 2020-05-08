 package dareka.processor.impl;
 
 import java.io.ByteArrayOutputStream;
 import java.io.EOFException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import dareka.common.CloseUtil;
 import dareka.common.Logger;
 import dareka.processor.HttpHeader;
 import dareka.processor.HttpResponseHeader;
 import dareka.processor.HttpUtil;
 import dareka.processor.TransferListener;
 
 /**
  * Record type(sm/ax/ca, etc.), title of movies. The URL to the movie server
  * does not contain the type, so it is mandatory to record the type when
  * we see the watch page.
  *
  */
 public class NicoRecordingWatchListener implements TransferListener {
    private static final int MAX_READ_SIZE = 32 * 1024; // for safety
     /**
      * In some page such as my memory, the URL does not contain
      * the type and id of the movie. In case of this, retrieve the title
      * from the page content. This may not work correctly
      * when the user comment matches this pattern...
      */
     private static final Pattern VIDEO_ID_PATTERN =
             Pattern.compile("(?:video_id = |id:\\s*)'([a-z]{2})(\\d+)'");
     // videoDetail&quot;:\{&quot;v&quot;:&quot;.+?&quot;,&quot;id&quot;:&quot;([a-z]+)(\d+)&quot;
     private static final Pattern VIDEO_ID_PATTERN2 =
             Pattern.compile("videoDetail&quot;:\\{&quot;v&quot;:&quot;.+?&quot;,&quot;id&quot;:&quot;([a-z]+)(\\d+)&quot;");
 
     /**
      * Retrieve title from h1 element instead of title element, because
      * some people rewrite the title element for there convenient by
      * Proxomitron and so on.
      *
      * From Apr 10, 2009, h1 became to include an A element. At this time,
      * the title can not include "<", so "</" is suitable for the delimiter.
      *
      * As of Oct 2010, h1 is no longer used. Instead, <p class="video_title">
      * is used.
      *
      */
     private static final Pattern TITLE_PATTERN =
             Pattern.compile("<p[^>]+?class=\"video_title\"[^>]*?>(.{1,1024}?)<a");
 
     private ByteArrayOutputStream out =
             new ByteArrayOutputStream(MAX_READ_SIZE);
     private String type;
     private String id;
     private String contentEncoding;
 
     public NicoRecordingWatchListener() {
         this(null, null);
     }
 
     public NicoRecordingWatchListener(String type, String id) {
         if (type != null && id != null) {
             this.type = type;
             this.id = id;
         }
     }
 
     public void onResponseHeader(HttpResponseHeader responseHeader) {
         contentEncoding =
                 responseHeader.getMessageHeader(HttpHeader.CONTENT_ENCODING);
     }
 
     public void onTransferBegin(OutputStream receiverOut) {
         // do nothing
     }
 
     public void onTransferring(byte[] buf, int length) {
         int remain = MAX_READ_SIZE - out.size();
         if (remain <= 0) {
             return;
         }
 
         int recordingLen;
         if (remain > length) {
             recordingLen = length;
         } else {
             recordingLen = remain;
         }
 
         out.write(buf, 0, recordingLen);
     }
 
     public void onTransferEnd(boolean completed) {
         if (!completed) {
             return;
         }
 
         String page = getPage();
 
         if (type == null) {
             Matcher mId = VIDEO_ID_PATTERN.matcher(page);
             if (!mId.find()) {
             	mId = VIDEO_ID_PATTERN2.matcher(page);
             	if (!mId.find()) {
                     Logger.warning("no video id found");
                     return;
             	}
             }
 
             type = mId.group(1);
             id = mId.group(2);
         }
 
         String title = getTitleFromWatchPage(page);
         if (title == null) {
             Logger.warning("no title found: " + type + id);
             return;
         }
 
         NicoIdInfoCache.getInstance().put(type, id, title);
 
         Logger.debugWithThread("title recorded: " + type + id + " => " + title);
     }
 
     private String getPage() {
         StringBuilder pageBuf = new StringBuilder(out.size() * 4);
 
         InputStreamReader r = null;
         try {
             InputStream decodedIn =
                     HttpUtil.getDecodedInputStream(out.toByteArray(),
                             contentEncoding);
             r = new InputStreamReader(decodedIn, "UTF-8");
             int ch;
 
             try {
                 while ((ch = r.read()) != -1) {
                     pageBuf.append((char) ch);
                 }
             } catch (EOFException e) {
                 // If the size of the content is more than MAX_READ_SIZE,
                 // read() throws IOException. This is expected behavior, so
                 // we want to just ignore the exception. However, there is no
                 // way to distinguish between this case and some other
                 // I/O errors.
                 // Some implementation of Java (such as Java 6 on Windows)
                 // returns EOFException, so we use the behavior.
 
                 Logger.debugWithThread("partial content: " + e.toString());
             } catch (IOException e) {
                 Logger.warning("content decoding error: " + e.toString());
             }
         } catch (UnsupportedEncodingException e) {
             // never happen
             Logger.error(e);
         } catch (IOException e) {
             Logger.error(e);
         } finally {
             CloseUtil.close(r);
         }
 
         return pageBuf.toString();
     }
 
     String getTitleFromWatchPage(String page) {
     	return NicoCachingTitleRetriever.getTitleFromResponse(page);
     }
 }
