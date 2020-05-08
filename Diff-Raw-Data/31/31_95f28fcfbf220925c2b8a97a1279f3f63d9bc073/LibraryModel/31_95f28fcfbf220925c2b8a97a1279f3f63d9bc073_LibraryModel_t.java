 /*
  * LibraryModel.java
  * Author:
  * Created on:
  */
 
 import java.sql.Connection;
 import java.sql.Date;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.*;
 
 
 public class LibraryModel {
 
 	private String url = "jdbc:postgresql://db.ecs.vuw.ac.nz/broomeisab_jdbc";
 	private Connection conn;
 	private Statement stmt;
 	private ResultSet res;
 
 	// For use in creating dialogs and making them modal
 	private JFrame dialogParent;
 
	public LibraryModel(JFrame parent, String userid, String password) throws SQLException{
 		dialogParent = parent;
 
 		// connect to the database
 		this.url = String.format("jdbc:postgresql://db.ecs.vuw.ac.nz/%s_jdbc", userid);
		this.conn = DriverManager.getConnection(url, userid, password);
		System.out.println("Connected to database " + url);		
 	}
 
 	//==========================================================================================
 	// Formatting helper/wrapper methods
 	//==========================================================================================
 	/**
 	 * Formats the title of the query.
 	 * @param title
 	 * @return
 	 */
 	private String formattedTitle(String title){
 		int length = title.length();
 
 		title += "\r\n";
 		for (int i = 0; i < length; i++){
 			title += "=";
 		}
 		title += "\r\n\r\n";
 		return title;
 	}
 
 	private String newLine(){
 		return "\r\n";
 	}
 
 	private String noResults(){
 		return "(No results)" + newLine();
 	}
 
 	//===========================================================================================
 
 	public String bookLookup(int isbn) {
 
 		try{
 			String result = formattedTitle("Book Lookup");
 			stmt = conn.createStatement();
 			Book book = new Book();
 
 			// add info from Book
 			String sql = String.format("SELECT * FROM Book WHERE ISBN = %d;", isbn);
 			res = stmt.executeQuery(sql);
 			// if no results
 			if(!res.isBeforeFirst()){
 				return result + noResults();
 			}
			res.next();
 			book.ISBN = res.getInt("ISBN");
 			book.Title = res.getString("Title");
 			book.NumOfCop = res.getInt("NumOfCop");
 			book.NumLeft = res.getInt("NumLeft");
 
 			// add info about Authors
 			sql = String.format("SELECT AuthorSeqNo, Name, Surname FROM Author NATURAL JOIN (SELECT * FROM Book_Author WHERE ISBN = %d ORDER BY AuthorSeqNo) as BA;", isbn);
 			res = stmt.executeQuery(sql);
 			while(res.next()){
 				Author author = new Author();
 				author.Name = res.getString("Name").trim();
 				author.Surname = res.getString("Surname").trim();
 				book.authors.add(author);
 			}
 
 			// format string result
 			result += book.toFullString();
 
 			return result;
 
 		} catch(SQLException e){
 			JOptionPane.showMessageDialog(dialogParent, e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
 			return "";
 		} finally{
 			try {
 				res.close();
 				stmt.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public String showCatalogue() {
 		try{
 
 			stmt = conn.createStatement();
 			String result = formattedTitle("Show Catalogue");
 			Map<Integer, Book> books = new HashMap<Integer, Book>();
 
 			// get info from Book
 			String sql = String.format("SELECT * FROM Book;");
 			res = stmt.executeQuery(sql);
 			// if no results
 			if(!res.isBeforeFirst()){
 				return result + noResults();
 			}
 			while(res.next()){
 				Book book = new Book();
 				book.ISBN = res.getInt("ISBN");
 				book.Title = res.getString("Title");
 				book.NumOfCop = res.getInt("NumOfCop");
 				book.NumLeft = res.getInt("NumLeft");
 				books.put(book.ISBN, book);
 			}
 
 			// get info from authors, zip together with books
 			sql = "SELECT ISBN, AuthorSeqNo, Name, Surname FROM Author NATURAL JOIN Book_Author ORDER BY ISBN, AuthorSeqNo;";
 			res = stmt.executeQuery(sql);
 			while(res.next()){
 				Author author = new Author();
 				author.Name = res.getString("Name").trim();
 				author.Surname = res.getString("Surname").trim();
 				int ISBN = res.getInt("ISBN");
 				books.get(ISBN).authors.add(author);
 			}
 
 			// print each book to result
 			List<Integer> keys = new ArrayList<Integer>(books.keySet());
 			Collections.sort(keys);
 			for(int isbn: keys){
 				result += books.get(isbn).toFullString() + newLine();
 			}
 
 			// print total number
 			result += String.format("(%d results)", keys.size()) + newLine();
 
 			return result;
 
 		} catch(SQLException e){
 			JOptionPane.showMessageDialog(dialogParent, e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
 			return "";
 		} finally{
 			try {
 				res.close();
 				stmt.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public String showLoanedBooks() {
 		try {
 
 			stmt = conn.createStatement();
 			String result = formattedTitle("Show Loaned Books");
 			Map<Integer, Book> books = new HashMap<Integer, Book>();
 
 			// get info from Books
 			String sql = String.format("SELECT * FROM Book WHERE ISBN IN (SELECT ISBN from Cust_Book);");
 			res = stmt.executeQuery(sql);
 			// if no results
 			if(!res.isBeforeFirst()){
 				return result + noResults() + newLine();
 			}
 			while(res.next()){
 				Book book = new Book();
 				book.ISBN = res.getInt("ISBN");
 				book.Title = res.getString("Title");
 				book.NumOfCop = res.getInt("NumOfCop");
 				book.NumLeft = res.getInt("NumLeft");
 				books.put(book.ISBN, book);
 			}
 
 			// get info from authors, zip together with books
 			sql = "SELECT ISBN, AuthorSeqNo, Name, Surname FROM Author NATURAL JOIN Book_Author WHERE ISBN IN (SELECT ISBN from Cust_Book) ORDER BY ISBN, AuthorSeqNo;";
 			res = stmt.executeQuery(sql);
 			while(res.next()){
 				Author author = new Author();
 				author.Name = res.getString("Name").trim();
 				author.Surname = res.getString("Surname").trim();
 				int ISBN = res.getInt("ISBN");
 				books.get(ISBN).authors.add(author);
 			}
 
 			// get and add info about customers who are borrowing each book
 			sql = "SELECT * FROM Customer NATURAL JOIN Cust_Book;";
 			res = stmt.executeQuery(sql);
 			while(res.next()){
 				Customer c = new Customer();
 				c.customerID = res.getInt("CustomerID");
 				c.f_name = res.getString("F_Name").trim();
 				c.l_name = res.getString("L_Name").trim();
 				c.city = res.getString("City").trim();
 
 				int ISBN = res.getInt("ISBN");
 				books.get(ISBN).borrowedBy.add(c);
 			}
 
 			// print each book to result
 			List<Integer> keys = new ArrayList<Integer>(books.keySet());
 			Collections.sort(keys);
 			for(int isbn: keys){
 				result += books.get(isbn).toFullString() + newLine();
 			}
 
 			// print total number
 			result += String.format("(%d results)", keys.size()) + newLine();
 
 			return result;
 
 		} catch(SQLException e){
 			JOptionPane.showMessageDialog(dialogParent, e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
 			return "";
 		} finally{
 			try {
 				res.close();
 				stmt.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public String showAuthor(int authorID) {
 		try{
 
 			stmt = conn.createStatement();
 			String result = formattedTitle("Show Author");
 			Author author = new Author();
 
 			// get the Author
 			String sql = String.format("SELECT * FROM Author WHERE AuthorId = %d;", authorID);
 			res = stmt.executeQuery(sql);
 			// if no results
 			if(!res.isBeforeFirst()){
 				return result + noResults();
 			}
			res.next();
 			author.AuthorId = res.getInt("AuthorId");
 			author.Name = res.getString("Name").trim();
 			author.Surname = res.getString("Surname").trim();
 
 			// get the books they've authored
 			sql = String.format("SELECT ISBN, Title FROM Book_Author NATURAL JOIN Book WHERE AuthorId = %d;", authorID);
 			res = stmt.executeQuery(sql);
 			while(res.next()){
 				Book b = new Book();
 				b.ISBN = res.getInt("ISBN");
 				b.Title = res.getString("Title");
 				author.booksAuthored.add(b);
 			}
 
 			return result + author.toFullString() + newLine();
 
 		} catch(SQLException e){
 			JOptionPane.showMessageDialog(dialogParent, e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
 			return "";
 		} finally{
 			try {
 				res.close();
 				stmt.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public String showAllAuthors() {
 		try{
 
 			stmt = conn.createStatement();
 			String result = formattedTitle("Show All Authors");
 			List<Author> authors = new ArrayList<Author>();
 
 			// get the authors basic details
 			String sql = String.format("SELECT * FROM Author ORDER BY AuthorId;");
 			res = stmt.executeQuery(sql);
 			// if no results
 			if(!res.isBeforeFirst()){
 				return result + noResults();
 			}
 			while(res.next()){
 				Author author = new Author();
 				author.AuthorId = res.getInt("AuthorId");
 				author.Name = res.getString("Name").trim();
 				author.Surname = res.getString("Surname").trim();
 				authors.add(author);
 			}
 
 			for(Author a: authors){
 				result += a.toShortString();
 			}
 
 			// print total number
 			result += newLine() + String.format("(%d results)", authors.size()) + newLine();
 
 			return result;
 
 		} catch(SQLException e){
 			JOptionPane.showMessageDialog(dialogParent, e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
 			return "";
 		} finally{
 			try {
 				res.close();
 				stmt.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public String showCustomer(int customerID) {
 		try{
 
 			stmt = conn.createStatement();
 			String result = formattedTitle("Show Customer");
 			Customer customer = new Customer();
 
 			// get the customer basic info
 			String sql = String.format("SELECT * FROM Customer WHERE CustomerID = %d;", customerID);
 			res = stmt.executeQuery(sql);
 			// if no results
 			if(!res.isBeforeFirst()){
 				return result + noResults();
 			}
 			while(res.next()){
 				customer.customerID = res.getInt("CustomerID");
 				customer.f_name = res.getString("F_Name").trim();
 				customer.l_name = res.getString("L_Name").trim();
 				customer.city = res.getString("City");
 				if(customer.city != null){
 					customer.city = customer.city.trim();
 				}
 			}
 
 			// get the books they have borrowed
 			sql = String.format("SELECT ISBN, Title FROM Book WHERE ISBN IN (SELECT ISBN from Cust_Book WHERE CustomerId = %d);", customerID);
 			res = stmt.executeQuery(sql);
 			while(res.next()){
 				Book book = new Book();
 				book.ISBN = res.getInt("ISBN");
 				book.Title = res.getString("Title");
 				customer.borrowing.add(book);
 			}
 
 			return result + customer.toFullString() + newLine();
 
 		} catch(SQLException e){
 			JOptionPane.showMessageDialog(dialogParent, e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
 			return "";
 		} finally{
 			try {
 				res.close();
 				stmt.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public String showAllCustomers() {
 		try{
 
 			stmt = conn.createStatement();
 			String result = formattedTitle("Show All Customers");
 			List<Customer> customers = new ArrayList<Customer>();
 
 			// get the customers' basic details
 			String sql = String.format("SELECT * FROM Customer ORDER BY CustomerId;");
 			res = stmt.executeQuery(sql);
 			// if no results
 			if(!res.isBeforeFirst()){
 				return result + noResults();
 			}
 			while(res.next()){
 				Customer customer = new Customer();
 				customer.customerID = res.getInt("CustomerId");
 				customer.f_name = res.getString("F_Name").trim();
 				customer.l_name = res.getString("L_Name").trim();
 				customer.city = res.getString("City");
 				if(customer.city != null){
 					customer.city = customer.city.trim();
 				}
 				customers.add(customer);
 			}
 
 			for(Customer c: customers){
 				result += c.toShortString();
 			}
 
 			// print total number
 			result += newLine() + String.format("(%d results)", customers.size()) + newLine();
 
 			return result;
 
 		} catch(SQLException e){
 			JOptionPane.showMessageDialog(dialogParent, e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
 			return "";
 		} finally{
 			try {
 				res.close();
 				stmt.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public String borrowBook(int isbn, int customerID, int day, int month, int year) {
 		try{
 
 			String result = formattedTitle("Borrow Book");
 
 			// begin transaction
 			conn.setAutoCommit(false);
 			stmt = conn.createStatement();
 
 			// check the customer exists and lock
 			String sql = String.format("SELECT * FROM Customer WHERE CustomerID = %d FOR UPDATE;", customerID);
 			res = stmt.executeQuery(sql);
 			if(!res.isBeforeFirst()){
 				conn.rollback();
 				return result + String.format("\tCustomer %d does not exist.", customerID) + newLine();
 			}
 			res.next();
 			String custName = String.format("%s %s",res.getString("F_Name").trim(), res.getString("L_Name").trim());
 
 			// select the book and lock
 			sql = String.format("SELECT * from Book where ISBN = %d FOR UPDATE;", isbn);
 			res = stmt.executeQuery(sql);
 			if(!res.isBeforeFirst()){
 				conn.rollback();
 				return result + String.format("\tBook %d does not exist.", isbn) + newLine();
 			}
 			res.next();
 			String title = res.getString("Title").trim();
 			
 			// check that the book is available
 			if(res.getInt("numLeft") == 0){
 				conn.rollback();
 				return result + String.format("\tThere are no copies of (%d) %s available.", isbn, title) + newLine();
 			}
 
 			// check that the customer isn't already borrowing it
 			sql = String.format("SELECT * from Cust_Book WHERE CustomerId = %d;", customerID);
 			res = stmt.executeQuery(sql);
 			if(res.isBeforeFirst()){
 				conn.rollback();
 				return result + String.format("\tCustomer (%d) %s already has book (%d) %s on loan.", customerID, custName, isbn, title) + newLine();
 			}
 
 			// insert an entry into Cust_Book
 			String date = String.format("%04d-%02d-%02d", year, month, day);
 			sql = String.format("INSERT INTO Cust_Book (customerId, DueDate, ISBN) VALUES (%d, date '%s', %d);", customerID, date, isbn);
 			int updated = stmt.executeUpdate(sql);
 			if(updated != 1){
 				conn.rollback();
 				return result + "\tSomething weird happened. The book could not be borrowed";
 			}
 
 			// pause
 			JOptionPane.showMessageDialog(dialogParent, "Paused. Press ok to continue.", "Paused", JOptionPane.OK_OPTION);
 
 			// edit the numLeft in Book
 			sql = String.format("UPDATE Book SET numLeft = numLeft - 1 WHERE ISBN = %d", isbn);
 			updated = stmt.executeUpdate(sql);
 			if(updated != 1){
 				conn.rollback();
 				return result + "\tSomething weird happened, and the book could not be borrowed" + newLine();
 			}
 
 			// commit
 			conn.commit();
 
 			return result + String.format("\t(%d) %s successfully borrowed (%d) %s.  Due back on %s.", customerID, custName, isbn, title, date) + newLine();
 
 		} catch(SQLException e){
 			JOptionPane.showMessageDialog(dialogParent, e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
 			return "";
 		} finally{
 			try {
 				res.close();
 				stmt.close();
 			} catch (SQLException e) {
 				e.printStackTrace();  // this shouldn't happen
 			}
 		}
 	}
 
 	public String returnBook(int isbn, int customerID) {
 		try{
 
 			String result = formattedTitle("Return Book");
 
 			// begin transaction
 			conn.setAutoCommit(false);
 			stmt = conn.createStatement();
 
 			// check the customer exists and lock
 			String sql = String.format("SELECT * FROM Customer WHERE CustomerID = %d FOR UPDATE;", customerID);
 			res = stmt.executeQuery(sql);
 			if(!res.isBeforeFirst()){
 				conn.rollback();
 				return result + String.format("\tCustomer %d does not exist.", customerID) + newLine();
 			}
 			res.next();
 			String custName = String.format("%s %s",res.getString("F_Name").trim(), res.getString("L_Name").trim());
 
 			// check book exists and lock
 			sql = String.format("SELECT * from Book where ISBN = %d FOR UPDATE;", isbn);
 			res = stmt.executeQuery(sql);
 			if(!res.isBeforeFirst()){
 				conn.rollback();
 				return result + String.format("\tBook %d does not exist.", isbn) + newLine();
 			}
 			res.next();
 			String title = res.getString("Title").trim();
 			
 			// check that the book is on loan
 			boolean someOnLoan = res.getInt("numLeft") < res.getInt("NumOfCop");
 
 			// check that the customer is currently borrowing it and lock
 			sql = String.format("SELECT * from Cust_Book WHERE CustomerId = %d FOR UPDATE;", customerID);
 			res = stmt.executeQuery(sql);
 			if(!res.isBeforeFirst()){
 				conn.rollback();
 				return result + String.format("\tCustomer %d is not currently borrowing that book.", customerID) + newLine();
 			}
 			else if(!someOnLoan){
 				throw new SQLException("The database is in an inconsistent state. Customer " + customerID + " is borrowing the book "+ isbn + ", so there should be some copies out in the Book table.");
 			}
 
 			// delete the entry from Cust_Book
 			sql = String.format("DELETE FROM Cust_Book WHERE CustomerId = %d;", customerID);
 			int updated = stmt.executeUpdate(sql);
 			if(updated != 1){
 				conn.rollback();
 				return result + "\tSomething weird happened. The book could not be returned.";
 			}
 
 			// pause
 			JOptionPane.showMessageDialog(dialogParent, "Paused. Press ok to continue.", "Paused", JOptionPane.OK_OPTION);
 
 			// edit the numLeft in Book
 			sql = String.format("UPDATE Book SET numLeft = numLeft + 1 WHERE ISBN = %d", isbn);
 			updated = stmt.executeUpdate(sql);
 			if(updated != 1){
 				conn.rollback();
 				return result + "\tSomething weird happened, and the book could not be returned." + newLine();
 			}
 
 			// commit
 			conn.commit();
 
 			return result + String.format("\t(%d) %s successfully returned (%d) %s.", customerID, custName, isbn, title) + newLine();
 
 		} catch(SQLException e){
 			JOptionPane.showMessageDialog(dialogParent, e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
 			return "";
 		} finally{
 			try {
 				res.close();
 				stmt.close();
 			} catch (SQLException e) {
 				e.printStackTrace();  // this shouldn't happen
 			}
 		}
 	}
 
 	public String deleteCus(int customerID){
 		try{
 
 			String result = formattedTitle("Delete Customer");
 
 			// begin transaction
 			conn.setAutoCommit(false);
 			stmt = conn.createStatement();
 
 			// Step 0 - check allowed
 			//-----------------------
 
 			// check the customer exists and lock
 			String sql = String.format("SELECT * FROM Customer WHERE CustomerID = %d FOR UPDATE;", customerID);
 			res = stmt.executeQuery(sql);
 			if(!res.isBeforeFirst()){
 				conn.rollback();
 				return result + String.format("\tCustomer %d does not exist.", customerID) + newLine();
 			}
 			res.next();
 			String custName = String.format("%s %s",res.getString("F_Name").trim(), res.getString("L_Name").trim());
 
 			// make sure the customer isn't borrowing any books
 			sql = String.format("SELECT * from Cust_Book WHERE CustomerId = %d;", customerID);
 			res = stmt.executeQuery(sql);
 			if(res.isBeforeFirst()){
 				conn.rollback();
 				return result + String.format("\tCustomer (%d) %s is currently borrowing some books. They must be returned first.", customerID, custName) + newLine();
 			}
 
 			// Step 1 - delete from Customer
 			//------------------------------
 
 			// delete the customer
 			sql = String.format("DELETE FROM Customer WHERE CustomerId = %d;", customerID);
 			int updated = stmt.executeUpdate(sql);
 			if(updated != 1){
 				conn.rollback();
 				return result + "\tSomething weird happened. The customer was not deleted.";
 			}
 
 			// commit
 			conn.commit();
 
 			return result + String.format("\tSuccessfully deleted customer (%d) %s.", customerID, custName) + newLine();
 
 		} catch(SQLException e){
 			JOptionPane.showMessageDialog(dialogParent, e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
 			return "";
 		} finally{
 			try {
 				res.close();
 				stmt.close();
 			} catch (SQLException e) {
 				e.printStackTrace();  // this shouldn't happen
 			}
 		}
 	}
 
 	/**
 	 * This method will delete a book as long as noone is currently borrowing it.
 	 *
 	 * @param isbn
 	 * @return
 	 */
 	public String deleteBook(int isbn){
 		try{
 
 			String result = formattedTitle("Delete Book");
 
 			// begin transaction
 			conn.setAutoCommit(false);
 			stmt = conn.createStatement();
 
 			// Step 0 - check allowed
 			//-----------------------
 
 			// check the book exists and lock
 			String sql = String.format("SELECT * FROM Book WHERE ISBN = %d FOR UPDATE;", isbn);
 			res = stmt.executeQuery(sql);
 			if(!res.isBeforeFirst()){
 				conn.rollback();
 				return result + String.format("\tBook %d does not exist.", isbn) + newLine();
 			}
 			res.next();
 			String title = res.getString("Title").trim();
 
 			// make sure noone is borrowing it
 			sql = String.format("SELECT * from Cust_Book WHERE isbn = %d;", isbn);
 			res = stmt.executeQuery(sql);
 			if(res.isBeforeFirst()){
 				conn.rollback();
 				return result + String.format("\tBook (%d) %s is currently on loan. All copies must be returned first.", isbn, title) + newLine();
 			}
 
 			// Step 1 - delete from Book_Author
 			//----------------------------------
 
 			sql = String.format("DELETE FROM Book_Author WHERE ISBN = %d;", isbn);
 			int updated = stmt.executeUpdate(sql);
 			if(updated == 0){
 				conn.rollback();
 				return result + "\tSomething weird happened. The book was not deleted.";
 			}
 
 			// Step 2 - delete from Book
 			//--------------------------
 
 			sql = String.format("DELETE FROM Book WHERE ISBN = %d;", isbn);
 			updated = stmt.executeUpdate(sql);
 			if(updated != 1){
 				conn.rollback();
 				return result + "\tSomething weird happened. The book was not deleted.";
 			}
 
 			// commit
 			conn.commit();
 
 			return result + String.format("\tSuccessfully deleted book (%d) %s.", isbn, title) + newLine();
 
 		} catch(SQLException e){
 			JOptionPane.showMessageDialog(dialogParent, e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
 			return "";
 		} finally{
 			try {
 				res.close();
 				stmt.close();
 			} catch (SQLException e) {
 				e.printStackTrace();  // this shouldn't happen
 			}
 		}
 	}
 
 	/**
 	 * This method will delete the author if there are no books in the system the Author has written.
 	 * (i.e. no entries for that authorID in Book_Author table).
 	 * @param authorID
 	 * @return
 	 */
 	public String deleteAuthor(int authorID){
 		try{
 
 			String result = formattedTitle("Delete Author");
 
 			// begin transaction
 			conn.setAutoCommit(false);
 			stmt = conn.createStatement();
 
 			// Step 0 - check allowed
 			//-----------------------
 
 			// check the author exists and lock
 			String sql = String.format("SELECT * FROM Author WHERE AuthorId = %d FOR UPDATE;", authorID);
 			res = stmt.executeQuery(sql);
 			if(!res.isBeforeFirst()){
 				conn.rollback();
 				return result + String.format("\tAuthor %d does not exist.", authorID) + newLine();
 			}
 			res.next();
 			String authorName = res.getString("Name").trim() + " " + res.getString("Surname").trim();
 
 			// make sure no books in the system by that author
 			sql = String.format("SELECT * from Book_Author WHERE AuthorId = %d;", authorID);
 			res = stmt.executeQuery(sql);
 			if(res.isBeforeFirst()){
 				conn.rollback();
 				return result + String.format("\tThere are still books by (%d) %s in the system. All books must be deleted first.", authorID, authorName) + newLine();
 			}
 
 			// Step 1 - delete from Author
 			//--------------------------
 
 			sql = String.format("DELETE FROM Author WHERE AuthorId = %d;", authorID);
 			int updated = stmt.executeUpdate(sql);
 			if(updated != 1){
 				conn.rollback();
 				return result + "\tSomething weird happened. The author was not deleted.";
 			}
 
 			// commit
 			conn.commit();
 
 			return result + String.format("\tSuccessfully deleted author (%d) %s.", authorID, authorName) + newLine();
 
 		} catch(SQLException e){
 			JOptionPane.showMessageDialog(dialogParent, e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
 			return "";
 		} finally{
 			try {
 				res.close();
 				stmt.close();
 			} catch (SQLException e) {
 				e.printStackTrace();  // this shouldn't happen
 			}
 		}
 	}
 
 	public void closeDBConnection() {
 		try{
 			conn.close();
 			System.out.println("Connection closed");
 		} catch (SQLException e){
 			JOptionPane.showMessageDialog(dialogParent, e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
 		}
 	}
 }
