 package it.dariofabbri.test.backbone.service.rest;
 
 import it.dariofabbri.test.backbone.dto.rest.Book;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @Path("/books")
 @Produces("application/json")
 public class BookResource {
 
 	private static final Logger logger = LoggerFactory.getLogger(BookResource.class);
 	
	private static List<Book> books;
 	
	static {
		books = new ArrayList<Book>();
 
 		Book book = new Book();
 		book.setId(1);
 		book.setAuthor("Dario Fabbri");
 		book.setTitle("Best tutorial ever");
 		book.setReleaseDate("10/9/2012");
 		book.setKeywords("Tech");
 		books.add(book);
 		
 		book = new Book();
 		book.setId(2);
 		book.setAuthor("Gino Pilota");
 		book.setTitle("The truth on JavaScript");
 		book.setReleaseDate("11/9/2012");
 		book.setKeywords("Tech");
 		books.add(book);
 	}
 	
	public BookResource() {
	}
	
 	@GET
 	public List<Book> getBooks() {
 
 		logger.debug("getBooks called!");
 		return books;
 	}
 	
 	@DELETE
 	@Path("/{id}")
 	public Response deleteBook(@PathParam("id") Integer id) {
 		logger.debug("deleteBook called!");
 		
 		Book bookToBeDeleted = null;
 		for(Book book : books) {
 			if(book.getId().equals(id)) {
 				bookToBeDeleted = book;
 			}
 		}
 		
 		if(bookToBeDeleted != null) {
 			books.remove(bookToBeDeleted);
 			return Response.ok().build();
 		}
 		else {
 			return Response.status(Status.NOT_FOUND).build();
 		}
 	}
 	
 	@POST
 	@Consumes("application/json")
 	public Response createBook(Book book) {
 		
 		logger.debug("createBook called!");
 		
 		books.add(book);
 		return Response.ok().build();
 	}
 }
