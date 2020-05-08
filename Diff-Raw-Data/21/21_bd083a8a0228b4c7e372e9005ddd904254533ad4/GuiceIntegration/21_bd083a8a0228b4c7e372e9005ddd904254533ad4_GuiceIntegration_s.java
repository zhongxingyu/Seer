 /*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
 package org.jboss.guice.spi;
 
 import java.util.Set;
 
 import com.google.inject.Binder;
 import com.google.inject.Provider;
 import com.google.inject.name.Names;
 import com.google.inject.spi.SourceProviders;
 import org.jboss.beans.metadata.plugins.factory.GenericBeanFactory;
 import org.jboss.dependency.spi.Controller;
 import org.jboss.dependency.spi.ControllerContext;
 import org.jboss.dependency.spi.ControllerState;
 import org.jboss.kernel.Kernel;
 import org.jboss.kernel.plugins.config.Configurator;
 import org.jboss.kernel.spi.config.KernelConfigurator;
 import org.jboss.kernel.spi.dependency.KernelController;
 import org.jboss.reflect.spi.ClassInfo;
 
 /**
  * Microcontainer + Guice integration.
  *
  * @author <a href="mailto:ales.justin@jboss.com">Ales Justin</a>
  */
 public abstract class GuiceIntegration extends BinderHolder
 {
    static
    {
       SourceProviders.skip(GuiceIntegration.class);
    }
 
    private static final ControllerContextBindFilter ALL = new ControllerContextBindFilter()
    {
       public boolean bind(ControllerContext context)
       {
          return true;
       }
    };
 
    private KernelController controller;
 
    protected GuiceIntegration(Kernel kernel, Binder binder)
    {
       super(binder);
       if (kernel == null)
          throw new IllegalArgumentException("Null kernel.");
       this.controller = kernel.getController();
    }
 
    /**
     * Get the kernel controller.
     *
     * @return the kernel controller
     */
    protected KernelController getController()
    {
       return controller;
    }
 
    /**
     * Creates a provider which looks up objects from Microcontainer using the given name.
     * Expects a binding to {@link org.jboss.dependency.spi.Controller}. Example usage:
     * <p/>
     * <pre>
     * bind(DataSource.class).toProvider(fromMicrocontainer(DataSource.class, "dataSource"));
     * </pre>
     *
     * @param type the class type
     * @param name the bean name
     * @return Provider instance
     */
    public static <T> Provider<T> fromMicrocontainer(Class<T> type, String name)
    {
       return new InjectableMicrocontainerProvider<T>(type, name);
    }
 
    /**
     * Binds all Microcontainer beans from the given factory by name. For a MC bean
     * named "foo", this method creates a binding to the bean's type and
     * {@code @Named("foo")}.
     *
     * @param binder     the binder
     * @param controller the controller
     * @see com.google.inject.name.Named
     * @see com.google.inject.name.Names#named(String)
     */
    public static void bindAll(Binder binder, Controller controller)
    {
       bindAll(binder, controller, ALL);
    }
 
    /**
     * Binds all Microcontainer beans from the given factory by name. For a MC bean
     * named "foo", this method creates a binding to the bean's type and
     * {@code @Named("foo")}.
     *
     * @param binder     the binder
     * @param controller the controller
     * @param filter the filter
     * @see com.google.inject.name.Named
     * @see com.google.inject.name.Names#named(String)
     */
    public static void bindAll(Binder binder, Controller controller, ControllerContextBindFilter filter)
    {
       Set<ControllerContext> installedContexts = controller.getContextsByState(ControllerState.INSTALLED);
       if (installedContexts != null && installedContexts.isEmpty() == false)
       {
          for (ControllerContext context : installedContexts)
          {
             if (filter.bind(context))
                bindContext(binder, controller, context);
          }
       }
    }
 
    /**
     * Bind controller context.
     *
     * @param binder     the binder
     * @param controller the controller
     * @param context    the context
     */
    protected static void bindContext(Binder binder, Controller controller, ControllerContext context)
    {
       Class<?> targetClass = null;
       Object target = context.getTarget();
       if (target != null)
       {
          targetClass = target.getClass();
          // is target bean factory
          if (GenericBeanFactory.class.isAssignableFrom(targetClass))
          {
             GenericBeanFactory gbf = (GenericBeanFactory)target;
             String bean = gbf.getBean();
             if (bean != null && controller instanceof KernelController)
             {
                try
                {
                   ClassLoader classLoader = Configurator.getClassLoader(gbf.getClassLoader());
                   KernelController kernelController = (KernelController)controller;
                   KernelConfigurator configurator = kernelController.getKernel().getConfigurator();
                   ClassInfo info = configurator.getClassInfo(bean, classLoader);
                   targetClass = info.getType();
                }
                catch (Throwable t)
                {
                   throw new RuntimeException(t);
                }
             }
          }
       }
       if (targetClass != null)
       {
          bindBean(binder, controller, context.getName().toString(), targetClass);
          Set<Object> aliases = context.getAliases();
          if (aliases != null && aliases.isEmpty() == false)
          {
             for (Object alias : aliases)
                bindBean(binder, controller, alias.toString(), targetClass);
          }
       }
    }
 
    /**
     * Bind bean.
     *
     * @param binder     the binder
     * @param controller the controller
     * @param name       the name
     * @param type       the type
     */
    static <T> void bindBean(Binder binder, Controller controller, String name, Class<T> type)
    {
       MicrocontainerProvider<T> provider = new MicrocontainerProvider<T>(type, name);
       provider.initialize(controller);
       binder.bind(type).annotatedWith(Names.named(name)).toProvider(provider);
    }
 }
