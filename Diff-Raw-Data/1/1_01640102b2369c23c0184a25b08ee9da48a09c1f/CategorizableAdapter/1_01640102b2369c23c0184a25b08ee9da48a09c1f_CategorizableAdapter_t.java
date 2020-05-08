 /*******************************************************************************
  * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.ui.internal.adapters;
 
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.tcf.protocol.Protocol;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
 import org.eclipse.tcf.te.ui.views.Managers;
 import org.eclipse.tcf.te.ui.views.interfaces.ICategory;
 import org.eclipse.tcf.te.ui.views.interfaces.IUIConstants;
 import org.eclipse.tcf.te.ui.views.interfaces.categories.ICategorizable;
 
 /**
  * Categorizable element adapter implementation
  */
 public class CategorizableAdapter implements ICategorizable {
 	// Reference to the adapted element
 	/* default */ final Object element;
 
 	/**
      * Constructor.
      *
      * @param element The adapted element. Must not be <code>null</code>.
      */
     public CategorizableAdapter(Object element) {
     	Assert.isNotNull(element);
     	this.element = element;
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.tcf.te.ui.views.interfaces.categories.ICategorizable#getId()
      */
     @Override
     public String getId() {
 		if (element instanceof IPeerModel) {
 			return ((IPeerModel)element).getPeerId();
 		}
 	    return null;
 	}
 
     /* (non-Javadoc)
      * @see org.eclipse.tcf.te.ui.views.interfaces.categories.ICategorizable#isValid(org.eclipse.tcf.te.ui.views.interfaces.categories.ICategorizable.OPERATION, org.eclipse.tcf.te.ui.views.interfaces.ICategory, org.eclipse.tcf.te.ui.views.interfaces.ICategory)
      */
     @Override
     public boolean isValid(OPERATION operation, ICategory parentCategory, ICategory category) {
     	Assert.isNotNull(operation);
     	Assert.isNotNull(category);
 
     	if (element instanceof IPeerModel) {
     		// ADD: Parent and destination category are the same -> not valid
     		if (OPERATION.ADD.equals(operation) && category.equals(parentCategory)) return false;
 
     		// ALL: Static peer's cannot be removed from or added to "My Targets"
     		if (IUIConstants.ID_CAT_MY_TARGETS.equals(category.getId())) {
     			final AtomicBoolean isStatic = new AtomicBoolean();
 
     			Runnable runnable = new Runnable() {
     				@Override
     				public void run() {
     					String value = ((IPeerModel)element).getPeer().getAttributes().get("static.transient"); //$NON-NLS-1$
     					isStatic.set(value != null && Boolean.parseBoolean(value.trim()));
     				}
     			};
 
     			if (Protocol.isDispatchThread()) runnable.run();
     			else Protocol.invokeAndWait(runnable);
 
     			if (isStatic.get()) return false;
     		}
 
     		// ALL: Destination is "Neighborhood" -> not valid
     		if (IUIConstants.ID_CAT_NEIGHBORHOOD.equals(category.getId())) return false;
 
     		return true;
     	}
 
         return false;
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.tcf.te.ui.views.interfaces.categories.ICategorizable#isEnabled(org.eclipse.tcf.te.ui.views.interfaces.categories.ICategorizable.OPERATION, org.eclipse.tcf.te.ui.views.interfaces.ICategory)
      */
     @Override
     public boolean isEnabled(OPERATION operation, ICategory category) {
     	Assert.isNotNull(operation);
     	Assert.isNotNull(category);
 
     	if (element instanceof IPeerModel) {
     		// ADD: element belongs to category -> not enabled
     		if (OPERATION.ADD.equals(operation)
     				&& Managers.getCategoryManager().belongsTo(category.getId(), getId())) {
     			return false;
     		}
 
     		// REMOVE: element belongs not to category -> not enabled
     		if (OPERATION.REMOVE.equals(operation)
     				&& !Managers.getCategoryManager().belongsTo(category.getId(), getId())) {
     			return false;
     		}
 
     		return true;
     	}
 
     	return false;
     }
 }
