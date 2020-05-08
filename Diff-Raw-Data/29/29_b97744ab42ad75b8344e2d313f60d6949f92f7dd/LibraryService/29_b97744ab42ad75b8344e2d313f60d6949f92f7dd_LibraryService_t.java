 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package no.hials.muldvarpweb.service;
 
 import java.util.ArrayList;
 import java.util.List;
 import javax.ejb.Stateless;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.TypedQuery;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 import no.hials.muldvarpweb.domain.Article;
 import no.hials.muldvarpweb.domain.Course;
 import no.hials.muldvarpweb.domain.LibraryItem;
 
 /**
  *
  * @author Nospherus
  */
 @Stateless
 @Path("Library")
 public class LibraryService {
     @PersistenceContext
     EntityManager entityManager;
     Boolean flag = true;
         
         @GET
         @Produces({MediaType.APPLICATION_JSON})
         public List<LibraryItem> getLibrary(){
             return entityManager.createQuery("SELECT l from LibraryItem l", LibraryItem.class).getResultList();
         }
         
         public void addLibraryItem(LibraryItem lI){
             lI = entityManager.merge(lI);
             entityManager.persist(lI);
         }
         
         public void removeLibraryItem(LibraryItem lI){
             lI = entityManager.merge(lI);
             entityManager.remove(lI);
         }
         
         @GET
         @Path("{id}")
         @Produces({MediaType.APPLICATION_JSON})
         public LibraryItem getDocuments(@PathParam("id") Long id) {
             TypedQuery<LibraryItem> q = entityManager.createQuery("Select d from LibraryItem d where d.id = :id", LibraryItem.class);
             q.setParameter("id", id);
             return q.getSingleResult();
         }
 
         
//        public void makeTestData(){
//            LibraryItem lI = new LibraryItem("A tale of two men", "How to make money", "Carl Brooke", "Austin Maxwell", "15.03.1998", "239", "This is a book explaining how a pair of people can make money", "http://unrestrictedstock.com/wp-content/uploads/office-icons-book-free-stock-vector.jpg", "http://whhs.cps-k12.org/library/images/icon_book.gif", "http://cran.r-project.org/doc/manuals/R-intro.pdf");
//            LibraryItem lI2 = new LibraryItem("Monster manual", "Defence against the black arts", "Severus Snape", "Harry Potter", "5.08.2008", "100", "This book explains how you can kick ass", "http://unrestrictedstock.com/wp-content/uploads/office-icons-book-free-stock-vector.jpg", "http://whhs.cps-k12.org/library/images/icon_book.gif", "http://cran.r-project.org/doc/manuals/R-intro.pdf");
//            LibraryItem lI3 = new LibraryItem("Master & Commander", "A wet movie", "Master B", "Commander D", "09.01.1460", "1902", "This book is a book about sailboats and domination", "http://unrestrictedstock.com/wp-content/uploads/office-icons-book-free-stock-vector.jpg", "http://whhs.cps-k12.org/library/images/icon_book.gif", "http://cran.r-project.org/doc/manuals/R-intro.pdf");
//            addLibraryItem(lI);
//            addLibraryItem(lI2);
//            addLibraryItem(lI3);
//            
//        }
 }
