 /**********************************************************************
  * Copyright (c) 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
 *
  * Contributors:
  *    IBM Corporation - Initial API and implementation
  **********************************************************************/
 package org.eclipse.jst.server.tomcat.core.internal;
 
 import org.eclipse.osgi.util.NLS;
 /**
  * Translated messages.
  */
 public class Messages extends NLS {
 	public static String copyingTask;
 	public static String errorCopyingFile;
 	public static String deletingTask;
 	public static String errorInstallDir;
 	public static String errorInstallDirTrailingSlash;
 	public static String errorJRE;
 	public static String warningJRE;
 	public static String target32runtime;
 	public static String target40runtime;
 	public static String target41runtime;
 	public static String target50runtime;
 	public static String target55runtime;
 	public static String portUnknown;
 	public static String loadingTask;
 	public static String errorCouldNotLoadConfiguration;
 	public static String savingTask;
 	public static String errorCouldNotSaveConfiguration;
 	public static String updatingConfigurationTask;
 	public static String canAddModule;
 	public static String errorSpec32;
 	public static String errorSpec40;
 	public static String errorSpec41;
 	public static String errorSpec50;
 	public static String errorSpec55;
 	public static String portServer;
 	public static String runtimeDirPrepared;
 	public static String publishConfigurationTask;
 	public static String publishContextConfigTask;
 	public static String savingContextConfigTask;
 	public static String checkingContextTask;
 	public static String serverPostProcessingComplete;
 	public static String errorPublishConfiguration;
 	public static String cleanupServerTask;
 	public static String detectingRemovedProjects;
 	public static String deletingContextFilesTask;
 	public static String deletingContextFile;
 	public static String deletedContextFile;
 	public static String errorCouldNotDeleteContextFile;
 	public static String errorCleanupServer;
 	public static String publisherPublishTask;
 	public static String errorNoConfiguration;
 	public static String errorWebModulesOnly;
 	public static String publishServerTask;
 	public static String errorPortInvalid;
 	public static String errorPortInUse;
 	public static String errorPortsInUse;
	public static String errorDuplicateContextRoot;
 	public static String configurationEditorActionModifyPortDescription;
 	public static String configurationEditorActionModifyPort;
 	public static String configurationEditorActionModifyMimeMappingDescription;
 	public static String configurationEditorActionModifyMimeMapping;
 	public static String configurationEditorActionAddMimeMappingDescription;
 	public static String configurationEditorActionAddMimeMapping;
 	public static String configurationEditorActionAddWebModuleDescription;
 	public static String configurationEditorActionAddWebModule;
 	public static String configurationEditorActionModifyWebModuleDescription;
 	public static String configurationEditorActionModifyWebModule;
 	public static String configurationEditorActionRemoveMimeMappingDescription;
 	public static String configurationEditorActionRemoveMimeMapping;
 	public static String configurationEditorActionRemoveWebModuleDescription;
 	public static String configurationEditorActionRemoveWebModule;
 	public static String serverEditorActionSetDebugModeDescription;
 	public static String serverEditorActionSetDebugMode;
 	public static String serverEditorActionSetSecureDescription;
 	public static String serverEditorActionSetSecure;
 	public static String serverEditorActionSetTestEnvironmentDescription;
 	public static String serverEditorActionSetTestEnvironment;
 	public static String configurationEditorActionEditWebModuleDescription;
 	public static String configurationEditorActionEditWebModulePath;
 	public static String fixModuleContextRootDescription;
 	public static String fixModuleContextRoot;
 
 	static {
 		NLS.initializeMessages(TomcatPlugin.PLUGIN_ID + ".internal.Messages", Messages.class);
 	}
 }
