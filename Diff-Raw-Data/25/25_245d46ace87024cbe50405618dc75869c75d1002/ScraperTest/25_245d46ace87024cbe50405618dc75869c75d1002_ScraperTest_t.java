 /*
  * Copyright (c) Mattia Barbon <mattia@barbon.org>
  * distributed under the terms of the MIT license
  */
 
 package org.barbon.mangaget.scrape.mangareader;
 
 import android.test.InstrumentationTestCase;
 
 import org.barbon.mangaget.data.DB;
 
 import org.barbon.mangaget.scrape.HtmlScrape;
 
 import org.barbon.mangaget.tests.R;
 import org.barbon.mangaget.tests.Utils;
 
 public class ScraperTest extends InstrumentationTestCase {
     public void testScrapeResults() {
         String searchPage =
             "http://www.mangareader.net/search/?w=&p=30";
         HtmlScrape.SearchResultPage res =
             Scraper.scrapeSearchResults(
                 Utils.getPage(this, R.raw.mangareader_results_html,
                               searchPage));
 
         assertEquals(2, res.currentPage);
         assertEquals("http://www.mangareader.net/search/?w=&p=%d",
                      res.pagingUrl);
         assertEquals(30, res.urls.size());
         assertEquals(30, res.titles.size());
 
         assertEquals("http://www.mangareader.net/132/yotsubato.html",
                      res.urls.get(0));
         assertEquals("http://www.mangareader.net/168/black-cat.html",
                      res.urls.get(29));
 
         assertEquals("Yotsubato!",
                      res.titles.get(0));
         assertEquals("Black Cat",
                      res.titles.get(29));
     }

    public void testScrapeEmptyResults() {
        String searchPage =
            "http://www.mangareader.net/search/?w=trzrt";
        HtmlScrape.SearchResultPage res =
            Scraper.scrapeSearchResults(
                Utils.getPage(this, R.raw.mangareader_results_empty_html,
                              searchPage));

        assertEquals(-1, res.currentPage);
        assertEquals(null, res.pagingUrl);
        assertEquals(0, res.urls.size());
        assertEquals(0, res.titles.size());
    }
 }
