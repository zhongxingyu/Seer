 package dao;
 
 /**
  *
  * @author Franco
  */
 import com.opensymphony.xwork2.ActionContext;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import model.Relaciones;
 import model.Twits;
 import model.Usuarios;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.Transaction;
 
 public class ApiDao {
 
                 	
 	public static boolean CheckKey(String key) {
                 	
 		 try {
 			SessionFactory sf = HibernateUtil.getSessionFactory();
                         Transaction t = null;
                         Session s = sf.openSession();
 			t = s.beginTransaction(); // start a new transaction
                         
 			 Query query = s.createQuery("FROM Usuarios t where t.apikey = :key");
      
                         
                            
                          query.setParameter("key", key);            
                          
                         if(query.list().size() >= 1) {
                             s.disconnect();
                             return true;
                         }
                         else {
                             s.disconnect();
                             return false;
                         }
 
 		
 		 	
 
 		
 		} catch (Exception ex) {
 			System.err.println("Error !-->" + ex.getMessage());
 			
 			return false;
 		}
         }
         
         public static boolean CheckAplication(String aplication) {
                 	
 		 try {
 			SessionFactory sf = HibernateUtil.getSessionFactory();
                         Transaction t = null;
                         Session s = sf.openSession();
 			t = s.beginTransaction(); // start a new transaction
                         
 			 Query query = s.createQuery("FROM Aplicaciones where apikey = :aplication");
   
                          query.setParameter("aplication", aplication);            
                          
                         if(query.list().size() >= 1) {
                             s.disconnect();
                             return true;
                         }
                         else {
                             s.disconnect();
                             return false;
                         }
 
 		
 		 	
 
 		
 		} catch (Exception ex) {
 			System.err.println("Error !-->" + ex.getMessage());
 			
 			return false;
 		}
         }
 
         
   public static boolean Twit(String key, String twit) {
                 List<Usuarios> listaUsuarios = null;
 		SessionFactory sf = HibernateUtil.getSessionFactory();
 		Transaction t = null;
                 Session s = sf.openSession();
 		
                 Query query = s.createQuery("FROM Usuarios u where u.apikey = :key");
                 query.setParameter("key", key);
             
 
             listaUsuarios = query.list();
             
             if(listaUsuarios.isEmpty()) {
                 s.disconnect();
                 return false;
             }
             else {
                 
                 try {
                     Twits mensaje = new Twits();                    
                         mensaje.setIdu(listaUsuarios.get(0).getIdu());
                         mensaje.setString(twit);
                         
                  Calendar cal = Calendar.getInstance();
                 SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                 mensaje.setTimestam(sdf.format(cal.getTime()));
                                                
 			t = s.beginTransaction(); // start a new transaction
 			s.persist(mensaje);
 			t.commit(); // commit transaction
 			s.disconnect();
                         return true;
 		} catch (Exception ex) {
 			System.err.println("Error -->" + ex.getMessage());
 			if (t != null)
 				t.rollback();
 			return false;
 		}
             }
 	}
    public static List<Twits> Timeline(String key, String offset) {
                 List<Usuarios> listaUsuarios = null;
                 List<Twits> listaTwits = null;
 		SessionFactory sf = HibernateUtil.getSessionFactory();
 		Transaction t = null;
                 Session s = sf.openSession();
 		
                 Query query = s.createQuery("FROM Usuarios u where u.apikey = :key");
                 
                 query.setParameter("key", key);
             
 
             listaUsuarios = query.list();
             
             if(listaUsuarios.isEmpty()) {
                s.disconnect();
                 return null;
             }
             else {
                 
                 try {
                 
                                                
 			t = s.beginTransaction(); // start a new transaction
 			Query query2 = s.createQuery("FROM Twits where idu = :idu or idu IN (select siguiendo from Relaciones where idusuario = :idu) order by timestam desc");
                         query2.setParameter("idu", listaUsuarios.get(0).getIdu());
                         listaTwits = query2.list();
 			t.commit(); // commit transaction
 			s.disconnect();
                         return listaTwits;
 		} catch (Exception ex) {
 			System.err.println("Error -->" + ex.getMessage());
 			if (t != null) {
                         t.rollback();
                     }
 			return null;
 		}
             }
 	}
 
     public static List<Usuarios> Friends(String key, String type) {
                 List<Usuarios> listaUsuarios = null;
 		SessionFactory sf = HibernateUtil.getSessionFactory();
                 Session s = sf.openSession();
 		
                 Query query = s.createQuery("FROM Usuarios u where u.apikey = :key");
                 query.setParameter("key", key);
             
 
             listaUsuarios = query.list();
             
             if(listaUsuarios.isEmpty()) {
                 s.disconnect();
                 return null;
             }
             else {
                 
                 try {
                     if(type.equals("1")) {
                        return hideDatos(relacionesDao.getFollowers(listaUsuarios.get(0).getIdu()));
                     }
                     if(type.equals("2")) {
                        return hideDatos(relacionesDao.getFollowings(listaUsuarios.get(0).getIdu()));
 
                     }
                     else {
                      return null;   
                     }
 		} catch (Exception ex) {
 			System.err.println("Error -->" + ex.getMessage());
                         return null;
 		}
             }
 	}
 
     
     public static boolean Follow(String key, String siguiendo) {
                 List<Usuarios> listaUsuarios = null;
 		SessionFactory sf = HibernateUtil.getSessionFactory();
 		Transaction t = null;
                 Session s = sf.openSession();
 		
                 Query query = s.createQuery("FROM Usuarios u where u.apikey = :key");
                 query.setParameter("key", key);
             
 
             listaUsuarios = query.list();
             
             if(listaUsuarios.isEmpty()) {
                 return false;
             }
             else {
                 
                 try {            
                       relacionesDao.follow(listaUsuarios.get(0).getIdu().longValue(),PerfilDao.traerPerfilNombre(siguiendo).getIdu());                        
 			
 			return true;
 		} catch (Exception ex) {
 			System.err.println("Errorfollow -->" + ex.getMessage());
 			if (t != null)
 				t.rollback();
 			return false;
 		}
             }
 	}
     
     public static boolean Unfollow(String key, String siguiendo) {
                 List<Usuarios> listaUsuarios = null;
 		SessionFactory sf = HibernateUtil.getSessionFactory();
 		Transaction t = null;
                 Session s = sf.openSession();
 		
                 Query query = s.createQuery("FROM Usuarios u where u.apikey = :key");
                 query.setParameter("key", key);
             
 
             listaUsuarios = query.list();
             s.close();
             if(listaUsuarios.isEmpty()) {
                 return false;
             }
             else {
 
                 try {
                 relacionesDao.unfollow(listaUsuarios.get(0).getIdu().longValue(),PerfilDao.traerPerfilNombre(siguiendo).getIdu());                        
 			return true;
 		} catch (Exception ex) {
 			System.err.println("Errorunfollow -->" + ex.getMessage());
 			if (t != null)
 				t.rollback();
 			return false;
 		}
             }
 	}

    public static List<Usuarios> hideDatos(List<Usuarios> listau) {
        for(Usuarios u : listau) {
	            u.setApikey("hidden");
                    u.setFlag1("hidden");
                    u.setFlag2("hidden");
                    u.setPass("hidden");
	        }
        return listau;
    }
 }
