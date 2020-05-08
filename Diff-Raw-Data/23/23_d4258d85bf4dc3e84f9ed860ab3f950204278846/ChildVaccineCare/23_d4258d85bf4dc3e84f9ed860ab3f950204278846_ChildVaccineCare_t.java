 package org.motechproject.ghana.national.domain.care;
 
 import org.joda.time.LocalDate;
 import org.motechproject.ghana.national.domain.Patient;
 
 import java.util.Date;
 
 import static org.motechproject.util.DateUtil.newDate;
 
 public abstract class ChildVaccineCare extends VaccineCare {
     protected LocalDate dateOfBirth;
 
     public ChildVaccineCare(Patient patient, Boolean hasActiveSchedule, String vaccineName, String lastDosage, Date lastTakenDate) {
         super(patient, hasActiveSchedule, vaccineName, lastDosage, lastTakenDate);
         this.dateOfBirth = newDate(patient.dateOfBirth());
     }
 
     @Override
     public boolean isWithinActiveRange() {
         return isWithinFiveYearsOfAge();
     }
 
     private boolean isWithinFiveYearsOfAge() {
         if (dateOfBirth != null) {
             LocalDate maxAge = dateOfBirth.plusYears(5);
            return lastTakenDate != null && !lastTakenDate.before(dateOfBirth.toDate()) && !lastTakenDate.after(maxAge.toDate());
         }
         return false;
     }
 }
