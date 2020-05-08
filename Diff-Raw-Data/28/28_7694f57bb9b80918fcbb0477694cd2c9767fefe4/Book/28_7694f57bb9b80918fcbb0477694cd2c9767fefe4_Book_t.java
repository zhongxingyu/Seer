 package domain;
 
 import java.sql.SQLException;
 
 import data.BookMapper;
 
 /**
  * Represent one book, with all relevant fields. Unsure whether to implement
  * foreign keys as int or Strings. Decided on Strings for now.
  * ISBN is set and retrieved via the getID and setID functions.
  * @author �sgeir Davidsson
  *
  */
 public class Book extends AbstractDomainClass {
 	
 	private int ISBN;
 	private String title;
 	private String authorLastName;
 	private String author;
 	private String genre;
 	private String publisher;
 	private String format;
 	private int quantity;
 	private int price;
 	
 	public Book(String author, String authorLastName, String genre, String publisher, String format, int price) {
 		this.author = author;
 		this.authorLastName = authorLastName;
 		this.genre = genre;
 		this.publisher = publisher;
 		this.format = format;
 		this.price = price;
 	}
 	
 	public Book(int ISBN, String title, String author, String authorLastName, String genre, String publisher, String format, int price) {
 		this.author = author;
 		this.title = title;
 		this.authorLastName = authorLastName;
 		this.genre = genre;
 		this.publisher = publisher;
 		this.format = format;
 		this.price = price;
 		this.ISBN = ISBN;
 	}
 	
 	public Book(int ISBN){
 		this.ISBN = ISBN;
 	}
 
 
 	public String getAuthor() {
 		return author;
 	}
 
 	public void setAuthor(String author) {
 		this.author = author;
 	}
 
 	public String getGenre() {
 		return genre;
 	}
 
 	public void setGenre(String genre) {
 		this.genre = genre;
 	}
 
 	public String getPublisher() {
 		return publisher;
 	}
 
 	public void setPublisher(String publisher) {
 		this.publisher = publisher;
 	}
 
 	public String getFormat() {
 		return format;
 	}
 
 	public void setFormat(String format) {
 		this.format = format;
 	}
 
 	public int getPrice() {
 		return price;
 	}
 
 	public void setPrice(int price) {
 		this.price = price;
 	}
 
 	@Override
 	public String getName() {
 		return this.title;
 	}
 
 	@Override
 	public String getLastName() {
 		return this.authorLastName;
 	}
 
 	@Override
 	public void setName(String name) {
 		this.title = name;
 		
 	}
 
 	@Override
 	public void setLastName(String name) {
 		this.authorLastName = name;
 		
 	}
 
 	@Override
 	public int getID() {
 		return this.ISBN;
 	}
 
 	@Override
 	public void setID(int id) {
 		this.ISBN = id;
 		
 	}
 
 	public int getQuantity() {
 		return quantity;
 	}
 
 	public void setQuantity(int quantity) {
 		this.quantity = quantity;
 	}
 	
 	// Function for calling db-function to insert a new book. Returns true if succeeded. 
 	// Used from AddAddButtonListener.
	public boolean saveBook() {
 		BookMapper bm = new BookMapper();
 		try {
			if(bm.insert(this)) {
 				return true;
 			}
 			else {
 				return false;
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 		
 		return false;
 	}
 }
