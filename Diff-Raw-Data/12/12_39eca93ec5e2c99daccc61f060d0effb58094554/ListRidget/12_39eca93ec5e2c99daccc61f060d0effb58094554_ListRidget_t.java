 /*******************************************************************************
  * Copyright (c) 2007, 2012 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.internal.ui.ridgets.swt;
 
 import org.eclipse.jface.viewers.AbstractListViewer;
 import org.eclipse.jface.viewers.ListViewer;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.List;
 
 import org.eclipse.riena.ui.ridgets.ISelectableRidget;
 import org.eclipse.riena.ui.ridgets.swt.AbstractListRidget;
 import org.eclipse.riena.ui.ridgets.swt.MarkerSupport;
 
 /**
  * Ridget for SWT {@link List} widgets.
  */
 public class ListRidget extends AbstractListRidget {
 
 	private ListViewer viewer;
 	private StructuredViewerFilterHolder filterHolder;
 
 	public ListRidget() {
 		selectionTypeEnforcer = new SelectionTypeEnforcer();
 	}
 
 	@Override
 	protected void checkUIControl(final Object uiControl) {
 		checkType(uiControl, List.class);
 	}
 
 	@Override
 	public List getUIControl() {
 		return (List) super.getUIControl();
 	}
 
 	@Override
 	protected int getUIControlSelectionIndex() {
 		return getUIControl().getSelectionIndex();
 	}
 
 	@Override
 	protected int[] getUIControlSelectionIndices() {
 		return getUIControl().getSelectionIndices();
 	}
 
 	@Override
 	protected int getUIControlItemCount() {
 		return getUIControl().getItemCount();
 	}
 
 	@Override
 	protected void bindUIControl() {
 		final List control = getUIControl();
 		if (control != null) {
 			viewer = new ListViewer(control);
 			if (hasViewerModel()) {
 				configureViewer(viewer);
 			}
 
 			updateComparator();
 			updateEnabled(isEnabled());
 
 			control.addSelectionListener(selectionTypeEnforcer);
 			getFilterHolder().activate(viewer);
 		}
 	}
 
 	@Override
 	protected void unbindUIControl() {
 		super.unbindUIControl();
 		getFilterHolder().deactivate(viewer);
 
 		final List control = getUIControl();
 		if (control != null) {
 			control.removeSelectionListener(selectionTypeEnforcer);
 		}
 		viewer = null;
 	}
 
 	@Override
 	protected AbstractListViewer getViewer() {
 		return viewer;
 	}
 
 	@Override
 	protected void updateEnabled(final boolean isEnabled) {
 		final String savedBackgroundKey = "oldbg"; //$NON-NLS-1$
		final List list = getUIControl();
		if (list != null) {
			// only the first time we come here, we remember THE (enabled) background color
			// this is a kind "final" field
			if (list.getData(savedBackgroundKey) == null) {
				list.setData(savedBackgroundKey, list.getBackground());
			}
 		}
 
 		if (isEnabled) {
 			if (hasViewer()) {
 				refreshViewer();
 				disposeSelectionBindings();
 				createSelectionBindings();
 
 				// set the background color we remembered at the beginning of this method
 				list.setBackground((Color) list.getData(savedBackgroundKey));
 			}
 		} else {
 			disposeSelectionBindings();
 			if (hasViewer()) {
 				refreshViewer();
 				if (MarkerSupport.isHideDisabledRidgetContent()) {
 					list.deselectAll();
 				}
 			}
 		}
 		updateMarkers();
 	}
 
 	@Override
 	protected StructuredViewerFilterHolder getFilterHolder() {
 		if (filterHolder == null) {
 			filterHolder = new StructuredViewerFilterHolder();
 		}
 		return filterHolder;
 	}
 
 	// helping classes
 	// ////////////////
 
 	/**
 	 * Enforces selection in the control:
 	 * <ul>
 	 * <li>disallows selection changes when the ridget is "output only"</li>
 	 * <li>disallows multiple selection is the selection type of the ridget is {@link ISelectableRidget.SelectionType#SINGLE}</li>
 	 * </ul>
 	 */
 	private final class SelectionTypeEnforcer extends SelectionAdapter {
 		@Override
 		public void widgetSelected(final SelectionEvent e) {
 			final List control = (List) e.widget;
 			if (isOutputOnly()) {
 				revertSelection(control);
 			} else if (SelectionType.SINGLE.equals(getSelectionType())) {
 				if (control.getSelectionCount() > 1) {
 					// ignore this event
 					e.doit = false;
 					selectFirstItem(control);
 				}
 			}
 		}
 
 		private void selectFirstItem(final List control) {
 			// set selection to most recent item
 			control.setSelection(control.getSelectionIndex());
 			// fire event
 			final Event event = new Event();
 			event.type = SWT.Selection;
 			event.doit = true;
 			control.notifyListeners(SWT.Selection, event);
 		}
 
 		private void revertSelection(final List control) {
 			control.setRedraw(false);
 			try {
 				// undo user selection when "output only"
 				viewer.setSelection(new StructuredSelection(getSelection()));
 			} finally {
 				// redraw control to remove "cheese" that is caused when
 				// using the keyboard to select the next row
 				control.setRedraw(true);
 			}
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * <p>
 	 * This Ridget only supports native tool tips (SWT tool tips). Because of this the method has no effect.
 	 */
 	public void setNativeToolTip(final boolean nativeToolTip) {
 		// do nothing
 	}
 
 }
