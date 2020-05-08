 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /*
  * FrmTablesLayout.java
  *
  * Created on 09/03/2013, 08:44:14
  */
 
 package restaurante.UI;
 
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JFrame;
 import javax.swing.JInternalFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import restaurante.Beans.Tables;
 import restaurante.Controller.TablesController;
 import restaurante.UI.CustomComponents.LayoutCreator;
 import restaurante.UI.CustomComponents.LayoutCreator.Status;
 import restaurante.UI.CustomComponents.LayoutCreator.Table;
 import restaurante.UI.Forms.FrmNavToolbar;
 import restaurante.UI.Forms.FrmTables;
 import restaurante.Utils;
 
 /**
  *
  * @author aluno
  */
 public class FrmTablesLayout extends JFrame implements LayoutCreator.OnTableSaveListener {
     TablesController controller = new TablesController();
     LayoutCreator.Table selectedTable = null;
     
     /** Creates new form FrmTablesLayout */
     public FrmTablesLayout() {
         initComponents();
         Utils.centerScreen(FrmTablesLayout.this);
         setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
         
         initContextMenu();
         
         layoutCreator.setOnTableSaveListener(FrmTablesLayout.this);
         
         loadTables();
     }
 
     private void initContextMenu() {
         layoutCreator.setContextMenu(jPopupMenu1);
         
         initEditAndDeleteTableMenuItem();
         
         initSetStatusTableMenuItem();
     }
     
     private void initSetStatusTableMenuItem() {
         final JMenuItem miSetStatusFreeTable = new JMenuItem("Livre");
         final JMenuItem miSetStatusCleaningTable = new JMenuItem("Em limpeza");
         final JMenuItem miSetStatusReservedTable = new JMenuItem("Reservar esta Mesa");
         
         ActionListener listerner = new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 if(e.getSource().equals(miSetStatusFreeTable)) {
                     setStatusTable(LayoutCreator.Status.FREE);
                 } else if(e.getSource().equals(miSetStatusCleaningTable)) {
                     setStatusTable(LayoutCreator.Status.CLEANING);
                 } else {
                     setStatusTable(LayoutCreator.Status.RESERVED);
                 }
             }
         };
         miSetStatusFreeTable.addActionListener(listerner);
         miSetStatusCleaningTable.addActionListener(listerner);
         miSetStatusReservedTable.addActionListener(listerner);
         
         JMenu subMenu = new JMenu("Alterar Status");
         jPopupMenu1.add(subMenu);
         subMenu.add(miSetStatusFreeTable);
         subMenu.add(miSetStatusCleaningTable);
         subMenu.add(miSetStatusReservedTable);
     }
     
     private void setStatusTable(Status status) {
         if(selectedTable == null || status == null) { return; }
         switch(status) {
             case FREE:
             case CLEANING:
                 layoutCreator.setSelectedTableStatus(status);
                 try {
                     controller.saveOnlyStatus(selectedTable.getTableNumber(), status.ordinal());
                 } catch (Exception ex) {
                     Logger.getLogger(FrmTablesLayout.class.getName()).log(Level.SEVERE, null, ex);
                 }
                 break;
             default:
                 layoutCreator.setSelectedTableStatus(status);
                 FrmTables frmTables = new FrmTables(selectedTable.getTableNumber(), true);
                 frmTables.setVisible(true);
                 break;
         }
     }
     
     private void initEditAndDeleteTableMenuItem() {
         JMenuItem miEditTable = new JMenuItem("Editar Mesa");
         jPopupMenu1.add(miEditTable);
         miEditTable.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 if(selectedTable == null) { return; }
                 FrmTables frmTables = new FrmTables(selectedTable.getTableNumber());
                 frmTables.setVisible(true);
             }
         });
         
         JMenuItem miDeleteTable = new JMenuItem("Excluir Mesa");
         miDeleteTable.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 deleteTable();
             }
         });
         jPopupMenu1.add(miDeleteTable);
     }
     
     private void loadTables() {
         try {
             List<Table> tableList = new ArrayList<Table>();
             for(Tables table : controller.list()) {
                 tableList.add(layoutCreator.new Table(
                         new Point(table.getPositionX(), table.getPositionY()), table.getTableNumber(),
                         LayoutCreator.Status.values()[table.getStatus().ordinal()]));
             }
             layoutCreator.loadFromArray(tableList.toArray(new Table[tableList.size()]));
         } catch (Exception ex) {
             Logger.getLogger(FrmTablesLayout.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jPopupMenu1 = new javax.swing.JPopupMenu();
         layoutCreator = new restaurante.UI.CustomComponents.LayoutCreator();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
 
         
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(layoutCreator, javax.swing.GroupLayout.DEFAULT_SIZE, 531, Short.MAX_VALUE)
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(layoutCreator, javax.swing.GroupLayout.DEFAULT_SIZE, 449, Short.MAX_VALUE)
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     /**
     * @param args the command line arguments
     */
     public static void main(String args[]) {
         java.awt.EventQueue.invokeLater(new Runnable() {
             public void run() {
                 new FrmTablesLayout().setVisible(true);
             }
         });
     }
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JPopupMenu jPopupMenu1;
     private restaurante.UI.CustomComponents.LayoutCreator layoutCreator;
     // End of variables declaration//GEN-END:variables
 
     @Override
     public void OnTableSave(Table table) {
         try {
            if(table == null) { return; }
             int tableNumber = controller.saveOnlyPosition(table.getTableNumber(), 
                     table.getPosition().x, table.getPosition().y, FrmMain.currentBranch.getId());
             table.setTableNumber(tableNumber);
         } catch (Exception ex) {
             Logger.getLogger(FrmTablesLayout.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     @Override
     public void OnTableSelect(Table table) {
         selectedTable = table;
     }
         
     private void deleteTable() {
         if(selectedTable.getStatus() != LayoutCreator.Status.FREE) { return; }
         if(!Utils.showQuestionMessage(this, FrmNavToolbar.DELETE_CONFIRMATION)) { return; }
         
         try {
             controller.delete(selectedTable.getTableNumber());
             layoutCreator.deleteSelectedTable();
         } catch (Exception ex) {
             Logger.getLogger(FrmTablesLayout.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public void OnTableDoubleClick(Table table) {
         return;
     }
 }
