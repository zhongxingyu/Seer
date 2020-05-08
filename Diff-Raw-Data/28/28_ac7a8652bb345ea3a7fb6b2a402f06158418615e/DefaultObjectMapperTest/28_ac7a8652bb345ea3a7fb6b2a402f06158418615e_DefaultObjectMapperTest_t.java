 package com.vonhof.babelshark.impl;
 
 import com.vonhof.babelshark.annotation.Ignore;
 import com.vonhof.babelshark.annotation.Name;
 import com.vonhof.babelshark.node.ObjectNode;
 import com.vonhof.babelshark.node.SharkNode;
 import java.util.ArrayList;
 import java.util.List;
 import junit.framework.TestCase;
 
 /**
  *
 * @author Henrik Hofmeister <@vonhofdk>
  */
 public class DefaultObjectMapperTest extends TestCase {
     private final Class<TestClass> type = TestClass.class;
     private final DefaultNodeMapper instance = new DefaultNodeMapper();
     private final TestClass testObject = new TestClass(true);
     private ObjectNode testNode;
     
     public DefaultObjectMapperTest(String testName) {
         super(testName);
     }
     
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         testNode = new ObjectNode();
         testNode.put("name","test name");
         testNode.put("_id","testId");
         ObjectNode sub = testNode.putArray("subs").addObject();
         sub.put("name","test name 2");
         sub.put("_id","test2");
         sub.putArray("subs");
         
     }
     
     
     public void testNodeToObject() throws Exception {
         TestClass result = instance.readAs(testNode, type);
         assertEquals(testObject, result);
     }
     
     public void testObjectToNode() throws Exception {
         SharkNode result = instance.toNode(testObject);
         assertEquals(testNode, result);
     }
     
     
     @Name("test")
     public static class TestClass {
         private String name = "test name";
         
         @Name("_id")
         private String id = "testId";
         
         @Ignore
         private String ignoredField = "ignored value";
         
         private String test = "not available";
         
         @Name(value="subs")
         public List<TestClass> children = new ArrayList<TestClass>();
 
         public TestClass() {
             this(false);
         }
         public TestClass(boolean fillOut) {
             if (fillOut)
                 children.add(new TestClass("test name 2", "test2"));
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
 
         @Override
         public boolean equals(Object obj) {
             if (obj == null) {
                 return false;
             }
             if (getClass() != obj.getClass()) {
                 return false;
             }
             final TestClass other = (TestClass) obj;
             if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
                 return false;
             }
             if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
                 return false;
             }
             if ((this.ignoredField == null) ? (other.ignoredField != null) : !this.ignoredField.equals(other.ignoredField)) {
                 return false;
             }
             if (this.children != other.children && (this.children == null || !this.children.equals(other.children))) {
                 return false;
             }
             return true;
         }
 
         @Override
         public int hashCode() {
             int hash = 5;
             hash = 67 * hash + (this.name != null ? this.name.hashCode() : 0);
             hash = 67 * hash + (this.id != null ? this.id.hashCode() : 0);
             hash = 67 * hash + (this.ignoredField != null ? this.ignoredField.hashCode() : 0);
             hash = 67 * hash + (this.children != null ? this.children.hashCode() : 0);
             return hash;
         }
     }
 }
