 package com.atlassian.plugin.osgi.factory.descriptor;
 
 import junit.framework.TestCase;
 import org.dom4j.DocumentHelper;
 import org.dom4j.Element;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 import static org.mockito.Matchers.anyObject;
 import com.atlassian.plugin.Plugin;
 
 public class TestComponentModuleDescriptor extends TestCase
 {
     public void testEnableDoesNotLoadClass() throws ClassNotFoundException
     {
         ComponentModuleDescriptor desc = new ComponentModuleDescriptor();
 
         Element e = DocumentHelper.createElement("foo");
         e.addAttribute("key", "foo");
         e.addAttribute("class", Foo.class.getName());
 
         Plugin plugin = mock(Plugin.class);
         when(plugin.<Foo>loadClass((String)anyObject(), (Class<?>) anyObject())).thenReturn(Foo.class);
         desc.init(plugin, e);
 
        Foo.called = false;
         desc.enabled();
         assertFalse(Foo.called);
     }
 
     public static class Foo
     {
         public static boolean called;
 
         public Foo()
         {
             called = true;
         }
     }
 }
