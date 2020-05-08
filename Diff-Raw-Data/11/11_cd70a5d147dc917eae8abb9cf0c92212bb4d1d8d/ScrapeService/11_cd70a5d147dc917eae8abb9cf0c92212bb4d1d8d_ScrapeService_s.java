 package org.quuux.eyecandy;
 
 import android.app.IntentService;
 import android.content.Intent;
 import com.android.volley.Request;
 import com.android.volley.RequestQueue;
 import com.android.volley.Response;
 import com.android.volley.VolleyError;
 import com.android.volley.toolbox.Volley;
 import org.quuux.eyecandy.utils.GsonRequest;
 import org.quuux.eyecandy.utils.OkHttpStack;
 import org.quuux.orm.Database;
 import org.quuux.orm.Session;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class ScrapeService extends IntentService implements ScrapeCompleteListener {
 
     private static final String TAG = Log.buildTag(ScrapeService.class);
 
     public static String ACTION_SCRAPE_COMPLETE = "org.quuux.eyecandy.intent.action.SCRAPE_COMPLETE";
 
     static {
         Database.attach(Image.class);
     }
 
     private static final String SUBREDDITS[] = {
 
             "Cinemagraphs",
 
 //            "earthporn",
 //            "villageporn",
 //            "cityporn",
 //            "spaceporn",
 //            "waterporn",
 //            "abandonedporn",
 //            "animalporn",
 //            "humanporn",
 //            "botanicalporn",
 //            "adrenalineporn",
 //            "destructionporn",
 //            "movieposterporn",
 //            "albumartporn",
 //            "machineporn",
 //            //"newsporn",
 //            "geekporn",
 //            "bookporn",
 //            //"mapporn",
 //            "adporn",
 //            "designporn",
 //            "roomporn",
 //            //"militaryporn",
 //            //"historyporn",
 //            //"quotesporn",
 //            "skyporn",
 //            "fireporn",
 //            "infrastructureporn",
 //            "macroporn",
 //            "instrumentporn",
 //            "climbingporn",
 //            "architectureporn",
 //            "artporn",
 //            "cemeteryporn",
 //            //"carporn",
 //            "fractalporn",
 //            "exposureporn",
 //            //"gunporn",
 //            //"culinaryporn",
 //            "dessertporn",
 //            "agricultureporn",
 //            "boatporn",
 //            "geologyporn",
 //            "futureporn",
 //            "winterporn",
 //            "JoshuaTree",
 //            "NZPhotos",
 //            "EyeCandy",
 //            "ruralporn",
 //            "spaceart"
 //            //"foodporn"
     };
 
     private RequestQueue mRequestQueue;
 
     private int mTaskCount = 0;
     private EyeCandyDatabase mDatabase;
 
     public ScrapeService() {
         super(ScrapeService.class.getName());
     }
 
     @Override
     protected void onHandleIntent(Intent intent) {
         mRequestQueue = Volley.newRequestQueue(this, new OkHttpStack());
 
         mDatabase = EyeCandyDatabase.getInstance(this);
  
         for (int i=0; i<SUBREDDITS.length; i++) {
             Log.d(TAG, "scraping %s", SUBREDDITS[i]);
             scrapeImgur(SUBREDDITS[i]);
             mTaskCount++;
         }
 
     }
 
     @Override
     public void onScrapeComplete(int numScraped) {
         mTaskCount--;
 
         //Log.d(TAG, "scrape complete: %d", numScraped);
 
         final Intent intent = new Intent(ACTION_SCRAPE_COMPLETE);
         sendBroadcast(intent);
 
         if (mTaskCount == 0) {
             stopSelf();
         }
     }
 
 
     private void scrapeImgur(final String subreddit) {
 
         final String url = String.format("http://imgur.com/r/%s.json", subreddit);
 
         final GsonRequest<ImgurImageList> request = new GsonRequest<ImgurImageList>(
                 ImgurImageList.class,
                 Request.Method.GET,
                 url,
                 new Response.Listener<ImgurImageList>() {
                     @Override
                     public void onResponse(final ImgurImageList response) {
 
                         final Session session = mDatabase.createSession();
 
                         for(final ImgurImage i : response.data) {
                            Log.d(TAG, "scraped image - %s - animated = %s / %s", i.getUrl(), i.animated, i.isAnimated());

                             final Image img =  Image.fromImgur(i.getUrl(), i.title, i.isAnimated());
                             session.add(img);

                         }
 
                         session.commit();
 
                         onScrapeComplete(response.data.size());
                     }
                 },
                 new Response.ErrorListener() {
                     @Override
                     public void onErrorResponse(final VolleyError error) {
                         Log.d(TAG, "Error scraping %s - %s", url, error.getMessage());
                     }
                 }
         );
 
         mRequestQueue.add(request);
     }
 
     private static class ImgurImage {
         String hash;
         String title;
         String mimetype;
         String ext;
         int width;
         int height;
         int size;
         String animated;
 
         public String getUrl() {
             return String.format("http://imgur.com/%s%s", hash, ext);
         }
 
 
         public boolean isAnimated() {
             return "1".equals(animated);
         }
     }
 
     private static class ImgurImageList {
         public List<ImgurImage> data = new ArrayList<ImgurImage>();
     }
 }
