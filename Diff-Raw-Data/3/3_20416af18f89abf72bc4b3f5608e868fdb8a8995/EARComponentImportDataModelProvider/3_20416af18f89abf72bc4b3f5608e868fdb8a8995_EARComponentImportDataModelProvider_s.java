 /*******************************************************************************
  * Copyright (c) 2003, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.application.internal.operations;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jem.util.emf.workbench.ProjectUtilities;
 import org.eclipse.jst.j2ee.applicationclient.internal.creation.AppClientComponentImportDataModelProvider;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.CommonArchiveResourceHandler;
 import org.eclipse.jst.j2ee.datamodel.properties.IAddWebComponentToEnterpriseApplicationDataModelProperties;
 import org.eclipse.jst.j2ee.datamodel.properties.IEARComponentImportDataModelProperties;
 import org.eclipse.jst.j2ee.datamodel.properties.IJ2EEComponentImportDataModelProperties;
 import org.eclipse.jst.j2ee.datamodel.properties.IJ2EEModuleImportDataModelProperties;
 import org.eclipse.jst.j2ee.datamodel.properties.IJavaUtilityJarImportDataModelProperties;
 import org.eclipse.jst.j2ee.internal.J2EEVersionConstants;
 import org.eclipse.jst.j2ee.internal.archive.ArchiveWrapper;
 import org.eclipse.jst.j2ee.internal.archive.operations.EARComponentImportOperation;
 import org.eclipse.jst.j2ee.internal.common.J2EEVersionUtil;
 import org.eclipse.jst.j2ee.internal.common.XMLResource;
 import org.eclipse.jst.j2ee.internal.earcreation.EARCreationResourceHandler;
 import org.eclipse.jst.j2ee.internal.moduleextension.EarModuleManager;
 import org.eclipse.jst.j2ee.internal.moduleextension.EjbModuleExtension;
 import org.eclipse.jst.j2ee.internal.moduleextension.JcaModuleExtension;
 import org.eclipse.jst.j2ee.internal.moduleextension.WebModuleExtension;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEPlugin;
 import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
 import org.eclipse.jst.j2ee.project.facet.EARFacetProjectCreationDataModelProvider;
 import org.eclipse.jst.j2ee.project.facet.IJ2EEFacetConstants;
 import org.eclipse.jst.j2ee.project.facet.IJ2EEFacetInstallDataModelProperties;
 import org.eclipse.jst.j2ee.project.facet.IJ2EEFacetProjectCreationDataModelProperties;
 import org.eclipse.jst.jee.archive.ArchiveOpenFailureException;
 import org.eclipse.jst.jee.util.internal.JavaEEQuickPeek;
 import org.eclipse.wst.common.componentcore.datamodel.properties.IFacetDataModelProperties;
 import org.eclipse.wst.common.componentcore.datamodel.properties.IFacetProjectCreationDataModelProperties;
 import org.eclipse.wst.common.componentcore.datamodel.properties.IFacetProjectCreationDataModelProperties.FacetDataModelMap;
 import org.eclipse.wst.common.frameworks.datamodel.DataModelEvent;
 import org.eclipse.wst.common.frameworks.datamodel.DataModelFactory;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModelListener;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModelOperation;
 import org.eclipse.wst.common.frameworks.internal.WTPPlugin;
 import org.eclipse.wst.common.frameworks.internal.operations.IProjectCreationPropertiesNew;
 import org.eclipse.wst.common.frameworks.internal.operations.ProjectCreationDataModelProviderNew;
 import org.eclipse.wst.common.frameworks.internal.plugin.WTPCommonPlugin;
 import org.eclipse.wst.common.project.facet.core.IFacetedProject;
 import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
 import org.eclipse.wst.common.project.facet.core.runtime.IRuntime;
 
 /**
  * This dataModel is used for to import Enterprise Applications(from EAR files) into the workspace.
  * 
  * This class (and all its fields and methods) is likely to change during the WTP 1.0 milestones as the new project
  * structures are adopted. Use at your own risk.
  * 
  * @plannedfor WTP 1.0
  */
 public final class EARComponentImportDataModelProvider extends J2EEArtifactImportDataModelProvider implements IAnnotationsDataModel, IEARComponentImportDataModelProperties {
 
 	/**
 	 * This is only to force validation for the nested projects; do not set.
 	 */
 	public static final String NESTED_PROJECTS_VALIDATION = "EARImportDataModel.NESTED_PROJECTS_VALIDATION"; //$NON-NLS-1$
 
 	/**
 	 * This is only to force validation for the EAR name against the nested projects; do not set.
 	 */
 	public static final String EAR_NAME_VALIDATION = "EARImportDataModel.EAR_NAME_VALIDATION";//$NON-NLS-1$
 
 	private IDataModelListener nestedListener = new IDataModelListener() {
 		public void propertyChanged(DataModelEvent event) {
 			if (event.getPropertyName().equals(PROJECT_NAME)) {
 				model.notifyPropertyChange(NESTED_PROJECTS_VALIDATION, IDataModel.DEFAULT_CHG);
 			}
 		}
 	};
 
 	private Hashtable<IDataModel, IDataModel> ejbJarModelsToClientJarModels = new Hashtable<IDataModel, IDataModel>();
 
 	private Hashtable<IDataModel, IDataModel> clientJarModelsToEjbJarModels = new Hashtable<IDataModel, IDataModel>();
 
 	private ArchiveWrapper cachedLoadError = null;
 
 	@Override
 	public Set getPropertyNames() {
 		Set propertyNames = super.getPropertyNames();
 		propertyNames.add(NESTED_MODULE_ROOT);
 		propertyNames.add(UTILITY_LIST);
 		propertyNames.add(MODULE_MODELS_LIST);
 		propertyNames.add(EJB_CLIENT_LIST);
 		propertyNames.add(UTILITY_MODELS_LIST);
 		propertyNames.add(NESTED_PROJECTS_VALIDATION);
 		propertyNames.add(EAR_NAME_VALIDATION);
 		propertyNames.add(SELECTED_MODELS_LIST);
 		propertyNames.add(USE_ANNOTATIONS);
 		propertyNames.add(ALL_PROJECT_MODELS_LIST);
 		propertyNames.add(UNHANDLED_PROJECT_MODELS_LIST);
 		propertyNames.add(HANDLED_PROJECT_MODELS_LIST);
 		return propertyNames;
 	}
 
 	@Override
 	public Object getDefaultProperty(String propertyName) {
 		if (NESTED_MODULE_ROOT.equals(propertyName)) {
 			return getLocation().toOSString();
 		} else if (MODULE_MODELS_LIST.equals(propertyName) || UTILITY_LIST.equals(propertyName) || UTILITY_MODELS_LIST.equals(propertyName) || SELECTED_MODELS_LIST.equals(propertyName) || EJB_CLIENT_LIST.equals(propertyName)) {
 			return Collections.EMPTY_LIST;
 		} else if (USE_ANNOTATIONS.equals(propertyName)) {
 			return Boolean.FALSE;
 		} else if (ALL_PROJECT_MODELS_LIST.equals(propertyName)) {
 			return getProjectModels();
 		} else if (UNHANDLED_PROJECT_MODELS_LIST.equals(propertyName)) {
 			return getUnhandledProjectModels();
 		} else if (HANDLED_PROJECT_MODELS_LIST.equals(propertyName)) {
 			return getHandledSelectedModels();
 		}
 		return super.getDefaultProperty(propertyName);
 	}
 
 	@Override
 	public void propertyChanged(DataModelEvent event) {
 		super.propertyChanged(event);
 		if (event.getPropertyName().equals(PROJECT_NAME)) {
 			changeModuleCreationLocationForNameChange(getProjectModels());
 		} else if (event.getPropertyName().equals(IFacetProjectCreationDataModelProperties.FACET_RUNTIME) && event.getDataModel() == model.getNestedModel(NESTED_MODEL_J2EE_COMPONENT_CREATION)) {
 			Object propertyValue = event.getProperty();
 			IDataModel nestedModel = null;
 			List projectModels = (List) getProperty(ALL_PROJECT_MODELS_LIST);
 			for (int i = 0; i < projectModels.size(); i++) {
 				nestedModel = (IDataModel) projectModels.get(i);
 				nestedModel.setProperty(IFacetProjectCreationDataModelProperties.FACET_RUNTIME, propertyValue);
 			}
 			fixupJavaFacets();
 		}
 	}
 
 	@Override
 	public boolean propertySet(String propertyName, Object propertyValue) {
 		if (ALL_PROJECT_MODELS_LIST.equals(propertyName) || UNHANDLED_PROJECT_MODELS_LIST.equals(propertyName) || HANDLED_PROJECT_MODELS_LIST.equals(propertyName) || EAR_NAME_VALIDATION.equals(propertyName)) {
 			throw new RuntimeException(propertyName + " is an unsettable property"); //$NON-NLS-1$
 		}
 		boolean doSet = super.propertySet(propertyName, propertyValue);
 		if (NESTED_MODULE_ROOT.equals(propertyName)) {
 			updateModuleRoot();
 		} else if (FILE_NAME.equals(propertyName)) {
 			List nestedModels = getModuleModels();
 			setProperty(MODULE_MODELS_LIST, nestedModels);
 			updateModuleRoot();
 			setProperty(UTILITY_LIST, null);
 
 			if (getArchiveWrapper() != null) {
 				refreshInterpretedSpecVersion();
 			}
 
 			model.notifyPropertyChange(PROJECT_NAME, IDataModel.VALID_VALUES_CHG);
 			if (getJ2EEVersion() < J2EEVersionConstants.VERSION_1_3)
 				setBooleanProperty(USE_ANNOTATIONS, false);
 			model.notifyPropertyChange(USE_ANNOTATIONS, IDataModel.ENABLE_CHG);
 			fixupJavaFacets();
 		} else if (UTILITY_LIST.equals(propertyName)) {
 			updateUtilityModels((List) propertyValue);
 		} else if (USE_ANNOTATIONS.equals(propertyName)) {
 			List projectModels = (List) getProperty(MODULE_MODELS_LIST);
 			IDataModel nestedModel = null;
 			for (int i = 0; i < projectModels.size(); i++) {
 				nestedModel = (IDataModel) projectModels.get(i);
 				if (nestedModel.isProperty(USE_ANNOTATIONS)) {
 					nestedModel.setProperty(USE_ANNOTATIONS, propertyValue);
 				}
 			}
 		} else if (MODULE_MODELS_LIST.equals(propertyName)) {
 			List newList = new ArrayList();
 			newList.addAll(getProjectModels());
 			setProperty(SELECTED_MODELS_LIST, newList);
 		} else if (PROJECT_NAME.equals(propertyName)) {
 			List nestedModels = (List) getProperty(MODULE_MODELS_LIST);
 			IDataModel nestedModel = null;
 			for (int i = 0; i < nestedModels.size(); i++) {
 				nestedModel = (IDataModel) nestedModels.get(i);
 				nestedModel.setProperty(IJ2EEFacetProjectCreationDataModelProperties.EAR_PROJECT_NAME, propertyValue);
 			}
 			nestedModels = (List) getProperty(UTILITY_MODELS_LIST);
 			for (int i = 0; i < nestedModels.size(); i++) {
 				nestedModel = (IDataModel) nestedModels.get(i);
 				nestedModel.setProperty(IJavaUtilityJarImportDataModelProperties.EAR_PROJECT_NAME, propertyValue);
 			}
 
 			if (ProjectCreationDataModelProviderNew.validateProjectName(getStringProperty(PROJECT_NAME)).isOK()) {
 				IProject project = ProjectUtilities.getProject(getStringProperty(PROJECT_NAME));
 				if (null != project && project.exists()) {
 
 					IFacetedProject facetedProject = null;
 					try {
 						facetedProject = ProjectFacetsManager.create(project);
 					} catch (CoreException e) {
 						J2EEPlugin.logError(e);
 					}
 
 					if (facetedProject != null ) {
 						IRuntime runtime = facetedProject.getRuntime();
 						if (null != runtime) {
 							setProperty(IFacetProjectCreationDataModelProperties.FACET_RUNTIME, runtime);
 						}
 					}
 				}
 			}
 		}
 		return doSet;
 	}
 	
 	@Override
 	protected void refreshInterpretedSpecVersion() {
 		IDataModel earProjectModel = model.getNestedModel(NESTED_MODEL_J2EE_COMPONENT_CREATION);
 		FacetDataModelMap map = (FacetDataModelMap) earProjectModel.getProperty(IFacetProjectCreationDataModelProperties.FACET_DM_MAP);
 		IDataModel earFacetDataModel = map.getFacetDataModel(J2EEProjectUtilities.ENTERPRISE_APPLICATION);
 		int minimumVersion = getInterpretedSpecVersion(getArchiveWrapper()).getJavaEEVersion();
 		if(minimumVersion != JavaEEQuickPeek.VERSION_6_0){
 			List nestedModels = getModuleModels();
 			if(nestedModels != null){
 				//increase the JavaEE facet version to accommodate the highest module version
 				for(int i=0;i<nestedModels.size(); i++){
 					IDataModel nestedModel = (IDataModel)nestedModels.get(i);
 					ArchiveWrapper nestedWrapper = (ArchiveWrapper)nestedModel.getProperty(ARCHIVE_WRAPPER);
 					int nestedEEVersion = getInterpretedSpecVersion(nestedWrapper).getJavaEEVersion();
 					if(nestedEEVersion > minimumVersion){
 						minimumVersion = nestedEEVersion;
 					}
 				}
 			}
 		}
 		String versionText = J2EEVersionUtil.getJ2EETextVersion( minimumVersion );
 		earFacetDataModel.setStringProperty(IFacetDataModelProperties.FACET_VERSION_STR, versionText);
 	}
 
 	protected boolean forceResetOnPreserveMetaData() {
 		return false;
 	}
 
 	protected void fixupJavaFacets() {
 		List subProjects = getSelectedModels();
 		IDataModel subDataModel = null;
 		for (int i = 0; i < subProjects.size(); i++) {
 			subDataModel = (IDataModel) subProjects.get(i);
 			subDataModel.validateProperty(FACET_RUNTIME);
 		}
 	}
 
 	@Override
 	public IStatus validate(String propertyName) {
 		if (propertyName.equals(NESTED_PROJECTS_VALIDATION) || propertyName.equals(EAR_NAME_VALIDATION)) {
 			boolean checkAgainstEARNameOnly = propertyName.equals(EAR_NAME_VALIDATION);
 
 			String earProjectName = getStringProperty(IFacetProjectCreationDataModelProperties.FACET_PROJECT_NAME);
 			List subProjects = getSelectedModels();
 			IDataModel subDataModel = null;
 			String tempProjectName = null;
 			ArchiveWrapper tempArchive = null;
 			IStatus tempStatus = null;
 			Hashtable<String, ArchiveWrapper> projects = new Hashtable<String, ArchiveWrapper>(4);
 			for (int i = 0; i < subProjects.size(); i++) {
 				subDataModel = (IDataModel) subProjects.get(i);
 				tempProjectName = subDataModel.getStringProperty(IFacetProjectCreationDataModelProperties.FACET_PROJECT_NAME);
 				// TODO: add manual validation
 				// IStatus status =
 				// ProjectCreationDataModel.validateProjectName(tempProjectName);
 				// if (!status.isOK()) {
 				// return status;
 				// }
 				tempArchive = (ArchiveWrapper) subDataModel.getProperty(ARCHIVE_WRAPPER);
 
 				if (checkAgainstEARNameOnly) {
 					if (tempProjectName.equals(earProjectName)) {
 						return WTPCommonPlugin.createWarningStatus(EARCreationResourceHandler.bind(EARCreationResourceHandler.EARImportDataModel_UI_1, new Object[] { tempProjectName,
 								tempArchive.getPath() }));
 					} else if (!WTPPlugin.isPlatformCaseSensitive()) {
 						if (tempProjectName.toLowerCase().equals(earProjectName.toLowerCase())) {
 							return WTPCommonPlugin.createWarningStatus(EARCreationResourceHandler.bind(EARCreationResourceHandler.EARImportDataModel_UI_1a, new Object[] { earProjectName,
 									tempProjectName, tempArchive.getPath() }));
 						}
 					}
 				} else {
 					// if (!overwrite && subDataModel.getProject().exists()) {
 					// return
 					// WTPCommonPlugin.createErrorStatus(EARCreationResourceHandler.getString("EARImportDataModel_UI_0",
 					// new Object[]{tempProjectName, tempArchive.getURI()})); //$NON-NLS-1$
 					// }
 					tempStatus = subDataModel.validateProperty(IFacetDataModelProperties.FACET_PROJECT_NAME);
 					if (!tempStatus.isOK()) {
						return WTPCommonPlugin.createErrorStatus(EARCreationResourceHandler.bind(EARCreationResourceHandler.EARImportDataModel_UI_0, new Object[] { tempProjectName,
 								tempArchive.getPath() }));
 					}
 					tempStatus = subDataModel.validate();
 					if (!tempStatus.isOK()) {
 						return tempStatus;
 					}
 					if (tempProjectName.equals(earProjectName)) {
 						return WTPCommonPlugin.createErrorStatus(EARCreationResourceHandler.bind(EARCreationResourceHandler.EARImportDataModel_UI_1, new Object[] { tempProjectName,
 								tempArchive.getPath() }));
 					} else if (!WTPPlugin.isPlatformCaseSensitive()) {
 						if (tempProjectName.toLowerCase().equals(earProjectName.toLowerCase())) {
 							return WTPCommonPlugin.createErrorStatus(EARCreationResourceHandler.bind(EARCreationResourceHandler.EARImportDataModel_UI_1a, new Object[] { earProjectName,
 									tempProjectName, tempArchive.getPath() }));
 						}
 					}
 					if (projects.containsKey(tempProjectName)) {
 						return WTPCommonPlugin.createErrorStatus(EARCreationResourceHandler.bind(EARCreationResourceHandler.EARImportDataModel_UI_2, new Object[] { tempProjectName,
 								tempArchive.getPath(), (projects.get(tempProjectName)).getPath() }));
 					} else if (!WTPPlugin.isPlatformCaseSensitive()) {
 						String lowerCaseProjectName = tempProjectName.toLowerCase();
 						String currentKey = null;
 						Enumeration keys = projects.keys();
 						while (keys.hasMoreElements()) {
 							currentKey = (String) keys.nextElement();
 							if (currentKey.toLowerCase().equals(lowerCaseProjectName)) {
 								return WTPCommonPlugin.createErrorStatus(EARCreationResourceHandler.bind(EARCreationResourceHandler.EARImportDataModel_UI_2a, new Object[] { tempProjectName,
 										currentKey, tempArchive.getPath(), projects.get(currentKey).getPath() }));
 							}
 						}
 					}
 					projects.put(tempProjectName, tempArchive);
 				}
 			}
 		} else if (propertyName.equals(FILE_NAME)) {
 			IStatus status = super.validate(propertyName);
 			if (!status.isOK()) {
 				return status;
 			} else if (cachedLoadError != null) {
 				return WTPCommonPlugin
 						.createWarningStatus(EARCreationResourceHandler.bind(EARCreationResourceHandler.EARImportDataModel_UI_4, new Object[] { cachedLoadError.getPath().toOSString() }));
 			}
 			return status;
 		}
 		// TODO: check context root is not inside current working
 		// directory...this is invalid
 		return super.validate(propertyName);
 	}
 
 	private void updateModuleRoot() {
 		List projects = getProjectModels();
 		String basePath = isPropertySet(NESTED_MODULE_ROOT) ? getStringProperty(NESTED_MODULE_ROOT) : null;
 		boolean useDefault = basePath == null;
 		IDataModel localModel = null;
 		for (int i = 0; null != projects && i < projects.size(); i++) {
 			localModel = (IDataModel) projects.get(i);
 			localModel.setProperty(IProjectCreationPropertiesNew.USER_DEFINED_BASE_LOCATION, basePath);
 			localModel.setBooleanProperty(IProjectCreationPropertiesNew.USE_DEFAULT_LOCATION, useDefault);
 		}
 	}
 
 	private void changeModuleCreationLocationForNameChange(List projects) {
 		IDataModel localModel = null;
 		for (int i = 0; null != projects && i < projects.size(); i++) {
 			localModel = (IDataModel) projects.get(i);
 			if (isPropertySet(NESTED_MODULE_ROOT)) {
 				IPath newPath = new Path((String) getProperty(NESTED_MODULE_ROOT));
 				newPath = newPath.append((String) localModel.getProperty(IJ2EEComponentImportDataModelProperties.PROJECT_NAME));
 				// model.setProperty(J2EEComponentCreationDataModel.PROJECT_LOCATION,
 				// newPath.toOSString());
 			} else {
 				// model.setProperty(J2EEComponentCreationDataModel.PROJECT_LOCATION, null);
 			}
 		}
 	}
 
 	private IPath getLocation() {
 		return ResourcesPlugin.getWorkspace().getRoot().getLocation();
 	}
 
 	private void trimSelection() {
 		boolean modified = false;
 		List selectedList = getSelectedModels();
 		List allList = getProjectModels();
 		for (int i = selectedList.size() - 1; i > -1; i--) {
 			if (!allList.contains(selectedList.get(i))) {
 				modified = true;
 				selectedList.remove(i);
 			}
 		}
 		if (modified) {
 			List newList = new ArrayList();
 			newList.addAll(selectedList);
 			setProperty(SELECTED_MODELS_LIST, newList);
 		}
 	}
 
 	private void updateUtilityModels(List<ArchiveWrapper> utilityJars) {
 		updateUtilityModels(utilityJars, SELECTED_MODELS_LIST, UTILITY_MODELS_LIST);
 	}
 
 	private void updateUtilityModels(List<ArchiveWrapper> utilityJars, String selectedProperty, String listTypeProperty) {
 		boolean allSelected = true;
 		List<IDataModel> selectedList = (List<IDataModel>) getProperty(selectedProperty);
 		List<IDataModel> allList = getProjectModels();
 		if (selectedList.size() == allList.size()) {
 			for (int i = 0; i < selectedList.size() && allSelected; i++) {
 				if (!selectedList.contains(allList.get(i)) || !allList.contains(selectedList.get(i))) {
 					allSelected = false;
 				}
 			}
 		} else {
 			allSelected = false;
 		}
 		List<IDataModel> utilityModels = (List<IDataModel>) getProperty(listTypeProperty);
 		ArchiveWrapper currentArchive = null;
 		IDataModel currentUtilityModel = null;
 		boolean utilityJarsModified = false;
 		// Add missing
 		for (int i = 0; null != utilityJars && i < utilityJars.size(); i++) {
 			currentArchive = utilityJars.get(i);
 			boolean added = false;
 			for (int j = 0; utilityModels != null && j < utilityModels.size() && !added; j++) {
 				currentUtilityModel = utilityModels.get(j);
 				if (currentUtilityModel.getProperty(IJavaUtilityJarImportDataModelProperties.ARCHIVE_WRAPPER).equals(currentArchive)) {
 					added = true;
 				}
 			}
 			if (!added) {
 				if (!isPropertySet(listTypeProperty)) {
 					utilityModels = new ArrayList<IDataModel>();
 					setProperty(listTypeProperty, utilityModels);
 				}
 				IDataModel localModel = DataModelFactory.createDataModel(new J2EEUtilityJarImportDataModelProvider());
 				localModel.setProperty(IJavaUtilityJarImportDataModelProperties.ARCHIVE_WRAPPER, currentArchive);
 				localModel.setProperty(IJavaUtilityJarImportDataModelProperties.EAR_PROJECT_NAME, getStringProperty(PROJECT_NAME));
 				localModel.setProperty(IFacetProjectCreationDataModelProperties.FACET_RUNTIME, getProperty(IFacetProjectCreationDataModelProperties.FACET_RUNTIME));
 				if(utilityModels != null){
 					utilityModels.add(localModel);
 				}
 				localModel.addListener(nestedListener);
 				utilityJarsModified = true;
 			}
 		} // Remove extras
 		if(utilityModels != null){
 			for (int i = utilityModels.size() - 1; i >= 0; i--) {
 				currentUtilityModel = utilityModels.get(i);
 				currentArchive = (ArchiveWrapper) currentUtilityModel.getProperty(IJavaUtilityJarImportDataModelProperties.ARCHIVE_WRAPPER);
 				if (null == utilityJars || !utilityJars.contains(currentArchive)) {
 					currentUtilityModel.removeListener(nestedListener);
 					currentUtilityModel.setBooleanProperty(IJavaUtilityJarImportDataModelProperties.CLOSE_ARCHIVE_ON_DISPOSE, false);
 					currentUtilityModel.dispose();
 					utilityModels.remove(currentUtilityModel);
 					utilityJarsModified = true;
 				}
 			}
 		}
 		allList = getProjectModels();
 		if (allSelected) {
 			List<IDataModel> newList = new ArrayList<IDataModel>();
 			newList.addAll(allList);
 			setProperty(SELECTED_MODELS_LIST, newList);
 		} else {
 			trimSelection();
 		}
 		if (utilityJarsModified) {
 			model.notifyPropertyChange(NESTED_PROJECTS_VALIDATION, IDataModel.VALUE_CHG);
 		}
 	}
 
 	private List getModuleModels() {
 		ArchiveWrapper earWrapper = getArchiveWrapper();
 		if (earWrapper == null)
 			return Collections.EMPTY_LIST;
 		cachedLoadError = null;
 		List<ArchiveWrapper> modules = earWrapper.getEarModules();
 		List moduleModels = new ArrayList();
 		List<ArchiveWrapper> clientJarArchives = new ArrayList();
 		IDataModel localModel;
 		String earProjectName = getStringProperty(IFacetProjectCreationDataModelProperties.FACET_PROJECT_NAME);
 
 		List defaultModuleNames = new ArrayList();
 		defaultModuleNames.add(earProjectName);
 		List collidingModuleNames = null;
 		Hashtable<IDataModel, ArchiveWrapper> ejbJarsWithClients = new Hashtable();
 		for (int i = 0; i < modules.size(); i++) {
 			localModel = null;
 			ArchiveWrapper temp = modules.get(i);
 			try {
 				if (temp.isApplicationClientFile()) {
 					localModel = DataModelFactory.createDataModel(new AppClientComponentImportDataModelProvider());
 				} else if (temp.isWARFile()) {
 					WebModuleExtension webExt = EarModuleManager.getWebModuleExtension();
 					if (webExt != null) {
 						localModel = webExt.createImportDataModel();
 						String ctxRt = temp.getWebContextRoot();
 						if (null != ctxRt) {
 							localModel.setProperty(IAddWebComponentToEnterpriseApplicationDataModelProperties.CONTEXT_ROOT, ctxRt);
 						}
 					}
 				} else if (temp.isEJBJarFile()) {
 					EjbModuleExtension ejbExt = EarModuleManager.getEJBModuleExtension();
 					if (ejbExt != null) {
 						localModel = ejbExt.createImportDataModel();
 					}
 					try {
 						ArchiveWrapper clientArch = earWrapper.getEJBClientArchiveWrapper(temp);
 						if (null != clientArch) {
 							clientJarArchives.add(clientArch);
 							ejbJarsWithClients.put(localModel, clientArch);
 						}
 					} catch (Exception e) {
 						J2EEPlugin.logError(e);
 					}
 
 				} else if (temp.isRARFile()) {
 					JcaModuleExtension rarExt = EarModuleManager.getJCAModuleExtension();
 					if (rarExt != null) {
 						localModel = rarExt.createImportDataModel();
 					}
 				}
 				if (localModel != null) {
 					localModel.setProperty(ARCHIVE_WRAPPER, temp);
 					localModel.setProperty(IJ2EEFacetProjectCreationDataModelProperties.EAR_PROJECT_NAME, earProjectName);
 					localModel.setProperty(IFacetProjectCreationDataModelProperties.FACET_RUNTIME, getProperty(IFacetProjectCreationDataModelProperties.FACET_RUNTIME));
 					localModel.addListener(this);
 					localModel.addListener(nestedListener);
 					moduleModels.add(localModel);
 					String moduleName = localModel.getStringProperty(IJ2EEFacetProjectCreationDataModelProperties.FACET_PROJECT_NAME);
 					if (defaultModuleNames.contains(moduleName)) {
 						if (collidingModuleNames == null) {
 							collidingModuleNames = new ArrayList();
 						}
 						collidingModuleNames.add(moduleName);
 					} else {
 						defaultModuleNames.add(moduleName);
 					}
 				}
 			} catch (Exception e) {
 				J2EEPlugin.logError("Error loading nested archive: " + temp.getPath().toOSString()); //$NON-NLS-1$
 				J2EEPlugin.logError(e);
 				cachedLoadError = temp;
 			}
 		}
 		updateUtilityModels(clientJarArchives, EJB_CLIENT_LIST, EJB_CLIENT_LIST);
 		List<IDataModel> clientModelList = (List<IDataModel>) getProperty(EJB_CLIENT_LIST);
 		Enumeration<IDataModel> ejbModels = ejbJarsWithClients.keys();
 		ejbJarModelsToClientJarModels.clear();
 		clientJarModelsToEjbJarModels.clear();
 		while (ejbModels.hasMoreElements()) {
 			IDataModel ejbModel = ejbModels.nextElement();
 			ArchiveWrapper ejbClientArchiveWrapper = ejbJarsWithClients.get(ejbModel);
 			IDataModel clientModel = null;
 			for (int i = 0; clientModel == null && i < clientModelList.size(); i++) {
 				if (((ArchiveWrapper) clientModelList.get(i).getProperty(ARCHIVE_WRAPPER)).getUnderLyingArchive() == ejbClientArchiveWrapper.getUnderLyingArchive()) {
 					clientModel = clientModelList.get(i);
 				}
 			}
 			ejbJarModelsToClientJarModels.put(ejbModel, clientModel);
 			clientJarModelsToEjbJarModels.put(clientModel, ejbModel);
 		}
 
 		for (int i = 0; collidingModuleNames != null && i < moduleModels.size(); i++) {
 			localModel = (IDataModel) moduleModels.get(i);
 			String moduleName = localModel.getStringProperty(IJ2EEModuleImportDataModelProperties.PROJECT_NAME);
 			if (collidingModuleNames.contains(moduleName)) {
 				ArchiveWrapper module = (ArchiveWrapper) localModel.getProperty(IJ2EEModuleImportDataModelProperties.ARCHIVE_WRAPPER);
 				String suffix = null;
 				if (module.isApplicationClientFile()) {
 					suffix = "_AppClient"; //$NON-NLS-1$
 				} else if (module.isWARFile()) {
 					suffix = "_WEB"; //$NON-NLS-1$
 				} else if (module.isEJBJarFile()) {
 					suffix = "_EJB"; //$NON-NLS-1$
 				} else if (module.isRARFile()) {
 					suffix = "_JCA"; //$NON-NLS-1$
 				}
 				if (defaultModuleNames.contains(moduleName + suffix)) {
 					int count = 1;
 					for (; defaultModuleNames.contains(moduleName + suffix + count) && count < 10; count++){
 						//do nothing simply incrementing count
 					}
 					suffix += count;
 				}
 				localModel.setProperty(IJ2EEModuleImportDataModelProperties.PROJECT_NAME, moduleName + suffix);
 			}
 		}
 		return moduleModels;
 	}
 
 	@Override
 	protected int getType() {
 		return XMLResource.APPLICATION_TYPE;
 	}
 
 	private List<IDataModel> getProjectModels() {
 		List<IDataModel> temp = new ArrayList<IDataModel>();
 		List tempList = (List) getProperty(MODULE_MODELS_LIST);
 		if (null != tempList) {
 			temp.addAll(tempList);
 		}
 		tempList = (List) getProperty(UTILITY_MODELS_LIST);
 		if (null != tempList) {
 			temp.addAll(tempList);
 		}
 		tempList = (List) getProperty(EJB_CLIENT_LIST);
 		if (null != tempList) {
 			temp.addAll(tempList);
 		}
 		return temp;
 	}
 
 	private List getUnhandledProjectModels() {
 		List handled = removeHandledModels(getProjectModels(), getProjectModels(), false);
 		List all = getProjectModels();
 		all.removeAll(handled);
 		return all;
 	}
 
 	public List getSelectedModels() {
 		return (List) getProperty(SELECTED_MODELS_LIST);
 	}
 
 	private List removeHandledModels(List listToPrune, List modelsToCheck, boolean addModels) {
 		List newList = new ArrayList();
 		newList.addAll(listToPrune);
 		// IDataModel localModel = null;
 		// for (int i = 0; i < modelsToCheck.size(); i++) {
 		// localModel = (IDataModel) modelsToCheck.get(i);
 		// model.extractHandled(newList, addModels);
 		// }
 		return newList;
 	}
 
 	private List getHandledSelectedModels() {
 		List selectedModels = getSelectedModels();
 		return removeHandledModels(selectedModels, selectedModels, true);
 	}
 
 	@Override
 	public boolean isPropertyEnabled(String propertyName) {
 		if (!super.isPropertyEnabled(propertyName)) {
 			return false;
 		}
 		if (propertyName.equals(USE_ANNOTATIONS)) {
 			if (getJ2EEVersion() < J2EEVersionConstants.VERSION_1_3)
 				return false;
 			return true;
 		}
 		return true;
 	}
 
 	@Override
 	public void dispose() {
 		super.dispose();
 		List list = getProjectModels();
 		for (int i = 0; i < list.size(); i++) {
 			((IDataModel) list.get(i)).dispose();
 		}
 	}
 
 	@Override
 	protected IDataModel createJ2EEComponentCreationDataModel() {
 		return DataModelFactory.createDataModel(new EARFacetProjectCreationDataModelProvider());
 	}
 
 	@Override
 	public IDataModelOperation getDefaultOperation() {
 		return new EARComponentImportOperation(model);
 	}
 
 	@Override
 	public void init() {
 		super.init();
 		IDataModel componentCreationDM = model.getNestedModel(NESTED_MODEL_J2EE_COMPONENT_CREATION);
 		FacetDataModelMap map = (FacetDataModelMap) componentCreationDM.getProperty(IFacetProjectCreationDataModelProperties.FACET_DM_MAP);
 		IDataModel earFacet = map.getFacetDataModel( IJ2EEFacetConstants.ENTERPRISE_APPLICATION );	
 		earFacet.setBooleanProperty(IJ2EEFacetInstallDataModelProperties.GENERATE_DD, false);
 	}
 	
 	@Override
 	protected ArchiveWrapper openArchiveWrapper(String uri) throws ArchiveOpenFailureException {
 		ArchiveWrapper wrapper = super.openArchiveWrapper(uri);
 		if(null != wrapper){
 			JavaEEQuickPeek jqp =  wrapper.getJavaEEQuickPeek();
 			if(jqp.getType() != JavaEEQuickPeek.APPLICATION_TYPE){
 				wrapper.close();
 				throw new ArchiveOpenFailureException(CommonArchiveResourceHandler.getString(CommonArchiveResourceHandler.could_not_open_EXC_, (new Object[]{uri})));
 			}
 		}
 		return wrapper;
 	}
 	
 	@Override
 	protected void handleUnknownType(JavaEEQuickPeek jqp) {
 		jqp.setType(J2EEVersionConstants.APPLICATION_TYPE);
 		jqp.setVersion(J2EEVersionConstants.JEE_6_0_ID);
 		jqp.setJavaEEVersion(J2EEVersionConstants.JEE_6_0_ID);
 	}
 }
