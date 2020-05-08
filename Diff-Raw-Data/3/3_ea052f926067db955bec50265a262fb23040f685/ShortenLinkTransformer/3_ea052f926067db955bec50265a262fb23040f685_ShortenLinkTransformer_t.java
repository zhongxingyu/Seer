 /**
  *
  */
 package kwitches.text.hyperlink;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import kwitches.text.TextTransformer;
 
 import com.google.appengine.api.urlfetch.FetchOptions;
 import com.google.appengine.api.urlfetch.HTTPMethod;
 import com.google.appengine.api.urlfetch.HTTPRequest;
 import com.google.appengine.api.urlfetch.HTTPResponse;
 import com.google.appengine.api.urlfetch.URLFetchService;
 import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
 
 /**
  * @author thrakt
  *
  */
 public class ShortenLinkTransformer
     extends HyperlinkTransformAbstract {
 
     private static final String ARTICLE_TYPE = "shorten";
     private static final String REGEXP_URL_STRING = "^http://((t.co)|(bit.ly)|(j.mp))/.*";
 
     /* (非 Javadoc)
      * @see kwitches.text.hyperlink.HyperlinkTransformInterface#getArticleType()
      */
     public String getArticleType() {
         return ARTICLE_TYPE;
     }
 
     /* (非 Javadoc)
      * @see kwitches.text.LineMessageTransformInterface#getRegexp()
      */
     public String getRegexp() {
         return REGEXP_URL_STRING;
     }
 
     /* (非 Javadoc)
      * @see kwitches.text.LineMessageTransformInterface#transform(java.lang.String)
      */
     @SuppressWarnings("serial")
     public String transform(String rawString) {
         Pattern p = Pattern.compile(this.getRegexp(), Pattern.CASE_INSENSITIVE);
         Matcher m = p.matcher(rawString);
         if (!m.matches()) {
             return rawString;
         }
 
         String result = m.group(0);
         try {
             URLFetchService ufs = URLFetchServiceFactory.getURLFetchService();
             HTTPRequest request =
                 new HTTPRequest(
                     new URL(result),
                     HTTPMethod.GET,
                     FetchOptions.Builder.withDeadline(5.0));
             HTTPResponse response = ufs.fetch(request);
            if (response.getFinalUrl() != null
                && result.equals(response.getFinalUrl().toString()) == false) {
                 result = TextTransformer.transform(response.getFinalUrl().toString());
             }
         } catch (MalformedURLException e) {
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         return result;
     }
 
 }
