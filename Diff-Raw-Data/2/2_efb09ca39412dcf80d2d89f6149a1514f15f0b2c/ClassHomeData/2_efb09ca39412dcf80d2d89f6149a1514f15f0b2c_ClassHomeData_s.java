 /**
  *
  * @author David Findley (ThinksInBits)
  * 
  * The source for this application may be found in its entirety at 
  * https://github.com/ThinksInBits/OU-Mobile-App
  * 
  * This application is published on the Google Play Store under
  * the title: OU Mobile Alpha:
  * https://play.google.com/store/apps/details?id=com.geared.ou
  * 
  * If you want to follow the official development of this application
  * then check out my Trello board for the project at:
  * https://trello.com/board/ou-app/4f1f697a28390abb75008a97
  * 
  * Please email me at: thefindley@gmail.com with questions.
  * 
  */
 
 package com.geared.ou;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 
 import com.geared.ou.D2LSourceGetter.SGError;
 
 /**
  *
  * This is a data wrapper class that supports the ClassHomeActivity class. It is
  * initialized in the Application object. Class news data that
  * is either pulled from D2L or the local database.
  * 
  */
 public class ClassHomeData {
     
     public class NewsItem {
         protected String name;
         protected String content;
         protected int id;
         
         public NewsItem(String name, String content, int id) {
             this.name = name;
             this.content = content;
             this.id = id;
         }
         
         public int getId() {
             return id;
         }
         
         public String getContent() {
             return content;
         }
         
         public String getName() {
             return name;
         }
     }
     
     private ArrayList<NewsItem> newsItems;
     private String classNewsSource;
     protected Date lastUpdate;
     protected Boolean force;
     protected static final String NEWS_URL_PRE = "http://learn.ou.edu/d2l/lp/homepage/home.d2l?ou=";
     protected ClassesData.Course course;
     protected OUApplication app;
     private static final long UPDATE_INTERVAL = 86400000L; // 24 Hours
     
     public ClassHomeData(OUApplication app, ClassesData.Course course) {
         force = false;
         this.app = app;
         this.course = course;
         lastUpdate = new Date();
         lastUpdate.setTime(lastUpdate.getTime() - UPDATE_INTERVAL);
         newsItems = new ArrayList<NewsItem>();
     }
     
     public Boolean update() {
         app.updateSGCredentials();
         D2LSourceGetter sg = app.getSourceGetter();
         // If this update was force, unset force
         if (force) {
             force = false;
         }
         SGError result = sg.pullSource(NEWS_URL_PRE+course.getOuId());
         if (result != SGError.NO_ERROR)
             return false;
         classNewsSource = sg.getPulledSource();
         if (!pullNewsItems())
             return false;
         return true;
     }
     
     private Boolean pullNewsItems() {
         Document doc = Jsoup.parse(classNewsSource);
         classNewsSource = null;
         newsItems.clear();
         
         /***********************************************************************
          *                      START specialized code
          **********************************************************************/
         Elements results = doc.getElementsByAttributeValueMatching("summary", ".*news.*");
         if (results.size() != 1)
             return false;
         Element newsTable = results.first();
         Elements newsList = newsTable.children().first().children();
         if (newsList.size() < 3) // The first two elemts are not news items. So start on the third. <3
         {
             if (newsList.size() == 1) { // Assuming that the 1 tr is saying 0 news items.
                 newsItems.add(new NewsItem("No News", "There are no news items for this course", 0));
             }
             else
                 return false;
         }
         // Loop through each newsTr
         for (int i = 2; i < newsList.size(); i+=2) {
            String name = newsList.get(i).text();
             String content = newsList.get(i+1).text();
             
             NewsItem n = new NewsItem(name, content, i+course.getOuId());
             newsItems.add(n);
         }
         /***********************************************************************
          *                     END specialized code
          ***********************************************************************/
         
         lastUpdate = new Date();
         writeToDb();
         return true;
     }
     
     private Boolean populateFromDb() {
         newsItems.clear();
         SQLiteDatabase db = app.getDb();
         Cursor result = db.rawQuery("select * from course_news where user='"+app.getUser()+"' and ou_id="+course.getOuId(), null);
         if (result.getCount() < 1) {
             return false;
         }
         int counter = 0;
         while(result.moveToNext())
         {
             if(counter == 0)        
                 lastUpdate.setTime(((long)(result.getInt(result.getColumnIndex(DbHelper.C_CN_LAST_UPDATE))))*1000);
             String name = result.getString(result.getColumnIndex(DbHelper.C_CN_NAME));
             String content = result.getString(result.getColumnIndex(DbHelper.C_CN_CONTENT));
             int id = result.getInt(result.getColumnIndex(DbHelper.C_CN_ID));
             NewsItem n = new NewsItem(name, content, id);
             newsItems.add(n);
             counter++;
         }
         
         db.close();
         return true;
     }
     
     public Boolean needsUpdate() {
         if (newsItems.isEmpty())
             populateFromDb();
         if (newsItems.isEmpty())
             return true;
         if ((new Date().getTime() - lastUpdate.getTime() > UPDATE_INTERVAL))
             return true;
         
         return false;
     }
     
     private Boolean writeToDb() {
         SQLiteDatabase db = app.getDb();
         db.rawQuery("delete from course_news where user='"+app.getUser()+"' and ou_id="+course.getOuId(), null);
         ContentValues values = new ContentValues();
         
         for (NewsItem n : newsItems) {
             values.clear();
             values.put(DbHelper.C_CN_ID, n.getId());
             values.put(DbHelper.C_CN_NAME, n.getName());
             values.put(DbHelper.C_CN_USER, app.getUser());
             values.put(DbHelper.C_CN_OUID, course.getOuId());
             values.put(DbHelper.C_GRA_LAST_UPDATE, (int)((lastUpdate.getTime())/1000));
             values.put(DbHelper.C_CN_CONTENT, n.getContent());
             db.insert(DbHelper.T_COURSENEWS, null, values);
         }
         db.close();
         return true;
     }
     
     public void forceNextUpdate() {
         force = true;
     }
     
     public ArrayList<NewsItem> getNewsItems() {
         return newsItems;
     }
     
     public Date getLastUpdate() {
         return lastUpdate;
     }
 }
