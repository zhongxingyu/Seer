 package com.feedly.cassandra.entity.enhance;
 
 import static org.junit.Assert.*;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.junit.Test;
 
 
 public class EnhancerTest
 {
     @Test
     public void testEnhancer()
     {
         SampleBean bean = new SampleBean();
         assertTrue(bean instanceof IEnhancedEntity); //enhancer ant task runs as part of process-test-classes maven phase...
 
         IEnhancedEntity enh = (IEnhancedEntity) bean;
         assertNotNull(enh.getModifiedFields());
         
         assertTrue(enh.getModifiedFields().isEmpty()); //nothing should have been set
 
         /*
          * the bitset hold fields that have been modified (setter has been invoked), the order is sorted field name
          * 
          * 0 boolVal
          * 1 charVal
          * 2 dateVal
          * 3 doubleVal
          * 4 floatVal
          * 5 intVal
          * 6 longVal
         * 7 sampleEnum
         * 8 strVal
          * 
          * also not saved val is not saved
          */
         
         bean.setNotSaved(12);
         assertTrue(enh.getModifiedFields().isEmpty());
 
         bean.setIntVal(7);
         assertEquals(1, enh.getModifiedFields().cardinality()); 
         assertEquals(true, enh.getModifiedFields().get(5)); 
         assertEquals(7, bean.getIntVal()); //make sure the modified setter still sets the property value
         
         bean.setStrVal(null);
         assertEquals(2, enh.getModifiedFields().cardinality()); 
         assertEquals(true, enh.getModifiedFields().get(5)); 
        assertEquals(true, enh.getModifiedFields().get(8)); 
         assertNull(bean.getStrVal());
         
         bean.setStrVal("bar");
         assertEquals(2, enh.getModifiedFields().cardinality()); 
         assertEquals(true, enh.getModifiedFields().get(5)); 
        assertEquals(true, enh.getModifiedFields().get(8)); 
         assertEquals("bar", bean.getStrVal());
 
         //test anonymous handlers
         bean = new SampleBean();
         enh = (IEnhancedEntity) bean;
         Map<String, Object> map = new HashMap<String, Object>();
         bean.setUnmapped(map);
         assertTrue(enh.getUnmappedFieldsModified());
         enh.setUnmappedFieldsModified(false);
 
         assertTrue(map == bean.getUnmapped());
         assertTrue(enh.getUnmappedFieldsModified());
         
         //test collections - dirty field should work on getters AND setters
         
         MapBean mapBean = new MapBean();
         enh = (IEnhancedEntity) mapBean;
         map = new HashMap<String, Object>();
         mapBean.setMapProp(map);
         assertEquals(enh.getModifiedFields().toString(), 1, enh.getModifiedFields().cardinality());
         assertTrue(enh.getModifiedFields().get(0));
         enh.getModifiedFields().clear();
 
         assertTrue(map == mapBean.getMapProp());
         assertEquals(1, enh.getModifiedFields().cardinality());
         assertTrue(enh.getModifiedFields().get(0));
         
     }
 }
