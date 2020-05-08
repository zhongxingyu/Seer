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
  * Created on Nov 6, 2003
  *
  * To change the template for this generated file go to
  * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
  */
 package org.eclipse.jst.j2ee.internal.web.archive.operations;
 
 import java.lang.reflect.InvocationTargetException;
 
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.jst.common.jdt.internal.integration.JavaProjectCreationDataModel;
 import org.eclipse.jst.j2ee.application.operations.AddModuleToEARDataModel;
 import org.eclipse.jst.j2ee.application.operations.AddWebModuleToEARDataModel;
 import org.eclipse.jst.j2ee.application.operations.J2EEModuleCreationDataModel;
 import org.eclipse.jst.j2ee.common.XMLResource;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.impl.CommonarchiveFactoryImpl;
 import org.eclipse.jst.j2ee.internal.J2EEVersionConstants;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEPlugin;
 import org.eclipse.jst.j2ee.internal.project.IWebNatureConstants;
 import org.eclipse.jst.j2ee.internal.servertarget.ServerTargetDataModel;
 import org.eclipse.wst.common.frameworks.internal.operations.ProjectCreationDataModel;
 import org.eclipse.wst.common.frameworks.operations.WTPOperation;
 import org.eclipse.wst.common.frameworks.operations.WTPOperationDataModelEvent;
 import org.eclipse.wst.common.frameworks.operations.WTPPropertyDescriptor;
 import org.eclipse.wst.common.modulecore.internal.util.IModuleConstants;
 
 import com.ibm.wtp.common.logger.proxy.Logger;
 
 /**
  * This dataModel is used for to create Web Modules.
  * 
  * This class (and all its fields and methods) is likely to change during the WTP 1.0 milestones as
  * the new project structures are adopted. Use at your own risk.
  * 
  * @since WTP 1.0
  */
 public class WebModuleCreationDataModel extends J2EEModuleCreationDataModel {
 
 	/**
 	 * Type Integer
 	 */
 	public static final String SERVLET_VERSION = "WebModuleCreationDataModel.SERVLET_VERSION"; //$NON-NLS-1$
 	/**
 	 * Type Integer
 	 */
 	public static final String JSP_VERSION = "WebModuleCreationDataModel.JSP_VERSION"; //$NON-NLS-1$
 	/**
 	 * Type String
 	 */
 	public static final String CONTEXT_ROOT = AddWebModuleToEARDataModel.CONTEXT_ROOT;
 
 	public static final String WEB_CONTENT = "WebModuleCreationDataModel.WEB_CONTENT"; //$NON-NLS-1$
 	public static final String MIGRATE_WEB_SETTINGS = "WebModuleCreationDataModel.MIGRATE_WEB_SETTINGS"; //$NON-NLS-1$
 
 	/**
 	 * Creates a Web project with the specified name and version in the specified location.
 	 * 
 	 * @param projectName
 	 *            The name of the Web project to create.
 	 * @param projectLocation
 	 *            Sets the local file system location for the described project. The path must be
 	 *            either an absolute file system path, or a relative path whose first segment is the
 	 *            name of a defined workspace path variable. If <code>null</code> is specified,
 	 *            the default location is used.
 	 * @param connectorModuleVersion
 	 *            Sets the Web Module Version for the descibed project. The version must be one of
 	 *            the following: <code>J2EEVersionConstants.WEB_2_2_ID</code>,
 	 *            <code>J2EEVersionConstants.WEB_2_3_ID</code>, or
 	 * 			  <code>J2EEVersionConstants.WEB_2_4_ID</code>
 	 *            or.
 	 * @since WTP 1.0
 	 */
 	public static void createProject(String projectName, IPath projectLocation, int connectorModuleVersion) {
 		WebModuleCreationDataModel dataModel = new WebModuleCreationDataModel();
 		dataModel.setProperty(PROJECT_NAME, projectName);
 		if (null != projectLocation) {
 			dataModel.setProperty(PROJECT_LOCATION, projectLocation.toOSString());
 		}
 		dataModel.setIntProperty(J2EE_MODULE_VERSION, connectorModuleVersion);
 		try {
 			dataModel.getDefaultOperation().run(null);
 		} catch (InvocationTargetException e) {
 			Logger.getLogger().logError(e);
 		} catch (InterruptedException e) {
 			Logger.getLogger().logError(e);
 		}
 	}
 
 	public WTPOperation getDefaultOperation() {
 		return new WebModuleCreationOperation(this);
 	}
 
 	/**
 	 * @return Returns the default J2EE spec level based on the Global J2EE Preference
 	 */
 	protected Integer getDefaultJ2EEModuleVersion() {
 		int highestJ2EEPref = J2EEPlugin.getDefault().getJ2EEPreferences().getHighestJ2EEVersionID();
 		switch (highestJ2EEPref) {
 			case (J2EEVersionConstants.J2EE_1_4_ID) :
 				return new Integer(J2EEVersionConstants.WEB_2_4_ID);
 			case (J2EEVersionConstants.J2EE_1_3_ID) :
 				return new Integer(J2EEVersionConstants.WEB_2_3_ID);
 			case (J2EEVersionConstants.J2EE_1_2_ID) :
 				return new Integer(J2EEVersionConstants.WEB_2_2_ID);
 			default :
 				return new Integer(J2EEVersionConstants.WEB_2_4_ID);
 		}
 	}
 
 
 	protected void init() {
 		setJ2EENatureID(IWebNatureConstants.J2EE_NATURE_ID);
 		setProperty(EDIT_MODEL_ID, IWebNatureConstants.EDIT_MODEL_ID);
 		getServerTargetDataModel().setIntProperty(ServerTargetDataModel.DEPLOYMENT_TYPE_ID, XMLResource.WEB_APP_TYPE);
 		getProjectDataModel().setProperty(ProjectCreationDataModel.PROJECT_NATURES, new String[]{IWebNatureConstants.J2EE_NATURE_ID,IModuleConstants.MODULE_NATURE_ID});
 		getJavaProjectCreationDataModel().setProperty(JavaProjectCreationDataModel.SOURCE_FOLDERS, new String[]{getDefaultJavaSourceFolderName()});
 		updateOutputLocation();
 		super.init();
 	}
 
 	/**
 	 * @return
 	 */
 	private String getDefaultJavaSourceFolderName() {
 		String javaSrcFolderPref = J2EEPlugin.getDefault().getJ2EEPreferences().getJavaSourceFolderName();
 		if (javaSrcFolderPref == null || javaSrcFolderPref.length() == 0)
 			javaSrcFolderPref = IWebNatureConstants.JAVA_SOURCE;
 		return javaSrcFolderPref;
 	}
 
 	protected boolean doSetProperty(String propertyName, Object propertyValue) {
 		boolean retVal = super.doSetProperty(propertyName, propertyValue);
 		if (WEB_CONTENT.equals(propertyName)) {
 			updateOutputLocation();
 		} else if (propertyName.equals(ADD_TO_EAR)) {
 			Boolean value = (Boolean) propertyValue;
 			if (value.booleanValue())
 				getApplicationCreationDataModel().enableValidation();
 			else
 				getApplicationCreationDataModel().disableValidation();
 		} else if (propertyName.equals(USE_ANNOTATIONS)) {
 			notifyEnablementChange(J2EE_MODULE_VERSION);
 		} else if (propertyName.equals(J2EE_MODULE_VERSION)) {
 			if (getJ2EEVersion() < J2EEVersionConstants.VERSION_1_3)
 				setProperty(USE_ANNOTATIONS, Boolean.FALSE);
 			notifyEnablementChange(USE_ANNOTATIONS);
 		} else if (propertyName.equals(CONTEXT_ROOT)) {
 			getAddModuleToApplicationDataModel().setProperty(AddWebModuleToEARDataModel.CONTEXT_ROOT, propertyValue);
 		}
 		return retVal;
 	}
 
 	private void updateOutputLocation() {
 		getJavaProjectCreationDataModel().setProperty(JavaProjectCreationDataModel.OUTPUT_LOCATION, getOutputLocation());
 	}
 
 	private Object getOutputLocation() {
 		StringBuffer buf = new StringBuffer(getStringProperty(WEB_CONTENT));
 		buf.append(IPath.SEPARATOR);
 		buf.append(IWebNatureConstants.INFO_DIRECTORY);
 		buf.append(IPath.SEPARATOR);
 		buf.append(IWebNatureConstants.CLASSES_DIRECTORY);
 		return buf.toString();
 	}
 
 	protected void initValidBaseProperties() {
 		super.initValidBaseProperties();
 		addValidBaseProperty(SERVLET_VERSION);
 		addValidBaseProperty(JSP_VERSION);
 		addValidBaseProperty(WEB_CONTENT);
 		addValidBaseProperty(USE_ANNOTATIONS);
 		addValidBaseProperty(MIGRATE_WEB_SETTINGS);
 		addValidBaseProperty(CONTEXT_ROOT);
 	}
 
 	protected AddModuleToEARDataModel createModuleNestedModel() {
 		return new AddWebModuleToEARDataModel();
 	}
 
 	protected Object getDefaultProperty(String propertyName) {
 		if (propertyName.equals(MIGRATE_WEB_SETTINGS)) {
 			return Boolean.TRUE;
 		}
 
 		if (propertyName.equals(WEB_CONTENT)) {
 			String webContentFolderPref = J2EEPlugin.getDefault().getJ2EEPreferences().getJ2EEWebContentFolderName();
 			if (webContentFolderPref == null || webContentFolderPref.length() == 0)
 				webContentFolderPref = IWebNatureConstants.WEB_MODULE_DIRECTORY_;
 			return webContentFolderPref;
 		}
 		if (propertyName.equals(CONTEXT_ROOT)) {
 			return getAddModuleToApplicationDataModel().getProperty(CONTEXT_ROOT);
 		}
 
 		if (propertyName.equals(SERVLET_VERSION)) {
 			int moduleVersion = getIntProperty(J2EE_MODULE_VERSION);
 			int servletVersion = J2EEVersionConstants.SERVLET_2_2;
 			switch (moduleVersion) {
 				case J2EEVersionConstants.WEB_2_2_ID :
 					servletVersion = J2EEVersionConstants.SERVLET_2_2;
 					break;
 				case J2EEVersionConstants.WEB_2_3_ID :
 				case J2EEVersionConstants.WEB_2_4_ID :
 					servletVersion = J2EEVersionConstants.SERVLET_2_3;
 					break;
 			}
 			return new Integer(servletVersion);
 		}
 		if (propertyName.equals(JSP_VERSION)) {
 			int moduleVersion = getIntProperty(J2EE_MODULE_VERSION);
 			int jspVersion = J2EEVersionConstants.JSP_1_2_ID;
 			switch (moduleVersion) {
 				case J2EEVersionConstants.WEB_2_2_ID :
 					jspVersion = J2EEVersionConstants.JSP_1_2_ID;
 					break;
 				case J2EEVersionConstants.WEB_2_3_ID :
 				case J2EEVersionConstants.WEB_2_4_ID :
 					jspVersion = J2EEVersionConstants.JSP_2_0_ID;
 					break;
 			}
 			return new Integer(jspVersion);
 		}
 		return super.getDefaultProperty(propertyName);
 	}
 
 	protected WTPPropertyDescriptor doGetPropertyDescriptor(String propertyName) {
 		if (propertyName.equals(J2EE_MODULE_VERSION)) {
 			Integer propertyValue = (Integer) getProperty(propertyName);
 			String description = null;
 			switch (propertyValue.intValue()) {
 				case J2EEVersionConstants.WEB_2_2_ID :
 					description = J2EEVersionConstants.VERSION_2_2_TEXT;
 					break;
 				case J2EEVersionConstants.WEB_2_3_ID :
 					description = J2EEVersionConstants.VERSION_2_3_TEXT;
 					break;
 				case J2EEVersionConstants.WEB_2_4_ID :
 				default :
 					description = J2EEVersionConstants.VERSION_2_4_TEXT;
 					break;
 			}
 			return new WTPPropertyDescriptor(propertyValue, description);
 		}
 		return super.doGetPropertyDescriptor(propertyName);
 	}
 
 	protected WTPPropertyDescriptor[] getValidJ2EEModuleVersionDescriptors() {
 		int highestJ2EEPref = J2EEPlugin.getDefault().getJ2EEPreferences().getHighestJ2EEVersionID();
 		WTPPropertyDescriptor[] descriptors = null;
 		switch (highestJ2EEPref) {
 			case J2EEVersionConstants.J2EE_1_2_ID :
 				descriptors = new WTPPropertyDescriptor[1];
 				descriptors[0] = new WTPPropertyDescriptor(new Integer(J2EEVersionConstants.WEB_2_2_ID), J2EEVersionConstants.VERSION_2_2_TEXT);
 				break;
 			case J2EEVersionConstants.J2EE_1_3_ID :
 				descriptors = new WTPPropertyDescriptor[2];
 				descriptors[0] = new WTPPropertyDescriptor(new Integer(J2EEVersionConstants.WEB_2_2_ID), J2EEVersionConstants.VERSION_2_2_TEXT);
 				descriptors[1] = new WTPPropertyDescriptor(new Integer(J2EEVersionConstants.WEB_2_3_ID), J2EEVersionConstants.VERSION_2_3_TEXT);
 				break;
 			case J2EEVersionConstants.J2EE_1_4_ID :
 			default :
 				descriptors = new WTPPropertyDescriptor[3];
 				descriptors[0] = new WTPPropertyDescriptor(new Integer(J2EEVersionConstants.WEB_2_2_ID), J2EEVersionConstants.VERSION_2_2_TEXT);
 				descriptors[1] = new WTPPropertyDescriptor(new Integer(J2EEVersionConstants.WEB_2_3_ID), J2EEVersionConstants.VERSION_2_3_TEXT);
 				descriptors[2] = new WTPPropertyDescriptor(new Integer(J2EEVersionConstants.WEB_2_4_ID), J2EEVersionConstants.VERSION_2_4_TEXT);
 				break;
 		}
 		return descriptors;
 	}
 
 	protected int convertModuleVersionToJ2EEVersion(int moduleVersion) {
 		switch (moduleVersion) {
 			case J2EEVersionConstants.WEB_2_2_ID :
 				return J2EEVersionConstants.J2EE_1_2_ID;
 			case J2EEVersionConstants.WEB_2_3_ID :
 				return J2EEVersionConstants.J2EE_1_3_ID;
 			case J2EEVersionConstants.WEB_2_4_ID :
 				return J2EEVersionConstants.J2EE_1_4_ID;
 		}
 		return -1;
 	}
 
 	protected Integer convertJ2EEVersionToModuleVersion(Integer j2eeVersion) {
 		switch (j2eeVersion.intValue()) {
 			case J2EEVersionConstants.J2EE_1_2_ID :
 				return new Integer(J2EEVersionConstants.WEB_2_2_ID);
 			case J2EEVersionConstants.J2EE_1_3_ID :
 				return new Integer(J2EEVersionConstants.WEB_2_3_ID);
 			case J2EEVersionConstants.J2EE_1_4_ID :
 				return new Integer(J2EEVersionConstants.WEB_2_4_ID);
 		}
 		return super.convertJ2EEVersionToModuleVersion(j2eeVersion);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.j2ee.internal.internal.application.operations.J2EEModuleCreationDataModel#getModuleType()
 	 */
 	protected EClass getModuleType() {
 		return CommonarchiveFactoryImpl.getPackage().getWARFile();
 	}
 
 	protected String getModuleExtension() {
 		return ".war"; //$NON-NLS-1$
 	}
 
 	protected Boolean basicIsEnabled(String propertyName) {
 		if (USE_ANNOTATIONS.equals(propertyName)) {
 			if (getJ2EEVersion() < J2EEVersionConstants.VERSION_1_3)
 				return Boolean.FALSE;
 			return Boolean.TRUE;
 		}
 		return super.basicIsEnabled(propertyName);
 	}
 
 	protected IStatus doValidateProperty(String propertyName) {
 		if (propertyName.equals(CONTEXT_ROOT)) {
 			if (getBooleanProperty(ADD_TO_EAR)) {
 				return getAddModuleToApplicationDataModel().validateProperty(AddWebModuleToEARDataModel.CONTEXT_ROOT);
 			}
 			return OK_STATUS;
 
 		}
 		return super.doValidateProperty(propertyName);
 	}
 
 	public void propertyChanged(WTPOperationDataModelEvent event) {
 		super.propertyChanged(event);
		if (event.getDataModel() == getAddModuleToApplicationDataModel() && event.getPropertyName().equals(AddWebModuleToEARDataModel.CONTEXT_ROOT) && event.getDataModel().isSet(AddWebModuleToEARDataModel.CONTEXT_ROOT)) {
 			setProperty(CONTEXT_ROOT, event.getProperty());
 		}
 	}
 }
