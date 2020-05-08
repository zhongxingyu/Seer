 /*
  * SafeOnline project.
  *
  * Copyright 2006-2007 Lin.k N.V. All rights reserved.
  * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
  */
 
 package test.unit.net.link.util.test;
 
 import junit.framework.TestCase;
import net.link.util.j2ee.JNDIUtils;
 import net.link.util.test.j2ee.JNDITestUtils;
 
 import java.util.List;
 import java.util.Map;
 
 
 public class EJBUtilsTest extends TestCase {
 
     private JNDITestUtils jndiTestUtils;
 
     @Override
     protected void setUp()
             throws Exception {
 
         super.setUp();
 
         jndiTestUtils = new JNDITestUtils();
         jndiTestUtils.setUp();
     }
 
     @Override
     protected void tearDown()
             throws Exception {
 
         jndiTestUtils.tearDown();
         super.tearDown();
     }
 
     public void testGetComponentNamesReturnsEmptyMap()
             throws Exception {
 
         // Setup Data
         String jndiPrefix = "test/prefix/" + getName();
 
         // Test
        Map<String, TestType> result = JNDIUtils.getComponentNames(jndiPrefix, TestType.class);
 
         // Verify
         assertNotNull(result);
         assertTrue(result.isEmpty());
     }
 
     public void testGetComponentNamesReturnsObject()
             throws Exception {
 
         // Setup Data
         String jndiPrefix = "test/prefix/" + getName();
         TestType testObject = new TestType();
         String objectName = "test-object-name";
         jndiTestUtils.bindComponent(jndiPrefix + "/" + objectName, testObject);
 
         // Test
        Map<String, TestType> result = JNDIUtils.getComponentNames(jndiPrefix, TestType.class);
 
         // Verify
         assertEquals(1, result.size());
         assertEquals(testObject, result.get(objectName));
     }
 
     public void testGetComponentsIsEmpty()
             throws Exception {
 
         // Setup Data
         String jndiPrefix = "test/prefix/" + getName();
 
         // Test
        List<TestType> result = JNDIUtils.getComponents(jndiPrefix, TestType.class);
 
         // Verify
         assertNotNull(result);
         assertTrue(result.isEmpty());
     }
 
     public void testGetComponentsReturnsObject()
             throws Exception {
 
         // Setup Data
         String jndiPrefix = "test/prefix/" + getName();
         TestType testObject = new TestType();
         String objectName = "test-object-name";
         jndiTestUtils.bindComponent(jndiPrefix + "/" + objectName, testObject);
 
         // Test
        List<TestType> result = JNDIUtils.getComponents(jndiPrefix, TestType.class);
 
         // Verify
         assertEquals(1, result.size());
         assertEquals(testObject, result.get(0));
     }
 
     static class TestType {
 
     }
 }
