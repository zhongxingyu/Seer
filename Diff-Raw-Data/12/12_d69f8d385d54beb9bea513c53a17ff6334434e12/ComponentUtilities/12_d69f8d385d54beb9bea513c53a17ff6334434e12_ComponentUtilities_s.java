 package org.eclipse.jst.common.componentcore.util;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourceAttributes;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
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
 import org.eclipse.wst.common.componentcore.internal.util.ArtifactEditRegistryReader;
 import org.eclipse.wst.common.componentcore.internal.util.IArtifactEditFactory;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;
 import org.eclipse.wst.common.componentcore.resources.IVirtualResource;
 
 public class ComponentUtilities {
 
 	public static IPackageFragmentRoot[] getSourceContainers(IVirtualComponent wc) {
 		 List sourceFolders = new ArrayList();
 		 try {
 		 IVirtualResource[] resources = wc.members();
 		 if(resources != null || resources.length > 0) {
 			 for(int i = 0; i < resources.length; i++) {
 				 if(resources[i].getType() == IVirtualResource.FOLDER) {
 				 IFolder folder = ((IVirtualFolder)resources[i]).getUnderlyingFolder();
 				 IJavaElement element = JavaCore.create(folder);
 				 if(element != null && element.getElementType() == IJavaElement.PACKAGE_FRAGMENT_ROOT)
 				    sourceFolders.add(element);
 				 }
 			 }
 		   }
 		 } catch (CoreException ce) {
 		 Logger.getLogger().log(ce);
 		 }
 		 return (IPackageFragmentRoot[]) sourceFolders.toArray(new IPackageFragmentRoot[sourceFolders.size()]);
 	}
	
	
	
 
 	/**
 	 * Ensure the container is not read-only.
 	 * <p>
 	 * For Linux, a Resource cannot be created in a ReadOnly folder. This is only necessary for new files.
 	 * 
 	 * @param resource
 	 *            workspace resource to make read/write
 	 * @since 1.0.0
 	 */
 	public static void ensureContainerNotReadOnly(IResource resource) {
 		if (resource != null && !resource.exists()) { //it must be new
 			IContainer container = resource.getParent(); 
 			if (container != null ) {
 				ResourceAttributes attr = container.getResourceAttributes();
 				if(!attr.isReadOnly())
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
 
 
 
 
 	public static IVirtualComponent findComponent(EObject anObject) {
 	WorkbenchComponent module = null;
 		IProject project = ProjectUtilities.getProject(anObject);
 		StructureEdit moduleCore = null;
 		try {
 			moduleCore = StructureEdit.getStructureEditForRead(project);
 			URI uri = WorkbenchResourceHelperBase.getNonPlatformURI(anObject.eResource().getURI());
 			ComponentResource[] resources = moduleCore.findResourcesBySourcePath(uri);
 			for (int i=0; i<resources.length; i++) {
 				module = resources[i].getComponent();
 				if (module !=null)
 					break;
 			}
 		} catch (UnresolveableURIException e) {
 			//Ignore
 		} finally {
 			if (moduleCore !=null)
 				moduleCore.dispose();
 		}
 	return ComponentCore.createComponent(project,module.getName());
 	}
 
 
 
 
 	public static ArtifactEdit getArtifactEditForWrite(IVirtualComponent comp) {
 		ArtifactEditRegistryReader reader = ArtifactEditRegistryReader.instance();
 		IArtifactEditFactory factory = reader.getArtifactEdit(comp.getComponentTypeId());
 		return factory.createArtifactEditForWrite(comp);
 	}
 
 }
