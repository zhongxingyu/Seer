 package org.barbon.mangaget.tests;
 
 import android.content.Context;
 
 import android.content.pm.PackageManager;
 
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 
 import android.test.InstrumentationTestCase;
 
 import org.barbon.mangaget.data.DB;
 
 import org.barbon.mangaget.scrape.Downloader;
 
 import org.barbon.mangaget.tests.DummyDownloader;
 
 public class Utils {
     private static boolean setUp = false;
     private static DummyDownloader downloader;
 
     public static long firstDummyManga, secondDummyManga;
 
     public static boolean networkConnected(Context context) {
         ConnectivityManager manager =
             (ConnectivityManager) context.getSystemService(
                 Context.CONNECTIVITY_SERVICE);
         NetworkInfo active = manager.getActiveNetworkInfo();
 
         System.out.println("Network connected: " +
                            Boolean.toString(active != null &&
                                             active.isConnected()));
 
         return active != null && active.isConnected();
     }
 
     public static void createDatabase(InstrumentationTestCase test) {
         Context targetContext = test.getInstrumentation().getTargetContext();
 
         if (setUp) {
             // force DB creation
             DB.getInstance(null).insertManga("", "", "");
 
             // scrub database
             SQLiteDatabase db = targetContext.openOrCreateDatabase(
                 "manga_test", 0, null);
 
             db.execSQL("DELETE FROM pages");
             db.execSQL("DELETE FROM chapters");
             db.execSQL("DELETE FROM manga");
 
             db.close();
 
             return;
         }
 
         setUp = true;
 
         // set up dummy database
         targetContext.deleteDatabase("manga_test");
 
         DB db = DB.getNewInstance(targetContext, "manga_test");
 
         DB.setInstance(db);
     }
 
     public static void setupTestAnimeaEnvironment(InstrumentationTestCase test)
             throws Exception {
         createDatabase(test);
 
         Context testContext = test.getInstrumentation().getContext()
             .createPackageContext("org.barbon.mangaget.tests", 0);
         downloader = new DummyDownloader(testContext);
 
         final String baseUrl = "http://manga.animea.net";
         final String baseImage = "http://s2-a.animea-server.net";
 
         // set up dummy search results
         downloader.addUrl(baseUrl + "/search.html?title=",
                           R.raw.animea_results_html);
 
         // set up dummy pages
         String base = baseUrl + "/papillon-hana-to-chou-chapter-1-page-";
 
         downloader.addUrl(base + "1.html", R.raw.animea_papillon_c1_p1_html);
 
         for (int i = 2; i < 46; ++i)
             downloader.addUrl(base + Integer.toString(i) + ".html",
                               R.raw.animea_papillon_c1_p2_html);
 
         // chapter list page
         downloader.addUrl(baseUrl + "/papillon-hana-to-chou.html?skip=1",
                           R.raw.animea_papillon_chapters_html);
 
         // set up dummy images
         String p1 = baseImage + "/5338%2F1_JHMCN%2F00_fuuchifighters.jpg" ;
         String pn = baseImage + "/5338%2F1_JHMCN%2F001_cover.jpg" ;
 
         downloader.addUrl(p1, R.raw.animea_papillon_dummy_img);
         downloader.addUrl(pn, R.raw.animea_papillon_dummy_img);
 
         Downloader.setInstance(downloader);
     }
 
     public static void setupTestAnimeaDatabase(InstrumentationTestCase test) {
         // setup DB before getting the activity
         DB db = DB.getInstance(null);
 
         long m1 = db.insertManga(
            "Title1", "MangaGetTest/Dummy1-%chapter%.cbz",
             "http://manga.animea.net/dummy1.html");
         long m2 = db.insertManga(
            "Title2", "MangaGetTest/Dummy2-%chapter%.cbz",
             "http://manga.animea.net/dummy2.html");
 
         long c1 = db.insertChapter(
             m1, 1, 45, "Chapter 1",
             "http://manga.animea.net/dummy1-1.html");
         long c2 = db.insertChapter(
             m2, 1, 45, "Chapter 1",
             "http://manga.animea.net/dummy2-1.html");
         long c3 = db.insertChapter(
             m2, 2, 45, "Chapter 2",
             "http://manga.animea.net/dummy2-2.html");
 
         firstDummyManga = m1;
         secondDummyManga = m2;
 
         final String baseUrl = "http://manga.animea.net";
 
         // set up some dummy download pages
 
         downloader.addUrl(baseUrl + "/dummy1.html?skip=1",
                           R.raw.animea_papillon_chapters_html);
     }
 
     public static Downloader.DownloadDestination getPage(
             InstrumentationTestCase test, int resource, String base) {
         Context testContext;
 
         try {
             testContext = test.getInstrumentation().getContext()
                 .createPackageContext("org.barbon.mangaget.tests", 0);
         }
         catch (PackageManager.NameNotFoundException e) {
             throw new RuntimeException(e);
         }
 
         Downloader.DownloadDestination dest =
             new Downloader.DownloadDestination();
 
         dest.stream = testContext.getResources().openRawResource(resource);
         dest.encoding = "utf-8";
         dest.baseUrl = base;
 
         return dest;
     }
 }
