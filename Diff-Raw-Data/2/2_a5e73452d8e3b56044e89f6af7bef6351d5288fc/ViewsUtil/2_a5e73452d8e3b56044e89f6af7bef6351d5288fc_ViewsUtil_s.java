 /*******************************************************************************
  * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.ui.views;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.StructuredViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.tcf.te.ui.views.extensions.CategoriesExtensionPointManager;
 import org.eclipse.tcf.te.ui.views.handler.PropertiesCommandHandler;
 import org.eclipse.tcf.te.ui.views.interfaces.ICategory;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.IViewReference;
 import org.eclipse.ui.IViewSite;
 import org.eclipse.ui.IWorkbenchActionConstants;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.navigator.CommonNavigator;
 
 /**
  * Utility methods to deal with views.
  */
 public class ViewsUtil {
 
 	/**
 	 * Returns the workbench part identified by the given id.
 	 *
 	 * @param id The view id. Must not be <code>null</code>.
 	 * @return The workbench part or <code>null</code>.
 	 */
 	public static IWorkbenchPart getPart(String id) {
 		// Check the active workbench window and active page instances
 		if (PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null && PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage() != null) {
 			// Get the view reference
 			IViewReference reference = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findViewReference(id);
 			// Return the view part from the reference, but do not restore it
 			return reference != null ? reference.getPart(false) : null;
 		}
 		return null;
 	}
 
 	/**
 	 * Asynchronously show the view identified by the given id.
 	 *
 	 * @param id The view id. Must not be <code>null</code>.
 	 */
 	public static void show(final String id) {
 		Assert.isNotNull(id);
 		// Create the runnable
 		Runnable runnable = new Runnable() {
 			@Override
 			public void run() {
 				// Check the active workbench window and active page instances
 				if (PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null && PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage() != null) {
 					// Show the view
 					try {
 	                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(id);
                     }
                     catch (PartInitException e) { /* ignored on purpose */ }
 				}
 			}
 		};
 
 		// Execute asynchronously
 		if (PlatformUI.isWorkbenchRunning()) {
 			PlatformUI.getWorkbench().getDisplay().asyncExec(runnable);
 		}
 	}
 
 	/**
 	 * Asynchronously refresh the view identified by the given id.
 	 *
 	 * @param id The view id. Must not be <code>null</code>.
 	 */
 	public static void refresh(final String id) {
 		Assert.isNotNull(id);
 
 		// Create the runnable
 		Runnable runnable = new Runnable() {
 			@Override
 			public void run() {
 				// Check the active workbench window and active page instances
 				if (PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null && PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage() != null) {
 					// Get the view reference
 					IViewReference reference = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findViewReference(id);
 					// Get the view part from the reference, but do not restore it
 					IWorkbenchPart part = reference != null ? reference.getPart(false) : null;
 					// If the part is a common navigator, get the common viewer
 					Viewer viewer = part instanceof CommonNavigator ? ((CommonNavigator)part).getCommonViewer() : null;
 					// If not a common navigator, try to adapt to the viewer
 					if (viewer == null) viewer = part != null ? (Viewer)part.getAdapter(Viewer.class) : null;
 					// Refresh the viewer
 					if (viewer != null) viewer.refresh();
 				}
 			}
 		};
 
 		// Execute asynchronously
 		if (PlatformUI.isWorkbenchRunning()) {
 			PlatformUI.getWorkbench().getDisplay().asyncExec(runnable);
 		}
 	}
 
 	/**
 	 * Asynchronously refresh the given element within the view identified
 	 * by the given id.
 	 *
 	 * @param id The view id. Must not be <code>null</code>.
 	 * @param element The element to refresh. Must not be <code>null</code>.
 	 */
 	public static void refresh(final String id, final Object element) {
 		Assert.isNotNull(id);
 		Assert.isNotNull(element);
 
 		// Create the runnable
 		Runnable runnable = new Runnable() {
 			@Override
 			public void run() {
 				// Check the active workbench window and active page instances
 				if (PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null && PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage() != null) {
 					// Get the view reference
 					IViewReference reference = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findViewReference(id);
 					// Get the view part from the reference, but do not restore it
 					IWorkbenchPart part = reference != null ? reference.getPart(false) : null;
 					// If the part is a common navigator, get the common viewer
 					Viewer viewer = part instanceof CommonNavigator ? ((CommonNavigator)part).getCommonViewer() : null;
 					// If not a common navigator, try to adapt to the viewer
 					if (viewer == null) viewer = part != null ? (Viewer)part.getAdapter(Viewer.class) : null;
 					// Refresh the viewer
 					if (viewer instanceof StructuredViewer) ((StructuredViewer)viewer).refresh(element, true);
					else viewer.refresh();
 				}
 			}
 		};
 
 		// Execute asynchronously
 		if (PlatformUI.isWorkbenchRunning()) {
 			PlatformUI.getWorkbench().getDisplay().asyncExec(runnable);
 		}
 	}
 
 	/**
 	 * Asynchronously set the given selection to the view identified by the given id.
 	 *
 	 * @param id The view id. Must not be <code>null</code>.
 	 * @param selection The selection or <code>null</code>.
 	 */
 	public static void setSelection(final String id, final ISelection selection) {
 		Assert.isNotNull(id);
 
 		// Create the runnable
 		Runnable runnable = new Runnable() {
 			@Override
 			public void run() {
 				// Check the active workbench window and active page instances
 				if (PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null && PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage() != null) {
 					// Get the view reference
 					IViewReference reference = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findViewReference(id);
 					// Get the view part from the reference, but do not restore it
 					IWorkbenchPart part = reference != null ? reference.getPart(false) : null;
 					// Get the selection provider
 					ISelectionProvider selectionProvider = part != null && part.getSite() != null ? part.getSite().getSelectionProvider() : null;
 					// And apply the selection
 					if (selectionProvider != null) selectionProvider.setSelection(selection);
 				}
 			}
 		};
 
 		// Execute asynchronously
 		if (PlatformUI.isWorkbenchRunning()) {
 			PlatformUI.getWorkbench().getDisplay().asyncExec(runnable);
 		}
 	}
 
 	/**
 	 * Opens the properties editor or dialog on the given selection.
 	 *
 	 * @param selection The selection. Must not be <code>null</code>.
 	 */
 	public static void openProperties(final ISelection selection) {
 		Assert.isNotNull(selection);
 
 		// Create the runnable
 		Runnable runnable = new Runnable() {
 			@Override
 			public void run() {
 				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
 				Assert.isNotNull(window);
 				PropertiesCommandHandler.openEditorOnSelection(window, selection);
 			}
 		};
 
 		// Execute asynchronously
 		if (PlatformUI.isWorkbenchRunning()) {
 			PlatformUI.getWorkbench().getDisplay().asyncExec(runnable);
 		}
 	}
 
 	/**
 	 * "Go Into" the category identified by the given category id, within the view
 	 * identified by the given id.
 	 * <p>
 	 * <b>Note:</b> This method is actively changing the selection of the view.
 	 *
 	 * @param id The view id. Must not be <code>null</code>.
 	 * @param categoryId The category id. Must not be <code>null</code>.
 	 */
 	public static void goInto(final String id, final String categoryId) {
 		Assert.isNotNull(id);
 		Assert.isNotNull(categoryId);
 
 		ICategory category = CategoriesExtensionPointManager.getInstance().getCategory(categoryId, false);
 		if (category != null) goInto(id, category);
 	}
 
 	/**
 	 * "Go Into" the given node within the view identified by the given id.
 	 * <p>
 	 * <b>Note:</b> This method is actively changing the selection of the view.
 	 *
 	 * @param id The view id. Must not be <code>null</code>.
 	 * @param node The node to go into. Must not be <code>null</code>.
 	 */
 	public static void goInto(final String id, final Object node) {
 		Assert.isNotNull(id);
 		Assert.isNotNull(node);
 
 		goInto(id, new StructuredSelection(node));
 	}
 
 	/**
 	 * "Go Into" the given selection within the view identified by the given id.
 	 * <p>
 	 * <b>Note:</b> This method is actively changing the selection of the view.
 	 *
 	 * @param id The view id. Must not be <code>null</code>.
 	 * @param selection The selection. Must not be <code>null</code>.
 	 */
 	public static void goInto(final String id, final ISelection selection) {
 		Assert.isNotNull(id);
 		Assert.isNotNull(selection);
 
 		// Set the selection
 		setSelection(id, selection);
 
 		// Create the runnable
 		Runnable runnable = new Runnable() {
             @Override
 			public void run() {
 				// Check the active workbench window and active page instances
 				if (PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null && PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage() != null) {
 					// Get the view reference
 					IViewReference reference = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findViewReference(id);
 					// Get the view part from the reference, but do not restore it
 					IWorkbenchPart part = reference != null ? reference.getPart(false) : null;
 					// Get the action bars
 					IActionBars actionBars = part != null && part.getSite() instanceof IViewSite ? ((IViewSite)part.getSite()).getActionBars() : null;
 					// Get the "Go Into" action
 					IAction action = actionBars != null ? actionBars.getGlobalActionHandler(IWorkbenchActionConstants.GO_INTO) : null;
 					// Run the action
 					if (action != null) action.run();
 				}
 			}
 		};
 
 		// Execute asynchronously
 		if (PlatformUI.isWorkbenchRunning()) {
 			PlatformUI.getWorkbench().getDisplay().asyncExec(runnable);
 		}
 	}
 }
