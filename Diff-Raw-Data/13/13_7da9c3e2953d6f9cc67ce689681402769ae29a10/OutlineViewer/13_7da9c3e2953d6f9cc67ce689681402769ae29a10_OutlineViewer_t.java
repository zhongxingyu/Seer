 /*******************************************************************************
  * Copyright (c) 2008 Olivier Moises
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   Olivier Moises- initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.wazaabi.ide.ui.outline;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.TypedListener;
 import org.eclipse.wazaabi.engine.core.editparts.WidgetEditPart;
 import org.eclipse.wazaabi.engine.core.views.WidgetView;
 import org.eclipse.wazaabi.engine.core.views.factories.WidgetViewFactory;
 import org.eclipse.wazaabi.engine.swt.editparts.SWTRootEditPart;
 import org.eclipse.wazaabi.engine.swt.viewers.SWTControlViewer;
 import org.eclipse.wazaabi.engine.swt.views.SWTWidgetView;
 import org.eclipse.wazaabi.ide.ui.editparts.AbstractComponentTreeEditPart;
 import org.eclipse.wazaabi.ide.ui.outline.widgetviews.OutlineWidgetViewFactory;
 import org.eclipse.wazaabi.mm.core.widgets.AbstractComponent;
 import org.eclipse.wazaabi.mm.core.widgets.Container;
 
 public class OutlineViewer extends SWTControlViewer implements
 		ISelectionChangedListener {
 
 	private OutlineWidgetViewFactory outlineWidgetViewFactory = null;
 	private List<Control> selectedControls = new ArrayList<Control>();
 
 	private PaintListener paintListener = new PaintListener() {
 
 		public void paintControl(PaintEvent e) {
 			for (Control c : selectedControls) {
 				Point controlLocation = c.getLocation();
 				e.gc.setLineStyle(SWT.LINE_SOLID);
 				e.gc.setLineWidth(2);
 				Color previousColor = e.gc.getForeground();
 				e.gc.setForeground(e.display.getSystemColor(SWT.COLOR_RED));
 				if (c.getParent() == e.widget)
 					e.gc.drawRectangle(controlLocation.x - 1,
 							controlLocation.y - 1, c.getSize().x + 2,
 							c.getSize().y + 2);
 				else if (c == OutlineViewer.this.getControl())
 					e.gc.drawRectangle(controlLocation.x + 1,
 							controlLocation.y + 1, c.getSize().x - 2,
 							c.getSize().y - 2);
 				e.gc.setForeground(previousColor);
 			}
 
 		}
 	};
 
 	public OutlineViewer(Composite parent) {
 		super(parent);
 	}
 
 	public OutlineViewer(Composite parent, SWTRootEditPart rootEditPart) {
 		super(parent, rootEditPart);
 	}
 
 	@Override
 	public WidgetViewFactory getWidgetViewFactory() {
 		if (this.outlineWidgetViewFactory == null)
 			this.outlineWidgetViewFactory = new OutlineWidgetViewFactory();
 		return this.outlineWidgetViewFactory;
 	}
 
 	protected void addUniquePaintListener(Control control,
 			PaintListener paintListener) {
 		if (control == null || control.isDisposed() || paintListener == null)
 			return;
 		if (!hasPaintListener(control))
 			control.addPaintListener(paintListener);
 	}
 
 	private boolean hasPaintListener(Control control) {
 		for (Listener l : control.getListeners(SWT.Paint))
 			if (l instanceof TypedListener
 					&& ((TypedListener) l).getEventListener().equals(
 							paintListener))
 				return true;
 		return false;
 	}
 
 	protected void removeUniquePaintListener(Control control,
 			PaintListener paintListener) {
 		if (control == null || control.isDisposed() || paintListener == null)
 			return;
 		if (hasPaintListener(control))
 			control.removePaintListener(paintListener);
 	}
 
 	public void selectionChanged(SelectionChangedEvent event) {
 		if (getControl() == null || getControl().isDisposed()
 				|| event.getSelection() == null)
 			return;
 		List<Control> newlySelectedControls = new ArrayList<Control>();
 		if (event.getSelection() instanceof StructuredSelection) {
 			for (Object selected : ((StructuredSelection) event.getSelection())
 					.toList()) {
 				if (selected instanceof AbstractComponentTreeEditPart) {
 					AbstractComponent outlineComponent = getOutlineComponent(((AbstractComponentTreeEditPart) selected)
 							.getAbstractComponentModel());
 					WidgetEditPart ep = (WidgetEditPart) getEditPartRegistry()
 							.get(outlineComponent);
 					if (ep != null) {
 						WidgetView widgetView = ep.getWidgetView();
 						if (widgetView instanceof SWTWidgetView
 								&& ((SWTWidgetView) widgetView).getSWTWidget() instanceof Control)
 							newlySelectedControls
 									.add((Control) ((SWTWidgetView) widgetView)
 											.getSWTWidget());
 					}
 
 				}
 			}
 		}
 
 		List<Control> toUnselect = new ArrayList<Control>();
 		for (Control c : selectedControls)
			if (c.isDisposed() || !newlySelectedControls.contains(c))
 				toUnselect.add(c);
 
 		for (Control c : toUnselect)
 			selectedControls.remove(c);
 
 		for (Control c : newlySelectedControls)
 			if (!selectedControls.contains(c))
 				selectedControls.add(c);
 
 		List<Control> toRemovePaintListener = new ArrayList<Control>();
 		List<Control> toAddPaintListener = new ArrayList<Control>();
 
 		for (Control c : toUnselect)
			if (!c.isDisposed()) {
				if (c != getControl())
					toRemovePaintListener.add(c.getParent());
				else
					toRemovePaintListener.add(c);
			}
 
 		// we remove only what we don't need to add later
 		for (Control c : selectedControls)
 			if (c != getControl()) {
 				toAddPaintListener.add(c.getParent());
 				if (toRemovePaintListener.contains(c.getParent()))
 					toRemovePaintListener.remove(c.getParent());
 			} else {
 				toAddPaintListener.add(c);
 				if (toRemovePaintListener.contains(c))
 					toRemovePaintListener.remove(c);
 			}
 
 		for (Control c : toRemovePaintListener) {
 			removeUniquePaintListener(c, paintListener);
 			c.redraw();
 		}
 		for (Control c : toAddPaintListener) {
 			addUniquePaintListener(c, paintListener);
 			c.redraw();
 		}
 	}
 
 	public List<Control> getSelectedControls() {
 		return selectedControls;
 	}
 
 	public void refreshSelection() {
 		for (Control c : getSelectedControls()) {
 			c.getParent().redraw();
 			c.redraw();
 		}
 	}
 
 	protected AbstractComponent getOutlineComponent(
 			AbstractComponent editorComponent) {
 		if (editorComponent == null)
 			return null;
 		if (getRootEditPart().getContents() != null
 				&& getRootEditPart().getContents().getModel() instanceof AbstractComponent) {
 			AbstractComponent viewerRoot = (AbstractComponent) getRootEditPart()
 					.getContents().getModel();
 			if (editorComponent.eContainer() == null)
 				return viewerRoot;
 
 			if (!(viewerRoot instanceof Container))
 				return null;
 			List<Integer> indexes = new ArrayList<Integer>();
 			AbstractComponent current = editorComponent;
 			Container parent = null;
 			while ((parent = (Container) current.eContainer()) != null) {
 				indexes.add(parent.getChildren().indexOf(current));
 				current = parent;
 			}
 			if (!indexes.isEmpty()) {
 				current = viewerRoot;
 				for (int i = indexes.size() - 1; i >= 0; i--) {
 					int index = indexes.get(i);
 					if (current instanceof Container
 							&& ((Container) current).getChildren().size() > index)
 						current = ((Container) current).getChildren()
 								.get(index);
 					else
 						break;
 				}
 				return current;
 			}
 		}
 		return null;
 	}
 }
