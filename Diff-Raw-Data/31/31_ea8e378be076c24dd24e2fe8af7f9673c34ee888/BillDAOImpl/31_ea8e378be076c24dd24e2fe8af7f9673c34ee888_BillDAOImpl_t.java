 package com.uhsarp.billrive.dao.framework.impl;
  
  import com.googlecode.genericdao.search.ExampleOptions;
  import com.googlecode.genericdao.search.Filter;
  import com.googlecode.genericdao.search.ISearch;
  import com.googlecode.genericdao.search.SearchResult;
  import com.uhsarp.billrive.dao.framework.BillDAO;
  import com.uhsarp.billrive.domain.Bill;
 import com.uhsarp.billrive.domain.User;
 import com.uhsarp.billrive.webservices.rest.BillController;
  import java.util.List;
  import javax.persistence.EntityManager;
  import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 import org.apache.log4j.Logger;
  import org.springframework.stereotype.Repository;
  import org.springframework.transaction.annotation.Transactional;
  
  /**
   *
   * @author Prashanth Batchu
   */
  @Repository("billDAO")
  @Transactional
  public class BillDAOImpl  implements BillDAO{
  //public class BillDAOImpl extends JpaConfig  implements BillDAO{
  
       private static final Logger logger_c = Logger.getLogger(BillController.class);
         @PersistenceContext
  	private EntityManager em;
      
      public Bill find(Long id) {
          throw new UnsupportedOperationException("Not supported yet."); //To change body of generated  methods, choose Tools | Templates.
      }
  
      public Bill[] find(Long... ids) {
          throw new UnsupportedOperationException("Not supported yet."); //To change body of generated  methods, choose Tools | Templates.
      }
  
      public Bill getReference(Long id) {
          throw new UnsupportedOperationException("Not supported yet."); //To change body of generated  methods, choose Tools | Templates.
      }
  
      public Bill[] getReferences(Long... ids) {
          throw new UnsupportedOperationException("Not supported yet."); //To change body of generated  methods, choose Tools | Templates.
      }
  
      public void persist(Bill... ts) {
          throw new UnsupportedOperationException("Not supported yet."); //To change body of generated  methods, choose Tools | Templates.
      }
  
      public Bill merge(Bill t) {
          throw new UnsupportedOperationException("Not supported yet."); //To change body of generated  methods, choose Tools | Templates.
      }
  
      public Bill[] merge(Bill... ts) {
          throw new UnsupportedOperationException("Not supported yet."); //To change body of generated  methods, choose Tools | Templates.
      }
  
   
  @Transactional
      public Bill save(Bill bill) {
          if (bill.getId() == null) {
                         try{
                             em.persist(bill);
                         }
                         catch(Exception e){
                         logger_c.debug(e);
                         }
                         
  			return bill;
 		} 
  			return em.merge(bill);
      }
  
      public Bill[] save(Bill... ts) {
          throw new UnsupportedOperationException("Not supported yet."); //To change body of generated  methods, choose Tools | Templates.
      }
  
      public boolean remove(Bill t) {
         
          return false;
      }
  
      public void remove(Bill... ts) {
          throw new UnsupportedOperationException("Not supported yet."); //To change body of generated  methods, choose Tools | Templates.
      }
  
      public boolean removeById(Long billId) {
          
          Query q =  em.createQuery("delete from Bill b  WHERE b.id="+billId);    
         int deleted = q.executeUpdate();
         if(deleted>0)
             return true;
         else
          return false;
      }
  
      public void removeByIds(Long... ids) {
          throw new UnsupportedOperationException("Not supported yet."); //To change body of generated  methods, choose Tools | Templates.
      }
  
      public List<Bill> findAll() {
          throw new UnsupportedOperationException("Not supported yet."); //To change body of generated  methods, choose Tools | Templates.
      }
  
      public <RT> List<RT> search(ISearch is) {
          throw new UnsupportedOperationException("Not supported yet."); //To change body of generated  methods, choose Tools | Templates.
      }
  
      public <RT> RT searchUnique(ISearch is) {
          throw new UnsupportedOperationException("Not supported yet."); //To change body of generated  methods, choose Tools | Templates.
      }
  
      public int count(ISearch is) {
          throw new UnsupportedOperationException("Not supported yet."); //To change body of generated  methods, choose Tools | Templates.
      }
  
      public <RT> SearchResult<RT> searchAndCount(ISearch is) {
          throw new UnsupportedOperationException("Not supported yet."); //To change body of generated  methods, choose Tools | Templates.
      }
  
      public boolean isAttached(Bill t) {
          throw new UnsupportedOperationException("Not supported yet."); //To change body of generated  methods, choose Tools | Templates.
      }
  
      public void refresh(Bill... ts) {
          throw new UnsupportedOperationException("Not supported yet."); //To change body of generated  methods, choose Tools | Templates.
      }
  
      public void flush() {
          throw new UnsupportedOperationException("Not supported yet."); //To change body of generated  methods, choose Tools | Templates.
      }
  
      public Filter getFilterFromExample(Bill t) {
          throw new UnsupportedOperationException("Not supported yet."); //To change body of generated  methods, choose Tools | Templates.
      }
  
      public Filter getFilterFromExample(Bill t, ExampleOptions eo) {
          throw new UnsupportedOperationException("Not supported yet."); //To change body of generated  methods, choose Tools | Templates.
      }
  
      public List<Bill> getBillsByGroupId(Long groupId) {
         return em.createQuery("select t from Bill t WHERE t.groupId = :groupId")
  //       return this.getEntityManager().createQuery("select t from Bill t WHERE t.groupId = :groupId" 
                  .setParameter("groupId", groupId)
                  .getResultList();
            
          
      }
      
  }
   
