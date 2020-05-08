 package au.com.dius.resilience.persistence;
 
 import android.test.AndroidTestCase;
 import au.com.dius.resilience.model.Incident;
 import au.com.dius.resilience.model.IncidentFactory;
 
 import java.util.List;
 
 public class SqlLiteRepositoryTest extends AndroidTestCase {
 
   public void testSqlLiteSaveAndLoad() {
     SqlLiteRepository sqlLiteRepository = new SqlLiteRepository(getContext());
 
<<<<<<< HEAD
     sqlLiteRepository.save(IncidentFactory.createIncident("Test1", Long.valueOf(999), "Hello World"));
     sqlLiteRepository.save(IncidentFactory.createIncident("Test2", Long.valueOf(888), "Goodbye World"));
     sqlLiteRepository.save(IncidentFactory.createIncident("Test3", Long.valueOf(777), "Sup, World?"));
=======
    sqlLiteRepository.save(IncidentFactory.createIncident("Test1", Long.valueOf(999), "Hello World", "Cat1", "Subcat1", ImpactScale.LOW));
    sqlLiteRepository.save(IncidentFactory.createIncident("Test2", Long.valueOf(888), "Goodbye World", "Cat2", "Subcat2", ImpactScale.MEDIUM));
    sqlLiteRepository.save(IncidentFactory.createIncident("Test3", Long.valueOf(777), "Sup, World?", "Cat3", "Subcat3", ImpactScale.HIGH));
>>>>>>> 2ed11af... merge fix
 
     List<Incident> findAll = sqlLiteRepository.findAll();
 
     assertEquals(3, findAll.size());
 
     assertNotNull(findAll.get(0).getId());
     assertNotNull(findAll.get(1).getId());
     assertNotNull(findAll.get(2).getId());
 
     assertTrue("Test1".equals(findAll.get(0).getName()));
     assertTrue("Test2".equals(findAll.get(1).getName()));
     assertTrue("Test3".equals(findAll.get(2).getName()));
 
     assertTrue(999 == findAll.get(0).getDateCreated());
     assertTrue(888 == findAll.get(1).getDateCreated());
     assertTrue(777 == findAll.get(2).getDateCreated());
 
     assertTrue("Hello World".equals(findAll.get(0).getNote()));
     assertTrue("Goodbye World".equals(findAll.get(1).getNote()));
     assertTrue("Sup, World?".equals(findAll.get(2).getNote()));
   }
 
 }
