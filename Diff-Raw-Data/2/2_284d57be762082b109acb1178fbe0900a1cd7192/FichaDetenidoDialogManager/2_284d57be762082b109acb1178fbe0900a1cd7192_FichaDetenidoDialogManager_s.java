 package ve.com.fsjv.devsicodetv.controladores;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import ve.com.fsjv.devsicodetv.controladores.eventos.FichaDetenidoDialogEvents;
 import ve.com.fsjv.devsicodetv.controladores.eventos.FichaDetenidoDialogValidates;
 import ve.com.fsjv.devsicodetv.controladores.eventos.FichaDetenidoDialogMethods;
 import ve.com.fsjv.devsicodetv.utilitarios.excepciones.ExcepcionCampoVacio;
 import ve.com.fsjv.devsicodetv.utilitarios.excepciones.ExcepcionComponenteNulo;
 import ve.com.fsjv.devsicodetv.utilitarios.otros.ConstantesApp;
 import ve.com.fsjv.devsicodetv.utilitarios.otros.Procesos;
 import ve.com.fsjv.devsicodetv.vistas.FichaDetenidoDialog;
 
 /**
  *
  * @author franklin
  */
 public class FichaDetenidoDialogManager implements ActionListener, KeyListener {
     private FichaDetenidoDialog formulario;
     private Procesos procesos;
     private Integer codigo;
     private FichaDetenidoDialogMethods metodos;
     private FichaDetenidoDialogEvents eventos;
     private FichaDetenidoDialogValidates validaciones;
     
     public FichaDetenidoDialogManager() throws ExcepcionComponenteNulo, ExcepcionCampoVacio, InterruptedException, InvocationTargetException {
         this.formulario = new FichaDetenidoDialog(new JFrame(), true);
         this.procesos = new Procesos();
         this.eventos = new FichaDetenidoDialogEvents(this.formulario, this.procesos);
         this.validaciones = new FichaDetenidoDialogValidates(this.formulario, this.procesos);
         this.metodos = new FichaDetenidoDialogMethods(this.formulario, this.procesos);
         this.eventos.iniciarBotones();
         this.formulario.setTitle(this.procesos.cargarMembreteBarraTitulo(ConstantesApp.ACRONIMO_MODULO_FICHA_DETENIDO, "Administrador", ConstantesApp.TITULO_COMPLETO));
         this.formulario.setSize(1180, 740);
         this.formulario.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
         this.procesos.limpiarClipboard();
         this.formulario.setCmbAnio(this.procesos.llenarListaAnios(this.formulario.getCmbAnio()));
         this.formulario.setCmbMes(this.procesos.llenarListaMeses(this.formulario.getCmbMes()));
         this.formulario.getTxtCedulaIdentidad().addKeyListener(this);
         this.formulario.getCmbReservista().addActionListener(this);
         this.formulario.getCmbAnio().addActionListener(this);
         this.formulario.getCmbMes().addActionListener(this);
         this.formulario.getCmbDia().addActionListener(this);
         
         this.formulario.getBtnNuevo().addActionListener(this);
         this.formulario.getBtnCancelar().addActionListener(this);
         this.formulario.getBtnGuardar().addActionListener(this);
         this.formulario.getBtnBuscar().addActionListener(this);
         this.formulario.setVisible(true);         
     }
     
     public void actionPerformed(ActionEvent e) {
         if(e.getSource() == this.formulario.getBtnNuevo()){
             try {
                 this.eventos.eventoGuardar(ConstantesApp.READONLY_DEFAULT);
                 this.codigo = this.procesos.generarCodigo(ConstantesApp.ACRONIMO_MODULO_FICHA_DETENIDO, ConstantesApp.ACRONIMO_INDICE_MODULO_FICHA_DETENIDO, ConstantesApp.BANDERA_CODIGO_INTERNO);
                 this.formulario.getTxtCodigoInterno().setText(this.codigo.toString());
             } catch (ExcepcionComponenteNulo ex) { }
         }else if(e.getSource() == this.formulario.getBtnBuscar()){
             try {
                 BusquedaDetenidosDialogManager busqueda = new BusquedaDetenidosDialogManager();
             } catch (ExcepcionCampoVacio ex) { }
         }else if(e.getSource() == this.formulario.getBtnCancelar()){
             this.eventos.eventoCancelar();
         }else if(e.getSource() == this.formulario.getBtnGuardar()){
             ArrayList<String> listaErrores = this.validaciones.validarCampos();
             String mensaje = ConstantesApp.VALIDACION_EXITOSA;
             String tituloMensaje = ConstantesApp.TITULO_VALIDACION;
             int iconoMensaje = JOptionPane.INFORMATION_MESSAGE;
             if(listaErrores.size() > 0){
                 iconoMensaje = JOptionPane.ERROR_MESSAGE;
                 mensaje = ConstantesApp.VALIDACION_ERROR + "\n";
                 for(int i = 0; i < listaErrores.size(); i++){
                     mensaje = mensaje + "     - " + listaErrores.get(i) + "\n";
                 }
                 JOptionPane.showMessageDialog(this.formulario, mensaje, tituloMensaje, iconoMensaje);
             }else{
                 JOptionPane.showMessageDialog(this.formulario, mensaje, tituloMensaje, iconoMensaje);
                 int respuesta = JOptionPane.showConfirmDialog(formulario, "Desea guardar los cambios realizados?", tituloMensaje, iconoMensaje);
                 if(respuesta == JOptionPane.YES_OPTION){
                     boolean resultado = this.metodos.guardar(this.codigo);
                     if(resultado){
                         JOptionPane.showMessageDialog(this.formulario, "Datos guardados con exito!", "Mensaje", JOptionPane.INFORMATION_MESSAGE);
                         this.formulario.getBtnAgregarCausa().setEnabled(ConstantesApp.EDITABLE);
                     }
                 }else if(respuesta == JOptionPane.CANCEL_OPTION){
                     this.eventos.eventoCancelar();
                 }
             }
         }else if(e.getSource() == this.formulario.getCmbReservista()){
             this.formulario.getTxtAnio().setText(ConstantesApp.INICIALIZAR_STRING);
             if(this.formulario.getCmbReservista().getSelectedItem().toString().toLowerCase().equals("si")){
                 this.formulario.getTxtAnio().setEditable(ConstantesApp.EDITABLE);
             }else{
                 this.formulario.getTxtAnio().setEditable(ConstantesApp.NO_EDITABLE);
             }
         }else if(e.getSource() == this.formulario.getCmbAnio()){
             if(this.formulario.getCmbAnio().getSelectedIndex() > 0){
                 this.formulario.getTxtEdad().setText(ConstantesApp.INICIALIZAR_STRING);
                 this.formulario.getCmbMes().setSelectedIndex(0);
                 this.formulario.getCmbMes().setEnabled(ConstantesApp.EDITABLE);
                 this.formulario.getCmbDia().setSelectedIndex(0);
                 this.formulario.getCmbDia().setEnabled(ConstantesApp.NO_EDITABLE);
             }else{
                 this.formulario.getTxtEdad().setText(ConstantesApp.INICIALIZAR_STRING);
                 this.formulario.getCmbMes().setSelectedIndex(0);
                 this.formulario.getCmbMes().setEnabled(ConstantesApp.NO_EDITABLE);
                 this.formulario.getCmbDia().setSelectedIndex(0);
                 this.formulario.getCmbDia().setEnabled(ConstantesApp.NO_EDITABLE);
             }
         }else if(e.getSource() == this.formulario.getCmbMes()){
             if(this.formulario.getCmbMes().getSelectedIndex() > 0){
                 this.formulario.getTxtEdad().setText(ConstantesApp.INICIALIZAR_STRING);
                 this.formulario.getCmbDia().removeAllItems();
                 this.formulario.getCmbDia().addItem("Dia");
                 this.formulario.setCmbDia(this.procesos.llenarDias(
                         this.formulario.getCmbDia(), 
                         Integer.parseInt(this.formulario.getCmbMes().getSelectedItem().toString()), 
                         Integer.parseInt(this.formulario.getCmbAnio().getSelectedItem().toString())));
                 this.formulario.getCmbDia().setSelectedIndex(0);
                 this.formulario.getCmbDia().setEnabled(ConstantesApp.EDITABLE);
             }else{
                 this.formulario.getTxtEdad().setText(ConstantesApp.INICIALIZAR_STRING);
                 this.formulario.getCmbDia().setSelectedIndex(0);
                 this.formulario.getCmbDia().setEnabled(ConstantesApp.NO_EDITABLE);
             }
         }else if(e.getSource() == this.formulario.getCmbDia()){
             if(this.formulario.getCmbDia().getSelectedIndex() > 0){
                 String fecha = this.formulario.getCmbDia().getSelectedItem().toString() + "/" + this.formulario.getCmbMes().getSelectedItem().toString() + "/" + this.formulario.getCmbAnio().getSelectedItem().toString();
                 int edad = this.procesos.calcularTiempoAnios(fecha);
                 this.formulario.getTxtEdad().setText(String.valueOf(edad));
             }else{
                 this.formulario.getTxtEdad().setText(ConstantesApp.INICIALIZAR_STRING);
             }
         }
     }
     public void keyTyped(KeyEvent e) {
         if(e.getSource() == this.formulario.getTxtCedulaIdentidad()){
             this.procesos.permitirSoloNumeros(e);
         }
     }
 
     public void keyPressed(KeyEvent e) {
     }
 
     public void keyReleased(KeyEvent e) {
     }
     
 }
