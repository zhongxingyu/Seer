 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package edacc.manageDB;
 
 import javax.swing.JTable;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 /**
  *
  * @author dgall
  */
 public class ParameterTableSelectionListener implements ListSelectionListener {
 
     private JTable table;
     private ManageDBParameters controller;
 
     public ParameterTableSelectionListener(JTable table, ManageDBParameters controller) {
         this.table = table;
         this.controller = controller;
     }
 
     @Override
     public void valueChanged(ListSelectionEvent e) {
         if (e.getSource() == table.getSelectionModel() && table.getRowSelectionAllowed()) {
             int row = table.getSelectedRow();
             if (row < 0 || row >= table.getRowCount())
                 return;
            ParameterTableModel model = (ParameterTableModel)table.getModel();
            System.out.print(row);
            System.out.print(" " + model.getParameter(row).getName());
            System.out.print("--->");
             row = table.convertRowIndexToModel(table.getSelectedRow());
            System.out.print(row);

            System.out.println(" " + model.getParameter(row).getName());
             controller.showParameter(row);
         }
     }
 }
