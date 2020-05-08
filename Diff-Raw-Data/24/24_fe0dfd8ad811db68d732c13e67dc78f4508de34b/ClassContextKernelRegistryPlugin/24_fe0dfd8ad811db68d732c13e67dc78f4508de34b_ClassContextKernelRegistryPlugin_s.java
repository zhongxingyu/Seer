 package org.jboss.kernel.plugins.dependency;
 
 import org.jboss.kernel.spi.registry.KernelRegistryPlugin;
 import org.jboss.kernel.spi.registry.KernelRegistryEntry;
 import org.jboss.kernel.spi.dependency.KernelController;
 
 /**
  * @author <a href="mailto:ales.justin@gmail.com">Ales Justin</a>
  */
 public class ClassContextKernelRegistryPlugin implements KernelRegistryPlugin
 {
    private KernelController controller;
 
    public ClassContextKernelRegistryPlugin(KernelController controller)
    {
       this.controller = controller;
    }
 
    public KernelRegistryEntry getEntry(Object name)
    {
       if (name instanceof Class)
       {
         return controller.getContextByClass((Class)name);        
       }
       return null;
    }
 
 }
