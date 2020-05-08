 package org.jboss.forge.ui.eclipse;
 
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.concurrent.Callable;
 
 import net.sf.cglib.proxy.Enhancer;
 
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.jboss.forge.container.Forge;
 import org.jboss.forge.container.util.ClassLoaders;
 import org.jboss.forge.se.init.ClassLoaderAdapterCallback;
 import org.jboss.forge.ui.eclipse.integration.ForgeService;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.wiring.BundleWiring;
 
 /**
  * The activator class controls the plug-in life cycle
  */
 public class Activator extends AbstractUIPlugin
 {
 
    // The plug-in ID
    public static final String PLUGIN_ID = "org.jboss.forge.ui.eclipse"; //$NON-NLS-1$
 
    // The shared instance
    private static Activator plugin;
 
    /**
     * The constructor
     */
    public Activator()
    {
    }
 
    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start(BundleContext context) throws Exception
    {
       super.start(context);
 
       BundleWiring wiring = context.getBundle().adapt(BundleWiring.class);
       Collection<String> entries = wiring.listResources("bootpath", "*.jar", BundleWiring.LISTRESOURCES_LOCAL);
       Collection<URL> resources = new HashSet<URL>();
       if (entries != null)
          for (String resource : entries)
          {
             URL jar = context.getBundle().getResource(resource);
             if (jar != null)
                resources.add(jar);
          }
 
       final URLClassLoader loader = new URLClassLoader(resources.toArray(new URL[resources.size()]), null);
       Forge forge = ClassLoaders.executeIn(loader, new Callable<Forge>()
       {
          @Override
          public Forge call() throws Exception
          {
            Class<?> bootstrapType = loader.loadClass("org.jboss.forge.container.Forge");
             return (Forge) Enhancer.create(Forge.class,
                      new ClassLoaderAdapterCallback(loader, bootstrapType.newInstance()));
          }
       });
       ForgeService.INSTANCE.setForge(forge);
       ForgeService.INSTANCE.start();
       plugin = this;
    }
 
    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception
    {
       plugin = null;
       super.stop(context);
       ForgeService.INSTANCE.stop();
    }
 
    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static Activator getDefault()
    {
       return plugin;
    }
 
    /**
     * Returns an image descriptor for the image file at the given plug-in relative path
     *
     * @param path the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path)
    {
       return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }
 }
