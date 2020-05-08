 /**
  * <copyright>
  * </copyright>
  *
  * $Id$
  */
 package era.foss.objecteditor;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.EventObject;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceChangeListener;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IResourceDeltaVisitor;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.emf.common.command.BasicCommandStack;
 import org.eclipse.emf.common.command.Command;
 import org.eclipse.emf.common.command.CommandStack;
 import org.eclipse.emf.common.command.CommandStackListener;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.ui.MarkerHelper;
 import org.eclipse.emf.common.ui.ViewerPane;
 import org.eclipse.emf.common.ui.viewer.IViewerProvider;
 import org.eclipse.emf.common.util.BasicDiagnostic;
 import org.eclipse.emf.common.util.Diagnostic;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EValidator;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.util.Diagnostician;
 import org.eclipse.emf.ecore.util.EContentAdapter;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.ecore.xmi.XMIResource;
 import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.emf.edit.domain.IEditingDomainProvider;
 import org.eclipse.emf.edit.provider.AdapterFactoryItemDelegator;
 import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
 import org.eclipse.emf.edit.provider.ReflectiveItemProviderAdapterFactory;
 import org.eclipse.emf.edit.provider.resource.ResourceItemProviderAdapterFactory;
 import org.eclipse.emf.edit.ui.util.EditUIMarkerHelper;
 import org.eclipse.emf.edit.ui.util.EditUIUtil;
 import org.eclipse.emf.edit.ui.view.ExtendedPropertySheetPage;
 import org.eclipse.jface.action.IStatusLineManager;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.dialogs.ProgressMonitorDialog;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.actions.WorkspaceModifyOperation;
 import org.eclipse.ui.dialogs.SaveAsDialog;
 import org.eclipse.ui.ide.IGotoMarker;
 import org.eclipse.ui.part.EditorPart;
 import org.eclipse.ui.part.FileEditorInput;
 import org.eclipse.ui.part.WorkbenchPart;
 import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
 import org.eclipse.ui.views.properties.IPropertySheetPage;
 import org.eclipse.ui.views.properties.PropertySheetPage;
 import era.foss.erf.ERF;
 import era.foss.erf.SpecObject;
 import era.foss.erf.provider.ErfItemProviderAdapterFactory;
 
 /**
  * This is an example of a Erf model editor.
  */
 public class ErfObjectEditor extends EditorPart implements IEditorPart, IEditingDomainProvider, ISelectionProvider,
         IViewerProvider, IGotoMarker, IAdapterFactoryProvider {
 
     /**
      * This keeps track of the editing domain that is used to track all changes to the model.
      */
     protected AdapterFactoryEditingDomain editingDomain;
 
     /**
      * This is the one adapter factory used for providing views of the model.
      */
     protected ComposedAdapterFactory adapterFactory;
 
     /**
      * This is the content outline page.
      */
     protected IContentOutlinePage contentOutlinePage;
 
     /**
      * This is a kludge...
      */
     protected IStatusLineManager contentOutlineStatusLineManager;
 
     /**
      * This keeps track of the active viewer pane, in the book. <!-- begin-user-doc --> <!-- end-user-doc -->
      * 
      * @generated
      */
     protected ViewerPane specObjectViewerPane;
 
     /**
      * This is the content outline page's viewer.
      */
     protected TreeViewer contentOutlineViewer;
 
     /**
      * This is the property sheet page.
      */
     protected PropertySheetPage propertySheetPage;
 
     /**
      * This keeps track of the active content viewer, which may be either one of the viewers in the pages or the content
      * outline viewer.
      */
     protected Viewer currentViewer;
 
     /**
      * This listens to which ever viewer is active.
      */
     protected ISelectionChangedListener selectionChangedListener;
 
     /**
      * This keeps track of all the {@link org.eclipse.jface.viewers.ISelectionChangedListener}s that are listening to
      * this editor.
      */
     protected Collection<ISelectionChangedListener> selectionChangedListeners = new ArrayList<ISelectionChangedListener>();
 
     /**
      * This keeps track of the selection of the editor as a whole.
      */
     protected ISelection editorSelection = StructuredSelection.EMPTY;
 
     /**
      * The MarkerHelper is responsible for creating workspace resource markers presented in Eclipse's Problems View.
      */
     protected MarkerHelper markerHelper = new EditUIMarkerHelper();
 
     /**
      * Resources that have been removed since last activation.
      */
     protected Collection<Resource> removedResources = new ArrayList<Resource>();
 
     /**
      * Resources that have been changed since last activation.
      */
     protected Collection<Resource> changedResources = new ArrayList<Resource>();
 
     /**
      * Resources that have been saved.
      */
     protected Collection<Resource> savedResources = new ArrayList<Resource>();
 
     /**
      * Map to store the diagnostic associated with a resource.
      */
     protected Map<Resource, Diagnostic> resourceToDiagnosticMap = new LinkedHashMap<Resource, Diagnostic>();
 
     /**
      * Controls whether the problem indication should be updated.
      */
     protected boolean updateProblemIndication = true;
     
 
 
     /**
      * Adapter used to update the problem indication when resources are demanded loaded.
      */
     protected EContentAdapter problemIndicationAdapter = new EContentAdapter() {
         @Override
         public void notifyChanged( Notification notification ) {
             if( notification.getNotifier() instanceof Resource ) {
                 switch (notification.getFeatureID( Resource.class )) {
                 case Resource.RESOURCE__IS_LOADED:
                 case Resource.RESOURCE__ERRORS:
                 case Resource.RESOURCE__WARNINGS: {
                     Resource resource = (Resource)notification.getNotifier();
                     Diagnostic diagnostic = analyzeResourceProblems( resource, null );
                     if( diagnostic.getSeverity() != Diagnostic.OK ) {
                         resourceToDiagnosticMap.put( resource, diagnostic );
                     } else {
                         resourceToDiagnosticMap.remove( resource );
                     }
 
                     if( updateProblemIndication ) {
                         getSite().getShell().getDisplay().asyncExec( new Runnable() {
                             public void run() {
                                 updateProblemIndication();
                             }
                         } );
                     }
                     break;
                 }
                 }
             } else {
                 super.notifyChanged( notification );
             }
         }
 
         @Override
         protected void setTarget( Resource target ) {
             basicSetTarget( target );
         }
 
         @Override
         protected void unsetTarget( Resource target ) {
             basicUnsetTarget( target );
         }
     };
 
     /**
      * This listens for workspace changes.
      */
     protected IResourceChangeListener resourceChangeListener = new IResourceChangeListener() {
         public void resourceChanged( IResourceChangeEvent event ) {
             IResourceDelta delta = event.getDelta();
             try {
                 class ResourceDeltaVisitor implements IResourceDeltaVisitor {
                     protected ResourceSet resourceSet = editingDomain.getResourceSet();
                     protected Collection<Resource> changedResources = new ArrayList<Resource>();
                     protected Collection<Resource> removedResources = new ArrayList<Resource>();
 
                     public boolean visit( IResourceDelta delta ) {
                         if( delta.getResource().getType() == IResource.FILE ) {
                             if( delta.getKind() == IResourceDelta.REMOVED
                                 || delta.getKind() == IResourceDelta.CHANGED
                                 && delta.getFlags() != IResourceDelta.MARKERS ) {
                                 Resource resource = resourceSet.getResource( URI.createPlatformResourceURI( delta.getFullPath()
                                                                                                                  .toString(),
                                                                                                             true ),
                                                                              false );
                                 if( resource != null ) {
                                     if( delta.getKind() == IResourceDelta.REMOVED ) {
                                         removedResources.add( resource );
                                     } else if( !savedResources.remove( resource ) ) {
                                         changedResources.add( resource );
                                     }
                                 }
                             }
                         }
 
                         return true;
                     }
 
                     public Collection<Resource> getChangedResources() {
                         return changedResources;
                     }
 
                     public Collection<Resource> getRemovedResources() {
                         return removedResources;
                     }
                 }
 
                 final ResourceDeltaVisitor visitor = new ResourceDeltaVisitor();
                 delta.accept( visitor );
 
                 if( !visitor.getRemovedResources().isEmpty() ) {
                     getSite().getShell().getDisplay().asyncExec( new Runnable() {
                         public void run() {
                             removedResources.addAll( visitor.getRemovedResources() );
                             if( !isDirty() ) {
                                 getSite().getPage().closeEditor( ErfObjectEditor.this, false );
                             }
                         }
                     } );
                 }
 
                 if( !visitor.getChangedResources().isEmpty() ) {
                     getSite().getShell().getDisplay().asyncExec( new Runnable() {
                         public void run() {
                             changedResources.addAll( visitor.getChangedResources() );
                             if( getSite().getPage().getActiveEditor() == ErfObjectEditor.this ) {
                                 handleActivate();
                             }
                         }
                     } );
                 }
             } catch( CoreException exception ) {
                 ErfObjectEditorPlugin.INSTANCE.log( exception );
             }
         }
     };
 
     /**
      * Handles activation of the editor or it's associated views.
      */
     protected void handleActivate() {
         // Recompute the read only state.
         //
         if( editingDomain.getResourceToReadOnlyMap() != null ) {
             editingDomain.getResourceToReadOnlyMap().clear();
 
             // Refresh any actions that may become enabled or disabled.
             //
             setSelection( getSelection() );
         }
 
         if( !removedResources.isEmpty() ) {
             if( handleDirtyConflict() ) {
                 getSite().getPage().closeEditor( ErfObjectEditor.this, false );
             } else {
                 removedResources.clear();
                 changedResources.clear();
                 savedResources.clear();
             }
         } else if( !changedResources.isEmpty() ) {
             changedResources.removeAll( savedResources );
             handleChangedResources();
             changedResources.clear();
             savedResources.clear();
         }
     }
 
     /**
      * Handles what to do with changed resources on activation.
      */
     protected void handleChangedResources() {
         if( !changedResources.isEmpty() && (!isDirty() || handleDirtyConflict()) ) {
             if( isDirty() ) {
                 changedResources.addAll( editingDomain.getResourceSet().getResources() );
             }
             editingDomain.getCommandStack().flush();
 
             updateProblemIndication = false;
             for( Resource resource : changedResources ) {
                 if( resource.isLoaded() ) {
                     resource.unload();
                     try {
                         resource.load( Collections.EMPTY_MAP );
                     } catch( IOException exception ) {
                         if( !resourceToDiagnosticMap.containsKey( resource ) ) {
                             resourceToDiagnosticMap.put( resource, analyzeResourceProblems( resource, exception ) );
                         }
                     }
                 }
             }
 
             if( AdapterFactoryEditingDomain.isStale( editorSelection ) ) {
                 setSelection( StructuredSelection.EMPTY );
             }
 
             updateProblemIndication = true;
             updateProblemIndication();
         }
     }
 
     /**
      * Updates the problems indication with the information described in the specified diagnostic.
      */
     protected void updateProblemIndication() {
         if( updateProblemIndication ) {
             BasicDiagnostic diagnostic = new BasicDiagnostic(
                 Diagnostic.OK,
                 "era.foss.objecteditor",
                 0,
                 null,
                 new Object[]{editingDomain.getResourceSet()} );
             for( Diagnostic childDiagnostic : resourceToDiagnosticMap.values() ) {
                 if( childDiagnostic.getSeverity() != Diagnostic.OK ) {
                     diagnostic.add( childDiagnostic );
                 }
             }
 
             // TODO: CPN-deletion - Is ProblemEditorPart of any relevance?
             // int lastEditorPage = getPageCount() - 1;
             // if( lastEditorPage >= 0 && getEditor( lastEditorPage ) instanceof ProblemEditorPart ) {
             // ((ProblemEditorPart)getEditor( lastEditorPage )).setDiagnostic( diagnostic );
             // if( diagnostic.getSeverity() != Diagnostic.OK ) {
             // setActivePage( lastEditorPage );
             // }
             // } else if( diagnostic.getSeverity() != Diagnostic.OK ) {
             // ProblemEditorPart problemEditorPart = new ProblemEditorPart();
             // problemEditorPart.setDiagnostic( diagnostic );
             // problemEditorPart.setMarkerHelper( markerHelper );
             // try {
             // addPage( ++lastEditorPage, problemEditorPart, getEditorInput() );
             // setPageText( lastEditorPage, problemEditorPart.getPartName() );
             // setActivePage( lastEditorPage );
             // showTabs();
             // } catch( PartInitException exception ) {
             // ErfObjectEditorPlugin.INSTANCE.log( exception );
             // }
             // }
             markerHelper.deleteMarkers( editingDomain.getResourceSet() );
             if( markerHelper.hasMarkers( editingDomain.getResourceSet() ) ) {
                 if( diagnostic.getSeverity() != Diagnostic.OK ) {
                     try {
                         markerHelper.createMarkers( diagnostic );
                     } catch( CoreException exception ) {
                         ErfObjectEditorPlugin.INSTANCE.log( exception );
                     }
                 }
             }
         }
     }
 
     /**
      * Shows a dialog that asks if conflicting changes should be discarded.
      */
     protected boolean handleDirtyConflict() {
         return MessageDialog.openQuestion( getSite().getShell(),
                                            getString( "_UI_FileConflict_label" ),
                                            getString( "_WARN_FileConflict" ) );
     }
 
     /**
      * This creates a model editor.
      */
     public ErfObjectEditor() {
         super();
         initializeEditingDomain();
     }
 
     /**
      * This sets up the editing domain for the model editor.
      */
     protected void initializeEditingDomain() {
         // Create an adapter factory that yields item providers.
         //
         adapterFactory = new ComposedAdapterFactory( ComposedAdapterFactory.Descriptor.Registry.INSTANCE );
 
         adapterFactory.addAdapterFactory( new ResourceItemProviderAdapterFactory() );
         adapterFactory.addAdapterFactory( new ErfItemProviderAdapterFactory() );
         adapterFactory.addAdapterFactory( new ReflectiveItemProviderAdapterFactory() );
 
         // Create the command stack that will notify this editor as commands are executed.
         //
         BasicCommandStack commandStack = new EraCommandStack();
 
         // Add a listener to set the most recent command's affected objects to be the selection of the viewer with
         // focus.
         //
         commandStack.addCommandStackListener( new CommandStackListener() {
             
             private Job validateJob = null;
             
             /**
              * create a validate job if none is existing yet
              * The validate job asynchronously checks if the erfModel is valid
              * 
              * @return the validate job 
              */
             private Job getValidateJob() {
                 if( validateJob == null ) {
                     validateJob = new Job( "Validation" ) {
                         ErfMarkerHelper markerHelper = new ErfMarkerHelper();
                         Resource erfResource = (XMIResource)editingDomain.getResourceSet()
                                                                          .getResource( EditUIUtil.getURI( ErfObjectEditor.this.getEditorInput() ),
                                                                                        true );
                         ERF erfModel = (ERF)(erfResource).getContents().get( 0 );
 
                         public IStatus run( IProgressMonitor monitor ) {
                             markerHelper.deleteMarkers( erfResource );
                             Diagnostic diagnostic = Diagnostician.INSTANCE. validate( erfModel);
                             markerHelper.createMarkers( diagnostic );
                             return Status.OK_STATUS;
                         }
                     };
                 }
                 return validateJob;
             }
              
             
             public void commandStackChanged( final EventObject event ) {
                 ErfObjectEditor.this.specObjectViewerPane.getControl().getDisplay().asyncExec( new Runnable() {
                     public void run() {
                         firePropertyChange( IEditorPart.PROP_DIRTY );
 
                         // Try to select the affected objects. (e.g. skip to respective objects when perform ing an UNDO
                         // operation)
                         Command mostRecentCommand = ((CommandStack)event.getSource()).getMostRecentCommand();
                         if( mostRecentCommand != null ) {
                             setSelectionToViewer( mostRecentCommand.getAffectedObjects() );
                         }
                         
                         // refresh property page
                         if( propertySheetPage != null && !propertySheetPage.getControl().isDisposed() ) {
                             propertySheetPage.refresh();
                         }
                         
                         // start model validation
                         getValidateJob().cancel();
                         getValidateJob().schedule();
                     }
                 } );
                 
             }
         } );
 
         // Create the editing domain with a special command stack.
         //
         editingDomain = new AdapterFactoryEditingDomain(
             adapterFactory,
             commandStack,
             new HashMap<Resource, Boolean>() );
     }
 
     /**
      * This is here for the listener to be able to call it.
      */
     // TODO: CPN-wtf? - fire property changes
     @Override
     protected void firePropertyChange( int action ) {
         super.firePropertyChange( action );
     }
 
     /**
      * This sets the selection into whichever viewer is active.
      */
     public void setSelectionToViewer( Collection<?> collection ) {
         final Collection<?> theSelection = collection;
         if( theSelection != null && !theSelection.isEmpty() ) {
             Runnable runnable = new Runnable() {
                 public void run() {
                     ArrayList<SpecObject> specObjectList = new ArrayList<SpecObject>();
                     // Check all elements in selection and set to the spec object
                     for( Object element : theSelection ) {
                         if( element instanceof EObject ) {
                             EObject eObject = (EObject)element;
                             if( eObject instanceof SpecObject ) {
                                 specObjectList.add( (SpecObject)eObject );
                             } else if( eObject.eContainer() instanceof SpecObject ) {
                                 specObjectList.add( (SpecObject)eObject.eContainer() );
                             }
                             // TODO: if element is part of the type editor, open type editor, 
                             // navigate to correct tab and mark the respective element
                         }
                     }
 
                     if( currentViewer != null && ! specObjectList.isEmpty()) {
                         currentViewer.setSelection( new StructuredSelection( specObjectList.toArray() ), true );
                     }
                 }
             };
             getSite().getShell().getDisplay().asyncExec( runnable );
         }
     }
 
     /**
      * This returns the editing domain as required by the {@link IEditingDomainProvider} interface. This is important
      * for implementing the static methods of {@link AdapterFactoryEditingDomain} and for supporting
      * {@link org.eclipse.emf.edit.ui.action.CommandAction}.
      */
     public EditingDomain getEditingDomain() {
         return editingDomain;
     }
 
     /**
      * This makes sure that one content viewer, either for the current page or the outline view, if it has focus, is the
      * current one.
      */
     public void setCurrentViewer( Viewer viewer ) {
         // If it is changing...
         //
         if( currentViewer != viewer ) {
             if( selectionChangedListener == null ) {
                 // Create the listener on demand.
                 //
                 selectionChangedListener = new ISelectionChangedListener() {
                     // This just notifies those things that are affected by the section.
                     //
                     public void selectionChanged( SelectionChangedEvent selectionChangedEvent ) {
                         setSelection( selectionChangedEvent.getSelection() );
                     }
                 };
             }
 
             // Stop listening to the old one.
             //
             if( currentViewer != null ) {
                 currentViewer.removeSelectionChangedListener( selectionChangedListener );
             }
 
             // Start listening to the new one.
             //
             if( viewer != null ) {
                 viewer.addSelectionChangedListener( selectionChangedListener );
             }
 
             // Remember it.
             //
             currentViewer = viewer;
 
             // Set the editors selection based on the current viewer's selection.
             //
             setSelection( currentViewer == null ? StructuredSelection.EMPTY : currentViewer.getSelection() );
         }
     }
 
     /**
      * This returns the viewer as required by the {@link IViewerProvider} interface.
      */
     public Viewer getViewer() {
         return currentViewer;
     }
 
     /**
      * This is the method called to load a resource into the editing domain's resource set based on the editor's input.
      */
     public void createModel() {
         URI resourceURI = EditUIUtil.getURI( getEditorInput() );
         Exception exception = null;
         Resource resource = null;
         try {
             // Load the resource through the editing domain.
             //
             resource = editingDomain.getResourceSet().getResource( resourceURI, true );
         } catch( Exception e ) {
             exception = e;
             resource = editingDomain.getResourceSet().getResource( resourceURI, false );
         }
 
         Diagnostic diagnostic = analyzeResourceProblems( resource, exception );
         if( diagnostic.getSeverity() != Diagnostic.OK ) {
             resourceToDiagnosticMap.put( resource, analyzeResourceProblems( resource, exception ) );
         }
         editingDomain.getResourceSet().eAdapters().add( problemIndicationAdapter );
     }
 
     /**
      * Returns a diagnostic describing the errors and warnings listed in the resource and the specified exception (if
      * any).
      */
     public Diagnostic analyzeResourceProblems( Resource resource, Exception exception ) {
         if( !resource.getErrors().isEmpty() || !resource.getWarnings().isEmpty() ) {
             BasicDiagnostic basicDiagnostic = new BasicDiagnostic(
                 Diagnostic.ERROR,
                 "era.foss.objecteditor",
                 0,
                 getString( "_UI_CreateModelError_message", resource.getURI() ),
                 new Object[]{exception == null ? (Object)resource : exception} );
             basicDiagnostic.merge( EcoreUtil.computeDiagnostic( resource, true ) );
             return basicDiagnostic;
         } else if( exception != null ) {
             return new BasicDiagnostic(
                 Diagnostic.ERROR,
                 "era.foss.objecteditor",
                 0,
                 getString( "_UI_CreateModelError_message", resource.getURI() ),
                 new Object[]{exception} );
         } else {
             return Diagnostic.OK_INSTANCE;
         }
     }
 
     /**
      * This is how the framework determines which interfaces we implement.
      */
     @Override
    public Object getAdapter( @SuppressWarnings("rawtypes") Class key ) {
         if( key.equals( IContentOutlinePage.class ) ) {
             return showOutlineView() ? getContentOutlinePage() : null;
         } else if( key.equals( IPropertySheetPage.class ) ) {
             return getPropertySheetPage();
         } else if( key.equals( IGotoMarker.class ) ) {
             return this;
         } else {
             return super.getAdapter( key );
         }
     }
 
     /**
      * This accesses a cached version of the content outliner.
      */
     public IContentOutlinePage getContentOutlinePage() {
         //TODO: the outline shall display the hierarchy as soon as it is supported by ERA
         return null;
     }
 
     /**
      * This accesses a cached version of the property sheet.
      */
     public IPropertySheetPage getPropertySheetPage() {
         if( propertySheetPage == null ) {
             
             propertySheetPage = new ExtendedPropertySheetPage( editingDomain ) {
                 @Override
                 public void setSelectionToViewer( List<?> selection ) {
                     ErfObjectEditor.this.setSelectionToViewer( selection );
                     ErfObjectEditor.this.setFocus();
                 }
                 
             };
             
             propertySheetPage.setPropertySourceProvider( new SpecObjectPropertySourceProvider());
         }
 
         return propertySheetPage;
     }
 
     /**
      * This is for implementing {@link IEditorPart} and simply tests the command stack.
      */
     @Override
     public boolean isDirty() {
         return ((BasicCommandStack)editingDomain.getCommandStack()).isSaveNeeded();
     }
 
     /**
      * This is for implementing {@link IEditorPart} and simply saves the model file.
      */
     @Override
     public void doSave( IProgressMonitor progressMonitor ) {
         // Save only resources that have actually changed.
         //
         final Map<Object, Object> saveOptions = new HashMap<Object, Object>();
         saveOptions.put( Resource.OPTION_SAVE_ONLY_IF_CHANGED, Resource.OPTION_SAVE_ONLY_IF_CHANGED_MEMORY_BUFFER );
 
         // Do the work within an operation because this is a long running activity that modifies the workbench.
         //
         WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {
             // This is the method that gets invoked when the operation runs.
             //
             @Override
             public void execute( IProgressMonitor monitor ) {
                 // Save the resources to the file system.
                 //
                 boolean first = true;
                 for( Resource resource : editingDomain.getResourceSet().getResources() ) {
                     if( (first || !resource.getContents().isEmpty() || isPersisted( resource ))
                         && !editingDomain.isReadOnly( resource ) ) {
                         try {
                             long timeStamp = resource.getTimeStamp();
                             resource.save( saveOptions );
                             if( resource.getTimeStamp() != timeStamp ) {
                                 savedResources.add( resource );
                             }
                         } catch( Exception exception ) {
                             resourceToDiagnosticMap.put( resource, analyzeResourceProblems( resource, exception ) );
                         }
                         first = false;
                     }
                 }
             }
         };
 
         updateProblemIndication = false;
         try {
             // This runs the options, and shows progress.
             //
             new ProgressMonitorDialog( getSite().getShell() ).run( true, false, operation );
 
             // Refresh the necessary state.
             //
             ((BasicCommandStack)editingDomain.getCommandStack()).saveIsDone();
             firePropertyChange( IEditorPart.PROP_DIRTY );
         } catch( Exception exception ) {
             // Something went wrong that shouldn't.
             //
             ErfObjectEditorPlugin.INSTANCE.log( exception );
         }
         updateProblemIndication = true;
         updateProblemIndication();
     }
 
     /**
      * This returns whether something has been persisted to the URI of the specified resource. The implementation uses
      * the URI converter from the editor's resource set to try to open an input stream.
      */
     protected boolean isPersisted( Resource resource ) {
         boolean result = false;
         try {
             InputStream stream = editingDomain.getResourceSet()
                                               .getURIConverter()
                                               .createInputStream( resource.getURI() );
             if( stream != null ) {
                 result = true;
                 stream.close();
             }
         } catch( IOException e ) {
             // Ignore
         }
         return result;
     }
 
     /**
      * This always returns true because it is not currently supported.
      */
     @Override
     public boolean isSaveAsAllowed() {
         return true;
     }
 
     /**
      * This also changes the editor's input.
      */
     @Override
     public void doSaveAs() {
         SaveAsDialog saveAsDialog = new SaveAsDialog( getSite().getShell() );
         saveAsDialog.open();
         IPath path = saveAsDialog.getResult();
         if( path != null ) {
             IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile( path );
             if( file != null ) {
                 doSaveAs( URI.createPlatformResourceURI( file.getFullPath().toString(), true ), new FileEditorInput(
                     file ) );
             }
         }
     }
 
     /**
      * See {@link org.eclipse.ui.ide.IGotoMarker}
      */
     public void gotoMarker( IMarker marker ) {
         try {
             if( marker.getType().equals( EValidator.MARKER ) ) {
                 String uriAttribute = marker.getAttribute( EValidator.URI_ATTRIBUTE, null );
                 if( uriAttribute != null ) {
                     URI uri = URI.createURI( uriAttribute );
                     EObject eObject = editingDomain.getResourceSet().getEObject( uri, true );
                     if( eObject != null ) {
                         setSelectionToViewer( Collections.singleton( editingDomain.getWrapper( eObject ) ) );
                     }
                 }
             }
         } catch( CoreException exception ) {
             ErfObjectEditorPlugin.INSTANCE.log( exception );
         }
     }
 
     /**
      * Handle save operation (Save As)
      */
     protected void doSaveAs( URI uri, IEditorInput editorInput ) {
         (editingDomain.getResourceSet().getResources().get( 0 )).setURI( uri );
         setInputWithNotify( editorInput );
         setPartName( editorInput.getName() );
         IProgressMonitor progressMonitor = new NullProgressMonitor();
         doSave( progressMonitor );
     }
 
     /**
      * Initializes the editor part
      * 
      * This is called during startup.
      */
     @Override
     public void init( IEditorSite site, IEditorInput editorInput ) {
         setSite( site );
         setInputWithNotify( editorInput );
         setPartName( editorInput.getName() );
         site.setSelectionProvider( this );
         ResourcesPlugin.getWorkspace().addResourceChangeListener( resourceChangeListener,
                                                                   IResourceChangeEvent.POST_CHANGE );
     }
 
     /**
      * See {@link WorkbenchPart#setFocus()}
      */
     @Override
     public void setFocus() {
         specObjectViewerPane.getControl().setFocus();
     }
 
     /**
      * This implements {@link org.eclipse.jface.viewers.ISelectionProvider}.
      * 
      * Also see {@link org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(ISelectionChangedListener)}
      */
     public void addSelectionChangedListener( ISelectionChangedListener listener ) {
         selectionChangedListeners.add( listener );
     }
 
     /**
      * This implements {@link org.eclipse.jface.viewers.ISelectionProvider}.
      */
     public void removeSelectionChangedListener( ISelectionChangedListener listener ) {
         selectionChangedListeners.remove( listener );
     }
 
     /**
      * This implements {@link org.eclipse.jface.viewers.ISelectionProvider} to return this editor's overall selection.
      */
     public ISelection getSelection() {
         return editorSelection;
     }
 
     /**
      * This implements {@link org.eclipse.jface.viewers.ISelectionProvider} to set this editor's overall selection.
      * Calling this result will notify the listeners.
      */
     public void setSelection( ISelection selection ) {
         editorSelection = selection;
 
         for( ISelectionChangedListener listener : selectionChangedListeners ) {
             listener.selectionChanged( new SelectionChangedEvent( this, selection ) );
         }
         setStatusLineManager( selection );
     }
 
     /**
      * Set text displayed in status line according to the element selcted in the 
      * viewer
      */
     public void setStatusLineManager( ISelection selection ) {
         
         /* TODO: discuss if it make sense to show anything here. it would make sense to 
         * show the object ID as soon we are able to define an attribute definition as 
         * ID for an spec object. Then again, do we need to specify a certain attribute defintion
         * as ID ? We certainly need some kind of unique attribute... Check if ReqIf does 
         * specify a 'unique' attribute for Attribute definitions.
         */
         IStatusLineManager statusLineManager = currentViewer != null && currentViewer == contentOutlineViewer
             ? contentOutlineStatusLineManager
             : null;
 
         if( statusLineManager != null ) {
             if( selection instanceof IStructuredSelection ) {
                 Collection<?> collection = ((IStructuredSelection)selection).toList();
                 switch (collection.size()) {
                 case 0: {
                     statusLineManager.setMessage( getString( "_UI_NoObjectSelected" ) );
                     break;
                 }
                 case 1: {
                     String text = new AdapterFactoryItemDelegator( adapterFactory ).getText( collection.iterator()
                                                                                                        .next() );
                     statusLineManager.setMessage( getString( "_UI_SingleObjectSelected", text ) );
                     break;
                 }
                 default: {
                     statusLineManager.setMessage( getString( "_UI_MultiObjectSelected",
                                                              Integer.toString( collection.size() ) ) );
                     break;
                 }
                 }
             } else {
                 statusLineManager.setMessage( "" );
             }
         }
     }
 
     /**
      * This looks up a string in the plugin's plugin.properties file.
      */
     private static String getString( String key ) {
         return ErfObjectEditorPlugin.INSTANCE.getString( key );
     }
 
     /**
      * This looks up a string in plugin.properties, making a substitution.
      */
     private static String getString( String key, Object s1 ) {
         return ErfObjectEditorPlugin.INSTANCE.getString( key, new Object[]{s1} );
     }
 
     /**
      * @see era.foss.objecteditor.IAdapterFactoryProvider#getAdapterFactory()
      * @since Oct 28, 2010
      */
     public AdapterFactory getAdapterFactory() {
         return adapterFactory;
     }
 
     
     /**
      * Dispose this control and controls created by this one
      */
     @Override
     public void dispose() {
         updateProblemIndication = false;
         
         ResourcesPlugin.getWorkspace().removeResourceChangeListener( resourceChangeListener );
 
         adapterFactory.dispose();
 
         if( propertySheetPage != null ) {
             propertySheetPage.dispose();
         }
 
         if( contentOutlinePage != null ) {
             contentOutlinePage.dispose();
         }
 
         super.dispose();
     }
 
     /**
      * Returns whether the outline view should be presented to the user. (Currently: "yes, always")
      */
     protected boolean showOutlineView() {
         return true;
     }
 
     @Override
     public void createPartControl( Composite parent ) {
 
         // Creates the model from the editor input
         //
         createModel();
 
         // Only creates the other pages if there is something that can be edited
         //
         if( !getEditingDomain().getResourceSet().getResources().isEmpty() ) {
             // This is the page for the table viewer.
             //
             {
                 specObjectViewerPane = new SpecObjectViewerPane( getSite().getPage(), ErfObjectEditor.this, parent ){
                     @Override
                     public void requestActivation() {
                         super.requestActivation();
                         setCurrentViewer( specObjectViewerPane.getViewer() );
                     }
                 };
                 this.setCurrentViewer( specObjectViewerPane.getViewer() );
             }
         }
 
         getSite().getShell().getDisplay().asyncExec( new Runnable() {
             public void run() {
                 updateProblemIndication();
             }
         } );
     }
 }
