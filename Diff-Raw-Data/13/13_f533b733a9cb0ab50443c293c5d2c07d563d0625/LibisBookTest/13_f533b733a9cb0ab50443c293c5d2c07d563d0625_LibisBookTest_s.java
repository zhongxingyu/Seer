 package bookshelf.apis.libis.junit;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.util.ArrayList;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import bookshelf.AbstractBook;
 import bookshelf.ISBN;
 import bookshelf.apis.libis.LibisBookshelf;
 
 
 public class LibisBookTest {
 	
	LibisBookshelf collection = new LibisBookshelf("SG4JHVPDF9CHC5V9D2XQE4Y6278V1KFUCFDRDV6UGY7J9R76MA-25544");
 	ArrayList<AbstractBook> books;
 
 	@Before
 	public void setUp() throws Exception {
		books = collection.getBooks("009906485");
 	}
 	
 	@Test
 	public void retrieveBookMetadata() {
 		AbstractBook book = books.get(0);
 		
 		String title = "An introduction to object-oriented programming with Java";
 		String author = "Wu, C. Thomas";
 		String publisher = "McGraw Hill";
 		int year = 2004;
 		ISBN isbn = new ISBN("0072518847");
 		
 		assertEquals(title, book.getTitle());
 		assertEquals(year, book.getPublishingYear());
 		assertEquals(isbn, book.getISBN());
 		assertTrue(book.getAuthors().contains(author));
 		assertEquals(publisher, book.getPublisher());
 	}
 }
