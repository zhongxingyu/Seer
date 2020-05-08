 package org.patientview.radar.test.service;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.junit.Before;
import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.patientview.model.Unit;
 import org.patientview.radar.model.user.User;
 import org.patientview.radar.service.UnitManager;
 import org.patientview.radar.test.TestDataHelper;
 import org.patientview.radar.test.TestPvDbSchema;
 import org.patientview.radar.test.roles.unitadmin.RoleHelper;
 import org.springframework.test.context.ContextConfiguration;
 
 import javax.inject.Inject;
 import java.util.List;
 
 import static org.junit.Assert.assertTrue;
 
 /**
  * User: james@solidstategroup.com
  * Date: 04/12/13
  * Time: 18:00
  */
 @RunWith(org.springframework.test.context.junit4.SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"classpath:test-context.xml"})
 public class UnitManagerTest extends TestPvDbSchema {
 
     @Inject
     private UnitManager unitManager;
 
     @Inject
     private RoleHelper roleHelper;
 
     @Inject
     private TestDataHelper testDataHelper;
 
 
     @Before
     public void setup(){
         // Create the following radar units "1,2,58"
         // Create the following renal units "RWM51, 5"
         testDataHelper.createUnit();
     }
 
     @Test
    @Ignore // Incomplete test
     public void testGetRenalUnitsForUser() throws Exception {
 
         User user = roleHelper.createUnitAdmin("9999999999", "RW51");
 
         List<Unit> units = unitManager.getRenalUnits(user);
 
         assertTrue("The unit admin has one Renal Unit" , CollectionUtils.isNotEmpty(units) && units.size() == 1);
 
     }
 
 }
