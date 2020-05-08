 /*******************************************************************************
  * Copyright (c) 2006 Sybase, Inc. and others.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Sybase, Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.pagedesigner.editors.palette;
 
 import java.util.List;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceChangeListener;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IResourceDeltaVisitor;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.Preferences;
 import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
 import org.eclipse.gef.palette.PaletteRoot;
 import org.eclipse.gef.ui.palette.PaletteViewer;
 import org.eclipse.gef.ui.palette.customize.PaletteCustomizerDialog;
 import org.eclipse.jst.pagedesigner.PDPlugin;
 import org.eclipse.jst.pagedesigner.editors.palette.impl.PaletteItemManager;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IEditorReference;
 import org.eclipse.ui.IFileEditorInput;
 import org.eclipse.ui.IWorkbenchPage;
 
 /**
  * XXX: currently create this class is just for add/remove listener to
  * preference change, so can refresh the palette.
  * 
  * @author mengbo
  * @author mengbo
  * @version 1.5
  */
 public class DesignerPaletteViewer extends PaletteViewer {
 	private PaletteCustomizerDialog _customizerDialog = null;
 
 	/**
 	 * 
 	 */
 	public DesignerPaletteViewer() {
 		super();
 
 		this.enableVerticalScrollbar(true);
 	}
 
 	Preferences.IPropertyChangeListener listener = new Preferences.IPropertyChangeListener() {
 		public void propertyChange(PropertyChangeEvent event) {
 			PaletteRoot root = getPaletteRoot();
 			if (root instanceof DesignerPaletteRoot) {
 				((DesignerPaletteRoot) root).refresh();
 				// XXX: setActiveTool to null to workaround GEF bug of NPE
 				// setActiveTool(null);
 			}
 		}
 	};
 
 	IEntryChangeListener _paletteModelListener = new IEntryChangeListener() {
 
 		public void modelChanged(List oldDefinitions, List newDefinitions) {
			PaletteRoot root = getPaletteRoot();
 			if (root instanceof DesignerPaletteRoot) {
				((DesignerPaletteRoot) root).refresh();
 			}
 		}
 
 	};
 
 	IResourceChangeListener _resourceChangeListener = new IResourceChangeListener() {
 
 		public void resourceChanged(IResourceChangeEvent event) {
 			IResourceDeltaVisitor visitor = new IResourceDeltaVisitor() {
 				public boolean visit(IResourceDelta delta) throws CoreException {
 					IResource resource = delta.getResource();
 					// FIXME need make performance better
 					if (resource.getType() == IResource.FILE
 							&& (delta.getFlags() & IResourceDelta.CONTENT) != 0) {
 						String ext = ((IFile) resource).getFileExtension();
 						// resource.getFullPath().
 						if (ext != null
 								&& ("tld".equalsIgnoreCase(ext) || "jar"
 										.equalsIgnoreCase(ext))) {
 							PaletteRoot root = getPaletteRoot();
 							if (root instanceof DesignerPaletteRoot) {
 								IPaletteItemManager imanager = ((DesignerPaletteRoot) root)
 										.getPaletteManager();
 								if (imanager instanceof PaletteItemManager) {
 									PaletteItemManager manager = (PaletteItemManager) imanager;
 									manager.reset();
 								}
 
 							}
 						}
 					}
 					return true;
 				}
 			};
 			try {
 				IResourceDelta delta = event.getDelta();
 				if (delta != null) {
 					delta.accept(visitor);
 				}
 			} catch (CoreException e) {
 				// ignore
 			}
 		}
 
 	};
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.gef.ui.palette.PaletteViewer#hookControl()
 	 */
 	protected void hookControl() {
 		super.hookControl();
 		PDPlugin.getDefault().getPluginPreferences().addPropertyChangeListener(
 				listener);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.gef.ui.palette.PaletteViewer#unhookControl()
 	 */
 	protected void unhookControl() {
 		PDPlugin.getDefault().getPluginPreferences()
 				.removePropertyChangeListener(listener);
 		// remove palette model change listener
 		PaletteRoot root = getPaletteRoot();
 		if (root instanceof DesignerPaletteRoot) {
 			IPaletteItemManager imanager = ((DesignerPaletteRoot) root)
 					.getPaletteManager();
 			if (imanager instanceof PaletteItemManager) {
 				PaletteItemManager manager = (PaletteItemManager) imanager;
 				ResourcesPlugin.getWorkspace().removeResourceChangeListener(
 						_resourceChangeListener);
 				manager.removeEntryChangeListener(_paletteModelListener);
 				PaletteItemManager.clearPaletteItemManager();
 			}
 		}
 		super.unhookControl();
 	}
 
 	public boolean isFileOpenedInProject(IProject project) {
 		IWorkbenchPage page = PDPlugin.getDefault().getWorkbench()
 				.getActiveWorkbenchWindow().getActivePage();
 		IEditorReference[] editors = page.getEditorReferences();
 		for (int i = 0; i < editors.length; i++) {
 			IEditorPart part = (IEditorPart) editors[i].getPart(false);
 			if (part != null) {
 				IEditorInput input = part.getEditorInput();
 				IFile inputFile = null;
 				if (input != null && input instanceof IFileEditorInput) {
 					inputFile = ((IFileEditorInput) input).getFile();
 					IProject refProject = inputFile.getProject();
 					if (project == refProject) {
 						return true;
 					}
 				}
 			}
 		}
 
 		return false;
 	}
 
 	public PaletteCustomizerDialog getCustomizerDialog() {
 		if (_customizerDialog == null) {
 			_customizerDialog = new DesignerPaletteCustomizerDialog(
 					getControl().getShell(), getCustomizer(), getPaletteRoot());
 		}
 		return _customizerDialog;
 	}
 
 	public void setPaletteRoot(PaletteRoot root) {
 		super.setPaletteRoot(root);
 		// add palette model change listener
 		// PaletteRoot root = getPaletteRoot();
 		if (root instanceof DesignerPaletteRoot) {
 			((DesignerPaletteRoot) root).getPaletteManager()
 					.addEntryChangeListener(_paletteModelListener);
 			ResourcesPlugin.getWorkspace().addResourceChangeListener(
 					_resourceChangeListener);
 		}
 
 	}
 }
