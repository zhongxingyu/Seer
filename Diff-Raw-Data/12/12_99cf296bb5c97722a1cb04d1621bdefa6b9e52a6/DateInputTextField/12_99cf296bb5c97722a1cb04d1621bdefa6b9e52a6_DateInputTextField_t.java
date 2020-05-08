 package terminKalender;
 /**
 * Copyright © 2006 Bastie - Sebastian Ritter
  */
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 import java.util.logging.Logger;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FontMetrics;
 import javax.swing.*;
 import javax.swing.border.Border;
 import javax.swing.text.DefaultFormatterFactory;
 import javax.swing.text.MaskFormatter;
 
 /**
  * Simple TextField to input a date.
  *
  * @author Bastie - Sebastian Ritter
  * @version 1.0
  * @since Java 1.5
  */
 public class DateInputTextField extends JFormattedTextField {
 
   /**
 	 * 
 	 */
 	private static final long serialVersionUID = 12179858122717833L;
 public DateInputTextField () {
     this.setFormatterFactory(new DefaultFormatterFactory (this.getDateMask()));
     this.setInputVerifier(new DateInputVerifier());
   }
 
   /**
    * Preferred size for date mask
    * @return Dimension
    */
   public Dimension getPreferredSize () {
     final double height = super.getPreferredSize().getHeight();
     final FontMetrics fm = this.getFontMetrics(this.getFont());
     int breite = fm.charsWidth (this.getDateMask ().getMask().toCharArray(), 0, this.getDateMask ().getMask().length ());
     breite += this.getInsets().left+this.getInsets().right;
     if (this.getBorder() != null)
       breite += this.getBorder().getBorderInsets(this).left + this.getBorder().getBorderInsets(this).right;
     Dimension d = new Dimension();
     d.setSize((double) breite, height);
     return d;
   }
 
   /**
    * Format of input
    * @return MaskFormatter
    */
   protected MaskFormatter getDateMask () {
     MaskFormatter formatter = null;
     try {
       if (Locale.getDefault ().getLanguage ().equals (Locale.GERMANY.getLanguage())) {
         formatter = new MaskFormatter ("##.##.####");
       }
       else {
         formatter = new MaskFormatter ("####-##-##");
       }
       if (this.getPlaceHolder() != null) {
         formatter.setPlaceholderCharacter (this.getPlaceHolder());
       }
     }
     catch (final ParseException ignored) {
       Logger.getLogger(this.getClass().getName()).throwing (this.getClass().getName(),"getDateMask", ignored);
     }
     return formatter;
   }
 
 
   private Character placeholder = null;
   /**
    * Set an Empty Character for delete the Input. If Empty Character is null,
    * a valid value need to input.
    * @param c Character
    */
   public void setPlaceholder (final Character c) {
     this.placeholder = c;
   }
 
   /**
    * Return the char for delete the input or null if delete not allowed.
    * @return Character
    */
   public Character getPlaceHolder () {
     return this.placeholder;
   }
 
 
   /**
    * Simple Date Verifier
    * @author Bastie - Sebastian Ritter
    * @version 1.0
    */
   protected static class DateInputVerifier extends InputVerifier {
     public boolean verify (final JComponent input) {
       if (input instanceof DateInputTextField) {
         return this.isAlowedDate((DateInputTextField)input);
       }
       else {
         return true;
       }
     }
 
     /**
      * Check the incomming Date
      * @param input DateInputTextField
      * @return boolean
      */
     protected boolean isAlowedDate (final DateInputTextField input) {
       final DateFormat sdf = this.getDateFormat ();
       try {
         final Date d = sdf.parse (input.getText());
         SwingUtilities.invokeLater(new Runnable () {
           public void run () {
             input.setText(sdf.format(d));
           }
         });
         return true;
       }
       catch (final ParseException notValidOrDelete) {
         if (input.getPlaceHolder() != null) {
           String noMaskValue = null;
           if (Locale.getDefault ().getLanguage ().equals (Locale.GERMANY.getLanguage ())) {
             noMaskValue = input.getText().replace ('.',input.getPlaceHolder ());
           }
           else {
             noMaskValue = input.getText().replace ('-',input.getPlaceHolder ());
           }
           for (char c : noMaskValue.toCharArray()) {
             if (c != input.getPlaceHolder()) return false;
           }
           return true;
         }
         return false;
       }
     }
 
     /**
      * Return i18n DateFormat
      * @return DateFormat
      */
     protected DateFormat getDateFormat () {
       if (Locale.getDefault().getLanguage().equals(Locale.GERMANY.getLanguage())) {
         return new SimpleDateFormat ("dd.MM.yyyy");
       }
       else {
         return new SimpleDateFormat("yyyy-MM-dd");
       }
     }
 
     /**
      * Change the gui.
      * @param input the JComponent to verify
      * @return true when valid, false when invalid
      *
      */
     public boolean shouldYieldFocus (final JComponent input) {
       if (!verify(input)) {
         input.setForeground(Color.RED);
         input.setBorder(BorderFactory.createEtchedBorder(Color.RED, new Color (255,50,50)));
         return false;
       }
       else {
         input.setForeground(Color.BLACK);
         input.setBorder((Border)UIManager.getLookAndFeelDefaults().get("TextField.border"));
         return true;
       }
     }
   }
 }
