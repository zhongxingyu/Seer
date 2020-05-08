 package com.cloudbees.genapp.tomcat7;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 import javax.xml.parsers.*;
 
 import com.cloudbees.genapp.XmlUtils;
 import org.w3c.dom.*;
 
 import com.cloudbees.genapp.metadata.Metadata;
 import com.cloudbees.genapp.metadata.resource.*;
 
 public class ContextXmlBuilder {
 
     private Metadata metadata;
     private Set<String> databaseProperties = new HashSet(Arrays.asList("minIdle", "maxIdle", "maxActive", "maxWait",
             "initialSize",
             "validationQuery", "validationQueryTimeout", "testOnBorrow", "testOnReturn",
             "timeBetweenEvictionRunsMillis", "numTestsPerEvictionRun", "minEvictableIdleTimeMillis", "testWhileIdle",
             "removeAbandoned", "removeAbandonedTimeout", "logAbandoned", "defaultAutoCommit", "defaultReadOnly",
             "defaultTransactionIsolation", "poolPreparedStatements", "maxOpenPreparedStatements", "defaultCatalog",
             "connectionInitSqls", "connectionProperties", "accessToUnderlyingConnectionAllowed",
             /* Tomcat JDBC Enhanced Attributes */
             "factory", "type", "validatorClassName", "initSQL", "jdbcInterceptors", "validationInterval", "jmxEnabled",
             "fairQueue", "abandonWhenPercentageFull", "maxAge", "useEquals", "suspectTimeout", "rollbackOnReturn",
             "commitOnReturn", "alternateUsernameAllowed", "useDisposableConnectionFacade", "logValidationErrors",
             "propagateInterruptState"));
     private File appDir;
 
     public ContextXmlBuilder(Metadata metadata, File appDir) {
         this.metadata = metadata;
         if (!appDir.exists()) {
             throw new IllegalArgumentException("appDir does not exist '" + appDir.getAbsolutePath() + "'");
         } else if (!appDir.isDirectory()) {
             throw new IllegalArgumentException("appDir must be a directory '" + appDir.getAbsolutePath() + "'");
         }
         this.appDir = appDir;
     }
 
     protected ContextXmlBuilder addDatabase(Database database, Document serverDocument, Document contextXmlDocument) {
         Element e = contextXmlDocument.createElement("Resource");
         e.setAttribute("name", "jdbc/" + database.getName());
         e.setAttribute("auth", "Container");
         e.setAttribute("type", "javax.sql.DataSource");
         e.setAttribute("url", "jdbc:" + database.getUrl());
         e.setAttribute("driverClassName", database.getJavaDriver());
         e.setAttribute("username", database.getUsername());
         e.setAttribute("password", database.getPassword());
 
         // by default, use use tomcat-jdbc-pool
         e.setAttribute("factory", "org.apache.tomcat.jdbc.pool.DataSourceFactory");
 
         // by default max to 20 connections which is the limit of CloudBees MySQL databases
         e.setAttribute("maxActive", "20");
         e.setAttribute("maxIdle", "10");
         e.setAttribute("minIdle", "1");
 
         // test on borrow and while idle to release idle connections
         e.setAttribute("testOnBorrow", "true");
         e.setAttribute("testWhileIdle", "true");
         e.setAttribute("validationQuery", database.getValidationQuery());
         e.setAttribute("validationInterval", "5000"); // 5 secs
 
         // all the parameters can be overwritten
         for (Map.Entry<String, String> entry : database.getProperties().entrySet()) {
             if (databaseProperties.contains(entry.getKey())) {
                 e.setAttribute(entry.getKey(), entry.getValue());
             } else {
                 System.out.println("Ignore unknown datasource property '" + entry + "'");
             }
         }
 
         contextXmlDocument.getDocumentElement().appendChild(e);
         return this;
     }
 
     protected ContextXmlBuilder addEmail(Email email, Document serverDocument, Document contextXmlDocument) {
         Element e = contextXmlDocument.createElement("Resource");
         e.setAttribute("name", email.getName());
         e.setAttribute("auth", "Container");
         e.setAttribute("type", "javax.mail.Session");
         e.setAttribute("mail.smtp.user", email.getUsername());
        e.setAttribute("password", email.getPassword());
         e.setAttribute("mail.smtp.host", email.getHost());
         e.setAttribute("mail.smtp.auth", "true");
 
         contextXmlDocument.getDocumentElement().appendChild(e);
         return this;
     }
 
     protected ContextXmlBuilder addSessionStore(SessionStore store, Document serverDocument, Document contextXmlDocument) {
         Element e = contextXmlDocument.createElement("Manager");
         e.setAttribute("className", "de.javakaffee.web.msm.MemcachedBackupSessionManager");
         e.setAttribute("transcoderFactoryClass", "de.javakaffee.web.msm.serializer.kryo.KryoTranscoderFactory");
         e.setAttribute("memcachedProtocol", "binary");
         e.setAttribute("requestUriIgnorePattern", ".*\\.(ico|png|gif|jpg|css|js)$");
         e.setAttribute("sessionBackupAsync", "false");
         e.setAttribute("sticky", "false");
         e.setAttribute("memcachedNodes", store.getNodes());
         e.setAttribute("username", store.getUsername());
         e.setAttribute("password", store.getPassword());
 
         contextXmlDocument.getDocumentElement().appendChild(e);
         return this;
     }
 
     protected ContextXmlBuilder addPrivateAppValve(Metadata metadata, Document serverXmlDocument, Document contextXmlDocument) {
         String section = "privateApp";
 
         if (metadata.getRuntimeProperty(section) == null) {
             return this;
         }
         System.out.println("Insert PrivateAppValve");
 
         Set<String> privateAppProperties = new HashSet<String>(Arrays.asList(
                 "className", "secretKey",
                 "authenticationEntryPointName",
                 "authenticationParameterName", "authenticationHeaderName", "authenticationUri", "authenticationCookieName",
                 "enabled", "realmName", "ignoredUriRegexp"));
 
         Element privateAppValve = serverXmlDocument.createElement("Valve");
 
         privateAppValve.setAttribute("className", "com.cloudbees.tomcat.valves.PrivateAppValve");
 
 
         for (Map.Entry<String, String> entry : metadata.getRuntimeProperty(section).entrySet()) {
             if (privateAppProperties.contains(entry.getKey())) {
                 privateAppValve.setAttribute(entry.getKey(), entry.getValue());
             } else {
                 System.out.println("privateAppValve: ignore unknown property '" + entry.getKey() + "'");
             }
 
         }
         if (privateAppValve.getAttribute("secretKey").isEmpty()) {
             throw new IllegalStateException("Invalid '" + section +
                     "' configuration, '" + section + "." + "secretKey' is missing");
         }
 
         Element remoteIpValve = XmlUtils.getUniqueElement(serverXmlDocument, "//Valve[@className='org.apache.catalina.valves.RemoteIpValve']");
         XmlUtils.insertSiblingAfter(privateAppValve, remoteIpValve);
         return this;
     }
 
     protected void buildTomcatConfiguration(Metadata metadata, Document serverXmlDocument, Document contextXmlDocument) throws ParserConfigurationException {
 
         String message = "File generated by tomcat7-clickstack at " + new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(new Date());
 
         serverXmlDocument.appendChild(serverXmlDocument.createComment(message));
         contextXmlDocument.appendChild(contextXmlDocument.createComment(message));
 
         for (Resource resource : metadata.getResources().values()) {
             if (resource instanceof Database) {
                 addDatabase((Database) resource, serverXmlDocument, contextXmlDocument);
             } else if (resource instanceof Email) {
                 addEmail((Email) resource, serverXmlDocument, contextXmlDocument);
             } else if (resource instanceof SessionStore) {
                 addSessionStore((SessionStore) resource, serverXmlDocument, contextXmlDocument);
             }
         }
         addPrivateAppValve(metadata, serverXmlDocument, contextXmlDocument);
     }
 
     /**
      * @param serverXmlFilePath relative to {@link #appDir}
      * @param contextXmlFilePath relative to {@link #appDir}
      */
     public void buildTomcatConfigurationFiles(String serverXmlFilePath, String contextXmlFilePath) throws Exception {
 
         File contextXmlFile = new File(appDir, contextXmlFilePath);
         Document contextXmlDocument = XmlUtils.loadXmlDocumentFromFile(contextXmlFile);
         XmlUtils.checkRootElement(contextXmlDocument, "Context");
 
         File serverXmlFile = new File(appDir, serverXmlFilePath);
         Document serverXmlDocument = XmlUtils.loadXmlDocumentFromFile(serverXmlFile);
 
         this.buildTomcatConfiguration(metadata, serverXmlDocument, contextXmlDocument);
 
         XmlUtils.flush(contextXmlDocument, new FileOutputStream(contextXmlFile));
         XmlUtils.flush(serverXmlDocument, new FileOutputStream(serverXmlFile));
     }
 }
