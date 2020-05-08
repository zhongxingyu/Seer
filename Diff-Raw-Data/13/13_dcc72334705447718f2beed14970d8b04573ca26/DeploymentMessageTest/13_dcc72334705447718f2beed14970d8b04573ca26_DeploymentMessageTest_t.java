 /*
  * Copyright 2005-2006 The Apache Software Foundation.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
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
 
 import java.io.File;
 
 import javax.xml.transform.TransformerException;
 
 import org.apache.servicemix.jbi.framework.ManagementSupport;
 import org.apache.servicemix.jbi.jaxp.SourceTransformer;
 import org.apache.servicemix.jbi.jaxp.StringSource;
 import org.apache.servicemix.jbi.util.DOMUtil;
 import org.apache.xml.utils.PrefixResolver;
 import org.apache.xpath.CachedXPathAPI;
 import org.apache.xpath.objects.XObject;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 public class DeploymentMessageTest extends AbstractManagementTest {
 
     protected void initContainer() {
         container.setCreateMBeanServer(false);
         container.setMonitorInstallationDirectory(false);
         container.setMonitorDeploymentDirectory(false);
     }
     
     public void testDeployNullJarUrl() throws Exception {
         startContainer(true);
         try {
             getDeploymentService().deploy(null);
             fail("Deploy with null jar url should have failed");
         } catch (Exception e) {
             assertTrue(e instanceof Exception);
             String str = e.getMessage();
             System.err.println(str);
             Node node = new SourceTransformer().toDOMNode(new StringSource(str));
             assertNotNull(node);
             assertEquals("FAILED", textValueOfXPath(node, "//jbi:frmwk-task-result//jbi:task-result"));
             assertEquals("ERROR", textValueOfXPath(node, "//jbi:frmwk-task-result//jbi:message-type"));
             assertEquals("deploy", textValueOfXPath(node, "//jbi:frmwk-task-result//jbi:task-id"));
         }
     }
      
     public void testDeployNonExistentJar() throws Exception {
         startContainer(true);
         try {
             getDeploymentService().deploy("hello");
             fail("Deploy with non existent jar url should have failed");
         } catch (Exception e) {
             assertTrue(e instanceof Exception);
             String str = e.getMessage();
             System.err.println(str);
             Node node = new SourceTransformer().toDOMNode(new StringSource(str));
             assertNotNull(node);
             assertEquals("FAILED", textValueOfXPath(node, "//jbi:frmwk-task-result//jbi:task-result"));
             assertEquals("ERROR", textValueOfXPath(node, "//jbi:frmwk-task-result//jbi:message-type"));
             assertEquals("deploy", textValueOfXPath(node, "//jbi:frmwk-task-result//jbi:task-id"));
         }
     }
     
     public void testDeployNoDescriptor() throws Exception {
         startContainer(true);
         String jarUrl = createDummyArchive().getAbsolutePath();
         try {
             getDeploymentService().deploy(jarUrl);
             fail("Deploy with non existent descriptor should have failed");
         } catch (Exception e) {
             assertTrue(e instanceof Exception);
             String str = e.getMessage();
             System.err.println(str);
             Node node = new SourceTransformer().toDOMNode(new StringSource(str));
             assertNotNull(node);
             assertEquals("FAILED", textValueOfXPath(node, "//jbi:frmwk-task-result//jbi:task-result"));
             assertEquals("ERROR", textValueOfXPath(node, "//jbi:frmwk-task-result//jbi:message-type"));
             assertEquals("deploy", textValueOfXPath(node, "//jbi:frmwk-task-result//jbi:task-id"));
         }
     }
     
     public void testDeployWithNonSADescriptor() throws Exception {
         startContainer(true);
         String jarUrl = createInstallerArchive("component1").getAbsolutePath();
         try {
             getDeploymentService().deploy(jarUrl);
             fail("Deploy with non existent descriptor should have failed");
         } catch (Exception e) {
             assertTrue(e instanceof Exception);
             String str = e.getMessage();
             System.err.println(str);
             Node node = new SourceTransformer().toDOMNode(new StringSource(str));
             assertNotNull(node);
             assertEquals("FAILED", textValueOfXPath(node, "//jbi:frmwk-task-result//jbi:task-result"));
             assertEquals("ERROR", textValueOfXPath(node, "//jbi:frmwk-task-result//jbi:message-type"));
             assertEquals("deploy", textValueOfXPath(node, "//jbi:frmwk-task-result//jbi:task-id"));
         }
     }
     
     public void testDeployWithSuccess() throws Exception {
         DummyComponent component = new DummyComponent();
         String deployResult = DOMUtil.asXML(ManagementSupport.createComponentSuccess("deploy", "component1"));
         component.setResult(deployResult);
         System.err.println(deployResult);
         startContainer(true);
         getContainer().activateComponent(component, "component1");
         getContainer().getEnvironmentContext().createComponentRootDirectory("component1");
         File installSaUrl = createServiceAssemblyArchive("sa", "su", "component1");
         String result = getDeploymentService().deploy(installSaUrl.getAbsolutePath());
         System.err.println(result);
         Node node = new SourceTransformer().toDOMNode(new StringSource(result));
         assertNotNull(node);
         // main task
         assertEquals("SUCCESS", textValueOfXPath(node, "//jbi:frmwk-task-result//jbi:task-result"));
         assertEquals("deploy", textValueOfXPath(node, "//jbi:frmwk-task-result//jbi:task-id"));
         // component task
         assertEquals("SUCCESS", textValueOfXPath(node, "//jbi:component-task-result//jbi:task-result"));
         assertEquals("deploy", textValueOfXPath(node, "//jbi:component-task-result//jbi:task-id"));
     }
     
     public void testDeployWithOneSuccessAndOneError() throws Exception {
         DummyComponent component1 = new DummyComponent();
         String deployResult1 = DOMUtil.asXML(ManagementSupport.createComponentFailure("deploy", "component1", "xxx", null));
         component1.setResult(deployResult1);
         component1.setException(true);
         System.err.println(deployResult1);
         DummyComponent component2 = new DummyComponent();
         String deployResult2 = DOMUtil.asXML(ManagementSupport.createComponentSuccess("deploy", "component2"));
         component2.setResult(deployResult2);
         System.err.println(deployResult2);
         startContainer(true);
         getContainer().activateComponent(component1, "component1");
         getContainer().getEnvironmentContext().createComponentRootDirectory("component1");
         getContainer().activateComponent(component2, "component2");
         getContainer().getEnvironmentContext().createComponentRootDirectory("component2");
         File installSaUrl = createServiceAssemblyArchive("sa", new String[] { "su1", "su2" }, new String[] { "component1", "component2"});
        String result = null;;
        try {
            result = getDeploymentService().deploy(installSaUrl.getAbsolutePath());
            fail("Deployment with an error is not supported");
        } catch (Exception e) {
            result = e.getMessage();
        }
         System.err.println(result);
         Node node = new SourceTransformer().toDOMNode(new StringSource(result));
         assertNotNull(node);
         // main task
        assertEquals("FAILED", textValueOfXPath(node, "//jbi:frmwk-task-result//jbi:task-result"));
        assertEquals("ERROR", textValueOfXPath(node, "//jbi:frmwk-task-result//jbi:message-type"));
         assertEquals("deploy", textValueOfXPath(node, "//jbi:frmwk-task-result//jbi:task-id"));
         // first component task
         assertEquals("FAILED", textValueOfXPath(node, "//jbi:component-task-result[1]//jbi:task-result"));
         assertEquals("deploy", textValueOfXPath(node, "//jbi:component-task-result[1]//jbi:task-id"));
         // second component task
         assertEquals("SUCCESS", textValueOfXPath(node, "//jbi:component-task-result[2]//jbi:task-result"));
         assertEquals("deploy", textValueOfXPath(node, "//jbi:component-task-result[2]//jbi:task-id"));
     }
     
     public void testUndeployNullJarUrl() throws Exception {
         startContainer(true);
         try {
             getDeploymentService().undeploy(null);
             fail("Deploy with null jar url should have failed");
         } catch (Exception e) {
             assertTrue(e instanceof Exception);
             String str = e.getMessage();
             System.err.println(str);
             Node node = new SourceTransformer().toDOMNode(new StringSource(str));
             assertNotNull(node);
             assertEquals("FAILED", textValueOfXPath(node, "//jbi:frmwk-task-result//jbi:task-result"));
             assertEquals("ERROR", textValueOfXPath(node, "//jbi:frmwk-task-result//jbi:message-type"));
             assertEquals("undeploy", textValueOfXPath(node, "//jbi:frmwk-task-result//jbi:task-id"));
         }
     }
      
     public void testUndeployNonDeployedSA() throws Exception {
         startContainer(true);
         try {
             getDeploymentService().undeploy("my-sa");
             fail("Deploy with non deployed sa should have failed");
         } catch (Exception e) {
             assertTrue(e instanceof Exception);
             String str = e.getMessage();
             System.err.println(str);
             Node node = new SourceTransformer().toDOMNode(new StringSource(str));
             assertNotNull(node);
             assertEquals("FAILED", textValueOfXPath(node, "//jbi:frmwk-task-result//jbi:task-result"));
             assertEquals("ERROR", textValueOfXPath(node, "//jbi:frmwk-task-result//jbi:message-type"));
             assertEquals("undeploy", textValueOfXPath(node, "//jbi:frmwk-task-result//jbi:task-id"));
         }
     }
      
     protected String textValueOfXPath(Node node, String xpath) throws TransformerException {
         CachedXPathAPI cachedXPathAPI = new CachedXPathAPI();
         XObject list = cachedXPathAPI.eval(node, xpath, new PrefixResolver() {
             public String getNamespaceForPrefix(String prefix) {
                 if (prefix.equals("jbi")) {
                     return "http://java.sun.com/xml/ns/jbi/management-message";
                 }
                 return null;
             }
             public String getNamespaceForPrefix(String arg0, Node arg1) {
                 return null;
             }
             public String getBaseIdentifier() {
                 return null;
             }
             public boolean handlesNullPrefixes() {
                 return false;
             }
         });
         Node root = list.nodeset().nextNode();
         if (root instanceof Element) {
             Element element = (Element) root;
             if (element == null) {
                 return "";
             }
             String text = DOMUtil.getElementText(element);
             return text;
         }
         else if (root != null) {
             return root.getNodeValue();
         } else {
             return null;
         }
     }
     
 }
