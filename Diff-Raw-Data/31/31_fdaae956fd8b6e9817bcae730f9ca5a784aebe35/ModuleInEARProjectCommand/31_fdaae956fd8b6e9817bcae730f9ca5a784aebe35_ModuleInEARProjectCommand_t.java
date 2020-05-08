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
 
 
 import org.eclipse.core.commands.Command;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.emf.common.command.AbstractCommand;
 import org.eclipse.jst.j2ee.application.Application;
 import org.eclipse.jst.j2ee.application.Module;
 import org.eclipse.jst.j2ee.componentcore.util.EARArtifactEdit;
 
 
 /**
  * Insert the type's description here. Creation date: (03/29/01 4:48:46 PM)
  * 
  * @author: Administrator
  */
 public abstract class ModuleInEARProjectCommand extends AbstractCommand {
 	protected EARArtifactEdit earArtifactEdit;
 	protected IProject earProject;
 	protected IProject nestedJ2EEProject;
 	protected String moduleUri;
 	protected String moduleAltDD;
 	protected String moduleContextRoot;
 	protected boolean moduleSuccessful;
 	protected boolean mapSuccessful;
 	protected Module module;
 
 	/**
 	 * AddModuleToEARProjectCommand constructor comment.
 	 */
	protected ModuleInEARProjectCommand() {
		super();
	}

	/**
	 * AddModuleToEARProjectCommand constructor comment.
	 */
 	protected ModuleInEARProjectCommand(EARArtifactEdit earArtifactEdit) {
 		super();
 		this.earArtifactEdit = earArtifactEdit;
 	}
 
 	/**
 	 * AddModuleToEARProjectCommand constructor comment.
 	 * 
 	 * @param label
 	 *            java.lang.String
 	 */
 	protected ModuleInEARProjectCommand(String label) {
 		super(label);
 	}
 
 	/**
 	 * AddModuleToEARProjectCommand constructor comment.
 	 * 
 	 * @param label
 	 *            java.lang.String
 	 * @param description
 	 *            java.lang.String
 	 */
 	protected ModuleInEARProjectCommand(String label, String description) {
 		super(label, description);
 	}
 
 	public ModuleInEARProjectCommand(IProject aNestedJ2EEProject, IProject anEarProject, String uri, String contextRoot, String altDD) {
 		super();
 		setNestedJ2EEProject(aNestedJ2EEProject);
 		setEarProject(anEarProject);
 		setModuleUri(uri);
 		setModuleContextRoot(contextRoot);
 		setModuleAltDD(altDD);
 	}
 
 	protected void addModule() {
 		Application dd = getApplication();
 		if (dd == null) {
 			return;
 		}
 		Module m = dd.getFirstModule(moduleUri);
 //		if (m == null) {
 //			J2EEModuleNature j2eeNature = (J2EEModuleNature) J2EENature.getRegisteredRuntime(getNestedJ2EEProject());
 //			m = j2eeNature.createNewModule();
 //
 //			if (m == null)
 //				return;
 //			m.setUri(moduleUri);
 //			m.setAltDD(moduleAltDD);
 //			if (m instanceof WebModule) {
 //				((WebModule) m).setContextRoot(moduleContextRoot);
 //			}
 //			dd.getModules().add(m);
 //		}
 		setModule(m);
 	}
 
	protected void addModuleMapping() {
 //		if (module != null) {
 //			ModuleMapping map = editModel.addModuleMapping(module, getNestedJ2EEProject());
 //			mapSuccessful = (map != null);
 //		}
	}
 
 	public void dispose() {
 		setNestedJ2EEProject(null);
 		setEarProject(null);
 	}
 
 	/**
 	 * @see Command
 	 */
 	public void execute() {
 //		try {
 //			setupEditModel();
 //			if (editModel != null) {
 				primExecute();
 //				editModel.saveIfNecessary(this);
 //			} else
 //				moduleSuccessful = false;
 //		} finally {
 //			releaseEditModel();
 //		}
 	}
 
 	protected Application getApplication() {
 		return earArtifactEdit.getApplication();
 	}
 
 	/**
 	 * Insert the method's description here. Creation date: (03/29/01 5:34:26 PM)
 	 * 
 	 * @return org.eclipse.core.resources.IProject
 	 */
 	public org.eclipse.core.resources.IProject getEarProject() {
 		return earProject;
 	}
 
 	/**
 	 * Insert the method's description here. Creation date: (3/29/2001 11:44:01 PM)
 	 * 
 	 * @return org.eclipse.jst.j2ee.internal.internal.application.Module
 	 */
 	public Module getModule() {
 		return module;
 	}
 
 	/**
 	 * Insert the method's description here. Creation date: (03/29/01 5:34:26 PM)
 	 * 
 	 * @return java.lang.String
 	 */
 	public java.lang.String getModuleAltDD() {
 		return moduleAltDD;
 	}
 
 	/**
 	 * Insert the method's description here. Creation date: (03/29/01 5:34:26 PM)
 	 * 
 	 * @return java.lang.String
 	 */
 	public java.lang.String getModuleContextRoot() {
 		return moduleContextRoot;
 	}
 
 	/**
 	 * Insert the method's description here. Creation date: (03/29/01 5:34:26 PM)
 	 * 
 	 * @return java.lang.String
 	 */
 	public java.lang.String getModuleUri() {
 		return moduleUri;
 	}
 
 	protected EARNatureRuntime getNature() {
 		return EARNatureRuntime.getRuntime(getEarProject());
 	}
 
 	/**
 	 * Insert the method's description here. Creation date: (03/29/01 5:34:26 PM)
 	 * 
 	 * @return org.eclipse.core.resources.IProject
 	 */
 	public org.eclipse.core.resources.IProject getNestedJ2EEProject() {
 		return nestedJ2EEProject;
 	}
 
 	public boolean isExecuteSuccess() {
 		return moduleSuccessful && mapSuccessful;
 	}
 
 	protected boolean prepare() {
 		return true;
 	}
 
 	protected abstract void primExecute();
 
 	protected abstract void primUndo();
 
 	/**
 	 * @see Command
 	 */
 	public void redo() {
 		execute();
 	}
 
 //	protected void releaseEditModel() {
 //		if (editModel != null) {
 //			editModel.releaseAccess(this);
 //			editModel = null;
 //		}
 //	}
 
 	protected void removeModule() {
 		getApplication().getModules().remove(getModule());
 	}
 
	protected void removeModuleMapping() {
 //
 //		org.eclipse.jst.j2ee.internal.earcreation.modulemap.ModuleMapping map = editModel.getModuleMapping(module);
 //		if (map != null)
 //			editModel.getModuleMappings().remove(map);
 //
	}
 
 	/**
 	 * Insert the method's description here. Creation date: (03/29/01 5:34:26 PM)
 	 * 
 	 * @param newEarProject
 	 *            org.eclipse.core.resources.IProject
 	 */
 	public void setEarProject(org.eclipse.core.resources.IProject newEarProject) {
 		earProject = newEarProject;
 	}
 
 	/**
 	 * Insert the method's description here. Creation date: (3/29/2001 11:44:01 PM)
 	 * 
 	 * @param newAddedModule
 	 *            org.eclipse.jst.j2ee.internal.internal.application.Module
 	 */
 	public void setModule(Module module) {
 		this.module = module;
 	}
 
 	/**
 	 * Insert the method's description here. Creation date: (03/29/01 5:34:26 PM)
 	 * 
 	 * @param newModuleUri
 	 *            java.lang.String
 	 */
 	public void setModuleAltDD(java.lang.String newModuleAltDD) {
 		moduleAltDD = newModuleAltDD;
 	}
 
 	/**
 	 * Insert the method's description here. Creation date: (03/29/01 5:34:26 PM)
 	 * 
 	 * @param newModuleUri
 	 *            java.lang.String
 	 */
 	public void setModuleContextRoot(java.lang.String newModuleContextRoot) {
 		moduleContextRoot = newModuleContextRoot;
 	}
 
 	/**
 	 * Insert the method's description here. Creation date: (03/29/01 5:34:26 PM)
 	 * 
 	 * @param newModuleUri
 	 *            java.lang.String
 	 */
 	public void setModuleUri(java.lang.String newModuleUri) {
 		moduleUri = newModuleUri;
 	}
 
 	/**
 	 * Insert the method's description here. Creation date: (03/29/01 5:34:26 PM)
 	 * 
 	 * @param newNestedJ2EEProject
 	 *            org.eclipse.core.resources.IProject
 	 */
 	public void setNestedJ2EEProject(org.eclipse.core.resources.IProject newNestedJ2EEProject) {
 		nestedJ2EEProject = newNestedJ2EEProject;
 	}
 
 //	protected void setupEditModel() {
 //		EARNatureRuntime nature = getNature();
 //		if (nature != null)
 //			editModel = nature.getEarEditModelForWrite(this);
 //		else
 //			editModel = null; // arived here with a valid project, but not an EAR nature.
 //	}
 
 	public void undo() {
 //		try {
 //			setupEditModel();
 			primUndo();
 //			editModel.saveIfNecessary(this);
 //		} finally {
 //			releaseEditModel();
 //		}
 	}
 }
