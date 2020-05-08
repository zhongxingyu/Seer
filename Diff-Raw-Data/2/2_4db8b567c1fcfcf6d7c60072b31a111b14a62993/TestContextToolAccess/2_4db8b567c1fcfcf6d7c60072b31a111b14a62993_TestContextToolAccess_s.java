 
 package org.webmacro.template;
 
 import org.webmacro.Context;
 
 import java.util.Hashtable;
 import java.util.Map;
 
 /**
  * @author Marc Palmer (<a href="mailto:wj5@wangjammers.org">wj5@wangjammers.org</a>)
  */
 public class TestContextToolAccess extends TemplateTestCase
 {
 
     public TestContextToolAccess( String name )
     {
         super( name );
     }
 
     public void testContextToolMethodCall()
     {
        assertStringTemplateEquals("$Text.HTMLEncode('&amp;')", "&");
     }
 
     protected void stuffContext( Context context ) throws Exception
     {
     }
 }
