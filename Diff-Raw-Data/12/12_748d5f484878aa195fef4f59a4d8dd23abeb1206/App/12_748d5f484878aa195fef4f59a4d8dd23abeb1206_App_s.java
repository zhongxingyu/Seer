 package demo;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.persistence.EntityManager;
 import javax.persistence.Persistence;
 
 import org.eclipse.persistence.config.EntityManagerProperties;
 import org.eclipse.persistence.config.PersistenceUnitProperties;
 
 public class App 
 {
     public static void main( String[] args )
     {
         Map<String, String> properties = new HashMap<String, String>();
         properties.put(PersistenceUnitProperties.MULTITENANT_PROPERTY_DEFAULT, "707");   
         EntityManager em = Persistence.createEntityManagerFactory("multi-tenant-pu", properties).createEntityManager();
         
         //EntityManager em = Persistence.createEntityManagerFactory("multi-tenant-pu").createEntityManager();
         em.getTransaction().begin();
        em.setProperty("other.tenant.id.property", "707");
        em.setProperty(EntityManagerProperties.MULTITENANT_PROPERTY_DEFAULT, "707");
 
         Customer customer = new Customer();
         customer.setName("AMCE");
         em.persist(customer);
         em.getTransaction().commit();
     }
 }
