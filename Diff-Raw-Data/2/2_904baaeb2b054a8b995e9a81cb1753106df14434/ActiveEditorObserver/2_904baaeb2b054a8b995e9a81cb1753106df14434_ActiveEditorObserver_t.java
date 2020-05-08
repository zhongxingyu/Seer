 /**
  * Copyright (c) 2012 modelversioning.org
  * All rights reserved. This program and the accompanying materials are
  * made available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  *  http://www.eclipse.org/legal/epl-v10.html
  */
 package org.modelversioning.emfprofile.application.registry.ui.observer;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 
 import org.eclipse.core.commands.Command;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.ecore.ENamedElement;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.transaction.util.TransactionUtil;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.ILabelProvider;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchListener;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.commands.ICommandService;
 import org.eclipse.ui.handlers.RegistryToggleState;
 import org.eclipse.ui.services.ISourceProviderService;
 import org.modelversioning.emfprofile.Stereotype;
 import org.modelversioning.emfprofile.application.registry.ProfileApplicationDecorator;
 import org.modelversioning.emfprofile.application.registry.ProfileApplicationRegistry;
 import org.modelversioning.emfprofile.application.registry.ui.EMFProfileApplicationRegistryUIPlugin;
 import org.modelversioning.emfprofile.application.registry.ui.commands.handlers.StereotypeApplicationsOnSelectedElementHandler;
 import org.modelversioning.emfprofile.application.registry.ui.commands.sourceprovider.ToolbarCommandEnabledState;
 import org.modelversioning.emfprofile.application.registry.ui.dialogs.ApplyStereotypeOnEObjectDialog;
 import org.modelversioning.emfprofile.application.registry.ui.extensionpoint.decorator.EMFProfileApplicationDecorator;
 import org.modelversioning.emfprofile.application.registry.ui.extensionpoint.decorator.PluginExtensionOperationsListener;
 import org.modelversioning.emfprofile.application.registry.ui.extensionpoint.decorator.handler.EMFProfileApplicationDecoratorHandler;
 import org.modelversioning.emfprofile.application.registry.ui.providers.ProfileApplicationDecoratorReflectiveItemProviderAdapterFactory;
 import org.modelversioning.emfprofile.application.registry.ui.views.filters.StereotypesOfEObjectViewerFilter;
 import org.modelversioning.emfprofileapplication.ProfileApplication;
 import org.modelversioning.emfprofileapplication.StereotypeApplicability;
 import org.modelversioning.emfprofileapplication.StereotypeApplication;
 
 /**
  * It manages mapping of opened editors of interest to 
  * the generated id for an opened model in editor.
  * It is also a {@link PluginExtensionOperationsListener}.
  * @author <a href="mailto:becirb@gmail.com">Becir Basic</a>
  *
  */
 public class ActiveEditorObserver implements PluginExtensionOperationsListener {
 	
 	public static ActiveEditorObserver INSTANCE = new ActiveEditorObserver();
 	
 	private Map<IWorkbenchPart, String> editorPartToModelIdMap = new HashMap<>();
 	private Map<IWorkbenchPart, ViewerState> editorPartToViewerStateMap = new HashMap<>();
 	
 	
 	private IWorkbenchPage activePage;
 	private EMFProfileApplicationDecoratorHandler decoratorHandler;
 	private TreeViewer viewer;
 	private ToolbarCommandEnabledState toolbarCommandEnabeldStateService;
 	private StereotypesOfEObjectViewerFilter viewerFilter = new StereotypesOfEObjectViewerFilter(null);
 	private boolean viewerFilterActivated = false;
 
 	private DecoratableEditorPartListener decoratableEditorPartListener;
 	
 	// hide default constructor
 	private ActiveEditorObserver(){
 	}
 	
 	public IWorkbenchPart getLastActiveEditorPart(){
 		return decoratableEditorPartListener.getLastActiveEditPart();
 	}
 	
 	/**
 	 * To set the Tree Viewer from outside.
 	 * After calling this method a part listener will be added on active page
 	 * which registers activation of editors that can be decorated. </br>
 	 * <b>Note:</b> without setting a tree viewer the services of this
 	 * class implementation will work properly. 
 	 * @param viewer
 	 */
 	public void setViewer(TreeViewer viewer){
 		decoratorHandler = EMFProfileApplicationDecoratorHandler.getInstance();
 		decoratorHandler.setPluginExtensionOperationsListener(ActiveEditorObserver.INSTANCE);
 		
 		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
 		if(window == null)
 			throw new RuntimeException("could not locate workbench active window!");
 		
 		 // Get the source provider service
 	    ISourceProviderService sourceProviderService = (ISourceProviderService) window.getService(ISourceProviderService.class);
 	    // Now get the service for enabling/disenabling menu commands in viewer toolbar 
 	    toolbarCommandEnabeldStateService = (ToolbarCommandEnabledState) sourceProviderService
 	        .getSourceProvider(ToolbarCommandEnabledState.MY_STATE);
 		activePage = window.getActivePage();
 		if(activePage == null)
 			throw new RuntimeException("could not locate active page for active window ");
 		
 		this.viewer = viewer;
 				
 		// getting the value of the viewer command for activating/deactivating viewer tree filter 
 	    ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
 	    Command cmd = commandService.getCommand(StereotypeApplicationsOnSelectedElementHandler.COMMAND_ID);
 	    setActivateViewFilter((Boolean)cmd.getState(RegistryToggleState.STATE_ID).getValue());
 	    
 //		When the plug-in starts, we should check if there is an active editor and if it can be decorated
 		IEditorPart editorPart = activePage.getActiveEditor();
 		IWorkbenchPart lastActiveEditorPart = null;
 		if(editorPart != null){
 			if(decoratorHandler.hasDecoratorForEditorPart(editorPart)){
 				// Create an id for workbench part and put it into map
 				editorPartToModelIdMap.put(editorPart, UUID.randomUUID().toString());
 				lastActiveEditorPart = editorPart;
 				toolbarCommandEnabeldStateService.setEnabled(true);
 			}
 		}
 		// listener that gets notified for workbench changes and registers editor parts of interest
 		decoratableEditorPartListener = new DecoratableEditorPartListener(decoratorHandler, editorPartToModelIdMap, lastActiveEditorPart, viewer, toolbarCommandEnabeldStateService, editorPartToViewerStateMap);
 		activePage.addPartListener(decoratableEditorPartListener);
 		
 		// when workbench is about to close, we have to perform clean-up for all 
 		// editors of interest and their profile applications
 		PlatformUI.getWorkbench().addWorkbenchListener(new IWorkbenchListener() {
 			
 			@Override
 			public boolean preShutdown(IWorkbench workbench, boolean forced) {
 				cleanUp();
 				decoratableEditorPartListener.cleanUpForAllEditorParts();
 				return true;
 			}
 			
 			@Override
 			public void postShutdown(IWorkbench workbench) {
 				// nothing to do here
 			}
 		});
 	}
 	
 	/**
 	 * Complete refresh of the viewer tree if needed.
 	 */
 	public void refreshViewer() {
 		if(viewer == null || viewer.getTree().isDisposed()) // viewer was disposed
 			return;
 		viewer.getTree().getDisplay().asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				if(viewer.getInput().equals(Collections.emptyList())){
 					viewer.setInput(ProfileApplicationRegistry.INSTANCE.getProfileApplications(editorPartToModelIdMap.get(decoratableEditorPartListener.getLastActiveEditPart())));
 				}else{
 					viewer.refresh();
 					viewer.expandToLevel(2);
 				}
 				
 			}
 		});
 	}
 	
 	/**
 	 * Collection can be provided as parameter.
 	 * Refresh will be executed asynchronously for whole group of collection items in one runnable.
 	 * @param object an viewer tree element or a collection of them.
 	 */
 	public void refreshViewer(final Object object){
 		if(viewer == null || viewer.getTree().isDisposed())
 			return;
 		viewer.getTree().getDisplay().asyncExec(new Runnable() {
 			
 			@Override
 			public void run() {
 				if(object instanceof Collection<?>){
 					Iterator<?> iterator = ((Collection<?>) object).iterator();
 					while(iterator.hasNext())
 						viewer.refresh(iterator.next());
 				}else{
 					viewer.refresh(object);
 				}
 				viewer.expandToLevel(2);
 			}
 		});
 		
 	}
 	
 	/**
 	 * Updates the element of the viewer.
 	 * @param element of the tree in question.
 	 */
 	public void updateViewer(final Object element){
 		if(viewer == null || viewer.getTree().isDisposed())
 			return;
 		viewer.getTree().getDisplay().asyncExec(new Runnable() {
 			
 			@Override
 			public void run() {
 				viewer.update(element, null);
 			}
 		});
 	}
 	
 	public void revealElement(final Object element){
 		if(viewer == null || viewer.getTree().isDisposed())
 			return;
 		viewer.getTree().getDisplay().asyncExec(new Runnable() {
 			
 			@Override
 			public void run() {
 				viewer.reveal(element);
 			}
 		});
 	}
 	public String getModelIdForWorkbenchPart(IWorkbenchPart part) {
 		return editorPartToModelIdMap.get(part);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void applyStereotype(final EObject eObject) {
 		Assert.isNotNull(eObject);
 		// we are looking in all loaded profiles if there are any stereotypes applicable on eObject 
 		final Map<ProfileApplicationDecorator, Collection<StereotypeApplicability>> profileToStereotypeApplicabilityForEObjectMap = new HashMap<>();
 		for (ProfileApplicationDecorator profileApplication : ProfileApplicationRegistry.INSTANCE.getProfileApplications(editorPartToModelIdMap.get(decoratableEditorPartListener.getLastActiveEditPart()))) {
 			profileToStereotypeApplicabilityForEObjectMap.put(profileApplication, (Collection<StereotypeApplicability>) profileApplication.getApplicableStereotypes(eObject));
 		}
 		boolean mayApplyStereotype = false;
 		for (Collection<?> stereotypesApplicabilities : profileToStereotypeApplicabilityForEObjectMap.values()) {
 			if( ! stereotypesApplicabilities.isEmpty()){
 				mayApplyStereotype = true;
 				break;
 			}
 		}
 		if (mayApplyStereotype) {
 			ApplyStereotypeOnEObjectDialog applySteretypeDialog = new ApplyStereotypeOnEObjectDialog(profileToStereotypeApplicabilityForEObjectMap);
 			applySteretypeDialog.openApplyStereotypeDialog(eObject);
 		} else {
			MessageDialog.openInformation(viewer.getControl().getShell(), "Info", "Can not apply any stereotype to EObject: " + eObject.toString() );
 		}
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void eObjectSelected(EObject eObject) {	
 		if(eObject == null){
 			if(viewerFilter.getSelectedEObject() != null){
 				viewerFilter.setSelectedEObject(eObject);
 				if(viewerFilterActivated)
 					refreshViewer();
 			}
 		}else if( ! eObject.equals(viewerFilter.getSelectedEObject())){
 			viewerFilter.setSelectedEObject(eObject);
 			if(viewerFilterActivated)
 				refreshViewer();
 		}
 	}
 	
 	/**
 	 * Called by {@link StereotypeApplicationsOnSelectedElementHandler}
 	 * @param activateFilter <code>true</code> or <code>false</code>
 	 */
 	public void setActivateViewFilter(boolean activateFilter) {
 		this.viewerFilterActivated = activateFilter;
 		if(activateFilter)
 			viewer.addFilter(viewerFilter);
 		else
 			viewer.removeFilter(viewerFilter);
 		viewer.expandToLevel(2);
 	}
 	
 	private void showError(String message, Throwable throwable) {
 		ErrorDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Error Occured",
 				message, new Status(IStatus.ERROR,
 						EMFProfileApplicationRegistryUIPlugin.PLUGIN_ID, throwable.getMessage(),
 						throwable));
 	}
 
 
 	/**
 	 * It calls the {@link #refreshDecoration(EObject)} for each {@link EObject}. </br>
 	 * <b>Note:</b> This method can be used to refresh decorations when loading or unloading
 	 * profile applications.
 	 * @param eObjects collection of {@link EObject}s for which decorations must be refreshed.
 	 */
 	public void refreshDecorations(Collection<EObject> eObjects) {
 		for (EObject eObject : eObjects) {
 			refreshDecoration(eObject);
 		}
 	}
 
 
 	
 	/**
 	 * The method collects all stereotypes applied to <code>eObject</code>
 	 * from profile applications that can be found for this model in {@link ProfileApplicationRegistry}
 	 * and then informs active editor decorator to decorate the eObject. 
 	 * @param eObject
 	 * 			that has stereotype applications
 	 */
 	public void refreshDecoration(final EObject eObject) {
 		
 		final EMFProfileApplicationDecorator decorator;
 		if(decoratableEditorPartListener.getCleaningUpForEditorPart() != null){
 			// if editor is disposed then there is no need to refresh decorations
 			if(decoratableEditorPartListener.isCleaningUpForClosedEditorPart()){
 				return;
 			}
 			decorator = decoratorHandler.getDecoratorForEditorPart(decoratableEditorPartListener.getCleaningUpForEditorPart());
 		}else {
 			decorator = decoratorHandler.getDecoratorForEditorPart(decoratableEditorPartListener.getLastActiveEditPart());			
 		}
 		final List<Image> images = new ArrayList<>();
 		final List<String> toolTipTexts = new ArrayList<>();
 		for (ProfileApplicationDecorator profileApplication : ProfileApplicationRegistry.INSTANCE.getProfileApplications(getModelIdForWorkbenchPart(decoratableEditorPartListener.getLastActiveEditPart()))) {
 			Collection<StereotypeApplication> stereotypeApplications = profileApplication.getStereotypeApplications(eObject);
 			for (StereotypeApplication stereotypeApplication : stereotypeApplications) {
 				images.add(((ILabelProvider)viewer.getLabelProvider()).getImage(stereotypeApplication));
 				toolTipTexts.add(getStereotypeLabel(stereotypeApplication));
 			}
 		}
 		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
 			public void run() {
 				try {
 					TransactionUtil.getEditingDomain(eObject.eResource())
 							.runExclusive(new Runnable() {
 								public void run() {
 									decorator.decorate(eObject, images, toolTipTexts);
 								}
 							});
 				} catch (Exception e) {
 					e.printStackTrace();
 					showError("Calling decorate method on decorator for editor id: " + decoratableEditorPartListener.getLastActiveEditPart().getSite().getId() 
 							+ " throw an exception:", e);
 				}
 			}
 		});
 	}
 
 	/**
 	 * Returns the tool tip text for the supplied
 	 * <code>stereotypeApplication</code>.
 	 * 
 	 * @param stereotypeApplication
 	 *            to get tool tip text for.
 	 * @return the tool tip text.
 	 */
 	private String getStereotypeLabel(StereotypeApplication stereotypeApplication) {
 		if (stereotypeApplication.eClass() instanceof Stereotype) {
 			return "<<" + ((Stereotype) stereotypeApplication.eClass()).getName() + ">>"; //$NON-NLS-1$ $NON-NLS-2$
 		}
 		return "Stereotype application"; //$NON-NLS-1$
 	}
 
 	/**
 	 * The cleanup is executed if the profile application view in workbench is closing,
 	 * but not the Workbench.
 	 * If the workbench is closing, then the clean-up will be executed in {@link IWorkbenchListener#preShutdown(IWorkbench, boolean)}
 	 * which calls clean-up for all profile applications in {@link DecoratableEditorPartListener#cleanUpForAllEditorParts()}.
 	 */
 	public void cleanUp() {
 		decoratorHandler.unsetPluginExtensionOperationsListener();
 		activePage.removePartListener(decoratableEditorPartListener);
 		if(PlatformUI.getWorkbench().isClosing() == false){
 			decoratableEditorPartListener.cleanUpForAllEditorParts();
 		}
 	}
 
 	/**
 	 * This method will be called from {@link ProfileApplicationDecoratorReflectiveItemProviderAdapterFactory}
 	 * when notification is fired that an attribute is changed in properties view.
 	 * Notifications will be fired for every change, but we are here only interested in scenario
 	 * when only one tree element is selected and we can find profile application decorator from it,
 	 * otherwise this method will do nothing.
 	 */
 	public void setProfileApplicationChanged(){
 		viewer.getTree().getDisplay().asyncExec(new Runnable() {
 			@Override
 			public void run() {
 				// we have to find current selection in the view tree, 
 				// then traverse to profile application decorator
 				// and set it to dirty and update the view tree
 				ISelection selection = viewer.getSelection();
 				if(selection != null && selection instanceof IStructuredSelection){
 					EObject eObject = (EObject) ((IStructuredSelection)selection).getFirstElement();
 					if(eObject == null) // probably was deleted, so nothing to do
 						return;
 					ProfileApplicationDecorator profileApplication = findProfileApplicationDecorator(eObject);
 					if(profileApplication == null) // could not find it, do nothing
 						return;
 					profileApplication.setDirty(true);
 					updateViewer(profileApplication);
 				}
 			}
 		});
 		
 	}
 	
 	/**
 	 * If we need {@link ProfileApplicationDecorator} because of its extended functionalities, and 
 	 * calling {@link EObject#eContainer()} will eventually return {@link ProfileApplication}
 	 * but we cannot cast it to {@link ProfileApplicationDecorator}. Thus, the easiest way
 	 * to get it is to ask the {@link ProfileApplicationRegistry}, which this method does for you.
 	 * @param eObject
 	 * @return
 	 */
 	public ProfileApplicationDecorator findProfileApplicationDecorator(EObject eObject){
 		return ProfileApplicationRegistry.INSTANCE.getProfileApplicationDecoratorOfContainedEObject(editorPartToModelIdMap.get(decoratableEditorPartListener.getLastActiveEditPart()), eObject);
 	}
 }
