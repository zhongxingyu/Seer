 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.tomhume.webapps;
 
 import java.util.ArrayList;
 import java.util.List;
 import javax.ejb.Stateless;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 
 /**
  *
  * @author twhume
  */
 @Stateless
 public class PortfolioController implements PortfolioControllerLocal {
     
     @PersistenceContext(unitName = "StockPortfolio-ejbPU")
     private EntityManager em;
     
     @Override
     public List<ShareTransaction> list() {
        Query query = em.createQuery("select s from ShareTransaction s order by s.when ASC, s.id ASC");
         return new ArrayList<ShareTransaction>(query.getResultList());
     }
 
     @Override
     public void add(ShareTransaction transaction) {
         em.persist(transaction);
     }
 
     @Override
     public void delete(ShareTransaction transaction) {
         ShareTransaction managedTransaction = em.merge(transaction);
         em.remove(managedTransaction);
     }
 
     public List<ShareTransaction> listForCompany(String name) {
        Query query = em.createQuery("select s from ShareTransaction s where s.company = :name order by s.when ASC, s.id ASC").setParameter("name", name);
         return new ArrayList<ShareTransaction>(query.getResultList());
     }
 
     
     
 }
