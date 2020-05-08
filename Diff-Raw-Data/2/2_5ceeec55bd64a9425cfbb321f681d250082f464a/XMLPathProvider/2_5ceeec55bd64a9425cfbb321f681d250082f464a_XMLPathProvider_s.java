 package net.bensdeals.provider;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 import com.google.inject.Singleton;
 
 @Singleton
 public class XMLPathProvider implements Provider<XMLPathProvider.XMLPath> {
     public final SharedPreferences preferences;
     public final String FILE_NAME = XMLPath.class.getSimpleName();
     public final String PREF_KEY = "PATH";
     public XMLPath path;
 
     @Inject
     public XMLPathProvider(Context context) {
         preferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
     }
 
     @Override
     public XMLPath get() {
         if (path == null)
             return XMLPath.fromString(preferences.getString(PREF_KEY, XMLPath.HOMEPAGE.name()));
         return path;
     }
 
     public XMLPathProvider set(final XMLPath xmlPath) {
         this.path = xmlPath;
         new AsyncTask<Void, Void, Void>() {
             @Override
             protected Void doInBackground(Void... voids) {
                 preferences.edit()
                         .putString(PREF_KEY, xmlPath.name())
                         .commit();
                 return null;
             }
         }.execute((Void) null);
         return this;
     }
 
     public enum XMLPath {
         HOMEPAGE("http://bensbargains.net/rss/", "Homepage"),
         DEALS("http://bensbargains.net/deals/rss/", "Deals"),
         COUPONS("http://bensbargains.net/coupons/rss/", "Coupons"),
         REBATES("http://bensbargains.net/rebates/rss/", "Rebates"),
        FEEBIES("http://bensbargains.net/free-stuff/rss/", "Feebies"),
         MOSTCLICKED("http://bensbargains.net/most/popular/rss/", "Most Clicked"),
         MOSTDISCUSSED("http://bensbargains.net/most/discussed/rss/", "Most Discussed"),
         MOSTBOOKMARKED("http://bensbargains.net/most/bookmarked/rss/", "Most Bookmarked"),
         MOSTVIEWED("http://bensbargains.net/most/viewed/rss/", "Most Viewed");
         private String path;
         private String title;
 
         XMLPath(String path, String title) {
             this.path = path;
             this.title = title;
         }
 
         public String getPath() {
             return path;
         }
 
         public String getTitle() {
             return title;
         }
 
         public static XMLPath fromString(String pathName) {
             for (XMLPath xmlPath : values()) {
                 if (xmlPath.name().equalsIgnoreCase(pathName)) return xmlPath;
             }
             return HOMEPAGE;
         }
     }
 }
