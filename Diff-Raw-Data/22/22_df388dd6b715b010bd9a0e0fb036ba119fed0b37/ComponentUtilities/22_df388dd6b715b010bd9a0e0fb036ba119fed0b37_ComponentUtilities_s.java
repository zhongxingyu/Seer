 package org.eclipse.jst.common.componentcore.util;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourceAttributes;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jem.util.emf.workbench.ProjectUtilities;
 import org.eclipse.jem.util.emf.workbench.WorkbenchResourceHelperBase;
 import org.eclipse.jem.util.logger.proxy.Logger;
 import org.eclipse.wst.common.componentcore.ArtifactEdit;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.UnresolveableURIException;
 import org.eclipse.wst.common.componentcore.internal.ComponentResource;
 import org.eclipse.wst.common.componentcore.internal.StructureEdit;
 import org.eclipse.wst.common.componentcore.internal.WorkbenchComponent;
 import org.eclipse.wst.common.componentcore.internal.resources.VirtualComponent;
 import org.eclipse.wst.common.componentcore.internal.util.ArtifactEditRegistryReader;
 import org.eclipse.wst.common.componentcore.internal.util.IArtifactEditFactory;
 import org.eclipse.wst.common.componentcore.internal.util.IModuleConstants;
 import org.eclipse.wst.common.componentcore.resources.IFlexibleProject;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualContainer;
 import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;
 import org.eclipse.wst.common.componentcore.resources.IVirtualResource;
 
 public class ComponentUtilities {
 	
 	public static String JAVA_NATURE = "org.eclipse.jdt.core.javanature";
 
 	/**
 	 * Retrieve all the source containers for a given virtual workbench component
 	 * 
 	 * @param wc
 	 * @return the array of IPackageFragmentRoots
 	 */
 	public static IPackageFragmentRoot[] getSourceContainers(IVirtualComponent wc) {
 		List sourceFolders = new ArrayList();
 		try {
 			IVirtualResource[] resources = wc.members();
 			//recursively collect the source folders from the top level component folders
 			addSourceContainers(sourceFolders,resources);
 		} catch (CoreException ce) {
 			Logger.getLogger().log(ce);
 		}
 		return (IPackageFragmentRoot[]) sourceFolders.toArray(new IPackageFragmentRoot[sourceFolders.size()]);
 	}
 	
 	private static void addSourceContainers(List sourceFolders, IVirtualResource[] resources) {
 		if (resources != null || resources.length > 0) {
 			for (int i = 0; i < resources.length; i++) {
 				IVirtualResource resource = resources[i];
 				// if the virtual resource is of type folder, check to see if it is source folder
 				if (resource.getType() == IVirtualResource.FOLDER) {
 					IVirtualResource[] childResources = null;
 					try {
 						childResources = ((IVirtualFolder) resource).members();
 					} catch (CoreException ce) {
 						Logger.getLogger().log(ce);
 					} 
 					IFolder folder = ((IVirtualFolder) resource).getUnderlyingFolder();
 					IJavaElement element = JavaCore.create(folder);
 					// if it is a java package fragment add it to the result
 					if (element != null && element.getElementType() == IJavaElement.PACKAGE_FRAGMENT_ROOT)
 						sourceFolders.add(element);
 					// otherwise recursively check that folders child folders
 					else if (childResources.length>0)
 						addSourceContainers(sourceFolders,childResources);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Ensure the container is not read-only.
 	 * <p>
 	 * For Linux, a Resource cannot be created in a ReadOnly folder. This is only necessary for new
 	 * files.
 	 * 
 	 * @param resource
 	 *            workspace resource to make read/write
 	 * @since 1.0.0
 	 */
 	public static void ensureContainerNotReadOnly(IResource resource) {
 		if (resource != null && !resource.exists()) { // it must be new
 			IContainer container = resource.getParent();
 			if (container != null) {
 				ResourceAttributes attr = container.getResourceAttributes();
 				if (!attr.isReadOnly())
 					container = container.getParent();
 				attr.setReadOnly(false);
 			}
 		}
 	}
 
 	public static IFolder createFolderInComponent(IVirtualComponent component, String folderName) throws CoreException {
 		if (folderName != null) {
 			IFolder folder = ResourcesPlugin.getWorkspace().getRoot().getFolder(component.getProjectRelativePath().append(folderName));
 			if (!folder.exists()) {
 				ProjectUtilities.ensureContainerNotReadOnly(folder);
 				folder.create(true, true, null);
 			}
 			return folder;
 		}
 		return null;
 	}
 
 
 
 	public static ArtifactEdit getArtifactEditForRead(IVirtualComponent comp) {
 		ArtifactEditRegistryReader reader = ArtifactEditRegistryReader.instance();
 		IArtifactEditFactory factory = reader.getArtifactEdit(comp.getComponentTypeId());
 		return factory.createArtifactEditForRead(comp);
 	}
 
 
 
 	public static IFile findFile(IVirtualComponent comp, IPath aPath) throws CoreException {
 		IVirtualResource[] members = comp.members();
 		for (int i = 0; i < members.length; i++) {
 			IVirtualResource resource = members[i];
 			if (resource.getType() == IVirtualResource.FOLDER) {
 				IVirtualResource file = ((IVirtualContainer)resource).findMember(aPath);
 				if (file != null) return (IFile)file.getUnderlyingResource();
 			}
 		}
 		return null;
 	}
 
 	private static IVirtualComponent findComponent(IProject project, Resource res) {
 		
 		StructureEdit moduleCore = null;
 		WorkbenchComponent module = null;
 		try {
 			moduleCore = StructureEdit.getStructureEditForRead(project);
 			URI uri = WorkbenchResourceHelperBase.getNonPlatformURI(res.getURI());
 			ComponentResource[] resources = moduleCore.findResourcesBySourcePath(uri);
 			for (int i = 0; i < resources.length; i++) {
 				module = resources[i].getComponent();
 				if (module != null)
 					break;
 			}
 		} catch (UnresolveableURIException e) {
 			// Ignore
 		} finally {
 			if (moduleCore != null)
 				moduleCore.dispose();
 		}
 		return ComponentCore.createComponent(project, module.getName());
 	}

 	public static IVirtualComponent[] getAllWorkbenchComponents() {
 		List components = new ArrayList();
 		List projects = Arrays.asList(ResourcesPlugin.getWorkspace().getRoot().getProjects());
 		for (int i = 0; i < projects.size(); i++) {
 				IFlexibleProject flexProject = ComponentCore.createFlexibleProject((IProject) projects.get(i));
 				IVirtualComponent[] wbComp = flexProject.getComponents();
 				for (int j = 0; j < wbComp.length; j++) {
 					components.add(ComponentCore.createComponent((IProject) projects.get(i), wbComp[j].getName()));
 				}
 		}
 		VirtualComponent[] temp = (VirtualComponent[]) components.toArray(new VirtualComponent[components.size()]);
 		return temp;
 	}
 	
 	public static IVirtualComponent[] getComponentsForProject(IProject project) {
 		IFlexibleProject flexProject = ComponentCore.createFlexibleProject(project);
 		return flexProject.getComponents();
 	}
 
 	public static IVirtualComponent[] getComponent(String componentName) {
 		IVirtualComponent[] allComponents = getAllWorkbenchComponents();
 		List matchingComponents = new ArrayList();
 		for (int i = 0; i < allComponents.length; i++) {
 			if (allComponents[i].getName().equals(componentName)) {
 				matchingComponents.add(allComponents[i]);
 			}
 		}
 		VirtualComponent[] temp = (VirtualComponent[]) matchingComponents.toArray(new VirtualComponent[matchingComponents.size()]);
 		return temp;
 	}
 
 
 
 	public static ArtifactEdit getArtifactEditForWrite(IVirtualComponent comp) {
 		ArtifactEditRegistryReader reader = ArtifactEditRegistryReader.instance();
 		IArtifactEditFactory factory = reader.getArtifactEdit(comp.getComponentTypeId());
 		return factory.createArtifactEditForWrite(comp);
 	}
 	public static IVirtualComponent findComponent(EObject anObject) {
 		IProject project = ProjectUtilities.getProject(anObject);
 		Resource res = anObject.eResource();
 		return findComponent(project, res);
 	}
 
 	public static IVirtualComponent findComponent(Resource aResource) {
 		IProject project = ProjectUtilities.getProject(aResource);
 		return findComponent(project, aResource);
 	}
 	
 	public static List getAllJavaNonFlexProjects() throws CoreException {
 		List nonFlexJavaProjects = new ArrayList();
 		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
 		for (int i = 0; i < projects.length; i++) {
 			if(projects[i].hasNature(JAVA_NATURE) && !projects[i].hasNature(IModuleConstants.MODULE_NATURE_ID)) {
 				nonFlexJavaProjects.add(projects[i]);
 			}
 		}
 		return nonFlexJavaProjects;
 	}
 
 }
