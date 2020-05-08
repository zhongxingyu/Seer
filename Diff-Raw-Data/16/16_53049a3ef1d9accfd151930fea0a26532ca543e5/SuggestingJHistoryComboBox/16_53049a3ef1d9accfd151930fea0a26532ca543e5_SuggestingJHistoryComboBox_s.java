 package lazybones.gui.components.historycombobox;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JTextField;
import javax.swing.text.AbstractDocument;
 import javax.swing.text.AttributeSet;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.DocumentFilter;
 import javax.swing.text.JTextComponent;
 
 public class SuggestingJHistoryComboBox extends JHistoryComboBox implements KeyListener {
 
     private EventConsumingPlainDocument doc = new EventConsumingPlainDocument();
 
     public SuggestingJHistoryComboBox(List<String> history) {
         super(history);
 
         // add keylistener for ctrl + space
         getEditor().getEditorComponent().addKeyListener(this);
 
         // add specialized document, which can consume events, which are
         // produced by the suggestion
         ((JTextComponent) getEditor().getEditorComponent()).setDocument(doc);
 
         // add DocumentFilter to trigger suggestion
        JTextField editor = (JTextField) getEditor().getEditorComponent();
        final AbstractDocument doc = (AbstractDocument) editor.getDocument();
         doc.setDocumentFilter(new DocumentFilter() {
             @Override
             public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                     throws BadLocationException {
                 super.insertString(fb, offset, string, attr);
                if (doc.getLength() > 0) {
                     suggest();
                 }
             }
 
             @Override
             public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                     throws BadLocationException {
                 super.replace(fb, offset, length, text, attrs);
                if (doc.getLength() > 0) {
                     suggest();
                 }
             }
         });
     }
 
     public SuggestingJHistoryComboBox() {
         this(new ArrayList<String>());
     }
 
     @Override
     public void actionPerformed(ActionEvent e) {
         if (e.getSource() instanceof JTextField) {
             JTextField textField = (JTextField) e.getSource();
 
             // if the ActionCommand equals SUGGEST, the user confirms a suggestion
             if ("SUGGEST".equals(e.getActionCommand())) {
                 textField.setSelectionStart(textField.getText().length());
                 textField.setSelectionEnd(textField.getText().length());
                 textField.setActionCommand("");
             } else { // the user has finished the input
                 super.actionPerformed(e);
             }
         }
     }
 
     private void suggest() {
         JTextField textField = (JTextField) getEditor().getEditorComponent();
         String text = textField.getText();
 
         // suggest text
         for (String suggestion : super.model) {
             if (suggestion.startsWith(text)) {
                 textField.setActionCommand("SUGGEST");
                 doc.setConsumeEvents(true);
                 textField.setText(suggestion);
                 textField.setSelectionStart(text.length());
                 textField.setSelectionEnd(textField.getText().length());
                 doc.setConsumeEvents(false);
                 break;
             }
         }
     }
 
     @Override
     public void keyReleased(KeyEvent e) {
         if (e.getKeyCode() == KeyEvent.VK_SPACE && e.isControlDown()) {
             suggest();
         }
     }
 
     @Override
     public void keyPressed(KeyEvent e) {
     }
 
     @Override
     public void keyTyped(KeyEvent e) {
     }
 }
