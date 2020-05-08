 /*******************************************************************************
  * Copyright (c) 2003, 2005 IBM Corporation and others.
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
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jem.util.emf.workbench.ProjectUtilities;
 import org.eclipse.jem.util.logger.proxy.Logger;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.Archive;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.exception.OpenFailureException;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.helpers.ArchiveOptions;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.helpers.SaveFilter;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.util.ArchiveUtil;
 import org.eclipse.jst.j2ee.datamodel.properties.IJ2EEComponentImportDataModelProperties;
 import org.eclipse.jst.j2ee.internal.project.J2EECreationResourceHandler;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.datamodel.FacetProjectCreationDataModelProvider;
 import org.eclipse.wst.common.componentcore.datamodel.properties.IFacetDataModelProperties;
 import org.eclipse.wst.common.componentcore.datamodel.properties.IFacetProjectCreationDataModelProperties;
 import org.eclipse.wst.common.componentcore.datamodel.properties.IFacetProjectCreationDataModelProperties.FacetDataModelMap;
 import org.eclipse.wst.common.componentcore.internal.util.IModuleConstants;
 import org.eclipse.wst.common.frameworks.datamodel.AbstractDataModelProvider;
 import org.eclipse.wst.common.frameworks.datamodel.DataModelEvent;
 import org.eclipse.wst.common.frameworks.datamodel.DataModelPropertyDescriptor;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModelListener;
 import org.eclipse.wst.common.frameworks.internal.plugin.WTPCommonMessages;
 import org.eclipse.wst.common.frameworks.internal.plugin.WTPCommonPlugin;
 import org.eclipse.wst.common.project.facet.core.IProjectFacet;
 import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
 import org.eclipse.wst.common.project.facet.core.runtime.IRuntime;
 
 public abstract class J2EEArtifactImportDataModelProvider extends AbstractDataModelProvider implements IJ2EEComponentImportDataModelProperties, IDataModelListener {
 
 	private static final String USE_DEFAULT_PROJECT_NAME = "J2EEArtifactImportDataModelProvider.USE_DEFAULT_PROJECT_NAME"; //$NON-NLS-1$
 	
 	public static final String FACET_RUNTIME = "IJ2EEArtifactImportDataModelProperties.FACET_RUNTIME"; //$NON-NLS-1$	
 
 	private IDataModel componentCreationDM;
 	private OpenFailureException cachedOpenFailureException = null;
 
 	public Set getPropertyNames() {
 		Set propertyNames = super.getPropertyNames();
 		propertyNames.add(FILE_NAME);
 		propertyNames.add(FILE);
 		propertyNames.add(SAVE_FILTER);
 		propertyNames.add(OVERWRITE_HANDLER);
 		propertyNames.add(CLOSE_ARCHIVE_ON_DISPOSE);
 		propertyNames.add(USE_DEFAULT_PROJECT_NAME);
 		propertyNames.add(PROJECT_NAME);
 		propertyNames.add(COMPONENT);
 		propertyNames.add( FACET_RUNTIME );
 		return propertyNames;
 	}
 
 	public void init() {
 		super.init();
 		componentCreationDM = createJ2EEComponentCreationDataModel();
 		componentCreationDM.setBooleanProperty(FacetProjectCreationDataModelProvider.FORCE_VERSION_COMPLIANCE, false);
 		componentCreationDM.addListener(this);
 		model.addNestedModel(NESTED_MODEL_J2EE_COMPONENT_CREATION, componentCreationDM);
 	}
 
 	public Object getDefaultProperty(String propertyName) {
 		if (propertyName.equals(CLOSE_ARCHIVE_ON_DISPOSE)) {
 			return Boolean.TRUE;
 		} else if (propertyName.equals(USE_DEFAULT_PROJECT_NAME)) {
 			return Boolean.TRUE;
 		}else if( propertyName.equals(COMPONENT)){
 			String projectName = getStringProperty(PROJECT_NAME);
 			IProject project = ProjectUtilities.getProject(projectName);
 			return ComponentCore.createComponent(project);			
 		}
 		return super.getDefaultProperty(propertyName);
 	}
 
 	private boolean settingFileName = false;
 
 	public boolean propertySet(String propertyName, Object propertyValue) {
 		if (propertyName.equals(FILE)) {
 			if (!settingFileName) {
 				setProperty(FILE_NAME, null);
 			}
 			updateDefaultComponentName();
 			return true;
 		} else if (propertyName.equals(SAVE_FILTER) && getArchiveFile() != null) {
 			getArchiveFile().setSaveFilter(getSaveFilter());
 		} else if (FILE_NAME.equals(propertyName)) {
 			try {
 				cachedOpenFailureException = null;
 				handleArchiveSetup((String) propertyValue);
 			} catch (OpenFailureException oe) {
 				cachedOpenFailureException = oe;
 			}
 		} else if( COMPONENT.equals(propertyName)){
 			throw new RuntimeException(propertyName + " should not be set."); //$NON-NLS-1$
 		}else if (PROJECT_NAME.equals(propertyName)) {
 			List nestedModels = new ArrayList(model.getNestedModels());
 			IDataModel nestedModel = null;
 			for (int i = 0; i < nestedModels.size(); i++) {
 				nestedModel = (IDataModel) nestedModels.get(i);
 				try {
 					nestedModel.setProperty(IFacetProjectCreationDataModelProperties.FACET_PROJECT_NAME, propertyValue);
 				} catch (Exception e) {
 					Logger.getLogger().logError(e);
 				}
 			}
 		} else if( FACET_RUNTIME.equals( propertyName )){
 			throw new RuntimeException(propertyName + " should not be set."); //$NON-NLS-1$
 		}
 		return true;
 	}
 
 	private boolean doingComponentUpdate;
 
 	private void updateDefaultComponentName() {
 		Archive archive = getArchiveFile();
 		if (null != archive && getBooleanProperty(USE_DEFAULT_PROJECT_NAME)) {
 			try {
 				doingComponentUpdate = true;
 				Path path = new Path(archive.getURI());
 				String defaultProjectName = path.segment(path.segmentCount() - 1);
 				if (defaultProjectName.indexOf('.') > 0) {
 					defaultProjectName = defaultProjectName.substring(0, defaultProjectName.lastIndexOf('.'));
 				}
 				setProperty(PROJECT_NAME, defaultProjectName);
 			} finally {
 				doingComponentUpdate = false;
 			}
 
 		}
 	}
 
 	private boolean handleArchiveSetup(String fileName) throws OpenFailureException {
 		try {
 			settingFileName = true;
 			Archive archive = getArchiveFile();
 			if (archive != null) {
 				archive.close();
 				setProperty(FILE, null);
 			}
 			String uri = getStringProperty(FILE_NAME);
 			if (!archiveExistsOnFile())
 				return false;
 			archive = openArchive(uri);
 			if (null != archive) {
 				archive.setSaveFilter(getSaveFilter());
 			}
 			setProperty(FILE, archive);
 			return archive != null;
 		} finally {
 			settingFileName = false;
 		}
 
 	}
 
 	protected abstract Archive openArchive(String uri) throws OpenFailureException;
 
 	private boolean closeModuleFile() {
 		if (null != getArchiveFile()) {
 			getArchiveFile().close();
 		}
 		return true;
 	}
 
 	public IStatus validate(String propertyName) {
 		if (FILE_NAME.equals(propertyName) && !isPropertySet(FILE)) {
 			String fileName = getStringProperty(propertyName);
 			if (fileName == null || fileName.length() == 0) {
 				return WTPCommonPlugin.createErrorStatus(WTPCommonPlugin.getResourceString(WTPCommonMessages.ARCHIVE_FILE_NAME_EMPTY_ERROR, new Object[]{ArchiveUtil.getModuleFileTypeName(getType())}));
 			} else if (cachedOpenFailureException != null) {
 				return WTPCommonPlugin.createErrorStatus(WTPCommonPlugin.getResourceString(cachedOpenFailureException.getMessage()));
 			} else if (fileName != null && !archiveExistsOnFile()) {
 				return WTPCommonPlugin.createErrorStatus(WTPCommonPlugin.getResourceString(WTPCommonMessages.FILE_DOES_NOT_EXIST_ERROR, new Object[]{ArchiveUtil.getModuleFileTypeName(getType())}));
 			}
 		} else if (NESTED_MODEL_J2EE_COMPONENT_CREATION.equals(propertyName) ) {
 			return getDataModel().getNestedModel(NESTED_MODEL_J2EE_COMPONENT_CREATION).validate(true);
 		} else if(FACET_RUNTIME.equals(propertyName)){
 			return validateVersionSupportedByServer();
 		}
 		return OK_STATUS;
 	}
 
 	protected int getJ2EEVersion() {
 		return 0;
 	}
 
 	protected abstract IDataModel createJ2EEComponentCreationDataModel();
 
 	/*
 	 * @see XMLResource#APP_CLIENT_TYPE
 	 * @see XMLResource#APPLICATION_TYPE
 	 * @see XMLResource#EJB_TYPE
 	 * @see XMLResource#WEB_APP_TYPE
 	 * @see XMLResource#RAR_TYPE
 	 */
 	protected abstract int getType();
 
 	private boolean archiveExistsOnFile() {
 		String jarName = (String) getProperty(FILE_NAME);
 		if (jarName != null && jarName.length() > 0) {
 			java.io.File file = new java.io.File(jarName);
 			return file.exists() && !file.isDirectory();
 		}
 		return false;
 	}
 
 	public void dispose() {
 		if (getBooleanProperty(CLOSE_ARCHIVE_ON_DISPOSE))
 			closeModuleFile();
 		super.dispose();
 	}
 
 	protected final void setArchiveFile(Archive archiveFile) {
 		setProperty(FILE, archiveFile);
 	}
 
 	protected final Archive getArchiveFile() {
 		return (Archive) getProperty(FILE);
 	}
 
 	protected final ArchiveOptions getArchiveOptions() {
 		ArchiveOptions opts = new ArchiveOptions();
 		opts.setIsReadOnly(true);
 		return opts;
 	}
 
 	private SaveFilter getSaveFilter() {
 		return (SaveFilter) getProperty(SAVE_FILTER);
 	}
 
 	public DataModelPropertyDescriptor[] getValidPropertyDescriptors(String propertyName) {
 		return super.getValidPropertyDescriptors(propertyName);
 	}
 
 	public void propertyChanged(DataModelEvent event) {
 		if (!doingComponentUpdate && event.getDataModel() == componentCreationDM && event.getPropertyName().equals(PROJECT_NAME) && getBooleanProperty(USE_DEFAULT_PROJECT_NAME)) {
 			setBooleanProperty(USE_DEFAULT_PROJECT_NAME, false);
 		}
 		if( event.getDataModel() == componentCreationDM && event.getPropertyName().equals(IFacetProjectCreationDataModelProperties.FACET_RUNTIME)){
 			model.notifyPropertyChange(FACET_RUNTIME, IDataModel.DEFAULT_CHG);
 		}
 	}
 	
 	protected IStatus validateVersionSupportedByServer(){
 		if( model.isPropertySet(FILE)){
 			IDataModel projectModel = model.getNestedModel(NESTED_MODEL_J2EE_COMPONENT_CREATION);
 			FacetDataModelMap map = (FacetDataModelMap) projectModel.getProperty(IFacetProjectCreationDataModelProperties.FACET_DM_MAP);
 			Collection projectFacets = (Collection)getProperty(FacetProjectCreationDataModelProvider.REQUIRED_FACETS_COLLECTION);
 					
 			IRuntime runtime = (IRuntime) getProperty(IFacetProjectCreationDataModelProperties.FACET_RUNTIME); 
 			for(Iterator iterator = projectFacets.iterator(); iterator.hasNext();){
 				IDataModel facetDataModel = map.getFacetDataModel(((IProjectFacet)iterator.next()).getId());
 				IProjectFacetVersion facetVersion = (IProjectFacetVersion)facetDataModel.getProperty(IFacetDataModelProperties.FACET_VERSION);
 				if(facetVersion.getProjectFacet().getId().equals(IModuleConstants.JST_JAVA)){
 					Set set = Collections.singleton(facetVersion.getProjectFacet());
 					try {
						if( runtime != null ){
							Set correctSet = runtime.getDefaultFacets(set);
							IProjectFacetVersion correctVersion = null;
							Iterator correctVersions = correctSet.iterator();
							while(correctVersions.hasNext() && correctVersion == null){
								IProjectFacetVersion version = (IProjectFacetVersion)correctVersions.next();
								if(version.getProjectFacet().getId().equals(IModuleConstants.JST_JAVA)){
									correctVersion = version;
								}
							}
							
							if(correctVersion != null){
								facetDataModel.setProperty(IFacetDataModelProperties.FACET_VERSION, correctVersion);
								facetVersion = correctVersion;
 							}
 						}
 					} catch (CoreException e) {
 						Logger.getLogger().logError(e);
 					}
 				}
 			
 				if(runtime != null && !runtime.supports(facetVersion)){
 					return WTPCommonPlugin.createErrorStatus( J2EECreationResourceHandler.VERSION_NOT_SUPPORTED ); //$NON-NLS-1$
 				}
 			}
 		}
 		return OK_STATUS;
 	}
 }
