 package de.cismet.tools.gui.autocomplete;
 
 import javax.swing.JComboBox;
 import javax.swing.JTextField;
 import javax.swing.text.AttributeSet;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.DocumentFilter.FilterBypass;
 import javax.swing.text.PlainDocument;
 
 /**
  * Class to add completion mechanism to combo boxes.
  * The class assumes that the look and field uses a JTextField as the combo
  * box editor. A check should be done to ensure this is true before
  * adding this class to a ComboBox.
  * 
  * To add to a combo, call the static method addCompletionMechanism(yourCombo),
  * or do the following:
  * 
  *   if (!(myCombo.getEditor().getEditorComponent() instanceof JTextField))
  *     return;
  *   
  *   JTextField tf = (JTextField)myCombo.getEditor().getEditorComponent();
  *   PlainDocument pd = new PlainDocument();
  *   this.filter = new ComboCompleterFilter(myCombo);
  *   pd.setDocumentFilter(this.filter);
  *   tf.setDocument(pd);
  *
  * @author ncochran
  *
  */
 public class ComboCompleterFilter extends AbstractCompleterFilter {
 
     private final JComboBox combo;
    private String nullRespresentation = "null";  //NOI18N
 
     /**
      * @return the nullRespresentation
      */
     public String getNullRespresentation() {
         return nullRespresentation;
     }
 
     /**
      * @param nullRespresentation the nullRespresentation to set
      */
     public void setNullRespresentation(String nullRespresentation) {
        this.nullRespresentation = nullRespresentation != null ? nullRespresentation : "null";  //NOI18N
     }
 
     public ComboCompleterFilter(JComboBox combo) {
         this.combo = combo;
     }
 
     @Override
     public int getCompleterListSize() {
         return this.combo.getModel().getSize();
     }
 
     @Override
     public Object getCompleterObjectAt(int i) {
         return this.combo.getItemAt(i);
     }
 
     @Override
     public JTextField getTextField() {
         return (JTextField) this.combo.getEditor().getEditorComponent();
     }
 
     /**
      * Helper method to add auto-completion to a jcombobox or derivation.
      *
      * The look and feel must use a JTextField as the combo box editor (or null
      * will be returned).
      *
      * The JTextField will have it's document set to a new PlainDocument and the returned
      * filter will be set to autocomplete the contents.
      * Use the returned filter to set options
      * such as case-sensitivity.
      *
      * @param combo
      */
     static public ComboCompleterFilter addCompletionMechanism(JComboBox combo) {
         combo.setEditable(true);
         if (!(combo.getEditor().getEditorComponent() instanceof JTextField)) {
             return null;
         }
 
         JTextField tf = (JTextField) combo.getEditor().getEditorComponent();
         PlainDocument pd = new PlainDocument();
         ComboCompleterFilter filter = new ComboCompleterFilter(combo);
         pd.setDocumentFilter(filter);
         tf.setDocument(pd);
         return filter;
     }
 
     @Override
     public void replace(FilterBypass filterBypass, int offset, int length, String string, AttributeSet attributeSet) throws BadLocationException {
         //TODO: DANGER??
         if (combo.isFocusOwner() || getTextField().isFocusOwner() || getTextField().getText().length() < 1) {
             super.replace(filterBypass, offset, length, string, attributeSet);
             // Try to select the item in the combo list
             if (firstSelectedIndex != -1) {
                 final JTextField tf = getTextField();
                 int preTextLen = this.preText.length();
                 final String text = tf.getText();
                 combo.setSelectedIndex(firstSelectedIndex);
                 filterBypass.replace(0, tf.getDocument().getLength(), text, attributeSet);
                 getTextField().select(preTextLen, tf.getDocument().getLength());
             }
         }
     }
 
     @Override
     public void remove(FilterBypass filterBypass, int offset, int length) throws BadLocationException {
         super.remove(filterBypass, offset, length);
         if (strict || firstSelectedIndex > -1) {
             combo.setSelectedIndex(firstSelectedIndex);
         }
     }
 
     @Override
     public void setStrict(boolean strict) {
         super.setStrict(strict);
         if (!strict) {
             combo.setSelectedIndex(-1);
         }
     }
 }
