 package org.patientview.radar.dao.impl;
 
 import org.apache.commons.lang.StringUtils;
 import org.patientview.model.Centre;
 import org.patientview.radar.dao.UserDao;
 import org.patientview.radar.dao.UtilityDao;
 import org.patientview.radar.exception.UserCreationException;
 import org.patientview.radar.exception.UserMappingException;
 import org.patientview.radar.exception.UserRoleException;
 import org.patientview.radar.model.filter.PatientUserFilter;
 import org.patientview.radar.model.filter.ProfessionalUserFilter;
 import org.patientview.radar.model.user.AdminUser;
 import org.patientview.radar.model.user.PatientUser;
 import org.patientview.radar.model.user.ProfessionalUser;
 import org.patientview.radar.model.user.User;
 import org.patientview.radar.model.user.UserMapping;
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
 
 public class UserDaoImpl extends BaseDaoImpl implements UserDao {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(UserDaoImpl.class);
 
     // tables
     private static final String USER_TABLE_NAME = "user"; // main user table
     private static final String USER_MAPPING_TABLE_NAME = "rdr_user_mapping"; // maps user accounts to roles in radar
     private static final String PV_USER_MAPPING_TABLE_NAME = "usermapping"; // maps user accounts to units in pv
     private static final String PV_SPECIALTY_USER_ROLE_TABLE_NAME = "specialtyuserrole"; // maps user to a role in pv
     private static final String ADMIN_USER_TABLE_NAME = "tbl_adminusers"; // maps user accounts to roles in radar
     private static final String PROFESSIONAL_USER_TABLE_NAME = "tbl_users"; // rdr specific user
     private static final String PATIENT_USER_TABLE_NAME = "tbl_patient_users"; // rdr specific patient information
 
     // user mapping table fields
     private static final String ID_FIELD_NAME = "id";
     private static final String USER_MAPPING_USER_ID_FIELD_NAME = "userId";
     private static final String USER_MAPPING_ROLE_FIELD_NAME = "role";
     private static final String USER_MAPPING_RADAR_USER_ID_FIELD_NAME = "radarUserId";
 
     // pv user mapping table fields
     private static final String PV_ID_FIELD_NAME = "id";
     private static final String PV_USER_MAPPING_USERNAME_FIELD_NAME = "username";
     private static final String PV_USER_MAPPING_UNITCODE_FIELD_NAME = "unitcode";
     private static final String PV_USER_MAPPING_NHSNO_FIELD_NAME = "nhsno";
     private static final String PV_USER_MAPPING_SPECIALITY_ID_FIELD_NAME = "specialty_id";
 
     // pv specialty user role table
     private static final String PV_SPECIALTY_USER_ROLE_ROLE_FIELD_NAME = "role";
     private static final String PV_SPECIALTY_USER_ROLE_SPECIALTY_ID_FIELD_NAME = "specialty_id";
     private static final String PV_SPECIALTY_USER_ROLE_USER_ID_FIELD_NAME = "user_id";
 
     // user table fields
     private static final String USER_USERNAME_FIELD_NAME = "username";
     private static final String USER_PASSWORD_FIELD_NAME = "password";
     private static final String USER_EMAIL_FIELD_NAME = "email";
     private static final String USER_NAME_FIELD_NAME = "name";
     private static final String USER_DUMMY_PATIENT_FIELD_NAME = "dummypatient";
     private static final String USER_IS_CLINICIAN_FIELD_NAME = "isClinician";
     private static final String USER_ACCOUNT_LOCKED_FIELD_NAME = "accountlocked";
 
     // admin user fields
     private static final String ADMIN_USER_ID_FIELD_NAME = "uID";
 
     // patient table fields
     private static final String PATIENT_USER_ID_FIELD_NAME = "pId";
     private static final String PATIENT_USER_RADAR_NO_FIELD_NAME = "RADAR_NO";
     private static final String PATIENT_USER_DOB_FIELD_NAME = "pDOB";
     private static final String PATIENT_USER_DATE_OF_REGISTRATION_FIELD_NAME = "pDateReg";
 
     // professional user table fields
     private static final String PROFESSIONAL_USER_ID_FIELD_NAME = "uID";
     private static final String PROFESSIONAL_USER_SURNAME_FIELD_NAME = "uSurname";
     private static final String PROFESSIONAL_USER_FORENAME_FIELD_NAME = "uForename";
     private static final String PROFESSIONAL_USER_TITLE_FIELD_NAME = "uTitle";
     private static final String PROFESSIONAL_USER_PHONE_FIELD_NAME = "uPhone";
     private static final String PROFESSIONAL_USER_DATE_JOINED_FIELD_NAME = "uDateJoin";
     private static final String PROFESSIONAL_USER_GMC_FIELD_NAME = "uGMC";
     private static final String PROFESSIONAL_USER_CENTRE_ID_FIELD_NAME = "uCentre";
     private static final String PROFESSIONAL_USER_CENTRE_ROLE_FIELD_NAME = "uRole";
 
     private SimpleJdbcInsert userInsert;
     private SimpleJdbcInsert userMappingInsert;
     private SimpleJdbcInsert pvUserMappingInsert;
     private SimpleJdbcInsert pvSpecialtyUserRoleInsert;
     private SimpleJdbcInsert adminUsersInsert;
     private SimpleJdbcInsert professionalUsersInsert;
     private SimpleJdbcInsert patientUsersInsert;
 
     private UtilityDao utilityDao;
 
 
 
     @Override
     public void setDataSource(DataSource dataSource) {
         super.setDataSource(dataSource);
 
         userInsert = new SimpleJdbcInsert(dataSource).withTableName(USER_TABLE_NAME)
                 .usingGeneratedKeyColumns(ID_FIELD_NAME)
                 .usingColumns(USER_USERNAME_FIELD_NAME, USER_PASSWORD_FIELD_NAME,
                         USER_EMAIL_FIELD_NAME, USER_NAME_FIELD_NAME, USER_DUMMY_PATIENT_FIELD_NAME,
                         USER_ACCOUNT_LOCKED_FIELD_NAME,
                         USER_IS_CLINICIAN_FIELD_NAME);
 
         userMappingInsert = new SimpleJdbcInsert(dataSource).withTableName(USER_MAPPING_TABLE_NAME)
                 .usingGeneratedKeyColumns(ID_FIELD_NAME)
                 .usingColumns(USER_MAPPING_USER_ID_FIELD_NAME, USER_MAPPING_ROLE_FIELD_NAME,
                         USER_MAPPING_RADAR_USER_ID_FIELD_NAME);
 
         pvUserMappingInsert = new SimpleJdbcInsert(dataSource).withTableName(PV_USER_MAPPING_TABLE_NAME)
                 .usingGeneratedKeyColumns(PV_ID_FIELD_NAME)
                 .usingColumns(PV_USER_MAPPING_USERNAME_FIELD_NAME, PV_USER_MAPPING_UNITCODE_FIELD_NAME,
                         PV_USER_MAPPING_NHSNO_FIELD_NAME, PV_USER_MAPPING_SPECIALITY_ID_FIELD_NAME);
 
         pvSpecialtyUserRoleInsert = new SimpleJdbcInsert(dataSource).withTableName(PV_SPECIALTY_USER_ROLE_TABLE_NAME)
                 .usingGeneratedKeyColumns(PV_ID_FIELD_NAME)
                 .usingColumns(PV_SPECIALTY_USER_ROLE_ROLE_FIELD_NAME, PV_SPECIALTY_USER_ROLE_SPECIALTY_ID_FIELD_NAME,
                         PV_SPECIALTY_USER_ROLE_USER_ID_FIELD_NAME);
 
         adminUsersInsert = new SimpleJdbcInsert(dataSource).withTableName(ADMIN_USER_TABLE_NAME)
                 .usingGeneratedKeyColumns(ADMIN_USER_ID_FIELD_NAME);
 
         professionalUsersInsert = new SimpleJdbcInsert(dataSource).withTableName(PROFESSIONAL_USER_TABLE_NAME)
                 .usingGeneratedKeyColumns(PROFESSIONAL_USER_ID_FIELD_NAME)
                 .usingColumns(PROFESSIONAL_USER_SURNAME_FIELD_NAME, PROFESSIONAL_USER_FORENAME_FIELD_NAME,
                         PROFESSIONAL_USER_TITLE_FIELD_NAME, PROFESSIONAL_USER_GMC_FIELD_NAME,
                         PROFESSIONAL_USER_CENTRE_ROLE_FIELD_NAME,
                         PROFESSIONAL_USER_PHONE_FIELD_NAME, PROFESSIONAL_USER_CENTRE_ID_FIELD_NAME,
                         PROFESSIONAL_USER_DATE_JOINED_FIELD_NAME);
 
         patientUsersInsert = new SimpleJdbcInsert(dataSource).withTableName(PATIENT_USER_TABLE_NAME)
                 .usingGeneratedKeyColumns(PATIENT_USER_ID_FIELD_NAME)
                 .usingColumns(PATIENT_USER_RADAR_NO_FIELD_NAME, PATIENT_USER_DOB_FIELD_NAME,
                         PATIENT_USER_DATE_OF_REGISTRATION_FIELD_NAME);
     }
 
     public<T extends User> T getUser(String email) {
         UserMapping userMapping = getUserMapping(email);
 
         if (userMapping != null) {
             if (userMapping.getRole().equals(User.ROLE_ADMIN)) {
                 return (T) getAdminUser(userMapping.getRadarId());
             } else if (userMapping.getRole().equals(User.ROLE_PROFESSIONAL)
                     || userMapping.getRole().equals(User.ROLE_SUPER_USER)) {
                 return (T) getProfessionalUser(userMapping.getRadarId());
             } else if (userMapping.getRole().equals(User.ROLE_PATIENT)) {
                 return (T) getPatientUser(userMapping.getRadarId());
             }
         }
 
         return null;
     }
 
     public AdminUser getAdminUser(String email) {
         try {
             return jdbcTemplate.queryForObject(buildBaseUserSelectFromStatement(ADMIN_USER_TABLE_NAME)
                     + buildUserWhereEmailStatement(ADMIN_USER_TABLE_NAME, USER_EMAIL_FIELD_NAME,
                     ADMIN_USER_ID_FIELD_NAME, true),
                     new Object[]{email, User.ROLE_ADMIN}, new AdminUserRowMapper());
         } catch (EmptyResultDataAccessException e) {
             LOGGER.debug("Could not admin user " + USER_TABLE_NAME + " with " + USER_EMAIL_FIELD_NAME + " {}", email);
         }
 
         return null;
     }
 
     public AdminUser getAdminUserWithUsername(String username) {
         try {
             return jdbcTemplate.queryForObject(buildBaseUserSelectFromStatement(ADMIN_USER_TABLE_NAME)
                     + buildUserWhereEmailStatement(ADMIN_USER_TABLE_NAME, USER_USERNAME_FIELD_NAME,
                     ADMIN_USER_ID_FIELD_NAME, true),
                     new Object[]{username, User.ROLE_ADMIN}, new AdminUserRowMapper());
         } catch (EmptyResultDataAccessException e) {
             LOGGER.debug("Could not admin user " + USER_TABLE_NAME + " with " + USER_USERNAME_FIELD_NAME + " {}",
                     username);
         }
 
         return null;
     }
 
     public AdminUser getAdminUser(Long id) {
         try {
             return jdbcTemplate.queryForObject(buildBaseUserSelectFromStatement(ADMIN_USER_TABLE_NAME)
                     + buildUserWhereIdStatement(ADMIN_USER_TABLE_NAME, ADMIN_USER_ID_FIELD_NAME),
                     new Object[]{id, User.ROLE_ADMIN}, new AdminUserRowMapper());
         } catch (EmptyResultDataAccessException e) {
             LOGGER.debug("Could not admin user with " + ADMIN_USER_ID_FIELD_NAME + " {}", id);
         }
 
         return null;
     }
 
     public List<AdminUser> getAdminUsers() {
         String sql = buildBaseUserSelectFromStatement(ADMIN_USER_TABLE_NAME) +
                 " WHERE " + USER_MAPPING_TABLE_NAME + "." + USER_MAPPING_ROLE_FIELD_NAME
                 + " = '" + User.ROLE_ADMIN + "'" + " " +
                 " AND " + ADMIN_USER_TABLE_NAME + "." + ADMIN_USER_ID_FIELD_NAME
                 + " = " + USER_MAPPING_TABLE_NAME + "." + USER_MAPPING_RADAR_USER_ID_FIELD_NAME +
                 " AND " + USER_TABLE_NAME + "." + ID_FIELD_NAME
                 + " = " + USER_MAPPING_TABLE_NAME + "." + USER_MAPPING_USER_ID_FIELD_NAME;
 
         return jdbcTemplate.query(sql, new Object[]{}, new AdminUserRowMapper());
     }
 
     public void saveAdminUser(final AdminUser adminUser) throws Exception {
         // save details of the user into the radar tables
         Map<String, Object> adminUserMap = new HashMap<String, Object>() {
             {
                 put(ADMIN_USER_ID_FIELD_NAME, adminUser.getId());
             }
         };
 
         // the only field in this table is id so only need to do new inserts
         if (!adminUser.hasValidId()) {
             Number id = adminUsersInsert.executeAndReturnKey(adminUserMap);
             adminUser.setId(id.longValue());
         }
 
         // save main user login into the shared rpv table
         saveUser(adminUser);
     }
 
     public ProfessionalUser getProfessionalUser(Long id) {
         try {
             return jdbcTemplate.queryForObject(buildBaseUserSelectFromStatement(PROFESSIONAL_USER_TABLE_NAME)
                     + buildUserWhereIdStatement(PROFESSIONAL_USER_TABLE_NAME, PROFESSIONAL_USER_ID_FIELD_NAME),
                     new Object[]{id, User.ROLE_PROFESSIONAL}, new ProfessionalUserRowMapper());
         } catch (EmptyResultDataAccessException e) {
             LOGGER.debug("Could not professional user with " + PROFESSIONAL_USER_ID_FIELD_NAME + " {}", id);
         }
 
         return null;
     }
 
 
     public ProfessionalUser getProfessionalUserByUsername(String username) {
 
         try {
             StringBuilder query = new StringBuilder();
             query.append("SELECT  prf.* ");
             query.append(",       usr.* ");
             query.append("FROM    rdr_user_mapping rmp ");
             query.append(",       user usr ");
             query.append(",       tbl_users prf ");
             query.append("WHERE   rmp.userId = usr.id ");
             query.append("AND     prf.uId = rmp.radarUserId ");
             query.append("AND     usr.username = ?");
 
             return jdbcTemplate.queryForObject(query.toString(), new Object[]{username},
                     new ProfessionalUserRowMapper());
         } catch (EmptyResultDataAccessException e) {
             LOGGER.debug("Could not find professional user with username {}", username);
         }
 
         return null;
 
     }
 
 
     // TODO no idea why this is using email and getting passed a username but it's used in a few places
     public ProfessionalUser getProfessionalUser(String email) {
         try {
             return jdbcTemplate.queryForObject(buildBaseUserSelectFromStatement(PROFESSIONAL_USER_TABLE_NAME)
                     + buildUserWhereEmailStatement(PROFESSIONAL_USER_TABLE_NAME, USER_EMAIL_FIELD_NAME,
                     PROFESSIONAL_USER_ID_FIELD_NAME,
                     true),
                     new Object[]{email, User.ROLE_PROFESSIONAL}, new ProfessionalUserRowMapper());
         } catch (EmptyResultDataAccessException e) {
             LOGGER.debug("Could not professional user with " + USER_EMAIL_FIELD_NAME + " {}", email);
         }
 
         return null;
     }
 
     public ProfessionalUser getProfessionalUserWithUsername(String username) {
 
         ProfessionalUser professionalUser = null;
 
         try {
             professionalUser = jdbcTemplate
                     .queryForObject(buildBaseUserSelectFromStatement(PROFESSIONAL_USER_TABLE_NAME)
                             + buildUserWhereEmailStatement(PROFESSIONAL_USER_TABLE_NAME, USER_USERNAME_FIELD_NAME,
                             PROFESSIONAL_USER_ID_FIELD_NAME,
                             true),
                             new Object[]{username, User.ROLE_PROFESSIONAL}, new ProfessionalUserRowMapper());
         } catch (EmptyResultDataAccessException e) {
             LOGGER.debug("Could not professional user with " + USER_USERNAME_FIELD_NAME + " {}", username);
         }
 
         if (professionalUser != null) {
             professionalUser.setGroupAdmin(utilityDao.isGroupAdmin(username));
         }
 
 
         return professionalUser;
     }
 
     public User getSuperUserWithUsername(String username) {
 
         /**
          *  This one is an odd one.
          *
          *  Super admins are just professional users with a different role in the mapping table.
          *
          *  There was some crazy getSecurityRole() method in ProfessionalUser that restricted super user roles to
          *  certain ids.  The SuperUserRowMapper changes this behaviour
          */
 
         try {
             return jdbcTemplate.queryForObject(buildBaseUserSelectFromStatement(PROFESSIONAL_USER_TABLE_NAME)
                     + buildUserWhereEmailStatement(PROFESSIONAL_USER_TABLE_NAME, USER_USERNAME_FIELD_NAME,
                     PROFESSIONAL_USER_ID_FIELD_NAME,
                     true),
                     new Object[]{username, User.ROLE_SUPER_USER}, new SuperUserRowMapper());
         } catch (EmptyResultDataAccessException e) {
             LOGGER.debug("Could not professional user with " + USER_USERNAME_FIELD_NAME + " {}", username);
         }
 
         return null;
     }
 
     public List<ProfessionalUser> getProfessionalUsers(ProfessionalUserFilter filter, int page, int numberPerPage) {
         if (filter == null) {
             filter = new ProfessionalUserFilter();
         }
 
         List<String> sqlQueries = new ArrayList<String>();
         List<Object> params = new ArrayList<Object>();
 
         // normal sql query without any filter options
         sqlQueries.add(buildSelectFromStatement(USER_TABLE_NAME, USER_MAPPING_TABLE_NAME,
                 PROFESSIONAL_USER_TABLE_NAME, "unit") +
                 " WHERE " + USER_MAPPING_TABLE_NAME + "." + USER_MAPPING_ROLE_FIELD_NAME
                 + " = '" + User.ROLE_PROFESSIONAL + "'" + " " +
                 " AND " + PROFESSIONAL_USER_TABLE_NAME + "." + PROFESSIONAL_USER_ID_FIELD_NAME
                 + " = " + USER_MAPPING_TABLE_NAME + "." + USER_MAPPING_RADAR_USER_ID_FIELD_NAME +
                 " AND " + USER_TABLE_NAME + "." + ID_FIELD_NAME
                 + " = " + USER_MAPPING_TABLE_NAME + "." + USER_MAPPING_USER_ID_FIELD_NAME +
                 " AND " + PROFESSIONAL_USER_TABLE_NAME + "." + PROFESSIONAL_USER_CENTRE_ID_FIELD_NAME
                 + " = unit.id"
         );
 
         // if there are search queries then build the where
         if (filter.hasSearchCriteria()) {
             sqlQueries.add(" AND " + buildWhereQuery(false, filter.getSearchFields(), true, params));
         }
 
         // if the filter has a sort then order by it
         if (filter.hasSortFilter()) {
             sqlQueries.add(buildOrderQuery(filter.getSortField(), filter.isReverse()));
         }
 
         // if a range has been set limit the results
         sqlQueries.add(buildLimitQuery(page, numberPerPage, params));
 
         // combine the statement and return result
         return jdbcTemplate.query(StringUtils.join(sqlQueries.toArray(), " "), params.toArray(),
                 new ProfessionalUserRowMapper());
     }
 
     public void saveProfessionalUser(final ProfessionalUser professionalUser) throws Exception {
         // save details of the user into the radar tables
         Map<String, Object> professionalUserMap = new HashMap<String, Object>() {
             {
                 put(PROFESSIONAL_USER_SURNAME_FIELD_NAME, professionalUser.getSurname());
                 put(PROFESSIONAL_USER_FORENAME_FIELD_NAME, professionalUser.getForename());
                 put(PROFESSIONAL_USER_TITLE_FIELD_NAME, professionalUser.getTitle());
                 put(PROFESSIONAL_USER_GMC_FIELD_NAME, professionalUser.getGmc());
                 put(PROFESSIONAL_USER_CENTRE_ROLE_FIELD_NAME, professionalUser.getRole());
                 put(PROFESSIONAL_USER_PHONE_FIELD_NAME, professionalUser.getPhone());
                 put(PROFESSIONAL_USER_CENTRE_ID_FIELD_NAME, professionalUser.getCentre().getId());
                 put(PROFESSIONAL_USER_DATE_JOINED_FIELD_NAME, professionalUser.getDateRegistered());
                 put(PROFESSIONAL_USER_ID_FIELD_NAME, professionalUser.getId());
             }
         };
 
         if (professionalUser.hasValidId()) {
             String updateSql = buildUpdateQuery(PROFESSIONAL_USER_TABLE_NAME, PROFESSIONAL_USER_ID_FIELD_NAME,
                     professionalUserMap);
             namedParameterJdbcTemplate.update(updateSql, professionalUserMap);
         } else {
             Number id = professionalUsersInsert.executeAndReturnKey(professionalUserMap);
             professionalUser.setId(id.longValue());
         }
 
         // save main user login into the shared rpv table
         saveUser(professionalUser);
 
         // create a mapping and role so they can login to PV
         // make sure we have all the centre data as sometimes it just the id set
         Centre centre = utilityDao.getCentre(professionalUser.getCentre().getId());
 
         createUserMappingAndRoleInPatientView(professionalUser.getUserId(), professionalUser.getUsername(), null,
                 centre.getUnitCode(), "unitadmin");
     }
 
     public void deleteProfessionalUser(ProfessionalUser professionalUser) throws Exception {
         // delete the main user login object
         deleteUser(professionalUser);
 
         // delete the radar specific user information for professional users
         Map<String, Object> professionalUserMap = new HashMap<String, Object>();
         professionalUserMap.put(PROFESSIONAL_USER_ID_FIELD_NAME, professionalUser.getId());
 
         namedParameterJdbcTemplate.update("DELETE FROM " + PROFESSIONAL_USER_TABLE_NAME + " WHERE "
                 + PROFESSIONAL_USER_ID_FIELD_NAME + " = :" + PROFESSIONAL_USER_ID_FIELD_NAME, professionalUserMap);
 
         // remove mappings and roles in PV
         deleteUserMappingInPatientView(professionalUser.getUsername());
         deleteRoleInPatientView(professionalUser.getUserId());
     }
 
     public PatientUser getPatientUser(Long id) {
         try {
             return jdbcTemplate.queryForObject(buildBaseUserSelectFromStatement(PATIENT_USER_TABLE_NAME)
                     + buildUserWhereIdStatement(PATIENT_USER_TABLE_NAME, PATIENT_USER_ID_FIELD_NAME),
                     new Object[]{id, User.ROLE_PATIENT}, new PatientUserRowMapper());
         } catch (EmptyResultDataAccessException e) {
             LOGGER.debug("Could not patient user with " + ID_FIELD_NAME + " {}", id);
         }
 
         return null;
     }
 
     public PatientUser getPatientUser(String email) {
         try {
             String query =  buildBaseUserSelectFromStatement(PATIENT_USER_TABLE_NAME)
                     + buildUserWhereEmailStatement(PATIENT_USER_TABLE_NAME, USER_EMAIL_FIELD_NAME,
                     PATIENT_USER_ID_FIELD_NAME, true);
 
             return jdbcTemplate.queryForObject(query,
                     new Object[]{email, User.ROLE_PATIENT}, new PatientUserRowMapper());
         } catch (EmptyResultDataAccessException e) {
             LOGGER.debug("Could not patient user with " + USER_EMAIL_FIELD_NAME + " {}", email);
         }
 
         return null;
     }
 
     public PatientUser getPatientUserWithUsername(String username) {
         try {
             return jdbcTemplate.queryForObject(buildBaseUserSelectFromStatement(PATIENT_USER_TABLE_NAME)
                     + buildUserWhereEmailStatement(PATIENT_USER_TABLE_NAME, USER_USERNAME_FIELD_NAME,
                     PATIENT_USER_ID_FIELD_NAME, true),
                     new Object[]{username, User.ROLE_PATIENT}, new PatientUserRowMapper());
         } catch (EmptyResultDataAccessException e) {
             LOGGER.debug("Could not patient find user with " + USER_USERNAME_FIELD_NAME + " {}", username);
         }
 
         return null;
     }
 
     public PatientUser getPatientUserByRadarNo(Long radarNo) {
         try {
             List<PatientUser> patients = jdbcTemplate.query(buildSelectFromStatement(PATIENT_USER_TABLE_NAME)
                     + " where radar_no = ?",
                     new Object[]{radarNo}, new BasicPatientUserRowMapper());
 
             if (patients != null && patients.size() > 1) {
                 LOGGER.error("Duplicate patient users found for radarno {}, taking first", radarNo);
             }
 
             return patients != null && patients.size() > 0 ? patients.get(0) : null;
         } catch (EmptyResultDataAccessException e) {
             LOGGER.debug("Could not patient user with " + PATIENT_USER_RADAR_NO_FIELD_NAME + " {}", radarNo);
         }
 
         return null;
     }
 
     public User createUser(User user) {
         Map<String, Object> userMap = new HashMap<String, Object>();
         userMap.put(USER_USERNAME_FIELD_NAME, user.getUsername());
         userMap.put(USER_PASSWORD_FIELD_NAME, user.getPassword());
         userMap.put(USER_NAME_FIELD_NAME, user.getName());
         userMap.put(USER_EMAIL_FIELD_NAME, user.getEmail());
         userMap.put(USER_DUMMY_PATIENT_FIELD_NAME, false);
        userMap.put(USER_ACCOUNT_LOCKED_FIELD_NAME, false);
         userMap.put(USER_IS_CLINICIAN_FIELD_NAME, false);
 
         Number id = userInsert.executeAndReturnKey(userMap);
         user.setId(id.longValue());
         return user;
 
     }
 
     public List<String> getUnitCodes(User user) {
 
         return jdbcTemplate.queryForList("SELECT DISTINCT unitcode FROM usermapping WHERE username = ?",
                 new Object[]{user.getUsername()}, String.class);
     }
 
     public List<String> getAllUnitCodes() {
         return jdbcTemplate.queryForList("SELECT DISTINCT unitcode FROM unit",
                 new Object[]{}, String.class);
     }
 
     public Long createLockedPVUser(String username, String password, String name, String email) throws Exception {
 
         Map<String, Object> userMap = new HashMap<String, Object>();
         userMap.put(USER_USERNAME_FIELD_NAME, username);
         userMap.put(USER_PASSWORD_FIELD_NAME, password);
         userMap.put(USER_NAME_FIELD_NAME, name);
         userMap.put(USER_EMAIL_FIELD_NAME, email);
         userMap.put(USER_DUMMY_PATIENT_FIELD_NAME, false);
         userMap.put(USER_ACCOUNT_LOCKED_FIELD_NAME, true);
         userMap.put(USER_IS_CLINICIAN_FIELD_NAME, false);
 
         Number id = userInsert.executeAndReturnKey(userMap);
 
         createRoleInPatientView(id.longValue(), "patient");
 
         return id.longValue();
     }
 
     // users are created in Patient View without our radar mappings
     public PatientUser getPatientViewUser(String nhsno) {
         try {
 
             String sql = "SELECT DISTINCT u.* FROM USER u, usermapping m WHERE u.username = m.username AND nhsno = ? " +
                     "AND u.name NOT LIKE '%-GP%'";
 
 
             List<PatientUser> patients = jdbcTemplate.query(sql,
                     new Object[]{nhsno}, new ExternallyCreatedPatientUserRowMapper());
 
             if (patients != null && patients.size() > 1) {
                 LOGGER.error("Duplicate patient user found for nhsno {}, taking first", nhsno);
             }
 
             return patients != null && patients.size() > 0 ? patients.get(0) : null;
 
         } catch (Exception e) {
             LOGGER.debug("Could not patient user with nhsno {}", nhsno);
         }
 
         return null;
     }
 
     public List<PatientUser> getPatientUsers(PatientUserFilter filter, int page, int numberPerPage) {
         if (filter == null) {
             filter = new PatientUserFilter();
         }
 
         List<String> sqlQueries = new ArrayList<String>();
         List<Object> params = new ArrayList<Object>();
 
         // normal sql query without any filter options
         String query =  buildBaseUserSelectFromStatement(PATIENT_USER_TABLE_NAME) +
                 " WHERE " + USER_MAPPING_TABLE_NAME + "." + USER_MAPPING_ROLE_FIELD_NAME
                 + " = '" + User.ROLE_PATIENT + "'" + " " +
                 " AND " + PATIENT_USER_TABLE_NAME + "." + PATIENT_USER_ID_FIELD_NAME
                 + " = " + USER_MAPPING_TABLE_NAME + "." + USER_MAPPING_RADAR_USER_ID_FIELD_NAME +
                 " AND " + USER_TABLE_NAME + "." + ID_FIELD_NAME
                 + " = " + USER_MAPPING_TABLE_NAME + "." + USER_MAPPING_USER_ID_FIELD_NAME;
 
         sqlQueries.add(query);
 
         // if there are search queries then build the where
         if (filter.hasSearchCriteria()) {
             sqlQueries.add(" AND " + buildWhereQuery(false, filter.getSearchFields(), true, params));
         }
 
         // if the filter has a sort then order by it
         if (filter.hasSortFilter()) {
             sqlQueries.add(buildOrderQuery(filter.getSortField(), filter.isReverse()));
         }
 
         // if a range has been set limit the results
         sqlQueries.add(buildLimitQuery(page, numberPerPage, params));
 
         // combine the statement and return result
         return jdbcTemplate.query(StringUtils.join(sqlQueries.toArray(), " "), params.toArray(),
                 new PatientUserRowMapper());
     }
 
     public void savePatientUser(final PatientUser patientUser) throws UserCreationException {
         // save details of the user into the radar tables
         try {
             Map<String, Object> patientUserMap = new HashMap<String, Object>() {
                 {
                     put(PATIENT_USER_ID_FIELD_NAME, patientUser.getId());
                     put(PATIENT_USER_RADAR_NO_FIELD_NAME, patientUser.getRadarNumber());
                     put(PATIENT_USER_DOB_FIELD_NAME, patientUser.getDateOfBirth());
                     put(PATIENT_USER_DATE_OF_REGISTRATION_FIELD_NAME, patientUser.getDateRegistered());
                 }
             };
 
             if (patientUser.hasValidId()) {
                 String updateSql = buildUpdateQuery(PATIENT_USER_TABLE_NAME, PATIENT_USER_ID_FIELD_NAME,
                         patientUserMap);
                 namedParameterJdbcTemplate.update(updateSql, patientUserMap);
             } else {
                 Number id = patientUsersInsert.executeAndReturnKey(patientUserMap);
                 patientUser.setId(id.longValue());
             }
         } catch (Exception e) {
             LOGGER.error("There has been an exception creating the radar user");
             throw new UserCreationException("Error creating the radar user", e);
         }
 
     }
 
     public void deletePatientUser(PatientUser patientUser) throws Exception {
         // delete the main user login object
         deleteUser(patientUser);
 
         // delete the radar specific user information for professional users
         Map<String, Object> professionalUserMap = new HashMap<String, Object>();
         professionalUserMap.put(PATIENT_USER_ID_FIELD_NAME, patientUser.getId());
 
         namedParameterJdbcTemplate.update("DELETE FROM " + PATIENT_USER_TABLE_NAME + " WHERE "
                 + PATIENT_USER_ID_FIELD_NAME + " = :" + PATIENT_USER_ID_FIELD_NAME, professionalUserMap);
     }
 
     public boolean userExistsInPatientView(String nhsno) {
 
         if (nhsno == null || nhsno.length() == 0) {
             throw new IllegalArgumentException("Missing required param: nhsno");
         }
 
         String sql = "SELECT COUNT(*) FROM usermapping WHERE nhsno = ?";
         return jdbcTemplate.queryForInt(sql, nhsno) > 0;
     }
 
     public boolean usernameExistsInPatientView(String username) {
 
 
         if (username == null || username.length() == 0) {
             throw new IllegalArgumentException("Missing required param: username");
         }
 
         String sql = "SELECT COUNT(*) FROM user WHERE username = ?";
         return jdbcTemplate.queryForInt(sql, username) > 0;
     }
 
     public boolean userExistsInPatientView(String nhsno, String unitcode) {
 
         if (nhsno == null || nhsno.length() == 0) {
             throw new IllegalArgumentException("Missing required param: nhsno");
         }
 
         if (unitcode == null || unitcode.length() == 0) {
             throw new IllegalArgumentException("Missing required param: unitcode");
         }
 
         String sql = "SELECT COUNT(*) FROM usermapping WHERE nhsno = ? AND unitcode = ?";
         return jdbcTemplate.queryForInt(sql, nhsno, unitcode) > 0;
     }
 
     /**
      * TODO UserMapping object already exists but for Radar. Change this to return a object!
      *
      * @param nhsNo
      * @return
      */
     public List<String> getPatientRadarMappings(String nhsNo) {
 
         StringBuilder query = new StringBuilder();
         query.append("SELECT un.unitcode ");
         query.append("FROM   usermapping mp ");
         query.append(",      unit un ");
         query.append("WHERE  un.unitcode = mp.unitcode ");
         query.append("AND    mp.nhsno = '");
         query.append(nhsNo);
         query.append("' ");
         query.append("AND    un.sourceType = 'radargroup' ");
 
         return jdbcTemplate.queryForList(query.toString(), String.class) ;
 
     }
 
     public void createUserMappingAndRoleInPatientView(Long userId, String username, String nhsno, String unitcode,
                                                       String rpvRole) throws Exception {
         createUserMappingInPatientView(username, nhsno, unitcode);
         createRoleInPatientView(userId, rpvRole);
     }
 
     public void createUserMappingInPatientView(String username, String nhsno, String unitcode) throws Exception {
 
         // also need to create a usermapping so this user can also log into rpv to add users
         Map<String, Object> userMappingMap = new HashMap<String, Object>();
         userMappingMap.put(PV_USER_MAPPING_USERNAME_FIELD_NAME, username);
         userMappingMap.put(PV_USER_MAPPING_UNITCODE_FIELD_NAME, unitcode);
         userMappingMap.put(PV_USER_MAPPING_NHSNO_FIELD_NAME, nhsno);
         userMappingMap.put(PV_USER_MAPPING_SPECIALITY_ID_FIELD_NAME, 1);
 
         // add mapping
         pvUserMappingInsert.execute(userMappingMap);
     }
 
     public void deleteUserMappingInPatientView(String username) throws Exception {
         Map<String, Object> userMappingMap = new HashMap<String, Object>();
         userMappingMap.put(PV_USER_MAPPING_USERNAME_FIELD_NAME, username);
         userMappingMap.put(PV_USER_MAPPING_SPECIALITY_ID_FIELD_NAME, 1);
 
         namedParameterJdbcTemplate.update("DELETE FROM " + PV_USER_MAPPING_TABLE_NAME + " WHERE "
                 + PV_USER_MAPPING_USERNAME_FIELD_NAME + " = :" + PV_USER_MAPPING_USERNAME_FIELD_NAME + " AND " +
                 PV_USER_MAPPING_SPECIALITY_ID_FIELD_NAME + " = :" + PV_USER_MAPPING_SPECIALITY_ID_FIELD_NAME,
                 userMappingMap);
     }
 
     public void createRoleInPatientView(Long userId, String rpvRole) throws UserRoleException {
         try {
             Map<String, Object> specialtyUserRoleMap = new HashMap<String, Object>();
             specialtyUserRoleMap.put(PV_SPECIALTY_USER_ROLE_ROLE_FIELD_NAME, rpvRole);
             specialtyUserRoleMap.put(PV_SPECIALTY_USER_ROLE_SPECIALTY_ID_FIELD_NAME, 1);
             specialtyUserRoleMap.put(PV_SPECIALTY_USER_ROLE_USER_ID_FIELD_NAME, userId);
 
             pvSpecialtyUserRoleInsert.execute(specialtyUserRoleMap);
         } catch (Exception e) {
             LOGGER.error("The has been an error creating the user specialty role", e);
             new UserRoleException("Error creating the specialty user role", e);
 
         }
     }
 
     public void deleteRoleInPatientView(Long userId) throws Exception {
         Map<String, Object> specialtyUserRoleMap = new HashMap<String, Object>();
         specialtyUserRoleMap.put(PV_SPECIALTY_USER_ROLE_USER_ID_FIELD_NAME, userId);
         specialtyUserRoleMap.put(PV_SPECIALTY_USER_ROLE_SPECIALTY_ID_FIELD_NAME, 1);
 
         namedParameterJdbcTemplate.update("DELETE FROM " + PV_SPECIALTY_USER_ROLE_TABLE_NAME + " WHERE "
                 + PV_SPECIALTY_USER_ROLE_USER_ID_FIELD_NAME + " = :" + PV_SPECIALTY_USER_ROLE_USER_ID_FIELD_NAME
                 + " AND " + PV_SPECIALTY_USER_ROLE_SPECIALTY_ID_FIELD_NAME + " = :" +
                 PV_SPECIALTY_USER_ROLE_SPECIALTY_ID_FIELD_NAME,
                 specialtyUserRoleMap);
     }
 
     /**
      * Will map the base user properties from the RPV user table to the user being pulled out for radar
      * @param resultSet ResultSet
      * @param user User object to map to
      * @return User
      * @throws SQLException
      */
     private User mapUserObject(ResultSet resultSet, User user) throws SQLException {
         user.setUserId(resultSet.getLong(ID_FIELD_NAME));
         user.setUsername(resultSet.getString(USER_USERNAME_FIELD_NAME));
         user.setPassword(resultSet.getString(USER_PASSWORD_FIELD_NAME));
         user.setEmail(resultSet.getString(USER_EMAIL_FIELD_NAME));
         user.setName(resultSet.getString(USER_NAME_FIELD_NAME));
         return user;
     }
 
     /**
      * Will save the base user properties to the shared table with RPV
      * @param user User
      */
     private void saveUser(User user) throws Exception {
         Map<String, Object> userMap = new HashMap<String, Object>();
         userMap.put(ID_FIELD_NAME, user.getUserId());
         userMap.put(USER_USERNAME_FIELD_NAME, user.getUsername());
         userMap.put(USER_PASSWORD_FIELD_NAME, user.getPassword());
         userMap.put(USER_NAME_FIELD_NAME, user.getName());
         userMap.put(USER_EMAIL_FIELD_NAME, user.getEmail());
         userMap.put(USER_DUMMY_PATIENT_FIELD_NAME, false);
         userMap.put(USER_IS_CLINICIAN_FIELD_NAME, user.isClinician());
 
         if (user.hasValidUserId()) {
             namedParameterJdbcTemplate.update(buildUpdateQuery(USER_TABLE_NAME, ID_FIELD_NAME, userMap), userMap);
         } else {
             Number id = userInsert.executeAndReturnKey(userMap);
             user.setUserId(id.longValue());
         }
 
         // have to also create a record in the radar mapping table so we know what role it is
         if (user.hasValidId()) {
             saveUserMapping(user);
         }
     }
 
     /**
      * Remove a user from radar - this will delete the record in the shared RPV table and the radar user mapping
      * @param user User
      */
     public void deleteUser(User user) throws Exception {
         Map<String, Object> userMap = new HashMap<String, Object>();
         userMap.put(ID_FIELD_NAME, user.getUserId());
 
         // delete the main user object
         namedParameterJdbcTemplate.update("DELETE FROM " + USER_TABLE_NAME + " WHERE "
                 + ID_FIELD_NAME + " = :" + ID_FIELD_NAME, userMap);
 
         // delete the user mapping for this user
         deleteUserMapping(user);
     }
 
     /**
      * Users are saved in a shared table with RPV
      * Radar has different roles to RPV and has its own user mapping table that has the role the user has been assigned
      * @param user User
      */
     public void saveUserMapping(User user) throws UserMappingException {
 
         // we only ever want one user mapping per user so just delete any existing and re add
         Map<String, Object> userMap = new HashMap<String, Object>();
         userMap.put(USER_MAPPING_USER_ID_FIELD_NAME, user.getUserId());
         userMap.put(USER_MAPPING_ROLE_FIELD_NAME, user.getSecurityRole());
         userMap.put(USER_MAPPING_RADAR_USER_ID_FIELD_NAME, user.getId());
 
         try {
             // delete any mappings already so we dont end up with two
             deleteUserMapping(user);
 
             // add mapping
             userMappingInsert.execute(userMap);
         } catch (Exception e) {
             LOGGER.error("Error creating a user mapping in radar", e);
             throw new UserMappingException("Could not create radar mapping", e);
         }
     }
 
     /**
      * Delete any user mappings roles for this user
      * @param user User
      */
     public void deleteUserMapping(User user) throws Exception {
         Map<String, Object> userMap = new HashMap<String, Object>();
         userMap.put(USER_MAPPING_USER_ID_FIELD_NAME, user.getUserId());
 
         namedParameterJdbcTemplate.update("DELETE FROM " + USER_MAPPING_TABLE_NAME + " WHERE "
                 + USER_MAPPING_USER_ID_FIELD_NAME + " = :" + USER_MAPPING_USER_ID_FIELD_NAME, userMap);
     }
 
     public UserMapping getUserMapping(Long userId, Long radarUserId, String role) {
         try {
             String sql = buildSelectFromStatement(USER_MAPPING_TABLE_NAME) +
                     " WHERE " + USER_MAPPING_TABLE_NAME + "." + USER_MAPPING_USER_ID_FIELD_NAME + " = ? "  +
                     " AND " + USER_MAPPING_TABLE_NAME + "." + USER_MAPPING_RADAR_USER_ID_FIELD_NAME + " = ? "  +
                     " AND " + USER_MAPPING_TABLE_NAME + "." + USER_MAPPING_ROLE_FIELD_NAME + " = ? ";
             return jdbcTemplate.queryForObject(sql,
                     new Object[]{userId, radarUserId, role}, new UserMappingRowMapper());
         } catch (EmptyResultDataAccessException e) {
             LOGGER.debug("Could not getUserMapping " + USER_MAPPING_USER_ID_FIELD_NAME + " {}", userId);
         }
 
         return null;
     }
 
 
     private UserMapping getUserMapping(String email) {
         try {
             return jdbcTemplate.queryForObject(buildSelectFromStatement(USER_TABLE_NAME, USER_MAPPING_TABLE_NAME) +
                     " WHERE " +
                     USER_TABLE_NAME + "." + USER_EMAIL_FIELD_NAME + " = ? " +
                     " AND " +
                     USER_TABLE_NAME + "." + ID_FIELD_NAME + " = " + USER_MAPPING_TABLE_NAME + "."
                     + USER_MAPPING_USER_ID_FIELD_NAME,
                     new Object[]{email}, new UserMappingRowMapper());
         } catch (EmptyResultDataAccessException e) {
             LOGGER.debug("Could not patient user with " + USER_EMAIL_FIELD_NAME + " {}", email);
         }
 
         return null;
     }
 
     /**
      * Will build the main select/from statement for the users table
      * @param userTable Radar user table to use in the select
      * @return String
      */
     private String buildBaseUserSelectFromStatement(String userTable) {
         return buildSelectFromStatement(USER_TABLE_NAME, USER_MAPPING_TABLE_NAME, userTable);
     }
 
     /**
      * Will take a list of tables and build a select from statement in the form of
      * SELECT tableName1.*, tableName2 FROM tableName1, tableName2
      * @param tableNames List of tables to include in the select
      * @return String
      */
     private String buildSelectFromStatement(String... tableNames) {
         String sql = "SELECT ";
         int count = 0;
 
         // build the field selets, we are just assuming all rows from each table
         for (String table : tableNames) {
             sql += table + ".*";
 
             if (count < tableNames.length - 1) {
                 sql += ", ";
             }
 
             count++;
         }
 
         return sql += " FROM " + StringUtils.join(tableNames, ", ");
     }
 
     /**
      * Builds a general user select statement to pull back by the radar user id not the RPV user id
      * @param userTable User table we are selecting from
      * @param mapIdField ID field of that user table to match to
      * @return String
      */
     private String buildUserWhereIdStatement(String userTable, String mapIdField) {
         return " WHERE " +
                 USER_MAPPING_TABLE_NAME + "." + USER_MAPPING_RADAR_USER_ID_FIELD_NAME + " = ? " +
                 " AND " +
                 USER_MAPPING_TABLE_NAME + "." + USER_MAPPING_ROLE_FIELD_NAME + " = ? " +
                 " AND " +
                 userTable + "." + mapIdField + " = " + USER_MAPPING_TABLE_NAME + "."
                 + USER_MAPPING_RADAR_USER_ID_FIELD_NAME +
                 " AND " +
                 USER_TABLE_NAME + "." + ID_FIELD_NAME + " = " + USER_MAPPING_TABLE_NAME
                 + "." + USER_MAPPING_USER_ID_FIELD_NAME;
     }
 
     /**
      * Builds a general user select statement to pull back by the email address of the user
      * @param userTable User table we are selecting from
      * @param mapIdField ID field of that user table to match to
      * @return String
      */
     private String buildUserWhereEmailStatement(String userTable, String whereField, String mapIdField,
                                                 boolean includeRole) {
         String sql = " WHERE " +
                     USER_TABLE_NAME + "." + whereField + " = ? ";
                 if (includeRole) {
                     sql += "AND " +
                     USER_MAPPING_TABLE_NAME + "." + USER_MAPPING_ROLE_FIELD_NAME + " = ? ";
                 }
                 sql += "AND " +
                     USER_TABLE_NAME + "." + ID_FIELD_NAME + " = " + USER_MAPPING_TABLE_NAME + "."
                     + USER_MAPPING_USER_ID_FIELD_NAME + " " +
                     "AND " +
                     userTable + "." + mapIdField + " = " + USER_MAPPING_TABLE_NAME + "."
                     + USER_MAPPING_RADAR_USER_ID_FIELD_NAME;
 
         return sql;
     }
 
     private class AdminUserRowMapper implements RowMapper<AdminUser> {
         public AdminUser mapRow(ResultSet resultSet, int i) throws SQLException {
             AdminUser adminUser = (AdminUser) mapUserObject(resultSet, new AdminUser());
             adminUser.setId(resultSet.getLong(ADMIN_USER_ID_FIELD_NAME));
             return adminUser;
         }
     }
 
     private class ProfessionalUserRowMapper implements RowMapper<ProfessionalUser> {
         public ProfessionalUser mapRow(ResultSet resultSet, int i) throws SQLException {
             // map the base user properties
             ProfessionalUser professionalUser = (ProfessionalUser) mapUserObject(resultSet, new ProfessionalUser());
 
             // now map the specific props for this user in radar
             professionalUser.setId(resultSet.getLong(PROFESSIONAL_USER_ID_FIELD_NAME));
             professionalUser.setSurname(resultSet.getString(PROFESSIONAL_USER_SURNAME_FIELD_NAME));
             professionalUser.setForename(resultSet.getString(PROFESSIONAL_USER_FORENAME_FIELD_NAME));
             professionalUser.setTitle(resultSet.getString(PROFESSIONAL_USER_TITLE_FIELD_NAME));
             professionalUser.setGmc(resultSet.getString(PROFESSIONAL_USER_GMC_FIELD_NAME));
             professionalUser.setRole(resultSet.getString(PROFESSIONAL_USER_CENTRE_ROLE_FIELD_NAME));
             professionalUser.setPhone(resultSet.getString(PROFESSIONAL_USER_PHONE_FIELD_NAME));
             professionalUser.setDateRegistered(resultSet.getDate(PROFESSIONAL_USER_DATE_JOINED_FIELD_NAME));
             professionalUser.setSecurityRole(User.ROLE_PROFESSIONAL);
 
             // Set the centre
             Long centreId = resultSet.getLong(PROFESSIONAL_USER_CENTRE_ID_FIELD_NAME);
             if (centreId != null && centreId > 0) {
                 professionalUser.setCentre(utilityDao.getCentre(centreId));
             }
 
 
 
             return professionalUser;
         }
     }
 
     private class SuperUserRowMapper extends ProfessionalUserRowMapper {
         @Override
         public ProfessionalUser mapRow(ResultSet resultSet, int i) throws SQLException {
             ProfessionalUser professionalUser = super.mapRow(resultSet, i);
             professionalUser.setSecurityRole(User.ROLE_SUPER_USER);
             return professionalUser;
         }
     }
 
     private class PatientUserRowMapper implements RowMapper<PatientUser> {
         public PatientUser mapRow(ResultSet resultSet, int i) throws SQLException {
             // map the base user properties
             PatientUser patientUser = (PatientUser) mapUserObject(resultSet, new PatientUser());
 
             // map radar specific ones
             patientUser.setId(resultSet.getLong(PATIENT_USER_ID_FIELD_NAME));
             patientUser.setDateOfBirth(resultSet.getDate(PATIENT_USER_DOB_FIELD_NAME));
             patientUser.setDateRegistered(resultSet.getDate(PATIENT_USER_DATE_OF_REGISTRATION_FIELD_NAME));
             patientUser.setRadarNumber(resultSet.getLong(PATIENT_USER_RADAR_NO_FIELD_NAME));
 
             // legacy - patients never had emails but rpv table does so just patient username
             if (patientUser.getEmail() == null || patientUser.getEmail().length() == 0) {
                 patientUser.setEmail(patientUser.getUsername());
             }
 
             return patientUser;
         }
     }
 
     private class BasicPatientUserRowMapper implements RowMapper<PatientUser> {
         public PatientUser mapRow(ResultSet resultSet, int i) throws SQLException {
             // map the base user properties
             PatientUser patientUser = new PatientUser();
 
             // map radar specific ones
             patientUser.setId(resultSet.getLong(PATIENT_USER_ID_FIELD_NAME));
             patientUser.setDateOfBirth(resultSet.getDate(PATIENT_USER_DOB_FIELD_NAME));
             patientUser.setDateRegistered(resultSet.getDate(PATIENT_USER_DATE_OF_REGISTRATION_FIELD_NAME));
             patientUser.setRadarNumber(resultSet.getLong(PATIENT_USER_RADAR_NO_FIELD_NAME));
 
             return patientUser;
         }
     }
 
     private class ExternallyCreatedPatientUserRowMapper implements RowMapper<PatientUser> {
         public PatientUser mapRow(ResultSet resultSet, int i) throws SQLException {
             // map the base user properties
             return (PatientUser) mapUserObject(resultSet, new PatientUser());
         }
     }
 
     private class UserMappingRowMapper implements RowMapper<UserMapping> {
         public UserMapping mapRow(ResultSet resultSet, int i) throws SQLException {
             UserMapping userMapping = new UserMapping();
 
             userMapping.setUserId(resultSet.getLong(USER_MAPPING_USER_ID_FIELD_NAME));
             userMapping.setRadarId(resultSet.getLong(USER_MAPPING_RADAR_USER_ID_FIELD_NAME));
             userMapping.setRole(resultSet.getString(USER_MAPPING_ROLE_FIELD_NAME));
 
             return userMapping;
         }
     }
 
     public void setUtilityDao(UtilityDao utilityDao) {
         this.utilityDao = utilityDao;
     }
 }
