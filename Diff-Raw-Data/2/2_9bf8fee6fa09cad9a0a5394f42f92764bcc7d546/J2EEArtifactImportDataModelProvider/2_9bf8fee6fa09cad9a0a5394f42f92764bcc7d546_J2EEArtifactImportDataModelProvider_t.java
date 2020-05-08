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
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.zip.ZipException;
 import java.util.zip.ZipFile;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jem.util.emf.workbench.ProjectUtilities;
 import org.eclipse.jst.common.project.facet.core.JavaFacet;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.util.ArchiveUtil;
 import org.eclipse.jst.j2ee.datamodel.properties.IJ2EEComponentImportDataModelProperties;
 import org.eclipse.jst.j2ee.internal.J2EEConstants;
 import org.eclipse.jst.j2ee.internal.J2EEVersionConstants;
 import org.eclipse.jst.j2ee.internal.archive.ArchiveWrapper;
 import org.eclipse.jst.j2ee.internal.archive.JavaEEArchiveUtilities;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEPlugin;
 import org.eclipse.jst.j2ee.internal.project.J2EECreationResourceHandler;
 import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
 import org.eclipse.jst.j2ee.project.facet.IJ2EEFacetConstants;
 import org.eclipse.jst.jee.archive.ArchiveOpenFailureException;
 import org.eclipse.jst.jee.archive.ArchiveOptions;
 import org.eclipse.jst.jee.archive.IArchive;
 import org.eclipse.jst.jee.archive.internal.ZipFileArchiveLoadAdapterImpl;
 import org.eclipse.jst.jee.util.internal.JavaEEQuickPeek;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.datamodel.FacetProjectCreationDataModelProvider;
 import org.eclipse.wst.common.componentcore.datamodel.properties.IFacetDataModelProperties;
 import org.eclipse.wst.common.componentcore.datamodel.properties.IFacetProjectCreationDataModelProperties;
 import org.eclipse.wst.common.componentcore.datamodel.properties.IFacetProjectCreationDataModelProperties.FacetDataModelMap;
 import org.eclipse.wst.common.frameworks.datamodel.AbstractDataModelProvider;
 import org.eclipse.wst.common.frameworks.datamodel.DataModelEvent;
 import org.eclipse.wst.common.frameworks.datamodel.DataModelPropertyDescriptor;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModelListener;
 import org.eclipse.wst.common.frameworks.internal.plugin.WTPCommonMessages;
 import org.eclipse.wst.common.frameworks.internal.plugin.WTPCommonPlugin;
 import org.eclipse.wst.common.project.facet.core.IFacetedProjectWorkingCopy;
 import org.eclipse.wst.common.project.facet.core.IProjectFacet;
 import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
 import org.eclipse.wst.common.project.facet.core.runtime.IRuntime;
 
 public abstract class J2EEArtifactImportDataModelProvider extends AbstractDataModelProvider implements IJ2EEComponentImportDataModelProperties, IDataModelListener {
 
 	private static final String USE_DEFAULT_PROJECT_NAME = "J2EEArtifactImportDataModelProvider.USE_DEFAULT_PROJECT_NAME"; //$NON-NLS-1$
 	
 	public static final String FACET_RUNTIME = "IJ2EEArtifactImportDataModelProperties.FACET_RUNTIME"; //$NON-NLS-1$	
 
 	private IDataModel componentCreationDM;
 	private Throwable archiveOpenFailure = null;
 
 	@Override
 	public Set getPropertyNames() {
 		Set propertyNames = super.getPropertyNames();
 		propertyNames.add(FILE_NAME);
 		propertyNames.add(CLOSE_ARCHIVE_ON_DISPOSE);
 		propertyNames.add(USE_DEFAULT_PROJECT_NAME);
 		propertyNames.add(PROJECT_NAME);
 		propertyNames.add(COMPONENT);
 		propertyNames.add( FACET_RUNTIME );
 		propertyNames.add(ARCHIVE_WRAPPER);
 		return propertyNames;
 	}
 
 	@Override
 	public void init() {
 		super.init();
 		componentCreationDM = createJ2EEComponentCreationDataModel();
 		componentCreationDM.setBooleanProperty(FacetProjectCreationDataModelProvider.FORCE_VERSION_COMPLIANCE, false);
 		componentCreationDM.addListener(this);
 		model.addNestedModel(NESTED_MODEL_J2EE_COMPONENT_CREATION, componentCreationDM);
 	}
 
 	@Override
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
 
 	@Override
 	public boolean propertySet(String propertyName, Object propertyValue) {
 		if (propertyName.equals(ARCHIVE_WRAPPER)) {
 			if(propertyValue != null){
 				if (!settingFileName) {
 					setProperty(FILE_NAME, null);
 				}
 				updateDefaultComponentName();
 			}
 			return true;
 		} else if (FILE_NAME.equals(propertyName)) {
 			try {
 				archiveOpenFailure = null;
 				handleArchiveSetup((String) propertyValue);
 			} catch (ArchiveOpenFailureException e) {
 				archiveOpenFailure = e;
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
					IFacetedProjectWorkingCopy fpwc = (IFacetedProjectWorkingCopy)nestedModel.getProperty(IFacetProjectCreationDataModelProperties.FACETED_PROJECT_WORKING_COPY);
					fpwc.setProjectName((String)propertyValue);
 				} catch (Exception e) {
 					J2EEPlugin.logError(e);
 				}
 			}
 		} else if( FACET_RUNTIME.equals( propertyName )){
 			throw new RuntimeException(propertyName + " should not be set."); //$NON-NLS-1$
 		}
 		return true;
 	}
 
 	private boolean doingComponentUpdate;
 
 	private void updateDefaultComponentName() {
 		ArchiveWrapper wrapper = getArchiveWrapper();
 		if (null != wrapper && getBooleanProperty(USE_DEFAULT_PROJECT_NAME)) {
 			try {
 				doingComponentUpdate = true;
 				IPath path = wrapper.getPath();
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
 
 	private boolean handleArchiveSetup(String fileName) throws ArchiveOpenFailureException {
 		try {
 			settingFileName = true;
 			ArchiveWrapper wrapper = getArchiveWrapper();
 			if (wrapper!= null) {
 				wrapper.close();
 				setProperty(ARCHIVE_WRAPPER, null);
 			}
 			String uri = getStringProperty(FILE_NAME);
 			if (!archiveExistsOnFile())
 				return false;
 			wrapper = openArchiveWrapper(uri);
 			if(wrapper != null){
 				setProperty(ARCHIVE_WRAPPER, wrapper);
 			}
 			return wrapper!= null;
 		} finally {
 			settingFileName = false;
 		}
 
 	}
 
 	protected ArchiveOptions getArchiveOptions(IPath archivePath) throws ArchiveOpenFailureException {
 		java.io.File file = new java.io.File(archivePath.toOSString());
 		ZipFile zipFile;
 		try {
 			zipFile = org.eclipse.jst.jee.archive.internal.ArchiveUtil.newZipFile(file);
 		} catch (ZipException e) {
 			ArchiveOpenFailureException openFailureException = new ArchiveOpenFailureException(e);
 			throw openFailureException;
 		} catch (IOException e) {
 			ArchiveOpenFailureException openFailureException = new ArchiveOpenFailureException(e);
 			throw openFailureException;
 		}
 		ZipFileArchiveLoadAdapterImpl loadAdapter = new ZipFileArchiveLoadAdapterImpl(zipFile);
 		ArchiveOptions archiveOptions = new ArchiveOptions();
 		archiveOptions.setOption(ArchiveOptions.LOAD_ADAPTER, loadAdapter);
 		archiveOptions.setOption(ArchiveOptions.ARCHIVE_PATH, archivePath);
 		archiveOptions.setOption(JavaEEArchiveUtilities.DISCRIMINATE_EJB_ANNOTATIONS, Boolean.TRUE);
 		return archiveOptions;
 	}
 	
 	protected ArchiveWrapper openArchiveWrapper(String uri) throws ArchiveOpenFailureException{
 		IArchive archive = null;
 		IPath path = new Path(uri);
 		ArchiveOptions archiveOptions = getArchiveOptions(path);
 		archive = JavaEEArchiveUtilities.INSTANCE.openArchive(archiveOptions);
 		archive.setPath(path);
 		JavaEEQuickPeek jqp = JavaEEArchiveUtilities.INSTANCE.getJavaEEQuickPeek(archive);
 		
 		if(jqp.getJavaEEVersion() == J2EEConstants.UNKNOWN && jqp.getType() == J2EEConstants.UNKNOWN){
 			handleUnknownType(jqp);
 		}
 		
 		return new ArchiveWrapper(archive);
 	}
 	/**
 	 * This method allows subclasses to handle an unknown archive type.
 	 * @param jqp
 	 */
 	protected void handleUnknownType(JavaEEQuickPeek jqp) {
 	}
 
 	private boolean closeArchive() {
 		if (null != getArchiveWrapper()) {
 			getArchiveWrapper().close();
 		}
 		return true;
 	}
 
 	@Override
 	public IStatus validate(String propertyName) {
 		if (FILE_NAME.equals(propertyName) && !isPropertySet(ARCHIVE_WRAPPER)) {
 			String fileName = getStringProperty(propertyName);
 			if (fileName == null || fileName.length() == 0) {
 				return WTPCommonPlugin.createErrorStatus(WTPCommonPlugin.getResourceString(WTPCommonMessages.ARCHIVE_FILE_NAME_EMPTY_ERROR, new Object[]{ArchiveUtil.getModuleFileTypeName(getType())}));
 			} else if (archiveOpenFailure != null) {
 				return WTPCommonPlugin.createErrorStatus(archiveOpenFailure.getMessage());
 			} else if (!archiveExistsOnFile()) {
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
 
 	@Override
 	public void dispose() {
 		if (getBooleanProperty(CLOSE_ARCHIVE_ON_DISPOSE))
 			closeArchive();
 		super.dispose();
 	}
 
 	protected final ArchiveWrapper getArchiveWrapper(){
 		return (ArchiveWrapper)getProperty(ARCHIVE_WRAPPER);
 	}
 	
 	@Override
 	public DataModelPropertyDescriptor[] getValidPropertyDescriptors(String propertyName) {
 		return super.getValidPropertyDescriptors(propertyName);
 	}
 
 	public void propertyChanged(DataModelEvent event) {
 		if (!doingComponentUpdate && event.getDataModel() == componentCreationDM && event.getPropertyName().equals(PROJECT_NAME) && getBooleanProperty(USE_DEFAULT_PROJECT_NAME)) {
 			setBooleanProperty(USE_DEFAULT_PROJECT_NAME, false);
 		}
 		if( event.getDataModel() == componentCreationDM && event.getPropertyName().equals(IFacetProjectCreationDataModelProperties.FACET_RUNTIME)){
 			model.notifyPropertyChange(FACET_RUNTIME, IDataModel.DEFAULT_CHG);
 			if(isPropertySet(ARCHIVE_WRAPPER)){
 				refreshInterpretedSpecVersion();
 			}
 		}
 	}
 	
 	protected void refreshInterpretedSpecVersion(){
 		
 	}
 	
 	/**
 	 * Updates the Java Facet Version so it is compliant with the Java EE Module version 
 	 */
 	protected void updateJavaFacetVersion() {
 		IProjectFacetVersion javaFacetVersion = null;
 		IRuntime runtime = (IRuntime)getProperty(IFacetProjectCreationDataModelProperties.FACET_RUNTIME);
 		if(runtime != null){
 			if(runtime.supports(JavaFacet.VERSION_1_7)){
 				javaFacetVersion = JavaFacet.VERSION_1_7;
 			} else if(runtime.supports(JavaFacet.VERSION_1_6)){
 				javaFacetVersion = JavaFacet.VERSION_1_6;
 			} else if(runtime.supports(JavaFacet.VERSION_1_5)){
 				javaFacetVersion = JavaFacet.VERSION_1_5;
 			} else {
 				javaFacetVersion = JavaFacet.VERSION_1_4;
 			}
 		} else {
 			JavaEEQuickPeek jqp = getInterpretedSpecVersion(getArchiveWrapper());
 			int javaEEVersion = jqp.getJavaEEVersion();
 			switch (javaEEVersion){
 			case J2EEVersionConstants.J2EE_1_2_ID:
 			case J2EEVersionConstants.J2EE_1_3_ID:
 			case J2EEVersionConstants.J2EE_1_4_ID:
 				javaFacetVersion = JavaFacet.VERSION_1_4;
 				break;
 			case J2EEVersionConstants.JEE_5_0_ID:
 				javaFacetVersion = JavaFacet.VERSION_1_5;
 				break;
 			case J2EEVersionConstants.JEE_6_0_ID:
 				javaFacetVersion = JavaFacet.VERSION_1_6;
 				break;
 			}
 		}
 		if(javaFacetVersion != null){
 			IDataModel moduleDM = model.getNestedModel(NESTED_MODEL_J2EE_COMPONENT_CREATION);
 			FacetDataModelMap map = (FacetDataModelMap) moduleDM.getProperty(IFacetProjectCreationDataModelProperties.FACET_DM_MAP);
 			IDataModel javaFacetDataModel = map.getFacetDataModel( J2EEProjectUtilities.JAVA );
 			javaFacetDataModel.setProperty(IFacetDataModelProperties.FACET_VERSION, javaFacetVersion);
 			updateWorkingCopyFacetVersion(moduleDM, javaFacetDataModel);
 		}
 	}
 	
 	protected void updateWorkingCopyFacetVersion(IDataModel moduleDM, IDataModel facetDM) {
 		IProjectFacetVersion facetVersion = (IProjectFacetVersion)facetDM.getProperty(IFacetDataModelProperties.FACET_VERSION);
 		//[Bug 314162] IFacetedProjectWorkingCopy facet version is not automatically updated so it has to be done manually
 		IFacetedProjectWorkingCopy fpwc = (IFacetedProjectWorkingCopy)moduleDM.getProperty(IFacetProjectCreationDataModelProperties.FACETED_PROJECT_WORKING_COPY);
 		fpwc.changeProjectFacetVersion(facetVersion);
 	}
 	
 	/**
 	 * Calling this method will fixup the JST facet version if it is incompatible with the selected runtime
 	 * It should be called when the Server Runtime or the Archive properties are set.
 	 * @return
 	 */
 	protected IStatus validateVersionSupportedByServer(){
 		if( model.isPropertySet(ARCHIVE_WRAPPER) && model.isPropertySet(IFacetProjectCreationDataModelProperties.FACET_RUNTIME)){
 			IDataModel projectModel = model.getNestedModel(NESTED_MODEL_J2EE_COMPONENT_CREATION);
 			FacetDataModelMap map = (FacetDataModelMap) projectModel.getProperty(IFacetProjectCreationDataModelProperties.FACET_DM_MAP);
 			Collection projectFacets = (Collection)getProperty(FacetProjectCreationDataModelProvider.REQUIRED_FACETS_COLLECTION);
 					
 			IRuntime runtime = (IRuntime) getProperty(IFacetProjectCreationDataModelProperties.FACET_RUNTIME);
 			if(runtime != null){
 				for(Iterator iterator = projectFacets.iterator(); iterator.hasNext();){
 					IDataModel facetDataModel = map.getFacetDataModel(((IProjectFacet)iterator.next()).getId());
 					IProjectFacetVersion facetVersion = (IProjectFacetVersion)facetDataModel.getProperty(IFacetDataModelProperties.FACET_VERSION);
 					if(facetVersion.getProjectFacet().equals(JavaFacet.FACET)){
 						Set set = Collections.singleton(facetVersion.getProjectFacet());
 						try {
 							Set correctSet = runtime.getDefaultFacets(set);
 							IProjectFacetVersion correctVersion = null;
 							Iterator correctVersions = correctSet.iterator();
 							while(correctVersions.hasNext() && correctVersion == null){
 								IProjectFacetVersion version = (IProjectFacetVersion)correctVersions.next();
 								if(version.getProjectFacet().equals(JavaFacet.FACET)){
 									correctVersion = version;
 								}
 							}
 							
 							if(correctVersion != null){
 								if(!facetVersion.equals(correctVersion)){
 									facetDataModel.setProperty(IFacetDataModelProperties.FACET_VERSION, correctVersion);
 									facetVersion = correctVersion;
 								}
 							}
 						} catch (CoreException e) {
 							J2EEPlugin.logError(e);
 						}
 					}
 				
 					if(!runtime.supports(facetVersion)){
 						return WTPCommonPlugin.createErrorStatus( J2EECreationResourceHandler.VERSION_NOT_SUPPORTED );
 					}
 				}
 			}
 		}
 		return OK_STATUS;
 	}
 	
 	/**
 	 * If the archive does not have a deployment descriptor, then the 
 	 * version will be interpreted as the highest version supported
 	 * by the server.
 	 * @return
 	 */
 	protected JavaEEQuickPeek getInterpretedSpecVersion(ArchiveWrapper wrapper){
 		JavaEEQuickPeek jqp = wrapper.getJavaEEQuickPeek();
 		IArchive archive = wrapper.getIArchive();
 		if(!archive.isOpen()){
 			return jqp;
 		}
 		int archiveType = jqp.getType();
 		String ddURI = null;
 		IProjectFacetVersion [] highestProjectFacetVersion = new IProjectFacetVersion [2];
 		int [] highestJQPVersion = new int[2];
 		int EE6 = 0;
 		int EE5 = 1;
 		switch(archiveType){
 		case JavaEEQuickPeek.APPLICATION_TYPE:
 			ddURI = J2EEConstants.APPLICATION_DD_URI;
 			highestProjectFacetVersion[EE6] = IJ2EEFacetConstants.ENTERPRISE_APPLICATION_60;
 			highestProjectFacetVersion[EE5] = IJ2EEFacetConstants.ENTERPRISE_APPLICATION_50;
 			highestJQPVersion[EE6] = JavaEEQuickPeek.VERSION_6_0;
 			highestJQPVersion[EE5] = JavaEEQuickPeek.VERSION_5_0;
 			break;
 		case JavaEEQuickPeek.APPLICATION_CLIENT_TYPE:
 			ddURI = J2EEConstants.APP_CLIENT_DD_URI;
 			highestProjectFacetVersion[EE6] = IJ2EEFacetConstants.APPLICATION_CLIENT_60;
 			highestProjectFacetVersion[EE5] = IJ2EEFacetConstants.APPLICATION_CLIENT_50;
 			highestJQPVersion[EE6] = JavaEEQuickPeek.VERSION_6_0;
 			highestJQPVersion[EE5] = JavaEEQuickPeek.VERSION_5_0;
 			break;
 		case JavaEEQuickPeek.EJB_TYPE:
 			ddURI = J2EEConstants.EJBJAR_DD_URI;
 			highestProjectFacetVersion[EE6] = IJ2EEFacetConstants.EJB_31;
 			highestProjectFacetVersion[EE5] = IJ2EEFacetConstants.EJB_30;
 			highestJQPVersion[EE6] = JavaEEQuickPeek.VERSION_3_1;
 			highestJQPVersion[EE5] = JavaEEQuickPeek.VERSION_3_0;
 			break;
 		case JavaEEQuickPeek.WEB_TYPE:
 			ddURI = J2EEConstants.WEBAPP_DD_URI;
 			highestProjectFacetVersion[EE6] = IJ2EEFacetConstants.DYNAMIC_WEB_30;
 			highestProjectFacetVersion[EE5] = IJ2EEFacetConstants.DYNAMIC_WEB_25;
 			highestJQPVersion[EE6] = JavaEEQuickPeek.VERSION_3_0;
 			highestJQPVersion[EE5] = JavaEEQuickPeek.VERSION_2_5;
 			break;
 		case JavaEEQuickPeek.CONNECTOR_TYPE:
 			ddURI = J2EEConstants.RAR_DD_URI;
 			highestProjectFacetVersion[EE6] = IJ2EEFacetConstants.JCA_16;
 			highestJQPVersion[EE6] = JavaEEQuickPeek.VERSION_1_6;
 			break;
 		default:
 			return jqp;
 		}
 		IPath ddPath = new Path(ddURI);
 		if(archive.containsArchiveResource(ddPath)){
 			return jqp;
 		}
 		
 		IRuntime runtime = (IRuntime)getProperty(IFacetProjectCreationDataModelProperties.FACET_RUNTIME);
 		if(archiveType == JavaEEQuickPeek.CONNECTOR_TYPE || runtime == null || runtime.supports(highestProjectFacetVersion[EE6]) ){
 			return new JavaEEQuickPeek(jqp.getType(), highestJQPVersion[EE6]);
 		} else if(runtime.supports(highestProjectFacetVersion[EE5])){
 			return new JavaEEQuickPeek(jqp.getType(), highestJQPVersion[EE5]);
 		} else{
 			return jqp;
 		}
 	}
 }
