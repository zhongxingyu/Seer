 package PizzaPck;
 
 import com.trolltech.qt.gui.QMessageBox;
 import com.trolltech.qt.gui.QWidget;
 
 /**
  * This class is a collection of error messages.
  * @author Everyone
  *
  */
 
 public class ErrorMessage {
 	
 	/**
 	 * User doesn't exist in database, message box is displayed.
 	 * @param parent
 	 */
 	public static void noSuchUser(QWidget parent){
 		QMessageBox.information(parent, "Info", "Brukeren finnes ikke i databasen");
 	}
 	
 	/**
 	 * Invalid input/No input, message box is displayed.
 	 * @param parent
 	 */
	public static void invalidInput(QWidget parent){
		QMessageBox.information(parent, "Info", "Alle feltene er ikke utfylt eller ikke riktig utfylt");
 	}
 	
 	/**
 	 * Message box is displayed if database-error occurs.
 	 * @param parent
 	 * @param s
 	 */
 	
 	public static void databaseError(QWidget parent, String s){
 		QMessageBox.warning(parent, "Database error", s);
 	}
 	
 	/**
 	 * Message box is displayed if the program fails to get 
 	 * products from the database
 	 * @param parent
 	 * @param s
 	 */
 	
 	public static void failMenu(QWidget parent, String s){
 		
 		QMessageBox.critical(parent, "Major problem",s);
 	}
 	
 	/**
 	 * Message box is displayed if no dishes is added to
 	 * the order and the user try to confirm the order.
 	 * @param parent
 	 */
 	public static void noDishAdded(QWidget parent){
 		QMessageBox.information(parent, "Info", "Ingen ordre er lagt til."+
 		"\nHvis du ikke ønsker å legge inn en ordre trykk avbryt"+
 		"\nnederst i vinduet.");
 	}
 	
 	/**
 	 * Message box is displayed if the program fails  
 	 * to write to file, where the limit prices are kept.
 	 * @param parent
 	 */
 	public static void couldNotReadFromFile(QWidget parent){
 		QMessageBox.information(parent, "Info", "En error oppsto."+
 				"\nKunne ikke skrive fra fil.");
 	}
 
 }
