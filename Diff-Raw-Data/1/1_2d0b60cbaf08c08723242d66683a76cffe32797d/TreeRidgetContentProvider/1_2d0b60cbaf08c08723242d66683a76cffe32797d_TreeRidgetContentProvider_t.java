 /*******************************************************************************
  * Copyright (c) 2007, 2008 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.internal.ui.ridgets.swt;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 
 import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.jface.databinding.viewers.ObservableListTreeContentProvider;
 import org.eclipse.jface.databinding.viewers.TreeStructureAdvisor;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.riena.core.util.ReflectionUtils;
 import org.eclipse.riena.ui.ridgets.ITreeRidget;
 
 /**
  * Extends a standard observable tree content provider with support for:
  * <ul>
  * <li>handling Object[] input</li>
  * <li>knowing when we have valid input</li>
  * <li>showing / hiding the roots of the tree - see
  * {@link ITreeRidget#setRootsVisible(boolean)}</li>
  * </ul>
  */
 public final class TreeRidgetContentProvider extends ObservableListTreeContentProvider {
 
 	private final TreeViewer viewer;
 	private boolean hasInput = false;
 	private PropertyChangeListener listener;
 
 	public TreeRidgetContentProvider(TreeViewer viewer, IObservableFactory listFactory,
 			TreeStructureAdvisor structureAdvisor) {
 		super(listFactory, structureAdvisor);
 		Assert.isNotNull(viewer);
 		this.viewer = viewer;
 	}
 
 	@Override
 	public Object[] getElements(Object inputElement) {
 		if (inputElement instanceof TreeRidget.FakeRoot) {
 			return ((TreeRidget.FakeRoot) inputElement).toArray();
 		}
 		return (Object[]) inputElement;
 	}
 
 	@Override
 	public synchronized void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 		/*
 		 * this is a workaround to allow our set change listener, which is in
 		 * charge triggering an update of the tree icons, to skip the update
 		 * when the viewer is in the process of disposing itself (newInput ==
 		 * null)
 		 */
 		hasInput = (newInput != null);
 		if (oldInput instanceof TreeRidget.FakeRoot) {
 			removePropertyChangeListener((TreeRidget.FakeRoot) oldInput);
 		}
 		if (newInput instanceof TreeRidget.FakeRoot) {
 			addPropertyChangeListener((TreeRidget.FakeRoot) newInput);
 		}
 		super.inputChanged(viewer, oldInput, newInput);
 	}
 
 	/** Returns true if we have a valid (i.e. non-null) input. */
 	public boolean hasInput() {
 		return hasInput;
 	}
 
 	// helping methods
 	//////////////////
 
 	/** Remove property change listener from real root element. */
 	private synchronized void removePropertyChangeListener(final TreeRidget.FakeRoot fakeRoot) {
 		if (listener != null) {
 			ReflectionUtils.invoke(fakeRoot.getRoot(), "removePropertyChangeListener", listener); //$NON-NLS-1$
 			listener = null;
 		}
 	}
 
 	/** Add property change listener to real root element. */
 	private synchronized void addPropertyChangeListener(final TreeRidget.FakeRoot fakeRoot) {
 		Assert.isLegal(listener == null);
 		listener = new PropertyChangeListener() {
 			private final String accessor = fakeRoot.getChildrenAccessor().toUpperCase();
 
 			public void propertyChange(PropertyChangeEvent evt) {
 				if (evt.getPropertyName().toUpperCase().endsWith(accessor)) {
 					fakeRoot.refresh();
 					viewer.refresh(fakeRoot);
					viewer.update(fakeRoot.toArray(), null);
 				}
 			}
 		};
 		ReflectionUtils.invoke(fakeRoot.getRoot(), "addPropertyChangeListener", listener); //$NON-NLS-1$
 	}
 }
