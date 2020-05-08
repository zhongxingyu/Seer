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
 package org.eclipse.wst.server.core.internal;
 
 import org.eclipse.osgi.util.NLS;
 /**
  * Translated messages.
  */
 public class Messages extends NLS {
 	public static String publishing;
 	public static String errorRuntimeName;
 	public static String errorDuplicateRuntimeName;
 	public static String errorSaving;
 	public static String errorLoading;
 	public static String defaultVendor;
 	public static String defaultVersion;
 	public static String savingTask;
 	public static String taskPerforming;
 	public static String deletingTask;
 	public static String defaultServerProjectName;
 	public static String defaultRuntimeName;
 	public static String defaultRuntimeName2;
 	public static String defaultServerName;
 	public static String defaultServerName2;
 	public static String defaultServerName3;
 	public static String defaultServerName4;
 	public static String taskModifyModules;
 	public static String errorWorkingCopyTimestamp;
 	public static String errorPublishStarting;
 	public static String errorPublishNoRuntime;
 	public static String errorPublishNoConfiguration;
 	public static String errorNoConfiguration;
 	public static String canPublishOk;
 	public static String publishingStatus;
 	public static String publishingCancelled;
 	public static String errorPublishing;
 	public static String publishingStop;
 	public static String publishingModule;
 	public static String publishedModule;
 	public static String errorStartTimeout;
 	public static String errorStartFailed;
 	public static String errorModuleRestartFailed;
 	public static String canRestartModuleOk;
 	public static String errorRestartModule;
 	public static String canStartErrorState;
 	public static String errorLaunchMode;
 	public static String canStartOk;
 	public static String canRestartOk;
 	public static String errorRestartNotStarted;
 	public static String errorStopAlreadyStopped;
 	public static String canStopOk;
 	public static String moduleTypeUnknown;
 	public static String jobStartingServer;
 	public static String jobRestartingServer;
 	
 	static {
 		NLS.initializeMessages(ServerPlugin.PLUGIN_ID + ".internal.Messages", Messages.class);
 	}
 }
