 package com.buglabs.support;
 
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.osgi.service.log.LogService;
 
 import com.buglabs.util.LogServiceUtil;
 
 /**
 * This class is meant to be subclassed by a virtual bug or real bug version it
  * is to facilitate the gathering of bug information currently for the use of
  * the /support web service (which is in com.buglabs.bug.emulator.base &
  * com.buglabs.bug.base)
  * 
  * @author bballantine
  * 
  */
 public abstract class SupportInfo {
 
 	private BundleContext context;
 
 	protected abstract String getKernelVersion();
 
 	protected abstract String getRootfsVersion();
 
 	public SupportInfo(BundleContext context) {
 		this.context = context;
 	}
 
 	/**
 	 * Main method to gather all the info and return as a formatted string
 	 * 
 	 * @param formatter
 	 * @return
 	 */
 	public String getInfo(ISupportInfoFormatter formatter) {
 		return formatter.buildResponse(getDescription(), getKernelVersion(), getRootfsVersion(), getJVMProperties(), getBundleVersions());
 	}
 
 	/**
 	 * helper for subclasses to return the log service for the given context
 	 * 
 	 * @return
 	 */
 	protected LogService getLogService() {
 		return LogServiceUtil.getLogService(context);
 	}
 
 	/**
 	 * Static description and timestamp
 	 * 
 	 * @return
 	 */
 	private String getDescription() {
 		return "BUG Support information.  This file generated at " + Calendar.getInstance().getTime().toString() + ".";
 	}
 
 	/**
 	 * Return all bundles and versions.
 	 */
 	private Map getBundleVersions() {
 		Map bundleVersions = new HashMap();
 		Bundle[] allBundles = context.getBundles();
 		for (int i = 0; i < allBundles.length; ++i) {
 			bundleVersions.put(allBundles[i].getHeaders().get("Bundle-Name"), allBundles[i].getHeaders().get("Bundle-Version"));
 		}
 		return bundleVersions;
 	}
 
 	/**
 	 * Return JVM Properties.
 	 */
 	private Map getJVMProperties() {
 		Map jvmProperties = new HashMap();
 		Iterator itr = System.getProperties().keySet().iterator();
 		String key;
 		while (itr.hasNext()) {
 			key = (String) itr.next();
 			jvmProperties.put(key, System.getProperties().getProperty(key));
 		}
 		return jvmProperties;
 	}
 
 }
