 package com.vonhof.babelshark.impl;
 
 import com.vonhof.babelshark.MappedBean;
 import com.vonhof.babelshark.MappedBean.ObjectField;
 import com.vonhof.babelshark.annotation.Ignore;
 import com.vonhof.babelshark.annotation.Name;
 import com.vonhof.babelshark.node.SharkType;
 import com.vonhof.babelshark.reflect.ClassInfo;
 import com.vonhof.babelshark.reflect.FieldInfo;
 import com.vonhof.babelshark.reflect.MethodInfo;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import junit.framework.TestCase;
 
 /**
  *
 * @author Henrik Hofmeister <@vonhofdk>
  */
 public class DefaultBeanMapperTest extends TestCase {
     
     public DefaultBeanMapperTest(String testName) {
         super(testName);
     }
     
     private final ClassInfo type = ClassInfo.from(TestClass.class);
     private final DefaultBeanMapper instance = new DefaultBeanMapper();
     
     private MappedBean expResult;
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         expResult = new MappedBean(type.getType());
         expResult.addField("_id", type.getField("id"),type.getMethod("getId"), type.getMethodByClassParms("setId",String.class));
         expResult.addField("name", type.getField("name"),type.getMethod("getName"), type.getMethodByClassParms("setName",String.class));
         expResult.addField("subs", type.getField("children"),null,null);
         expResult.addField("fields", type.getField("fields"),type.getMethod("getFields"), type.getMethodByClassParms("setFields",Map.class));        
     }
 
     public void testIgnoreField() throws Exception {
         FieldInfo ignoredField = type.getField("ignoredField");
         boolean result = instance.ignoreField(ignoredField);
         assertEquals(true, result);
     }
     
     public void testCanGetFieldName() throws Exception {
         FieldInfo nameField = type.getField("name");
         String result = instance.getFieldName(nameField);
         assertEquals("name", result);
     }
     
     public void testCanOverrideFieldName() throws Exception {
         FieldInfo idField = type.getField("id");
         String result = instance.getFieldName(idField);
         assertEquals("_id", result);
     }
     
     public void testCanGetListType() throws Exception {
         MappedBean map = instance.getMap(type);
         ObjectField subsField = map.getField("subs");
         assertEquals(SharkType.forCollection(List.class,TestClass.class),subsField.getType());
     }
     
     public void testCanGetMapType() throws Exception {
         MappedBean map = instance.getMap(type);
         ObjectField subsField = map.getField("fields");
         assertEquals(SharkType.forMap(Map.class,TestClass.class),subsField.getType());
     }
 
     public void testCanGetBeanGetter() throws Exception {
         FieldInfo idField = type.getField("id");
         MethodInfo expGetter = type.getMethod("getId");
         MethodInfo getter = instance.getGetter(type,idField);
         assertEquals(expGetter, getter);
     }
     
     public void testCanGetPublicGetter() throws Exception {
         FieldInfo childrenField = type.getField("children");
         MethodInfo getter = instance.getGetter(type, childrenField);
         assertNull(getter);
     }
     
     public void testCanGetBeanSetter() throws Exception {
         FieldInfo idField = type.getField("id");
         MethodInfo expSetter = type.getMethodByClassParms("setId",String.class);
         MethodInfo setter = instance.getSetter(type, idField);
         assertEquals(expSetter, setter);
     }
     
     public void testCanGetPublicSetter() throws Exception {
         FieldInfo childrenField = type.getField("children");
         MethodInfo setter = instance.getSetter(type, childrenField);
         assertNull(setter);
     }
     
     public void testMapping() throws Exception {
         MappedBean result = instance.getMap(type.getType());
         assertEquals(expResult, result);
     }
 
 
     
     @Name("test")
     public static class TestClass {
         private String name;
         
         @Name("_id")
         private String id;
         
         @Ignore
         private String ignoredField = "ignored value";
         
         @Name(value="subs")
         public List<TestClass> children = new ArrayList<DefaultBeanMapperTest.TestClass>();
         
         private Map<String,TestClass> fields = new HashMap<String, TestClass>();
 
         public TestClass() {
             children.add(new TestClass("test name", "testId"));
         }
 
         public TestClass(String name, String id) {
             this.name = name;
             this.id = id;
         }
 
 
         public String getId() {
             return id;
         }
 
         public void setId(String id) {
             this.id = id;
         }
 
         public String getIgnoredField() {
             return ignoredField;
         }
 
         public void setIgnoredField(String ignoredField) {
             this.ignoredField = ignoredField;
         }
 
         public String getName() {
             return name;
         }
 
         public void setName(String name) {
             this.name = name;
         }
 
         public Map<String, TestClass> getFields() {
             return fields;
         }
 
         public void setFields(Map<String, TestClass> fields) {
             this.fields = fields;
         }
     }
 }
