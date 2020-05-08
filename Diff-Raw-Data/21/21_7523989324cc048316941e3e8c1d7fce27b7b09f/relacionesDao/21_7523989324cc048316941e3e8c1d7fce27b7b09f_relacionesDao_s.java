 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package dao;
 
 
 import model.Relaciones;
 
 import com.opensymphony.xwork2.ActionContext;
 import com.sun.corba.se.impl.protocol.giopmsgheaders.Message;
 import java.net.PasswordAuthentication;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.Properties;
 import model.Usuarios;
 
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.Transaction;
 import view.SendMail;
 
 /**
  *
  * @author Franco
  */
 public class relacionesDao {
        public static int countFollowers(Long idu) {
                 	
 		 try {
 			SessionFactory sf = HibernateUtil.getSessionFactory();
                         Transaction t = null;
                         Session s = sf.openSession();
 			t = s.beginTransaction(); // start a new transaction
                         
 			 Query query = s.createQuery("FROM Relaciones where siguiendo = :idu");
                          query.setParameter("idu", idu.toString());            
                          
                         return query.list().size();
 
 		
 		} catch (Exception ex) {
 			System.err.println("Error !-->" + ex.getMessage());
 			
 			return 0;
 		}
   }
     public static int countFollowing(Long idu) {
                 	
 		 try {
 			SessionFactory sf = HibernateUtil.getSessionFactory();
                         Transaction t = null;
                         Session s = sf.openSession();
 			t = s.beginTransaction(); // start a new transaction
                         
 			 Query query = s.createQuery("FROM Relaciones where idusuario = :idu");
                          query.setLong("idu", idu);
                          s.disconnect();
                         return query.list().size();
 
 		
 		} catch (Exception ex) {
 			System.err.println("Error !!-->" + ex.getMessage());
 			
 			return 0;
 		}
   }
     
      public static int countTwits(Long idu) {
                 	
 		 try {
 			SessionFactory sf = HibernateUtil.getSessionFactory();
                         Transaction t = null;
                         Session s = sf.openSession();
 			t = s.beginTransaction(); // start a new transaction
                         
 			 Query query = s.createQuery("FROM Twits where idu = :idu");
                          query.setLong("idu", idu);
                          s.disconnect();
                         return query.list().size();
 
 		
 		} catch (Exception ex) {
 			System.err.println("Error !!-->" + ex.getMessage());
 			
 			return 0;
 		}
   }
 
      public static int checkRelacion(Long idu, Long siguiendo) {
 		 try {
 			SessionFactory sf = HibernateUtil.getSessionFactory();
                         Transaction t = null;
                         Session s = sf.openSession();
 			t = s.beginTransaction(); // start a new transaction
                        
 			 Query query = s.createQuery("FROM Relaciones where idusuario = :idu and siguiendo = :siguiendo");
                         query.setLong("idu", idu);
                         query.setParameter("siguiendo", siguiendo.toString());
                                                  s.disconnect();
 
                         if(query.list().size() > 0) {
                             return 1;
                         }
                         else {
                             return 0;
                         }
 
 		
 		} catch (Exception ex) {
 			System.err.println("Error :o-->" + ex.getMessage());
 			
 			return -1;
 		}
   }
      public static int unfollow(Long idu, Long siguiendo) {
                 	
                         Relaciones rel = new Relaciones();
 			SessionFactory sf = HibernateUtil.getSessionFactory();
                         Session s = sf.openSession();
                         Transaction t = s.beginTransaction();
 			Query query = s.createQuery("delete Relaciones where idusuario = :idu and siguiendo = :siguiendo");
                         query.setLong("idu", idu);
                         query.setParameter("siguiendo", siguiendo.toString());
                          int rows= query.executeUpdate();
                          t.commit();
                          s.disconnect();
                          			System.err.println("Columnas borradas-->" + rows);
 
                                                 return 0;
                          
   }
      public static int follow(Long idu, Long siguiendo) {
                 	                	
 		 try {
 			SessionFactory sf = HibernateUtil.getSessionFactory();
                         Transaction t = null;
                         Session s = sf.openSession();
 			t = s.beginTransaction(); // start a new transaction
                         Relaciones re = new Relaciones();
                         re.setIdusuario( idu.intValue());
                         re.setSiguiendo(siguiendo.toString());
                         s.persist(re);
                         t.commit();
                         s.close();
                         
                         
                         sendNotificationFollow(re.getSiguiendo());
                        
                        
                        return 1;
 
 		
 		} catch (Exception ex) {
 			System.err.println("Error-0->" + ex.getMessage());
 			return 0;
 		}
 
     }
 
 
      public static ArrayList<Usuarios> getFollowers(Long idu) {
                 	
 		 try {
 			SessionFactory sf = HibernateUtil.getSessionFactory();
                         Transaction t = null;
                         Session s = sf.openSession();
 			t = s.beginTransaction(); // start a new transaction
                         
 			 Query query = s.createQuery("from Usuarios where idu in (select r.idusuario FROM Relaciones r where r.siguiendo = :idusuario)");
                          Map auth = ActionContext.getContext().getSession();
                            
                         // query.setParameter("idu", ((Number)auth.get("idusuario")));    
                         //query.setParameter("idusuario", ((Number)auth.get("idusuario")).intValue());
                          query.setParameter("idusuario", idu.toString());
                          
                          
                                                  s.disconnect();
 
                          return (ArrayList<Usuarios>)query.list();
 
 		
 		} catch (Exception ex) {
 			System.err.println("Error !-->" + ex.getMessage());
 			
 			return null;
 		}
                  
   }
      public static ArrayList<Usuarios> getFollowings(Long idu) {
                 	
 		 try {
 			SessionFactory sf = HibernateUtil.getSessionFactory();
                         Transaction t = null;
                         Session s = sf.openSession();
 			t = s.beginTransaction(); // start a new transaction
                         
 			 Query query = s.createQuery("from Usuarios where idu in (select r.siguiendo FROM Relaciones r where r.idusuario = :idusuario)");
                         
                            
                         // query.setParameter("idu", ((Number)auth.get("idusuario")));    
                         query.setParameter("idusuario", idu.intValue());
                          //query.setParameter("idusuario", ((Number)auth.get("idusuario")).toString());
                          			                          s.disconnect();
 
                         return (ArrayList<Usuarios>)query.list();
 
 		
 		} catch (Exception ex) {
 			System.err.println("Error !-->" + ex.getMessage());
 			
 			return null;
 		}
                  
   }
      public static boolean sendNotificationFollow(String idu) {
          SessionFactory sf = HibernateUtil.getSessionFactory();
                         Transaction t = null;
                         Session s = sf.openSession();
 			t = s.beginTransaction(); // start a new transaction
                    try {    
                                               System.err.println("idu parametro-->" + idu);
                        
                        Usuarios seguidor = new Usuarios();
                        seguidor = PerfilDao.traerPerfil(Long.parseLong(idu));
                      
                        if(seguidor.getFlag1().compareTo("true") == 0) {
                       
 
                        SendMail correo = new SendMail(seguidor.getMail(),seguidor.getNombre(),2);  
                        Thread thread = new Thread(correo);  
                        thread.start(); 
                        }
                        s.close();
                        return true;
      }
                        catch (Exception ex) {
 			System.err.println("Error-:p->" + ex.getMessage());
 			return false;
 		}
     }
 }
