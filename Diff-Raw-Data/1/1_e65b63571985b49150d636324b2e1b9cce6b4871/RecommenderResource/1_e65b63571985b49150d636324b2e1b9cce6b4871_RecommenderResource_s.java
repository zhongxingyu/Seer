 package com.xebia.appinc.blogrecommender.rest;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import javax.inject.Inject;
 import javax.ws.rs.DefaultValue;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.dom4j.Document;
 import org.dom4j.DocumentException;
 import org.dom4j.DocumentHelper;
 import org.dom4j.tree.DefaultElement;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.dasberg.guice.InjectDependencies;
 import com.sun.jersey.api.representation.Form;
 import com.xebia.appinc.blogrecommender.model.Blog;
 import com.xebia.appinc.blogrecommender.recommender.IRecommendationCentre;
 import com.xebia.appinc.blogrecommender.storage.Storage;
 
 /**
  * Rest resource for the expense module.
  * 
  * @author mischa
  */
 @Path("/recommendations/")
 public class RecommenderResource {
     private static DateTimeFormatter fmt = DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss Z")
                                                          .withLocale(Locale.US);
     
     private final Logger logger = LoggerFactory.getLogger(RecommenderResource.class);
 
     @Inject
     private Storage storageService;
 
     @Inject
     private Map<String, IRecommendationCentre> recommendationServices;
 
     /** Default constructor. */
     @InjectDependencies
     public RecommenderResource() {
     }
 
     @Path("blogs")
     @GET
     @Produces({ MediaType.APPLICATION_JSON })
     public List<Blog> getAllBlogs() {
         return storageService.getAllBlogs();
     }
 
     @Path("add/blog")
     @POST
     @Produces({ MediaType.APPLICATION_FORM_URLENCODED })
     public String addBlog(Form form) {
         storageService.addBlog(new Blog(form.getFirst("url"), "title", "author", null, "text"));
         return "added";
     }
 
     @Path("add/feed")
     @POST
     @Produces({ MediaType.APPLICATION_FORM_URLENCODED })
     public String addFeed(Form form) {
         try {
             GetMethod method = new GetMethod(form.getFirst("feed"));
             int i = new HttpClient().executeMethod(method);
             if (i == 200) {
                 Document document = DocumentHelper.parseText(new String(method.getResponseBody()));
                 for (DefaultElement element : (List<DefaultElement>) document.selectNodes("//item")) {
                     String title = element.selectSingleNode("title").getText();
                     String link = element.selectSingleNode("link").getText();
                     String pubDate = element.selectSingleNode("pubDate").getText();
                     String author = element.selectSingleNode("dc:creator").getText();
                     String description = element.selectSingleNode("description").getText();
                     if (description.length() > 250) {
                         description = description.substring(0, 250);
                     }
                     storageService.addBlog(new Blog(link, title, author, fmt.parseDateTime(pubDate), description));
                 }
             } else {
                 return "oops";
             }
 
         } catch (IOException e) {
             return "oops";
         } catch (DocumentException e) {
             return "oops";
         }
 
         return "added";
     }
 
     @Path("recommend")
     @GET
     @Produces({ MediaType.APPLICATION_JSON })
     public List<Blog> getRecommendation(
             @QueryParam("blogUrl")
             String blogUrl, 
             @QueryParam("engine") 
             @DefaultValue("default") 
             String engine) {
         IRecommendationCentre recommender = recommendationServices.get(engine);
         logger.info("Recommending for {} with {}", blogUrl, engine);
         if (recommender == null) {
             logger.info("There is no engine {}, throwing HTTP 400", engine);
             throw new WebApplicationException(
                     Response.status(Status.BAD_REQUEST)
                    .entity("Unknown recommendation engine \"" + engine + "\"")
                     .build());
         }
         return recommender.getRecommendations(storageService.findBlog(blogUrl));
     }
 
     @Path("testdata")
     @GET
     public void addTestData() {
         storageService.addBlog(new Blog("http://blog.xebia.com/1", "First Blog", "~lvanderpoel", null, "Very smart text"));
         storageService.addBlog(new Blog("http://blog.xebia.com/2", "Second Blog", "~lvanderpoel", null, "Very smart text"));
         storageService.addBlog(new Blog("http://blog.xebia.com/3", "Third Blog", "~lvanderpoel", null, "Very smart text"));
         storageService.addBlog(new Blog("http://blog.xebia.com/4", "Fourth Blog", "~lvanderpoel", null, "Very smart text"));
         storageService.addBlog(new Blog("http://blog.xebia.com/5", "Fifth", "~mdasberg", null, "Very smart text"));
         storageService.addBlog(new Blog("http://blog.xebia.com/6", "Sixth Blog", "~mdasberg", null, "Very smart text"));
         storageService.addBlog(new Blog("http://blog.xebia.com/7", "Seventh Blog", "~mdasberg", null, "Very smart text"));
         storageService.addBlog(new Blog("http://blog.xebia.com/8", "Eighth Blog", "~bgarvelink", null, "Very smart text"));
         storageService.addBlog(new Blog("http://blog.xebia.com/9", "Ninth Blog", "~bgarvelink", null, "Very smart text"));
     }
 }
