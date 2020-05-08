 package net.argius.stew;
 
 import java.sql.*;
 import java.util.*;
 
 /**
  * This class provides functions to manage database connections in this application. 
  */
 public final class Connector {
 
     private static final Logger log = Logger.getLogger(Connector.class);
 
     private final String id;
     private final Properties props;
     private final Password password;
 
     private transient Driver driver;
 
     /**
      * A constructor.
      * @param id
      * @param props
      */
     public Connector(String id, Properties props) {
         assert id != null;
        if (!id.matches("[A-Za-z0-9]+")) { // XXX move to new public method isValid
            throw new IllegalArgumentException(ResourceManager.Default.get("e.id-can-only-contain-alphanum", id));
         }
         Properties p = new Properties();
         p.putAll(props);
         Password password = createPasswordInstance(props.getProperty("password.class"));
         password.setTransformedString(props.getProperty("password"));
         this.id = id;
         this.props = p;
         this.password = password;
     }
 
     private static Password createPasswordInstance(String className) {
         if (className != null) {
             try {
                 return (Password)DynamicLoader.newInstance(className);
             } catch (Exception ex) {
                 log.warn(ex);
             }
         }
         return new PlainTextPassword();
     }
 
     /**
      * A constructor (for copying).
      * @param id
      * @param src
      */
     public Connector(String id, Connector src) {
         this(id, (Properties)src.props.clone());
     }
 
     /**
      * Returns the ID.
      * @return
      */
     public String getId() {
         return id;
     }
 
     /**
      * Returns the name.
      * @return
      */
     public String getName() {
         return props.getProperty("name");
     }
 
     /**
      * Returns the classpath.
      * @return
      */
     public String getClasspath() {
         return props.getProperty("classpath", "");
     }
 
     /**
      * Returns the JDBC driver (class name).
      * @return
      */
     public String getDriver() {
         final String driver = props.getProperty("driver");
         log.debug("driver=[%s]", driver);
         return driver;
     }
 
     /**
      * Returns the URL.
      * @return
      */
     public String getUrl() {
         return props.getProperty("url");
     }
 
     /**
      * Returns the user.
      * @return
      */
     public String getUser() {
         return props.getProperty("user");
     }
 
     /**
      * Returns the Password object.
      * @return
      */
     public Password getPassword() {
         return password;
     }
 
     /**
      * Returns whether the connection is read-only or not.
      * @return
      */
     public boolean isReadOnly() {
         String s = props.getProperty("readonly");
         return Boolean.valueOf(s).booleanValue();
     }
 
     /**
      * Returns whether the connection uses auto-rollback or not.
      * @return
      */
     public boolean usesAutoRollback() {
         String s = props.getProperty("rollback");
         return Boolean.valueOf(s).booleanValue();
     }
 
     /**
      * Converts this to Properties.
      * @return
      */
     public Properties toProperties() {
         return (Properties)props.clone();
     }
 
     /**
      * Attempts to establish a connection.
      * @return
      * @throws SQLException
      */
     public Connection getConnection() throws SQLException {
         if (driver == null) {
             driver = ConnectorDriverManager.getDriver(getUrl(), getDriver(), getClasspath());
             if (driver == null) {
                 throw new SQLException("failed to load driver");
             }
             log.debug(driver);
         }
         Properties p = new Properties();
         p.setProperty("user", getUser());
         p.setProperty("password", getPassword().getRawString());
         if (!driver.acceptsURL(getUrl())) {
             throw new SQLException("invalid url: " + getUrl());
         }
         log.info("driver.connect start");
         Connection conn = driver.connect(getUrl(), p);
         log.info("driver.connect end");
         if (conn == null) {
             throw new IllegalStateException("driver returned null");
         }
         return conn;
     }
 
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = prime * result + ((props == null) ? 0 : props.hashCode());
         return result;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (this == obj) {
             return true;
         }
         if (!(obj instanceof Connector)) {
             return false;
         }
         Connector other = (Connector)obj;
         return props.equals(other.props);
     }
 
     @Override
     public String toString() {
         return "Connector:" + id;
     }
 
 }
