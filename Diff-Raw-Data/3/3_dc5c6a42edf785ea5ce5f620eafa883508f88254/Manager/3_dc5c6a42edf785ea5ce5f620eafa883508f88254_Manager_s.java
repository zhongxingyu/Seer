 /*******************************************************************************
  * Copyright (c) 2008, 2009 Bug Labs, Inc.
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *    - Redistributions of source code must retain the above copyright notice,
  *      this list of conditions and the following disclaimer.
  *    - Redistributions in binary form must reproduce the above copyright
  *      notice, this list of conditions and the following disclaimer in the
  *      documentation and/or other materials provided with the distribution.
  *    - Neither the name of Bug Labs, Inc. nor the names of its contributors may be
  *      used to endorse or promote products derived from this software without
  *      specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  *******************************************************************************/
 package com.buglabs.bug.bmi.pub;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.Dictionary;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.BundleException;
 import org.osgi.service.log.LogService;
 
 import com.buglabs.bug.module.pub.IModlet;
 import com.buglabs.bug.module.pub.IModletFactory;
 import com.buglabs.util.BugBundleConstants;
 
 /**
  * Manages logic of receiving messages from BMI and making changes to runtime.
  * runtime.
  * 
  * @author ken
  * 
  */
 public class Manager {
 	private static Manager ref;
 
 	private static LogService logService;
 
 	private static Map modletFactories;
 
 	private static Map activeModlets;
 
 	public static Map getActiveModlets() {
 		return activeModlets;
 	}
 
 	/**
 	 * @return
 	 */
 	synchronized public static Manager getManager() {
 		return ref;
 	}
 
 	synchronized static public Manager getManager(BundleContext context, LogService logService, Map modletFactories, Map activeModlets) {
 		if (ref == null) {
 			ref = new Manager(context, logService, modletFactories, activeModlets);
 		}
 
 		return ref;
 	}
 
 	private final BundleContext context;
 
 	private Manager(BundleContext context, LogService logService2, Map modletFactories, Map activeModlets) {
 		Manager.logService = logService2;
 		Manager.modletFactories = modletFactories;
 		Manager.activeModlets = activeModlets;
 		this.context = context;
 	}
 
 	/**
 	 * Iterate through all runtime bundles and see if a bundle exists that is a
 	 * Module Bundle of the type we need.
 	 * 
 	 * @param moduleId
 	 * @return
 	 */
 	private List findLocalBundles(String moduleId) {
 		Bundle[] bundles = context.getBundles();
 		List matches = new ArrayList();
 		String id;
 		for (int i = 0; i < bundles.length; ++i) {
 			Dictionary d = bundles[i].getHeaders();
 
 			if ((id = (String) d.get(BugBundleConstants.BUG_BUNDLE_MODULE_ID)) != null) {
 				if (id.equals(moduleId)) {
 					matches.add(bundles[i]);
 				}
 			}
 		}
 
 		return matches;
 	}
 
 	public List getAllModuleIds() {
 		List l = new ArrayList();
 
 		for (Iterator i = modletFactories.keySet().iterator(); i.hasNext();) {
 			l.add(i.next());
 		}
 
 		return l;
 	}
 
 	/**
 	 * This method is responsible for loading and starting any bundles that
 	 * provide Modlets for given module type.
 	 * 
 	 * After bundle(s) are started, those bundles expose IModulet services. The
 	 * BMI activator then listens for modlets. Upon new modlet creation, the
 	 * setup is called.
 	 * 
 	 * @param msg
 	 */
 	public void processMessage(String msg) {
 		BMIMessage message = new BMIMessage(msg);
 		List ml;
		boolean startedBundles = false;
 		try {
 			if (message.parse()) {
 
 				switch (message.getEvent()) {
 				case BMIMessage.EVENT_INSERT:
 					// first see if bundle is already installed.
 					List matchingBundles = findLocalBundles(message.getModuleId());
 
 					if (matchingBundles.size() > 0) {
 						// check to see if any are loaded into the slot.
 						for (Iterator i = matchingBundles.iterator(); i.hasNext();) {
 							Bundle b = (Bundle) i.next();
 
 							if (b.getState() != Bundle.ACTIVE) {
 								b.start();
 								logService.log(LogService.LOG_INFO, "Bundle " + b.getLocation() + " has been started to provide Modlets for module " + message.getModuleId());
								startedBundles = true;
 							}
 						}
 					} else {
 						// bundle is not installed, see if it can be retrieved
 						// from the cache.
 
 						logService.log(LogService.LOG_ERROR, "This case is not implemented; starting a module bundle that is not already in the runtime environment.");
 						return;
 					}
 
 					// Now that all the bundles associated with given module ID
 					// have been aquired and started, look for modlets to start.
 
 					ml = (List) modletFactories.get(message.getModuleId());
 
 					if (ml != null) {
 						for (Iterator i = ml.iterator(); i.hasNext();) {
 							IModletFactory mf = (IModletFactory) i.next();
 
 							// TODO we want to do some logic like get only the
 							// latest version of a given modlet factory.
 							IModlet m = mf.createModlet(context, message.getSlot());
 							try {
 								m.setup();
 							} catch (Exception e) {
 								logService.log(LogService.LOG_ERROR, "Unable to setup Modlet " + mf.getName() + ": " + e.getMessage());
 								continue;
 							}
 
 							m.start();
 							logService.log(LogService.LOG_INFO, "Started modlet from factory " + mf.getName() + "...");
 
 							// Add this model to our map of running Modlets.
 							if (!activeModlets.containsKey(m.getModuleId())) {
 								activeModlets.put(m.getModuleId(), new ArrayList());
 							}
 
 							List am = (List) activeModlets.get(m.getModuleId());
 
 							if (!am.contains(m)) {
 								am.add(m);
 							}
 						}
 					} else {
 						logService.log(LogService.LOG_ERROR, "No modlet factories support module: " + message.getModuleId());
 					}
 
 					break;
 				case BMIMessage.EVENT_REMOVE:
 					ml = (List) activeModlets.get(message.getModuleId());
 					List removalList = new ArrayList();
 
 					if (ml != null) {
 						for (Iterator i = ml.iterator(); i.hasNext();) {
 							IModlet m = (IModlet) i.next();
 							if (m.getSlotId() == message.getSlot()) {
 								logService.log(LogService.LOG_INFO, "Stopping modlet " + m.getModuleId() + "...");
 								m.stop();
 								removalList.add(m);
 							} else {
 								logService.log(LogService.LOG_INFO, "Ignoring " + m.getModuleId() + " in slot " + m.getSlotId());
 							}
 						}
 
 						for (Iterator i = removalList.iterator(); i.hasNext();) {
 							ml.remove(i.next());
 						}
 
 						removalList.clear();
 						logService.log(LogService.LOG_INFO, "Modlet cleanup complete.");
 					} else {
 						logService.log(LogService.LOG_WARNING, "There were no modlets loaded for " + message.getModuleId());
 					}
 					break;
 				}
 			} else {
 				throw new RuntimeException("Unable to parse message: " + msg);
 			}
 
 		} catch (BundleException e) {
 			logService.log(LogService.LOG_ERROR, "Bundle/Modlet error occurred: " + e.getClass().getName() + ", " + e.getMessage());
 			StringWriter sw = new StringWriter();
 			e.printStackTrace(new PrintWriter(sw));
 			logService.log(LogService.LOG_ERROR, sw.getBuffer().toString());
 
 			if (e.getNestedException() != null) {
 				logService.log(LogService.LOG_ERROR, "Nested Exception: " + e.getNestedException().getClass().getName() + ", " + e.getNestedException().getMessage());
 			}
 
 			e.printStackTrace();
 		} catch (Exception e) {
 
 			logService.log(LogService.LOG_ERROR, "Bundle/Modlet error occurred: " + e.getClass().getName() + ", " + e.getMessage());
 			StringWriter sw = new StringWriter();
 			e.printStackTrace(new PrintWriter(sw));
 			logService.log(LogService.LOG_ERROR, sw.getBuffer().toString());
 
 		}
 	}
 }
