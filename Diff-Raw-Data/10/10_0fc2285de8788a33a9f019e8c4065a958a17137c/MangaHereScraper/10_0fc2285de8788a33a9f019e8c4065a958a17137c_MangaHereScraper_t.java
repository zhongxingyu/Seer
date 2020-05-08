 /*
  * Copyright (c) Mattia Barbon <mattia@barbon.org>
  * distributed under the terms of the MIT license
  */
 
 package org.barbon.mangaget.scrape.mangahere;
 
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
 
 import org.jsoup.select.Elements;
 
 public class MangaHereScraper {
     private static final String[] SUPPORTED_TAGS = new String[] {
         "Action", "Adventure", "Comedy", "Doujinshi", "Drama",
         "Ecchi", "Fantasy", "Gender Bender", "Harem", "Historical",
         "Horror", "Josei", "Martial Arts", "Mature", "Mecha",
         "Mystery", "One Shot", "Psychological", "Romance", "School Life",
         "Sci-fi", "Seinen", "Shoujo", "Shoujo Ai", "Shounen", "Shounen Ai",
         "Slice of Life", "Sports", "Supernatural", "Tragedy"
     };
 
     // scraper interface
     public static class Provider extends Scraper.Provider {
         private static final String MANGAHERE_URL =
             "http://www.mangahere.com/";
 
         @Override
         public boolean canHandleUrl(String url) {
             return url.startsWith(MANGAHERE_URL);
         }
 
         @Override
         public String getName() {
             return "MangaHere";
         }
 
         @Override
         public String composeSearchUrl(Scraper.SearchCriteria criteria) {
             return MangaHereScraper.composeSearchUrl(criteria);
         }
 
         @Override
         public String composePagingUrl(String pagingUrl, int page) {
             return super.composePagingUrl(pagingUrl, (page - 1) * 30);
         }
 
         @Override
         public String[] supportedTags() {
             return SUPPORTED_TAGS;
         }
 
         @Override
         public List<String> scrapeChapterPages(
                 Downloader.DownloadDestination target) {
             return MangaHereScraper.scrapeChapterPages(target);
         }
 
         @Override
         public List<String> scrapeImageUrls(
                 Downloader.DownloadDestination target) {
             List<String> urls = new ArrayList<String>(1);
 
             urls.add(MangaHereScraper.scrapeImageUrl(target));
 
             return urls;
         }
 
         @Override
         public HtmlScrape.SearchResultPage scrapeSearchResults(
                 Downloader.DownloadDestination target) {
             return MangaHereScraper.scrapeSearchResults(target);
         }
 
         @Override
         public HtmlScrape.ChapterPage scrapeMangaPage(
                 Downloader.DownloadDestination target) {
             return MangaHereScraper.scrapeMangaPage(target);
         }
     }
 
     public static String composeSearchUrl(Scraper.SearchCriteria criteria) {
         String encodedTitle = "";
         StringBuffer genres = new StringBuffer();
 
         try {
             if (criteria.title != null)
                 encodedTitle = URLEncoder.encode(criteria.title, "UTF-8");
         }
         catch (UnsupportedEncodingException e) {
             throw new RuntimeException(e);
         }
 
         if (criteria.includeTags != null && criteria.includeTags.size() != 0) {
             boolean selected = false;
 
             for (String tag : SUPPORTED_TAGS) {
                 if (criteria.includeTags.indexOf(tag) != -1) {
                     // &genres[<tag>]=1
                     genres.append("&genres%5B");
                     genres.append(tag);
                     genres.append("%5D=1");
 
                     selected = true;
                 }
             }
 
             if (!selected)
                 return null;
         }
 
         return "http://www.mangahere.com/search.php?name_method=cw&name=" +
             encodedTitle + genres + "&page=1";
     }
 
     // pure HTML scraping
 
     public static List<String> scrapeChapterPages(
             Downloader.DownloadDestination target) {
         Document doc = HtmlScrape.parseHtmlPage(target);
         Element pageMenu = doc.select("div.go_page select.wid60").first();
 
         Elements options = pageMenu.select("option");
         List<String> result = new ArrayList<String>();
 
         for (Element option : options) {
             if (!option.hasAttr("value"))
                 continue;
             result.add(HtmlScrape.absoluteUrl(
                            option.attr("value"), target.baseUrl));
         }
 
         return result;
     }
 
     public static String scrapeImageUrl(
             Downloader.DownloadDestination target) {
         Document doc = HtmlScrape.parseHtmlPage(target);
         Element img = doc.select("section.read_img > a > img").first();
 
         if (!img.hasAttr("src"))
             return null;
 
         return img.attr("abs:src");
     }
 
     public static HtmlScrape.SearchResultPage scrapeSearchResults(
             Downloader.DownloadDestination target) {
         Document doc = HtmlScrape.parseHtmlPage(target);
         Elements mangas = doc.select("div.result_search > dl > dt > a.manga_info");
         List<String> urls = new ArrayList<String>();
         List<String> titles = new ArrayList<String>();
 
         for (Element manga : mangas) {
             urls.add(manga.attr("abs:href"));
             titles.add(manga.text());
         }
 
         Elements links = doc.select("div.next-page > a");
         Element current = doc.select("div.next-page > a.hover").first();
         int currentPage = -1, lastPage = -1;
         String pagingUrl = null;
 
         if (current != null)
             currentPage = Integer.valueOf(current.text());
 
         for (Element link : links) {
             String href = link.attr("abs:href");
             int index = href.lastIndexOf("&page=");
 
             if (index == -1 || link.text().equalsIgnoreCase("next"))
                 continue;
 
             pagingUrl = href.substring(0, index + 6).replace("%", "%%") + "%d";
             lastPage = Integer.valueOf(href.substring(index + 6));
         }
 
         HtmlScrape.SearchResultPage page = new HtmlScrape.SearchResultPage();
 
         page.urls = urls;
         page.titles = titles;
         page.pagingUrl = pagingUrl;
         page.currentPage = currentPage;
         page.lastPage = lastPage > currentPage ? lastPage : currentPage;
 
         return page;
     }
 
     public static HtmlScrape.ChapterPage scrapeMangaPage(
             Downloader.DownloadDestination target) {
         Document doc = HtmlScrape.parseHtmlPage(target);
         Elements links = doc.select("div.detail_list span.left a");
         List<HtmlScrape.ChapterInfo> chapters =
             new ArrayList<HtmlScrape.ChapterInfo>();
         HtmlScrape.ChapterPage result = new HtmlScrape.ChapterPage(chapters);
 
         for (Element link : links) {
             if (!link.hasAttr("href"))
                 continue;
 
             String url = link.attr("abs:href");
             if (url.indexOf("/manga/") == -1 || !url.endsWith("/"))
                 continue;
             int chap = url.lastIndexOf("/c");
             if (chap == -1)
                 continue;
             String indexS = url.substring(chap + 2, url.length() - 1);
 
             String title = link.text();
             HtmlScrape.ChapterInfo info = new HtmlScrape.ChapterInfo();
 
             info.title = title;
             info.url = url;
             info.index = (int) (Float.valueOf(indexS) * 100);
 
             // assumes chapters are listed in reverse order
             chapters.add(0, info);
         }
 
         for (Element li : doc.select("ul.detail_topText li")) {
             Element label = li.select("label").first();
 
             if (label == null)
                 continue;
 
             Node text = label.nextSibling();
             if (text == null || !(text instanceof TextNode))
                 continue;
 
             String value = ((TextNode)text).text().trim();
 
             if (label.text().trim().endsWith("Summary:")) {
                Element summary = li.select("p#show").first();

                if (summary != null) {
                    result.summary = summary.text().trim();

                    if (result.summary.endsWith("Show less"))
                        result.summary = result.summary.substring(
                            0, result.summary.length() - 9);
                }
             } else if (label.text().trim().equals("Genre(s):")) {
                 result.genres = new ArrayList<String>();
 
                 for (String genre : value.split(", "))
                     result.genres.add(genre);
             }
         }
 
         return result;
     }
 }
