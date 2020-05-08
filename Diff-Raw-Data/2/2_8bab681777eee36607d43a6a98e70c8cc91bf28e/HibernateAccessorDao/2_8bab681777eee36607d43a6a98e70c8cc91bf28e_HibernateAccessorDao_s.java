 package uk.ac.ox.oucs.oauth.dao;
 
 import org.hibernate.HibernateException;
 import org.hibernate.Session;
 import org.springframework.orm.hibernate3.HibernateCallback;
 import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
 import uk.ac.ox.oucs.oauth.domain.Accessor;
 
 import java.sql.SQLException;
 import java.util.Date;
 import java.util.List;
 
 public class HibernateAccessorDao extends HibernateDaoSupport implements AccessorDao {
     public void create(final Accessor accessor) {
         getHibernateTemplate().save(accessor);
     }
 
     public Accessor get(String accessorId) {
         return (Accessor) getHibernateTemplate().get(Accessor.class, accessorId);
     }
 
     public List<Accessor> getByUser(String userId) {
         return (List<Accessor>) getHibernateTemplate().find(
                "FROM Accessor a WHERE a.user = ?",
                 new Object[]{userId});
     }
 
     public void markExpiredAccessors() {
         getHibernateTemplate().execute(new HibernateCallback() {
             public Object doInHibernate(Session session) throws HibernateException, SQLException {
                 session.createQuery(
                         "UPDATE Accessor a SET a.status=? WHERE a.expirationDate < ?")
                         .setParameter(0, Accessor.Status.EXPIRED)
                         .setDate(1, new Date())
                         .executeUpdate();
                 return null;
             }
         });
     }
 
 
     public Accessor update(Accessor accessor) {
         getHibernateTemplate().update(accessor);
         return accessor;
     }
 
     public void remove(Accessor accessor) {
         getHibernateTemplate().delete(accessor);
     }
 }
