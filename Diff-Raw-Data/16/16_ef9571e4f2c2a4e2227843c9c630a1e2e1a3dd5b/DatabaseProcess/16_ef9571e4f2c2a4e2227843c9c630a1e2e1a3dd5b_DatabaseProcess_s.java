 package dbprocess;
 
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 import model.Book;
 import model.Cart;
 import model.User;
 
 import com.mysql.jdbc.Connection;
 
 import exceptions.CartException;
 import exceptions.NoUsernameOrPasswordException;
 import exceptions.NullUserException;
 import exceptions.UserAlreadyExistsException;
 
 /**
  * @author cml220
  */
 public class DatabaseProcess {
     /** AUTHOR flag used for getBooksBy. */
     public static final int AUTHOR = 0;
     /** TITLE flag used for getBooksBy. */
     public static final int TITLE = 1;
     /** USERNAME flag used for getBooksBy. */
     public static final int USERNAME = 2;
     /** CATALOGUE flag used for getBooksBy. */
     public static final int CATALOGUE = 3;
     /** name of the database. */
     private static String dbname = "cmpt371group_CTiger";
     /** the instance of DatabaseProcess. */
     private static DatabaseProcess instance;
     /** the db connection. */
     private Connection conn;
 
     /** constructor.
      * @throws SQLException        failed sql methods
      */
     public DatabaseProcess() throws SQLException {
         initDatabaseConnection();
     }
 
     /** Initialize a connection to the database.
      * @postcond    connection to the database initialized
      * @throws SQLException        failed sql methods
      */
     private void initDatabaseConnection() throws SQLException {
         try {
             Class.forName("com.mysql.jdbc.Driver").newInstance();
             String url;
             url = "jdbc:mysql://edjo.usask.ca/"
                     + dbname
                     + "?user=cmpt371gCT_user&password=TiggerTyger1";
             conn = (Connection) DriverManager.getConnection(url);
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     /** Singleton pattern DatabaseProcess init.
      * @return    single instance of DatabaseProcess
      */
     public static synchronized DatabaseProcess getInstance() {
         if (instance == null) {
             try {
                 instance = new DatabaseProcess();
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
         return instance;
     }
 
     /** Get the the user's data as html in a string.
      * @param user    the user whose assoc. data is to be found
      * @return    the data (as a string) if found, otherwise null
      * @throws SQLException        failed sql methods
      */
     public final User getUserInfo(final String user) throws SQLException {
         GetterProcess db = GetterProcess.getInstance(conn);
         return db.getUserInfo(user);
     }
 
     /** Admin only. Return password of a given user.
      * @param userName        User whose password is needed
      * @return                User password
      * @throws SQLException        failed sql methods
      */
     public final String getUserPassWord(
             final String userName) throws SQLException {
         GetterProcess db = GetterProcess.getInstance(conn);
         return db.getUserPassWord(userName);
     }
 
     /** get an existing user's cart.
      * @param userName            the user whose cart is to be gotten
      * @return                    the user's cart
      * @throws SQLException        failed sql methods
      * @throws CartException    failed cart methods
      */
     public final Cart getUserCart(
             final String userName) throws SQLException, CartException {
         GetterProcess db = GetterProcess.getInstance(conn);
         return db.getUserCart(userName);
     }
 
     /** Find books by specified query, using specific option.
      * @param    option    Define parameters of search
      * @param    query    Define what to search for.
      * @return            A list of books that satisfy the search parameters.
      * @throws SQLException        failed sql methods
      */
     public final ArrayList<Book> getBooksBy(
             final int option, final String query) throws SQLException {
         GetterProcess db = GetterProcess.getInstance(conn);
         return db.getBooksBy(option, query);
     }
 
     /** Find a book by isbn (exact).
      * @param    isbn    isbn to search for
      * @return    the book with the given ISBN if found; otherwise null
      * @throws SQLException        failed sql methods
      */
     public final Book getBookByIsbn(final long isbn) throws SQLException {
         GetterProcess db = GetterProcess.getInstance(conn);
         return db.getBookByIsbn(isbn);
     }
 
     /** Get the admin status of a user.
      * @param    username    the username of the user
      * @return true if user is admin; false otherwise
      * @throws SQLException        failed sql methods
      */
     public final boolean getAdminStatus(
             final String username) throws SQLException {
         GetterProcess db = GetterProcess.getInstance(conn);
         return db.getAdminStatus(username);
     }
 
     /** Get pertinent book info.
      * @param isbn    of the book to be gotten
      * @return    the info as a string
      * @throws SQLException        failed sql methods
      */
     public final String getBookInfo(final long isbn) throws SQLException {
         GetterProcess db = GetterProcess.getInstance(conn);
         return db.getBookInfo(isbn);
     }
 
     /** Add a book to a user's rentals.
      * @param isbn        isbn of book to be added
      * @param username    the user for it to be added to
      * @throws SQLException        failed sql methods
      */
     public final void addBookToUser(
             final long isbn, final String username) throws SQLException {
         InsertionProcess db = InsertionProcess.getInstance(conn);
         db.addBookToUser(isbn, username);
     }
 
     /** Add a book to the catalogue.
      * @param    b    book to be added to the catalogue
      * @throws SQLException        failed sql methods
      */
     public final void addBookToCatalogue(final Book b) throws SQLException {
         InsertionProcess db = InsertionProcess.getInstance(conn);
         db.addBookToCatalogue(b);
     }
 
     /** Remove a single book from the catalogue as well as all rentals.
      * @param isbn        isbn of the book to be removed
      * @throws SQLException        failed sql methods
      */
     public final void removeBookFromCatalogue(
             final long isbn) throws SQLException {
         RemovalProcess db = RemovalProcess.getInstance(conn);
         db.removeBookFromCatalogue(isbn);
     }
 
     /** Create a new user for the system.
      * @param userName    user to be added to the system
      * @param passWord    password to be given to user
      * @param email        email to be given to user
      * @postcond    user has been added to the system
      * @return      true if successful; exception thrown otherwise
      * @throws SQLException sql method fail
      * @throws NoUsernameOrPasswordException self-expl
      * @throws NullUserException self-expl
      * @throws UserAlreadyExistsException self-expl
      */
    public final boolean createUser(final String userName, final String email, final String passWord) 
                            throws SQLException, NoUsernameOrPasswordException,
                                    NullUserException, UserAlreadyExistsException {
         InsertionProcess db = InsertionProcess.getInstance(conn);
         return db.createUser(userName, email, passWord);
     }
 
     /** Save a cart and its contents in the appropriate tables.
      * @param c            the cart to be saved
      * @param username    the user for it to be saved to
      * @param shopdate    the date in which it was saved
      * @throws SQLException        failed sql methods
      */
     public final void saveShoppingCart(
             final Cart c, final String username,
             final String shopdate) throws SQLException {
         InsertionProcess db = InsertionProcess.getInstance(conn);
         db.saveShoppingCart(c, username, shopdate);
     }
 
     /** Edit an existing user's information incl. password, email, admin status.
      * @param user            user obj containing new info
      *                        incl. email, admin status
      * @param passWord        original password for verification
      * @param newPassWord     new password;
      *                        pass original password to keep this unchanged
      * @throws Exception      failed sql methods
      */
     public final void editUserInfo(
             final User user, final String passWord,
             final String newPassWord) throws Exception {
         ModificationProcess db = ModificationProcess.getInstance(conn);
         db.editUserInfo(user, passWord, newPassWord);
     }
 
     /** Remove a user from the db.
      * @param username    the user to be removed
      * @return true        if the user has been removed; false otherwise
      * @throws SQLException        failed sql methods
      */
     public final boolean removeUser(
             final String username) throws SQLException {
         RemovalProcess db = RemovalProcess.getInstance(conn);
         return db.removeUser(username);
     }
 
     /** Remove a book from a user's rentals.
      * @param username    the user
      * @param isbn        the book to be removed from the user
      * @return true       if the book has been removed from the user
      *                    ; false otherwise
      * @throws SQLException        failed sql methods
      */
     public final boolean removeBookFromUser(
             final String username, final long isbn) throws SQLException {
         RemovalProcess db = RemovalProcess.getInstance(conn);
         return db.removeBookFromUser(username, isbn);
     }
 
     /** remove an existing user's shopping cart.
      * @param userName        user whose cart is to be removed
      * @return                true if removed; false otherwise
      * @throws SQLException        failed sql methods
      */
     public final boolean removeShoppingCart(
             final String userName) throws SQLException {
         RemovalProcess db = RemovalProcess.getInstance(conn);
         return db.removeShoppingCart(userName);
     }
 
     /** Check if a user with the given login info is registered.
      * @param username  the login name to be searched for
      * @param password    the password to be searched for
      * @return    true if a user with that info is registered; false otherwise
      * @throws SQLException        failed sql methods
      */
     public final boolean checkUser(
             final String username, final String password) throws SQLException {
         VerificationProcess db = VerificationProcess.getInstance(conn);
         return db.checkUser(username, password);
     }
 
     /** is the provided username available?
      * @param username    the username to search for
      * @return    true if the username is available; false otherwise
      * @throws SQLException        failed sql methods
      */
     public final boolean isNameAvailable(
             final String username) throws SQLException {
         VerificationProcess db = VerificationProcess.getInstance(conn);
         return db.isNameAvailable(username);
     }
 
     /** Check if a user has a book in their rentals.
      * @param username    the user to check
      * @param isbn        the book to check for
      * @return    true     if the user has the book; false otherwise
      * @throws SQLException        failed sql methods
      */
     public final boolean userHasBook(
             final String username, final long isbn) throws SQLException {
         VerificationProcess db = VerificationProcess.getInstance(conn);
         return db.userHasBook(username, isbn);
     }
 }
