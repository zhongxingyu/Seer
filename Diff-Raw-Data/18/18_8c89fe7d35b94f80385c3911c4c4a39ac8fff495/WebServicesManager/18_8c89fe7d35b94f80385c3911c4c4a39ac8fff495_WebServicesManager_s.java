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
 
 
 
 package org.eclipse.jst.j2ee.internal.webservices;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
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
 import org.eclipse.jst.j2ee.applicationclient.internal.creation.IApplicationClientNatureConstants;
 import org.eclipse.jst.j2ee.client.ApplicationClient;
 import org.eclipse.jst.j2ee.ejb.EJBJar;
 import org.eclipse.jst.j2ee.ejb.EnterpriseBean;
 import org.eclipse.jst.j2ee.internal.J2EEConstants;
 import org.eclipse.jst.j2ee.internal.J2EEEditModel;
 import org.eclipse.jst.j2ee.internal.J2EEVersionConstants;
 import org.eclipse.jst.j2ee.internal.common.XMLResource;
 import org.eclipse.jst.j2ee.internal.project.IEJBNatureConstants;
 import org.eclipse.jst.j2ee.internal.project.IWebNatureConstants;
 import org.eclipse.jst.j2ee.internal.project.J2EEModuleNature;
 import org.eclipse.jst.j2ee.internal.project.J2EENature;
 import org.eclipse.jst.j2ee.webapplication.WebApp;
 import org.eclipse.jst.j2ee.webservice.wsclient.ComponentScopedRefs;
 import org.eclipse.jst.j2ee.webservice.wsclient.ServiceRef;
 import org.eclipse.jst.j2ee.webservice.wsclient.WebServicesClient;
 import org.eclipse.jst.j2ee.webservice.wsclient.WebServicesResource;
 import org.eclipse.jst.j2ee.webservice.wsclient.Webservice_clientFactory;
 import org.eclipse.jst.j2ee.webservice.wsdd.PortComponent;
 import org.eclipse.jst.j2ee.webservice.wsdd.ServiceImplBean;
 import org.eclipse.jst.j2ee.webservice.wsdd.WebServiceDescription;
 import org.eclipse.jst.j2ee.webservice.wsdd.WebServices;
 import org.eclipse.jst.j2ee.webservice.wsdd.WsddResource;
 import org.eclipse.wst.common.internal.emfworkbench.WorkbenchResourceHelper;
 import org.eclipse.wst.common.internal.emfworkbench.integration.EditModel;
 import org.eclipse.wst.common.internal.emfworkbench.integration.EditModelEvent;
 import org.eclipse.wst.common.internal.emfworkbench.integration.EditModelListener;
 
import org.eclipse.jem.util.logger.proxy.Logger;
import org.eclipse.jem.util.emf.workbench.ProjectResourceSet;
import org.eclipse.jem.util.emf.workbench.ProjectUtilities;
import org.eclipse.jem.util.emf.workbench.WorkbenchResourceHelperBase;

 /**
  * @author jlanuti
  * 
  * To change the template for this generated type comment go to Window - Preferences - Java - Code
  * Generation - Code and Comments
  */
 public class WebServicesManager implements EditModelListener, IResourceChangeListener, IResourceDeltaVisitor {
 
 	private HashMap editModels;
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
 		collectProjectEditModels();
 		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
 	}
 
 	private void collectProjectEditModels() {
 		IProject[] projects = ProjectUtilities.getAllProjects();
 		for (int i = 0; i < projects.length; i++) {
 			IProject project = projects[i];
 			WebServiceEditModel editModel = getWebServiceEditModelForProject(project);
 			if (editModel != null) {
 				editModel.addListener(this);
 				getEditModels().put(project, editModel);
 			}
 		}
 	}
 
 	/**
 	 * @return Returns the editModels.
 	 */
 	private HashMap getEditModels() {
 		if (editModels == null)
 			editModels = new HashMap();
 		return editModels;
 	}
 
 	private List getEditModelList() {
 		return new ArrayList(getEditModels().values());
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
 
 	public WebServiceEditModel getWSEditModel(IProject project) {
 		List localEditModels = getEditModelList();
 		for (int i = 0; i < localEditModels.size(); i++) {
 			WebServiceEditModel wsEditModel = (WebServiceEditModel) localEditModels.get(i);
 			if (wsEditModel.getProject() == project)
 				return wsEditModel;
 		}
 		return null;
 	}
 
 	public J2EEModuleNature getWebServiceNature(IProject project) {
 		try {
 			if (project.hasNature(IEJBNatureConstants.NATURE_ID))
 				return (J2EEModuleNature) project.getNature(IEJBNatureConstants.NATURE_ID);
 			else if (project.hasNature(IApplicationClientNatureConstants.NATURE_ID))
 				return (J2EEModuleNature) project.getNature(IApplicationClientNatureConstants.NATURE_ID);
 			else if (project.hasNature(IWebNatureConstants.J2EE_NATURE_ID))
 				return (J2EEModuleNature) project.getNature(IWebNatureConstants.J2EE_NATURE_ID);
 		} catch (Exception e) {
 			//Ignore
 		}
 		return null;
 	}
 
 	private WebServiceEditModel getWebServiceEditModelForProject(IProject project) {
 		J2EEModuleNature nature = getWebServiceNature(project);
 		HashMap editModelMap = getEditModels();
 		WebServiceEditModel wsEditModel = (WebServiceEditModel) editModelMap.get(project);
 		if (wsEditModel == null && nature != null) {
 			wsEditModel = nature.getWebServiceEditModelForRead(this);
 			editModelMap.put(project, wsEditModel);
 			return wsEditModel;
 		}
 		return wsEditModel;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.wst.common.internal.emfworkbench.integration.EditModelListener#editModelChanged(org.eclipse.wst.common.internal.emfworkbench.integration.EditModelEvent)
 	 */
 	public void editModelChanged(EditModelEvent anEvent) {
 		if (anEvent.getEventCode() == EditModelEvent.PRE_DISPOSE) {
 			EditModel editModel = anEvent.getEditModel();
 			getEditModels().remove(editModel.getProject());
 			editModel.removeListener(this);
 			editModel.releaseAccess(this);
 		}
 		notifyListeners(anEvent);
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
 
 	private void releaseEditModels() {
 		for (int i = 0; i < getEditModelList().size(); i++) {
 			WebServiceEditModel editModel = (WebServiceEditModel) getEditModelList().get(i);
 			editModel.removeListener(this);
 			editModel.releaseAccess(this);
 		}
 		getEditModels().clear();
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
 		for (int i = 0; i < getEditModelList().size(); i++) {
 			WebServiceEditModel editModel = (WebServiceEditModel) getEditModelList().get(i);
 			if (editModel.getProject() != null) {
 				WebServices webServices = editModel.getWebServices();
 				if (webServices != null)
 					result.add(webServices);
 			}
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
 		releaseEditModels();
 		INSTANCE = null;
 	}
 
 	public Resource getWSDLResource(WebServiceDescription webService) {
 		String wsdlFileName = webService.getWsdlFile();
 		Resource res = null;
 		String projName = ProjectUtilities.getProject(webService).getName();
 		for (int i = 0; i < getEditModelList().size(); i++) {
 			WebServiceEditModel editModel = (WebServiceEditModel) getEditModelList().get(i);
 			if (editModel.getProject() != null && editModel.getProject().getName().equals(projName)) {
 				res = editModel.getWsdlResource(wsdlFileName);
 				if (res != null)
 					break;
 			}
 		}
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
 		for (int i = 0; i < getEditModelList().size(); i++) {
 			WebServiceEditModel editModel = (WebServiceEditModel) getEditModelList().get(i);
 			if (editModel.getProject() != null) {
 				WebServicesResource res = editModel.get13WebServicesClientResource();
 				if (res != null && res.isLoaded() && res.getWebServicesClient() != null)
 					result.addAll(res.getWebServicesClient().getServiceRefs());
 			}
 		}
 		return result;
 	}
 
 	public List get13ServiceRefs(J2EEEditModel moduleEditModel) {
 
 		List result = new ArrayList();
 		WebServicesResource res = getWscddResource(moduleEditModel);
 		if (res != null && res.isLoaded() && res.getWebServicesClient() != null)
 			result.addAll(res.getWebServicesClient().getServiceRefs());
 
 		return result;
 	}
 
 	/**
 	 * @param moduleEditModel
 	 * @return
 	 */
 	private WebServicesResource getWscddResource(J2EEEditModel moduleEditModel) {
 
 		if (moduleEditModel.getJ2EENature().getNatureID().equals(IEJBNatureConstants.NATURE_ID))
 			return (WebServicesResource) moduleEditModel.getResource(J2EEConstants.WEB_SERVICES_CLIENT_META_INF_DD_URI_OBJ);
 		if (moduleEditModel.getJ2EENature().getNatureID().equals(IWebNatureConstants.J2EE_NATURE_ID))
 			return (WebServicesResource) moduleEditModel.getResource(J2EEConstants.WEB_SERVICES_CLIENT_WEB_INF_DD_URI_OBJ);
 		if (moduleEditModel.getJ2EENature().getNatureID().equals(IApplicationClientNatureConstants.NATURE_ID))
 			return (WebServicesResource) moduleEditModel.getResource(J2EEConstants.WEB_SERVICES_CLIENT_META_INF_DD_URI_OBJ);
 		return null;
 	}
 
 	public List getWorkspace14ServiceRefs() {
 		List result = new ArrayList();
 		for (int i = 0; i < getEditModelList().size(); i++) {
 			WebServiceEditModel editModel = (WebServiceEditModel) getEditModelList().get(i);
 			if (editModel.getProject() != null) {
 				J2EEEditModel j2eeEditModel = editModel.getJ2EEEditModel(this);
 				try {
 					EObject rootObject = j2eeEditModel.getPrimaryRootObject();
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
 					j2eeEditModel.releaseAccess(this);
 				}
 			}
 		}
 		return result;
 	}
 
 	public boolean isJ2EE14(ServiceRef ref) {
 		return !(ref.eContainer() instanceof WebServicesClient);
 	}
 
 	public List getWorkspaceWSILFiles() {
 		List result = new ArrayList();
 		for (int i = 0; i < getEditModelList().size(); i++) {
 			WebServiceEditModel editModel = (WebServiceEditModel) getEditModelList().get(i);
 			if (editModel.getProject() != null) {
 				List files = ProjectUtilities.getAllProjectFiles(editModel.getProject());
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
 		for (int i = 0; i < getEditModelList().size(); i++) {
 			WebServiceEditModel editModel = (WebServiceEditModel) getEditModelList().get(i);
 			if (editModel.getProject() != null) {
 				List wsdlResources = editModel.getWSDLResources();
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
 	public Collection get13ServiceRefs(EnterpriseBean bean) {
 
 		IProject proj = ProjectUtilities.getProject(bean);
 		if (proj == null)
 			return Collections.EMPTY_LIST;
 		WebServicesResource res = getWebServicesClientResource(proj);
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
 	public Collection get13ServiceRefs(ApplicationClient client) {
 		IProject proj = ((ProjectResourceSet) client.eResource().getResourceSet()).getProject();
 		WebServicesResource res = getWebServicesClientResource(proj);
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
 	public Collection get13ServiceRefs(WebApp webapp) {
 		IProject proj = ((ProjectResourceSet) webapp.eResource().getResourceSet()).getProject();
 		WebServicesResource res = getWebServicesClientResource(proj);
 		if (res != null) {
 			WebServicesClient webClient = res.getWebServicesClient();
 			if (webClient != null)
 				return webClient.getServiceRefs();
 		}
 		return Collections.EMPTY_LIST;
 	}
 
 	public WebServicesResource getDefaultWebServicesResource(EObject proj, J2EEEditModel model) {
 		WebServicesResource res = getWebServicesClientResource(model.getProject());
 		if (res != null) {
 			if (res.getContents().isEmpty()) {
 				WebServicesClient wsc = Webservice_clientFactory.eINSTANCE.createWebServicesClient();
 				res.getContents().add(wsc);
 			}
 			return res;
 		}
 		if (proj instanceof WebApp)
 			res = (WebServicesResource) model.createResource(URI.createURI(WebServiceEditModel.WS_CLIENT_WEB_INF_PATH));
 		else
 			res = (WebServicesResource) model.createResource(URI.createURI(WebServiceEditModel.WS_CLIENT_META_INF_PATH));
 		WebServicesClient wsc = Webservice_clientFactory.eINSTANCE.createWebServicesClient();
 		res.getContents().add(wsc);
 		return res;
 	}
 
 	public WebServicesResource getWebServicesClientResource(IProject proj) {
 		J2EENature nature = (J2EENature.getRegisteredRuntime(proj));
 		WebServicesResource res = null;
 		try {
 			if (nature.getDeploymentDescriptorType() == XMLResource.WEB_APP_TYPE)
 				res = (WebServicesResource) nature.getResource(J2EEConstants.WEB_SERVICES_CLIENT_WEB_INF_DD_URI_OBJ);
 			else
 				res = (WebServicesResource) nature.getResource(J2EEConstants.WEB_SERVICES_CLIENT_META_INF_DD_URI_OBJ);
 		} catch (Exception e) {
 			//Ignore
 		}
 		return res;
 	}
 
 	public WsddResource getWebServicesResource(IProject proj) {
 		J2EENature nature = (J2EENature.getRegisteredRuntime(proj));
 		WsddResource res = null;
 		try {
 			if (nature.getDeploymentDescriptorType() == XMLResource.WEB_APP_TYPE)
 				res = (WsddResource) nature.getResource(J2EEConstants.WEB_SERVICES_WEB_INF_DD_URI_OBJ);
 			else
 				res = (WsddResource) nature.getResource(J2EEConstants.WEB_SERVICES_META_INF_DD_URI_OBJ);
 		} catch (Exception e) {
 			//Ignore
 		}
 
 		if (res != null && WorkbenchResourceHelper.getFile(res).exists())
 			return res;
 		return null;
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
 			// Handle project adds
 			if (delta.getKind() == IResourceDelta.ADDED) {
 				WebServiceEditModel editModel = getWebServiceEditModelForProject(p);
 				if (editModel !=null && !getEditModels().containsValue(editModel)) {
 					getEditModels().put(p,editModel);
 					editModel.addListener(this);
 					// forward an edit model event to manager's listeners
 					notifyListeners(new EditModelEvent(EditModelEvent.ADDED_RESOURCE,editModel));
 					return false;
 				}
 			}
 			// Handle project close events removals
 			else if (delta.getKind() == IResourceDelta.CHANGED) {
 				boolean projectOpenStateChanged = ((delta.getFlags() & IResourceDelta.OPEN) != 0);
 				if (projectOpenStateChanged) {
 				WebServiceEditModel editModel = (WebServiceEditModel)getEditModels().get(p);
 					if (editModel !=null) {
 						editModel.removeListener(this);
 						getEditModels().remove(p);
 						notifyListeners(new EditModelEvent(EditModelEvent.REMOVED_RESOURCE,editModel));
 						return false;
 					}		
 				}
 			}
 			// Handle project delete events
 			else if (delta.getKind() == IResourceDelta.REMOVED) {
 				WebServiceEditModel editModel = (WebServiceEditModel)getEditModels().get(p);
 				if (editModel !=null) {
 					editModel.removeListener(this);
 					getEditModels().remove(p);
 					notifyListeners(new EditModelEvent(EditModelEvent.REMOVED_RESOURCE,editModel));
 					return false;
 				}		
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
		Resource res = WorkbenchResourceHelperBase.getResource(wsdl, true);
		IProject p = ProjectUtilities.getProject(res);
 		WebServiceEditModel editModel = getWebServiceEditModelForProject(p);
 		// forward an edit model event to manager's listeners
 		notifyListeners(new EditModelEvent(EditModelEvent.ADDED_RESOURCE, editModel));
 	}
 
 	protected void addedWsil(IFile wsil) {
 		if (!wsil.exists())
 			return;
 		//Resource res = WorkbenchResourceHelper.getResource(wsil,true);
		IProject p = ProjectUtilities.getProject(wsil);
 		WebServiceEditModel editModel = getWebServiceEditModelForProject(p);
 		// forward an edit model event to manager's listeners
 		notifyListeners(new EditModelEvent(EditModelEvent.ADDED_RESOURCE, editModel));
 
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
