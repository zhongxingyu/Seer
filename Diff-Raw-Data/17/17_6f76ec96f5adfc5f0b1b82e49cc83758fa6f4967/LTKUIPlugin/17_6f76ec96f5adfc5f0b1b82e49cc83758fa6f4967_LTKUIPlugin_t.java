 /*******************************************************************************
  * Copyright (c) 2009-2010 WalWare/StatET-Project (www.walware.de/goto/statet)
  * and others. All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Stephan Wahlbrink - initial API and implementation
  *******************************************************************************/
 
 package de.walware.ecommons.ltk.internal.ui;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.model.WorkbenchLabelProvider;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.statushandlers.StatusManager;
 import org.osgi.framework.BundleContext;
 
 import de.walware.ecommons.ICommonStatusConstants;
 import de.walware.ecommons.IDisposable;
 import de.walware.ecommons.ui.util.ImageRegistryUtil;
 
 import de.walware.ecommons.ltk.ui.LTKUI;
 
 
 public class LTKUIPlugin extends AbstractUIPlugin {
 	
 	
 	public static final String PLUGIN_ID = "de.walware.ecommons.ltk.ui"; //$NON-NLS-1$
 	
 	
 	/** The shared instance */
 	private static LTKUIPlugin gPlugin;
 	
 	/**
 	 * Returns the shared plug-in instance
 	 *
 	 * @return the shared instance
 	 */
 	public static LTKUIPlugin getDefault() {
 		return gPlugin;
 	}
 	
 	
 	private boolean fStarted;
 	
 	private final List<IDisposable> fDisposables = new ArrayList<IDisposable>();
 	
 	private WorkbenchLabelProvider fWorkbenchLabelProvider;
 	
 	
 	/**
 	 * The default constructor
 	 */
 	public LTKUIPlugin() {
 	}
 	
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void start(final BundleContext context) throws Exception {
 		super.start(context);
 		gPlugin = this;
 		
 		fStarted = true;
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void stop(final BundleContext context) throws Exception {
 		try {
 			synchronized (this) {
 				fStarted = false;
 				
 				if (fWorkbenchLabelProvider != null) {
					try {
						if (PlatformUI.isWorkbenchRunning() &&
								!PlatformUI.getWorkbench().isClosing()) {
							fWorkbenchLabelProvider.dispose();
						}
					}
					catch (Exception e) {
						StatusManager.getManager().handle(new Status(IStatus.ERROR, PLUGIN_ID, -1,
								"An error occurred when disposing the shared WorkbenchLabelProvider.", e));
 					}
 					fWorkbenchLabelProvider = null;
 				}
 			}
 			
 			for (final IDisposable listener : fDisposables) {
 				try {
 					listener.dispose();
 				}
 				catch (final Throwable e) {
 					getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, ICommonStatusConstants.INTERNAL_PLUGGED_IN, "Error occured when dispose module", e)); 
 				}
 			}
 			fDisposables.clear();
 		}
 		finally {
 			gPlugin = null;
 			super.stop(context);
 		}
 	}
 	
 	
 	public void addStoppingListener(final IDisposable listener) {
 		if (listener == null) {
 			throw new NullPointerException();
 		}
 		synchronized (this) {
 			if (!fStarted) {
 				throw new IllegalStateException("Plug-in is not started.");
 			}
 			fDisposables.add(listener);
 		}
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void initializeImageRegistry(final ImageRegistry reg) {
 		if (!fStarted) {
 			throw new IllegalStateException("Plug-in is not started.");
 		}
 		final ImageRegistryUtil util = new ImageRegistryUtil(this);
 		
 		util.register(LTKUI.OBJ_TEXT_TEMPLATE, ImageRegistryUtil.T_OBJ, "text-template.png"); //$NON-NLS-1$
 		util.register(LTKUI.OBJ_TEXT_AT_TAG, ImageRegistryUtil.T_OBJ, "text-at_tag.png"); //$NON-NLS-1$
 		util.register(LTKUI.OBJ_TEXT_LINKEDRENAME, ImageRegistryUtil.T_OBJ, "text-linked_rename.png"); //$NON-NLS-1$
 	}
 	
 	
 	/**
 	 * Access to resource decoration.
 	 */
 	public synchronized WorkbenchLabelProvider getWorkbenchLabelProvider() {
 		if (fWorkbenchLabelProvider == null) {
 			if (!fStarted) {
 				throw new IllegalStateException("Plug-in is not started.");
 			}
 			fWorkbenchLabelProvider = new WorkbenchLabelProvider();
 		}
 		return fWorkbenchLabelProvider;
 	}
 	
 }
