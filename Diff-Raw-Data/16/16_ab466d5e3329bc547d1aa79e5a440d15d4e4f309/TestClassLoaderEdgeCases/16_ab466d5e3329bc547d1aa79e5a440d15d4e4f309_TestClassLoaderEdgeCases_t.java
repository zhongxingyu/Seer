 package com.atlassian.plugin.osgi;
 
 import com.atlassian.plugin.test.PluginJarBuilder;
 import com.atlassian.plugin.JarPluginArtifact;
import com.atlassian.plugin.DefaultModuleDescriptorFactory;
import com.atlassian.plugin.hostcontainer.DefaultHostContainer;
 import com.atlassian.plugin.osgi.hostcomponents.HostComponentProvider;
 import com.atlassian.plugin.osgi.hostcomponents.ComponentRegistrar;
 import com.atlassian.plugin.osgi.factory.OsgiPlugin;
 
 import java.io.File;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 /**
  * These tests are mainly here to demonstrate different edge cases you can encounter
  */
 public class TestClassLoaderEdgeCases extends PluginInContainerTestBase
 {
     public void testLinkageError() throws Exception
     {
         File privateJar = new PluginJarBuilder("private-jar")
                 .addFormattedJava("com.atlassian.plugin.osgi.Callable2",
                         "package com.atlassian.plugin.osgi;",
                         "public interface Callable2 {",
                         "    String call() throws Exception;",
                         "}")
                 .build();
 
         File pluginJar = new PluginJarBuilder("privatejartest")
                 .addFormattedResource("atlassian-plugin.xml",
                         "<atlassian-plugin name='Test' key='test.privatejar.plugin' pluginsVersion='2'>",
                         "    <plugin-info>",
                         "        <version>1.0</version>",
                         "    </plugin-info>",
                        "    <object key='obj' class='my.Foo'/>",
                         "</atlassian-plugin>")
                 .addFormattedJava("my.Foo",
                         "package my;",
                         "import com.atlassian.plugin.osgi.Callable2;",
                         "import com.atlassian.plugin.osgi.test.Callable2Factory;",
                         "public class Foo {",
                         "  public String call() throws Exception { return 'hi ' + new Callable2Factory().create().call();}",
                         "}")
                 .addFile("META-INF/lib/private.jar", privateJar)
                 .build();
        DefaultModuleDescriptorFactory factory = new DefaultModuleDescriptorFactory(new DefaultHostContainer());
        factory.addModuleDescriptor("object", ObjectModuleDescriptor.class);
        initPluginManager(null, factory);
         pluginManager.installPlugin(new JarPluginArtifact(pluginJar));
         assertEquals(1, pluginManager.getEnabledPlugins().size());
         OsgiPlugin plugin = (OsgiPlugin) pluginManager.getPlugin("test.privatejar.plugin");
         assertEquals("Test", plugin.getName());
         Class foo = plugin.getModuleDescriptor("obj").getModuleClass();
         Object fooObj = plugin.autowire(foo);
         try
         {
             Method method = foo.getMethod("call");
             method.invoke(fooObj);
             fail("Should have thrown linkage error");
         }
         catch (InvocationTargetException ex)
         {
             if (ex.getTargetException() instanceof LinkageError) {
                 // passed
             }
             else
             {
                 fail("Should have thrown linkage error");
             }
         }
     }
 }
