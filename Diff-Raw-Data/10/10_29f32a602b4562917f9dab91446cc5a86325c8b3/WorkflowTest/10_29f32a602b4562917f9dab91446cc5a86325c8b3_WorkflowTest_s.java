 package com.redshape.plugins;
 
 import com.redshape.plugins.loaders.IPluginLoadersRegistry;
 import com.redshape.plugins.loaders.IPluginsLoader;
 import com.redshape.plugins.loaders.resources.IPluginResource;
 import com.redshape.plugins.meta.IMetaLoader;
 import com.redshape.plugins.meta.IMetaLoadersRegistry;
 import com.redshape.plugins.meta.IPluginInfo;
 import com.redshape.plugins.packagers.*;
 import com.redshape.plugins.registry.IPluginsRegistry;
 import com.redshape.plugins.starters.EngineType;
 import com.redshape.plugins.starters.IStarterEngine;
 import com.redshape.plugins.starters.IStartersRegistry;
 import com.redshape.utils.tests.AbstractContextAwareTest;
 import org.junit.Test;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 
 import static org.junit.Assert.*;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Nikelin
  * Date: 09.01.12
  * Time: 17:48
  * To change this template use File | Settings | File Templates.
  */
 public class WorkflowTest extends AbstractContextAwareTest<String> {
     
     public WorkflowTest() {
         super("src/test/resources/context.xml");
     }
 
     protected IPluginsRegistry getPluginsRegistry() {
         return this.getContext().getBean( IPluginsRegistry.class );
     }
 
     protected IPluginLoadersRegistry getLoadersRegistry() {
         return this.getContext().getBean(IPluginLoadersRegistry.class);
     }
 
     protected IPackagesRegistry getPackagesRegistry() {
         return this.getContext().getBean(IPackagesRegistry.class);
     }
     
     protected IMetaLoadersRegistry getMetaLoadersRegistry() {
         return this.getContext().getBean(IMetaLoadersRegistry.class);
     }
     
     protected IStartersRegistry getStartersRegistry() {
         return this.getContext().getBean(IStartersRegistry.class);
     }
     
     protected URI getTestPluginURI() throws URISyntaxException {
         return new URI( this.getContext().getBean("pluginPath", String.class) );
     }
     
     @Test
     public void testMain() throws URISyntaxException, LoaderException, PackagerException {
         URI testPluginURI = this.getTestPluginURI();
         IPluginLoadersRegistry loadersRegistry = this.getLoadersRegistry();
         IPluginsLoader loader = loadersRegistry.selectLoader( testPluginURI );
         assertNotNull(loader);
         IPluginResource resource = loader.load(testPluginURI);
         assertNotNull(resource);
         assertTrue( resource.canRead() );
         IPackagesRegistry packagesRegistry = this.getPackagesRegistry();
         PackagingType packageType = this.getPackagesRegistry().detectType(resource);
         assertNotSame( PackagingType.UNKNOWN, packageType );
         assertEquals( PackagingType.INLINE, packageType );
         IPackageSupport packager = packagesRegistry.getSupport(packageType);
         assertNotNull(packager);
         IPackageDescriptor descriptor = packager.processResource(packageType, resource);
         assertNotNull(descriptor);
         IMetaLoadersRegistry metaLoadersRegistry = this.getMetaLoadersRegistry();
         assertTrue( metaLoadersRegistry.isSupported(descriptor) );
         IMetaLoader metaLoader = metaLoadersRegistry.selectLoader(descriptor);
         assertNotNull(metaLoader);
         IPluginInfo info = metaLoader.load(descriptor);
         assertEquals("3.0.0", info.getArchVersion() );
         assertEquals("Test Plugin", info.getName() );
         assertEquals("Redshape Ltd.", info.getPublisher().getCompany() );
         assertEquals("http://redshape.ru", info.getPublisher().getURI().toString() );
         
         assertNotNull(info.getStarterInfo());
         assertEquals(EngineType.Java, info.getStarterInfo().getEngineType());
         assertEquals("1.6", info.getStarterInfo().getVersion());
 
         IStarterEngine engine = this.getStartersRegistry().selectEngine( info.getStarterInfo().getEngineType() );
         assertNotNull(engine);
         
        IPlugin plugin = engine.resolve(info);
        IPluginsRegistry registry = this.getPluginsRegistry();
        registry.registerPlugin( info, plugin );
        engine.start(plugin);
        engine.stop(plugin);
     }
 
 }
