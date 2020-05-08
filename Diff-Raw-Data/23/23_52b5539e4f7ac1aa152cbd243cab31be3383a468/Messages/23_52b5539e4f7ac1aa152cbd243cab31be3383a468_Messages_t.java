 /*******************************************************************************
  * Copyright (c) 2005 Oracle Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Gerry Kessler - initial API and implementation
  *    Ian Trimble - changed to work correctly with org.eclipse.osgi.util.NLS
  *******************************************************************************/ 
 package org.eclipse.jst.jsf.core.internal;
 
 import org.eclipse.osgi.util.NLS;
 
 /**
  * String resource handler.
  * 
  * @author Gerry Kessler - Oracle, Ian Trimble - Oracle
  */
 public class Messages extends NLS {
 	private static final String BUNDLE_NAME = "org.eclipse.jst.jsf.core.internal.messages"; //$NON-NLS-1$
 
 	public static String JSFLibraryRegistry_ErrorCreatingURL;
 	public static String JSFLibraryRegistry_NoLoadCreatingNew;
 	public static String JSFLibraryRegistry_ErrorSaving;
 	public static String JSFLibraryRegistry_DEFAULT_IMPL_LABEL;
 	public static String JSFLibraryRegistry_ErrorLoadingFromExtPt;
 
 	public static String JSFFacetInstallDataModelProvider_ValidateServletName;
 	public static String JSFFacetInstallDataModelProvider_ValidateJSFImpl;
 	public static String JSFFacetInstallDataModelProvider_ValidateConfigFileEmpty;
 	public static String JSFFacetInstallDataModelProvider_ValidateConfigFilePath;
 	public static String JSFFacetInstallDataModelProvider_ValidateConfigFileRelative1;
 	public static String JSFFacetInstallDataModelProvider_ValidateConfigFileRelative2;
 	public static String JSFFacetInstallDataModelProvider_ValidateConfigFileXML;
 	public static String JSFFacetInstallDataModelProvider_ValidateConfigFileSlashes;
 
 	public static String ArchiveFileImpl_CannotCopyFile;
 	public static String ArchiveFileImpl_CannotCloseFile;
 	public static String ArchiveFileImpl_CannotLocatePluginRelativeFile;
 
 	public static String PluginProvidedJSFLibraryCreationHelper_ErrorCreating;
 	public static String PluginProvidedJSFLibraryCreationHelper_ErrorMultipleDefinition;
 
 	public static String JSFUtils_ErrorCreatingConfigFile;
 	public static String JSFUtils_ErrorClosingConfigFile;
 
	public static String JARFileJSFAppConfigProvider_ErrorLoadingModel;

 	static {
 		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
 	}
 }
