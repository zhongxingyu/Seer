 package org.apache.tapestry5.contextmenu.services;
 
 import org.apache.tapestry5.contextmenu.internal.GridCellWorker;
 import org.apache.tapestry5.ioc.Configuration;
 import org.apache.tapestry5.ioc.OrderedConfiguration;
 import org.apache.tapestry5.ioc.annotations.Contribute;
 import org.apache.tapestry5.ioc.annotations.Primary;
 import org.apache.tapestry5.services.ComponentClassResolver;
 import org.apache.tapestry5.services.Environment;
 import org.apache.tapestry5.services.LibraryMapping;
 import org.apache.tapestry5.services.transform.ComponentClassTransformWorker2;
 
 public class ContextMenuModule
 {
     @Contribute(ComponentClassResolver.class)
     public void provideComponentClassResolver(Configuration<LibraryMapping> configuration)
     {
         configuration.add(new LibraryMapping("core", "org.apache.tapestry5.contextmenu"));
     }
 
     @Contribute(ComponentClassTransformWorker2.class)
     @Primary
     public static void provideGridCellWorker(
             OrderedConfiguration<ComponentClassTransformWorker2> configuration,
             Environment environment)
     {
        configuration.add("GridCellWorker", new GridCellWorker(environment));
     }
 }
