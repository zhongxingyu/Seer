 package nmd.rss.collector.feed;
 
 import com.sun.syndication.feed.synd.SyndContent;
 import com.sun.syndication.feed.synd.SyndEntry;
 import com.sun.syndication.feed.synd.SyndFeed;
 import com.sun.syndication.io.SyndFeedInput;
 
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.UUID;
 
 import static nmd.rss.collector.util.Assert.assertStringIsValid;
 import static nmd.rss.collector.util.Assert.assertValidUrl;
 import static nmd.rss.collector.util.Parameter.isValidUrl;
 
 /**
  * Author : Igor Usenko ( igors48@gmail.com )
  * Date : 28.04.13
  */
 public final class FeedParser {
 
     public static Feed parse(final String feedUrl, final String feedData) throws FeedParserException {
         assertValidUrl(feedUrl);
         assertStringIsValid(feedData);
 
         try {
             final String correctedData = stripNonValidXMLCharacters(feedData);
             final StringReader reader = new StringReader(correctedData);
             final SyndFeedInput input = new SyndFeedInput();
             final SyndFeed feed = input.build(reader);
 
             final FeedHeader header = parseHeader(feedUrl, feed);
 
             if (header == null) {
                 return null;
             }
 
             final List<FeedItem> items = new ArrayList<>();
 
             for (int i = 0; i < feed.getEntries().size(); i++) {
                 final SyndEntry entry = (SyndEntry) feed.getEntries().get(i);
                 final FeedItem item = parseItem(entry);
 
                 if (item != null) {
                     items.add(item);
                 }
             }
 
             return new Feed(header, items);
         } catch (Exception exception) {
             throw new FeedParserException(exception);
         }
 
     }
 
     private static FeedHeader parseHeader(final String feedUrl, final SyndFeed feed) {
 
        if (!isValidUrl(feed.getLink())) {
             return null;
         }
 
        final String title = createTitle(feed.getTitle(), feed.getLink());
         final String description = feed.getDescription() == null ? "" : feed.getDescription();
        final String link = feed.getLink();
 
        return new FeedHeader(UUID.randomUUID(), feedUrl, title, description, link);
     }
 
     private static FeedItem parseItem(final SyndEntry entry) {
 
        if (!isValidUrl(entry.getLink())) {
             return null;
         }
 
        final String title = createTitle(entry.getTitle(), entry.getLink());
         final String description = createDescription(entry);
        final String link = entry.getLink();
         final Date date = createDate(entry.getPublishedDate(), new Date());
         final String guid = UUID.randomUUID().toString();
 
         return new FeedItem(title, description, link, date, guid);
     }
 
     private static Date createDate(final Date date, final Date currentDate) {
         return date == null ? currentDate : date;
     }
 
     private static String createTitle(final String title, final String feedLink) {
         return (title == null) || (title.isEmpty()) ? feedLink : title;
     }
 
     private static String createDescription(final SyndEntry entry) {
         final String title = entry.getTitle();
         final List contentsList = entry.getContents();
         final StringBuilder contents = new StringBuilder();
 
         if (contentsList != null && !contentsList.isEmpty()) {
 
             for (final Object current : contentsList) {
                 contents.append(((SyndContent) current).getValue());
             }
         }
 
         final String description = entry.getDescription() == null ? "" : entry.getDescription().getValue();
 
         final String result = contents.length() == 0 ? description : contents.toString();
 
         return result.isEmpty() ? title : result;
     }
 
     private static String stripNonValidXMLCharacters(final String data) {
         final StringBuilder result = new StringBuilder();
 
         for (int i = 0; i < data.length(); i++) {
             final char current = data.charAt(i);
 
             if ((current == 0x9) || (current == 0xA) || (current == 0xD) ||
                     ((current >= 0x20) && (current <= 0xD7FF)) ||
                     ((current >= 0xE000) && (current <= 0xFFFD)) ||
                     ((current >= 0x10000) && (current <= 0x10FFFF))) {
                 result.append(current);
             }
         }
 
         return result.toString();
     }
 
     private FeedParser() {
         // empty
     }
 
 }
