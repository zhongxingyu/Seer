 package org.chat.android;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.util.Log;
 
 import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
 import com.j256.ormlite.dao.Dao;
 import com.j256.ormlite.support.ConnectionSource;
 import com.j256.ormlite.table.TableUtils;
 
 import java.sql.SQLException;
 
 import org.chat.android.models.Attendance;
 import org.chat.android.models.Client;
 import org.chat.android.models.HealthPage;
 import org.chat.android.models.HealthSelect;
 import org.chat.android.models.HealthSelectRecorded;
 import org.chat.android.models.HealthTheme;
 import org.chat.android.models.HealthTopic;
 import org.chat.android.models.HealthTopicAccessed;
 import org.chat.android.models.Household;
 import org.chat.android.models.PageSelect1;
 import org.chat.android.models.PageText1;
 import org.chat.android.models.PageVideo1;
 import org.chat.android.models.Resource;
 import org.chat.android.models.ResourceAccessed;
 import org.chat.android.models.Role;
 import org.chat.android.models.Service;
 import org.chat.android.models.ServiceAccessed;
 import org.chat.android.models.TopicVideo;
 import org.chat.android.models.Video;
 import org.chat.android.models.VideoAccessed;
 import org.chat.android.models.Visit;
 import org.chat.android.models.Worker;
 
 /**
  * Created by Armin Krauss on 2013-06-12.
  */
 public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
 
     private static final String DATABASE_NAME = "chat.db";
    private static final int DATABASE_VERSION = 74;
     private Dao<Visit, Integer> visitsDao = null;
     private Dao<Client, Integer> clientsDao = null;
     private Dao<Household, Integer> householdsDao = null;
     private Dao<Attendance, Integer> attendanceDao = null;
     private Dao<Role, Integer> rolesDao = null;
     private Dao<Service, Integer> servicesDao = null;
     private Dao<ServiceAccessed, Integer> serviceAccessedDao = null;
     private Dao<Worker, Integer> workersDao = null;
     private Dao<Video, Integer> videosDao = null;
     private Dao<VideoAccessed, Integer> videoAccessedDao = null;
     private Dao<Resource, Integer> resourcesDao = null;
     private Dao<ResourceAccessed, Integer> resourceAccessedDao = null;
     
     private Dao<HealthTheme, Integer> healthThemeDao = null;
     private Dao<HealthSelect, Integer> healthSelectDao = null;
     private Dao<HealthSelectRecorded, Integer> healthSelectRecordedDao = null;
     
     private Dao<HealthTopic, Integer> healthTopicsDao = null;
     private Dao<HealthTopicAccessed, Integer> healthTopicAccessedDao = null;
     private Dao<HealthPage, Integer> healthPagesDao = null;
     private Dao<PageText1, Integer> pageText1Dao = null;
     private Dao<PageSelect1, Integer> pageSelect1Dao = null;
     private Dao<PageVideo1, Integer> pageVideo1Dao = null;
     private Dao<TopicVideo, Integer> topicVideoDao = null;
     
 
     public DatabaseHelper(Context context) {
         super(context, DATABASE_NAME, null, DATABASE_VERSION);
     }
 
 //    public DatabaseHelper(Context context, String databaseName, SQLiteDatabase.CursorFactory factory, int databaseVersion) {
 //        super(context, databaseName, factory, databaseVersion);
 //    }
 
     @Override
     public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource) {
         try {
             TableUtils.createTable(connectionSource, Visit.class);
             TableUtils.createTable(connectionSource, Client.class);
             TableUtils.createTable(connectionSource, Household.class);
             TableUtils.createTable(connectionSource, Attendance.class);
             TableUtils.createTable(connectionSource, Role.class);
             TableUtils.createTable(connectionSource, Service.class);
             TableUtils.createTable(connectionSource, ServiceAccessed.class);
             TableUtils.createTable(connectionSource, Worker.class);
             TableUtils.createTable(connectionSource, Video.class);
             TableUtils.createTable(connectionSource, VideoAccessed.class);
             TableUtils.createTable(connectionSource, Resource.class);
             TableUtils.createTable(connectionSource, ResourceAccessed.class);
             TableUtils.createTable(connectionSource, HealthTheme.class);
             TableUtils.createTable(connectionSource, HealthSelect.class);
             TableUtils.createTable(connectionSource, HealthSelectRecorded.class);
             TableUtils.createTable(connectionSource, HealthTopic.class);
             TableUtils.createTable(connectionSource, HealthTopicAccessed.class);
             TableUtils.createTable(connectionSource, HealthPage.class);
             TableUtils.createTable(connectionSource, PageText1.class);
             TableUtils.createTable(connectionSource, PageSelect1.class);
             TableUtils.createTable(connectionSource, PageVideo1.class);
             TableUtils.createTable(connectionSource, TopicVideo.class);
         } catch (SQLException e) {
             Log.e(DatabaseHelper.class.getName(), "Unable to create databases", e);
         }
     }
 
     @Override
     public void onUpgrade(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource, int oldVer, int newVer) {
         try {
             TableUtils.dropTable(connectionSource, Visit.class, true);
             TableUtils.dropTable(connectionSource, Client.class, true);
             TableUtils.dropTable(connectionSource, Household.class, true);
             TableUtils.dropTable(connectionSource, Attendance.class, true);
             TableUtils.dropTable(connectionSource, Role.class, true);
             TableUtils.dropTable(connectionSource, Service.class, true);
             TableUtils.dropTable(connectionSource, ServiceAccessed.class, true);
             TableUtils.dropTable(connectionSource, Worker.class, true);
             TableUtils.dropTable(connectionSource, Video.class, true);
             TableUtils.dropTable(connectionSource, VideoAccessed.class, true);
             TableUtils.dropTable(connectionSource, Resource.class, true);
             TableUtils.dropTable(connectionSource, ResourceAccessed.class, true);
             TableUtils.dropTable(connectionSource, HealthTheme.class, true);
             TableUtils.dropTable(connectionSource, HealthSelect.class, true);
             TableUtils.dropTable(connectionSource, HealthSelectRecorded.class, true);
             TableUtils.dropTable(connectionSource, HealthTopic.class, true);
             TableUtils.dropTable(connectionSource, HealthTopicAccessed.class, true);
             TableUtils.dropTable(connectionSource, HealthPage.class, true);
             TableUtils.dropTable(connectionSource, PageText1.class, true);
             TableUtils.dropTable(connectionSource, PageSelect1.class, true);
             TableUtils.dropTable(connectionSource, PageVideo1.class, true);
             TableUtils.dropTable(connectionSource, TopicVideo.class, true);
             onCreate(sqLiteDatabase, connectionSource);
         } catch (SQLException e) {
             Log.e(DatabaseHelper.class.getName(), "Unable to upgrade database from version " + oldVer + " to new " + newVer, e);
         }    	 
     }
 
     /**
      * Functions that returns the Data Access Object DAO00
      */
     public Dao<Visit, Integer> getVisitsDao() throws SQLException {
         if (visitsDao == null) {
         	visitsDao = getDao(Visit.class);
         }
         return visitsDao;
     }
     public Dao<Client, Integer> getClientsDao() throws SQLException {
         if (clientsDao == null) {
         	clientsDao = getDao(Client.class);
         }
         return clientsDao;
     }
     public Dao<Household, Integer> getHouseholdsDao() throws SQLException {
         if (householdsDao == null) {
         	householdsDao = getDao(Household.class);
         }
         return householdsDao;
     }
     public Dao<Attendance, Integer> getAttendanceDao() throws SQLException {
         if (attendanceDao == null) {
             attendanceDao = getDao(Attendance.class);
         }
         return attendanceDao;
     }
     public Dao<Role, Integer> getRolesDao() throws SQLException {
         if (rolesDao == null) {
             rolesDao = getDao(Role.class);
         }
         return rolesDao;
     }
     public Dao<Service, Integer> getServicesDao() throws SQLException {
         if (servicesDao == null) {
         	servicesDao = getDao(Service.class);
         }
         return servicesDao;
     }
     public Dao<ServiceAccessed, Integer> getServiceAccessedDao() throws SQLException {
         if (serviceAccessedDao == null) {
         	serviceAccessedDao = getDao(ServiceAccessed.class);
         }
         return serviceAccessedDao;
     }
     public Dao<Worker, Integer> getWorkersDao() throws SQLException {
         if (workersDao == null) {
             workersDao = getDao(Worker.class);
         }
         return workersDao;
     }
     public Dao<Video, Integer> getVideosDao() throws SQLException {
         if (videosDao == null) {
         	videosDao = getDao(Video.class);
         }
         return videosDao;
     }
     public Dao<VideoAccessed, Integer> getVideosAccessedDao() throws SQLException {
         if (videoAccessedDao == null) {
         	videoAccessedDao = getDao(VideoAccessed.class);
         }
         return videoAccessedDao;
     }
     public Dao<Resource, Integer> getResourcesDao() throws SQLException {
         if (resourcesDao == null) {
         	resourcesDao = getDao(Resource.class);
         }
         return resourcesDao;
     }
     public Dao<ResourceAccessed, Integer> getResourceAccessedDao() throws SQLException {
         if (resourceAccessedDao == null) {
         	resourceAccessedDao = getDao(ResourceAccessed.class);
         }
         return resourceAccessedDao;
     }
     public Dao<HealthTheme, Integer> getHealthThemeDao() throws SQLException {
         if (healthThemeDao == null) {
         	healthThemeDao = getDao(HealthTheme.class);
         }
         return healthThemeDao;
     }
     public Dao<HealthSelect, Integer> getHealthSelectDao() throws SQLException {
         if (healthSelectDao == null) {
         	healthSelectDao = getDao(HealthSelect.class);
         }
         return healthSelectDao;
     }
     public Dao<HealthSelectRecorded, Integer> getHealthSelectRecordedDao() throws SQLException {
         if (healthSelectRecordedDao == null) {
         	healthSelectRecordedDao = getDao(HealthSelectRecorded.class);
         }
         return healthSelectRecordedDao;
     }
     public Dao<HealthTopic, Integer> getHealthTopicsDao() throws SQLException {
         if (healthTopicsDao == null) {
         	healthTopicsDao = getDao(HealthTopic.class);
         }
         return healthTopicsDao;
     }
     public Dao<HealthTopicAccessed, Integer> getHealthTopicsAccessed() throws SQLException {
         if (healthTopicAccessedDao == null) {
         	healthTopicAccessedDao = getDao(HealthTopicAccessed.class);
         }
         return healthTopicAccessedDao;
     }
     public Dao<HealthPage, Integer> getHealthPagesDao() throws SQLException {
         if (healthPagesDao == null) {
         	healthPagesDao = getDao(HealthPage.class);
         }
         return healthPagesDao;
     }
     public Dao<PageText1, Integer> getPageText1Dao() throws SQLException {
         if (pageText1Dao == null) {
         	pageText1Dao = getDao(PageText1.class);
         }
         return pageText1Dao;
     }
     public Dao<PageSelect1, Integer> getPageSelect1Dao() throws SQLException {
         if (pageSelect1Dao == null) {
         	pageSelect1Dao = getDao(PageSelect1.class);
         }
         return pageSelect1Dao;
     }
     public Dao<PageVideo1, Integer> getPageVideo1Dao() throws SQLException {
         if (pageVideo1Dao == null) {
         	pageVideo1Dao = getDao(PageVideo1.class);
         }
         return pageVideo1Dao;
     }
     public Dao<TopicVideo, Integer> getTopicVideosDao() throws SQLException {
         if (topicVideoDao == null) {
         	topicVideoDao = getDao(TopicVideo.class);
         }
         return topicVideoDao;
     }
 
     /**
      * Close the database connections and clear any cached DAOs.
      */
     @Override
     public void close() {
         super.close();
         clientsDao = null;
         visitsDao = null;
         householdsDao = null;
         attendanceDao = null;
         rolesDao = null;
         servicesDao = null;
         serviceAccessedDao = null;
         workersDao = null;
         videosDao = null;
         videoAccessedDao = null;
         resourcesDao = null;
         resourceAccessedDao = null;
         healthTopicsDao = null;
         healthTopicAccessedDao = null;
         healthPagesDao = null;
         pageText1Dao = null;
         pageSelect1Dao = null;
         pageVideo1Dao = null;
         topicVideoDao = null;
     }
 }
