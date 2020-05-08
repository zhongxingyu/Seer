 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package DAO;
 
 /**
  *
  * @author athanatos
  */
 import Modelo.Zenoss.zenossHibernateUtil;
 import org.hibernate.Session;
 import org.hibernate.Query;
 import java.util.List;
 import Modelo.Zenoss.History;
 public class AlarmaDAO {
     Session session = null;
 
     public AlarmaDAO() {
     this.session = zenossHibernateUtil.getSessionFactory().getCurrentSession();
     }
     
     public List getHistory(int id){
     List<History> ListHistory =null;
     
     
     return ListHistory;
     }
     
 }
