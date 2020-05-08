 /*******************************************************************************
  * Copyright (c) 2008, 2009 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.jst.javaee.ltk.core.change;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 import java.util.concurrent.ExecutionException;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jem.util.emf.workbench.ProjectUtilities;
 import org.eclipse.jst.j2ee.application.internal.operations.UpdateManifestDataModelProperties;
 import org.eclipse.jst.j2ee.application.internal.operations.UpdateManifestDataModelProvider;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.helpers.ArchiveManifest;
 import org.eclipse.jst.j2ee.internal.J2EEConstants;
 import org.eclipse.jst.j2ee.internal.common.CreationConstants;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEPlugin;
 import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
 import org.eclipse.jst.j2ee.model.IModelProvider;
 import org.eclipse.jst.j2ee.model.ModelProviderManager;
 import org.eclipse.jst.j2ee.project.JavaEEProjectUtilities;
 import org.eclipse.jst.javaee.ltk.core.nls.RefactoringResourceHandler;
 import org.eclipse.ltk.core.refactoring.Change;
 import org.eclipse.ltk.core.refactoring.ChangeDescriptor;
 import org.eclipse.ltk.core.refactoring.RefactoringStatus;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.internal.resources.VirtualComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualFile;
 import org.eclipse.wst.common.componentcore.resources.IVirtualReference;
 import org.eclipse.wst.common.frameworks.datamodel.DataModelFactory;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 
 
 public class NonEARModuleReferenceRemoveChange extends Change {
 
 	
 	public NonEARModuleReferenceRemoveChange(IProject referencingEARProject, IProject projectToRemove) {
 		super();
 		this.referencingModuleProject = referencingEARProject;
 		this.projectToRemove = projectToRemove;
 		this.referencingModuleProjectComp = (VirtualComponent)ComponentCore.createComponent(referencingEARProject);
 		cachedRefs = referencingModuleProjectComp.getReferences();
 		this.projectToRemoveComp = ComponentCore.createComponent(projectToRemove);
 	}
 	
 	IProject referencingModuleProject = null;
 	VirtualComponent referencingModuleProjectComp = null;
 	IProject projectToRemove = null;
 	IVirtualComponent projectToRemoveComp = null;
 	IVirtualReference[] cachedRefs = null;
 	@Override
 	public Object getModifiedElement() {
 		return null;
 	}
 
 	@Override
 	public String getName() {
 		
 		String name = NLS.bind(
 				RefactoringResourceHandler.Remove_JavaEE_References,
 				new Object[] {projectToRemove.getName()});
 		
 		name += referencingModuleProject.getName();
 		return name;
 		
 	}
 
 	@Override
 	public void initializeValidationData(IProgressMonitor pm) {
 	
 	}
 
 	@Override
 	public RefactoringStatus isValid(IProgressMonitor pm) throws CoreException,
 			OperationCanceledException {
 		return null;
 	}
 
 	@Override
 	public Change perform(IProgressMonitor pm) throws CoreException {
 		
 		try {
 			removeModuleDependency();
 			if(isEJBClientDeletion()){
 				updateEJBDDWithEJBClientDeletion();
 			}
 		} catch (ExecutionException e) {
 			J2EEPlugin.logError(e);
 		}
 		return null;
 	}
 	
 	public ChangeDescriptor getDescriptor() {
 		return null;
 	}
 	
 	private boolean isEJBClientDeletion(){
 		
 		if(!JavaEEProjectUtilities.isEJBProject(referencingModuleProject))
 			return false;
 		Properties props = referencingModuleProjectComp.getMetaProperties();
 		String clientCompName = props.getProperty(CreationConstants.EJB_CLIENT_NAME);
		if(clientCompName == null || clientCompName.length() == 0){
 			return false;
 		}
 			
 		 if(clientCompName.equals(projectToRemove.getName())){
 			 return true;
 		 }
 	        return false;
 	}
 	
 	
 	/*
 	 * Remove the client JAR entry from the deployment descriptor
 	 * This method is to be used only to remove EJB Client jar entry from
 	 * EJB DD
 	 */
 	private void updateEJBDDWithEJBClientDeletion() {
 		IModelProvider ejbModel = ModelProviderManager.getModelProvider(referencingModuleProject);
         ejbModel.modify(new Runnable() {
             public void run() {
                 IModelProvider writableEjbModel = ModelProviderManager.getModelProvider(referencingModuleProject);
                 Object modelObject = writableEjbModel.getModelObject();
                 
                 if (modelObject instanceof org.eclipse.jst.javaee.ejb.EJBJar) {
                     org.eclipse.jst.javaee.ejb.EJBJar ejbres = (org.eclipse.jst.javaee.ejb.EJBJar) writableEjbModel.getModelObject();
                     if (ejbres != null)
                     	ejbres.setEjbClientJar(null);
                 }
                 else {
                     org.eclipse.jst.j2ee.ejb.EJBJar ejbres = (org.eclipse.jst.j2ee.ejb.EJBJar) writableEjbModel.getModelObject();
                     ejbres.setEjbClientJar(null);
                 }
             	Properties props = referencingModuleProjectComp.getMetaProperties();
             	props.remove(CreationConstants.CLIENT_JAR_URI);
             	props.remove(CreationConstants.EJB_CLIENT_NAME);
             	referencingModuleProjectComp.clearMetaProperties();
             	referencingModuleProjectComp.setMetaProperties(props);
          		
             }
         },null);
 	}
 
 	
 	
 	protected void removeModuleDependency() throws ExecutionException {
 		
 		// create IVirtualComponents for the dependent and the refactored project
 		final IVirtualComponent dependentComp = referencingModuleProjectComp;
 		final IVirtualComponent refactoredComp = projectToRemoveComp;
 		final IProgressMonitor monitor = new NullProgressMonitor();
 		// Does the dependent project have a .component reference on the refactored project?
 		final IVirtualReference ref = hadReference();
 		final boolean webLibDep = hasWebLibDependency(ref);
 		
 		// remove the component reference on the deleted project
 		if (refactoredComp != null) {
 			removeReferencedComponents(monitor);
 		}
 		
 		// update the manifest
 			updateManifestDependency(true);
 	}
 	
 	
 	protected void updateManifestDependency(final boolean remove) throws ExecutionException {
 		final IVirtualComponent dependentComp = referencingModuleProjectComp;
 		IProject project= dependentComp.getProject();
 		if(project.isAccessible()){
 			final String dependentProjName = referencingModuleProject.getName();
 			final String refactoredProjName = projectToRemove.getName();
 			final IVirtualFile vf = dependentComp.getRootFolder().getFile(new Path(J2EEConstants.MANIFEST_URI) );
 			final IFile manifestmf = vf.getUnderlyingFile();
 			// adding this check for https://bugs.eclipse.org/bugs/show_bug.cgi?id=170074
 			// (some adopters have non-jst.ear module projects that are missing manifests)
 			if (!manifestmf.exists()) {  
 				return;
 			}
 			final IProgressMonitor monitor = new NullProgressMonitor();
 			final IDataModel updateManifestDataModel = DataModelFactory.createDataModel(new UpdateManifestDataModelProvider());
 			updateManifestDataModel.setProperty(UpdateManifestDataModelProperties.PROJECT_NAME, dependentProjName);
 			updateManifestDataModel.setBooleanProperty(UpdateManifestDataModelProperties.MERGE, false);
 			updateManifestDataModel.setProperty(UpdateManifestDataModelProperties.MANIFEST_FILE, manifestmf);
 			final ArchiveManifest manifest = J2EEProjectUtilities.readManifest(manifestmf);
 			String[] cp = manifest.getClassPathTokenized();
 			List cpList = new ArrayList();
 			String newCp = refactoredProjName + ".jar";//$NON-NLS-1$
 			for (int i = 0; i < cp.length; i++) {
 				if (!cp[i].equals(newCp)) {
 					cpList.add(cp[i]);
 				}
 			}
 			if (!remove) {
 				cpList.add(newCp);
 			}
 			updateManifestDataModel.setProperty(UpdateManifestDataModelProperties.JAR_LIST, cpList);
 			try {
 				updateManifestDataModel.getDefaultOperation().execute(monitor, null );
 			} catch (org.eclipse.core.commands.ExecutionException e) {
 				J2EEPlugin.logError(e);
 			}
 		}
 	}
 	
 	protected void removeReferencedComponents(IProgressMonitor monitor) {
 		
 		if (referencingModuleProjectComp == null || !referencingModuleProjectComp.getProject().isAccessible() || referencingModuleProjectComp.isBinary()) return;
 		
 		IVirtualReference [] existingReferencesArray = cachedRefs;
 		if(existingReferencesArray == null || existingReferencesArray.length == 0){
 			return;
 		}
 		
 		List existingReferences = new ArrayList();
 		for(int i=0;i<existingReferencesArray.length; i++){
 			existingReferences.add(existingReferencesArray[i]);
 		}
 		
 		List targetprojectList = new ArrayList();
 			if (projectToRemoveComp==null )
 				return;
 
 			IVirtualReference ref = findMatchingReference(existingReferences, projectToRemoveComp, null);
 			//if a ref was found matching the specified deployPath, then remove it
 			if(ref != null){
 				removeRefereneceInComponent(referencingModuleProjectComp, ref);
 				existingReferences.remove(ref);
 				//after removing the ref, check to see if it was the last ref removed to that component
 				//and if it was, then also remove the project reference
 				ref = findMatchingReference(existingReferences, projectToRemoveComp);
 				if(ref == null){
 					IProject targetProject = projectToRemoveComp.getProject();
 					targetprojectList.add(targetProject);
 				}
 			}
 		
 		
 		try {
 			ProjectUtilities.removeReferenceProjects(referencingModuleProjectComp.getProject(),targetprojectList);
 		} catch (CoreException e) {
 			J2EEPlugin.logError(e);
 		}		
 		
 	}
 	
 	private IVirtualReference findMatchingReference(List existingReferences, IVirtualComponent comp) {
 		return findMatchingReference(existingReferences, comp, null);
 	}
 
 	protected void removeRefereneceInComponent(IVirtualComponent component, IVirtualReference reference) {
 		((VirtualComponent)component.getComponent()).removeReference(reference);
 	}
 	
 	private IVirtualReference findMatchingReference(List existingReferences, IVirtualComponent comp, IPath path) {
 		for(int i=0;i<existingReferences.size(); i++){
 			IVirtualReference ref = (IVirtualReference)existingReferences.get(i);
 			IVirtualComponent c = ref.getReferencedComponent();
 			if(c != null && c.getName().equals(comp.getName())){
 				if(path == null){
 					return ref;
 				} else if(path.equals(ref.getRuntimePath())){
 					return ref;
 				}
 			}
 		}
 		return null;
 	}
 	
 	
 	/**
 	 * Does the dependent project have a .component reference on the refactored project?
 	 * @return IVirtualReference or null if one didn't exist.
 	 */
 	protected IVirtualReference hadReference() {
 		final IVirtualComponent refactoredComp = projectToRemoveComp;
 		if (refactoredComp == null) {
 			return null;
 		}
 		final IVirtualReference[] refs = cachedRefs;
 		IVirtualReference ref = null;
 		for (int i = 0; i < refs.length; i++) {
 			if (refs[i].getReferencedComponent().equals(refactoredComp)) {
 				ref = refs[i];
 				break;
 			}
 		}
 		return ref;
 	}
 	
 	/**
 	 * Does the dependent project have a .project reference on the refactored project?
 	 * (dynamic project refs don't count)
 	 * @return True if a project reference exists.
 	 */
 	protected boolean hadProjectReference() {
 		try {
 			final IProject[] refs = referencingModuleProject.getDescription().getReferencedProjects();
 			final IProject refactoredProject= projectToRemove;
 			for (int i = 0; i < refs.length; i++) {
 				if (refs[i].equals(refactoredProject)) {
 					return true;
 				}
 			} 
 		} catch (CoreException ce) {
 			J2EEPlugin.logError(ce);
 		}
 		return false;
 	}
 	
 	/**
 	 * Returns true if the dependency was a web library dependency. 
 	 * @param ref
 	 * @return
 	 */
 	protected static boolean hasWebLibDependency(final IVirtualReference ref) {
 		if (ref == null) {
 			return false;
 		}
 		return ref.getRuntimePath().equals(new Path("/WEB-INF/lib")); //$NON-NLS-1$
 	}
 
 }
