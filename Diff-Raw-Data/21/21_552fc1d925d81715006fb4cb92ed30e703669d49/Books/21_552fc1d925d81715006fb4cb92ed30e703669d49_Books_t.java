 package com.jayway.jerseylab;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
 
 @Path("/books")
 public class Books {
 
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	public List<Book> getAllBooks() {
 		ArrayList<Book> books = new ArrayList<Book>();
 		books.add(new Book("Bo", "Bok1", "12345"));
 		books.add(new Book("Albin", "Bok2", "54321"));
 		books.add(new Book("Groda", "grodans bok", "12643"));
 		return books;
 	}
 
 	@GET
 	@Path("/{bookId}")
 	@Produces(MediaType.APPLICATION_JSON)
	public Response getBook(@PathParam("bookId") String id) {
		if (id.startsWith("999")) {
			return Response
					.status(503)
					.entity("Id was 999... simulating service unavailable")
					.build();
		}
		return Response
					.ok(new Book("Bo Ek", "Book of Bo", id))
					.build();
 	}

 	@GET
 	@Path("/search")
 	@Produces(MediaType.TEXT_PLAIN)
 	public String search(@QueryParam("title") String query) {
 		return "Book search query: " + query;
 	}
 
 }
