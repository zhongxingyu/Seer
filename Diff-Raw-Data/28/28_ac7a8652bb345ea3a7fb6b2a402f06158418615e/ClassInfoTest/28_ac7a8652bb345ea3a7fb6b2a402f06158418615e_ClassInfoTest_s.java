/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
 package com.vonhof.babelshark.reflect;
 
 import com.vonhof.babelshark.reflect.MethodInfo.Parameter;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import junit.framework.TestCase;
 
 /**
  *
 * @author Henrik Hofmeister <hh@cphse.com>
  */
 public class ClassInfoTest extends TestCase {
     
     private final ClassInfo classInfo = ClassInfo.from(CrazyBean.class);
     
     public ClassInfoTest(String testName) {
         super(testName);
     }
     
     public void test_can_read_generic_list() throws Exception {
         assertEquals(true,classInfo.isBean());
         
         FieldInfo field = classInfo.getField("someList");
         assertNotNull("Can find field",field);
         
         assertEquals("Can find generic type",String.class,field.getType().getGenericTypes()[0]);
     }
     
     public void test_can_read_generic_map() throws Exception {
         
         FieldInfo field = classInfo.getField("someMap");
         assertNotNull("Can find field",field);
         
         assertEquals("Can find generic map key",String.class,field.getType().getGenericTypes()[0]);
         
         assertEquals("Can find generic map value",Integer.class,field.getType().getGenericTypes()[1]);
     }
     
     public void test_can_read_generic_parm() throws Exception {
         MethodInfo method = classInfo.getMethodByClassParms("setSomeList", List.class);
         assertNotNull("Can find method",method);
         assertEquals("Can find parm",1,method.getParameters().size());
         Parameter p = method.getParameter(0);
         assertEquals("Can find parm generic type",String.class,p.getType().getGenericTypes()[0]);
     }
     
     public void test_can_read_parm_names() throws Exception {
         MethodInfo method = classInfo.getMethodByClassParms("setSomeList", List.class,boolean.class);
         assertNotNull("Can find method",method);
         assertEquals("Can find parms",2,method.getParameters().size());
         assertEquals("Can find parm name","someList",method.getParameter(0).getName());
         assertEquals("Can find parm name","other",method.getParameter(1).getName());
     }
     
     public void test_can_read_generic_return_type() throws Exception {
         MethodInfo method = classInfo.getMethodByClassParms("getSomeList");
         assertNotNull("Can find method",method);
         assertEquals("Can find generic return type",List.class,method.getReturnType().getType());
         assertEquals("Can find generic return type class",String.class,method.getReturnType().getGenericTypes()[0]);
     }
     
     public void test_can_read_array_return_type() throws Exception {
         MethodInfo method = classInfo.getMethodByClassParms("getStringArray");
         assertNotNull("Can find method",method);
         assertTrue("Can find array return type",method.getReturnType().isArray());
         assertEquals("Can find array return type class",String.class,method.getReturnType().getComponentType());
     }
 
     public static class CrazyBean {
         private List<String> someList = new ArrayList<String>();
         private Map<String,Integer> someMap = new HashMap<String, Integer>();
         
         private int[] intArray = {1,2,3,4,5};
         private String[] stringArray = {"sdf","sdgfhtd"};
         
         private String someHiddenField = "test";
 
         public int[] getIntArray() {
             return intArray;
         }
 
         public void setIntArray(int[] intArray) {
             this.intArray = intArray;
         }
 
         public String getSomeHiddenField() {
             return someHiddenField;
         }
 
         public void setSomeHiddenField(String someHiddenField) {
             this.someHiddenField = someHiddenField;
         }
 
         public List<String> getSomeList() {
             return someList;
         }
         
         public List<String> getSomeList(boolean some) {
             return someList;
         }
 
         public void setSomeList(List<String> someList,boolean other) {
             this.someList = someList;
         }
         
         public void setSomeList(List<String> someList) {
             this.someList = someList;
         }
 
         public Map<String, Integer> getSomeMap() {
             return someMap;
         }
 
         public void setSomeMap(Map<String, Integer> someMap) {
             this.someMap = someMap;
         }
 
         public String[] getStringArray() {
             return stringArray;
         }
 
     }
 }
