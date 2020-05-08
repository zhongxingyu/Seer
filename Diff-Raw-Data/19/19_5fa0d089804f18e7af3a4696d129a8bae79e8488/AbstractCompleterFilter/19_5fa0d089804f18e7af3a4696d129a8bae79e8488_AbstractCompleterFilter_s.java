 package de.cismet.tools.gui.autocomplete;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JTextField;
 import javax.swing.UIManager;
 import javax.swing.text.AttributeSet;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Document;
 import javax.swing.text.DocumentFilter;
 
 abstract public class AbstractCompleterFilter extends DocumentFilter {
 
     static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractCompleterFilter.class);
 
     abstract public int getCompleterListSize();
 
     abstract public Object getCompleterObjectAt(int i);
 
     abstract public JTextField getTextField();
    private static final String NULL_REPRESENTATION = "null";
 
     @Override
     public void replace(final FilterBypass filterBypass, final int offset, final int length, final String string, final AttributeSet attributeSet) throws BadLocationException {
         boolean found = false;
         try {
             super.replace(filterBypass, offset, length, string, attributeSet);
         } catch (Exception e) {
             log.warn(e, e);
         }
         final Document doc = filterBypass.getDocument();
         this.preText = doc.getText(0, doc.getLength());
         this.firstSelectedIndex = -1;
 
         for (int i = 0; i < getCompleterListSize(); i++) {
             String objString = objectToStringAndHandleNullValue(getCompleterObjectAt(i));
             if ((this.caseSensitive)
                     ? objString.equals(this.preText)
                     : objString.equalsIgnoreCase(this.preText)) {
                 this.firstSelectedIndex = i;
 
                 if (this.corrective) {
                     filterBypass.replace(0, this.preText.length(), objString, attributeSet);
                 }
                 found = true;
                 break;
             }
 
             if (objString.length() <= this.preText.length()) {
                 continue;
             }
 
             String objStringStart = objString.substring(0, this.preText.length());
 
             if ((this.caseSensitive)
                     ? objStringStart.equals(this.preText)
                     : objStringStart.equalsIgnoreCase(this.preText)) {
                 String objStringEnd = objString.substring(this.preText.length());
                 if (this.corrective) {
                     filterBypass.replace(0, this.preText.length(), objString, attributeSet);
                 } else {
                     filterBypass.insertString(this.preText.length(), objStringEnd, attributeSet);
                 }
 
                 getTextField().select(this.preText.length(), doc.getLength());
                 this.firstSelectedIndex = i;
                 found = true;
                 break;
             }
         }
         if (!found) {
             firstSelectedIndex = -1;
             if (strict) {
                 this.preText = this.preText.substring(0, this.preText.length() - 1);
                 UIManager.getLookAndFeel().provideErrorFeedback(getTextField());
                 if (preText.length() == 0) {
                     if (getCompleterListSize() > -1) {
                         final String objStr = objectToStringAndHandleNullValue(getCompleterObjectAt(0));
                         final JTextField tf = getTextField();
                         tf.setText(objStr);
                         int end = tf.getText().length();
 //                        replace(filterBypass, 0, Math.min(preText.length() + 1, obj.length()), obj, attributeSet);
                         tf.setCaretPosition(end);
                         tf.moveCaretPosition(Math.min(end, 1));
                     }
                 } else {
                     filterBypass.replace(0, preText.length() + 1, this.preText, attributeSet);
                     if (this.preText.length() > 0) {
                         replace(filterBypass, 0, preText.length(), this.preText, attributeSet);
                     }
                 }
             }
         }
     }
 
     @Override
     public void insertString(FilterBypass filterBypass, int offset, String string, AttributeSet attributeSet)
             throws BadLocationException {
         if (!strict) {
             super.insertString(filterBypass, offset, string, attributeSet);
         }
 //        else {
 //            final JTextField tf = getTextField();
 //            final String tfTxt = tf.getText();
 //            replace(filterBypass, offset, tfTxt.length() + string.length(), string, attributeSet);
 //        }
     }
 
     private final String objectToStringAndHandleNullValue(Object o) {
         return o != null ? o.toString() : NULL_REPRESENTATION;
     }
 
     @Override
     public void remove(FilterBypass filterBypass, int offset, int length)
             throws BadLocationException {
         if (!strict) {
             super.remove(filterBypass, offset, length);
             final String comp = getTextField().getText();
             for (int i = 0; i < getCompleterListSize(); ++i) {
                 final String comp2 = objectToStringAndHandleNullValue(getCompleterObjectAt(i));
                 if (caseSensitive && comp.equals(comp2)) {
                     firstSelectedIndex = i;
                     return;
                 } else if (!caseSensitive && comp.equalsIgnoreCase(comp2)) {
                     firstSelectedIndex = i;
                     return;
                 }
             }
             firstSelectedIndex = -1;
         } else {
             final JTextField tf = getTextField();
             int newSelStart = (Math.min(tf.getSelectionStart(), tf.getCaretPosition()) - 1);
             if (newSelStart > -1) {
                 tf.setCaretPosition(tf.getText().length());
                 tf.moveCaretPosition(newSelStart);
             } else {
                 UIManager.getLookAndFeel().provideErrorFeedback(getTextField());
             }
         }
     }
 
     public void setCaseSensitive(boolean correctCaseSensitive) {
         this.caseSensitive = correctCaseSensitive;
     }
 
     public boolean isCaseSensitive() {
         return this.caseSensitive;
     }
 
     /**
      * Will change the user entered part of the string to match the caseSensitive of the matched item.
      *
      * e.g.
      * "europe/lONdon" would be corrected to "Europe/London"
      *
      * This option only makes sense if caseSensitive sensitive is turned off
      */
     public void setCorrectCase(boolean correctCase) {
         this.corrective = correctCase;
     }
 
     public boolean isCorrectingCase() {
         return this.corrective;
     }
 
     /**
      *
      * @return the index of the first object in the object array that can match
      * the user entered string (-1 if no object is currently being used as a match)
      */
     public int getLeadingSelectedIndex() {
         return this.firstSelectedIndex;
     }
     protected String preText; // The text in the input field before we started last looking for matches.
     protected boolean caseSensitive = false;
     protected boolean corrective = true;
     protected boolean strict = false;
     protected int firstSelectedIndex = -1;
 
     /**
      * @return the this.strict
      */
     public boolean isStrict() {
         return this.strict;
     }
 
     /**
      * @param strict the this.strict to set
      */
     public void setStrict(boolean strict) {
         this.strict = strict;
     }
 }
