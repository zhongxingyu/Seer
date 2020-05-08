 package com.solidstategroup.radar.test.service;
 
 import com.solidstategroup.radar.model.exception.RegistrationException;
 import com.solidstategroup.radar.model.exception.UserEmailAlreadyExists;
 import com.solidstategroup.radar.model.user.PatientUser;
 import com.solidstategroup.radar.service.UserManager;
 import com.solidstategroup.radar.test.TestPvDbSchema;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 @RunWith(org.springframework.test.context.junit4.SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"classpath:context.xml"})
 public class UserManagerTest extends TestPvDbSchema {
 
     private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
     @Autowired
     private UserManager userManager;
 
     @Test(expected = RegistrationException.class)
     public void testPatientUserRegistrationUnknownRadarNumber() throws RegistrationException, UserEmailAlreadyExists {
 
         // Construct a patient user
         PatientUser patientUser = new PatientUser();
         patientUser.setRadarNumber(23232L);
         patientUser.setUsername("test_user");
         patientUser.setDateOfBirth(new Date());
 
         // Try and register - will throw an exception as no matching radar number
         userManager.registerPatient(patientUser);
     }
 
     @Test(expected = RegistrationException.class)
     public void testPatientUserRegistrationUnkownDateOfBirth() throws RegistrationException, UserEmailAlreadyExists {
 
         // Construct a patient user
         PatientUser patientUser = new PatientUser();
         patientUser.setRadarNumber(267L);
         patientUser.setUsername("test_user");
         patientUser.setDateOfBirth(new Date());
 
         // Try and register - will throw an exception as no matching radar number
         userManager.registerPatient(patientUser);
     }
 
     @Test
     public void testPatientUserRegistration() throws RegistrationException, ParseException, UserEmailAlreadyExists {
 
         // Construct a patient user
         PatientUser patientUser = new PatientUser();
         patientUser.setRadarNumber(267L);
         patientUser.setUsername("test_user");
 
         // Date of birth for this within test dataset is 6th November 1994
        patientUser.setDateOfBirth(DATE_FORMAT.parse("06/11/1991"));
 
         // Try and register - this should complete successfully as the data matches up
         userManager.registerPatient(patientUser);
     }
 
 }
