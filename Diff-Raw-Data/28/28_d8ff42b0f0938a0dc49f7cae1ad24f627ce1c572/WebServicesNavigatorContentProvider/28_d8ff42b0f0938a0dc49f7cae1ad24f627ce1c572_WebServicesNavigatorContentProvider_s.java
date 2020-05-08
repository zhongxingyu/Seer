 /***************************************************************************************************
  * Copyright (c) 2003, 2005 IBM Corporation and others. All rights reserved. This program and the
  * accompanying materials are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: IBM Corporation - initial API and implementation
  **************************************************************************************************/
 /*
  * Created on Jan 19, 2004
  * 
  * To change the template for this generated file go to Window - Preferences - Java - Code
  * Generation - Code and Comments
  */
 package org.eclipse.jst.j2ee.internal.webservice;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jst.j2ee.internal.webservice.helper.WebServiceEvent;
 import org.eclipse.jst.j2ee.internal.webservice.helper.WebServiceManagerListener;
 import org.eclipse.jst.j2ee.internal.webservice.helper.WebServicesManager;
 import org.eclipse.jst.j2ee.internal.webservice.plugin.WebServiceUIPlugin;
 import org.eclipse.jst.j2ee.internal.webservices.WSDLServiceExtManager;
 import org.eclipse.jst.j2ee.navigator.internal.IJ2EENavigatorConstants;
 import org.eclipse.jst.j2ee.webservice.wsclient.ServiceRef;
 import org.eclipse.jst.j2ee.webservice.wsdd.Handler;
 import org.eclipse.jst.j2ee.webservice.wsdd.PortComponent;
 import org.eclipse.ui.IMemento;
 import org.eclipse.ui.navigator.ICommonContentExtensionSite;
 import org.eclipse.ui.navigator.ICommonContentProvider;
 import org.eclipse.ui.progress.UIJob;
 import org.eclipse.wst.common.internal.emfworkbench.integration.DynamicAdapterFactory;
 
 /**
  * @author jlanuti
  * 
  * To change the template for this generated type comment go to Window - Preferences - Java - Code
  * Generation - Code and Comments
  */
 public class WebServicesNavigatorContentProvider extends AdapterFactoryContentProvider implements ICommonContentProvider, WebServiceManagerListener {
 
 	private static final Object[] NO_CHILDREN = new Object[0];
 
 	private WebServicesManager webServicesManager = null;
 	private boolean activityEnabled = false;
 	private WebServiceNavigatorGroup webServiceNavigatorGroup;
 	private WebServiceNavigatorGroupType SERVICES = null;
 	private WebServiceNavigatorGroupType CLIENTS = null;
 	private HashMap HANDLERS = new HashMap();
 	private TreeViewer viewer = null;
 
 	private Job indexJob = new WebServiceIndexJob();
 	private Job updateJob = new UpdateWebServicesNodeUIJob(); 
 	private Job removeJob = new RemoveWebServicesNodeUIJob(); 
 
 	private boolean navigatorGroupAdded = false;
 	private boolean indexJobScheduled = false;
 
 	public WebServicesNavigatorContentProvider() {
 		super(createAdapterFactory());
 		WebServicesManager.getInstance().addListener(this);
 		// create the default synchronizer for any web service editor to use with view due
 		// to the usage of seperate edit models.
 		WebServicesNavigatorSynchronizer.createInstance(createAdapterFactory(), this);
 
 	}
 
 	/**
 	 * Configure and return a composite adapter factory for our contents
 	 */
 	public static AdapterFactory createAdapterFactory() {
 		return new DynamicAdapterFactory(IJ2EENavigatorConstants.VIEWER_ID);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.wst.common.navigator.internal.views.navigator.INavigatorContentProvider#getChildren(java.lang.Object)
 	 */
 	public Object[] getChildren(Object parentElement) {
 		// if (!NavigatorActivityHelper.isActivityEnabled(getContainingExtension())) {
 		// activityEnabled = false;
 		// return super.getChildren(parentElement);
 		// }
 		// activityEnabled = true;
 		if (parentElement instanceof IWorkspaceRoot) {
 			// return new Object[]{ getWebServicesNavigatorGroup(parentElement) };
 			if (!hasNavigatorGroupBeenAdded()) {
 				if (!hasIndexJobBeenScheduled())
 					indexJob.schedule();
 				return NO_CHILDREN;
 			} else {
 				return new Object[]{getNavigatorGroup()};
 			}
 		} else if (parentElement instanceof WebServiceNavigatorGroup)
 			return new Object[]{getServicesGroup(), getClientsGroup()};
 
 		else if (parentElement instanceof WebServiceNavigatorGroupType) {
 			WebServiceNavigatorGroupType wsGroupType = (WebServiceNavigatorGroupType) parentElement;
 			return wsGroupType.getChildren();
 		} else if (WSDLServiceExtManager.getServiceHelper().isService(parentElement))
 			return getServiceLevelNodes(parentElement).toArray();
 
 		else if (parentElement instanceof ServiceRef)
 			return new Object[]{getHandlersGroup(parentElement)};
 
 		else if (parentElement instanceof Handler || parentElement instanceof org.eclipse.jst.j2ee.webservice.wsclient.Handler || WSDLServiceExtManager.getServiceHelper().isWSDLResource(parentElement))
 			return NO_CHILDREN;
 
 		else
 			return super.getChildren(parentElement);
 	}
 
 	/**
 	 * Employ a Test-And-Set (TST) primitive to ensure the Job is only scheduled once per load.
 	 * 
 	 * @return True if the the index job has been scheduled. The value of indexJobSchedule will
 	 *         _always_ be true after this method executes, so if false is returned, the job must be
 	 *         scheduled by the caller.
 	 */
 	private synchronized boolean hasIndexJobBeenScheduled() {
 		if (!indexJobScheduled) {
 			indexJobScheduled = true;
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Multiple threads access this boolean flag, so we synchronize it to ensure that its value is
 	 * consistent across different threads.
 	 * 
 	 * @return True if the WebServicesNavigatorGroup has already been processed and added to the
 	 *         tree.
 	 */
 	private synchronized boolean hasNavigatorGroupBeenAdded() {
 		return navigatorGroupAdded;
 	} 
 	
 	private synchronized void setNavigatorGroupAdded(boolean hasBeenAdded) {
 		navigatorGroupAdded = hasBeenAdded;
 	}
 
 	private List getServiceLevelNodes(Object parentElement) {
 		List result = new ArrayList();
 		// add service classes
 		if (getWebServicesManager().isServiceInternal((EObject) parentElement) && getWebServicesManager().getServiceImplBean((EObject) parentElement) != null)
 			result.add(getWebServicesManager().getServiceImplBean((EObject) parentElement));
 		// Add handlers
 		if (getWebServicesManager().isServiceInternal((EObject) parentElement))
 			result.add(getHandlersGroup(parentElement));
 		// add wsdl file
 		Resource wsdl = getWebServicesManager().getWSDLResource((EObject) parentElement);
 		if (wsdl != null)
 			result.add(wsdl);
 		return result;
 	}
 
 	private List getHandlerChildren(Object parentElement) {
 		List result = new ArrayList();
 		WebServiceNavigatorGroupType handlersGroup = (WebServiceNavigatorGroupType) parentElement;
 		// handle web service handlers case
 		if (handlersGroup.getWsdlService() != null) {
 			PortComponent port = WebServicesManager.getInstance().getPortComponent(handlersGroup.getWsdlService());
 			if (port != null && port.getHandlers() != null && !port.getHandlers().isEmpty())
 				result.addAll(port.getHandlers());
 		}
 		// handle service ref case
 		else if (handlersGroup.getServiceRef() != null) {
 			result.addAll(handlersGroup.getServiceRef().getHandlers());
 		}
 		return result;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.wst.common.navigator.internal.views.navigator.INavigatorContentProvider#getParent(java.lang.Object)
 	 */
 	public Object getParent(Object element) {
 		if (element instanceof IWorkspaceRoot)
 			return null;
 		else if (element instanceof WebServiceNavigatorGroup)
 			return ((WebServiceNavigatorGroup) element).getRoot();
 		else
 			return super.getParent(element);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.wst.common.navigator.internal.views.navigator.INavigatorContentProvider#hasChildren(java.lang.Object)
 	 */
 	public boolean hasChildren(Object element) {
 		return true;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.wst.common.navigator.internal.views.navigator.INavigatorContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
 	 *      java.lang.Object, java.lang.Object)
 	 */
 	public void inputChanged(Viewer aViewer, Object oldInput, Object newInput) {
 		// TODO handle change events
 		if (aViewer != null && aViewer instanceof TreeViewer)
 			viewer = (TreeViewer) aViewer;
 		super.inputChanged(aViewer, oldInput, newInput);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
 	 */
 	public void dispose() {
 		super.dispose();
 		WebServicesManager.getInstance().removeListener(this);
 		// dispose current instance of web service editor/explorer synchronizer
 		WebServicesNavigatorSynchronizer.disposeInstance();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.wst.common.internal.emfworkbench.integration.EditModelListener#editModelChanged(org.eclipse.wst.common.internal.emfworkbench.integration.EditModelEvent)
 	 */
 	public void webServiceManagerChanged(WebServiceEvent anEvent) {
 		
 		switch (anEvent.getEventType()) {
 			case WebServiceEvent.REFRESH:
 
 				if(!hasNavigatorGroupBeenAdded()) {
 					if(!hasIndexJobBeenScheduled())
 						indexJob.schedule();
 					else {
 						new AddWebServicesNodeUIJob().schedule();
 					}
 				} else {
 					updateJob.schedule();
 				}
 				break;
 			case WebServiceEvent.REMOVE:
 				removeJob.schedule();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
 	 */
 	public Object[] getElements(Object inputElement) {
 		return getChildren(inputElement);
 	}
 
 	/**
 	 * @return Returns the navigatorGroup.
 	 */
 	public WebServiceNavigatorGroup getNavigatorGroup() {
 		if (null == webServiceNavigatorGroup) {
 			webServiceNavigatorGroup = new WebServiceNavigatorGroup(ResourcesPlugin.getWorkspace().getRoot());
 		}
 		return webServiceNavigatorGroup;
 	}
 
 	protected WebServicesManager getWebServicesManager() {
 		if (webServicesManager == null)
 			webServicesManager = WebServicesManager.getInstance();
 		return webServicesManager;
 	}
 
 	/**
 	 * @return Returns the activityEnabled.
 	 */
 	public boolean isActivityEnabled() {
 		return activityEnabled;
 	}
 
 	private WebServiceNavigatorGroupType getServicesGroup() {
 		if (SERVICES == null)
 			SERVICES = new WebServiceNavigatorGroupType(WebServiceNavigatorGroupType.SERVICES);
 		return SERVICES;
 	}
 
 	private WebServiceNavigatorGroupType getClientsGroup() {
 		if (CLIENTS == null)
 			CLIENTS = new WebServiceNavigatorGroupType(WebServiceNavigatorGroupType.CLIENTS);
 		return CLIENTS;
 	}
 
 	private WebServiceNavigatorGroupType getHandlersGroup(Object key) {
 		if (key == null)
 			return null;
 		WebServiceNavigatorGroupType handler = null;
 		handler = (WebServiceNavigatorGroupType) HANDLERS.get(key);
 		if (handler == null) {
 			if (WSDLServiceExtManager.getServiceHelper().isService(key))
 				handler = new WebServiceNavigatorGroupType(WebServiceNavigatorGroupType.HANDLERS, (EObject) key);
 			else if (key instanceof ServiceRef)
 				handler = new WebServiceNavigatorGroupType(WebServiceNavigatorGroupType.HANDLERS, (ServiceRef) key);
 
 			if (handler != null)
 				HANDLERS.put(key, handler);
 		}
 		return handler;
 	}
 
 	/**
 	 * @return Returns the viewer.
 	 */
 	public TreeViewer getViewer() {
 		return viewer;
 	}
 
 	public void restoreState(IMemento aMemento) {
 
 	}
 
 	public void saveState(IMemento aMemento) {
 
 	}
 
 	public void init(ICommonContentExtensionSite site) {
 	}
 
 	/**
 	 * @param monitor
 	 * @return
 	 */
 	private boolean indexWebServices(IProgressMonitor monitor) {
 		boolean hasChildren = false;
 		if (!monitor.isCanceled()) {
 			try {
 				hasChildren |= getWebServicesManager().getWorkspace13ServiceRefs().size() > 0;
 			} catch (RuntimeException e) { 
 				WebServiceUIPlugin.logError(0, e.getMessage(), e);
 			}
 		} else {
 			return hasChildren;
 		}
 		monitor.worked(1);
 
 		if (!monitor.isCanceled()) {
 			try {
 				hasChildren |= getWebServicesManager().getWorkspace14ServiceRefs().size() > 0;
 			} catch (RuntimeException e) { 
 				WebServiceUIPlugin.logError(0, e.getMessage(), e);
 			}
 		} else {
 			return hasChildren;
 		}
 		monitor.worked(1);
 
 		if (!monitor.isCanceled()) {
 			try {
 				hasChildren |= getWebServicesManager().getInternalWSDLServices().size() > 0;
 			} catch (RuntimeException e) { 
 				WebServiceUIPlugin.logError(0, e.getMessage(), e);
 			}
 		} else {
 			return hasChildren;
 		}
 		monitor.worked(1);
 
 		if (!monitor.isCanceled()) {
 			try {
 				hasChildren |= getWebServicesManager().getExternalWSDLServices().size() > 0;
 			} catch (RuntimeException e) { 
 				WebServiceUIPlugin.logError(0, e.getMessage(), e);
 			}
 		} else {
 			return hasChildren;
 		}
 		monitor.worked(1);
 		return hasChildren;
 	}
 
 	public class WebServiceIndexJob extends Job {
 
 		public WebServiceIndexJob() {
			super("Indexing JSR-109 Web Services");
 		}
 
 
 		protected IStatus run(IProgressMonitor monitor) {
			monitor.beginTask("Load Web Service Components", 4);
   
 			if (indexWebServices(monitor)) {
 				new AddWebServicesNodeUIJob().schedule();
 			}
 
 			return Status.OK_STATUS;
 		}
 	}
 
 	public class AddWebServicesNodeUIJob extends UIJob {
 
 
 		public AddWebServicesNodeUIJob() {
			super("Add JSR-109 Web Services node to viewer");
 		}
 
 		public IStatus runInUIThread(IProgressMonitor monitor) {
 			getViewer().add(getViewer().getInput(), getNavigatorGroup());
 			setNavigatorGroupAdded(true);
 			return Status.OK_STATUS;
 		} 
 	}
 
 	public class UpdateWebServicesNodeUIJob extends UIJob {
 
 
 		public UpdateWebServicesNodeUIJob () {
			super("Update JSR-109 Web Services node in viewer");
 		}
 
 		public IStatus runInUIThread(IProgressMonitor monitor) {
 			if(hasNavigatorGroupBeenAdded())
 				getViewer().refresh(getNavigatorGroup());
 			else {
 				getViewer().add(getViewer().getInput(), getNavigatorGroup());
 				setNavigatorGroupAdded(true);
 			}
 			return Status.OK_STATUS;
 		} 
 	}
 	 
 	public class RemoveWebServicesNodeUIJob extends UIJob { 
 
 		public RemoveWebServicesNodeUIJob() {
			super("Update JSR-109 Web Services node in viewer");
 		}
 
 		public IStatus runInUIThread(IProgressMonitor monitor) { 
 
			monitor.beginTask("Updating Index of Web Service Components", 4);
   
 			if (indexWebServices(monitor)) {
 				getViewer().refresh(getNavigatorGroup());
 			} else {
 				getViewer().remove(getNavigatorGroup());
 				setNavigatorGroupAdded(false);
 			}
 			return Status.OK_STATUS;
 		} 
 	}
 }
