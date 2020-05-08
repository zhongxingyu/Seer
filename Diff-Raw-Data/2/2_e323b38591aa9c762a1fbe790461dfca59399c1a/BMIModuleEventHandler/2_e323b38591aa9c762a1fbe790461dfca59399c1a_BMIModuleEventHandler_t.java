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
 package com.buglabs.bug.bmi;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.BundleException;
 import org.osgi.service.log.LogService;
 
 import com.buglabs.bug.bmi.api.BMIModuleProperties;
 import com.buglabs.bug.bmi.api.IModlet;
 import com.buglabs.bug.bmi.api.IModletFactory;
 import com.buglabs.util.osgi.LogServiceUtil;
 
 /**
  * Manages logic of receiving messages from BMI and making changes to runtime.
  * runtime.
  * 
  * @author ken
  * 
  */
 public class BMIModuleEventHandler {
 	private static LogService logService;
 
 	private static Map<String, List<IModletFactory>> modletFactories;
 
 	private static Map<String, List<IModlet>> activeModlets;
 
 	/**
 	 * @return Map of active IModlets
 	 */
 	public static Map<String, List<IModlet>> getActiveModlets() {
 		return activeModlets;
 	}
 
 	private final BundleContext context;
 
 	/**
 	 * @param context BundleContext
 	 * @param logService log service
 	 * @param modletFactories map of modlet factories
 	 * @param activeModlets map of active modlets
 	 */
 	protected BMIModuleEventHandler(BundleContext context, LogService logService, Map<String, List<IModletFactory>> modletFactories, Map<String, List<IModlet>> activeModlets) {
 		BMIModuleEventHandler.logService = logService;
 		BMIModuleEventHandler.modletFactories = modletFactories;
 		BMIModuleEventHandler.activeModlets = activeModlets;
 		this.context = context;
 	}
 
 	/**
 	 * This method is responsible for loading and starting any bundles that
 	 * provide Modlets for given module type.
 	 * 
 	 * After bundle(s) are started, those bundles expose IModulet services. The
 	 * BMI activator then listens for modlets. Upon new modlet creation, the
 	 * setup is called.
 	 * 
 	 * @param event event to handle
 	 */
 	public void handleEvent(BMIModuleEvent event) {
 		if (!modletFactories.containsKey(event.getModuleId())) {
 			logService.log(LogService.LOG_ERROR, "No modlet factories support module, aborting event: " + event.getModuleId());
 			return;
 		}
 		
 		try {
 			List<IModlet> ml;
 			switch (event.getType()) {
 			case INSERT:
 
 				for (IModletFactory mf : modletFactories.get(event.getModuleId())) {
 					IModlet m = mf.createModlet(context, event.getSlot(), event.getBMIDevice());
 					try {
 						m.setup();
 					} catch (Exception e) {
 						logService.log(LogService.LOG_ERROR, "Unable to setup Modlet " + mf.getName() + ": " + e.getMessage(), e);
 						continue;
 					}
 
 					m.start();
 					logService.log(LogService.LOG_INFO, "Started modlet from factory " + mf.getName() + "...");
 
 					// Add this model to our map of running Modlets.
 					if (!activeModlets.containsKey(m.getModuleId())) {
 						activeModlets.put(m.getModuleId(), new ArrayList<IModlet>());
 					}
 
 					List<IModlet> am = activeModlets.get(m.getModuleId());
 
 					if (!am.contains(m)) {
 						am.add(m);
 					}
 				}
 
 				break;
 			case REMOVE:
 				List<IModlet> removalList = new ArrayList<IModlet>();
 				ml = activeModlets.get(event.getModuleId());
 				for (IModlet m : ml) {	
 					if (m.getSlotId() == event.getSlot()) {
 						logService.log(LogService.LOG_INFO, "Stopping modlet " + m.getModuleId() + "...");
 						m.stop();
 						removalList.add(m);
 					}
 				}
 
				for (IModlet m : removalList) 
 					ml.remove(m);
 				
 				removalList.clear();
 				logService.log(LogService.LOG_INFO, "Modlet removal complete.");
 			
 				break;
 			}
 		} catch (BundleException e) {
 			logService.log(LogService.LOG_ERROR
 					, "Bundle/Modlet error occurred: " + e.getClass().getName() + ", " + e.getMessage());
 			StringWriter sw = new StringWriter();
 			e.printStackTrace(new PrintWriter(sw));
 			logService.log(LogService.LOG_ERROR, sw.getBuffer().toString());
 
 			if (e.getNestedException() != null) {
 				logService.log(LogService.LOG_ERROR
 						, "Nested Exception: " + e.getNestedException().getClass().getName() + ", " + e.getNestedException().getMessage());
 			}
 
 			e.printStackTrace();
 		} catch (Exception e) {
 			logService.log(LogService.LOG_ERROR
 					, "Bundle/Modlet error occurred: " + e.getClass().getName() + ", " + e.getMessage());
 			StringWriter sw = new StringWriter();
 			e.printStackTrace(new PrintWriter(sw));
 			logService.log(LogService.LOG_ERROR, sw.getBuffer().toString());
 		}
 	}
 }
