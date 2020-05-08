 /*
  *
  *     This program is free software; you can redistribute it and/or modify it
  *     under the terms of the GNU General Public License, Version 2 as published
  *     by the Free Software Foundation.
  *
  *     This program is distributed in the hope that it will be useful, but
  *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
  *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
  *     for more details.
  *
  *     You should have received a copy of the GNU General Public License along
  *     with this program; if not, write to the Free Software Foundation, Inc., 59
  *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
  *
  */
 package at.sciencesoft.osgi;
 
 import at.sciencesoft.plugin.BundleIface;
 import at.sciencesoft.plugin.Plugin;
 import at.sciencesoft.plugin.PluginManager;
 import at.sciencesoft.plugin.ResultSet;
 
 /*
 import com.openexchange.exception.OXException;
 import com.openexchange.osgi.DeferredActivator;
 import com.openexchange.osgi.ServiceRegistry;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import org.osgi.service.http.HttpService;
 import org.osgi.service.http.NamespaceException;
 **/
 //import com.openexchange.sessiond.services.*;
 //import com.openexchange.sessionstorage.*;       
 
 import java.util.Properties;
 import javax.security.auth.login.LoginException;
 import org.apache.commons.logging.Log;
 import org.osgi.framework.ServiceRegistration;
 import java.util.concurrent.atomic.AtomicBoolean;
 import com.openexchange.exception.OXException;
 import com.openexchange.log.LogFactory;
 import com.openexchange.osgi.DeferredActivator;
 import com.openexchange.osgi.ServiceRegistry;
 import org.osgi.service.http.HttpService;
 import javax.servlet.ServletException;
 import org.osgi.service.http.NamespaceException;
 
 /**
  * 
  * @author <a href="mailto:peter.sauer@sciencesoft.at">Peter Sauer</a>
  */
 public final class OXAdminGuiServletActivator extends DeferredActivator {
 
     private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(OXAdminGuiServletActivator.class);
    private static final String PWC_SRVLT_ALIAS = "servlet/webserver/*";
     private final AtomicBoolean registered;
 
     /**
      * Initializes a new {@link mwOXAdminGuiServletActivator}
      */
     public OXAdminGuiServletActivator() {
         super();
         registered = new AtomicBoolean();
     }
     // private static final Class<?>[] NEEDED_SERVICES = {HttpService.class, SessiondService.class};
     private static final Class<?>[] NEEDED_SERVICES = { HttpService.class };
     
     @Override
     protected Class<?>[] getNeededServices() {
         return NEEDED_SERVICES;
     }
  
     @Override
     protected void handleAvailability(final Class<?> clazz) {
         /*
          * Add available service to registry
          */
         LOG.info("Going to register Peter's OX Admin GUI Servlet");
         OXAdminGuiServletServiceRegistry.getServiceRegistry().addService(clazz, getService(clazz));
         if (OXAdminGuiServletServiceRegistry.getServiceRegistry().size() == NEEDED_SERVICES.length) {
             /*
              * All needed services available: Register servlet
              */
             try {
                 registerServlet();
             } catch (final ServletException e) {
                 LOG.error(e.getMessage(), e);
             } catch (final NamespaceException e) {
                 LOG.error(e.getMessage(), e);
             }
         }
     }
 
     @Override
     protected void handleUnavailability(final Class<?> clazz) {
         unregisterServlet();
         /*
          * Remove unavailable service from registry
          */
         OXAdminGuiServletServiceRegistry.getServiceRegistry().removeService(clazz);
     }
 
     @Override
     protected void startBundle() throws Exception {
         try {
             
             /*
              * (Re-)Initialize service registry with available services
              */
             {
                 final ServiceRegistry registry = OXAdminGuiServletServiceRegistry.getServiceRegistry();
                 registry.clearRegistry();
                 final Class<?>[] classes = getNeededServices();
                 for (int i = 0; i < classes.length; i++) {
                     final Object service = getService(classes[i]);
                     if (null != service) {
                         registry.addService(classes[i], service);
                     }
                 }
 
             }
 
             // context.registerService(PasswordChangeService.class.getName(), new PasswordChange(), null);
 
             /*
              * Register servlet
              */
             registerServlet();
             // call plugins
             Plugin[] plist = PluginManager.getPulgin(Plugin.PLUGIN.BUNDLE);
             if (plist != null) {
                 for (int j = 0; j < plist.length; ++j) {
                     BundleIface bi = plist[j].getBundleIface();
                     ResultSet rs = bi.startBundle();
                     if (!rs.isSuccess()) {
                         throw new Exception(rs.getErrorMsg());
                     }
                 }
             }
 
            
            
         } catch (final Exception e) {
             LOG.error(e.getMessage(), e);
             throw e;
         }
 
     }
 
     @Override
     protected void stopBundle() throws Exception {
         try {
             /*
              * Unregister servlet
              */
             unregisterServlet();
             /*
              * Clear service registry
              */
             OXAdminGuiServletServiceRegistry.getServiceRegistry().clearRegistry();
 
             // call plugins
             Plugin[] plist = PluginManager.getPulgin(Plugin.PLUGIN.BUNDLE);
             if (plist != null) {
                 for (int j = 0; j < plist.length; ++j) {
                     BundleIface bi = plist[j].getBundleIface();
                     ResultSet rs = bi.stopBundle();
                     if (!rs.isSuccess()) {
                         throw new Exception(rs.getErrorMsg());
                     }
                 }
             }
 
         } catch (final Exception e) {
             LOG.error(e.getMessage(), e);
             throw e;
         }
 
     }
 
     private void registerServlet() throws ServletException, NamespaceException {
         if (registered.compareAndSet(false, true)) {
             final HttpService httpService = OXAdminGuiServletServiceRegistry.getServiceRegistry().getService(HttpService.class);
             if (httpService == null) {
                 LOG.error("HTTP service is null. Peter's OX Admin GUI servlet cannot be registered");
             } else {
                 /*
                  * Register servlet
                  */
                 httpService.registerServlet(PWC_SRVLT_ALIAS, new at.sciencesoft.webserver.WebServer(), null, null);
                 if (LOG.isInfoEnabled()) {
                     LOG.info("Peter's OX Admin GUI servlet successfully registered");
                 }
             }
         }
     }
 
     private void unregisterServlet() {
         if (registered.compareAndSet(true, false)) {
             /*
              * Unregister servlet
              */
             final HttpService httpService = OXAdminGuiServletServiceRegistry.getServiceRegistry().getService(HttpService.class);
             if (httpService == null) {
                 LOG.error("HTTP service is null. Peter's OX Admin GUI servlet cannot be unregistered");
             } else {
                 /*
                  * Unregister servlet
                  */
                 httpService.unregister(PWC_SRVLT_ALIAS);
                 if (LOG.isInfoEnabled()) {
                     LOG.info("Peter's OX Admin GUI servlet successfully unregistered");
                 }
             }
         }
     }
     // public static SessiondService ss;
 }
