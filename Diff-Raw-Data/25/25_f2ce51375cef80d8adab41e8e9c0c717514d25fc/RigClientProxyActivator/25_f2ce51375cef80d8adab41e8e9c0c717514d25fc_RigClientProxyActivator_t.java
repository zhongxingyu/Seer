 /**
  * SAHARA Scheduling Server
  *
  * Schedules and assigns local laboratory rigs.
  *
  * @license See LICENSE in the top level directory for complete license terms.
  *
  * Copyright (c) 2009, University of Technology, Sydney
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
  * @date 5th April 2009
  */
 
 package au.edu.uts.eng.remotelabs.schedserver.rigclientproxy;
 
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceReference;
 import org.osgi.util.tracker.ServiceTracker;
 
 import au.edu.uts.eng.remotelabs.schedserver.config.Config;
 import au.edu.uts.eng.remotelabs.schedserver.logger.Logger;
 import au.edu.uts.eng.remotelabs.schedserver.logger.LoggerActivator;
 import au.edu.uts.eng.remotelabs.schedserver.rigprovider.identok.IdentityToken;
 
 /**
  * The RigClient proxy bundles provides a client side implementation of the
  * RigClient SOAP interface.
  */
 public class RigClientProxyActivator implements BundleActivator 
 {
     /** The default async operation timeout in seconds. */
     public static final int DEFAULT_ASYNC_TIMEOUT = 60;
     
     /** Identity token register. */
     private static ServiceTracker idenTokTracker;
     
     /** The timeout in seconds for the response to be recieved from a SOAP
      *  operation. */
     private static int timeout;
     
     /** Logger. */
     private Logger logger;
     
     @Override
 	public void start(BundleContext context) throws Exception 
 	{
         this.logger = LoggerActivator.getLogger();
         this.logger.info("Rig client proxy bundle starting up...");
         
         RigClientProxyActivator.idenTokTracker = new ServiceTracker(context, IdentityToken.class.getName(), null);
         RigClientProxyActivator.idenTokTracker.open();        
        
        /* Load the service timeout. */
 	    ServiceReference confRef = context.getServiceReference(Config.class.getName());
 	    if (confRef == null)
 	    {
 	        RigClientProxyActivator.timeout = RigClientProxyActivator.DEFAULT_ASYNC_TIMEOUT;
 	        this.logger.info("Configuration service not loaded, using default async operation timeout of " +
 	                RigClientProxyActivator.timeout + " seconds.");
 	        return;
 	    }
 	    
 	    Config conf = (Config) context.getService(confRef);
 	    if (conf == null)
 	    {
 	        RigClientProxyActivator.timeout = RigClientProxyActivator.DEFAULT_ASYNC_TIMEOUT;
 	        this.logger.info("Configuration service not loaded, using default async operation timeout of " +
 	                RigClientProxyActivator.timeout + " seconds.");
 	        return;
 	    }
 	    
 	    try
 	    {
 	        RigClientProxyActivator.timeout = Integer.parseInt(conf.getProperty("Rig_Client_Async_Timeout"));
 	        this.logger.info("Rig client proxy async operation timeout is " + RigClientProxyActivator.timeout + 
 	                " seconds.");
 	    }
 	    catch (NumberFormatException ex)
 	    {
 	        RigClientProxyActivator.timeout = RigClientProxyActivator.DEFAULT_ASYNC_TIMEOUT;
 	        this.logger.info("Using default rig client proxy async operation timeout of " + 
 	                RigClientProxyActivator.DEFAULT_ASYNC_TIMEOUT + " seconds");
 	    }
 	}
 
	@Override
	public void stop(BundleContext context) throws Exception 
	{
	    this.logger.info("Rig client proxy bundle shutting down...");
	    if (RigClientProxyActivator.idenTokTracker != null)
	    {
	        RigClientProxyActivator.idenTokTracker.close();
	    }
	}

 	/**
 	 * Returns an identity token register instance if the identity token service
 	 * is registered. Returns if <code>null</code> if no identity token service 
 	 * is registered.
 	 * 
 	 * @return identity token register instance or null
 	 */
 	public static IdentityToken getIdentityTokenRegister()
 	{
 	    return (IdentityToken) RigClientProxyActivator.idenTokTracker.getService();
 	}
 	
 	/**
 	 * Returns the timeout in seconds for async SOAP operations.
 	 * 
 	 * @return timeout in seconds
 	 */
 	public static int getAsyncTimeout()
 	{
 	    return RigClientProxyActivator.timeout;
 	}
 }
