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
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.resources.IResource;
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
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.emf.edit.domain.IEditingDomainProvider;
 import org.eclipse.emf.edit.ui.action.LoadResourceAction.LoadResourceDialog;
 import org.eclipse.jdt.annotation.NonNull;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.ocl.examples.pivot.Element;
 import org.eclipse.ocl.examples.pivot.ParserException;
 import org.eclipse.ocl.examples.pivot.ecore.Ecore2Pivot;
 import org.eclipse.ocl.examples.pivot.manager.MetaModelManager;
 import org.eclipse.ocl.examples.pivot.manager.MetaModelManagerResourceSetAdapter;
 import org.eclipse.ocl.examples.pivot.utilities.BaseResource;
 import org.eclipse.ocl.examples.pivot.utilities.PivotUtil;
 import org.eclipse.ocl.examples.pivot.validation.PivotEObjectValidator;
 import org.eclipse.ocl.examples.xtext.completeocl.CompleteOCLStandaloneSetup;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.dnd.DND;
 import org.eclipse.swt.dnd.DropTarget;
 import org.eclipse.swt.dnd.DropTargetAdapter;
 import org.eclipse.swt.dnd.DropTargetEvent;
 import org.eclipse.swt.dnd.FileTransfer;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.ISources;
 import org.eclipse.ui.handlers.HandlerUtil;
 import org.eclipse.ui.part.ResourceTransfer;
 import org.eclipse.xtext.resource.XtextResource;
 import org.eclipse.xtext.ui.editor.XtextEditor;
 import org.eclipse.xtext.ui.editor.model.IXtextDocument;
 import org.eclipse.xtext.util.concurrent.IUnitOfWork;
 
 
 /**
  */
 public class LoadCompleteOCLResourceHandler extends AbstractHandler
 {
 	protected class ResourceDialog extends LoadResourceDialog
 	{
 		public class URIDropTargetListener extends DropTargetAdapter
 		{
 			@Override
 			public void dragEnter(DropTargetEvent e) {
 				e.detail = DND.DROP_LINK;
 			}
 
 			@Override
 			public void dragOperationChanged(DropTargetEvent e) {
 				e.detail = DND.DROP_LINK;
 			}
 
 			@Override
 			public void drop(DropTargetEvent event) {
 				Object data = event.data;
 				if (data == null) {
 					event.detail = DND.DROP_NONE;
 					return;
 				}
 				if (data instanceof IResource[]) {
 					StringBuilder s = new StringBuilder();
 					for (IResource resource : (IResource[])data) {
 						if (s.length() > 0) {
 							s.append(" ");
 						}
 						s.append(URI.createPlatformResourceURI(resource.getFullPath().toString(), true));
 					}
 					uriField.setText(s.toString());
 				}
 				else if (data instanceof String[]) {
 					StringBuilder s = new StringBuilder();
 					for (String resource : (String[])data) {
 						if (s.length() > 0) {
 							s.append(" ");
 						}
 						s.append(URI.createFileURI(resource));
 					}
 					uriField.setText(s.toString());
 				}
 				else {
 					uriField.setText(((String) data));
 				}
 			}
 		}
 
 		protected final Shell parent;
 		protected final @NonNull ResourceSet resourceSet;
 		private DropTarget target;
 		
 		protected ResourceDialog(Shell parent, EditingDomain domain, @NonNull ResourceSet resourceSet) {
 			super(parent, domain);
 			this.parent = parent;
 			this.resourceSet = resourceSet;
 			int shellStyle = getShellStyle();
 			int newShellStyle = shellStyle & ~(SWT.APPLICATION_MODAL | SWT.PRIMARY_MODAL | SWT.SYSTEM_MODAL);
 			setShellStyle(newShellStyle);
 		}
 
 		@Override
 		protected void configureShell(Shell shell) {
 			super.configureShell(shell);
 			shell.setText("Load Complete OCL Resource");
 		}
 
 		@Override
 		protected Control createContents(Composite parent) {
 			Control control = super.createContents(parent);
 			int operations = /*DND.DROP_MOVE |*/ DND.DROP_COPY | DND.DROP_LINK;
 			target = new DropTarget(uriField, operations);
 			target.setTransfer(new Transfer[] {ResourceTransfer.getInstance(), FileTransfer.getInstance()});
 			target.addDropListener(new URIDropTargetListener());
 			return control;
 		}
 
 		@Override
 		protected Control createDialogArea(Composite parent) {
 			Composite createDialogArea = (Composite) super.createDialogArea(parent);
 			
 			Label helpLabel = new Label(createDialogArea, SWT.CENTER);
 		    helpLabel.setText("You may Drag and Drop from an Eclipse or Operating System Explorer.");
 		    {
 		      FormData data = new FormData();
 		      data.top = new FormAttachment(uriField, 2 * CONTROL_OFFSET);	// Separator is at 1 * CONTROL_OFFSET
 		      data.left = new FormAttachment(0, CONTROL_OFFSET);
 		      data.right = new FormAttachment(100, -CONTROL_OFFSET);
 		      helpLabel.setLayoutData(data);
 		    }
 			
 			return createDialogArea;
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
			return error(message, new BasicDiagnostic(Diagnostic.ERROR, "source", 0, detailMessage, null));
 		}
 
 		public boolean loadCSResource(@NonNull ResourceSet resourceSet,
 				@NonNull MetaModelManager metaModelManager, @NonNull URI oclURI) {
 			BaseResource xtextResource = null;
 			CompleteOCLStandaloneSetup.init();
 			try {
 				xtextResource = (BaseResource) resourceSet.getResource(oclURI, true);
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
 					xtextResource = (BaseResource) resourceSet.getResource(retryURI, true);			
 				}
 				else {
 					throw e;
 				}
 			}
 			List<org.eclipse.emf.ecore.resource.Resource.Diagnostic> errors = xtextResource.getErrors();
 			assert errors != null;
 			String message = PivotUtil.formatResourceDiagnostics(errors, "", "\n");
 			if (message != null) {
 				return error("Failed to load '" + oclURI, message);
 			}
 			Resource pivotResource = xtextResource.getPivotResource(metaModelManager);
 			errors = pivotResource.getErrors();
 			assert errors != null;
 			message = PivotUtil.formatResourceDiagnostics(errors, "", "\n");
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
 			finally {
 				if (target != null) {
 					target.dispose();
 					target = null;
 				}
 			}
 		}
 
 		@Override
 		protected boolean processResources() {
 			MetaModelManagerResourceSetAdapter adapter = MetaModelManagerResourceSetAdapter.getAdapter(resourceSet, null);	// ?? Shared global MMM
 			MetaModelManager metaModelManager = adapter.getMetaModelManager();
 			Set<EPackage> mmPackages = new HashSet<EPackage>();
 			for (Resource resource : resourceSet.getResources()) {
 				assert resource != null;
 				Ecore2Pivot ecore2Pivot = Ecore2Pivot.findAdapter(resource, metaModelManager);
 				if (ecore2Pivot == null) {			// Pivot has its own validation
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
 			}
 			Set<Resource> mmResources = new HashSet<Resource>();
 			for (EPackage mmPackage : mmPackages) {
 				Resource mmResource = EcoreUtil.getRootContainer(mmPackage).eResource();
 				if (mmResource != null) {
 					mmResources.add(mmResource);
 				}
  			}
 			for (Resource mmResource : mmResources) {
 				assert mmResource != null;
 				try {
 					Element pivotRoot = metaModelManager.loadResource(mmResource, null);
 					if (pivotRoot != null) {
 						List<org.eclipse.emf.ecore.resource.Resource.Diagnostic> errors = pivotRoot.eResource().getErrors();
 						assert errors != null;
 						String message = PivotUtil.formatResourceDiagnostics(errors, "", "\n");
 						if (message != null) {
 							return error("Failed to load Pivot from '" + mmResource.getURI(), message);
 						}
 					}
 					else {
 						return error("Failed to load Pivot from '" + mmResource.getURI(), "");
 					}
 				} catch (ParserException e) {
 					return error("Failed to load Pivot from '" + mmResource.getURI(), e.getMessage());
 				}
 			}
 
 			for (URI oclURI : getURIs()) {
 				assert oclURI != null;
 				if (!loadCSResource(resourceSet, metaModelManager, oclURI)) {
 					return false;
 				}
 			}
 	    	PivotEObjectValidator.install(resourceSet, metaModelManager);
 		    for (EPackage mmPackage : mmPackages) {
 		    	assert mmPackage != null;
 		    	PivotEObjectValidator.install(mmPackage);
 		    }
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
 		if (resourceSet != null) {
 			ResourceDialog dialog = new ResourceDialog((Shell)shell, editingDomain, resourceSet);
 			dialog.open();
 		}
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
 					MetaModelManager metaModelManager = PivotUtil.findMetaModelManager(xtextResource);
 					if (metaModelManager != null) {
 						return metaModelManager.getExternalResourceSet();
 					}
 					else {
 						return xtextResource.getResourceSet();
 					}
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
