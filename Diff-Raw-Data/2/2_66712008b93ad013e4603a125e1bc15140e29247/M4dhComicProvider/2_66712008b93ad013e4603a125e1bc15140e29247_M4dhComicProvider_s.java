 package net.bytten.m4dh;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.bytten.comicviewer.IComicInfo;
 import net.bytten.comicviewer.IComicProvider;
 import net.bytten.comicviewer.Utility;
 import net.bytten.comicviewer.ArchiveData.ArchiveItem;
 
 import android.net.Uri;
 import android.text.Html;
 
 public class M4dhComicProvider implements IComicProvider {
 
     private static final Pattern archiveItemPattern = Pattern.compile(
             // group(1): comic id;      group(2): title
             "<a href=\"http://milkfordeadhamsters.com/comics/([^\"]+)\" rel=\"bookmark\">([^<]+)</a>");
     private static final String ARCHIVE_URL = "http://milkfordeadhamsters.com/master-list";
     
     @Override
     public Uri comicDataUrlForUrl(Uri url) {
         Matcher m = M4dhComicDefinition.comicUrlPattern
             .matcher(url.toString());
         if (m.matches())
             return createComicUrl(m.group(2));
         return null;
     }
 
     @Override
     public Uri createComicUrl(String comicId) {
         return Uri.parse("http://milkfordeadhamsters.com/comics/"+comicId);
     }
 
     private static final Pattern
         titlePattern = Pattern.compile("<title>([^<]+)</title>"),
         prevPattern = Pattern.compile("<a href=\"http://milkfordeadhamsters.com/comics/([^\"]+)\"\\s+rel=\"prev\">"),
         nextPattern = Pattern.compile("<a href=\"http://milkfordeadhamsters.com/comics/([^\"]+)\"\\s+rel=\"next\">"),
        imageTagPattern = Pattern.compile("<div class=\"entry\">\\s+<p[^>]*>\\s*(<a href=\"http://milkfordeadhamsters.com/comics/([^\"/]+)(/([^\"]*))?\"[^>]*>\\s*)?<img([^>]+)/>"),
         imagePattern = Pattern.compile("src=\"(http://(milkfordeadhamsters.com|torchspark.com/comic2)/wp-content/uploads/[^\"]+)\""),
         altPattern = Pattern.compile("title=\"([^\"]+)\""),
         backupIdPattern = Pattern.compile("<a href=\"http://milkfordeadhamsters.com/comics/([^\"]+)\"\\s+(title=\"[^\"]*\"\\s+)?rel=\"bookmark\"");
     
     @Override
     public IComicInfo fetchComicInfo(Uri url) throws Exception {
         System.out.println("fetching "+url.toString());
         String content = Utility.blockingReadUri(url);
         
         String img = "", alt = "", id = "", title = "",
             nextId = null, prevId = null;
         
         Matcher m = imageTagPattern.matcher(content);
         if (m.find()) {
             if (m.group(1) != null)
                 id = m.group(2);
             String imgTag = m.group(5);
             m = imagePattern.matcher(imgTag);
             if (m.find()) {
                 img = m.group(1);
             }
             m = altPattern.matcher(imgTag);
             if (m.find()) {
                 alt = m.group(1);
             }
         }
         
         if ("".equals(id)) {
             m = backupIdPattern.matcher(content);
             if (m.find()) {
                 id = m.group(1);
             }
         }
         
         m = titlePattern.matcher(content);
         if (m.find()) {
             title = m.group(1);
             String commonPart = " - Milk for Dead Hamsters";
             int pos = title.lastIndexOf(commonPart);
             if (pos != -1 && pos+commonPart.length() == title.length()) {
                 title = title.substring(0, pos);
             }
             commonPart = "m4dh: ";
             pos = title.indexOf(commonPart);
             if (pos == 0)
                 title = title.substring(commonPart.length());
         }
         
         m = prevPattern.matcher(content);
         if (m.find()) {
             prevId = m.group(1);
         }
         
         m = nextPattern.matcher(content);
         if (m.find()) {
             nextId = m.group(1);
         }
         
         M4dhComicInfo data = new M4dhComicInfo();
         data.img = Uri.parse(Html.fromHtml(img).toString());
         data.id = Html.fromHtml(id).toString();
         data.alt = Html.fromHtml(alt).toString();
         data.title = Html.fromHtml(title).toString();
         if (nextId != null)
             data.nextId = Html.fromHtml(nextId).toString();
         if (prevId != null)
             data.prevId = Html.fromHtml(prevId).toString();
         return data;
     }
 
     @Override
     public Uri fetchRandomComicUrl() throws Exception {
         return Uri.parse("http://milkfordeadhamsters.com/?random&random_cat_id=5");
     }
 
     @Override
     public Uri getFinalComicUrl() {
         return Uri.parse("http://milkfordeadhamsters.com/");
     }
 
     @Override
     public String getFirstId() {
         return "pet-fish";
     }
 
     @Override
     public IComicInfo createEmptyComicInfo() {
         return new M4dhComicInfo();
     }
 
     @Override
     public List<ArchiveItem> fetchArchive() throws Exception {
         // TODO
         List<ArchiveItem> archiveItems = new ArrayList<ArchiveItem>();
         URL url = new URL(ARCHIVE_URL);
         BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
 
         try {
             String line;
             while ((line = br.readLine()) != null) {
                 Matcher m = archiveItemPattern.matcher(line);
                 while (m.find()) {
                     ArchiveItem item = new ArchiveItem();
                     item.comicId = Html.fromHtml(m.group(1)).toString();
                     item.title = Html.fromHtml(m.group(2)).toString();
                     archiveItems.add(item);
                 }
 
                 Utility.allowInterrupt();
             }
 
         } finally {
             br.close();
         }
         return archiveItems;
     }
 
 }
