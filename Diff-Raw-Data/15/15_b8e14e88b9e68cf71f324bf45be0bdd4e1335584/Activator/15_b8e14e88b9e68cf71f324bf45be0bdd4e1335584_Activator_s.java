 /*
  * Copyright (c) 2011: Edmund Wagner, Wolfram Weidel
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * * Redistributions of source code must retain the above copyright
  * notice, this list of conditions and the following disclaimer.
  * * Redistributions in binary form must reproduce the above copyright
  * notice, this list of conditions and the following disclaimer in the
  * documentation and/or other materials provided with the distribution.
  * * Neither the name of the jeconfig nor the
  * names of its contributors may be used to endorse or promote products
  * derived from this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package org.jeconfig.aspect.internal;
 
 import org.jeconfig.api.IConfigService;
 import org.jeconfig.api.autosave.IConfigAutoSaveService;
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 import org.osgi.util.tracker.ServiceTracker;
 
 public class Activator implements BundleActivator {
 	private static Activator instance = new Activator();
 
 	private BundleContext bundleContext;
 	private ServiceTracker configServiceTracker = null;
 	private ServiceTracker configAutoSaveServiceTracker = null;
 	private IConfigService configService = null;
 	private IConfigAutoSaveService configAutoSaveService = null;
 
 	public void setConfigService(final IConfigService configService) {
 		this.configService = configService;
 	}
 
 	public void setConfigAutoSaveService(final IConfigAutoSaveService configAutoSaveService) {
 		this.configAutoSaveService = configAutoSaveService;
 	}
 
 	@Override
 	public void start(final BundleContext context) throws Exception {
 		bundleContext = context;
 		configServiceTracker = new ServiceTracker(bundleContext, IConfigService.class.getName(), null);
 		configServiceTracker.open();
 
 		configAutoSaveServiceTracker = new ServiceTracker(bundleContext, IConfigAutoSaveService.class.getName(), null);
 		configAutoSaveServiceTracker.open();
 		instance = this;
 	}
 
 	public IConfigService getConfigService() {
 		if (configServiceTracker != null) {
 			final IConfigService cfgService = (IConfigService) configServiceTracker.getService();
 			if (cfgService == null) {
 				throw new RuntimeException("Didn't find " + IConfigService.class.getSimpleName() + "!"); //$NON-NLS-1$//$NON-NLS-2$
 			}
			return cfgService;
 		} else {
 			if (configService != null) {
 				return configService;
 			}
 			throw new RuntimeException("Didn't find " + IConfigService.class.getSimpleName() + "!"); //$NON-NLS-1$//$NON-NLS-2$
 		}
 	}
 
 	public IConfigAutoSaveService getConfigAutoSaveService() {
 		if (configAutoSaveServiceTracker != null) {
 			final IConfigAutoSaveService cfgAutoSaveService = (IConfigAutoSaveService) configAutoSaveServiceTracker.getService();
 			if (cfgAutoSaveService == null) {
 				throw new RuntimeException("Didn't find " + IConfigAutoSaveService.class.getSimpleName() + "!"); //$NON-NLS-1$//$NON-NLS-2$
 			}
 		} else {
 			if (configAutoSaveService != null) {
 				return configAutoSaveService;
 			} else {
 				throw new RuntimeException("Didn't find " + IConfigAutoSaveService.class.getSimpleName() + "!"); //$NON-NLS-1$//$NON-NLS-2$
 			}
 		}

		return configAutoSaveService;
 	}
 
 	@Override
 	public void stop(final BundleContext context) throws Exception {
 		bundleContext = null;
 	}
 
 	public static Activator getInstance() {
 		return instance;
 	}
 
 	public BundleContext getBundleContext() {
 		return bundleContext;
 	}
 }
