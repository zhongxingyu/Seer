 package org.guiceae.main.web;
 
 import com.google.appengine.api.users.UserServiceFactory;
 import com.sun.jersey.api.view.Viewable;
 import org.guiceae.main.model.Article;
 import org.guiceae.main.repositories.ArticleRepository;
 import org.guiceae.util.UserPrincipalHolder;
 
 import javax.inject.Inject;
 import javax.ws.rs.*;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import java.net.URI;
 import java.net.URISyntaxException;
 
 @Path("/app/article")
 public class ArticleController {
     @Inject
     ArticleRepository articleRepository;
 
     @GET
     @Path("/edit/{id}")
     public Viewable givenFeed(@PathParam("id") Long id){
         Article article = articleRepository.getArticle(id);
         return new Viewable("/editArticle.jsp",article);
     }
 
     @GET
     @Path("/add/{feed}")
     public Viewable givenFeed(@PathParam("feed") String feed){
         Article article = new Article();
         article.setFeed(feed);
         article.setId(0L);
         article.setContent("");
         return new Viewable("/editArticle.jsp",article);
     }
 
     @DELETE
     @Path("/delete/{id}")
     public Response delete(@PathParam("id") Long id) throws URISyntaxException{
         articleRepository.delete(id);
         return Response.ok().build();
     }
 
     @POST
    @Path("/publish/{id}")
    public Response publish(@PathParam("id") Long id) throws URISyntaxException{
        articleRepository.publish(id);
        return Response.ok().build();
    }

    @POST
     @Path("/save")
     public Response saveArticle(@FormParam("id") Long id,
                                 @FormParam("feed") String feed,
                                 @FormParam("title") String title,
                                 @FormParam("permalink") String permalink,
                                 @FormParam("content") String content) throws URISyntaxException{
         Article article = new Article();
         article.setId(id);
         article.setContent(content);
         article.setFeed(feed);
         article.setTitle(title);
         article.setPermalink(permalink);
         article.setContent(content);
         if (article.getAuthor()==null){
             article.setAuthor(UserServiceFactory.getUserService().getCurrentUser().getNickname());
         }
         articleRepository.mergeArticle(article);
         return Response.seeOther(new URI("/app/feed/"+article.getFeed())).build();
     }
 }
