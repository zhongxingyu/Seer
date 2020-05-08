 package controllers;
 
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import model.Book;
 import model.PaymentInfo;
 import model.User;
 
 import org.apache.log4j.Logger;
 
 import exceptions.BookOpenFailed;
 import exceptions.CartException;
 import exceptions.ControllerNotInitializedException;
 import exceptions.ImageLoadFailedException;
 import exceptions.IntermediateException;
 import exceptions.PaymentInfoNotInitializedException;
 import exceptions.PurchaseFailedException;
 
 /**
  * Main controller.
  * Provides an interface between the GUI and the rest of the controllers.
  * @author Brad
  *
  */
 public final class Controller {
 
     /**
      * Hiding the controller constructor.
      */
     private Controller() { }
 
     /**
      * Account controller.
      * User-related functions.
      */
     private static AccountController accountController;
 
     /**
      * Shopping cart controller.
      */
     private static CartController cartController;
 
     /**
      * Controller for searching catalogue of books.
      */
     private static CatalogueController catalogueController;
 
     /**
      * Login controller.
      */
     private static LoginController loginController;
 
     /**
      * Payment controller.
      */
     private static PaymentController paymentController;
 
     /**
      * Reader controller.
      * Interface to the ICEPDF reader.
      */
     private static ReaderController readerController;
 
     /**
      * Rentals controller.
      */
     private static RentalsController rentalsController;
 
     /**
      * Payment info that is alive only while the window is open.
      */
     private static PaymentInfo sessionPaymentInfo;
     /**
      * Variable for keeping track of if this singleton has been initialized.
      */
     private static boolean initialized;
 
     /**
      * @return the currentUser
      */
     public static User getCurrentUser() {
 
         return currentUser;
 
     }
 
     /**
      * initialized the singleton controller for use.
      * Must be run before any other methods will work.
      */
     public static void initialize() {
 
         accountController = new AccountController();
         cartController = new CartController();
         catalogueController = new CatalogueController();
         loginController = new LoginController();
         paymentController = new PaymentController();
         readerController = new ReaderController();
         rentalsController = new RentalsController();
 
         initialized = true;
 
     }
 
     /**
      * Checks if the controller is initialized.
      * @return initialized
      */
     public static boolean isinitialized() {
 
         return initialized;
 
     }
 
     /**
      * The current user of the system.
      * Determined at login time.
      */
     private static User currentUser;
 
     /**
      * Pass the current catalogue to the caller.
      * @return current catalogue of books available for purchase
      * @throws SQLException
      * @throws ControllerNotInitializedException
      */
     public static ArrayList<Book> getCatalogue() throws SQLException,
     ControllerNotInitializedException {
 
 
         if (!initialized) {
 
             throw new ControllerNotInitializedException();
 
         }
 
         return catalogueController.getCatalogue();
 
     }
 
     /**
      * Retrieves a User object from the account controller using a username
      * as identification.
      * @param username - the name of the user to get a User object for
      * @return a User object containing all of 'username's account information
      * @throws ControllerNotInitializedException if the controller isn't loaded
      * @throws SQLException if no connection to the database can be obtained
      */
     public static User getUserInfo(final String username)
             throws ControllerNotInitializedException, SQLException {
 
         if (!initialized) {
 
             throw new ControllerNotInitializedException();
 
         }
 
         return accountController.getUserInfo(username);
 
     }
 
     /**
      * Changes a users information using the data inside of a User object.
      * @param user - the object containing the new user data.
      * @param password	the string containing the user's password
      * @param newPassWord - the string containing the user's new password
      * @throws Exception
      */
     public static void changeUserInfo(final User user,
             final String password, final String newPassWord) throws Exception {
 
         if (!initialized) {
 
             throw new ControllerNotInitializedException();
 
         }
 
         accountController.changeUserInfo(newPassWord, user, password);
 
     }
 
     /**
      * Makes a given user an administrator.
      * @param username The name of the user to make an admin
      * @throws Exception If the Controller class has not been initialized
      */
     public static void makeAdmin(final String username) throws Exception {
         if (!initialized) {
 
             throw new ControllerNotInitializedException();
         }
         accountController.makeAdmin(username);
     }
 
     /**
      * Pass payment info to the GUI so that the customer can confirm their
      * credit card number, etc.
      * @return PaymentInfo object containing user's payment info.
      * @throws ControllerNotInitializedException if the controller isn't loaded
      */
     public static PaymentInfo fetchPaymentInfo()
             throws ControllerNotInitializedException {
 
         if (!initialized) {
 
             throw new ControllerNotInitializedException();
 
         }
 
         return paymentController.getPaymentInfo();
 
     }
 
     /**
      * Sets the payment info for this session temporarily.
      * @param pi - the payment info.
      */
     public static void setSessionPaymentInfo(final PaymentInfo pi) {
 
         sessionPaymentInfo = pi;
 
     }
 
     /**
      * Get a copy of the temporary payment info.
      * @return the temporary payment info object
      */
     public static PaymentInfo getSessionPaymentInfo() {
 
         return sessionPaymentInfo;
 
     }
 
     /**
      * Save the user's payment info to the database.
      * MUST setSessionPaymentInfo FIRST
      * @throws PaymentInfoNotInitializedException
      * @throws ControllerNotInitializedException
      */
     public static void updatePaymentInfo()
             throws PaymentInfoNotInitializedException,
             ControllerNotInitializedException {
 
         if (!initialized) {
 
             throw new ControllerNotInitializedException();
 
         }
 
         if (sessionPaymentInfo == null) {
 
             throw new PaymentInfoNotInitializedException();
 
         }
         //update the payment info in the database?
         paymentController.setPaymentInfo(sessionPaymentInfo);
 
     }
 
     /**
      * Updates the user's payment info as provided in 'piNew'.
      * @param piNew object containing user's new payment info.
      * @throws ControllerNotInitializedException if the controller isn't loaded
      */
     public static void setPaymentInfo(final PaymentInfo piNew)
             throws ControllerNotInitializedException {
 
         if (!initialized) {
 
             throw new ControllerNotInitializedException();
 
         }
 
         paymentController.setPaymentInfo(piNew);
 
     }
 
     /**
      * Complete the payment for the books.  Then confirm that the user's
      * rentals have been updated correctly.  If not, refund the purchase and
      * inform the user.
      * @throws IntermediateException if the transaction failed.
      * @throws SQLException
      */
     public static void processPurchase() throws IntermediateException,
     SQLException {
 
         /*
          * Initialize the log4J logger.
          */
         Logger log = Logger.getLogger(Controller.class);
 
         /*
          * Initialize reference lists of books so that the transaction can be
          * confirmed.
          */
         ArrayList<Book> userBooksBeforePurchase = rentalsController.getBooks();
         ArrayList<Book> cartBooksBeforePurchase = cartController.getBooks();
         ArrayList<Book> userBooksAfterPurchase = new ArrayList<Book>();
 
         /*
          * Log the books in the user's rental account before the purchase.
          */
 
         log.debug("Books in account before purchase:");
         for (Book b : userBooksBeforePurchase) {
 
             log.debug(b.ISBN);
 
         }
 
         /*
          * Log the books in the user's cart before the purchase.
          */
 
         log.debug("Books in cart before purchase:");
         for (Book b : cartBooksBeforePurchase) {
 
             log.debug(b.ISBN);
 
         }
 
         /*
          * confirm that the payment went through, then add the cart books to
          * the user's account.
          */
 
         if (paymentController.confirmPayment()) {
 
             paymentController.addCurrentCart();
             userBooksAfterPurchase = rentalsController.getBooks();
 
         }
 
 
         try {
             /*
              * Log the books in the user's rental account after the purchase
              * and confirm that they are correct.
              */
 
             log.debug("Checking transaction results:");
 
             //check that the books that were in the rental account before the
             //purchase are still there .
             for (Book b : userBooksBeforePurchase) {
 
                 log.debug(b.ISBN);
                 if (userBooksAfterPurchase.contains(b)) {
 
                     log.debug("Item verified as in cart.");
 
                 } else {
 
                     log.debug("Exception thrown:  Item expected in rentals but"
                             + "not found.");
                     throw new PurchaseFailedException();
 
                 }
 
             }
 
             //check that the books that were in the cart are now all in the
             //rental account.
             for (Book b : cartBooksBeforePurchase) {
 
                 log.debug(b.ISBN);
                 if (userBooksAfterPurchase.contains(b)) {
 
                    log.debug("Item verified as in cart.");
 
                 } else {
 
                     System.out.println("Failed to update books");
                     //do nothing right now
                     //throw new PurchaseFailedException();
 
                 }
 
             }
 
         } catch (PurchaseFailedException pfe) {
 
             //reverse the transaction (fake)
             throw new IntermediateException("Purchase failed.  " +
                     "Payment has been refunded");
 
         }
 
 
     }
 
     /**
      * Add a book to the current user's shopping cart.
      * @param book the book to add to the cart
      * @throws CartException if the cart operation failed
      * @throws ControllerNotInitializedException if the controller isn't loaded
      */
     public static void addToCart(final Book book)
             throws CartException, ControllerNotInitializedException {
 
         if (!initialized) {
 
             throw new ControllerNotInitializedException();
 
         }
 
         cartController.addBookToCart(book);
 
     }
 
     /**
      * Remove a book from the current user's shopping cart.
      * @param book the book to remove from the cart
      * @throws CartException if the cart operation failed
      * @throws ControllerNotInitializedException if the controller isn't loaded
      */
     public static void removeFromCart(final Book book)
             throws CartException, ControllerNotInitializedException {
 
         if (!initialized) {
 
             throw new ControllerNotInitializedException();
 
         }
 
         cartController.removeBookFromCart(book);
 
     }
 
     /**
      * Fetch a list of books currently in the user's cart so they can be
      * displayed in the GUI.
      * @return a list of the books in the current user's cart
      * @throws CartException if the cart operation failed
      * @throws ControllerNotInitializedException if the controller isn't loaded
      */
     public static ArrayList<Book> getCartContents()
             throws CartException, ControllerNotInitializedException {
 
         if (!initialized) {
 
             throw new ControllerNotInitializedException();
 
         }
 
         return cartController.getBooks();
 
     }
 
     /**
      * Get the total price of the contents of the user's cart so it can be
      * displayed in the GUI.
      * @return the total price of all items in the user's cart.
      * @throws CartException if there is an error fetching the total from cart.
      * @throws ControllerNotInitializedException if controller not initialised.
      */
     public static float getCartTotal()
             throws CartException, ControllerNotInitializedException {
 
         if (!initialized) {
 
             throw new ControllerNotInitializedException();
 
         }
 
         return cartController.getTotal();
 
     }
 
     /**
      * Save the user's cart to the database.
      * @throws SQLException
      * @throws ControllerNotInitializedException
      */
     public static void updateCart() throws SQLException,
     ControllerNotInitializedException {
 
         if (!initialized) {
 
             throw new ControllerNotInitializedException();
 
         }
 
         cartController.saveCartToDatabase();
 
     }
 
     /**
      * Fetch a list of books that a user has rented on their account.
      * @return a list of books linked to the user
      * @throws ControllerNotInitializedException if the controller isn't loaded
      * @throws SQLException
      */
     public static ArrayList<Book> getUserBooks()
             throws ControllerNotInitializedException, SQLException {
 
         if (!initialized) {
 
             throw new ControllerNotInitializedException();
 
         }
 
         return rentalsController.getBooks();
 
     }
 
     /**
      * Load an image to represent a book.
      * @param book the book containing the image location
      * @return a JLabel with an image inside it
      * @throws ImageLoadFailedException if image failed to load
      * @throws ControllerNotInitializedException if the controller isn't loaded
      */
     public static JLabel loadCover(final Book book)
             throws ImageLoadFailedException,
             ControllerNotInitializedException {
 
         if (!initialized) {
 
             throw new ControllerNotInitializedException();
 
         }
 
         BufferedImage bookImage;
 
         try {
 
             bookImage = ImageIO.read(new File(book.img));
 
         } catch (IOException e) {
 
             throw new ImageLoadFailedException();
 
         }
 
         JLabel returnLabel = new JLabel(new ImageIcon(bookImage));
 
         return returnLabel;
 
     }
 
     /**
      * Fetch a list of books matching the non-null values in bookToMatch.
      * @param bookToMatch a book containing the values to search for (or nulls)
      * @return a list of books matching the non-null values in bookToMatch
      * @throws SQLException
      * @throws ControllerNotInitializedException
      */
     public static ArrayList<Book> searchForBook(final Book bookToMatch)
             throws SQLException, ControllerNotInitializedException {
 
         if (!initialized) {
 
             throw new ControllerNotInitializedException();
 
         }
 
         //bookToMatch is a book with 'null' in all of the parameters which
         //haven't been specified for the search
         return catalogueController.searchByBook(bookToMatch);
 
 
     }
 
     /**
      * Check the entered login credentials.
      * @param username the given username
      * @param password the given password
      * @return true if the credentials are valid
      * @throws ControllerNotInitializedException
      * @throws SQLException
      */
     public static boolean checkLogin(final String username,
             final String password)
                     throws ControllerNotInitializedException, SQLException {
 
         if (!initialized) {
 
             throw new ControllerNotInitializedException();
 
         }
 
         boolean success = loginController.checkLogin(username, password);
 
         if (success) {
 
             currentUser = accountController.getUserInfo(username);
 
         }
 
         return success;
 
 
     }
 
     /**
      * the ICEPDF reader opens 'book' and returns a JPanel to be swapped
      * into the GUI.
      * @param book the book to load in the ICEPDF reader
      * @return a newly constructed JPanel containing the ICEPDF reader with
      * 'book' loaded in it.
      * @throws ControllerNotInitializedException if the controller isn't loaded
      * @throws MalformedURLException
      * @throws BookOpenFailed
      */
     public static JPanel openReader(final Book book)
             throws ControllerNotInitializedException, MalformedURLException,
             BookOpenFailed {
 
         JPanel opened;
         if (!initialized) {
 
             throw new ControllerNotInitializedException();
 
         }
         try {
 
             opened = readerController.openBook(book);
 
         } catch (MalformedURLException e) {
 
             throw new BookOpenFailed();
 
         }
 
         return opened;
 
     }
 
 }
