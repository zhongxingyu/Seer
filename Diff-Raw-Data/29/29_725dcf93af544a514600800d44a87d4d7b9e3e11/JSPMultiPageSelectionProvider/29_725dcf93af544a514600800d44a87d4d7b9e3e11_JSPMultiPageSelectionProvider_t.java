 /*******************************************************************************
  * Copyright (c) 2007 Exadel, Inc. and Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
  ******************************************************************************/ 
 package org.jboss.tools.jst.jsp.jspeditor;
 
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.wst.sse.core.internal.provisional.IndexedRegion;
 import org.eclipse.core.runtime.ListenerList;
 import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.text.TextSelection;
 import org.eclipse.jface.util.Assert;
 import org.eclipse.jface.util.SafeRunnable;
 import org.eclipse.jface.viewers.*;
import org.jboss.tools.common.model.XModelObject;
import org.jboss.tools.common.model.filesystems.impl.FileAnyImpl;
import org.jboss.tools.common.model.util.PositionSearcher;
 
 /**
  * 
  * @author eskimo(dgolovin@exadel.com)
  *
  */
 public class JSPMultiPageSelectionProvider implements IPostSelectionProvider,
 		ISelectionProvider {
 
 	private ListenerList listeners = new ListenerList();
 
 	private JSPMultiPageEditorPart multiPageEditor;
 
 	public JSPMultiPageSelectionProvider(JSPMultiPageEditorPart multiPageEditor) {
 		Assert.isNotNull(multiPageEditor);
 		this.multiPageEditor = multiPageEditor;
 	}
 
 	Object last = null;
 
 	public void addSelectionChangedListener(ISelectionChangedListener listener) {
 		listeners.add(listener);
 		last = listener;
 	}
 
 	public void fireSelectionChanged(final SelectionChangedEvent event) {
 		Object[] listeners = this.listeners.getListeners();
 		for (int i = 0; i < listeners.length; ++i) {
 			final ISelectionChangedListener l = (ISelectionChangedListener) listeners[i];
 			SafeRunner.run(new SafeRunnable() {
 				public void run() {
 					l.selectionChanged(event);
 				}
 			});
 		}
 	}
 
 	public JSPMultiPageEditorPart getMultiPageEditor() {
 		return multiPageEditor;
 	}
 
 	public ISelection getSelection() {
 		IEditorPart activeEditor = multiPageEditor.getActiveEditor();
 		if (activeEditor != null) {
 			ISelectionProvider selectionProvider = activeEditor.getSite()
 					.getSelectionProvider();
 			if (selectionProvider != null)
 				return selectionProvider.getSelection();
 		}
 		return null;
 	}
 
 	/*
 	 * (non-JavaDoc) Method declaed on <code>ISelectionProvider</code>.
 	 */
 	public void removeSelectionChangedListener(
 			ISelectionChangedListener listener) {
 		listeners.remove(listener);
 	}
 
 	public void setSelection(ISelection selection) {
		selection = convertObjectSelection(selection);
 		if (!isAppropriateSelected(selection))
 			return;
 		if (isFiringSelection)
 			return;
 		isFiringSelection = true;
 		try {
 			IEditorPart activeEditor = multiPageEditor.getActiveEditor();
 			if (activeEditor != null) {
 				ISelectionProvider selectionProvider = activeEditor.getSite()
 						.getSelectionProvider();
 				if (selectionProvider != null)
 					selectionProvider.setSelection(selection);
 			}
 		} finally {
 			isFiringSelection = false;
 		}
 	}
 
	private ISelection convertObjectSelection(ISelection selection) {
		if(selection instanceof IStructuredSelection && !selection.isEmpty()
				&& ((IStructuredSelection)selection).getFirstElement() instanceof XModelObject) {
			XModelObject o = (XModelObject)((IStructuredSelection)selection).getFirstElement();
			XModelObject f = o;
			while(f != null && f.getFileType() != XModelObject.FILE) f = f.getParent();
			if(((JSPMultiPageEditor)multiPageEditor).getModelObject() == f) {
				String text = ((FileAnyImpl)f).getAsText();
				PositionSearcher searcher = new PositionSearcher();
				searcher.init(text, o, null);
				searcher.execute();
				int bp = searcher.getStartPosition();
				int ep = searcher.getEndPosition();
				if(bp >= 0 && ep >= bp) {
					selection = new TextSelection(bp,  ep - bp);
				}
			}
		}
		return selection;
	}

 	private boolean isAppropriateSelected(ISelection selection) {
 		if (!(selection instanceof IStructuredSelection))
 			return true;
 		if (selection.isEmpty())
 			return true;
 		Object o = ((IStructuredSelection) selection).getFirstElement();
 		return (o instanceof IndexedRegion);
 	}
 
 	private ListenerList postListeners = new ListenerList();
 
 	public void addPostSelectionChangedListener(
 			ISelectionChangedListener listener) {
 		postListeners.add(listener);
 		if (last != null) {
 			listeners.remove(last);
 			last = null;
 		}
 	}
 
 	public void removePostSelectionChangedListener(
 			ISelectionChangedListener listener) {
 		postListeners.remove(listener);
 	}
 
 	boolean isFiringSelection = false;
 
 	public boolean isFiringSelection() {
 		return isFiringSelection;
 	}
 
 	public void firePostSelectionChanged(final SelectionChangedEvent event) {
 		fireSelectionChanged(event, postListeners);
 	}
 
 	public void fireSelectionChanged(final SelectionChangedEvent event,
 			ListenerList listenerList) {
 		if (isFiringSelection)
 			return;
 		Object[] listeners = listenerList.getListeners();
 		isFiringSelection = true;
 		for (int i = 0; i < listeners.length; ++i) {
 			final ISelectionChangedListener l = (ISelectionChangedListener) listeners[i];
 			SafeRunner.run(new SafeRunnable() {
 				public void run() {
 					l.selectionChanged(event);
 				}
 			});
 		}
 		isFiringSelection = false;
 	}
 
 }
