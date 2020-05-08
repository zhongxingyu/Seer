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
    private EntityManager em;
 
     public BookDaoImpl(EntityManagerFactory emf) {
         this.emf = emf;
     }    
 
    public BookDaoImpl(EntityManager em) {
        this.em = em;
    }        

     public void createBook(Book book) {
         checkBookAttributes(book);
         if(book.getId() != null) {
             throw new IllegalArgumentException("cannot create book with assigned id");
         }
        em.persist(book);  
     }
     
     public List<Book> findAllBooks() {
         Query query = em.createQuery("SELECT b FROM Book b");
         return query.getResultList();        
     }
 
     public Book findBookById(Long id) {
         if (id == null) {
             throw new NullPointerException("id is null");
         }
         return em.find(Book.class, id);
         
     }
 
     public void deleteBook(Book book) {
         if (book == null) {
             throw new NullPointerException("book is null");
         }
         if (book.getId() == null) {
             throw new IllegalArgumentException("cannot delete book with no id");
         }
         Book toRemove = em.merge(book);
         em.remove(toRemove);
         book.setId(null);
         System.out.println(toRemove.getId());
     }
 
     public void updateBook(Book book) {
         checkBookAttributes(book);
         em.merge(book);
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
