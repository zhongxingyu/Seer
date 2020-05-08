 package domain;
 
 import java.sql.SQLException;
 
 import presentation.AddBooksPanel;
 import presentation.BrowsePanel;
 
 import data.BookMapper;
 
 /**
  * Represent one book, with all relevant fields. Unsure whether to implement
  * foreign keys as int or Strings. Decided on Strings for now.
  * ISBN is set and retrieved via the getID and setID functions.
  * @author �sgeir Davidsson
  * @author Therese Karlander
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
 	/**
 	 * Constructors for Book. One with ISBN, one without, and one empty
 	 * @param author
 	 * @param authorLastName
 	 * @param genre
 	 * @param publisher
 	 * @param format
 	 * @param price
 	 */
 	public Book(String title, String author, String authorLastName, String genre, String publisher, String format,int quantity, int price) {
 		this.title = title;
 		this.author = author;
 		this.authorLastName = authorLastName;
 		this.genre = genre;
 		this.publisher = publisher;
 		this.format = format;
 		this.quantity = quantity;
 		this.price = price;
 	}
 	
 	public Book(int ISBN, String title, String author, String authorLastName, String genre, String publisher, String format, int quantity, int price) {
 		this.author = author;
 		this.title = title;
 		this.authorLastName = authorLastName;
 		this.genre = genre;
 		this.publisher = publisher;
 		this.format = format;
 		this.quantity = quantity;
 		this.price = price;
 		this.ISBN = ISBN;
 	}
 	
 	public Book(int ISBN){
 		this.ISBN = ISBN;
 	}
 	
 	public Book() {
 		
 	}
 	
 	public Book(int isbn, int price, int quantity) {
 		this.ISBN = isbn;
 		this.price = price;
 		this.quantity = quantity;
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
 	
 	/**
 	 * Method for calling db-method to insert a new book.
 	 * @return boolean
 	 */
 	public boolean saveBook() {
 		BookMapper bm = new BookMapper();
 		try {
 			bm.insert(this);
 			return true;
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return false;
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 	/**
 	 * Method for retrieving book from db
 	 * @param AddBooksPanel abp
 	 * @return
 	 */
 	public boolean getBook(AddBooksPanel abp) {
 		BookMapper bm = new BookMapper();
 		try {
 			Book book = bm.retrieve(ISBN);
 			if(book != null) {
 				abp.setBook(book);
 				abp.setCheckHiddenButtons(1);
 				return true;
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return false;
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 			return false;
 		} 
 		return false;
 	}
 	
 	/**
 	 * Method for retrieving a book from db. 
 	 * Its added to the upper JTable on startpage.
 	 * @param bp
 	 * @return
 	 */
 	public Boolean getBookToBuy(BrowsePanel bp) {
 		BookMapper bm = new BookMapper();
 		try {
 			Book book = bm.retrieve(ISBN);
 			if(book != null) {
 				bp.setBookInfo(book);
 				bp.setBookBuyInfo(book);
 				bp.addRowUpperTable();
 				return true;
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return false;
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 			return false;
 		} 
 		return false;
 	}
 	
 	/**
 	 * Method for updating a book in db.
 	 * @param abp
 	 * @return
 	 */
 	public Boolean updateBook(AddBooksPanel abp) {
 		BookMapper bm = new BookMapper();
 		try {
 				bm.update(this);
 				return true;
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return false;
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 	
 	/**
 	 * Method for a deleting a book in db.
 	 * @return
 	 */
 	public Boolean removeBook() {
 		BookMapper bm = new BookMapper();
 		try {
 			bm.delete(this);
 			return true;
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return false;
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 	
 	/**
 	 * Method thats update the books quantity in db, 
 	 * and adds info in sales table
 	 * @return
 	 */
 	public Boolean sellBook() {
 		BookMapper bm = new BookMapper();
 		try {
 			bm.sell(this);
 			return true;
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return false;
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 	
 	/**
 	 * Check if fields valid when adding a new book
 	 * @return boolean if it is valid
 	 */
 	public boolean isNewBookValid(){
 		return !this.title.isEmpty() && !this.author.isEmpty() && !this.authorLastName.isEmpty() && 
 			   !this.genre.isEmpty() && !this.publisher.isEmpty() && !this.format.isEmpty() && 
 			   this.price >= 1 && this.ISBN >= 1;
 		
 	}	
 	
 	/**
 	 * Check if fields are valid when editing a book
 	 * @return boolean if it is valid
 	 */
 	public boolean isEditBookValid(){
 		return !this.author.isEmpty() && !this.authorLastName.isEmpty() && 
 			   !this.genre.isEmpty() && !this.publisher.isEmpty() && !this.format.isEmpty() && 
 			   this.price >= 1;
 		
 	}
 }
