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
 
 package org.eclipse.virgo.snaps.test;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.lang.management.ManagementFactory;
 import java.util.Properties;
 
 import javax.management.MBeanServer;
 import javax.management.ObjectName;
 
import org.junit.Ignore;
 import org.junit.Test;
 import org.osgi.framework.Constants;
 
 import org.eclipse.virgo.kernel.deployer.core.DeploymentException;
 import org.eclipse.virgo.kernel.deployer.core.DeploymentIdentity;
 
 public class SnapCapabilityTests extends AbstractCapabilityTests {
 
     private DeploymentIdentity snap;
 
     private void deployHostAndSnap() throws DeploymentException {
         deployHost();
         snap = deploy("src/test/resources/simple-snap.jar");
     }
 
     private void undeployHostAndSnap() throws DeploymentException {
         getDeployer().undeploy(snap);
         undeployHost();
     }
 
     @Test
     public void simpleMapping() throws Exception {
         deployHostAndSnap();
         try {
             RequestUtils.assertContent("test1", "/simple-host", "/simple/test1");
         } finally {
             undeployHostAndSnap();
         }
     }
     
     @Test
     public void simpleMappingWithRootHost() throws Exception {
         deployRootHost();
         snap = deploy("src/test/resources/simple-snap.jar");
         try {
             RequestUtils.assertContent("test1", "/", "/simple/test1");
         } finally {
             undeployHostAndSnap();
         }
     }
 
     @Test
     public void servletLifecycle() throws Exception {
         deployHostAndSnap();
         ObjectName mBeanName = new ObjectName("snaps:type=Test");
         MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
         try {
             assertTrue(platformMBeanServer.isRegistered(mBeanName));
         } finally {
             undeployHostAndSnap();
         }
         assertFalse(platformMBeanServer.isRegistered(mBeanName));
     }
 
     @Test
     public void requestPathTranslationExact() throws Exception {
         deployHostAndSnap();
         try {
             Properties properties = RequestUtils.requestForProperties("/simple-host", "/simple/exact");
             assertEquals("/simple-host", properties.get("ContextPath"));
             assertEquals("/simple/exact", properties.get("ServletPath"));
             assertEquals("null", properties.get("PathInfo"));
             assertEquals("/simple-host/simple/exact", properties.get("RequestUri"));
         } finally {
             undeployHostAndSnap();
         }
     }
 
     @Test
     public void requestPathTranslationPathMapping() throws Exception {
         deployHostAndSnap();
         try {
             Properties properties = RequestUtils.requestForProperties("/simple-host", "/simple/path/test");
             assertEquals("/simple-host", properties.get("ContextPath"));
             assertEquals("/simple/path", properties.get("ServletPath"));
             assertEquals("/test", properties.get("PathInfo"));
             assertEquals("/simple-host/simple/path/test", properties.get("RequestUri"));
         } finally {
             undeployHostAndSnap();
         }
     }
 
     @Test
     public void directJspRequest() throws Exception {
         deployHostAndSnap();
         try {
             RequestUtils.assertContent("OK", "/simple-host", "/simple/index.jsp");
         } finally {
             undeployHostAndSnap();
         }
     }
 
     @Test
     public void dispatchLocalJsp() throws Exception {
         deployHostAndSnap();
         try {
             RequestUtils.assertContent("OK", "/simple-host", "/simple/rd?test=forward-jsp");
         } finally {
             undeployHostAndSnap();
         }
     }
 
     @Test
     public void dispatchLocalServlet() throws Exception {
         deployHostAndSnap();
         try {
             RequestUtils.assertContent("test1", "/simple-host", "/simple/rd?test=forward-servlet");
         } finally {
             undeployHostAndSnap();
         }
     }
 
     @Test
     public void dispatchHostJsp() throws Exception {
         deployHostAndSnap();
         try {
             RequestUtils.assertContent("index", "/simple-host", "/simple/rd?test=forward-host-jsp");
         } finally {
             undeployHostAndSnap();
         }
     }
 
     @Test
     public void forwardAttributesAfterSingleForward() throws Exception {
         deployHostAndSnap();
         try {
             Properties properties = RequestUtils.requestForProperties("/simple-host", "/simple/forward/index.htm?foo=bar");
             assertEquals("/simple-host/simple/forward/index.htm", properties.get("javax.servlet.forward.request_uri"));
             assertEquals("/simple-host", properties.get("javax.servlet.forward.context_path"));
             assertEquals("/simple/forward", properties.get("javax.servlet.forward.servlet_path"));
             assertEquals("/index.htm", properties.get("javax.servlet.forward.path_info"));
             assertEquals("foo=bar", properties.get("javax.servlet.forward.query_string"));
 
             assertEquals("/simple-host/simple/fa", properties.get("requestURI"));
             assertEquals("/simple-host", properties.get("ContextPath"));
             assertEquals("/simple/fa", properties.get("ServletPath"));
             assertEquals("null", properties.get("PathInfo"));
             // TODO This looks right to me, but we get foo=bar back
             // assertEquals("null", properties.get("QueryString"));
         } finally {
             undeployHostAndSnap();
         }
     }
 
     @Test
     public void forwardAttributesAfterMultipleForwards() throws Exception {
         deployHostAndSnap();
         try {
             Properties properties = RequestUtils.requestForProperties("/simple-host", "/simple/rd?test=forward");
             assertEquals("/simple-host/simple/rd", properties.get("javax.servlet.forward.request_uri"));
             assertEquals("/simple-host", properties.get("javax.servlet.forward.context_path"));
             assertEquals("/simple/rd", properties.get("javax.servlet.forward.servlet_path"));
             assertEquals("null", properties.get("javax.servlet.forward.path_info"));
             assertEquals("test=forward", properties.get("javax.servlet.forward.query_string"));
 
             assertEquals("/simple-host/simple/fa", properties.get("requestURI"));
             assertEquals("/simple-host", properties.get("ContextPath"));
             assertEquals("/simple/fa", properties.get("ServletPath"));
             assertEquals("null", properties.get("PathInfo"));
             // TODO This looks right to me, but we get test=forward back
             // assertEquals("null", properties.get("QueryString"));
         } finally {
             undeployHostAndSnap();
         }
     }
 
     @Test
     public void forwardWithPathTranslationPathMapping() throws Exception {
         deployHostAndSnap();
         try {
             Properties properties = RequestUtils.requestForProperties("/simple-host", "/simple/rd?test=forward-path");
             assertEquals("/simple-host/simple/rd", properties.get("javax.servlet.forward.request_uri"));
             assertEquals("/simple-host", properties.get("javax.servlet.forward.context_path"));
             assertEquals("/simple/rd", properties.get("javax.servlet.forward.servlet_path"));
             assertEquals("null", properties.get("javax.servlet.forward.path_info"));
             assertEquals("test=forward-path", properties.get("javax.servlet.forward.query_string"));
 
             assertEquals("/simple-host/simple/fa/test", properties.get("requestURI"));
             assertEquals("/simple-host", properties.get("ContextPath"));
             assertEquals("/simple/fa", properties.get("ServletPath"));
             assertEquals("/test", properties.get("PathInfo"));
             // TODO This looks right to me, but we get test=forward-path back
             // assertEquals("null", properties.get("QueryString"));
         } finally {
             undeployHostAndSnap();
         }
     }
 
     @Test
     public void includeAttributesAfterSingleInclude() throws Exception {
         deployHostAndSnap();
         try {
             Properties properties = RequestUtils.requestForProperties("/simple-host", "/simple/include/index.htm?foo=bar");
             assertEquals("/simple-host/simple/ia", properties.get("javax.servlet.include.request_uri"));
             assertEquals("/simple-host", properties.get("javax.servlet.include.context_path"));
             assertEquals("/simple/ia", properties.get("javax.servlet.include.servlet_path"));
             assertEquals("null", properties.get("javax.servlet.include.path_info"));
             assertEquals("null", properties.get("javax.servlet.include.query_string"));
             
             assertEquals("/simple-host/simple/include/index.htm", properties.get("requestURI"));
             assertEquals("/simple-host", properties.get("ContextPath"));
             assertEquals("/simple/include", properties.get("ServletPath"));
             assertEquals("/index.htm", properties.get("PathInfo"));
             assertEquals("foo=bar", properties.get("QueryString"));
         } finally {
             undeployHostAndSnap();
         }
     }
 
     @Test
     public void filterLifecycle() throws Exception {
         deployHostAndSnap();
         ObjectName mBeanName = new ObjectName("snaps:type=FilterLifecycleTest");
         MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
         try {
             assertTrue(platformMBeanServer.isRegistered(mBeanName));
         } finally {
             undeployHostAndSnap();
         }
         assertFalse(platformMBeanServer.isRegistered(mBeanName));
     }
 
     @Test
     public void requestFilter() throws Exception {
         deployHostAndSnap();
         try {
             RequestUtils.assertContent("foobartest1", "/simple-host", "/simple/filterTest1");
         } finally {
             undeployHostAndSnap();
         }
     }
 
     @Test
     public void forwardFilter() throws Exception {
         deployHostAndSnap();
         try {
             RequestUtils.assertContent("baztest1", "/simple-host", "/simple/rd?test=forward-filter-servlet");
         } finally {
             undeployHostAndSnap();
         }
     }
 
     @Test
    @Ignore("Fix with bugzilla 333164")
     public void snapBundleContext() throws Exception {
         deployHostAndSnap();
         try {
             Properties properties = RequestUtils.requestForProperties("/simple-host", "/simple/osgiBundleContext");
             assertEquals("simple.snap", properties.getProperty(Constants.BUNDLE_SYMBOLICNAME));
         } finally {
             undeployHostAndSnap();
         }
     }
 }
