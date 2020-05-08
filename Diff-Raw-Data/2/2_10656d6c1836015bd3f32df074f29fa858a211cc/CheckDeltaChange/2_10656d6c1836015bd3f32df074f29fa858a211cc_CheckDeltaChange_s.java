 package com.vectorsf.jvoice.core.reconciliator;
 
 import java.util.List;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IResourceDeltaVisitor;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 
 import com.vectorsf.jvoice.base.model.service.BaseModel;
 import com.vectorsf.jvoice.core.project.JVoiceProjectNature;
 import com.vectorsf.jvoice.model.base.Configuration;
 import com.vectorsf.jvoice.model.base.JVBean;
 import com.vectorsf.jvoice.model.base.JVPackage;
 import com.vectorsf.jvoice.model.base.JVProject;
 
 public class CheckDeltaChange implements IResourceDeltaVisitor {
 
 	private final static IPath pkgPath = new Path(BaseModel.JV_PATH);
 	private final static IPath configPath = new Path(BaseModel.PROPERTIES_PATH);
 
 	private JVProject jvProject;
 	private JVPackage currentPackage;
 
 	public CheckDeltaChange(JVProject prj, IFolder packageFolder) {
 		jvProject = prj;
 		currentPackage = null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
 	 */
 	@Override
 	public boolean visit(IResourceDelta delta) throws CoreException {
 		IResource resource = delta.getResource();
 		switch (resource.getType()) {
 		case IResource.FOLDER:
 			// SI es un paquete de configuración no debe aparecer en la lista
 			if (isValidFolder(resource) && !isConfigurationFolder(resource)) {
 				IPath relPath = getRelativePath(resource);
 				JVPackage pkg = jvProject.getPackage(relPath.toString().replace("/", "."));
 
 				if (pkg != null) {
 					currentPackage = pkg;
 					if (delta.getKind() == IResourceDelta.REMOVED) {
 						jvProject.getPackages().remove(currentPackage);
 						removeResources(currentPackage);
						return false;
 					}
 				} else {
 					currentPackage = JVoiceModelReconcilier.getInstance().createPackage((IFolder) resource);
 					jvProject.getPackages().add(currentPackage);
 					return true;
 				}
 			}
 			return true;
 		case IResource.FILE:
 			if (isInterestingDelta(delta)) {
 				IPath relPath = getRelativePath(resource.getParent());
 				JVPackage pkg = jvProject.getPackage(relPath.toString().replace("/", "."));
 				if (pkg != null) {
 					URI uri = URI.createPlatformResourceURI(resource.getFullPath().toString(),
 							true);
 
 					final List<JVBean> beans = pkg.getBeans();
 					if (delta.getKind() == IResourceDelta.ADDED) {
 						beans.add(JVoiceModelReconcilier.getInstance().createBean((IFile) resource));
 
 					} else if (delta.getKind() == IResourceDelta.CHANGED) {
 						ResourceSet resourceSet = BaseModel.getInstance().getResourceSet();
 						Resource eResource = resourceSet.getResource(uri, false);
 						
 						if (eResource != null)
 						{
 							JVBean bean = getBeanFromResource(eResource);
 							int index = beans.indexOf(bean);
 							if (index!= -1) {
 								beans.remove(index);
 								eResource.unload();
 								beans.add(index, JVoiceModelReconcilier.getInstance().createBean((IFile) resource));
 							}
 						}
 
 					} else if (delta.getKind() == IResourceDelta.REMOVED) {
 						// se busca el elemento en el paquete y se elimina
 						ResourceSet resourceSet = BaseModel.getInstance().getResourceSet();
 						Resource eResource = resourceSet.getResource(uri, false);
 						if (eResource != null)
 						{
 							beans.remove(getBeanFromResource(eResource));
 							resourceSet.getResources().remove(eResource);
 						}
 
 					}
 					// Se trata de un archivo de configuración
 				} else if (resource.getFileExtension().equalsIgnoreCase("properties")) {
 					if (!isConfigurationFolder(resource.getParent())){
 						return false;
 					}
 					String name = resource.getName().substring(0, resource.getName().lastIndexOf('.'));
 					// si es un evento de adición al paquete, por culpa del bug de eclipse que lanza el delta 2 veces
 					// tenemos que comprobar que lo hemos agregado ya antes, para no duplicarlo
 					if (delta.getKind() == IResourceDelta.ADDED) {
 						Configuration config = jvProject.getConfiguration(name);
 						if (config == null) {
 							jvProject.getConfiguration().add(
 									JVoiceModelReconcilier.getInstance().createConfigurationFromFile((IFile) resource));
 						}
 
 					} else if (delta.getKind() == IResourceDelta.REMOVED) {
 						jvProject.getConfiguration().remove(jvProject.getConfiguration(name));
 
 					} else if (delta.getKind() == IResourceDelta.CHANGED) {
 						Configuration config = jvProject.getConfiguration(name);
 						JVoiceModelReconcilier.getInstance().reloadConfigurationProperties((IFile) resource, config);
 					}
 				}
 			}
 			return true;
 		case IResource.PROJECT:
 			if (delta.getKind() == IResourceDelta.REMOVED) {
 				jvProject.getModel().getProjects().remove(jvProject);
 				removeResources(jvProject);
 
 				return false;
 			}
 			if (delta.getKind() == IResourceDelta.CHANGED && delta.getFlags() == IResourceDelta.DESCRIPTION) {
 				IProject project = (IProject) resource;
 				if (!project.isOpen() || !project.hasNature(JVoiceProjectNature.NATURE_ID)) {
 					jvProject.getModel().getProjects().remove(jvProject);
 					removeResources(jvProject);
 					return false;
 				}
 			}
 		}
 
 		return true;
 	}
 
 	private JVBean getBeanFromResource(Resource eResource) {
 		for(EObject e: eResource.getContents())
 		{
 			if (e instanceof JVBean) {
 				return (JVBean) e;
 			}
 		}
 		return null;
 	}
 
 	private void removeResources(JVProject jvProject) {
 		for (JVPackage pck : jvProject.getPackages()) {
 			removeResources(pck);
 		}
 
 	}
 
 	private void removeResources(JVPackage jvPackage) {
 		ResourceSet resourceSet = BaseModel.getInstance().getResourceSet();
 		for (JVBean bean : jvPackage.getBeans()) {
 			resourceSet.getResources().remove(bean.eResource());
 		}
 
 	}
 
 	private boolean isInterestingDelta(IResourceDelta delta) {
 		return delta.getKind() == IResourceDelta.ADDED || delta.getKind() == IResourceDelta.REMOVED
 				|| delta.getKind() == IResourceDelta.CHANGED && (delta.getFlags() & IResourceDelta.CONTENT) != 0;
 	}
 
 	private boolean isValidFolder(IResource resource) {
 		if (pkgPath.isPrefixOf(resource.getProjectRelativePath()) && !pkgPath.equals(resource.getProjectRelativePath())) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	private IPath getRelativePath(IResource resource) {
 		if (pkgPath.isPrefixOf(resource.getProjectRelativePath()) && !pkgPath.equals(resource.getProjectRelativePath())) {
 			return resource.getProjectRelativePath().makeRelativeTo(pkgPath);
 		} else {
 			return null;
 		}
 	}
 
 	private boolean isConfigurationFolder(IResource resource) {
 		if (configPath.isPrefixOf(resource.getProjectRelativePath())
 				&& configPath.equals(resource.getProjectRelativePath())) {
 			return true;
 		}
 		return false;
 	}
 }
