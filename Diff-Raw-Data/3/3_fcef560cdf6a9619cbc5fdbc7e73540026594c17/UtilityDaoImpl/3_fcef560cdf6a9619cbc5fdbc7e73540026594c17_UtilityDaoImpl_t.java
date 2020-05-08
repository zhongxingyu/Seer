 package org.patientview.radar.dao.impl;
 
 import org.apache.commons.lang.StringUtils;
 import org.patientview.model.Centre;
 import org.patientview.model.Clinician;
 import org.patientview.model.Country;
 import org.patientview.model.Ethnicity;
 import org.patientview.radar.dao.UtilityDao;
 import org.patientview.radar.model.Consultant;
 import org.patientview.radar.model.DiagnosisCode;
 import org.patientview.radar.model.Relative;
 import org.patientview.radar.model.filter.ConsultantFilter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.dao.EmptyResultDataAccessException;
 import org.springframework.jdbc.core.RowMapper;
 import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
 
 import javax.sql.DataSource;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class UtilityDaoImpl extends BaseDaoImpl implements UtilityDao {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(UtilityDaoImpl.class);
 
     private SimpleJdbcInsert consultantsInsert;
 
     public void setDataSource(DataSource dataSource) {
         super.setDataSource(dataSource);
 
         consultantsInsert = new SimpleJdbcInsert(dataSource).withTableName("tbl_Consultants")
                 .usingGeneratedKeyColumns("cID")
                 .usingColumns("cSNAME", "cFNAME", "cCentre");
     }
 
     public void createUnit(String unitCode) {
         jdbcTemplate.execute("INSERT INTO unit(unitcode, NAME, shortname, specialty_id) VALUES ('"
                    + unitCode + "','"
                    + unitCode + "','"
                    + unitCode + "',0)");
     }
 
     public void deleteUnit(String unitCode) {
         jdbcTemplate.execute("DELETE FROM unit WHERE unitcode = '" + unitCode + "'");
     }
 
     public void deletePatientViewUser(String nshNo) {
 
         jdbcTemplate.execute("DELETE "
         + " FROM    USER "
         + " WHERE   username IN (SELECT  username "
         + " FROM    usermapping "
         + " WHERE   nhsno = '" + nshNo + "')");
 
     }
 
     public void deletePatientViewMapping(String nhsNo) {
         jdbcTemplate.execute("DELETE FROM usermapping WHERE nhsno = '" + nhsNo + "'");
     }
 
     public Centre getCentre(long id) {
         return jdbcTemplate
                 .queryForObject("SELECT * FROM unit WHERE id = ?", new Object[]{id}, new CentreRowMapper());
     }
 
     public List<Centre> getCentres() {
         return jdbcTemplate.query("SELECT * FROM unit WHERE sourceType = ? ORDER BY name", new Object[]{"renalunit"},
                 new CentreRowMapper());
     }
 
     public List<Centre> getCentres(String nhsNo) {
         return jdbcTemplate.query("SELECT DISTINCT u.* " +
                 " FROM usermapping um, unit u " +
                 "WHERE um.unitcode = u.unitcode " +
                 "  AND u.sourceType = ?  " +
                 "  AND um.nhsno = ?  " +
                 "  AND um.username NOT LIKE '%-GP%'", new Object[]{"renalunit", nhsNo},
                 new CentreRowMapper());
     }
 
     public Consultant getConsultant(long id) {
         return jdbcTemplate.queryForObject("SELECT * FROM tbl_Consultants WHERE cID = ?", new Object[]{id},
                 new ConsultantRowMapper());
     }
 
     public List<Consultant> getConsultants(ConsultantFilter filter, int page, int numberPerPage) {
         if (filter == null) {
             filter = new ConsultantFilter();
         }
 
         List<String> sqlQueries = new ArrayList<String>();
         List<Object> params = new ArrayList<Object>();
 
         // normal sql query without any filter options
         sqlQueries.add("SELECT "
                     + "   tbl_Consultants.*, "
                     + "   unit.name AS cName "
                     + "FROM "
                     + "   tbl_Consultants "
                     + "INNER JOIN "
                     + "   unit "
                     + "ON "
                     + "   tbl_Consultants.cCentre = unit.id");
 
         // if there are search queries then build the where
         if (filter.hasSearchCriteria()) {
             sqlQueries.add(buildWhereQuery(filter.getSearchFields(), true, params));
         }
 
         // if the filter has a sort then order by it
         if (filter.hasSortFilter()) {
             sqlQueries.add(buildOrderQuery(filter.getSortField(), filter.isReverse()));
         }
 
         // if a range has been set limit the results
         sqlQueries.add(buildLimitQuery(page, numberPerPage, params));
 
         // combine the statement and return result
         return jdbcTemplate.query(StringUtils.join(sqlQueries.toArray(), " "), params.toArray(),
                 new ConsultantRowMapper());
     }
 
     public List<Consultant> getConsultantsByCentre(Centre centre) {
         return jdbcTemplate.query("SELECT * FROM tbl_Consultants WHERE cCentre = ?", new Object[]{centre.getId()},
                 new ConsultantRowMapper());
 
     }
 
     public void saveConsultant(final Consultant consultant) throws Exception {
         Map<String, Object> consultantMap = new HashMap<String, Object>() {
             {
                 put("cSNAME", consultant.getSurname());
                 put("cFNAME", consultant.getForename());
                 put("cCentre", consultant.getCentre().getId());
                 put("cID", consultant.getId());
             }
         };
 
         if (consultant.hasValidId()) {
             String updateSql = buildUpdateQuery("tbl_Consultants", "cID", consultantMap);
             namedParameterJdbcTemplate.update(updateSql, consultantMap);
         } else {
             Number id = consultantsInsert.executeAndReturnKey(consultantMap);
             consultant.setId(id.longValue());
         }
     }
 
     public void deleteConsultant(Consultant consultant) throws Exception {
         Map<String, Object> consultantMap = new HashMap<String, Object>();
         consultantMap.put("cID", consultant.getId());
         namedParameterJdbcTemplate.update("DELETE FROM tbl_Consultants WHERE cID = :cID;", consultantMap);
     }
 
     public Country getCountry(long id) {
         try {
             return jdbcTemplate
                     .queryForObject("SELECT * FROM tbl_Country WHERE cID = ?", new Long[]{id}, new CountryRowMapper());
         } catch (EmptyResultDataAccessException e) {
             LOGGER.debug("Could not get country with id {}", id);
             return null;
         }
 
     }
 
     public List<Country> getCountries() {
         return jdbcTemplate.query("SELECT * FROM tbl_Country", new CountryRowMapper());
     }
 
     public Ethnicity getEthnicityByCode(String ethnicityCode) {
         try {
             return jdbcTemplate.queryForObject("SELECT * FROM tbl_Ethnicity WHERE eCode = ?",
                     new Object[]{ethnicityCode}, new EthnicityRowMapper());
         } catch (EmptyResultDataAccessException e) {
             LOGGER.debug("Could not get ethnicity with code {}", ethnicityCode);
             return null;
         }
     }
 
 
     public List<Ethnicity> getEthnicities() {
         return jdbcTemplate.query("SELECT * FROM tbl_Ethnicity", new EthnicityRowMapper());
     }
 
     public Relative getRelative(long id) {
         try {
             return jdbcTemplate.queryForObject("SELECT * FROM tbl_Relative WHERE rID = ?", new Object[]{id},
                     new RelativeRowMapper());
         } catch (EmptyResultDataAccessException e) {
             LOGGER.debug("Could not get relative with ID {}", id);
             return null;
         }
     }
 
     public List<Relative> getRelatives() {
         return jdbcTemplate.query("SELECT * FROM tbl_Relative", new RelativeRowMapper());
     }
 
     public Map<Long, Integer> getPatientCountPerUnitByDiagnosisCode(DiagnosisCode diagnosisCode) {
         List<PatientCountItem> patientCountList = jdbcTemplate.query(
                 "SELECT COUNT(*) as \"count\", u.id as \"unitcode\" " +
                 "FROM   patient p " +
                 "INNER JOIN tbl_diagnosis diagnosis ON p.radarNo = diagnosis.RADAR_NO " +
                 "INNER JOIN usermapping um on p.nhsno = um.nhsno " +
                 "INNER JOIN unit u ON um.unitcode = u.unitcode " +
                 "WHERE diag = ? " +
                 "AND    u.sourceType = ? " +
                 "AND   um.username NOT LIKE '%-GP%' " +
                 "GROUP BY u.id;", new Object[]{diagnosisCode.getId(), "renalunit"},
                 new PatientCountByUnitRowMapper());
 
         Map<Long, Integer> patientCountMap = new HashMap<Long, Integer>();
 
         for (PatientCountItem item : patientCountList) {
             patientCountMap.put(item.getHospitalId(), item.getCount());
         }
         return patientCountMap;
     }
 
     public int getPatientCountByUnit(Centre centre) {
         try {
 
             StringBuilder query = new StringBuilder();
             query.append("SELECT  COUNT(DISTINCT nhsno) ");
             query.append("FROM    usermapping usm ");
             query.append("WHERE   EXISTS (SELECT  1 ");
             query.append("                FROM    patient ptt ");
             query.append("                WHERE   ptt.nhsno = usm.nhsno) ");
             query.append("AND     usm.unitCode = ? ");
 
             return jdbcTemplate.queryForObject(query.toString(), new Object[]{centre.getUnitCode()}, Integer.class);
         } catch (EmptyResultDataAccessException e) {
             return 0;
         }
     }
 
     private class CentreRowMapper implements RowMapper<Centre> {
         public Centre mapRow(ResultSet resultSet, int i) throws SQLException {
             // Create a centre and set the fields from the resultset
             Centre centre = new Centre();
             centre.setId(resultSet.getLong("id"));
             centre.setName(resultSet.getString("name"));
             centre.setAbbreviation(resultSet.getString("shortname"));
             // Set country from our DAO
             centre.setCountry(getCountry(resultSet.getLong("country")));
             centre.setUnitCode(resultSet.getString("unitcode"));
             centre.setRenalAdminEmail(resultSet.getString("renaladminemail"));
             return centre;
         }
     }
 
     private class CountryRowMapper implements RowMapper<Country> {
         public Country mapRow(ResultSet resultSet, int i) throws SQLException {
             // Create a country and set the fields from our resultset
             Country country = new Country();
             country.setId(resultSet.getLong("cID"));
             country.setName(resultSet.getString("cName"));
             return country;
         }
     }
 
     private class EthnicityRowMapper implements RowMapper<Ethnicity> {
         public Ethnicity mapRow(ResultSet resultSet, int i) throws SQLException {
             // Construct ethnicity object and set fields
             Ethnicity ethnicity = new Ethnicity();
             ethnicity.setId(resultSet.getLong("eID"));
             ethnicity.setName(resultSet.getString("eName"));
             ethnicity.setCode(resultSet.getString("eCode"));
             return ethnicity;
         }
     }
 
     private class RelativeRowMapper implements RowMapper<Relative> {
         public Relative mapRow(ResultSet resultSet, int i) throws SQLException {
             // Construct a relative object and set all the fields
             Relative relative = new Relative();
             relative.setId(resultSet.getLong("rID"));
             relative.setName(resultSet.getString("RELATIVE"));
             return relative;
         }
     }
 
     private class ConsultantRowMapper implements RowMapper<Consultant> {
         public Consultant mapRow(ResultSet resultSet, int i) throws SQLException {
             // Construct a consultant object and set all the fields
             Consultant consultant = new Consultant();
             consultant.setId(resultSet.getLong("cID"));
             consultant.setSurname(resultSet.getString("cSNAME"));
             consultant.setForename(resultSet.getString("cFNAME"));
 
             // Centre could be null, in which case we get a 0 returned by getLong
             long centreId = resultSet.getLong("cCentre");
             if (centreId > 0) {
                 consultant.setCentre(getCentre(centreId));
             }
 
             return consultant;
         }
     }
 
     private class PatientCountByUnitRowMapper implements RowMapper<PatientCountItem> {
         public PatientCountItem mapRow(ResultSet resultSet, int i) throws SQLException {
 
             return new PatientCountItem(resultSet.getLong("unitcode"), resultSet.getInt("count"));
         }
     }
 
     private class PatientCountItem {
         long hospitalId;
         int count;
 
         private PatientCountItem(long hospitalId, int count) {
             this.hospitalId = hospitalId;
             this.count = count;
         }
 
         public long getHospitalId() {
             return hospitalId;
         }
 
         public void setHospitalId(long hospitalId) {
             this.hospitalId = hospitalId;
         }
 
         public int getCount() {
             return count;
         }
 
         public void setCount(int count) {
             this.count = count;
         }
     }
 
     public Clinician getClinician(Long id) {
         List<Clinician> clinicians = jdbcTemplate.query("SELECT " +
                 " u.id, u.username, u.name, um.unitcode " +
                 "FROM user u, usermapping um " +
                 "WHERE " +
                 "    u.username = um.username " +
                 "  AND um.username NOT LIKE '%-GP%' " +
                 "  AND um.unitcode != 'PATIENT' " +
                 "AND u.id = ? ", new Long[]{id}, new ClinicianRowMapper());
 
         if (clinicians != null && !clinicians.isEmpty()) {
             return clinicians.get(0);
         }
 
         return null;
     }
 
     public List<Clinician> getClinicians(Centre centre) {
         return jdbcTemplate.query("SELECT " +
                 " u.*, um.unitcode " +
                 "FROM user u, usermapping um " +
                 "WHERE " +
                 "    u.username = um.username " +
                 "AND u.isclinician = 1 " +
                 "AND um.unitcode = ? ", new String[]{centre.getUnitCode()}, new ClinicianRowMapper());
     }
 
     public Centre getCentre(String unitCode) {
         try {
             return jdbcTemplate
                    .queryForObject("SELECT * FROM unit WHERE unitcode = ?", new Object[]{unitCode},
                            new CentreRowMapper());
         } catch (EmptyResultDataAccessException e) {
             LOGGER.error("Could not get unit with unitcode {}", unitCode);
             return null;
         }
     }
 
     public List<Centre> getRenalUnitCentre(String nhsNo) {
         try {
             return jdbcTemplate
                     .query("SELECT * FROM usermapping um LEFT OUTER JOIN unit u ON um.unitcode = u.unitcode " +
                             "WHERE um.nhsno = ? " +
                             "  AND um.username NOT LIKE '%-GP%' " +
                             "  AND um.unitcode != 'PATIENT' " +
                             "  AND u.sourceType = 'renalunit' ", new Object[]{nhsNo}, new CentreRowMapper());
         } catch (EmptyResultDataAccessException e) {
             LOGGER.debug("Could not get unit with nhsno {}", nhsNo);
             return null;
         }
     }
 
     public void deletePatient(String nshNo) {
         jdbcTemplate.execute("DELETE FROM patient WHERE nhsno  = '" + nshNo + "'");
     }
 
     public void deletePatientForRadar(Long id) {
         Map<String, Object> parameters = new HashMap<String, Object>();
         parameters.put("id", id);
         namedParameterJdbcTemplate.update("DELETE FROM tbl_patient_user WHERE id  = :id", parameters);
     }
 
     public String getUserName(String nhsNo) {
         String username = null;
 
         try {
             username = jdbcTemplate
                 .queryForObject("SELECT DISTINCT u.name FROM user u, usermapping um " +
                         "WHERE u.username = um.username " +
                         "AND um.nhsno = ? " +
                         "AND u.name NOT LIKE '%-GP%'; ", new Object[]{nhsNo}, String.class);
         } catch (EmptyResultDataAccessException era) {
             LOGGER.debug("No username result found for " + nhsNo);
         }
 
         return username;
 
     }
 
     // Does the username have any mappings to any renal units.
     public boolean isGroupAdmin(String username) {
 
         StringBuilder query = new StringBuilder();
         query.append("SELECT  DISTINCT 1 ");
         query.append("FROM    unit unt ");
         query.append(",       usermapping map ");
         query.append("WHERE   map.unitcode = unt.unitcode  ");
         query.append("AND     map.username = '");
         query.append(username);
         query.append("' ");
         query.append("AND     unt.sourceType = 'renalunit' ");
 
         try {
             jdbcTemplate.queryForObject(query.toString(), Integer.class);
         } catch (EmptyResultDataAccessException ee) {
             return true;
         }
 
         return false;
 
     }
 
     public String getUserName(Long id) {
         if (id == null) {
             return "";
         }
         try {
             return jdbcTemplate
                     .queryForObject("SELECT u.name FROM user u " +
                             "WHERE u.id = ? " +
                             "AND u.name NOT LIKE '%-GP%'; ", new Object[]{id}, String.class);
         } catch (EmptyResultDataAccessException e) {
             LOGGER.debug("Could not get user with id {}", id);
             return "";
 
         }
     }
 
     private class ClinicianRowMapper implements RowMapper<Clinician> {
         public Clinician mapRow(ResultSet resultSet, int i) throws SQLException {
             // Construct a relative object and set all the fields
             Clinician clinician = new Clinician();
 
             // In future we might need to split the fullname of the user for a clinician
             String fullName = resultSet.getString("name");
             clinician.setId(resultSet.getLong("id"));
             clinician.setSurname(fullName);
 
              // Centre could be null, in which case we get a 0 returned by getLong
             String unitcode = resultSet.getString("unitcode");
             if (unitcode != null && !"".equals(unitcode)) {
                 clinician.setCentre(getCentre(unitcode));
             }
             return clinician;
         }
     }
 }
