 
 package loginscreen;
 import java.sql.Connection;
 import javax.swing.JTree;
 import javax.swing.DefaultListModel;
 import javax.swing.JComponent;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.SwingUtilities;
 import javax.swing.TransferHandler;
 import javax.swing.DropMode;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreeSelectionModel;
 import java.awt.datatransfer.*;
 
 import javax.swing.*;
 import java.sql.ResultSet;
 import java.sql.PreparedStatement;
 /**
  *
  * @author jroberti
  */
class DragNdrop extends javax.swing.JFrame {   
      Mediator mediator;
      Connection conn = null;
      ResultSet rs = null;
      PreparedStatement pst = null;
 
     public DragNdrop(Mediator m, Connection c) {
         mediator = m;
         conn = c;
         initComponents();
         list1.setDropMode(DropMode.ON_OR_INSERT);
     }
    private void displayDropLocation(final String string) {
         SwingUtilities.invokeLater(new Runnable() {
                 public void run() {
                     JOptionPane.showMessageDialog(null, string);
                 }
             });
     }
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jPanel3 = new javax.swing.JPanel();
         jScrollPane1 = new javax.swing.JScrollPane();
         tree = new javax.swing.JTree();
         jPanel13 = new javax.swing.JPanel();
         jLabel1 = new javax.swing.JLabel();
         jLabel2 = new javax.swing.JLabel();
         jLabel3 = new javax.swing.JLabel();
         jLabel4 = new javax.swing.JLabel();
         jLabel5 = new javax.swing.JLabel();
         jLabel9 = new javax.swing.JLabel();
         jPanel14 = new javax.swing.JPanel();
         jLabel198 = new javax.swing.JLabel();
         jScrollPane4 = new javax.swing.JScrollPane();
         list1 = new javax.swing.JList();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         setBackground(new java.awt.Color(255, 255, 255));
 
         //Create a tree.
         DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Major");
         DefaultMutableTreeNode folder = new DefaultMutableTreeNode("CSCI");
         rootNode.add(folder);
         try{
             String sql = "select * from courses WHERE course_type = 'CSCI'";
             pst = conn.prepareStatement(sql);
             rs = pst.executeQuery();
             while (rs.next()){
                 String cid = rs.getString("course_id");
                 DefaultMutableTreeNode majorCoruses = new DefaultMutableTreeNode(cid);
                 folder.add(majorCoruses);
 
             }
         } catch(Exception e){
             JOptionPane.showMessageDialog(null, e);
         }
         DefaultMutableTreeNode subFolder = new DefaultMutableTreeNode("Math");
         rootNode.add(subFolder);
         try{
             String sql = "select * from courses WHERE course_type = 'MATH'";
             pst = conn.prepareStatement(sql);
             rs = pst.executeQuery();
             while (rs.next()){
                 String cid = rs.getString("course_id");
                 DefaultMutableTreeNode majorCoruses = new DefaultMutableTreeNode(cid);
                 subFolder.add(majorCoruses);
 
             }
         } catch(Exception e){
             JOptionPane.showMessageDialog(null, e);
         }
         //DefaultMutableTreeNode anya = new DefaultMutableTreeNode("Anya");
         //folder.add(anya);
         //folder.add(new DefaultMutableTreeNode("Bongo"));
 
         //anya.add(new DefaultMutableTreeNode("Winky"));
         DefaultTreeModel model = new DefaultTreeModel(rootNode);
         tree = new JTree(model);
         tree.getSelectionModel().setSelectionMode
         (TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
         tree.setDragEnabled(true);
         jScrollPane1.setViewportView(tree);
 
         jPanel13.setBackground(new java.awt.Color(225, 225, 225));
         jPanel13.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
 
         jLabel1.setText("Course 1");
         jLabel1.setToolTipText("");
 
         jLabel2.setText("Course 2");
 
         jLabel3.setText("Course 3");
         jLabel3.setToolTipText("");
 
         jLabel4.setText("Course 4");
 
         jLabel5.setText("Course 5");
 
         jLabel9.setText("Course 6");
 
         jPanel14.setBackground(new java.awt.Color(204, 204, 204));
 
         jLabel198.setText("                                                                                                                                Fall Semester 1");
         jLabel198.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
 
         javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
         jPanel14.setLayout(jPanel14Layout);
         jPanel14Layout.setHorizontalGroup(
             jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel14Layout.createSequentialGroup()
                 .addComponent(jLabel198, javax.swing.GroupLayout.DEFAULT_SIZE, 808, Short.MAX_VALUE)
                 .addContainerGap())
         );
         jPanel14Layout.setVerticalGroup(
             jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jLabel198, javax.swing.GroupLayout.DEFAULT_SIZE, 27, Short.MAX_VALUE)
         );
 
         DefaultListModel listModel = new DefaultListModel();
         listModel.addElement("Add and drop courses");
         listModel.addElement(" ");
         listModel.addElement(" ");
         listModel.addElement(" ");
         listModel.addElement("");
         listModel.addElement(" ");
         list1.setToolTipText("");
         list1 = new JList(listModel);
         //list.setVisibleRowCount(-1);
         list1.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
 
         list1.setTransferHandler(new TransferHandler() {
 
             public boolean canImport(TransferHandler.TransferSupport info) {
                 // we only import Strings
                 if (!info.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                     return false;
                 }
 
                 JList.DropLocation dl = (JList.DropLocation)info.getDropLocation();
                 if (dl.getIndex() == -1) {
                     return false;
                 }
                 return true;
             }
 
             public boolean importData(TransferHandler.TransferSupport info) {
                 if (!info.isDrop()) {
                     return false;
                 }
 
                 // Check for String flavor
                 if (!info.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                     displayDropLocation("List doesn't accept a drop of this type.");
                     return false;
                 }
 
                 JList.DropLocation dl = (JList.DropLocation)info.getDropLocation();
                 DefaultListModel listModel = (DefaultListModel)list1.getModel();
                 int index = dl.getIndex();
                 boolean insert = dl.isInsert();
                 // Get the current string under the drop.
                 String value = (String)listModel.getElementAt(index);
 
                 // Get the string that is being dropped.
                 Transferable t = info.getTransferable();
                 String data;
                 try {
                     data = (String)t.getTransferData(DataFlavor.stringFlavor);
                 }
                 catch (Exception e) { return false; }
 
                 // Perform the actual import.
                 if (insert) {
                     listModel.add(index, data);
                     tree.DefaultMutableTreeNode(index);
                 } else if( list = 7){
                     JOptionPane.showMessageDialog(null, "You cannot take more then 6 courses");
                 }
                 else {
                     listModel.set(index, data);
                 }
                 return true;
 
                 //return false;
             }
 
             public int getSourceActions(JComponent c) {
                 return MOVE;
             }
 
             protected Transferable createTransferable(JComponent c) {
                 JList list = (JList)c;
                 Object[] values = list.getSelectedValues();
 
                 StringBuffer buff = new StringBuffer();
 
                 for (int i = 0; i < values.length; i++) {
                     Object val = values[i];
                     buff.append(val == null ? "" : val.toString());
                     if (i != values.length - 1) {
                         buff.append("\n");
                     }
                 }
                 return new StringSelection(buff.toString());
             }
         });
         jScrollPane4.setViewportView(list1);
 
         javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
         jPanel13.setLayout(jPanel13Layout);
         jPanel13Layout.setHorizontalGroup(
             jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel13Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jPanel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addGroup(jPanel13Layout.createSequentialGroup()
                         .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jLabel1)
                             .addComponent(jLabel2)
                             .addComponent(jLabel3)
                             .addComponent(jLabel4)
                             .addComponent(jLabel5)
                             .addComponent(jLabel9))
                         .addGap(14, 14, 14)
                         .addComponent(jScrollPane4)))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         jPanel13Layout.setVerticalGroup(
             jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel13Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jPanel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(15, 15, 15)
                 .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel13Layout.createSequentialGroup()
                         .addComponent(jLabel1)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jLabel2)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jLabel3)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jLabel4)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addGap(90, 90, 90))
                     .addGroup(jPanel13Layout.createSequentialGroup()
                         .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
         );
 
         javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
         jPanel3.setLayout(jPanel3Layout);
         jPanel3Layout.setHorizontalGroup(
             jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel3Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 203, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(0, 0, 0))
         );
         jPanel3Layout.setVerticalGroup(
             jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel3Layout.createSequentialGroup()
                 .addGap(0, 0, 0)
                 .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel3Layout.createSequentialGroup()
                         .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(0, 0, Short.MAX_VALUE))
                     .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 518, Short.MAX_VALUE))
                 .addContainerGap())
         );
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(0, 0, 0))
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                 .addGap(0, 0, 0)
                 .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap())
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
    
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel198;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel3;
     private javax.swing.JLabel jLabel4;
     private javax.swing.JLabel jLabel5;
     private javax.swing.JLabel jLabel9;
     private javax.swing.JPanel jPanel13;
     private javax.swing.JPanel jPanel14;
     private javax.swing.JPanel jPanel3;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JScrollPane jScrollPane4;
     private javax.swing.JList list1;
     private javax.swing.JTree tree;
     // End of variables declaration//GEN-END:variables
 }
