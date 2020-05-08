 /*******************************************************************************
  * <copyright>
  *
  * Copyright (c) 2011, 2012 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Bug 336488 - DiagramEditor API
  *    mwenz - Bug 372753 - save shouldn't (necessarily) flush the command stack
  *    mwenz - Bug 376008 - Iterating through navigation history causes exceptions
  *    mwenz - Bug 393074 - Save Editor Progress Monitor Argument
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.ui.editor;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.MultiStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.emf.common.command.BasicCommandStack;
 import org.eclipse.emf.common.command.Command;
 import org.eclipse.emf.common.command.CommandStack;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.common.util.WrappedException;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.transaction.RecordingCommand;
 import org.eclipse.emf.transaction.TransactionalEditingDomain;
 import org.eclipse.graphiti.dt.IDiagramTypeProvider;
 import org.eclipse.graphiti.internal.IDiagramVersion;
 import org.eclipse.graphiti.mm.pictograms.Diagram;
 import org.eclipse.graphiti.mm.pictograms.PictogramsPackage;
 import org.eclipse.graphiti.ui.internal.GraphitiUIPlugin;
 import org.eclipse.graphiti.ui.internal.T;
 import org.eclipse.graphiti.ui.internal.services.GraphitiUiInternal;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.operation.ModalContext;
 import org.eclipse.swt.widgets.Display;
 
 /**
  * The default implementation for the {@link DiagramEditor} behavior extension
  * that controls the persistence behavior of the Graphiti diagram Editor.
  * Clients may subclass to change the behavior; use
  * {@link DiagramEditor#createPersistencyBehavior()} to return the instance that
  * shall be used.<br>
  * Note that there is always a 1:1 relation with a {@link DiagramEditor}.
  * 
  * @since 0.9
  */
 public class DefaultPersistencyBehavior {
 
 	/**
 	 * The associated {@link DiagramEditor}
 	 */
 	protected final DiagramEditor diagramEditor;
 
 	/**
 	 * Used to store the command that was executed before the editor was saved.
 	 * By comparing with the top of the current undo stack this point in the
 	 * command stack indicates if the editor is dirty.
 	 */
 	protected Command savedCommand = null;
 
 	/**
 	 * Creates a new instance of {@link DefaultPersistencyBehavior} that is
 	 * associated with the given {@link DiagramEditor}.
 	 * 
 	 * @param diagramEditor
 	 *            the associated {@link DiagramEditor}
 	 */
 	public DefaultPersistencyBehavior(DiagramEditor diagramEditor) {
 		this.diagramEditor = diagramEditor;
 	}
 
 	/**
 	 * This method is called to load the diagram into the editor. The default
 	 * implementation here will use the {@link TransactionalEditingDomain} and
 	 * its {@link ResourceSet} to load an EMF {@link Resource} that holds the
 	 * {@link Diagram}. It will also enable modification tracking on the diagram
 	 * {@link Resource}.
 	 * 
 	 * @param uri
 	 *            the {@link URI} of the diagram to load
 	 * @return the instance of the {@link Diagram} as it is resolved within the
 	 *         editor, meaning as it is resolved within the editor's
 	 *         {@link TransactionalEditingDomain}.
 	 */
 	public Diagram loadDiagram(URI uri) {
 		if (uri != null) {
 			final TransactionalEditingDomain editingDomain = diagramEditor.getEditingDomain();
 			if (editingDomain != null) {
 				// First try the URI resolution without loading not yet loaded
 				// resources because calling with loadOnDemand will _always_
 				// create a new Resource instance for newly created and not yet
 				// saved Resources, no matter if they already exist within the
 				// ResourceSet or not
 				EObject modelElement = null;
 				// Catch exceptions that happen while loading the resource to
 				// avoid spamming the log and showing nasty messages to the user
 				// in the editor, see Bug 376008
 				try {
 					modelElement = editingDomain.getResourceSet().getEObject(uri, false);
 					if (modelElement == null) {
 						modelElement = editingDomain.getResourceSet().getEObject(uri, true);
 						if (modelElement == null) {
 							return null;
 						}
 					}
 				} catch (WrappedException e) {
 					// Log only if debug tracing is active to avoid user
 					// confusion (message is shown in the editor anyhow)
 					T.racer().debug("Diagram with URI '" + uri.toString() + "' could not be loaded", e); //$NON-NLS-1$ //$NON-NLS-2$
 					return null;
 				}
 				modelElement.eResource().setTrackingModification(true);
 				return (Diagram) modelElement;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * This method is called to save a diagram. The default implementation here
 	 * saves all changes done to any of the EMF resources loaded within the
 	 * {@link DiagramEditor} so that the complete state of all modified objects
 	 * will be persisted in the file system.<br>
 	 * The default implementation also sets the current version information
 	 * (currently 0.10.0) to the diagram before saving it and wraps the save
 	 * operation inside a {@link IRunnableWithProgress} that cares about sending
 	 * only one {@link Resource} change event holding all modified files.
 	 * Besides also all adapters are temporarily switched off (see
 	 * {@link DiagramEditor#disableAdapters()}).<br>
 	 * To only modify the actual saving clients should rather override
 	 * {@link #save(TransactionalEditingDomain, Map)}.
 	 * 
 	 * @param monitor
 	 *            the Eclipse {@link IProgressMonitor} to use to report progress
 	 */
 	public void saveDiagram(IProgressMonitor monitor) {
 		if (monitor == null) {
 			monitor = new NullProgressMonitor();
 		}
 
 		// set version info.
 		final Diagram diagram = diagramEditor.getDiagramTypeProvider().getDiagram();
 		setDiagramVersion(diagram);
 
 		Map<Resource, Map<?, ?>> saveOptions = createSaveOptions();
 		final Set<Resource> savedResources = new HashSet<Resource>();
		final IRunnableWithProgress operation = createOperation(savedResources, saveOptions, monitor);
 
 		diagramEditor.disableAdapters();
 
 		try {
 			// This runs the options in a background thread reporting progress
 			// to the progress monitor passed into this method (see Bug 393074)
 			ModalContext.run(operation, true, monitor, Display.getCurrent());
 
 			BasicCommandStack commandStack = (BasicCommandStack) diagramEditor.getEditingDomain().getCommandStack();
 			commandStack.saveIsDone();
 
 			// Store the last executed command on the undo stack as save point
 			// and refresh the dirty state of the editor
 			savedCommand = commandStack.getUndoCommand();
 			diagramEditor.updateDirtyState();
 		} catch (final Exception exception) {
 			// Something went wrong that shouldn't.
 			T.racer().error(exception.getMessage(), exception);
 		} finally {
 			diagramEditor.enableAdapters();
 		}
 
 		Resource[] savedResourcesArray = savedResources.toArray(new Resource[savedResources.size()]);
 		diagramEditor.commandStackChanged(null);
 		IDiagramTypeProvider provider = diagramEditor.getConfigurationProvider().getDiagramTypeProvider();
 		provider.resourcesSaved(diagramEditor.getDiagramTypeProvider().getDiagram(), savedResourcesArray);
 	}
 
 	/**
 	 * Returns if the editor needs to be saved or not. Is queried by the
 	 * {@link DiagramEditor#isDirty()} method. The default implementation checks
 	 * if the top of the current undo stack is equal to the stored top command
 	 * of the undo stack at the time of the last saving of the editor.
 	 * 
 	 * @return <code>true</code> in case the editor needs to be saved,
 	 *         <code>false</code> otherwise.
 	 */
 	public boolean isDirty() {
 		BasicCommandStack commandStack = (BasicCommandStack) diagramEditor.getEditingDomain().getCommandStack();
 		return savedCommand != commandStack.getUndoCommand();
 	}
 
 	/**
 	 * Returns the EMF save options to be used when saving the EMF
 	 * {@link Resource}s.
 	 * 
 	 * @return a {@link Map} object holding the used EMF save options.
 	 */
 	protected Map<Resource, Map<?, ?>> createSaveOptions() {
 		// Save only resources that have actually changed.
 		final Map<Object, Object> saveOption = new HashMap<Object, Object>();
 		saveOption.put(Resource.OPTION_SAVE_ONLY_IF_CHANGED, Resource.OPTION_SAVE_ONLY_IF_CHANGED_MEMORY_BUFFER);
 		EList<Resource> resources = diagramEditor.getEditingDomain().getResourceSet().getResources();
 		final Map<Resource, Map<?, ?>> saveOptions = new HashMap<Resource, Map<?, ?>>();
 		for (Resource resource : resources) {
 			saveOptions.put(resource, saveOption);
 		}
 		return saveOptions;
 	}
 
 	/**
 	 * Creates the runnable to be used to wrap the actual saving of the EMF
 	 * {@link Resource}s.<br>
 	 * To only modify the actual saving clients should rather override
 	 * {@link #save(TransactionalEditingDomain, Map)}.
 	 * 
 	 * @param savedResources
 	 *            this parameter will after the operation has been performed
 	 *            contain all EMF {@link Resource}s that have really been saved.
 	 * 
 	 * @param saveOptions
 	 *            the EMF save options to use.
	 * @param monitor
	 *            The progress monitor to use inside the created runnable to
	 *            report progress
 	 * @return an {@link IRunnableWithProgress} instance wrapping the actual
 	 *         save process.
	 * @since 0.10 The parameter monitor has been added compared to the 0.9
	 *        version of this method
 	 */
 	protected IRunnableWithProgress createOperation(final Set<Resource> savedResources,
			final Map<Resource, Map<?, ?>> saveOptions, IProgressMonitor monitor) {
 		// Do the work within an operation because this is a long running
 		// activity that modifies the workbench.
 		final IRunnableWithProgress operation = new IRunnableWithProgress() {
 			// This is the method that gets invoked when the operation runs.
 			public void run(IProgressMonitor monitor) {
 				// Save the resources to the file system.
 				try {
 					savedResources.addAll(save(diagramEditor.getEditingDomain(), saveOptions, monitor));
 				} catch (final WrappedException e) {
 					final MultiStatus errorStatus = new MultiStatus(GraphitiUIPlugin.PLUGIN_ID, 0, e.getMessage(),
 							e.exception());
 					GraphitiUIPlugin.getDefault().getLog().log(errorStatus);
 					T.racer().error(e.getMessage(), e.exception());
 				}
 			}
 		};
 		return operation;
 	}
 
 	/**
 	 * Saves all resources in the given {@link TransactionalEditingDomain}. Can
 	 * be overridden to enable additional (call the super method to save the EMF
 	 * resources) or other persistencies.
 	 * 
 	 * @param editingDomain
 	 *            the {@link TransactionalEditingDomain} for which all resources
 	 *            will be saved
 	 * @param saveOptions
 	 *            the EMF save options used for the saving.
 	 * @param monitor
 	 *            The progress monitor to use for reporting progress
 	 * @return a {@link Set} of all EMF {@link Resource}s that where actually
 	 *         saved.
 	 * @since 0.10 The parameter monitor has been added compared to the 0.9
 	 *        version of this method
 	 */
 	protected Set<Resource> save(TransactionalEditingDomain editingDomain, Map<Resource, Map<?, ?>> saveOptions,
 			IProgressMonitor monitor) {
 		return GraphitiUiInternal.getEmfService().save(editingDomain, saveOptions, monitor);
 	}
 
 	/**
 	 * Called in {@link #saveDiagram(IProgressMonitor)} to update the Graphiti
 	 * diagram version before saving a diagram. Currently the diagram version is
 	 * set to 0.10.0
 	 * 
 	 * @param diagram
 	 *            the {@link Diagram} to update the version attribute for
 	 */
 	protected void setDiagramVersion(final Diagram diagram) {
 		// Only trigger a command if the version really changes to avoid an
 		// empty entry in the command stack / undo stack
 		if (!IDiagramVersion.CURRENT.equals(diagram.getVersion())) {
 			CommandStack commandStack = diagramEditor.getEditingDomain().getCommandStack();
 			commandStack.execute(new RecordingCommand(diagramEditor.getEditingDomain()) {
 				@Override
 				protected void doExecute() {
 					diagram.eSet(PictogramsPackage.eINSTANCE.getDiagram_Version(), IDiagramVersion.CURRENT);
 				}
 			});
 		}
 	}
 }
