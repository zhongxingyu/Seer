 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.apache.servicemix.jbi.installation;
 
 import java.io.*;
 
 import javax.jbi.JBIException;
 import javax.jbi.component.Bootstrap;
 import javax.jbi.component.Component;
 import javax.jbi.component.ComponentLifeCycle;
 import javax.jbi.component.ServiceUnitManager;
 import javax.jbi.management.LifeCycleMBean;
 import javax.management.MBeanServerInvocationHandler;
 import javax.management.ObjectName;
 
 import org.apache.servicemix.jbi.util.FileUtil;
 import org.easymock.MockControl;
 
 public class HotDeployTest extends AbstractManagementTest {
 
     // Create mocks
     protected ExtMockControl bootstrapMock;
 
     protected Bootstrap bootstrap;
 
     protected ExtMockControl componentMock;
 
     protected Component component;
 
     protected ExtMockControl lifecycleMock;
 
     protected ComponentLifeCycle lifecycle;
 
     protected ExtMockControl managerMock;
 
     protected ServiceUnitManager manager;
 
     protected void setUp() throws Exception {
         super.setUp();
         // Create mocks
         bootstrapMock = ExtMockControl.createControl(Bootstrap.class);
         bootstrap = (Bootstrap) bootstrapMock.getMock();
         Bootstrap1.setDelegate(bootstrap);
         componentMock = ExtMockControl.createControl(Component.class);
         component = (Component) componentMock.getMock();
         Component1.setDelegate(component);
         lifecycleMock = ExtMockControl.createControl(ComponentLifeCycle.class);
         lifecycle = (ComponentLifeCycle) lifecycleMock.getMock();
         managerMock = ExtMockControl.createControl(ServiceUnitManager.class);
         manager = (ServiceUnitManager) managerMock.getMock();
     }
 
     protected void reset() {
         bootstrapMock.reset();
         componentMock.reset();
         lifecycleMock.reset();
         managerMock.reset();
     }
 
     protected void replay() {
         bootstrapMock.replay();
         componentMock.replay();
         lifecycleMock.replay();
         managerMock.replay();
     }
 
     protected void verify() {
         bootstrapMock.verify();
         componentMock.verify();
         lifecycleMock.verify();
         managerMock.verify();
     }
 
     protected void initContainer() {
         container.setCreateMBeanServer(true);
         container.setMonitorInstallationDirectory(true);
         container.setMonitorDeploymentDirectory(true);
         container.setMonitorInterval(1);
     }
 
     /**
      * Simulate a slow copy or a large file deployment by copying small chinks of the file at a time.
      * 
      * @param in
      * @param out
      * @throws IOException
      */
     private static void slowCopyInputStream(InputStream in, OutputStream out) throws IOException {
         byte[] buffer = new byte[1];
         int len = in.read(buffer);
         while (len >= 0) {
             try {
                Thread.sleep(10);
             } catch (InterruptedException e) {
                 // Do nothing
             }
             out.write(buffer, 0, len);
             len = in.read(buffer);
         }
         in.close();
         out.close();
     }
 
     /**
      * Test a slow copy or similar type of file copy such as scp or ftp remote upload.
      * 
      * @throws Exception
      */
 
     public void testHotDeploySlowCopy() throws Exception {
         final Object lock = new Object();
         // configure mocks
         Bootstrap1.setDelegate(new BootstrapDelegate(bootstrap) {
             public void cleanUp() throws JBIException {
                 super.cleanUp();
                 synchronized (lock) {
                     lock.notify();
                 }
             }
         });
         reset();
         bootstrap.init(null);
         bootstrapMock.setMatcher(MockControl.ALWAYS_MATCHER);
         bootstrap.getExtensionMBeanName();
         bootstrapMock.setReturnValue(null);
         bootstrap.onInstall();
         bootstrap.cleanUp();
         component.getLifeCycle();
         componentMock.setReturnValue(lifecycle);
         lifecycle.init(null);
         lifecycleMock.setMatcher(MockControl.ALWAYS_MATCHER);
         lifecycle.start();
         replay();
         // test component installation
         startContainer(true);
         String installJarUrl = createInstallerArchive("component1").getAbsolutePath();
         File hdInstaller = new File(container.getEnvironmentContext().getInstallationDir(), new File(installJarUrl).getName());
 
         synchronized (lock) {
             slowCopyInputStream(new FileInputStream(installJarUrl), new FileOutputStream(hdInstaller));
             lock.wait(5000);
         }
         Thread.sleep(50);
 
         ObjectName lifecycleName = container.getComponent("component1").getMBeanName();
         assertNotNull(lifecycleName);
         LifeCycleMBean lifecycleMBean = (LifeCycleMBean) MBeanServerInvocationHandler.newProxyInstance(container.getMBeanServer(),
                         lifecycleName, LifeCycleMBean.class, false);
         assertEquals(LifeCycleMBean.STARTED, lifecycleMBean.getCurrentState());
         // check mocks
         verify();
 
         // Clean shutdown
         reset();
         component.getLifeCycle();
         componentMock.setReturnValue(lifecycle);
         lifecycle.stop();
         lifecycle.shutDown();
         replay();
         shutdownContainer();
     }
 
     public void testHotDeployComponent() throws Exception {
         final Object lock = new Object();
         // configure mocks
         Bootstrap1.setDelegate(new BootstrapDelegate(bootstrap) {
             public void cleanUp() throws JBIException {
                 super.cleanUp();
                 synchronized (lock) {
                     lock.notify();
                 }
             }
         });
         reset();
         bootstrap.init(null);
         bootstrapMock.setMatcher(MockControl.ALWAYS_MATCHER);
         bootstrap.getExtensionMBeanName();
         bootstrapMock.setReturnValue(null);
         bootstrap.onInstall();
         bootstrap.cleanUp();
         component.getLifeCycle();
         componentMock.setReturnValue(lifecycle);
         lifecycle.init(null);
         lifecycleMock.setMatcher(MockControl.ALWAYS_MATCHER);
         lifecycle.start();
         replay();
         // test component installation
         startContainer(true);
         String installJarUrl = createInstallerArchive("component1").getAbsolutePath();
         File hdInstaller = new File(container.getEnvironmentContext().getInstallationDir(), new File(installJarUrl).getName());
         synchronized (lock) {
             FileUtil.copyInputStream(new FileInputStream(installJarUrl), new FileOutputStream(hdInstaller));
             lock.wait(5000);
         }
         Thread.sleep(50);
         ObjectName lifecycleName = container.getComponent("component1").getMBeanName();
         assertNotNull(lifecycleName);
         LifeCycleMBean lifecycleMBean = (LifeCycleMBean) MBeanServerInvocationHandler.newProxyInstance(container.getMBeanServer(),
                         lifecycleName, LifeCycleMBean.class, false);
         assertEquals(LifeCycleMBean.STARTED, lifecycleMBean.getCurrentState());
         // check mocks
         verify();
 
         // Clean shutdown
         reset();
         component.getLifeCycle();
         componentMock.setReturnValue(lifecycle);
         lifecycle.stop();
         lifecycle.shutDown();
         replay();
         shutdownContainer();
     }
 
     public void testHotDeployUndeployComponent() throws Exception {
         final Object lock = new Object();
         // configure mocks
         Bootstrap1.setDelegate(new BootstrapDelegate(bootstrap) {
             public void cleanUp() throws JBIException {
                 super.cleanUp();
                 synchronized (lock) {
                     lock.notify();
                 }
             }
         });
         reset();
         bootstrap.init(null);
         bootstrapMock.setMatcher(MockControl.ALWAYS_MATCHER);
         bootstrap.getExtensionMBeanName();
         bootstrapMock.setReturnValue(null);
         bootstrap.onInstall();
         bootstrap.cleanUp();
         component.getLifeCycle();
         componentMock.setReturnValue(lifecycle);
         lifecycle.init(null);
         lifecycleMock.setMatcher(MockControl.ALWAYS_MATCHER);
         lifecycle.start();
         replay();
         // test component installation
         startContainer(true);
         String installJarUrl = createInstallerArchive("component1").getAbsolutePath();
         File hdInstaller = new File(container.getEnvironmentContext().getInstallationDir(), new File(installJarUrl).getName());
         synchronized (lock) {
             FileUtil.copyInputStream(new FileInputStream(installJarUrl), new FileOutputStream(hdInstaller));
             lock.wait(5000);
         }
         Thread.sleep(50);
         ObjectName lifecycleName = container.getComponent("component1").getMBeanName();
         assertNotNull(lifecycleName);
         LifeCycleMBean lifecycleMBean = (LifeCycleMBean) MBeanServerInvocationHandler.newProxyInstance(container.getMBeanServer(),
                         lifecycleName, LifeCycleMBean.class, false);
         assertEquals(LifeCycleMBean.STARTED, lifecycleMBean.getCurrentState());
         // check mocks
         verify();
 
         // Configure mocks
         reset();
         bootstrap.onUninstall();
         bootstrap.cleanUp();
         lifecycle.stop();
         lifecycle.shutDown();
         // manager.shutDown("su");
         replay();
         // test component uninstallation
         synchronized (lock) {
             assertTrue(hdInstaller.delete());
             lock.wait(5000);
         }
         Thread.sleep(50);
         assertNull(container.getComponent("component1"));
         // check mocks
         verify();
 
         // Clean shutdown
         reset();
         replay();
         shutdownContainer();
     }
 
     public void testDeploySAThenComponent() throws Exception {
         final Object lock = new Object();
         // configure mocks
         Bootstrap1.setDelegate(new BootstrapDelegate(bootstrap) {
             public void cleanUp() throws JBIException {
                 super.cleanUp();
                 synchronized (lock) {
                     lock.notify();
                 }
             }
         });
         reset();
         bootstrap.init(null);
         bootstrapMock.setMatcher(MockControl.ALWAYS_MATCHER);
         bootstrap.getExtensionMBeanName();
         bootstrapMock.setReturnValue(null);
         bootstrap.onInstall();
         bootstrap.cleanUp();
         component.getLifeCycle();
         componentMock.setReturnValue(lifecycle);
         lifecycle.init(null);
         lifecycleMock.setMatcher(MockControl.ALWAYS_MATCHER);
         lifecycle.start();
         component.getServiceUnitManager();
         componentMock.setReturnValue(manager, MockControl.ONE_OR_MORE);
         manager.deploy(null, null);
         managerMock.setMatcher(MockControl.ALWAYS_MATCHER);
         managerMock.setReturnValue(null);
         manager.init(null, null);
         managerMock.setMatcher(MockControl.ALWAYS_MATCHER);
         manager.start("su");
         replay();
         // test component installation
         startContainer(true);
         File saJarUrl = createServiceAssemblyArchive("sa", "su", "component1");
         File hdSa = new File(container.getEnvironmentContext().getDeploymentDir(), saJarUrl.getName());
         FileUtil.copyInputStream(new FileInputStream(saJarUrl), new FileOutputStream(hdSa));
         Thread.sleep(2000);
 
         String installJarUrl = createInstallerArchive("component1").getAbsolutePath();
         File hdInstaller = new File(container.getEnvironmentContext().getInstallationDir(), new File(installJarUrl).getName());
         synchronized (lock) {
             FileUtil.copyInputStream(new FileInputStream(installJarUrl), new FileOutputStream(hdInstaller));
             lock.wait(5000);
         }
         Thread.sleep(2000);
         ObjectName lifecycleName = container.getComponent("component1").getMBeanName();
         assertNotNull(lifecycleName);
         LifeCycleMBean lifecycleMBean = (LifeCycleMBean) MBeanServerInvocationHandler.newProxyInstance(container.getMBeanServer(),
                         lifecycleName, LifeCycleMBean.class, false);
         assertEquals(LifeCycleMBean.STARTED, lifecycleMBean.getCurrentState());
         // check mocks
         verify();
 
         // Clean shutdown
         reset();
         component.getLifeCycle();
         componentMock.setReturnValue(lifecycle);
         component.getServiceUnitManager();
         componentMock.setReturnValue(manager, MockControl.ONE_OR_MORE);
         lifecycle.stop();
         manager.shutDown("su");
         lifecycle.shutDown();
         replay();
         shutdownContainer();
     }
 }
