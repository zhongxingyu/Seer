 package cz.muni.fi.pa165.library.daos;
 
 import cz.muni.fi.pa165.library.entities.Book;
 import java.util.List;
 import javax.persistence.Entity;
 import javax.persistence.Query;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 
 
 /**
  * 
  * @author Mjartan
  */
 public class BookDaoImpl implements BookDao {
     
     private EntityManagerFactory emf;
 
     public BookDaoImpl(EntityManagerFactory emf) {
         this.emf = emf;
     }    
 
     public void createBook(Book book) {
         checkBookAttributes(book);
         if(book.getId() != null) {
             throw new IllegalArgumentException("cannot create book with assigned id");
         }
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(book);
        em.getTransaction().commit();
        
     }
     
     public List<Book> findAllBooks() {
        EntityManager em = emf.createEntityManager();
         Query query = em.createQuery("SELECT b FROM Book b");
         return query.getResultList();        
     }
 
     public Book findBookById(Long id) {
         if (id == null) {
             throw new NullPointerException("id is null");
         }
        EntityManager em = emf.createEntityManager();
         return em.find(Book.class, id);
         
     }
 
     public void deleteBook(Book book) {
         if (book == null) {
             throw new NullPointerException("book is null");
         }
         if (book.getId() == null) {
             throw new IllegalArgumentException("cannot delete book with no id");
         }
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
         Book toRemove = em.merge(book);
         em.remove(toRemove);
        em.getTransaction().commit();
         book.setId(null);
         System.out.println(toRemove.getId());
     }
 
     public void updateBook(Book book) {
         checkBookAttributes(book);
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
         em.merge(book);
        em.getTransaction().commit();
     }
 
     private void checkBookAttributes(Book book) throws IllegalArgumentException, NullPointerException {
         if(book == null) {
             throw new NullPointerException("book is null");
         }
         if(book.getImpression() == null) {
             throw new IllegalArgumentException("book.impression cannot be null");
         }
         if (book.getPrinted() == null) {
             throw new IllegalArgumentException("book.printed cannot be null");
         }
     }
     
 }
