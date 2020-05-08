 /*******************************************************************************
  * Copyright (c) 2009, 2012 SpringSource, a divison of VMware, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     SpringSource, a division of VMware, Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.virgo.ide.manifest.core;
 
 /**
  * @author Leo Dos Santos
  */
 public interface IHeaderConstants {
 
 	public String IMPORT_BUNDLE = "Import-Bundle";
 
 	public String IMPORT_LIBRARY = "Import-Library";
 
 	public String IMPORT_TEMPLATE = "Import-Template";
 
 	public String EXPORT_TEMPLATE = "Export-Template";
 
 	public String EXCLUDED_IMPORTS = "Excluded-Imports";
 
 	public String EXCLUDED_EXPORTS = "Excluded-Exports";
 
 	public String UNVERSIONED_IMPORTS = "Unversioned-Imports";
 
 	public String IGNORED_EXISTING_HEADERS = "Ignored-Existing-Headers";
 
 	public String PAR_SYMBOLICNAME = "Application-SymbolicName";
 
 	public String PAR_VERSION = "Application-Version";
 
 	public String PAR_NAME = "Application-Name";
 
 	public String PAR_DESCRIPTION = "Application-Description";
 
 	public String PROMOTES_DIRECTIVE = "promotes-exports";
 
	// template.mf headers

	public String TEST_IMPORT_BUNDLE = "Test-Import-Bundle";

	public String TEST_IMPORT_LIBRARY = "Test-Import-Library";

	public String TEST_IMPORT_PACKAGE = "Test-Import-Package";

 }
