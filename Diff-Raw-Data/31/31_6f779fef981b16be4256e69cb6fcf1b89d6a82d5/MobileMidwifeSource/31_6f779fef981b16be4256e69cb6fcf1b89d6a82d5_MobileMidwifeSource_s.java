 package org.motechproject.ghana.national.tools.seed.data.source;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang.StringUtils;
 import org.motechproject.ghana.national.domain.mobilemidwife.*;
 import org.motechproject.model.DayOfWeek;
 import org.motechproject.model.Time;
 import org.springframework.jdbc.core.PreparedStatementCreator;
 import org.springframework.jdbc.core.RowMapper;
 import org.springframework.stereotype.Repository;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 @Repository
 public class MobileMidwifeSource extends BaseSeedSource {
 
     public List<HashMap<String, String>> getEnrollmentData() {
         return jdbcTemplate.query("select program_name, person_id, start_date, obs_id, motechmodule_enrollment_id " +
                 "from motechmodule_enrollment " +
                 "where program_name != 'Expected Care Message Program' and end_date is null", new RowMapper<HashMap<String, String>>() {
             @Override
             public HashMap<String, String> mapRow(ResultSet resultSet, int i) throws SQLException {
                 final HashMap<String, String> map = new HashMap<String, String>();
                 map.put("patientId", resultSet.getString("person_id"));
                 map.put("programName", resultSet.getString("program_name"));
                 map.put("startDate", resultSet.getString("start_date"));
                 map.put("obs_id", resultSet.getString("obs_id"));
                 map.put("enrollment_id", resultSet.getString("motechmodule_enrollment_id"));
                 return map;
             }
         });
     }
 
     public String getPatientMotechId(final String patientId) {
         List<String> list = jdbcTemplate.query(new PreparedStatementCreator() {
                     @Override
                     public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                         PreparedStatement preparedStatement = connection.prepareStatement("select identifier from patient_identifier where patient_id = ?");
                         preparedStatement.setString(1, patientId);
                         return preparedStatement;
                     }
                 },
                 new RowMapper<String>() {
                     @Override
                     public String mapRow(ResultSet resultSet, int i) throws SQLException {
                         return resultSet.getString("identifier");
                     }
                 }
         );
         return (list.isEmpty()) ? null : list.get(0);
     }
 
     public Map<String, String> locationToProviderMapping(final String observationId) {
         List<Map<String, String>> list = jdbcTemplate.query(new PreparedStatementCreator() {
                     @Override
                     public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                         PreparedStatement preparedStatement = connection.prepareStatement("select e.location_id, u.system_id from encounter e, obs o, users u  " +
                                 "where o.encounter_id = e.encounter_id and e.provider_id = u.user_id and o.obs_id=?");
                         preparedStatement.setString(1, observationId);
                         return preparedStatement;
                     }
                 },
                 new RowMapper<Map<String, String>>() {
                     @Override
                     public Map<String, String> mapRow(ResultSet resultSet, int i) throws SQLException {
                         Map<String, String> result = new HashMap<String, String>();
                         result.put("location_id", resultSet.getString("location_id"));
                         result.put("system_id", resultSet.getString("system_id"));
                         return result;
                     }
                 }
         );
         return (list.isEmpty()) ? new HashMap<String, String>(): list.get(0);
     }
 
     public String getStaffIdFromPatientEncounter(final String patientId) {
         List<String> list = jdbcTemplate.query(new PreparedStatementCreator() {
                     @Override
                     public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                         PreparedStatement preparedStatement = connection.prepareStatement("select u.system_id from users u, encounter e where " +
                                 "e.provider_id = u.user_id and e.patient_id = ? limit 1");
                         preparedStatement.setString(1, patientId);
                         return preparedStatement;
                     }
                 },
                 new RowMapper<String>() {
                     @Override
                     public String mapRow(ResultSet resultSet, int i) throws SQLException {
 
                         return resultSet.getString("system_id");
                     }
                 }
         );
         return (list.isEmpty()) ? null : list.get(0);
     }
 
     public String getMotechStaffIdForMotechFacilityId(final String facilityId) {
         List<String> list = jdbcTemplate.query(new PreparedStatementCreator() {
                     @Override
                     public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                         PreparedStatement preparedStatement = connection.prepareStatement("select u.system_id from encounter e, motechmodule_facility mf, users u " +
                                 "where mf.location_id = e.location_id and e.provider_id = u.user_id and mf.facility_id = ? limit 1");
                         preparedStatement.setString(1, facilityId);
                         return preparedStatement;
                     }
                 },
                 new RowMapper<String>() {
                     @Override
                     public String mapRow(ResultSet resultSet, int i) throws SQLException {
                         return resultSet.getString("system_id");
                     }
                 }
         );
         return (list.isEmpty()) ? null : list.get(0);
     }
 
 
     public List<MobileMidwifeEnrollment> getPatients() {
         return jdbcTemplate.query("select \n" +
                 "pi.identifier as 'Identifier', \n" +
                 "group_concat(if(pa.person_attribute_type_id = 8, value, null)) as 'Phone',\n" +
                 "group_concat(if(pa.person_attribute_type_id = 11, value, null)) as 'Language',\n" +
                 "group_concat(if(pa.person_attribute_type_id = 12, value, null)) as 'Phone_Type',\n" +
                 "group_concat(if(pa.person_attribute_type_id = 13, value, null)) as 'Media',\n" +
                 "group_concat(if(pa.person_attribute_type_id = 14, value, null)) as 'Day',\n" +
                 "group_concat(if(pa.person_attribute_type_id = 15, value, null)) as 'Time',\n" +
                 "group_concat(if(pa.person_attribute_type_id = 17, value, null)) as 'How',\n" +
                 "group_concat(if(pa.person_attribute_type_id = 18, value, null)) as 'Reason'\n" +
                 "from person_attribute pa, patient_identifier pi\n" +
                 "where pa.person_id = pi.patient_id\n" +
                 "and pa.person_attribute_type_id in (8,11,12,13,14,15,17,18)\n" +
                 "and pi.identifier_type = 3\n" +
                 "and pa.voided = 0\n" +
                 "group by pa.person_id",
 
                 new RowMapper<MobileMidwifeEnrollment>() {
                     @Override
                     public MobileMidwifeEnrollment mapRow(ResultSet resultSet, int i) throws SQLException {
                         MobileMidwifeEnrollment enrollment = new MobileMidwifeEnrollment();
                         // TODO: to set enrolled date
                         enrollment.setPatientId(resultSet.getString(1));
 
                         enrollment.setPhoneNumber(resultSet.getString(2));
 
                         String phoneType = resultSet.getString(4);
                         if (StringUtils.isNotBlank(phoneType) && !",".equals(phoneType)) {
                             phoneType = correctText(phoneType);
                             enrollment.setPhoneOwnership(PhoneOwnership.valueOf(phoneType));
                         }
 
                         String language = resultSet.getString(3);
                         if (StringUtils.isNotBlank(language) && !",".equals(language)) {
                             language = correctText(language);
                             enrollment.setLanguage(Language.valueOf(language.toUpperCase()));
                         }
 
                         String media = resultSet.getString(5);
                         if (StringUtils.isNotBlank(media)) {
                             enrollment.setMedium(Medium.TEXT_SMS_MEDIUM.equals(media) ? Medium.SMS : Medium.VOICE);
                         }
 
                         String deliverDay = resultSet.getString(6);
                         if (StringUtils.isNotBlank(deliverDay) && !",".equals(deliverDay)) {
                             deliverDay = correctText(deliverDay);
                             enrollment.setDayOfWeek(DayOfWeek.valueOf(StringUtils.capitalize(deliverDay.toLowerCase())));
                         }
 
                         String time = resultSet.getString(7);
                         if (StringUtils.isNotBlank(time) && !",".equals(time)) {
                             time = correctText(time);
                             String[] times = time.split(":");
                             enrollment.setTimeOfDay(new Time(Integer.parseInt(times[0]), Integer.parseInt(times[1])));
                         }
 
                         String reason = resultSet.getString(9);
                         if (StringUtils.isNotBlank(reason) && !",".equals(reason)) {
                             reason = correctText(reason);
                             enrollment.setReasonToJoin(ReasonToJoin.valueOf(reason));
                         }
 
                         String how = resultSet.getString(8);
                         if (StringUtils.isNotBlank(how) && !",".equals(how)) {
                             how = correctText(how);
                             enrollment.setLearnedFrom(LearnedFrom.valueOf(how));
                         }
                         return enrollment;
                     }
                 });
     }
 
     public String getPatientFacility(final String patientId) {
         List<String> facilities = jdbcTemplate.query(new PreparedStatementCreator() {
                     @Override
                     public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                         PreparedStatement preparedStatement = connection.prepareStatement("select location_id from patient_identifier where patient_id = ?");
                         preparedStatement.setString(1, patientId);
                         return preparedStatement;
                     }
                 },
                 new RowMapper<String>() {
                     @Override
                     public String mapRow(ResultSet resultSet, int i) throws SQLException {
                         return resultSet.getString(1);
                     }
                 });
         return CollectionUtils.isEmpty(facilities) ? null : facilities.get(0);
     }
 
     public String getMotechFacilityId(final String locationId) {
         List<String> facilities = jdbcTemplate.query(new PreparedStatementCreator() {
                     @Override
                     public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                         PreparedStatement preparedStatement = connection.prepareStatement("select facility_id from motechmodule_facility where location_id= ?");
                         preparedStatement.setString(1, locationId);
                         return preparedStatement;
                     }
                 },
                 new RowMapper<String>() {
                     @Override
                     public String mapRow(ResultSet resultSet, int i) throws SQLException {
                         return resultSet.getString(1);
                     }
                 });
         return CollectionUtils.isEmpty(facilities) ? null : facilities.get(0);
     }
 
     private String correctText(String how) {
         if (how.split(",").length == 2) {
             how = how.split(",")[0];
         }
         return how;
     }
 
     public List<Map<String, Object>> scheduledMessage(final String enrollmentId) {
         return jdbcTemplate.query(new PreparedStatementCreator() {
                     @Override
                     public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                         PreparedStatement preparedStatement = connection.prepareStatement("select en.program_name as program_name, sm.recipient_id as person_id, md.message_key as message_key, sm.scheduled_for as scheduled_date " +
                                 "from motechmodule_scheduledmessage sm, motechmodule_messagedefinition md, motechmodule_enrollment en where sm.definition_id = md.motechmodule_messagedefinition_id and sm.care_name is null " +
                                "and en.end_date is null and en.obs_id is not null and sm.enrollment_id= ? and DATEDIFF(sm.scheduled_for,SYSDATE())>=0");
                         preparedStatement.setString(1, enrollmentId);
                         return preparedStatement;
                     }
                 },
                 new RowMapper<Map<String, Object>>() {
                     @Override
                     public Map<String, Object> mapRow(ResultSet resultSet, int i) throws SQLException {
                         Map<String, Object> map = new HashMap<String, Object>();
                         map.put("program_name", resultSet.getString("program_name"));
                         map.put("person_id", resultSet.getString("person_id"));
                         map.put("message_key", resultSet.getString("message_key"));
                         map.put("scheduled_date", resultSet.getString("scheduled_date"));
                         return map;
                     }
                 }
         );
     }
 }
