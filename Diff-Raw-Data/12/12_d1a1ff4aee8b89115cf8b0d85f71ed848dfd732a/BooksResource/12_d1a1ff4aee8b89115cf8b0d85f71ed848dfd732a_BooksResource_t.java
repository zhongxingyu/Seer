 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.smartitengineering.demo.roa;
 
 import com.smartitengineering.demo.roa.domains.Book;
 import com.smartitengineering.demo.roa.services.AuthorNotFoundException;
 import com.smartitengineering.demo.roa.services.Services;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.ResponseBuilder;
 import javax.ws.rs.core.Response.Status;
 import javax.ws.rs.core.UriBuilder;
 import org.apache.abdera.model.Entry;
 import org.apache.abdera.model.Feed;
 import org.apache.abdera.model.Link;
 
 /**
  *
  * @author imyousuf
  */
 @Path("/books")
 public class BooksResource extends AbstractResource {
 
   static final UriBuilder BOOK_URI_BUILDER;
   static final UriBuilder BOOKS_AFTER_ISBN_BUILDER;
   static final UriBuilder BOOKS_BEFORE_ISBN_BUILDER;
 
   static {
     BOOK_URI_BUILDER = UriBuilder.fromResource(BookResource.class);
     BOOKS_BEFORE_ISBN_BUILDER = UriBuilder.fromResource(BooksResource.class);
     try {
       BOOKS_BEFORE_ISBN_BUILDER.path(BooksResource.class.getMethod("getBefore", String.class));
     }
     catch (Exception ex) {
       throw new InstantiationError();
     }
     BOOKS_AFTER_ISBN_BUILDER = UriBuilder.fromResource(BooksResource.class);
     try {
       BOOKS_AFTER_ISBN_BUILDER.path(BooksResource.class.getMethod("getAfter", String.class));
     }
     catch (Exception ex) {
       throw new InstantiationError();
     }
   }
   @QueryParam("name")
   private String nameLike;
   @QueryParam("author_nick")
   private String authorNickName;
   @QueryParam("count")
   private Integer count;
 
   @GET
   @Produces(MediaType.APPLICATION_ATOM_XML)
   @Path("/before/{beforeIsbn}")
   public Response getBefore(@PathParam("beforeIsbn") String beforeIsbn) {
     return get(beforeIsbn, true);
   }
 
   @GET
   @Produces(MediaType.APPLICATION_ATOM_XML)
   @Path("/after/{afterIsbn}")
   public Response getAfter(@PathParam("afterIsbn") String afterIsbn) {
     return get(afterIsbn, false);
   }
 
   @GET
   @Produces(MediaType.APPLICATION_ATOM_XML)
   public Response get() {
     return get(null, true);
   }
 
   public Response get(String isbn, boolean isBefore) {
     if (count == null) {
       count = 10;
     }
     ResponseBuilder responseBuilder = Response.ok();
     Feed atomFeed = getFeed("Books", new Date());
     Link booksLink = abderaFactory.newLink();
     booksLink.setHref(UriBuilder.fromResource(RootResource.class).build().toString());
     booksLink.setRel("root");
     atomFeed.addLink(booksLink);
     Collection<Book> books = Services.getInstance().getBookService().getBooks(authorNickName, nameLike, isbn, isBefore,
                                                                               count);
     if (books != null && !books.isEmpty()) {
       List<Book> bookList = new ArrayList<Book>(books);
       Link nextLink = abderaFactory.newLink();
       nextLink.setRel(Link.REL_NEXT);
       Book lastBook = bookList.get(books.size() - 1);
       nextLink.setHref(BOOKS_BEFORE_ISBN_BUILDER.clone().build(lastBook.getIsbn()).toString());
       atomFeed.addLink(nextLink);
       Link prevLink = abderaFactory.newLink();
       prevLink.setRel(Link.REL_PREVIOUS);
       Book firstBook = bookList.get(books.size() - 1);
       prevLink.setHref(BOOKS_AFTER_ISBN_BUILDER.clone().build(firstBook.getIsbn()).toString());
       atomFeed.addLink(prevLink);
       for (Book book : books) {
         Entry bookEntry = abderaFactory.newEntry();
        bookEntry.setId(book.getIsbn());
        bookEntry.setTitle(book.getName());
        bookEntry.setSummary(book.getName());
        bookEntry.setUpdated(book.getLastModifiedDate());
         Link bookLink = abderaFactory.newLink();
         bookLink.setHref(BookResource.BOOK_URI_BUILDER.clone().build(book.getIsbn()).toString());
        bookLink.setRel(Link.REL_ALTERNATE);
        bookLink.setMimeType(MediaType.APPLICATION_ATOM_XML);
         bookEntry.addLink(bookLink);
        atomFeed.addEntry(bookEntry);
       }
     }
     responseBuilder.entity(atomFeed);
     return responseBuilder.build();
 
   }
 
   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   public Response post(Book book) {
     ResponseBuilder responseBuilder;
     try {
       Services.getInstance().getAuthorService().populateAuthor(book);
       Services.getInstance().getBookService().save(book);
       responseBuilder = Response.status(Status.CREATED);
     }
     catch (AuthorNotFoundException ex) {
       responseBuilder = Response.status(Status.BAD_REQUEST);
     }
     catch (Exception ex) {
       responseBuilder = Response.status(Status.INTERNAL_SERVER_ERROR);
     }
     return responseBuilder.build();
   }
 }
