 /*******************************************************************************
  * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.processes.ui.internal.filters;
 
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.jface.viewers.TreePath;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerFilter;
 import org.eclipse.tcf.protocol.Protocol;
 import org.eclipse.tcf.te.tcf.processes.core.model.interfaces.IProcessContextNode;
 
 /**
  * The filter to filter out the single thread of a process which has the same name and id with its
  * parent process.
  */
 public class SingleThreadFilter extends ViewerFilter {
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
 	 */
 	@Override
 	public boolean select(final Viewer viewer, Object parentElement, Object element) {
 		if (parentElement instanceof TreePath) {
 			parentElement = ((TreePath) parentElement).getLastSegment();
 		}
 		if (parentElement instanceof IProcessContextNode && element instanceof IProcessContextNode) {
 			final AtomicBoolean selected = new AtomicBoolean();
 			final Object pe = parentElement;
 			final Object e = element;
 
 			Runnable runnable = new Runnable() {
 				@Override
 				public void run() {
 					IProcessContextNode parent = (IProcessContextNode)pe;
 					IProcessContextNode child = (IProcessContextNode)e;
 					if (parent.getChildren().length == 1) {
 						if (parent.getSysMonitorContext().getPID() == child.getSysMonitorContext().getPID()) {
 							if (parent.getName() != null) {
 								selected.set(!parent.getName().equals(child.getName()));
 							}
 							else if (child.getName() != null) {
 								selected.set(!child.getName().equals(parent.getName()));
 							}
 						}
					} else {
						selected.set(true);
 					}
 				}
 			};
 
 			Assert.isTrue(!Protocol.isDispatchThread());
 			Protocol.invokeAndWait(runnable);
 
 			return selected.get();
 		}
 		return true;
 	}
 }
