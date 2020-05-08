 package org.motechproject.ghana.national.web.form;
 
import org.motechproject.ghana.national.domain.Facility;
import org.motechproject.ghana.national.web.helper.FacilityHelper;

 import java.util.Date;
 
 public class CWCEnrollmentForm {
 
     private String patientMotechId;
     private String serialNumber;
     private Date registrationDate;
     private FacilityForm facilityForm;
 
    public CWCEnrollmentForm(CWCEnrollment CWCEnrollment, Facility facility) {
        if (CWCEnrollment != null) {
            patientMotechId = CWCEnrollment.getPatientId();
            serialNumber = CWCEnrollment.getSerialNumber();
            registrationDate = CWCEnrollment.getRegistrationDate();
            facilityForm = new FacilityHelper().copyFacilityValuesToForm(facility);
        }
    }

     public FacilityForm getFacilityForm() {
         return facilityForm;
     }
 
     public void setFacilityForm(FacilityForm facilityForm) {
         this.facilityForm = facilityForm;
     }
 
     public String getPatientMotechId() {
         return patientMotechId;
     }
 
     public void setPatientMotechId(String patientMotechId) {
         this.patientMotechId = patientMotechId;
     }
 
     public String getSerialNumber() {
         return serialNumber;
     }
 
     public void setSerialNumber(String serialNumber) {
         this.serialNumber = serialNumber;
     }
 
     public Date getRegistrationDate() {
         return registrationDate;
     }
 
     public void setRegistrationDate(Date registrationDate) {
         this.registrationDate = registrationDate;
     }
 }
