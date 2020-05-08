 package org.eclipse.jst.j2ee.internal;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectDescription;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.jem.util.logger.proxy.Logger;
 import org.eclipse.jst.common.project.facet.IJavaFacetInstallDataModelProperties;
 import org.eclipse.jst.common.project.facet.JavaFacetInstallDataModelProvider;
 import org.eclipse.jst.common.project.facet.WtpUtils;
 import org.eclipse.jst.j2ee.internal.common.CreationConstants;
 import org.eclipse.jst.j2ee.internal.earcreation.EarFacetInstallDataModelProvider;
 import org.eclipse.jst.j2ee.internal.ejb.project.operations.EjbFacetInstallDataModelProvider;
 import org.eclipse.jst.j2ee.internal.ejb.project.operations.IEjbFacetInstallDataModelProperties;
 import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
 import org.eclipse.jst.j2ee.jca.project.facet.ConnectorFacetInstallDataModelProvider;
 import org.eclipse.jst.j2ee.project.facet.AppClientFacetInstallDataModelProvider;
 import org.eclipse.jst.j2ee.project.facet.IAppClientFacetInstallDataModelProperties;
 import org.eclipse.jst.j2ee.project.facet.IJ2EEModuleFacetInstallDataModelProperties;
 import org.eclipse.jst.j2ee.project.facet.UtilityFacetInstallDataModelProvider;
 import org.eclipse.jst.j2ee.web.project.facet.WebFacetInstallDataModelProvider;
 import org.eclipse.jst.server.core.FacetUtil;
 import org.eclipse.wst.common.componentcore.datamodel.FacetProjectCreationDataModelProvider;
 import org.eclipse.wst.common.componentcore.datamodel.properties.IFacetDataModelProperties;
 import org.eclipse.wst.common.componentcore.datamodel.properties.IFacetProjectCreationDataModelProperties;
 import org.eclipse.wst.common.componentcore.datamodel.properties.IFacetProjectCreationDataModelProperties.FacetDataModelMap;
 import org.eclipse.wst.common.componentcore.internal.ComponentType;
 import org.eclipse.wst.common.componentcore.internal.IComponentProjectMigrator;
 import org.eclipse.wst.common.componentcore.internal.StructureEdit;
 import org.eclipse.wst.common.componentcore.internal.WorkbenchComponent;
 import org.eclipse.wst.common.frameworks.datamodel.DataModelFactory;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 import org.eclipse.wst.server.core.IRuntime;
 import org.eclipse.wst.server.core.ServerCore;
 import org.eclipse.wst.server.core.ServerUtil;
 
 public class J2EEComponentProjectMigrator implements IComponentProjectMigrator {
 
 	private IProject project;
 	public J2EEComponentProjectMigrator() {
 		super();
 		// TODO Auto-generated constructor stub
 	}
 
 	public void migrateProject(IProject aProject) {
 		if (aProject.isAccessible()) {
 			project = aProject;
 			removeComponentBuilders(project);
 			if (multipleComponentsDetected())
 				createNewProjects();
 			String facetid = getFacetFromProject(project);
 			if (facetid.length() == 0)
 				addFacets(project);
 		}
 
 	}
 
 		private void createNewProjects() {
 
 			StructureEdit se = null;
 			try {
 				se = StructureEdit.getStructureEditForWrite(project);
 				List comps = se.getComponentModelRoot().getComponents();
 				List removedComps = new ArrayList();
 				for (int i = 1;i<comps.size();i++) {
 					WorkbenchComponent comp = (WorkbenchComponent) comps.get(i);
 					IWorkspace ws = ResourcesPlugin.getWorkspace();
 					IProject newProj = ws.getRoot().getProject(comp.getName());
 					if (!newProj.exists()) {
 						try {
 							createProj(newProj,(!comp.getComponentType().getComponentTypeId().equals(J2EEProjectUtilities.ENTERPRISE_APPLICATION)));
 							WtpUtils.addNatures(newProj);
 						} catch (CoreException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 					}
 					addFacetsToProject(newProj,comp.getComponentType().getComponentTypeId(),comp.getComponentType().getVersion(),false);
 					removedComps.add(comp);
 					IFolder compFolder = project.getFolder(comp.getName());
 					if (compFolder.exists())
 						try {
 							compFolder.delete(true,null);
 						} catch (CoreException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
					se.save(null);
 				}
 				se.getComponentModelRoot().getComponents().removeAll(removedComps);
 			
 			} finally {
 				if (se != null)
 					se.dispose();
 			}
 	
 		
 	}
 
 		private void createProj(IProject newProj, boolean isJavaProject) throws CoreException {
 			newProj.create(null);
 			IProjectDescription description = ResourcesPlugin.getWorkspace().newProjectDescription(newProj.getName());
 //			if (isJavaProject)
 //				description.setNatureIds(new String[]{JavaCore.NATURE_ID});
 			description.setLocation(null);
 			newProj.open(null);
 			newProj.setDescription(description, null);
 		}
 
 		private boolean multipleComponentsDetected() {
 			StructureEdit se = null;
 			try {
 				se = StructureEdit.getStructureEditForRead(project);
 				if (se == null) return false;
 				return se.getComponentModelRoot().getComponents().size() > 1;
 			} finally {
 				if (se != null)
 					se.dispose();
 			}
 	}
 
 		private void removeComponentBuilders(IProject aProject) {
 		try {
 			aProject.refreshLocal(IResource.DEPTH_INFINITE,null);
 		} catch (CoreException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		//IJavaProject javaP = JemProjectUtilities.getJavaProject(aProject);
 		List oldBuilders = new ArrayList();
 		oldBuilders.add("org.eclipse.wst.common.modulecore.ComponentStructuralBuilder");
 		oldBuilders.add("org.eclipse.wst.common.modulecore.ComponentStructuralBuilderDependencyResolver");
 		oldBuilders.add("org.eclipse.wst.common.modulecore.DependencyGraphBuilder");
 		try {
 			J2EEProjectUtilities.removeBuilders(aProject,oldBuilders);
 		} catch (CoreException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	}
 
 		public String getFacetFromProject(IProject aProject) {
 			return J2EEProjectUtilities.getJ2EEProjectType(aProject);
 		}
 
 		
 		protected IDataModel setupJavaInstallAction(IProject aProject, boolean existing,String srcFolder) {
 			IDataModel dm = DataModelFactory.createDataModel(new JavaFacetInstallDataModelProvider());
 			dm.setProperty(IFacetDataModelProperties.FACET_PROJECT_NAME, aProject.getName());
 			dm.setProperty(IFacetDataModelProperties.FACET_VERSION_STR, "1.4"); //$NON-NLS-1$
 			if (!existing)
 				dm.setStringProperty(IJavaFacetInstallDataModelProperties.SOURCE_FOLDER_NAME, srcFolder); //$NON-NLS-1$
 			return dm;
 		}
 		
 		protected IDataModel setupUtilInstallAction(IProject aProject,String specVersion) {
 			IDataModel aFacetInstallDataModel = DataModelFactory.createDataModel(new UtilityFacetInstallDataModelProvider());
 			aFacetInstallDataModel.setProperty(IFacetDataModelProperties.FACET_PROJECT_NAME, aProject.getName());
 			aFacetInstallDataModel.setProperty(IFacetDataModelProperties.FACET_VERSION_STR, specVersion);
 			aFacetInstallDataModel.setBooleanProperty(IJ2EEModuleFacetInstallDataModelProperties.ADD_TO_EAR,false);
 			aFacetInstallDataModel.setStringProperty(IJ2EEModuleFacetInstallDataModelProperties.EAR_PROJECT_NAME,null);
 			return aFacetInstallDataModel;
 		}
 		protected IDataModel setupEarInstallAction(IProject aProject,String specVersion) {
 			IDataModel earFacetInstallDataModel = DataModelFactory.createDataModel(new EarFacetInstallDataModelProvider());
 			earFacetInstallDataModel.setProperty(IFacetDataModelProperties.FACET_PROJECT_NAME, aProject.getName());
 			earFacetInstallDataModel.setProperty(IFacetDataModelProperties.FACET_VERSION_STR, specVersion);
 			
 			return earFacetInstallDataModel;
 		}
 		protected IDataModel setupAppClientInstallAction(IProject aProject,String specVersion) {
 			IDataModel aFacetInstallDataModel = DataModelFactory.createDataModel(new AppClientFacetInstallDataModelProvider());
 			aFacetInstallDataModel.setProperty(IFacetDataModelProperties.FACET_PROJECT_NAME, aProject.getName());
 			aFacetInstallDataModel.setProperty(IFacetDataModelProperties.FACET_VERSION_STR, specVersion);
 			aFacetInstallDataModel.setBooleanProperty(IJ2EEModuleFacetInstallDataModelProperties.ADD_TO_EAR,false);
 			aFacetInstallDataModel.setStringProperty(IJ2EEModuleFacetInstallDataModelProperties.EAR_PROJECT_NAME,null);
 			aFacetInstallDataModel.setBooleanProperty(IAppClientFacetInstallDataModelProperties.CREATE_DEFAULT_MAIN_CLASS,false);
 			return aFacetInstallDataModel;
 		}
 		protected IDataModel setupConnectorInstallAction(IProject aProject,String specVersion) {
 			IDataModel aFacetInstallDataModel = DataModelFactory.createDataModel(new ConnectorFacetInstallDataModelProvider());
 			aFacetInstallDataModel.setProperty(IFacetDataModelProperties.FACET_PROJECT_NAME, aProject.getName());
 			aFacetInstallDataModel.setProperty(IFacetDataModelProperties.FACET_VERSION_STR, specVersion);
 			aFacetInstallDataModel.setBooleanProperty(IJ2EEModuleFacetInstallDataModelProperties.ADD_TO_EAR,false);
 			aFacetInstallDataModel.setStringProperty(IJ2EEModuleFacetInstallDataModelProperties.EAR_PROJECT_NAME,null);
 			return aFacetInstallDataModel;
 		}
 
 		private void addFacets(IProject aProject) {
 			StructureEdit edit = null;
 			try {
 				edit = StructureEdit.getStructureEditForRead(aProject);
 				if (edit == null) return;  // Not a component project....
 				if (edit.getComponent() == null) return; // Can't migrate
 				ComponentType type = edit.getComponent().getComponentType();
 				if (type == null) return;  // Can't migrate
 				String compId = type.getComponentTypeId();
 				String specVersion = edit.getComponent().getComponentType().getVersion();
 				addFacetsToProject(aProject, compId, specVersion,true);
 			}
 			finally {
 				if (edit != null)
 					edit.dispose();
 			}
 			
 		}
 
 		private void addFacetsToProject(IProject aProject, String compId, String specVersion,boolean existing) {
 			if (compId.equals(J2EEProjectUtilities.DYNAMIC_WEB))
 				installWEBFacets(aProject,specVersion,existing);
 			else if (compId.equals(J2EEProjectUtilities.EJB))
 				installEJBFacets(aProject,specVersion,existing);
 			else if (compId.equals(J2EEProjectUtilities.APPLICATION_CLIENT))
 				installAppClientFacets(aProject,specVersion,existing);
 			else if (compId.equals(J2EEProjectUtilities.ENTERPRISE_APPLICATION))
 				installEARFacets(aProject,specVersion,existing);
 			else if (compId.equals(J2EEProjectUtilities.JCA))
 				installConnectorFacets(aProject,specVersion,existing);
 			else if (compId.equals(J2EEProjectUtilities.UTILITY))
 				installUtilityFacets(aProject,specVersion,existing);
 			else if (compId.equals(J2EEProjectUtilities.STATIC_WEB))
 				installStaticWebFacets(aProject,specVersion,existing);
 		}
 
 		private void installStaticWebFacets(IProject project2, String specVersion, boolean existing) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		private void installUtilityFacets(IProject aProject, String specVersion, boolean existing) {
 			IDataModel dm = DataModelFactory.createDataModel(new FacetProjectCreationDataModelProvider());
 			dm.setProperty(IFacetProjectCreationDataModelProperties.FACET_PROJECT_NAME, aProject.getName());
 			FacetDataModelMap facetDMs = (FacetDataModelMap) dm.getProperty(IFacetProjectCreationDataModelProperties.FACET_DM_MAP);
 			facetDMs.add(setupJavaInstallAction(aProject,existing,"src"));
 			IDataModel newModel = setupUtilInstallAction(aProject,specVersion);
 			facetDMs.add(newModel);
 			try {
 				IStatus stat =  dm.getDefaultOperation().execute(null,null);
 			} catch (ExecutionException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 		}
 
 		private void installConnectorFacets(IProject aProject, String specVersion, boolean existing) {
 			IDataModel dm = DataModelFactory.createDataModel(new FacetProjectCreationDataModelProvider());
 			dm.setProperty(IFacetProjectCreationDataModelProperties.FACET_PROJECT_NAME, aProject.getName());
 			FacetDataModelMap facetDMs = (FacetDataModelMap) dm.getProperty(IFacetProjectCreationDataModelProperties.FACET_DM_MAP);
 			facetDMs.add(setupJavaInstallAction(aProject,existing,CreationConstants.DEFAULT_CONNECTOR_SOURCE_FOLDER));
 			IDataModel newModel = setupConnectorInstallAction(aProject,specVersion);
 			facetDMs.add(newModel);
 			try {
 				IStatus stat =  dm.getDefaultOperation().execute(null,null);
 			} catch (ExecutionException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 		}
 
 		private void installEARFacets(IProject aProject, String specVersion, boolean existing) {
 			IDataModel dm = DataModelFactory.createDataModel(new FacetProjectCreationDataModelProvider());
 			dm.setProperty(IFacetProjectCreationDataModelProperties.FACET_PROJECT_NAME, aProject.getName());
 			FacetDataModelMap facetDMs = (FacetDataModelMap) dm.getProperty(IFacetProjectCreationDataModelProperties.FACET_DM_MAP);
 			IDataModel newModel = setupEarInstallAction(aProject,specVersion);
 			facetDMs.add(newModel);
 			try {
 				IStatus stat =  dm.getDefaultOperation().execute(null,null);
 			} catch (ExecutionException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 		}
 
 		private void installAppClientFacets(IProject aProject, String specVersion, boolean existing) {
 			IDataModel dm = DataModelFactory.createDataModel(new FacetProjectCreationDataModelProvider());
 			dm.setProperty(IFacetProjectCreationDataModelProperties.FACET_PROJECT_NAME, aProject.getName());
 			FacetDataModelMap facetDMs = (FacetDataModelMap) dm.getProperty(IFacetProjectCreationDataModelProperties.FACET_DM_MAP);
 			facetDMs.add(setupJavaInstallAction(aProject,existing,CreationConstants.DEFAULT_APPCLIENT_SOURCE_FOLDER));
 			IDataModel newModel = setupAppClientInstallAction(aProject,specVersion);
 			facetDMs.add(newModel);
 			try {
 				IStatus stat =  dm.getDefaultOperation().execute(null,null);
 			} catch (ExecutionException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 		}
 
 		private void installEJBFacets(IProject ejbProject2,String ejbVersion, boolean existing) {
 			IDataModel dm = DataModelFactory.createDataModel(new FacetProjectCreationDataModelProvider());
 			dm.setProperty(IFacetProjectCreationDataModelProperties.FACET_PROJECT_NAME, ejbProject2.getName());
 			FacetDataModelMap facetDMs = (FacetDataModelMap) dm.getProperty(IFacetProjectCreationDataModelProperties.FACET_DM_MAP);
 			facetDMs.add(setupJavaInstallAction(ejbProject2,existing,CreationConstants.DEFAULT_EJB_SOURCE_FOLDER));
 			IDataModel newModel = setupEjbInstallAction(ejbProject2,ejbVersion,existing);
 			facetDMs.add(newModel);
 			//setRuntime(ejbProject2,dm); //Setting runtime property
 			try {
 				IStatus stat =  dm.getDefaultOperation().execute(null,null);
 			} catch (ExecutionException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 		}
 		private void installWEBFacets(IProject webProj,String specVersion, boolean existing) {
 			IDataModel dm = DataModelFactory.createDataModel(new FacetProjectCreationDataModelProvider());
 			dm.setProperty(IFacetProjectCreationDataModelProperties.FACET_PROJECT_NAME, webProj.getName());
 			FacetDataModelMap facetDMs = (FacetDataModelMap) dm.getProperty(IFacetProjectCreationDataModelProperties.FACET_DM_MAP);
 			facetDMs.add(setupJavaInstallAction(webProj,existing,CreationConstants.DEFAULT_WEB_SOURCE_FOLDER));
 			IDataModel newModel = setupWebInstallAction(webProj,specVersion);
 			facetDMs.add(newModel);
 			//setRuntime(webProj,dm); //Setting runtime property
 			try {
 				IStatus stat =  dm.getDefaultOperation().execute(null,null);
 			} catch (ExecutionException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 		}
 		protected IRuntime getRuntimeByID(String id) {
 			IRuntime[] targets = ServerUtil.getRuntimes("", "");
 			for (int i = 0; i < targets.length; i++) {
 				IRuntime target = targets[i];
 				if (id.equals(target.getId()))
 					return target;
 			}
 			return null;
 		}
 
 		private void setRuntime(IProject aProject,IDataModel facetModel) {
 
 			IRuntime runtime = ServerCore.getProjectProperties(aProject).getRuntimeTarget();
 			try {
 				if (runtime != null) {
 					IRuntime run = getRuntimeByID(runtime.getId());
 					org.eclipse.wst.common.project.facet.core.runtime.IRuntime facetRuntime = null;
 					try {
 						if (run != null)
 							facetRuntime = FacetUtil.getRuntime(run);
 					}
 					catch (IllegalArgumentException ex)
 					{}
 					if (facetRuntime != null) {
 						facetModel.setProperty(IFacetProjectCreationDataModelProperties.FACET_RUNTIME,facetRuntime);
 					}
 				}
 				} catch (IllegalArgumentException e) {
 				Logger.getLogger().logError(e);
 			}
 		
 			
 		}
 
 		protected IDataModel setupEjbInstallAction(IProject aProject,String ejbVersion, boolean existing) {
 			IDataModel ejbFacetInstallDataModel = DataModelFactory.createDataModel(new EjbFacetInstallDataModelProvider());
 			ejbFacetInstallDataModel.setProperty(IFacetDataModelProperties.FACET_PROJECT_NAME, aProject.getName());
 			ejbFacetInstallDataModel.setProperty(IFacetDataModelProperties.FACET_VERSION_STR, ejbVersion);
 			ejbFacetInstallDataModel.setBooleanProperty(IJ2EEModuleFacetInstallDataModelProperties.ADD_TO_EAR,false);
 			ejbFacetInstallDataModel.setStringProperty(IJ2EEModuleFacetInstallDataModelProperties.EAR_PROJECT_NAME,null);
 			if (!existing)
 				ejbFacetInstallDataModel.setProperty(IEjbFacetInstallDataModelProperties.CONFIG_FOLDER, CreationConstants.DEFAULT_EJB_SOURCE_FOLDER);
 			return ejbFacetInstallDataModel;
 		}
 
 		protected IDataModel setupWebInstallAction(IProject aProject,String specVersion) {
 			IDataModel webFacetInstallDataModel = DataModelFactory.createDataModel(new WebFacetInstallDataModelProvider());
 			webFacetInstallDataModel.setProperty(IFacetDataModelProperties.FACET_PROJECT_NAME, aProject.getName());
 			webFacetInstallDataModel.setProperty(IFacetDataModelProperties.FACET_VERSION_STR, specVersion);
 			webFacetInstallDataModel.setBooleanProperty(IJ2EEModuleFacetInstallDataModelProperties.ADD_TO_EAR,false);
 			webFacetInstallDataModel.setStringProperty(IJ2EEModuleFacetInstallDataModelProperties.EAR_PROJECT_NAME,null);
 			return webFacetInstallDataModel;
 		}
 
 
 }
