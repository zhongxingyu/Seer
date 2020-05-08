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
  * Created on Feb 9, 2004
  *
  * To change the template for this generated file go to
  * Window - Preferences - Java - Code Generation - Code and Comments
  */
 
 
 
 package org.eclipse.jst.j2ee.internal.webservice.helper;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceChangeListener;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IResourceDeltaVisitor;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.jem.util.emf.workbench.ProjectUtilities;
 import org.eclipse.jem.util.emf.workbench.WorkbenchResourceHelperBase;
 import org.eclipse.jem.util.logger.proxy.Logger;
 import org.eclipse.jst.j2ee.client.ApplicationClient;
 import org.eclipse.jst.j2ee.ejb.EJBJar;
 import org.eclipse.jst.j2ee.ejb.EnterpriseBean;
 import org.eclipse.jst.j2ee.internal.J2EEVersionConstants;
 import org.eclipse.jst.j2ee.internal.webservice.componentcore.util.WSCDDArtifactEdit;
 import org.eclipse.jst.j2ee.internal.webservice.componentcore.util.WSDDArtifactEdit;
 import org.eclipse.jst.j2ee.internal.webservices.WSDLServiceExtManager;
 import org.eclipse.jst.j2ee.internal.webservices.WSDLServiceHelper;
 import org.eclipse.jst.j2ee.webapplication.WebApp;
 import org.eclipse.jst.j2ee.webservice.wsclient.ComponentScopedRefs;
 import org.eclipse.jst.j2ee.webservice.wsclient.ServiceRef;
 import org.eclipse.jst.j2ee.webservice.wsclient.WebServicesClient;
 import org.eclipse.jst.j2ee.webservice.wsclient.WebServicesResource;
 import org.eclipse.jst.j2ee.webservice.wsdd.PortComponent;
 import org.eclipse.jst.j2ee.webservice.wsdd.ServiceImplBean;
 import org.eclipse.jst.j2ee.webservice.wsdd.WebServiceDescription;
 import org.eclipse.jst.j2ee.webservice.wsdd.WebServices;
 import org.eclipse.jst.j2ee.webservice.wsdd.WsddResource;
 import org.eclipse.wst.common.componentcore.ArtifactEdit;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.internal.ArtifactEditModel;
 import org.eclipse.wst.common.componentcore.resources.ComponentHandle;
 import org.eclipse.wst.common.componentcore.resources.IFlexibleProject;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualResource;
 import org.eclipse.wst.common.internal.emfworkbench.WorkbenchResourceHelper;
 import org.eclipse.wst.common.internal.emfworkbench.integration.EditModelEvent;
 import org.eclipse.wst.common.internal.emfworkbench.integration.EditModelListener;
 
 /**
  * @author jlanuti
  * 
  * To change the template for this generated type comment go to Window - Preferences - Java - Code
  * Generation - Code and Comments
  */
 public class WebServicesManager implements EditModelListener, IResourceChangeListener, IResourceDeltaVisitor {
 
 	private List wsArtifactEdits;
 	private List wsClientArtifactEdits;
 	private static WebServicesManager INSTANCE = null;
 	private List listeners;
 	private List removedListeners = new ArrayList();
 	private boolean isNotifing = false;
 
 	public static final String WSDL_EXT = "wsdl"; //$NON-NLS-1$
 	public static final String WSIL_EXT = "wsil"; //$NON-NLS-1$
 
 	public static WebServicesManager getInstance() {
 		if (INSTANCE == null)
 			INSTANCE = new WebServicesManager();
 		return INSTANCE;
 	}
 
 	/**
 	 * Default Constructor
 	 */
 	public WebServicesManager() {
 		super();
 		init();
 	}
 
 	private void init() {
 		collectArtifactEdits();
 		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
 	}
 
 	private void collectArtifactEdits() {
 		IProject[] projects = ProjectUtilities.getAllProjects();
 		for (int i = 0; i < projects.length; i++) {
 			IProject project = projects[i];
 			WSDDArtifactEdit wsddArtifactEdit = null;
 			WSCDDArtifactEdit wscddArtifactEdit = null;
 			IFlexibleProject flexProject = ComponentCore.createFlexibleProject(project);
 			if (flexProject==null) continue;
 			IVirtualComponent[] components = flexProject.getComponents();
 			if (components==null) continue;
 			for (int j=0; j<components.length; j++) {
 				//TODO implement a smarter solution than try catchs
 				try {
 				wsddArtifactEdit = WSDDArtifactEdit.getWSDDArtifactEditForRead(components[j]);
 				if (wsddArtifactEdit != null) {
 					wsddArtifactEdit.addListener(this);
 					getWSArtifactEdits().add(wsddArtifactEdit);
 				}
 				} catch (Exception e) {
 					if (wsddArtifactEdit != null) wsddArtifactEdit.dispose();
 				}
 				try {
 				wscddArtifactEdit = WSCDDArtifactEdit.getWSCDDArtifactEditForRead(components[j]);
 				if (wscddArtifactEdit != null) {
 					wscddArtifactEdit.addListener(this);
 					getWSClientArtifactEdits().add(wscddArtifactEdit);
 				}
 				} catch (Exception e) {
 					if (wscddArtifactEdit != null) wscddArtifactEdit.dispose();
 				}
 			}
 		}
 	}
 
 	/**
 	 * @return Returns the editModels.
 	 */
 	private List getWSArtifactEdits() {
 		if (wsArtifactEdits == null)
 			wsArtifactEdits = new ArrayList();
 		return wsArtifactEdits;
 	}
 	
 	/**
 	 * @return Returns the editModels.
 	 */
 	private List getWSClientArtifactEdits() {
 		if (wsClientArtifactEdits == null)
 			wsClientArtifactEdits = new ArrayList();
 		return wsClientArtifactEdits;
 	}
 
 	private List getListeners() {
 		if (listeners == null)
 			listeners = new ArrayList();
 		return listeners;
 	}
 
 	/**
 	 * Add aListener to the list of listeners.
 	 */
 	public void addListener(EditModelListener aListener) {
 		if (aListener != null && !getListeners().contains(aListener))
 			getListeners().add(aListener);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.wst.common.internal.emfworkbench.integration.EditModelListener#editModelChanged(org.eclipse.wst.common.internal.emfworkbench.integration.EditModelEvent)
 	 */
 	public void editModelChanged(EditModelEvent anEvent) {
 		if (anEvent.getEventCode() == EditModelEvent.PRE_DISPOSE) {
 			ArtifactEditModel editModel = (ArtifactEditModel) anEvent.getEditModel();
 			ComponentHandle handle = editModel.getComponentHandle();
 			WSDDArtifactEdit wsArtifactEdit = getWSArtifactEdit(handle);
 			getWSArtifactEdits().remove(wsArtifactEdit);
 			wsArtifactEdit.removeListener(this);
 			wsArtifactEdit.dispose();
 			WSCDDArtifactEdit wsClientArtifactEdit = getWSClientArtifactEdit(handle);
 			getWSClientArtifactEdits().remove(wsClientArtifactEdit);
 			wsClientArtifactEdit.removeListener(this);
 			wsClientArtifactEdit.dispose();
 		}
 		notifyListeners(anEvent);
 	}
 	
 	private WSDDArtifactEdit getWSArtifactEdit(ComponentHandle handle) {
 		for (int i=0; i<getWSArtifactEdits().size(); i++) {
 			WSDDArtifactEdit artifactEdit = (WSDDArtifactEdit) getWSArtifactEdits().get(i);
 			ComponentHandle editHandle = artifactEdit.getComponentHandle();
 			if (editHandle.getProject()==handle.getProject() && editHandle.getName().equals(handle.getName()))
 				return artifactEdit;
 		}
 		return null;
 	}
 	
 	private WSCDDArtifactEdit getWSClientArtifactEdit(ComponentHandle handle) {
 		for (int i=0; i<getWSClientArtifactEdits().size(); i++) {
			WSCDDArtifactEdit artifactEdit = (WSCDDArtifactEdit) getWSArtifactEdits().get(i);
 			ComponentHandle editHandle = artifactEdit.getComponentHandle();
 			if (editHandle.getProject()==handle.getProject() && editHandle.getName().equals(handle.getName()))
 				return artifactEdit;
 		}
 		return null;
 	}
 
 	/**
 	 * Notify listeners of
 	 * 
 	 * @anEvent.
 	 */
 	protected void notifyListeners(EditModelEvent anEvent) {
 		if (listeners == null)
 			return;
 		synchronized (this) {
 			isNotifing = true;
 		}
 		try {
 			List list = getListeners();
 			for (int i = 0; i < list.size(); i++)
 				((EditModelListener) list.get(i)).editModelChanged(anEvent);
 		} finally {
 			synchronized (this) {
 				isNotifing = false;
 				if (removedListeners != null && !removedListeners.isEmpty()) {
 					for (int i = 0; i < removedListeners.size(); i++)
 						listeners.remove(removedListeners.get(i));
 					removedListeners.clear();
 				}
 			}
 		}
 	}
 
 	/**
 	 * Remove aListener from the list of listeners.
 	 */
 	public synchronized boolean removeListener(EditModelListener aListener) {
 		if (aListener != null) {
 			if (isNotifing)
 				return removedListeners.add(aListener);
 			return getListeners().remove(aListener);
 		}
 		return false;
 	}
 
 	private void releaseArtifactEdits() {
 		for (int i = 0; i < getWSArtifactEdits().size(); i++) {
 			WSDDArtifactEdit artifactEdit = (WSDDArtifactEdit) getWSArtifactEdits().get(i);
 			artifactEdit.removeListener(this);
 			artifactEdit.dispose();
 		}
 		getWSArtifactEdits().clear();
 		
 		for (int i = 0; i < getWSClientArtifactEdits().size(); i++) {
 			WSCDDArtifactEdit artifactEdit = (WSCDDArtifactEdit) getWSClientArtifactEdits().get(i);
 			artifactEdit.removeListener(this);
 			artifactEdit.dispose();
 		}
 		getWSClientArtifactEdits().clear();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
 	 */
 	public void resourceChanged(IResourceChangeEvent event) {
 		acceptDelta(event);
 	}
 
 
 	protected void acceptDelta(IResourceChangeEvent event) {
 		IResourceDelta delta = event.getDelta();
 		// search for changes to any projects using a visitor
 		if (delta != null) {
 			try {
 				delta.accept(this);
 			} catch (Exception e) {
 				Logger.getLogger().logError(e);
 			}
 		}
 	}
 
 	/**
 	 * returns a list of internal web services descriptions in the workspace
 	 */
 	public List getInternalWebServicesDescriptions() {
 		List result = new ArrayList();
 		List webServices = getInternalWebServices();
 		for (int i = 0; i < webServices.size(); i++) {
 			WebServices webService = (WebServices) webServices.get(i);
 			if (webService != null)
 				result.addAll(webService.getWebServiceDescriptions());
 		}
 		return result;
 	}
 
 	/**
 	 * @return all internal web services instances in workspace
 	 */
 	public List getInternalWebServices() {
 		List result = new ArrayList();
 		for (int i = 0; i < getWSArtifactEdits().size(); i++) {
 			WSDDArtifactEdit artifactEdit = (WSDDArtifactEdit) getWSArtifactEdits().get(i);
 			WebServices webServices = artifactEdit.getWebServices();
 			if (webServices != null)
 				result.add(webServices);
 		}
 		return result;
 	}
 
 	/**
 	 * returns a list of all the internal wsdl services in wsdl's pointed to by wsdd's
 	 */
 	public List getInternalWSDLServices() {
 		return getWSDLServicesFromWSDLResources(getInternalWSDLResources());
 	}
 
 	public List getInternalWSDLResources() {
 		List result = new ArrayList();
 		List wsddWebServices = getInternalWebServicesDescriptions();
 		for (int i = 0; i < wsddWebServices.size(); i++) {
 			WebServiceDescription webServices = (WebServiceDescription) wsddWebServices.get(i);
 			Resource wsdl = getWSDLResource(webServices);
 			if (wsdl != null && !result.contains(wsdl))
 				result.add(wsdl);
 		}
 		return result;
 	}
 
 		public List getExternalWSDLResources() {
 			//TODO fix up for basis off .wsil
 			List result = getWorkspaceWSDLResources();
 			result.removeAll(getInternalWSDLResources());
 			List serviceRefs = getAllWorkspaceServiceRefs();
 			for (int i=0; i<serviceRefs.size(); i++) {
 				ServiceRef ref = (ServiceRef) serviceRefs.get(i);
 				try {
 					Resource res = WorkbenchResourceHelperBase.getResource(URI.createURI(ref.getWsdlFile()), true);
 					if (res !=null && result.contains(res))
 						result.remove(res);
 				} catch (Exception e) {
 					//Ignore
 				}
 			}
 			return result;
 		}
 		
 	public boolean isServiceInternal(EObject service) {
 		return getInternalWSDLResources().contains(getWSDLResource(service));
 	}
 
 	private List getWSDLServicesFromWSDLResources(List wsdlResources) {
 		List result = new ArrayList();
 		for (int i = 0; i < wsdlResources.size(); i++) {
 			Resource wsdl = (Resource) wsdlResources.get(i);
 			List services = getWSDLServices(wsdl);
 			if (wsdl != null && services != null && !services.isEmpty())
 				result.addAll(services);
 		}
 		return result;
 	}
 
 	public List getExternalWSDLServices() {
 		List result = getWsdlServicesFromWorkspaceWSILs();
 		result.removeAll(getInternalWSDLServices());
 		return result;
 	}
 
 	public List getWsdlServicesFromWorkspaceWSILs() {
 		List result = new ArrayList();
 		List wsilFiles = getWorkspaceWSILFiles();
 		for (int i = 0; i < wsilFiles.size(); i++) {
 			IFile wsil = (IFile) wsilFiles.get(i);
 			List services = getWsdlServicesFromWsilFile(wsil);
 			if (!services.isEmpty())
 				result.addAll(services);
 		}
 	return result;
 	}
 
 	public List getWsdlServicesFromWsilFile(IFile wsil) {
 		WSDLServiceHelper serviceHelper = WSDLServiceExtManager.getServiceHelper();
 		return serviceHelper.getWsdlServicesFromWsilFile(wsil);
 	}
 
 	/**
 	 * Returns all WSDL Services, both internal and external
 	 */
 	public List getAllWSDLServices() {
 		List result = new ArrayList();
 		result.addAll(getInternalWSDLServices());
 		result.addAll(getExternalWSDLServices());
 		return result;
 	}
 
 	protected void dispose() {
 		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
 		releaseArtifactEdits();
 		INSTANCE = null;
 	}
 
 	public Resource getWSDLResource(WebServiceDescription webService) {
 		String wsdlFileName = webService.getWsdlFile();
 		Resource res = null;
 		IVirtualResource[] resources = ComponentCore.createResources(WorkbenchResourceHelper.getFile(webService));
 		if (resources == null) return res;
 		ComponentHandle handle = ComponentHandle.create(resources[0].getComponent().getProject(),resources[0].getComponent().getName());
 		WSDDArtifactEdit artifactEdit = getWSArtifactEdit(handle);
 		res = artifactEdit.getWsdlResource(wsdlFileName);
 		return res;
 	}
 
 	public List getWSDLServices(Resource wsdl) {
 		WSDLServiceHelper serviceHelper = WSDLServiceExtManager.getServiceHelper();
 		List result = new ArrayList();
 		Object def = serviceHelper.getWSDLDefinition(wsdl);
 		if (def == null)
 			return result;
 		result = new ArrayList(serviceHelper.getDefinitionServices(def).values());
 		return result;
 	}
 
 	public EObject getWSDLServiceForWebService(WebServiceDescription webService) {
 		EObject service = null;
 		WSDLServiceHelper serviceHelper = WSDLServiceExtManager.getServiceHelper();
 		Resource wsdl = getWSDLResource(webService);
 		if (wsdl == null) return service;
 		Object definition = serviceHelper.getWSDLDefinition(wsdl);
 		if (definition == null) return service;
 		Map services = serviceHelper.getDefinitionServices(definition);
 		if (services.isEmpty()) return service;
 		PortComponent portComp = null;
 		if (webService.getPortComponents()!=null && webService.getPortComponents().size()>0) {
 			portComp = (PortComponent) webService.getPortComponents().get(0);
 			return getService(portComp);
 		}
 		return service;
 	}
 
 	public Resource getWSDLResource(EObject wsdlService) {
 		return wsdlService.eResource();
 	}
 
 	public EObject getService(PortComponent port) {
 		List services = getInternalWSDLServices();
 		WSDLServiceHelper serviceHelper = WSDLServiceExtManager.getServiceHelper();
 		for (int i = 0; i < services.size(); i++) {
 			EObject service = (EObject)services.get(i);
 			if (serviceHelper.getServicePorts(service).size() == 1) {
 				Object wsdlPort = serviceHelper.getServicePorts(service).values().toArray()[0];
 				String qName = serviceHelper.getPortBindingNamespaceURI(wsdlPort);
 				if (port.getWsdlPort().getNamespaceURI().equals(qName))
 					return service;
 			}
 		}
 		return null;
 	}
 
 	public PortComponent getPortComponent(String qName) {
 		List wsDescs = getInternalWebServicesDescriptions();
 		for (int i = 0; i < wsDescs.size(); i++) {
 			WebServiceDescription wsDesc = (WebServiceDescription) wsDescs.get(i);
 			List ports = wsDesc.getPortComponents();
 			for (int j = 0; j < ports.size(); j++) {
 				PortComponent portComp = (PortComponent) ports.get(j);
 				if (portComp.getWsdlPort().getNamespaceURI().equals(qName))
 					return portComp;
 			}
 		}
 		return null;
 	}
 
 	public PortComponent getPortComponent(String qName, IProject project) {
 		List wsDescs = getInternalWebServicesDescriptions();
 		for (int i = 0; i < wsDescs.size(); i++) {
 			WebServiceDescription wsDesc = (WebServiceDescription) wsDescs.get(i);
 			List ports = wsDesc.getPortComponents();
 			for (int j = 0; j < ports.size(); j++) {
 				PortComponent portComp = (PortComponent) ports.get(j);
 				if (portComp.getWsdlPort().getNamespaceURI().equals(qName) && project == ProjectUtilities.getProject(portComp))
 					return portComp;
 			}
 		}
 		return null;
 	}
 
 	public PortComponent getPortComponent(EObject wsdlService) {
 		// If there is only one port in the wsdl service, find the matching port component
 		// otherwise if multiple ports return null because we need more information
 		WSDLServiceHelper serviceHelper = WSDLServiceExtManager.getServiceHelper();
 		if (wsdlService == null || serviceHelper.getServicePorts(wsdlService).isEmpty())
 			return null;
 		if (serviceHelper.getServicePorts(wsdlService).size() == 1) {
 			Object port = serviceHelper.getServicePorts(wsdlService).values().toArray()[0];
 			String qName = serviceHelper.getPortBindingNamespaceURI(port);
 			return getPortComponent(qName, ProjectUtilities.getProject(wsdlService));
 		}
 		return null;
 	}
 
 	public ServiceImplBean getServiceImplBean(EObject wsdlService) {
 		PortComponent port = getPortComponent(wsdlService);
 		if (port == null)
 			return null;
 		return port.getServiceImplBean();
 	}
 
 	public WsddResource getWsddResource(EObject wsdlService) {
 		PortComponent port = getPortComponent(wsdlService);
 		if (port == null)
 			return null;
 		return (WsddResource) port.eResource();
 	}
 
 	public String getServiceEndpointInterface(EObject wsdlService) {
 		PortComponent port = getPortComponent(wsdlService);
 		if (port == null)
 			return null;
 		return port.getServiceEndpointInterface();
 	}
 
 	public List getAllWorkspaceServiceRefs() {
 		List result = new ArrayList();
 		result.addAll(getWorkspace13ServiceRefs());
 		result.addAll(getWorkspace14ServiceRefs());
 		return result;
 	}
 
 	public List getWorkspace13ServiceRefs() {
 		List result = new ArrayList();
 		for (int i = 0; i < getWSClientArtifactEdits().size(); i++) {
 			WSCDDArtifactEdit artifactEdit = (WSCDDArtifactEdit) getWSClientArtifactEdits().get(i);
 			WebServicesResource res = artifactEdit.getWscddXmiResource();
 				if (res != null && res.isLoaded() && res.getWebServicesClient() != null)
 					result.addAll(res.getWebServicesClient().getServiceRefs());
 		}
 		return result;
 	}
 
 	public List get13ServiceRefs(ComponentHandle handle) {
 
 		List result = new ArrayList();
 		WSCDDArtifactEdit wsClientArtifactEdit = getWSClientArtifactEdit(handle);
 		WebServicesResource res = wsClientArtifactEdit.getWscddXmiResource();
 		if (res != null && res.isLoaded() && res.getWebServicesClient() != null)
 			result.addAll(res.getWebServicesClient().getServiceRefs());
 
 		return result;
 	}
 
 	public List getWorkspace14ServiceRefs() {
 		List result = new ArrayList();
 		for (int i = 0; i < getWSArtifactEdits().size(); i++) {
 			WSDDArtifactEdit wsArtifactEdit = (WSDDArtifactEdit) getWSArtifactEdits().get(i);
 			ArtifactEdit artifactEdit = ArtifactEdit.getArtifactEditForRead(wsArtifactEdit.getComponentHandle());
 			try {
 				EObject rootObject = artifactEdit.getContentModelRoot();
 				// handle EJB project case
 				if (rootObject instanceof EJBJar) {
 					List cmps = ((EJBJar) rootObject).getEnterpriseBeans();
 					for (int j = 0; j < cmps.size(); j++) {
 						EnterpriseBean bean = (EnterpriseBean) cmps.get(j);
 						if (bean.getServiceRefs() != null && !bean.getServiceRefs().isEmpty())
 							result.addAll(bean.getServiceRefs());
 					}
 				}
 				// handle Web Project
 				else if (rootObject instanceof WebApp) {
 					if (((WebApp) rootObject).getServiceRefs() != null && !((WebApp) rootObject).getServiceRefs().isEmpty())
 						result.addAll(((WebApp) rootObject).getServiceRefs());
 				}
 				// handle App clients
 				else if (rootObject instanceof ApplicationClient) {
 					if (((ApplicationClient) rootObject).getServiceRefs() != null && !((ApplicationClient) rootObject).getServiceRefs().isEmpty())
 						result.addAll(((ApplicationClient) rootObject).getServiceRefs());
 				}
 			} finally {
 				if (artifactEdit != null)
 					artifactEdit.dispose();
 			}
 		}
 		return result;
 	}
 
 	public boolean isJ2EE14(ServiceRef ref) {
 		return !(ref.eContainer() instanceof WebServicesClient);
 	}
 
 	public List getWorkspaceWSILFiles() {
 		List result = new ArrayList();
 		for (int i = 0; i < getWSArtifactEdits().size(); i++) {
 			WSDDArtifactEdit artifactEdit = (WSDDArtifactEdit) getWSArtifactEdits().get(i);
 			IProject project = artifactEdit.getComponentHandle().getProject();
 			if (project != null) {
 				List files = ProjectUtilities.getAllProjectFiles(project);
 				for (int j = 0; j < files.size(); j++) {
 					IFile file = (IFile) files.get(j);
 					if (file != null && WSIL_EXT.equals(file.getFileExtension()))
 						result.add(file);
 				}
 			}
 		}
 		return result;
 	}
 
 	public List getWorkspaceWSDLResources() {
 		List result = new ArrayList();
 		for (int i = 0; i < getWSArtifactEdits().size(); i++) {
 			WSDDArtifactEdit artifactEdit = (WSDDArtifactEdit) getWSArtifactEdits().get(i);
 			IProject project = artifactEdit.getComponentHandle().getProject();
 			if (project != null) {
 				List wsdlResources = artifactEdit.getWSDLResources();
 				if (wsdlResources != null && !wsdlResources.isEmpty()) {
 					for (int j = 0; j < wsdlResources.size(); j++) {
 						Resource wsdl = (Resource) wsdlResources.get(j);
 						if (!result.contains(wsdl))
 							result.add(wsdl);
 					}
 				}
 			}
 		}
 		return result;
 	}
 
 	public List getWSDLServices() {
 		List result = new ArrayList();
 		List internalWsdls = getInternalWSDLServices();
 		if (internalWsdls != null && !internalWsdls.isEmpty())
 			result.addAll(internalWsdls);
 		//TODO add externals
 		return result;
 	}
 
 	/**
 	 * @param bean
 	 * @return
 	 */
 	public List get13ServiceRefs(EnterpriseBean bean) {
 		ComponentHandle handle = getComponentHandle(WorkbenchResourceHelper.getFile(bean));
 		if (handle == null)
 			return Collections.EMPTY_LIST;
 		WSCDDArtifactEdit artifactEdit = getWSClientArtifactEdit(handle);
 		WebServicesResource res = artifactEdit.getWscddXmiResource();
 		if (res != null && res.getWebServicesClient() != null) {
 			String ejbName = bean.getName();
 			List scopes = res.getWebServicesClient().getComponentScopedRefs();
 			for (Iterator iter = scopes.iterator(); iter.hasNext();) {
 				ComponentScopedRefs scope = (ComponentScopedRefs) iter.next();
 				if (scope.getComponentName().equals(ejbName))
 					return scope.getServiceRefs();
 			}
 		}
 		return Collections.EMPTY_LIST;
 	}
 
 	/**
 	 * @param client
 	 * @return
 	 */
 	public List get13ServiceRefs(ApplicationClient client) {
 		ComponentHandle handle = getComponentHandle(WorkbenchResourceHelper.getFile(client));
 		if (handle == null)
 			return Collections.EMPTY_LIST;
 		WSCDDArtifactEdit artifactEdit = getWSClientArtifactEdit(handle);
 		WebServicesResource res = artifactEdit.getWscddXmiResource();
 		if (res != null) {
 			WebServicesClient webClient = res.getWebServicesClient();
 			if (webClient != null)
 				return webClient.getServiceRefs();
 		}
 		return Collections.EMPTY_LIST;
 	}
 
 	/**
 	 * @param webapp
 	 * @return
 	 */
 	public List get13ServiceRefs(WebApp webapp) {
 		ComponentHandle handle = getComponentHandle(WorkbenchResourceHelper.getFile(webapp));
 		if (handle == null)
 			return Collections.EMPTY_LIST;
 		WSCDDArtifactEdit artifactEdit = getWSClientArtifactEdit(handle);
 		WebServicesResource res = artifactEdit.getWscddXmiResource();
 		if (res != null) {
 			WebServicesClient webClient = res.getWebServicesClient();
 			if (webClient != null)
 				return webClient.getServiceRefs();
 		}
 		return Collections.EMPTY_LIST;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
 	 */
 	public boolean visit(IResourceDelta delta) throws CoreException {
 		IResource resource = delta.getResource();
 		if (resource.getType() == IResource.PROJECT) {
 			IProject p = (IProject) resource;
 			IFlexibleProject flexProject = ComponentCore.createFlexibleProject(p);
 			// Handle project adds
 			if (delta.getKind() == IResourceDelta.ADDED && flexProject != null) {
 				boolean state = true;
 				IVirtualComponent[] components = flexProject.getComponents();
 				for (int i=0; i<components.length; i++) {
 					ComponentHandle handle = ComponentHandle.create(components[i].getProject(),components[i].getName());
 					WSDDArtifactEdit wsArtifactEdit = getWSArtifactEdit(handle);
 					if (wsArtifactEdit ==null) {
 						wsArtifactEdit = WSDDArtifactEdit.getWSDDArtifactEditForRead(handle);
 						getWSArtifactEdits().add(wsArtifactEdit);
 						wsArtifactEdit.addListener(this);
 						// forward an edit model event to manager's listeners
 						// TODO forward edit model event?
 						//notifyListeners(new EditModelEvent(EditModelEvent.ADDED_RESOURCE,wsArtifactEdit));
 						state = false;
 					}
 					WSCDDArtifactEdit wscArtifactEdit = getWSClientArtifactEdit(handle);
 					if (wscArtifactEdit ==null) {
 						wscArtifactEdit = WSCDDArtifactEdit.getWSCDDArtifactEditForRead(handle);
 						getWSArtifactEdits().add(wscArtifactEdit);
 						wscArtifactEdit.addListener(this);
 						// forward an edit model event to manager's listeners
 						// TODO forward edit model event?
 						//notifyListeners(new EditModelEvent(EditModelEvent.ADDED_RESOURCE,wscArtifactEdit));
 						state = false;
 					}
 				}
 				return state;
 			}
 			// Handle project close events removals
 			else if (delta.getKind() == IResourceDelta.CHANGED && flexProject != null) {
 				boolean state = true;
 				boolean projectOpenStateChanged = ((delta.getFlags() & IResourceDelta.OPEN) != 0);
 				if (projectOpenStateChanged) {
 					IVirtualComponent[] components = flexProject.getComponents();
 					for (int i=0; i<components.length; i++) {
 						ComponentHandle handle = ComponentHandle.create(components[i].getProject(),components[i].getName());
 						WSDDArtifactEdit wsArtifactEdit = getWSArtifactEdit(handle);
 						if (wsArtifactEdit != null) {
 							getWSArtifactEdits().remove(wsArtifactEdit);
 							// forward an edit model event to manager's listeners
 							// TODO forward edit model event?
 							//notifyListeners(new EditModelEvent(EditModelEvent.REMOVED_RESOURCE,wsArtifactEdit));
 							state = false;
 						}
 						
 						WSCDDArtifactEdit wscArtifactEdit = getWSClientArtifactEdit(handle);
 						if (wscArtifactEdit != null) {
 							getWSArtifactEdits().remove(wscArtifactEdit);
 							// forward an edit model event to manager's listeners
 							// TODO forward edit model event?
 							//notifyListeners(new EditModelEvent(EditModelEvent.REMOVED_RESOURCE,wscArtifactEdit));
 							state = false;
 						}
 					}	
 				}
 				return state;
 			}
 			// Handle project delete events
 			else if (delta.getKind() == IResourceDelta.REMOVED && flexProject != null) {
 				boolean state = true;
 				IVirtualComponent[] components = flexProject.getComponents();
 				for (int i=0; i<components.length; i++) {
 					ComponentHandle handle = ComponentHandle.create(components[i].getProject(),components[i].getName());
 					WSDDArtifactEdit wsArtifactEdit = getWSArtifactEdit(handle);
 					if (wsArtifactEdit != null) {
 						getWSArtifactEdits().remove(wsArtifactEdit);
 						// forward an edit model event to manager's listeners
 						// TODO forward edit model event?
 						//notifyListeners(new EditModelEvent(EditModelEvent.REMOVED_RESOURCE,wsArtifactEdit));
 						state = false;
 					}
 					
 					WSCDDArtifactEdit wscArtifactEdit = getWSClientArtifactEdit(handle);
 					if (wscArtifactEdit != null) {
 						getWSArtifactEdits().remove(wscArtifactEdit);
 						// forward an edit model event to manager's listeners
 						// TODO forward edit model event?
 						//notifyListeners(new EditModelEvent(EditModelEvent.REMOVED_RESOURCE,wscArtifactEdit));
 						state = false;
 					}
 				}
 				return state;		
 			}
 		}
 		
 		else if (resource.getType() == IResource.FILE && isInterrestedInFile((IFile) resource)) {
 			if ((delta.getKind() == IResourceDelta.ADDED) || ((delta.getFlags() & IResourceDelta.MOVED_TO) != 0))
 				if (resource.getFileExtension().equals(WSDL_EXT))
 				    addedWsdl((IFile) resource);
 				else if (resource.getFileExtension().equals(WSIL_EXT))
 				    addedWsil((IFile)resource);
 			return false;
 		}
 		return true;
 	}
 
 	protected void addedWsdl(IFile wsdl) {
 		if (!wsdl.exists())
 			return;
 		ComponentHandle handle = getComponentHandle(wsdl);
 		WSDDArtifactEdit artifactEdit = getWSArtifactEdit(handle);
 		// forward an edit model event to manager's listeners
 		// TODO forward edit model event?
 		//notifyListeners(new EditModelEvent(EditModelEvent.ADDED_RESOURCE, artifactEdit));
 	}
 
 	protected void addedWsil(IFile wsil) {
 		if (!wsil.exists())
 			return;
 		ComponentHandle handle = getComponentHandle(wsil);
 		WSDDArtifactEdit artifactEdit = getWSArtifactEdit(handle);
 		// forward an edit model event to manager's listeners
 		// TODO forward edit model event?
 		//notifyListeners(new EditModelEvent(EditModelEvent.ADDED_RESOURCE, artifactEdit));
 	}
 	
 	private ComponentHandle getComponentHandle(IFile res) {
 		IVirtualResource[] resources = ComponentCore.createResources(res);
 		if (resources == null || resources.length==0) 
 			return null;
 		String name = resources[0].getComponent().getName();
 		IProject p = res.getProject();
 		ComponentHandle handle = ComponentHandle.create(p,name);
 		return handle;
 	}
 
 	protected boolean isInterrestedInFile(IFile aFile) {
 		if (aFile != null && aFile.getFileExtension() != null) {
 			String extension = aFile.getFileExtension();
 			return extension.equals(WSDL_EXT) || extension.equals(WSIL_EXT);
 		}
 		return false;
 	}
 
 	/**
 	 * @param object
 	 * @return
 	 */
 	public Collection getServiceRefs(EJBJar jar) {
 
 		List list = new ArrayList();
 		List beans = jar.getEnterpriseBeans();
 		try {
 			for (int i = 0; i < beans.size(); i++) {
 				EnterpriseBean bean = (EnterpriseBean) beans.get(i);
 				list.addAll(getServiceRefs(bean));
 			}
 		} catch (Exception e) {
 			//Ignore
 		}
 		return list;
 	}
 
 	public Collection getServiceRefs(EnterpriseBean bean) {
 		List list = new ArrayList();
 		if (bean.getEjbJar().getJ2EEVersionID() >= J2EEVersionConstants.J2EE_1_4_ID)
 			list.addAll(bean.getServiceRefs());
 		else
 			list.addAll(get13ServiceRefs(bean));
 		return list;
 	}
 
 	public Collection getServiceRefs(WebApp webapp) {
 
 		List list = new ArrayList();
 		try {
 			if (webapp.getVersionID() >= J2EEVersionConstants.WEB_2_4_ID)
 				list.addAll(webapp.getServiceRefs());
 			else
 				list.addAll(get13ServiceRefs(webapp));
 		} catch (Exception e) {
 			//Ignore
 		}
 		return list;
 	}
 
 	public Collection getServiceRefs(ApplicationClient client) {
 
 		List list = new ArrayList();
 		try {
 			if (client.getJ2EEVersionID() >= J2EEVersionConstants.J2EE_1_4_ID)
 				list.addAll(client.getServiceRefs());
 			else
 				list.addAll(get13ServiceRefs(client));
 		} catch (Exception e) {
 			//Ignore
 		}
 		return list;
 	}
 }
