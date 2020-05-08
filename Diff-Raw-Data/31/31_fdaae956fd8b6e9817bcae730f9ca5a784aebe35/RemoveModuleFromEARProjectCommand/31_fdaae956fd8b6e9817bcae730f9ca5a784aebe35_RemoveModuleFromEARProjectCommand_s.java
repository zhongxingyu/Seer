 /*******************************************************************************
  * Copyright (c) 2003, 2004 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.internal.earcreation;
 
 
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.jst.j2ee.application.Module;
 import org.eclipse.jst.j2ee.componentcore.util.EARArtifactEdit;
 
 
 /**
  * Insert the type's description here. Creation date: (03/29/01 4:48:46 PM)
  * 
  * @author: Administrator
  */
 public class RemoveModuleFromEARProjectCommand extends ModuleInEARProjectCommand {
 	public RemoveModuleFromEARProjectCommand(EARArtifactEdit earArtifactEdit, 
 			Module module, IProject anEarProject) {
 		super(earArtifactEdit);
 		setEarProject(anEarProject);
 		setModuleUri(module.getUri());
 		setModuleAltDD(module.getAltDD());
 		setModule(module);
 	}
 
 	public boolean canExecute() {
 		return super.canExecute();
 	}
 
 	protected void primExecute() {
 		//Required for validate edit support
 		refreshModule();
 		// Set up nestedJ2EEProject before the map removed
 		// It is needed for undo remove
 //		if (editModel != null) {
 //			ModuleMapping map = editModel.getModuleMapping(module);
 //			if (map != null)
 //				setNestedJ2EEProject(ResourcesPlugin.getWorkspace().getRoot().getProject(map.getProjectName()));
 //		}
 //		removeModuleMapping();
 //		if (mapSuccessful)
 			removeModule();
 	}
 
 	/*
 	 * If this was a deferred command, then the module object might be stale because a file was
 	 * checked out
 	 */
 	protected void refreshModule() {
 		if (module == null)
 			return;
 		Module m = (Module) EcoreUtil.resolve(module, getApplication());
 		if (m != null)
 			module = m;
 	}
 
 	protected void primUndo() {
 		if (moduleSuccessful) {
 			addModule();
 			moduleSuccessful = false;
 		}
 //		if (mapSuccessful) {
 //			addModuleMapping();
 //			mapSuccessful = false;
 //		}
 	}
 
 	protected void removeModule() {
 
 		super.removeModule();
 		moduleSuccessful = true;
 	}
 
 //	protected void removeModuleMapping() {
 //		super.removeModuleMapping();
 //		mapSuccessful = true;
 //	}
 
 	// override
 	public String getLabel() {
 		return label == null ? EARCreationResourceHandler.getString("Remove_Module_Command_Label_UI_") : label;//$NON-NLS-1$
 	}
 }
