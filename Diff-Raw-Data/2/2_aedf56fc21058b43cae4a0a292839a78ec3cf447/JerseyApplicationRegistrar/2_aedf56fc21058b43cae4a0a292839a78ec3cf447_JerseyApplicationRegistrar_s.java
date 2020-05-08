 package org.restmodules.jersey;
 
 import javax.servlet.Filter;
 import javax.servlet.Servlet;
 import javax.ws.rs.core.Application;
 
 import org.osgi.framework.BundleContext;
 import org.osgi.service.http.HttpContext;
 import org.restmodules.AbstractApplicationRegistrar;
 import org.restmodules.ApplicationProvider;
 import org.restmodules.ApplicationRegistrar;
 import org.restmodules.RestmodulesApplication;
 import org.restmodules.filter.DefaultFilterRegistry;
 import org.restmodules.ioc.Proxied;
 import org.restmodules.ioc.Scope;
 import org.restmodules.ioc.Scoped;
 
 import com.sun.jersey.api.core.ResourceConfig;
 import com.sun.jersey.core.spi.component.ComponentContext;
 import com.sun.jersey.core.spi.component.ComponentScope;
 import com.sun.jersey.core.spi.component.ioc.IoCComponentProvider;
 import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
 import com.sun.jersey.core.spi.component.ioc.IoCInstantiatedComponentProvider;
 import com.sun.jersey.core.spi.component.ioc.IoCManagedComponentProvider;
 import com.sun.jersey.spi.container.WebApplication;
 import com.sun.jersey.spi.container.servlet.ServletContainer;
 import org.restmodules.ioc.Provider;
 
 /**
  * Jersey specific implementation for an {@link ApplicationRegistrar}.
  *
  * @author Mathias Broekelmann
  *
  * @since 11.01.2010
  *
  */
 public class JerseyApplicationRegistrar extends AbstractApplicationRegistrar {
 
     public JerseyApplicationRegistrar(final ApplicationProvider provider, final BundleContext bc) {
         super(provider);
     }
 
     @Override
     protected Servlet servlet() {
         final Application application = getApplication();
         final Servlet servlet = new ServletContainer(application) {
 
             @Override
             protected void initiate(final ResourceConfig rc, final WebApplication wa) {
                 if (application instanceof JerseyApplication) {
                     final IoCComponentProviderFactory providerFactory = ((JerseyApplication) application).getComponentProviderFactory();
                     wa.initiate(rc, providerFactory);
                 } else if (application instanceof RestmodulesApplication) {
                     final IoCComponentProviderFactory providerFactory = createProviderFactory((RestmodulesApplication) application);
                     wa.initiate(rc, providerFactory);
                 } else {
                     super.initiate(rc, wa);
                 }
             }
         };
         return servlet;
     }
 
     protected IoCComponentProviderFactory createProviderFactory(final RestmodulesApplication application) {
         return new IoCComponentProviderFactory() {
             public IoCComponentProvider getComponentProvider(final Class<?> c) {
                 final org.restmodules.ioc.Provider<?> provider = application.getProvider(c);
                 IoCComponentProvider result = null;
                 if (provider != null) {
                     result = asComponentProvider(provider);
                 }
                 return result;
             }
 
             public IoCComponentProvider getComponentProvider(final ComponentContext cc, final Class<?> c) {
                return getComponentProvider(null, c);
             }
         };
     }
 
     protected IoCComponentProvider asComponentProvider(final org.restmodules.ioc.Provider<?> provider) {
         IoCComponentProvider result = null;
         if (provider instanceof Scoped) {
             final Scope scope = ((Scoped) provider).getScope();
             result = new IoCManagedComponentProvider() {
 
                 public Object getInstance() {
                     return provider.get();
                 }
 
                 public Object getInjectableInstance(final Object o) {
                     return unproxy(provider, o);
                 }
 
                 public ComponentScope getScope() {
                     return asComponentScope(scope);
                 }
             };
         } else {
             result = new IoCInstantiatedComponentProvider() {
 
                 public Object getInstance() {
                     return provider.get();
                 }
 
                 public Object getInjectableInstance(final Object o) {
                     return unproxy(provider, o);
                 }
             };
         }
         return result;
     }
 
     protected ComponentScope asComponentScope(final Scope scope) {
         ComponentScope result = ComponentScope.Undefined;
         if (scope != null) {
             switch (scope) {
                 case Request:
                     result = ComponentScope.PerRequest;
                     break;
                 case Singleton:
                     result = ComponentScope.Singleton;
                     break;
             }
         }
         return result;
     }
 
     @Override
     protected DefaultFilterRegistry filterRegistry() {
         return new DefaultFilterRegistry() {
 
             @Override
             protected Provider<Filter> createFilterProvider(final Class<Filter> filterClazz) {
                 final Application app = getApplication();
                 if (app instanceof JerseyApplication) {
                     final IoCComponentProviderFactory cpf = ((JerseyApplication) app).getComponentProviderFactory();
                     if (cpf != null) {
                         final IoCComponentProvider provider = cpf.getComponentProvider(filterClazz);
                         if (provider != null) {
                             return new Provider<Filter>() {
 
                                 public Filter get() {
                                     return (Filter) provider.getInstance();
                                 }
                             };
                         }
                     }
                 }
                 return super.createFilterProvider(filterClazz);
             }
         };
     }
 
     @Override
     protected HttpContext httpContext() {
         final Application application = getApplication();
         final HttpContext httpContext;
         if (application instanceof JerseyApplication) {
             httpContext = ((JerseyApplication) application).getHttpContext();
         } else {
             httpContext = super.httpContext();
         }
         return httpContext;
     }
 
     private Object unproxy(final org.restmodules.ioc.Provider<?> provider, final Object o) {
         Object result = o;
         if (provider instanceof Proxied) {
             result = ((Proxied) provider).unproxy(o);
         }
         return result;
     }
 }
