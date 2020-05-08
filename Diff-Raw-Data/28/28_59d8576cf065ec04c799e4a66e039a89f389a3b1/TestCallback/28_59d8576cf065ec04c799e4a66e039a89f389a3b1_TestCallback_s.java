 package org.libj.xquery;
 
 import org.junit.Test;
 import org.libj.xquery.runtime.FlattenList;
 import org.libj.xquery.runtime.RecursiveList;
 
 import static org.junit.Assert.assertEquals;
 import static org.libj.xquery.Asserts.*;
 import static org.libj.xquery.Compile.compile;
 import static org.libj.xquery.Compile.eval;
 
 public class TestCallback {
     public void assertCallback(String script, String expected) {
         XQuery q = compile(script);
         FlattenList list = new FlattenList();
         q.eval(list);
         assertEquals(script, expected, list.toString());
     }
 
     @Test
     public void testCallback() {
         assertCallback("1", "1");
         assertCallback("<x/>", "<x/>");
         assertCallback("for $i in (1 to 3) return $i + 3", "4 5 6");
         assertCallback("let $i := 3 where $i != 3 return $i", "");
     }
 
     @Test
     public void assertRawCallback() {
         Callback callback = new Callback() {
             public void call(Object result) {
                 //
             }
         };
         compile("1").eval(callback);
         compile("for $i in 1 to 1 return $i").eval(callback);
     }
 
 }
