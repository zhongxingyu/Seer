 package org.eclipse.jst.j2ee.internal.webservice.componentcore.util;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.jst.j2ee.componentcore.EnterpriseArtifactEdit;
 import org.eclipse.jst.j2ee.internal.J2EEConstants;
 import org.eclipse.jst.j2ee.internal.webservices.WSDLServiceExtManager;
 import org.eclipse.jst.j2ee.internal.webservices.WSDLServiceHelper;
 import org.eclipse.jst.j2ee.webservice.wsdd.WebServices;
 import org.eclipse.jst.j2ee.webservice.wsdd.WsddFactory;
 import org.eclipse.jst.j2ee.webservice.wsdd.WsddResource;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.ModuleCoreNature;
 import org.eclipse.wst.common.componentcore.UnresolveableURIException;
 import org.eclipse.wst.common.componentcore.internal.ArtifactEditModel;
 import org.eclipse.wst.common.componentcore.internal.util.IModuleConstants;
 import org.eclipse.wst.common.componentcore.resources.ComponentHandle;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 
 /**
  * <p>
  * WSDDArtifactEdit obtains a WS Deployment Descriptor metamodel specifec data
  * from a {@see org.eclipse.jst.j2ee.ejb.EJBResource}&nbsp; which stores the
  * metamodel. The {@see org.eclipse.jst.j2ee.ejb.EJBResource}&nbsp;is retrieved
  * from the {@see org.eclipse.wst.common.modulecore.ArtifactEditModel}&nbsp;using
  * a constant {@see J2EEConstants#EJBJAR_DD_URI_OBJ}. The defined methods
  * extract data or manipulate the contents of the underlying resource.
  * </p>
  * 
  */
 public class WSDDArtifactEdit extends EnterpriseArtifactEdit {
 
 	/**
 	 * <p>
 	 * Identifier used to link WSDDArtifactEdit to a WsddAdapterFactory {@see
 	 * WsddAdapterFactory} stored in an AdapterManger (@see AdapterManager)
 	 * </p>
 	 */
 
 	public static final Class ADAPTER_TYPE = WSDDArtifactEdit.class;
 	
 	public static final String WSIL_FILE_EXT = "wsil"; //$NON-NLS-1$
 	public static final String WSDL_FILE_EXT = "wsdl"; //$NON-NLS-1$
 
 
 	/**
 	 * @param aHandle
 	 * @param toAccessAsReadOnly
 	 * @throws IllegalArgumentException
 	 */
 	public WSDDArtifactEdit(ComponentHandle aHandle, boolean toAccessAsReadOnly) throws IllegalArgumentException {
 		super(aHandle, toAccessAsReadOnly);
 		// TODO Auto-generated constructor stub
 	}
 
 	/**
 	 * <p>
 	 * Creates an instance facade for the given {@see ArtifactEditModel}.
 	 * </p>
 	 * 
 	 * @param anArtifactEditModel
 	 */
 	public WSDDArtifactEdit(ArtifactEditModel model) {
 		super(model);
 	}
 
 	/**
 	 * <p>
 	 * Creates an instance facade for the given {@see ArtifactEditModel}
 	 * </p>
 	 * 
 	 * <p>
 	 * Note: This method is for internal use only. Clients should not call this
 	 * method.
 	 * </p>
 	 * 
 	 * @param aNature
 	 *            A non-null {@see ModuleCoreNature}for an accessible project
 	 * @param aModule
 	 *            A non-null {@see WorkbenchComponent}pointing to a module from
 	 *            the given {@see ModuleCoreNature}
 	 */
 	public WSDDArtifactEdit(ModuleCoreNature aNature, IVirtualComponent aModule, boolean toAccessAsReadOnly) {
 		super(aNature, aModule, toAccessAsReadOnly);
 	}
 
 	/**
 	 * 
 	 * @return WsddResource from (@link getDeploymentDescriptorResource())
 	 * 
 	 */
 
 	public WsddResource getWsddXmiResource() {
 		return (WsddResource) getDeploymentDescriptorResource();
 	}
 
 	/**
 	 * <p>
 	 * Retrieves J2EE version information from EJBResource.
 	 * </p>
 	 * 
 	 * @return an integer representation of a J2EE Spec version
 	 * 
 	 */
 
 	public int getJ2EEVersion() {
 		return getWsddXmiResource().getJ2EEVersionID();
 	}
 
 	/**
 	 * <p>
 	 * Retrieves the underlying resource from the ArtifactEditModel using
 	 * defined URI.
 	 * </p>
 	 * 
 	 * @return Resource
 	 * 
 	 */
 
 	public Resource getDeploymentDescriptorResource() {
 		return getArtifactEditModel().getResource(getWebServicesXmlResourceURI());
 	}
 
 	public URI getWebServicesXmlResourceURI() {
 
 		URI resourceURI = J2EEConstants.WEB_SERVICES_WEB_INF_DD_URI_OBJ;
 		if (isValidAppClientModule(ComponentCore.createComponent(getComponentHandle().getProject(), getComponentHandle().getName())))
 			resourceURI = J2EEConstants.WEB_SERVICES_META_INF_DD_URI_OBJ;
 		else if (isValidEJBModule(ComponentCore.createComponent(getComponentHandle().getProject(), getComponentHandle().getName())))
 			resourceURI = J2EEConstants.WEB_SERVICES_META_INF_DD_URI_OBJ; //$NON-NLS-1$
 		return resourceURI;
 	}
 
 	/**
 	 * 
 	 * @return WebServices from (@link getDeploymentDescriptorRoot())
 	 * 
 	 */
 	public WebServices getWebServices() {
 		return (WebServices) getDeploymentDescriptorRoot();
 	}
 
 	/**
 	 * <p>
 	 * Obtains the WebServices (@see WebServices) root object from the
 	 * WsddResource. If the root object does not exist, then one is created
 	 * (@link addEJBJarIfNecessary(getEJBJarXmiResource())). The root object
 	 * contains all other resource defined objects.
 	 * </p>
 	 * 
 	 * @return EObject
 	 * 
 	 */
 	public EObject getDeploymentDescriptorRoot() {
 		List contents = getDeploymentDescriptorResource().getContents();
 		if (contents.size() > 0)
 			return (EObject) contents.get(0);
 		addWebServicesIfNecessary(getWsddXmiResource());
 		return (EObject) contents.get(0);
 	}
 
 	/**
 	 * <p>
 	 * Creates a deployment descriptor root object (WebServices) and populates
 	 * with data. Adds the root object to the deployment descriptor resource.
 	 * </p>
 	 * 
 	 * <p>
 	 * 
 	 * @param aModule
 	 *            A non-null pointing to a {@see XMLResource} Note: This method
 	 *            is typically used for JUNIT - move?
 	 *            </p>
 	 */
 	protected void addWebServicesIfNecessary(WsddResource aResource) {
 		if (aResource != null) {
 			if (aResource.getContents() == null || aResource.getContents().isEmpty()) {
 				WebServices ws = WsddFactory.eINSTANCE.createWebServices();
 				aResource.getContents().add(ws);
 			}
 			WebServices ws = (WebServices) aResource.getContents().get(0);
 			URI moduleURI = getArtifactEditModel().getModuleURI();
 			try {
 				aResource.saveIfNecessary();
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * <p>
 	 * Returns an instance facade to manage the underlying edit model for the
 	 * given {@see WorkbenchComponent}. Instances of ArtifactEdit that are
 	 * returned through this method must be {@see #dispose()}ed of when no
 	 * longer in use.
 	 * </p>
 	 * <p>
 	 * Use to acquire an ArtifactEdit facade for a specific
 	 * {@see WorkbenchComponent}&nbsp;that will not be used for editing.
 	 * Invocations of any save*() API on an instance returned from this method
 	 * will throw exceptions.
 	 * </p>
 	 * <p>
 	 * <b>The following method may return null. </b>
 	 * </p>
 	 * 
 	 * @param aModule
 	 *            A valid {@see WorkbenchComponent}&nbsp;with a handle that
 	 *            resolves to an accessible project in the workspace
 	 * @return An instance of ArtifactEdit that may only be used to read the
 	 *         underlying content model
 	 */
 	public static WSDDArtifactEdit getWSDDArtifactEditForRead(ComponentHandle aHandle) {
 		WSDDArtifactEdit artifactEdit = null;
 		try {
 			artifactEdit = new WSDDArtifactEdit(aHandle, true);
 		} catch (IllegalArgumentException iae) {
 			artifactEdit = null;
 		}
 		return artifactEdit;
 	}
 	/**
 	 * <p>
 	 * Returns an instance facade to manage the underlying edit model for the
 	 * given {@see WorkbenchComponent}. Instances of ArtifactEdit that are
 	 * returned through this method must be {@see #dispose()}ed of when no
 	 * longer in use.
 	 * </p>
 	 * <p>
 	 * Use to acquire an ArtifactEdit facade for a specific
 	 * {@see WorkbenchComponent}&nbsp;that will be used for editing.
 	 * </p>
 	 * <p>
 	 * <b>The following method may return null. </b>
 	 * </p>
 	 * 
 	 * @param aModule
 	 *            A valid {@see WorkbenchComponent}&nbsp;with a handle that
 	 *            resolves to an accessible project in the workspace
 	 * @return An instance of ArtifactEdit that may be used to modify and
 	 *         persist changes to the underlying content model
 	 */
 	public static WSDDArtifactEdit getWSDDArtifactEditForWrite(ComponentHandle aHandle) {
 		WSDDArtifactEdit artifactEdit = null;
 		try {
 			artifactEdit = new WSDDArtifactEdit(aHandle, false);
 		} catch (IllegalArgumentException iae) {
 			artifactEdit = null;
 		}
 		return artifactEdit;
 	}
 
 	/**
 	 * <p>
 	 * Returns an instance facade to manage the underlying edit model for the
 	 * given {@see WorkbenchComponent}. Instances of WSDDArtifactEdit that are
 	 * returned through this method must be {@see #dispose()}ed of when no
 	 * longer in use.
 	 * </p>
 	 * <p>
 	 * Use to acquire an WSDDArtifactEdit facade for a specific
 	 * {@see WorkbenchComponent}&nbsp;that will not be used for editing.
 	 * Invocations of any save*() API on an instance returned from this method
 	 * will throw exceptions.
 	 * </p>
 	 * <p>
 	 * <b>This method may return null. </b>
 	 * </p>
 	 * 
 	 * <p>
 	 * Note: This method is for internal use only. Clients should not call this
 	 * method.
 	 * </p>
 	 * 
 	 * @param aModule
 	 *            A valid {@see WorkbenchComponent}&nbsp;with a handle that
 	 *            resolves to an accessible project in the workspace
 	 * @return An instance of WSDDArtifactEdit that may only be used to read the
 	 *         underlying content model
 	 * @throws UnresolveableURIException
 	 *             could not resolve uri.
 	 */
 	public static WSDDArtifactEdit getWSDDArtifactEditForRead(IVirtualComponent aModule) {
 		IProject project = aModule.getProject();
 		ModuleCoreNature nature = ModuleCoreNature.getModuleCoreNature(project);
 		return new WSDDArtifactEdit(nature, aModule, true);
 	}
 
 	/**
 	 * <p>
 	 * Returns an instance facade to manage the underlying edit model for the
 	 * given {@see WorkbenchComponent}. Instances of EJBArtifactEdit that are
 	 * returned through this method must be {@see #dispose()}ed of when no
 	 * longer in use.
 	 * </p>
 	 * <p>
 	 * Use to acquire an WSDDArtifactEdit facade for a specific
 	 * {@see WorkbenchComponent}&nbsp;that will be used for editing.
 	 * </p>
 	 * <p>
 	 * <b>This method may return null. </b>
 	 * </p>
 	 * 
 	 * <p>
 	 * Note: This method is for internal use only. Clients should not call this
 	 * method.
 	 * </p>
 	 * 
 	 * @param aModule
 	 *            A valid {@see WorkbenchComponent}&nbsp;with a handle that
 	 *            resolves to an accessible project in the workspace
 	 * @return An instance of WSDDArtifactEdit that may be used to modify and
 	 *         persist changes to the underlying content model
 	 */
 	public static WSDDArtifactEdit getWSDDArtifactEditForWrite(IVirtualComponent aModule) {
 		IProject project = aModule.getProject();
 		ModuleCoreNature nature = ModuleCoreNature.getModuleCoreNature(project);
 		return new WSDDArtifactEdit(nature, aModule, false);
 	}
 
 	/**
 	 * @param component
 	 *            A {@see IVirtualComponent}
 	 * @return True if the supplied module
 	 *         {@see ArtifactEdit#isValidEditableModule(IVirtualComponent)}and
 	 *         the moduleTypeId is a JST module
 	 */
 	public static boolean isValidEJBModule(IVirtualComponent aComponent) {
 
 		/* and match the JST_EJB_MODULE type */
 		if (!IModuleConstants.JST_EJB_MODULE.equals(aComponent.getComponentTypeId()))
 			return false;
 		return true;
 	}
 	/**
 	 * @param component
 	 *            A {@see IVirtualComponent}
 	 * @return True if the supplied module
 	 *         {@see ArtifactEdit#isValidEditableModule(IVirtualComponent)}and
 	 *         the moduleTypeId is a JST module
 	 */
 	public static boolean isValidWebModule(IVirtualComponent aComponent) {
 
 		/* and match the JST_WEB_MODULE type */
 		if (!IModuleConstants.JST_WEB_MODULE.equals(aComponent.getComponentTypeId()))
 			return false;
 		return true;
 	}
 	/**
 	 * @param component
 	 *            A {@see IVirtualComponent}
 	 * @return True if the supplied module
 	 *         {@see ArtifactEdit#isValidEditableModule(IVirtualComponent)}and
 	 *         the moduleTypeId is a JST module
 	 */
 	public static boolean isValidAppClientModule(IVirtualComponent aComponent) {
 
 		/* and match the JST_AppClient_MODULE type */
 		if (!IModuleConstants.JST_APPCLIENT_MODULE.equals(aComponent.getComponentTypeId()))
 			return false;
 		return true;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.j2ee.internal.modulecore.util.EnterpriseArtifactEdit#createModelRoot()
 	 */
 	public EObject createModelRoot() {
 		return createModelRoot(getJ2EEVersion());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.j2ee.internal.modulecore.util.EnterpriseArtifactEdit#createModelRoot(int)
 	 */
 	public EObject createModelRoot(int version) {
 		WsddResource res = (WsddResource) getDeploymentDescriptorResource();
 		res.setModuleVersionID(version);
 		addWebServicesIfNecessary(res);
 		return getWebServices();
 	}
 
 	public EObject getContentModelRoot() {
 		return getWebServices();
 	}
 	
 	public List getWSILResources() {
 		return getResources(WSIL_FILE_EXT);
 	}
 
 	public List getWSDLResources() {
 		return getResources(WSDL_FILE_EXT);
 	}
 
 	private List getResources(String ext) {
 		List resources = getArtifactEditModel().getResources();
 		List result = new ArrayList();
 		for (int i = 0; i < resources.size(); i++) {
 			Resource res = (Resource) resources.get(i);
 			if (res != null && res.getURI().fileExtension() != null && res.getURI().fileExtension().equals(ext))
 				result.add(res);
 		}
 		return result;
 	}
 	
 	/**
 	 * return the WSDLResource if it exists, otherwise return null
 	 */
 	public Resource getWsdlResource(String path) {
 		if (path == null || path.equals(""))return null; //$NON-NLS-1$
 		Resource res = null;
 		try {
 			res = getArtifactEditModel().getResource(URI.createURI(path));
 		} catch (Exception e) {
 			//Ignore
 		}
 		WSDLServiceHelper serviceHelper = WSDLServiceExtManager.getServiceHelper();
 		if (res != null && res.isLoaded() && serviceHelper.isWSDLResource(res))
 			return res;
 		return null;
 	}
 }
