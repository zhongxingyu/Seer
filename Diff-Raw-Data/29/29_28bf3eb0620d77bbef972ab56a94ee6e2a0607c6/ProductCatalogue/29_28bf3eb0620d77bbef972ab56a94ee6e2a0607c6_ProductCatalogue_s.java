 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package dit126.group4.group4shop.core;
 
 import java.util.List;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 import javax.persistence.Query;
 
 /**
  *
  * @author Christian
  */
 public class ProductCatalogue implements IProductCatalogue{
     
     private EntityManagerFactory emf;
     private final Class<Product> clazz;
     
     public ProductCatalogue(String puName){
         this.emf = Persistence.createEntityManagerFactory(puName);
         this.clazz = Product.class;
     }
     
 
     public void add(Product product) {
        EntityManager em = this.emf.createEntityManager();
         try{
             em.getTransaction().begin();
             em.persist(product);
             em.getTransaction().commit();
         }finally{
             em.close();
         }
     }
     
     public void remove(Long id) {
        EntityManager em = this.emf.createEntityManager();
         Product product = em.find(clazz, id);
         try{
             em.getTransaction().begin();
             em.remove(product);
             em.getTransaction().commit();
         }finally{
             em.close();
         }
     }
 
     public void update(Product product) {
        EntityManager em = this.emf.createEntityManager();
         try{
             em.getTransaction().begin();
             em.merge(product);
             em.getTransaction().commit();
         }finally{
             em.close();
         }
     }
 
     public Product find(Long id) {
        EntityManager em = this.emf.createEntityManager();
         Product product = null;
         try{
             em.getTransaction().begin();
             product = em.find(clazz, id);
             em.getTransaction().commit();
         }finally{
             em.close();
             return product;
         }
     }
 
     public List<Product> getRange(int first, int nItems) {
        EntityManager em = this.emf.createEntityManager();
         Query query = null;
         List<Product> list = null;
         try{
             em.getTransaction().begin();
             query = em.createQuery("SELECT p FROM "+ clazz.getSimpleName().toUpperCase() + " p");
             em.getTransaction().commit();
             list = query.getResultList();
         }finally{
             em.close();
             return list.subList(first, nItems);
         }
     }
 
     public int getCount() {
        EntityManager em = this.emf.createEntityManager();
         int count = 0;
         try{
             em.getTransaction().begin();
             Query query = em.createQuery("SELECT p FROM " + clazz.getSimpleName().toUpperCase() + " p");
             em.getTransaction().commit();
             count = query.getResultList().size();
         }finally{
             em.close();
             return count;
         }
     }
     
 }
