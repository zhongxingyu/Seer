 /*******************************************************************************
  * Copyright (c) 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.ui.interfaces.handler;
 
 import org.eclipse.jface.viewers.TreePath;
 
 /**
  * A delete handler delegate supports the delegate handler determine
  * if the delete operation can be executed and/or how the delete is executed.
  */
 public interface IDeleteHandlerDelegate {
 
 	/**
 	 * Returns if or if not the given tree path can be deleted in its current state.
 	 * <p>
 	 * The method is expected to return <code>true</code> if the passed in tree path
 	 * cannot be analyzed by the handler.
 	 *
 	 * @param treePath The tree path. Must not be <code>null</code>.
 	 *
 	 * @return <code>True</code> if the tree path is deletable, <code>false</code> otherwise.
 	 */
 	public boolean canDelete(TreePath treePath);

	/**
	 * Called from the delete handler to signal that the given element
	 * has been removed.
	 *
	 * @param element The removed element. Must not be <code>null</code>.
	 */
	public void postDelete(Object element);
 }
