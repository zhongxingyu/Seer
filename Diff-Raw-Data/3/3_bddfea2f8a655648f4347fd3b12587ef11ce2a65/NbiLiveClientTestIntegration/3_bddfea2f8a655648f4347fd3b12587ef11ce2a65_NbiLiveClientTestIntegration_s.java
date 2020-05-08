 package nl.surfnet.bod.nbi;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.greaterThan;
 import static org.hamcrest.Matchers.hasSize;
 
 import java.util.List;
 
 import nl.surfnet.bod.nbi.generated.TerminationPoint;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = "classpath:/spring/appCtx*.xml")
 public class NbiLiveClientTestIntegration {
 
   @Autowired
   private NbiClient nbiClient;
 
   @Test
   public void testFindAllPortsWithDetails() {
     final List<TerminationPoint> allTerminationPoints = nbiClient.findAllPorts();
 
     assertThat(allTerminationPoints, hasSize(greaterThan(0)));
   }
 
 }
