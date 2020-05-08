 /*
  * Copyright (C) 2013 Jack Wakefield
  *
  * This file is part of TorrentFreak Reader.
  *
  * TorrentFreak Reader is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * TorrentFreak Reader is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with TorrentFreak Reader.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.torrentfreak.reader.free.articles.providers;
 
 import java.lang.Exception;
 import java.lang.Integer;
 import java.util.ArrayList;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import android.util.Log;
 import com.torrentfreak.reader.free.articles.ArticleItem;
 import com.torrentfreak.reader.free.articles.providers.ArticleListProvider;
 import com.torrentfreak.reader.free.articles.providers.exceptions.ArticleScrapeException;
 import com.torrentfreak.reader.free.categories.CategoryItem;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 public class CategoryListProvider extends ArticleListProvider {
     /**
      * The date scraper.
      */
     private static final Pattern dateScraper =
         Pattern.compile("([0-9]{1,2})/([0-9]{1,2})/([0-9]{4})");
 
     public CategoryListProvider(final CategoryItem category) {
         super(category);
     }
 
     public List<ArticleItem> scrape(final Document document) throws Exception {
         // ensure the document exists
         if (document == null) {
             throw new ArticleScrapeException("unable to parse document");
         }
 
         // retrieve the article elements
         final Elements articleElements = document.getElementsByTag("article");
         final List<ArticleItem> articles = new ArrayList<ArticleItem>();
 
         // loop through each article element
         for (final Element articleElement : articleElements) {
             ArticleItem article = null;
 
             try {
                 // attempt to scrape the article element
                 article = scrapeArticleItem(articleElement);
             } catch (final ArticleScrapeException e) {
                 throw e;
             }
 
             // ensure the article exists
             if (article != null) {
                 // add the article to the article list
                 articles.add(article);
             }
         }
 
         return articles;
     }
 
     private ArticleItem scrapeArticleItem(final Element articleElement)
         throws ArticleScrapeException {
         // retrieve the title element
         final Element titleElement = articleElement.select("header h4 a").first();
 
         // ensure the title element exists
         if (titleElement == null) {
             throw new ArticleScrapeException("title not found");
         }
 
         // retrieve the comments element
         final Element commentsElement =
             articleElement.select("footer ul li:nth-child(1) a").first();
 
         // ensure the comments element exists
         if (commentsElement == null) {
             throw new ArticleScrapeException("comment count not found");
         }
 
         // retrieve the date element
        final Element dateElement = articleElement.select("footer ul li:nth-child(3)").first();
 
         // ensure the date element exists
         if (dateElement == null) {
             throw new ArticleScrapeException("date not found");
         }
 
         // retrieve the title and URL text
         final String title = titleElement.text();
         final String url = titleElement.attr("href");
 
         // retrieve the comment count text and strip out any character which is not numerical
         String commentCountText = commentsElement.text();
         commentCountText = commentCountText.replaceAll("[^0-9]", "");
 
         int commentCount = 0;
 
         if (commentCountText.length() > 0) {
             commentCount = Integer.parseInt(commentCountText);
         }
 
         final Matcher dateMatcher = dateScraper.matcher(dateElement.text());
         final GregorianCalendar date = new GregorianCalendar();
 
         // attempt to find a match for the date from the date element text
         if (dateMatcher.find()) {
             final int year = Integer.parseInt(dateMatcher.group(3));
             final int month = Integer.parseInt(dateMatcher.group(2)) - 1;
             final int day = Integer.parseInt(dateMatcher.group(1));
 
             date.set(year, month, day);
         }
 
         // create the article setting the details to those retrieved
         final ArticleItem article = new ArticleItem();
         article.setCategoryId(category.getId());
         article.setTitle(title);
         article.setUrl(url);
         article.setDate(date);
         article.setCommentCount(commentCount);
 
         return article;
     }
 }
