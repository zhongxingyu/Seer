 /**
  * Written by Tien Nguyen <lilylnx@users.sf.net>
  * FREE FOR ALL BUT DOES NOT MEAN THERE IS NO PRICE.
  */
 package net.lilylnx.springnet.core.hibernate;
 
 import java.io.Serializable;
 import java.lang.reflect.ParameterizedType;
 import java.util.List;
 
 import org.hibernate.Criteria;
 import org.hibernate.LockOptions;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Projections;
 import org.hibernate.criterion.Restrictions;
 
 import net.lilylnx.springnet.repository.Repository;
 
 /**
  * @author Tien Nguyen
  * @version $Id: HibernateGenericDAO.java,v 1.0 2011/06/24 1:54:03 lilylnx Exp $
  */
 public abstract class HibernateGenericDAO<T> implements Repository<T> {
 
   protected Class<T> persistClass;
   private SessionFactory sessionFactory;
   
   @SuppressWarnings("unchecked")
   public HibernateGenericDAO(SessionFactory sessionFactory) {
     this.persistClass = (Class<T>)((ParameterizedType)this.getClass().getGenericSuperclass())
         .getActualTypeArguments()[0];
     this.sessionFactory = sessionFactory;
   }
  
  public Class<T> getPersist() {
    return persistClass;
  }
 
   protected Session session() {
     return sessionFactory.getCurrentSession();
   }
   
   @SuppressWarnings("unchecked")
   @Override
  public T get(int id) {
     return (T)this.session().get(persistClass, id);
   }
   
   /* (non-Javadoc)
    * @see net.lilylnx.springnet.repository.Repository#add(java.lang.Object)
    */
   @Override
   public void add(T entity) {
     this.session().save(entity);
   }
   
   /* (non-Javadoc)
    * @see net.lilylnx.springnet.repository.Repository#remove(java.lang.Object)
    */
   @Override
   public void remove(T entity) {
     this.session().delete(entity);
   }
   
   /* (non-Javadoc)
    * @see net.lilylnx.springnet.repository.Repository#update(java.lang.Object)
    */
   @Override
   public void update(T entity) {
     this.session().update(entity);
   }
 
   @Override
   public T find(Object id) {
     return find(id, false);
   }
 
   @SuppressWarnings("unchecked")
   @Override
   public T find(Object id, boolean locked) {
     Object o = null;
 
     if (locked) {
       o = this.session().get(persistClass, (Serializable)id, LockOptions.UPGRADE);
     }
     else {
       o = this.session().get(persistClass, (Serializable)id);
     }
 
     return (T)o;
   }
 
   @Override
   public List<T> findAll() {
     return findAll(false);
   }
 
   @Override
   public List<T> findAll(boolean cacheable) {
     return findAll(false, null, cacheable);
   }
 
   @Override
   public List<T> findAll(boolean reverse, String propertyName) {
     return findAll(reverse, propertyName, false);
   }
 
   @SuppressWarnings("unchecked")
   @Override
   public List<T> findAll(boolean reverse, String propertyName, boolean cacheable) {   
     Criteria c = this.session().createCriteria(persistClass);
 
     if (propertyName != null) {
       if (reverse) {
         c.addOrder(Order.desc(propertyName));
       }
       else {
         c.addOrder(Order.asc(propertyName));
       }
     }
     
     c.setCacheable(cacheable);
     return c.list();
   }
 
   @Override
   public List<T> findRange(int[] range) {
     return findRange(range, false, null);
   }
 
   @SuppressWarnings("unchecked")
   @Override
   public List<T> findRange(int[] range, boolean reverse, String propertyName) {
     Criteria c = this.session().createCriteria(persistClass);
     c.setMaxResults(range[1] - range[0]);
     c.setFirstResult(range[0]);
     
     if (propertyName != null) {
       if (reverse) {
         c.addOrder(Order.desc(propertyName));
       }
       else {
         c.addOrder(Order.asc(propertyName));
       }
     }
 
     return c.list();
   }
 
   @Override
   public List<T> select(String where) {
     return select(where, null);
   }
 
   @Override
   public List<T> select(String[] orders) {
     return select(null, orders);
   }
 
   @Override
   public List<T> select(String where, String[] orders) {
     return select(where, orders, null);
   }
 
   @SuppressWarnings("unchecked")
   @Override
   public List<T> select(String where, String[] orders, int[] range) {
     Criteria c = this.session().createCriteria(persistClass);
     
     if (where != null && where.length() != 0) {
       c.add(Restrictions.sqlRestriction(where));
     }
     
     String[] _order = null;
     if (orders != null) {
       for (String order : orders) {
         if (order.contains(" ")) {
           _order = order.split(" ");
           if (_order[1].equals("desc")) {
             c.addOrder(Order.desc(_order[0]));
           }
           else if (_order[1].equals("asc")) {
             c.addOrder(Order.asc(_order[0]));
           }
         }
         else {
           c.addOrder(Order.asc(order));
         }
       }
     }
     
     if (range != null) {
       c.setMaxResults(range[1] - range[0]);
       c.setFirstResult(range[0]);
     }
     
     return c.list();
   }
 
   @Override
   public Number count() {
     Criteria c = this.session().createCriteria(persistClass);
     c.setProjection(Projections.rowCount());
     return (Number)c.uniqueResult();
   }
 
 }
