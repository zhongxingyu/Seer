 /*
  * Copyright (c) Mattia Barbon <mattia@barbon.org>
  * distributed under the terms of the MIT license
  */
 
 package org.barbon.mangaget.scrape.animea;
 
 import java.io.UnsupportedEncodingException;
 
 import java.net.URLEncoder;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.barbon.mangaget.scrape.Downloader;
 import org.barbon.mangaget.scrape.HtmlScrape;
 import org.barbon.mangaget.scrape.Scraper;
 
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.nodes.Node;
 import org.jsoup.nodes.TextNode;
 
 import org.jsoup.parser.Tag;
 
 import org.jsoup.select.Elements;
 
 public class AnimeAScraper {
     // scraper interface
     public static class Provider extends Scraper.Provider {
         private static final String ANIMEA_URL = "http://manga.animea.net/";
 
         @Override
         public boolean canHandleUrl(String url) {
             return url.startsWith(ANIMEA_URL);
         }
 
         @Override
         public String getName() {
             return "AnimeA";
         }
 
         @Override
         public String composeMangaUrl(String url) {
             // skip=1 is to skip the "Mature content" warning
             return url + "?skip=1";
         }
 
         @Override
         public String composeSearchUrl(String title) {
             try {
                 return "http://manga.animea.net/search.html?title=" +
                     URLEncoder.encode(title, "UTF-8");
             }
             catch (UnsupportedEncodingException e) {
                 throw new RuntimeException(e);
             }
         }
 
         @Override
         public List<String> scrapeChapterPages(
                 Downloader.DownloadDestination target) {
             return AnimeAScraper.scrapeChapterPages(target);
         }
 
         @Override
         public List<String> scrapeImageUrls(
                 Downloader.DownloadDestination target) {
             List<String> urls = new ArrayList<String>(1);
 
             urls.add(AnimeAScraper.scrapeImageUrl(target));
 
             return urls;
         }
 
         @Override
         public HtmlScrape.SearchResultPage scrapeSearchResults(
                 Downloader.DownloadDestination target) {
             return AnimeAScraper.scrapeSearchResults(target);
         }
 
         @Override
         public HtmlScrape.ChapterPage scrapeMangaPage(
                 Downloader.DownloadDestination target) {
             return new HtmlScrape.ChapterPage(
                 AnimeAScraper.scrapeMangaPage(target));
         }
     }
 
     // pure HTML scraping
 
     public static List<String> scrapeChapterPages(
             Downloader.DownloadDestination target) {
         Document doc = HtmlScrape.parseHtmlPage(target);
         Element page = doc.select("select[name=page]").first();
 
         if (!page.hasAttr("onchange"))
             return null;
         Elements options = page.select("option");
         String urlTemplate = page.attr("onchange");
         List<String> result = new ArrayList<String>();
 
         urlTemplate = urlTemplate.replaceFirst(
             "javascript:window.location='(.*\\.html).*", "$1");
         urlTemplate = HtmlScrape.absoluteUrl(urlTemplate, target.baseUrl);
 
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
         Document doc = HtmlScrape.parseHtmlPage(target);
         Element img = doc.select("img.mangaimg").first();
 
         if (!img.hasAttr("src"))
             return null;
 
         return img.attr("abs:src");
     }
 
     public static HtmlScrape.SearchResultPage scrapeSearchResults(
             Downloader.DownloadDestination target) {
         Document doc = HtmlScrape.parseHtmlPage(target);
         Elements mangas = doc.select("a.manga_title");
         List<String> urls = new ArrayList<String>();
         List<String> titles = new ArrayList<String>();
 
         for (Element manga : mangas) {
             urls.add(manga.attr("abs:href"));
             titles.add(manga.text());
         }
 
         Elements items = doc.select("div.pagingdiv > ul:not(.order) > li");
         int currentPage = -1;
         String pagingUrl = null;
 
         for (Element item : items) {
             if (   item.children().size() == 0
                 && (   !item.hasAttr("class")
                     || !item.attr("class").equals("totalmanga"))) {
                 String num = item.text();
 
                 if (   !num.equalsIgnoreCase("previous")
                     && !num.equalsIgnoreCase("next"))
                     currentPage = Integer.valueOf(num);
             }
             else if (   item.children().size() == 1
                      && item.child(0).tag() == Tag.valueOf("a")) {
                 Element link = item.child(0);
                 String href = link.attr("abs:href");
                 int index = href.lastIndexOf("&page=");
 
                 if (index == -1)
                     continue;
 
                 pagingUrl = href.substring(0, index + 6) + "%d";
             }
 
             if (currentPage != -1 && pagingUrl != null)
                 break;
         }
 
         HtmlScrape.SearchResultPage page = new HtmlScrape.SearchResultPage();
 
         page.urls = urls;
         page.titles = titles;
         page.pagingUrl = pagingUrl;
         page.currentPage = currentPage;
 
         return page;
     }
 
     public static List<HtmlScrape.ChapterInfo> scrapeMangaPage(
             Downloader.DownloadDestination target) {
         Document doc = HtmlScrape.parseHtmlPage(target);
         Elements links = doc.select("ul.chapters_list li > a");
         List<HtmlScrape.ChapterInfo> chapters =
             new ArrayList<HtmlScrape.ChapterInfo>();
 
         for (Element link : links) {
             if (!link.hasAttr("href"))
                 continue;
 
             String url = link.attr("abs:href");
 
            if (!url.endsWith("-page-1.html"))
                 continue;
             int dash = url.lastIndexOf('-', url.length() - 13);
             String indexS = url.substring(dash + 1, url.length() - 12);
 
             int elementIndex = link.parent().childNodes().indexOf(link);
             Node text = link.parent().childNode(elementIndex + 1);
 
             if (!(text instanceof TextNode))
                 continue;
 
             String title = ((TextNode)text).text().trim();
             HtmlScrape.ChapterInfo info = new HtmlScrape.ChapterInfo();
 
             info.title = title;
             info.url = url;
 
             // assumes chapters are listed in reverse order
             chapters.add(0, info);
         }
 
         return chapters;
     }
 }
