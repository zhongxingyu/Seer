 package edu.uft.tocantins.View;
 
 import edu.uft.tocantins.Models.MyGraph;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import java.sql.SQLException;
 
 /**
  * Definição de MainWindow
  * Janela inicial de interação com o usuário
  * @author <a href="mailto:wolfdragon2@gmail.com">Sávio S. Dias</a>
  * @version 1.0
  */
 public class MainWindow extends JFrame 
 {
     // <------------------- 0. Variables ------------------->
     private MyGraph myGraph;
     
     // <------------------- 1. Constructors ------------------->
     /**
      * Construtor da classe MainWindow
      */
     public MainWindow() 
     {
         initComponents();
         setLocationRelativeTo( null );
         
         myGraph = MyGraph.getInstance();
     }
 
     
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jLabel1 = new javax.swing.JLabel();
         jButton1 = new javax.swing.JButton();
         jButton2 = new javax.swing.JButton();
         jButton3 = new javax.swing.JButton();
         jButton4 = new javax.swing.JButton();
         jLabel2 = new javax.swing.JLabel();
         jButton5 = new javax.swing.JButton();
         jButton6 = new javax.swing.JButton();
         jLabel3 = new javax.swing.JLabel();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
 
         jLabel1.setText("GRAFO:");
 
         jButton1.setText("Mostrar");
         jButton1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton1ActionPerformed(evt);
             }
         });
 
         jButton2.setText("Matriz de Adjacência");
         jButton2.setEnabled(false);
         jButton2.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton2ActionPerformed(evt);
             }
         });
 
         jButton3.setText("Cidade");
         jButton3.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton3ActionPerformed(evt);
             }
         });
 
         jButton4.setText("Estrada");
         jButton4.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton4ActionPerformed(evt);
             }
         });
 
         jLabel2.setText("CADASTRAR:");
 
         jButton5.setText("Dijkstra");
         jButton5.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton5ActionPerformed(evt);
             }
         });
 
         jButton6.setText("Dijkstra Aperfeiçoado");
         jButton6.setEnabled(false);
         jButton6.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton6ActionPerformed(evt);
             }
         });
 
         jLabel3.setText("CALCULAR:");
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createSequentialGroup()
                         .addContainerGap()
                         .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 176, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(18, 18, 18)
                         .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(layout.createSequentialGroup()
                         .addGap(177, 177, 177)
                         .addComponent(jLabel1))
                     .addGroup(layout.createSequentialGroup()
                         .addContainerGap()
                         .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 176, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(18, 18, 18)
                         .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(layout.createSequentialGroup()
                         .addContainerGap()
                         .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 176, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(18, 18, 18)
                         .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                 .addContainerGap(165, Short.MAX_VALUE)
                 .addComponent(jLabel2)
                 .addGap(159, 159, 159))
             .addGroup(layout.createSequentialGroup()
                 .addGap(166, 166, 166)
                 .addComponent(jLabel3)
                 .addContainerGap(167, Short.MAX_VALUE))
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jLabel1)
                 .addGap(11, 11, 11)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(18, 18, 18)
                 .addComponent(jLabel2)
                 .addGap(11, 11, 11)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(18, 18, 18)
                 .addComponent(jLabel3)
                 .addGap(11, 11, 11)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     // <------------------- 2. Events ------------------->
     /**
      * "Mostrar Grafo" action performed
      * @param evt 
      */
     private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
         // Show Graph structure
         new GraphWindow().setVisible( true );
     }//GEN-LAST:event_jButton1ActionPerformed
 
     /**
      * "Mostrar matriz de adjacência" action performed
      * @param evt 
      */
     private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
         // Show Graph adjacency matrix
     }//GEN-LAST:event_jButton2ActionPerformed
 
     /**
      * "Cadastrar Cidades" action performed
      * @param evt 
      */
     private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
         // Add new cities (vertexes) in graph and database
         new CitiesForm().setVisible( true );
     }//GEN-LAST:event_jButton3ActionPerformed
 
     /**
      * "Cadastrar Estradas" action performed
      * @param evt 
      */
     private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
         // Add new roads (edges) in graph and database
         try
         {
             new RoadsForm().setVisible(true);
         }
         catch ( SQLException exception )
         {
             JOptionPane.showMessageDialog( null, exception.getMessage() );
         }
     }//GEN-LAST:event_jButton4ActionPerformed
 
     /**
      * "Calcular Dijkstra" action performed
      * @param evt 
      */
     private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
         // Use Dijkstra algorithm to calcule SHORTEST path
        JOptionPane.showMessageDialog( null, myGraph.dijkstra( 1, 2 ) );
     }//GEN-LAST:event_jButton5ActionPerformed
 
     /**
      * "Calcular Dijkstra Aperfeiçoado" action performed
      * @param evt 
      */
     private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
         // Use Dijktra algorithm to calcule BEST path
         
     }//GEN-LAST:event_jButton6ActionPerformed
 
     // <------------------- 3. main ------------------->
     /**
      * Função principal, chamada pelo processador
      * @param args argumentos de linha de comando
      */
     public static void main( String args[] ) 
     {
         /* Set the Nimbus look and feel */
         //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
         /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
          * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
          */
         try {
             for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                 if ("Nimbus".equals(info.getName())) {
                     javax.swing.UIManager.setLookAndFeel(info.getClassName());
                     break;
                 }
             }
         } catch (ClassNotFoundException ex) {
             java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (InstantiationException ex) {
             java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
             java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (javax.swing.UnsupportedLookAndFeelException ex) {
             java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         }
         //</editor-fold>
         
         new MainWindow().setVisible(true);
     }
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton jButton1;
     private javax.swing.JButton jButton2;
     private javax.swing.JButton jButton3;
     private javax.swing.JButton jButton4;
     private javax.swing.JButton jButton5;
     private javax.swing.JButton jButton6;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel3;
     // End of variables declaration//GEN-END:variables
 }
