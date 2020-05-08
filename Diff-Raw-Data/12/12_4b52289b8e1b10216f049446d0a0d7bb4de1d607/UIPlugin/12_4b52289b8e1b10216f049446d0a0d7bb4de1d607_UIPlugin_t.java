 /*******************************************************************************
  * Copyright (c) 2011, 2013 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.ui.activator;
 
 import java.net.URL;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.ListenerList;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.resource.ImageRegistry;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.tcf.protocol.Protocol;
 import org.eclipse.tcf.te.core.async.AsyncCallbackCollector;
 import org.eclipse.tcf.te.runtime.concurrent.util.ExecutorsUtil;
 import org.eclipse.tcf.te.runtime.interfaces.callback.ICallback;
 import org.eclipse.tcf.te.runtime.interfaces.properties.IPropertiesContainer;
 import org.eclipse.tcf.te.runtime.properties.PropertiesContainer;
 import org.eclipse.tcf.te.runtime.services.ServiceManager;
 import org.eclipse.tcf.te.runtime.stepper.interfaces.IStepContext;
 import org.eclipse.tcf.te.runtime.stepper.interfaces.IStepperService;
 import org.eclipse.tcf.te.runtime.stepper.job.StepperJob;
 import org.eclipse.tcf.te.tcf.core.Tcf;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.ILocatorModel;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
 import org.eclipse.tcf.te.tcf.locator.model.Model;
 import org.eclipse.tcf.te.tcf.ui.internal.ImageConsts;
 import org.eclipse.tcf.te.ui.jface.images.AbstractImageDescriptor;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchListener;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.osgi.framework.BundleContext;
 
 
 /**
  * The activator class controls the plug-in life cycle
  */
 public class UIPlugin extends AbstractUIPlugin {
 	// The shared instance
 	private static UIPlugin plugin;
 	// The workbench listener instance
 	private IWorkbenchListener listener;
 	// Reference to the workbench listener
 	/* default */ final ListenerList listeners = new ListenerList();
 
 	/**
 	 * Constructor.
 	 */
 	public UIPlugin() {
 	}
 
 	/**
 	 * Returns the shared instance
 	 *
 	 * @return the shared instance
 	 */
 	public static UIPlugin getDefault() {
 		return plugin;
 	}
 
 	/**
 	 * Convenience method which returns the unique identifier of this plugin.
 	 */
 	public static String getUniqueIdentifier() {
 		if (getDefault() != null && getDefault().getBundle() != null) {
 			return getDefault().getBundle().getSymbolicName();
 		}
 		return "org.eclipse.tcf.te.tcf.ui"; //$NON-NLS-1$
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
 	 */
 	@Override
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 		plugin = this;
 
 		// Create and register the workbench listener instance
 		listener = new IWorkbenchListener() {
 
 			@Override
 			public boolean preShutdown(IWorkbench workbench, boolean forced) {
 				boolean proceedShutdown = true;
 
 				// If there are workbench listener registered here, than
 				// invoke them now before closing all the channels.
 				Object[] candidates = listeners.getListeners();
 				for (Object listener : candidates) {
 					if (!(listener instanceof IWorkbenchListener)) continue;
 					proceedShutdown &= ((IWorkbenchListener)listener).preShutdown(workbench, forced);
 					if (!proceedShutdown && !forced) break;
 				}
 
 				if (proceedShutdown || forced) {
 					// Terminate the scanner
 					final ILocatorModel model = Model.getModel(true);
					if (model != null) {
						Runnable runnable = new Runnable() {
							@Override
							public void run() {
								model.getScanner().terminate();
							}
						};

						Assert.isTrue(!Protocol.isDispatchThread());
						Protocol.invokeAndWait(runnable);
					}
 
 					// Disconnect all connected connections via the stepper service
 					if (model != null) {
 						final AsyncCallbackCollector collector = new AsyncCallbackCollector();
 
 						Runnable runnable = new Runnable() {
 							@Override
 							public void run() {
 								// Get all peer model objects
 								IPeerModel[] peers = model.getPeers();
 								// Loop them and check if disconnect is available
 								for (IPeerModel peerModel : peers) {
 									IStepperService service = ServiceManager.getInstance().getService(peerModel, IStepperService.class);
 									if (service != null) {
 										String stepGroupId = service.getStepGroupId(peerModel, IStepperService.OPERATION_DISCONNECT);
 										IStepContext stepContext = service.getStepContext(peerModel, IStepperService.OPERATION_DISCONNECT);
 										String name = service.getStepGroupName(peerModel, IStepperService.OPERATION_DISCONNECT);
 										boolean isEnabled = service.isEnabled(peerModel, IStepperService.OPERATION_DISCONNECT);
 
 										if (isEnabled && stepGroupId != null && stepContext != null) {
 											IPropertiesContainer data = new PropertiesContainer();
 											StepperJob job = new StepperJob(name != null ? name : "", //$NON-NLS-1$
 															stepContext,
 															data,
 															stepGroupId,
 															IStepperService.OPERATION_DISCONNECT,
 															false);
 
 											ICallback callback = new AsyncCallbackCollector.SimpleCollectorCallback(collector);
 											job.setJobCallback(callback);
 
 											job.schedule();
 										}
 									}
 								}
 
 								collector.initDone();
 							}
 						};
 
 						Assert.isTrue(!Protocol.isDispatchThread());
 						Protocol.invokeAndWait(runnable);
 
 						ExecutorsUtil.waitAndExecute(0, collector.getConditionTester());
 					}
 
 					// Close all channels now
 					Tcf.getChannelManager().closeAll(!Protocol.isDispatchThread());
 				}
 
 				return proceedShutdown;
 			}
 
 			@Override
 			public void postShutdown(IWorkbench workbench) {
 				// If there are workbench listener registered here, than invoke them now.
 				Object[] candidates = listeners.getListeners();
 				for (Object listener : candidates) {
 					if (!(listener instanceof IWorkbenchListener)) continue;
 					((IWorkbenchListener)listener).postShutdown(workbench);
 				}
 			}
 		};
 		PlatformUI.getWorkbench().addWorkbenchListener(listener);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
 	 */
 	@Override
 	public void stop(BundleContext context) throws Exception {
 		plugin = null;
 		if (listener != null) { PlatformUI.getWorkbench().removeWorkbenchListener(listener); listener = null; }
 		super.stop(context);
 	}
 
 	/**
 	 * Adds the given workbench listener.
 	 * <p>
 	 * Has not effect if the same listener is already registered.
 	 *
 	 * @param listener The listener. Must not be <code>null</code>.
 	 */
 	public void addListener(IWorkbenchListener listener) {
     	Assert.isNotNull(listener);
     	listeners.add(listener);
 	}
 
 	/**
 	 * Removes the given workbench listener.
 	 * <p>
 	 * Has no effect if the same listener was not already registered.
 	 *
 	 * @param listener The listener. Must not be <code>null</code>.
 	 */
 	public void removeListener(IWorkbenchListener listener) {
     	Assert.isNotNull(listener);
     	listeners.remove(listener);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#initializeImageRegistry(org.eclipse.jface.resource.ImageRegistry)
 	 */
 	@Override
 	protected void initializeImageRegistry(ImageRegistry registry) {
 		URL url = UIPlugin.getDefault().getBundle().getEntry(ImageConsts.IMAGE_DIR_ROOT + ImageConsts.IMAGE_DIR_OBJ + "peer.gif"); //$NON-NLS-1$
 		registry.put(ImageConsts.PEER, ImageDescriptor.createFromURL(url));
 		url = UIPlugin.getDefault().getBundle().getEntry(ImageConsts.IMAGE_DIR_ROOT + ImageConsts.IMAGE_DIR_OBJ + "peer_discovered.gif"); //$NON-NLS-1$
 		registry.put(ImageConsts.PEER_DISCOVERED, ImageDescriptor.createFromURL(url));
 		url = UIPlugin.getDefault().getBundle().getEntry(ImageConsts.IMAGE_DIR_ROOT + ImageConsts.IMAGE_DIR_OBJ + "discovery_root.gif"); //$NON-NLS-1$
 		registry.put(ImageConsts.DISCOVERY_ROOT, ImageDescriptor.createFromURL(url));
 
 		url = UIPlugin.getDefault().getBundle().getEntry(ImageConsts.IMAGE_DIR_ROOT + ImageConsts.IMAGE_DIR_OVR + "gold_ovr.gif"); //$NON-NLS-1$
 		registry.put(ImageConsts.GOLD_OVR, ImageDescriptor.createFromURL(url));
 		url = UIPlugin.getDefault().getBundle().getEntry(ImageConsts.IMAGE_DIR_ROOT + ImageConsts.IMAGE_DIR_OVR + "green_ovr.gif"); //$NON-NLS-1$
 		registry.put(ImageConsts.GREEN_OVR, ImageDescriptor.createFromURL(url));
 		url = UIPlugin.getDefault().getBundle().getEntry(ImageConsts.IMAGE_DIR_ROOT + ImageConsts.IMAGE_DIR_OVR + "grey_ovr.gif"); //$NON-NLS-1$
 		registry.put(ImageConsts.GREY_OVR, ImageDescriptor.createFromURL(url));
 		url = UIPlugin.getDefault().getBundle().getEntry(ImageConsts.IMAGE_DIR_ROOT + ImageConsts.IMAGE_DIR_OVR + "red_ovr.gif"); //$NON-NLS-1$
 		registry.put(ImageConsts.RED_OVR, ImageDescriptor.createFromURL(url));
 		url = UIPlugin.getDefault().getBundle().getEntry(ImageConsts.IMAGE_DIR_ROOT + ImageConsts.IMAGE_DIR_OVR + "redX_ovr.gif"); //$NON-NLS-1$
 		registry.put(ImageConsts.RED_X_OVR, ImageDescriptor.createFromURL(url));
 		url = UIPlugin.getDefault().getBundle().getEntry(ImageConsts.IMAGE_DIR_ROOT + ImageConsts.IMAGE_DIR_OVR + "link_ovr.gif"); //$NON-NLS-1$
 		registry.put(ImageConsts.LINK_OVR, ImageDescriptor.createFromURL(url));
 
 		url = UIPlugin.getDefault().getBundle().getEntry(ImageConsts.IMAGE_DIR_ROOT + ImageConsts.IMAGE_DIR_DTOOL + "run_exc.gif"); //$NON-NLS-1$
 		registry.put(ImageConsts.RUN_DISABLED, ImageDescriptor.createFromURL(url));
 		url = UIPlugin.getDefault().getBundle().getEntry(ImageConsts.IMAGE_DIR_ROOT + ImageConsts.IMAGE_DIR_ETOOL + "run_exc.gif"); //$NON-NLS-1$
 		registry.put(ImageConsts.RUN_ENABLED, ImageDescriptor.createFromURL(url));
 	}
 
 	/**
 	 * Loads the image registered under the specified key from the image
 	 * registry and returns the <code>Image</code> object instance.
 	 *
 	 * @param key The key the image is registered with.
 	 * @return The <code>Image</code> object instance or <code>null</code>.
 	 */
 	public static Image getImage(String key) {
 		return getDefault().getImageRegistry().get(key);
 	}
 
 	/**
 	 * Loads the image registered under the specified key from the image
 	 * registry and returns the <code>ImageDescriptor</code> object instance.
 	 *
 	 * @param key The key the image is registered with.
 	 * @return The <code>ImageDescriptor</code> object instance or <code>null</code>.
 	 */
 	public static ImageDescriptor getImageDescriptor(String key) {
 		return getDefault().getImageRegistry().getDescriptor(key);
 	}
 
 	/**
 	 * Loads the image given by the specified image descriptor from the image
 	 * registry. If the image has been loaded ones before already, the cached
 	 * <code>Image</code> object instance is returned. Otherwise, the <code>
 	 * Image</code> object instance will be created and cached before returned.
 	 *
 	 * @param descriptor The image descriptor.
 	 * @return The corresponding <code>Image</code> object instance or <code>null</code>.
 	 */
 	public static Image getSharedImage(AbstractImageDescriptor descriptor) {
 		ImageRegistry registry = getDefault().getImageRegistry();
 
 		String imageKey = descriptor.getDecriptorKey();
 		Image image = registry.get(imageKey);
 		if (image == null) {
 			registry.put(imageKey, descriptor);
 			image = registry.get(imageKey);
 		}
 
 		return image;
 	}
 }
