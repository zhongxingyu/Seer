 package sorcer.boot;
 
 import com.sun.jini.start.*;
 import net.jini.config.Configuration;
 import net.jini.export.ProxyAccessor;
 import net.jini.security.BasicProxyPreparer;
 import net.jini.security.ProxyPreparer;
 import net.jini.security.policy.DynamicPolicyProvider;
 import net.jini.security.policy.PolicyFileProvider;
 import net.jini.security.policy.PolicyInitializationException;
 import org.rioproject.opstring.ClassBundle;
 import org.rioproject.opstring.ServiceElement;
 import org.rioproject.resolver.ResolverException;
 import org.rioproject.resolver.ResolverHelper;
 import sorcer.provider.boot.AbstractServiceDescriptor;
 
 import java.io.File;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.security.AllPermission;
 import java.security.Permission;
 import java.security.Policy;
 
 /**
  * @author Rafał Krupiński
  */
 public class OpstringServiceDescriptor extends AbstractServiceDescriptor {
     public static final NoOpLifeCycle NOOP = new NoOpLifeCycle();
     private ServiceElement serviceElement;
     private ProxyPreparer servicePreparer = new BasicProxyPreparer();
     private URL oar;
     private URL policyFile;
 
     private static AggregatePolicyProvider globalPolicy = null;
     private static Policy initialGlobalPolicy = null;
 
     public OpstringServiceDescriptor(ServiceElement serviceElement, URL oar, URL policyFile) {
         this.serviceElement = serviceElement;
         this.oar = oar;
         this.policyFile = policyFile;
     }
 
     @Override
     protected Object doCreate(Configuration config) throws Exception {
         ClassLoader cl = getClassLoader(serviceElement.getComponentBundle(), serviceElement, getCommonClassLoader(config), new URL[]{oar});
 
         security(cl);
 
         Class implClass = cl.loadClass(serviceElement.getComponentBundle().getClassName());
         ClassLoaderUtil.displayClassLoaderTree(cl);
         Object impl;
         Object proxy;
         Thread currentThread = Thread.currentThread();
         ClassLoader currentClassLoader = currentThread.getContextClassLoader();
         currentThread.setContextClassLoader(cl);
         try {
             logger.trace("Attempting to get implementation constructor");
             Constructor constructor = implClass.getDeclaredConstructor(new Class[]{String[].class, LifeCycle.class});
             logger.trace("Obtained implementation constructor: {}", constructor.toString());
             constructor.setAccessible(true);
             impl = constructor.newInstance(serviceElement.getServiceBeanConfig().getConfigArgs(), NOOP);
             logger.trace("Obtained implementation instance: {}", impl.toString());
             if (impl instanceof ServiceProxyAccessor) {
                 proxy = ((ServiceProxyAccessor) impl).getServiceProxy();
             } else if (impl instanceof ProxyAccessor) {
                 proxy = ((ProxyAccessor) impl).getProxy();
             } else {
                 proxy = null; // just for insurance
             }
             if (proxy != null) {
                 proxy = servicePreparer.prepareProxy(proxy);
             }
             logger.trace("Proxy:  {}", proxy == null ? "<NULL>" : proxy.toString());
 
         } catch (InvocationTargetException e) {
             Throwable t = e.getCause() == null ? e.getTargetException() : e.getCause();
             if (t != null && t instanceof Exception)
                 throw (Exception) t;
             throw e;
 
         } finally {
             currentThread.setContextClassLoader(currentClassLoader);
         }
         return new Created(impl, proxy);
     }
 
     private void security(ClassLoader cl) throws PolicyInitializationException {
         synchronized (OpstringServiceDescriptor.class) {
             if (globalPolicy == null) {
                 initialGlobalPolicy = Policy.getPolicy();
                 globalPolicy = new AggregatePolicyProvider(initialGlobalPolicy);
                 Policy.setPolicy(globalPolicy);
                 logger.debug("Global policy set: {}",
                         globalPolicy);
             }
 
            String policyFile = this.policyFile.toExternalForm();
            DynamicPolicyProvider service_policy = new DynamicPolicyProvider(
                    new PolicyFileProvider(policyFile));
            LoaderSplitPolicyProvider splitServicePolicy = new LoaderSplitPolicyProvider(
                    cl, service_policy, new DynamicPolicyProvider(
                    initialGlobalPolicy));
            /*
             * Grant "this" code enough permission to do its work under the
			 * service policy, which takes effect (below) after the context
			 * loader is (re)set.
			 */
            splitServicePolicy.grant(OpstringServiceDescriptor.class, null,
                    new Permission[]{new AllPermission()});
            globalPolicy.setPolicy(cl, splitServicePolicy);
         }
     }
 
 
     private static URLClassLoader getClassLoader(ClassBundle bundle, ServiceElement serviceElement, ClassLoader parentCL, URL[] extraCp) throws ResolverException, MalformedURLException {
         String[] urlStrings = ResolverHelper.resolve(bundle.getArtifact(), ResolverHelper.getResolver(), serviceElement.getRemoteRepositories());
         URL[] urls = new URL[urlStrings.length + extraCp.length];
 
         for (int i = 0; i < urlStrings.length; i++) {
             String urlString = urlStrings[i];
             urls[i] = new URL(urlString);
         }
         System.arraycopy(extraCp, 0, urls, urlStrings.length, extraCp.length);
         return new URLClassLoader(urls, parentCL);
     }
 
     private static class NoOpLifeCycle implements LifeCycle {
         @Override
         public boolean unregister(Object impl) {
             return false;
         }
     }
 }
