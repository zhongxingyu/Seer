 /*******************************************************************************
  * Copyright (c) 2009, 2010 SpringSource, a divison of VMware, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     SpringSource, a division of VMware, Inc. - initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.virgo.ide.ui.editors;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.EventObject;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Map;
 
 import org.eclipse.core.filesystem.EFS;
 import org.eclipse.core.filesystem.IFileStore;
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.ProjectScope;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.common.command.BasicCommandStack;
 import org.eclipse.emf.common.command.Command;
 import org.eclipse.emf.common.command.CommandStack;
 import org.eclipse.emf.common.command.CommandStackListener;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
 import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
 import org.eclipse.emf.edit.provider.ReflectiveItemProviderAdapterFactory;
 import org.eclipse.emf.edit.provider.resource.ResourceItemProviderAdapterFactory;
 import org.eclipse.emf.edit.ui.util.EditUIUtil;
 import org.eclipse.jface.dialogs.ProgressMonitorDialog;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.pde.internal.core.ICoreConstants;
 import org.eclipse.pde.internal.core.PDECore;
 import org.eclipse.pde.internal.ui.PDEPlugin;
 import org.eclipse.pde.internal.ui.editor.context.InputContextManager;
 import org.eclipse.pde.internal.ui.editor.plugin.BundleInputContext;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IFileEditorInput;
 import org.eclipse.ui.IURIEditorInput;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.actions.WorkspaceModifyOperation;
 import org.eclipse.ui.dialogs.SaveAsDialog;
 import org.eclipse.ui.ide.FileStoreEditorInput;
 import org.eclipse.ui.part.FileEditorInput;
 import org.eclipse.virgo.ide.facet.core.FacetUtils;
 import org.eclipse.virgo.ide.par.Par;
 import org.eclipse.virgo.ide.par.provider.ParItemProviderAdapterFactory;
 import org.eclipse.virgo.ide.ui.ServerIdeUiPlugin;
 import org.eclipse.virgo.ide.ui.StatusHandler;
 import org.eclipse.virgo.ide.ui.editors.model.BundleModelUtility;
 
 /**
  * @author Christian Dupuis
  * @author Steffen Pingel
  */
 public class ParManifestEditor extends BundleManifestEditor {
 
 	public static String ID_EDITOR = "org.eclipse.virgo.ide.ui.parmanifest";
 
 	protected ComposedAdapterFactory adapterFactory;
 
 	protected Viewer currentViewer;
 
 	protected AdapterFactoryEditingDomain editingDomain;
 
 	protected Collection<Resource> savedResources = new ArrayList<Resource>();
 
 	private Resource parResource;
 
 	private Par par;
 
 	private IEditorInput parInput;
 
 	public AdapterFactoryEditingDomain getEditingDomain() {
 		return editingDomain;
 	}
 
 	public Par getPar() {
 		return par;
 	}
 
 	@Override
 	protected void addEditorPages() {
 		try {
 			addPage(new ParOverviewPage(this));
 			if (parInput != null) {
 				initializeEditingDomain();
 				createParPages();
 			}
 		} catch (PartInitException e) {
 			StatusHandler.log(new Status(IStatus.ERROR, ServerIdeUiPlugin.PLUGIN_ID, "Failed to create editor pages", e));
 		}
 		addSourcePage(BundleInputContext.CONTEXT_ID);
 	}
 
 	@Override
 	protected void createResourceContexts(InputContextManager manager, IFileEditorInput input) {
 		IFile file = input.getFile();
 		IContainer container = file.getParent();
 		org.eclipse.core.resources.IProject project = file.getProject();
 
 		IFile manifestFile = null;
 		IFile parFile = null;
 
 		String name = file.getName().toLowerCase(Locale.ENGLISH);
		if (name.equals("manifest.mf")) { //$NON-NLS-1$
 			if (container instanceof IFolder) {
 				container = container.getParent();
 			}
 			manifestFile = file;
 			parFile = FacetUtils.getParFile(project);
 		} else if (name.equalsIgnoreCase("org.eclipse.virgo.ide.runtime.core.par.xml")) {
 			parFile = file;
 			manifestFile = container.getProject().getFile(new Path("META-INF/MANIFEST.MF")); //$NON-NLS-1$
 		}
 		if (manifestFile != null && manifestFile.exists()) {
 			IEditorInput in = new FileEditorInput(manifestFile);
 			manager.putContext(in, new SpringBundleInputContext(this, in, file == manifestFile));
 		}
 		if (parFile != null && parFile.exists()) {
 			parInput = new FileEditorInput(parFile);
 		}
 		manager.monitorFile(manifestFile);
 		manager.monitorFile(parFile);
 
 		fPrefs = new ProjectScope(container.getProject()).getNode(PDECore.PLUGIN_ID);
 		if (fPrefs != null) {
 			fShowExtensions = fPrefs.getBoolean(ICoreConstants.EXTENSIONS_PROPERTY, true);
 			fEquinox = fPrefs.getBoolean(ICoreConstants.EQUINOX_PROPERTY, true);
 		}
 	}
 
 	@Override
 	protected void createSystemFileContexts(InputContextManager manager, IEditorInput input) {
 		File file = (File) input.getAdapter(File.class);
 		if (file == null && input instanceof FileStoreEditorInput) {
 			file = new File(((IURIEditorInput) input).getURI());
 		}
 		if (file == null) {
 			return;
 		}
 		File manifestFile = null;
 		File parFile = null;
 		String name = file.getName().toLowerCase(Locale.ENGLISH);
		if (name.equals("manifest.mf")) { //$NON-NLS-1$
 			manifestFile = file;
 			File dir = file.getParentFile().getParentFile();
 			parFile = new File(dir, "org.eclipse.virgo.ide.runtime.core.par.xml");
 		} else if (name.equals("org.eclipse.virgo.ide.runtime.core.par.xml")) {
 			parFile = file;
 			File dir = file.getParentFile();
 			manifestFile = new File(dir.getParentFile(), "META-INF/MANIFEST.MF"); //$NON-NLS-1$
 		}
 		try {
 			if (manifestFile != null && manifestFile.exists()) {
 				IEditorInput in = PdeCompatibilityUtil.createSystemFileEditorInput(manifestFile);
 				if (in == null) {
 					// Eclipse 3.5 or later
 					IFileStore store = EFS.getStore(manifestFile.toURI());
 					in = new FileStoreEditorInput(store);
 				}
 				manager.putContext(in, new SpringBundleInputContext(this, in, file == manifestFile));
 			}
 			if (parFile != null && parFile.exists()) {
 				parInput = PdeCompatibilityUtil.createSystemFileEditorInput(parFile);
 				if (parInput == null) {
 					// Eclipse 3.5 or later
 					IFileStore store = EFS.getStore(parFile.toURI());
 					parInput = new FileStoreEditorInput(store);
 				}
 			}
 		} catch (CoreException e) {
 			PDEPlugin.logException(e);
 		}
 	}
 
 	protected void initializeEditingDomain() {
 		// Create an adapter factory that yields item providers.
 		//
 		adapterFactory = new ComposedAdapterFactory(ComposedAdapterFactory.Descriptor.Registry.INSTANCE);
 
 		adapterFactory.addAdapterFactory(new ResourceItemProviderAdapterFactory());
 		adapterFactory.addAdapterFactory(new ParItemProviderAdapterFactory());
 		adapterFactory.addAdapterFactory(new ReflectiveItemProviderAdapterFactory());
 
 		// Create the command stack that will notify this editor as commands are
 		// executed.
 		//
 		BasicCommandStack commandStack = new BasicCommandStack();
 
 		// Add a listener to set the most recent command's affected objects to
 		// be the selection of the viewer with focus.
 		//
 		commandStack.addCommandStackListener(new CommandStackListener() {
 			public void commandStackChanged(final EventObject event) {
 				getContainer().getDisplay().asyncExec(new Runnable() {
 					public void run() {
 						firePropertyChange(IEditorPart.PROP_DIRTY);
 
 						// Try to select the affected objects.
 						//
 						Command mostRecentCommand = ((CommandStack) event.getSource()).getMostRecentCommand();
 						if (mostRecentCommand != null) {
 							setSelectionToViewer(mostRecentCommand.getAffectedObjects());
 						}
 					}
 				});
 			}
 		});
 
 		// Create the editing domain with a special command stack.
 		//
 		editingDomain = new AdapterFactoryEditingDomain(adapterFactory, commandStack, new HashMap<Resource, Boolean>());
 	}
 
 	public void setSelectionToViewer(Collection<?> collection) {
 		final Collection<?> theSelection = collection;
 		// Make sure it's okay.
 		//
 		if (theSelection != null && !theSelection.isEmpty()) {
 			// I don't know if this should be run this deferred
 			// because we might have to give the editor a chance to process the
 			// viewer update events
 			// and hence to update the views first.
 			//
 			//
 			Runnable runnable = new Runnable() {
 				public void run() {
 					// Try to select the items in the current content viewer
 					// of the editor.
 					//
 					if (currentViewer != null) {
 						currentViewer.setSelection(new StructuredSelection(theSelection.toArray()), true);
 					}
 				}
 			};
 			runnable.run();
 		}
 	}
 
 	public void createModel() {
 		URI resourceURI = EditUIUtil.getURI(parInput);
 		try {
 			// Load the resource through the editing domain.
 			//
 			parResource = editingDomain.getResourceSet().getResource(resourceURI, true);
 		} catch (Exception e) {
 			ServerIdeUiPlugin.getDefault().log(e);
 			parResource = editingDomain.getResourceSet().getResource(resourceURI, false);
 		}
 
 		par = (Par) parResource.getContents().iterator().next();
 	}
 
 	public void createParPages() {
 		createModel();
 		addParPages();
 	}
 
 	protected void addParPages() {
 		try {
 			addPage(new ParXmlEditorPage(this, "org.eclipse.virgo.ide.ui.editor.par.dependencies", "Dependencies"));
 		} catch (PartInitException e) {
 			ServerIdeUiPlugin.getDefault().log(e);
 		}
 	}
 
 	@Override
 	public void doSave(IProgressMonitor progressMonitor) {
 		// Save only resources that have actually changed.
 		//
 		final Map<Object, Object> saveOptions = new HashMap<Object, Object>();
 		saveOptions.put(Resource.OPTION_SAVE_ONLY_IF_CHANGED, Resource.OPTION_SAVE_ONLY_IF_CHANGED_MEMORY_BUFFER);
 
 		// Do the work within an operation because this is a long running
 		// activity that modifies the workbench.
 		//
 		WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {
 			// This is the method that gets invoked when the operation runs.
 			//
 			@Override
 			public void execute(IProgressMonitor monitor) {
 				// Save the resources to the file system.
 				//
 				boolean first = true;
 				for (Resource resource : editingDomain.getResourceSet().getResources()) {
 					if ((first || !resource.getContents().isEmpty() || isPersisted(resource))
 							&& !editingDomain.isReadOnly(resource)) {
 						try {
 							savedResources.add(resource);
 							resource.save(saveOptions);
 						} catch (IOException exception) {
 							handleError(exception);
 						}
 						first = false;
 					}
 				}
 			}
 		};
 
 		try {
 			// This runs the options, and shows progress.
 			//
 			new ProgressMonitorDialog(getSite().getShell()).run(true, false, operation);
 
 			// Refresh the necessary state.
 			//
 			((BasicCommandStack) editingDomain.getCommandStack()).saveIsDone();
 			firePropertyChange(IEditorPart.PROP_DIRTY);
 		} catch (Exception exception) {
 			// Something went wrong that shouldn't.
 			//
 			ServerIdeUiPlugin.getDefault().log(exception);
 		}
 		super.doSave(progressMonitor);
 	}
 
 	protected boolean isPersisted(Resource resource) {
 		boolean result = false;
 		try {
 			InputStream stream = editingDomain.getResourceSet().getURIConverter().createInputStream(resource.getURI());
 			if (stream != null) {
 				result = true;
 				stream.close();
 			}
 		} catch (IOException e) {
 			// Ignore
 		}
 		return result;
 	}
 
 	private void handleError(IOException exception) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void doSaveAs() {
 		SaveAsDialog saveAsDialog = new SaveAsDialog(getSite().getShell());
 		saveAsDialog.open();
 		IPath path = saveAsDialog.getResult();
 		if (path != null) {
 			IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
 			if (file != null) {
 				doSaveAs(URI.createPlatformResourceURI(file.getFullPath().toString(), true), new FileEditorInput(file));
 			}
 		}
 		super.doSaveAs();
 	}
 
 	protected void doSaveAs(URI uri, IEditorInput editorInput) {
 		(editingDomain.getResourceSet().getResources().get(0)).setURI(uri);
 		setInputWithNotify(editorInput);
 		setPartName(editorInput.getName());
 		IProgressMonitor progressMonitor = new NullProgressMonitor();
 		// getActionBars().getStatusLineManager() != null ? getActionBars()
 		// .getStatusLineManager().getProgressMonitor() : new
 		// NullProgressMonitor();
 		doSave(progressMonitor);
 	}
 
 	@Override
 	public boolean isDirty() {
 		if (editingDomain != null && editingDomain.getCommandStack() != null) {
 			boolean dirty = ((BasicCommandStack) editingDomain.getCommandStack()).isSaveNeeded();
 			return dirty || super.isDirty();
 		}
 		return super.isDirty();
 	}
 
 }
