 package com.podiobooks.iphone.service;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.nio.CharBuffer;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import com.podiobooks.iphone.Book;
 import com.podiobooks.iphone.BookList;
 import com.podiobooks.iphone.Episode;
 import com.podiobooks.iphone.dao.DefaultFeedDao;
 import com.podiobooks.iphone.dao.FeedDao;
 import com.sun.jersey.spi.resource.Singleton;
 import com.sun.syndication.feed.synd.SyndCategory;
 import com.sun.syndication.feed.synd.SyndEnclosure;
 import com.sun.syndication.feed.synd.SyndEntry;
 import com.sun.syndication.feed.synd.SyndFeed;
 import com.sun.syndication.io.SyndFeedInput;
 
 /**
  * The default implementation of the book service, exposing the methods as a
  * RESTful web service via the Jersey JAX-RS framework.
  * 
  * @author mkimberlin
  */
 @Path("books")
 @Singleton
 public class DefaultBookService implements BookService {
 //    private static final Log log = LogFactory.getLog(DefaultBookService.class);
     
     // TODO: Implement Guice injection...
     FeedDao feedDao = new DefaultFeedDao();
 
     private static final String TITLE_PLACEHOLDER = "__TITLE_PLACEHOLDER__";
    // TODO: Make these property or database driven...
     private static final String BASE_BOOK_FEED_URL = "http://podiobooks.com/title/"
             + TITLE_PLACEHOLDER + "/feed/";
     private static final String ALL_BOOKS_FEED = "http://www.podiobooks.com/opml/all/";
     private static final String BASE_SEARCH_URL = "http://www.podiobooks.com/podiobooks/search.php?includeAdult=1&keyword=";
     private static final String MAIN_URL = "http://www.podiobooks.com/";
     
     /**
      * {@inheritDoc}
      */
     @GET
     @Path("title/{title}")
     @Produces("application/json")
     @Override
     public Book getBook(@PathParam("title") String title) {
         String url = BASE_BOOK_FEED_URL.replaceFirst(TITLE_PLACEHOLDER, title);
         return getBookByFeedUrl(url);
     }
    
    @SuppressWarnings("unchecked")
     private Book getBookByFeedUrl(String url) {
         Book book = new Book();
         try {
             Document doc = feedDao.retrieveFeed(url);
             SyndFeedInput input = new SyndFeedInput();
             SyndFeed feed = input.build(doc);
             book.setTitle(feed.getTitle().substring(0,
                 feed.getTitle().indexOf(" - A free audiobook by")));
             book.setCopyright(feed.getCopyright());
             book.setDescription(feed.getDescription());
             book.setImageUrl(feed.getImage().getUrl());
             book.setLastUpdated(feed.getPublishedDate());
             book.setCategories(parseCategoryNames(feed.getCategories()));
             book.setUrl(feed.getLink());
 
             book.setEpisodes(parseEpisodes(feed.getEntries()));
 
             // Author is not provided as a part of the core RSS portion of the
             // feed.  As such, it must be taken from the iTunes portion.
             NodeList authorElems = doc.getElementsByTagName("itunes:author");
             book.setAuthors(parseAuthors(authorElems));
         } catch (Exception e) {
 //            log.error("An error occurred while retrieving or parsing the RSS feed: "
 //                + url, e);
             book.setTitle(null);
         }
 
         return book;
     }
     
     private List<String> parseAuthors(NodeList authorNodes) {
         List<String> authors = new ArrayList<String>();
         for(int i=0; i<authorNodes.getLength(); i++) {
             Node authorNode = authorNodes.item(i);
             if (authorNode != null) {
                 String author = authorNode.getTextContent();
                 if(!authors.contains(author) && !"Podiobooks Staff".equalsIgnoreCase(author))
                     authors.add(author);
             }    
         }
         return authors;
     }
 
     private List<Episode> parseEpisodes(List<SyndEntry> entries) {
         List<Episode> episodes = new ArrayList<Episode>();
         for (SyndEntry entry : entries) {
             Episode ep = new Episode();
             ep.setTitle(entry.getTitle());
             // Assuming a single enclosure here. I think that is a safe bet.
             ep.setUrl(((SyndEnclosure) entry.getEnclosures().get(0)).getUrl());
             episodes.add(ep);
         }
         return episodes;
     }
 
     private List<String> parseCategoryNames(List<SyndCategory> categories) {
         List<String> catNames = new ArrayList<String>();
         for (SyndCategory cat : categories) {
             catNames.add(cat.getName());
         }
         return catNames;
     }
 
     @GET
     @Path("category/{category}")
     @Produces("application/json")
     @Override
     public BookList getBooks(@PathParam("category")String category) {
         category = category.replaceAll("-_-", "/");
         List<Book> books = new ArrayList<Book>();
         try {
             Document doc = feedDao.retrieveFeed(ALL_BOOKS_FEED);
             NodeList children = doc.getElementsByTagName("body").item(0).getChildNodes().item(1).getChildNodes();
             for(int i=0; i<children.getLength(); i++) {
                 Node child = children.item(i);
                 if(child.getNodeName().compareToIgnoreCase("outline") == 0
                     && child.getAttributes().getNamedItem("text").getNodeValue().compareToIgnoreCase(category) == 0) {
                     books = getBooks(child.getChildNodes());
                 }
             }
         } catch(Exception e) {
 //            log.error("An error occured while loading books for the category \"" +category+"\".", e);
         }
         BookList list = new BookList();
         list.setBooks(books);
         return list;
     }
 
     private List<Book> getBooks(NodeList categoryListing) {
         List<Book> books = new ArrayList<Book>();
         for(int i=0; i<categoryListing.getLength(); i++) {
             Node bookNode = categoryListing.item(i);
             if(bookNode.getNodeName().compareToIgnoreCase("outline") == 0) {
                 Book book = new Book();
                 book.setTitle(bookNode.getAttributes().getNamedItem("text").getNodeValue());
                 book.setFeedUrl(bookNode.getAttributes().getNamedItem("url").getNodeValue());
                 books.add(book);
             }
         }
         return books;
     }
 
     @GET
     @Path("search")
     @Produces("application/json")
     @Override
     public BookList searchBooks(@QueryParam("keyword") String keywords) {
         BookList bookList = new BookList();
         try {keywords = URLEncoder.encode(keywords, "UTF-8");}catch(Exception e ) {}
         List<Book> books = new ArrayList<Book>();
         try {
             URL url = new URL(BASE_SEARCH_URL+keywords);
             BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
             StringBuilder result = new StringBuilder();
             CharBuffer current = CharBuffer.allocate(1024);
             while (in.read(current) != -1) {
                 result.append(current.array());
                 current.clear();
             }
             while(result.indexOf("/title/") >= 0) {
                 result.delete(0, result.indexOf("/title/")+7);
                 int nextPos = result.indexOf("/title/")+7;
                 int endPos = result.indexOf("\"", nextPos);
                 String titleUrlFragment = result.substring(nextPos, endPos);
                 result.delete(0, endPos);
                 String title = result.substring(result.indexOf(">")+1, result.indexOf("</a"));
                result.delete(0, result.indexOf("</tr>"));
                 Book book = new Book();
                 book.setTitle(title);
                 book.setFeedUrl(BASE_BOOK_FEED_URL.replaceFirst(TITLE_PLACEHOLDER, titleUrlFragment));
                 books.add(book);
             }
         } catch (Exception e) {}
         bookList.setBooks(books);
         return bookList;
     }
     
     @GET
     @Path("recent")
     @Produces("application/json")
     @Override
     public BookList getRecentUpdates() {
         BookList bookList = new BookList();
         List<Book> books = new ArrayList<Book>();
         try {
             URL url = new URL(MAIN_URL);
             BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
             StringBuilder result = new StringBuilder();
             CharBuffer current = CharBuffer.allocate(1024);
             while (in.read(current) != -1) {
                 result.append(current.array());
                 current.clear();
             }
             //Snip out the recent updates section...
             result.delete(0, result.indexOf("<p>Recent Updates</p>"));
             result.delete(result.indexOf("</ul>"), result.length());
             
             while(result.indexOf("/title/") >= 0) {
                 int nextPos = result.indexOf("/title/")+7;
                 int endPos = result.indexOf("/\"", nextPos);
                 String titleUrlFragment = result.substring(nextPos, endPos);
                 result.delete(0, endPos);
                 nextPos = result.indexOf(">")+1;
                 endPos = result.indexOf("</a");
                 String title = result.substring(nextPos, endPos);
                 result.delete(0, endPos);
                 Book book = new Book();
                 book.setTitle(title);
                 book.setFeedUrl(BASE_BOOK_FEED_URL.replaceFirst(TITLE_PLACEHOLDER, titleUrlFragment));
                 books.add(book);
             }
         } catch (Exception e) {}
         bookList.setBooks(books);
         return bookList;
     }
 
 }
