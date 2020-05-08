 package org.eclipse.jst.j2ee.jca.project.facet;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jem.util.emf.workbench.ProjectUtilities;
 import org.eclipse.jem.util.logger.proxy.Logger;
 import org.eclipse.jst.common.project.facet.WtpUtils;
 import org.eclipse.jst.common.project.facet.core.ClasspathHelper;
 import org.eclipse.jst.j2ee.application.ApplicationPackage;
 import org.eclipse.jst.j2ee.application.Module;
 import org.eclipse.jst.j2ee.application.internal.operations.AddComponentToEnterpriseApplicationDataModelProvider;
 import org.eclipse.jst.j2ee.application.internal.operations.AddComponentToEnterpriseApplicationOp;
 import org.eclipse.jst.j2ee.application.internal.operations.IAddComponentToEnterpriseApplicationDataModelProperties;
 import org.eclipse.jst.j2ee.internal.J2EEConstants;
 import org.eclipse.jst.j2ee.internal.common.J2EEVersionUtil;
 import org.eclipse.jst.j2ee.jca.modulecore.util.ConnectorArtifactEdit;
 import org.eclipse.jst.j2ee.project.facet.IJ2EEModuleFacetInstallDataModelProperties;
 import org.eclipse.jst.j2ee.project.facet.J2EEFacetInstallDelegate;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.datamodel.properties.ICreateReferenceComponentsDataModelProperties;
 import org.eclipse.wst.common.componentcore.datamodel.properties.IFacetDataModelProperties;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;
 import org.eclipse.wst.common.frameworks.datamodel.DataModelFactory;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModelOperation;
 import org.eclipse.wst.common.project.facet.core.IDelegate;
 import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
 
 public class ConnectorFacetInstallDelegate extends J2EEFacetInstallDelegate implements IDelegate {
 
 	public void execute(IProject project, IProjectFacetVersion fv, Object config, IProgressMonitor monitor) throws CoreException {
 		if (monitor != null) {
 			monitor.beginTask("", 1);
 		}
 
 		try {
 			IDataModel model = (IDataModel) config;
 
 			final IJavaProject jproj = JavaCore.create(project);
 
 			// Add WTP natures.
 
 			WtpUtils.addNatures(project);
 
 			// Create the directory structure.
 
 			final IWorkspace ws = ResourcesPlugin.getWorkspace();
 			final IPath pjpath = project.getFullPath();
 
 			// Setup the flexible project structure.
 
 			final IVirtualComponent c = ComponentCore.createComponent(project);
 
 			c.create(0, null);
 			c.setMetaProperty("java-output-path", "/build/classes/");
 
 
 
 			final IVirtualFolder root = c.getRootFolder();
 			IFolder sourceFolder = null;
 			String configFolder = null;
 			if (root.getProjectRelativePath().segmentCount() == 0) {
 				configFolder = model.getStringProperty(IJ2EEModuleFacetInstallDataModelProperties.CONFIG_FOLDER);
 				root.createLink(new Path("/" + configFolder), 0, null);
 
 
 				String configFolderName = model.getStringProperty(IJ2EEModuleFacetInstallDataModelProperties.CONFIG_FOLDER);
 				IPath configFolderpath = pjpath.append(configFolderName);
 
 				sourceFolder = ws.getRoot().getFolder(configFolderpath);
 			} else
 				sourceFolder = project.getFolder(root.getProjectRelativePath());
 
 			if (!sourceFolder.getFile(J2EEConstants.RAR_DD_URI).exists()) {
 				String ver = model.getStringProperty(IFacetDataModelProperties.FACET_VERSION_STR);
 				int nVer = J2EEVersionUtil.convertVersionStringToInt(ver);
 				ConnectorArtifactEdit.createDeploymentDescriptor(project, nVer);
 			}
 
 			try {
 				createManifest(project, sourceFolder, monitor);
 			} catch (InvocationTargetException e) {
 				Logger.getLogger().logError(e);
 			} catch (InterruptedException e) {
 				Logger.getLogger().logError(e);
 			}
 
 			// Setup the classpath.
 			ClasspathHelper.removeClasspathEntries(project, fv);
 
 			if (!ClasspathHelper.addClasspathEntries(project, fv)) {
 				// TODO: Support the no runtime case.
 				// ClasspathHelper.addClasspathEntries( project, fv, <something> );
 			}
 
 			// Associate with an EAR, if necessary.
 
 			final String earProjectName = (String) model.getProperty(IJ2EEModuleFacetInstallDataModelProperties.EAR_PROJECT_NAME);
 			if (earProjectName != null && !earProjectName.equals("")) {
 
 				String ver = fv.getVersionString();
 				String j2eeVersionText = J2EEVersionUtil.convertVersionIntToString(J2EEVersionUtil.convertWebVersionStringToJ2EEVersionID(ver));
 				installEARFacet(j2eeVersionText, earProjectName, monitor);
 
 				IProject earProject = ProjectUtilities.getProject(earProjectName);
 				IVirtualComponent earComp = ComponentCore.createComponent(earProject);
 
 				final IDataModel dataModel = DataModelFactory.createDataModel(new AddComponentToEnterpriseApplicationDataModelProvider() {
 					public Object getDefaultProperty(String propertyName) {
 						if (IAddComponentToEnterpriseApplicationDataModelProperties.TARGET_COMPONENTS_TO_URI_MAP.equals(propertyName)) {
 							Map map = new HashMap();
 							List components = (List) getProperty(TARGET_COMPONENT_LIST);
 							for (int i = 0; i < components.size(); i++) {
 								IVirtualComponent component = (IVirtualComponent) components.get(i);
 								String name = component.getName();
 								name += ".rar"; //$NON-NLS-1$
 								map.put(component, name);
 							}
 							return map;
 						}
 						return super.getDefaultProperty(propertyName);
 					}
 
 					public IDataModelOperation getDefaultOperation() {
 						return new AddComponentToEnterpriseApplicationOp(model) {
 							protected Module createNewModule(IVirtualComponent wc) {
 								return ((ApplicationPackage) EPackage.Registry.INSTANCE.getEPackage(ApplicationPackage.eNS_URI)).getApplicationFactory().createConnectorModule();
 							}
 						};
 					}
 				});
 				dataModel.setProperty(ICreateReferenceComponentsDataModelProperties.SOURCE_COMPONENT, earComp);
 				List modList = (List) dataModel.getProperty(ICreateReferenceComponentsDataModelProperties.TARGET_COMPONENT_LIST);
 				modList.add(c);
 				dataModel.setProperty(ICreateReferenceComponentsDataModelProperties.TARGET_COMPONENT_LIST, modList);
 				try {
 					dataModel.getDefaultOperation().execute(null, null);
 				} catch (ExecutionException e) {
 					Logger.getLogger().logError(e);
 				}
 			}
 
 			if (monitor != null) {
 				monitor.worked(1);
 			}
 		}
 
 		finally {
 			if (monitor != null) {
 				monitor.done();
 			}
 		}
 
 	}
 
 }
