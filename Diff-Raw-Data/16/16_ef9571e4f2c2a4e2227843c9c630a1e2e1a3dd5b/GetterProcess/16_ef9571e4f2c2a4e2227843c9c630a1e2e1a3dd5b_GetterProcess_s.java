/*
 * @author Colin Larson
 */

 package dbprocess;
 
 import java.sql.Connection;
 
 import exceptions.CartException;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 
 import model.Book;
 import model.Cart;
 import model.User;
 
 /** Getter Processes for Database.
  * @author Colin
  */
 public class GetterProcess {
     /** db connection. */
     private Connection conn;
     /** process instance. */
     private static GetterProcess instance;
 
     /** Constructor.
      * @param conn database connection
      * @throws SQLException sql method fail
      */
     protected GetterProcess(
             final Connection conn) throws SQLException {
         this.conn = conn;
     }
 
     /** Singleton pattern DatabaseProcess init.
      * @param conn  the db connection
      * @return    single instance of DatabaseProcess
      */
     public static synchronized GetterProcess getInstance(
             final Connection conn) {
         if (instance == null) {
             try {
                 instance = new GetterProcess(conn);
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
         return instance;
     }
 
     /** Get the the user's data as html in a string.
      * @param username    the user whose assoc. data is to be found
      * @return    the data (as a string) if found, otherwise null
      * @throws SQLException sql method fail
      */
     protected final User getUserInfo(
             final String username) throws SQLException {
         Statement stmt = conn.createStatement();
         ResultSet rs;
         rs = stmt.executeQuery("SELECT * FROM tblUser WHERE UserName = "
                 + "\"" + username + "\"");
         Statement stmt2 = conn.createStatement();
         ResultSet rs2;
         rs2 = stmt2.executeQuery("SELECT * FROM "
                 + "tblAccountInfo WHERE UserName = "
                 + "\"" + username + "\"");
         if (rs.next()) {
             rs2.next();
             User u;
             u = new User(rs.getString("UserName"),
                     false, rs2.getString("email"));
             if (rs.getString("isAdmin").equals("Y")) { u.isAdmin = true; }
             else { u.isAdmin = false; }
             return u;
         } else {
             return null;
         }
     }
 
     /**
      * Admin only. Return password of a given user.
      * @param userName        User whose password is needed
      * @return                User password
      * @throws SQLException sql method fail
      */
     protected final String getUserPassWord(
             final String userName) throws SQLException {
         Statement stmt = conn.createStatement();
         ResultSet rs;
         rs = stmt.executeQuery("SELECT PassWord FROM "
                 + "tblUser WHERE UserName = \""
                 + userName + "\"");
         if (rs.next()) {
             return rs.getString("PassWord");
         }
         return null;
     }
 
     /** get an existing user's cart.
      * @param userName            the user whose cart is to be gotten
      * @return                    the user's cart
      * @throws SQLException        failed sql methods
      * @throws CartException    failed cart methods
      */
     protected final Cart getUserCart(
             final String userName) throws SQLException, CartException {
         Cart c = new Cart();
         DatabaseProcess db = DatabaseProcess.getInstance();
         Statement stmt = conn.createStatement();
         ResultSet rs;
         rs = stmt.executeQuery("SELECT CartNumber FROM "
                 + "tblShoppingCart WHERE UserName = \""
                 + userName + "\"");
         if (rs.next()) {
             ResultSet rs2;
             rs2 = stmt.executeQuery("SELECT BookISBN FROM "
                     + "tblCartContent WHERE CartNumber = "
                     + rs.getInt("CartNumber"));
             if (rs2.next()) {
                 Book b = db.getBookByIsbn(rs2.getLong("BookISBN"));
                 c.add(b);
                 while (rs2.next()) {
                     long isbn = rs2.getLong("BookISBN");
                     c.add((db.getBookByIsbn(isbn)));
                 }
             }
            }
         else {
             return null;
         }
         return c;
     }
 
     /** Find books by specified query, using specific option.
      * @param    option    Define parameters of search
      * @param    query    Define what to search for.
      * @return            A list of books that satisfy the search parameters.
      * @throws SQLException        failed sql methods
      */
     protected final ArrayList<Book> getBooksBy(
             final int option, final String query) throws SQLException {
         ResultSet rs = null;
         Statement stmt = conn.createStatement();
         if (option == DatabaseProcess.AUTHOR && !query.equals("")) {
             rs = stmt.executeQuery("SELECT * FROM "
                     + "tblBook WHERE Author LIKE \"%"
                     + query + "%\" ORDER BY Title;");
         }
         else if (option == DatabaseProcess.TITLE && !query.equals("")) {
             rs = stmt.executeQuery("SELECT * FROM "
                     + "tblBook WHERE Title LIKE \"%"
                     + query + "%\" ORDER BY Title;");
         }
         else if (option == DatabaseProcess.USERNAME && !query.equals("")) {
             rs = stmt.executeQuery("SELECT tblBook.* FROM "
                     + "tblBook, tblBookRental WHERE "
                     + "tblBook.ISBN=tblBookRental.BookISBN AND"
                     + " tblBookRental.UserName=\"" + query + "\";");
         }
         else if (option == DatabaseProcess.CATALOGUE && query.equals("")) {
             rs = stmt.executeQuery("SELECT * FROM tblBook ORDER BY Title;");
         }
         else {
             return null;
         }
         ArrayList<Book> bookList = new ArrayList<Book>();
         while (rs.next()) {
             bookList.add(new Book(rs.getString("Title"), rs.getString("Author"),
                     rs.getDouble("Price"), rs.getString("Url"),
                     rs.getInt("ISBN"), rs.getString("picUrl"),
                     rs.getString("Description")));
         }
         if (bookList.size() == 0) {
             return null;
         }
         return bookList;
     }
 
     /** Find a book by isbn (exact).
      * @param    isbn    isbn to search for
      * @return    the book with the given ISBN if found; otherwise null
      * @throws SQLException        failed sql methods
      */
     protected final Book getBookByIsbn(final long isbn) throws SQLException {
         if (isbn < 0) {
             return null;
         }
         Statement stmt = conn.createStatement();
         ResultSet rs;
         rs = stmt.executeQuery("SELECT * FROM "
                 + "tblBook WHERE ISBN=" + isbn + ";");
         if (rs.next()) {
             return new Book(rs.getString("Title"), rs.getString("Author"),
                     rs.getDouble("Price"), rs.getString("Url"),
                     rs.getInt("ISBN"), rs.getString("picURL"),
                     rs.getString("Description"));
         } else {    //no book found
             return null;
         }
     }
 
     /** Get the admin status of a user.
      * @param    username    the username of the user
      * @return true if user is admin; false otherwise
      * @throws SQLException sql method fail
      */
     protected final boolean getAdminStatus(
             final String username) throws SQLException {
         if (username == null) {
             return false;
         }
         Statement stmt = conn.createStatement();
         ResultSet rs;
         rs = stmt.executeQuery("SELECT IsAdmin FROM "
                 + "tblUser WHERE UserName=\"" + username + "\";");
         if (rs.next()) {
             return rs.getBoolean("IsAdmin");
         } else {
             return false;
         }
     }
 
     /** Get pertinent book info.
      * @param isbn    of the book to be gotten
      * @return    the info as a string
      * @throws SQLException sql method fail
      */
     protected final String getBookInfo(
             final long isbn) throws SQLException {
         String info;
         if (isbn < 0) {
             return null;
         }
         Statement stmt = conn.createStatement();
         ResultSet rs;
         rs = stmt.executeQuery("SELECT * FROM "
                 + "tblBook WHERE (ISBN=" + isbn + ") LIMIT 1;");
         if (rs.first()) {
             info = "<html>Title: " + rs.getString("title")
                     + "<br>Author: " + rs.getString("author")
                     + "<br>ISBN: " + rs.getString("isbn")
                     + "<br>Description: <br>"
                     + rs.getString("description");
         } else {
             return "No info found!";
         }
         return info;
     }
 }
