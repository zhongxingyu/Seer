 /*******************************************************************************
  * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.ui.internal.adapters;
 
 import org.eclipse.core.runtime.IAdapterFactory;
 import org.eclipse.jface.viewers.ILabelProvider;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
 import org.eclipse.tcf.te.tcf.ui.navigator.DelegatingLabelProvider;
 import org.eclipse.tcf.te.ui.views.interfaces.categories.ICategorizable;
import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IPersistableElement;
 
 /**
  * Adapter factory implementation.
  */
 public class AdapterFactory implements IAdapterFactory {
 	// The adapter for ILabelProvider.class
 	private final DelegatingLabelProvider labelProvider = new DelegatingLabelProvider();
 
 	// The adapter class.
 	private Class<?>[] adapters = {
 					ILabelProvider.class,
 					IPersistableElement.class,
 					ICategorizable.class,
 					IPeerModel.class
 				};
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
 	 */
 	@Override
 	public Object getAdapter(Object adaptableObject, Class adapterType) {
 		if (adaptableObject instanceof IPeerModel) {
 			if (ILabelProvider.class.equals(adapterType)) {
 				return labelProvider;
 			}
 			if (IPersistableElement.class.equals(adapterType)) {
 				return new PersistablePeerModel((IPeerModel)adaptableObject);
 			}
 			if (ICategorizable.class.equals(adapterType)) {
 				return new CategorizableAdapter(adaptableObject);
 			}
 
 		}
 
		if (adaptableObject instanceof IEditorInput) {
			return ((IEditorInput)adaptableObject).getAdapter(adapterType);
 		}
 
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
 	 */
 	@Override
 	public Class[] getAdapterList() {
 		return adapters;
 	}
 
 }
