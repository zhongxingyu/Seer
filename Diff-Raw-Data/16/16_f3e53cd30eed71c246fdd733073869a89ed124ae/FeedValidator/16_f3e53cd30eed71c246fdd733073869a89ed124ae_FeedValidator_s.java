 package de.hdodenhof.holoreader.backend.parser;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 import org.xmlpull.v1.XmlPullParserFactory;
 
 import de.hdodenhof.holoreader.backend.exception.InvalidFeedException;
 
 public class FeedValidator {
 
     @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger("FeedValidator");
 
     public static final String RESULT_NAME = "name";
     public static final String RESULT_URL = "url";
 
     public static Map<String, String> parseFeed(String urlString) throws InvalidFeedException {
         String resultUrl = urlString;
         String name = null;
         String error = null;
 
         try {
             URL url = prepareUrl(urlString);
 
             logger.info("Working on " + url.toString());
 
             URLConnection connection = url.openConnection();
             connection.setRequestProperty("User-agent", "Holo Reader/1.0");
             connection.setConnectTimeout(5000);
             connection.setReadTimeout(5000);
             connection.connect();
             String contentType = connection.getContentType();
             if (contentType.contains("xml")) {
                 logger.info("XML content type detected");
                 InputStream inputStream = connection.getInputStream();
                 try {
                     name = validateFeedAndExtractName(inputStream);
                     logger.info("Feed " + url.toString() + " is valid!");
                 } catch (InvalidFeedException e) {
                     error = e.getMessage();
                     logger.warning("Feed " + url.toString() + " is invalid!");
                 } finally {
                     inputStream.close();
                 }
             } else {
                 logger.info("Invalid content type, trying to discover feed");
                 String alternateUrl = discoverFeed(url);
                 if (alternateUrl == null) {
                     logger.warning("No feed discovered, aborting!");
                     throw new InvalidFeedException();
                 } else {
                     logger.info("Discovered " + alternateUrl);
                     URLConnection secondConnection = new URL(alternateUrl).openConnection();
                     secondConnection.setRequestProperty("User-agent", "Holo Reader/1.0");
                     secondConnection.setConnectTimeout(2000);
                     secondConnection.setReadTimeout(2000);
                     secondConnection.connect();
 
                     InputStream inputStream = secondConnection.getInputStream();
                     try {
                         name = validateFeedAndExtractName(inputStream);
                         logger.info("Discovered feed " + alternateUrl + " is valid.");
                         resultUrl = alternateUrl;
                     } catch (InvalidFeedException e) {
                         error = e.getMessage();
                         logger.warning("Discovered feed " + alternateUrl + " is invalid!");
                     } finally {
                         inputStream.close();
                     }
                 }
             }
 
             if (name == null) {
                 throw new InvalidFeedException(error);
             }
         } catch (IOException e) {
             throw new InvalidFeedException("IOEXCEPTION");
         } catch (Exception e) {
             throw new InvalidFeedException();
         }
 
         Map<String, String> result = new HashMap<String, String>();
         result.put(RESULT_NAME, name);
         result.put(RESULT_URL, resultUrl);
 
         return result;
     }
 
     private static String validateFeedAndExtractName(InputStream inputStream) throws InvalidFeedException {
         String name = "";
         boolean foundName = false;
         boolean isFeed = false;
         boolean isArticle = false;
         boolean hasContent = false;
         boolean hasSummary = false;
 
         try {
 
             XmlPullParserFactory parserFactory = XmlPullParserFactory.newInstance();
             parserFactory.setNamespaceAware(true);
             XmlPullParser pullParser = parserFactory.newPullParser();
             pullParser.setInput(inputStream, null);
 
             int eventType = pullParser.getEventType();
 
             while (eventType != XmlPullParser.END_DOCUMENT) {
                 if (eventType == XmlPullParser.START_TAG) {
                     String currentTag = pullParser.getName();
                     String currentPrefix = pullParser.getPrefix();
 
                     if (currentPrefix == null) {
                         currentPrefix = "";
                     }
 
                     if (currentTag.equalsIgnoreCase("rss") || currentTag.equalsIgnoreCase("feed") || currentTag.equalsIgnoreCase("rdf")) {
                         isFeed = true;
                     } else if (currentTag.equalsIgnoreCase("title") && isFeed && foundName == false) {
                         name = pullParser.nextText();
                         foundName = true;
                     } else if ((currentTag.equalsIgnoreCase("item") || currentTag.equalsIgnoreCase("entry")) && isFeed) {
                         isArticle = true;
                     } else if (((currentTag.equalsIgnoreCase("encoded") && currentPrefix.equalsIgnoreCase("content")) || (currentTag
                             .equalsIgnoreCase("content") && currentPrefix.equalsIgnoreCase(""))) && isArticle == true) {
                         hasContent = true;
                     } else if ((currentTag.equalsIgnoreCase("summary") || currentTag.equalsIgnoreCase("description")) && isArticle == true
                             && currentPrefix.equalsIgnoreCase("")) {
                         hasSummary = true;
                     }
                 } else if (eventType == XmlPullParser.END_TAG) {
                     String currentTag = pullParser.getName();
 
                     if ((currentTag.equalsIgnoreCase("item") || currentTag.equalsIgnoreCase("entry")) && isFeed) {
                         break;
                     }
                 }
                 eventType = pullParser.next();
             }
 
             inputStream.close();
         } catch (IOException e) {
             throw new InvalidFeedException("IOEXCEPTION");
         } catch (XmlPullParserException e) {
             throw new InvalidFeedException("XMLPULLPARSEREXCEPTION");
         }
 
         if (isFeed && (hasContent || hasSummary)) {
             return name;
         } else if (isFeed) {
             throw new InvalidFeedException("NO_CONTENT");
         } else {
             throw new InvalidFeedException("NO_FEED");
         }
     }
 
     private static String discoverFeed(URL url) {
         logger.info("Trying to discover feeds from " + url.toString());
 
         try {
             Document document = Jsoup.connect(url.toString()).userAgent("Holo Reader/1.0").timeout(2000).get();
             String rssUrl = document.select("link[rel=alternate][type=application/rss+xml]").attr("href");
             if (rssUrl == null || rssUrl == "") {
                 rssUrl = document.select("link[rel=alternate][type=application/atom+xml]").attr("href");
             }
 
             logger.info("Found " + rssUrl);
 
             if (rssUrl == null || rssUrl == "") {
                 return null;
             } else {
                 return rssUrl;
             }
         } catch (IOException e) {
             return null;
         }
     }
 
     private static URL prepareUrl(String url) throws MalformedURLException, InvalidFeedException {
         URL parsedUrl = null;
 
         if (url.length() < 7 || !url.substring(0, 7).equalsIgnoreCase("http://")) {
             url = "http://" + url;
         }
 
         Pattern pattern = PATTERN_WEB;
         Matcher matcher = pattern.matcher(url);
 
         if (matcher.matches()) {
             parsedUrl = new URL(url);
         } else {
             throw new InvalidFeedException();
         }
 
         return parsedUrl;
     }
 
     /* @formatter:off */
     /**
      * based on AOSP
      */
     private static final Pattern PATTERN_WEB = Pattern.compile(new StringBuilder()
             .append("((?:(http|Http):")
             .append("\\/\\/(?:(?:[a-zA-Z0-9\\$\\-\\_\\.\\+\\!\\*\\'\\(\\)")
             .append("\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,64}(?:\\:(?:[a-zA-Z0-9\\$\\-\\_")
             .append("\\.\\+\\!\\*\\'\\(\\)\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,25})?\\@)?)?")
             .append("((?:(?:[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}\\.)+")
             // named host
             .append("(?:")
             // plus top level domain
             .append("(?:aero|arpa|asia|a[cdefgilmnoqrstuwxz])")
             .append("|(?:biz|b[abdefghijmnorstvwyz])")
             .append("|(?:cat|com|coop|c[acdfghiklmnoruvxyz])")
             .append("|d[ejkmoz]")
             .append("|(?:edu|e[cegrstu])")
             .append("|f[ijkmor]")
             .append("|(?:gov|g[abdefghilmnpqrstuwy])")
             .append("|h[kmnrtu]")
             .append("|(?:info|int|i[delmnoqrst])")
             .append("|(?:jobs|j[emop])")
             .append("|k[eghimnrwyz]")
             .append("|l[abcikrstuvy]")
             .append("|(?:mil|mobi|museum|m[acdghklmnopqrstuvwxyz])")
             .append("|(?:name|net|n[acefgilopruz])")
             .append("|(?:org|om)")
             .append("|(?:pro|p[aefghklmnrstwy])")
             .append("|qa")
             .append("|r[eouw]")
             .append("|s[abcdeghijklmnortuvyz]")
             .append("|(?:tel|travel|t[cdfghjklmnoprtvwz])")
             .append("|u[agkmsyz]")
             .append("|v[aceginu]")
             .append("|w[fs]")
             .append("|y[etu]")
             .append("|z[amw]))")
             .append("|(?:(?:25[0-5]|2[0-4]")
             // or ip address
             .append("[0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(?:25[0-5]|2[0-4][0-9]")
             .append("|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(?:25[0-5]|2[0-4][0-9]|[0-1]")
             .append("[0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(?:25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}")
             .append("|[1-9][0-9]|[0-9])))")
             .append("(?:\\:\\d{1,5})?)") // plus option port number
             .append("(\\/(?:(?:[a-zA-Z0-9\\;\\/\\?\\:\\@\\&\\=\\#\\~") // plus option query params
             .append("\\-\\.\\+\\!\\*\\'\\(\\)\\,\\_])|(?:\\%[a-fA-F0-9]{2}))*)?")
             .append("(?:\\b|$)").toString());
 }
