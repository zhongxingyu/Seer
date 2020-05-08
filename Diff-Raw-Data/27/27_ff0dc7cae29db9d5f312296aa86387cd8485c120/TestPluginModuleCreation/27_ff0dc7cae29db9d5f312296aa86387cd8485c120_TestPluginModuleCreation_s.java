 package com.atlassian.plugin.osgi;
 
 import com.atlassian.plugin.DefaultModuleDescriptorFactory;
 import com.atlassian.plugin.JarPluginArtifact;
 import com.atlassian.plugin.Plugin;
 import com.atlassian.plugin.hostcontainer.HostContainer;
 import com.atlassian.plugin.impl.UnloadablePlugin;
 import com.atlassian.plugin.loaders.ClassPathPluginLoader;
 import com.atlassian.plugin.module.ClassPrefixModuleFactory;
 import com.atlassian.plugin.module.PrefixDelegatingModuleFactory;
 import com.atlassian.plugin.module.PrefixModuleFactory;
 import com.atlassian.plugin.osgi.hostcomponents.ComponentRegistrar;
 import com.atlassian.plugin.osgi.hostcomponents.HostComponentProvider;
 import com.atlassian.plugin.osgi.module.BeanPrefixModuleFactory;
 import com.atlassian.plugin.osgi.test.TestServlet;
 import com.atlassian.plugin.servlet.ServletModuleManager;
 import com.atlassian.plugin.servlet.descriptors.ServletModuleDescriptor;
 import com.atlassian.plugin.test.PluginJarBuilder;
 import org.mockito.invocation.InvocationOnMock;
 import org.mockito.stubbing.Answer;
 
 import java.io.File;
 import java.util.HashSet;
 
 import static org.mockito.Matchers.anyObject;
 import static org.mockito.Mockito.doAnswer;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 /**
  * Tests around the creation of the module class of {@link com.atlassian.plugin.ModuleDescriptor}
  */
 public class TestPluginModuleCreation extends PluginInContainerTestBase
 {
     public void testInstallPlugin2AndGetModuleClass() throws Exception
     {
         final PluginJarBuilder firstBuilder = new PluginJarBuilder("first");
         final File jar = firstBuilder
                 .addFormattedResource("atlassian-plugin.xml",
                         "<atlassian-plugin name='Test' key='first' pluginsVersion='2'>",
                         "    <plugin-info>",
                         "        <version>1.0</version>",
                         "    </plugin-info>",
                         "    <servlet key='foo' class='first.MyServlet'>",
                         "       <url-pattern>/foo</url-pattern>",
                         "    </servlet>",
                         "</atlassian-plugin>")
                 .addFormattedJava("first.MyServlet",
                         "package first;",
                         "import javax.servlet.http.HttpServlet;",
                         "public class MyServlet extends javax.servlet.http.HttpServlet {",
                         "   public String getServletInfo() {",
                         "       return 'bob';",
                         "   }",
                         "}")
                 .build();
 
         final ServletModuleManager servletModuleManager = mock(ServletModuleManager.class);
 
         HostContainer hostContainer = mock(HostContainer.class);
         when(hostContainer.create(StubServletModuleDescriptor.class)).thenReturn(new StubServletModuleDescriptor(moduleFactory, servletModuleManager));
 
         final DefaultModuleDescriptorFactory moduleDescriptorFactory = new DefaultModuleDescriptorFactory(hostContainer);
         moduleDescriptorFactory.addModuleDescriptor("servlet", StubServletModuleDescriptor.class);
 
         initPluginManager(new HostComponentProvider()
         {
             public void provide(final ComponentRegistrar registrar)
             {
             }
         }, moduleDescriptorFactory);
 
         pluginManager.installPlugin(new JarPluginArtifact(jar));
 
         assertEquals(1, pluginManager.getEnabledPlugins().size());
         assertEquals("first.MyServlet", pluginManager.getPlugin("first").getModuleDescriptor("foo").getModule().getClass().getName());
     }
 
     public void testInstallPlugins1AndGetModuleClass() throws Exception
     {
         ClassPathPluginLoader classPathPluginLoader = new ClassPathPluginLoader("testInstallPlugins1AndGetModuleClass.xml");
         final ServletModuleManager servletModuleManager = mock(ServletModuleManager.class);
         final HostContainer hostContainer = mock(HostContainer.class);
         moduleFactory = new PrefixDelegatingModuleFactory(new HashSet<PrefixModuleFactory>()
         {{
             add(new ClassPrefixModuleFactory(hostContainer));
             add(new BeanPrefixModuleFactory());
         }});
         when(hostContainer.create(StubServletModuleDescriptor.class)).thenReturn(new StubServletModuleDescriptor(moduleFactory, servletModuleManager));
         when(hostContainer.create(TestServlet.class)).thenReturn(new TestServlet());
 
         final DefaultModuleDescriptorFactory moduleDescriptorFactory = new DefaultModuleDescriptorFactory(hostContainer);
         moduleDescriptorFactory.addModuleDescriptor("servlet", StubServletModuleDescriptor.class);
 
         initPluginManager(moduleDescriptorFactory, classPathPluginLoader);
 
         assertEquals(1, pluginManager.getEnabledPlugins().size());
         assertEquals("com.atlassian.plugin.osgi.test.TestServlet", pluginManager.getPlugin("first").getModuleDescriptor("foo").getModule().getClass().getName());
     }
 
     public void testInstallPlugins1AndFailToGetModuleClassFromSpring() throws Exception
     {
         ClassPathPluginLoader classPathPluginLoader = new ClassPathPluginLoader("testInstallPlugins1AndFailToGetModuleClassFromSpring.xml");
         final ServletModuleManager servletModuleManager = mock(ServletModuleManager.class);
 
         final HostContainer hostContainer = mock(HostContainer.class);
         moduleFactory = new PrefixDelegatingModuleFactory(new HashSet<PrefixModuleFactory>()
         {{
             add(new ClassPrefixModuleFactory(hostContainer));
             add(new BeanPrefixModuleFactory());
         }});
         when(hostContainer.create(StubServletModuleDescriptor.class)).thenReturn(new StubServletModuleDescriptor(moduleFactory, servletModuleManager));
         when(hostContainer.create(TestServlet.class)).thenReturn(new TestServlet());
         doAnswer(new Answer()
         {
             public Object answer(InvocationOnMock invocation) throws Throwable
             {
                 ((ServletModuleDescriptor)invocation.getArguments()[0]).getModule();
                 return null;
             }
         }).when(servletModuleManager).addServletModule((ServletModuleDescriptor)anyObject());
 
         final DefaultModuleDescriptorFactory moduleDescriptorFactory = new DefaultModuleDescriptorFactory(hostContainer);
         moduleDescriptorFactory.addModuleDescriptor("servlet", StubServletModuleDescriptor.class);
 
         initPluginManager(moduleDescriptorFactory, classPathPluginLoader);
         assertEquals(1, pluginManager.getPlugins().size());
         final Plugin plugin = pluginManager.getPlugins().iterator().next();
         assertTrue(plugin instanceof UnloadablePlugin);
         assertEquals(0, pluginManager.getEnabledPlugins().size());
     }
 
     public void testInstallPlugins2AndGetModuleClassFromSpring() throws Exception
     {
         final PluginJarBuilder firstBuilder = new PluginJarBuilder("first");
         final File jar = firstBuilder
                 .addFormattedResource("atlassian-plugin.xml",
                         "<atlassian-plugin name='Test' key='first' pluginsVersion='2'>",
                         "    <plugin-info>",
                         "        <version>1.0</version>",
                         "    </plugin-info>",
                         "    <servlet key='foo' class='bean:obj'>",
                         "       <url-pattern>/foo</url-pattern>",
                         "    </servlet>",
                         "<component key='obj' class='first.MyServlet'/>",
                         "</atlassian-plugin>")
                 .addFormattedJava("first.MyServlet",
                         "package first;",
                         "import javax.servlet.http.HttpServlet;",
                         "public class MyServlet extends javax.servlet.http.HttpServlet {",
                         "   public String getServletInfo() {",
                         "       return 'bob';",
                         "   }",
                         "}")
                 .build();
 
         final ServletModuleManager servletModuleManager = mock(ServletModuleManager.class);
         HostContainer hostContainer = mock(HostContainer.class);
         when(hostContainer.create(StubServletModuleDescriptor.class)).thenReturn(new StubServletModuleDescriptor(moduleFactory, servletModuleManager));
 
         final DefaultModuleDescriptorFactory moduleDescriptorFactory = new DefaultModuleDescriptorFactory(hostContainer);
         moduleDescriptorFactory.addModuleDescriptor("servlet", StubServletModuleDescriptor.class);
 
         initPluginManager(new HostComponentProvider()
         {
             public void provide(final ComponentRegistrar registrar)
             {
             }
         }, moduleDescriptorFactory);
 
         pluginManager.installPlugin(new JarPluginArtifact(jar));
 
         assertEquals(1, pluginManager.getEnabledPlugins().size());
         assertEquals("first.MyServlet", pluginManager.getPlugin("first").getModuleDescriptor("foo").getModule().getClass().getName());
     }
 
     public void testGetModuleClassFromComponentModuleDescriptor() throws Exception
     {
         final PluginJarBuilder firstBuilder = new PluginJarBuilder("first");
         final File jar = firstBuilder
                 .addFormattedResource("atlassian-plugin.xml",
                         "<atlassian-plugin name='Test' key='first' pluginsVersion='2'>",
                         "    <plugin-info>",
                         "        <version>1.0</version>",
                         "    </plugin-info>",
                         "<component key='obj' class='first.MyServlet'/>",
                         "</atlassian-plugin>")
                 .addFormattedJava("first.MyServlet",
                         "package first;",
                         "import javax.servlet.http.HttpServlet;",
                         "public class MyServlet extends javax.servlet.http.HttpServlet {",
                         "   public String getServletInfo() {",
                         "       return 'bob';",
                         "   }",
                         "}")
                 .build();
 
 
         initPluginManager();
 
         pluginManager.installPlugin(new JarPluginArtifact(jar));
 
         assertEquals(1, pluginManager.getEnabledPlugins().size());
         assertEquals("first.MyServlet", pluginManager.getPlugin("first").getModuleDescriptor("obj").getModule().getClass().getName());
     }
 
     public void testGetModuleClassFromComponentImportModuleDescriptor() throws Exception
     {
         final PluginJarBuilder firstBuilder = new PluginJarBuilder("first");
         final File jar1 = firstBuilder
                         .addFormattedResource("atlassian-plugin.xml",
                                 "<atlassian-plugin name='Test1' key='first' pluginsVersion='2'>",
                                 "    <plugin-info>",
                                 "        <version>1.0</version>",
                                 "    </plugin-info>",
                                 "<component key='obj' class='first.MyServlet' public='true'>",
                                 "<interface>com.atlassian.plugin.osgi.SomeInterface</interface>",
                                 "</component>",
                                 "</atlassian-plugin>")
                         .addFormattedJava("com.atlassian.plugin.osgi.SomeInterface",
                             "package com.atlassian.plugin.osgi;",
                             "public interface SomeInterface {}")
                         .addFormattedJava("first.MyServlet",
                                 "package first;",
                                 "import javax.servlet.http.HttpServlet;",
                                 "public class MyServlet extends javax.servlet.http.HttpServlet implements com.atlassian.plugin.osgi.SomeInterface {",
                                 "   public String getServletInfo() {",
                                 "       return 'bob';",
                                 "   }",
                                 "}")
                         .build();
 
         final File jar2 = new PluginJarBuilder("second")
                 .addFormattedResource("atlassian-plugin.xml",
                         "<atlassian-plugin name='Test2' key='second' pluginsVersion='2'>",
                         "    <plugin-info>",
                         "        <version>1.0</version>",
                         "    </plugin-info>",
                         "    <component-import key='obj' interface='com.atlassian.plugin.osgi.SomeInterface' />",
                         "</atlassian-plugin>"
                 )
                 .addFormattedJava("com.atlassian.plugin.osgi.SomeInterface",
                             "package com.atlassian.plugin.osgi;",
                             "public interface SomeInterface {}")
                 .build();
 
         initPluginManager();
         pluginManager.installPlugin(new JarPluginArtifact(jar1));
         pluginManager.installPlugin(new JarPluginArtifact(jar2));
 
 
         assertEquals(2, pluginManager.getEnabledPlugins().size());
         assertEquals("first.MyServlet", pluginManager.getPlugin("first").getModuleDescriptor("obj").getModule().getClass().getName());
         assertTrue(pluginManager.getPlugin("second").getModuleDescriptor("obj").getModule() instanceof SomeInterface);
     }
 
     public void testFailToGetModuleClassFromSpring() throws Exception
     {
         final PluginJarBuilder firstBuilder = new PluginJarBuilder("first");
         final File jar = firstBuilder
                 .addFormattedResource("atlassian-plugin.xml",
                         "<atlassian-plugin name='Test' key='first' pluginsVersion='2'>",
                         "    <plugin-info>",
                         "        <version>1.0</version>",
                         "    </plugin-info>",
                         "    <servlet key='foo' class='bean:beanId' name='spring bean for servlet'>",
                         "       <url-pattern>/foo</url-pattern>",
                         "    </servlet>",
                         "<component key='obj' class='first.MyServlet' />",
                         "</atlassian-plugin>")
                 .addFormattedJava("first.MyServlet",
                         "package first;",
                         "import javax.servlet.http.HttpServlet;",
                         "public class MyServlet extends javax.servlet.http.HttpServlet {",
                         "   public String getServletInfo() {",
                         "       return 'bob';",
                         "   }",
                         "}")
                 .build();
 
         final ServletModuleManager servletModuleManager = mock(ServletModuleManager.class);
         doAnswer(new Answer()
         {
             public Object answer(InvocationOnMock invocation) throws Throwable
             {
                 ((ServletModuleDescriptor)invocation.getArguments()[0]).getModule();
                 return null;
             }
         }).when(servletModuleManager).addServletModule((ServletModuleDescriptor)anyObject());
         final HostContainer hostContainer = mock(HostContainer.class);
         moduleFactory = new PrefixDelegatingModuleFactory(new HashSet<PrefixModuleFactory>()
         {{
             add(new ClassPrefixModuleFactory(hostContainer));
             add(new BeanPrefixModuleFactory());
         }});
         when(hostContainer.create(StubServletModuleDescriptor.class)).thenReturn(new StubServletModuleDescriptor(moduleFactory, servletModuleManager));
 
         final DefaultModuleDescriptorFactory moduleDescriptorFactory = new DefaultModuleDescriptorFactory(hostContainer);
         moduleDescriptorFactory.addModuleDescriptor("servlet", StubServletModuleDescriptor.class);
 
         initPluginManager(new HostComponentProvider()
         {
             public void provide(final ComponentRegistrar registrar)
             {
             }
         }, moduleDescriptorFactory);
 
         pluginManager.installPlugin(new JarPluginArtifact(jar));
         assertEquals(0, pluginManager.getEnabledPlugins().size());
         final Plugin plugin = pluginManager.getPlugins().iterator().next();
         assertTrue(plugin instanceof UnloadablePlugin);
     }
 }
