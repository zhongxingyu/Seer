 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  ******************************************************************************/
 package org.eclipse.emf.emfstore.internal.server.model.provider;
 
 import java.util.ArrayList;
 import java.util.List;
 
import org.eclipse.emf.emfstore.common.IDisposable;
 import org.eclipse.emf.emfstore.common.extensionpoint.ExtensionElement;
import org.eclipse.emf.emfstore.common.extensionpoint.ExtensionPoint;
 import org.eclipse.emf.emfstore.common.extensionpoint.ExtensionPointException;
 import org.eclipse.emf.emfstore.internal.common.model.util.ModelUtil;
 import org.eclipse.emf.emfstore.internal.server.model.versioning.operations.AbstractOperation;
 
 /**
  * Class for managing customLabelProvider for specified operations.
  * 
  * @author Michael Kagel
  * @author emueller
  */
 public final class CustomOperationLabelProviderManager implements IDisposable {
 
 	/**
 	 * Initializes the singleton instance statically.
 	 */
 	private static class SingletonHolder {
 		public static final CustomOperationLabelProviderManager INSTANCE = new CustomOperationLabelProviderManager();
 	}
 
 	/**
 	 * Returns the singleton instance.
 	 * 
 	 * @return The DragSourcePlaceHolder instance
 	 */
 	public static CustomOperationLabelProviderManager getInstance() {
 		return SingletonHolder.INSTANCE;
 	}
 
 	private List<AbstractOperationCustomLabelProvider> list;
 
 	/**
 	 * Constructor.
 	 */
 	private CustomOperationLabelProviderManager() {
 		list = new ArrayList<AbstractOperationCustomLabelProvider>();
 		collectExtensions();
 	}
 
 	/**
 	 * Provides a customLabelProvider for a specified operation.
 	 * 
 	 * @param operation
 	 *            for which the method should provide a customLabelProvider.
 	 * @return The customLabelProvider for the given operation or null if there
 	 *         is no customLabelProvider.
 	 */
 	public AbstractOperationCustomLabelProvider getCustomLabelProvider(AbstractOperation operation) {
 
 		AbstractOperationCustomLabelProvider highestVisualizer = null;
 
 		int highestNumber = 0;
 		int currentRenderState = 0;
 
 		for (AbstractOperationCustomLabelProvider changePackageVisualizer : list) {
 			currentRenderState = changePackageVisualizer.canRender(operation);
 			// Take the highest provider
 			if (currentRenderState > highestNumber) {
 				highestNumber = currentRenderState;
 				highestVisualizer = changePackageVisualizer;
 			}
 		}
 
 		return highestVisualizer;
 	}
 
 	private void collectExtensions() {
 		for (ExtensionElement element : new ExtensionPoint(
			"org.eclipse.emf.emfstore.internal.server.model.edit.customOperationLabelProvider", true)
			.getExtensionElements()) {
 			try {
 				AbstractOperationCustomLabelProvider provider = element.getClass("class",
 					AbstractOperationCustomLabelProvider.class);
 				list.add(provider);
 			} catch (ExtensionPointException e) {
 				ModelUtil.logException("Exception occured while initializing custom label provider extensions!", e);
 			}
 		}
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.common.IDisposable#dispose()
 	 */
 	public void dispose() {
 		list = null;
 	}
 }
