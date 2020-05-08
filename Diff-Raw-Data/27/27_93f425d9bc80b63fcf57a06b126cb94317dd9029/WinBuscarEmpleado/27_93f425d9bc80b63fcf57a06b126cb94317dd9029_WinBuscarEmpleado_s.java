 
 package Gui;
 import Dao.BusquedaEmpleadoDAO;
 import Utilitarios.Query;
 import javax.swing.table.DefaultTableModel;
 import Utilitarios.Data;
 import java.awt.Image;
 import java.awt.event.KeyEvent;
 import java.io.File;
 import javax.swing.ImageIcon;
 
 
 public class WinBuscarEmpleado extends javax.swing.JInternalFrame {
     private BusquedaEmpleadoDAO Bus;
     private Query qs;
     public int Form;
     public static String Nomape;
     public Data dt;
     
     public WinBuscarEmpleado() {
         initComponents();
         dt = new Data();
         this.setTitle(dt.G_TITULOS[27]);
         cargaForm();
         
     }
     private void cargaForm(){
         qs = new Query();
         Bus = new BusquedaEmpleadoDAO();
         
         qs.loadChoice(CboEstado,"estadoemp","nombre");
         
         qs.loadChoice(Cboarea,"area","nombre");
         qs.setIdentify("idare");
         qs.loadChoiceDefault(CboCargo,"cargo","nombre",
             Integer.parseInt(qs.idChoice("area","nombre",
             String.valueOf(Cboarea.getSelectedItem()))));
         
         qs.loadChoice(CboEmpresa,"empresa","nombre");
         qs.setIdentify("idempr");
         qs.loadChoiceDefault(CboSucursal,"sucursal","nombre",
             Integer.parseInt(qs.idChoice("empresa","nombre",
             String.valueOf(CboEmpresa.getSelectedItem()))));
         LblModulo.setVisible(false);
         buscar();
     }
     private void buscar(){
         Bus = new BusquedaEmpleadoDAO();
         qs = new Query();
         String[] campos = new String[4];
         String Nombres = TxtNombres.getText().toUpperCase();
         String apellidos = Txtapellidos.getText().toUpperCase();
         String idemp = qs.idChoice("empresa", "nombre", String.valueOf(CboEmpresa.getSelectedItem()));
         campos[0] = "nombre";
         campos[1] = String.valueOf(CboSucursal.getSelectedItem());
         campos[2] = "idempr";
         campos[3] = idemp;
         String idsuc = qs.getIdMltiSentences("sucursal","idsuc", campos);
         String idarea = qs.idChoice("area", "nombre", String.valueOf(Cboarea.getSelectedItem()));
         campos[0] = "nombre";
         campos[1] = String.valueOf(CboCargo.getSelectedItem());
         campos[2] = "idare";
         campos[3] = idarea;
         String idcargo = qs.getIdMltiSentences("cargo","idcar", campos);
         String estado = qs.idChoice("estadoemp", "nombre", String.valueOf(CboEstado.getSelectedItem()));
         Bus.find(Nombres, apellidos,idarea,idcargo,idemp,idsuc,estado,Tblbuscador, lblcant);
     }
           
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jPanel1 = new javax.swing.JPanel();
         jLabel1 = new javax.swing.JLabel();
         jLabel2 = new javax.swing.JLabel();
         Txtapellidos = new javax.swing.JTextField();
         jLabel3 = new javax.swing.JLabel();
         jLabel4 = new javax.swing.JLabel();
         CboEmpresa = new javax.swing.JComboBox();
         CboSucursal = new javax.swing.JComboBox();
         CboCargo = new javax.swing.JComboBox();
         jButton1 = new javax.swing.JButton();
         jLabel5 = new javax.swing.JLabel();
         Cboarea = new javax.swing.JComboBox();
         jLabel6 = new javax.swing.JLabel();
         CboEstado = new javax.swing.JComboBox();
         LblModulo = new javax.swing.JLabel();
         jLabel8 = new javax.swing.JLabel();
         TxtNombres = new javax.swing.JTextField();
         jScrollPane1 = new javax.swing.JScrollPane();
         Tblbuscador = new javax.swing.JTable();
         lblcant = new javax.swing.JLabel();
         jLabel7 = new javax.swing.JLabel();
        jSpinner1 = new javax.swing.JSpinner();
 
         jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Criterios de busqueda"));
 
         jLabel1.setText("Apellidos");
 
         jLabel2.setText("Empresa");
 
         jLabel3.setText("Sucursal");
 
         jLabel4.setText("Cargo");
 
         CboEmpresa.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 CboEmpresaActionPerformed(evt);
             }
         });
 
         jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Utilitarios/Img/consulta.png"))); // NOI18N
         jButton1.setText("Buscar");
         jButton1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton1ActionPerformed(evt);
             }
         });
 
         jLabel5.setText("Area");
 
         Cboarea.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 CboareaActionPerformed(evt);
             }
         });
 
         jLabel6.setText("Estado");
 
         jLabel8.setText("Nombres");
 
         TxtNombres.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 TxtNombresKeyPressed(evt);
             }
         });
 
         javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
         jPanel1.setLayout(jPanel1Layout);
         jPanel1Layout.setHorizontalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel1Layout.createSequentialGroup()
                 .addGap(14, 14, 14)
                 .addComponent(jLabel8)
                 .addGap(6, 6, 6)
                 .addComponent(TxtNombres, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(30, 30, 30)
                 .addComponent(jLabel1)
                 .addGap(8, 8, 8)
                 .addComponent(Txtapellidos, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
             .addGroup(jPanel1Layout.createSequentialGroup()
                 .addGap(12, 12, 12)
                 .addComponent(jLabel2)
                 .addGap(12, 12, 12)
                 .addComponent(CboEmpresa, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(26, 26, 26)
                 .addComponent(jLabel3)
                 .addGap(12, 12, 12)
                 .addComponent(CboSucursal, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
             .addGroup(jPanel1Layout.createSequentialGroup()
                 .addGap(12, 12, 12)
                 .addComponent(jLabel5)
                 .addGap(41, 41, 41)
                 .addComponent(Cboarea, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(26, 26, 26)
                 .addComponent(jLabel4)
                 .addGap(29, 29, 29)
                 .addComponent(CboCargo, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE))
             .addGroup(jPanel1Layout.createSequentialGroup()
                 .addGap(12, 12, 12)
                 .addComponent(LblModulo)
                 .addGap(2, 2, 2)
                 .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(0, 0, 0)
                 .addComponent(CboEstado, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(97, 97, 97)
                 .addComponent(jButton1))
         );
         jPanel1Layout.setVerticalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel1Layout.createSequentialGroup()
                 .addGap(11, 11, 11)
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jLabel8)
                     .addComponent(TxtNombres, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel1)
                     .addComponent(Txtapellidos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(19, 19, 19)
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(CboEmpresa, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(CboSucursal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addGroup(jPanel1Layout.createSequentialGroup()
                         .addGap(5, 5, 5)
                         .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jLabel2)
                             .addComponent(jLabel3))))
                 .addGap(18, 18, 18)
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(Cboarea, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(CboCargo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addGroup(jPanel1Layout.createSequentialGroup()
                         .addGap(5, 5, 5)
                         .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jLabel5)
                             .addComponent(jLabel4))))
                 .addGap(6, 6, 6)
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(LblModulo)
                     .addComponent(jButton1)
                     .addGroup(jPanel1Layout.createSequentialGroup()
                         .addGap(6, 6, 6)
                         .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jLabel6)
                             .addComponent(CboEstado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         Tblbuscador.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {},
                 {},
                 {},
                 {}
             },
             new String [] {
 
             }
         ));
         Tblbuscador.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mousePressed(java.awt.event.MouseEvent evt) {
                 TblbuscadorMousePressed(evt);
             }
         });
         jScrollPane1.setViewportView(Tblbuscador);
 
         lblcant.setBorder(javax.swing.BorderFactory.createEtchedBorder());
 
         jLabel7.setText("Total: ");
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(jScrollPane1))
                 .addContainerGap())
             .addGroup(layout.createSequentialGroup()
                 .addGap(27, 27, 27)
                 .addComponent(jLabel7)
                 .addGap(17, 17, 17)
                 .addComponent(lblcant, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(137, 137, 137)
                .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 352, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jLabel7)
                    .addComponent(lblcant, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(16, 16, 16))
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
         buscar();
     }//GEN-LAST:event_jButton1ActionPerformed
 
     private void CboEmpresaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CboEmpresaActionPerformed
         qs = new Query();
         qs.setIdentify("idempr");
         qs.loadChoiceDefault(CboSucursal,"sucursal","nombre",
             Integer.parseInt(qs.idChoice("empresa","nombre",String.valueOf(CboEmpresa.getSelectedItem()))));
     }//GEN-LAST:event_CboEmpresaActionPerformed
 
     private void CboareaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CboareaActionPerformed
         qs = new Query();
         qs.setIdentify("idare");
         qs.loadChoiceDefault(CboCargo,"cargo","nombre",
             Integer.parseInt(qs.idChoice("area","nombre",String.valueOf(Cboarea.getSelectedItem()))));
     }//GEN-LAST:event_CboareaActionPerformed
 
     private void TblbuscadorMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TblbuscadorMousePressed
         int fsel;
         fsel = this.Tblbuscador.getSelectedRow();
         DefaultTableModel m = new DefaultTableModel();
         m = (DefaultTableModel) this.Tblbuscador.getModel();
         String idEmp = String.valueOf(m.getValueAt(fsel, 0));
         
         String id = String.valueOf(m.getValueAt(fsel, 0));
         String nombre = String.valueOf(m.getValueAt(fsel, 1));
         String apellido = String.valueOf(m.getValueAt(fsel, 2));
         String area = String.valueOf(m.getValueAt(fsel, 3));
         String cargo = String.valueOf(m.getValueAt(fsel, 4));
         switch(LblModulo.getText()){
             case "1":
                 Gui.WinConsulta.TxtName.setText(nombre + " "+ apellido);
                 Gui.WinConsulta.Lblidemp.setText(id);
                 this.dispose();
                 break;
             case "2": 
                 Gui.WinJustificacion.lblNomape.setText(nombre + " "+ apellido);
                 Gui.WinJustificacion.lblID.setText(id);
                 Gui.WinJustificacion.Lblarea.setText(area);
                 Gui.WinJustificacion.lblcargo.setText(cargo);
                 File archivo = new  File("src/imagenes/"+idEmp+".jpg");
                 if(archivo.exists()){
                     String archivoimg = "src/imagenes/"+idEmp+".jpg";
                     ImageIcon imagenfoto = new ImageIcon(archivoimg);
 
                     Image iamgendimen = imagenfoto.getImage();
                      Image newimg = iamgendimen.getScaledInstance(100,100,java.awt.Image.SCALE_SMOOTH);
                      ImageIcon newIcon = new ImageIcon(newimg);
                      Gui.WinJustificacion.lblFoto.setIcon(newIcon);
                 }else{
                     ImageIcon imagenFondo = new ImageIcon(getClass().getResource("/imagenes/defecto.jpg"));
                     Image iamgendimen = imagenFondo.getImage();
                           Image newimg = iamgendimen.getScaledInstance(100,100,java.awt.Image.SCALE_SMOOTH);
                           ImageIcon newIcon = new ImageIcon(newimg);
                     Gui.WinJustificacion.lblFoto.setIcon(newIcon);
                 }
                 
                 this.dispose();
                 break;
             case "3":
                 Gui.WinReportes.TxtEmpleado.setText(nombre + " "+ apellido);
                 Gui.WinReportes.Lblidemp.setText(id);
                 this.dispose();
                 ;break;
             
         }
     }//GEN-LAST:event_TblbuscadorMousePressed
 
     private void TxtNombresKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TxtNombresKeyPressed
      if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
         buscar();
     }
     }//GEN-LAST:event_TxtNombresKeyPressed
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JComboBox CboCargo;
     private javax.swing.JComboBox CboEmpresa;
     private javax.swing.JComboBox CboEstado;
     private javax.swing.JComboBox CboSucursal;
     private javax.swing.JComboBox Cboarea;
     public javax.swing.JLabel LblModulo;
     private javax.swing.JTable Tblbuscador;
     private javax.swing.JTextField TxtNombres;
     private javax.swing.JTextField Txtapellidos;
     private javax.swing.JButton jButton1;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel3;
     private javax.swing.JLabel jLabel4;
     private javax.swing.JLabel jLabel5;
     private javax.swing.JLabel jLabel6;
     private javax.swing.JLabel jLabel7;
     private javax.swing.JLabel jLabel8;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSpinner jSpinner1;
     private javax.swing.JLabel lblcant;
     // End of variables declaration//GEN-END:variables
 }
