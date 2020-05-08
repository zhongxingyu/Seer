 /*************************************************************************************
  * Copyright (c) 2008-2011 Red Hat, Inc. and others.
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     JBoss by Red Hat - Initial implementation.
  ************************************************************************************/
 package org.jboss.tools.portlet.core;
 
 
 
 /**
  * @author snjeza
  * 
  */
 
 public interface IPortletConstants {
 
 	static final String PORTLET_FACET_ID="jboss.portlet"; //$NON-NLS-1$
 	
 	static final String JSFPORTLET_FACET_ID="jboss.jsfportlet"; //$NON-NLS-1$
 	
 	static final String SEAMPORTLET_FACET_ID="jboss.seamportlet"; //$NON-NLS-1$
 	
 	static final String CONFIG_PATH = "WEB-INF/portlet.xml"; //$NON-NLS-1$
 	
 	static final String PORTLET_FACET_VERSION_10 = "1.0"; //$NON-NLS-1$
 	
 	static final String PORTLET_FACET_VERSION_20 = "2.0"; //$NON-NLS-1$
 	
 	static final String JSFPORTLET_FACET_VERSION_10 = "1.0"; //$NON-NLS-1$
 	
 	static final String PORTLET_CONTAINER_20_ID = "org.jboss.tools.portlet.core.internal.portletlibrarycontainer.v20"; //$NON-NLS-1$
 	
 	static final String PORTLET_RUNTIME_CONTAINER_ID= "org.jboss.tools.portlet.core.internal.portletlibrarycontainer.runtime"; //$NON-NLS-1$
 	
 	static final String JSFPORTLET_CONTAINER_10_ID = "org.jboss.tools.portlet.core.internal.jsfportletlibrarycontainer.v10"; //$NON-NLS-1$
 
 	static final String PORTLET_INSTANCES_FILE = "WEB-INF/portlet-instances.xml"; //$NON-NLS-1$
 	
 	static final String PORTLET_OBJECT_FILE = "WEB-INF/default-object.xml"; //$NON-NLS-1$
 	
 	static final String JBOSS_APP_FILE = "WEB-INF/jboss-app.xml"; //$NON-NLS-1$
 
 	static final String DEPLOY_JARS = "DEPLOY_JARS"; //$NON-NLS-1$
 	
 	static final String PORTLET_BRIDGE_RUNTIME = "PORTLET_BRIDGE_RUNTIME"; //$NON-NLS-1$
 
 	static final String JSF_SECTION = "jsfSection"; //$NON-NLS-1$
 
 	static final String WEB_INF_LIB = "WEB-INF/lib"; //$NON-NLS-1$
 	
 	static final String LIB = "lib"; //$NON-NLS-1$
 
 	static final String JBOSS_PORTLET_FILE = "WEB-INF/jboss-portlet.xml"; //$NON-NLS-1$
 
 	static final String DEPLOY_PORTLET_JARS = "DEPLOY_PORTLET_JARS"; //$NON-NLS-1$
 
 	static final String ENABLE_IMPLEMENTATION_LIBRARY = "ENABLE_IMPLEMENTATION_LIBRARY"; //$NON-NLS-1$
 	
 	static final String PORTLET_SECTION = "portletSection"; //$NON-NLS-1$
 	
 	static final String USER_LIBRARY = Messages.IPortletConstants_User_library;
 	
 	static final String LIBRARIES_PROVIDED_BY_SERVER_RUNTIME = Messages.IPortletConstants_Libraries_provided_by_server_runtime;
 
 	static final String IMPLEMENTATION_LIBRARY = "implementationLibrary"; //$NON-NLS-1$
 
 	static final String USER_LIBRARY_NAME = "userLibraryName"; //$NON-NLS-1$
 	
 	static final String SERVER_DEFAULT_DEPLOY_JBOSS_PORTAL_SAR = "deploy/jboss-portal.sar"; //$NON-NLS-1$
 	
 	static final String SERVER_DEFAULT_DEPLOY_JBOSS_PORTAL_HA_SAR = "deploy/jboss-portal-ha.sar"; //$NON-NLS-1$
 
 	static final String SERVER_DEFAULT_DEPLOY_SIMPLE_PORTAL = "deploy/simple-portal"; //$NON-NLS-1$
 	
 	static final String SERVER_DEFAULT_DEPLOY_SIMPLE_PORTAL_SAR = "deploy/simple-portal.sar"; //$NON-NLS-1$
 	
 	static final String SERVER_DEFAULT_DEPLOY_GATEIN = "deploy/gatein.ear"; //$NON-NLS-1$
 	
 	static final String SERVER_DEFAULT_DEPLOY_GATEIN33 = "standalone/deployments/gatein.ear"; //$NON-NLS-1$
 	
 	static final String SERVER_DEFAULT_DEPLOY_JPP60 = "gatein/gatein.ear"; //$NON-NLS-1$
 	
 
 	static final String GATEIN_MODULES_JAVAX_PORTLET_API_MAIN = "gatein/modules/javax/portlet/api/main"; //$NON-NLS-1$
 	
 	static final String MODULES_JAVAX_PORTLET_API_MAIN = "modules/javax/portlet/api/main"; //$NON-NLS-1$
 	
 	static final String MODULES_JAVAX_PORTLET_API_MAIN72 = "modules/system/layers/base/javax/portlet/api/main"; //$NON-NLS-1$
 	
 	static final String MODULES_JAVAX_PORTLET_API_JPP61 = "modules/system/add-ons/gatein/javax/portlet/api/main/"; //$NON-NLS-1$
 	
	static final String MODULES_JAVAX_PORTLET_API_JPP61a = "modules/system/layers/gatein/javax/portlet/api/main/"; //$NON-NLS-1$
	
 	static final String TOMCAT_LIB = "lib"; //$NON-NLS-1$
 
 	static final String JAR = ".jar"; //$NON-NLS-1$
 
 	static final String PORTLET_API = "portlet-api"; //$NON-NLS-1$
 
 	static final String LIBRARIES_PROVIDED_BY_PORTLETBRIDGE = Messages.Libraries_provided_by_portletbridge;
 
 	static final String LIBRARIES_PROVIDED_BY_RICHFACES = Messages.Libraries_provided_by_richfaces;
 
 	static final String RICHFACES_RUNTIME = "RICHFACES_RUNTIME"; //$NON-NLS-1$
 
 	static final String IS_EPP = "IS_EPP"; //$NON-NLS-1$
 
 	static final String RICHFACES_LIBRARIES_SELECTED = "RICHFACES_LIBRARIES_SELECTED"; //$NON-NLS-1$
 	
 	static final String RICHFACES_LIBRARIES_TYPE = "RICHFACES_LIBRARIES_TYPE"; //$NON-NLS-1$
 
 	static final String RICHFACES_CAPABILITIES = "RICHFACES_CAPABILITIES"; //$NON-NLS-1$
 
 	static final String PORTLET_SAR_LIB = "deploy/jboss-portal.sar/portal-identity.sar/portal-identity.war/WEB-INF/lib"; //$NON-NLS-1$
 	
 	static final String PORTLET_SAR_HA_LIB = "deploy/jboss-portal-ha.sar/portal-identity.sar/portal-identity.war/WEB-INF/lib"; //$NON-NLS-1$
 	
 	static final String PORTLET_LIBRARY_PROVIDER_DELEGATE = "PORTLET_LIBRARY_PROVIDER_DELEGATE"; //$NON-NLS-1$
 	
 	static final String JSFPORTLET_LIBRARY_PROVIDER_DELEGATE = "JSFPORTLET_LIBRARY_PROVIDER_DELEGATE"; //$NON-NLS-1$
 
 	static final String PORTLETBRIDGE_HOME = "portletbridgeHome"; //$NON-NLS-1$
 	
 	static final String PREFS_PORTLETBRIDGE_HOME = "jsfportlet.library.provider/portletbridge_home"; //$NON-NLS-1$
 
 	static final String PORTLET_BRIDGE_HOME = "portletBridgeHome"; //$NON-NLS-1$
 }
