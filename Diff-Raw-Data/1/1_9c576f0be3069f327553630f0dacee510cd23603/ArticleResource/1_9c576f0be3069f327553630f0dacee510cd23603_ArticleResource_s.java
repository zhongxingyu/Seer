 package no.orientering.resources;
 
 import java.net.URI;
 import java.util.List;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.HeaderParam;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.*;
 import javax.ws.rs.core.Response.Status;
 
 import no.orientering.models.User;
 
 import no.orientering.DAO.jdbc.ArticleDAO;
 import no.orientering.DAO.jdbc.UserDAO;
 import no.orientering.models.Article;
 import no.orientering.utils.AuthHelper;
 
 @Path("/articles")
 @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
 @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
 public class ArticleResource {
 	private final ArticleDAO db = new ArticleDAO();
 	
 	@GET
 	public final List<Article> getAllArticles(){
 		return db.getArticles();
 	}
 	
 	@GET
 	@Path("{id}")
 	public final Article getSpecificArticle(@PathParam("id") final int id){
 		return db.getArticle(id);
 	}
 	
 	@POST
 	public final Response makeArticle(@HeaderParam("Authorization") final String authHeader, final Article article){
 		User current = AuthHelper.AuthorizeFromHeader(authHeader);
 		if(current == null){
 			throw new WebApplicationException(Status.UNAUTHORIZED);
 		}
		UserDAO users = new UserDAO();
 		
 		article.setAuthor(current);
 		
 		final int id = db.saveNew(article);
 		return Response.created(URI.create("/" + id)).build();
 	}
 	
 	@PUT
 	@Path("{id}")
 	public final void putArticle(@HeaderParam("Authorization") final String authHeader, @PathParam("id") final int id, final Article article){
 		User current = AuthHelper.AuthorizeFromHeader(authHeader);
 		if(current == null){
 			throw new WebApplicationException(Status.UNAUTHORIZED);
 		}
 		if(db.getArticle(id) == null){
 			throw new WebApplicationException(Status.FORBIDDEN);
 		}
 		
 		db.saveArticle(article);
 	}
 	
 
 }
