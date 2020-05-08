 /***************************************************************
  *  This file is part of the [fleXive](R) project.
  *
  *  Copyright (c) 1999-2008
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU General Public
  *  License as published by the Free Software Foundation;
  *  either version 2 of the License, or (at your option) any
  *  later version.
  *
  *  The GNU General Public License can be found at
  *  http://www.gnu.org/copyleft/gpl.html.
  *  A copy is found in the textfile GPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.tests.embedded;
 
 import com.flexive.core.security.UserTicketImpl;
 import com.flexive.core.structure.StructureLoader;
 import com.flexive.shared.EJBLookup;
 import com.flexive.shared.FxContext;
import com.flexive.shared.CacheAdmin;
 import com.flexive.shared.configuration.DivisionData;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.interfaces.AccountEngine;
 import com.flexive.shared.security.UserTicket;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.jboss.ejb3.embedded.EJB3StandaloneBootstrap;
 import org.jboss.ejb3.embedded.EJB3StandaloneDeployer;
 import org.testng.annotations.AfterSuite;
 import org.testng.annotations.BeforeSuite;
 import org.testng.annotations.Test;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * JBoss EJB3 embedded container bootstrap
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 @Test(groups = {"bootstrap", "ejb", "configuration", "content", "structure", "jsf", "security",
         "workflow", "streaming", "scripting", "valuetest", "cache", "image", "tree", "relation",
         "search", "tutorial", "benchmark", "environment", "mandator", "templates", "importexport"})
 public class ContainerBootstrap {
     private static final transient Log LOG = LogFactory.getLog(ContainerBootstrap.class);
 
     private EJB3StandaloneDeployer deployer;
     private List<Object> deployedBeans = new ArrayList<Object>();
     protected final List<URL> deployDirectories = new ArrayList<URL>();
 
     @BeforeSuite
     public void startup() throws FxApplicationException {
         System.out.println("=== starting EJB3 container ===");
         try {
             System.setProperty("java.naming.factory.initial", "org.jnp.interfaces.LocalOnlyContextFactory");
             System.setProperty("java.naming.factory.url.pkgs", "org.jboss.naming:org.jnp.interfaces");
             EJB3StandaloneBootstrap.boot(null);
             // enable security manager
             EJB3StandaloneBootstrap.deployXmlResource("security-beans.xml");
             deployer = EJB3StandaloneBootstrap.createDeployer();
             if (LOG.isInfoEnabled()) {
                 LOG.info("Running on: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
             }
 
             deployDirectories.add(0, new URL(getFileUrl(getFlexiveBaseDir()) + getFlexiveDistDir()));
             for (URL url : deployDirectories) {
                 deployDirectory(url);
             }
 
             deployer.getArchivesByResource().add(getFlexiveBaseDir() + "/src/META-INF/embeddedJBossCacheConfig.xml");
 
 //            deployer.setMbeanServer(MBeanServerFactory.createMBeanServer("flexive"));
 
             if (LOG.isInfoEnabled()) {
                 LOG.info("Deploying...");
             }
             deployer.create();
             deployer.start();
 
             if (LOG.isDebugEnabled()) {
                 LOG.debug("MBeanServer: " + deployer.getMbeanServer());
             }
             /*
                         FxCacheMBean cache = new FxCache();
                         cache.create();
                         deployer.getMbeanServer().registerMBean(cache, new ObjectName(CacheAdmin.CACHE_SERVICE_NAME));
                         deployedBeans.add(cache);
 
                         StructureMBean structure = new Structure();
                         structure.create();
                         deployer.getMbeanServer().registerMBean(structure, new ObjectName(StructureAdmin.STRUCTURE_SERVICE_NAME));
                         deployedBeans.add(structure);
             */
             FxContext.get().setDivisionId(DivisionData.DIVISION_TEST);
             FxContext.get().setContextPath("flexiveTests");
            CacheAdmin.getEnvironment();
             FxContext.get().setTicket(UserTicketImpl.getGuestTicket());
             TestUsers.initializeUsers();
         } catch (Exception ex) {
             throw new RuntimeException(ex);
         }
     }
 
     protected String getFlexiveDistDir() {
         return "/build/framework/jar";
     }
 
     protected void deployDirectory(URL deployDir) throws MalformedURLException {
         deployer.getDeployDirs().add(deployDir);
     }
 
     protected String getFileUrl(String fileName) {
         return "file:" + (System.getProperty("os.name").toLowerCase().indexOf("windows") >= 0 ? "/" : "") + fileName;
     }
 
     protected String getFlexiveBaseDir() {
         return getBuildDir();
     }
 
     protected String getBuildDir() {
         return System.getProperty("user.dir");
     }
 
     @AfterSuite
     public void shutdown() throws FxApplicationException {
         try {
             ScriptingTest.allowTearDown = true;
             ScriptingTest.suiteShutDown();
             TestUsers.deleteUsers();
             System.out.println("=== shutting down EJB3 container ===");
             AccountEngine accountEngine = EJBLookup.getAccountEngine();
             for (UserTicket ticket : accountEngine.getActiveUserTickets())
                 System.out.println("Still logged in: " + ticket);
 
             // destroy MBeans (in reverse order)
             Collections.reverse(deployedBeans);
             for (Object bean : deployedBeans) {
                 try {
                     bean.getClass().getMethod("destroy", new Class[0]).invoke(bean, new Object[0]);
                 } catch (Exception e) {
                     System.out.println("Failed to destroy MBean: " + e.getMessage());
                     e.printStackTrace();
                 }
             }
 
             deployer.stop();
 //            deployer.destroy();
 //            EJB3StandaloneBootstrap.shutdown();
         } catch (Exception ex) {
             ex.printStackTrace();
             throw new RuntimeException(ex);
         }
     }
 
 }
