 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.sola.services.ejb.search.repository.entities;
 
 import java.math.BigDecimal;
 import java.util.Date;
 import javax.persistence.Column;
 import org.sola.services.common.repository.entities.AbstractReadOnlyEntity;
 
 public class RightsExportResult extends AbstractReadOnlyEntity {
 
     public static final String PARAM_DATE_FROM = "dateFrom";
     public static final String PARAM_DATE_TO = "dateTo";
     public static final String PARAM_RIGHT_TYPE = "rightType";
     public static final String SEARCH_QUERY =
             "SELECT DISTINCT b.id AS ba_unit_id, b.name_firstpart, b.name_lastpart, "
             + "(SELECT size FROM administrative.ba_unit_area WHERE ba_unit_id = b.id AND type_code = 'officialArea' LIMIT 1) AS area, "
             + "r.id AS right_id, r.type_code AS right_type, r.registration_date, r.expiration_date, r.amount, "
            + "(SELECT string_agg(COALESCE(p.name, '') || ' ' || COALESCE(p.last_name, ''), ',') "
             + "  FROM administrative.party_for_rrr pr "
             + "  INNER JOIN party.party p ON pr.party_id = p.id WHERE pr.rrr_id = r.id) AS owners, "
             + "p.id AS applicant_id, p.name AS applicant_name, p.last_name AS applicant_last_name, "
             + "ad.description AS applicant_address, p.phone AS applicant_phone, p.mobile AS applicant_mobile, "
             + "p.email AS applicant_email, p.id_number AS applicant_id_number, p.id_type_code AS applicant_id_type_code "
             + "FROM administrative.ba_unit b INNER JOIN "
             + "(administrative.rrr r LEFT JOIN "
             + "  (transaction.transaction t INNER JOIN "
             + "    (application.service s INNER JOIN "
             + "      (application.application a INNER JOIN "
             + "        (party.party p LEFT JOIN address.address ad ON p.address_id = ad.id) "
             + "        ON a.contact_person_id = p.id) "
             + "      ON s.application_id = a.id) "
             + "    ON t.from_service_id = s.id) "
             + "  ON r.transaction_id = t.id) "
             + "ON b.id = r.ba_unit_id "
             + "WHERE (r.type_code = #{" + PARAM_RIGHT_TYPE + "} OR #{" + PARAM_RIGHT_TYPE + "} = '') "
             + "AND b.status_code = 'current' AND r.status_code = 'current' "
             + "AND r.registration_date BETWEEN #{" + PARAM_DATE_FROM + "} AND #{" + PARAM_DATE_TO + "}";
     
     @Column(name = "ba_unit_id")
     private String baUnitId;
     @Column(name = "name_firstpart")
     private String nameFirstPart;
     @Column(name = "name_lastpart")
     private String nameLastPart;
     @Column(name = "area")
     private BigDecimal area;
     @Column(name = "right_id")
     private String rightId;
     @Column(name = "right_type")
     private String rightType;
     @Column(name = "registration_date")
     private Date registrationDate;
     @Column(name = "expiration_date")
     private Date expirationDate;
     @Column(name = "amount")
     private BigDecimal amount;
     @Column(name = "owners")
     private String owners;
     @Column(name = "applicant_id")
     private String applicantId;
     @Column(name = "applicant_name")
     private String applicantName;
     @Column(name = "applicant_last_name")
     private String applicantLastName;
     @Column(name = "applicant_address")
     private String applicantAddress;
     @Column(name = "applicant_phone")
     private String applicantPhone;
     @Column(name = "applicant_mobile")
     private String applicantMobile;
     @Column(name = "applicant_email")
     private String applicantEmail;
     @Column(name = "applicantIdNumber")
     private String applicantIdNumber;
     @Column(name = "applicant_id_type_code")
     private String applicantIdTypeCode;
 
     public RightsExportResult() {
         super();
     }
 
     public BigDecimal getAmount() {
         return amount;
     }
 
     public void setAmount(BigDecimal amount) {
         this.amount = amount;
     }
 
     public String getApplicantAddress() {
         return applicantAddress;
     }
 
     public void setApplicantAddress(String applicantAddress) {
         this.applicantAddress = applicantAddress;
     }
 
     public String getApplicantEmail() {
         return applicantEmail;
     }
 
     public void setApplicantEmail(String applicantEmail) {
         this.applicantEmail = applicantEmail;
     }
 
     public String getApplicantId() {
         return applicantId;
     }
 
     public void setApplicantId(String applicantId) {
         this.applicantId = applicantId;
     }
 
     public String getApplicantIdNumber() {
         return applicantIdNumber;
     }
 
     public void setApplicantIdNumber(String applicantIdNumber) {
         this.applicantIdNumber = applicantIdNumber;
     }
 
     public String getApplicantIdTypeCode() {
         return applicantIdTypeCode;
     }
 
     public void setApplicantIdTypeCode(String applicantIdTypeCode) {
         this.applicantIdTypeCode = applicantIdTypeCode;
     }
 
     public String getApplicantLastName() {
         return applicantLastName;
     }
 
     public void setApplicantLastName(String applicantLastName) {
         this.applicantLastName = applicantLastName;
     }
 
     public String getApplicantMobile() {
         return applicantMobile;
     }
 
     public void setApplicantMobile(String applicantMobile) {
         this.applicantMobile = applicantMobile;
     }
 
     public String getApplicantName() {
         return applicantName;
     }
 
     public void setApplicantName(String applicantName) {
         this.applicantName = applicantName;
     }
 
     public String getApplicantPhone() {
         return applicantPhone;
     }
 
     public void setApplicantPhone(String applicantPhone) {
         this.applicantPhone = applicantPhone;
     }
 
     public BigDecimal getArea() {
         return area;
     }
 
     public void setArea(BigDecimal area) {
         this.area = area;
     }
 
     public String getBaUnitId() {
         return baUnitId;
     }
 
     public void setBaUnitId(String baUnitId) {
         this.baUnitId = baUnitId;
     }
 
     public Date getExpirationDate() {
         return expirationDate;
     }
 
     public void setExpirationDate(Date expirationDate) {
         this.expirationDate = expirationDate;
     }
 
     public String getNameFirstPart() {
         return nameFirstPart;
     }
 
     public void setNameFirstPart(String nameFirstPart) {
         this.nameFirstPart = nameFirstPart;
     }
 
     public String getNameLastPart() {
         return nameLastPart;
     }
 
     public void setNameLastPart(String nameLastPart) {
         this.nameLastPart = nameLastPart;
     }
 
     public String getOwners() {
         return owners;
     }
 
     public void setOwners(String owners) {
         this.owners = owners;
     }
 
     public Date getRegistrationDate() {
         return registrationDate;
     }
 
     public void setRegistrationDate(Date registrationDate) {
         this.registrationDate = registrationDate;
     }
 
     public String getRightType() {
         return rightType;
     }
 
     public void setRightType(String rightType) {
         this.rightType = rightType;
     }
 
     public String getRightId() {
         return rightId;
     }
 
     public void setRightId(String rightId) {
         this.rightId = rightId;
     }
 }
