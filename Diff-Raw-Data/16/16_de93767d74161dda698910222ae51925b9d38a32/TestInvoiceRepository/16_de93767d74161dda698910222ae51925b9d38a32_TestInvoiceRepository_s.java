 package db;
 
 import org.hibernate.SessionFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Isolation;
 import org.springframework.transaction.annotation.Transactional;
 
 @Repository
 public class TestInvoiceRepository {
 
     @Autowired
     protected SessionFactory sessionFactory;
 
     @Transactional(isolation = Isolation.READ_COMMITTED)
     public void deleteAll() {
        sessionFactory.getCurrentSession().createSQLQuery("truncate table invoice").executeUpdate();
     }
 }
