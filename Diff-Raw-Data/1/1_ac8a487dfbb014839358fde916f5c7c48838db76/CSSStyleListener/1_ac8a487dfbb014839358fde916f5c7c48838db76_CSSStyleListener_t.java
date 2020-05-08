 /*******************************************************************************
  * Copyright (c) 2007-2009 Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributor:
  *     Red Hat, Inc. - initial API and implementation
  ******************************************************************************/
 
 package org.jboss.tools.jst.css.common;
 
 import org.eclipse.core.runtime.ListenerList;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IPartListener;
 import org.eclipse.ui.ISelectionListener;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.views.contentoutline.ContentOutline;
 import org.eclipse.ui.views.properties.IPropertySheetPage;
 import org.eclipse.wst.css.core.internal.provisional.document.ICSSStyleDeclItem;
 import org.eclipse.wst.css.core.internal.provisional.document.ICSSStyleDeclaration;
 import org.eclipse.wst.sse.core.internal.provisional.INodeAdapter;
 import org.eclipse.wst.sse.core.internal.provisional.INodeNotifier;
 import org.eclipse.wst.sse.ui.StructuredTextEditor;
 import org.eclipse.wst.sse.ui.internal.properties.ConfigurablePropertySheetPage;
 import org.jboss.tools.jst.css.CSSPlugin;
 
 @SuppressWarnings("restriction")
 public class CSSStyleListener implements ISelectionListener, INodeAdapter,
 		IPartListener {
 
 	private static CSSStyleListener instance;
 
 	private ListenerList listeners = new ListenerList();
 
 	private StyleContainer currentStyle;
 
 	private IWorkbenchPart currentPart;
 
 	private CSSStyleListener() {
 	}
 
 	public synchronized static CSSStyleListener getInstance() {
 
 		if (instance == null) {
 			instance = new CSSStyleListener();
 		}
 		return instance;
 	}
 
 	public void addSelectionListener(ICSSViewListner listener) {
 
 		// if added the first listener start listing
 		if (listeners.size() == 0)
 			startListening();
 
 		listeners.add(listener);
 	}
 
 	public void removeSelectionListener(ICSSViewListner listener) {
 		listeners.remove(listener);
 
 		// if removed last listener start listing
 		if (listeners.size() == 0)
 			stopListening();
 	}
 
 	private void startListening() {
 		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService()
 				.addPartListener(this);
 		PlatformUI.getWorkbench().getActiveWorkbenchWindow()
 				.getSelectionService().addPostSelectionListener(this);
 
 		// PlatformUI.getWorkbench().getActiveWorkbenchWindow()
 		// .getSelectionService().addSelectionListener(this);
 	}
 
 	private void stopListening() {
 		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService()
 				.removePartListener(this);
 		PlatformUI.getWorkbench().getActiveWorkbenchWindow()
 				.getSelectionService().removePostSelectionListener(this);
 		// PlatformUI.getWorkbench().getActiveWorkbenchWindow()
 		// .getSelectionService().addSelectionListener(this);
 
 	}
 
 	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
 		// The following line is a workaround for: JBIDE-12318: Test is failing on Eclipse Juno 4.2: SelectionLosingByPropertySheet_JBIDE4791.testSelectionLosingByPropertySheet
 		// The essence of the issue is that the implementation of org.eclipse.ui.views.properties.PropertySheet imply
 		// that the partActivated event is fired before the selectionChanged event. But it is not guaranteed. 
 		// So to workaround this, we call partActivated before any selectionChanged processing.
 		partActivated(part);
 		
 		StyleContainer newStyle = CSSStyleManager.recognizeCSSStyle(selection);
 
 		if (isImportant(part)
 				&& ((currentStyle == null) || !(currentStyle.equals(newStyle)))) {
 
 			disconnect(currentStyle);
 			connect(newStyle);
 			currentStyle = newStyle;
 
 			ISelection selectionToLiteners = null;
 
 			if (newStyle != null && newStyle.isValid()) {
 				selectionToLiteners = new StructuredSelection(newStyle);
 			} else {
 				selectionToLiteners = StructuredSelection.EMPTY;
 			}
 
 			Object[] array = listeners.getListeners();
 			for (int i = 0; i < array.length; i++) {
 				final ICSSViewListner l = (ICSSViewListner) array[i];
 				if ((part != null) && (l != currentPart) && (selection != null)) {
 
 					try {
 						l.selectionChanged(part, selectionToLiteners);
 					} catch (Exception e) {
 						CSSPlugin.log(e.getLocalizedMessage());
 					}
 				}
 
 			}
 
 		}
 
 	}
 
 	protected boolean isImportant(IWorkbenchPart part) {
 		if ((part instanceof IEditorPart) || (part instanceof ContentOutline))
 			return true;
 		return false;
 	}
 
 	private void connect(StyleContainer style) {
 
 		if (style != null) {
 			style.addNodeListener(this);
 		}
 
 	}
 
 	private void disconnect(StyleContainer style) {
 		if (style != null) {
 			style.removeNodelListener(this);
 		}
 	}
 
 	public boolean isAdapterForType(Object type) {
 		return type.equals(CSSStyleListener.class);
 	}
 
 	public void notifyChanged(INodeNotifier notifier, int eventType,
 			Object changedFeature, Object oldValue, Object newValue, int pos) {
 		/*
 		 * Fixed by yzhishko. See https://jira.jboss.org/jira/browse/JBIDE-5979.
 		 */
 		IEditorPart editorPart = PlatformUI.getWorkbench()
 				.getActiveWorkbenchWindow().getActivePage().getActiveEditor();
 		if (editorPart instanceof StructuredTextEditor) {
 			StructuredTextEditor structuredTextEditor = (StructuredTextEditor) editorPart;
 			Object page = structuredTextEditor.getAdapter(IPropertySheetPage.class);
 			if (page instanceof ConfigurablePropertySheetPage) {
 				((ConfigurablePropertySheetPage) page).refresh();
 			}
 		}
 		clearListeners(newValue);
 		clearListeners(oldValue);
 		Object[] array = listeners.getListeners();
 		for (int i = 0; i < array.length; i++) {
 			final ICSSViewListner l = (ICSSViewListner) array[i];
 
 			if (currentPart != l) {
 				try {
 					l.styleChanged(currentStyle);
 				} catch (Exception e) {
 					CSSPlugin.log(e.getLocalizedMessage());
 				}
 			}
 
 		}
 	}
 
 	/*
 	 * Fixed by yzhishko. See https://jira.jboss.org/jira/browse/JBIDE-5954.
 	 */
 	private void clearListeners(Object node) {
 		if (!(node instanceof ICSSStyleDeclItem)) {
 			return;
 		}
 		if (!(node instanceof INodeNotifier)) {
 			return;
 		}
 		((INodeNotifier) node).removeAdapter(this);
 		((INodeNotifier) node).addAdapter(this);
 	}
 
 	public void partActivated(IWorkbenchPart part) {
 		currentPart = part;
 		Object[] array = listeners.getListeners();
 		for (int i = 0; i < array.length; i++) {
 			final ICSSViewListner l = (ICSSViewListner) array[i];
 
 			if (l instanceof IPartListener) {
 				try {
 					((IPartListener) l).partActivated(part);
 				} catch (Exception e) {
 					CSSPlugin.log(e.getLocalizedMessage());
 				}
 			}
 		}
 	}
 
 	public void partBroughtToTop(IWorkbenchPart part) {
 		partActivated(part);
 	}
 
 	public void partClosed(IWorkbenchPart part) {
 	}
 
 	public void partDeactivated(IWorkbenchPart part) {
 	}
 
 	public void partOpened(IWorkbenchPart part) {
 	}
 }
