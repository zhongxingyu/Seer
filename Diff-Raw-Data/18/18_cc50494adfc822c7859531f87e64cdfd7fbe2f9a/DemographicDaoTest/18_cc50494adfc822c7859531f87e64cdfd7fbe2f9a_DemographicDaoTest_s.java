 package com.solidstategroup.radar.test.dao;
 
 import com.solidstategroup.radar.dao.DemographicsDao;
 import com.solidstategroup.radar.model.Centre;
 import com.solidstategroup.radar.model.Demographics;
 import com.solidstategroup.radar.model.Sex;
 import com.solidstategroup.radar.model.Status;
 import com.solidstategroup.radar.model.filter.DemographicsFilter;
 import org.apache.commons.lang.StringUtils;
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import java.util.List;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 
 public class DemographicDaoTest extends BaseDaoTest {
 
     @Autowired
     private DemographicsDao demographicDao;
 
     @Test
     public void testSaveDemographics() {
         Demographics demographics = new Demographics();
         demographics.setForename("Test");
         demographics.setSurname("User");
         demographicDao.saveDemographics(demographics);
         assertNotNull(demographics.getId());
     }
 
     @Test
     public void testGetDemographic() {
         Demographics demographics = demographicDao.getDemographicsByRadarNumber(241L);
 
         // Check it's not null
         assertNotNull("Demographics was null", demographics);
 
         // Check radar number is correct
         assertEquals("Wrong radar number", new Long(241), demographics.getId());

        // We should definitely have a valid forename and surname
        assertTrue("Forename was empty", StringUtils.isNotBlank(demographics.getForename()));
        assertTrue("Surname was empty", StringUtils.isNotBlank(demographics.getSurname()));

        // Check address according to correct values in dataset
        assertEquals("Wrong address 1", "9 Lowther Road", demographics.getAddress1());
        assertEquals("Wrong address 2", "Yeovil", demographics.getAddress2());
        assertEquals("Wrong address 3", "Somerset", demographics.getAddress3());
        assertEquals("Wrong postcode", "BA21 5PF", demographics.getPostcode());

        // Check hospital number and NHS number
        assertEquals("Wrong NHS number", "4921148228", demographics.getNhsNumber());
        assertEquals("Wrong Hospital number", "1703489M", demographics.getHospitalNumber());

        // Centre should be set as 3, Bristol
        assertNotNull("Centre was null", demographics.getRenalUnit());
        assertEquals("Centre was not Bristol", demographics.getRenalUnit().getAbbreviation(), "Bristol");
     }
 
     @Test
     public void testGetDemographics() throws Exception {
         List<Demographics> demographics = demographicDao.getDemographics(new DemographicsFilter(), -1, -1);
         assertNotNull("List was null", demographics);
         assertTrue(demographics.size() > 0);
     }
 
     @Test
     public void testGetDemographicsPage1() {
         List<Demographics> demographics = demographicDao.getDemographics(new DemographicsFilter(), 1, 1);
         assertNotNull(demographics);
         assertTrue(demographics.size() == 1);
     }
 
     @Test
     public void testSearchDemographics() {
         DemographicsFilter demographicsFilter = new DemographicsFilter();
         demographicsFilter.addSearchCriteria(DemographicsFilter.UserField.DIAGNOSIS.getDatabaseFieldName(), "srns");
         List<Demographics> demographics = demographicDao.getDemographics(demographicsFilter, -1, -1);
         assertNotNull(demographics);
         assertTrue(demographics.size() > 0);
     }
 
     @Test
     public void testGetDemographicsByCentre() throws Exception {
         // Construct centre
         Centre centre = new Centre();
         centre.setId(14L);
 
         // Call DAO
         List<Demographics> demographics = demographicDao.getDemographicsByRenalUnit(centre);
         assertNotNull("List was null", demographics);
         assertEquals("Wrong size", 4, demographics.size());
         for (Demographics de : demographics) {
             assertTrue("Wrong centre", de.getRenalUnit().getId().equals(14L) || de.getRenalUnitAuthorised().getId().
                     equals(14L));
         }
     }
 
     @Test
     public void testGetSexUnknown() throws Exception {
         Sex sex = demographicDao.getSex(23232L);
         assertNull("Sex not null for unknown", sex);
     }
 
     @Test
     public void testGetSexes() throws Exception {
         List<Sex> sexes = demographicDao.getSexes();
         assertNotNull("Sexes was null", sexes);
         assertEquals("Wrong size", 3, sexes.size());
     }
 
     @Test
     public void testGetStatusUnknown() throws Exception {
         Status status = demographicDao.getStatus(23232L);
         assertNull("Status not null for unknown", status);
     }
 
     @Test
     public void testGetStatuses() throws Exception {
         List<Status> statuses = demographicDao.getStatuses();
         assertNotNull("Statuses was null", statuses);
         assertEquals("Wrong size", 6, statuses.size());
     }
 }
