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
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.Constants;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.ServiceEvent;
 import org.osgi.framework.ServiceListener;
 import org.osgi.framework.ServiceReference;
 import org.osgi.service.log.LogService;
 
 import com.buglabs.bug.bmi.pub.BMIMessage;
 import com.buglabs.bug.bmi.pub.Manager;
 import com.buglabs.bug.module.pub.BMIModuleProperties;
 import com.buglabs.bug.module.pub.IModlet;
 import com.buglabs.bug.module.pub.IModletFactory;
 import com.buglabs.util.osgi.FilterUtil;
 import com.buglabs.util.osgi.LogServiceUtil;
 
 public class Activator implements BundleActivator, ServiceListener {
 	private static final String DEFAULT_PIPE_FILENAME = "/tmp/eventpipe";
 
 	private static Activator ref;
 
 	public static Activator getRef() {
 		return ref;
 	}
 
 	private PipeReader pipeReader;
 
 	private String pipeFilename;
 
 	private LogService logService;
 
 	private Map modletFactories;
 
 	private Map activeModlets;
 
 	private BundleContext context;
 	
 
 	public void start(BundleContext context) throws Exception {
 		this.context = context;
 		Activator.ref = this;
 
 		modletFactories = new Hashtable();
 		activeModlets = new Hashtable();
 		logService = LogServiceUtil.getLogService(context);
 		
 		context.addServiceListener(this, FilterUtil.generateServiceFilter(IModletFactory.class.getName()));
 		registerExistingServices(context);
 
 		pipeFilename = context.getProperty("com.buglabs.pipename");
 
 		if (pipeFilename == null || pipeFilename.length() == 0) {
 			pipeFilename = DEFAULT_PIPE_FILENAME;
 		}
 
 		logService.log(LogService.LOG_INFO, "Creating pipe " + pipeFilename);
 		createPipe(pipeFilename);
 		pipeReader = new PipeReader(pipeFilename, Manager.getManager(context, logService, modletFactories, activeModlets), logService);
 
 		logService.log(LogService.LOG_INFO, "Initializing existing modules");
 
 		coldPlug();
 
 		logService.log(LogService.LOG_INFO, "Listening to event pipe. " + pipeFilename);
 
 		pipeReader.start();
 	}
 
 	public void stop(BundleContext context) throws Exception {
 		context.removeServiceListener(this);
 		stopModlets(activeModlets);
 		if (pipeReader != null) {
 			pipeReader.cancel();
 			pipeReader.interrupt();
 			logService.log(LogService.LOG_INFO, "Deleting pipe " + pipeFilename);
 			destroyPipe(new File(pipeFilename));
 		}
 		modletFactories.clear();
 	}
 
 	private void coldPlug() throws IOException {
 		Manager m = Manager.getManager();
 
 		List modules = getSysFSModules();
 		
 		if (modules != null) {
 			for (Iterator i = modules.iterator(); i.hasNext();) {
 				BMIMessage bmiMessage = (BMIMessage) i.next();
 				logService.log(LogService.LOG_INFO, "Registering existing module with message: " + bmiMessage.toString());
 				m.processMessage(bmiMessage);
 			}
 		}
 	}
 
 	private void createModlets(IModletFactory factory) throws Exception {
 		IModlet modlet = factory.createModlet(context, 0);
 		modlet.setup();
 		modlet.start();
 	}
 
 	/**
 	 * Create a pipe file by executing an external process. Requires that the
 	 * host system has the "mkfifo" program.
 	 * 
 	 * @param filename
 	 * @throws IOException
 	 */
 	private void createPipe(String filename) throws IOException {
 		File f = new File(filename);
 
 		// Check to see if file exists. If so delete and recreate to confirm
 		// it's a pipe.
 		if (f.exists()) {
 			logService.log(LogService.LOG_INFO, "Pipe " + f.getAbsolutePath() + " already exists, deleting.");
 			destroyPipe(f);
 		}
 		String cmd = "/usr/bin/mkfifo " + f.getAbsolutePath();
 
 		String error = execute(cmd);
 		logService.log(LogService.LOG_INFO, "Execution Completed.  Response: " + error);
 	}
 
 	
 	/**
 	 * Deletes a file
 	 * 
 	 * @param file
 	 * @throws IOException
 	 */
 	private void destroyPipe(File file) throws IOException {
 		if (!file.delete()) {
 			throw new IOException("Unable to delete file " + file.getAbsolutePath());
 		}
 		logService.log(LogService.LOG_INFO, "Deleted " + file.getAbsolutePath());
 	}
 
 	/**
 	 * @param cmd
 	 * @return null on success, or String of error message on failure.
 	 * @throws IOException
 	 */
 	private String execute(String cmd) throws IOException {
 		String s = null;
 		StringBuffer sb = new StringBuffer();
 		boolean hasError = false;
 		
 		//logService.log(LogService.LOG_DEBUG, "Executing: " + cmd);
 		Process p = Runtime.getRuntime().exec(cmd);
 		BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
 
 		while ((s = stdError.readLine()) != null) {
 			sb.append(s);
 			hasError = true;
 		}
 
 		if (hasError) {
 			// temp fix for ADS board. All commands return with this error.
 			if (!sb.toString().equals("Using fallback suid method")) {
 				new IOException("Failed to execute command: " + sb.toString());
 			}
 		}
 
 		BufferedReader stdOut = new BufferedReader(new InputStreamReader(p.getInputStream()));
 		sb = new StringBuffer();
 		while ((s = stdOut.readLine()) != null) {
 			sb.append(s);
 			sb.append('\n');
 		}
 
 		return sb.toString();
 	}
 
 	protected Map getActiveModlets() {
 		return activeModlets;
 	}
 
 	protected Map getModletFactories() {
 		return modletFactories;
 	}
 
 	/**
 	 * Get a list of BMIMessage strings for existing modules based on entries in
 	 * the /sys filesystem.
 	 * 
 	 * @return
 	 * @throws IOException
 	 */
 	private List getSysFSModules() throws IOException {
 		List slots = null;
 
 		for (int i = 0; i < 4; ++i) {
 			//this changed as of kernel 7e8ddd053c9a06fefb7e71a944846bdecb25c9ee, Oct 6, 2010
 			File prodFile = new File("/sys/class/bmi/bmi-"+i+"/bmi-dev-"+i+"/product");		
 			if (!prodFile.exists()) {
 				logService.log(LogService.LOG_DEBUG, "No module was found in slot " + i);
 				continue;
 			}
 			
 			// Lazily create data structure. If no modules then not needed.
 			if (slots == null) {
 				slots = new ArrayList();
 			}
 			
 			BMIModuleProperties props = BMIModuleProperties.createFromSYSDirectory(prodFile.getParentFile());
 
 			BMIMessage m = new BMIMessage(props, i);
 			
 			slots.add(m);
 		}
 
 		return slots;
 	}
 	
 	private boolean isEmpty(String element) {
 		return element == null || element.length() == 0;
 	}
 
 	private void registerExistingServices(BundleContext context2) throws InvalidSyntaxException {
		ServiceReference sr[] = context2.getServiceReferences((String) null, "(" + Constants.OBJECTCLASS + "=" + IModletFactory.class.getName() + ")");
 
 		if (sr != null) {
 			for (int i = 0; i < sr.length; ++i) {
 				registerService(sr[i], ServiceEvent.REGISTERED);
 			}
 		}
 	}
 
 	private void registerService(ServiceReference sr, int eventType) {
 		IModletFactory factory = (IModletFactory) context.getService(sr);
 
 		validateFactory(factory);
 
 		switch (eventType) {
 		case ServiceEvent.REGISTERED:
 			if (!modletFactories.containsKey(factory.getModuleId())) {
 				modletFactories.put(factory.getModuleId(), new ArrayList());
 			} else {
 				logService.log(LogService.LOG_WARNING, "IModletFactory " + factory.getName() + " is already registered, ignoring registration.");
 			}
 
 			List ml = (List) modletFactories.get(factory.getModuleId());
 
 			if (!ml.contains(factory)) {
 				ml.add(factory);
 			}
 			logService.log(LogService.LOG_INFO, "Added modlet factory " + factory.getName() + " (" + factory.getModuleId() + ") to map.");
 
 			// Discovery Mode needs to know of all services a BUG contains. This
 			// causes all available modlets to be created and started.
 			if (context.getProperty("com.buglabs.bug.discoveryMode") != null && context.getProperty("com.buglabs.bug.discoveryMode").equals("true")) {
 				try {
 					createModlets(factory);
 				} catch (Exception e) {
 					logService.log(LogService.LOG_ERROR, "Unable to start modlet in discovery mode: " + e.getMessage());
 				}
 			}
 
 			break;
 		case ServiceEvent.UNREGISTERING:
 			if (modletFactories.containsKey(factory.getModuleId())) {
 				List ml2 = (List) modletFactories.get(factory.getModuleId());
 
 				if (ml2.contains(factory)) {
 					ml2.remove(factory);
 				}
 			}
 			logService.log(LogService.LOG_INFO, "Removed modlet factory " + factory.getName() + " to map.");
 			break;
 		}
 	}
 
 	public void serviceChanged(ServiceEvent event) {
 		ServiceReference sr = event.getServiceReference();
 
 		registerService(sr, event.getType());
 	}
 
 	/**
 	 * Stop all active modlets.
 	 * 
 	 * @param activeModlets
 	 */
 	private void stopModlets(Map modlets) {
 		for (Iterator i = modlets.keySet().iterator(); i.hasNext();) {
 			String key = (String) i.next();
 
 			List modl = (List) modlets.get(key);
 
 			for (Iterator j = modl.iterator(); j.hasNext();) {
 				IModlet m = (IModlet) j.next();
 
 				try {
 					m.stop();
 				} catch (Exception e) {
 					logService.log(LogService.LOG_ERROR, "Error occured while stopping " + m.getModuleId() + ": " + e.getMessage());
 				}
 			}
 		}
 	}
 
 	private void validateFactory(IModletFactory factory) {
 		if (isEmpty(factory.getModuleId())) {
 			throw new RuntimeException("IModletFactory has empty Module ID.");
 		}
 
 		if (isEmpty(factory.getName())) {
 			throw new RuntimeException("IModletFactory has empty Name.");
 		}
 	}
 }
