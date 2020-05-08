 /**
  * 
  */
 package org.gethydrated.hydra.test.config;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import org.gethydrated.hydra.api.configuration.ConfigItemNotFoundException;
 import org.gethydrated.hydra.api.configuration.ConfigItemTypeException;
 import org.gethydrated.hydra.api.configuration.Configuration;
 import org.gethydrated.hydra.api.configuration.ConfigurationItem;
 import org.gethydrated.hydra.config.ConfigurationImpl;
 import org.gethydrated.hydra.config.tree.ConfigList;
 import org.gethydrated.hydra.config.tree.ConfigValue;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * @author Hanno Sternberg
  * @since 0.1.0
  * 
  */
 public class ConfigurationTest {
 
     /**
      * @var Test configuration.
      */
     private ConfigurationImpl cfg;
 
     /**
      * @throws java.lang.Exception .
      */
     @Before
     public final void setUp() throws Exception {
         cfg = new ConfigurationImpl();
     }
 
     /**
      * @throws java.lang.Exception .
      */
     @After
     public final void tearDown() throws Exception {
 
     }
 
     /**
      * Test method for
      * {@link org.gethydrated.hydra.config.ConfigurationImpl#set(java.lang.String, java.lang.Object)}
      * .
      */
     @Test
     public final void testSetValue() {
         try {
             cfg.set("Name", "test");
             assertEquals(cfg.getRoot().getChildren().size(), 1);
         } catch (Exception e) {
             fail("Got Exception");
         }
 
     }
 
     /**
      * Test method for
      * {@link org.gethydrated.hydra.config.ConfigurationImpl#set(java.lang.String, java.lang.Object)}
      * .
      */
     @Test
     public final void testSetSubValue() {
         ConfigurationItem child = null;
         cfg.set("Network.Port", 1337);
         cfg.set("Network.Host", "local");
         try {
             assertEquals(cfg.getRoot().getChildren().size(), 1);
             child = ((ConfigList) cfg.getRoot()).getChild("Network");
         } catch (ConfigItemNotFoundException e) {
             fail("SubList not created!");
         } catch (ConfigItemTypeException e) {
             fail("Got type exception");
         }
         assertNotNull(child);
         assertTrue(child.hasChildren());
         assertEquals(((ConfigList) child).getChildren().size(), 2);
         try {
             assertEquals(
                     ((ConfigValue<?>) ((ConfigList) child).getChild("Port"))
                             .value(),
                     1337);
             assertEquals(
                     ((ConfigValue<?>) ((ConfigList) child).getChild("Host"))
                             .value(),
                     "local");
         } catch (ConfigItemNotFoundException e) {
             fail("SubItem not created.");
         }
 
     }
 
     /**
      * Test method for
      * {@link org.gethydrated.hydra.config.ConfigurationImpl#list(java.lang.String)}
      * .
      */
     @Test
     public final void testList() {
         cfg.set("Name", "test");
         cfg.set("Network.Port", 1337);
         cfg.set("Network.Host", "local");
 
         assertEquals("[Name, Network]", cfg.list("").toString());
         assertEquals("[Port, Host]", cfg.list("Network").toString());
     }
 
     /**
      * Test method for
      * {@link org.gethydrated.hydra.config.ConfigurationImpl#get(java.lang.String)}
      * . .
      */
     @Test
     public final void testGet() {
         cfg.set("Name", "test");
         cfg.set("Network.Port", 1337);
         cfg.set("Network.Host", "local");
 
         try {
             assertEquals(cfg.get("Name"), "test");
             assertEquals(cfg.get("Network.Host"), "local");
             assertEquals(cfg.get("Network.Port"), 1337);
         } catch (ConfigItemNotFoundException e) {
             fail("Item nor found");
         }
     }
 
     /**
      * Test method for
      * {@link org.gethydrated.hydra.config.ConfigurationImpl#setBoolean(java.lang.String, java.lang.Boolean)}
      * .
      */
     @SuppressWarnings("unchecked")
     @Test
     public final void testSetBoolean() {
         cfg.setBoolean("Active", true);
         try {
             assertTrue(((ConfigValue<Boolean>) ((ConfigList) cfg.getRoot()).getChild("Active"))
                     .value());
         } catch (ConfigItemNotFoundException e) {
             fail("Value not set");
         }
     }
 
     /**
      * Test method for
      * {@link org.gethydrated.hydra.config.ConfigurationImpl#getBoolean(java.lang.String)}
      * .
      */
     @Test
     public final void testGetBoolean() {
         cfg.setBoolean("Active", true);
         try {
             assertTrue(cfg.getBoolean("Active"));
         } catch (ConfigItemNotFoundException e) {
             fail("Value not set");
         }
     }
 
     /**
      * Test method for
      * {@link org.gethydrated.hydra.config.ConfigurationImpl#setInteger(java.lang.String, java.lang.Integer)}
      * .
      */
     @SuppressWarnings("unchecked")
     @Test
     public final void testSetInteger() {
         cfg.setInteger("A_Number", 1337);
         try {
             assertEquals(
                     ((ConfigValue<Integer>) ((ConfigList) cfg.getRoot()).getChild("A_Number"))
                             .value(),
                     (Integer) 1337);
         } catch (ConfigItemNotFoundException e) {
             fail("Value not set");
         }
     }
 
     /**
      * Test method for
      * {@link org.gethydrated.hydra.config.ConfigurationImpl#getInteger(java.lang.String)}
      * .
      */
     @Test
     public final void testGetInteger() {
         cfg.setInteger("A_Number", 1337);
         try {
             assertEquals(cfg.getInteger("A_Number"), (Integer) 1337);
         } catch (ConfigItemNotFoundException e) {
             fail("Value not set");
         }
     }
 
     /**
      * Test method for
      * {@link org.gethydrated.hydra.config.ConfigurationImpl#setFloat(java.lang.String, java.lang.Double)}
      * .
      */
     @SuppressWarnings("unchecked")
     @Test
     public final void testSetFloat() {
         cfg.setFloat("A_Double", 13.37);
         try {
             assertEquals(
                     ((ConfigValue<Double>) ((ConfigList) cfg.getRoot()).getChild("A_Double"))
                             .value(),
                     (Double) 13.37);
         } catch (ConfigItemNotFoundException e) {
             fail("Value not set");
         }
     }
 
     /**
      * Test method for
      * {@link org.gethydrated.hydra.config.ConfigurationImpl#getFloat(java.lang.String)}
      * .
      */
     @SuppressWarnings("unchecked")
     @Test
     public final void testGetFloat() {
         cfg.setFloat("A_Double", 13.37);
         try {
             assertEquals(
                     ((ConfigValue<Double>) ((ConfigList) cfg.getRoot()).getChild("A_Double"))
                             .value(),
                     (Double) 13.37);
         } catch (ConfigItemNotFoundException e) {
             fail("Value not set");
         }
     }
 
     /**
      * Test method for
      * {@link org.gethydrated.hydra.config.ConfigurationImpl#setString(java.lang.String, java.lang.String)}
      * .
      */
     @SuppressWarnings("unchecked")
     @Test
     public final void testSetString() {
         cfg.setString("A_String", "foobar");
         try {
             assertEquals(
                     ((ConfigValue<String>) ((ConfigList) cfg.getRoot()).getChild("A_String"))
                             .value(),
                     "foobar");
         } catch (ConfigItemNotFoundException e) {
             fail("Value not set");
         }
     }
 
     /**
      * Test method for
      * {@link org.gethydrated.hydra.config.ConfigurationImpl#getString(java.lang.String)}
      * .
      */
     @Test
     public final void testGetString() {
         cfg.setString("A_String", "foobar");
         try {
             assertEquals(cfg.getString("A_String"), "foobar");
         } catch (ConfigItemNotFoundException e) {
             fail("Value not set");
         }
     }
 
     /**
      * Test method for copy.
      */
     @Test
     public final void testCopy() {
         ConfigurationImpl cp = cfg.copy();
         assertFalse(cp == cfg);
         assertEquals(cfg, cp);
     }
 
     @Test
     public final void testGetSubItem() {
         cfg.set("Name", "test");
         cfg.set("Network.Port", 1337);
         cfg.set("Network.Host", "local");
         try {
             Configuration sub = cfg.getSubItems("Network");
             assertEquals("[Port, Host]", sub.list("").toString());
         } catch (ConfigItemTypeException e) {
             fail("Type error");
         } catch (ConfigItemNotFoundException e) {
             fail("Item not found");
         }
 
     }
 
 }
