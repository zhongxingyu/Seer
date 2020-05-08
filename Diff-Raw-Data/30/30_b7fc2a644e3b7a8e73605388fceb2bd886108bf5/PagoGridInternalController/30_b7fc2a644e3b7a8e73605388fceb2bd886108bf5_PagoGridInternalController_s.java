 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.jswitch.pagos.controlador;
 
 import com.jswitch.base.controlador.logger.LoggerUtil;
 import com.jswitch.base.controlador.util.DefaultGridInternalController;
 import com.jswitch.base.modelo.util.bean.BeanVO;
 import com.jswitch.pagos.vista.PagoDetailFrame;
 import com.jswitch.siniestros.modelo.maestra.DetalleSiniestro;
 import org.openswing.swing.message.receive.java.ValueObject;
 
 /**
  *
  * @author Adrian
  */
 public class PagoGridInternalController extends DefaultGridInternalController {
 
    public PagoGridInternalController() {
             try {
             t = Class.forName(DetalleSiniestro.class.getName());
             getMethod = t.getMethod("getPagos", new Class[0]);
         } catch (Exception ex) {
             LoggerUtil.error(this.getClass(), "new", ex);
         }
     
     }
 
     @Override
     public void doubleClick(int rowNumber, ValueObject persistentObject) {
         new PagoDetailFrameController(PagoDetailFrame.class.getName(), miGrid, (BeanVO) persistentObject, Boolean.TRUE);
     }
}
