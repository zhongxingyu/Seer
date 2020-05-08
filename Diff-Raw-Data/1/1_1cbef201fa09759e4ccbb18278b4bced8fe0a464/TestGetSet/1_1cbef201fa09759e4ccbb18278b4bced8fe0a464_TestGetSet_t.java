 package org.webmacro.template;
 
 import org.webmacro.Context;
 import org.webmacro.PropertyException;
 import org.webmacro.engine.DefaultEvaluationExceptionHandler;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class TestGetSet extends TemplateTestCase
 {
 
     public static class TestObject
     {
         public static int intField;
         public static long longField;
         private Map map = new HashMap();
         Object obj = null;
         Object[] objArray = new Object[]{"one", "two"};
 
         private static int intValue;
 
 
         public static void setInt (int i)
         {
             intValue = i;
         }
 
 
         public static int getInt ()
         {
             return intValue;
         }
 
 
         private static long longValue;
 
 
         public static void setLong (long i)
         {
             longValue = i;
         }
 
 
         public static long getLong ()
         {
             return longValue;
         }
 
 
         public void put (String key, Object value)
         {
             map.put(key, value);
         }
 
 
         public Object get (String key)
         {
             return map.get(key);
         }
 
 
         public void setObjectValue (Object value)
         {
             this.obj = value;
         }
 
 
         public Object[] getArray ()
         {
             return objArray;
         }
 
 
         private java.util.HashMap myProps = new java.util.HashMap();
 
 
         public Object getMyProp (String name)
         {
             return myProps.get(name);
         }
 
 
         public void setMyProp (String name, Object val)
         {
             myProps.put(name, val);
         }
 
 
         public java.util.Map getProps ()
         {
             return myProps;
         }
     
         private boolean bool = false;
     
         public void setBool(boolean val){
             bool = val;
         }
         
     }
 
     public static TestObject to = new TestObject(), to2 = new TestObject();
 
 
     public TestGetSet (String name)
     {
         super(name);
     }
 
 
     public void stuffContext (Context context) throws Exception
     {
         context.setEvaluationExceptionHandler(
                 new DefaultEvaluationExceptionHandler());
 
         context.put("TestObject", to);
         context.put("two", (int) 2);
         context.put("twoLong", (long) 2);
 
         to2.intField = 1;
         to2.put("Value", new TestObject());
         context.put("TestObject2", to2);
     }
 
 
     public void test1 () throws Exception
     {
         to.intField = 1;
         assertStringTemplateEquals("$TestObject.intField", "1");
         assertStringTemplateEquals("#set $TestObject.intField=2", "");
         assertTrue(to.intField == 2);
 
         to.intField = 1;
         assertStringTemplateEquals("#set $TestObject.intField=$two", "");
         assertTrue(to.intField == 2);
 
         to.longField = 1;
         assertStringTemplateEquals("$TestObject.longField", "1");
         assertStringTemplateEquals("#set $TestObject.longField=2", "");
         assertTrue(to.longField == 2);
 
         to.longField = 1;
         assertStringTemplateEquals("$TestObject.longField", "1");
         assertStringTemplateEquals("#set $TestObject.longField=$twoLong", "");
         assertTrue(to.longField == 2);
 
         to.setInt(1);
         assertStringTemplateEquals("$TestObject.Int", "1");
         assertStringTemplateEquals("#set $TestObject.Int=2", "");
         assertTrue(to.getInt() == 2);
 
         to.setInt(1);
         assertStringTemplateEquals("$TestObject.Int", "1");
         assertStringTemplateEquals("#set $TestObject.Int=$two", "");
         assertTrue(to.getInt() == 2);
     }
 
 
     /** Using WM shorthand syntax, get an object from
      * a map and call a method on it
      */
     public void test2 () throws Exception
     {
         String tmpl = "#if($TestObject2.Value.getInt() > 0){pass}#else{fail}";
         assertStringTemplateEquals(tmpl, "pass");
     }
 
 
     /** pass a null as a parameter to a method */
     public void testPassANull1 () throws Exception
     {
         String tmpl = "$TestObject2.setObjectValue(null)";
         assertStringTemplateEquals(tmpl, "");
     }
 
 
     /** same as test3, but use the null via a variable reference */
     public void testPassANull2 () throws Exception
     {
         assertStringTemplateEquals("#set $foo = null", "");
         assertBooleanExpr("$foo == null", true);
         to2.obj = new Object();
         assertStringTemplateEquals("$TestObject2.setObjectValue($foo)", "");
         assertTrue(to2.obj == null);
     }
 
 
     /** test the binary accessor/mutator syntax */
     public void testBinaryMutator () throws Exception
     {
         String tmpl = "#set $TestObject.ObjectValue='foo'";
         assertStringTemplateEquals(tmpl, "");
         assertTrue(to.obj.equals("foo"));
 
         tmpl = "#set $TestObject.MyProp.Foo='Bar'\n$TestObject.MyProp.Foo";
         assertStringTemplateEquals(tmpl, "Bar");
 
         tmpl = "#set $TestObject.Props.MyProp=123\n$TestObject.Props.MyProp";
         assertStringTemplateEquals(tmpl, "123");
     }
 
    public void testBinarySetBoolean() throws Exception {
       String tmpl = "#set $TestObject.Bool=true";
      assertStringTemplateEquals(tmpl, "");
       assertTrue(to.bool);
    }
     /** call the ".length" field of an array */
     public void testDotLengthOnArray ()
     {
         String tmpl = "$TestObject.getArray().length";
         assertStringTemplateEquals(tmpl, "2");
     }
 
     /** Make sure we are not evaluating stuff that doesn't exist */
     public void testNonExistentProperties () throws Exception
     {
         String tmpl = "$TestObject.Int.Int.Int.Int";
         assertStringTemplateThrows( tmpl, PropertyException.class );
     }
 }
