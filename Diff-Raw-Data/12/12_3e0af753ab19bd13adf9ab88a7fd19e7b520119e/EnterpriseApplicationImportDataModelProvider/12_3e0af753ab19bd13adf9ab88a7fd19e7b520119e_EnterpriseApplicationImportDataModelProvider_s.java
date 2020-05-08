 /***************************************************************************************************
  * Copyright (c) 2003, 2004 IBM Corporation and others. All rights reserved. This program and the
  * accompanying materials are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: IBM Corporation - initial API and implementation
  **************************************************************************************************/
 package org.eclipse.jst.j2ee.application.internal.operations;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.List;
 
 import org.eclipse.core.internal.localstore.CoreFileSystemLibrary;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jem.util.emf.workbench.ProjectUtilities;
 import org.eclipse.jst.j2ee.application.WebModule;
 import org.eclipse.jst.j2ee.applicationclient.internal.creation.AppClientComponentImportDataModelProvider;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.Archive;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.CommonarchiveFactory;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.EARFile;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.EJBJarFile;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.ModuleFile;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.WARFile;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.exception.OpenFailureException;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.impl.FileImpl;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.impl.WARFileImpl;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.util.ArchiveUtil;
 import org.eclipse.jst.j2ee.datamodel.properties.IAddWebComponentToEnterpriseApplicationDataModelProperties;
 import org.eclipse.jst.j2ee.datamodel.properties.IEARComponentImportDataModelProperties;
 import org.eclipse.jst.j2ee.datamodel.properties.IEarComponentCreationDataModelProperties;
 import org.eclipse.jst.j2ee.datamodel.properties.IJ2EEComponentImportDataModelProperties;
 import org.eclipse.jst.j2ee.datamodel.properties.IJ2EEModuleImportDataModelProperties;
 import org.eclipse.jst.j2ee.datamodel.properties.IJavaUtilityJarImportDataModelProperties;
 import org.eclipse.jst.j2ee.ejb.EJBJar;
 import org.eclipse.jst.j2ee.internal.J2EEVersionConstants;
 import org.eclipse.jst.j2ee.internal.archive.operations.EARComponentImportOperation;
 import org.eclipse.jst.j2ee.internal.common.XMLResource;
 import org.eclipse.jst.j2ee.internal.earcreation.EARCreationResourceHandler;
 import org.eclipse.jst.j2ee.internal.earcreation.EarComponentCreationDataModelProvider;
 import org.eclipse.jst.j2ee.internal.moduleextension.EarModuleManager;
 import org.eclipse.jst.j2ee.internal.moduleextension.EjbModuleExtension;
 import org.eclipse.jst.j2ee.internal.moduleextension.JcaModuleExtension;
 import org.eclipse.jst.j2ee.internal.moduleextension.WebModuleExtension;
 import org.eclipse.wst.common.frameworks.datamodel.DataModelEvent;
 import org.eclipse.wst.common.frameworks.datamodel.DataModelFactory;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModelListener;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModelOperation;
 import org.eclipse.wst.common.frameworks.internal.plugin.WTPCommonPlugin;
 import org.eclipse.wst.server.core.IRuntime;
 import org.eclipse.wst.server.core.ServerCore;
 
 //TODO rename to EARComponentImportDataModelProvider
 /**
  * This dataModel is used for to import Enterprise Applications(from EAR files) into the workspace.
  * 
  * This class (and all its fields and methods) is likely to change during the WTP 1.0 milestones as
  * the new project structures are adopted. Use at your own risk.
  * 
  * @plannedfor WTP 1.0
  */
 public final class EnterpriseApplicationImportDataModelProvider extends J2EEArtifactImportDataModelProvider implements IAnnotationsDataModel, IEARComponentImportDataModelProperties {
 
 	/**
 	 * Optional, type Boolean, default false, This flag is set to allow nested projects (module
 	 * projects & utility projects to be overwritten or not.
 	 */
 	public static final String OVERWRITE_NESTED_PROJECTS = "EARImportDataModel.OVERWRITE_NESTED_PROJECTS"; //$NON-NLS-1$
 
 	/**
 	 * Booleam, default is true. When all the module projects are added to the ear, this controls
 	 * whether their server targets will be set to be the same as the one set on the ear.
 	 */
 	public static final String SYNC_SERVER_TARGETS_WITH_EAR = "EARImportDataModel.SYNC_SERVER_TARGETS_WITH_EAR"; //$NON-NLS-1$
 
 	/**
 	 * This is only to force validation for the nested projects; do not set.
 	 */
 	public static final String NESTED_PROJECTS_VALIDATION = "EARImportDataModel.NESTED_PROJECTS_VALIDATION"; //$NON-NLS-1$
 
 
 	private IDataModelListener nestedListener = new IDataModelListener() {
 		public void propertyChanged(DataModelEvent event) {
 			if (event.getPropertyName().equals(PROJECT_NAME)) {
 				model.notifyPropertyChange(NESTED_PROJECTS_VALIDATION, IDataModel.DEFAULT_CHG);
 			}
 		}
 	};
 
 	private Hashtable ejbJarToClientJarModels = new Hashtable();
 
 	private Hashtable clientJarToEjbJarModels = new Hashtable();
 
 	public String[] getPropertyNames() {
 		return combineProperties(super.getPropertyNames(), new String[]{NESTED_MODULE_ROOT, UTILITY_LIST, MODULE_MODELS_LIST, EJB_CLIENT_LIST, UTILITY_MODELS_LIST, NESTED_PROJECTS_VALIDATION, SELECTED_MODELS_LIST, OVERWRITE_NESTED_PROJECTS, SYNC_SERVER_TARGETS_WITH_EAR, USE_ANNOTATIONS, ALL_PROJECT_MODELS_LIST, UNHANDLED_PROJECT_MODELS_LIST, HANDLED_PROJECT_MODELS_LIST});
 	}
 
 	public Object getDefaultProperty(String propertyName) {
 		if (NESTED_MODULE_ROOT.equals(propertyName)) {
 			return getLocation().toOSString();
 		} else if (MODULE_MODELS_LIST.equals(propertyName) || UTILITY_LIST.equals(propertyName) || UTILITY_MODELS_LIST.equals(propertyName) || SELECTED_MODELS_LIST.equals(propertyName) || EJB_CLIENT_LIST.equals(propertyName)) {
 			return Collections.EMPTY_LIST;
 		} else if (OVERWRITE_NESTED_PROJECTS.equals(propertyName)) {
 			return Boolean.FALSE;
 		} else if (SYNC_SERVER_TARGETS_WITH_EAR.equals(propertyName)) {
 			return Boolean.TRUE;
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
 
 	public void propertyChanged(DataModelEvent event) {
 		super.propertyChanged(event);
 		if (event.getPropertyName().equals(PROJECT_NAME)) {
 			changeModuleCreationLocationForNameChange(getProjectModels());
 		}
 		// if (event.getPropertyName().equals(J2EEArtifactImportDataModel.SERVER_TARGET_ID)) {
 		// changeModuleServerTargets((List) getProperty(MODULE_MODELS_LIST));
 		// }
 	}
 
 	/**
 	 * @param list
 	 */
 	private void changeModuleServerTargets(List projectModels) {
 		// IDataModel nestedModel = null;
 		// for (int i = 0; i < projectModels.size(); i++) {
 		// nestedModel = (IDataModel) projectModels.get(i);
 		// nestedModel.setProperty(J2EEArtifactImportDataModel.SERVER_TARGET_ID,
 		// getProperty(ServerTargetDataModel.RUNTIME_TARGET_ID));
 		// }
 	}
 
 	public boolean propertySet(String propertyName, Object propertyValue) {
 		if (ALL_PROJECT_MODELS_LIST.equals(propertyName) || UNHANDLED_PROJECT_MODELS_LIST.equals(propertyName) || HANDLED_PROJECT_MODELS_LIST.equals(propertyName)) {
 			throw new RuntimeException(propertyName + " is an unsettable property");
 		}
 		if (OVERWRITE_NESTED_PROJECTS.equals(propertyName)) {
 			List projectModels = getProjectModels();
 			IDataModel nestedModel = null;
 			for (int i = 0; i < projectModels.size(); i++) {
 				nestedModel = (IDataModel) projectModels.get(i);
 			}
 		}
 		boolean doSet = super.propertySet(propertyName, propertyValue);
 		if (NESTED_MODULE_ROOT.equals(propertyName)) {
 			changeModuleCreationLocationForAll(getProjectModels(), (String) propertyValue);
 		} else if (FILE_NAME.equals(propertyName)) {
 			setProperty(MODULE_MODELS_LIST, getModuleModels());
 			setProperty(UTILITY_LIST, null);
 
 
 			IDataModel earProjectModel = model.getNestedModel(NESTED_MODEL_J2EE_COMPONENT_CREATION);
 			if (getArchiveFile() != null) {
 				earProjectModel.setIntProperty(IEarComponentCreationDataModelProperties.COMPONENT_VERSION, ArchiveUtil.getFastSpecVersion((ModuleFile) getArchiveFile()));
 			}
 
 			model.notifyPropertyChange(PROJECT_NAME, IDataModel.VALID_VALUES_CHG);
 			if (getJ2EEVersion() < J2EEVersionConstants.VERSION_1_3)
 				setBooleanProperty(USE_ANNOTATIONS, false);
 			model.notifyPropertyChange(USE_ANNOTATIONS, IDataModel.ENABLE_CHG);
 		} else if (UTILITY_LIST.equals(propertyName)) {
 			updateUtilityModels((List) propertyValue);
 		} else if (RUNTIME_TARGET_ID.equals(propertyName)) {
 			List projectModels = (List) getProperty(MODULE_MODELS_LIST);
 			IDataModel nestedModel = null;
 			for (int i = 0; i < projectModels.size(); i++) {
 				nestedModel = (IDataModel) projectModels.get(i);
 				nestedModel.setProperty(RUNTIME_TARGET_ID, propertyValue);
 			}
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
 				nestedModel.setProperty(IJ2EEModuleImportDataModelProperties.EAR_COMPONENT_NAME, propertyValue);
 			}
 			nestedModels = (List) getProperty(UTILITY_MODELS_LIST);
 			for (int i = 0; i < nestedModels.size(); i++) {
 				nestedModel = (IDataModel) nestedModels.get(i);
 				nestedModel.setProperty(IJavaUtilityJarImportDataModelProperties.EAR_PROJECT_NAME, propertyValue);
 			}
 			IProject project = ProjectUtilities.getProject(getStringProperty(PROJECT_NAME));
 			if (null != project && project.exists()) {
 				IRuntime target = ServerCore.getProjectProperties(project).getRuntimeTarget();
 				if (null != target) {
 					setProperty(RUNTIME_TARGET_ID, target.getId());
 				}
 			}
 		}
 		return doSet;
 	}
 
 	private List getUtilitiesForMetaDataImport(EARFile earFile) {
 		List list = getAllUtilitiesExceptEJBClients(earFile);
 		for (int i = list.size() - 1; i > -1; i--) {
 			Archive archive = (Archive) list.get(i);
 			if (!archive.containsFile(".project")) { //$NON-NLS-1$
 				list.remove(archive);
 			}
 		}
 		return list;
 	}
 
 	public List getAllUtilitiesExceptEJBClients(EARFile earFile) {
 		List clientList = (List) getProperty(EJB_CLIENT_LIST);
 		List list = getAllUtilities(earFile);
 		for (int i = list.size() - 1; i > -1; i--) {
 			FileImpl file = (FileImpl) list.get(i);
 			boolean shouldRemove = false;
 			for (int j = 0; j < clientList.size() && !shouldRemove; j++) {
 				IDataModel model = (IDataModel) clientList.get(j);
 				if (model.getProperty(IJ2EEComponentImportDataModelProperties.FILE) == file) {
 					shouldRemove = true;
 				}
 			}
 
 			if (shouldRemove) {
 				list.remove(i);
 			}
 		}
 		return list;
 	}
 
 	public static List getAllUtilities(EARFile earFile) {
 		List files = earFile.getFiles();
 		List utilJars = new ArrayList();
 		for (int i = 0; i < files.size(); i++) {
 			FileImpl file = (FileImpl) files.get(i);
 			if (file.isArchive() && !file.isModuleFile() && file.getURI().endsWith(".jar")) { //$NON-NLS-1$
 				utilJars.add(file);
 			}
 			if (file.isWARFile()) {
 				utilJars.addAll(getWebLibs((WARFile)file));
 			}
 		}
 		return utilJars;
 	}
 	
 	public static List getWebLibs(WARFile warFile){
 		return ((WARFileImpl) warFile).getLibArchives();
 	}
 	
 
 	protected boolean forceResetOnPreserveMetaData() {
 		return false;
 	}
 
 	public IStatus validate(String propertyName) {
 		if (propertyName.equals(NESTED_PROJECTS_VALIDATION)) {
 			boolean overwrite = getBooleanProperty(OVERWRITE_NESTED_PROJECTS);
 			String earProjectName = getStringProperty(PROJECT_NAME);
 			List subProjects = getSelectedModels();
 			IDataModel subDataModel = null;
 			String tempProjectName = null;
 			Archive tempArchive = null;
 			IStatus tempStatus = null;
 			Hashtable projects = new Hashtable(4);
 			for (int i = 0; i < subProjects.size(); i++) {
 				subDataModel = (IDataModel) subProjects.get(i);
 				tempProjectName = subDataModel.getStringProperty(PROJECT_NAME);
                 //TODO: add manual validation
 //				IStatus status = ProjectCreationDataModel.validateProjectName(tempProjectName);
 //				if (!status.isOK()) {
 //					return status;
 //				}
 				tempArchive = (Archive) subDataModel.getProperty(FILE);
 				// if (!overwrite && subDataModel.getProject().exists()) {
 				// return
 				// WTPCommonPlugin.createErrorStatus(EARCreationResourceHandler.getString("EARImportDataModel_UI_0",
 				// new Object[]{tempProjectName, tempArchive.getURI()})); //$NON-NLS-1$
 				// }
 				tempStatus = subDataModel.validate();
 				if (!tempStatus.isOK()) {
 					return tempStatus;
 				}
 				if (tempProjectName.equals(earProjectName)) {
 					return WTPCommonPlugin.createErrorStatus(EARCreationResourceHandler.getString("EARImportDataModel_UI_1", new Object[]{tempProjectName, tempArchive.getURI()})); //$NON-NLS-1$
 				} else if (!CoreFileSystemLibrary.isCaseSensitive()) {
 					if (tempProjectName.toLowerCase().equals(earProjectName.toLowerCase())) {
 						return WTPCommonPlugin.createErrorStatus(EARCreationResourceHandler.getString("EARImportDataModel_UI_1", new Object[]{tempProjectName, tempArchive.getURI()})); //$NON-NLS-1$
 					}
 				}
 				if (projects.containsKey(tempProjectName)) {
 					return WTPCommonPlugin.createErrorStatus(EARCreationResourceHandler.getString("EARImportDataModel_UI_2", new Object[]{tempProjectName, tempArchive.getURI(), ((Archive) projects.get(tempProjectName)).getURI()})); //$NON-NLS-1$
 				} else if (!CoreFileSystemLibrary.isCaseSensitive()) {
 					String lowerCaseProjectName = tempProjectName.toLowerCase();
 					String currentKey = null;
 					Enumeration keys = projects.keys();
 					while (keys.hasMoreElements()) {
 						currentKey = (String) keys.nextElement();
 						if (currentKey.toLowerCase().equals(lowerCaseProjectName)) {
 							return WTPCommonPlugin.createErrorStatus(EARCreationResourceHandler.getString("EARImportDataModel_UI_2", new Object[]{tempProjectName, tempArchive.getURI(), ((Archive) projects.get(currentKey)).getURI()})); //$NON-NLS-1$
 						}
 					}
 				}
 				projects.put(tempProjectName, tempArchive);
 			}
 		}
 		// TODO: check context root is not inside current working
 		// directory...this is invalid
 		return super.validate(propertyName);
 	}
 
 	private void changeModuleCreationLocationForAll(List projects, String property) {
 		IDataModel model = null;
 		for (int i = 0; null != projects && i < projects.size(); i++) {
 			model = (IDataModel) projects.get(i);
 			IPath newPath = new Path(property);
 			newPath = newPath.append((String) model.getProperty(IJ2EEComponentImportDataModelProperties.PROJECT_NAME));
 			// model.setProperty(J2EEComponentCreationDataModel.PROJECT_LOCATION,
 			// newPath.toOSString());
 		}
 	}
 
 	private void changeModuleCreationLocationForNameChange(List projects) {
 		IDataModel model = null;
 		for (int i = 0; null != projects && i < projects.size(); i++) {
 			model = (IDataModel) projects.get(i);
 			if (isPropertySet(NESTED_MODULE_ROOT)) {
 				IPath newPath = new Path((String) getProperty(NESTED_MODULE_ROOT));
 				newPath = newPath.append((String) model.getProperty(IJ2EEComponentImportDataModelProperties.PROJECT_NAME));
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
 
 	private void updateUtilityModels(List utilityJars) {
 		updateUtilityModels(utilityJars, SELECTED_MODELS_LIST, UTILITY_MODELS_LIST);
 	}
 
 	private void updateUtilityModels(List utilityJars, String selectedProperty, String listTypeProperty) {
 		boolean allSelected = true;
 		List selectedList = (List) getProperty(selectedProperty);
 		List allList = getProjectModels();
 		if (selectedList.size() == allList.size()) {
 			for (int i = 0; i < selectedList.size() && allSelected; i++) {
 				if (!selectedList.contains(allList.get(i)) || !allList.contains(selectedList.get(i))) {
 					allSelected = false;
 				}
 			}
 		} else {
 			allSelected = false;
 		}
 		List utilityModels = (List) getProperty(listTypeProperty);
 		Archive currentArchive = null;
 		IDataModel currentUtilityModel = null;
 		boolean utilityJarsModified = false;
 		// Add missing
 		for (int i = 0; null != utilityJars && i < utilityJars.size(); i++) {
 			currentArchive = (Archive) utilityJars.get(i);
 			boolean added = false;
 			for (int j = 0; utilityModels != null && j < utilityModels.size() && !added; j++) {
 				currentUtilityModel = (IDataModel) utilityModels.get(j);
 				if (currentUtilityModel.getProperty(IJavaUtilityJarImportDataModelProperties.FILE) == currentArchive) {
 					added = true;
 				}
 			}
 			if (!added) {
 				if (!isPropertySet(listTypeProperty)) {
 					utilityModels = new ArrayList();
 					setProperty(listTypeProperty, utilityModels);
 				}
 				IDataModel model = DataModelFactory.createDataModel(new J2EEUtilityJarImportDataModelProvider());
 				model.setProperty(IJavaUtilityJarImportDataModelProperties.FILE, currentArchive);
 				model.setProperty(IJavaUtilityJarImportDataModelProperties.EAR_PROJECT_NAME, getStringProperty(PROJECT_NAME));
 				model.setProperty(IJavaUtilityJarImportDataModelProperties.EAR_COMPONENT_NAME, getStringProperty(COMPONENT_NAME));
 				utilityModels.add(model);
 				model.addListener(nestedListener);
 				utilityJarsModified = true;
 			}
 		} // Remove extras
 		for (int i = utilityModels.size() - 1; i >= 0; i--) {
 			currentUtilityModel = (IDataModel) utilityModels.get(i);
 			currentArchive = (Archive) currentUtilityModel.getProperty(IJavaUtilityJarImportDataModelProperties.FILE);
 			if (null == utilityJars || !utilityJars.contains(currentArchive)) {
 				currentUtilityModel.removeListener(nestedListener);
 				currentUtilityModel.dispose();
 				utilityModels.remove(currentUtilityModel);
 				utilityJarsModified = true;
 			}
 		}
 		allList = getProjectModels();
 		if (allSelected) {
 			List newList = new ArrayList();
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
 		if (getArchiveFile() == null)
 			return Collections.EMPTY_LIST;
 		List moduleFiles = getEARFile().getModuleFiles();
 		List moduleModels = new ArrayList();
 		List clientJarArchives = new ArrayList();
 		IDataModel localModel;
 		String earProjectName = getStringProperty(PROJECT_NAME);
 		List defaultModuleNames = new ArrayList();
 		List collidingModuleNames = null;
 		Hashtable ejbJarsWithClients = new Hashtable();
 		for (int i = 0; i < moduleFiles.size(); i++) {
 			localModel = null;
 			ModuleFile temp = (ModuleFile) moduleFiles.get(i);
 			if (temp.isApplicationClientFile()) {
 				localModel = DataModelFactory.createDataModel(new AppClientComponentImportDataModelProvider());
 			} else if (temp.isWARFile()) {
 				WebModuleExtension webExt = EarModuleManager.getWebModuleExtension();
 				if (webExt != null) {
 					localModel = webExt.createImportDataModel();
 					WebModule webModule = (WebModule) getEARFile().getModule(temp.getURI(), null);
 					if (null != webModule && null != webModule.getContextRoot()) {
 						localModel.setProperty(IAddWebComponentToEnterpriseApplicationDataModelProperties.CONTEXT_ROOT, webModule.getContextRoot());
 					}
 				}
 			} else if (temp.isEJBJarFile()) {
 				EjbModuleExtension ejbExt = EarModuleManager.getEJBModuleExtension();
 				if (ejbExt != null) {
 					localModel = ejbExt.createImportDataModel();
 				}
 				EJBJar jar = ((EJBJarFile) temp).getDeploymentDescriptor();
 				if (jar != null) {
 					if (jar.getEjbClientJar() != null) {
 						String clientName = jar.getEjbClientJar();
 						try {
 							Archive clientArchive = (Archive) getEARFile().getFile(clientName);
 							clientJarArchives.add(clientArchive);
 							ejbJarsWithClients.put(localModel, clientArchive);
 						} catch (Exception e) {
 							// TODO: handle exception
 						}
 					}
 				}
 			} else if (temp.isRARFile()) {
 				JcaModuleExtension rarExt = EarModuleManager.getJCAModuleExtension();
 				if (rarExt != null) {
 					localModel = rarExt.createImportDataModel();
 				}
 			}
 			if (localModel != null) {
 				localModel.setProperty(FILE, temp);
 				localModel.setProperty(IJ2EEModuleImportDataModelProperties.EAR_COMPONENT_NAME, earProjectName);
 				localModel.setBooleanProperty(IJ2EEModuleImportDataModelProperties.ADD_TO_EAR, false);
 				localModel.setProperty(RUNTIME_TARGET_ID, getProperty(RUNTIME_TARGET_ID));
 				localModel.addListener(this);
 				localModel.addListener(nestedListener);
 				moduleModels.add(localModel);
 				String moduleName = localModel.getStringProperty(IJ2EEModuleImportDataModelProperties.PROJECT_NAME);
 				if (defaultModuleNames.contains(moduleName)) {
 					if (collidingModuleNames == null) {
 						collidingModuleNames = new ArrayList();
 					}
 					collidingModuleNames.add(moduleName);
 				} else {
 					defaultModuleNames.add(moduleName);
 				}
 			}
 		}
 		updateUtilityModels(clientJarArchives, EJB_CLIENT_LIST, EJB_CLIENT_LIST);
 		List clientModelList = (List) getProperty(EJB_CLIENT_LIST);
 		Enumeration ejbModels = ejbJarsWithClients.keys();
 		ejbJarToClientJarModels.clear();
 		clientJarToEjbJarModels.clear();
 		while (ejbModels.hasMoreElements()) {
 			Object ejbModel = ejbModels.nextElement();
 			Object archive = ejbJarsWithClients.get(ejbModel);
 			Object clientModel = null;
 			for (int i = 0; clientModel == null && i < clientModelList.size(); i++) {
 				if (((IDataModel) clientModelList.get(i)).getProperty(FILE) == archive) {
 					clientModel = clientModelList.get(i);
 				}
 			}
 			ejbJarToClientJarModels.put(ejbModel, clientModel);
 			clientJarToEjbJarModels.put(clientModel, ejbModel);
 		}
 
 		for (int i = 0; collidingModuleNames != null && i < moduleModels.size(); i++) {
 			localModel = (IDataModel) moduleModels.get(i);
 			String moduleName = localModel.getStringProperty(IJ2EEModuleImportDataModelProperties.PROJECT_NAME);
 			if (collidingModuleNames.contains(moduleName)) {
 				ModuleFile module = (ModuleFile) localModel.getProperty(IJ2EEModuleImportDataModelProperties.FILE);
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
 					for (; defaultModuleNames.contains(moduleName + suffix + count) && count < 10; count++);
 					suffix += count;
 				}
 				localModel.setProperty(IJ2EEModuleImportDataModelProperties.PROJECT_NAME, moduleName + suffix);
 			}
 		}
 		return moduleModels;
 	}
 
 	protected int getType() {
 		return XMLResource.APPLICATION_TYPE;
 	}
 
 	protected Archive openArchive(String uri) throws OpenFailureException {
 		return CommonarchiveFactory.eINSTANCE.openEARFile(getArchiveOptions(), uri);
 	}
 
 	private EARFile getEARFile() {
 		return (EARFile) getArchiveFile();
 	}
 
 	public boolean handlesArchive(Archive anArchive) {
 		List temp = new ArrayList();
 		List tempList = (List) getProperty(MODULE_MODELS_LIST);
 		if (null != tempList) {
 			temp.addAll(tempList);
 		}
 		tempList = (List) getProperty(EJB_CLIENT_LIST);
 		if (null != tempList) {
 			temp.addAll(tempList);
 		}
 		tempList = getSelectedModels();
 		for (int i = 0; i < tempList.size(); i++) {
 			if (!temp.contains(tempList.get(i))) {
 				temp.add(tempList.get(i));
 			}
 		}
 		IDataModel importDM = null;
 		for (int i = 0; i < temp.size(); i++) {
 			importDM = (IDataModel) temp.get(i);
 			if (importDM.getProperty(FILE) == anArchive) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private List getProjectModels() {
 		List temp = new ArrayList();
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
 		IDataModel model = null;
 		for (int i = 0; i < modelsToCheck.size(); i++) {
 			model = (IDataModel) modelsToCheck.get(i);
 			// model.extractHandled(newList, addModels);
 		}
 		return newList;
 	}
 
 	private List getHandledSelectedModels() {
 		List selectedModels = getSelectedModels();
 		return removeHandledModels(selectedModels, selectedModels, true);
 	}
 
 	public int getJ2EEVersion() {
 		EARFile ef = getEARFile();
 		return null == ef ? J2EEVersionConstants.J2EE_1_2_ID : ArchiveUtil.getFastSpecVersion(ef);
 	}
 
 	public boolean isPropertyEnabled(String propertyName) {
 		if (!super.isPropertyEnabled(propertyName)) {
 			return false;
 		}
 		if (propertyName.equals(IEarComponentCreationDataModelProperties.RUNTIME_TARGET_ID)) {
 			IProject project = ProjectUtilities.getProject(getStringProperty(PROJECT_NAME));
 			if (null == project || !project.exists()) {
 				return true;
 			}
 		} else if (propertyName.equals(USE_ANNOTATIONS)) {
 			if (getJ2EEVersion() < J2EEVersionConstants.VERSION_1_3)
 				return false;
 			return true;
 		}
 		return true;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.j2ee.internal.internal.application.operations.J2EEImportDataModel#dispose()
 	 */
 	public void dispose() {
 		super.dispose();
 		List list = getProjectModels();
 		for (int i = 0; i < list.size(); i++) {
 			((IDataModel) list.get(i)).dispose();
 		}
 		EARFile earFile = getEARFile();
 		if (earFile != null)
 			earFile.close();
 	}
 //TODO: Implement with J2EEArtifactImportDataModelProvider
 /*	public J2EEArtifactImportDataModel getMatchingEJBJarOrClient(J2EEArtifactImportDataModel model) {
 		if (clientJarToEjbJarModels.containsKey(model)) {
 			return (J2EEArtifactImportDataModel) clientJarToEjbJarModels.get(model);
 		} else if (ejbJarToClientJarModels.containsKey(model)) {
 			return (J2EEArtifactImportDataModel) ejbJarToClientJarModels.get(model);
 		} else {
 			return null;
 		}
 	}*/
 
 	protected IDataModel createJ2EEComponentCreationDataModel() {
 		return DataModelFactory.createDataModel(new EarComponentCreationDataModelProvider());
 	}
 
 	public IDataModelOperation getDefaultOperation() {
 		return new EARComponentImportOperation(model);
 	}
 
 }
