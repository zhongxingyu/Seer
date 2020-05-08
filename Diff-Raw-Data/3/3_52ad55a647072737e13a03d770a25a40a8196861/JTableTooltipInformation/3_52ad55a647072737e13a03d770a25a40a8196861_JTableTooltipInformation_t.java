 package edacc;
 
 import javax.swing.JTable;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.event.TableModelEvent;
 import javax.swing.table.TableModel;
 
 /**
  *
  * @author simon
  */
 public class JTableTooltipInformation extends JTable {
 
     private String defaultToolTip;
 
     public JTableTooltipInformation() {
         super();
         defaultToolTip = "";
         this.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
 
             @Override
             public void valueChanged(ListSelectionEvent e) {
                 updateToolTipText();
             }
         });
     }
 
     public JTableTooltipInformation(TableModel tableModel) {
        this();
        this.setModel(tableModel);
     }
 
     @Override
     public void setToolTipText(String text) {
         defaultToolTip = text;
         updateToolTipText();
     }
     
     
 
     private void updateToolTipText() {
         String text = "<html>";
         if (defaultToolTip != null && !"".equals(defaultToolTip)) {
             text += defaultToolTip + "<br/>";
         }
         int rows = JTableTooltipInformation.this.getSelectedRowCount();
         text += rows + " / " + JTableTooltipInformation.this.getRowCount() + " selected</html>";
         JTableTooltipInformation.super.setToolTipText(text);
     }
 
     @Override
     public void tableChanged(TableModelEvent e) {
         super.tableChanged(e);
         updateToolTipText();
     }
 }
