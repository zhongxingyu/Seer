 /***********************************************************************
  * Copyright (c) 2008 by SAP AG, Walldorf. 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     SAP AG - initial API and implementation
  ***********************************************************************/
 package org.eclipse.jst.jee.model.internal;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
 import org.eclipse.jst.j2ee.model.IModelProvider;
 import org.eclipse.jst.j2ee.model.IModelProviderEvent;
 import org.eclipse.jst.javaee.web.WebApp;
 import org.eclipse.jst.javaee.web.WebFactory;
 import org.eclipse.jst.jee.model.internal.common.AbstractMergedModelProvider;
 import org.eclipse.jst.jee.model.internal.mergers.ModelElementMerger;
 import org.eclipse.jst.jee.model.internal.mergers.ModelException;
 import org.eclipse.jst.jee.model.internal.mergers.WebAppMerger;
 import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
 
 /**
  * @author Kiril Mitov k.mitov@sap.com
  * 
  */
 public class Web25MergedModelProvider extends AbstractMergedModelProvider<WebApp> {
 
 	public Web25MergedModelProvider(IProject project) {
 		super(project);
 	}
 
 	/**
 	 * (non-Javadoc) Synchronizing the loading of the model
 	 * 
 	 * @see org.eclipse.jst.jee.model.internal.common.AbstractMergedModelProvider#loadModel()
 	 */
 	protected final synchronized WebApp loadModel() throws CoreException {
 		return super.loadModel();
 	}
 
 	@Override
 	protected IModelProvider loadAnnotationModel(WebApp ddModel) throws CoreException {
 		return new WebAnnotationReader(ProjectFacetsManager.create(project), ddModel);
 	}
 
 	@Override
 	protected IModelProvider loadDeploymentDescriptorModel() throws CoreException {
 		return new Web25ModelProvider(project);
 	}
 
 	private WebApp getAnnotationWebApp() {
 		return (WebApp) annotationModelProvider.getModelObject();
 	}
 
 	private WebApp getXmlWebApp() {
 		return (WebApp) ddProvider.getModelObject();
 	}
 
 	public Object getModelObject(IPath modelPath) {
 		return null;
 	}
 
 	public void modify(Runnable runnable, IPath modelPath) {
 		/*
 		 * Someone has called modify before loading the model. Try to load the
 		 * model.
 		 */
 		getMergedModel();
 		/*
 		 * Still not supporting modifications of the merged model. During modify
 		 * the model is becoming the ddModel. After modifying the model is
 		 * unloaded.
 		 */
 		WebApp backup = mergedModel;
 		mergedModel = (WebApp) ddProvider.getModelObject();
 		ddProvider.modify(runnable, modelPath);
 		if (isDisposed()) {
 			return;
 		}
 		mergedModel = backup;
 		clearModel(mergedModel);
 		try {
 			WebAppMerger merger = new WebAppMerger(mergedModel, (WebApp) ddProvider.getModelObject(),
 					ModelElementMerger.ADD);
 			merger.process();
 			merger = new WebAppMerger(mergedModel, getAnnotationWebApp(), ModelElementMerger.ADD);
 			merger.process();
 		} catch (ModelException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	private void clearModel(WebApp app) {
 		if (app == null) {
 			return;
 		}
 		app.getContextParams().clear();
 		app.getDescriptions().clear();
 		app.getDisplayNames().clear();
 		app.getDistributables().clear();
 		app.getEjbLocalRefs().clear();
 		app.getEjbRefs().clear();
 		app.getEnvEntries().clear();
 		app.getErrorPages().clear();
 		app.getFilterMappings().clear();
 		app.getFilters().clear();
 		app.getIcons().clear();
 		app.getJspConfigs().clear();
 		app.getListeners().clear();
 		app.getLocalEncodingMappingsLists().clear();
 		app.getLoginConfigs().clear();
 		app.getMessageDestinationRefs().clear();
 		app.getMessageDestinations().clear();
 		app.getMimeMappings().clear();
 		app.getPersistenceContextRefs().clear();
 		app.getPersistenceUnitRefs().clear();
 		app.getPostConstructs().clear();
 		app.getPreDestroys().clear();
 		app.getResourceEnvRefs().clear();
 		app.getResourceRefs().clear();
 		app.getSecurityConstraints().clear();
 		app.getSecurityRoles().clear();
 		app.getServiceRefs().clear();
 		app.getServletMappings().clear();
 		app.getServlets().clear();
 		app.getSessionConfigs().clear();
 		app.getWelcomeFileLists().clear();
 	}
 
 	protected void annotationModelChanged(IModelProviderEvent event) {
 		internalModelChanged(event);
 	}
 
 	protected void xmlModelChanged(IModelProviderEvent event) {
 		if (isDisposed())
 			return;
 		if (shouldDispose(event)) {
 			dispose();
 			notifyListeners(event);
 			return;
 		}
 		notifyListeners(event);
 	}
 
 	/*
 	 * Notifications from xml and annotation may come in different threads. This
 	 * depends on the implementation of the model providers, but to make sure
 	 * that no race conditions occurs I am synchronizing this method.
 	 */
 	private synchronized void internalModelChanged(IModelProviderEvent event) {
 		if (isDisposed())
 			return;
 		if (shouldDispose(event)) {
 			dispose();
 			notifyListeners(event);
 			return;
 		}
 		merge(getXmlWebApp(), getAnnotationWebApp());
 		notifyListeners(event);
 	}
 
 	@Override
 	protected WebApp merge(WebApp ddModel, WebApp annotationsModel) {
 
 		try {
 			WebAppMerger merger;
 			if (mergedModel == null) {
 				mergedModel = (WebApp) WebFactory.eINSTANCE.createWebApp();
				Resource resourceDD = ((EObject)ddModel).eResource();
				Resource resourceMM = ((EObject)mergedModel).eResource();
				if (resourceDD != null && resourceMM == null){
				  ResourceImpl resRes = new ResourceImpl(resourceDD.getURI());
				  resRes.getContents().add((EObject)mergedModel);
			    }
 			} else {
 				clearModel(mergedModel);
 
 			}
 			merger = new WebAppMerger(mergedModel, ddModel, ModelElementMerger.ADD);
 			merger.process();
 
 			merger = new WebAppMerger(mergedModel, annotationsModel, ModelElementMerger.ADD);
 			merger.process();
 		} catch (ModelException e) {
 			e.printStackTrace();
 		}
 		return mergedModel;
 	}
 
 }
