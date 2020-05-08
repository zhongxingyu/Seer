 package gov.abrs.etms.service.report;
 
 import static org.junit.Assert.*;
 import gov.abrs.etms.model.rept.Rept;
 
 import java.util.List;
 
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 
 import com.google.common.collect.Lists;
 import com.taiji.common.test.DatabaseTestCase;
 
@ContextConfiguration(locations = { "/conf/spring/applicationContext-test.xml" })
 public class ReptServiceTest extends DatabaseTestCase {
 
 	@Autowired
 	private ReptService reptService;
 
 	@Test
 	public void testGetReptGroups() {
 		String reptTime = "201101";
 		List<ReptGroup> list = reptService.getReptGroups(reptTime);
 		List<Rept> abnRepts = Lists.newArrayList();
 		List<Rept> imptRepts = Lists.newArrayList();
 		for (ReptGroup reptGroup : list) {
 			if (reptGroup instanceof AbnReptGroup) {
 				abnRepts = reptGroup.getRepts();
 			}
 			if (reptGroup instanceof ImptReptGroup) {
 				imptRepts = reptGroup.getRepts();
 			}
 		}
 		assertNotNull(list);
 		assertEquals(2, list.size());
 		assertEquals(new Long(5), abnRepts.get(1).getId());
 		assertEquals(new Long(2), imptRepts.get(1).getId());
 	}
 
 	@Test
 	public void testGetAbnReptGroup() {
 		String reptTime = "201101";
 		ReptGroup reptGroup = reptService.getAbnReptGroup(reptTime);
 		List<Rept> abnRepts = reptGroup.getRepts();
 		assertEquals(new Long(5), abnRepts.get(1).getId());
 	}
 
 	@Test
 	public void testGetReptGroup() {
 		String reptTime = "201101";
 		ReptGroup reptGroup = reptService.getReptGroup(reptTime, "ACCD");
 		List<Rept> abnRepts = reptGroup.getRepts();
 		assertEquals(new Long(5), abnRepts.get(1).getId());
 	}
 }
