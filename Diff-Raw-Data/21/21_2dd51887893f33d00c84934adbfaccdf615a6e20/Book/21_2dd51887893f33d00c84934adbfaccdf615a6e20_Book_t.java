 /* CPSC 304 - Library Checkout System
  * © Mar. 2013 Kevin Petersen. All rights reserved.
  */
 
 package com.book;
 
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.Main;
 import com.book.records.HoldRequest;
 
 /**
  * Representation of a book as described by Book in tables.sql.
  * 
  * @author Kevin Petersen
  */
 public class Book {
 	private static Connection con = Main.con;  
 	
 	private int callNumber;
 	private float isbn;
 	private String title;
 	private String mainAuthor;
 	private String publisher;
 	private int year;
 	 
 	/**
 	 * Constructor used only by this class to enforce an instance being a valid
 	 * entry in the Book table
 	 * 
 	 * @param callNumber
 	 *            Primary key id number for this book
 	 * @param isbn
 	 *            This book's ISBN number
 	 * @param title
 	 *            Book's full title
 	 * @param mainAuthor
 	 *            Book's main author
 	 * @param publisher
 	 *            Book's publisher
 	 * @param year
 	 *            Book's publishing year
 	 */
 	private Book(int callNumber, float isbn, String title, String mainAuthor,
 			String publisher, int year) {
 		this.callNumber = callNumber;
 		this.isbn = isbn;
 		this.title = title;
 		this.mainAuthor = mainAuthor;
 		this.publisher = publisher;
 		this.year = year;
 	 }
 	
 	/**
 	 * Add a book to the Book table.
 	 * 
 	 * @param isbn
 	 *            This book's ISBN number
 	 * @param title
 	 *            Book's full title
 	 * @param mainAuthor
 	 *            Book's main author
 	 * @param publisher
 	 *            Book's publisher
 	 * @param year
 	 *            Book's publishing year
 	 * @return Object representing the newly created entry
 	 * @throws SQLException
 	 *             if a database access error occurs; this method is called on a
 	 *             closed PreparedStatement or the SQL statement returns a
 	 *             ResultSet object
 	 */
	public static Book add(float isbn, String title, String mainAuthor,
 			String publisher, int year) throws SQLException {
 		int callNumber = generateKey();
 		
 		try {
 			PreparedStatement ps = con.prepareStatement("INSERT INTO Book VALUES (?,?,?,?,?,?)");
 			
 			ps.setInt(1, callNumber);
 			ps.setFloat(2, isbn);
 			ps.setString(3, title);
 			ps.setString(4, mainAuthor);
 			ps.setString(5, publisher);
 			ps.setInt(6, year);
 			
 			// All inputs are OK
 			ps.executeUpdate();
 			con.commit();
 			ps.close();
 			
 			return new Book(callNumber, isbn, title, mainAuthor, publisher, year);
 		} catch (SQLException sql) {
 			System.out.println("Message: " + sql.getMessage());
 			try {
 				// Undo
 				con.rollback();
 			} catch (SQLException sql2) {
 				System.out.println("Message: " + sql2.getMessage());
 				System.exit(-1);
 			}
 			throw sql;
 		}
 	}
 	
 	/**
 	 * Generates a new key for an entry in the Borrower table
 	 * 
 	 * @return An unused unique key for this table
 	 * @throws SQLException
 	 *             if a database access error occurs; this method is called on a
 	 *             closed PreparedStatement or the SQL statement does not return
 	 *             a ResultSet object
 	 */
 	private static int generateKey() throws SQLException {
 		try {
 			PreparedStatement ps = con.prepareStatement("SELECT MAX(callNumber) as maxCallNumber FROM Book");
 			ResultSet r = ps.executeQuery();
 			
 			return (r.next() ? r.getInt("maxCallNumber") + 1 : 1);
 		} catch (SQLException sql) {
 			System.out.println("Message: " + sql.getMessage());
 			throw sql;
 		}
 	}
 
 	/**
 	 * Looks up the entry for the given key in the Book table and returns
 	 * the corresponding object.
 	 * 
 	 * @param callNumber
 	 *            The primary key used to look up the entry
 	 * @return A Book object representing the entry with the given key
 	 * @throws SQLException
 	 *             if a database access error occurs; this method is called on a
 	 *             closed PreparedStatement or the SQL statement does not return
 	 *             a ResultSet object
 	 */
 	public static Book get(int callNumber) throws SQLException {
 		try {
 			PreparedStatement ps = con.prepareStatement("SELECT * FROM Book WHERE callNumber=?");
 			ps.setInt(1, callNumber);
 			ps.setMaxRows(1);
 			ResultSet r = ps.executeQuery();
 			
 			if(r.next()) {
 				return parseLine(r);
 			} else {
 				throw new SQLException("No such Book.");
 			}
 		} catch (SQLException sql) {
 			System.out.println("Message: " + sql.getMessage());
 			throw sql;
 		}
 	}
 
 	/**
 	 * Looks up all the entries in the Book table and returns the corresponding
 	 * objects in a list.
 	 * 
 	 * @return A List of Book objects representing the entries of the Book table
 	 * @throws SQLException
 	 *             if a database access error occurs; this method is called on a
 	 *             closed PreparedStatement or the SQL statement does not return
 	 *             a ResultSet object
 	 */
 	public static List<Book> getAll() throws SQLException {
 		try {
 			PreparedStatement ps = con.prepareStatement("SELECT * FROM Book");
 			ResultSet r = ps.executeQuery();
 			List<Book> allBooks = new ArrayList<Book>();
 			
 			while(r.next()) {
 				allBooks.add(parseLine(r));
 			}
 			
 			return allBooks;
 		} catch (SQLException sql) {
 			System.out.println("Message: " + sql.getMessage());
 			throw sql;
 		}
 	}
 	
 	/**
 	 * Reads the data from the current row in the result set and generates the
 	 * corresponding Book object
 	 * 
 	 * @param r
 	 *            Result Set of a query
 	 * @return The book represented by the current row of data
 	 * @throws SQLException
 	 *             if the columnLabel is not valid; if a database access error
 	 *             occurs or this method is called on a closed result set
 	 */
 	private static Book parseLine(ResultSet r) throws SQLException {
 		int callNumber = r.getInt("callNumber");
 		float isbn = r.getFloat("isbn");
 		String title = r.getString("title");
 		String mainAuthor = r.getString("mainAuthor");
 		
 		String publisher = r.getString("publisher");
 		publisher = (r.wasNull() ? null : publisher);
 		
 		int year = r.getInt("year");
 		
 		return new Book(callNumber, isbn, title, mainAuthor, publisher, year);
 	}
 
 	/**
 	 * Deletes a book from the Book table.
 	 * 
 	 * @throws SQLException
 	 *             if a database access error occurs; this method is called on a
 	 *             closed PreparedStatement or the SQL statement returns a
 	 *             ResultSet object
 	 */
 	public void delete() throws SQLException {
 		try {
 			PreparedStatement ps = con.prepareStatement("DELETE FROM Book WHERE callNumber=?");
 			ps.setInt(1, this.callNumber);
 			
 			ps.executeUpdate();
 			con.commit();
 			ps.close();
 		} catch (SQLException sql) {
 			System.out.println("Message: " + sql.getMessage());
 			try {
 				// Undo
 				con.rollback();
 			} catch (SQLException sql2) {
 				System.out.println("Message: " + sql2.getMessage());
 				System.exit(-1);
 			}
 			throw sql;
 		}
 	}
 	
 	/**
 	 * Searches the BookCopy table for a copy of this book that's "in"
 	 * 
 	 * @return An available BookCopy object
 	 * @throws SQLException
 	 *             if a database access error occurs; this method is called on a
 	 *             closed PreparedStatement or the SQL statement does not return
 	 *             a ResultSet object
 	 */
 	public BookCopy findAvailableCopy() throws SQLException {
 		int copyNo;
 		
 		try {
 			PreparedStatement ps = con.prepareStatement("SELECT copyNo FROM BookCopy WHERE callNumber=? AND status='in'");
 			ps.setInt(1, this.callNumber);
 			ResultSet r = ps.executeQuery();
 			
 			if(r.next()) {
 				copyNo = r.getInt("copyNo");
 			} else {
 				throw new SQLException("No available copies.");
 			} 
 		} catch (SQLException sql) {
 			System.out.println("Message: " + sql.getMessage());
 			throw sql;
 		}
 
 		return BookCopy.get(this, copyNo);
 	}
 	
 	public boolean hasHold() throws SQLException {
 		PreparedStatement ps = con.prepareStatement("SELECT * FROM HoldRequest WHERE callNumber=?");
 		ps.setInt(1, this.callNumber);
 		ps.setMaxRows(1);
 		ResultSet r = ps.executeQuery();
 		
 		return r.next();
 	}
 	
 	public HoldRequest getHold() throws SQLException {
 		Date minDate;
 		int hid;
 		
 		try {
 			PreparedStatement ps = con.prepareStatement("SELECT MIN(issuedDate) as minDate FROM HoldRequest WHERE callNumber=?");
 			ps.setInt(1, this.callNumber);
 			ResultSet r = ps.executeQuery();
 			
 			if(r.next()) {
 				minDate = r.getDate("minDate");
 			} else {
 				throw new SQLException("No holds.");
 			}
 			
 			ps = con.prepareStatement("SELECT hid FROM HoldRequest WHERE callNumber=? AND issuedDate=?");
 			ps.setInt(1, this.callNumber);
 			ps.setDate(2, minDate);
 			r = ps.executeQuery();
 			
 			if(r.next()) {
 				hid = r.getInt("hid");
 			} else {
 				throw new SQLException("No holds.");
 			}
 		} catch (SQLException sql) {
 			System.out.println("Message: " + sql.getMessage());
 			throw sql;
 		}
 		
 		return HoldRequest.get(hid);
 	}
 
 	/**
 	 * @return Primary key id number for this book
 	 */
 	public int getCallNumber() {
 		return this.callNumber;
 	}
 
 	/**
 	 * @return This book's ISBN number
 	 */
 	public float getIsbn() {
 		return this.isbn;
 	}
 
 	/**
 	 * Updates this object and the Book table
 	 * 
 	 * @param isbn
 	 *            This book's ISBN number
 	 * @throws SQLException
 	 *             if a database access error occurs; this method is called on a
 	 *             closed PreparedStatement or the SQL statement returns a
 	 *             ResultSet object
 	 */
 	public void setIsbn(float isbn) throws SQLException {
 		try {
 			PreparedStatement ps = con.prepareStatement("UPDATE Book SET isbn=? WHERE callNumber=?");
 			ps.setInt(2, this.callNumber);
 			
 			ps.setFloat(1, isbn);
 			
 			ps.executeUpdate();
 			con.commit();
 			ps.close();
 			
 			this.isbn = isbn;
 		} catch (SQLException sql) {
 			System.out.println("Message: " + sql.getMessage());
 			try {
 				// Undo
 				con.rollback();
 			} catch (SQLException sql2) {
 				System.out.println("Message: " + sql2.getMessage());
 				System.exit(-1);
 			}
 			throw sql;
 		}
 	}
 
 	/**
 	 * @return Book's full title
 	 */
 	public String getTitle() {
 		return this.title;
 	}
 
 	/**
 	 * Updates this object and the Book table
 	 * 
 	 * @param title
 	 *            Book's full title
 	 * @throws SQLException
 	 *             if a database access error occurs; this method is called on a
 	 *             closed PreparedStatement or the SQL statement returns a
 	 *             ResultSet object
 	 */
 	public void setTitle(String title) throws SQLException {
 		try {
 			PreparedStatement ps = con.prepareStatement("UPDATE Book SET title=? WHERE callNumber=?");
 			ps.setInt(2, this.callNumber);
 			
 			ps.setString(1, title);
 			
 			ps.executeUpdate();
 			con.commit();
 			ps.close();
 			
 			this.title = title;
 		} catch (SQLException sql) {
 			System.out.println("Message: " + sql.getMessage());
 			try {
 				// Undo
 				con.rollback();
 			} catch (SQLException sql2) {
 				System.out.println("Message: " + sql2.getMessage());
 				System.exit(-1);
 			}
 			throw sql;
 		}
 	}
 
 	/**
 	 * @return Book's main author
 	 */
 	public String getMainAuthor() {
 		return this.mainAuthor;
 	}
 
 	/**
 	 * Updates this object and the Book table
 	 * 
 	 * @param mainAuthor
 	 *            Book's main author
 	 * @throws SQLException
 	 *             if a database access error occurs; this method is called on a
 	 *             closed PreparedStatement or the SQL statement returns a
 	 *             ResultSet object
 	 */
 	public void setMainAuthor(String mainAuthor) throws SQLException {
 		try {
 			PreparedStatement ps = con.prepareStatement("UPDATE Book SET mainAuthor=? WHERE callNumber=?");
 			ps.setInt(2, this.callNumber);
 			
 			ps.setString(1, mainAuthor);
 			
 			ps.executeUpdate();
 			con.commit();
 			ps.close();
 			
 			this.mainAuthor = mainAuthor;
 		} catch (SQLException sql) {
 			System.out.println("Message: " + sql.getMessage());
 			try {
 				// Undo
 				con.rollback();
 			} catch (SQLException sql2) {
 				System.out.println("Message: " + sql2.getMessage());
 				System.exit(-1);
 			}
 			throw sql;
 		}
 	}
 
 	/**
 	 * @return Book's publisher
 	 */
 	public String getPublisher() {
 		return this.publisher;
 	}
 
 	/**
 	 * Updates this object and the Book table
 	 * 
 	 * @param publisher
 	 *            Book's publisher
 	 * @throws SQLException
 	 *             if a database access error occurs; this method is called on a
 	 *             closed PreparedStatement or the SQL statement returns a
 	 *             ResultSet object
 	 */
 	public void setPublisher(String publisher) throws SQLException {
 		try {
 			PreparedStatement ps = con.prepareStatement("UPDATE Book SET publisher=? WHERE callNumber=?");
 			ps.setInt(2, this.callNumber);
 			
 			ps.setString(1, publisher);
 			
 			ps.executeUpdate();
 			con.commit();
 			ps.close();
 			
 			this.publisher = publisher;
 		} catch (SQLException sql) {
 			System.out.println("Message: " + sql.getMessage());
 			try {
 				// Undo
 				con.rollback();
 			} catch (SQLException sql2) {
 				System.out.println("Message: " + sql2.getMessage());
 				System.exit(-1);
 			}
 			throw sql;
 		}
 	}
 
 	/**
 	 * @return Book's publishing year
 	 */
 	public int getYear() {
 		return this.year;
 	}
 
 	/**
 	 * Updates this object and the Book table
 	 * 
 	 * @param year
 	 *            Book's publishing year
 	 * @throws SQLException
 	 *             if a database access error occurs; this method is called on a
 	 *             closed PreparedStatement or the SQL statement returns a
 	 *             ResultSet object
 	 */
 	public void setYear(int year) throws SQLException {
 		try {
 			PreparedStatement ps = con.prepareStatement("UPDATE Book SET year=? WHERE callNumber=?");
 			ps.setInt(2, this.callNumber);
 			
 			ps.setInt(1, year);
 			
 			ps.executeUpdate();
 			con.commit();
 			ps.close();
 			
 			this.year = year;
 		} catch (SQLException sql) {
 			System.out.println("Message: " + sql.getMessage());
 			try {
 				// Undo
 				con.rollback();
 			} catch (SQLException sql2) {
 				System.out.println("Message: " + sql2.getMessage());
 				System.exit(-1);
 			}
 			throw sql;
 		}
 	}
 }
