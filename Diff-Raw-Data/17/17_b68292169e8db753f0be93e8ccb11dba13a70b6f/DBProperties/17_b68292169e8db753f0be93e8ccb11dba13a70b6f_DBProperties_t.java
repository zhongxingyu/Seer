 package com.grooveshark.util.db;
 
 import com.grooveshark.util.FileUtils;
 import com.google.gson.JsonElement;
 
 import org.apache.log4j.Logger;
 
 public class DBProperties
 {
     public static String DEFAULT_MYSQL_URL = "jdbc:mysql://localhost:3306/mysql";
     public static String DEFAULT_MYSQL_USER = "root";
     public static String DEFAULT_MYSQL_PASS = "pass";
     public static String DEFAULT_MONGO_HOST = "localhost";
     public static int DEFAULT_MONGO_PORT = 2718;
     public static String DEFAULT_MONGO_DB = "test";
 
     public static String DEFAULT_MYSQL_DRIVER = "com.mysql.jdbc.Driver";
     public static String DEFAULT_HIVE_DRIVER = "org.apache.hadoop.hive.jdbc.HiveDriver";
 
     public static String DEFAULTS_DSN_FILENAME = "dsn/sample.dsn.json";
 
 
     protected String mysqlURL = "";
     protected String mysqlUser = "";
     protected String mysqlPass = "";
     protected String mysqlDriver = "";
 
     protected String mysqlHost = "";
     protected String mysqlDB = "";
     protected String mysqlOptions = "";
 
     protected String mongoHost = "";
     protected String mongoDB = "";
     protected int mongoPort = 2718;
 
     protected String hiveUrl = "";
     protected String hiveTestQuery = "";
 
 
     protected String hiveHost = "";
     protected String hiveDB = "";
     protected int hivePort = 10000;
 
     public static final Logger log = Logger.getLogger(DBProperties.class);
 
     public DBProperties() {
         log.debug("Loading defaults");
         try {
             this.loadDefaults();
         } catch (Exception e) {
             e.printStackTrace();
         }
 
     }
 
     public void loadDefaults() throws Exception {
         String[] defaults = { "defaults" };
        JsonElement je = this.getJsonElement(defaults);
         this.mongoHost = this.getJsonMongoHost(je);
         this.mongoPort = this.getJsonMongoPort(je);
         this.mongoDB = this.getJsonMongoDB(je);
         this.mysqlURL = this.getJsonMysqlUrl(je);
         this.mysqlDriver = this.getJsonMysqlDriver(je);
         this.hiveUrl = this.getJsonHiveUrl(je);
         this.hiveTestQuery = this.getJsonHiveTestQuery(je);
         log.debug("DEFAULT_MONGO_HOST=" + this.DEFAULT_MONGO_HOST);
         log.debug("DEFAULT_MONGO_PORT=" + this.DEFAULT_MONGO_PORT);
         log.debug("DEFAULT_MONGO_DB=" + this.DEFAULT_MONGO_DB);
         log.debug("DEFAULT_MYSQL_URL=" + this.DEFAULT_MYSQL_URL);
         log.debug("DEFAULT_MYSQL_DRIVER=" + this.DEFAULT_MYSQL_DRIVER);
         log.debug("HIVE_URL=" + this.hiveUrl);
         log.debug("HIVE_TEST_QUERY=" + this.hiveTestQuery);
     }
 
     public void setMysqlDsn(String[] jsonHeaders) throws Exception {
         JsonElement mysqlJe = this.getJsonElement(jsonHeaders);
         this.mysqlURL = this.getJsonMysqlUrl(mysqlJe);
         this.mysqlUser = this.getJsonMysqlUser(mysqlJe);
         this.mysqlPass = this.getJsonMysqlPassword(mysqlJe);
     }
     public JsonElement getJsonElement(String[] jsonHeaders) throws Exception {
         return FileUtils.parseJson(this.DEFAULTS_DSN_FILENAME, jsonHeaders);
     }
 
     public String getJsonHiveTestQuery(JsonElement je) throws Exception {
         String hive_test_query = FileUtils.getJsonValue(je, "hive_test_query");
         return hive_test_query.equals("") ? this.hiveTestQuery : hive_test_query;
     }
 
     public String getJsonHiveUrl(JsonElement je) throws Exception {
         this.hiveHost = this.getJsonHiveHost(je);
         this.hiveDB = this.getJsonHiveDB(je);
         this.hivePort = this.getJsonHivePort(je);
         if (this.hiveHost.equals("") && this.hiveDB.equals("") && this.hivePort == 0) {
             return "jdbc:hive://";
         }
         return "jdbc:hive://" + this.hiveHost + ":" + this.hivePort + "/" + this.hiveDB;
     }
 
     public String getJsonHiveHost(JsonElement je) throws Exception {
         String hive_host = FileUtils.getJsonValue(je, "hive_host");
         return hive_host.equals("") ? this.hiveHost : hive_host;
     }
 
     public String getJsonHiveDB(JsonElement je) throws Exception {
         String hive_db = FileUtils.getJsonValue(je, "hive_db");
         return hive_db.equals("") ? this.hiveDB : hive_db;
     }
 
     public int getJsonHivePort(JsonElement je) throws Exception {
         String hive_port = FileUtils.getJsonValue(je, "hive_port");
         return hive_port.equals("") ? this.hivePort : Integer.parseInt(hive_port);
     }
 
     public String getJsonMongoHost(JsonElement je) throws Exception {
         String mongo_host = FileUtils.getJsonValue(je, "mongo_host");
         return mongo_host.equals("") ? this.mongoHost : mongo_host;
     }
 
     public int getJsonMongoPort(JsonElement je) throws Exception {
         String mongo_port = FileUtils.getJsonValue(je, "mongo_port");
         return mongo_port.equals("") ? this.mongoPort : Integer.parseInt(mongo_port);
     }
 
     public String getJsonMongoDB(JsonElement je) throws Exception {
         String mongo_db = FileUtils.getJsonValue(je, "mongo_db");
         return mongo_db.equals("") ? this.mongoDB : mongo_db;
     }
 
     public String getJsonMysqlDriver(JsonElement je) throws Exception {
         String mysql_driver = FileUtils.getJsonValue(je, "mysql_driver");
         return mysql_driver.equals("") ? this.DEFAULT_MYSQL_DRIVER : mysql_driver;
     }
 
     public String getJsonMysqlUrl(JsonElement je) throws Exception {
         this.mysqlHost = this.getJsonMysqlHost(je);
         this.mysqlDB = this.getJsonMysqlDB(je);
         this.mysqlOptions = this.getJsonMysqlOptions(je);
         if (this.mysqlHost.equals("") && this.mysqlDB.equals("")) {
             return DBProperties.DEFAULT_MYSQL_URL;
         }
         return "jdbc:mysql://" + this.mysqlHost + ":3306/" + this.mysqlDB + this.mysqlOptions;
     }
 
     public String getJsonMysqlPassword(JsonElement je) throws Exception {
         String mysql_password = FileUtils.getJsonValue(je, "mysql_password");
         if (mysql_password.equals("")) {
             String type = FileUtils.getJsonValue(je, "type");
             if (type.equals("mysql")) {
                mysql_password = FileUtils.getJsonValue(je, "password");
             }
         }
         return mysql_password.equals("") ? this.mysqlPass : mysql_password;
     }
 
 
     public String getJsonMysqlUser(JsonElement je) throws Exception {
         String mysql_user = FileUtils.getJsonValue(je, "mysql_user");
         if (mysql_user.equals("")) {
             String type = FileUtils.getJsonValue(je, "type");
             if (type.equals("mysql")) {
                mysql_user = FileUtils.getJsonValue(je, "user");
             }
         }
         return mysql_user.equals("") ? this.mysqlUser : mysql_user;
     }
 
     public String getJsonMysqlOptions(JsonElement je) throws Exception {
         String mysql_options = FileUtils.getJsonValue(je, "mysql_options");
         if (mysql_options.equals("")) {
             String type = FileUtils.getJsonValue(je, "type");
             if (type.equals("mysql")) {
                mysql_options = FileUtils.getJsonValue(je, "options");
             }
         }
         return mysql_options.equals("") ? this.mysqlOptions : mysql_options;
     }
 
     public String getJsonMysqlHost(JsonElement je) throws Exception {
         String mysql_host = FileUtils.getJsonValue(je, "mysql_host");
         if (mysql_host.equals("")) {
             String type = FileUtils.getJsonValue(je, "type");
             if (type.equals("mysql")) {
                mysql_host = FileUtils.getJsonValue(je, "host");
             }
         }
         return mysql_host.equals("") ? this.mysqlHost : mysql_host;
     }
 
     public String getJsonMysqlDB(JsonElement je) throws Exception {
         String mysql_db = FileUtils.getJsonValue(je, "mysql_db");
         if (mysql_db.equals("")) {
             String type = FileUtils.getJsonValue(je, "type");
             if (type.equals("mysql")) {
                mysql_db = FileUtils.getJsonValue(je, "db");
             }
         }
         return mysql_db.equals("") ? this.mysqlDB : mysql_db;
     }
 
     public void setDefaultsDsnFilename(String filename) throws DBPropertiesException {
         if (!FileUtils.isFileExists(filename)) {
             throw new DBPropertiesException("File: '" + filename + "' does not exist");
         }
         this.DEFAULTS_DSN_FILENAME = filename;
     }
 
     /**
      * Setter method for mysqlURL
      */
     public void setMysqlURL(String mysqlURL) {
         this.mysqlURL = mysqlURL;
     }
     /**
      * Getter method for mysqlURL
      */
     public String getMysqlURL() {
         return this.mysqlURL;
     }
     /**
      * Setter method for mysqlUser
      */
     public void setMysqlUser(String mysqlUser) {
         this.mysqlUser = mysqlUser;
     }
     /**
      * Getter method for mysqlUser
      */
     public String getMysqlUser() {
         return this.mysqlUser;
     }
     /**
      * Setter method for mysqlPass
      */
     public void setMysqlPass(String mysqlPass) {
         this.mysqlPass = mysqlPass;
     }
     /**
      * Getter method for mysqlPass
      */
     public String getMysqlPass() {
         return this.mysqlPass;
     }
     /**
      * Setter method for mysqlDriver
      */
     public void setMysqlDriver(String mysqlDriver) {
         this.mysqlDriver = mysqlDriver;
     }
     /**
      * Getter method for mysqlDriver
      */
     public String getMysqlDriver() {
         return this.mysqlDriver;
     }
     /**
      * Setter method for mysqlHost
      */
     public void setMysqlHost(String mysqlHost) {
         this.mysqlHost = mysqlHost;
     }
     /**
      * Getter method for mysqlHost
      */
     public String getMysqlHost() {
         return this.mysqlHost;
     }
     /**
      * Setter method for mysqlDB
      */
     public void setMysqlDB(String mysqlDB) {
         this.mysqlDB = mysqlDB;
     }
     /**
      * Getter method for mysqlDB
      */
     public String getMysqlDB() {
         return this.mysqlDB;
     }
     /**
      * Setter method for mysqlOptions
      */
     public void setMysqlOptions(String mysqlOptions) {
         this.mysqlOptions = mysqlOptions;
     }
     /**
      * Getter method for mysqlOptions
      */
     public String getMysqlOptions() {
         return this.mysqlOptions;
     }
     /**
      * Setter method for mongoHost
      */
     public void setMongoHost(String mongoHost) {
         this.mongoHost = mongoHost;
     }
     /**
      * Getter method for mongoHost
      */
     public String getMongoHost() {
         return this.mongoHost;
     }
     /**
      * Setter method for mongoPort
      */
     public void setMongoPort(int mongoPort) {
         this.mongoPort = mongoPort;
     }
     /**
      * Getter method for mongoPort
      */
     public int getMongoPort() {
         return this.mongoPort;
     }
     /**
      * Setter method for mongoDB
      */
     public void setMongoDB(String mongoDB) {
         this.mongoDB = mongoDB;
     }
     /**
      * Getter method for mongoDB
      */
     public String getMongoDB() {
         return this.mongoDB;
     }
     /**
      * Setter method for hiveHost
      */
     public void setHiveHost(String hiveHost) {
         this.hiveHost = hiveHost;
     }
     /**
      * Getter method for hiveHost
      */
     public String getHiveHost() {
         return this.hiveHost;
     }
     /**
      * Setter method for hiveDB
      */
     public void setHiveDB(String hiveDB) {
         this.hiveDB = hiveDB;
     }
     /**
      * Getter method for hiveDB
      */
     public String getHiveDB() {
         return this.hiveDB;
     }
     /**
      * Setter method for hivePort
      */
     public void setHivePort(int hivePort) {
         this.hivePort = hivePort;
     }
     /**
      * Getter method for hivePort
      */
     public int getHivePort() {
         return this.hivePort;
     }
     /**
      * Setter method for hiveUrl
      */
     public void setHiveUrl(String hiveUrl) {
         this.hiveUrl = hiveUrl;
     }
     /**
      * Getter method for hiveUrl
      */
     public String getHiveUrl() {
         return this.hiveUrl;
     }
     /**
      * Setter method for hiveTestQuery
      */
     public void setHiveTestQuery(String hiveTestQuery) {
         this.hiveTestQuery = hiveTestQuery;
     }
     /**
      * Getter method for hiveTestQuery
      */
     public String getHiveTestQuery() {
         return this.hiveTestQuery;
     }
 }
