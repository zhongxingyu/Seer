 package ch.adorsaz.loungeDroid.servercom;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import ch.adorsaz.loungeDroid.article.Article;
 import ch.adorsaz.loungeDroid.article.ToDisplay;
 import ch.adorsaz.loungeDroid.exception.AuthenticationFailLoungeException;
 import ch.adorsaz.loungeDroid.exception.GetArticleListException;
 import ch.adorsaz.loungeDroid.exception.ParseArticleException;
 import android.os.AsyncTask;
 import android.util.Log;
 
 /*
  * This interface implements minimum required to connect to a server.
  */
 public class ArticleListGetter extends
         AsyncTask<ToDisplay, Object, List<Article>> {
     private SessionManager mSessionManager = SessionManager.getInstance(null);
 
     /* Some urls needed to get feeds */
     private final static String ARTICLES_PAGE_RSSLOUNGE = "/item/list";
 
     public ArticleListGetter() {
 
     }
 
     @Override
     protected void onPreExecute() {
         mSessionManager = SessionManager.getInstance(null);
     }
 
     @Override
     protected List<Article> doInBackground(ToDisplay... toDisplay) {
         List<Article> articles = null;
         try {
             articles = getArticles();
         } catch (AuthenticationFailLoungeException e) {
             // TODO Pass to offline mode
             Log.e(SessionManager.LOG_DEBUG_LOUNGE,
                     "Cannot log in. Check your connection. We'll check if we have saved data before.");
         } catch (GetArticleListException e) {
             // TODO Pass to offline mode
             Log.e(SessionManager.LOG_DEBUG_LOUNGE,
                     "Cannot get article list, are you correctly logged ? Using saved data if available.");
         } catch (ParseArticleException e) {
             // TODO Make Toast
             Log.e(SessionManager.LOG_DEBUG_LOUNGE,
                     "Cannot parse JSON response. Trying to display already parsed articles. Please contact developpers.");
         }
         return articles;
     }
 
     @Override
     protected void onPostExecute(List<Article> allArticles) {
         // TODO : Fill the list activity
     }
 
     private List<Article> getArticles()
         throws GetArticleListException,
         AuthenticationFailLoungeException,
         ParseArticleException {
         List<Article> articleList = new LinkedList<Article>();
         JSONArray messages = null;
 
         JSONObject jsonResponse = mSessionManager.serverRequest(
                 ARTICLES_PAGE_RSSLOUNGE, SessionManager.JSON_GET_RSSLOUNGE);
 
         // TODO : check if message object array exists if all feeds are read and
         // show only unread.
         try {
            messages = jsonResponse.getJSONArray("messages");
         } catch (JSONException e) {
             throw new GetArticleListException();
         }
 
         try {
             for (int i = 0; i < messages.length(); i++) {
                 JSONObject thisMessage = messages.getJSONObject(i);
                 int id = thisMessage.getInt("id");
                 String datetime = thisMessage.getString("datetime");
                 int day = Character.getNumericValue(datetime.charAt(8)) * 10
                         + Character.getNumericValue(datetime.charAt(9));
                 int month = Character.getNumericValue(datetime.charAt(5)) * 10
                         + Character.getNumericValue(datetime.charAt(6));
                 String subject = thisMessage.getString("title");
                 String content = thisMessage.getString("content");
                 String author = thisMessage.getString("name");
                 String link = thisMessage.getString("link");
                 String icon = thisMessage.getString("icon");
                 Boolean isRead = thisMessage.getInt("unread") == 1;
                 Boolean isStarred = thisMessage.getInt("starred") == 1;
 
                 Article article = new Article(id, day, month, subject, content,
                         author, link, icon, isRead, isStarred);
                 articleList.add(article);
             }
         } catch (JSONException e) {
             throw new ParseArticleException();
         }
 
         if (articleList.get(0) != null) {
             Log.d(SessionManager.LOG_DEBUG_LOUNGE, "First article : "
                     + articleList.get(0).toString());
         }
         return articleList;
     }
 }
