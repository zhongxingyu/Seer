 /*******************************************************************************
  * Copyright (c) 2003, 2006 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.navigator.internal;
 
 import org.eclipse.jst.ejb.ui.internal.wizard.EJBComponentExportWizard;
 import org.eclipse.jst.ejb.ui.internal.wizard.EJBComponentImportWizard;
 import org.eclipse.jst.j2ee.internal.wizard.AppClientComponentExportWizard;
 import org.eclipse.jst.j2ee.internal.wizard.AppClientComponentImportWizard;
 import org.eclipse.jst.j2ee.internal.wizard.EARComponentExportWizard;
 import org.eclipse.jst.j2ee.internal.wizard.EARComponentImportWizard;
 import org.eclipse.jst.j2ee.internal.wizard.J2EEUtilityJarImportWizardNew;
 import org.eclipse.jst.j2ee.jca.ui.internal.wizard.ConnectorComponentExportWizard;
 import org.eclipse.jst.j2ee.jca.ui.internal.wizard.ConnectorComponentImportWizard;
 
 /**
  * <p>
  * The following class is experimental until fully documented.
  * </p>
  * <p>
  * The Creation IDs are used for activity filtering and as such are declared on the individual
  * wizards. All other IDs must be maintained to stay in sync with the values found in the plugin.xml
  * files of the respective module UI plugins.
  */
 public interface IJ2EEWizardConstants {
 
 //	String NEW_EAR_PROJECT_WIZARD_ID = EARComponentCreationWizard.WIZARD_ID;
 
 //	String NEW_APPCLIENT_PROJECT_WIZARD_ID = AppClientComponentCreationWizard.WIZARD_ID;
 //
 //	String NEW_JCA_PROJECT_WIZARD_ID = ConnectorComponentCreationWizard.WIZARD_ID;
 //
 //	String NEW_EJB_PROJECT_WIZARD_ID = EJBComponentCreationWizard.WIZARD_ID;
 //
 //	String NEW_WEB_PROJECT_WIZARD_ID = WebComponentCreationWizard.WIZARD_ID;
 
 	String NEW_ENTERPRISE_BEAN_WIZARD_ID = "org.eclipse.jst.j2ee.ejb.ui.util.createEJBWizard"; //$NON-NLS-1$
 
 	String IMPORT_EAR_WIZARD_ID = EARComponentImportWizard.class.getName();
 
 	String IMPORT_APPCLIENT_WIZARD_ID = AppClientComponentImportWizard.class.getName();
 
 	String IMPORT_CONNECTOR_WIZARD_ID = ConnectorComponentImportWizard.class.getName();
 
 	String IMPORT_ENTERPRISE_BEAN_WIZARD_ID = EJBComponentImportWizard.class.getName();
 
	String IMPORT_WEB_MODULE_WIZARD_ID = "org.eclipse.jst.servlet.ui.internal.wizard.WebComponentImportWizard"; //$NON-NLS-1$
	
	String EXPORT_WEB_MODULE_WIZARD_ID = "org.eclipse.jst.servlet.ui.internal.wizard.WebComponentExportWizard"; //$NON-NLS-1$
 
 	String IMPORT_UTILITY_JAR_WIZARD_ID = J2EEUtilityJarImportWizardNew.class.getName();
 
 	String EXPORT_EAR_WIZARD_ID = EARComponentExportWizard.class.getName();
 
 	String EXPORT_APPCLIENT_WIZARD_ID = AppClientComponentExportWizard.class.getName();
 
 	String EXPORT_CONNECTOR_WIZARD_ID = ConnectorComponentExportWizard.class.getName();
 
 	String EXPORT_ENTERPRISE_BEAN_WIZARD_ID = EJBComponentExportWizard.class.getName();
 
	
 }
