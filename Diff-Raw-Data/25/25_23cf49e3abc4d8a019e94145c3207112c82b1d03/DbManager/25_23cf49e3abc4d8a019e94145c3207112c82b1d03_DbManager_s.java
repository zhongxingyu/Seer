 package org.hackystat.sensorbase.db;
 
 import java.lang.reflect.Constructor;
 import java.util.List;
 import javax.xml.datatype.XMLGregorianCalendar;
 import org.hackystat.sensorbase.resource.projects.jaxb.Project;
 import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorData;
 import org.hackystat.sensorbase.resource.sensordatatypes.jaxb.SensorDataType;
 import org.hackystat.sensorbase.resource.users.jaxb.User;
 import org.hackystat.sensorbase.server.Server;
 import org.hackystat.sensorbase.server.ServerProperties;
 import static org.hackystat.sensorbase.server.ServerProperties.DB_IMPL_KEY;
 import org.hackystat.sensorbase.uripattern.UriPattern;
 import org.hackystat.utilities.stacktrace.StackTrace;
 
 /**
  * Provides an interface to storage for the resources managed by the SensorBase.
  * Currently we have one storage mechanisms: a persistent store which is implemented by
  * an embedded Derby database.
  * In future, we can enhance this DbManager to enable "swappable" persistent stores and other
  * alternatives to storage. 
  * @author Philip Johnson
  */
 public class DbManager {
   
   /** The chosen Storage system. */
   private DbImplementation dbImpl;
 
   /** The SensorDataIndex open tag. */
   public static final String sensorDataIndexOpenTag = "<SensorDataIndex>";
   
   /** The SensorDataIndex close tag. */
   public static final String sensorDataIndexCloseTag = "</SensorDataIndex>";
 
   /**
    * Creates a new DbManager which manages access to the underlying persistency layer(s).
    * Instantiates the underlying storage system to use. 
    * @param server The Restlet server instance. 
    */
   public DbManager(Server server) {
     //Defaults to: "org.hackystat.sensorbase.db.derby.DerbyImplementation"
     String dbClassName = ServerProperties.get(DB_IMPL_KEY); 
     Class<?> dbClass = null;
     //First, try to find the class specified in the sensorbase.properties file (or the default) 
     try {
       dbClass = Class.forName(dbClassName);
     }
     catch (ClassNotFoundException e) {
       String msg = "DB error instantiating " + dbClassName + ". Could not find this class.";
       server.getLogger().warning(msg + "\n" + StackTrace.toString(e));
       throw new IllegalArgumentException(e);
     }
     // Next, try to find a constructor that accepts a Server as its parameter. 
     Class[] constructorParam = {org.hackystat.sensorbase.server.Server.class};
     Constructor dbConstructor = null;
     try {
       dbConstructor = dbClass.getConstructor(constructorParam);
     }
     catch (Exception e) {
       String msg = "DB error instantiating " + dbClassName + ". Could not find Constructor(server)";
       server.getLogger().warning(msg + "\n" + StackTrace.toString(e));
       throw new IllegalArgumentException(e);
     }
     // Next, try to create an instance of DbImplementation from the Constructor.
     Object[] serverArg = {server};
     try {
       this.dbImpl = (DbImplementation) dbConstructor.newInstance(serverArg);
     }
     catch (Exception e) {
       String msg = "DB error instantiating " + dbClassName + ". Could not create instance.";
       server.getLogger().warning(msg + "\n" + StackTrace.toString(e));
       throw new IllegalArgumentException(e);
     }
     //this.dbImpl = new DerbyImplementation(this.server);
     this.dbImpl.initialize();
   }
   
   /**
   * Returns true if this Db was freshly initialized upon instantiation of the DbManager.
   * This indicates the need for the various Resource Managers to read in the default data
   * to this database.  
   * We only care about the persistent store. We know that the InMemoryImpl will always be
   * "fresh" each time we come up.
   * @return True if this DB was freshly created or not.
   */
  public boolean isFreshDb() {
    return true;  
  }

  /**
    * Persists a SensorData instance.  If the Owner/Timestamp already exists in the table, it is
    * overwritten.
    * @param data The sensor data. 
    * @param xmlSensorData The sensor data resource as an XML String.  
    * @param xmlSensorDataRef The sensor data resource as an XML resource reference
    */
   public void storeSensorData(SensorData data, String xmlSensorData, String xmlSensorDataRef) {
     this.dbImpl.storeSensorData(data, xmlSensorData, xmlSensorDataRef);
   }
   
   /**
    * Persists a SensorDataType instance.  If the SDT name already exists in the table, it is
    * overwritten.
    * @param sdt The sensor data. 
    * @param xmlSensorDataType The SDT resource as an XML String.  
    * @param xmlSensorDataTypeRef The SDT as an XML resource reference
    */
   public void storeSensorDataType(SensorDataType sdt, String xmlSensorDataType, 
       String xmlSensorDataTypeRef) {
     this.dbImpl.storeSensorDataType(sdt, xmlSensorDataType, xmlSensorDataTypeRef);
   }
   
   /**
    * Persists a User instance.  If the User email already exists in the table, it is
    * overwritten.
    * @param user The user instance.
    * @param xmlUser The User resource as an XML String.  
    * @param xmlUserRef The User as an XML resource reference
    */
   public void storeUser(User user, String xmlUser, String xmlUserRef) {
     this.dbImpl.storeUser(user, xmlUser, xmlUserRef);
   }
   
   /**
    * Persists a Project instance.  If the Project already exists in the db, it is
    * overwritten.
    * @param project The project instance.
    * @param xmlProject The Project resource as an XML String.  
    * @param xmlProjectRef The Project as an XML resource reference
    */
   public void storeProject(Project project, String xmlProject, String xmlProjectRef) {
     this.dbImpl.storeProject(project, xmlProject, xmlProjectRef);
   }
   
   /**
    * Returns the XML SensorDataIndex for all sensor data.
    * @return An XML String providing an index of all sensor data resources.
    */
   public String getSensorDataIndex() {
     return this.dbImpl.getSensorDataIndex();
   }
   
  
   /**
    * Returns the XML SensorDataIndex for all sensor data for this user. 
    * @param user The User whose sensor data is to be returned. 
    * @return The XML String providing an index of all relevent sensor data resources.
    */
   public String getSensorDataIndex(User user) {
     return this.dbImpl.getSensorDataIndex(user);
   }
   
   /**
    * Returns the XML SensorDataIndex for all sensor data for this user and sensor data type.
    * @param user The User whose sensor data is to be returned. 
    * @param sdtName The sensor data type name.
    * @return The XML Document instance providing an index of all relevent sensor data resources.
    */
   public String getSensorDataIndex(User user, String sdtName) {
     return this.dbImpl.getSensorDataIndex(user, sdtName);
   }
   
   /**
    * Returns the XML SensorDataIndex for all sensor data matching this user, start/end time, and 
    * whose resource string matches at least one in the list of UriPatterns. 
    * @param user The user. 
    * @param startTime The start time. 
    * @param endTime The end time. 
    * @param uriPatterns A list of UriPatterns. 
    * @return The XML SensorDataIndex string corresponding to the matching sensor data. 
    */
   public String getSensorDataIndex(User user, XMLGregorianCalendar startTime, 
       XMLGregorianCalendar endTime, List<UriPattern> uriPatterns) {
     return this.dbImpl.getSensorDataIndex(user, startTime, endTime, uriPatterns);
   }
   
   /**
    * Returns the XML SensorDataTypeIndex for all sensor data.
    * @return An XML String providing an index of all SDT resources.
    */
   public String getSensorDataTypeIndex() {
     return this.dbImpl.getSensorDataTypeIndex();
   }
   
   /**
    * Returns the XML UserIndex for all Users..
    * @return An XML String providing an index of all User resources.
    */
   public String getUserIndex() {
     return this.dbImpl.getUserIndex();
   }
   
   /**
    * Returns the XML Project Index for all Projects.
    * @return An XML String providing an index of all Project resources.
    */
   public String getProjectIndex() {
     return this.dbImpl.getProjectIndex();
   }
   
   
   /**
    * Returns the SensorData instance as an XML string, or null.
    * @param user The user.
    * @param timestamp The timestamp associated with this sensor data.
    * @return The SensorData instance as an XML string, or null.
    */
   public String getSensorData(User user, XMLGregorianCalendar timestamp) {
     return this.dbImpl.getSensorData(user, timestamp);
   }
   
   /**
    * Returns the SensorDataType instance as an XML string, or null.
    * @param sdtName The name of the SDT to retrieve.
    * @return The SensorDataType instance as an XML string, or null.
    */
   public String getSensorDataType(String sdtName) {
     return this.dbImpl.getSensorDataType(sdtName);
   }
   
   /**
    * Returns the User instance as an XML string, or null.
    * @param email The email address of the User to retrieve.
    * @return The User instance as an XML string, or null.
    */
   public String getUser(String email) {
     return this.dbImpl.getUser(email);
   }
   
   /**
    * Returns the Project instance as an XML string, or null.
    * @param user The User that owns the Project to retrieve.
    * @param projectName The name of the Project to retrieve.
    * @return The Project instance as an XML string, or null.
    */
   public String getProject(User user, String projectName) {
     return this.dbImpl.getProject(user, projectName);
   }
   
   /**
    * Returns true if the passed [user, timestamp] has sensor data defined for it.
    * @param user The user.
    * @param timestamp The timestamp
    * @return True if there is any sensor data for this [user, timestamp].
    */
   public boolean hasSensorData(User user, XMLGregorianCalendar timestamp) {
     return this.dbImpl.hasSensorData(user, timestamp);
   }  
   
   
   /**
    * Ensures that sensor data with the given user and timestamp no longer exists.
    * @param user The user.
    * @param timestamp The timestamp associated with this sensor data.
    */
   public void deleteSensorData(User user, XMLGregorianCalendar timestamp) {
     this.dbImpl.deleteSensorData(user, timestamp);
   }
   
   /**
    * Ensures that the SensorDataType with the given name no longer exists.
    * @param sdtName The SDT name.
    */
   public void deleteSensorDataType(String sdtName) {
     this.dbImpl.deleteSensorDataType(sdtName);
   }
   
   /**
    * Ensures that the User with the given email address is no longer present in this db.
    * @param email The user email.
    */
   public void deleteUser(String email) {
     this.dbImpl.deleteUser(email);
   }
   
   /**
    * Ensures that the Project with the given user and name is no longer present in this db.
    * @param user  The User who owns this Project.
    * @param projectName The name of the Project to delete.
    */
   public void deleteProject(User user, String projectName) {
     this.dbImpl.deleteProject(user, projectName);
   }
   
 }
