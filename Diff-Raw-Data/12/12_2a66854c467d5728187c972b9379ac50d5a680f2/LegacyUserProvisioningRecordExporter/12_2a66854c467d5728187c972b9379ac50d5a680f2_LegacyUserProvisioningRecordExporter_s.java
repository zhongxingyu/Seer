 package edu.northwestern.bioinformatics.studycalendar.web;
 
 import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
 import edu.northwestern.bioinformatics.studycalendar.dao.LegacyUserProvisioningRecordDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.LegacyUserProvisioningRecord;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.web.context.WebApplicationContext;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 import static java.lang.Boolean.valueOf;
 import static java.util.Arrays.asList;
 import static org.apache.commons.lang.StringUtils.isNotEmpty;
 import static org.springframework.web.context.support.WebApplicationContextUtils.getRequiredWebApplicationContext;
 
 public class LegacyUserProvisioningRecordExporter implements ServletContextListener {
     private final Logger log = LoggerFactory.getLogger(getClass());
 
     private ServletContext servletContext;
 
     private List<String> LEGACY_PROVISIONING_TABLES = asList("user_role_study_sites", "user_role_sites", "user_roles", "users");
 
     public void contextInitialized(ServletContextEvent sce) {
         servletContext = sce.getServletContext();
 
         if (!isExporterEnabled() || !isDataPresent()) { return; }
 
         String targetPath = exportRecords();
         wipeTables();
         log.info("Welcome to PSC 2.8.1+.  Your old user provisioning data has been exported to {}.",
             targetPath);
     }
 
     private void wipeTables() {
         for (String table : LEGACY_PROVISIONING_TABLES) {
             getJdbcTemplate().execute("DELETE FROM " + table);
         }
     }
 
     private String exportRecords() {
        String catalinaHome = System.getProperty("catalina.home");
 
         if (isNotEmpty(catalinaHome)) {
             File logDir = new File(catalinaHome + File.separator + "logs");
 
             //noinspection ResultOfMethodCallIgnored
             logDir.mkdirs();
 
             if (logDir.exists()) {
                 LegacyUserProvisioningRecordFile f =
                     new LegacyUserProvisioningRecordFile(logDir.getPath());
                 f.write(LegacyUserProvisioningRecord.CSV_HEADER);
                 for (LegacyUserProvisioningRecord r : getDao().getAll()) {
                     f.write(r.csv());
                 }
                 f.close();
 
                 return f.getPath();
             } else {
                 String msg = "Problem creating the log directory: " + logDir.getPath();
                 log.debug(msg);
                 throw new StudyCalendarSystemException(msg);
             }
         } else {
            String msg = "Problem exporting the legacy user provisioning data. $CATALINA_HOME is not set.";
             log.debug(msg);
             throw new StudyCalendarSystemException(msg);
         }
     }
 
     public void contextDestroyed(ServletContextEvent servletContextEvent) {}
 
     protected WebApplicationContext getWebApplicationContext() {
         return getRequiredWebApplicationContext(servletContext);
     }
 
     protected JdbcTemplate getJdbcTemplate() {
         return (JdbcTemplate) getWebApplicationContext().getBean("jdbcTemplate");
     }
 
     protected LegacyUserProvisioningRecordDao getDao() {
         return new LegacyUserProvisioningRecordDao(getJdbcTemplate());
     }
 
     protected boolean isExporterEnabled() {
         return valueOf(servletContext.getInitParameter("legacyUserProvisioningExporterSwitch"));
     }
 
     protected boolean isDataPresent() {
         for (String table : LEGACY_PROVISIONING_TABLES) {
             if (hasData(table)) return true;
         }
         return false;
     }
 
     private boolean hasData(String table) {
         return getJdbcTemplate().queryForInt("SELECT COUNT(*) FROM " + table) > 0;
     }
 
     class LegacyUserProvisioningRecordFile {
         private File file;
         private String directoryPath;
         private BufferedWriter writer;
 
         LegacyUserProvisioningRecordFile(String directoryPath) {
             this.directoryPath = directoryPath;
         }
 
         private String getFileName() {
             String timestamp = (new SimpleDateFormat("yyyyMMddHHmmss")).format(new Date());
             return "studycalendar-pre28-users-" + timestamp + ".csv";
         }
 
         protected String getPath() {
             return directoryPath + File.separator + getFileName();
         }
 
         public void write(String line) {
             if (open()) {
                 try {
                     writer.write(line);
                     writer.newLine();
                 } catch (IOException e) {
                     String msg = "Problem writing out the legacy user provisioning information: " + file.getPath();
                     log.debug(msg, e);
                     throw new StudyCalendarSystemException(msg, e);
                 }
             }
         }
 
         protected boolean open() {
             if (file == null || writer == null) {
                 file = new File(getPath());
 
                 try {
                     if (file.createNewFile()) {
                         log.debug("Legacy provisioning info file {} already exists; will overwrite.",
                             file.getAbsolutePath());
                     } else {
                         log.debug("Successfully created new legacy provisioning info file {}.",
                             file.getAbsolutePath());
                     }
                 } catch (IOException e) {
                     String msg = "Problem creating the legacy user provisioning information file: " + file.getPath();
                     log.debug(msg, e);
                     throw new StudyCalendarSystemException(msg, e);
                 }
 
                 try {
                     writer = new BufferedWriter(new FileWriter(file));
                 } catch (IOException e) {
                     String msg = "Problem opening the legacy user provisioning information file: " + file.getPath();
                     log.debug(msg, e);
                     throw new StudyCalendarSystemException(msg, e);
                 }
             }
 
             return true;
         }
 
         public void close() {
             if (writer != null) {
                 try {
                     writer.close();
                 } catch (IOException e) {
                     String msg = "Problem closing the legacy user provisioning information file: " + file.getPath();
                     log.debug(msg, e);
                     throw new StudyCalendarSystemException(msg, e);
                 }
             }
         }
     }
 }
