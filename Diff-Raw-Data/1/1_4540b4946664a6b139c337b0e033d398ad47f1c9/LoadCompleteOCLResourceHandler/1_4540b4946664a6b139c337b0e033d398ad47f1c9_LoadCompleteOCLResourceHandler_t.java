 /**
  * <copyright>
  *
  * Copyright (c) 2012 E.D.Willink and others.
  * All rights reserved.   This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   E.D.Willink - Initial API and implementation
  *
  * </copyright>
  */
 package org.eclipse.ocl.examples.xtext.completeocl.ui.commands;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.resources.IResourceStatus;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.emf.common.ui.dialogs.DiagnosticDialog;
 import org.eclipse.emf.common.util.BasicDiagnostic;
 import org.eclipse.emf.common.util.Diagnostic;
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.common.util.WrappedException;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EValidator;
 import org.eclipse.emf.ecore.presentation.EcoreActionBarContributor.ExtendedLoadResourceAction.ExtendedLoadResourceDialog;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.emf.edit.domain.IEditingDomainProvider;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.ocl.examples.pivot.Package;
 import org.eclipse.ocl.examples.pivot.ecore.Ecore2Pivot;
 import org.eclipse.ocl.examples.pivot.manager.MetaModelManager;
 import org.eclipse.ocl.examples.pivot.manager.MetaModelManagerResourceSetAdapter;
 import org.eclipse.ocl.examples.pivot.utilities.PivotUtil;
 import org.eclipse.ocl.examples.xtext.base.utilities.BaseCSResource;
 import org.eclipse.ocl.examples.xtext.base.utilities.CS2PivotResourceAdapter;
 import org.eclipse.ocl.examples.xtext.completeocl.validation.BasicCompleteOCLEObjectValidator;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.ISources;
 import org.eclipse.ui.handlers.HandlerUtil;
 import org.eclipse.xtext.resource.XtextResource;
 import org.eclipse.xtext.ui.editor.XtextEditor;
 import org.eclipse.xtext.ui.editor.model.IXtextDocument;
 import org.eclipse.xtext.util.concurrent.IUnitOfWork;
 
 
 /**
  */
 public class LoadCompleteOCLResourceHandler extends AbstractHandler
 {
 	protected class ResourceDialog extends ExtendedLoadResourceDialog
 	{
 		protected final Shell parent;
 		protected final ResourceSet resourceSet;
 		
 		protected ResourceDialog(Shell parent, EditingDomain domain, ResourceSet resourceSet) {
 			super(parent, domain);
 			this.parent = parent;
 			this.resourceSet = resourceSet;
 		}
 
 		protected boolean error(String message, Diagnostic diagnostic) {
 			Shell shell = parent; /*PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell()*/
 		    if (diagnostic != null) {
 		    	DiagnosticDialog.open(shell, title, message, diagnostic);		// FIXME all diagnostics
 		    }
 		    else {
 		    	MessageDialog.openInformation(shell, title, message);
 		    }
 			return false;
 		}
 
 		private boolean error(String message, String detailMessage) {
 			return error(message, new BasicDiagnostic("source", 0, detailMessage, null));
 		}
 
 		public boolean loadCSResource(ResourceSet resourceSet,
 				MetaModelManager metaModelManager, URI oclURI) {
 			BaseCSResource xtextResource = null;
 			try {
 				xtextResource = (BaseCSResource) resourceSet.getResource(oclURI, true);
 			}
 			catch (WrappedException e) {
 				URI retryURI = null;
 				Throwable cause = e.getCause();
 				if (cause instanceof CoreException) {
 					IStatus status = ((CoreException)cause).getStatus();
 					if ((status.getCode() == IResourceStatus.RESOURCE_NOT_FOUND) && status.getPlugin().equals(ResourcesPlugin.PI_RESOURCES)) {
 						if (oclURI.isPlatformResource()) {
 							retryURI = URI.createPlatformPluginURI(oclURI.toPlatformString(false), false);
 						}
 					}
 				}
 				if (retryURI != null) {
 					xtextResource = (BaseCSResource) resourceSet.getResource(retryURI, true);			
 				}
 				else {
 					throw e;
 				}
 			}
 			String message = PivotUtil.formatResourceDiagnostics(xtextResource.getErrors(), "", "\n");
 			if (message != null) {
 				return error("Failed to load '" + oclURI, message);
 			}
 			CS2PivotResourceAdapter cs2pivot = CS2PivotResourceAdapter.getAdapter(xtextResource, metaModelManager);
 			Resource pivotResource = cs2pivot.getPivotResource(xtextResource);
 			message = PivotUtil.formatResourceDiagnostics(pivotResource.getErrors(), "", "\n");
 			if (message != null) {
 				return error("Failed to load Pivot from '" + oclURI, message);
 			}
 			return true;
 		}
 
 		@Override
 		public int open() {
 			try {
 				return super.open();
 			}
 			catch (Throwable e) {
 				error(e.getMessage(), (Diagnostic)null);
 				return CANCEL;
 			}
 		}
 
 		@Override
 		protected boolean processResources() {
 			Set<EPackage> mmPackages = new HashSet<EPackage>();
 			for (Resource resource : resourceSet.getResources()) {
 				for (TreeIterator<EObject> tit = resource.getAllContents(); tit.hasNext(); ) {
 					EObject eObject = tit.next();
 					EClass eClass = eObject.eClass();
 					if (eClass != null) {
 						EPackage mmPackage = eClass.getEPackage();
 						if (mmPackage != null) {
 							mmPackages.add(mmPackage);
 						}
 					}
 				}
  			}
 			Set<Resource> mmResources = new HashSet<Resource>();
 			for (EPackage mmPackage : mmPackages) {
 				if (mmPackages.add(mmPackage)) {
 					Resource mmResource = EcoreUtil.getRootContainer(mmPackage).eResource();
 					if (mmResource != null) {
 						mmResources.add(mmResource);
 					}
 				}
  			}
 			MetaModelManagerResourceSetAdapter adapter = MetaModelManagerResourceSetAdapter.getAdapter(resourceSet, null);	// ?? Shared global MMM
 			MetaModelManager metaModelManager = adapter.getMetaModelManager();
 			for (Resource mmResource : mmResources) {
 				Ecore2Pivot ecore2Pivot = Ecore2Pivot.getAdapter(mmResource, metaModelManager);
 				Package pivotRoot = ecore2Pivot.getPivotRoot();
 				String message = PivotUtil.formatResourceDiagnostics(pivotRoot.eResource().getErrors(), "", "\n");
 				if (message != null) {
 					return error("Failed to load Pivot from '" + mmResource.getURI(), message);
 				}
 			}
 
 			for (URI oclURI : getURIs()) {
 				if (!loadCSResource(resourceSet, metaModelManager, oclURI)) {
 					return false;
 				}
 			}
 		    BasicCompleteOCLEObjectValidator.install(resourceSet, metaModelManager);
 		    BasicCompleteOCLEObjectValidator.install(EValidator.Registry.INSTANCE, mmPackages);
 			return true;
 		}
 
 		@Override
 		protected boolean processResource(Resource resource) {
 			// FIXME errors, install
 			return true;
 		}
 	}
 
 	public Object execute(ExecutionEvent event) throws ExecutionException {
 		Object applicationContext = event.getApplicationContext();
 		EditingDomain editingDomain = getEditingDomain(applicationContext);
		ResourceSet resourceSet = getResourceSet(applicationContext);
 //		System.out.println("execute " + event);
 		Object shell = HandlerUtil.getVariable(applicationContext, ISources.ACTIVE_SHELL_NAME);
 		if (!(shell instanceof Shell)) {
 			return null;
 		}
 		ResourceDialog dialog = new ResourceDialog((Shell)shell, editingDomain, resourceSet);
 		dialog.open();
 		return null;
 	}
 	
 	public static EditingDomain getEditingDomain(Object evaluationContext) {
 		Object o = HandlerUtil.getVariable(evaluationContext, ISources.ACTIVE_EDITOR_NAME);
 		if (!(o instanceof IEditorPart)) {
 			return null;
 		}
 		IEditingDomainProvider editor = (IEditingDomainProvider) ((IEditorPart)o).getAdapter(IEditingDomainProvider.class);
 		if (editor == null) {
 			return null;
 		}
 		EditingDomain editingDomain = editor.getEditingDomain();
 		if (editingDomain == null) {
 			return null;
 		}
 		return editingDomain;
 	}
 	
 	public static ResourceSet getResourceSet(Object evaluationContext) {
 		Object o = HandlerUtil.getVariable(evaluationContext, ISources.ACTIVE_EDITOR_NAME);
 		if (!(o instanceof IEditorPart)) {
 			return null;
 		}
 		IEditingDomainProvider editingDomainProvider = (IEditingDomainProvider) ((IEditorPart)o).getAdapter(IEditingDomainProvider.class);
 		if (editingDomainProvider != null) {
 			EditingDomain editingDomain = editingDomainProvider.getEditingDomain();
 			if (editingDomain == null) {
 				return null;
 			}
 			ResourceSet resourceSet = editingDomain.getResourceSet();
 			return resourceSet;
 		}
 		XtextEditor xtextEditor = (XtextEditor) ((IEditorPart)o).getAdapter(XtextEditor.class);
 		if (xtextEditor != null) {
 			IXtextDocument document = xtextEditor.getDocument();
 			ResourceSet resourceSet = document.readOnly(new IUnitOfWork<ResourceSet, XtextResource>() {
 				public ResourceSet exec(XtextResource xtextResource) {
 					if (xtextResource == null) {
 						return null;
 					}
 					ResourceSet resourceSet = xtextResource.getResourceSet();
 					return resourceSet;
 				}					
 			});
 			return resourceSet;
 		}
 		return null;
 	}
 
 	@Override
 	public void setEnabled(Object evaluationContext) {
 //		System.out.println("setEnabled " + evaluationContext);
 		setBaseEnabled(getResourceSet(evaluationContext) != null);
 	}
 }
