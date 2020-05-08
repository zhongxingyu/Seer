 /******************************************************************************* 
  * Copyright (c) 2011 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Red Hat, Inc. - initial API and implementation 
  ******************************************************************************/
 package org.jboss.tools.jst.web.validation;
 
 import java.text.MessageFormat;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.wst.common.project.facet.core.IFacetedProject;
 import org.eclipse.wst.common.project.facet.core.IProjectFacet;
 import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
 import org.eclipse.wst.validation.internal.core.ValidationException;
 import org.eclipse.wst.validation.internal.provisional.core.IReporter;
 import org.jboss.tools.common.model.XModelObject;
 import org.jboss.tools.common.model.util.EclipseResourceUtil;
 import org.jboss.tools.common.validation.ContextValidationHelper;
 import org.jboss.tools.common.validation.IProjectValidationContext;
 import org.jboss.tools.common.validation.IValidatingProjectSet;
 import org.jboss.tools.common.validation.IValidatingProjectTree;
 import org.jboss.tools.common.validation.IValidator;
 import org.jboss.tools.common.validation.ValidationErrorManager;
 import org.jboss.tools.common.validation.ValidatorManager;
 import org.jboss.tools.common.validation.internal.ProjectValidationContext;
 import org.jboss.tools.common.validation.internal.SimpleValidatingProjectTree;
 import org.jboss.tools.common.validation.internal.ValidatingProjectSet;
 import org.jboss.tools.common.web.WebUtils;
 import org.jboss.tools.jst.web.WebModelPlugin;
 import org.jboss.tools.jst.web.model.helpers.WebAppHelper;
 
 /**
  * @author Viacheslav Kabanovich
  */
 public class WebXMLCoreValidator extends ValidationErrorManager implements IValidator {
 	public static final String ID = "org.jboss.tools.esb.validator.ESBCoreValidator"; //$NON-NLS-1$
 	public static final String PROBLEM_TYPE = "org.jboss.tools.jst.web.webxmlproblem"; //$NON-NLS-1$
 	public static final String PREFERENCE_PAGE_ID = "org.jboss.tools.jst.web.ui.WebXMLValidatorPreferencePage"; //$NON-NLS-1$
 
 	public static String SHORT_ID = "verification"; //$NON-NLS-1$
 
 	static String XML_EXT = ".xml"; //$NON-NLS-1$
 
 	String projectName;
 	Map<IProject, IProjectValidationContext> contexts = new HashMap<IProject, IProjectValidationContext>();
 
 	Map<String, Set<Check>> checks = new HashMap<String, Set<Check>>();
 
 	public WebXMLCoreValidator() {
 		createChecks();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.jboss.tools.jst.web.kb.internal.validation.ValidationErrorManager#getPreference(org.eclipse.core.resources.IProject, java.lang.String)
 	 */
 	@Override
 	protected String getPreference(IProject project, String preferenceKey) {
 		return WebXMLPreferences.getInstance().getProjectPreference(project, preferenceKey);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.jboss.tools.jst.web.kb.internal.validation.ValidationErrorManager#getMaxNumberOfMarkersPerFile(org.eclipse.core.resources.IProject)
 	 */
 	@Override
 	public int getMaxNumberOfMarkersPerFile(IProject project) {
 		return WebXMLPreferences.getMaxNumberOfProblemMarkersPerFile(project);
 	}
 
 	private void addCheck(Check check, String... entities) {
 		for (String entity: entities) {
 			Set<Check> cs = checks.get(entity);
 			if(cs == null) {
 				cs = new HashSet<Check>();
 				checks.put(entity, cs);
 			}
 			cs.add(check);
 		}
 	}
 
 	void createChecks() {
 		addCheck(new CheckClass(this, WebXMLPreferences.INVALID_EXCEPTION_TYPE, "exception-type", false, null, "java.lang.Exception"), 
 				"WebAppErrorPage");
 		addCheck(new CheckClass(this, WebXMLPreferences.INVALID_FILTER_CLASS, "filter-class"), 
 				WebAppHelper.FILTER_ENTITY, WebAppHelper.FILTER_30_ENTITY);
 		addCheck(new CheckClass(this, WebXMLPreferences.INVALID_SERVLET_CLASS, "servlet-class", false, "javax.servlet.Servlet", null), 
 				WebAppHelper.SERVLET_ENTITY, WebAppHelper.SERVLET_30_ENTITY);
 		addCheck(new CheckClass(this, WebXMLPreferences.INVALID_LISTENER_CLASS, "listener-class"), 
 				WebAppHelper.LISTENER_ENTITY, WebAppHelper.LISTENER_24_ENTITY);
 		addCheck(new CheckClass(this, WebXMLPreferences.INVALID_EXCEPTION_TYPE, "res-type"), 
 				"WebAppResourceRef");
 		addCheck(new CheckClass(this, WebXMLPreferences.INVALID_MESSAGE_DESTINATION_TYPE, "message-destination-type"), 
 				"WebAppMessageDestinationRef");
 
 		addCheck(new CheckResource(this, WebXMLPreferences.INVALID_TAGLIB_REF, "taglib-location", false, ".tld", WebXMLValidatorMessages.PATH_NOT_TAGLIB), 
 				WebAppHelper.TAGLIB_ENTITY);
 		addCheck(new CheckResource(this, WebXMLPreferences.INVALID_WELCOME_FILE_REF, "file name", false, ".jsp .html .htm .jspx", WebXMLValidatorMessages.PATH_NOT_PAGE), 
 				"WebAppWelcomFile");
 		addCheck(new CheckResource(this, WebXMLPreferences.INVALID_ERROR_PAGE_REF, "location", false, null, null).acceptServlet(), 
 				"WebAppErrorPage");
 		addCheck(new CheckResource(this, WebXMLPreferences.INVALID_FORM_ERROR_PAGE_REF, "form-error-page").acceptServlet(), 
 				"WebAppLoginConfig");
 		addCheck(new CheckResource(this, WebXMLPreferences.INVALID_FORM_LOGIN_PAGE_REF, "form-login-page").acceptServlet(), 
 				"WebAppLoginConfig");
 		addCheck(new CheckResource(this, WebXMLPreferences.INVALID_JSP_FILE_REF, "jsp-file"), 
 				WebAppHelper.SERVLET_ENTITY, WebAppHelper.SERVLET_30_ENTITY);
 		addCheck(new CheckResource(this, WebXMLPreferences.INVALID_ICON_REF, "small-icon", true, ".jpg .gif .png", WebXMLValidatorMessages.PATH_NOT_ICON), 
 				WebAppHelper.SERVLET_ENTITY, WebAppHelper.SERVLET_30_ENTITY, WebAppHelper.FILTER_ENTITY, WebAppHelper.FILTER_30_ENTITY, "FileWebApp", "FileWebApp24", "FileWebApp25", "FileWebApp30");
 		addCheck(new CheckResource(this, WebXMLPreferences.INVALID_ICON_REF, "large-icon", true, ".jpg .gif .png", WebXMLValidatorMessages.PATH_NOT_ICON), 
 				WebAppHelper.SERVLET_ENTITY, WebAppHelper.SERVLET_30_ENTITY, WebAppHelper.FILTER_ENTITY, WebAppHelper.FILTER_30_ENTITY, "FileWebApp", "FileWebApp24", "FileWebApp25", "FileWebApp30");
 
 		addCheck(new CheckServletMappingName(this, WebXMLPreferences.INVALID_SERVLET_REF, false), 
 				WebAppHelper.SERVLET_MAPPING_ENTITY);
 		addCheck(new CheckServletMappingName(this, WebXMLPreferences.INVALID_SERVLET_REF, true), 
 				WebAppHelper.FILTER_MAPPING_ENTITY, WebAppHelper.FILTER_MAPPING_24_ENTITY);
 		addCheck(new CheckFilterMappingName(this, WebXMLPreferences.INVALID_FILTER_REF), 
 				WebAppHelper.FILTER_MAPPING_ENTITY, WebAppHelper.FILTER_MAPPING_24_ENTITY);
 		addCheck(new CheckRoleReferenceName(this, WebXMLPreferences.INVALID_ROLE_REF, "role-link"), 
 				"WebAppSecurityRoleRef");
 		addCheck(new CheckRoleReferenceName(this, WebXMLPreferences.INVALID_ROLE_REF, "role-name"), 
 				"WebAppRunAs", "WebAppRoleName");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.jboss.tools.jst.web.kb.internal.validation.ValidationErrorManager#getMarkerType()
 	 */
 	@Override
 	public String getMarkerType() {
 		return PROBLEM_TYPE;
 	}
 
 	public String getId() {
 		return ID;
 	}
 
 	public String getBuilderId() {
 		return null;
 	}
 
 	public IValidatingProjectTree getValidatingProjects(IProject project) {
 		IProjectValidationContext rootContext = contexts.get(project);
 		if(rootContext == null) {
 			rootContext = new ProjectValidationContext();
 			contexts.put(project, rootContext);
 		}
 
 		Set<IProject> projects = new HashSet<IProject>();
 		projects.add(project);
 
 		IValidatingProjectSet projectSet = new ValidatingProjectSet(project, projects, rootContext);
 		return new SimpleValidatingProjectTree(projectSet);
 	}
 
 	public boolean shouldValidate(IProject project) {
 		if(!project.isAccessible()) {
 			return false;
 		}
 
 		try {
 			IFacetedProject fp = ProjectFacetsManager.create(project);
 			IProjectFacet f = ProjectFacetsManager.getProjectFacet("jst.web");
 			if(fp != null && f != null && fp.getInstalledVersion(f) != null) {
 				return true;
 			}
 		} catch (CoreException e) {
 			WebModelPlugin.getDefault().logError(e);
 		}
 		return false;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.jboss.tools.jst.web.kb.internal.validation.ValidationErrorManager#init(org.eclipse.core.resources.IProject, org.jboss.tools.jst.web.kb.internal.validation.ContextValidationHelper, org.jboss.tools.jst.web.kb.validation.IProjectValidationContext, org.eclipse.wst.validation.internal.provisional.core.IValidator, org.eclipse.wst.validation.internal.provisional.core.IReporter)
 	 */
 	@Override
 	public void init(IProject project, ContextValidationHelper validationHelper, IProjectValidationContext context, org.eclipse.wst.validation.internal.provisional.core.IValidator manager, IReporter reporter) {
 		super.init(project, validationHelper, context, manager, reporter);
 		projectName = project.getName();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.jboss.tools.jst.web.kb.validation.IValidator#validate(java.util.Set, org.eclipse.core.resources.IProject, org.jboss.tools.jst.web.kb.internal.validation.ContextValidationHelper, org.jboss.tools.jst.web.kb.validation.IProjectValidationContext, org.jboss.tools.jst.web.kb.internal.validation.ValidatorManager, org.eclipse.wst.validation.internal.provisional.core.IReporter)
 	 */
 	public IStatus validate(Set<IFile> changedFiles, IProject project,
 			ContextValidationHelper validationHelper, IProjectValidationContext context, ValidatorManager manager,
 			IReporter reporter) throws ValidationException {
 		init(project, validationHelper, context, manager, reporter);
 
 		for (IFile file: changedFiles) {
 			if(file.getName().endsWith(XML_EXT)) {
 				XModelObject o = EclipseResourceUtil.createObjectForResource(file);
 				if(o != null && o.getModelEntity().getName().startsWith("FileWebApp")) {
 					validateWebXMLFile(o, file);
 				}
 			}
 		}
 		return OK_STATUS;
 	}
 
 	private void validateWebXMLFile(XModelObject object, IFile file) {
 		validateObject(object);
 	}
 
 	private void validateObject(XModelObject object) {
 		String entity = object.getModelEntity().getName();
 		Set<Check> ch = checks.get(entity);
 		if(ch != null) {
 			for (Check c: ch) {
 				c.check(object);
 			}
 		}
 		XModelObject[] cs = object.getChildren();
 		for (XModelObject c: cs) {
 			validateObject(c);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.jboss.tools.jst.web.kb.validation.IValidator#validateAll(org.eclipse.core.resources.IProject, org.jboss.tools.jst.web.kb.internal.validation.ContextValidationHelper, org.jboss.tools.jst.web.kb.validation.IProjectValidationContext, org.jboss.tools.jst.web.kb.internal.validation.ValidatorManager, org.eclipse.wst.validation.internal.provisional.core.IReporter)
 	 */
 	public IStatus validateAll(IProject project,
 			ContextValidationHelper validationHelper, IProjectValidationContext context, ValidatorManager manager,
 			IReporter reporter) throws ValidationException {
 		init(project, validationHelper, context, manager, reporter);
 		displaySubtask(WebXMLValidatorMessages.VALIDATING_PROJECT, new String[]{projectName});
 
 		IPath webContentPath = WebUtils.getFirstWebContentPath(project);
 		IFolder webInf = null;
 		try {
 			
 			// This code line never return null
 			webInf = project.getFolder(webContentPath.removeFirstSegments(1).append("WEB-INF")); //$NON-NLS-1$
 			// so never check it for null
 			if(webInf.isAccessible()) {
 				IResource[] rs = webInf.members();
 				// exception is not required here because if esbContent is not exist control
 				// never gets here
 				for (IResource r: rs) {
 					if(r instanceof IFile) {
 						IFile file = (IFile)r;
 						String name = file.getName();
 						if(name.endsWith(XML_EXT)) {
 							XModelObject o = EclipseResourceUtil.createObjectForResource(file);
 							if(o != null && o.getModelEntity().getName().startsWith("FileWebApp")) {
 								validateWebXMLFile(o, file);
 							}
 						}
 					}
 				}
 			}
 		} catch (CoreException e) {
 			// hiding exceptions is the evil so lets return EROOR Status with exception
 			return new Status(IStatus.ERROR,WebModelPlugin.PLUGIN_ID,MessageFormat.format("Validation error for project {0}",project.getLocation().toString()),e);
 		}
 		
 		return OK_STATUS;
 	}
 
 	
 
 	public boolean isEnabled(IProject project) {
 		return WebXMLPreferences.isValidationEnabled(project);
 	}
 
 	@Override
 	protected String getPreferencePageId() {
 		return PREFERENCE_PAGE_ID;
 	}
 
 }
