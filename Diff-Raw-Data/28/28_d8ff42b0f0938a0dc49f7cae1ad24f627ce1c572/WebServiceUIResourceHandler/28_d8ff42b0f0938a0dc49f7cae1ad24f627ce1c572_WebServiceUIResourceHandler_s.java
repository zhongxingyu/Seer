 /*******************************************************************************
  * Copyright (c) 2000, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.internal.webservice;
 
 import org.eclipse.osgi.util.NLS;
 
 public final class WebServiceUIResourceHandler extends NLS {
 
 	private static final String BUNDLE_NAME = "webserviceui";//$NON-NLS-1$
 
 	private WebServiceUIResourceHandler() {
 		// Do not instantiate
 	}
 
 	public static String NEW_BUTTON_LABEL;
 	public static String NEW_HANDLER_WIZARD_WINDOW_TITLE;
 	public static String NEW_HANDLER_WIZARD_PAGE_TITLE;
 	public static String NEW_HANDLER_WIZARD_PAGE_DESC;
 	public static String HANDLER_DISPLAY_NAME_LABEL;
 	public static String HANDLER_DESCRIPTION_LABEL;
 	public static String HANDLER_NAME_LABEL;
 	public static String HANDLER_CLASS_NAME_LABEL;
 	public static String HANDLER_INIT_PARAM_WIZARD_WINDOW_TITLE;
 	public static String HANDLER_INIT_PARAM_WIZARD_PAGE_TITLE;
 	public static String HANDLER_INIT_PARAM_WIZARD_PAGE_DESC;
 	public static String HANDLER_SOAP_HEADER_WIZARD_WINDOW_TITLE;
 	public static String HANDLER_SOAP_HEADER_WIZARD_PAGE_TITLE;
 	public static String HANDLER_SOAP_HEADER_WIZARD_PAGE_DESC;
 	public static String HANDLER_NAMESPACE_URL_LABEL;
 	public static String HANDLER_LOCAL_PART_LABEL;
 	public static String PortComponentRefLabelProvider_UI_1;
 	public static String PortComponentRefLabelProvider_UI_2;
 	public static String WebServiceNavigatorGroupType_UI_0;
 	public static String WebServiceNavigatorGroupType_UI_1;
 	public static String WebServiceNavigatorGroupType_UI_2;
 	public static String WebServiceFilesContribution_UI_0;
 	public static String WebServiceGroupContentExtension_UI_1;
 	public static String WebServiceNavigatorGroup_UI_0;
 	public static String Server_Invalid_For_Exisitng_EAR_UI_;
 	public static String ServiceReferenceSelectionWizard_UI_6;
 	public static String ServiceReferenceSelectionWizard_UI_7;
 	public static String ServiceReferenceSelectionPage_UI_0;
 	public static String ServiceReferenceSelectionPage_UI_1;
 	public static String ServiceReferenceSelectionPage_UI_2;
 	public static String SERVICE_CLASSES_UI_;
 	public static String PORT_UI_;
 	public static String HANDLER_UI_;
 	public static String WSDL_PORT_UI_;
 	public static String SERVICE_IMPL_UI_;
 	public static String WSDL_UI_;
 
 	static {
 		NLS.initializeMessages(BUNDLE_NAME, WebServiceUIResourceHandler.class);
 	}
 }
