 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ve.com.fsjv.devsicodetv.controladores;
 
 import com.github.sarxos.webcam.WebcamResolution;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JDialog;
import javax.swing.JOptionPane;
 import ve.com.fsjv.devsicodetv.utilitarios.excepciones.ExcepcionCampoVacio;
 import ve.com.fsjv.devsicodetv.utilitarios.excepciones.ExcepcionComponenteNulo;
 import ve.com.fsjv.devsicodetv.utilitarios.otros.CamaraThread;
 import ve.com.fsjv.devsicodetv.utilitarios.otros.ConstantesApp;
 import ve.com.fsjv.devsicodetv.utilitarios.otros.Procesos;
 import ve.com.fsjv.devsicodetv.vistas.CapturarFotoDialog;
 
 /**
  *
  * @author Juan
  */
 public class CapturarFotoManager implements ActionListener {
 
     private CapturarFotoDialog capturarFotoDialog;
     private final Procesos procesos;
     private CamaraThread camaraThread;
     private List<BufferedImage> listaImagenes;
    private int detenido;
             
    public CapturarFotoManager (int detenido) throws ExcepcionComponenteNulo, ExcepcionCampoVacio {
         this.capturarFotoDialog = new CapturarFotoDialog(null, true);
         this.procesos = new Procesos();
        this.detenido = detenido;
        JOptionPane.showMessageDialog(this.capturarFotoDialog, "id del detenido: " + detenido);
         
         this.capturarFotoDialog.getBtnAceptar().addActionListener(this);
         this.capturarFotoDialog.getBtnCamara().addActionListener(this);
         this.capturarFotoDialog.getBtnCancelar().addActionListener(this);
         this.capturarFotoDialog.getBtnConfiguracion().addActionListener(this);
         this.capturarFotoDialog.getBtnRecapturar().addActionListener(this);
         this.capturarFotoDialog.getBtnZoomIn().addActionListener(this);
         this.capturarFotoDialog.getBtnZoomOut().addActionListener(this);
         
         this.capturarFotoDialog.setTitle(this.procesos.cargarMembreteBarraTitulo(ConstantesApp.ACRONIMO_MODULO_FICHA_DETENIDO, "Administrador", ConstantesApp.TITULO_COMPLETO));
         //this.capturarFotoDialog.setSize(1150, 740);
         this.capturarFotoDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
         
         this.camaraThread = new CamaraThread(capturarFotoDialog, 0, WebcamResolution.VGA.getSize());
         camaraThread.start();
         this.listaImagenes = new ArrayList<BufferedImage>();
         //this.capturarFotoDialog.getBtnCancelar().setVisible(false);
         activarTomarFoto();
         System.out.println("pasa por aqui");
         this.capturarFotoDialog.setVisible(true);
         
     }
     
     private void activarTomarFoto(){
         this.capturarFotoDialog.getBtnCamara().setEnabled(true);
         this.capturarFotoDialog.getBtnAceptar().setEnabled(false);
         this.capturarFotoDialog.getBtnCancelar().setEnabled(true);
         this.capturarFotoDialog.getBtnConfiguracion().setEnabled(true);
         this.capturarFotoDialog.getBtnRecapturar().setEnabled(false);
         this.capturarFotoDialog.getBtnZoomIn().setEnabled(true);
         this.capturarFotoDialog.getBtnZoomOut().setEnabled(true);
     }
     
     private void desactivarTomarFoto(){
         this.capturarFotoDialog.getBtnCamara().setEnabled(false);
         this.capturarFotoDialog.getBtnAceptar().setEnabled(true);
         this.capturarFotoDialog.getBtnCancelar().setEnabled(false);
         this.capturarFotoDialog.getBtnConfiguracion().setEnabled(true);
         this.capturarFotoDialog.getBtnRecapturar().setEnabled(true);
         this.capturarFotoDialog.getBtnZoomIn().setEnabled(false);
         this.capturarFotoDialog.getBtnZoomOut().setEnabled(false);
     }
     
     public void actionPerformed(ActionEvent e) {
         if(e.getSource()== this.capturarFotoDialog.getBtnConfiguracion()){
             
         }else if(e.getSource()== this.capturarFotoDialog.getBtnAceptar()){
             this.listaImagenes.add(this.camaraThread.getImagenCamara());
             this.camaraThread.seguirImagen();
             activarTomarFoto();
         }else if(e.getSource()== this.capturarFotoDialog.getBtnCamara()){
             this.camaraThread.detenerImagen();
             desactivarTomarFoto();
         }else if(e.getSource()== this.capturarFotoDialog.getBtnCancelar()){
             try {
                 this.camaraThread.detenerImagen();
                 new ListaFotosManager(listaImagenes);
                 //new GaleriaFotosManager(listaImagenes);
                 this.camaraThread.seguirImagen();
             } catch (Exception ex) {
                 ex.printStackTrace();
             }
         }else if(e.getSource()== this.capturarFotoDialog.getBtnRecapturar()){
             this.camaraThread.seguirImagen();
             activarTomarFoto();
         }else if(e.getSource()== this.capturarFotoDialog.getBtnZoomIn()){
             this.camaraThread.zoomIn();
         }else if(e.getSource()== this.capturarFotoDialog.getBtnZoomOut()){
             this.camaraThread.zoomOut();
         }
         //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
     
 }
