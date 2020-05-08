 package database.gui.forms;
 
 import database.control.ControllersHolder;
 import database.entity.Table;
 import database.model.table.EntityTableModel;
 import javax.swing.JFrame;
 
 public class DataForm
         extends JFrame {
 
     private JFrame mainForm;
     private ControllersHolder ctls;
     private EntityTableModel entityTableModel;
 
     public DataForm ( JFrame mainForm, ControllersHolder ctls ) {
         initComponents();
         this.setLocation( 200, 200 );
         setDefaultCloseOperation( DISPOSE_ON_CLOSE );
         this.mainForm = mainForm;
         this.ctls = ctls;
         updateFormTitle();
     }
 
     @Override
     public void dispose () {
         mainForm.setEnabled( true );
         super.dispose();
     }
 
     @SuppressWarnings ( "unchecked" )
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jMenuItem5 = new javax.swing.JMenuItem();
         jScrollPane2 = new javax.swing.JScrollPane();
         dataTable = new javax.swing.JTable();
         jLabel2 = new javax.swing.JLabel();
         jMenuBar1 = new javax.swing.JMenuBar();
         jMenu3 = new javax.swing.JMenu();
         jMenu4 = new javax.swing.JMenu();
         jMenu1 = new javax.swing.JMenu();
 
         jMenuItem5.setText("jMenuItem5");
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
 
         dataTable.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null}
             },
             new String [] {
                 "Title 1", "Title 2", "Title 3", "Title 4"
             }
         ));
         jScrollPane2.setViewportView(dataTable);
 
         jLabel2.setText("Данi:");
 
         jMenu3.setText("Додати рядок");
         jMenu3.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 addRow(evt);
             }
         });
         jMenuBar1.add(jMenu3);
 
         jMenu4.setText("Видалити рядок");
         jMenu4.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 delRow(evt);
             }
         });
         jMenuBar1.add(jMenu4);
 
         jMenu1.setText("Зберегти");
         jMenu1.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 saveData(evt);
             }
         });
         jMenuBar1.add(jMenu1);
 
         setJMenuBar(jMenuBar1);
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jScrollPane2)
                     .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 710, Short.MAX_VALUE))
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jLabel2)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 402, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void addRow(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addRow
         this.entityTableModel.addEmptyRow();
     }//GEN-LAST:event_addRow
 
     private void delRow(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_delRow
         int rowIndex = this.dataTable.getSelectedRow();
        
        if ( rowIndex != -1 )
            this.entityTableModel.delRowAt( rowIndex );
        else {
            ctls.showMessage( this, "Рядок не обрано." );
        }
     }//GEN-LAST:event_delRow
 
     private void saveData(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_saveData
         boolean isSuccess = ctls.getEnCtrl().saveData( entityTableModel );
         ctls.showMessage( this );
         if ( isSuccess )
             this.dispose();
     }//GEN-LAST:event_saveData
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JTable dataTable;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JMenu jMenu1;
     private javax.swing.JMenu jMenu3;
     private javax.swing.JMenu jMenu4;
     private javax.swing.JMenuBar jMenuBar1;
     private javax.swing.JMenuItem jMenuItem5;
     private javax.swing.JScrollPane jScrollPane2;
     // End of variables declaration//GEN-END:variables
 
     private void updateFormTitle () {
         this.setTitle( "Данi таблицi " + getTableName() );
     }
 
     private String getTableName () {
 
         String result = "";
 
         try {
             result = ctls.getEnCtrl().getWorkingTable().getName();
 
         } catch ( NullPointerException ex ) {
         }
 
         return result;
     }
 
     void showForTable ( Table table ) {
         ctls.getEnCtrl().setWorkingTable( table );
         entityTableModel = ctls.getEnCtrl().createEntityTableModel();
         dataTable.setModel( entityTableModel );
 
         this.setVisible( true );
         mainForm.setEnabled( false );
     }
 }
