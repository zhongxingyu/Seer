 package ve.com.fsjv.devsicodetv.controladores;
 
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import ve.com.fsjv.devsicodetv.vistas.vFichaDetenido;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
 import java.util.logging.Level;
 import java.util.logging.Logger;
import javax.imageio.ImageIO;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
import javax.swing.JOptionPane;
 import ve.com.fsjv.devsicodetv.utilitarios.excepciones.ExcepcionCampoVacio;
 import ve.com.fsjv.devsicodetv.utilitarios.excepciones.ExcepcionComponenteNulo;
import ve.com.fsjv.devsicodetv.utilitarios.otros.Procesos;
 import ve.com.fsjv.devsicodetv.utilitarios.otros.ConstantesApp;
 
 /**
  *
  * @author franklin
  */
 public class cFichaDetenido implements ActionListener, KeyListener {
     private vFichaDetenido formulario;
     private Procesos procesos;
     
     public cFichaDetenido() throws ExcepcionComponenteNulo, ExcepcionCampoVacio {
         this.formulario = new vFichaDetenido(new JFrame(), true);
         this.procesos = new Procesos();
         this.formulario = (vFichaDetenido) this.procesos.aplicarReadOnly(ConstantesApp.ACRONIMO_MODULO_FICHA_DETENIDO, false, ConstantesApp.READONLY_DEFAULT);
         this.formulario.txtCedulaIdentidad.setEditable(true);
         this.formulario.cmbNacionalidad.setEnabled(true);
         this.formulario.txtCedulaIdentidad.addKeyListener(this);
         this.formulario.cmbNacionalidad.addKeyListener(this);
         this.formulario.setSize(1150, 740);
         this.formulario.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
         this.formulario.setVisible(true);
         
         
     }
     
     public void actionPerformed(ActionEvent e) {
         
     }
 
     public void keyTyped(KeyEvent e) {
         
     }
 
     public void keyPressed(KeyEvent e) {
         
     }
     
     public void keyReleased(KeyEvent e) {
         if(this.procesos != null){
             this.procesos.deshabilitarTeclas(e);
         }else{
             try {
                 throw new ExcepcionComponenteNulo(ConstantesApp.MENSAJE_CLASE_PROCESOS_NULA);
             } catch (ExcepcionComponenteNulo ex) {
                 Logger.getLogger(cFichaDetenido.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
     }
     
 }
