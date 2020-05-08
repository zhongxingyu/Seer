 package com.enjoyxstudy.ircbotconsole.notifier;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.Reader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.mozilla.javascript.Context;
 import org.mozilla.javascript.Scriptable;
 import org.mozilla.javascript.ScriptableObject;
 import org.mozilla.javascript.Undefined;
 
 import com.enjoyxstudy.ircbotconsole.IrcBot;
 import com.enjoyxstudy.ircbotconsole.ScriptUtils;
 import com.sun.syndication.feed.synd.SyndEntry;
 import com.sun.syndication.feed.synd.SyndFeed;
 import com.sun.syndication.io.FeedException;
 import com.sun.syndication.io.SyndFeedInput;
 import com.sun.syndication.io.XmlReader;
 
 /**
  * RSSを通知するクラスです。
  *
  * @author onozaty
  */
 public class RssNotifier extends AbstractNotifier {
 
     /** STREAM_BUF_SIZE */
     private static final int STREAM_BUF_SIZE = 1024;
 
     /** RSSフィードのURLです。 */
     private String feedUrl;
 
     /** RSSのメッセージ書式のスクリプトです。 */
     private String messageFormatScript;
 
     /** 前回取得のRSSファイルです。 */
     private File oldFeedFile;
 
     /** 今回取得のRSSファイルです。 */
     private File newFeedFile;
 
     /**
      * コンストラクタです。
      *
      * @param channel
      * @param scheduler
      * @param feedUrl
      * @param messageFormatScript
      * @param workDirectory
      * @throws NoSuchAlgorithmException
      */
     public RssNotifier(String channel, Scheduler scheduler, String feedUrl,
             String messageFormatScript, File workDirectory)
             throws NoSuchAlgorithmException {
         super(channel, scheduler);
 
         this.feedUrl = feedUrl;
         this.messageFormatScript = messageFormatScript;
 
         String urlHash = urlToHash(feedUrl);
 
         this.oldFeedFile = new File(workDirectory, channel + urlHash + ".old");
         this.newFeedFile = new File(workDirectory, channel + urlHash + ".new");
     }
 
     /**
      * @throws IOException
      * @throws FeedException
      * @throws IllegalArgumentException
      * @see com.enjoyxstudy.ircbotconsole.notifier.AbstractNotifier#createMessage(com.enjoyxstudy.ircbotconsole.IrcBot)
      */
     @Override
     protected String[] createMessage(IrcBot ircBot) throws IOException,
             IllegalArgumentException, FeedException {
 
         HttpURLConnection connection = (HttpURLConnection) new URL(feedUrl)
                 .openConnection();
 
         try {
             if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                 throw new IOException("request failed.  statusCode=["
                         + connection.getResponseCode() + "]");
             }
 
             if (newFeedFile.exists()) {
                 oldFeedFile.delete();
                 newFeedFile.renameTo(oldFeedFile);
             }
 
             OutputStream outputStream = new FileOutputStream(newFeedFile);
             try {
                 copyStream(connection.getInputStream(), outputStream);
             } finally {
                 outputStream.close();
             }
 
             ArrayList<SyndEntry> newEntryList = collectNewEntry();
 
             ArrayList<String> messageList = new ArrayList<String>();
             for (SyndEntry entry : newEntryList) {
                 String message = formatMessage(entry, ircBot);
                 if (message != null) {
                     messageList.add(message);
                 }
             }
 
             return messageList.toArray(new String[messageList.size()]);
 
         } finally {
             connection.disconnect();
         }
     }
 
     /**
      * RSS通知メッセージを整形します。
      *
      * @param entry
      * @param ircBot
      * @return メッセージ
      * @throws IOException
      */
     private String formatMessage(SyndEntry entry, IrcBot ircBot)
             throws IOException {
 
         if (messageFormatScript == null
                 || messageFormatScript.trim().length() == 0) {
             // 書式が設定されていない場合
             // デフォルトの形式にて(タイトル＋URL)
             return entry.getTitle() + " " + entry.getLink();
         }
 
         Context context = ScriptUtils.createContext();
         Scriptable scope = ScriptUtils.initScope(context);
 
         // JSのオブジェクトにマッピング
         ScriptableObject.putProperty(scope, "_channel", Context.javaToJS(
                 channel, scope));
         ScriptableObject.putProperty(scope, "_ircBot", Context.javaToJS(ircBot,
                 scope));
 
         ScriptableObject.putProperty(scope, "_title", Context.javaToJS(entry
                 .getTitle(), scope));
         ScriptableObject.putProperty(scope, "_link", Context.javaToJS(entry
                 .getLink(), scope));
         ScriptableObject.putProperty(scope, "_description", Context.javaToJS(
                entry.getDescription().getValue(), scope));
         ScriptableObject.putProperty(scope, "_updatedDate", Context.javaToJS(
                 entry.getUpdatedDate(), scope));
         ScriptableObject.putProperty(scope, "_publishedDate", Context.javaToJS(
                 entry.getPublishedDate(), scope));
 
         Object result = context.evaluateString(scope, messageFormatScript,
                 "<script>", 1, null);
 
         if (result == null || result instanceof Undefined) {
             // nullまたはundefinedの場合はメッセージを送信しない
             return null;
         }
 
         return Context.toString(result);
     }
 
     /**
      * 新規/更新エントリを取得します。
      *
      * @return 新規/更新エントリのリスト
      * @throws FileNotFoundException
      * @throws IllegalArgumentException
      * @throws IOException
      * @throws FeedException
      */
     @SuppressWarnings("unchecked")
     private ArrayList<SyndEntry> collectNewEntry()
             throws FileNotFoundException, IllegalArgumentException,
             IOException, FeedException {
 
         Reader newFeedReader = null;
         Reader oldFeedReader = null;
 
         try {
             newFeedReader = new XmlReader(newFeedFile);
             SyndFeedInput input = new SyndFeedInput();
             SyndFeed newFeed = input.build(newFeedReader);
             List<SyndEntry> newEntries = newFeed.getEntries();
 
             List<SyndEntry> oldEntries;
             if (oldFeedFile.exists()) {
                 oldFeedReader = new XmlReader(oldFeedFile);
                 SyndFeed oldFeed = input.build(oldFeedReader);
                 oldEntries = oldFeed.getEntries();
             } else {
                 oldEntries = new ArrayList<SyndEntry>();
             }
 
             ArrayList<SyndEntry> newEntryList = new ArrayList<SyndEntry>();
 
             for (Iterator<SyndEntry> newIterator = new ArrayList<SyndEntry>(
                     newEntries).iterator(); newIterator.hasNext();) {
                 SyndEntry newEntry = newIterator.next();
                 boolean isProcessed = false;
                 for (Iterator<SyndEntry> oldIterator = new ArrayList<SyndEntry>(
                         oldEntries).iterator(); oldIterator.hasNext();) {
                     SyndEntry oldEntry = oldIterator.next();
                     if (newEntry.getLink().equals(oldEntry.getLink())) {
                         if (isUpdateEntry(newEntry, oldEntry)) {
                             // 更新されたエントリ
                             newEntryList.add(newEntry);
                         }
                         isProcessed = true;
                         break;
                     }
                 }
                 if (!isProcessed) {
                     if (newEntry.getUpdatedDate() != null) {
                         // 更新されたエントリ
                         newEntryList.add(newEntry);
 
                     } else {
                         // 追加されたエントリ
                         newEntryList.add(newEntry);
                     }
                 }
             }
 
             return newEntryList;
 
         } finally {
             if (newFeedReader != null) {
                 newFeedReader.close();
             }
             if (oldFeedReader != null) {
                 oldFeedReader.close();
             }
         }
     }
 
     /**
      * エントリを比較し、更新されているか判定します。
      *
      * @param newEntry 新しいエントリ
      * @param oldEntry 古いエントリ
      * @return 更新されている場合true
      */
     private boolean isUpdateEntry(SyndEntry newEntry, SyndEntry oldEntry) {
         return !(equals(newEntry.getUpdatedDate(), oldEntry.getUpdatedDate())
                 && equals(newEntry.getUpdatedDate(), oldEntry.getUpdatedDate())
                 && equals(newEntry.getTitle(), oldEntry.getTitle()) && equals(
                 newEntry.getDescription(), oldEntry.getDescription()));
     }
 
     /**
      * オブジェクトの比較を行います。
      * nullどうしの場合、trueを返却します。
      *
      * @param o1
      * @param o2
      * @return 比較結果
      */
     private boolean equals(Object o1, Object o2) {
 
         if (o1 == null && o2 == null) {
             return true;
         }
         if (o1 == null || o2 == null) {
             return false;
         }
 
         return o1.equals(o2);
     }
 
     /**
      * URLのハッシュを取得します。
      *
      * @param url URL
      * @return ハッシュ(MD5)
      * @throws NoSuchAlgorithmException
      */
     private String urlToHash(String url) throws NoSuchAlgorithmException {
 
         MessageDigest md = MessageDigest.getInstance("MD5");
         md.update(url.getBytes());
         byte[] hash = md.digest();
 
         StringBuffer hexString = new StringBuffer();
         for (int i = 0; i < hash.length; i++) {
             if ((0xff & hash[i]) < 0x10) {
                 hexString.append("0" + Integer.toHexString((0xFF & hash[i])));
             } else {
                 hexString.append(Integer.toHexString(0xFF & hash[i]));
             }
         }
 
         return hexString.toString();
     }
 
     /**
      * ストリームをコピーします。
      *
      * @param inputStream
      * @param outputStream
      * @throws IOException
      */
     private void copyStream(InputStream inputStream, OutputStream outputStream)
             throws IOException {
 
         byte[] buf = new byte[STREAM_BUF_SIZE];
         int bufSize = 0;
 
         // 入力ストリームの終了まで固定サイズで読み込み→書き込みを繰り返し
         while ((bufSize = inputStream.read(buf, 0, STREAM_BUF_SIZE)) != -1) {
             outputStream.write(buf, 0, bufSize);
         }
     }
 
 }
