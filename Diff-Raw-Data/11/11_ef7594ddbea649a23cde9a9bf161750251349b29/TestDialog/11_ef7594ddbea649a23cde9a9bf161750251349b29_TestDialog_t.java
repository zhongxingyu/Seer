 /*
  * TestDialog.java
  *
  * Created on 17/01/2012, 10:45:43 PM
  */
 package interfaz;
 
 import java.awt.Color;
import java.awt.Point;
 import java.awt.Rectangle;
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JOptionPane;
 import logica.Datadb;
 import logica.Escala;
 import logica.Item;
 import logica.Usuario;
 
 /**
  *
  * @author edward
  */
 public class TestDialog extends javax.swing.JDialog {
     private Datadb db;
     private int nescala;
     private Usuario user;
     
     private Escala escala; //la escala que se muestra actual
     private List<ItemPanel> listItemsPanels;
     
     private Color verde = new java.awt.Color(230, 249, 232);
     private Color naranja = Color.ORANGE;
     
     public TestDialog(java.awt.Frame parent, boolean modal, Usuario user) {
         super(parent, modal);
         initComponents();
         this.user = user;
         initOtherComponents();
     }
     
     private void initOtherComponents(){
         listItemsPanels = new ArrayList();
         nescala = user.lastEscala()+1;
         escala = null;
         lblTituloEscala.setVisible(false);
         this.lblAvance.setText( nescala + "/" + Escala.TOTAL_ESCALAS);
         this.setTitle("Test de inteligencia emocional");
         try {
             db = new Datadb();
         } catch (SQLException ex) {
             //Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
         } catch (ClassNotFoundException ex) {
             //Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
         }
         jScrollPane1.getVerticalScrollBar().setUnitIncrement(50);
         cargarEscala(nescala);
     }
     
     /**
      * TRUE: si todos los items son seleccionados
      * Los ITEMs faltantes se resaltan a naranja
      * @return 
      */
     
     private boolean validarIngresoItems(){
         boolean correcto = true;
         
         for(ItemPanel pnl: listItemsPanels){
             if( pnl.indicadorSelected() == -1){
                 correcto = false;
                 pnl.setColor(naranja);
             }
         }
         
         return correcto;
     }
     
     /**
      * TRUE si es la ultima escala
      * @return 
      */
     
     private boolean siguienteEscala(){
         boolean isLast = false;
         if(validarIngresoItems()){
             nescala++;
             if( nescala > Escala.TOTAL_ESCALAS){
                 btnGuardar.setText("Guardar");
 
                 guardarDatosEscala();
                 isLast= true;
             }else{
                 guardarDatosEscala();
                 cargarEscala(nescala);
             }
         }else{
             JOptionPane.showMessageDialog
                     (this, 
                     "Debe responder todos los items.", 
                     App.NAME, JOptionPane.INFORMATION_MESSAGE);
         }
         return isLast;
     }
     
     private void mostrarResultados(){
         try {
             String[] nameEscalas = db.nombresEscalas();
             int[] puntajes = user.puntajes();
             int[] max = user.maxPuntajes();
             
             String message = "<html><b><font size=13>Resultados:</font></b><br>";
             int limit = puntajes.length;
             for(int i = 0; i < limit; i++){
                 message += "<b>" + nameEscalas[i] + "</b>";
                 message += ":" + String.valueOf( puntajes[i] );
                 message += "/" + String.valueOf( max[i] );
                 message += "<br>";
             }
             message += "</html>";
             
             JOptionPane.showMessageDialog
                     (this, message, App.NAME, JOptionPane.INFORMATION_MESSAGE);
             
         } catch (SQLException ex) {
             JOptionPane.showMessageDialog
                     (this, 
                     "No se puede mostrar los resultados", 
                     App.NAME, JOptionPane.ERROR_MESSAGE);
         }
     }
     
     /**
      * Guarda en un fichero en el directorio
      * del programa
      */
     
     private void guardarDatosEscala(){
         int limit = listItemsPanels.size();
         List<Item> listItems = escala.getListItems();
         
         for(int i = 0; i < limit; i++){
             int idEscala = escala.getIdEscala();
             int itemSelected = listItemsPanels.get(i).indicadorSelected();
             int value = listItems.get(i).getValorIndicador()[itemSelected];
             int idItem = listItems.get(i).getId();
             
             
             user.addData(idEscala, idItem, value, itemSelected);
         }
         
         try {
             user.save();
         } catch (IOException ex) {
             JOptionPane.showMessageDialog
                     (this, "Error al guardar archivo.\nDetalles:\n" + 
                     ex.getMessage(), App.NAME, JOptionPane.ERROR_MESSAGE);
         }
     }
 
     private boolean cargarEscala(int idEscala){
         boolean existe = true;
         try {
             escala = db.findEscalaById(idEscala);
             if( escala == null){
                 existe = false;
             }else{
                 txtaDescripcionEscala.setText( escala.getDescripcion() );
                 lblTituloEscala.setText(escala.getNombre());
 
                 //limpiar paneles
                 listItemsPanels.clear();
                 pnlItems.removeAll();
 
                 int i = 0;
                 for(Item item: escala.getListItems()){
                     ItemPanel pnl = new ItemPanel(item, escala.getIndicadores());
                     pnl.setOpaque(true);
                     if( i%2 == 0){
                         pnl.setColor(verde);
                         pnl.setDefaultColor(verde);
                     }else{
                         pnl.setColor(Color.WHITE);
                         pnl.setDefaultColor(Color.WHITE);
                     }
                     listItemsPanels.add(pnl);
                     pnlItems.add(pnl);
                     i++;
                 }   
                 this.lblAvance.setText( nescala + "/" + Escala.TOTAL_ESCALAS);
                 pnlItems.updateUI();    
                 
             }
         } catch (SQLException ex) {
             existe = false;
             System.out.println( ex.getMessage() );
         }
         return existe;
     }
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         pnlBak = new javax.swing.JPanel();
         norPanel = new javax.swing.JPanel();
         lblTituloEscala = new javax.swing.JLabel();
         txtaDescripcionEscala = new javax.swing.JTextArea();
         surPanel = new javax.swing.JPanel();
         lblAvance = new javax.swing.JLabel();
         btnGuardar = new javax.swing.JButton();
         jScrollPane1 = new javax.swing.JScrollPane();
         pnlItems = new javax.swing.JPanel();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
 
         pnlBak.setLayout(new java.awt.BorderLayout());
 
         norPanel.setPreferredSize(new java.awt.Dimension(240, 70));
         norPanel.setLayout(new java.awt.BorderLayout());
 
         lblTituloEscala.setFont(new java.awt.Font("Ubuntu", 1, 20)); // NOI18N
         lblTituloEscala.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         lblTituloEscala.setText("Título escala");
         norPanel.add(lblTituloEscala, java.awt.BorderLayout.NORTH);
 
         txtaDescripcionEscala.setColumns(20);
         txtaDescripcionEscala.setEditable(false);
         txtaDescripcionEscala.setLineWrap(true);
         txtaDescripcionEscala.setRows(5);
         txtaDescripcionEscala.setWrapStyleWord(true);
         txtaDescripcionEscala.setPreferredSize(new java.awt.Dimension(240, 40));
         norPanel.add(txtaDescripcionEscala, java.awt.BorderLayout.CENTER);
 
         pnlBak.add(norPanel, java.awt.BorderLayout.NORTH);
 
         surPanel.setLayout(new java.awt.BorderLayout());
 
         lblAvance.setText("jLabel1");
         surPanel.add(lblAvance, java.awt.BorderLayout.WEST);
 
         btnGuardar.setText("Siguiente");
         btnGuardar.setPreferredSize(new java.awt.Dimension(102, 32));
         btnGuardar.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnGuardarActionPerformed(evt);
             }
         });
         surPanel.add(btnGuardar, java.awt.BorderLayout.CENTER);
 
         pnlBak.add(surPanel, java.awt.BorderLayout.SOUTH);
 
         pnlItems.setLayout(new javax.swing.BoxLayout(pnlItems, javax.swing.BoxLayout.Y_AXIS));
         jScrollPane1.setViewportView(pnlItems);
 
         pnlBak.add(jScrollPane1, java.awt.BorderLayout.CENTER);
 
         getContentPane().add(pnlBak, java.awt.BorderLayout.CENTER);
 
         java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
         setBounds((screenSize.width-768)/2, (screenSize.height-645)/2, 768, 645);
     }// </editor-fold>//GEN-END:initComponents
 
     private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
         if( siguienteEscala() ){
             JOptionPane.showMessageDialog
                     (this,"Ha terminado el test. "
                     + "Ahora ya es posible exportarlo.",
                     App.NAME, JOptionPane.INFORMATION_MESSAGE);
             mostrarResultados();
             dispose();
         }
     }//GEN-LAST:event_btnGuardarActionPerformed
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btnGuardar;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JLabel lblAvance;
     private javax.swing.JLabel lblTituloEscala;
     private javax.swing.JPanel norPanel;
     private javax.swing.JPanel pnlBak;
     private javax.swing.JPanel pnlItems;
     private javax.swing.JPanel surPanel;
     private javax.swing.JTextArea txtaDescripcionEscala;
     // End of variables declaration//GEN-END:variables
 }
