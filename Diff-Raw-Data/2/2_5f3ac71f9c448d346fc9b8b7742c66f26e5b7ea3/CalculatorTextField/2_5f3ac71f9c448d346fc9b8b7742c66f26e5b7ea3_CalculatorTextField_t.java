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
     private boolean displayMode;
 
     /**
      * Constructor
      */
     public CalculatorTextField() {
         this.setColumns(DEFAULT_CTF_COLUMNS);
         this.setEditable(false);
         this.displayMode = false;
     }
 
     /**
      * Display Mode is a mode of <code>CalculatorTextField</code>.
      * This mode should be set after the display of a calculation result.
      * It should be disabled once the user begins to input another number.
      * @param b setting
      */
     public void setDisplayMode(boolean b) {
         displayMode = b;
     }
 
     /**
      * Returns the display mode status of <code>this</code>.
      * @return <code>true</code> if <code>this</code> is in display mode,
      * <code>false</code> otherwise
      */
     public boolean getDisplayMode() {
         return displayMode;
     }
 
     /**
      * Set the text of <code>this</code> to display the given 
      * <code>Fraction</code>.
      * @param f Fraction to display
      */
     public void setText(Fraction f) {
        super.setText(f.toString(true));
     }
 }
