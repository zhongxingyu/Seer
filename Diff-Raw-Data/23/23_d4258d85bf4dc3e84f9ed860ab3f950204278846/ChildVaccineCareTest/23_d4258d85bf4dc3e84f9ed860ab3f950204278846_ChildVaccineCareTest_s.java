 package org.motechproject.ghana.national.domain.care;
 
 import org.joda.time.LocalDate;
 import org.junit.Test;
 import org.motechproject.ghana.national.domain.Patient;
 import org.motechproject.mrs.model.MRSFacility;
 import org.motechproject.mrs.model.MRSPatient;
 import org.motechproject.mrs.model.MRSPerson;
 import org.motechproject.util.DateUtil;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 public class ChildVaccineCareTest {
     @Test
     public void shouldCheckIfCapturedHistoryDateFallsWithinFiveYearsOfAgeOfChild() {
         final LocalDate birthDate = DateUtil.today().minusMonths(5);
         final LocalDate lastPentaDate = birthDate.plusMonths(2);
         final Patient patient = new Patient(new MRSPatient("mrsPatientId", new MRSPerson().dateOfBirth(birthDate.toDate()), new MRSFacility("facilityId")));
         ChildVaccineCare childVaccineCare = new PentaVaccineCare(patient, DateUtil.today(), false, "PENTA1", lastPentaDate.toDate());
         assertTrue(childVaccineCare.isWithinActiveRange());
     }
 
     @Test
     public void shouldCheckIfCapturedHistoryDateDoesnotFallWithinFiveYearsOfAgeOfChild() {
         final LocalDate birthDate = DateUtil.today().minusMonths(5);
         final Patient patient = new Patient(new MRSPatient("mrsPatientId", new MRSPerson().dateOfBirth(birthDate.toDate()), new MRSFacility("facilityId")));
         LocalDate lastPentaDate = birthDate.plusYears(6);
 
         ChildVaccineCare childVaccineCare = new PentaVaccineCare(patient, DateUtil.today(), false, "PENTA1", lastPentaDate.toDate());
         assertFalse(childVaccineCare.isWithinActiveRange());
 
 
         lastPentaDate = birthDate.minusYears(1);
         childVaccineCare = new PentaVaccineCare(patient, DateUtil.today(), false, "PENTA1", lastPentaDate.toDate());
         assertFalse(childVaccineCare.isWithinActiveRange());
     }
 
 }
