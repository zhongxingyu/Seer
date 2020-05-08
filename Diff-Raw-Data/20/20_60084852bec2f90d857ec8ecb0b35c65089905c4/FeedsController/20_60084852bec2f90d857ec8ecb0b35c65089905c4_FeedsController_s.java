 package org.guiceae.main.web;
 
 import com.google.common.collect.ImmutableMap;
 import com.sun.jersey.api.view.Viewable;
 import com.sun.syndication.feed.synd.*;
 import com.sun.syndication.io.FeedException;
 import com.sun.syndication.io.SyndFeedOutput;
 import org.guiceae.main.model.Article;
 import org.guiceae.main.repositories.ArticleRepository;
 import org.guiceae.util.UserPrincipalHolder;
 
 import javax.inject.Inject;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.ws.rs.*;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.Response;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * User: Igor Petruk
  * Date: 29.06.12
  * Time: 22:12
  */
 @Path("/app/feed")
 public class FeedsController {
     @Inject
     ArticleRepository articleRepository;
 
     @Inject
     UserPrincipalHolder userPrincipalHolder;
 
     Map<String, String> feeds = new ImmutableMap.Builder<String, String>()
             .put("news", "Новини")
             .put("charity", "Благодійність")
             .put("activity", "Депутатська діяльність")
            .put("biography", "Автобіографія")
             .build();
 
 
     @GET
     @Path("/{feed}")
     public Viewable givenFeed(
             @PathParam("feed") String feed,
             @QueryParam("offset") @DefaultValue("0") Integer offset) {
         return produceFeed(feed, offset);
     }
 
     private Viewable produceFeed(String feed, Integer offset) {
         boolean showPending = userPrincipalHolder.get().contains("cm");
         long count = articleRepository.count(feed, true);
         List<Article> articles = articleRepository.getFeed(feed, !showPending, offset);
         Map<String, Object> it = new HashMap<String, Object>();
         it.put("feed", articles);
         it.put("feedName", feed);
         it.put("pagesCount", (count % 5 == 0) ? count / 5 : count / 5 + 1);
         it.put("currentFirst", offset);
         it.put("feedsNames", feeds);
         return new Viewable("/feed.jsp", it);
     }
 
     @GET
     @Path("/{feed}/rss")
     @Produces("application/rss+xml")
     public Response rssFeed(@PathParam("feed") String feedName,
                             @Context HttpServletRequest request,
                             @Context HttpServletResponse response) throws IOException, FeedException {
         SyndFeed feed = buildFeed(feedName, request);
 
         feed.setFeedType("rss_2.0");
 
         SyndFeedOutput output = new SyndFeedOutput();
         output.output(feed, response.getWriter());
 
         return Response.ok().build();
     }
 
     private SyndFeed buildFeed(String feedName, HttpServletRequest request) throws MalformedURLException {
         final String siteUrl = new URL(request.getRequestURL().toString()).getAuthority();
 
         SyndFeed feed = new SyndFeedImpl();
         feed.setTitle("Канал " + feedName);
         feed.setLink("http://" + siteUrl + "/");
         feed.setDescription("RSS канал " + feedName);
         feed.setEncoding("UTF-8");
         List<Article> articles = articleRepository.getFeed(feedName, true, 0);
         List<SyndEntry> entries = new ArrayList<SyndEntry>();
 
         for (Article article : articles) {
             SyndEntry entry = new SyndEntryImpl();
             entry.setAuthor(article.getAuthor());
             entry.setUpdatedDate(article.getLastUpdated());
             entry.setPublishedDate(article.getCreated());
             entry.setTitle(article.getTitle());
             entry.setLink("http://" + siteUrl
                     + "/app/article/detail/" + article.getPermalink());
             SyndContent description = new SyndContentImpl();
             description.setType("text/html");
             description.setValue("<html><body>" + article.getShortContent() + "</body></html>");
             entry.setDescription(description);
             entries.add(entry);
         }
         feed.setEntries(entries);
         return feed;
     }
 }
