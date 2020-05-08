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
 /*
  * Created on Mar 11, 2004
  * 
  * To change the template for this generated file go to Window - Preferences -
  * Java - Code Generation - Code and Comments
  */
 package org.eclipse.jst.j2ee.internal.web.operations;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jst.j2ee.internal.J2EEVersionConstants;
 import org.eclipse.jst.j2ee.internal.project.IWebNatureConstants;
 import org.eclipse.jst.j2ee.internal.project.J2EENature;
 import org.eclipse.jst.j2ee.internal.project.J2EESettings;
 import org.eclipse.wst.common.frameworks.internal.WTPProjectUtilities;
 import org.eclipse.wst.common.internal.migration.CompatibilityUtils;
 import org.eclipse.wst.common.internal.migration.IDeprecatedConstants;
 import org.eclipse.wst.common.internal.migration.IMigrator;
 import org.eclipse.wst.web.internal.operation.WebSettings;
 
 import com.ibm.wtp.emf.workbench.ProjectUtilities;
 
 /**
  * @author vijayb
  * 
  * To change the template for this generated type comment go to Window - Preferences - Java - Code
  * Generation - Code and Comments
  */
 public class WebSettingsMigrator implements IMigrator {
 	protected J2EESettings j2eeSettings;
 	protected String WEBSETINGS_KEY = "j2eesettings"; //$NON-NLS-1$
 
 	/**
 	 *  
 	 */
 	public WebSettingsMigrator() {
 		super();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.common.migration.IMigrator#migrate(org.eclipse.core.resources.IProject)
 	 */
 	public boolean migrate(IProject project) {
		migrateProjectFile(project);
 		if (migrateWebSettingsFile(project))
 			return true;
 		return false;
 	}
 
 	/**
 	 * @param project
 	 */
 	private boolean migrateWebSettingsFile(IProject project) {
 		J2EEWebNatureRuntime webNature = J2EEWebNatureRuntime.getRuntime(project);
 		if (webNature != null) {
 			WebSettings webSettings = webNature.getWebSettings();
 			IFile webSettingsFile = webNature.getFile(IWebNatureConstants.WEBSETTINGS_FILE_NAME);
 			try {
 				if (webSettings != null) {
 					boolean j2eeSuccesful = performJ2EESettingsMigration(project, webNature);
 					boolean webSuccessful = performWebSettingsMigration(project, webNature, webSettings, webSettingsFile);
 					return j2eeSuccesful && webSuccessful;
 				}
 			} catch (CoreException ce) {
 				ce.printStackTrace();
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * @param project
 	 * @param webNature
 	 * @param jspVersion
 	 * @return
 	 */
 	private boolean performWebSettingsMigration(IProject project, J2EEWebNatureRuntime webNature, WebSettings webSettings, IFile webSettingsFile) {
 		if (!CompatibilityUtils.isPersistedTimestampCurrent(project, webSettingsFile)) {
 			try {
 				webSettings.setVersion(J2EESettings.CURRENT_VERSION);
 				webSettings.setProjectType("J2EE"); //$NON-NLS-1$
 				webSettings.setWebContentName(getWebContentOutputFolderName(project));
 				webSettings.setContextRoot(webNature.getContextRoot());
 				String jspLevel = getJSPLevel(webNature);
 				if (jspLevel != null) {
 					webSettings.setJSPLevel(jspLevel);
 				}
 				webSettings.write();
 				CompatibilityUtils.updateTimestamp(project, webSettingsFile);
 				return true;
 			} catch (CoreException ce) {
 				ce.printStackTrace();
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * @param servletLevel
 	 * @return
 	 */
 	private String getJSPLevel(J2EEWebNatureRuntime webNature) {
 		String version = webNature.getModuleVersionText();
 		if (version.equals(J2EEVersionConstants.VERSION_2_2_TEXT))
 			return J2EEVersionConstants.VERSION_1_1_TEXT;
 		else if (version.equals(J2EEVersionConstants.VERSION_2_3_TEXT))
 			return J2EEVersionConstants.VERSION_1_2_TEXT;
 		else if (version.equals(J2EEVersionConstants.VERSION_2_4_TEXT))
 			return J2EEVersionConstants.VERSION_1_3_TEXT;
 		return null;
 	}
 
 	/**
 	 * @param project
 	 * @param webNature
 	 * @param jspVersion
 	 * @return
 	 * @throws CoreException
 	 */
 	private boolean performJ2EESettingsMigration(IProject project, J2EEWebNatureRuntime webNature) throws CoreException {
 
 		IFile j2eeSettingsFile = project.getFile(J2EESettings.J2EE_SETTINGS_FILE_NAME);
 		if (!CompatibilityUtils.isPersistedTimestampCurrent(project, j2eeSettingsFile)) {
 			if (j2eeSettingsFile == null && j2eeSettingsFile.exists())
 				j2eeSettings = getJ2EESettings(project);
 			else
 				j2eeSettings = new J2EESettings(project, webNature);
 			j2eeSettings.setVersion(J2EESettings.CURRENT_VERSION);
 			String version = getJSPLevel(webNature);
 			if (version.equals(J2EEVersionConstants.VERSION_1_1_TEXT))
 				j2eeSettings.setModuleVersion(J2EEVersionConstants.VERSION_2_2);
 			else if (version.equals(J2EEVersionConstants.VERSION_1_2_TEXT))
 				j2eeSettings.setModuleVersion(J2EEVersionConstants.VERSION_2_3);
 			else if (version.equals(J2EEVersionConstants.VERSION_1_3_TEXT))
 				j2eeSettings.setModuleVersion(J2EEVersionConstants.VERSION_2_4);
 			j2eeSettings.write();
 			CompatibilityUtils.updateTimestamp(project, j2eeSettingsFile);
 			return true;
 		}
 		return false;
 	}
 
 	protected J2EESettings getJ2EESettings(IProject project) {
 		return new J2EESettings(project);
 	}
 
 	private String getWebContentOutputFolderName(IProject project) {
 		IPath outputPath = ProjectUtilities.getJavaProjectOutputLocation(project);
 		String[] folders = outputPath.segments();
 		for (int i = 0; i < folders.length; i++) {
 			IContainer container = project.getFolder(folders[i]);
 			IResource resource = container.findMember(new Path(IWebNatureConstants.INFO_DIRECTORY + "/" + IWebNatureConstants.DEPLOYMENT_DESCRIPTOR_FILE_NAME)); //$NON-NLS-1$
 			if (resource != null) {
 				return container.getFullPath().removeFirstSegments(1).toString();
 			}
 
 		}
 		return ""; //$NON-NLS-1$
 	}
 
 	private void migrateProjectFile(IProject project) {
 		try {
 			J2EENature nature = J2EENature.getRegisteredRuntime(project);
 			int j2eeVersion = nature.getJ2EEVersion();
 			if (j2eeVersion != J2EEVersionConstants.J2EE_1_4_ID) {
 
 				if (project.hasNature(IWebNatureConstants.J2EE_NATURE_ID)) {
 					//WTPProjectUtilities.addOldNatureToProject( project,
 					// IDeprecatedConstants.WEBNATURE );
 					WTPProjectUtilities.addOldNatureToProject(project, IDeprecatedConstants.WEBNATURE, 1);
 					ProjectUtilities.addToBuildSpec(IDeprecatedConstants.LIBCOPYBUILDER, project);
 				}
 			}
 		} catch (CoreException ce) {
 			ce.printStackTrace();
 		}
 	}
 }
