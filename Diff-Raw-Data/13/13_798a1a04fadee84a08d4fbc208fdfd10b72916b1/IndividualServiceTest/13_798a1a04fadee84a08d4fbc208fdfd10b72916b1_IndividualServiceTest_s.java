 package fr.cg95.cvq.service.users;
 
 import org.junit.Test;
 
 import fr.cg95.cvq.business.users.Individual;
 import fr.cg95.cvq.exception.CvqException;
 import fr.cg95.cvq.testtool.ServiceTestCase;
 import static org.junit.Assert.*;
 
 /**
  * The tests for the individual service.
  *
  * @author vsi@zenexity.com
  */
 public class IndividualServiceTest extends ServiceTestCase {
 
     @Test
     public void testAssignLogin() throws CvqException {
         Individual emilie = new Individual();
         Individual jean = new Individual();
 
         // Test with apostrophe
         String lnA = "L'ALBATROS";
         String fnA = "Emilie";
         String loginA = "emilie.lalbatros";
 
         emilie.setLastName(lnA);
         emilie.setFirstName(fnA);
         individualService.assignLogin(emilie);
 
         // Test with accent
         String lnB = "DUPÖÑT";
         String fnB = "Jean";
         String loginB = "jean.dupont";
 
         jean.setLastName(lnB);
         jean.setFirstName(fnB);
         individualService.assignLogin(jean);
 
         assertEquals(loginA, emilie.getLogin());
         assertEquals(loginB, jean.getLogin());
     }
 }
