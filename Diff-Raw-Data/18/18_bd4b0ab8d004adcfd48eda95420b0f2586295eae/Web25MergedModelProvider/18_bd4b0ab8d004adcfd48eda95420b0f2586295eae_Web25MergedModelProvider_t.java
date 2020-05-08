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
 import org.eclipse.jst.j2ee.model.IModelProvider;
 import org.eclipse.jst.j2ee.model.IModelProviderEvent;
 import org.eclipse.jst.javaee.web.WebApp;
 import org.eclipse.jst.javaee.web.WebFactory;
 import org.eclipse.jst.jee.model.internal.common.AbstractMergedModelProvider;
 import org.eclipse.jst.jee.model.internal.mergers.ModelElementMerger;
 import org.eclipse.jst.jee.model.internal.mergers.ModelException;
 import org.eclipse.jst.jee.model.internal.mergers.WebAppMerger;
 import org.eclipse.jst.jee.web.Activator;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
 
 /**
  * @author Kiril Mitov k.mitov@sap.com
  * 
  */
 public class Web25MergedModelProvider extends AbstractMergedModelProvider<WebApp> {
 
 	public Web25MergedModelProvider(IProject project) {
 		super(project);
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
		return annotationModelProvider != null ? (WebApp) annotationModelProvider.getModelObject() : null;
 	}
 
 	private WebApp getXmlWebApp() {
		return ddProvider != null ? (WebApp) ddProvider.getModelObject() : null;
 	}
 
 	public Object getModelObject(IPath modelPath) {
 		return null;
 	}
 
 	@Override
 	public void modify(Runnable runnable, IPath modelPath) {
 		/*
 		 * Someone has called modify before loading the model. Try to load the
 		 * model.
 		 */
 		if (mergedModel == null)
 			getMergedModel();
 		if (isDisposed()) {
 			return;
 		}
 		/*
 		 * Still not supporting modifications of the merged model. During modify
 		 * the model is becoming the ddModel. After modifying the model is
 		 * unloaded.
 		 */
 		WebApp backup = mergedModel;
 		try {
 			mergedModel = (WebApp) ddProvider.getModelObject();
 			ddProvider.modify(runnable, modelPath);
 		} finally {
 			mergedModel = backup;
 		}
		if (mergedModel == null)
			mergedModel = getMergedModel();
		else
			merge(getXmlWebApp(), getAnnotationWebApp());
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
 
 	@Override
 	protected void annotationModelChanged(IModelProviderEvent event) {
 		internalModelChanged(event);
 	}
 
 	@Override
 	protected void xmlModelChanged(IModelProviderEvent event) {
 		internalModelChanged(event);
 	}
 
 	/*
 	 * Notifications from xml and annotation may come in different threads. This
 	 * depends on the implementation of the model providers, but to make sure
 	 * that no race conditions occurs I am synchronizing this method.
 	 */
 	private synchronized void internalModelChanged(IModelProviderEvent event) {
 		merge(getXmlWebApp(), getAnnotationWebApp());
 		notifyListeners(event);
 	}
 
 	@Override
 	protected WebApp merge(WebApp ddModel, WebApp annotationsModel) {
 		try {
 			if (mergedModel != ddModel) {
 				clearModel(mergedModel);
 				mergeWithModel(ddModel);
 				mergeWithModel(annotationsModel);
 			}
 		} catch (ModelException e) {
 			Activator.logError(e);
 		}
 		return mergedModel;
 	}
 
 	private void mergeWithModel(WebApp model) throws ModelException {
		if (model == null)
			return;
 		WebAppMerger merger = new WebAppMerger(mergedModel, model, ModelElementMerger.ADD);
 		merger.process();
 	}
 
 	@Override
 	protected WebApp createNewModelInstance() {
 		return WebFactory.eINSTANCE.createWebApp();
 	}
 
 }
