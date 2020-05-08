 package org.motechproject.ghana.national.bean;
 
 import org.apache.commons.lang.StringUtils;
 import org.motechproject.ghana.national.domain.CwcCareHistory;
 import org.motechproject.ghana.national.domain.RegistrationToday;
 import org.motechproject.ghana.national.domain.mobilemidwife.MobileMidwifeEnrollment;
 import org.motechproject.ghana.national.validator.field.MotechId;
 import org.motechproject.mobileforms.api.validator.annotations.MaxLength;
 import org.motechproject.mobileforms.api.validator.annotations.RegEx;
 import org.motechproject.mobileforms.api.validator.annotations.Required;
 import org.motechproject.openmrs.omod.validator.MotechIdVerhoeffValidator;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 public class RegisterCWCForm extends MobileMidWifeIncludeForm {
     public static final String NUMERIC_OR_NOTAPPLICABLE_PATTERN = "([0-9]+(.[0-9]+)?|[nN][aA])";
     public static final String MOTECH_ID_PATTERN = "[0-9]{7}";
 
     @Required
     @MaxLength(size = 50)
     @RegEx(pattern = NUMERIC_OR_NOTAPPLICABLE_PATTERN)
     private String staffId;
 
     @Required
     @MaxLength(size = 50)
     @RegEx(pattern = NUMERIC_OR_NOTAPPLICABLE_PATTERN)
     private String facilityId;
 
     @Required
     private Date registrationDate;
 
     @Required
     @RegEx(pattern = MOTECH_ID_PATTERN)
     @MotechId(validator = MotechIdVerhoeffValidator.class)
     private String motechId;
 
     @Required
     private String serialNumber;
 
     @Required
     private RegistrationToday registrationToday;
 
     @Required
     private Boolean addHistory;
 
    @Required
     private String addCareHistory;
 
     @RegEx(pattern = "0[0-9]{9}")
     private String regPhone;
 
     public String getRegPhone() {
         return regPhone;
     }
 
     public void setRegPhone(String regPhone) {
         this.regPhone = regPhone;
     }
 
     private Date bcgDate;
     private Date lastOPVDate;
     private Date lastVitaminADate;
     private Date lastIPTiDate;
     private Date yellowFeverDate;
     private Date lastPentaDate;
     private Date measlesDate;
 
     private Integer lastOPV;
     private Integer lastIPTi;
     private Integer lastPenta;
 
     public void setRegistrationToday(RegistrationToday registrationToday) {
         this.registrationToday = registrationToday;
     }
 
     public RegistrationToday getRegistrationToday() {
         return registrationToday;
     }
 
     public String getStaffId() {
         return staffId;
     }
 
     public void setStaffId(String staffId) {
         this.staffId = staffId;
     }
 
     public String getFacilityId() {
         return facilityId;
     }
 
     public void setFacilityId(String facilityId) {
         this.facilityId = facilityId;
     }
 
     public Date getRegistrationDate() {
         return registrationDate;
     }
 
     public void setRegistrationDate(Date registrationDate) {
         this.registrationDate = registrationDate;
     }
 
     public String getMotechId() {
         return motechId;
     }
 
     public void setMotechId(String motechId) {
         this.motechId = motechId;
     }
 
     public String getSerialNumber() {
         return serialNumber;
     }
 
     public void setSerialNumber(String serialNumber) {
         this.serialNumber = serialNumber;
     }
 
     public Boolean getAddHistory() {
         return addHistory;
     }
 
     public void setAddHistory(Boolean addHistory) {
         this.addHistory = addHistory;
     }
 
     public String getAddCareHistory() {
         return addCareHistory;
     }
 
     public void setAddCareHistory(String addCareHistory) {
         this.addCareHistory = addCareHistory;
     }
 
     public Date getBcgDate() {
         return bcgDate;
     }
 
     public void setBcgDate(Date bcgDate) {
         this.bcgDate = bcgDate;
     }
 
     public Date getLastOPVDate() {
         return lastOPVDate;
     }
 
     public void setLastOPVDate(Date lastOPVDate) {
         this.lastOPVDate = lastOPVDate;
     }
 
     public Date getLastVitaminADate() {
         return lastVitaminADate;
     }
 
     public void setLastVitaminADate(Date lastVitaminADate) {
         this.lastVitaminADate = lastVitaminADate;
     }
 
     public Date getLastIPTiDate() {
         return lastIPTiDate;
     }
 
     public void setLastIPTiDate(Date lastIPTiDate) {
         this.lastIPTiDate = lastIPTiDate;
     }
 
     public Date getYellowFeverDate() {
         return yellowFeverDate;
     }
 
     public void setYellowFeverDate(Date yellowFeverDate) {
         this.yellowFeverDate = yellowFeverDate;
     }
 
     public Date getLastPentaDate() {
         return lastPentaDate;
     }
 
     public void setLastPentaDate(Date lastPentaDate) {
         this.lastPentaDate = lastPentaDate;
     }
 
     public Date getMeaslesDate() {
         return measlesDate;
     }
 
     public void setMeaslesDate(Date measlesDate) {
         this.measlesDate = measlesDate;
     }
 
     public Integer getLastOPV() {
         return lastOPV;
     }
 
     public void setLastOPV(Integer lastOPV) {
         this.lastOPV = lastOPV;
     }
 
     public Integer getLastIPTi() {
         return lastIPTi;
     }
 
     public void setLastIPTi(Integer lastIPTi) {
         this.lastIPTi = lastIPTi;
     }
 
     public Integer getLastPenta() {
         return lastPenta;
     }
 
     public void setLastPenta(Integer lastPenta) {
         this.lastPenta = lastPenta;
     }
 
     public MobileMidwifeEnrollment createMobileMidwifeEnrollment() {
         if(isEnrolledForMobileMidwifeProgram()) {
             MobileMidwifeEnrollment enrollment = fillEnrollment(new MobileMidwifeEnrollment());
             return enrollment.setStaffId(getStaffId()).setFacilityId(getFacilityId()).setPatientId(getMotechId());
         }
         return null;
     }
 
     public List<CwcCareHistory> getCWCCareHistories() {
         final ArrayList<CwcCareHistory> cwcCareHistories = new ArrayList<CwcCareHistory>();
         if (StringUtils.isNotEmpty(addCareHistory)) {
             for (String value : addCareHistory.split(" ")) {
                 cwcCareHistories.add(CwcCareHistory.valueOf(value));
             }
         }
         return cwcCareHistories;
     }
 }
