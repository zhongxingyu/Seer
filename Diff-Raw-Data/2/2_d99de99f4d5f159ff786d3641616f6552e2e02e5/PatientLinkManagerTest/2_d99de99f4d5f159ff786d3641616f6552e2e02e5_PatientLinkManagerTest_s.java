 package org.patientview.radar.test.service;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.patientview.model.Patient;
 import org.patientview.model.generic.DiseaseGroup;
 import org.patientview.radar.dao.DemographicsDao;
 import org.patientview.radar.dao.UtilityDao;
 import org.patientview.radar.model.PatientLink;
 import org.patientview.radar.service.PatientLinkManager;
 import org.patientview.radar.test.TestPvDbSchema;
 import org.springframework.test.context.ContextConfiguration;
 
 import javax.inject.Inject;
 import java.util.Date;
 import java.util.List;
 
 /**
  * User: james@solidstategroup.com
  * Date: 15/11/13
  * Time: 14:32
  */
 @RunWith(org.springframework.test.context.junit4.SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"classpath:test-context.xml"})
 public class PatientLinkManagerTest extends TestPvDbSchema {
 
     private final static String testDiseaseUnit = "Allports";
     private final static String testRenalUnit = "RENALA";
 
     @Inject
     private PatientLinkManager patientLinkManager;
 
     @Inject
     private DemographicsDao demographicsDao;
 
     @Inject
     private UtilityDao utilityDao;
 
     /**
      * A unit admin is created by the superadmin in PV.
      * <p/>
      * Create a basic PV user structure - User - Role as unit admin etc
      */
     @Before
     public void setup() {
         // Setup a Renal Unit and Disease Group
         try {
             utilityDao.createUnit(testRenalUnit);
             utilityDao.createUnit(testDiseaseUnit);
         } catch (Exception e) {
 
         }
     }
 
 
     /**
      * Test to create a link record and then query to find is that record is linked.
      *
      */
     @Test
    public void testLinkingPatientRecord() {
 
         Patient patient = new Patient();
 
         patient.setUnitcode(testRenalUnit);
         patient.setSurname("Test");
         patient.setForename("Test");
         patient.setDob(new Date());
         patient.setNhsno("234235242");
 
         DiseaseGroup diseaseGroup = new DiseaseGroup();
         diseaseGroup.setId(testDiseaseUnit);
         patient.setDiseaseGroup(diseaseGroup);
         // Save the patient record before linking because the record should already exist
         demographicsDao.saveDemographics(patient);
 
         patientLinkManager.linkPatientRecord(patient);
 
         List<PatientLink> patientLink = patientLinkManager.getPatientLink(patient.getNhsno(), testRenalUnit);
         Assert.assertTrue("The list return should not be empty." , CollectionUtils.isNotEmpty(patientLink));
         Assert.assertTrue("The should only be one link recreated", patientLink.size() == 1);
 
     }
 
 
     @After
     public void tearDown(){
         try {
             this.clearData();
         } catch (Exception e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
 
     }
 
 }
