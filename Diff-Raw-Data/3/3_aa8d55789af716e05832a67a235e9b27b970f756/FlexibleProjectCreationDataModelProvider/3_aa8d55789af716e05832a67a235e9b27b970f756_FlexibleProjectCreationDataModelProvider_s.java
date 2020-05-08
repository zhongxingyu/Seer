 package org.eclipse.jst.j2ee.application.internal.operations;
 
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.wst.common.frameworks.datamodel.AbstractDataModelProvider;
 import org.eclipse.wst.common.frameworks.datamodel.DataModelFactory;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModelOperation;
 import org.eclipse.wst.common.frameworks.datamodel.properties.IFlexibleProjectCreationDataModelProperties;
 import org.eclipse.wst.common.frameworks.internal.operations.IProjectCreationProperties;
 import org.eclipse.wst.common.frameworks.internal.operations.ProjectCreationDataModelProvider;
 
 public class FlexibleProjectCreationDataModelProvider  extends AbstractDataModelProvider 
 		implements IFlexibleProjectCreationDataModelProperties {
 
 	public FlexibleProjectCreationDataModelProvider() {
 		super();
         
 	}
 	public void init() {
 	    super.init();
         initNestedProjectModel();
 	}
 	protected void initNestedProjectModel() {
         IDataModel projModel = DataModelFactory.createDataModel(new ProjectCreationDataModelProvider());
         model.addNestedModel(NESTED_MODEL_PROJECT_CREATION, projModel);
     }
 
     public String[] getPropertyNames() {
 		return new String[] {PROJECT_NAME, PROJECT_LOCATION, NESTED_MODEL_PROJECT_CREATION};
 	}
 	
 	public Object getDefaultProperty(String propertyName) {
 		if (PROJECT_LOCATION.equals(propertyName)) {
 			return getDefaultLocation();
 		}
 		return super.getDefaultProperty(propertyName);
 	}
     public boolean propertySet(String propertyName, Object propertyValue) {
         boolean status = super.propertySet(propertyName, propertyValue);
         if (PROJECT_NAME.equals(propertyName)) {
             IDataModel projModel = model.getNestedModel(NESTED_MODEL_PROJECT_CREATION);
             projModel.setProperty(IProjectCreationProperties.PROJECT_NAME, propertyValue);
         }
         return status;
     }
 	
 	public IStatus validate(String propertyName) {
 		if (PROJECT_NAME.equals(propertyName)) {
 			return validateProjectName();
 		} 
 		return OK_STATUS;
 	}
 	
 	private IStatus validateProjectName() {
 		IDataModel projModel = model.getNestedModel(NESTED_MODEL_PROJECT_CREATION);
 		return projModel.validateProperty(IProjectCreationProperties.PROJECT_NAME);
 	}
 
 	private String getDefaultLocation() {
 		IPath path = getRootLocation();
 		String projectName = (String) getProperty(PROJECT_NAME);
 		if (projectName != null)
 			path = path.append(projectName);
 		return path.toOSString();
 	}
 
 	private IPath getRootLocation() {
 		return ResourcesPlugin.getWorkspace().getRoot().getLocation();
 	}
 //	
 //	protected void initNestedModels() {
 //		super.initNestedModels();
 //		initProjectModel();
 //		addNestedModel(NESTED_MODEL_PROJECT_CREATION, projectDataModel);
 //
 //		serverTargetDataModel = new J2EEProjectServerTargetDataModel();
 //		addNestedModel(NESTED_MODEL_SERVER_TARGET, serverTargetDataModel);
 //	}
 	
 //	protected void initProjectModel() {
 //		projectDataModel = new ProjectCreationDataModel();
 //	}
 //	
     public IDataModelOperation getDefaultOperation() {
         return new FlexibleProjectCreationOperation(model);
     }
 }
