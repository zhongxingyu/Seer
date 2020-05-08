 package org.apache.tapestry5.sax.internal;
 
 import org.apache.tapestry5.internal.InternalSymbols;
 import org.apache.tapestry5.internal.SingleKeySymbolProvider;
 import org.apache.tapestry5.internal.SyntheticModuleDef;
 import org.apache.tapestry5.internal.SyntheticSymbolSourceContributionDef;
 import org.apache.tapestry5.ioc.AnnotationProvider;
 import org.apache.tapestry5.ioc.Registry;
 import org.apache.tapestry5.ioc.RegistryBuilder;
 import org.apache.tapestry5.ioc.def.ContributionDef;
 import org.apache.tapestry5.ioc.def.ModuleDef;
 import org.apache.tapestry5.ioc.services.SymbolProvider;
 import org.apache.tapestry5.services.TapestryModule;
 import org.apache.tapestry5.test.TapestryTestCase;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.AfterSuite;
 import org.testng.annotations.BeforeSuite;
 
 public class InternalBaseTestCase extends TapestryTestCase implements Registry
 {
     /**
      * The current working directory (i.e., property "user.dir").
      */
     public static final String CURRENT_DIR_PATH = System.getProperty("user.dir");
     /**
      * The Surefire plugin sets basedir but DOES NOT change the current working directory. When building across modules,
      * basedir changes for each module, but user.dir does not. This value should be used when referecing local files.
      * Outside of surefire, the "basedir" property will not be set, and the current working directory will be the
      * default.
      */
     public static final String MODULE_BASE_DIR_PATH = System.getProperty("basedir", CURRENT_DIR_PATH);
 
    // TODO check why this has to be static
    private static Registry registry;
 
     @BeforeSuite
     public final void setup_registry()
     {
         RegistryBuilder builder = new RegistryBuilder();
 
         builder.add(TapestryModule.class, SaxParserModule.class);
 
         // A synthetic module to ensure that the tapestry.alias-mode is set correctly.
 
         SymbolProvider provider = new SingleKeySymbolProvider(InternalSymbols.ALIAS_MODE, "servlet");
         ContributionDef contribution = new SyntheticSymbolSourceContributionDef("AliasMode", provider,
                                                                                 "before:ApplicationDefaults");
 
         ModuleDef module = new SyntheticModuleDef(contribution);
 
         builder.add(module);
 
         registry = builder.build();
 
         registry.performRegistryStartup();
     }
 
     @AfterSuite
     public final void shutdown_registry()
     {
         registry.shutdown();
 
         registry = null;
     }
 
     @AfterMethod
     public final void cleanupThread()
     {
         registry.cleanupThread();
     }
 
     public void performRegistryStartup()
     {
         registry.performRegistryStartup();
     }
 
     public final <T> T getObject(Class<T> objectType, AnnotationProvider annotationProvider)
     {
         return registry.getObject(objectType, annotationProvider);
     }
 
     public final <T> T getService(Class<T> serviceInterface)
     {
         return registry.getService(serviceInterface);
     }
 
     public final <T> T getService(String serviceId, Class<T> serviceInterface)
     {
         return registry.getService(serviceId, serviceInterface);
     }
 
     public final <T> T autobuild(Class<T> clazz)
     {
         return registry.autobuild(clazz);
     }
 
     public <T> T proxy(Class<T> interfaceClass, Class<? extends T> implementationClass)
     {
         return registry.proxy(interfaceClass, implementationClass);
     }
 
     public final void shutdown()
     {
         throw new UnsupportedOperationException("No registry shutdown until @AfterSuite.");
     }
 }
