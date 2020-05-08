 /*
  * Copyright (c) Mattia Barbon <mattia@barbon.org>
  * distributed under the terms of the MIT license
  */
 
 package org.barbon.mangaget.scrape;
 
 import android.content.Context;
 
 import android.os.Environment;
 
 import android.test.InstrumentationTestCase;
 
 import java.io.File;
 
 import java.util.List;
 
 import org.barbon.mangaget.data.DB;
 
 import org.barbon.mangaget.tests.DummyDownloader;
 import org.barbon.mangaget.tests.R;
 
 import org.barbon.mangaget.scrape.animea.Scraper;
 
 public class ScraperTest extends InstrumentationTestCase {
     private DummyDownloader downloader;
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
 
         final String baseUrl = "http://manga.animea.net";
         final String baseImage = "http://s2-a.animea-server.net";
 
         // set up dummy search results
         downloader.addUrl(baseUrl + "search.html?title=",
                           R.raw.animea_results_html);
 
         // set up dummy pages
         String base = baseUrl + "/papillon-hana-to-chou-chapter-1-page-";
 
         downloader.addUrl(base + "1.html", R.raw.papillon_c1_p1_html);
 
         for (int i = 2; i < 46; ++i)
             downloader.addUrl(base + Integer.toString(i) + ".html",
                               R.raw.papillon_c1_p2_html);
 
         // set up dummy images
         String p1 = baseImage + "/5338%2F1_JHMCN%2F00_fuuchifighters.jpg" ;
         String pn = baseImage + "/5338%2F1_JHMCN%2F001_cover.jpg" ;
 
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
 
     private Downloader.DownloadDestination getPage(int resource, String base) {
         Downloader.DownloadDestination dest =
             new Downloader.DownloadDestination();
 
         dest.stream = testContext.getResources().openRawResource(resource);
         dest.encoding = "utf-8";
         dest.baseUrl = base;
 
         return dest;
     }
 
     @Override
     public void tearDown() throws Exception {
     }
 
     public void testScrapeResults() {
         String searchPage =
            "http://manga.animea.net/search.html?title=";
         Scraper.SearchResultPage res =
             Scraper.scrapeSearchResults(getPage(R.raw.animea_results_html,
                                                 searchPage));
 
         assertEquals(2, res.currentPage);
         assertEquals("http://manga.animea.net/search.html?title=&completed=0&yor_range=0&yor=&type=any&author=&artist=&genre%5BAction%5D=2&genre%5BAdventure%5D=0&genre%5BComedy%5D=0&genre%5BDoujinshi%5D=0&genre%5BDrama%5D=0&genre%5BEcchi%5D=0&genre%5BFantasy%5D=0&genre%5BGender_Bender%5D=0&genre%5BHarem%5D=0&genre%5BHistorical%5D=0&genre%5BHorror%5D=0&genre%5BJosei%5D=0&genre%5BMartial_Arts%5D=0&genre%5BMature%5D=0&genre%5BMecha%5D=0&genre%5BMystery%5D=0&genre%5BPsychological%5D=0&genre%5BRomance%5D=0&genre%5BSchool_Life%5D=0&genre%5BSci-fi%5D=0&genre%5BSeinen%5D=0&genre%5BShotacon%5D=0&genre%5BShoujo%5D=0&genre%5BShoujo_Ai%5D=0&genre%5BShounen%5D=0&genre%5BShounen_Ai%5D=0&genre%5BSlice_of_Life%5D=0&genre%5BSmut%5D=0&genre%5BSports%5D=0&genre%5BSupernatural%5D=0&genre%5BTragedy%5D=0&genre%5BYaoi%5D=0&genre%5BYuri%5D=0&page=%d", res.pagingUrl);
         assertEquals(48, res.urls.size());
         assertEquals(48, res.titles.size());
 
         assertEquals("http://manga.animea.net/2-kaime-no-hajimete-no-koi.html",
                      res.urls.get(0));
         assertEquals("http://manga.animea.net/a-house-in-venice.html",
                      res.urls.get(47));
 
         assertEquals("2 Kaime no Hajimete no Koi",
                      res.titles.get(0));
         assertEquals("A House in Venice",
                      res.titles.get(47));
     }
 
    public void testScrapeEmptyResults() {
        String searchPage =
            "http://manga.animea.net/search.html?title=pavillon";
        Scraper.SearchResultPage res =
            Scraper.scrapeSearchResults(getPage(R.raw.animea_results_empty_html,
                                                searchPage));

        assertEquals(-1, res.currentPage);
        assertEquals(null, res.pagingUrl);
        assertEquals(0, res.urls.size());
        assertEquals(0, res.titles.size());
    }

     public void testScrapeMangaPage() {
         String mangaPage = "http://manga.animea.net/";
         List<Scraper.ChapterInfo> res =
             Scraper.scrapeMangaPage(getPage(R.raw.papillon_chapters_html,
                                             mangaPage));
 
         assertEquals(29, res.size());
         assertEquals("http://manga.animea.net/papillon-hana-to-chou-chapter-1-page-1.html",
                      res.get(0).url);
         assertEquals("http://manga.animea.net/papillon-hana-to-chou-chapter-29-page-1.html",
                      res.get(28).url);
     }
 
     public void testMangaUpdate() throws Throwable {
         final long mangaId = setUpTestManga();
         final Scraper scraper = new Scraper(db, downloader);
         final OperationProgress progress = new OperationProgress();
 
         class UiTask implements Runnable {
             @Override
             public void run() {
                 scraper.updateManga(
                     mangaId, progress);
             }
         }
 
         UiTask uiTask = new UiTask();
 
         runTestOnUiThread(uiTask);
 
         while (!progress.complete)
             Thread.sleep(500);
 
         assertTrue(progress.started);
         assertTrue(progress.complete);
 
         // TODO test manga chapters have been updated
     }
 
     public void testFullDownload() throws Throwable {
         final long mangaId = setUpTestManga();
         final long chapterId = setUpTestChapter(mangaId);
         final String targetCbz = new File(tempDir, "Dummy-1.cbz")
             .getAbsolutePath();
         final String tempDirString = tempDir.getAbsolutePath();
         final Scraper scraper = new Scraper(db, downloader);
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
         assertTrue(progress.complete);
         assertTrue(new File(targetCbz).exists());
     }
 
     // TODO test partial downloads
     //      page URLs already filled in
     //      all pages downloaded
     //      page marked downloaded but no file there
 }
