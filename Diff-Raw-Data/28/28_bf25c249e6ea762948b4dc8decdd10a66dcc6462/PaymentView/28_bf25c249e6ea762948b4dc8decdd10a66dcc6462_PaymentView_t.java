 package arcade.view.forms.payment;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ResourceBundle;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 
 import arcade.controller.Controller;
 import arcade.exceptions.InvalidPaymentException;
 import arcade.games.GameInfo;
 import arcade.view.TextKeywords;
 import arcade.view.forms.Form;
 
 
 /**
  * The view where a user can enter his/her payment information to buy a game.
  * 
  * Different types of payment systems should subclass this view to tailor
  * which fields they need.
  * 
  * @author Ellango
  * 
  */
 @SuppressWarnings("serial")
 public abstract class PaymentView extends Form {
     private GameInfo myGameInfo;
     private String myTransactionType;
 
     /**
      * Constructs the PaymentView with a Controller, ResourceBundle, and a GameInfo
      * for the game to be bought.
      * 
      * @param controller
      * @param resources
      * @param info
      */
     public PaymentView (Controller controller, ResourceBundle resources, GameInfo game, String transactionType) {
         super(controller, resources);
         myGameInfo = game;
         myTransactionType = transactionType;
         setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
         pack();
         setLocationRelativeTo(null);
     }
 
     /**
      * Creates the button to perform the transaction
      * 
      * @return
      */
     protected JComponent createBuyButton () {
         return createButton(TextKeywords.BUY,
                             new ActionListener() {
                                 @Override
                                 public void actionPerformed (ActionEvent e) {
                                     try {
                                         getController().performTransaction(myGameInfo,
                                                                       myTransactionType,
                                                                       getPaymentInfo());
                                        dispose();
                                     }
                                     catch (InvalidPaymentException e1) {
                                         sendMessage(getResources()
                                                 .getString(e1.getLocalizedMessage()));
                                     }
                                 }
                             });
     }
     
     /**
      * Returns the game being paid for so subclasses have access.
      */
     protected GameInfo getGame() {
         return myGameInfo;
     }
 
     /**
      * Submits the required payment information fields to be processed.
      */
     protected abstract String[] getPaymentInfo ();
     
 
 }
