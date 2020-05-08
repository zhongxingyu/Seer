 package jBox;
 
 import db.registros.UsuarioCarpeta;
 import java.io.IOException;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.io.File;
 import java.util.ArrayList;
 import javax.swing.JFileChooser;
 import login.DialogLogin;
 import modelos.CarpetasTableModel;
 import modelos.StatusTableModel;
 import org.jdesktop.swingx.JXCollapsiblePane;
 import puntoGit.PuntoGit;
 import puntoGit.Status;
 import utils.DialogoMensage;
 
 /**
  *
  * @author fermani
  */
 public class JBox extends generic.GenericFrame {
 
     private JFileChooser fileChooser;
     private PuntoGit pg;
     private JXCollapsiblePane pnlCollapsiblePane;
     private DialogoMensage dialogoMensaje;
 
     public JBox() throws IOException {
         super();
         DialogLogin dialogLogin = new DialogLogin(this);
         dialogLogin.setVisible(true);
         if (getConexion().getUsuario() == null) {
             System.exit(0);
         } else {
             initComponents();
             setLocationRelativeTo(null);
             pnlCollapsiblePane = new JXCollapsiblePane();
             pnlCollapsiblePane.setLayout(new java.awt.BorderLayout());
             pnlCollapsiblePane.add(pnlStatus, java.awt.BorderLayout.CENTER);
             pnlCollapsiblePane.setDirection(JXCollapsiblePane.Direction.LEFT);
             pnlCollapsiblePane.setCollapsed(true);
             pnlDatos.add(pnlCollapsiblePane, java.awt.BorderLayout.EAST);
             _initData();
 
         }
     }
 
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         pnlStatus = new javax.swing.JPanel();
         scrollStatus = new javax.swing.JScrollPane();
         tblStatus = new org.jdesktop.swingx.JXTable();
         tbrBotonesCarpetas = new javax.swing.JToolBar();
         btnActCarpeta = new javax.swing.JButton();
         btnGuardar = new javax.swing.JButton();
         pnlDatos = new javax.swing.JPanel();
         pnlSur = new javax.swing.JPanel();
         pnlNorte = new javax.swing.JPanel();
         jButton1 = new javax.swing.JButton();
         pnlCentro = new javax.swing.JPanel();
         scroll = new javax.swing.JScrollPane();
         tblCarpetas = new org.jdesktop.swingx.JXTable();
         jToolBar2 = new javax.swing.JToolBar();
         btnAgrCarp = new javax.swing.JButton();
         btnCompartir = new javax.swing.JButton();
         btnStatus = new javax.swing.JToggleButton();
 
         pnlStatus.setPreferredSize(new java.awt.Dimension(458, 300));
 
         tblStatus.setColumnControlVisible(true);
         tblStatus.setHorizontalScrollEnabled(true);
         scrollStatus.setViewportView(tblStatus);
 
         tbrBotonesCarpetas.setFloatable(false);
         tbrBotonesCarpetas.setRollover(true);
 
        btnActCarpeta.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        btnActCarpeta.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/sincronizar/sincr_24.png"))); // NOI18N
         btnActCarpeta.setText("Actualizar");
         btnActCarpeta.setMargin(new java.awt.Insets(2, 2, 2, 2));
         btnActCarpeta.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnActCarpetaActionPerformed(evt);
             }
         });
         tbrBotonesCarpetas.add(btnActCarpeta);
 
         btnGuardar.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        btnGuardar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/guardar/guardar_24.png"))); // NOI18N
         btnGuardar.setText("Guardar");
         btnGuardar.setFocusable(false);
         btnGuardar.setMargin(new java.awt.Insets(2, 2, 2, 2));
         btnGuardar.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnGuardarActionPerformed(evt);
             }
         });
         tbrBotonesCarpetas.add(btnGuardar);
 
         javax.swing.GroupLayout pnlStatusLayout = new javax.swing.GroupLayout(pnlStatus);
         pnlStatus.setLayout(pnlStatusLayout);
         pnlStatusLayout.setHorizontalGroup(
             pnlStatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlStatusLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(pnlStatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(tbrBotonesCarpetas, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 434, Short.MAX_VALUE)
                     .addComponent(scrollStatus, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 434, Short.MAX_VALUE))
                 .addContainerGap())
         );
         pnlStatusLayout.setVerticalGroup(
             pnlStatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlStatusLayout.createSequentialGroup()
                 .addComponent(tbrBotonesCarpetas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(scrollStatus, javax.swing.GroupLayout.DEFAULT_SIZE, 244, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
 
         pnlDatos.setLayout(new java.awt.BorderLayout());
 
         pnlSur.setPreferredSize(new java.awt.Dimension(40, 50));
 
         javax.swing.GroupLayout pnlSurLayout = new javax.swing.GroupLayout(pnlSur);
         pnlSur.setLayout(pnlSurLayout);
         pnlSurLayout.setHorizontalGroup(
             pnlSurLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 845, Short.MAX_VALUE)
         );
         pnlSurLayout.setVerticalGroup(
             pnlSurLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 50, Short.MAX_VALUE)
         );
 
         pnlDatos.add(pnlSur, java.awt.BorderLayout.SOUTH);
 
         pnlNorte.setBackground(new java.awt.Color(184, 184, 255));
         pnlNorte.setPreferredSize(new java.awt.Dimension(845, 80));
 
         jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/config/config_32.png"))); // NOI18N
 
         javax.swing.GroupLayout pnlNorteLayout = new javax.swing.GroupLayout(pnlNorte);
         pnlNorte.setLayout(pnlNorteLayout);
         pnlNorteLayout.setHorizontalGroup(
             pnlNorteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlNorteLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(780, Short.MAX_VALUE))
         );
         pnlNorteLayout.setVerticalGroup(
             pnlNorteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlNorteLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addGap(48, 48, 48))
         );
 
         pnlDatos.add(pnlNorte, java.awt.BorderLayout.PAGE_START);
 
         tblCarpetas.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
 
             },
             new String [] {
 
             }
         ));
         tblCarpetas.setColumnControlVisible(true);
         tblCarpetas.setHorizontalScrollEnabled(true);
         scroll.setViewportView(tblCarpetas);
 
         jToolBar2.setFloatable(false);
         jToolBar2.setRollover(true);
 
        btnAgrCarp.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        btnAgrCarp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/agregar/compartir_24.png"))); // NOI18N
         btnAgrCarp.setText("Agregar Carpeta");
         btnAgrCarp.setMargin(new java.awt.Insets(2, 2, 2, 2));
         btnAgrCarp.setPreferredSize(new java.awt.Dimension(101, 36));
         btnAgrCarp.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnAgrCarpActionPerformed(evt);
             }
         });
         jToolBar2.add(btnAgrCarp);
 
        btnCompartir.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        btnCompartir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/compartir/compartir_24.png"))); // NOI18N
         btnCompartir.setText("Compartir");
         btnCompartir.setMargin(new java.awt.Insets(2, 2, 2, 2));
         btnCompartir.setPreferredSize(new java.awt.Dimension(101, 36));
         btnCompartir.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnCompartirActionPerformed(evt);
             }
         });
         jToolBar2.add(btnCompartir);
 
        btnStatus.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        btnStatus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/imagenes/estado/status_24.png"))); // NOI18N
         btnStatus.setText("Estado");
         btnStatus.setMargin(new java.awt.Insets(2, 2, 2, 2));
         btnStatus.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnStatusActionPerformed(evt);
             }
         });
         jToolBar2.add(btnStatus);
 
         javax.swing.GroupLayout pnlCentroLayout = new javax.swing.GroupLayout(pnlCentro);
         pnlCentro.setLayout(pnlCentroLayout);
         pnlCentroLayout.setHorizontalGroup(
             pnlCentroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlCentroLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(pnlCentroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(scroll, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 821, Short.MAX_VALUE)
                     .addComponent(jToolBar2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 821, Short.MAX_VALUE))
                 .addContainerGap())
         );
         pnlCentroLayout.setVerticalGroup(
             pnlCentroLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlCentroLayout.createSequentialGroup()
                 .addComponent(jToolBar2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(scroll, javax.swing.GroupLayout.DEFAULT_SIZE, 395, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         pnlDatos.add(pnlCentro, java.awt.BorderLayout.CENTER);
 
         getContentPane().add(pnlDatos, java.awt.BorderLayout.CENTER);
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void btnAgrCarpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAgrCarpActionPerformed
         if (fileChooser == null) {
             fileChooser = new JFileChooser();
             fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
         }
         int showOpenDialog = fileChooser.showDialog(this, "Compartir");
         if (showOpenDialog == JFileChooser.APPROVE_OPTION) {
             File file = fileChooser.getSelectedFile();
             System.out.println(file.getAbsoluteFile());
             System.out.println(file.getName());
             pg = new PuntoGit(file, getConsole());
             try {
                 pg.init(getConexion());
             } catch (Exception ex) {
                 Logger.getLogger(JBox.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
     }//GEN-LAST:event_btnAgrCarpActionPerformed
 
     private void btnStatusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStatusActionPerformed
         if (btnStatus.getModel().isSelected()) {
             if (tblCarpetas.getSelectedRow() != -1
                     && tblCarpetas.convertRowIndexToModel(tblCarpetas.getSelectedRow()) != -1) {
                 pnlCollapsiblePane.setCollapsed(false);
                 _actualizarCarpeta();
                 _editar(false);
             } else {
                 msg("Seleccione una carpeta.");
                 btnStatus.getModel().setSelected(false);
             }
         } else {
             _editar(true);
             pnlCollapsiblePane.setCollapsed(true);
         }
     }//GEN-LAST:event_btnStatusActionPerformed
 
     private void btnCompartirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCompartirActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_btnCompartirActionPerformed
 
     private void btnActCarpetaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnActCarpetaActionPerformed
         _actualizarCarpeta();
     }//GEN-LAST:event_btnActCarpetaActionPerformed
 
     private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
         int pos = tblCarpetas.convertRowIndexToModel(tblCarpetas.getSelectedRow());
         pg = ((UsuarioCarpeta) ((CarpetasTableModel) tblCarpetas.getModel()).get(pos)).getPuntoGit();
         try {
             if (dialogoMensaje == null) {
                 dialogoMensaje = new DialogoMensage(this, true);
             }
             dialogoMensaje.setVisible(true);
             if (dialogoMensaje.isAcepto()) {
                 pg.push(dialogoMensaje.getMensaje());
                 _actualizarCarpeta();
             }
         } catch (IOException ex) {
             Logger.getLogger(JBox.class.getName()).log(Level.SEVERE, null, ex);
         } catch (InterruptedException ex) {
             Logger.getLogger(JBox.class.getName()).log(Level.SEVERE, null, ex);
         }
     }//GEN-LAST:event_btnGuardarActionPerformed
 
     public static void main(String args[]) {
         /* Create and display the form */
         java.awt.EventQueue.invokeLater(new Runnable() {
 
             @Override
             public void run() {
                 try {
                     new JBox().setVisible(true);
                 } catch (IOException ex) {
                     Exception e = new Exception("No se encontró el archivo de logger", ex);
                     Logger.getLogger(JBox.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         });
 
     }
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btnActCarpeta;
     private javax.swing.JButton btnAgrCarp;
     private javax.swing.JButton btnCompartir;
     private javax.swing.JButton btnGuardar;
     private javax.swing.JToggleButton btnStatus;
     private javax.swing.JButton jButton1;
     private javax.swing.JToolBar jToolBar2;
     private javax.swing.JPanel pnlCentro;
     private javax.swing.JPanel pnlDatos;
     private javax.swing.JPanel pnlNorte;
     private javax.swing.JPanel pnlStatus;
     private javax.swing.JPanel pnlSur;
     private javax.swing.JScrollPane scroll;
     private javax.swing.JScrollPane scrollStatus;
     private org.jdesktop.swingx.JXTable tblCarpetas;
     private org.jdesktop.swingx.JXTable tblStatus;
     private javax.swing.JToolBar tbrBotonesCarpetas;
     // End of variables declaration//GEN-END:variables
 
     private void _initData() {
 
         tblCarpetas.setModel(new CarpetasTableModel());
 
         String qry = "select"
                 + "        usuario_carpeta.id_usuario,"
                 + "        usuario_carpeta.id_carpeta,"
                 + "        usuario_carpeta.id_tipo,"
                 + "        usuario_carpeta.carpeta_local,"
                 + "        carpetas.carpeta "
                 + "from"
                 + "        carpetas JOIN usuario_carpeta ON carpetas.id = usuario_carpeta.id_carpeta "
                 + "where"
                 + "        usuario_carpeta.id_usuario = " + getConexion().getUsuario().getId() + ";";
         try {
             ResultSet rs = getConexion().execSQLSelect(qry);
             UsuarioCarpeta uc;
             ArrayList<UsuarioCarpeta> list = new ArrayList<UsuarioCarpeta>();
             while (rs.next()) {
                 uc = new UsuarioCarpeta(getConsole());
                 uc.getCarpeta().setId(rs.getInt("id_carpeta"));
                 uc.getCarpeta().setNombre(rs.getString("carpeta"));
                 uc.getTipo().setId(rs.getInt("id_tipo"));
                 uc.setUsuario(getConexion().getUsuario());
                 uc.setCarpetaLocal(rs.getString("carpeta_local"));
 
                 list.add(uc);
             }
 
             ((CarpetasTableModel) tblCarpetas.getModel()).newData(list);
             tblCarpetas.packAll();
 
         } catch (SQLException ex) {
             Logger.getLogger(JBox.class.getName()).log(Level.SEVERE, null, ex);
         }
 
     }
 
     private void _actualizarCarpeta() {
 
 
         try {
             int pos = tblCarpetas.convertRowIndexToModel(tblCarpetas.getSelectedRow());
             pg = ((UsuarioCarpeta) ((CarpetasTableModel) tblCarpetas.getModel()).get(pos)).getPuntoGit();
 
             Status status = pg.status();
             tblStatus.setModel(new StatusTableModel(status, pg));
             tblStatus.getColumn(0).setMaxWidth(30);
             tblStatus.getColumn(0).setMinWidth(30);
             tblStatus.getColumn(0).setPreferredWidth(30);
             tblStatus.packAll();
             pnlCollapsiblePane.setCollapsed(false);
         } catch (IOException ex) {
             logger.log(Level.SEVERE, ex.getMessage(), ex);
         } catch (InterruptedException ex) {
             logger.log(Level.SEVERE, ex.getMessage(), ex);
         }
 
     }
 
     private void _editar(boolean b) {
         tblCarpetas.setEnabled(b);
         btnAgrCarp.setEnabled(b);
         btnCompartir.setEnabled(b);
     }
 }
