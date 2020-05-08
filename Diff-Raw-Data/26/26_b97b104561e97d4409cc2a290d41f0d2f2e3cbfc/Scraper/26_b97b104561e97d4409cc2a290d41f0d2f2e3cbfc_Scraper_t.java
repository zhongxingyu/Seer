 /*
  * Copyright (c) Mattia Barbon <mattia@barbon.org>
  * distributed under the terms of the MIT license
  */
 
 package org.barbon.mangaget.scrape.animea;
 
 import android.content.ContentValues;
 import android.content.Context;
 
 import android.database.Cursor;
 
 import java.io.File;
 import java.io.InputStream;
 import java.io.IOException;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.barbon.mangaget.CBZFile;
 
 import org.barbon.mangaget.data.DB;
 
 import org.barbon.mangaget.scrape.Downloader;
 
 import org.jsoup.Jsoup;
 
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 
 import org.jsoup.select.Elements;
 
 public class Scraper {
     private DB db;
     private Downloader downloader;
 
     private static final OnChapterDownloadProgress dummyListener =
         new OnChapterDownloadProgress() {
             @Override
             public void downloadStarted() { }
 
             @Override
             public void downloadProgress(int current, int total) { }
 
             @Override
             public void downloadComplete(boolean success) { }
         };
 
     private class ChapterDownload {
         public long id;
         public ContentValues chapter;
         public String targetPath, tempDir;
         public OnChapterDownloadProgress listener;
     }
 
     private class PageDownload {
         public long id;
         public String url;
         public String imageUrl;
         public String targetPath;
         public int status;
     }
 
     private class ChapterDownloader
             extends Downloader.OnDownloadProgressAdapter {
         private ChapterDownload download;
         private Downloader.DownloadDestination target;
 
         public ChapterDownloader(ChapterDownload info) {
             super();
 
             download = info;
         }
 
         public void start() {
             target = downloader.requestDownload(
                 download.chapter.getAsString(DB.CHAPTER_URL), this);
         }
 
         @Override
         public void downloadStarted() {
             super.downloadStarted();
 
             db.updateChapterStatus(download.id, DB.DOWNLOAD_STARTED);
         }
 
         @Override
         public void downloadComplete(boolean success) {
             super.downloadComplete(success);
 
             if (!success) {
                 db.updateChapterStatus(download.id, DB.DOWNLOAD_REQUESTED);
                 download.listener.downloadComplete(success);
 
                 return;
             }
 
             List<String> pageUrls = scrapeChapterPages(target);
 
             int index = 0;
             for (String url : pageUrls)
                 db.insertPage(download.id, index++, url, null,
                               DB.DOWNLOAD_REQUESTED);
 
             db.updateChapterStatus(download.id, DB.DOWNLOAD_COMPLETE);
             downloadPages(download);
         }
     }
 
     private class PageDownloader {
         private class PageInfoDownloader
                 extends Downloader.OnDownloadProgressAdapter {
             private PageDownload page;
             private Downloader.DownloadDestination target;
 
             public PageInfoDownloader(PageDownload _page) {
                 page = _page;
             }
 
             public void start() {
                 target = downloader.requestDownload(page.url, this);
             }
 
             @Override
             public void downloadStarted() {
                 super.downloadStarted();
 
                 db.updatePageStatus(page.id, DB.DOWNLOAD_STARTED);
             }
 
             @Override
             public void downloadComplete(boolean success) {
                 super.downloadComplete(success);
 
                 if (!success) {
                     db.updatePageStatus(page.id, DB.DOWNLOAD_REQUESTED);
                     download.listener.downloadComplete(success);
 
                     // TODO stop all downloads
 
                     return;
                 }
 
                 page.imageUrl = scrapeImageUrl(target);
                 db.updatePageImage(page.id, page.imageUrl);
 
                 count -= 1;
 
                 downloadPageImage(page);
             }
         }
 
         private class PageImageDownloader
                 extends Downloader.OnDownloadProgressAdapter {
             private PageDownload page;
 
             public PageImageDownloader(PageDownload _page) {
                 page = _page;
             }
 
             public void start() {
                 downloader.requestDownload(page.imageUrl, this,
                                            new File(page.targetPath));
             }
 
             @Override
             public void downloadStarted() {
                 super.downloadStarted();
 
                 db.updatePageStatus(page.id, DB.DOWNLOAD_STARTED);
             }
 
             @Override
             public void downloadComplete(boolean success) {
                 super.downloadComplete(success);
 
                 if (!success) {
                     db.updatePageStatus(page.id, DB.DOWNLOAD_REQUESTED);
                     download.listener.downloadComplete(success);
 
                     // TODO stop all downloads
 
                     return;
                 }
 
                 db.updatePageStatus(page.id, DB.DOWNLOAD_COMPLETE);
                 count -= 1;
 
                 // TODO rename the image according to image type
                 //      using magic + content-type + (real) url
 
                if (count == 0)
                    downloadFinished();
             }
         }
 
         private ChapterDownload download;
         private List<PageDownload> pages;
         private int count;
 
         public PageDownloader(ChapterDownload _download,
                               List<PageDownload> _pages) {
             download = _download;
             pages = _pages;
         }
 
         public void start() {
             for (PageDownload page : pages) {
                 if (page.imageUrl == null) {
                     count += 2;
 
                     downloadPageInfo(page);
                 }
                 else if (page.status != DB.DOWNLOAD_COMPLETE) {
                     count += 1;
 
                     downloadPageImage(page);
                 }
             }

            // all done!
            if (count == 0)
                downloadFinished();
         }
 
         public void downloadPageInfo(PageDownload page) {
             PageInfoDownloader downloader = new PageInfoDownloader(page);
 
             downloader.start();
         }
 
         public void downloadPageImage(PageDownload page) {
             PageImageDownloader downloader = new PageImageDownloader(page);
 
             downloader.start();
         }

        private void downloadFinished() {
            createChapterArchive(download, pages);
            download.listener.downloadComplete(true);
        }
     }
 
     public interface OnChapterDownloadProgress {
         public void downloadStarted();
         public void downloadProgress(int current, int total);
         public void downloadComplete(boolean success);
     }
 
     public Scraper(DB _db, Downloader _downloader) {
         db = _db;
         downloader = _downloader;
     }
 
     public void downloadChapter(long chapterId, String targetPath,
                                 String tempDir,
                                 OnChapterDownloadProgress listener) {
         ContentValues chapter = db.getChapter(chapterId);
         ChapterDownload download = new ChapterDownload();
 
         download.id = chapterId;
         download.chapter = chapter;
         download.listener = listener != null ? listener : dummyListener;
         download.tempDir = tempDir;
         download.targetPath = targetPath;
 
         // TODO notify when it really starts
         download.listener.downloadStarted();
 
         if (chapter.getAsInteger(DB.DOWNLOAD_STATUS) != DB.DOWNLOAD_COMPLETE)
             downloadPageListAndPages(download);
         else
             downloadPages(download);
     }
 
     private void downloadPages(ChapterDownload download) {
         Cursor pageCursor = db.getPages(download.id);
         List<PageDownload> pageList = new ArrayList<PageDownload>();
         final int idI = pageCursor.getColumnIndex(DB.ID);
         final int urlI = pageCursor.getColumnIndex(DB.PAGE_URL);
         final int imageUrlI = pageCursor.getColumnIndex(DB.PAGE_IMAGE_URL);
         final int statusI = pageCursor.getColumnIndex(DB.DOWNLOAD_STATUS);
 
         while (pageCursor.moveToNext()) {
             PageDownload page = new PageDownload();
 
             page.id = pageCursor.getInt(idI);
             page.url = pageCursor.getString(urlI);
             page.imageUrl = pageCursor.getString(imageUrlI);
             page.status = pageCursor.getInt(statusI);
 
             String imgName =
                 download.chapter.getAsString(DB.CHAPTER_MANGA_ID) + "-" +
                 Long.toString(download.id) + "-" +
                 Long.toString(page.id) + ".jpg";
 
             page.targetPath = new File(download.tempDir, imgName)
                 .getAbsolutePath();
 
             pageList.add(page);
         }
 
         PageDownloader pages = new PageDownloader(download, pageList);
 
         pages.start();
     }
 
     private void downloadPageListAndPages(ChapterDownload download) {
         ChapterDownloader chapter = new ChapterDownloader(download);
 
         chapter.start();
     }
 
     public static List<String> scrapeChapterPages(
             Downloader.DownloadDestination target) {
         Document doc;
 
         try {
             doc = Jsoup.parse(target.stream, target.encoding, target.baseUrl);
         }
         catch (IOException e) {
             throw new RuntimeException(e);
         }
 
         Element page = doc.select("select[name=page]").first();
 
         if (!page.hasAttr("onchange"))
             return null;
         Elements options = page.select("option");
         String urlTemplate = page.attr("onchange");
         List<String> result = new ArrayList<String>();
 
         urlTemplate = urlTemplate.replaceFirst(".*(http:.*\\.html).*", "$1");
 
         for (Element option : options) {
             if (!option.hasAttr("value"))
                 continue;
             result.add(urlTemplate.replaceFirst(
                            "'\\s*\\+\\s*this\\.value\\s*\\+\\s*\\'",
                            option.attr("value")));
         }
 
         return result;
     }
 
     public static String scrapeImageUrl(Downloader.DownloadDestination target) {
         Document doc;
 
         try {
             doc = Jsoup.parse(target.stream, target.encoding, target.baseUrl);
         }
         catch (IOException e) {
             throw new RuntimeException(e);
         }
 
         Element img = doc.select("img.chapter_img").first();
 
         if (!img.hasAttr("src"))
             return null;
 
         return img.attr("src");
     }
 
     public static void createChapterArchive(ChapterDownload chapter,
                                             List<PageDownload> pages) {
         List<String> paths = new ArrayList<String>();
 
         for (PageDownload page : pages)
             paths.add(page.targetPath);
 
         CBZFile.createFile(chapter.targetPath, paths);
     }
 }
