 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.vonhof.babelshark.node;
 
 import com.vonhof.babelshark.reflect.ClassInfo;
 import com.vonhof.babelshark.reflect.FieldInfo;
 import java.util.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import junit.framework.TestCase;
 
 /**
  *
  * @author Henrik Hofmeister <@vonhofdk>
  */
 public class SharkTypeTest extends TestCase {
     
     public SharkTypeTest(String testName) {
         super(testName);
     }
     
     @Override
     protected void setUp() throws Exception {
         super.setUp();
     }
     
     @Override
     protected void tearDown() throws Exception {
         super.tearDown();
     }
     
     final Map<String,Map<String,Map<String,Map<String,Boolean>>>> map = new HashMap<String, Map<String, Map<String, Map<String, Boolean>>>>();
     final List<List<List<List<Boolean>>>> list = new ArrayList<List<List<List<Boolean>>>>();
     
     final List<Map<String,List<Collection<Boolean[]>>>> mixed = new ArrayList<Map<String, List<Collection<Boolean[]>>>>();
     
     
     private FieldInfo getField(String name) {
         try {
             return new FieldInfo(getClass().getDeclaredField(name));
         } catch (NoSuchFieldException ex) {
             Logger.getLogger(SharkTypeTest.class.getName()).log(Level.SEVERE, null, ex);
         } catch (SecurityException ex) {
             Logger.getLogger(SharkTypeTest.class.getName()).log(Level.SEVERE, null, ex);
         }
         return null;
     }
     
     public void testDeeplyNestedMaps() {
         SharkType type = SharkType.get(getField("map"));
         
         assertEquals("Root",Map.class,type.getType());
         assertEquals("Level 1",Map.class,type.getValueType().getType());
         assertEquals("Level 2",Map.class,type.getValueType().getValueType().getType());
         assertEquals("Level 3",Map.class,type.getValueType().getValueType().getValueType().getType());
         assertEquals("Actual value",Boolean.class,type.getValueType().getValueType().getValueType().getValueType().getType());
         
     }
     
     public void testDeeplyNestedLists() {
         SharkType type = SharkType.get(getField("list"));
         
         assertEquals("Root",List.class,type.getType());
         assertEquals("Level 1",List.class,type.getValueType().getType());
         assertEquals("Level 2",List.class,type.getValueType().getValueType().getType());
         assertEquals("Level 3",List.class,type.getValueType().getValueType().getValueType().getType());
         assertEquals("Actual value",Boolean.class,type.getValueType().getValueType().getValueType().getValueType().getType());
     }
     
     public void testDeeplyNestedMixed() {
         SharkType type = SharkType.get(getField("mixed"));
         
         assertEquals("Root",List.class,type.getType());
         assertEquals("Level 1",Map.class,type.getValueType().getType());
         assertEquals("Level 2",List.class,type.getValueType().getValueType().getType());
         assertEquals("Level 3",Collection.class,type.getValueType().getValueType().getValueType().getType());
        assertEquals("Actual value",Boolean.class,type.getValueType().getValueType().getValueType().getValueType().getType());
         assertTrue("Actual value is array",type.getValueType().getValueType().getValueType().getValueType().isArray());
     }
 
 }
