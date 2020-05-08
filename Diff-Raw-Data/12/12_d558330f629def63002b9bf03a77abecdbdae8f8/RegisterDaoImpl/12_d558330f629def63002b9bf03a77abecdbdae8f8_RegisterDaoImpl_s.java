 package dao.impl;
 
 import dao.RegisterDao;
 import org.hibernate.HibernateException;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.springframework.orm.hibernate3.HibernateCallback;
 import org.springframework.orm.hibernate3.HibernateTemplate;
 import pojo.Register;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * User: yumingzhe
  * Date: 5/21/12
  * Time: 8:22 PM
  */
 public class RegisterDaoImpl implements RegisterDao {
     private HibernateTemplate template;
     private SessionFactory factory;
 
     public HibernateTemplate getTemplate() {
         if (template == null) {
             template = new HibernateTemplate(factory);
         }
         return template;
     }
 
     public void setTemplate(HibernateTemplate template) {
         this.template = template;
     }
 
     public SessionFactory getFactory() {
         return factory;
     }
 
     public void setFactory(SessionFactory factory) {
         this.factory = factory;
     }
 
     @Override
     public List<Register> getAllRegister() {
         List registers = this.getTemplate().executeFind(new HibernateCallback<Object>() {
             @Override
             public Object doInHibernate(Session session) throws HibernateException, SQLException {
                 Query query = session.createQuery("from Register");
                 return query.list();
             }
         });
         return registers;
     }
 
     @Override
     public Register getRegisterBySequence(final String sequence) {
         List registers = this.getTemplate().executeFind(new HibernateCallback<Object>() {
             @Override
             public Object doInHibernate(Session session) throws HibernateException, SQLException {
                 Query query = session.createQuery("from Register as r where r.registerSequence= :secret").setString("secret", sequence);
                 return query.list();
 
             }
         });
         if (registers.size() == 0) {
             return null;
         }
         return (Register) registers.get(0);
     }
 
     @Override
     public void deleteAllRegisterByUID(final int uid) {
         List<Register> registers = (ArrayList<Register>) this.getTemplate().executeFind(new HibernateCallback<Object>() {
             @Override
             public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query query = session.createQuery("from Register as r where r.uid= :uid").setInteger("uid", uid);
                 return (ArrayList<Register>) query.list();
             }
         });
         for (Register register : registers) {
             this.getTemplate().delete(register);
         }
     }
 }
