 package nextgen.view;
 
 import nextgen.model.Element;
 import nextgen.model.Project;
 
 public class FProject extends javax.swing.JFrame {
 
     private Project project;
 
     public FProject(Project project) {
         this.project = project;
         initComponents();
         setElementList();
     }
 
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jPanel1 = new javax.swing.JPanel();
         jScrollPane1 = new javax.swing.JScrollPane();
         listElements = new javax.swing.JList();
         jPanel2 = new javax.swing.JPanel();
         bAdd = new javax.swing.JButton();
         bDelete = new javax.swing.JButton();
         spElements = new javax.swing.JScrollPane();
         jMenuBar1 = new javax.swing.JMenuBar();
         jMenu1 = new javax.swing.JMenu();
         jMenu2 = new javax.swing.JMenu();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
 
         jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Elements"));
 
         jScrollPane1.setViewportView(listElements);
 
         jPanel2.setLayout(new java.awt.GridLayout(1, 0));
 
         bAdd.setText("Add");
         jPanel2.add(bAdd);
 
         bDelete.setText("Delete");
         jPanel2.add(bDelete);
 
         javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
         jPanel1.setLayout(jPanel1Layout);
         jPanel1Layout.setHorizontalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel1Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 267, Short.MAX_VALUE)
                     .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addContainerGap())
         );
         jPanel1Layout.setVerticalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel1Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 436, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap())
         );
 
         jMenu1.setText("File");
         jMenuBar1.add(jMenu1);
 
         jMenu2.setText("Edit");
         jMenuBar1.add(jMenu2);
 
         setJMenuBar(jMenuBar1);
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(spElements, javax.swing.GroupLayout.DEFAULT_SIZE, 503, Short.MAX_VALUE)
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(spElements))
                 .addContainerGap())
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton bAdd;
     private javax.swing.JButton bDelete;
     private javax.swing.JMenu jMenu1;
     private javax.swing.JMenu jMenu2;
     private javax.swing.JMenuBar jMenuBar1;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JPanel jPanel2;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JList listElements;
     private javax.swing.JScrollPane spElements;
     // End of variables declaration//GEN-END:variables
 
     private void setElementList() {
        
     }
 }
