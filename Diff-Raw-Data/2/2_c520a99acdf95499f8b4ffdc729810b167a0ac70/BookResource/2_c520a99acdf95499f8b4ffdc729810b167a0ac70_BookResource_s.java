 package com.sismics.books.rest.resource;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.nio.file.Paths;
 import java.text.MessageFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import org.apache.commons.io.IOUtils;
 import org.codehaus.jettison.json.JSONException;
 import org.codehaus.jettison.json.JSONObject;
 
 import com.google.common.base.Strings;
 import com.google.common.collect.Lists;
 import com.sismics.books.core.dao.jpa.BookDao;
 import com.sismics.books.core.dao.jpa.TagDao;
 import com.sismics.books.core.dao.jpa.UserBookDao;
 import com.sismics.books.core.dao.jpa.UserDao;
 import com.sismics.books.core.dao.jpa.criteria.UserBookCriteria;
 import com.sismics.books.core.dao.jpa.dto.TagDto;
 import com.sismics.books.core.dao.jpa.dto.UserBookDto;
 import com.sismics.books.core.event.BookImportedEvent;
 import com.sismics.books.core.model.context.AppContext;
 import com.sismics.books.core.model.jpa.Book;
 import com.sismics.books.core.model.jpa.Tag;
 import com.sismics.books.core.model.jpa.User;
 import com.sismics.books.core.model.jpa.UserBook;
 import com.sismics.books.core.util.DirectoryUtil;
 import com.sismics.books.core.util.jpa.PaginatedList;
 import com.sismics.books.core.util.jpa.PaginatedLists;
 import com.sismics.books.core.util.jpa.SortCriteria;
 import com.sismics.rest.exception.ClientException;
 import com.sismics.rest.exception.ForbiddenClientException;
 import com.sismics.rest.exception.ServerException;
 import com.sismics.rest.util.ValidationUtil;
 import com.sun.jersey.multipart.FormDataBodyPart;
 import com.sun.jersey.multipart.FormDataParam;
 
 /**
  * Book REST resources.
  * 
  * @author bgamard
  */
 @Path("/book")
 public class BookResource extends BaseResource {
     /**
      * Creates a new book.
      * 
      * @param isbn ISBN Number
      * @return Response
      * @throws JSONException
      */
     @PUT
     @Produces(MediaType.APPLICATION_JSON)
     public Response add(
             @FormParam("isbn") String isbn) throws JSONException {
         if (!authenticate()) {
             throw new ForbiddenClientException();
         }
         
         // Validate input data
         isbn = ValidationUtil.validateLength(isbn, "isbn", 10, 13, false);
         
         // Fetch the book
         BookDao bookDao = new BookDao();
         Book book = bookDao.getByIsbn(isbn);
         if (book == null) {
             // Try to get the book from a public API
             try {
                 book = AppContext.getInstance().getBookDataService().searchBook(isbn);
             } catch (Exception e) {
                throw new ClientException("BookNotFound", e.getMessage(), e);
             }
             
             // Save the new book in database
             bookDao.create(book);
         }
         
         // Create the user book if needed
         UserBookDao userBookDao = new UserBookDao();
         UserBook userBook = userBookDao.getByBook(book.getId(), principal.getId());
         if (userBook == null) {
             userBook = new UserBook();
             userBook.setUserId(principal.getId());
             userBook.setBookId(book.getId());
             userBook.setCreateDate(new Date());
             userBookDao.create(userBook);
         } else {
             throw new ClientException("BookAlreadyAdded", "Book already added");
         }
         
         JSONObject response = new JSONObject();
         response.put("id", userBook.getId());
         return Response.ok().entity(response).build();
     }
     
     /**
      * Get a book.
      * 
      * @param id User book ID
      * @return Response
      * @throws JSONException
      */
     @GET
     @Path("{id: [a-z0-9\\-]+}")
     @Produces(MediaType.APPLICATION_JSON)
     public Response get(
             @PathParam("id") String userBookId) throws JSONException {
         if (!authenticate()) {
             throw new ForbiddenClientException();
         }
         
         // Fetch the user book
         UserBookDao userBookDao = new UserBookDao();
         UserBook userBook = userBookDao.getUserBook(userBookId, principal.getId());
         if (userBook == null) {
             throw new ClientException("BookNotFound", "Book not found with id " + userBookId);
         }
         
         // Fetch the book
         BookDao bookDao = new BookDao();
         Book bookDb = bookDao.getById(userBook.getBookId());
         
         // Return book data
         JSONObject book = new JSONObject();
         book.put("id", userBook.getId());
         book.put("title", bookDb.getTitle());
         book.put("subtitle", bookDb.getSubtitle());
         book.put("author", bookDb.getAuthor());
         book.put("page_count", bookDb.getPageCount());
         book.put("description", bookDb.getDescription());
         book.put("isbn10", bookDb.getIsbn10());
         book.put("isbn13", bookDb.getIsbn13());
         book.put("language", bookDb.getLanguage());
         book.put("publish_date", bookDb.getPublishDate().getTime());
         book.put("create_date", userBook.getCreateDate().getTime());
         if (userBook.getReadDate() != null) {
             book.put("read_date", userBook.getReadDate().getTime());
         }
         
         // Add tags
         TagDao tagDao = new TagDao();
         List<TagDto> tagDtoList = tagDao.getByUserBookId(userBookId);
         List<JSONObject> tags = new ArrayList<>();
         for (TagDto tagDto : tagDtoList) {
             JSONObject tag = new JSONObject();
             tag.put("id", tagDto.getId());
             tag.put("name", tagDto.getName());
             tag.put("color", tagDto.getColor());
             tags.add(tag);
         }
         book.put("tags", tags);
         
         return Response.ok().entity(book).build();
     }
     
     /**
      * Returns a book cover.
      * 
      * @param id User book ID
      * @return Response
      * @throws JSONException
      */
     @GET
     @Path("{id: [a-z0-9\\-]+}/cover")
     @Produces(MediaType.APPLICATION_OCTET_STREAM)
     public Response cover(
             @PathParam("id") final String userBookId) throws JSONException {
         // Get the user book
         UserBookDao userBookDao = new UserBookDao();
         UserBook userBook = userBookDao.getUserBook(userBookId);
         
         // Get the cover image
         File file = Paths.get(DirectoryUtil.getBookDirectory().getPath(), userBook.getBookId()).toFile();
         InputStream inputStream = null;
         try {
             if (file.exists()) {
                 inputStream = new FileInputStream(file);
             } else {
                 inputStream = new FileInputStream(new File(getClass().getResource("/dummy.png").getFile()));
             }
         } catch (FileNotFoundException e) {
             throw new ServerException("FileNotFound", "Cover file not found", e);
         }
 
         return Response.ok(inputStream)
                 .header("Content-Type", "image/jpeg")
                 .header("Expires", new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z").format(new Date().getTime() + 3600000 * 24))
                 .build();
     }
     
     /**
      * Returns all books.
      * 
      * @param limit Page limit
      * @param offset Page offset
      * @return Response
      * @throws JSONException
      */
     @GET
     @Path("list")
     @Produces(MediaType.APPLICATION_JSON)
     public Response list(
             @QueryParam("limit") Integer limit,
             @QueryParam("offset") Integer offset,
             @QueryParam("sort_column") Integer sortColumn,
             @QueryParam("asc") Boolean asc,
             @QueryParam("search") String search,
             @QueryParam("read") Boolean read,
             @QueryParam("tag") String tagName) throws JSONException {
         if (!authenticate()) {
             throw new ForbiddenClientException();
         }
         JSONObject response = new JSONObject();
         List<JSONObject> books = new ArrayList<>();
         
         UserBookDao userBookDao = new UserBookDao();
         TagDao tagDao = new TagDao();
         PaginatedList<UserBookDto> paginatedList = PaginatedLists.create(limit, offset);
         SortCriteria sortCriteria = new SortCriteria(sortColumn, asc);
         UserBookCriteria criteria = new UserBookCriteria();
         criteria.setSearch(search);
         criteria.setRead(read);
         criteria.setUserId(principal.getId());
         if (!Strings.isNullOrEmpty(tagName)) {
             Tag tag = tagDao.getByName(principal.getId(), tagName);
             if (tag != null) {
                 criteria.setTagIdList(Lists.newArrayList(tag.getId()));
             }
         }
         try {
             userBookDao.findByCriteria(paginatedList, criteria, sortCriteria);
         } catch (Exception e) {
             throw new ServerException("SearchError", "Error searching in books", e);
         }
 
         for (UserBookDto userBookDto : paginatedList.getResultList()) {
             JSONObject book = new JSONObject();
             book.put("id", userBookDto.getId());
             book.put("title", userBookDto.getTitle());
             book.put("subtitle", userBookDto.getSubtitle());
             book.put("author", userBookDto.getAuthor());
             book.put("language", userBookDto.getLanguage());
             book.put("publish_date", userBookDto.getPublishTimestamp());
             book.put("create_date", userBookDto.getCreateTimestamp());
             book.put("read_date", userBookDto.getReadTimestamp());
             
             // Get tags
             List<TagDto> tagDtoList = tagDao.getByUserBookId(userBookDto.getId());
             List<JSONObject> tags = new ArrayList<>();
             for (TagDto tagDto : tagDtoList) {
                 JSONObject tag = new JSONObject();
                 tag.put("id", tagDto.getId());
                 tag.put("name", tagDto.getName());
                 tag.put("color", tagDto.getColor());
                 tags.add(tag);
             }
             book.put("tags", tags);
             
             books.add(book);
         }
         response.put("total", paginatedList.getResultCount());
         response.put("books", books);
         
         return Response.ok().entity(response).build();
     }
     
     /**
      * Imports books.
      * 
      * @param fileBodyPart File to import
      * @return Response
      * @throws JSONException
      */
     @PUT
     @Consumes("multipart/form-data") 
     @Path("import")
     public Response importFile(
             @FormDataParam("file") FormDataBodyPart fileBodyPart) throws JSONException {
         if (!authenticate()) {
             throw new ForbiddenClientException();
         }
         
         // Validate input data
         ValidationUtil.validateRequired(fileBodyPart, "file");
 
         UserDao userDao = new UserDao();
         User user = userDao.getById(principal.getId());
         
         InputStream in = fileBodyPart.getValueAs(InputStream.class);
         File importFile = null;
         try {
             // Copy the incoming stream content into a temporary file
             importFile = File.createTempFile("books_import", null);
             IOUtils.copy(in, new FileOutputStream(importFile));
             
             BookImportedEvent event = new BookImportedEvent();
             event.setUser(user);
             event.setImportFile(importFile);
             AppContext.getInstance().getImportEventBus().post(event);
             
             // Always return ok
             JSONObject response = new JSONObject();
             response.put("status", "ok");
             return Response.ok().entity(response).build();
         } catch (Exception e) {
             if (importFile != null) {
                 try {
                     importFile.delete();
                 } catch (SecurityException e2) {
                     // NOP
                 }
             }
             throw new ServerException("ImportError", "Error importing books", e);
         }
     }
     
     /**
      * Updates the user book.
      * 
      * @param userBookId User book ID
      * @return Response
      * @throws JSONException
      */
     @POST
     @Path("{id: [a-z0-9\\-]+}")
     @Produces(MediaType.APPLICATION_JSON)
     public Response update(
             @PathParam("id") String userBookId,
             @FormParam("tags") List<String> tagList) throws JSONException {
         if (!authenticate()) {
             throw new ForbiddenClientException();
         }
         
         // Get the user book
         UserBookDao userBookDao = new UserBookDao();
         UserBook userBook = userBookDao.getUserBook(userBookId, principal.getId());
         if (userBook == null) {
             throw new ClientException("BookNotFound", "Book not found: " + userBookId);
         }
         
         // Update tags
         if (tagList != null) {
             TagDao tagDao = new TagDao();
             Set<String> tagSet = new HashSet<>();
             Set<String> tagIdSet = new HashSet<>();
             List<Tag> tagDbList = tagDao.getByUserId(principal.getId());
             for (Tag tagDb : tagDbList) {
                 tagIdSet.add(tagDb.getId());
             }
             for (String tagId : tagList) {
                 if (!tagIdSet.contains(tagId)) {
                     throw new ClientException("TagNotFound", MessageFormat.format("Tag not found: {0}", tagId));
                 }
                 tagSet.add(tagId);
             }
             tagDao.updateTagList(userBookId, tagSet);
         }
         
         // Return user book ID
         JSONObject response = new JSONObject();
         response.put("id", userBookId);
         return Response.ok().entity(response).build();
     }
     
     /**
      * Set a book as read/unread.
      * 
      * @param id User book ID
      * @param read Read state
      * @return Response
      * @throws JSONException
      */
     @POST
     @Path("{id: [a-z0-9\\-]+}/read")
     @Produces(MediaType.APPLICATION_JSON)
     public Response read(
             @PathParam("id") final String userBookId,
             @FormParam("read") boolean read) throws JSONException {
         // Get the user book
         UserBookDao userBookDao = new UserBookDao();
         UserBook userBook = userBookDao.getUserBook(userBookId);
         
         // Update the read date
         userBook.setReadDate(read ? new Date() : null);
         
         // Always return ok
         JSONObject response = new JSONObject();
         response.put("status", "ok");
         return Response.ok().entity(response).build();
     }
 }
