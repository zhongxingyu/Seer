 package org.patientview.radar.util;
 
 import com.Ostermiller.util.RandPass;
 import org.apache.commons.codec.digest.DigestUtils;
 import org.apache.wicket.request.mapper.parameter.PageParameters;
 import org.patientview.model.Patient;
 import org.patientview.model.generic.DiseaseGroup;
 import org.patientview.radar.web.pages.BasePage;
 import org.patientview.radar.web.pages.patient.GenericPatientPage;
 import org.patientview.radar.web.pages.patient.alport.AlportPatientPage;
 import org.patientview.radar.web.pages.patient.hnf1b.HNF1BPatientPage;
 import org.patientview.radar.web.pages.patient.srns.SrnsPatientPage;
 import org.springframework.util.StringUtils;
 
 import java.util.Date;
 
 /**
  * Radar Utility - miscellaneous utility methods go here
  */
 public class RadarUtility {
 
     /**
      * @param event1Start cannot be null
      * @param event2Start cannot be null
      * @return true if there is an overlap
      */
     public static boolean isEventsOverlapping(Date event1Start, Date event1End, Date event2Start, Date event2End) {
         if (event1End == null) {
             if (event2End == null) {
                 if (event1Start.compareTo(event2Start) == 0) {
                     return true;
                 }
             } else {
                 if (event1Start.compareTo(event2Start) >= 0 &&
                         event1Start.compareTo(event2End) < 1) {
                     return true;
                 }
             }
         } else {
             if (event2Start.compareTo(event1Start) >= 0 &&
                     event2Start.compareTo(event1End) < 1) {
                 return true;
             }
             if (event2End != null) {
                 if (event1Start.compareTo(event2Start) >= 0 && event1Start.compareTo(event2End) < 1 ||
                         event1End.compareTo(event2Start) >= 0 && event1End.compareTo(event2End) < 1 ||
                         event2Start.compareTo(event1Start) >= 0 && event2Start.compareTo(event1End) < 1 ||
                         event2End.compareTo(event1Start) >= 0 && event2End.compareTo(event1End) < 1) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     /**
      * returns a user friendly label for an enum for example PART1_PART2 will return Part1 Part2
      */
     public static String getLabelFromEnum(String enumString) {
         String label = enumString;
         label = label.replace("_", " ");
         String[] parts = label.split(" ");
         label = "";
         for (String part : parts) {
             String formatted = part.toLowerCase();
             formatted = Character.toUpperCase(formatted.charAt(0)) + formatted.substring(1);
             label += formatted;
             label += " ";
         }
         return label;
     }
 
     public static String hashPassword(String password) {
         return DigestUtils.sha256Hex(password);
     }
 
     public static String generateNewPassword() {
         return new RandPass(RandPass.NONCONFUSING_ALPHABET).getPass(8);
     }
 
     // Make sure the link record displays data from the
     public static Patient overRideLinkRecord(Patient source, Patient link) {
 
 
         link.setForename(source.getForename());
         link.setSurname(source.getSurname());
         link.setDob(source.getDob());
         link.setAddress1(source.getAddress1());
         link.setAddress2(source.getAddress2());
         link.setAddress3(source.getAddress3());
         link.setAddress4(source.getAddress4());
         link.setPostcode(source.getPostcode());
         link.setSex(source.getSex());
         link.setTelephone1(source.getTelephone1());
         link.setHospitalnumber(source.getHospitalnumber());
         link.setRenalUnit(source.getRenalUnit());

         link.setEditableDemographics(false);
 
         return link;
 
     }
 
     // Merge the two records together, source record taking priority on certain fields.
     // Done by getting the source record and just adding any radar stuff in it if it's found
     public static Patient mergePatientRecords(Patient source, Patient link) {
 
         // Properties to mock the source object into the linked one
         source.setId(link.getId());
         source.setUnitcode(link.getUnitcode());
         source.setNhsno(link.getNhsno());
 
         // Properties overridden has radar data
         if (StringUtils.hasText(link.getSurnameAlias())) {
             source.setSurnameAlias(link.getSurnameAlias());
         }
 
         if (StringUtils.hasText(link.getEthnicGp())) {
             source.setEthnicGp(link.getEthnicGp());
         }
 
         if (StringUtils.hasText(link.getTelephone2())){
             source.setTelephone2(link.getTelephone2());
         }
 
         if (StringUtils.hasText(link.getMobile())) {
             source.setMobile(link.getMobile());
         }
 
         if (link.getRrtModality() != null) {
             source.setRrtModality(link.getRrtModality());
         }
 
         if (StringUtils.hasText(link.getDiagnosis())) {
             source.setDiagnosis(link.getDiagnosis());
         }
 
         if (link.getDiagnosisDate() != null) {
             source.setDiagnosisDate(link.getDiagnosisDate());
         }
 
         if (link.getOtherClinicianAndContactInfo() != null) {
             source.setOtherClinicianAndContactInfo(link.getOtherClinicianAndContactInfo());
         }
 
         if (StringUtils.hasText(link.getComments())) {
             source.setComments(link.getComments());
         }
 
         source.setEditableDemographics(false);
 
         return source;
     }
 
 
     public static void cleanLinkRecord(Patient patient) {
         patient.setSurname(null);
         patient.setForename(null);
         patient.setDob(null);
         patient.setAddress1(null);
         patient.setAddress2(null);
         patient.setAddress3(null);
         patient.setAddress4(null);
         patient.setPostcode(null);
         patient.setSex(null);
         patient.setTelephone1(null);
         patient.setHospitalnumber(null);
         patient.setRenalUnit(null);
     }
 
 
     public static BasePage getDiseasePage(Patient patient, PageParameters pageParameters){
 
         if (patient.getDiseaseGroup() != null) {
 
             if (patient.getDiseaseGroup().getId().equals(DiseaseGroup.SRNS_DISEASE_GROUP_ID) ||
                     patient.getDiseaseGroup().getId().
                             equals(DiseaseGroup.MPGN_DISEASEGROUP_ID)) {
                 return new SrnsPatientPage(patient);
             } else if (patient.getDiseaseGroup().getId().equals(DiseaseGroup.ALPORT_DISEASEGROUP_ID)) {
                 return new AlportPatientPage(patient, pageParameters);
             } else if (patient.getDiseaseGroup().getId().equals(DiseaseGroup.HNF1B_DISEASEGROUP_ID)) {
                 return new HNF1BPatientPage(patient, pageParameters);
             } else {
                 return new GenericPatientPage(patient, pageParameters);
             }
         }  else {
             return new GenericPatientPage(patient, pageParameters);
         }
 
     }
 
 }
