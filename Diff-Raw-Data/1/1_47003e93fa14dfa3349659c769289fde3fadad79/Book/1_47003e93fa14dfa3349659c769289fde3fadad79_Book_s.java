 package de.codecentric.psd.worblehat.domain;
 
 import java.io.Serializable;
 import java.util.Date;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.NamedQueries;
 import javax.persistence.NamedQuery;
 import javax.persistence.OneToOne;
 import javax.persistence.Transient;
 
 /**
  * Entity implementation class for Entity: Book
  * 
  */
 @Entity
 @NamedQueries({
 		@NamedQuery(name = "findBorrowableBookByISBN", query = "from Book where isbn=:isbn and currentBorrowing is null"),
 		@NamedQuery(name = "findAllBorrowedBooksByEmail", query = "select book from Book as book where book.currentBorrowing.borrowerEmailAddress = :email"),
 		@NamedQuery(name = "findAllBooks", query = "from Book order by title") })
 public class Book implements Serializable {
 
 	private static final long serialVersionUID = 1L;
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	private long id;
 
 	private String title;
 	private String author;
 	private String edition;
 	private String isbn;
 	private String description;
 	@Transient
 	private String shortDescription;
 
 	private int year;
 
 	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
 	private Borrowing currentBorrowing;
 
 	/**
 	 * Empty constructor needed by Hibernate.
 	 */
 	private Book() {
 		super();
 	}
 
 	private String removeNondigits(String s) {
 		if (s == null) {
 			return s;
 		} else {
 
 			return s.replaceAll("-", "").replaceAll("\\s", "");
 		}
 	}
 
 	private String removeWhitespaceCharacters(String s) {
 
 		if (s != null) {
 			return s.replaceAll("\\n", " ").replaceAll("\\s.\\s", " ").trim();
 		} else {
 			return s;
 		}
 
 	}
 
 	/**
 	 * Creates a new book instance.
 	 * 
 	 * @param title
 	 *            the title
 	 * @param author
 	 *            the author
 	 * @param edition
 	 *            the edition
 	 * @param isbn
 	 *            the isbn
 	 * @param year
 	 *            the year
 	 * @param description
 	 *            the Book Description
 	 */
 	public Book(String title, String author, String edition, String isbn,
 			int year, String description) {
 		super();
 		this.title = removeWhitespaceCharacters(title);
 		this.author = removeWhitespaceCharacters(author);
 		this.edition = removeWhitespaceCharacters(edition);
 		this.isbn = removeNondigits(isbn);
 		this.year = year;
 		this.description = removeWhitespaceCharacters(description);
 		this.setShortDescription(this.description);
 	}
 
 	/**
 	 * Cut to long descriptions and appen 3 dots
 	 * 
 	 * @param description2cut
 	 */
 	private void setShortDescription(String description2cut) {
 		if (description2cut.length() > 100) {
 			this.shortDescription = description2cut.substring(0, 100) + "...";
 		} else {
 			this.shortDescription = description2cut;
 		}
 
 	}
 
 	public long getId() {
 		return id;
 	}
 
 	public String getTitle() {
 		return title;
 	}
 
 	public String getAuthor() {
 		return author;
 	}
 
 	public String getEdition() {
 		return edition;
 	}
 
 	public String getIsbn() {
 		return isbn;
 	}
 
 	public int getYear() {
 		return year;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public Borrowing getCurrentBorrowing() {
 		return currentBorrowing;
 	}
 
 	public String getShortDescription() {
 		return shortDescription;
 	}
 
 	/**
 	 * Borrow this book.
 	 * 
 	 * @param borrowerEmailAddress
 	 *            the user that borrows the book
 	 * @throws BookAlreadyBorrowedException
 	 *             if this current book is already borrowed
 	 */
 	public void borrow(String borrowerEmailAddress)
 			throws BookAlreadyBorrowedException {
 		if (currentBorrowing != null) {
 			throw new BookAlreadyBorrowedException("book is already borrowed");
 		} else {
 			currentBorrowing = new Borrowing(borrowerEmailAddress, new Date());
 		}
 	}
 
 	/**
 	 * Return the book.
 	 */
 	public void returnBook() {
 		this.currentBorrowing = null;
 	}
 
 }
