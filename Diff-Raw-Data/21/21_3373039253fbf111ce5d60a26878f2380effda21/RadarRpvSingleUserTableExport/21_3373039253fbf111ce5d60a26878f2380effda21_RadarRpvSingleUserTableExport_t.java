 package com.solidstategroup.radar.util;
 
 import com.mchange.v2.c3p0.ComboPooledDataSource;
 import com.solidstategroup.radar.dao.UserDao;
 import com.solidstategroup.radar.dao.UtilityDao;
 import com.solidstategroup.radar.model.user.AdminUser;
 import com.solidstategroup.radar.model.user.PatientUser;
 import com.solidstategroup.radar.model.user.ProfessionalUser;
 import com.solidstategroup.radar.model.user.User;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.core.RowMapper;
 import org.springframework.web.context.WebApplicationContext;
 import org.springframework.web.context.support.WebApplicationContextUtils;
 
 import javax.servlet.ServletContext;
 import javax.sql.DataSource;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Arrays;
 import java.util.List;
 
 public class RadarRpvSingleUserTableExport {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(RadarRpvSingleUserTableExport.class);
 
     @Autowired
     private UserDao userDao;
 
     @Autowired
     private UtilityDao utilityDao;
 
     @SpringBean
     private ComboPooledDataSource dataSource;
 
     protected JdbcTemplate jdbcTemplate;
 
     public void run(ServletContext servletContext) {
         WebApplicationContext webApplicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(
                 servletContext);
 
         jdbcTemplate = new JdbcTemplate((DataSource) webApplicationContext.getBean("dataSource"));
         userDao = (UserDao) webApplicationContext.getBean("userDao");
         utilityDao = (UtilityDao) webApplicationContext.getBean("utilityDao");
 
         List<AdminUser> adminUsers = jdbcTemplate.query("SELECT * FROM tbl_adminusers", new AdminUserRowMapper());
         for (AdminUser adminUser : adminUsers) {
             try {
                 userDao.saveAdminUser(adminUser);
             } catch (Exception e) {
                LOGGER.error("Could not export radar admin user " + adminUser.getId());
             }
         }
 
         List<ProfessionalUser> professionalUsers = jdbcTemplate.query("SELECT * FROM tbl_users",
                 new ProfessionalUserRowMapper());
         for (ProfessionalUser professionalUser : professionalUsers) {
             try {
                 userDao.saveProfessionalUser(professionalUser);
             } catch (Exception e) {
                LOGGER.error("Could not export radar professional user " + professionalUser.getId());
             }
         }
 
         List<PatientUser> patientUsers = jdbcTemplate.query("SELECT * FROM tbl_patient_users",
                 new PatientUserRowMapper());
         for (PatientUser patientUser : patientUsers) {
             try {
                 userDao.savePatientUser(patientUser);
             } catch (Exception e) {
                LOGGER.error("Could not export radar patient user " + patientUser.getId());
             }
         }
     }
 
     private String decryptField(byte[] fieldData) throws Exception {
         byte[] copy = Arrays.copyOf(fieldData, fieldData.length);
         return TripleDes.decrypt(copy);
     }
 
     private class AdminUserRowMapper implements RowMapper<AdminUser> {
         public AdminUser mapRow(ResultSet resultSet, int i) throws SQLException {
             AdminUser adminUser = new AdminUser();
 
             adminUser.setId(resultSet.getLong("uID"));
             adminUser.setName(resultSet.getString("uName"));
             adminUser.setEmail(resultSet.getString("uEmail"));
 
             try {
                 adminUser.setPassword(User.getPasswordHash(decryptField(resultSet.getBytes("uPass"))));
                 adminUser.setUsername(decryptField(resultSet.getBytes("uUserName")));
             } catch (Exception e) {
                LOGGER.error("Could not decrypt user information for admin user ", adminUser.getId());
             }
 
             return adminUser;
         }
     }
 
     private class ProfessionalUserRowMapper implements RowMapper<ProfessionalUser> {
         public ProfessionalUser mapRow(ResultSet resultSet, int i) throws SQLException {
             ProfessionalUser professionalUser = new ProfessionalUser();
 
             // now map the specific props for this user in radar
             professionalUser.setId(resultSet.getLong("uID"));
             professionalUser.setSurname(resultSet.getString("uSurname"));
             professionalUser.setForename(resultSet.getString("uForename"));
             professionalUser.setTitle(resultSet.getString("uTitle"));
             professionalUser.setGmc(resultSet.getString("uGMC"));
             professionalUser.setRole(resultSet.getString("uRole"));
             professionalUser.setPhone(resultSet.getString("uPhone"));
             professionalUser.setEmail(resultSet.getString("uEmail"));
             professionalUser.setDateRegistered(resultSet.getDate("uDateJoin"));
 
             // Set the centre
             Long centreId = resultSet.getLong("uCentre");
             if (centreId != null && centreId > 0) {
                 professionalUser.setCentre(utilityDao.getCentre(centreId));
             }
 
             try {
                 professionalUser.setPassword(User.getPasswordHash(decryptField(resultSet.getBytes("uPass"))));
                 professionalUser.setUsername(decryptField(resultSet.getBytes("uUserName")));
             } catch (Exception e) {
                LOGGER.error("Could not decrypt user information for professional user ", professionalUser.getId());
             }
 
             return professionalUser;
         }
     }
 
     private class PatientUserRowMapper implements RowMapper<PatientUser> {
         public PatientUser mapRow(ResultSet resultSet, int i) throws SQLException {
             PatientUser patientUser = new PatientUser();
 
             patientUser.setId(resultSet.getLong("pID"));
             patientUser.setRadarNumber(resultSet.getLong("RADAR_NO"));
             patientUser.setDateOfBirth(resultSet.getDate("pDOB"));
             patientUser.setDateRegistered(resultSet.getDate("pDateReg"));
            patientUser.setUsername(resultSet.getString("pUserName"));
 
             try {
                 patientUser.setPassword(User.getPasswordHash(decryptField(resultSet.getBytes("pPassWord"))));
             } catch (Exception e) {
                LOGGER.error("Could not decrypt user information for patient user ", patientUser.getId());
             }
 
             patientUser.setEmail(patientUser.getUsername());
 
             return patientUser;
         }
     }
 }
