 package nl.surfnet.bod.domain;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.greaterThan;
 import static org.hamcrest.Matchers.hasSize;
 import static org.hamcrest.Matchers.is;
 import static org.hamcrest.Matchers.nullValue;
 
 import java.util.List;
 
 import javax.validation.ConstraintViolationException;
 
 import nl.surfnet.bod.repo.PhysicalPortRepo;
 import nl.surfnet.bod.service.PhysicalPortService;
 import nl.surfnet.bod.support.PhysicalPortDataOnDemand;
 import nl.surfnet.bod.support.PhysicalPortFactory;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.transaction.annotation.Transactional;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = "classpath:/spring/appCtx*.xml")
 @Transactional
 public class PhysicalPortRepoImplDbTest {
 
   @Autowired
   private PhysicalPortDataOnDemand dod;
 
   @Autowired
   private PhysicalPortService physicalPortService;
 
   @Autowired
   private PhysicalPortRepo physicalPortRepo;
 
   @Test
   public void countAllPhysicalPorts() {
     dod.getRandomPhysicalPort();
 
     long count = physicalPortService.count();
 
     assertThat(count, greaterThan(0L));
   }
 
   @Test
   public void findPhysicalPort() {
     PhysicalPort obj = dod.getRandomPhysicalPort();
 
     PhysicalPort freshObj = physicalPortService.find(obj.getId());
 
     assertThat(obj, is(freshObj));
   }
 
   @Test
   public void testFindPhysicalPortEntries() {
     dod.getRandomPhysicalPort();
     int count = (int) physicalPortService.count();
 
     int maxResults = count > 20 ? 20 : count;
 
     List<PhysicalPort> result = physicalPortService.findEntries(0, maxResults);
 
    assertThat(result, hasSize(maxResults));
   }
 
   @Test
   public void updatePhysicalPortUpdate() {
     PhysicalPort obj = dod.getRandomPhysicalPort();
 
     Integer initialVersion = obj.getVersion();
     obj.setName("New name");
 
     PhysicalPort merged = physicalPortService.update(obj);
 
     physicalPortRepo.flush();
 
     assertThat(merged.getId(), is(obj.getId()));
     assertThat(merged.getVersion(), greaterThan(initialVersion));
   }
 
   @Test
   public void savePhysicalPort() {
     PhysicalPort obj = dod.getNewTransientPhysicalPort(Integer.MAX_VALUE);
     physicalPortService.save(obj);
     physicalPortRepo.flush();
 
     assertThat(obj.getId(), greaterThan(0L));
   }
 
   @Test
   public void deletePhysicalPort() {
     PhysicalPort obj = dod.getRandomPhysicalPort();
 
     physicalPortService.delete(obj);
     physicalPortRepo.flush();
 
     assertThat(physicalPortService.find(obj.getId()), nullValue());
   }
 
   @Test(expected = ConstraintViolationException.class)
   public void aPortShouldNotSaveWithoutAName() {
     PhysicalPort port = new PhysicalPortFactory().setName("").create();
 
     physicalPortService.save(port);
   }
 }
