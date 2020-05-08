 package ve.com.fsjv.devsicodetv;
 
 import ve.com.fsjv.devsicodetv.utilitarios.otros.Procesos;
 import org.hibernate.Session;
 import org.hibernate.HibernateException;
 import ve.com.fsjv.devsicodetv.utilitarios.conexion.ConexionSicodetUtil;
 import ve.com.fsjv.devsicodetv.utilitarios.conexion.ConexionSeguridadUtil;
 import ve.com.fsjv.devsicodetv.utilitarios.conexion.ConexionPersonalUtil;
 /**
  * Hello world!
  *
  */
 public class App 
 {
     public static void main( String[] args )
     {
         
         Session sesion = ConexionSicodetUtil.getSessionFactory().openSession();
         try{
             sesion.getTransaction().begin();
             System.out.println("Session: " + sesion.getTransaction().toString());
         }catch(HibernateException excep){
             System.err.println("Exception: " + excep);
         }
         
         Session sesion2 = ConexionSeguridadUtil.getSessionFactory().openSession();
         
         try{
             sesion2.getTransaction().begin();
             System.out.println("Session2: " + sesion2.getTransaction().toString());
         }catch(HibernateException excep){
             System.err.println("Exception: " + excep);
         }
         
         Session sesion3 = ConexionPersonalUtil.getSessionFactory().openSession();
         
         try{
             sesion3.getTransaction().begin();
             System.out.println("Session3: " + sesion3.getTransaction().toString());
         }catch(HibernateException excep){
             System.err.println("Exception: " + excep);
         }
         
        Procesos p = new Procesos();
        String codigo = p.generarCodigo("DET", "detenido", "15242874", 1);
        
        System.out.println("Nuevo codigo o identificador: " + codigo);
        
        
         
     }
 }
