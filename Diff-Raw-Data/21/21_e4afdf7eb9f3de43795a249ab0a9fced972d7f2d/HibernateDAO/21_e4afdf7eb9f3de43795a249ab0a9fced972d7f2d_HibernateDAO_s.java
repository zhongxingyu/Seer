 package com.ideas.persistence;
 
 import com.ideas.utils.HibernateUtil;
 import org.hibernate.Session;
 
 import java.io.Serializable;
 import java.lang.reflect.ParameterizedType;
 
 /**
  * Abstract interactions with hibernate sessions for DAOs
  */
 public class HibernateDAO<LoadType>
 {
 
     private Session session;
 
     public HibernateDAO()
     {
        setSession(HibernateUtil.getSessionFactory().getCurrentSession());
     }
 
     protected Session getSession()
     {
         return session;
     }
 
     private void setSession(Session session)
     {
         this.session = session;
     }
 
     /**
      * Save an object.
      *
      * @param object Object to save.
      * @return
      */
     protected Serializable save (Object object)
     {
         getSession().beginTransaction();
         Serializable output = getSession().save(object);
//        getSession().getTransaction().commit();
 
         return output;
     }
 
     protected LoadType load (long id)
     {
         getSession().beginTransaction();
         LoadType loadedObject = (LoadType)getSession().get(getLoadClass(), id);
//        getSession().getTransaction().commit();
         return loadedObject;
     }
 
     private Class<?> getLoadClass()
     {
         return (Class<?>) ((ParameterizedType)(HibernateDAO.this.getClass().getGenericSuperclass())).getActualTypeArguments()[0];
     }
 }
