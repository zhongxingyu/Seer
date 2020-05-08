 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package fr.gphy.piotrgui.j2eged.helpers;
 
 import fr.gphy.piotrgui.j2eged.hibernate.HibernateUtil;
 import fr.gphy.piotrgui.j2eged.model.Folder;
import java.io.Serializable;
 import java.util.List;
 import org.hibernate.Query;
 import org.hibernate.Session;
 
 /**
  *
  * @author Piotr
  */
public class TreeHelper implements Serializable {
     
     Session session = null;
 
     public TreeHelper() {
         this.session = HibernateUtil.getSessionFactory().getCurrentSession();
     }
     
     public void reloadSession() {
         this.session = HibernateUtil.getSessionFactory().openSession();
     }
     
        public List<Folder> getFolders(Integer clef) {
         session.beginTransaction();
 
         try {
             String sql = "select * from folder where parent_folder";
             if(clef != null) 
                 sql += "= " + clef + ";";
             else 
                 sql += " is null;";
             Query query = session.createSQLQuery(sql)
                     .addEntity(Folder.class);
 
             return (List<Folder>) query.list();
 
         } catch (Exception e) {
             throw e;
         }
 
     }
 }
