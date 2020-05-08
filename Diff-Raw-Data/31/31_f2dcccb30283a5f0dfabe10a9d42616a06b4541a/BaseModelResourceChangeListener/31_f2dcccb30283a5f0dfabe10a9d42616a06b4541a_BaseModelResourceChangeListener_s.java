 package com.vectorsf.jvoice.base.model.listener;
 
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceChangeListener;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.runtime.CoreException;
 
 import com.vectorsf.jvoice.base.model.service.BaseModel;
 import com.vectorsf.jvoice.model.base.JVModel;
 import com.vectorsf.jvoice.model.base.JVProject;
 
 public class BaseModelResourceChangeListener implements IResourceChangeListener{
 	
 		@Override
 		public void resourceChanged(IResourceChangeEvent event) {
 			boolean existe = false;
 			JVModel model = null;
 			model = BaseModel.getInstance().getModel();
 			
 			if (event.getDelta() == null) {
 				return;
 			}
 
 			for (IResourceDelta child : event.getDelta().getAffectedChildren()) {
 
 				IResource res = child.getResource();
 				if (res instanceof IProject) {
 					String name = res.getName();
 					// El modelo est� vac�o y se est� creando un proyecto
 					if (model.getProjects().isEmpty()) {
 						model.getProjects().add(BaseModel.getInstance().createProject((IProject) res));
 						break;
 					} else {
 						JVProject prj = model.getProject(name);
 						if (prj!=null){
 							updatePackages(child, prj);
 							existe = true;
							System.out.println(model.getProjects().size());
 						}
 						// el modelo tiene proyectos, pero no encuentra el solicitado. Es nuevo
 						if (!existe) {
 							model.getProjects().add(BaseModel.getInstance().createProject((IProject) res));
 						}
 					}
 
 				}
 			}
 
 		}
 		
 		public void updatePackages(IResourceDelta delta, JVProject prj) {
 			if (delta != null) {
 				CheckDeltaChange visitor = new CheckDeltaChange(prj, null);
 				try {
 					delta.accept(visitor);
 				} catch (CoreException e) {
 				}
 
 			}
 
 		}
 }
