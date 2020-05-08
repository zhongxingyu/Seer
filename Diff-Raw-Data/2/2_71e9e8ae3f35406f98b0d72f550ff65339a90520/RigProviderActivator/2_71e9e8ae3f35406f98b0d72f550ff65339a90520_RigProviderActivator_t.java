 /**
  * SAHARA Scheduling Server
  * Schedules and assigns local laboratory rigs.
  * 
  * @license See LICENSE in the top level directory for complete license terms.
  * 
  * Copyright (c) 2010, University of Technology, Sydney
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  *  * Redistributions of source code must retain the above copyright notice,
  *    this list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  *  * Neither the name of the University of Technology, Sydney nor the names
  *    of its contributors may be used to endorse or promote products derived from
  *    this software without specific prior written permission.
  *    
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  * 
  * @author Michael Diponio (mdiponio)
  * @date 12th January 2010
  */
 package au.edu.uts.eng.remotelabs.schedserver.rigprovider;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 
 import org.apache.axis2.transport.http.AxisServlet;
 import org.hibernate.Hibernate;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.Constants;
 import org.osgi.framework.ServiceEvent;
 import org.osgi.framework.ServiceReference;
 import org.osgi.framework.ServiceRegistration;
 import org.osgi.util.tracker.ServiceTracker;
 
 import au.edu.uts.eng.remotelabs.schedserver.config.Config;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.DataAccessActivator;
 import au.edu.uts.eng.remotelabs.schedserver.logger.Logger;
 import au.edu.uts.eng.remotelabs.schedserver.logger.LoggerActivator;
 import au.edu.uts.eng.remotelabs.schedserver.rigprovider.identok.IdentityToken;
 import au.edu.uts.eng.remotelabs.schedserver.rigprovider.identok.impl.IdentityTokenRegister;
 import au.edu.uts.eng.remotelabs.schedserver.rigprovider.impl.RigEventServiceListener;
 import au.edu.uts.eng.remotelabs.schedserver.rigprovider.impl.StatusTimeoutChecker;
 import au.edu.uts.eng.remotelabs.schedserver.server.ServletContainer;
 import au.edu.uts.eng.remotelabs.schedserver.server.ServletContainerService;
 
 /**
  * Activator for the local rig provider bundle.
  */
 public class RigProviderActivator implements BundleActivator
 {
     /** Servlet container service registration. */
     private ServiceRegistration serverReg;
 
     /** Identity token service registration. */
     private ServiceRegistration idenTokReg;
     
     /** Rig status message timeout checker. */
     private StatusTimeoutChecker tmChecker;
     
     /** Runnable status timeout checker service registration. */
     private ServiceRegistration runnableReg;
     
     /** Rig event listeners list. */
     private static List<RigEventListener> listenerList;
     
     /** Configuration service tracker. */
     private static ServiceTracker configTracker;
     
     /** Logger. */
     private Logger logger;
     
     @Override
     public void start(final BundleContext context) throws Exception
     {
         this.logger = LoggerActivator.getLogger();
         this.logger.info("Starting " + context.getBundle().getSymbolicName() + " bundle.");
         
         RigProviderActivator.configTracker = new ServiceTracker(context, Config.class.getName(), null);
         RigProviderActivator.configTracker.open();
         
         /* Service to allow other bundles to obtain identity tokens for rigs. */
         Properties props = new Properties();
         props.put("provider", "local");
         this.idenTokReg = context.registerService(IdentityToken.class.getName(), 
                 IdentityTokenRegister.getInstance(), props);
         
         /* Service to run the status timeout checker every 30 seconds. */
         this.tmChecker = new StatusTimeoutChecker();
         props = new Properties();
         props.put("period", "30");
         this.runnableReg = context.registerService(Runnable.class.getName(), this.tmChecker, props);
         
         /* Service to host the local rig provider interface. */
         ServletContainerService service = new ServletContainerService();
         service.addServlet(new ServletContainer(new AxisServlet(), true));
         this.serverReg = context.registerService(ServletContainerService.class.getName(), service, null);
         
         /* Add service listener to add and remove registered rig event listeners. */
         RigProviderActivator.listenerList = new ArrayList<RigEventListener>();
         RigEventServiceListener listener = new RigEventServiceListener(RigProviderActivator.listenerList, context);
         context.addServiceListener(listener, '(' + Constants.OBJECTCLASS + '=' + RigEventListener.class.getName() + ')');
         
         /* Fire pseudo events for all registered services. */
         ServiceReference refs[] = context.getServiceReferences(RigEventListener.class.getName(), null);
         if (refs != null)
         {
             for (ServiceReference ref : refs)
             {
                 listener.serviceChanged(new ServiceEvent(ServiceEvent.REGISTERED, ref));
             }
         }
     }
 
     @Override
     public void stop(final BundleContext context) throws Exception
     {
         this.logger.info("Stopping " + context.getBundle().getSymbolicName() + " bundle.");
         this.serverReg.unregister();
         
         /* Clean up identity tokens. */
         this.idenTokReg.unregister();
         IdentityTokenRegister.getInstance().expunge();
         
         this.runnableReg.unregister();
         
         /* Take all rigs offline. */
         Session ses = DataAccessActivator.getNewSession();
         if (ses != null)
         {
            Query qu = ses.createQuery("UPDATE Rig SET active=:false, in_session=:false, online=:false, " +
             		"session_id=:null, offline_reason=:offline");
             qu.setBoolean("false", false);
             qu.setParameter("null", null, Hibernate.BIG_INTEGER);
             qu.setString("offline", "Scheduling Server shutting down.");
             
             ses.beginTransaction();
             int num = qu.executeUpdate();
             ses.getTransaction().commit();
             this.logger.info("Took " + num + " rigs offline for shutdown.");
             ses.close();
         }
         
         /* Cleanup the configuration service tracker. */
         RigProviderActivator.configTracker.close();
         RigProviderActivator.configTracker = null;
     }
     
     /**
      * Returns the specified configuration property value or if this bundle is
      * unloaded or the configuration property does not exist, the specified
      * default is returned.
      *  
      * @param prop configuration property
      * @param def default value
      * @return configured value or default
      */
     public static String getConfigurationProperty(String prop, String def)
     {
         if (RigProviderActivator.configTracker == null)
         {
             return def;
         }
         
         Config config = (Config)RigProviderActivator.configTracker.getService();
         if (config == null)
         {
             return def;
         }
         
         return config.getProperty(prop, def);
     }
     
     /**
      * Returns the list of registered rig state change event listeners.
      * 
      * @return list of event listeners
      */
     public static RigEventListener[] getRigEventListeners()
     {
         if (RigProviderActivator.listenerList == null)
         {
             return new RigEventListener[0];
         }
         
         synchronized (RigProviderActivator.listenerList)
         {
             return RigProviderActivator.listenerList.toArray(
                     new RigEventListener[RigProviderActivator.listenerList.size()]);
         }
     }
 }
