 /*******************************************************************************
  * Copyright (c) 2003, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.internal.web.operations;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.List;
 
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jem.util.UIContextDetermination;
 import org.eclipse.jem.util.emf.workbench.ProjectUtilities;
 import org.eclipse.jem.util.logger.proxy.Logger;
 import org.eclipse.jst.j2ee.application.internal.operations.IAnnotationsDataModel;
 import org.eclipse.jst.j2ee.common.CommonFactory;
 import org.eclipse.jst.j2ee.common.Description;
 import org.eclipse.jst.j2ee.common.ParamValue;
 import org.eclipse.jst.j2ee.internal.J2EEVersionConstants;
 import org.eclipse.jst.j2ee.internal.common.operations.INewJavaClassDataModelProperties;
 import org.eclipse.jst.j2ee.model.IModelProvider;
 import org.eclipse.jst.j2ee.model.ModelProviderManager;
 import org.eclipse.jst.j2ee.webapplication.InitParam;
 import org.eclipse.jst.j2ee.webapplication.JSPType;
 import org.eclipse.jst.j2ee.webapplication.Servlet;
 import org.eclipse.jst.j2ee.webapplication.ServletMapping;
 import org.eclipse.jst.j2ee.webapplication.ServletType;
 import org.eclipse.jst.j2ee.webapplication.WebApp;
 import org.eclipse.jst.j2ee.webapplication.WebapplicationFactory;
 import org.eclipse.jst.javaee.core.DisplayName;
 import org.eclipse.jst.javaee.core.JavaeeFactory;
 import org.eclipse.jst.javaee.core.UrlPatternType;
 import org.eclipse.jst.javaee.web.WebFactory;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.wst.common.componentcore.internal.operation.ArtifactEditProviderOperation;
 import org.eclipse.wst.common.componentcore.internal.operation.IArtifactEditOperationDataModelProperties;
 import org.eclipse.wst.common.frameworks.datamodel.AbstractDataModelOperation;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 
 /**
  * This class, AddServlet Operation is a IDataModelOperation following the IDataModel wizard and
  * operation framework.
  * @see org.eclipse.wst.common.frameworks.datamodel.IDataModelOperation
  * @see org.eclipse.wst.common.frameworks.datamodel.IDataModelProvider
  * 
  * This operation subclasses the ArtifactEditProviderOperation so the changes made to the deployment descriptor
  * models are saved to the artifact edit model.
  * @see org.eclipse.wst.common.componentcore.internal.operation.ArtifactEditProviderOperation
  * 
  * It is the operation which should be used when adding a new servlet to
  * a web app, whether that be an annotated servlet or a non annotated servlet.  This uses the
  * NewServletClassDataModelProvider to retrieve properties set by the user in order to create the custom
  * servet.
  * @see org.eclipse.jst.j2ee.internal.web.operations.NewServletClassDataModelProvider
  * 
  * In the non annotated case, this operation will add the metadata necessary into the web deployment
  * descriptor.  In the annotated case, it will not, it will leave this up to the parsing of the
  * annotations to build the deployment descriptor artifacts.  To actually create the java class for
  * the servlet, the operation uses the NewServletClassOperation. The NewServletClassOperation 
  * shares the same data model provider.
  * @see org.eclipse.jst.j2ee.internal.web.operations.NewServletClassOperation
  * 
  * Clients may subclass this operation to provide their own behaviour on servlet creation.  The execute
  * method can be extended to do so. Also, generateServletMetaData and creteServletClass are exposed.
  * 
  * The use of this class is EXPERIMENTAL and is subject to substantial changes.
  */
 public class AddServletOperation extends AbstractDataModelOperation implements IArtifactEditOperationDataModelProperties{
 	
 	private IModelProvider provider = null;
 	/**
 	 * This is the constructor which should be used when creating the operation.
 	 * It will not accept null parameter.  It will not return null.
 	 * @see ArtifactEditProviderOperation#ArtifactEditProviderOperation(IDataModel)
 	 * 
 	 * @param dataModel 
 	 * @return AddServletOperation
 	 */
 	public AddServletOperation(IDataModel dataModel) {
 		super(dataModel);
 		provider = ModelProviderManager.getModelProvider( getTargetProject() );
 	}
 
 	/**
 	 * Subclasses may extend this method to add their own actions during execution.
 	 * The implementation of the execute method drives the running of the operation. This
 	 * implementation will create the servlet class, and then if the servlet is not
 	 * annotated, it will create the servlet metadata for the web deployment descriptor.
 	 * This method will accept null as a parameter.
 	 * @see org.eclipse.core.commands.operations.AbstractOperation#execute(org.eclipse.core.runtime.IProgressMonitor, org.eclipse.core.runtime.IAdaptable)
 	 * @see AddServletOperation#createServletClass()
 	 * @see AddServletOperation#generateServletMetaData(NewServletClassDataModel, String, boolean)
 	 * 
 	 * @param monitor IProgressMonitor
 	 * @param info IAdaptable
 	 * @throws CoreException
 	 * @throws InterruptedException
 	 * @throws InvocationTargetException
 	 */
 	public IStatus doExecute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
 		//Retrieve values set in the newservletclass data model
 		boolean isServletType = model.getBooleanProperty(INewServletClassDataModelProperties.IS_SERVLET_TYPE);
 		boolean useExisting = model.getBooleanProperty(INewServletClassDataModelProperties.USE_EXISTING_CLASS);
 		String qualifiedClassName = model.getStringProperty(INewJavaClassDataModelProperties.CLASS_NAME);
 		
 		// If it is servlet type, create the java class
 		if (isServletType && !useExisting)
 			qualifiedClassName = createServletClass();
 
 		// If the servlet is not annotated, generate the servlet metadata for the DD
 		if (!model.getBooleanProperty(IAnnotationsDataModel.USE_ANNOTATIONS))
 			generateServletMetaData(model, qualifiedClassName, isServletType);
 		
 		return OK_STATUS;
 	}
 	
 	/**
 	 * Subclasses may extend this method to add their own creation of the actual servlet java class.
 	 * This implementation uses the NewServletClassOperation which is a subclass of the NewJavaClassOperation.
 	 * The NewServletClassOperation will use the same NewServletClassDataModelProvider to retrieve the properties in
 	 * order to create the java class accordingly.  This method will not return null.
 	 * @see NewServletClassOperation
 	 * @see org.eclipse.jst.j2ee.internal.common.operations.NewJavaClassOperation
 	 * @see NewServletClassDataModelProvider
 	 * 
 	 * @return String qualified servlet classname
 	 */
 	protected String createServletClass() {
 		//	Create servlet java class file using the NewServletClassOperation.  The same data model is shared.
 		NewServletClassOperation op = new NewServletClassOperation(model);
 		try {
 			op.execute(new NullProgressMonitor(), null);
 		} catch (Exception e) {
 			Logger.getLogger().log(e);
 		} 
 		// Return the qualified classname of the newly created java class for the servlet
 		return getQualifiedClassName();
 	}
 	
 	/**
 	 * This method will return the qualified java class name as specified by the class name
 	 * and package name properties in the data model.
 	 * This method should not return null.
 	 * @see INewJavaClassDataModelProperties#CLASS_NAME
 	 * @see INewJavaClassDataModelProperties#JAVA_PACKAGE
 	 * 
 	 * @return String qualified java classname
 	 */
 	public final String getQualifiedClassName() {
 		// Use the java package name and unqualified class name to create a qualified java class name
 		String packageName = model.getStringProperty(INewJavaClassDataModelProperties.JAVA_PACKAGE);
 		String className = model.getStringProperty(INewJavaClassDataModelProperties.CLASS_NAME);
 		//Ensure the class is not in the default package before adding package name to qualified name
 		if (packageName != null && packageName.trim().length() > 0)
 			return packageName + "." + className; //$NON-NLS-1$
 		return className;
 	}
 
 	/**
 	 * Subclasses may extend this method to add their own generation steps for the creation of the
 	 * metadata for the web deployment descriptor.  This implementation uses the J2EE models to create
 	 * the Servlet model instance, any init params specified, and any servlet mappings.  It then adds 
 	 * these to the web application model.  This will then be written out to the deployment descriptor
 	 * file.  This method does not accept null parameters.
 	 * @see Servlet
 	 * @see AddServletOperation#createServlet(String, boolean)
 	 * @see AddServletOperation#setUpInitParams(List, Servlet)
 	 * @see AddServletOperation#setUpURLMappings(List, Servlet)
 	 * 
 	 * @param aModel
 	 * @param qualifiedClassName
 	 * @param isServletType
 	 */
 	protected void generateServletMetaData(IDataModel aModel, String qualifiedClassName, boolean isServletType) {
 		// Set up the servlet modelled object
 		Object servlet = createServlet(qualifiedClassName, isServletType);
 
 		// Set up the InitParams if any
 		List initParamList = (List) aModel.getProperty(INewServletClassDataModelProperties.INIT_PARAM);
 		if (initParamList != null)
 			setUpInitParams(initParamList, servlet);
 		
 		// Set up the servlet URL mappings if any
 		List urlMappingList = (List) aModel.getProperty(INewServletClassDataModelProperties.URL_MAPPINGS);
 		if (urlMappingList != null)
 			setUpURLMappings(urlMappingList, servlet);
 	}
 	
 	/**
 	 * This method is intended for private use only.  This method is used to create the servlet
 	 * modelled object, to set any parameters specified in the data model, and then to add the
 	 * servlet instance to the web application model.  This method does not accpet null parameters.
 	 * It will not return null.
 	 * @see AddServletOperation#generateServletMetaData(NewServletClassDataModel, String, boolean)
 	 * @see WebapplicationFactory#createServlet()
 	 * @see Servlet
 	 * 
 	 * @param qualifiedClassName
 	 * @param isServletType
 	 * @return Servlet instance
 	 */
 	private Object createServlet(String qualifiedClassName, boolean isServletType) {
 		// Get values from data model
 		String displayName = model.getStringProperty(INewServletClassDataModelProperties.DISPLAY_NAME);
 		String description = model.getStringProperty(INewServletClassDataModelProperties.DESCRIPTION);
 		
 		// Create the servlet instance and set up the parameters from data model
 		Object modelObject = provider.getModelObject();
 		if(modelObject instanceof org.eclipse.jst.j2ee.webapplication.WebApp ){
 		
 			Servlet servlet = WebapplicationFactory.eINSTANCE.createServlet();
 			servlet.setDisplayName(displayName);
 			servlet.setServletName(displayName);
 			servlet.setDescription(description);
 			// Handle servlet case
 			if (isServletType) {
 				ServletType servletType = WebapplicationFactory.eINSTANCE.createServletType();
 				servletType.setClassName(qualifiedClassName);
 				servlet.setWebType(servletType);
 			} 
 			// Handle JSP case
 			else {
 				JSPType jspType = WebapplicationFactory.eINSTANCE.createJSPType();
 				jspType.setJspFile(qualifiedClassName);
 				servlet.setWebType(jspType);
 			}
 			// Add the servlet to the web application model
 			
 			//WebApp webApp = (WebApp) artifactEdit.getContentModelRoot();
 			WebApp webApp = (WebApp) modelObject;
 			webApp.getServlets().add(servlet);
 			return servlet;
 		}else if(modelObject instanceof org.eclipse.jst.javaee.web.WebApp ){
 			
 			org.eclipse.jst.javaee.web.Servlet servlet = WebFactory.eINSTANCE.createServlet();
 
 			DisplayName displayNameObj = JavaeeFactory.eINSTANCE.createDisplayName();
 			displayNameObj.setValue(displayName);
 			servlet.getDisplayNames().add(displayNameObj);
 			
 			servlet.setServletName(displayName);
 			
 			org.eclipse.jst.javaee.core.Description descriptionObj = JavaeeFactory.eINSTANCE.createDescription();
 			descriptionObj.setValue(description);
 			servlet.getDescriptions().add(descriptionObj);
 			
 			// Handle servlet case
 			if (isServletType) {
 				servlet.setServletClass(qualifiedClassName);
 			} 
 			// Handle JSP case
 			else {
 				servlet.setJspFile(qualifiedClassName);
 			}
 			// Add the servlet to the web application model
 			
 			//WebApp webApp = (WebApp) artifactEdit.getContentModelRoot();
 			org.eclipse.jst.javaee.web.WebApp webApp = (org.eclipse.jst.javaee.web.WebApp)modelObject;
 			webApp.getServlets().add(servlet);	
 			return servlet;
 		}
 		// Return the servlet instance
 		return null;
 	}
 	
 	/**
 	 * This method is intended for internal use only.  This is used to create any init params
 	 * for the new servlet metadata.  It will not accept null parameters.  The init params are
 	 * set on the servlet modelled object.
 	 * @see AddServletOperation#generateServletMetaData(NewServletClassDataModel, String, boolean)
 	 * @see WebapplicationFactory#createInitParam()
 	 * 
 	 * @param initParamList
 	 * @param servlet
 	 */
 	private void setUpInitParams(List initParamList, Object servletObj) {
 		// Get the web app instance from the data model
 		Object modelObject = provider.getModelObject();
 		if(modelObject instanceof org.eclipse.jst.j2ee.webapplication.WebApp ){
 			WebApp webApp = (WebApp) modelObject;
 			Servlet servlet = (Servlet) servletObj;
 			
 			// If J2EE 1.4, add the param value and description info instances to the servlet init params
 			if (webApp.getJ2EEVersionID() >= J2EEVersionConstants.J2EE_1_4_ID) {
 				for (int iP = 0; iP < initParamList.size(); iP++) {
 					String[] stringArray = (String[]) initParamList.get(iP);
 					// Create 1.4 common param value
 					ParamValue param = CommonFactory.eINSTANCE.createParamValue();
 					param.setName(stringArray[0]);
 					param.setValue(stringArray[1]);
 					// Create 1.4 common descripton value
 					Description descriptionObj = CommonFactory.eINSTANCE.createDescription();
 					descriptionObj.setValue(stringArray[2]);
 					// Set the description on the param
 					param.getDescriptions().add(descriptionObj);
 					param.setDescription(stringArray[2]);
 					// Add the param to the servlet model list of init params
 					servlet.getInitParams().add(param);
 				}
 			}
 			// If J2EE 1.2 or 1.3, use the servlet specific init param instances
 			else {
 				for (int iP = 0; iP < initParamList.size(); iP++) {
 					String[] stringArray = (String[]) initParamList.get(iP);
 					// Create the web init param
 					InitParam ip = WebapplicationFactory.eINSTANCE.createInitParam();
 					// Set the param name
 					ip.setParamName(stringArray[0]);
 					// Set the param value
 					ip.setParamValue(stringArray[1]);
 					// Set the param description
 					ip.setDescription(stringArray[2]);
 					// Add the init param to the servlet model list of params
 					servlet.getParams().add(ip);
 				}
 			}
 		}else if(modelObject instanceof org.eclipse.jst.javaee.web.WebApp ){
 			org.eclipse.jst.javaee.web.WebApp webApp = (org.eclipse.jst.javaee.web.WebApp) modelObject;
 			org.eclipse.jst.javaee.web.Servlet servlet = (org.eclipse.jst.javaee.web.Servlet) servletObj;
 
 			for (int iP = 0; iP < initParamList.size(); iP++) {
 				String[] stringArray = (String[]) initParamList.get(iP);
 				// Create 1.4 common param value
 				org.eclipse.jst.javaee.core.ParamValue param= JavaeeFactory.eINSTANCE.createParamValue();
 				param.setParamName(stringArray[0]);
 				param.setParamValue(stringArray[1]);
 
 				org.eclipse.jst.javaee.core.Description descriptionObj = JavaeeFactory.eINSTANCE.createDescription();
 				descriptionObj.setValue(stringArray[2]);
 				// Set the description on the param
 				param.getDescriptions().add(descriptionObj);
 				// Add the param to the servlet model list of init params
 				servlet.getInitParams().add(param);
 			}
 		}
 	}
 	
 	/**
 	 * This method is intended for internal use only.  This method is used to create the servlet
 	 * mapping modelled objects so the metadata for the servlet mappings is store in the web
 	 * deployment descriptor.  This method will not accept null parameters.  The servlet mappings
 	 * are added to the web application modelled object.
 	 * @see AddServletOperation#generateServletMetaData(NewServletClassDataModel, String, boolean)
 	 * @see WebapplicationFactory#createServletMapping()
 	 * 
 	 * @param urlMappingList
 	 * @param servlet
 	 */
 	private void setUpURLMappings(List urlMappingList, Object servletObj) {
 		// Get the web app modelled object from the data model
 		//WebApp webApp = (WebApp) artifactEdit.getContentModelRoot();
 		Object modelObject = provider.getModelObject();
 
 		// Create the servlet mappings if any
 		if(modelObject instanceof org.eclipse.jst.j2ee.webapplication.WebApp ){	
 			WebApp webApp = (WebApp) modelObject;
 			Servlet servlet = (Servlet) servletObj;
 			for (int iM = 0; iM < urlMappingList.size(); iM++) {
 					String[] stringArray = (String[]) urlMappingList.get(iM);
 					// Create the servlet mapping instance from the web factory
 					ServletMapping mapping = WebapplicationFactory.eINSTANCE.createServletMapping();
 					// Set the servlet and servlet name
 					mapping.setServlet(servlet);
 					mapping.setName(servlet.getServletName());
 					// Set the URL pattern to map the servlet to
 					mapping.setUrlPattern(stringArray[0]);
 					// Add the servlet mapping to the web application modelled list
 					webApp.getServletMappings().add(mapping);
 			}
 		}else if (modelObject instanceof org.eclipse.jst.javaee.web.WebApp ){
 			org.eclipse.jst.javaee.web.WebApp webApp = (org.eclipse.jst.javaee.web.WebApp) modelObject;
 			org.eclipse.jst.javaee.web.Servlet servlet = (org.eclipse.jst.javaee.web.Servlet) servletObj;
			
			// Create the servlet mappings if any
			if (urlMappingList.size() > 0) {
 				// Create the servlet mapping instance from the web factory
 				org.eclipse.jst.javaee.web.ServletMapping mapping = WebFactory.eINSTANCE.createServletMapping();
 
 				mapping.setServletName(servlet.getServletName());
 				for (int i = 0; i < urlMappingList.size(); i++) {
					String[] stringArray = (String[]) urlMappingList.get(i);
					// Set the URL pattern to map the servlet to
					UrlPatternType url = JavaeeFactory.eINSTANCE.createUrlPatternType();
					url.setValue(stringArray[0]);
					mapping.getUrlPatterns().add(url);
				}
				// Add the servlet mapping to the web application model list
				webApp.getServletMappings().add(mapping);
			}			
 		}
 	}
 
 	public IProject getTargetProject() {
 		String projectName = model.getStringProperty(IArtifactEditOperationDataModelProperties.PROJECT_NAME);
 		return ProjectUtilities.getProject(projectName);
 	}
 	
 	@Override
 	public IStatus execute(final IProgressMonitor monitor, final IAdaptable info) throws ExecutionException {
 		Runnable runnable = null;
 		try {
 			Object ctx = null;
 			if( UIContextDetermination.getCurrentContext() == UIContextDetermination.UI_CONTEXT ){
 				Display display = Display.getCurrent();
 				if(display != null )
 					ctx = display.getActiveShell();
 			}
 
 			if (provider.validateEdit(null,ctx).isOK()){
 				runnable = new Runnable(){
 
 					public void run() {
 						try {
 							doExecute(monitor, info);
 						} catch (ExecutionException e) {
 							e.printStackTrace();
 						}
 					}
 				};
 				provider.modify(runnable, null);
 			}
 			//return doExecute(monitor, info);
 			return Status.CANCEL_STATUS;
 		} finally {
 
 		}		
 	}
 }
