 
 package view.util;
 
 import javafx.scene.paint.Color;
 import javafx.scene.paint.Paint;
 import javafx.scene.text.Text;
 
 /**
  * Custom Text element, used to display statuses.
  */
 public class StatusText extends Text {
     
     private static final Color ERROR_COLOR = Color.FIREBRICK;
     private Paint fill;
     
     /**
      * Instantiates a new status text.
      */
     public StatusText() {
         super();
         fill = getFill();
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
         fill = getFill();
     }
     
     /**
      * Instantiates a new status text.
      * 
      * @param arg0 the arg0
      */
     public StatusText(final String arg0) {
         super(arg0);
         fill = getFill();
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
      * Sets the text.
      * 
      * @param text the text
      * @param status the status
      */
     public void setText(final String text, final boolean status) {
         if (status) {
             setFill(fill);
         }
         else {
             setFill(ERROR_COLOR);
         }
         setText(text);
     }
    
    public void clear() {
        setText("", true);
    }
 }
