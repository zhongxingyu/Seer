 package org.eclipse.jst.j2ee.jca.modulecore.util;
 
 import java.util.List;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.Archive;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.CommonarchiveFactory;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.exception.OpenFailureException;
 import org.eclipse.jst.j2ee.componentcore.EnterpriseArtifactEdit;
 import org.eclipse.jst.j2ee.internal.J2EEConstants;
 import org.eclipse.jst.j2ee.internal.common.XMLResource;
 import org.eclipse.jst.j2ee.internal.jca.archive.operations.ConnectorComponentLoadStrategyImpl;
 import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
 import org.eclipse.jst.j2ee.jca.Connector;
 import org.eclipse.jst.j2ee.jca.ConnectorResource;
 import org.eclipse.jst.j2ee.jca.JcaFactory;
 import org.eclipse.wst.common.componentcore.ArtifactEdit;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.ModuleCoreNature;
 import org.eclipse.wst.common.componentcore.UnresolveableURIException;
 import org.eclipse.wst.common.componentcore.internal.ArtifactEditModel;
 import org.eclipse.wst.common.componentcore.internal.StructureEdit;
 import org.eclipse.wst.common.componentcore.internal.impl.ModuleURIUtil;
 import org.eclipse.wst.common.componentcore.internal.util.IArtifactEditFactory;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 
 /**
  * Provides access to J2EE Connector models using the ArtifactEdit pattern.
  * <p>
  * Clients should use getConnectorArtifactForRead/Write() to acquire an instance
  * of the correct artifact edit class.
  * </p>
  */
 public class ConnectorArtifactEdit extends EnterpriseArtifactEdit implements IArtifactEditFactory {
 	/**
 	 * <p>
 	 * Identifier used to link ConnectorArtifactEdit to a
 	 * ConnectorEditAdapterFactory {@see ConnectorEditAdapterFactory} stored in
 	 * an AdapterManger (@see AdapterManager)
 	 * </p>
 	 */
 
 	public static final Class ADAPTER_TYPE = ConnectorArtifactEdit.class;
 
 	/**
 	 * 
 	 */
 	public ConnectorArtifactEdit() {
 		super();
 	}
 
 	/**
 	 * @param aHandle
 	 * @param toAccessAsReadOnly
 	 * @throws IllegalArgumentException
 	 */
 	public ConnectorArtifactEdit(IProject aProject, boolean toAccessAsReadOnly) throws IllegalArgumentException {
 		super(aProject, toAccessAsReadOnly);
 	}
 	
	protected ConnectorArtifactEdit(IProject aProject, boolean toAccessAsReadOnly, boolean forCreate) throws IllegalArgumentException {
 		super(aProject, toAccessAsReadOnly, forCreate, J2EEProjectUtilities.JCA);
 	}
 
 	/**
 	 * <p>
 	 * Creates an instance facade for the given {@see ArtifactEditModel}.
 	 * </p>
 	 * 
 	 * @param anArtifactEditModel
 	 */
 
 	public ConnectorArtifactEdit(ArtifactEditModel anArtifactEditModel) {
 		super(anArtifactEditModel);
 	}
 
 	/**
 	 * <p>
 	 * Creates an instance facade for the given {@see ArtifactEditModel}
 	 * </p>
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
 
 	protected ConnectorArtifactEdit(ModuleCoreNature aNature, IVirtualComponent aModule, boolean toAccessAsReadOnly) {
 		super(aNature, aModule, toAccessAsReadOnly);
 	}
 
 	/**
 	 * @return ConnectorResource from (@link getDeploymentDescriptorResource())
 	 */
 
 	public ConnectorResource getConnectorXmiResource() {
 		return (ConnectorResource) getDeploymentDescriptorResource();
 	}
 
 	/**
 	 * <p>
 	 * Retrieves the underlying resource from the ArtifactEditModel using
 	 * defined URI.
 	 * </p>
 	 * 
 	 * @return Resource
 	 */
 
 	public Resource getDeploymentDescriptorResource() {
 		return getArtifactEditModel().getResource(J2EEConstants.RAR_DD_URI_OBJ);
 	}
 
 	/**
 	 * <p>
 	 * Obtains the Connector (@see Connector) root object from the
 	 * ConnectorResource. If the root object does not exist, then one is created
 	 * (@link addConnectorIfNecessary(getConnectorXmiResource())). The root
 	 * object contains all other resource defined objects.
 	 * </p>
 	 * 
 	 * @return EObject
 	 */
 	public EObject getDeploymentDescriptorRoot() {
 		List contents = getDeploymentDescriptorResource().getContents();
 		if (contents.size() > 0)
 			return (EObject) contents.get(0);
 		addConnectorIfNecessary(getConnectorXmiResource());
 		return (EObject) contents.get(0);
 	}
 
 	/**
 	 * <p>
 	 * Creates a deployment descriptor root object (Connector) and populates
 	 * with data. Adds the root object to the deployment descriptor resource.
 	 * </p>
 	 * <p>
 	 * 
 	 * @param aModule
 	 *            A non-null pointing to a {@see XMLResource} Note: This method
 	 *            is typically used for JUNIT - move?
 	 *            </p>
 	 */
 	protected void addConnectorIfNecessary(XMLResource aResource) {
 
 		if (aResource != null && aResource.getContents().isEmpty()) {
 			Connector connector = JcaFactory.eINSTANCE.createConnector();
 			aResource.getContents().add(connector);
 			URI moduleURI = getArtifactEditModel().getModuleURI();
 			try {
 				connector.setDisplayName(StructureEdit.getDeployedName(moduleURI));
 			} catch (UnresolveableURIException e) {
 				//Ignore
 			}
 			aResource.setID(connector, J2EEConstants.CONNECTOR_ID);
 			// TODO add more mandatory elements
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
 	public static ConnectorArtifactEdit getConnectorArtifactEditForRead(IProject aProject) {
 		ConnectorArtifactEdit artifactEdit = null;
 		try {
 			if (isValidConnectorModule(ComponentCore.createComponent(aProject)))
 				artifactEdit = new ConnectorArtifactEdit(aProject, true);
 		} catch (Exception e) {
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
 	public static ConnectorArtifactEdit getConnectorArtifactEditForWrite(IProject aProject) {
 		ConnectorArtifactEdit artifactEdit = null;
 		try {
 			if (isValidConnectorModule(ComponentCore.createComponent(aProject)))
 				artifactEdit = new ConnectorArtifactEdit(aProject, false);
 		} catch (Exception e) {
 			artifactEdit = null;
 		}
 		return artifactEdit;
 	}
 
 	/**
 	 * <p>
 	 * Returns an instance facade to manage the underlying edit model for the
 	 * given {@see WorkbenchComponent}. Instances of ConnectorArtifactEdit that
 	 * are returned through this method must be {@see #dispose()}ed of when no
 	 * longer in use.
 	 * </p>
 	 * <p>
 	 * Use to acquire an ConnectorArtifactEdit facade for a specific
 	 * {@see WorkbenchComponent}&nbsp;that will not be used for editing.
 	 * Invocations of any save*() API on an instance returned from this method
 	 * will throw exceptions.
 	 * </p>
 	 * <p>
 	 * <b>This method may return null. </b>
 	 * </p>
 	 * <p>
 	 * Note: This method is for internal use only. Clients should not call this
 	 * method.
 	 * </p>
 	 * 
 	 * @param aModule
 	 *            A valid {@see WorkbenchComponent}&nbsp;with a handle that
 	 *            resolves to an accessible project in the workspace
 	 * @return An instance of ConnectorArtifactEdit that may only be used to
 	 *         read the underlying content model
 	 * @throws UnresolveableURIException
 	 *             could not resolve uri.
 	 */
 	public static ConnectorArtifactEdit getConnectorArtifactEditForRead(IVirtualComponent aModule) {
 		try {
 			if (isValidConnectorModule(aModule)) {
 				IProject project = aModule.getProject();
 				ModuleCoreNature nature = ModuleCoreNature.getModuleCoreNature(project);
 				return new ConnectorArtifactEdit(nature, aModule, true);
 			}
 		} catch (UnresolveableURIException uue) {
 			//Ignore
 		}
 		return null;
 	}
 
 	/**
 	 * <p>
 	 * Returns an instance facade to manage the underlying edit model for the
 	 * given {@see WorkbenchComponent}. Instances of ConnectorArtifactEdit that
 	 * are returned through this method must be {@see #dispose()}ed of when no
 	 * longer in use.
 	 * </p>
 	 * <p>
 	 * Use to acquire an ConnectorArtifactEdit facade for a specific
 	 * {@see WorkbenchComponent}&nbsp;that will be used for editing.
 	 * </p>
 	 * <p>
 	 * <b>This method may return null. </b>
 	 * </p>
 	 * <p>
 	 * Note: This method is for internal use only. Clients should not call this
 	 * method.
 	 * </p>
 	 * 
 	 * @param aModule
 	 *            A valid {@see WorkbenchComponent}&nbsp;with a handle that
 	 *            resolves to an accessible project in the workspace
 	 * @return An instance of ConnectorArtifactEdit that may be used to modify
 	 *         and persist changes to the underlying content model
 	 */
 	public static ConnectorArtifactEdit getConnectorArtifactEditForWrite(IVirtualComponent aModule) {
 		try {
 			if (isValidConnectorModule(aModule)) {
 				IProject project = aModule.getProject();
 				ModuleCoreNature nature = ModuleCoreNature.getModuleCoreNature(project);
 				return new ConnectorArtifactEdit(nature, aModule, false);
 			}
 		} catch (UnresolveableURIException uue) {
 			//Ignore
 		}
 		return null;
 	}
 
 	/**
 	 * @param module
 	 *            A {@see WorkbenchComponent}
 	 * @return True if the supplied module
 	 *         {@see ArtifactEdit#isValidEditableModule(WorkbenchComponent)}and
 	 *         the moduleTypeId is a JST module
 	 */
 	public static boolean isValidConnectorModule(IVirtualComponent aModule) throws UnresolveableURIException {
 		if (!isValidEditableModule(aModule))
 			return false;
 		/* and match the JST_Connector_MODULE type */
 		return J2EEProjectUtilities.isJCAProject(aModule.getProject());
 	}
 
 	/**
 	 * <p>
 	 * Retrieves J2EE version information from ConnectorResource.
 	 * </p>
 	 * 
 	 * @return an integer representation of a J2EE Spec version
 	 */
 
 	public int getJ2EEVersion() {
 		return getConnectorXmiResource().getJ2EEVersionID();
 	}
 
 	/**
 	 * @return Connector from (@link getDeploymentDescriptorRoot())
 	 */
 	public Connector getConnector() {
 		return (Connector) getDeploymentDescriptorRoot();
 	}
 
 	public EObject createModelRoot() {
 		return createModelRoot(getJ2EEVersion());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.j2ee.internal.modulecore.util.EnterpriseArtifactEdit#createModelRoot(java.lang.Integer)
 	 */
 	public EObject createModelRoot(int version) {
 		ConnectorResource res = (ConnectorResource) getDeploymentDescriptorResource();
 		res.setModuleVersionID(version);
 		addConnectorIfNecessary(res);
 		return ((ConnectorResource) getDeploymentDescriptorResource()).getRootObject();
 	}
 
 	public ArtifactEdit createArtifactEditForRead(IVirtualComponent aComponent) {
 		
 		return getConnectorArtifactEditForRead(aComponent);
 	}
 
 	public ArtifactEdit createArtifactEditForWrite(IVirtualComponent aComponent) {
 		return getConnectorArtifactEditForWrite(aComponent);
 	}
 	
 	public Archive asArchive(boolean includeSource) throws OpenFailureException{
 		ConnectorComponentLoadStrategyImpl loader = new ConnectorComponentLoadStrategyImpl(getComponent());
 		loader.setExportSource(includeSource);
 		String uri = ModuleURIUtil.getHandleString(getComponent());
 		return CommonarchiveFactory.eINSTANCE.openRARFile(loader, uri);
 	}
 	
 	public static void createDeploymentDescriptor(IProject project, int version) {
 		ConnectorArtifactEdit jcaEdit = new ConnectorArtifactEdit(project,false,true);
 		try {
 			jcaEdit.createModelRoot(version);
 			jcaEdit.save(null);
 		} finally {
 			jcaEdit.dispose();
 		} 
 	}
 }
