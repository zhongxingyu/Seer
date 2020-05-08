 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.portletcontainer.descriptor;
 
 import junit.framework.Test;
 import junit.framework.TestSuite;
 import org.apache.cactus.ServletTestCase;
 import org.gridlab.gridsphere.core.persistence.PersistenceManagerException;
 import org.gridlab.gridsphere.portlet.Portlet;
 import org.gridlab.gridsphere.portlet.PortletRole;
 import org.gridlab.gridsphere.portlet.PortletWindow;
 import org.gridlab.gridsphere.portletcontainer.ConcretePortletConfig;
 import org.gridlab.gridsphere.portletcontainer.GridSphereConfig;
 import org.gridlab.gridsphere.portletcontainer.GridSphereConfigProperties;
 import org.gridlab.gridsphere.portletcontainer.impl.descriptor.*;
 
 import java.io.IOException;
 import java.util.Hashtable;
 import java.util.List;
 
 /**
  * This is the base fixture for service testing. Provides a service factory and the
  * properties file.
  */
 public class PortletDescriptorTest extends ServletTestCase {
 
     private final static String separator = System.getProperty("file.separator");
 
     public PortletDescriptorTest(String name) {
         super(name);
     }
 
     public static void main(String[] args) throws Exception {
         junit.textui.TestRunner.run(suite());
     }
 
     public static Test suite() {
         return new TestSuite(PortletDescriptorTest.class);
     }
 
     public void testPortletDescriptor() {
         PortletDeploymentDescriptor pdd = null;
 
         // load files from JAR
         String portletFile = GridSphereConfig.getProperty(GridSphereConfigProperties.TEST_HOME) + separator + "portlet-test.xml";
         String mappingFile = GridSphereConfig.getProperty(GridSphereConfigProperties.PORTLET_MAPPING);
 
         try {
             pdd = new PortletDeploymentDescriptor(portletFile, mappingFile);
         } catch (IOException e) {
             fail("IO error unmarshalling " + portletFile + " using " + mappingFile + " : " + e.getMessage());
         } catch (PersistenceManagerException e) {
             fail("Unable to unmarshall " + portletFile + " using " + mappingFile + " : " + e.getMessage());
         }
         List defs = pdd.getPortletDefinitionList();
 
         // assertEquals(expected, actual)
 
         // we have one app descriptions
         assertEquals(1, defs.size());
 
        String groupOwner = pdd.getGroupOwnerName();
        assertEquals("The Test Group", groupOwner);
         SportletDefinition def = (SportletDefinition) defs.get(0);
         ApplicationSportletConfig portletApp = def.getApplicationSportletConfig();
         List concreteApps = def.getConcreteSportletList();
 
         // we have two concrete portlet apps
         assertEquals(2, concreteApps.size());
         ConcreteSportletDefinition concreteOne = (ConcreteSportletDefinition) concreteApps.get(0);
         ConcreteSportletDefinition concreteTwo = (ConcreteSportletDefinition) concreteApps.get(1);
         assertEquals("org.gridlab.gridsphere.portlets.core.HelloWorld.666", portletApp.getApplicationPortletID());
         assertEquals("Hello World", portletApp.getPortletName());
         assertEquals("hello", portletApp.getServletName());
 
         Hashtable configHash = portletApp.getConfigParams();
         List cl = portletApp.getConfigParamList();
 
         assertEquals(2, cl.size());
         assertEquals(2, configHash.size());
 
         assertEquals(true, configHash.containsKey("AppConfigOne"));
         assertEquals(true, configHash.containsKey("AppConfigTwo"));
         assertEquals(false, configHash.containsKey("two"));
         String val = (String)configHash.get("AppConfigOne");
         assertEquals("one", val);
         val = (String)configHash.get("AppConfigTwo");
         assertEquals("two", val);
 
         // Test cache info -- later
         //CacheInfo c = portletApp.getCacheInfo();
         //assertEquals(120, c.getExpires());
         //assertEquals("true", c.getShared());
 
         List winstatelist = portletApp.getAllowedWindowStates();
         assertEquals(2, winstatelist.size());
         assertEquals(PortletWindow.State.MAXIMIZED, winstatelist.get(0));
         assertEquals(PortletWindow.State.MINIMIZED, winstatelist.get(1));
 
         List smodes = portletApp.getSupportedModes();
         assertEquals(4, smodes.size());
         assertEquals(Portlet.Mode.VIEW, smodes.get(0));
         assertEquals(Portlet.Mode.EDIT, smodes.get(1));
         assertEquals(Portlet.Mode.HELP, smodes.get(2));
         assertEquals(Portlet.Mode.CONFIGURE, smodes.get(3));
 
 
         // Check concrete one portal data
         assertEquals("org.gridlab.gridsphere.portlets.core.HelloWorld.666.2", concreteOne.getConcretePortletID());
 
         Hashtable contextList = concreteOne.getContextAttributes();
         assertEquals(1, contextList.size());
 
         assertEquals(true, contextList.containsKey("buzzle"));
         assertEquals(true, contextList.containsValue("yea"));
 
         ConcretePortletConfig onePI = concreteOne.getConcreteSportletConfig();
         assertEquals("Hello World 1", onePI.getName());
         assertEquals("en", onePI.getDefaultLocale());
 
         List langList = onePI.getLanguageList();
         assertEquals(langList.size(), 2);
         LanguageInfo langOne = (LanguageInfo) langList.get(0);
         LanguageInfo langTwo = (LanguageInfo) langList.get(1);
 
         assertEquals("Here is a simple portlet", langOne.getDescription());
         assertEquals("portlet hello world", langOne.getKeywords());
         assertEquals("en_US", langOne.getLocale());
         assertEquals("Hello World - Sample Portlet #1", langOne.getTitle());
         assertEquals("Hello World", langOne.getTitleShort());
 
         assertEquals("Hier ist ein gleicht portlet", langTwo.getDescription());
         assertEquals("portlet hallo welt", langTwo.getKeywords());
         assertEquals("en_DE", langTwo.getLocale());
         assertEquals("Hallo Welt - Sample Portlet #1", langTwo.getTitle());
         assertEquals("Hallo Welt", langTwo.getTitleShort());
 
 
         assertEquals(ConcretePortletConfig.Scope.PRIVATE, onePI.getConcretePortletScope());
         assertEquals(PortletRole.ADMIN, onePI.getRequiredRole());
 
         configHash = onePI.getConfigAttributes();
 
         assertEquals(configHash.size(), 1);
         assertEquals(true, configHash.containsKey("Portlet Mistress"));
         assertEquals(true, configHash.containsValue("mistress@domain.com"));
 
 
         // Check concrete two portal data
         assertEquals(concreteTwo.getConcretePortletID(), "org.gridlab.gridsphere.portlets.core.HelloWorld.666.4");
 
         configHash = concreteTwo.getContextAttributes();
         assertEquals(configHash.size(), 1);
 
         assertEquals(true, configHash.containsKey("Portlet Master"));
         assertEquals(true, configHash.containsValue("secondguy@some.com"));
 
         ConcretePortletConfig twoPI = concreteTwo.getConcreteSportletConfig();
         assertEquals(twoPI.getName(), "Hello World 2");
         assertEquals(twoPI.getDefaultLocale(), "en");
 
         langList = twoPI.getLanguageList();
         assertEquals(langList.size(), 1);
         langOne = (LanguageInfo) langList.get(0);
 
         assertEquals(langOne.getDescription(), "Here is another simple portlet");
         assertEquals(langOne.getKeywords(), "portlet hello world");
         assertEquals(langOne.getLocale(), "en_US");
         assertEquals(langOne.getTitle(), "Hello World - Sample Portlet #2");
         assertEquals(langOne.getTitleShort(), "Hello World");
 
 
         assertEquals(ConcretePortletConfig.Scope.PUBLIC, twoPI.getConcretePortletScope());
         assertEquals(PortletRole.USER, twoPI.getRequiredRole());
 
         configHash = twoPI.getConfigAttributes();
         assertEquals(configHash.size(), 1);
 
         assertEquals(true, configHash.containsKey("Portlet Master"));
         assertEquals(true, configHash.containsValue("secondguy@some.com"));
 
         // demonstrate saving
         /*
         Hashtable store = new Hashtable();
         store.put("beezle", "yo");
         store.put("buzzle", "yea");
         Enumeration enum = store.keys();
         Vector list = new Vector();
         while (enum.hasMoreElements()) {
             String k = (String)enum.nextElement();
             System.err.println(k);
             String value = (String)store.get(k);
             ConfigParam parms = new ConfigParam(k, value);
             list.add(parms);
         }
 
         concreteOne.setID("whose your daddy?");
         concreteOne.setContextParamList(list);
         portletApp.setPortletName("yo dude");
         pdd.setPortletAppDescriptor(portletApp);
         pdd.setConcretePortletApplication(concreteOne);
         try {
             pdd.save("/tmp/portlet.xml", "webapps/gridsphere/WEB-INF/conf/mapping/portlet-mapping.xml");
         } catch (Exception e) {
             System.err.println("Unable to save SportletApplicationSettings: " + e.getMessage());
         }
         */
 
     }
 
 
 }
