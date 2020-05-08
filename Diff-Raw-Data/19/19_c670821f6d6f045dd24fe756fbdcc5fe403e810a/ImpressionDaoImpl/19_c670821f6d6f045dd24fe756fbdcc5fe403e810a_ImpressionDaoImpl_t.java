 package cz.muni.fi.pa165.library.daos;
 
 import cz.muni.fi.pa165.library.entities.Book;
 import cz.muni.fi.pa165.library.entities.Impression;
 import cz.muni.fi.pa165.library.entities.Loan;
 import java.util.ArrayList;
 import java.util.List;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 import org.springframework.stereotype.Repository;
 
 /**
  *
  * @author MiškoHu
  */
 @Repository
 public class ImpressionDaoImpl implements ImpressionDao {
 
     @PersistenceContext
     private EntityManager em;
 
     public void setEntityManager(EntityManager em) {
         this.em = em;
     }
 
     public void createImpression(Impression impression) {
         checkImpressionAttributes(impression);
         if (impression.getId() != null) {
             throw new IllegalArgumentException("Cannot create impression with assigned ID.");
         }
         em.persist(impression);
     }
 
     public List<Impression> findAllImpressions() {
         Query query = em.createQuery("SELECT i FROM Impression i");
         return query.getResultList();
     }
 
     public Impression findImpressionById(Long id) {
         if (id == null) {
             throw new NullPointerException("ID is null.");
         }
         return em.find(Impression.class, id);
     }
 
     public void deleteImpression(Impression impression) {
         if (impression == null) {
             throw new NullPointerException("Impression is null.");
         }
         if (impression.getId() == null) {
             throw new IllegalArgumentException("Cannot delete impression with no ID.");
         }
 
         Query query = em.createQuery("SELECT b FROM Book b WHERE b.impression = :i");
         query.setParameter("i", impression);
         List<Book> books = query.getResultList();
         for (Book b : books){
             query = em.createQuery("DELETE FROM Loan l WHERE l.book = :b");
             query.setParameter("b", b);
             query.executeUpdate();
         }
 
         query = em.createQuery("DELETE FROM Book b WHERE b.impression = :i");
         query.setParameter("i", impression);
         query.executeUpdate();
 
         Impression toRemove = em.merge(impression);
         em.remove(toRemove);
         impression.setId(null);
     }
 
     public void updateImpression(Impression impression) {
         checkImpressionAttributes(impression);
         em.merge(impression);
     }
 
     private void checkImpressionAttributes(Impression impression)
             throws IllegalArgumentException, NullPointerException {
 
         if (impression == null) {
             throw new NullPointerException("Impression is null.");
         }
         if (impression.getIsbn() == null) {
             throw new IllegalArgumentException("Impression.isbn cannot be null.");
         }
         if (impression.getName() == null) {
             throw new IllegalArgumentException("Impression.name cannot be null.");
         }
         if (impression.getAuthor() == null) {
             throw new IllegalArgumentException("Impression.author cannot be null.");
         }
         if (impression.getReleaseDate() == null) {
             throw new IllegalArgumentException("Impression.releaseDate cannot be null.");
         }
         if (impression.getDepartment() == null) {
             throw new IllegalArgumentException("Impression.department cannot be null.");
         }
     }
 
     public List<Impression> findImpressions(String search) {
         Query query = em.createQuery("SELECT i FROM Impression i"
                 + " WHERE i.name LIKE :search OR i.author LIKE :search");
         query.setParameter("search", "%" + search + "%");
         return query.getResultList();
     }
 
     public List<Impression> findNotBorrowedImpressions(String search) {
         Query query = em.createQuery("SELECT DISTINCT i.id FROM Loan l JOIN l.book b JOIN b.impression i"
                 + " WHERE i.name LIKE :search OR i.author LIKE :search"
                 + " GROUP BY b.id, i.id"
                + " HAVING COUNT(l.toDate) = COUNT(*) OR COUNT(*) = 0");
         query.setParameter("search", "%" + search + "%");
         List<Long> ids = query.getResultList();
         ArrayList<Impression> list = new ArrayList<Impression>();
         for (Long id : ids) {
             list.add(em.find(Impression.class, id));
         }
         return list;
     }
 }
