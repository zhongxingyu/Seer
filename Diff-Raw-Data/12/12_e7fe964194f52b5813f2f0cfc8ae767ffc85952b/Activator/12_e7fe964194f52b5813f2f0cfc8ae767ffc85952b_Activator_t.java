 /*******************************************************************************
  *  Copyright (c) 2010 Weltevree Beheer BV, Remain Software & Industrial-TSI
  *                                                                      
  * All rights reserved. This program and the accompanying materials     
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at             
  * http://www.eclipse.org/legal/epl-v10.html                            
  *                                                                      
  * Contributors:                                                        
  *    Wim Jongman - initial API and implementation
  *******************************************************************************/
 package org.eclipse.ecf.salvo.ui.internal;
 
 import java.io.File;
 import java.net.URL;
 import java.util.Enumeration;
 
 import org.eclipse.ecf.protocol.nntp.core.Debug;
 import org.eclipse.ecf.protocol.nntp.core.StoreStore;
 import org.eclipse.ecf.protocol.nntp.core.UpdateRunner;
 import org.eclipse.ecf.protocol.nntp.model.ISecureStore;
 import org.eclipse.ecf.protocol.nntp.model.IStore;
 import org.eclipse.ecf.protocol.nntp.model.IStoreFactory;
 import org.eclipse.ecf.protocol.nntp.model.SALVO;
 import org.eclipse.ecf.protocol.nntp.model.StoreException;
 import org.eclipse.ecf.provider.nntp.security.SalvoSecureStore;
 import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceEvent;
 import org.osgi.framework.ServiceListener;
 import org.osgi.framework.ServiceReference;
 
 public class Activator extends AbstractUIPlugin implements ServiceListener {
 
 	private static Activator plugin;
 
 	private UpdateRunner updateRunner;
 
 	private BundleContext context;
 
 	public Activator() {
 	}
 
 	public static Activator getDefault() {
 		if (plugin == null)
 			plugin = new Activator();
 		return plugin;
 	}
 
 	@Override
 	public void start(BundleContext context) throws Exception {
 
 		// Other init
 		super.start(context);
 		this.context = context;
 		plugin = this;
 
 		// Make sure we have a home
 		File file = new File(SALVO.SALVO_HOME);
 		if (!file.exists())
 			file.mkdir();
 
 		// Find running stores
 		ServiceReference[] serviceReferences = context.getServiceReferences(
 				IStoreFactory.class.getName(), null);
 		if (serviceReferences != null) {
 			for (ServiceReference serviceReference : serviceReferences) {
 				registerStore(serviceReference);
 			}
 		}
 
 		// Catch store services
 		context.addServiceListener(this);
 
		// The UI thread must start the update thread to avoid a deadlock
		// See bug: https://bugs.eclipse.org/bugs/show_bug.cgi?id=344468
		Display.getCurrent().asyncExec(new Runnable() {
			
			public void run() {
				startUpdateThread();
			}
		});
 
 	}
 
 	private void startUpdateThread() {
 
 		if (updateRunner == null)
 			updateRunner = new UpdateRunner();
 
 		if (!updateRunner.isThreadRunning())
 			updateRunner.start();
 
 	}
 
 	@Override
 	protected void initializeImageRegistry(ImageRegistry reg) {
 		Enumeration<?> findEntries = getBundle().findEntries("icons", "*.gif",
 				true);
 		while (findEntries.hasMoreElements()) {
 			File file;
 			file = new File(((URL) findEntries.nextElement()).getFile());
 			reg.put(file.getName(),
 					imageDescriptorFromPlugin(this.getBundle()
 							.getSymbolicName(), "icons/" + file.getName()));
 		}
 	}
 
 	@Override
 	public void stop(BundleContext context) throws Exception {
 		super.stop(context);
 		updateRunner.stop();
 	}
 
 	public void serviceChanged(ServiceEvent event) {
 
 		// Register a store
 		if (event.getType() == ServiceEvent.REGISTERED) {
 			if (context.getService(event.getServiceReference()) instanceof IStoreFactory) {
 				registerStore(event.getServiceReference());
 			}
 		}
 
 		// Unregister a store
 		if (event.getType() == ServiceEvent.UNREGISTERING) {
 			if (context.getService(event.getServiceReference()) instanceof IStoreFactory) {
 				IStoreFactory factory = (IStoreFactory) context
 						.getService(event.getServiceReference());
 				Debug.log(getClass(), "Lost store factory "
 						+ factory.getClass().getName());
 				try {
 					IStore store = factory.createStore(SALVO.SALVO_HOME);
 					StoreStore.instance().unregisterStore(store);
 					Debug.log(getClass(),
 							"Unregistered store " + store.getDescription());
 				} catch (StoreException e) {
 					Debug.log(getClass(), e);
 				}
 			}
 		}
 	}
 
 	private void registerStore(ServiceReference serviceReference) {
 		IStoreFactory factory = (IStoreFactory) context
 				.getService(serviceReference);
 		ISecureStore prefs = new SalvoSecureStore();
 		Debug.log(getClass(), "Found store factory "
 				+ factory.getClass().getName());
 		try {
 			IStore store = factory.createStore(SALVO.SALVO_HOME);
 			store.setSecureStore(prefs);
 			StoreStore.instance().registerStore(store);
 			Debug.log(getClass(), "Registered store " + store.getDescription());
 		} catch (StoreException e) {
 			Debug.log(getClass(), e);
 		}
 	}
 }
