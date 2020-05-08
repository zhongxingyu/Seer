 /*
  * Copyright (c) Mattia Barbon <mattia@barbon.org>
  * distributed under the terms of the MIT license
  */
 
 package org.barbon.mangaget.scrape;
 
 import android.content.Context;
 
 import android.os.Environment;
 
 import android.test.InstrumentationTestCase;
 
 import java.io.File;
 
 import org.barbon.mangaget.data.DB;
 
 import org.barbon.mangaget.tests.DummyDownloader;
 import org.barbon.mangaget.tests.R;
 
 import org.barbon.mangaget.scrape.animea.Scraper;
 
 public class ScraperTest extends InstrumentationTestCase {
     private DummyDownloader downloader;
     private DB db;
     private Context testContext;
     private File tempDir;
 
     private class Progress implements Scraper.OnChapterDownloadProgress {
         public boolean started, complete;
 
         @Override
         public void downloadStarted() {
             started = true;
         }
 
         @Override
         public void downloadProgress(int current, int total) {
         }
 
         @Override
         public void downloadComplete(boolean success) {
             complete = true;
         }
     }
 
     @Override
     public void setUp() throws Exception {
         testContext = getInstrumentation().getContext()
             .createPackageContext("org.barbon.mangaget.tests", 0);
         downloader = new DummyDownloader(testContext);
 
         // set up dummy pages
         String base =
             "http://manga.animea.net/papillon-hana-to-chou-chapter-1-page-";
 
         downloader.addUrl(base + "1.html", R.raw.papillon_c1_p1_html);
 
         for (int i = 2; i < 46; ++i)
             downloader.addUrl(base + Integer.toString(i) + ".html",
                               R.raw.papillon_c1_p2_html);
 
         // set up dummy images
         String p1 = "http://s2-a.animea-server.net/5338%2F1_JHMCN%2F00_fuuchifighters.jpg" ;
         String pn = "http://s2-a.animea-server.net/5338%2F1_JHMCN%2F001_cover.jpg" ;
 
         downloader.addUrl(p1, R.raw.papillon_dummy_img);
         downloader.addUrl(pn, R.raw.papillon_dummy_img);
 
         // set up dummy database
         db = DB.getNewInstance(getInstrumentation().getTargetContext(),
                                "manga_test");
 
         // download directory
         File externalStorage = Environment.getExternalStorageDirectory();
         tempDir = new File(externalStorage, "MangaGetTest");
         tempDir.mkdir();
     }
 
     private long setUpTestManga() {
         return db.insertManga(
             "Title", "MangaGetTest/Dummy-%chapter%.cbz",
             "http://manga.animea.net/papillon-hana-to-chou.html");
     }
 
     private long setUpTestChapter(long mangaId) {
         return db.insertChapter(
             mangaId, 1, 45, "Chapter title",
             "http://manga.animea.net/papillon-hana-to-chou-chapter-1-page-1.html");
     }
 
     @Override
     public void tearDown() throws Exception {
     }
 
     public void testFullDownload() throws Throwable {
         final long mangaId = setUpTestManga();
         final long chapterId = setUpTestChapter(mangaId);
         final String targetCbz = new File(tempDir, "Dummy-1.cbz")
             .getAbsolutePath();
         final String tempDirString = tempDir.getAbsolutePath();
         final Scraper scraper = new Scraper(db, downloader);
         final Progress progress = new Progress();
 
         class UiTask implements Runnable {
             @Override
             public void run() {
                 scraper.downloadChapter(
                     chapterId, targetCbz, tempDirString, progress);
             }
         }
 
         UiTask uiTask = new UiTask();
 
         runTestOnUiThread(uiTask);
 
         while (!progress.complete)
             Thread.sleep(500);
 
         System.out.println("Temp dir: " + tempDirString);
         System.out.println("Target: " + targetCbz);
 
         assertTrue(progress.started);
         assertTrue(progress.complete);
         assertTrue(new File(targetCbz).exists());
     }
 }
