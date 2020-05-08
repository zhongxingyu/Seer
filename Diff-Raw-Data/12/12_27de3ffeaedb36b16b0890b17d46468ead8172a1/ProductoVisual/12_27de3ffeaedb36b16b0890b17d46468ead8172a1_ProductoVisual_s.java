 package code.google.com.opengis.gestionVISUAL;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.*;
 import javax.swing.border.TitledBorder;
 import javax.swing.table.DefaultTableModel;
 
 
 
 import code.google.com.opengis.gestion.Producto;
 import code.google.com.opengis.gestionDAO.ProductoDAO;
 
  
 public class ProductoVisual extends JInternalFrame {
 
 		private int ancho;
 		private int alto;
         private JPanel panelProducto;
         private JPanel panelProductoAlt;
         
         private JButton cmdAceptarAlt;
         private JButton cmdCancelarAlt;
         private JButton cmdCrear;
         private JButton cmdModificar;
         private JButton cmdDesactivar;
         
         private static JTable tblTabla = null;
         private static String[] columnNames = {"ID", "Nombre","Tamao","Descripcin", "Tarea", "Activo"};
         private static JScrollPane jScrollPaneTablaProductos = null;        
         static DefaultTableModel modelo = new DefaultTableModel(columnNames, 0);
 
 
 
         
         private static JTextField txtIdprod;
         private static JTextField txtNombre;
         private static JTextField txtTipo;
         private static JTextField txtBuscar;
         
         private static JTextArea txtDescripcion;
         
         private static JComboBox cmbTarea;
         private static String[] tipo = { "Arar", "Abonar", "Sembrar"};
         
         private static JCheckBox chkActivo;
         
 public ProductoVisual(int ancho, int alto){
         super("Producto",false, true, true, true);
         this.ancho = ancho;
         this.alto = alto;
         panelProducto = new JPanel ();
         panelProducto.setLayout(null);
         this.add(panelProducto);
         this.setBounds(0,0,ancho,alto);
         this.setTitle("Producto");
         this.setClosable(true);
         TitledBorder jb = new TitledBorder("Aadir / Modificar");
         panelProducto.setBorder(jb);
         double ii = ancho/1.7;
         double aa = alto/1.7;
         panelProducto.setBounds(new Rectangle(0,0,(int)ii,(int)aa));
         this.getJScrollPaneTablaUsuarios();
         principalProducto(panelProducto);
    
 }
 
 final static boolean shouldFill = true;
 final static boolean shouldWeightX = true;
 final static boolean RIGHT_TO_LEFT = false;
 
 	public void cargarAlta(){
 			
 		    panelProductoAlt = new JPanel ();
 	        panelProductoAlt.setLayout(null);
 	        panelProductoAlt.getBounds(panelProducto.getBounds());
 	        this.add(panelProductoAlt);
 	        this.setBounds(0,0,ancho,alto);
 	        this.setTitle("Producto");
 	        this.setClosable(true);
 	        TitledBorder jb2 = new TitledBorder("Aadir / Modificar");
 	        panelProductoAlt.setBorder(jb2);
 	        double ii = ancho/1.7;
 	        double aa = alto/1.7;
 	        panelProductoAlt.setOpaque(false);
 	        panelProductoAlt.setBounds(new Rectangle(0,0,(int)ii,(int)aa));
 
 	        
 	        altas(panelProductoAlt);
 	}
     public static void addComponentsToPane(Container pane) {
         
         
     }
 
 public void principalProducto(Container pane){
          if (RIGHT_TO_LEFT) {
          pane.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
      }
 
         
          cmdCrear =new JButton();
          cmdModificar= new JButton();
          cmdDesactivar= new JButton();
          tblTabla= new JTable();
      JLabel label;
      txtBuscar= new JTextField();
      
      pane.setLayout(new GridBagLayout());
      GridBagConstraints c = new GridBagConstraints();
      GridBagConstraints cTabla = new GridBagConstraints();
      if (shouldFill) {
      //natural height, maximum width
      c.fill = GridBagConstraints.HORIZONTAL;
      }
      c.insets = new Insets(8,8,8,8);
     // button = new JButton("Button 1");
      if (shouldWeightX) {
      c.weightx = 0.5;
      } 
      c.fill = GridBagConstraints.HORIZONTAL;
          c.gridx = 0;
          c.gridy = 0;
      
         label = new JLabel("Buscar");
         c.fill = GridBagConstraints.HORIZONTAL;
         c.gridx = 0;
         c.gridy = 0;
         pane.add(label,c);
         
         txtBuscar = new JTextField(10);
         c.fill = GridBagConstraints.HORIZONTAL;
         c.gridx = 1;
         c.gridy = 0;
         pane.add(txtBuscar, c);
         
         tblTabla.setModel(modelo);
         cTabla.fill = GridBagConstraints.HORIZONTAL;
         cTabla.gridx = 0;
         cTabla.gridy = 1;
         cTabla.gridwidth = 5;
         cTabla.gridheight = 5;
         pane.add(jScrollPaneTablaProductos, cTabla);
         
         cmdCrear =new JButton("Nuevo Producto");
         c.fill = GridBagConstraints.HORIZONTAL;
         c.gridx = 0;
         c.gridy = 7;
         cmdCrear.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent e) {                     
                                 panelProducto.hide();
                                 cargarAlta();
                 //abre el panel de Altas
                 }
         });
         pane.add(cmdCrear, c);
         
          cmdModificar= new JButton("Modificar Producto");
          c.fill = GridBagConstraints.HORIZONTAL;
          c.gridx = 1;
          c.gridy = 7;
          cmdModificar.addActionListener(new java.awt.event.ActionListener() {
          public void actionPerformed(java.awt.event.ActionEvent e) {                    
                                                 
                 //abre el panel de Modificar
                                 
                 }
          });
          pane.add(cmdModificar, c);
          
          cmdDesactivar= new JButton("Desactivar Producto");
          c.fill = GridBagConstraints.HORIZONTAL;
          c.gridx = 2;
          c.gridy = 7;
          cmdDesactivar.addActionListener(new java.awt.event.ActionListener() {
                 public void actionPerformed(java.awt.event.ActionEvent e) {                     
                                                 
                         //desactiva el producto 
                                 //bajaProducto();
                 }
         });
          pane.add(cmdDesactivar, c);
         
         
 }
  public void altas(Container pane){
          if (RIGHT_TO_LEFT) {
          pane.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
      }
 
      
          cmdAceptarAlt= new JButton();
          cmdCancelarAlt= new JButton();
      JLabel label;
          txtIdprod= new JTextField();
          txtNombre= new JTextField();
          txtTipo= new JTextField();
      txtDescripcion= new JTextArea();
      cmbTarea= new JComboBox();
     
                  
      
      
      chkActivo= new JCheckBox();
      
      pane.setLayout(new GridBagLayout());
      GridBagConstraints c = new GridBagConstraints();
      if (shouldFill) {
          //natural height, maximum width
          c.fill = GridBagConstraints.HORIZONTAL;
      }
      c.insets = new Insets(8,8,8,8);
         // button = new JButton("Button 1");
      if (shouldWeightX) {
          c.weightx = 0.5;
      }
      c.fill = GridBagConstraints.HORIZONTAL;
         c.gridx = 0;
         c.gridy = 0;
         //pane.add(button, c);
 
         label = new JLabel("Producto ID:");
         c.fill = GridBagConstraints.HORIZONTAL;
         c.gridx = 1;
         c.gridy = 0;
         pane.add(label,c);
 
         txtIdprod= new JTextField(10);
         c.fill = GridBagConstraints.HORIZONTAL;
         c.gridx = 2;
         c.gridy = 0;
         pane.add(txtIdprod, c);
 
         label = new JLabel("Nombre del Producto:");
         c.fill = GridBagConstraints.HORIZONTAL;
         c.gridx = 1;
         c.gridy = 1;
         pane.add(label,c);
 
         txtNombre = new JTextField();
         c.fill = GridBagConstraints.HORIZONTAL;
         c.gridx = 2;
         c.gridy = 1;
         pane.add(txtNombre, c);
 
         label = new JLabel("Tipo:");
         c.fill = GridBagConstraints.HORIZONTAL;
         c.gridx = 1;
         c.gridy = 2;
         pane.add(label,c);
 
         txtTipo = new JTextField();
         c.fill = GridBagConstraints.HORIZONTAL;
         c.gridx = 2;
         c.gridy = 2;
         pane.add(txtTipo, c);
 
         label = new JLabel("Descripcin:");
         c.fill = GridBagConstraints.HORIZONTAL;
         c.gridx = 1;
         c.gridy = 4;
         pane.add(label,c);
 
         txtDescripcion = new JTextArea("",5,10);
         c.fill = GridBagConstraints.HORIZONTAL;
         c.gridx = 2;
         c.gridy = 4;
         pane.add(txtDescripcion, c);
 
         label = new JLabel ("Tarea:");
         c.fill= GridBagConstraints.HORIZONTAL;
         c.gridx = 3;
         c.gridy= 0;
         pane.add(label,c);
  
         
         cmbTarea= new JComboBox(tipo);
         c.fill = GridBagConstraints.HORIZONTAL;
         c.gridx= 4;
         c.gridy= 0;
         pane.add(cmbTarea, c);
  
         label= new JLabel("Activo");
         c.fill=GridBagConstraints.HORIZONTAL ;
         c.gridx=3;
         c.gridy=1;
         pane.add(label,c);
  
         chkActivo =new JCheckBox ();
         c.fill=GridBagConstraints.HORIZONTAL;
         c.gridx = 4;
         c.gridy= 1;
         pane.add(chkActivo,c);
  
         cmdAceptarAlt = new JButton("Confirmar");
         c.fill = GridBagConstraints.HORIZONTAL;
         c.gridx = 1;
         c.gridy = 5;
         cmdAceptarAlt.addActionListener(new java.awt.event.ActionListener() {
                 public void actionPerformed(java.awt.event.ActionEvent e) {     
                         String cadena= (String) cmbTarea.getSelectedItem();
                         //      Producto p = new Producto(, txtNombre.getText(), txtTipo.getText(), txtDescripcion.getText(), cadena , chkActivo.get());        
                 //da de altas el producto       
                         //altaProducto();
                 }
         });
         pane.add(cmdAceptarAlt, c);
         
         cmdCancelarAlt = new JButton("Cancelar");
         c.fill = GridBagConstraints.HORIZONTAL;
         c.gridx = 2;
         c.gridy = 5;
         cmdCancelarAlt.addActionListener(new java.awt.event.ActionListener() {
                 public void actionPerformed(java.awt.event.ActionEvent e) {                     
                         panelProducto.show();
                         panelProductoAlt.hide();
                 }
         });
         pane.add(cmdCancelarAlt, c);
  }
  private JScrollPane getJScrollPaneTablaUsuarios() {
      if (jScrollPaneTablaProductos == null) {
              jScrollPaneTablaProductos = new JScrollPane();
              //jScrollPaneTablaUsuarios.setBounds(new Rectangle(9, 73, 638, 117));
              jScrollPaneTablaProductos.setViewportView(gettblTabla());
      }
      return jScrollPaneTablaProductos;
 }
  private JTable gettblTabla() {
      
      
      tblTabla = new JTable();
      tblTabla.setModel(modelo);
                      
      
      
 
 return tblTabla;
 }
 
 }
