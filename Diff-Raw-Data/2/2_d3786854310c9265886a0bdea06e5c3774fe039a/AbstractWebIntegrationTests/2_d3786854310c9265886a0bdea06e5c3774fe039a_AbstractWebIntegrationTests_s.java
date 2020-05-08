 /*******************************************************************************
  * Copyright (c) 2008, 2010 VMware Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   VMware Inc. - initial contribution
  *******************************************************************************/
 
 package org.eclipse.virgo.web.test;
 
 import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
 import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
 import static javax.servlet.http.HttpServletResponse.SC_FOUND;
 import static javax.servlet.http.HttpServletResponse.SC_OK;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 import java.lang.management.ManagementFactory;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.management.InstanceNotFoundException;
 import javax.management.JMException;
 import javax.management.JMX;
 import javax.management.MBeanServer;
 import javax.management.ObjectName;
 
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.runner.RunWith;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.FrameworkUtil;
 import org.osgi.framework.ServiceReference;
 
 import org.eclipse.virgo.nano.deployer.api.core.ApplicationDeployer;
 import org.eclipse.virgo.nano.deployer.api.core.DeploymentIdentity;
 import org.eclipse.virgo.nano.deployer.api.core.DeploymentOptions;
 import org.eclipse.virgo.kernel.model.management.ManageableArtifact;
 import org.eclipse.virgo.kernel.osgi.framework.OsgiFramework;
 import org.eclipse.virgo.test.framework.dmkernel.DmKernelTestRunner;
 import org.eclipse.virgo.util.io.PathReference;
 
 /**
  * Abstract base class for all web integration tests. Transparently retrieves the {@link ApplicationDeployer} from the
  * OSGi service registry and provides support for subclasses to deploy web applications (i.e., WARs, hybrid WARs, and
  * PARs which contain a web module). In addition, simple assertion facilities are provided for verifying that a
  * particular web resource is available (e.g., via an HTTP GET or POST request).
  * 
  */
 @RunWith(DmKernelTestRunner.class)
 public abstract class AbstractWebIntegrationTests {
 
     private static final long HOT_DEPLOY_TIMEOUT = 30000;
 
     private static final long WEB_PLAN_DEPLOY_TIMEOUT = 5*60*1000; // 5 minutes
 
	private static final String CURRENT_VERSION = "3.6.0";
 
     private static final String USER_REGION_NAME = "org.eclipse.virgo.region.user";
 
     protected final List<String> deployedWebApps = new ArrayList<String>();
 
     protected OsgiFramework osgiFramework;
 
     protected ApplicationDeployer appDeployer;
 
     protected boolean reuseHttpClient = true;
 
     private final HttpClient httpClient = new HttpClient();
 
     protected boolean followRedirects = true;
 
     /**
      * Gets the {@link HttpClient} to use for the current request. If {@link #reuseHttpClient} is set to
      * <code>true</code>, this method will return the same, pre-instantiated client; otherwise, this method will
      * instantiate and return a new client.
      */
     protected HttpClient getHttpClient() {
         return reuseHttpClient ? this.httpClient : new HttpClient();
     }
 
     /**
      * @param context the context path of the web-app, which if non-null, will be prepended to the resource path
      * @param resource the resource path to test against
      * @param expectedResponseCode the expected HTTP response code
      */
     protected void assertGetRequest(String context, String resource, int expectedResponseCode) throws Exception {
         assertGetRequest(context, resource, expectedResponseCode, (List<String>) null);
     }
 
     /**
      * @param context the context path of the web-app, which if non-null, will be prepended to the resource path
      * @param resource the resource path to test against
      * @param expectedResponseCode the expected HTTP response code
      * @param expectedContents text expected to exist in the returned resource
      */
     protected void assertGetRequest(String context, String resource, int expectedResponseCode, List<String> expectedContents) throws Exception {
         assertGetRequest(context, resource, this.followRedirects, expectedResponseCode, expectedContents);
     }
 
     /**
      * @param context the context path of the web-app, which if non-null, will be prepended to the resource path
      * @param resource the resource path to test against
      * @param followRedirects whether or not to automatically follow redirects
      * @param expectedResponseCode the expected HTTP response code
      * @param expectedContents list of text strings expected to exist in the returned resource
      */
     protected void assertGetRequest(String context, String resource, boolean followRedirects, int expectedResponseCode, List<String> expectedContents)
         throws Exception {
         assertGetRequest("http://localhost:8080/" + (context == null ? "" : context + "/") + resource, followRedirects, expectedResponseCode,
             expectedContents);
     }
 
     /**
      * @param address the complete address (i.e., URL) against which to test
      * @param expectedResponseCode the expected HTTP response code
      * @param expectedContents list of text strings expected to exist in the returned resource
      */
     protected void assertGetRequest(String address, int expectedResponseCode, List<String> expectedContents) throws Exception {
         assertGetRequest(address, this.followRedirects, expectedResponseCode, expectedContents);
     }
 
     /**
      * @param address the complete address (i.e., URL) against which to test
      * @param followRedirects whether or not to automatically follow redirects
      * @param expectedResponseCode the expected HTTP response code
      * @param expectedContents list of text strings expected to exist in the returned resource
      */
     protected void assertGetRequest(String address, boolean followRedirects, int expectedResponseCode, List<String> expectedContents)
         throws Exception {
         System.out.println("AbstractWebIntegrationTests: executing GET request for [" + address + "].");
         GetMethod get = new GetMethod(address);
         get.setFollowRedirects(followRedirects);
         int responseCode = getHttpClient().executeMethod(get);
         System.out.println(get.getResponseBodyAsString());
         assertEquals("Verifying HTTP response code for URL [" + address + "]", expectedResponseCode, responseCode);
 
         if (responseCode / 100 == 2 && expectedContents != null) {
             String body = get.getResponseBodyAsString();
             System.out.println(body);
             assertNotNull("The response body for URL [" + address + "] should not be null.", body);
             // System.err.println(body);
             for (String expected : expectedContents) {
                 assertTrue("The response body for URL [" + address + "] should contain [" + expected + "].", body.contains(expected));
             }
         }
     }
 
     /**
      * @param context the context path of the web-app, which if non-null, will be prepended to the resource path
      * @param resource the resource path to test against
      * @param params parameters in the form of name-value pairs to be passed in the initial POST request
      * @param followRedirectsForGet whether or not to automatically follow redirects for a GET request following the
      *        initial POST request
      * @param expectedPostResponseCode the expected HTTP response code for the initial POST request
      * @param expectedGetAfterPostResponseCode the expected HTTP response code for the subsequent GET request
      * @param expectedContents text expected to exist in the returned resource
      */
     protected void assertPostRequest(String context, String resource, Map<String, String> params, boolean followRedirectsForGet,
         int expectedPostResponseCode, int expectedGetAfterPostResponseCode, String expectedContents) throws Exception {
 
         final String address = "http://localhost:8080/" + (context == null ? "" : context + "/") + resource;
         System.out.println("AbstractWebIntegrationTests: executing POST request for [" + address + "].");
         final PostMethod post = new PostMethod(address);
         for (String name : params.keySet()) {
             post.setParameter(name, params.get(name));
         }
 
         int responseCode = getHttpClient().executeMethod(post);
         assertEquals("Verifying HTTP POST response code for URL [" + address + "]", expectedPostResponseCode, responseCode);
 
         if (responseCode / 100 == 2 && expectedContents != null) {
             String body = post.getResponseBodyAsString();
             assertNotNull("The response body for URL [" + address + "] should not be null.", body);
             // System.err.println(body);
             assertTrue("The response body for URL [" + address + "] should contain [" + expectedContents + "].", body.contains(expectedContents));
         } else if (responseCode == SC_FOUND && expectedContents != null) {
             String location = post.getResponseHeader("Location").getValue();
             assertGetRequest(location, followRedirectsForGet, expectedGetAfterPostResponseCode, Arrays.asList(expectedContents));
         }
     }
 
     /**
      * Verifies only the existence of the supplied resources (i.e., response code 200); does <b>not</b> check contents
      * of response body.
      * 
      * @param context the context path of the web-app
      * @param file the file from which to deploy the web-app
      * @param resources list of resources to check
      * @see #assertDeployAndUndeployBehavior(String, URI, String...)
      */
     protected void assertDeployAndUndeployBehavior(String context, File file, String... resources) throws Exception {
         assertDeployAndUndeployBehavior(context, file.toURI(), resources);
     }
 
     /**
      * Verifies only the existence of the supplied resources (i.e., response code 200); does <b>not</b> check contents
      * of response body.
      * 
      * @param context the context path of the web-app
      * @param uri the URI from which to deploy the web-app
      * @param resources list of resources to check
      * @see #assertDeployAndUndeployBehavior(String, File, Map)
      */
     protected void assertDeployAndUndeployBehavior(String context, URI uri, String... resources) throws Exception {
         DeploymentIdentity deploymentIdentity = this.appDeployer.deploy(uri, new DeploymentOptions(false, false, true));
         this.deployedWebApps.add(context);
 
         // Uncomment if you'd like to pause the test and view the results in a web browser.
         //System.in.read();
 
         try {
             for (String resource : resources) {
                 assertGetRequest(context, resource, SC_OK, null);
             }
         } finally {
             this.appDeployer.undeploy(deploymentIdentity);
         }
 
         for (String resource : resources) {
             assertGetRequest(context, resource, SC_NOT_FOUND);
         }
     }
 
     /**
      * Verifies the existence of the supplied resources (i.e., response code 200) and checks the contents of
      * corresponding response bodies.
      * 
      * @param context the context path of the web-app
      * @param file the file from which to deploy the web-app
      * @param expectations a map of expected contents per resource, keyed by resource path.
      */
     protected void assertDeployAndUndeployBehavior(String context, File file, Map<String, List<String>> expectations) throws Exception {
         final URI uri = file.toURI();
         DeploymentIdentity deploymentIdentity = this.appDeployer.deploy(uri);
         this.deployedWebApps.add(context);
 
         // Uncomment if you'd like to pause the test and view the results in a web browser.
         // System.in.read();
 
         try {
             for (String resource : expectations.keySet()) {
                 List<String> expectedContents = expectations.get(resource);
                 assertGetRequest(context, resource, SC_OK, expectedContents);
             }
         } finally {
             this.appDeployer.undeploy(deploymentIdentity);
         }
 
         for (String resource : expectations.keySet()) {
             assertGetRequest(context, resource, SC_NOT_FOUND, null);
         }
     }
 
     /**
      * @param context the context path of the web-app
      * @param file the file from which to deploy the web-app
      * @return the {@link DeploymentIdentity} of the deployed application
      */
     protected DeploymentIdentity assertDeployBehavior(String context, File file) throws Exception {
         return assertDeployBehavior(context, file, new HashMap<String, List<String>>());
     }
 
     /**
      * @param context the context path of the web-app
      * @param file the file from which to deploy the web-app
      * @param expectations a map of expected contents per resource, keyed by resource path.
      * @return the {@link DeploymentIdentity} of the deployed application
      */
     protected DeploymentIdentity assertDeployBehavior(String context, File file, Map<String, List<String>> expectations) throws Exception {
         final URI uri = file.toURI();
         // Deploy non-recoverably and not owned by the deployer. Non-recoverable should speed up the tests.
         DeploymentIdentity deploymentIdentity = this.appDeployer.deploy(uri, new DeploymentOptions(false, false, true));
         this.deployedWebApps.add(context);
 
         // Uncomment if you'd like to pause the test and view the results in a web browser.
         // System.in.read();
 
         for (String resource : expectations.keySet()) {
             List<String> expectedContents = expectations.get(resource);
             assertGetRequest(context, resource, SC_OK, expectedContents);
         }
         return deploymentIdentity;
     }
 
     /**
      * @param moduleSymbolicName the bundle symbolic name of the module to refresh
      * @param context the context path of the web-app
      * @param file the file from which to refresh the web-app
      * @param expectations a map of expected contents per resource, keyed by resource path.
      */
     protected void assertRefreshBehavior(String moduleSymbolicName, String context, File file, Map<String, List<String>> expectations)
         throws Exception {
         final URI uri = file.toURI();
         this.appDeployer.refresh(uri, moduleSymbolicName);
 
         // Uncomment if you'd like to pause the test and view the results in a web browser.
         // System.in.read();
 
         for (String resource : expectations.keySet()) {
             List<String> expectedContents = expectations.get(resource);
             assertGetRequest(context, resource, SC_OK, expectedContents);
         }
     }
 
     /**
      * @param context the context path of the web-app
      * @param deploymentIdentity the {@link DeploymentIdentity} of the deployed web-app
      */
     protected void assertUndeployBehavior(String context, DeploymentIdentity deploymentIdentity) throws Exception {
         assertUndeployBehavior(context, deploymentIdentity, new HashMap<String, List<String>>());
     }
 
     /**
      * @param context the context path of the web-app
      * @param deploymentIdentity the {@link DeploymentIdentity} of the deployed web-app
      * @param expectations a map of expected contents per resource, keyed by resource path.
      */
     protected void assertUndeployBehavior(String context, DeploymentIdentity deploymentIdentity, Map<String, List<String>> expectations)
         throws Exception {
         this.appDeployer.undeploy(deploymentIdentity);
         for (String resource : expectations.keySet()) {
             assertGetRequest(context, resource, SC_BAD_REQUEST, null);
         }
     }
 
     @Before
     public void setUp() throws Exception {
         awaitInitialArtifactDeployment();
 
         BundleContext bundleContext = FrameworkUtil.getBundle(getClass()).getBundleContext();
 
         ServiceReference<ApplicationDeployer> appDeployerServiceReference = bundleContext.getServiceReference(ApplicationDeployer.class);
         assertNotNull("ApplicationDeployer service reference not found", appDeployerServiceReference);
         this.appDeployer = bundleContext.getService(appDeployerServiceReference);
         assertNotNull("ApplicationDeployer service not found", this.appDeployer);
     }
 
     @AfterClass
     public static void cleanup() throws Exception {
         MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
         ObjectName objectName = new ObjectName("org.eclipse.virgo.kernel:type=ArtifactModel,artifact-type=plan,name=org.eclipse.virgo.web.tomcat,version=" + CURRENT_VERSION + ",region=global");
 
         try {
             mBeanServer.invoke(objectName, "stop", null, null);
             mBeanServer.invoke(objectName, "uninstall", null, null);
         } catch (JMException _) {
         }
     }
 
     private void awaitInitialArtifactDeployment() throws JMException, InterruptedException {
         MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
         ObjectName objectName = new ObjectName("org.eclipse.virgo.kernel:type=ArtifactModel,artifact-type=plan,name=org.eclipse.virgo.web.tomcat,version=" + CURRENT_VERSION + ",region=global");
 
         Object state = null;
         long startTime = System.currentTimeMillis();
 
         while (!"ACTIVE".equals(state)) {
             try {
                 state = mBeanServer.getAttribute(objectName, "State");
                 Thread.sleep(100);
             } catch (InstanceNotFoundException _) {
             }
             if (System.currentTimeMillis() - startTime > WEB_PLAN_DEPLOY_TIMEOUT) {
                 throw new RuntimeException("Web plan did not start within " + (WEB_PLAN_DEPLOY_TIMEOUT / 1000) + " seconds.");
             }
         }
     }
 
     protected PathReference hotDeploy(PathReference toDeploy, String name, String version) throws InterruptedException {
         PathReference deployed = toDeploy.copy(new PathReference("target/pickup"), true);
 
         awaitDeployment(toDeploy, deployed);
         awaitWebAppStart(name, version);
 
         return deployed;
     }
 
     private void awaitDeployment(PathReference toDeploy, PathReference deployed) throws InterruptedException {
         long startTime = System.currentTimeMillis();
         while (!(this.appDeployer.isDeployed(deployed.toURI()))) {
             Thread.sleep(100);
             if (System.currentTimeMillis() - startTime > HOT_DEPLOY_TIMEOUT) {
                 throw new RuntimeException(toDeploy + " failed to deploy within " + (HOT_DEPLOY_TIMEOUT / 1000) + " seconds.");
             }
         }
     }
 
     private void awaitWebAppStart(String name, String version) throws InterruptedException {
         MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
         try {
             ObjectName objectName = new ObjectName(String.format("org.eclipse.virgo.kernel:type=ArtifactModel,artifact-type=bundle,name=%s,version=%s,region=%s", name,
                 version, USER_REGION_NAME));
             ManageableArtifact artifact = JMX.newMXBeanProxy(mBeanServer, objectName, ManageableArtifact.class);
 
             long startTime = System.currentTimeMillis();
 
             while (artifact.getProperties().get("org.eclipse.virgo.web.contextPath") == null) {
                 Thread.sleep(100);
                 if (System.currentTimeMillis() - startTime > HOT_DEPLOY_TIMEOUT) {
                     throw new RuntimeException(name + " " + version + " failed to set its context path within " + (HOT_DEPLOY_TIMEOUT / 1000)
                         + " seconds.");
                 }
             }
         } catch (JMException e) {
             throw new RuntimeException(e);
         }
     }
 
     protected void hotUnDeploy(PathReference deployed) throws InterruptedException {
         URI uri = deployed.toURI();
         assertTrue(this.appDeployer.isDeployed(uri));
 
         deployed.delete(true);
 
         long startTime = System.currentTimeMillis();
 
         while (this.appDeployer.isDeployed(uri)) {
             Thread.sleep(100);
             if (System.currentTimeMillis() - startTime > HOT_DEPLOY_TIMEOUT) {
                 throw new RuntimeException(deployed + " failed to undeploy within " + (HOT_DEPLOY_TIMEOUT / 1000) + " seconds.");
             }
         }
     }
 
 }
