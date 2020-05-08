 /*******************************************************************************
  * Copyright (c) 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.internal;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectDescription;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.ProjectScope;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.preferences.IEclipsePreferences;
 import org.eclipse.core.runtime.preferences.IScopeContext;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jem.util.logger.proxy.Logger;
 import org.eclipse.jem.workbench.utility.JemProjectUtilities;
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
 import org.eclipse.wst.common.componentcore.internal.ComponentcoreFactory;
 import org.eclipse.wst.common.componentcore.internal.IComponentProjectMigrator;
 import org.eclipse.wst.common.componentcore.internal.Property;
 import org.eclipse.wst.common.componentcore.internal.StructureEdit;
 import org.eclipse.wst.common.componentcore.internal.WorkbenchComponent;
 import org.eclipse.wst.common.frameworks.datamodel.DataModelFactory;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 import org.eclipse.wst.project.facet.SimpleWebFacetInstallDataModelProvider;
 import org.eclipse.wst.server.core.IRuntime;
 import org.eclipse.wst.server.core.ServerCore;
 import org.eclipse.wst.server.core.ServerUtil;
 
 public class J2EEComponentProjectMigrator implements IComponentProjectMigrator {
 
 	private static final String WEB_LIB_CONTAINER = "org.eclipse.jst.j2ee.internal.web.container";
 	private static final String WEB_LIB_PATH = "/WEB-INF/lib";
 	private static final String OLD_DEPLOYABLES_PATH = ".deployables";
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
 					if (comp!=null && comp.getComponentType()!=null)
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
 				}
 				se.getComponentModelRoot().getComponents().removeAll(removedComps);
 				se.save(null);
 			
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
 				if (se.getComponentModelRoot() == null) return false;
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
 			String jVersion = "1.4";
 			IScopeContext context = new ProjectScope( project );
 		    IEclipsePreferences prefs 
 		            = context.getNode( JavaCore.PLUGIN_ID );
 			if (JavaCore.VERSION_1_5.equals(prefs.get(JavaCore.COMPILER_COMPLIANCE,JavaCore.VERSION_1_4))) {
 				jVersion = "5.0";
 			}
 			dm.setProperty(IFacetDataModelProperties.FACET_VERSION_STR, jVersion); //$NON-NLS-1$
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
 				edit = StructureEdit.getStructureEditForWrite(aProject);
 				if (edit == null) return;  // Not a component project....
 				if (edit.getComponent() == null) return; // Can't migrate
 				ComponentType type = edit.getComponent().getComponentType();
 				if (type == null) return;  // Can't migrate
 				String compId = type.getComponentTypeId();
 				String specVersion = edit.getComponent().getComponentType().getVersion();
 				moveMetaProperties(edit.getComponent(),type);
 				addFacetsToProject(aProject, compId, specVersion,true);
 			}
 			finally {
 				if (edit != null) {
 					edit.save(null);
 					edit.dispose();
 				}
 			}
 			
 		}
 
 		private void moveMetaProperties(WorkbenchComponent component, ComponentType type) {
 			List props = type.getProperties();
 			List compProps = component.getProperties();
 			for (Iterator iter = props.iterator(); iter.hasNext();) {
 				Property element = (Property) iter.next();
 				Property newProp = ComponentcoreFactory.eINSTANCE.createProperty();
 				newProp.setName(element.getName());
 				newProp.setValue(element.getValue());
 				compProps.add(newProp);
 			}
 			props.clear();
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
 			IDataModel dm = DataModelFactory.createDataModel(new FacetProjectCreationDataModelProvider());
 			dm.setProperty(IFacetProjectCreationDataModelProperties.FACET_PROJECT_NAME, project2.getName());
 			FacetDataModelMap facetDMs = (FacetDataModelMap) dm.getProperty(IFacetProjectCreationDataModelProperties.FACET_DM_MAP);
 			//facetDMs.add(setupJavaInstallAction(webProj,existing,CreationConstants.DEFAULT_WEB_SOURCE_FOLDER));
 			IDataModel newModel = setupStaticWebInstallAction(project2);
 			facetDMs.add(newModel);
 			//setRuntime(webProj,dm); //Setting runtime property
 			try {
 				/**
 				 * Warning cleanup 12/07/2005
 				 */
 				//IStatus stat =  dm.getDefaultOperation().execute(null,null);
 				dm.getDefaultOperation().execute(null,null);
 			} catch (ExecutionException e) {
 				Throwable realException = e.getCause();
 				if (realException != null && realException instanceof CoreException) {
 					IStatus st = ((CoreException)realException).getStatus();
 					if (st != null)
 						System.out.println(st);
 					realException.printStackTrace();
 				}
 			}
 		}
 
 		private IDataModel setupStaticWebInstallAction(IProject project2) {
 			IDataModel webFacetInstallDataModel = DataModelFactory.createDataModel(new SimpleWebFacetInstallDataModelProvider());
 			webFacetInstallDataModel.setProperty(IFacetDataModelProperties.FACET_PROJECT_NAME, project2.getName());
 			webFacetInstallDataModel.setProperty(IFacetDataModelProperties.FACET_VERSION_STR, "1.0");
 			
 			return webFacetInstallDataModel;
 		}
 
 		private void installUtilityFacets(IProject aProject, String specVersion, boolean existing) {
 			replaceDeployablesOutputIfNecessary(project);
 			IDataModel dm = DataModelFactory.createDataModel(new FacetProjectCreationDataModelProvider());
 			dm.setProperty(IFacetProjectCreationDataModelProperties.FACET_PROJECT_NAME, aProject.getName());
 			FacetDataModelMap facetDMs = (FacetDataModelMap) dm.getProperty(IFacetProjectCreationDataModelProperties.FACET_DM_MAP);
 			facetDMs.add(setupJavaInstallAction(aProject,existing,"src"));
 			IDataModel newModel = setupUtilInstallAction(aProject,specVersion);
 			facetDMs.add(newModel);
 			try {
 				/**
 				 * Warning cleanup 12/07/2005
 				 */
 				//IStatus stat =  dm.getDefaultOperation().execute(null,null);
 				dm.getDefaultOperation().execute(null,null);
 			} catch (ExecutionException e) {
 				Throwable realException = e.getCause();
 				if (realException != null && realException instanceof CoreException) {
 					IStatus st = ((CoreException)realException).getStatus();
 					if (st != null)
 						System.out.println(st);
 					realException.printStackTrace();
 				}
 			}
 			
 		}
 
 		private void installConnectorFacets(IProject aProject, String specVersion, boolean existing) {
 			replaceDeployablesOutputIfNecessary(project);
 			IDataModel dm = DataModelFactory.createDataModel(new FacetProjectCreationDataModelProvider());
 			dm.setProperty(IFacetProjectCreationDataModelProperties.FACET_PROJECT_NAME, aProject.getName());
 			FacetDataModelMap facetDMs = (FacetDataModelMap) dm.getProperty(IFacetProjectCreationDataModelProperties.FACET_DM_MAP);
 			facetDMs.add(setupJavaInstallAction(aProject,existing,CreationConstants.DEFAULT_CONNECTOR_SOURCE_FOLDER));
 			IDataModel newModel = setupConnectorInstallAction(aProject,specVersion);
 			facetDMs.add(newModel);
 			try {
 				/**
 				 * Warning cleanup 12/07/2005
 				 */
 				//IStatus stat =  dm.getDefaultOperation().execute(null,null);
 				dm.getDefaultOperation().execute(null,null);
 			} catch (ExecutionException e) {
 				Throwable realException = e.getCause();
 				if (realException != null && realException instanceof CoreException) {
 					IStatus st = ((CoreException)realException).getStatus();
 					if (st != null)
 						System.out.println(st);
 					realException.printStackTrace();
 				}
 			}
 			
 		}
 
 		private void installEARFacets(IProject aProject, String specVersion, boolean existing) {
 			IDataModel dm = DataModelFactory.createDataModel(new FacetProjectCreationDataModelProvider());
 			dm.setProperty(IFacetProjectCreationDataModelProperties.FACET_PROJECT_NAME, aProject.getName());
 			FacetDataModelMap facetDMs = (FacetDataModelMap) dm.getProperty(IFacetProjectCreationDataModelProperties.FACET_DM_MAP);
 			IDataModel newModel = setupEarInstallAction(aProject,specVersion);
 			facetDMs.add(newModel);
 			try {
 				/**
 				 * Warning cleanup 12/07/2005
 				 */
 				//IStatus stat =  dm.getDefaultOperation().execute(null,null);
 				dm.getDefaultOperation().execute(null,null);
 			} catch (ExecutionException e) {
 				Throwable realException = e.getCause();
 				if (realException != null && realException instanceof CoreException) {
 					IStatus st = ((CoreException)realException).getStatus();
 					if (st != null)
 						System.out.println(st);
 					realException.printStackTrace();
 				}
 			}
 			
 		}
 
 		private void installAppClientFacets(IProject aProject, String specVersion, boolean existing) {
 			replaceDeployablesOutputIfNecessary(project);
 			IDataModel dm = DataModelFactory.createDataModel(new FacetProjectCreationDataModelProvider());
 			dm.setProperty(IFacetProjectCreationDataModelProperties.FACET_PROJECT_NAME, aProject.getName());
 			FacetDataModelMap facetDMs = (FacetDataModelMap) dm.getProperty(IFacetProjectCreationDataModelProperties.FACET_DM_MAP);
 			facetDMs.add(setupJavaInstallAction(aProject,existing,CreationConstants.DEFAULT_APPCLIENT_SOURCE_FOLDER));
 			IDataModel newModel = setupAppClientInstallAction(aProject,specVersion);
 			facetDMs.add(newModel);
 			try {
 				/**
 				 * Warning cleanup 12/07/2005
 				 */
 				//IStatus stat =  dm.getDefaultOperation().execute(null,null);
 				dm.getDefaultOperation().execute(null,null);
 			} catch (ExecutionException e) {
 				Throwable realException = e.getCause();
 				if (realException != null && realException instanceof CoreException) {
 					IStatus st = ((CoreException)realException).getStatus();
 					if (st != null)
 						System.out.println(st);
 					realException.printStackTrace();
 				}
 			}
 			
 		}
 
 		private void installEJBFacets(IProject ejbProject2,String ejbVersion, boolean existing) {
 			replaceDeployablesOutputIfNecessary(project);
 			IDataModel dm = DataModelFactory.createDataModel(new FacetProjectCreationDataModelProvider());
 			dm.setProperty(IFacetProjectCreationDataModelProperties.FACET_PROJECT_NAME, ejbProject2.getName());
 			FacetDataModelMap facetDMs = (FacetDataModelMap) dm.getProperty(IFacetProjectCreationDataModelProperties.FACET_DM_MAP);
 			facetDMs.add(setupJavaInstallAction(ejbProject2,existing,CreationConstants.DEFAULT_EJB_SOURCE_FOLDER));
 			IDataModel newModel = setupEjbInstallAction(ejbProject2,ejbVersion,existing);
 			facetDMs.add(newModel);
 			//setRuntime(ejbProject2,dm); //Setting runtime property
 			try {
 				/**
 				 * Warning cleanup 12/07/2005
 				 */
 				//IStatus stat =  dm.getDefaultOperation().execute(null,null);
 				dm.getDefaultOperation().execute(null,null);
 			} catch (ExecutionException e) {
 				Throwable realException = e.getCause();
 				if (realException != null && realException instanceof CoreException) {
 					IStatus st = ((CoreException)realException).getStatus();
 					if (st != null)
 						System.out.println(st);
 					realException.printStackTrace();
 				}
 			}
 			
 		}
 		private void installWEBFacets(IProject webProj,String specVersion, boolean existing) {
 			removeOldWebContainerIfNecessary(project);
 			replaceDeployablesOutputIfNecessary(project);
 			
 			IDataModel dm = DataModelFactory.createDataModel(new FacetProjectCreationDataModelProvider());
 			dm.setProperty(IFacetProjectCreationDataModelProperties.FACET_PROJECT_NAME, webProj.getName());
 			FacetDataModelMap facetDMs = (FacetDataModelMap) dm.getProperty(IFacetProjectCreationDataModelProperties.FACET_DM_MAP);
 			facetDMs.add(setupJavaInstallAction(webProj,existing,CreationConstants.DEFAULT_WEB_SOURCE_FOLDER));
 			IDataModel newModel = setupWebInstallAction(webProj,specVersion);
 			facetDMs.add(newModel);
 			//setRuntime(webProj,dm); //Setting runtime property
 			try {
 				/**
 				 * Warning cleanup 12/07/2005
 				 */
 				//IStatus stat =  dm.getDefaultOperation().execute(null,null);
 				dm.getDefaultOperation().execute(null,null);
 			} catch (ExecutionException e) {
 				Throwable realException = e.getCause();
 				if (realException != null && realException instanceof CoreException) {
 					IStatus st = ((CoreException)realException).getStatus();
 					if (st != null)
 						System.out.println(st);
 					realException.printStackTrace();
 				}
 			} catch (Exception ex) {
 				if (ex != null && ex instanceof CoreException) {
 					IStatus st = ((CoreException)ex).getStatus();
 					if (st != null)
 						System.out.println(st);
 					ex.printStackTrace();
 				}
 			}
 			
 			
 		}
 		private void replaceDeployablesOutputIfNecessary(IProject proj) {
 
 
 			IJavaProject jproj = JemProjectUtilities.getJavaProject(proj);
 			final IClasspathEntry[] current;
 			try {
 				current = jproj.getRawClasspath();
 				List updatedList = new ArrayList();
 				IPath sourcePath = null;
 				for (int i = 0; i < current.length; i++) {
 					IClasspathEntry entry = current[i];
 					if ((entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) && (entry.getOutputLocation() != null && entry.getOutputLocation().toString().indexOf(OLD_DEPLOYABLES_PATH) != -1)) {
 						sourcePath = entry.getPath();
 						updatedList.add(JavaCore.newSourceEntry(sourcePath));
 					}
 					else
 						updatedList.add(entry);
 				}
 				IClasspathEntry[] updated = (IClasspathEntry[])updatedList.toArray(new IClasspathEntry[updatedList.size()]);
 				jproj.setRawClasspath(updated, null);
 				jproj.save(null, true);
 			} catch (JavaModelException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 		
 			
 		}
 
 		private void removeOldWebContainerIfNecessary(IProject webProj) {
 
 			IJavaProject jproj = JemProjectUtilities.getJavaProject(webProj);
 			final IClasspathEntry[] current;
 			try {
 				current = jproj.getRawClasspath();
 				List updatedList = new ArrayList();
 				for (int i = 0; i < current.length; i++) {
 					IClasspathEntry entry = current[i];
 					if ((entry.getPath().toString().indexOf(WEB_LIB_CONTAINER) == -1) && (entry.getPath().toString().indexOf(WEB_LIB_PATH) == -1))
 						updatedList.add(entry);
 				}
 				IClasspathEntry[] updated = (IClasspathEntry[])updatedList.toArray(new IClasspathEntry[updatedList.size()]);
 				jproj.setRawClasspath(updated, null);
 				jproj.save(null, true);
 			} catch (JavaModelException e) {
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
 		/**@deprecated 
 		 * If this method is not used it should be removed at a later time, marking as deprecated
 		 * Warning cleanup 12/07/2005
 		 */
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
