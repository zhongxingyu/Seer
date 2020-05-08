 package org.motechproject.ghana.national.tools.seed.data;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.motechproject.ghana.national.domain.mobilemidwife.MobileMidwifeEnrollment;
 import org.motechproject.ghana.national.domain.mobilemidwife.ServiceType;
 import org.motechproject.ghana.national.repository.AllMobileMidwifeEnrollments;
 import org.motechproject.ghana.national.service.MobileMidwifeService;
 import org.motechproject.ghana.national.tools.seed.Seed;
 import org.motechproject.ghana.national.tools.seed.data.source.MobileMidwifeSource;
 import org.motechproject.util.DateUtil;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 @Component
 public class MobileMidwifeMigrationSeed extends Seed {
 
     private static Logger LOG = Logger.getLogger(MobileMidwifeMigrationSeed.class);
 
     @Autowired
     MobileMidwifeSource mobileMidwifeSource;
 
     @Autowired
     AllMobileMidwifeEnrollments allMobileMidwifeEnrollments;
 
     @Autowired
     private MobileMidwifeService mobileMidwifeService;
 
     private static Map<String, String> FACILITY_CACHE = new HashMap<String, String>();
     private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
 
     @Override
     protected void load() {
         List<MobileMidwifeEnrollment> mobileMidwifeEnrollments = mobileMidwifeSource.getPatients();
         List<HashMap<String, String>> enrollmentData = mobileMidwifeSource.getEnrollmentData();
         List<Map<String, Object>> newListOfHashMaps = mobileMidwifeEnrollmentData(enrollmentData);
 
        for (MobileMidwifeEnrollment mobileMidwifeEnrollment : mobileMidwifeEnrollments) {
            final List<Map<String, Object>> enrollments = new ArrayList<Map<String, Object>>();
            String patientMotechId = mobileMidwifeEnrollment.getPatientId();
            for (Map<String, Object> map : newListOfHashMaps) {
                final String enrollmentPatientMotechId = (String) map.get("patient_motech_id");
                if (enrollmentPatientMotechId != null && enrollmentPatientMotechId.equals(patientMotechId)) {
                    enrollments.add(map);
                }
             }
             sortEnrollmentsByDate(enrollments);
             try {
                 addMobileMidwifeEnrollmentDataToDB(mobileMidwifeEnrollment, enrollments);
             } catch (ParseException e) {
                e.printStackTrace();
             }
         }
     }
 
     private void addMobileMidwifeEnrollmentDataToDB(MobileMidwifeEnrollment mobileMidwifeEnrollment, List<Map<String, Object>> enrollments) throws ParseException {
         for (int i = 0; i < enrollments.size(); i++) {
             Map<String, Object> map = enrollments.get(i);
             String patientMotechId = (String) map.get("patient_motech_id");
             boolean active = i == 0;
             Map<String, Object> earliestMessage = getEarliestMessage((String) map.get("enrollment_id"));
             if (earliestMessage != null) {
                 String earliestMessageKey = (String) earliestMessage.get("message_key");
                 String[] splitMessageKey = StringUtils.split(earliestMessageKey, ".");
                 ServiceType serviceType = (earliestMessageKey.contains("child")) ? ServiceType.CHILD_CARE : ServiceType.PREGNANCY;
                 int startWeek = Integer.parseInt(splitMessageKey[2]);
                 int messageStartWeek = (serviceType.equals(ServiceType.CHILD_CARE)) ? (startWeek + 40) : startWeek;
                 if (messageStartWeek != 0) {
                     mobileMidwifeEnrollment.setMessageStartWeek(String.valueOf(messageStartWeek));
                     mobileMidwifeEnrollment.setEnrollmentDateTime(DateUtil.now());
                     mobileMidwifeEnrollment = setEnrollmentDetails(mobileMidwifeEnrollment, patientMotechId, serviceType, active);
                     setFacility(mobileMidwifeEnrollment, map);
                     setStaffId(mobileMidwifeEnrollment, map);
                     allMobileMidwifeEnrollments.add(mobileMidwifeEnrollment);
                     mobileMidwifeService.startMobileMidwifeCampaign(mobileMidwifeEnrollment);
                     LOG.info("Migrated MobileMidwifeEnrollment for : " + mobileMidwifeEnrollment.getPatientId() + " in facility " + mobileMidwifeEnrollment.getFacilityId() + " under " + mobileMidwifeEnrollment.getServiceType().getDisplayName() + " campaign");
                 }
             }
             else {
                LOG.info("No schedule to migrate for "+mobileMidwifeEnrollment.getPatientId());
             }
         }
     }
 
 
     private void setStaffId(MobileMidwifeEnrollment mobileMidwifeEnrollment, Map<String, Object> map) {
         String staffMotechId;
         String staffId = (String) map.get("systemId");
         if (StringUtils.isNotBlank(staffId)) {
             staffMotechId = staffId;
         } else {
             staffMotechId = mobileMidwifeSource.getStaffIdFromPatientEncounter((String) map.get("patientId"));
         }
         if (staffMotechId == null) {
             //child clients who don't have *ANY* encounters.
             staffMotechId = mobileMidwifeSource.getMotechStaffIdForMotechFacilityId(mobileMidwifeEnrollment.getFacilityId());
         }
         mobileMidwifeEnrollment.setStaffId(staffMotechId);
     }
 
     private void setFacility(MobileMidwifeEnrollment mobileMidwifeEnrollment, Map<String, Object> map) {
         String facilityId;
         if (StringUtils.isBlank((String) map.get("facilityId"))) {
             facilityId = (String) map.get("patient_facility_id");
         } else {
             facilityId = (String) map.get("facilityId");
         }
 
         if (facilityId.equals("33")) facilityId = "1"; //33 equals 'Ghana' country, so its an unknown location.
 
         String facilityMotechId;
         if (FACILITY_CACHE.get(facilityId) != null) {
             facilityMotechId = FACILITY_CACHE.get(facilityId);
         } else {
             facilityMotechId = mobileMidwifeSource.getMotechFacilityId(facilityId);
             FACILITY_CACHE.put(facilityId, facilityMotechId);
         }
         mobileMidwifeEnrollment.setFacilityId(facilityMotechId);
     }
 
     private MobileMidwifeEnrollment setEnrollmentDetails(MobileMidwifeEnrollment mobileMidwifeEnrollment, String patientMotechId, ServiceType serviceType, boolean active) {
         mobileMidwifeEnrollment = MobileMidwifeEnrollment.cloneNew(mobileMidwifeEnrollment);
         mobileMidwifeEnrollment.setServiceType(serviceType);
         mobileMidwifeEnrollment.setActive(active);
         mobileMidwifeEnrollment.setConsent(true);
         mobileMidwifeEnrollment.setPatientId(patientMotechId);
         return mobileMidwifeEnrollment;
     }
 
     private Map<String, Object> getEarliestMessage(String enrollmentId) {
         List<Map<String, Object>> messages = mobileMidwifeSource.scheduledMessage(enrollmentId);
         sortMessagesByDate(messages);
         return (!messages.isEmpty()) ? messages.get(0) : null;
     }
 
     private void sortEnrollmentsByDate(List<Map<String, Object>> newList) {
         Collections.sort(newList, new Comparator<Map<String, Object>>() {
             @Override
             public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                 try {
 
                     return (dateFormat.parse((String) o1.get("startDate")).after(dateFormat.parse((String) o2.get("startDate")))) ? -1 : 1;
                 } catch (ParseException e) {
                     return 0;
                 }
             }
         });
     }
 
     private void sortMessagesByDate(List<Map<String, Object>> newList) {
         Collections.sort(newList, new Comparator<Map<String, Object>>() {
             @Override
             public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                 try {
                     return (dateFormat.parse((String) o1.get("scheduled_date")).after(dateFormat.parse((String) o2.get("scheduled_date")))) ? -1 : 1;
                 } catch (ParseException e) {
                     return 0;
                 }
             }
         });
     }
 
     private List<Map<String, Object>> mobileMidwifeEnrollmentData(List<HashMap<String, String>> enrollmentData) {
         List<Map<String, Object>> newListOfHashMaps = new ArrayList<Map<String, Object>>();
         for (HashMap<String, String> map : enrollmentData) {
             String patientId = map.get("patientId");
             final HashMap<String, Object> newHashMap = new HashMap<String, Object>();
 
             String patientMotechId = mobileMidwifeSource.getPatientMotechId(patientId);
             if (patientMotechId == null) {
                 LOG.info("Patient does not have a MotechId: " + patientId);
                 continue;  //patient has no motech id
             }
             newHashMap.put("patient_motech_id", patientMotechId);
 
             newHashMap.put("patient_facility_id", mobileMidwifeSource.getPatientFacility(patientId));
 
             if (StringUtils.isNotBlank(map.get("obs_id"))) { // Only Pregnant women will have observations captured.
                 String observationId = map.get("obs_id");
                 Map<String, String> locationProviderMap = mobileMidwifeSource.locationToProviderMapping(observationId);
                 newHashMap.put("facilityId", locationProviderMap.get("location_id"));
                 newHashMap.put("systemId", locationProviderMap.get("system_id"));
             }
 
             newHashMap.put("programName", map.get("programName"));
             newHashMap.put("startDate", map.get("startDate"));
             newHashMap.put("enrollment_id", map.get("enrollment_id"));
             newListOfHashMaps.add(newHashMap);
         }
         return newListOfHashMaps;
     }
 }
