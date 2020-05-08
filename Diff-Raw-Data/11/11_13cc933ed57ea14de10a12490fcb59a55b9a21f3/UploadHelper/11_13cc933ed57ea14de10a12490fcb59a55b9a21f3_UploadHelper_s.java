 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package fr.gphy.piotrgui.j2eged.helpers;
 
 import fr.gphy.piotrgui.j2eged.hibernate.HibernateUtil;
 import fr.gphy.piotrgui.j2eged.model.Document;
 import fr.gphy.piotrgui.j2eged.model.Metadata;
 import fr.gphy.piotrgui.j2eged.model.PhysicalDocument;
 import fr.gphy.piotrgui.j2eged.model.Type;
 import fr.gphy.piotrgui.j2eged.model.User;
 import fr.gphy.piotrgui.j2eged.model.Version;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 
 /**
  *
  * @author Piotr
  */
 
 public class UploadHelper {
 
     Session session = null;
 
     public UploadHelper() {
         this.session = HibernateUtil.getSessionFactory().getCurrentSession();
     }
     
     public void reloadSession() {
         this.session = HibernateUtil.getSessionFactory().openSession();
     }
 
     public void createNewFile() {
         session.beginTransaction();
 
         try {
             Query query = session.createSQLQuery("insert into document (doc_post, doc_ant) values (null, null)");
             query.executeUpdate();
         } catch (Exception e) {
             throw e;
         }
     }
 
     public Document getLastDocument() {
         session.beginTransaction();
 
         try {
             Query query = session.createSQLQuery("SELECT * FROM document WHERE id_doc='" + this.getLastIDFromDocument().toString() + "'").addEntity(Document.class);
             //System.err.println(((Document) query.uniqueResult()).getIdDoc());
             return (Document) query.uniqueResult();
         } catch (Exception e) {
             throw e;
         }
     }
 
     public Integer getLastIDFromDocument() {
         session.beginTransaction();
 
         try {
             Query query = session.createSQLQuery("SELECT max(id_doc) FROM document");
             return (Integer) query.uniqueResult();
         } catch (Exception e) {
             throw e;
         }
     }
 
     public void createNewPhysicalDoc(byte[] blob) {
         PhysicalDocument doc = new PhysicalDocument(this.getLastDocument(), blob);
         System.err.println(this.getLastDocument().getIdDoc().toString());
         
         session.beginTransaction();
 
         try {
             session.save(doc);
            
            session.flush();
            session.clear();
         } catch (Exception e) {
             throw e;
         }
     }
 
     public void createNewMetadata(Metadata meta) {
        session.beginTransaction();
 
         try {
 
             session.save(meta);
            session.flush();
            session.clear();
         } catch (Exception e) {
             throw e;
         }
     }
 
     public void createNewVersion(Version version) {
          Transaction tx = session.beginTransaction();
 
         try {
 
             session.save(version);
            
            session.flush();
            session.clear();
           
             tx.commit();
             
             this.reloadSession();
         } catch (Exception e) {
             throw e;
         }
     }
     
     
     public Type getTypeFromExtension(String ext) {
         session.beginTransaction();
 
         try {
             Query query = session.createSQLQuery("select * from type where file_extension='" + ext + "'").addEntity(Type.class);
             
             Type type = (Type) query.uniqueResult();
             
             System.err.println("1");
             if (type == null) {
                 type = this.getNullType();
             }
             
             return type;
             
             
         } catch (Exception e) {
             throw e;
         }
     }
 
     public Type getNullType() {
     session.beginTransaction();
 
         try {
             Query query = session.createSQLQuery("select * from type where file_extension='.'").addEntity(Type.class);
             
             return (Type) query.uniqueResult();            
             
         } catch (Exception e) {
             throw e;
         }    
     }
     
     public User retrieveUserFromLog(String login, String passwd) {
        Transaction tx = session.beginTransaction();
         try {
             Query query = session.createSQLQuery("select * from user where login='" + login + "' and password='" + passwd + "'").addEntity(User.class);
             return (User) query.uniqueResult();
 
         } catch (Exception e) {
             throw e;
         } 
     }
 }
