 /*
  * Copyright (c) Mattia Barbon <mattia@barbon.org>
  * distributed under the terms of the MIT license
  */
 
 package org.barbon.mangaget.scrape;
 
 import android.content.ContentValues;
 import android.content.Context;
 
 import android.database.Cursor;
 
 import android.os.Environment;
 
 import android.test.InstrumentationTestCase;
 
 import java.io.File;
 
 import java.util.List;
 
 import org.barbon.mangaget.data.DB;
 
 import org.barbon.mangaget.scrape.Downloader;
 
 import org.barbon.mangaget.tests.DummyDownloader;
 import org.barbon.mangaget.tests.R;
 import org.barbon.mangaget.tests.Utils;
 
 public class ScraperTest extends InstrumentationTestCase {
     private DB db;
     private Context testContext;
     private File tempDir;
 
     private class OperationProgress
             implements Scraper.OnOperationStatus {
         public boolean started, complete;
 
         @Override
         public void operationStarted() {
             started = true;
         }
 
         @Override
         public void operationComplete(boolean success) {
             complete = true;
         }
     }
 
     private class DownloadProgress
             implements Scraper.OnChapterDownloadProgress {
         public boolean started, progress, complete;
         public int totalCount;
 
         @Override
         public void downloadStarted() {
             started = true;
         }
 
         @Override
         public void downloadProgress(int current, int total) {
             progress = true;
             totalCount = total;
         }
 
         @Override
         public void downloadComplete(boolean success) {
             complete = true;
         }
     }
 
     private class SearchProgress
             implements Scraper.OnSearchResults {
         public int updateCount;
 
         @Override
         public void resultsUpdated() {
             updateCount += 1;
         }
     }
 
     @Override
     public void setUp() throws Exception {
         Utils.setupTestAnimeaEnvironment(this);
 
         testContext = getInstrumentation().getContext()
             .createPackageContext("org.barbon.mangaget.tests", 0);
         db = DB.getInstance(null);
 
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
         return setUpTestChapter(mangaId, 1, DB.DOWNLOAD_STOPPED);
     }
 
     private long setUpTestChapter(long mangaId, int index, int status) {
         long id = db.insertChapter(
             mangaId, index, 45, "Chapter title",
             "http://manga.animea.net/papillon-hana-to-chou-chapter-" +
             Integer.toString(index) + "-page-1.html");
 
         db.updateChapterStatus(id, status);
 
         return id;
     }
 
     private long setUpTestPage(long chapterId, int index) {
         return db.insertPage(
             chapterId, index,
             "http://manga.animea.net/papillon-hana-to-chou-chapter-1-page-" +
             Integer.toString(index + 1) + ".html", null, DB.DOWNLOAD_REQUESTED);
     }
 
     @Override
     public void tearDown() throws Exception {
     }
 
     private int getChapterCount(long mangaId) {
         DB db = DB.getInstance(null);
         Cursor chapters = db.getChapterList(mangaId);
         int count = chapters.getCount();
 
         chapters.close();
 
         return count;
     }
 
     public void testMangaUpdate() throws Throwable {
         final long mangaId = setUpTestManga();
         final Scraper scraper = Scraper.getInstance(testContext);
         final OperationProgress progress = new OperationProgress();
 
         setUpTestChapter(mangaId, 1, DB.DOWNLOAD_COMPLETE);
         setUpTestChapter(mangaId, 2, DB.DOWNLOAD_REQUESTED);
         setUpTestChapter(mangaId, 3, DB.DOWNLOAD_STOPPED);
 
         class UiTask implements Runnable {
             @Override
             public void run() {
                 scraper.updateManga(
                     mangaId, progress);
             }
         }
 
         assertEquals(3, getChapterCount(mangaId));
 
         UiTask uiTask = new UiTask();
 
         runTestOnUiThread(uiTask);
 
         while (!progress.complete)
             Thread.sleep(500);
 
         assertTrue(progress.started);
         assertTrue(progress.complete);
         assertEquals(29, getChapterCount(mangaId));
 
         ContentValues chapter;
 
         // chapter 1 (already present)
         chapter = db.getChapter(db.getChapterId(mangaId, 1));
 
         assertEquals("Vol. 1 Wish",
                      chapter.getAsString(DB.CHAPTER_TITLE));
         assertEquals(DB.DOWNLOAD_COMPLETE,
                      (long) chapter.getAsInteger(DB.DOWNLOAD_STATUS));
 
         // chapter 4 (new)
         chapter = db.getChapter(db.getChapterId(mangaId, 4));
 
         assertEquals("Vol. 1 Magic Spell",
                      chapter.getAsString(DB.CHAPTER_TITLE));
         assertEquals(DB.DOWNLOAD_STOPPED,
                      (long) chapter.getAsInteger(DB.DOWNLOAD_STATUS));
     }
 
     public void testFullDownload() throws Throwable {
         final long mangaId = setUpTestManga();
         final long chapterId = setUpTestChapter(mangaId);
         final String targetCbz = new File(tempDir, "Dummy-1.cbz")
             .getAbsolutePath();
         final String tempDirString = tempDir.getAbsolutePath();
         final Scraper scraper = Scraper.getInstance(testContext);
         final DownloadProgress progress = new DownloadProgress();
 
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
         assertTrue(progress.progress);
         assertTrue(progress.complete);
         assertEquals(90, progress.totalCount);
         assertTrue(new File(targetCbz).exists());
     }
 
     public void testOnlyDownloadPages() throws Throwable {
         final long mangaId = setUpTestManga();
         final long chapterId = setUpTestChapter(mangaId);
         final String targetCbz = new File(tempDir, "Dummy-1.cbz")
             .getAbsolutePath();
         final String tempDirString = tempDir.getAbsolutePath();
         final Scraper scraper = Scraper.getInstance(testContext);
         final DownloadProgress progress = new DownloadProgress();
 
         setUpTestPage(chapterId, 0);
         setUpTestPage(chapterId, 1);
         setUpTestPage(chapterId, 2);
         setUpTestPage(chapterId, 3);
 
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
 
         // TODO test DB status during download
 
         System.out.println("Temp dir: " + tempDirString);
         System.out.println("Target: " + targetCbz);
 
         assertTrue(progress.started);
         assertTrue(progress.progress);
         assertTrue(progress.complete);
         assertEquals(8, progress.totalCount);
         assertTrue(new File(targetCbz).exists());
     }
 
     // TODO test partial downloads
     //      all pages downloaded
     //      page marked downloaded but no file there
     //      download cancellation
 
     public void testSearchPager() throws Throwable {
         final Scraper scraper = Scraper.getInstance(testContext);
         final SearchProgress progress = new SearchProgress();
 
         class UiTask implements Runnable {
             public Scraper.ResultPager pager;
 
             @Override
             public void run() {
                 pager = scraper.searchManga("", progress);
                 assertEquals(0, pager.getCount()); // start the download
             }
         }
 
         UiTask uiTask = new UiTask();
 
         runTestOnUiThread(uiTask);
 
         while (progress.updateCount < 2)
             Thread.sleep(500);
 
         // uses the fact the only registered URLs are for AnimeA
         assertEquals(2, progress.updateCount);
         assertEquals(48, uiTask.pager.getCount());
         assertEquals("2 Kaime no Hajimete no Koi",
                      uiTask.pager.getItem(0).title);
         assertEquals("2-kaime-no-hajimete-no-koi",
                      uiTask.pager.getItem(0).pattern);
         assertEquals("AnimeA",
                      uiTask.pager.getItem(0).provider);
     }
 }
