 package com.podiobooks.iphone.service;
 
 import com.podiobooks.iphone.Book;
 import com.podiobooks.iphone.BookList;
 
 /**
  * A service for accessing information on any of the titles at Podiobooks.
  * 
  * @author mkimberlin
  */
 public interface BookService {
 
     /**
      * Retrieves the detailed information about a book with the given title.
      * For this service, the title is the one used by the current
      * podiobooks.com URL structure.  For the 2.0 site, the application will
      * make use of services provided directly by the main podiobooks.com code
      * base.
      * 
      * If an error should occur, it will be indicated by returning a Book
      * with a <code>null</code> title.
      * 
      * @param title  the book title as defined by the current URL structure
      *        (i.e. "alibi-jones" or "the-rookie")
      * @return  the detailed Book information, including currently available
      *          episodes
      */
     public Book getBook(String title);
     
     /**
      * Retrieves the list of all books belonging to the provided category.
      * 
      * Since this service is making use of the current podiobooks.com site,
      * the category must be specified by name (ids are not available in the
      * OPML feed).  Episodes for the books will not be listed, only book
      * titles and feed urls.
      * 
      * @param category  the name of the category of books to be retrieved
      * @return the list of books belonging to the specified category, with only
      *         title and feedUrl populated
      */
     public BookList getBooks(String category);
     
     /**
      * Performs a search of all books for the given keywords.
      * 
      * @param keywords  the keywords to be used in the search
      * @return the list of books matching the provided keywords with only the
      *         title and feedUrl populated
      */
     public BookList searchBooks(String keywords);
     
     /**
      * Retrieves the list of recent updates from the podiobooks.com front page.
      * 
      * @return the list of books containing recent updates with only the title
      *         and feedUrl populated
      */
     public BookList getRecentUpdates();
 }
