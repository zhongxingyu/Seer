 package org.patientview.radar.dao.impl;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang.StringUtils;
 import org.patientview.model.Clinician;
 import org.patientview.model.Ethnicity;
 import org.patientview.model.Patient;
 import org.patientview.model.Sex;
 import org.patientview.model.Status;
 import org.patientview.model.enums.NhsNumberType;
 import org.patientview.model.enums.SourceType;
 import org.patientview.model.generic.DiseaseGroup;
 import org.patientview.model.generic.GenericDiagnosis;
 import org.patientview.radar.dao.PatientDao;
 import org.patientview.radar.dao.UserDao;
 import org.patientview.radar.dao.UtilityDao;
 import org.patientview.radar.dao.generic.DiseaseGroupDao;
 import org.patientview.radar.dao.generic.GenericDiagnosisDao;
 import org.patientview.util.CommonUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.dao.EmptyResultDataAccessException;
 import org.springframework.jdbc.core.RowMapper;
 import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
 
 import javax.sql.DataSource;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.SimpleDateFormat;
 import java.util.HashMap;
 import java.util.List;
 
 
 
 /**
  * User: james@solidstategroup.com
  * Date: 06/11/13
  * Time: 10:13
  */
 public class PatientDaoImpl extends BaseDaoImpl implements PatientDao, InitializingBean {
 
 
     private SimpleJdbcInsert patientInsert;
     private SimpleJdbcInsert radarNumberInsert;
 
     private static final Logger LOGGER = LoggerFactory.getLogger(DemographicsDaoImpl.class);
     private static final String DATE_FORMAT = "yyyy-MM-dd";
 
     // Static data
     private List<Sex> sexes;
     private List<Status> statuses;
     private List<DiseaseGroup> diseaseGroups;
     private List<GenericDiagnosis> genericDiagnoses;
     private List<Ethnicity> ethnicities;
     private UtilityDao utilityDao;
     private DiseaseGroupDao diseaseGroupDao;
     private GenericDiagnosisDao genericDiagnosisDao;
     private UserDao userDao;
 
     @Override
     public void setDataSource(DataSource dataSource) {
         // Call super
         super.setDataSource(dataSource);
 
         // Initialise a simple JDBC insert to be able to get the allocated ID
         patientInsert = new SimpleJdbcInsert(dataSource).withTableName("patient")
         .usingGeneratedKeyColumns("id")
         .usingColumns(
                 "rrNo", "dateReg", "nhsno", "nhsNoType", "hospitalnumber", "uktNo", "surname",
                 "surnameAlias", "forename", "dateofbirth", "AGE", "SEX", "ethnicGp", "address1",
                 "address2", "address3", "address4", "POSTCODE", "radarConsentConfirmedByUserId",
                 "postcodeOld", "CONSENT", "dateBapnReg", "consNeph", "unitcode",
                 "STATUS", "emailAddress", "telephone1", "telephone2", "mobile", "rrtModality",
                 "genericDiagnosis", "dateOfGenericDiagnosis", "otherClinicianAndContactInfo", "comments",
                 "republicOfIrelandId", "isleOfManId", "channelIslandsId", "indiaId", "generic", "sourceType",
                 "patientLinkId");
 
         radarNumberInsert = new SimpleJdbcInsert(dataSource).withTableName("rdr_radar_number")
                 .usingGeneratedKeyColumns("id");
 
     }
 
     public void afterPropertiesSet() throws Exception {
         sexes = jdbcTemplate.query("SELECT * FROM tbl_Sex", new SexRowMapper());
         statuses = jdbcTemplate.query("SELECT * FROM tbl_Status ", new StatusRowMapper());
         diseaseGroups = diseaseGroupDao.getAll();
         genericDiagnoses = genericDiagnosisDao.getAll();
         ethnicities = utilityDao.getEthnicities();
     }
 
     public List<Patient> getPatientsByNhsNumber(final String nhsNo) {
         StringBuilder query = new StringBuilder();
         query.append("SELECT  id ");
         query.append(",       nhsno ");
         query.append(",       forename ");
         query.append(",       surname ");
         query.append(",       dateofbirth ");
         query.append(",       unitcode ");
         query.append(",       mostRecentTestResultDateRangeStopDate ");
         query.append("FROM    patient ");
         query.append("WHERE   nhsNo = ? ");
         query.append("ORDER BY   mostRecentTestResultDateRangeStopDate DESC ");
 
         return jdbcTemplate.query(query.toString(), new Object[]{nhsNo}, new PatientSearchMapper());
     }
 
     public List<Patient> getPatientsWithRadarSourceType() {
         StringBuilder query = new StringBuilder();
         query.append("SELECT  * ");
         query.append("FROM    patient ");
         query.append("WHERE   sourceType = ? ");
 
         return jdbcTemplate.query(query.toString(), new Object[]{"Radar"}, new EnhancedPatientSearchMapper());
     }
 
     public Patient getByRadarNumber(Long radarNumber) {
 
         Patient patient = null;
 
         try {
 
 
 
             StringBuilder query = new StringBuilder();
             query.append("SELECT  * ");
             query.append("FROM    patient ");
             query.append("WHERE   radarNo = ? ");
 
             patient = jdbcTemplate.queryForObject(query.toString(), new Object[]{radarNumber}, new PatientRowMapper());
 
         } catch (EmptyResultDataAccessException e) {
             // Can't find the patient by id
             LOGGER.debug("Cannot find patient with radar number {}", radarNumber);
         }
 
         return patient;
 
     }
 
     public Patient getById(final Long id) {
 
         Patient patient = null;
 
         try {
 
             StringBuilder query = new StringBuilder();
             query.append("SELECT  * ");
             query.append("FROM    patient ");
             query.append("WHERE   id = ? ");
 
             patient = jdbcTemplate.queryForObject(query.toString(), new Object[]{id}, new PatientRowMapper());
 
         } catch (EmptyResultDataAccessException e) {
             // Can't find the patient by id
             LOGGER.debug("Cannot find patient with id {}", id);
         }
 
         return patient;
     }
 
     private Long getNextRadarNumber() {

        Long radarNumber = radarNumberInsert.executeAndReturnKey(new HashMap() {}).longValue();
        return radarNumber;
     }
 
     private class PatientSearchMapper implements RowMapper<Patient> {
 
             public Patient mapRow(ResultSet resultSet, int i) throws SQLException {
                 Patient patient = new Patient();
                 patient.setId(resultSet.getLong("id"));
                 patient.setNhsno(resultSet.getString("nhsno"));
                 patient.setSurname(resultSet.getString("surname"));
                 patient.setForename(resultSet.getString("forename"));
                 patient.setDateofbirth(resultSet.getString("dateofbirth"));
                 patient.setUnitcode(resultSet.getString("unitcode"));
                 patient.setMostRecentTestResultDateRangeStopDate(
                         resultSet.getDate("mostRecentTestResultDateRangeStopDate"));
                 return patient;
             }
         }
 
     private class EnhancedPatientSearchMapper implements RowMapper<Patient> {
 
         public Patient mapRow(ResultSet resultSet, int i) throws SQLException {
             Patient patient = new Patient();
             patient.setId(resultSet.getLong("id"));
             patient.setNhsno(resultSet.getString("nhsno"));
             patient.setSurname(resultSet.getString("surname"));
             patient.setForename(resultSet.getString("forename"));
             patient.setDateofbirth(resultSet.getString("dateofbirth"));
             patient.setUnitcode(resultSet.getString("unitcode"));
             patient.setMostRecentTestResultDateRangeStopDate(
                     resultSet.getDate("mostRecentTestResultDateRangeStopDate"));
             patient.setRadarNo(resultSet.getLong("radarNo"));
             return patient;
         }
     }
 
 
     public void save(final Patient patient) {
 
         // If we have an ID then update, otherwise insert new and set the ID
         if (patient.hasValidId()) {
             jdbcTemplate.update(
                     "UPDATE patient SET " +
                             "rrNo = ?, " +
                             "dateReg = ?, " +
                             "nhsno = ?, " +
                             "nhsNoType = ?, " +
                             "hospitalnumber = ?, " +
                             "uktNo = ?, " +
                             "surname = ?, " +
                             "surnameAlias = ?, " +
                             "forename = ?, " +
                             "dateofbirth = ?, " +
                             "AGE = ?, " +
                             "SEX = ?, " +
                             "ethnicGp = ?, " +
                             "address1 = ?, " +
                             "address2 = ?, " +
                             "address3 = ?, " +
                             "address4 = ?, " +
                             "POSTCODE = ?, " +
                             "postcodeOld = ?," +
                             "CONSENT = ?, " +
                             "dateBapnReg = ?, " +
                             "consNeph = ?, " +
                             "STATUS = ?, " +
                             "emailAddress = ?, " +
                             "telephone1 = ?, " +
                             "telephone2 = ?, " +
                             "mobile = ?, " +
                             "rrtModality = ?, " +
                             "genericDiagnosis = ?, " +
                             "dateOfGenericDiagnosis = ?, " +
                             "otherClinicianAndContactInfo = ?, " +
                             "comments = ?, " +
                             "republicOfIrelandId = ?, " +
                             "isleOfManId = ?, " +
                             "channelIslandsId = ?, " +
                             "indiaId = ?, " +
                             "radarConsentConfirmedByUserId = ?, " +
                             "generic = ?, " +
                             "patientLinkId = ? " +
                             " WHERE id = ?",
                     patient.getRrNo(),
                     patient.getDateReg(),
                     patient.getNhsno(),
                     patient.getNhsNumberType() != null ? patient.getNhsNumberType().getId() : 1,
                     patient.getHospitalnumber(),
                     patient.getUktNo(),
                     patient.getSurname(),
                     patient.getSurnameAlias(),
                     patient.getForename(),
                     patient.getDob() != null ? new SimpleDateFormat(DATE_FORMAT).format(patient.getDob()) : null,
                     patient.getAge(),
                     patient.getSexModel() != null ? patient.getSexModel().getType() : null,
                     patient.getEthnicity() != null ? patient.getEthnicity().getCode() : null,
                     patient.getAddress1(),
                     patient.getAddress2(),
                     patient.getAddress3(),
                     patient.getAddress4(),
                     patient.getPostcode(),
                     patient.getPostcodeOld(),
                     patient.isConsent(),
                     patient.getDateReg(),
                     patient.getClinician() != null ? patient.getClinician().getId() : null,
                     patient.getStatusModel() != null ? patient.getStatusModel().getId() : null,
                     patient.getEmailAddress(),
                     patient.getTelephone1(),
                     patient.getTelephone2(),
                     patient.getMobile(),
                     patient.getRrtModalityEunm() != null ? patient.getRrtModalityEunm().getId() : null,
                     patient.getGenericDiagnosisModel() != null ? patient.getGenericDiagnosisModel().getId() : null,
                     patient.getDateOfGenericDiagnosis(),
                     patient.getOtherClinicianAndContactInfo(),
                     patient.getComments(),
                     patient.getRepublicOfIrelandId(),
                     patient.getIsleOfManId(),
                     patient.getChannelIslandsId(),
                     patient.getIndiaId(),
                     patient.getRadarConsentConfirmedByUserId(),
                     patient.isGeneric(),
                     patient.getPatientLinkId(),
                     patient.getId());
         } else {
             Number id = patientInsert.executeAndReturnKey(new HashMap<String, Object>() {
                 {
                     put("rrNo", patient.getRrNo());
                     put("dateReg", patient.getDateReg());
                     put("nhsno", patient.getNhsno());
                     put("nhsNoType", patient.getNhsNumberType() != null ? patient.getNhsNumberType().getId() : null);
                     put("hospitalnumber", patient.getHospitalnumber());
                     put("uktNo", patient.getUktNo());
                     put("surname", patient.getSurname());
                     put("surnameAlias", patient.getSurnameAlias());
                     put("forename", patient.getForename());
                     put("dateofbirth", patient.getDob() != null ? new SimpleDateFormat(DATE_FORMAT).format(
                                     patient.getDob()) : null);
                     put("AGE", patient.getAge());
                     put("SEX", patient.getSexModel() != null ? patient.getSexModel().getType() : null);
                     put("ethnicGp", patient.getEthnicity() != null ? patient.getEthnicity().getCode() : null);
                     put("address1", patient.getAddress1());
                     put("address2", patient.getAddress2());
                     put("address3", patient.getAddress3());
                     put("address4", patient.getAddress4());
                     put("POSTCODE", patient.getPostcode());
                     put("postcodeOld", patient.getPostcodeOld());
                     put("CONSENT", patient.isConsent());
                     put("dateBapnReg", null);
                     put("consNeph", patient.getClinician() != null ? patient.getClinician().getId(): null);
                     put("unitcode", patient.getUnitcode() != null ? patient.getUnitcode()
                             : patient.getRenalUnit().getUnitCode());
                     put("STATUS", patient.getStatusModel() != null ? patient.getStatusModel().getId() : null);
                     put("emailAddress", patient.getEmailAddress());
                     put("telephone1", patient.getTelephone1());
                     put("telephone2", patient.getTelephone2());
                     put("mobile", patient.getMobile());
                     put("rrtModality", patient.getRrtModalityEunm() != null ? patient.getRrtModalityEunm().getId()
                             : null);
                     put("genericDiagnosis", patient.getGenericDiagnosisModel() != null ?
                             patient.getGenericDiagnosisModel().getId() : null);
                     put("dateOfGenericDiagnosis", patient.getDateOfGenericDiagnosis());
                     put("otherClinicianAndContactInfo", patient.getOtherClinicianAndContactInfo());
                     put("comments", patient.getComments());
                     put("republicOfIrelandId", patient.getRepublicOfIrelandId());
                     put("isleOfManId", patient.getIsleOfManId());
                     put("channelIslandsId", patient.getChannelIslandsId());
                     put("indiaId", patient.getIndiaId());
                     put("generic", patient.isGeneric());
                     put("radarConsentConfirmedByUserId", patient.getRadarConsentConfirmedByUserId());
                     put("sourceType", SourceType.RADAR.getName());
                     put("patientLinkId", patient.getPatientLinkId());
                 }
             });
             patient.setId(id.longValue());
 
             //The id of the patient record is now the new radar number
             Long radarNumber = getNextRadarNumber();
             jdbcTemplate.update("UPDATE patient set radarNo = ? WHERE id = ? ", radarNumber, id.longValue());
             patient.setRadarNo(radarNumber);
 
         }
     }
 
     public List<Patient> getPatientsByUnitCode(List<String> unitCodes) {
         String unitCodeValues = buildValueList(unitCodes);
 
         StringBuilder query = new StringBuilder();
         query.append("SELECT  DISTINCT p.* ");
         query.append("FROM    user u ");
         query.append("INNER JOIN patient p ");
         query.append("INNER JOIN usermapping m ");
         query.append("WHERE  m.nhsno = p.nhsno ");
         query.append("AND    u.username NOT LIKE '%-GP%' ");
         query.append("AND    u.username = m.username ");
         query.append("AND    m.unitcode IN (");
         query.append(unitCodeValues);
         query.append(")");
         query.append("AND    p.sourceType = '");
         query.append(SourceType.RADAR.getName());
         query.append("'");
 
         if (StringUtils.isNotEmpty(unitCodeValues)) {
             return jdbcTemplate.query(query.toString(), new PatientRowMapper());
         } else {
             return null;
         }
     }
 
 
 
 
     // This is here to move away from the old Demographic Dao Class and clean the mapping up.
     public class PatientRowMapper implements RowMapper<Patient> {
         public Patient mapRow(ResultSet resultSet, int i) throws SQLException {
 
             // Construct object and set radar number
             Patient patient = new Patient();
             Long radarId = resultSet.getLong("radarNo");
             patient.setRadarNo(radarId);
             patient.setId(resultSet.getLong("id"));
             patient.setDateReg(resultSet.getDate("dateReg"));
             patient.setRrNo(resultSet.getString("rrNo"));
             patient.setUktNo(resultSet.getString("uktNo"));
             patient.setNhsno(resultSet.getString("nhsno"));
             patient.setNhsNumberType(NhsNumberType.getNhsNumberType(resultSet.getLong("nhsNoType")));
             patient.setHospitalnumber(resultSet.getString("hospitalnumber"));
             patient.setSurname(resultSet.getString("surname"));
             patient.setSurnameAlias(resultSet.getString("surnameAlias"));
             patient.setForename(resultSet.getString("forename"));
             patient.setDob(CommonUtils.parseDate(resultSet.getString("dateofbirth")));
             // Addresses
             patient.setAddress1(resultSet.getString("address1"));
             patient.setAddress2(resultSet.getString("address2"));
             patient.setAddress3(resultSet.getString("address3"));
             patient.setAddress4(resultSet.getString("address4"));
             patient.setPostcode(resultSet.getString("POSTCODE"));
             patient.setPostcodeOld(resultSet.getString("postcodeOld"));
             patient.setSexModel(getSex(resultSet.getString("SEX")));
             patient.setEthnicity(getEthnicity(resultSet.getString("ethnicGp")));
             patient.setConsent(resultSet.getBoolean("CONSENT"));
             patient.setStatusModel(getStatus(resultSet.getLong("STATUS")));
             // Not sure on the why this field is called this
             patient.setClinician(getClinician(resultSet.getLong("consNeph")));
             patient.setUnitcode(resultSet.getString("unitCode"));
             patient.setRenalUnit(utilityDao.getCentre(patient.getUnitcode()));
             patient.setEmailAddress(resultSet.getString("emailAddress"));
             patient.setTelephone1(resultSet.getString("telephone1"));
             patient.setTelephone2(resultSet.getString("telephone2"));
             patient.setMobile(resultSet.getString("mobile"));
             Integer rrtModalityId = getIntegerWithNullCheck("rrtModality", resultSet);
             if (rrtModalityId != null) {
                 patient.setRrtModalityEunm(getEnumValue(Patient.RRTModality.class, rrtModalityId));
             }
 
             patient.setDiseaseGroup(getDiseaseGroup(patient.getNhsno()));
             patient.setDateOfGenericDiagnosis(resultSet.getDate("dateOfGenericDiagnosis"));
             patient.setOtherClinicianAndContactInfo(resultSet.getString("otherClinicianAndContactInfo"));
             patient.setComments(resultSet.getString("comments")); //comments,
             patient.setRepublicOfIrelandId(resultSet.getString("republicOfIrelandId"));
             patient.setIsleOfManId(resultSet.getString("isleOfManId"));
             patient.setChannelIslandsId(resultSet.getString("channelIslandsId"));
             patient.setIndiaId(resultSet.getString("indiaId"));
             patient.setGeneric(resultSet.getBoolean("generic"));
             patient.setEthnicGp(resultSet.getString("ethnicGp"));
             // Needs fixing into getting a Enum
             patient.setSourceType(resultSet.getString("sourceType"));
             patient.setPatientLinkId(resultSet.getLong("patientLinkId"));
             patient.setRadarConsentConfirmedByUserId(resultSet.getLong("radarConsentConfirmedByUserId"));
             patient.setGenericDiagnosisModel(getGenericDiagnosis(resultSet.getString("genericDiagnosis"),
                     patient.getDiseaseGroup()));
 
             return patient;
         }
     }
 
 
 
     //// Helper methods for the Row Mapper
 
 
     private Ethnicity getEthnicity(String ethnicityCode) {
         if (StringUtils.isNotEmpty(ethnicityCode)) {
 
             for (Ethnicity ethnicity : ethnicities) {
                 if (ethnicity.getCode().equals(ethnicityCode)) {
                     return  ethnicity;
                 }
             }
 
         }
 
         return null;
     }
 
 
     private GenericDiagnosis getGenericDiagnosis(String genericDiagnosisId, DiseaseGroup diseaseGroupId) {
 
         if (diseaseGroupId != null && StringUtils.isNotEmpty(genericDiagnosisId) && StringUtils.isNotEmpty(
                 diseaseGroupId.getId())) {
 
             for (GenericDiagnosis genericDiagnosis : genericDiagnoses) {
 
                 if (genericDiagnosis.getPrdCode().equals(genericDiagnosisId) && genericDiagnosis.getWorkingGroup()
                         .equals(diseaseGroupId.getId())) {
                     return genericDiagnosis;
                 }
 
             }
 
         }
 
         return null;
     }
 
     private DiseaseGroup getDiseaseGroup(String nhsNo) {
 
         String diseaseGroupId = null;
 
         List<String> radarMappings = userDao.getPatientRadarMappings(nhsNo);
 
         if (CollectionUtils.isNotEmpty(radarMappings)) {
             diseaseGroupId = radarMappings.get(0);
         }
 
         for (DiseaseGroup diseaseGroup : diseaseGroups) {
             if (diseaseGroup.getId().equals(diseaseGroupId)) {
                 return diseaseGroup;
             }
         }
 
         return  null;
 
     }
 
 
 
     private Clinician getClinician(Long consultantId){
         if (consultantId != null) {
 
             try {
                 Clinician clinician = utilityDao.getClinician(consultantId);
                 return clinician;
             } catch (Exception e) {
                 LOGGER.error("Error return clinician", e);
                 return null;
             }
 
         }
 
         return null;
 
     }
 
     private Status getStatus(Long statusId) {
         if (statusId != null) {
             for (Status status : statuses) {
                 if (status.getId().equals(statusId)) {
                     return status;
                 }
             }
         }
         return null;
 
     }
 
     private Sex getSex(String sexType) {
         if (StringUtils.isNotEmpty(sexType)) {
             for (Sex sex : sexes) {
                 if (sex.getType().equals(sexType)) {
                     return sex;
                 }
             }
         }
         return null;
     }
 
 
 
     private class SexRowMapper implements RowMapper<Sex> {
         public Sex mapRow(ResultSet resultSet, int i) throws SQLException {
             Sex sex = new Sex();
             sex.setId(resultSet.getLong("sID"));
             sex.setType(resultSet.getString("sType"));
             return sex;
         }
     }
 
     private class StatusRowMapper implements RowMapper<Status> {
         public Status mapRow(ResultSet resultSet, int i) throws SQLException {
             // Cosntruct new status object
             Status status = new Status();
             status.setId(resultSet.getLong("sID"));
             status.setDescription(resultSet.getString("sDesc"));
             status.setAbbreviation(resultSet.getString("sAbbrev"));
             return status;
         }
     }
 
     public void setUtilityDao(UtilityDao utilityDao) {
         this.utilityDao = utilityDao;
     }
 
     public void setDiseaseGroupDao(DiseaseGroupDao diseaseGroupDao) {
         this.diseaseGroupDao = diseaseGroupDao;
     }
 
     public void setGenericDiagnosisDao(GenericDiagnosisDao genericDiagnosisDao) {
         this.genericDiagnosisDao = genericDiagnosisDao;
     }
 
     public void setUserDao(UserDao userDao) {
         this.userDao = userDao;
     }
 
     public List<Sex> getSexes() {
         return sexes;
     }
 
     public List<Status> getStatuses() {
         return statuses;
     }
 
     public List<DiseaseGroup> getDiseaseGroups() {
         return diseaseGroups;
     }
 
     public List<GenericDiagnosis> getGenericDiagnoses() {
         return genericDiagnoses;
     }
 
     public List<Ethnicity> getEthnicities() {
         return ethnicities;
     }
 }
