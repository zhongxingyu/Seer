 package cz.muni.fi.pa165.library.daos;
 
 import cz.muni.fi.pa165.library.entities.Book;
 import cz.muni.fi.pa165.library.entities.Impression;
 import cz.muni.fi.pa165.library.entities.Loan;
 import java.util.ArrayList;
 import java.util.List;
 import javax.persistence.Query;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import org.springframework.stereotype.Repository;
 
 /**
  *
  * @author Mjartan
  */
 @Repository
 public class BookDaoImpl implements BookDao {
 
     @PersistenceContext
     private EntityManager em;
 
     public void setEntityManager(EntityManager em) {
         this.em = em;
     }
 
     public void createBook(Book book) {
         checkBookAttributes(book);
         if (book.getId() != null) {
             throw new IllegalArgumentException("Cannot create book with assigned id");
         }
         em.persist(book);
     }
 
     public List<Book> findAllBooks() {
         Query query = em.createQuery("SELECT b FROM Book b");
         return query.getResultList();
     }
 
     public Book findBookById(Long id) {
         if (id == null) {
             throw new NullPointerException("ID is null");
         }
         return em.find(Book.class, id);
     }
 
     public void deleteBook(Book book) {
         if (book == null) {
             throw new NullPointerException("Book is null");
         }
         if (book.getId() == null) {
             throw new IllegalArgumentException("Cannot delete book with no id");
         }
         Query query = em.createQuery("DELETE FROM Loan l WHERE l.book = :b");
         query.setParameter("b", book);
         query.executeUpdate();
        
         Book toRemove = em.merge(book);
         em.remove(toRemove);
         book.setId(null);
     }
 
     public void updateBook(Book book) {
         checkBookAttributes(book);
         em.merge(book);
     }
 
     private void checkBookAttributes(Book book) throws IllegalArgumentException, NullPointerException {
         if (book == null) {
             throw new NullPointerException("book is null");
         }
         if (book.getImpression() == null) {
             throw new IllegalArgumentException("book.impression cannot be null");
         }
     }
 
     public List<Book> findBooksForImpression(Impression impression) {
         Query query = em.createQuery("SELECT b FROM Book b WHERE b.impression = :i");
         query.setParameter("i", impression);
         return query.getResultList();
     }
 
    public Book findNotBorrowedBookForImpression(Impression impression) {
        Query query = em.createQuery("SELECT DISTINCT b.id FROM Loan l JOIN l.book b JOIN b.impression i"
                + " WHERE i = :impression"
                + " GROUP BY b.id"
                + " HAVING COUNT(l.toDate) = COUNT(*)");
        query.setParameter("impression", impression);
         List<Long> ids = query.getResultList();
         if (ids.isEmpty()) {
             return null;
         }
        return em.find(Book.class, ids.get(0));
     }
 }
