 package co.gridport.server;
 
 import java.io.File;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import co.gridport.server.domain.Contract;
 import co.gridport.server.domain.Endpoint;
 import co.gridport.server.domain.User;
 import co.gridport.server.utils.Utils;
 
 public class ConfigProviderSQLite implements ConfigProvider {
 
     private static Logger log = LoggerFactory.getLogger("server");
 
     private Connection policydb;
 
     private Map<String, String> settings;
 
     private Map<String,User> users;
 
     private Map<String, Contract> contracts;
 
     private Map<Integer,Endpoint> endpoints;
 
     @Override
     public Map<String, String> getSettings() {
         return Collections.unmodifiableMap(settings);
     }
 
     @Override
     public Boolean has(String settingsKey) {
         return settings.containsKey(settingsKey);
     }
 
     @Override
     public String put(String settingsKey, String settingsValue) {
         try {
             Statement s = policydb.createStatement();
             s.addBatch("DELETE FROM settings WHERE name='"+settingsKey+"'");
             s.addBatch("INSERT INTO settings(name,value) VALUES('"+settingsKey+"','"+settingsValue+"')");
             s.executeBatch();
             s.close();
             return settings.put(settingsKey, settingsValue);
         } catch (SQLException e) {
             e.printStackTrace();
             return null;
         }
     }
 
     @Override
     public String get(String settingsKey) {
         return settings.get(settingsKey);
     }
 
 
     @Override
     public Map<String,List<User>> getGroups() {
         Map<String,List<User>> map = new HashMap<String,List<User>>();
         for(User user: users.values()) {
             for(String group: user.getGroups()) {
                 List<User> groupUsers;
                 if (map.containsKey(group)) {
                     groupUsers = map.get(group);
                 } else {
                     groupUsers = new ArrayList<User>();
                 }
                 groupUsers.add(user);
                 map.put(group, groupUsers);
             }
         }
         return map;
     }
 
     @Override
     public Collection<User> getUsers() {
         return Collections.unmodifiableCollection(users.values());
     }
 
     @Override
     public User getUser(String username) {
         return users.get(username);
     }
 
     @Override
     public User updateUser(User user) {
         try {
             Statement s = policydb.createStatement();
             s.addBatch("REPLACE INTO users(groups,username,passport) VALUES(" +
                 "'"+StringUtils.join(user.getGroups(),",")+"'" +
                 ",'"+user.getUsername()+"'" +
                 ",'"+user.getPassport("")+"'" +
             ")");
             s.executeBatch();
             s.close();
             return users.put(user.getUsername(),user);
         } catch (SQLException e) {
             e.printStackTrace();
             return null;
         }
     }
 
     @Override
     public Collection<Contract> getContracts() {
         return Collections.unmodifiableCollection(contracts.values());
     }
     @Override
     public Contract getContract(String contractName) {
         return contracts.get(contractName);
     }
     @Override
     public Contract updateContract(Contract contract) {
         try {
             Statement s = policydb.createStatement();
             s.addBatch("UPDATE contracts SET " +
                 "content='"+StringUtils.join(contract.getEndpoints(),",")+"'" +
                 ", ip_range='"+StringUtils.join(contract.getIpFilters(),",")+"'" +
                 ", interval="+contract.getIntervalMs() +
                 ", frequency="+contract.getFrequency() +
                 ", auth_group='"+StringUtils.join(contract.getGroups(),",")+"'" +
             " WHERE name ='"+contract.getName()+"'");
             s.executeBatch();
             s.close();
             return contracts.put(contract.getName(), contract);
         } catch (SQLException e) {
             e.printStackTrace();
             return null;
         }
     }
 
     @Override
     public Endpoint newEndpoint() {
         return newEndpoint("");
     }
     @Override
     public Endpoint newEndpoint(String endpointAddress) {
         try {
             Statement s = policydb.createStatement();
             s.executeUpdate("INSERT INTO endpoints(uri_base) VALUES('')");
             s.close();
             ResultSet rs = s.executeQuery("SELECT last_insert_rowid()");
             if (rs.next()) {
                 Integer id = rs.getInt(1);
                 s.close();
                 Endpoint endpoint = new Endpoint(id,null,null,null,null,null,endpointAddress,null);
                 endpoints.put(id, endpoint);
                 return endpoint;
             } else {
                 s.close();
                 return null;
             }
         } catch (SQLException e) {
             e.printStackTrace();
             return null;
         }
     }
 
     @Override
     public Map<Integer,Endpoint>  getEndpoints() {
         return Collections.unmodifiableMap(endpoints);
     }
 
     @Override
     public Endpoint getEndpointByTargetUrl(String targetUrl) {
         for(Endpoint e: endpoints.values()) {
             if (e.getEndpoint().equals(targetUrl)) {
                 return e;
             }
         }
         return null;
     }
 
     @Override
     public Endpoint updateEndpoint(Endpoint endpoint) {
         try {
             Statement s = policydb.createStatement();
             s.addBatch("REPLACE INTO endpoints(id,gateway_host,http_method,uri_base,ssl,async,service_endpoint,gateway) VALUES(" +
                 +endpoint.getId() +
                 ",'"+endpoint.getGatewayHost()+"'" +
                 ",'"+endpoint.getHttpMethod()+"'" +
                 ",'"+endpoint.getUriBase()+"'" +
                 ",'"+(endpoint.getSsl() == null ? "" : endpoint.getSsl() == true ? "1" : "0" )+"'" +
                 ",'"+endpoint.getAsync()+"'" +
                 ",'"+endpoint.getEndpoint()+"'" +
                 ",'"+endpoint.getGateway()+"'" +
             ")");
             s.executeBatch();
             s.close();
             return endpoints.put(endpoint.getId(), endpoint);
         } catch (SQLException e) {
             e.printStackTrace();
             return null;
         }
     }
 
     @Override
     public void close() {
         //shut down plicy db
         try {
             log.info("*** Shutting down policy db");
             if (policydb !=null ) policydb.close();
         } catch (SQLException e2) {
             log.error("*** shut_down",e2);
         }
     }
 
     public ConfigProviderSQLite() throws SQLException,IOException,ClassNotFoundException   {
         String policyDbFile = "./policy.db";
         Class.forName("org.sqlite.JDBC");
 
         int version = 0;
 
         File f = new File(policyDbFile);
         if (f.exists()) {
             policydb = DriverManager.getConnection("jdbc:sqlite:" + policyDbFile);
         } else  {
             log.info("initializing " + policyDbFile);
             policydb = DriverManager.getConnection("jdbc:sqlite:" + policyDbFile);
             Statement s = policydb.createStatement();
             s.addBatch("CREATE TABLE endpoints (ssl NUMERIC, service_endpoint TEXT, http_method TEXT, gateway_host TEXT, ID INTEGER PRIMARY KEY, async TEXT, auth_group TEXT, gateway TEXT, uri_base TEXT);");
             s.addBatch("CREATE TABLE settings (name TEXT, value TEXT);");
             s.addBatch("CREATE TABLE users (groups TEXT, username TEXT);");
             s.addBatch("CREATE UNIQUE INDEX config ON settings(name ASC);");
             s.addBatch("CREATE UNIQUE INDEX user ON users(username ASC);");
             s.executeBatch();
             s.close();
 
             s.addBatch("INSERT INTO settings(name,value) VALUES('httpPort','8040')");
             s.addBatch("INSERT INTO settings(name,value) VALUES('sslPort','')");
             s.addBatch("INSERT INTO settings(name,value) VALUES('keyStoreFile','')");
             s.addBatch("INSERT INTO settings(name,value) VALUES('keyStorePass','')");
             s.addBatch("INSERT INTO settings(name,value) VALUES('generalTimeout','')");
             s.addBatch("INSERT INTO endpoints(ID,http_method,uri_base,service_endpoint) VALUES(3,'GET POST','/example/*','http://localhost:80/')");
             s.executeBatch();
             s.close();
 
             version = 6;
         }
 
         log.info("*** POLICY-DB CONNECTED: " + f.getAbsolutePath());        
 
         Statement s = policydb.createStatement();
 
         ResultSet rs;
         rs = s.executeQuery("SELECT value FROM settings WHERE name='configVersion'"); 
         if (rs.next()) {
             version = rs.getInt("value");
         }
         s.close();
 
         try {
             if (version <1 ) { s.executeUpdate("ALTER TABLE endpoints ADD async TEXT "); s.close(); version = 1; }
             if (version <2 ) { s.executeUpdate("ALTER TABLE endpoints ADD auth_group TEXT "); s.close(); version = 2; }        
             if (version <3 ) { 
                     s.executeUpdate("CREATE TABLE users (groups TEXT, username TEXT) ");s.close();
                     s.executeUpdate("CREATE UNIQUE INDEX user ON users(username ASC)");s.close();
                     version = 3; 
             }
             if (version <4 ) {s.executeUpdate("ALTER TABLE endpoints ADD gateway TEXT"); s.close(); version = 4;}
             if (version <5 ) {s.executeUpdate("ALTER TABLE endpoints ADD uri_base TEXT"); s.close(); version = 5;}
             if (version <6 ) {
                 version = 6;
             }
 
             if (version <7 ) {
                 s.addBatch("CREATE TABLE contracts (name TEXT, content TEXT, ip_range TEXT, interval REAL, frequency INTEGER)");
                 s.executeBatch();
                 s.close(); 
                 version = 7;
             }
             if (version <8 ) {
                 s.addBatch("ALTER TABLE contracts ADD auth_group TEXT ");
                 s.addBatch("INSERT INTO users(groups,username) VALUES('examplegroup','exampleuser')");
                 s.addBatch("INSERT INTO contracts(name,content,ip_range) VALUES('local','','127.0.0.1,0:0:0:0:0:0:0:1')");
                 s.addBatch("INSERT INTO contracts(name,content,interval,frequency,auth_group) VALUES('examplecontract','3',1.0,1,'examplegroup')");
                 s.executeBatch();
                 s.close();
                 version = 8; 
             }
             if (version <9 ) {
                 s.addBatch("CREATE TEMPORARY TABLE temp_table (ID INTEGER PRIMARY KEY,gateway_host TEXT, http_method TEXT, uri_base TEXT, ssl NUMERIC, async TEXT, service_endpoint TEXT, gateway TEXT)");
                 s.addBatch("INSERT INTO temp_table SELECT ID,gateway_host, http_method, uri_base, ssl, async, service_endpoint, gateway FROM endpoints");
                 s.addBatch("DROP TABLE endpoints");
                 s.addBatch("CREATE TABLE endpoints (ID INTEGER PRIMARY KEY,gateway_host TEXT, http_method TEXT, uri_base TEXT, ssl NUMERIC, async TEXT, service_endpoint TEXT, gateway TEXT)");
                 s.addBatch("INSERT INTO endpoints SELECT * FROM temp_table");
                 s.addBatch("DROP TABLE temp_table");
                 s.addBatch("DELETE FROM settings WHERE name='R-service-log'");
                 s.executeBatch();
                 s.close(); 
                 version = 9; 
             }
             if (version <10 ) {
                 s.addBatch("DELETE FROM settings WHERE name='generalTimeout'");
                 s.executeBatch();
                 s.close(); 
                 version = 10; 
             }
             if (version <11 ) {
                 s.addBatch("ALTER TABLE users ADD passport TEXT ");
                 s.executeBatch();
                 s.close(); 
                 version = 11; 
             }
 
         } finally {
             log.info("*** POLICY-DB Version: "+ version);
             s = policydb.createStatement();
             s.addBatch("DELETE FROM settings WHERE name='configVersion'");
             s.addBatch("INSERT INTO settings(name,value) VALUES('configVersion','"+version+"')");
             s.executeBatch();
             s.close();
         }
 
         initializeSettings();
         initializeUsers();
         initializeContracts();
         initializeEndpoints();
 
     }
 
     private void initializeUsers() throws SQLException {
         users = new LinkedHashMap<String,User>();
         String qry = "SELECT * FROM users ORDER BY username";
         Statement sql = policydb.createStatement();
         ResultSet rs = sql.executeQuery(qry);
         while (rs.next()) {
             User user = new User(
                 rs.getString("username"),
                 rs.getString("groups"),
                 rs.getString("passport")
             );
             users.put(user.getUsername(),user);
         }
     }
 
     private void initializeEndpoints() throws SQLException {
         endpoints = new LinkedHashMap<Integer,Endpoint>();
         String qry = "SELECT * FROM endpoints ORDER BY uri_base";
         Statement sql = policydb.createStatement();
         ResultSet rs = sql.executeQuery(qry);
         while (rs.next()) {
             Endpoint endpoint = new Endpoint(
                 rs.getInt("ID"),
                 Utils.blank(rs.getString("ssl")) ? null : rs.getString("ssl").equals("1") ? true : false,
                 rs.getString("gateway"),
                 rs.getString("gateway_host"),
                 rs.getString("http_method"),
                 rs.getString("uri_base"),
                 rs.getString("service_endpoint"),
                 rs.getString("async")
             );
             endpoints.put(endpoint.getId(), endpoint);
         }
     }
 
     private void initializeSettings() throws SQLException {
         settings = new LinkedHashMap<String,String>();
         ResultSet rs;
         rs = policydb.createStatement().executeQuery("SELECT * FROM settings ORDER BY name");
         while (rs.next()) if (!Utils.blank(rs.getString("name")) && !Utils.blank(rs.getString("value"))) {
             settings.put(rs.getString("name"), rs.getString("value"));
         }
     }
 
     private void initializeContracts() {
         //initialize contracts
         contracts = new LinkedHashMap<String,Contract>();
         try {
             Statement sql = policydb.createStatement();
             try {
                 ResultSet rs = sql.executeQuery("SELECT * FROM contracts ORDER BY name");
                 while (rs.next()) {
                     String name = rs.getString("name") == null ? "default" : rs.getString("name");
                     synchronized(contracts) {
                         ArrayList<String> groups = new ArrayList<String>();
                         if (rs.getString("auth_group") != null && !rs.getString("auth_group").trim().equals("")) {
                             for(String s:rs.getString("auth_group").trim().split("[\\s\\n\\r,]+")) {
                                 if (!Utils.blank(s.trim())) groups.add(s.trim());
                             }
                         }
                         List<Integer> endpoints = new ArrayList<Integer>();
                         if (rs.getString("content") != null  && !rs.getString("content").trim().equals("")) {
                             for(String s:rs.getString("content").trim().split("[\\s\\n\\r,]+")) {
                                 if (!Utils.blank(s.trim())) endpoints.add(Integer.valueOf(s.trim()));
                             }
                         }
                         contracts.put(name, new Contract(
                             name,
                             rs.getString("ip_range"),
                            new Long(Math.round(rs.getFloat("interval") * 1000)),
                             rs.getLong("frequency"),
                             groups,
                             endpoints
                         ));
                     }
                 }
             } finally {
                 sql.close();
             }
         } catch (SQLException e) {
             log.error("Contract SQL error",e);
         }
 
     }
 
 }
