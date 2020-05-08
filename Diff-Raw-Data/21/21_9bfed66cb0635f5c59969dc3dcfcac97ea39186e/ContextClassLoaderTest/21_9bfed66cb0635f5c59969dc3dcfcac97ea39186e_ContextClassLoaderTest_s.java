 package com.atlassian.plugin.osgi;
 
 import com.atlassian.plugin.FilePluginJar;
 import com.atlassian.plugin.Plugin;
 import com.atlassian.plugin.osgi.hostcomponents.HostComponentProvider;
 import com.atlassian.plugin.osgi.hostcomponents.ComponentRegistrar;
 import com.atlassian.plugin.osgi.loader.OsgiPlugin;
 
 import java.io.File;
 
 import junit.framework.TestCase;
 import org.apache.log4j.Logger;
 import org.apache.log4j.BasicConfigurator;
 
 public class ContextClassLoaderTest extends PluginInContainerTestBase {
 
     public void testCorrectContextClassLoaderForHostComponents() throws Exception
     {
         final HostCompImpl comp = new HostCompImpl();
         File plugin = new PluginBuilder("ccltest")
                 .addResource("atlassian-plugin.xml",
                         "<atlassian-plugin key=\"ccltest\" pluginsVersion=\"2\">\n" +
                         "    <plugin-info>\n" +
                         "        <version>1.0</version>\n" +
                         "    </plugin-info>\n" +
                         "    <component key=\"foo\" class=\"my.FooImpl\" />\n" +
                         "</atlassian-plugin>")
                 .addJava("my.Foo", "package my;public interface Foo {}")
                .addJava("my.FooImpl", "package my;import com.atlassian.plugin.osgi.ContextClassLoaderTest.HostComp;" +
                         "public class FooImpl implements Foo {public FooImpl(HostComp comp) throws Exception { comp.run(); }}")
                 .build();
         HostComponentProvider prov = new HostComponentProvider()
         {
             public void provide(ComponentRegistrar registrar)
             {
                 registrar.register(HostComp.class).forInstance(comp).withName("hostComp");
             }
         };
 
         initPluginManager(prov);
         pluginManager.installPlugin(new FilePluginJar(plugin));
 
         assertNotNull(comp.cl);
         assertNotNull(comp.testClass);
         assertTrue(comp.testClass == TestCase.class);
 
     }
 
    public static interface HostComp
    {
        void run() throws ClassNotFoundException;
    }

     public static class HostCompImpl implements HostComp
     {
         public ClassLoader cl;
         public Class testClass;
         public void run() throws ClassNotFoundException
         {
             cl = Thread.currentThread().getContextClassLoader();
             testClass = cl.loadClass("junit.framework.TestCase");
         }
     }
 }
