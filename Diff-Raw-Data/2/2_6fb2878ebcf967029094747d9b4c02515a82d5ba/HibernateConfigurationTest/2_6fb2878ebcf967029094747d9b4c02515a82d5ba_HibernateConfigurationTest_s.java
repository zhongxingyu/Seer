 package enterprise.hibernate;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 import org.hibernate.SessionFactory;
 import org.hibernate.Session;
 
 /**
  *
  * @author paul
  */
 public class HibernateConfigurationTest {
 
     public HibernateConfigurationTest() {
     }
 
     /**
      * Test if a hibernate session factory is configured properly
      */
     @Test
     public void ConfigureHibernateSessionFactoryTest() {
         SessionFactory sessionFactory = EnterpriseHibernateUtil.getSessionFactory();
 
         assertNotNull(sessionFactory);
 
         Session currentSession = sessionFactory.openSession();
 
         assertNotNull(currentSession);
 
         Long id = Long.valueOf(1L);
         Report report = (Report) currentSession.get(Report.class, id);
 
         //FIXME: remove that
         System.out.println(report.getPerformer());
        
         assertNotNull(report);
         assertEquals(report.getId(), id);
 
         currentSession.close();
         sessionFactory.close();
 
     }
 }
