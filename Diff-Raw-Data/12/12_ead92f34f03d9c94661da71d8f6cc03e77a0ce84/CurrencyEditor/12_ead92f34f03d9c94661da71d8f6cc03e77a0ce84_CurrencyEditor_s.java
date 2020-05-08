 package vinoteque.gui;
 
 import java.awt.Component;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.math.BigDecimal;
 import java.text.ParseException;
 import java.util.EventObject;
 import javax.swing.JFormattedTextField;
 import javax.swing.JTable;
 import org.apache.log4j.Logger;
 
 /**
  *
  * @author George Ushakov
  */
 public class CurrencyEditor extends CellEditor {
     private static final Logger logger = Logger.getLogger(CurrencyEditor.class);
     private JFormattedTextField fmtTextField;
     private EditWithFocusListener focusListener;
 
     public CurrencyEditor(EditWithFocusListener focusListener) {
         fmtTextField = new JFormattedTextField(new java.text.DecimalFormat("#,##0.00"));
         fmtTextField.addKeyListener(new KeyAdapter() {
 
             @Override
             public void keyPressed(KeyEvent evt) {
                 if (evt.getKeyCode()==KeyEvent.VK_ENTER){
                     if (fmtTextField.isEditValid()){
                         try {
                             fmtTextField.commitEdit();
                         } catch (ParseException e) {
                             //do nothing
                         }
                     }
                     else {
                         fireEditingCanceled();
                     }
                 }
             }
 
         });
         this.focusListener = focusListener;
     }
 
     @Override
     public boolean isCellEditable(EventObject e) {
         if (e instanceof KeyEvent){
             boolean isEditable = super.isCellEditable(e);
             if (isEditable){
                 focusListener.editWithFocus(fmtTextField);
                 return false;
             }
         }
         return super.isCellEditable(e);
     }
 
     @Override
     public Object getCellEditorValue() {
         return fmtTextField.getValue();
     }
 
     @Override
     public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
         if (value!=null){
             fmtTextField.setValue(value);
         }
         else {
             fmtTextField.setValue(new BigDecimal(0));
         }
         fmtTextField.selectAll();
         return fmtTextField;
     }
 
     @Override
     public boolean stopCellEditing() {
         if (!fmtTextField.isEditValid()){
             boolean stopEdit = super.stopCellEditing();
             if (stopEdit){
                 fireEditingCanceled();
             }
             return stopEdit;
         }
         else {
             try {
                 fmtTextField.commitEdit();
            } catch (ParseException ex) {
                 //do nothing
             }
             return super.stopCellEditing();
         }
     }
 }
