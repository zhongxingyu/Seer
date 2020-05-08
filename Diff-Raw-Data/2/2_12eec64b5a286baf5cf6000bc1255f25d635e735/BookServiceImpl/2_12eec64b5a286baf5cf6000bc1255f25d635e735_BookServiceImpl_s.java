 package net.sukharevd.ws.rs.services;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import javax.ws.rs.Path;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 
 import net.sukharevd.ws.rs.entities.Book;
 
 import com.google.common.io.ByteStreams;
 import com.sun.jersey.core.header.FormDataContentDisposition;
 
 /** Implementation of book-related web-service. */
 @Path("books")
 public class BookServiceImpl implements BookService {
 
     /** File format that is allowed to upload. */
     private static final String ALLOWED_FILE_TYPE = "png";
     
     /** Maximum file size that can be uploaded. */
     private static final long FILE_SIZE_LIMIT = 1024 * 1024;
     
     /** Path to directory to which files will be uploaded. Web-service should have writing access to it. */
    private static final String UPLOAD_DIR_PATH = "/home/dmitriy/Desktop/upload/";
     
     /** Collection of books. */
     static Map<String, Book> index = new HashMap<String, Book>();
 
     static {
         List<Book> books = new CopyOnWriteArrayList<Book>();
         books.add(new Book("9780321356680", "Effective Java™, Second Edition", new String[] { "Joshua Bloch" }));
         books.add(new Book("9781617290503", "Android in Action, Third Edition", new String[] { "W. Frank Ableson", "Robi Sen", "Chris King", "C. Enrique Ortiz" }));
         books.add(new Book("9781935182993", "EJB 3 in Action, Second Edition", new String[] { "Debu Panda", "Reza Rahman", "Ryan Cuprak" }));
         books.add(new Book("1932394885", "Java Persistence with Hibernate", new String[] { "Christian Bauer", "Gavin King" }));
         books.add(new Book("9781935182351", "Spring in Action, Third Edition", new String[] { "Craig Walls" }));
         books.add(new Book("1932394419", "Hibernate Quickly", new String[] { "Patrick Peak", "Nick Heudecker" }));
         books.add(new Book("9781935182191", "Hadoop in Action", new String[] { "Chuck Lam" }));
         books.add(new Book("1933988177", "Lucene in Action, Second Edition", new String[] { "Michael McCandless", "Erik Hatcher", "Otis Gospodnetić" }));
         books.add(new Book("9781935182689", "Mahout in Action", new String[] { "Sean Owen", "Robin Anil", "Ted Dunning", "Ellen Friedman" }));
         for (Book book : books) {
             index.put(book.getId(), book);
         }
     }
     
     @Context UriInfo uriInfo;
     
     @Override
     public Book getBook(String id) {
         return index.get(id);
     }
     
     @Override
     public List<Book> getAllBooks() {
         return new ArrayList<Book>(index.values());
     }
 
     @Override
     public Response createBook(Book newbie) {
         if (newbie == null || newbie.getId() == null || newbie.getId().isEmpty()) {
             return Response.status(400).entity("Resource can't be null or have empty ID").build();
         }
         index.put(newbie.getId(), newbie);
         return Response.created(uriInfo.getAbsolutePath()).build();
     }
 
     @Override
     public Response updateBook(String id, Book book) {
         book.setId(id);
         index.put(id, book);
         return Response.ok().build();
     }
 
     @Override
     public Response deleteBook(String id) {
         Book victim = index.remove(id);
         if (victim != null) {
             return Response.status(200).build();
         }
         throw new WebApplicationException(Response.status(404).build());
     }
 
     @Override
     public Response downloadFile(String filename) throws IOException {
         File file = new File(UPLOAD_DIR_PATH + filename);
         return downloadFile(file);
     }
 
     protected Response downloadFile(File file) throws IOException {
         FileInputStream fis = null;
         try {
             fis = new FileInputStream(file);
             byte[] bytes = ByteStreams.toByteArray(fis);
             return Response.status(200).entity(bytes).build();
         } finally {
             if (fis != null) {
                 fis.close();
             }
         }
     }
 
     @Override
     public Response uploadFile(InputStream fileStream,
             FormDataContentDisposition fileDetail) throws IOException {
         if (fileDetail.getFileName() == null/* || !fileDetail.getFileName().endsWith(ALLOWED_FILE_TYPE)*/) {
             return Response.status(415).entity("Wrong image format").build();
         }
         File file = new File(UPLOAD_DIR_PATH + fileDetail.getFileName());
         try {
             return uploadFile(fileStream, file);
         } catch (FileSizeLimitExceededException e) {
             file.delete();
             return Response.status(413).entity("Request Entity Too Large").build();
         }
     }
 
     protected Response uploadFile(InputStream fis, File file) throws IOException, FileSizeLimitExceededException {
         FileOutputStream fos = null;
         try {
             fos = new FileOutputStream(file);
             byte[] buf = new byte[1024];
             int totalRead = 0;
             int read;
             while ((read = fis.read(buf)) > 0) {
                 fos.write(buf, 0, read);
                 totalRead += read;
                 if (totalRead >= FILE_SIZE_LIMIT) {
                     throw new FileSizeLimitExceededException();
                 }
             }
             String output = "File uploaded to : " + file.getAbsolutePath();
             System.out.println(output);
             return Response.status(200).entity(output).build();
         } finally {
             if (fis != null) {
                 fis.close();
             }
             if (fos != null) {
                 fos.close();
             }
         }
     }
 
     public class FileSizeLimitExceededException extends Exception {
         private static final long serialVersionUID = -5134454047355573562L;
     }
 }
