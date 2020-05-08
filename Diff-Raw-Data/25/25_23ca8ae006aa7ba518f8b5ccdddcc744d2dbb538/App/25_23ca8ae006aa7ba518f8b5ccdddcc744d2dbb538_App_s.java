 package ve.com.fsjv.devsicodetv;
 
 import javax.swing.UnsupportedLookAndFeelException;
 import ve.com.fsjv.devsicodetv.utilitarios.otros.Procesos;
 //import ve.com.fsjv.devsicodetv.utilitarios.otros.ConstantesApp;
 import org.hibernate.Session;
 import org.hibernate.HibernateException;
 import ve.com.fsjv.devsicodetv.utilitarios.conexion.ConexionSicodetUtil;
 import ve.com.fsjv.devsicodetv.utilitarios.conexion.ConexionSeguridadUtil;
 import ve.com.fsjv.devsicodetv.utilitarios.conexion.ConexionPersonalUtil;
 import ve.com.fsjv.devsicodetv.controladores.MenuDialogManager;
 import ve.com.fsjv.devsicodetv.utilitarios.otros.Procesos;
 import ve.com.fsjv.devsicodetv.modelos.*;
 import ve.com.fsjv.devsicodetv.DAO.*;
 import ve.com.fsjv.devsicodetv.utilitarios.otros.ConstantesApp;
 
 /**
  * Hello world!
  *
  */
 public class App 
 {
     public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
     {
        /*
        for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }*/
        
         MenuDialogManager app = new MenuDialogManager();
         
     }
 }
  
