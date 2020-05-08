 package com.worthsoln.patientview.model;
 
 import com.worthsoln.ibd.Ibd;
import com.worthsoln.patientview.parser.ResultParser;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Transient;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 @Entity
 public class Patient extends BaseModel {
 
     @Transient
     private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
 
     @Transient
     private final SimpleDateFormat UK_DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");
 
     @Column(nullable = false)
     private String nhsno;
     @Column
     private String surname;
     @Column
     private String forename;
     @Column
     private String dateofbirth;
     @Column
     private String sex;
     @Column
     private String address1;
     @Column
     private String address2;
     @Column
     private String address3;
     @Column
     private String address4;
     @Column
     private String postcode;
     @Column
     private String telephone1;
     @Column
     private String telephone2;
     @Column
     private String mobile;
     @Column(nullable = false)
     private String centreCode;
     @Column
     private String diagnosis;
     @Column
     private Date diagnosisDate;
     @Column
     private String treatment;
     @Column
     private String transplantstatus;
     @Column
     private String hospitalnumber;
     @Column
     private String gpname;
     @Column
     private String gpaddress1;
     @Column
     private String gpaddress2;
     @Column
     private String gpaddress3;
     @Column
     private String gppostcode;
     @Column
     private String gptelephone;
     @Column
     private String gpemail;
     @Column
     private Date bmdexam;
     @Column
     private String bloodgroup;
 
     @Column(nullable = true, columnDefinition = "TEXT")
     // Note: this is used at the moment for IBD only.  The patient details view shows a separate "Other Conditions"
     // which is pulling in through DiagnosisManager.getOtherDiagnoses()
     private String otherConditions;
 
     private static final Logger LOGGER = LoggerFactory.getLogger(Patient.class);
 
     public Patient() {
     }
 
     public Patient(String nhsno, String centreCode) {
         this.nhsno = nhsno;
         this.centreCode = centreCode;
     }
 
     public Patient(String nhsno, String surname, String forename, String dateofbirth, String sex, String address1,
                    String address2, String address3, String address4, String postcode, String telephone1,
                    String telephone2, String mobile, String centreCode, String diagnosis, String diagnosisDate,
                    String treatment, String transplantstatus, String hospitalnumber, String gpname, String gpaddress1,
                    String gpaddress2, String gpaddress3, String gppostcode, String gptelephone, String gpemail,
                    String bmdexam, String bloodgroup) {
         this.address1 = address1;
         this.address2 = address2;
         this.address3 = address3;
         this.address4 = address4;
         setCentreCode(centreCode);
         this.dateofbirth = dateofbirth;
         this.forename = forename;
         this.nhsno = nhsno;
         this.postcode = postcode;
         this.sex = sex;
         this.surname = surname;
         this.telephone1 = telephone1;
         this.telephone2 = telephone2;
         this.mobile = mobile;
         this.diagnosis = diagnosis;
         if (diagnosisDate != null) {
             try {
                this.diagnosisDate = ResultParser.IMPORT_DATE_FORMAT.parse(diagnosisDate);
             } catch (ParseException e) {
                 LOGGER.error("Could not parse diagnosisDate {} {}", diagnosisDate, e);
             }
         }
         this.treatment = treatment;
         this.transplantstatus = transplantstatus;
         this.hospitalnumber = hospitalnumber;
         this.gpname = gpname;
         this.gpaddress1 = gpaddress1;
         this.gpaddress2 = gpaddress2;
         this.gpaddress3 = gpaddress3;
         this.gppostcode = gppostcode;
         this.gptelephone = gptelephone;
         this.gpemail = gpemail;
         if (bmdexam != null) {
             try {
                 this.bmdexam = Ibd.DATE_FORMAT.parse(bmdexam);
             } catch (ParseException e) {
                 LOGGER.error("Could not parse bmdexam {} {}", bmdexam, e);
             }
         }
         this.bloodgroup = bloodgroup;
     }
 
     public String getAddress1() {
         return address1;
     }
 
     public void setAddress1(String address1) {
         this.address1 = address1;
     }
 
     public String getAddress2() {
         return address2;
     }
 
     public void setAddress2(String address2) {
         this.address2 = address2;
     }
 
     public String getAddress3() {
         return address3;
     }
 
     public void setAddress3(String address3) {
         this.address3 = address3;
     }
 
     public String getAddress4() {
         return address4;
     }
 
     public void setAddress4(String address4) {
         this.address4 = address4;
     }
 
     public String getCentreCode() {
         return centreCode;
     }
 
     public void setCentreCode(String centreCode) {
         this.centreCode = (centreCode != null) ? centreCode.toUpperCase() : centreCode;
     }
 
     public String getDateofbirth() {
         return dateofbirth;
     }
 
     public String getFormatedDateOfBirth() {
         try {
             return UK_DATE_FORMAT.format(DATE_FORMAT.parse(dateofbirth));
         } catch (ParseException e) {
             return dateofbirth;
         }
     }
 
     public void setDateofbirth(String dateofbirth) {
         this.dateofbirth = dateofbirth;
     }
 
     public String getForename() {
         return forename;
     }
 
     public void setForename(String forename) {
         this.forename = forename;
     }
 
     public String getNhsno() {
         return nhsno;
     }
 
     public void setNhsno(String nhsno) {
         this.nhsno = nhsno;
     }
 
     public String getPostcode() {
         return postcode;
     }
 
     public void setPostcode(String postcode) {
         this.postcode = postcode;
     }
 
     public String getSex() {
         return sex;
     }
 
     public void setSex(String sex) {
         this.sex = sex;
     }
 
     public String getSurname() {
         return surname;
     }
 
     public void setSurname(String surname) {
         this.surname = surname;
     }
 
     public String getTelephone1() {
         return telephone1;
     }
 
     public void setTelephone1(String telephone1) {
         this.telephone1 = telephone1;
     }
 
     public String getTelephone2() {
         return telephone2;
     }
 
     public void setTelephone2(String telephone2) {
         this.telephone2 = telephone2;
     }
 
     public String getMobile() {
         return mobile;
     }
 
     public void setMobile(String mobile) {
         this.mobile = mobile;
     }
 
     public String getDiagnosis() {
         return diagnosis;
     }
 
     public void setDiagnosis(String diagnosis) {
         this.diagnosis = diagnosis;
     }
 
     public Date getDiagnosisDate() {
         return diagnosisDate;
     }
 
     public void setDiagnosisDate(Date diagnosisDate) {
         this.diagnosisDate = diagnosisDate;
     }
 
     public String getTreatment() {
         return treatment;
     }
 
     public void setTreatment(String treatment) {
         this.treatment = treatment;
     }
 
     public String getHospitalnumber() {
         return hospitalnumber;
     }
 
     public void setHospitalnumber(String hospitalnumber) {
         this.hospitalnumber = hospitalnumber;
     }
 
     public String getTransplantstatus() {
         return transplantstatus;
     }
 
     public void setTransplantstatus(String transplantstatus) {
         this.transplantstatus = transplantstatus;
     }
 
     public String getGpaddress1() {
         return gpaddress1;
     }
 
     public void setGpaddress1(String gpaddress1) {
         this.gpaddress1 = gpaddress1;
     }
 
     public String getGpaddress2() {
         return gpaddress2;
     }
 
     public void setGpaddress2(String gpaddress2) {
         this.gpaddress2 = gpaddress2;
     }
 
     public String getGpaddress3() {
         return gpaddress3;
     }
 
     public void setGpaddress3(String gpaddress3) {
         this.gpaddress3 = gpaddress3;
     }
 
     public String getGpname() {
         return gpname;
     }
 
     public void setGpname(String gpname) {
         this.gpname = gpname;
     }
 
     public String getGppostcode() {
         return gppostcode;
     }
 
     public void setGppostcode(String gppostcode) {
         this.gppostcode = gppostcode;
     }
 
     public String getGptelephone() {
         return gptelephone;
     }
 
     public void setGptelephone(String gptelephone) {
         this.gptelephone = gptelephone;
     }
 
     public String getGpemail() {
         return gpemail;
     }
 
     public void setGpemail(String gpemail) {
         this.gpemail = gpemail;
     }
 
     public String getOtherConditions() {
         return otherConditions;
     }
 
     public void setOtherConditions(String otherConditions) {
         this.otherConditions = otherConditions;
     }
 
     public Date getBmdexam() {
         return bmdexam;
     }
 
     public void setBmdexam(Date bmdexam) {
         this.bmdexam = bmdexam;
     }
 
     public String getBloodgroup() {
         return bloodgroup;
     }
 
     public void setBloodgroup(String bloodgroup) {
         this.bloodgroup = bloodgroup;
     }
 }
 
