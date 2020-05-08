 package it.chalmers.fannysangles.friendzone.utils;
 
 import java.util.ArrayList;
 import java.util.List;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Root;
 
 /**
  * A container for entities, base class for OrderBook, ProductCatalogue,
  * CustomerRegistry The fundamental common operations are here (CRUD).
  *
  * T is type for items in container K is type of id (primary key)
  *
  * @author hajo
  */
 public abstract class AbstractManager<T, K>
         implements IManager<T, K> {
     
     private final Class<T> clazz;
     
    private static final String PU = "friendzone_pu";
     
     @PersistenceContext(unitName = PU)
     protected EntityManager em;
     
     public AbstractManager(Class<T> clazz){
         this.clazz = clazz;
     }
 
 
     @Override
     public void add(T t) {
         if (t == null) {
             throw new IllegalArgumentException("Nulls not allowed");
         }
         em.persist(t);
         em.close();
     }
 
     @Override
     public void remove(K id) {
         T t = em.getReference(clazz, id);
         em.remove(t);
         em.close();
     }
 
     @Override
     public T update(T t) {
         T updated = em.merge(t);
         em.close();
         return updated;
     }
 
     @Override
     public T find(K id) {
         return em.find(clazz, id);
     }
     
     @Override
     public List<T> getRange(int first, int nItems) {
         return get(false, nItems, first);
     }
     
     private List<T> get(boolean all, int maxResults, int firstResult){
         List<T> found;
         found = new ArrayList<>();
             CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
             cq.select(cq.from(clazz));
             Query q = em.createQuery(cq);
             if (!all) {
                 q.setMaxResults(maxResults);
                 q.setFirstResult(firstResult);
             }
             found.addAll(q.getResultList());
             em.close();
 
             return found;
     }
 
     @Override
     public int getCount() {
         int count = -1;
 
         CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
         Root<T> rt = cq.from(clazz);
         cq.select(em.getCriteriaBuilder().count(rt));
         Query q = em.createQuery(cq);
         count = ((Long) q.getSingleResult()).intValue();
 
         return count;
     }
 }
