 package CalculatorD;
 
 import javax.swing.JTextField;
 
 /**
  * Extension of <code>JTextField</code> that displays
  * the results of calculations.
  * @author Dustin Leavins
  */
 public class CalculatorTextField extends JTextField {
     
     private static final int DEFAULT_CTF_COLUMNS = 18;
     private static final long serialVersionUID = 4517210125082963647L;
     private boolean resultMode;
     private DisplayMode displayResultsAs;
 
     /**
      * Constructor
      */
     public CalculatorTextField() {
         this.setColumns(DEFAULT_CTF_COLUMNS);
         this.setEditable(false);
         this.resultMode = false;
         this.displayResultsAs = GUIOptions.DEFAULT_DISPLAY_MODE;
     }
 
     /**
      * Result Mode is a mode of <code>CalculatorTextField</code>.
      * This mode should be enabled after the display of a calculation result.
      * It should be disabled once the user begins to input another number.
      * @param b setting
      */
     public void setResultMode(boolean b) {
         resultMode = b;
     }
 
     /**
      * Returns the Result Mode status of <code>this</code>.
      * @return <code>true</code> if <code>this</code> is in Result Mode,
      * <code>false</code> otherwise
      */
     public boolean getResultMode() {
         return resultMode;
     }
 
     /**
      * Set the text of <code>this</code> to display the given 
      * <code>Fraction</code>.
      * @param f Fraction to display
      */
     public void setText(Fraction f) {
         if (displayResultsAs == DisplayMode.DECIMAL) {
             super.setText(f.toString(true));
         }
         else {
             super.setText(f.toString(false));
         }
     }
 
     public void displayResultsAs(DisplayMode dm) {
         this.displayResultsAs = dm;
     }
 }
