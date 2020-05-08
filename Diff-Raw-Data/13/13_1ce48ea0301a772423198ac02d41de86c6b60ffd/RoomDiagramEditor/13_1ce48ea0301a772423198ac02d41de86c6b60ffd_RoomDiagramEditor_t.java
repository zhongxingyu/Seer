 /*******************************************************************************
  * Copyright (c) 2011 protos software gmbh (http://www.protos.de).
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *******************************************************************************/
 
 package org.eclipse.etrice.ui.common.editor;
 
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature.Setting;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.etrice.core.ui.RoomUiModule;
 import org.eclipse.graphiti.ui.editor.DiagramEditor;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.xtext.resource.XtextResource;
 import org.eclipse.xtext.serializer.ISerializer;
 import org.eclipse.xtext.util.CancelIndicator;
 import org.eclipse.xtext.validation.CheckMode;
 import org.eclipse.xtext.validation.IResourceValidator;
 import org.eclipse.xtext.validation.Issue;
 
 import com.google.inject.Inject;
 import com.google.inject.Injector;
 
 /**
  * description
  *
  * @author Henrik Rentz-Reichert initial contribution and API
  *
  */
 public class RoomDiagramEditor extends DiagramEditor {
 
 	@Inject
 	protected IResourceValidator resourceValidator;
 
 	private ModificationTrackingEnabler mte = new ModificationTrackingEnabler();
 
 	public RoomDiagramEditor() {
 		super();
 		
 		Injector injector = RoomUiModule.getInjector();
 		injector.injectMembers(this);
 	}
 	
 	@SuppressWarnings("restriction")
 	@Override
 	protected void initializeGraphicalViewer() {
 		super.initializeGraphicalViewer();
 		
 		Map<EObject, Collection<Setting>> result = EcoreUtil.UnresolvedProxyCrossReferencer.find(getEditingDomain().getResourceSet());
 		if (!result.isEmpty())
 			System.err.println("ERROR in diagram viewer: could not resolve all proxies!");
 
 		mte.setTarget(getEditingDomain());
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.graphiti.ui.internal.editor.DiagramEditorInternal#dispose()
 	 */
 	@SuppressWarnings("restriction")
 	@Override
 	public void dispose() {
 		mte.unsetTarget(getEditingDomain());
 		
 		super.dispose();
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.graphiti.ui.internal.editor.DiagramEditorInternal#doSave(org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	@SuppressWarnings("restriction")
 	@Override
 	public void doSave(final IProgressMonitor monitor) {
 		ResourceSet rs = getEditingDomain().getResourceSet();
 		for (Resource res : rs.getResources()) {
 			if (res instanceof XtextResource) {
 				if (res.isModified()) {
 					XtextResource xres = (XtextResource) res;
 					ISerializer serializer = xres.getSerializer();
 					
					if (xres.getContents().isEmpty()) {
 						MessageDialog.openError(Display.getDefault().getActiveShell(), "ERROR", "Internal error: part of textual model is empty, can't save");
 						return;
 					}
 
 					// HOWTO: call serializer to validate the concrete syntax
 					// this throws an exception which is caught further up the stack
 					// and a dialog will be displayed
 					serializer.serialize(xres.getContents().get(0));
 					
 					List<Issue> result = resourceValidator.validate(res, CheckMode.NORMAL_AND_FAST, new CancelIndicator() {
 						public boolean isCanceled() {
 							return monitor.isCanceled();
 						}
 					});
 					if (!result.isEmpty()) {
 						MessageDialog.openError(Display.getDefault().getActiveShell(), "ERROR", "Internal error: model is invalid, can't save");
 						return;
 					}
 				}
 			}
 		}
 		super.doSave(monitor);
 	}
 
 	@SuppressWarnings("restriction")
 	@Override
 	public void createPartControl(Composite parent) {
 			super.createPartControl(parent);
 			
 			/* we have to save here whether changes have been done or not to get rid of the dirty state
 			 * CAUTION: save in
 			 * init(IEditorSite site, IEditorInput input)
 			 * or
 			 * setInput(IEditorInput input)
 			 * did not work correctly
 			 */
 	//		if (AutoUpdateFeature.isLastDoneChanges())
 				doSave(null);
 		}
 }
