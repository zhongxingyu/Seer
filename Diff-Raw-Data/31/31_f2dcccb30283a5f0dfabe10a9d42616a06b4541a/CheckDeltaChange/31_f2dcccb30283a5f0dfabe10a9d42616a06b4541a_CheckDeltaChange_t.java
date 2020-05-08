 package com.vectorsf.jvoice.base.model.listener;
 
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IResourceDeltaVisitor;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 
 import com.vectorsf.jvoice.base.model.service.BaseModel;
 import com.vectorsf.jvoice.model.base.Configuration;
 import com.vectorsf.jvoice.model.base.JVBean;
 import com.vectorsf.jvoice.model.base.JVModel;
 import com.vectorsf.jvoice.model.base.JVPackage;
 import com.vectorsf.jvoice.model.base.JVProject;
 
 public class CheckDeltaChange implements IResourceDeltaVisitor {
 
 	private JVProject jvProject;
 	private JVPackage currentPackage;
 	private IPath pkgPath = new Path("src/main/resources/jv");
 	private IPath configPath = new Path("src/main/config/properties");
 
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
 		boolean existePaquete = false;
 		IResource resource = delta.getResource();
 		switch (resource.getType()) {
 			case IResource.FOLDER:
 				// SI es un paquete de configuraci�n no debe aparecer en la lista
 				if (isValidFolder(resource) && !isConfigurantionFolder(resource)) {
 					IPath relPath = getRelativePath(resource);
 					// verificamos si el proyecto tiene paquetes
 					if (jvProject.getPackages().isEmpty()) {
 						// es un proyecto vac�o, hay que a�adirle el paquete
 						jvProject.getPackages().add(BaseModel.getInstance().createPackage((IFolder) resource));
 						return false;
 					}
 					JVPackage pkg = jvProject.getPackage(relPath.toString().replace("/", "."));
 					if (pkg!=null){
 						existePaquete = true;
 						currentPackage = pkg;
 						if (isInterestingDelta(delta))
 							verificarBorrado(delta.getKind(), resource);
 					}
 					// hemos recorrido los paquete del proyecto pero no encuentra el actual, es nuevo y hay que a�adirlo
 					if (!existePaquete) {
 						jvProject.getPackages().add(BaseModel.getInstance().createPackage((IFolder) resource));
 						return true;
 					}
 				}
 				return true;
 			case IResource.FILE:
 				boolean yaExiste = false;
 				if (isInterestingDelta(delta) && currentPackage != null) {
 					// el paquete est� vac�o, as� que hay que a�adir el bean
 					if (currentPackage.getBeans().isEmpty()) {
 						currentPackage.getBeans().add(BaseModel.getInstance().createBean((IFile) resource));
 						return false;
 					}
 					// si es un evento de adici�n al paquete, por culpa del bug de eclipse que lanza el delta 2 veces
 					// tenemos que comprobar que lo hemos agregado ya antes, para no duplicarlo
 					if (delta.getKind() == IResourceDelta.ADDED) {
 						JVBean bean = currentPackage.getBean(resource.getName());
 						if (bean!=null)
 							yaExiste = true;
 						if (!yaExiste) {
 							currentPackage.getBeans().add(BaseModel.getInstance().createBean((IFile) resource));
 							return false;
 						} else {
 							return true;
 						}
 					} else {
 						// se busca el elemento en el paquete y se elimina
 						verificarBorrado(delta.getKind(), resource);
 					}
 					// Se trata de un archivo de configuraci�n
 				} else if (isInterestingDelta(delta) 
 						&& resource.getFileExtension().equalsIgnoreCase("properties")) {
 					// si es un evento de adici�n al paquete, por culpa del bug de eclipse que lanza el delta 2 veces
 					// tenemos que comprobar que lo hemos agregado ya antes, para no duplicarlo
 					if (delta.getKind() == IResourceDelta.ADDED) {
 						String name = resource.getName().substring(0, resource.getName().lastIndexOf('.'));
 						Configuration config = jvProject.getConfiguration(name);
 						if(config!=null)
 							yaExiste = true;
 						if (!yaExiste) {
 							jvProject.getConfiguration().add(BaseModel.getInstance().createConfigurationFromFile((IFile) resource));
 							return false;
 						} else
 							return true;
 					} else if (delta.getKind() == IResourceDelta.REMOVED) {
 						String name = resource.getName().substring(0, resource.getName().lastIndexOf('.'));
 						Configuration config = jvProject.getConfiguration(name);
 						if (config!=null)
 							jvProject.getConfiguration().remove(config);
					} else if (delta.getKind() == IResourceDelta.CHANGED) {
						String name = resource.getName().substring(0, resource.getName().lastIndexOf('.'));
						Configuration config = jvProject.getConfiguration(name);
						if (config!=null){
							IFile fichero = (IFile) resource;
							Properties pr = new Properties();
							try {
								pr.load(fichero.getContents());
							} catch (IOException e) {
								throw new CoreException(null);
							}

							config.getParameters().clear();
							for (String etiqueta : pr.stringPropertyNames()) {
								config.getParameters().put(etiqueta, pr.getProperty(etiqueta));
							}
						}
						return false;
 					}
 				}
 
 				return true;
 			case IResource.PROJECT:
 				return verificarBorrado(delta.getKind(), resource);
 			}
 
 			return false;
 		}
 
 		private boolean isInterestingDelta(IResourceDelta delta) {
 			return delta.getKind() == IResourceDelta.ADDED || delta.getKind() == IResourceDelta.REMOVED
 					|| delta.getKind() == IResourceDelta.CHANGED && (delta.getFlags() & IResourceDelta.CONTENT) != 0;
 		}
 
 		private boolean verificarBorrado(int estado, IResource recurso) throws CoreException {
 			if (estado == IResourceDelta.REMOVED) {
 				if (recurso instanceof IFile) {
 					JVBean bean = currentPackage.getBean(recurso.getName());
 					if(bean!=null){
 						currentPackage.getBeans().remove(bean);
 						return false;
 					}
 				} else if (recurso instanceof IFolder) {
 					JVPackage paquete = jvProject.getPackage(recurso.getName());
 					if(paquete!=null){
 						jvProject.getPackages().remove(paquete);
 						return false;
 					}
 				} else if (recurso instanceof IProject) {
 					JVModel model = BaseModel.getInstance().getModel();
 					JVProject proyecto = model.getProject(recurso.getName());
 					if(proyecto!=null){
 						model.getProjects().remove(proyecto);
 						return false;
 					}
 				}
 
 			}
 			return true;
 		}
 
 		private boolean isValidFolder(IResource resource) {
 			if (pkgPath.isPrefixOf(resource.getProjectRelativePath())
 					&& !pkgPath.equals(resource.getProjectRelativePath())) {
 				return true;
 			} else {
 				return false;
 			}
 		}
 
 		private IPath getRelativePath(IResource resource) {
 			if (pkgPath.isPrefixOf(resource.getProjectRelativePath())
 					&& !pkgPath.equals(resource.getProjectRelativePath())) {
 				return resource.getProjectRelativePath().makeRelativeTo(pkgPath);
 			} else {
 				return null;
 			}
 		}
 
 		private boolean isConfigurantionFolder(IResource resource) {
 			if (configPath.isPrefixOf(resource.getProjectRelativePath())
 					&& configPath.equals(resource.getProjectRelativePath())) {
 				return true;
 			}
 			return false;
 		}
 }
