 package org.cagrid.tests.data.styles.cacore42.steps;
 
 import gov.nih.nci.cagrid.common.FaultHelper;
 import gov.nih.nci.cagrid.common.Utils;
 import gov.nih.nci.cagrid.cqlquery.CQLQuery;
 import gov.nih.nci.cagrid.cqlquery.Object;
 import gov.nih.nci.cagrid.data.client.DataServiceClient;
 import gov.nih.nci.cagrid.testing.system.deployment.SecureContainer;
 import gov.nih.nci.cagrid.testing.system.deployment.ServiceContainer;
 import gov.nih.nci.cagrid.testing.system.haste.Step;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.lang.reflect.Modifier;
 import java.util.Enumeration;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import java.util.StringTokenizer;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 
 import org.apache.axis.types.URI;
 import org.apache.axis.types.URI.MalformedURIException;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.cagrid.cql2.CQLTargetObject;
 import org.cagrid.data.test.creation.DataTestCaseInfo;
 import org.globus.gsi.GlobusCredential;
 import org.oasis.wsrf.faults.BaseFaultType;
 
 public class InvokeCsmDataServiceStep extends Step {
     
     public static final String ACCESS_DENIED_MESSAGE = "Access is denied";
 
     public static final String TESTS_BASE_DIR_PROPERTY = "sdk42.tests.base.dir";
     public static final String ACCESS_DENIED_LIST_FILE = "resources" + File.separator + "access.denied.expected.list";
     public static final String PROXY_FILENAME = "user.proxy";
     
     private static Log LOG = LogFactory.getLog(InvokeCsmDataServiceStep.class);
     
     private DataTestCaseInfo testInfo = null;
     private ServiceContainer container = null;
     
     public InvokeCsmDataServiceStep(DataTestCaseInfo testInfo, ServiceContainer container) {
         super();
         this.testInfo = testInfo;
         this.container = container;
     }
     
 
     public void runStep() throws Throwable {
         List<String> domainClasses = getDomainClassList();
         List<String> expectedDenied = getExpectedAccessDenied();
         testCql1(domainClasses, expectedDenied);
         testCql2(domainClasses, expectedDenied);
     }
     
     
     private void testCql1(List<String> domainClasses, List<String> expectedDenied) {
         DataServiceClient client = getServiceClient();
         for (String clazz : domainClasses) {
             CQLQuery query = new CQLQuery();
             Object target = new Object();
             target.setName(clazz);
             query.setTarget(target);
             try {
                 LOG.debug("Querying for " + clazz);
                 client.query(query);
                 if (expectedDenied.contains(clazz)) {
                     fail("CSM should have denied access to " + clazz + " but was allowed");
                 }
             } catch (Exception ex) {
                 if (isAccessDenied(ex)) {
                     if (expectedDenied.contains(clazz)) {
                         LOG.debug("Access correctly denied to " + clazz);
                     } else {
                         ex.printStackTrace();
                         fail("Access incorrectly denied to " + clazz);
                     }
                 } else if (isJava6CSMError(ex)) {
                     LOG.info("CSM isn't compatible with Java 6, so it threw this error", ex);
                 } else {
                     ex.printStackTrace();
                     fail("Unexpected error querying data service for class " 
                         + clazz + ": " + ex.getMessage());
                 }
             }
         }
     }
     
     
     private void testCql2(List<String> domainClasses, List<String> expectedDenied) {
         DataServiceClient client = getServiceClient();
         for (String clazz : domainClasses) {
             org.cagrid.cql2.CQLQuery query = new org.cagrid.cql2.CQLQuery();
             CQLTargetObject target = new CQLTargetObject();
             target.setClassName(clazz);
             query.setCQLTargetObject(target);
             try {
                 LOG.debug("Querying for " + clazz);
                 client.executeQuery(query);
                 if (expectedDenied.contains(clazz)) {
                     fail("CSM should have denied access to " + clazz + " but was allowed");
                 }
             } catch (Exception ex) {
                 if (isAccessDenied(ex)) {
                     if (expectedDenied.contains(clazz)) {
                         LOG.debug("Access correctly denied to " + clazz);
                     } else {
                         ex.printStackTrace();
                         fail("Access incorrectly denied to " + clazz);
                     }
                } else if (isJava6CSMError(ex)) {
                    LOG.info("CSM isn't compatible with Java 6, so it threw this error", ex);
                 } else {
                     ex.printStackTrace();
                     fail("Unexpected error querying data service for class " 
                         + clazz + ": " + ex.getMessage());
                 }
             }
         }
     }
     
     
     private List<String> getDomainClassList() {
         List<String> classes = new LinkedList<String>();
         File serviceLibDir = new File(testInfo.getDir(), "lib");
         JarFile beansJar = null;
         File[] beansJars = serviceLibDir.listFiles(new FileFilter() {
             public boolean accept(File pathname) {
                 return pathname.getName().endsWith("-beans.jar");
             }
         });
         assertEquals("Unexpected number of -beans.jar files found in service lib", 1, beansJars.length);
         try {
             beansJar = new JarFile(beansJars[0]);
         } catch (IOException ex) {
             ex.printStackTrace();
             fail("Error opening beans jar: " + ex.getMessage());
         }
         Enumeration<JarEntry> entries = beansJar.entries();
         while (entries.hasMoreElements()) {
             JarEntry entry = entries.nextElement();
             if (!entry.isDirectory()) {
                 String name = entry.getName();
                 if (name.endsWith(".class") && !name.contains("interfaze")) {
                     String klassName = name.replace('/', '.').substring(0, name.lastIndexOf('.'));
                     try {
                         Class<?> clazz = Class.forName(klassName);
                         if (!Modifier.isAbstract(clazz.getModifiers())) {
                             classes.add(klassName);
                         }
                     } catch (ClassNotFoundException ex) {
                         ex.printStackTrace();
                         fail("Unable to load class " + klassName);
                     }
                 }
             }
         }
         try {
             beansJar.close();
         } catch (IOException ex) {
             // whatever
             LOG.warn("Error closing beans jar: " + ex.getMessage(), ex);
         }
         return classes;
     }
     
     
     private List<String> getExpectedAccessDenied() {
         FileInputStream in = null;
         try {
             String baseDir = System.getProperty(TESTS_BASE_DIR_PROPERTY);
             in = new FileInputStream(new File(baseDir, ACCESS_DENIED_LIST_FILE));
         } catch (IOException ex) {
             ex.printStackTrace();
             fail("Could not open access denied list file: " + ex.getMessage());
         }
         List<String> list = new LinkedList<String>();
         try {
             StringBuffer contents = Utils.inputStreamToStringBuffer(in);
             in.close();
             StringTokenizer tok = new StringTokenizer(contents.toString());
             while (tok.hasMoreTokens()) {
                 list.add(tok.nextToken());
             }
         } catch (Exception ex) {
             ex.printStackTrace();
             fail("Error loading access denied list: " + ex.getMessage());
         }
         return list;
     }
     
     
     private boolean isAccessDenied(Exception ex) {
         Throwable cause = ex;
         Set<Throwable> seenCauses = new HashSet<Throwable>();
         boolean isDenied = false;
         while (cause != null && !seenCauses.contains(cause) && !isDenied) {
             String message = cause.getMessage();
             if (cause instanceof BaseFaultType) {
                 message = FaultHelper.getMessage(cause);
             }
             if (message.contains(ACCESS_DENIED_MESSAGE)) {
                 isDenied = true;
             }
             seenCauses.add(cause);
             cause = cause.getCause();
         }
         return isDenied;
     }
     
     
     private boolean isJava6CSMError(Exception ex) {
         boolean yep = false;
         if (isJava6()) {
             String message = FaultHelper.getMessage(ex);
             yep = message.contains("CSException occured");
         }
         return yep;
     }
     
     
     protected boolean isJava6() {
         boolean is6 = false;
         String val = System.getProperty("java.version");
         if (val != null && val.startsWith("1.6")) {
             is6 = true;
         }
         return is6;
     }
     
     
     private DataServiceClient getServiceClient() {
         DataServiceClient client = null;
         try {
             if (container instanceof SecureContainer) {
                 client = new DataServiceClient(getServiceUrl(), loadGlobusCredential());
                 client.setAnonymousPrefered(false);
             } else {
                 client = new DataServiceClient(getServiceUrl());
             }
         } catch (Exception ex) {
             ex.printStackTrace();
             fail("Error creating data service client: " + ex.getMessage());
         }
         return client;
     }
     
     
     private String getServiceUrl() {
         String url = null;
         try {
             URI baseUri = container.getContainerBaseURI();
             url = baseUri.toString() + "cagrid/" + testInfo.getName();
         } catch (MalformedURIException ex) {
             ex.printStackTrace();
             fail("Error generating service url: " + ex.getMessage());
         }
         LOG.debug("Data service url: " + url);
         return url;
     }
     
     
     private GlobusCredential loadGlobusCredential() {
         // Load the testing proxy cert
         GlobusCredential proxyCredential = null;
         try {
             File proxyFile = new File(((SecureContainer) container).getCertificatesDirectory(), PROXY_FILENAME);
             proxyCredential = new GlobusCredential(proxyFile.getAbsolutePath());
         } catch (Exception ex) {
             ex.printStackTrace();
             fail("Error obtaining client proxy: " + ex.getMessage());
         }
         return proxyCredential;
     }
 }
