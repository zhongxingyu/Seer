 package org.rulez.magwas.test;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.fail;
 
 import java.io.IOException;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.hibernate.Session;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.rulez.magwas.worldmodel.BaseObject;
 import org.rulez.magwas.worldmodel.BasicPlugin;
 import org.rulez.magwas.worldmodel.InputParseException;
 import org.rulez.magwas.worldmodel.Util;
 import org.xml.sax.SAXException;
 
 public class BasicPluginTest {
     
     private static BasicPlugin plugin;
     private static BaseObject  thing;
     private static Session     session;
     
     @BeforeClass
     public static void setUp() {
         session = Util.getSession();
         plugin = new BasicPlugin();
         try {
             plugin.init(session);
             thing = BaseObject.getBaseObjectByCompositeId("thing", session);
             plugin.finalizeObject(session, thing);
         } catch (Exception e) {
             e.printStackTrace();
            session.close();
             fail();
         }
     }
     
     @AfterClass
     public static void tearDown() {
         session.close();
     }
     
     @Test
     public void testName() {
         assertEquals("org.rulez.magwas.worldmodel.BasicPlugin",
                 plugin.getPluginName());
     }
     
     @Test
     public void testDoubleTest() throws InputParseException, SAXException,
             IOException, ParserConfigurationException {
         BaseObject bo = BaseObject
                 .fromString(
                         "<BaseObject id=\"doubleBasicFinalize\" type =\"thing\" source=\"thing\"/>",
                         session);
         session.save(bo);
         plugin.finalizeObject(session, bo);
         plugin.checkConsistency(session, bo);
         plugin.finalizeObject(session, bo);
         plugin.checkConsistency(session, bo);
     }
     
     @Test
     public void testThing() {
         assertNotNull(thing);
         assertEquals("thing", thing.getCompositeId());
         assertNull(thing.getDest());
         assertEquals(thing, thing.getType());
         assertNull(thing.getSource());
         assertNull(thing.getValue());
         
     }
     
     @Test
     public void testRevFields() throws Exception {
         
         BaseObject bo = BaseObject
                 .fromString(
                         "<BaseObject id=\"revSource\" type =\"thing\" source=\"thing\"/>",
                         session);
         session.save(bo);
         plugin.finalizeObject(session, bo);
         TestUtil.assertExpressionOnObject(
                 "//BaseObject/revsources/revsource = 'revSource'", thing);
         TestUtil.assertExpressionOnObject("count(//BaseObject/revsources) = 1",
                 thing);
         BaseObject bo2 = BaseObject
                 .fromString(
                         "<BaseObject id=\"revType\" type=\"thing\" source=\"revSource\"/>",
                         session);
         session.save(bo2);
         plugin.finalizeObject(session, bo2);
         TestUtil.assertExpressionOnObject(
                 "//BaseObject/revtypes/revtype = 'revType'", thing);
         TestUtil.assertExpressionOnObject("count(//BaseObject/revtypes) = 1",
                 thing);
         BaseObject bo3 = BaseObject
                 .fromString(
                         "<BaseObject id=\"revDest\" type =\"thing\" source=\"revType\" dest=\"thing\"/>",
                         session);
         session.save(bo3);
         plugin.finalizeObject(session, bo3);
         TestUtil.assertExpressionOnObject(
                 "//BaseObject/revdests/revdest = 'revDest'", thing);
         TestUtil.assertExpressionOnObject("count(//BaseObject/revdests) = 1",
                 thing);
         
     }
 }
