 package org.nchelp.meteor.provider.status;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.nchelp.meteor.provider.DataProvider;
 import org.nchelp.meteor.provider.DataProviderThread;
 import org.nchelp.meteor.provider.DistributedRegistry;
 import org.nchelp.meteor.provider.MeteorParameters;
 import org.nchelp.meteor.security.SecurityToken;
 import org.nchelp.meteor.util.MeteorConstants;
 import org.nchelp.meteor.util.Resource;
 import org.nchelp.meteor.util.ResourceFactory;
 import org.nchelp.meteor.util.exception.ParameterException;
 import org.nchelp.meteor.util.exception.SignatureException;
 
 public class StatusService {
 	private static transient Log log = LogFactory.getLog(StatusService.class);
 	private transient SecurityToken token;
 
 	/**
 	 * @param providers <code>java.util.List</code> containing Strings of the provider IDs
 	 * @return Document 
 	 */
 	public List callProviders(String[] providers) throws ParameterException {
 		
 		DistributedRegistry reg = DistributedRegistry.singleton();
 		
 		try {
 			token = reg.getAuthentication(SecurityToken.roleHELPDESK);
 		} catch (SignatureException e) {
 			throw new ParameterException(e);
 		}
 		
 		MeteorParameters params = new MeteorParameters();
 		params.setInstitutionID(this.getInstitutionID());
 		params.setRole(SecurityToken.roleHELPDESK);
 		params.setSsn(MeteorConstants.STATUSQUERY);
 		
 		ThreadGroup tg = new ThreadGroup("Meteor Status");
 		
 		List providerList = new ArrayList();
 		
 		log.info("Adding " + providers.length + " Providers to the Thread Group");
 		for(int i=0; i < providers.length; i++){
 			Provider p = new Provider();
 			providerList.add(p);
 			p.setSecurityToken(token);
 			p.setMeteorParameters(params);
 			DataProvider dp;
 			ProviderStatus ps = p.getProviderStatus();
 			dp = ps.getDataProvider();
 
 			if(dp == null){
 				dp = p.getDataProvider(providers[i]);
 				ps.setProvider(dp);
 			}
 			
 			log.debug("Adding Data Provider: " + dp.getId() + " to ThreadGroup");
 			DataProviderThread dpt = new DataProviderThread(tg, dp);
 			dpt.start();
 		}
 
 		int timeout = this.getDataProviderTimeout();
 		
 		try {
 			int elapsedTime = 0;
 			while (tg.activeCount() > 0 && elapsedTime < timeout)
 			{
 				try
 				{
 					Thread.sleep(100);
 				}
 				catch (InterruptedException ex)
 				{
 					log.error("Sleep was Interrupted!!");
 					break;
 				}
 				elapsedTime += 100;
 	
 				if (elapsedTime >= timeout)
 				{
 					log.info("Threadgroup terminating with an active count of: " + tg.activeCount());
 	
 					Thread[] t = new Thread[tg.activeCount()];
 					tg.enumerate(t);
 					for (int j = 0; j < t.length; j++)
 					{
 						Thread thread = t[j];
 						if (thread == null)
 							continue;
 	
 						if (log.isErrorEnabled())
 						{
							log.error("Thread '" + thread.toString() + "' is being forcefully interrupted");
 						}
 	
 						thread.interrupt();
 					}
 				}
 			}
 		} finally {
 			tg.interrupt();
 			if (!tg.isDestroyed())
 			{
 				try
 				{
 					tg.destroy();
 				}
 				catch (IllegalThreadStateException ex)
 				{
 					log.warn("ThreadGroup.destroy() Exception: " + ex.getMessage());
 				}
 			}
 		}
 		
 		return providerList;
 	}
 	
 	private String getInstitutionID() {
 		Resource res = ResourceFactory.createResource("authentication.properties");
 		return res.getProperty("authentication.identifier");
 	}
 
 	private int getDataProviderTimeout(){
 		int defaultTimeoutValue = 20000;
 
 		Resource res = ResourceFactory.createResource("accessprovider.properties");
 
 		// Get the timeout value with the default of 20 seonds
 		String strTimeout = res.getProperty("dataprovider.timeout", "20000");
 		int timeout = defaultTimeoutValue;
 
 		try
 		{
 			timeout = Integer.parseInt(strTimeout);
 		}
 		catch (NumberFormatException ex)
 		{
 			// If this throws an exception because the number
 			// wasn't correct, then rest it to the default
 			log.error("dataprovider.timeout was not a numeric value.  It is: " + strTimeout);
 			timeout = defaultTimeoutValue;
 		}
 
 		return timeout;
 	}
 }
