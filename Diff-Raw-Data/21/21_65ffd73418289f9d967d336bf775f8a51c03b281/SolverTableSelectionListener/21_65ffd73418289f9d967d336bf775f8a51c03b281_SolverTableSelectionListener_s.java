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
 public class SolverTableSelectionListener implements ListSelectionListener {
 
     private JTable table;
     private ManageDBSolvers controller;
 
     public SolverTableSelectionListener(JTable table, ManageDBSolvers controller) {
         this.table = table;
         this.controller = controller;
     }
 
     public void valueChanged(ListSelectionEvent e) {
         if (e.getSource() == table.getSelectionModel() && table.getRowSelectionAllowed()) {
             if (table.getSelectedRow() != -1) {
                 controller.showSolver(table.convertRowIndexToModel(table.getSelectedRow()));
            }
         }
     }
 }
