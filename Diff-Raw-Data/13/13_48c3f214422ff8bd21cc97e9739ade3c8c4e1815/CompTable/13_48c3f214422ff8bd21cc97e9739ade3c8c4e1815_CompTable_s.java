 
 package com.component.tabla;
 
 import com.component.coneccion.Coneccion;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.util.Vector;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.table.DefaultTableModel;
 
 public class CompTable extends javax.swing.JFrame {
 
     int cantidadAPMax=20;
     int cantidadAPMin=0;
     int cantidadColumnas;
     Object[] nombres;
     Object[] fila;
     Coneccion con = new Coneccion();
     String tabla;
     Vector filas = new Vector();
     private int INDEX_TABLA;
     
     DefaultTableModel modelo=new DefaultTableModel();
     
     public CompTable(String tabla) 
     {
         this.tabla = tabla;
         
         initComponents();
         this.setLocation(500, 200);
         setVisible(false);
         setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
         ManejadorBotones botones = new ManejadorBotones() ;
         adelante.addActionListener(botones);
         regresar.addActionListener(botones);
     }
     
     
     
     public void contNameTable()
     {
         try 
         {
             con.contNameTable();
             nombres = new Object[con.getContNameTAble()];
             System.out.println(con.getContNameTAble());
         } 
         catch (Exception e) 
         {
             JOptionPane.showMessageDialog(null, e, "Error", JOptionPane.ERROR_MESSAGE);
         }
     }
     public Object[] getModeloTabla()
     {
         try 
         {
             nombres = con.setModeloTablas();
         } 
         catch (Exception e) 
         {
             JOptionPane.showMessageDialog(null, e, "Error Cargando Lista de Tablas", JOptionPane.ERROR_MESSAGE);
         }
         return nombres;
     }
     public void llenaTabla()
     {
        tabla1.setModel(modelo);
         tabla1.removeAll();
         filas.removeAllElements();
         try
         {
             ResultSetMetaData rsMd;
             ResultSet rs;
             cantidadColumnas=0;
             con.setTabla(getTabla());
             rs = con.consulta();
             rsMd=rs.getMetaData();
             cantidadColumnas= rsMd.getColumnCount();
                     for (int i=1; i<= cantidadColumnas;i++)
                     {
                         modelo.addColumn(rsMd.getColumnLabel(i));
                     }
                     
                     while (rs.next())
                     {
                         fila=new Object[cantidadColumnas];
                         for (int i=0; i< cantidadColumnas;i++)
                         {
                             fila[i]=rs.getObject(i+1);
                            
                         }
                         filas.add(fila);
                     }
                     
                     agregaFilas(0, 20);
                     rs.close();
             con.desconecta();
         }
         catch(Exception e)
         {
             JOptionPane.showMessageDialog(null, e, "Error Llenando tabla ", JOptionPane.ERROR_MESSAGE);
         }
     }
     public void setIndexTabla(int index)
     {
         this.INDEX_TABLA = index;
     }
     public int getIndexTabla()
     {
         return INDEX_TABLA;
     }
     public void setTabla(String tabla)
     {
         this.tabla = tabla;
     }
     public String getTabla() 
     {
         return this.tabla;
     } 
     public void agregaFilas(int min, int max) 
     {
         for (int j = min; j < max; j++) 
         {
             Object[] temp = (Object[]) filas.get(j);
             modelo.addRow(temp);
         }
     }
     
     public void setDatosBD(String bd,String user, String pass, String schema)
     {
         con.setDatos(bd, user, pass, schema);
     }
     
     public void limpiaTabla()
     {
         while(tabla1.getRowCount()>0)
         {
             ((DefaultTableModel)tabla1.getModel()).removeRow(0);
         }        
     }
     private class ManejadorBotones implements ActionListener
     {
         public void actionPerformed(ActionEvent botones)
         {
      
             if(botones.getSource()==adelante)
             {
                 try 
                 {
                     limpiaTabla();
                     cantidadAPMax=cantidadAPMax+20;
                     cantidadAPMin=cantidadAPMin+20;
                     agregaFilas(cantidadAPMin, cantidadAPMax);
                 } 
                 catch (Exception e) 
                 {
                     JOptionPane.showMessageDialog(null, "No hay mas datos", "Alerta!!", JOptionPane.INFORMATION_MESSAGE);
                 }
             }
             if(botones.getSource()==regresar)
             {
                 try 
                 {
                     limpiaTabla();
                     cantidadAPMax=cantidadAPMax-20;
                     cantidadAPMin=cantidadAPMin-20;
                     agregaFilas(cantidadAPMin, cantidadAPMax);
                 } 
                 catch (Exception e) 
                 {
                     JOptionPane.showMessageDialog(null, "No hay mas datos", "Alerta!!", JOptionPane.INFORMATION_MESSAGE);
                 }
             }
         }
     }
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jScrollPane1 = new javax.swing.JScrollPane();
         tabla1 = new javax.swing.JTable();
         regresar = new javax.swing.JButton();
         adelante = new javax.swing.JButton();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
 
         tabla1.setModel(modelo);
         jScrollPane1.setViewportView(tabla1);
 
         regresar.setText("Atras");
 
         adelante.setText("Adelante");
         adelante.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 adelanteActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(regresar, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addComponent(adelante, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 375, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 329, Short.MAX_VALUE)
                 .addGap(18, 18, 18)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(adelante)
                     .addComponent(regresar))
                 .addContainerGap())
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void adelanteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_adelanteActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_adelanteActionPerformed
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton adelante;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JButton regresar;
     private javax.swing.JTable tabla1;
     // End of variables declaration//GEN-END:variables
 }
