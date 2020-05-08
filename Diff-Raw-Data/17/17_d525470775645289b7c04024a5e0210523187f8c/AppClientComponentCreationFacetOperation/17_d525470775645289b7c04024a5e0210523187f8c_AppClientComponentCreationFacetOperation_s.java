 package org.eclipse.jst.j2ee.applicationclient.internal.creation;
 
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.jem.util.emf.workbench.ProjectUtilities;
 import org.eclipse.jst.j2ee.datamodel.properties.IJ2EEComponentCreationDataModelProperties;
 import org.eclipse.jst.j2ee.datamodel.properties.IJavaComponentCreationDataModelProperties;
 import org.eclipse.jst.j2ee.project.facet.AppClientFacetInstallDataModelProvider;
 import org.eclipse.jst.j2ee.project.facet.IJ2EEModuleFacetInstallDataModelProperties;
 import org.eclipse.jst.j2ee.project.facet.J2EEComponentCreationFacetOperation;
 import org.eclipse.wst.common.componentcore.datamodel.FacetProjectCreationDataModelProvider;
 import org.eclipse.wst.common.componentcore.datamodel.properties.IComponentCreationDataModelProperties;
 import org.eclipse.wst.common.componentcore.datamodel.properties.IFacetDataModelProperties;
 import org.eclipse.wst.common.componentcore.datamodel.properties.IFacetProjectCreationDataModelProperties;
 import org.eclipse.wst.common.frameworks.datamodel.DataModelFactory;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 
 public class AppClientComponentCreationFacetOperation extends J2EEComponentCreationFacetOperation implements IFacetProjectCreationDataModelProperties {
 
 	public AppClientComponentCreationFacetOperation(IDataModel model) {
 		super(model);
 	}
 
 	public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
 		IDataModel dm = DataModelFactory.createDataModel(new FacetProjectCreationDataModelProvider());
 		String projectName = model.getStringProperty(IComponentCreationDataModelProperties.PROJECT_NAME);
 		dm.setProperty(IFacetProjectCreationDataModelProperties.FACET_PROJECT_NAME, projectName);
 
 		FacetDataModelMap map = (FacetDataModelMap) dm.getProperty(FACET_DM_MAP);
 		IDataModel javaDM = setupJavaInstallAction();
 		map.add(javaDM);
 		IDataModel newModel = setupAppClientFacetInstallAction();
 		map.add(newModel);
 		setRuntime(newModel, dm); // Setting runtime property
 
 		IStatus stat = dm.getDefaultOperation().execute(monitor, info);
 		if (stat.isOK()) {
 			String earProjectName = (String) model.getProperty(IJ2EEComponentCreationDataModelProperties.EAR_COMPONENT_NAME);
 			IProject earProject = ProjectUtilities.getProject(earProjectName);
 			if (earProject != null && earProject.exists())
 				stat = addtoEar(projectName, earProjectName);
 		}
 
 		return stat;
 	}
 
 	protected IDataModel setupAppClientFacetInstallAction() {
 		String versionStr = model.getPropertyDescriptor(IJ2EEComponentCreationDataModelProperties.COMPONENT_VERSION).getPropertyDescription();
 		IDataModel facetInstallDataModel = DataModelFactory.createDataModel(new AppClientFacetInstallDataModelProvider());
 		facetInstallDataModel.setProperty(IFacetDataModelProperties.FACET_PROJECT_NAME, model.getStringProperty(IComponentCreationDataModelProperties.PROJECT_NAME));
 		facetInstallDataModel.setProperty(IFacetDataModelProperties.FACET_VERSION_STR, versionStr);
 		facetInstallDataModel.setProperty(IJ2EEModuleFacetInstallDataModelProperties.CONFIG_FOLDER, model.getStringProperty(IJavaComponentCreationDataModelProperties.JAVASOURCE_FOLDER));
 		if (model.getBooleanProperty(IJ2EEComponentCreationDataModelProperties.ADD_TO_EAR))
 			facetInstallDataModel.setProperty(IJ2EEModuleFacetInstallDataModelProperties.EAR_PROJECT_NAME, model.getProperty(IJ2EEComponentCreationDataModelProperties.EAR_COMPONENT_NAME));
 		facetInstallDataModel.setProperty(IJ2EEModuleFacetInstallDataModelProperties.RUNTIME_TARGET_ID, model.getProperty(IJ2EEComponentCreationDataModelProperties.RUNTIME_TARGET_ID));
 		return facetInstallDataModel;
 	}
 }
