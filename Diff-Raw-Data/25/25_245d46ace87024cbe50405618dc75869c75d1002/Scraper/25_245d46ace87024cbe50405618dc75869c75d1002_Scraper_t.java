 /*
  * Copyright (c) Mattia Barbon <mattia@barbon.org>
  * distributed under the terms of the MIT license
  */
 
 package org.barbon.mangaget.scrape.mangareader;
 
 import java.io.IOException;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.barbon.mangaget.scrape.Downloader;
 import org.barbon.mangaget.scrape.HtmlScrape;
 
 import org.jsoup.Jsoup;
 
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 
 import org.jsoup.select.Elements;
 
 public class Scraper {
     // pure HTML scraping
 
     public static HtmlScrape.SearchResultPage scrapeSearchResults(
             Downloader.DownloadDestination target) {
         Document doc;
 
         try {
             doc = Jsoup.parse(target.stream, target.encoding, target.baseUrl);
         }
         catch (IOException e) {
             throw new RuntimeException(e);
         }
 
         Elements mangas = doc.select("div.manga_name a");
         List<String> urls = new ArrayList<String>();
         List<String> titles = new ArrayList<String>();
 
         for (Element manga : mangas) {
             urls.add(manga.attr("abs:href"));
             titles.add(manga.text());
         }
 
         Elements links = doc.select("div#sp > a");
         Element current = doc.select("div#sp > strong").first();
        int currentPage = -1;
         String pagingUrl = null;
 
        if (current != null)
            currentPage = Integer.valueOf(current.text());

         for (Element link : links) {
             String href = link.attr("abs:href");
             int index = href.lastIndexOf("&p=");
 
             if (index == -1)
                 continue;
 
             pagingUrl = href.substring(0, index + 3) + "%d";
             break;
         }
 
         HtmlScrape.SearchResultPage page = new HtmlScrape.SearchResultPage();
 
         page.urls = urls;
         page.titles = titles;
         page.pagingUrl = pagingUrl;
         page.currentPage = currentPage;
 
         return page;
     }
 }
