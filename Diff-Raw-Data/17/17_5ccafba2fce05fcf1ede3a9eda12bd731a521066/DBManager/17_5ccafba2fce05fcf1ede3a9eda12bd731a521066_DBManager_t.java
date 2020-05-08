 package az.his;
 
 import az.his.persist.DBListener;
 import org.hibernate.Criteria;
 import org.hibernate.Session;
 import org.hibernate.criterion.Restrictions;
 
 import java.io.Serializable;
 import java.util.List;
 
 public class DBManager {
     private Session session;
 
     public DBManager(Session sess) {
         session = sess;
     }
 
     @SuppressWarnings({"unchecked"})
     public <E> List<E> findAll(Class<E> entityClass) {
         Criteria query = session.createCriteria(entityClass);
         return query.setCacheable(true).list();
     }
 
     @SuppressWarnings({"unchecked"})
     public <E> E findByNaturalKey(Class<E> entityClass, String propertyName, Object propertyValue) {
         Criteria query = session.createCriteria(entityClass);
         query.add(Restrictions.eq(propertyName, propertyValue));
         return (E) query.setCacheable(true).uniqueResult();
     }
 
     @SuppressWarnings({"unchecked"})
     public <E> E get(Class<E> entityClass, Serializable id) {
         return (E) session.get(entityClass, id);
     }
 
     @SuppressWarnings({"unchecked"})
     public <E> E merge(E object) {
         return (E) session.merge(object);
     }
 
     public void persist(Object object) {
         if (object instanceof DBListener) {
             ((DBListener) object).beforeInsert(this);
         }
         session.persist(object);
     }
 
     public void update(Object object) {
         session.saveOrUpdate(object);
     }
 
     @SuppressWarnings({"unchecked"})
     public <E> List<E> findByProperty(Class<E> entityClass, String propertyName, Object propertyValue) {
         Criteria query = session.createCriteria(entityClass);
         query.add(Restrictions.eq(propertyName, propertyValue));
         return query.list();
     }
 
     @SuppressWarnings("unchecked")
     public void delete(Class cls, int id) {
         Object obj = get(cls, id);
         if (obj instanceof DBListener) {
             ((DBListener) obj).beforeDelete(this);
         }
         session.delete(obj);
     }
 
     public Session getSession() {
         return session;
     }

    public void flush() {
        getSession().flush();
    }
 }
