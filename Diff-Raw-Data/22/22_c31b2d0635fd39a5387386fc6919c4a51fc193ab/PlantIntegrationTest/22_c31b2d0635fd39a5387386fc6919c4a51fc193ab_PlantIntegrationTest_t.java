 package cs.ut.domain;
 import static org.junit.Assert.assertTrue;
 import cs.ut.repository.PlantRepository;
 
 import java.math.BigDecimal;
 import java.text.SimpleDateFormat;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.validation.ConstraintViolation;
 import javax.validation.ConstraintViolationException;
 
 import org.joda.time.DateTime;
 import org.junit.Assert;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.roo.addon.test.RooIntegrationTest;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.transaction.annotation.Transactional;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = "classpath*:/META-INF/spring/applicationContext*.xml")
 @Transactional
 @RooIntegrationTest(entity = Plant.class)
 public class PlantIntegrationTest {
 
     @Test
     public void testMarkerMethod() {
     }
 
 	@Autowired
     PlantDataOnDemand dod;
 
 	@Autowired
     PlantRepository plantRepository;
 
 	@Test
     public void testCount() {
         Assert.assertNotNull("Data on demand for 'Plant' failed to initialize correctly", dod.getRandomPlant());
         long count = plantRepository.count();
         Assert.assertTrue("Counter for 'Plant' incorrectly reported there were no entries", count > 0);
     }
 
 	@Test
     public void testFind() {
         Plant obj = dod.getRandomPlant();
         Assert.assertNotNull("Data on demand for 'Plant' failed to initialize correctly", obj);
         Long id = obj.getId();
         Assert.assertNotNull("Data on demand for 'Plant' failed to provide an identifier", id);
         obj = plantRepository.findOne(id);
         Assert.assertNotNull("Find method for 'Plant' illegally returned null for id '" + id + "'", obj);
         Assert.assertEquals("Find method for 'Plant' returned the incorrect identifier", id, obj.getId());
     }
 
 	@Test
     public void testFindAll() {
         Assert.assertNotNull("Data on demand for 'Plant' failed to initialize correctly", dod.getRandomPlant());
         long count = plantRepository.count();
         Assert.assertTrue("Too expensive to perform a find all test for 'Plant', as there are " + count + " entries; set the findAllMaximum to exceed this value or set findAll=false on the integration test annotation to disable the test", count < 250);
         List<Plant> result = plantRepository.findAll();
         Assert.assertNotNull("Find all method for 'Plant' illegally returned null", result);
         Assert.assertTrue("Find all method for 'Plant' failed to return any data", result.size() > 0);
     }
 
 	@Test
     public void testFindEntries() {
         Assert.assertNotNull("Data on demand for 'Plant' failed to initialize correctly", dod.getRandomPlant());
         long count = plantRepository.count();
         if (count > 20) count = 20;
         int firstResult = 0;
         int maxResults = (int) count;
         List<Plant> result = plantRepository.findAll(new org.springframework.data.domain.PageRequest(firstResult / maxResults, maxResults)).getContent();
         Assert.assertNotNull("Find entries method for 'Plant' illegally returned null", result);
         Assert.assertEquals("Find entries method for 'Plant' returned an incorrect number of entries", count, result.size());
     }
 
 	@Test
     public void testFlush() {
         Plant obj = dod.getRandomPlant();
         Assert.assertNotNull("Data on demand for 'Plant' failed to initialize correctly", obj);
         Long id = obj.getId();
         Assert.assertNotNull("Data on demand for 'Plant' failed to provide an identifier", id);
         obj = plantRepository.findOne(id);
         Assert.assertNotNull("Find method for 'Plant' illegally returned null for id '" + id + "'", obj);
         boolean modified =  dod.modifyPlant(obj);
         Integer currentVersion = obj.getVersion();
         plantRepository.flush();
         Assert.assertTrue("Version for 'Plant' failed to increment on flush directive", (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
     }
 
 	@Test
     public void testSaveUpdate() {
         Plant obj = dod.getRandomPlant();
         Assert.assertNotNull("Data on demand for 'Plant' failed to initialize correctly", obj);
         Long id = obj.getId();
         Assert.assertNotNull("Data on demand for 'Plant' failed to provide an identifier", id);
         obj = plantRepository.findOne(id);
         boolean modified =  dod.modifyPlant(obj);
         Integer currentVersion = obj.getVersion();
         Plant merged = plantRepository.save(obj);
         plantRepository.flush();
         Assert.assertEquals("Identifier of merged object not the same as identifier of original object", merged.getId(), id);
         Assert.assertTrue("Version for 'Plant' failed to increment on merge and flush directive", (currentVersion != null && obj.getVersion() > currentVersion) || !modified);
     }
 
 	@Test
     public void testSave() {
         Assert.assertNotNull("Data on demand for 'Plant' failed to initialize correctly", dod.getRandomPlant());
         Plant obj = dod.getNewTransientPlant(Integer.MAX_VALUE);
         Assert.assertNotNull("Data on demand for 'Plant' failed to provide a new transient entity", obj);
         Assert.assertNull("Expected 'Plant' identifier to be null", obj.getId());
         try {
             plantRepository.save(obj);
         } catch (final ConstraintViolationException e) {
             final StringBuilder msg = new StringBuilder();
             for (Iterator<ConstraintViolation<?>> iter = e.getConstraintViolations().iterator(); iter.hasNext();) {
                 final ConstraintViolation<?> cv = iter.next();
                 msg.append("[").append(cv.getRootBean().getClass().getName()).append(".").append(cv.getPropertyPath()).append(": ").append(cv.getMessage()).append(" (invalid value = ").append(cv.getInvalidValue()).append(")").append("]");
             }
             throw new IllegalStateException(msg.toString(), e);
         }
         plantRepository.flush();
         Assert.assertNotNull("Expected 'Plant' identifier to no longer be null", obj.getId());
     }
 	
 	@Test
 	public void testGetAvailablePlantsByDateRange() {
 		DateTime startDate = new DateTime();
 		DateTime endDate = startDate.plusDays(7);
 		
		List<Plant> availablePlants2 = plantRepository.findByDateRange(startDate.toDate(), endDate.toDate(), HireRequestStatus.PENDING_CONFIRMATION);
 		
 		long plantId;
 		Plant p = new Plant();
 		p.setDescription("Desc");
 		p.setName("NotAvailablePlant");
 		p.setPricePerDay(new BigDecimal(200));
 		p.persist();
 		plantId = p.getId();
 
 		PurchaseOrder po = new PurchaseOrder();
 		
 		po.setEndDate(endDate.toDate());
 		po.setPlant(Plant.findPlant(plantId));	
 		po.setStartDate(startDate.toDate());
 		po.setStatus(HireRequestStatus.OPEN);
 		po.setTotalCost(new BigDecimal(2));
 		po.persist();
 
		List<Plant> availablePlants = plantRepository.findByDateRange(startDate.toDate(), endDate.toDate(), HireRequestStatus.PENDING_CONFIRMATION);
 		for (Plant plant : availablePlants) {
 			assertTrue(!plant.getName().equals("NotAvailablePlant"));
 		}
 	}
 
 	@Test
     public void testDelete() {
         
     }
 }
