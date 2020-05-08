 
 package view.util;
 
 import javafx.scene.paint.Color;
 import javafx.scene.text.Text;
 
 /**
  * Custom Text element, used to display statuses.
  */
 public class StatusText extends Text {

    private static final Color ERROR_COLOR = Color.FIREBRICK;
     private static final Color SUCCESS_COLOR = Color.GREEN;

     /**
      * Instantiates a new status text.
      */
     public StatusText() {
         super();
         setFill(SUCCESS_COLOR);
     }

     /**
      * Instantiates a new status text.
      *
      * @param arg0 the arg0
      * @param arg1 the arg1
      * @param arg2 the arg2
      */
     public StatusText(final double arg0, final double arg1, final String arg2) {
         super(arg0, arg1, arg2);
         setFill(SUCCESS_COLOR);
     }

     /**
      * Instantiates a new status text.
      *
      * @param arg0 the arg0
      */
     public StatusText(final String arg0) {
         super(arg0);
         setFill(SUCCESS_COLOR);
     }

     /**
      * Sets the error text.
      *
      * @param text the new error text
      */
     public void setErrorText(final String text) {
         setText(text, false);
     }

     /**
      * Sets the success text.
      *
      * @param text the new success text
      */
     public void setSuccessText(final String text) {
         setText(text, true);
     }

     /**
      * Sets the text.
      *
      * @param text the text
      * @param status the status
      */
     public void setText(final String text, final boolean status) {
         if (status) {
             setFill(SUCCESS_COLOR);
         }
         else {
             setFill(ERROR_COLOR);
         }
         setText(text);
     }

     /**
      * Clear the status text.
      */
     public void clear() {
         setText("", true);
     }
 }
