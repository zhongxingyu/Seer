 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  ******************************************************************************/
 package org.eclipse.emf.emfstore.internal.client.ui.testers;
 
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
 import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
 import org.eclipse.emf.emfstore.client.ESLocalProject;
 import org.eclipse.emf.emfstore.client.ESProject;
 import org.eclipse.emf.emfstore.common.extensionpoint.ESExtensionElement;
 import org.eclipse.emf.emfstore.common.extensionpoint.ESExtensionPoint;
 import org.eclipse.emf.emfstore.internal.client.model.ESWorkspaceProviderImpl;
 import org.eclipse.emf.emfstore.internal.client.model.ProjectSpace;
 import org.eclipse.emf.emfstore.internal.client.observers.SaveStateChangedObserver;
 import org.eclipse.emf.emfstore.internal.common.model.util.ModelUtil;
 import org.eclipse.jface.viewers.ArrayContentProvider;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.AbstractSourceProvider;
 import org.eclipse.ui.ISources;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchListener;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.dialogs.ListSelectionDialog;
 
 /**
  * Source provider for properties of ProjectSpace.
  * 
  * @author mkoegel
  * 
  */
 public class ProjectSpacePropertySourceProvider extends AbstractSourceProvider {
 
 	/**
 	 * Name of the property defining the save state of the currently selected project space.
 	 */
 	public static final String CURRENT_SAVE_STATE_PROPERTY = "org.eclipse.emf.emfstore.client.ui.currentProjectSpaceSaveState";
 
 	private SaveStateChangedObserver saveStateChangedObserver;
 
 	private Map<String, Boolean> currentSaveStates;
 
 	private boolean saveDisabled;
 
 	/**
 	 * Default constructor.
 	 */
 	public ProjectSpacePropertySourceProvider() {
 
 		currentSaveStates = new LinkedHashMap<String, Boolean>();
 		// check if workspace can init, exit otherwise
 		try {
 			ESWorkspaceProviderImpl.init();
 			// BEGIN SUPRESS CATCH EXCEPTION
 		} catch (RuntimeException exception) {
 			// END SUPRESS CATCH EXCEPTION
 			ModelUtil.logException(
 				"ProjectSpacePropertySourceProvider init failed because workspace init failed with exception.",
 				exception);
 			return;
 		}
 		saveStateChangedObserver = new SaveStateChangedObserver() {
 			public void saveStateChanged(ProjectSpace projectSpace, boolean hasUnsavedChangesNow) {
 				Boolean newValue = new Boolean(hasUnsavedChangesNow);
 				currentSaveStates.put(projectSpace.getIdentifier(), newValue);
 				fireSourceChanged(ISources.WORKBENCH, CURRENT_SAVE_STATE_PROPERTY, newValue);
 			}
 
 		};
 		ESWorkspaceProviderImpl.getObserverBus().register(saveStateChangedObserver);
 		PlatformUI.getWorkbench().addWorkbenchListener(new IWorkbenchListener() {
 
 			public boolean preShutdown(IWorkbench workbench, boolean forced) {
 				if (saveDisabled) {
 					return true;
 				}
 				return handlePreShutdownOfWorkbench();
 			}
 
 			public void postShutdown(IWorkbench workbench) {
 				// do nothing
 			}
 		});
 		saveDisabled = initExtensionPoint();
 	}
 
 	// TODO: quick fix, duplicate code in IsAutoSaveEnabledTester
 	// TODO: provide extension point registry? discuss
 	private static boolean initExtensionPoint() {
 		ESExtensionPoint extensionPoint = new ESExtensionPoint(
 			"org.eclipse.emf.emfstore.client.ui.disableSaveControls");
 		ESExtensionElement element = extensionPoint.getFirst();
 
 		if (element == null) {
 			// default
 			return false;
 		}
 
 		return element.getBoolean("enabled", false);
 	}
 
 	private boolean handlePreShutdownOfWorkbench() {
 		AdapterFactoryLabelProvider labelProvider = new AdapterFactoryLabelProvider(new ComposedAdapterFactory(
			ComposedAdapterFactory.Descriptor.Registry.INSTANCE)) {
			@Override
			public String getColumnText(Object object, int columnIndex) {
				if (object instanceof ESLocalProject) {
					return ((ESLocalProject) object).getProjectName();
				}
				return super.getColumnText(object, columnIndex);
			}
		};
 		ArrayContentProvider contentProvider = new ArrayContentProvider();
 		ArrayList<ESProject> inputArray = new ArrayList<ESProject>();
 		for (ESLocalProject project : ESWorkspaceProviderImpl.getInstance().getWorkspace().getLocalProjects()) {
 			if (project.hasUnsavedChanges()) {
 				inputArray.add(project);
 			}
 		}
 		if (inputArray.size() < 1) {
 			return true;
 		}
 		ListSelectionDialog listselectionDialog = new ListSelectionDialog(Display.getCurrent().getActiveShell(),
 			inputArray, contentProvider, labelProvider,
 			"Which of the following projects with pending changes do you want to save?");
 		listselectionDialog.setTitle("Pending changes on exit");
 		listselectionDialog.setHelpAvailable(false);
 		int result = listselectionDialog.open();
 		if (result == ListSelectionDialog.CANCEL) {
 			return false;
 		}
 		Object[] selectedObjects = listselectionDialog.getResult();
 		for (Object projectSpaceObject : selectedObjects) {
 			if (projectSpaceObject instanceof ProjectSpace) {
 				ProjectSpace projectSpace = (ProjectSpace) projectSpaceObject;
 				projectSpace.save();
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.ui.ISourceProvider#dispose()
 	 */
 	public void dispose() {
 		if (saveStateChangedObserver != null) {
 			ESWorkspaceProviderImpl.getObserverBus().unregister(saveStateChangedObserver);
 		}
 
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.ui.ISourceProvider#getCurrentState()
 	 */
 	public Map<String, Object> getCurrentState() {
 		Map<String, Object> map = new LinkedHashMap<String, Object>();
 		map.put(CURRENT_SAVE_STATE_PROPERTY, currentSaveStates);
 		return map;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.ui.ISourceProvider#getProvidedSourceNames()
 	 */
 	public String[] getProvidedSourceNames() {
 		return new String[] { CURRENT_SAVE_STATE_PROPERTY };
 	}
 
 }
