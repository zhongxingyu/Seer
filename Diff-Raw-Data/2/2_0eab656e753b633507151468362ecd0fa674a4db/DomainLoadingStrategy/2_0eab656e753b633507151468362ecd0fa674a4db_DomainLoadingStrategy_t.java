 /*******************************************************************************
  * Copyright (c) 2007 Oracle Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Oracle - initial API and implementation
  *    
  ********************************************************************************/
 package org.eclipse.jst.jsf.common.metadata.internal;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * Default class used for loading metadata.  
  * Loads the source types from extensions defined against the domain.
  * 
  * @see <code>org.eclipse.jst.jsf.common.domainLoadingStrategies</code> ext-pt
  */
 public class DomainLoadingStrategy implements IDomainLoadingStrategy, IMetaDataObserver {
 
 	/**
 	 * Domain id
 	 */
 	protected String domain;
 
 	private MetaDataModel model;
 	private List /*<IDomainSourceModelType>*/ sourceTypes;
 	private List /*<IMetaDataSourceModelProvider>*/ sources;
 	
 	/**
 	 * Constructor
 	 * @param domain
 	 */
 	public DomainLoadingStrategy(String domain){
 		this.domain = domain;
 	}
 
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jst.jsf.common.metadata.internal.IDomainLoadingStrategy#load(org.eclipse.jst.jsf.common.metadata.internal.MetaDataModel)
 	 */
 	public void load(MetaDataModel model) {
 		this.model = model;
 		sourceTypes = loadDomainSourceModelTypes();
 		sortSourceTypes(sourceTypes);
 		sources = locateMetaDataSourceInstances(sourceTypes, model);
 	    mergeModel(model, sources);		
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.jst.jsf.common.metadata.internal.IDomainLoadingStrategy#reload()
 	 */
 	public void reload() throws ModelNotSetException {
 		System.out.println("reload");//debug
 		if (model == null)
 			throw new ModelNotSetException();
 		
 		removeOldLocatorObservers();
 		sources = locateMetaDataSourceInstances(sourceTypes, model);
 	    mergeModel(model, sources);		
 	}
 	
 	/**
 	 * Responsible for iterating through the sorted list of <code>IMetaDataSourceModelProvider</code>
 	 * and merging the models after first translating the source model as required, into a single mreged model of
 	 * standatd metadata Entities and Traits.
 	 * @param model 
 	 * @param sources
 	 */
 	protected void mergeModel(MetaDataModel model, List/*<IMetaDataSourceModelProvider>*/ sources) {		
 		IMetaDataModelMergeAssistant assistant = createModelMergeAssistant(model);
 		for (Iterator/*<IMetaDataSourceModelProvider>*/ it = sources.iterator();it.hasNext();){
 			IMetaDataSourceModelProvider mds = (IMetaDataSourceModelProvider)it.next();
 //			assistant.setSourceModel(mds.getSourceModel());
 			assistant.setSourceModelProvider(mds);
 			Iterator translators = mds.getLocator().getDomainSourceModelType().getTranslators().iterator();
 			while (translators.hasNext()){
 				IMetaDataTranslator translator = (IMetaDataTranslator)translators.next();
 				translator.translate(assistant);
 			}
 		}
 		assistant.setMergeComplete();
 	}
 	
 	/**
 	 * @param model
 	 * @return an instance of a IMetaDataModelMergeAssistant to be used while merging source models
 	 */
 	protected IMetaDataModelMergeAssistant createModelMergeAssistant(MetaDataModel model){
 		return new MetaDataModelMergeAssistantImpl(model);		
 	}
 
 	/**
 	 * Allows for subclasses to override the default mechanism for sorting the source types.
 	 * @param sourceTypes
 	 */
 	protected void sortSourceTypes(List/*<IDomainSourceModelType>*/ sourceTypes) {
 		//allows override
 	}
 
 	/**
 	 * @return list of <code>IDomainSourceModelType</code>s located in the <code>DomainSourceTypesRegistry</code> 
 	 * for the specified uri
 	 */
 	protected List/*<IDomainSourceModelType>*/ loadDomainSourceModelTypes() {
		return DomainSourceTypesRegistry.getInstance().getDomainSourceTypes(domain); 
 	}
 
 	/**
 	 * @param sourceTypes
 	 * @param model
 	 * @return list of <code>IMetaDataSourceModelProvider</code> instances from the domain source types applicable for 
 	 * this domain for this particular uri specified in the model
 	 */
 	protected List/*<IMetaDataSourceModelProvider>*/ locateMetaDataSourceInstances(List/*<IDomainSourceModelType>*/ sourceTypes, MetaDataModel model) {
 		List/*<IMetaDataSourceModelProvider>*/ sources = new ArrayList/*<IMetaDataSourceModelProvider>*/();		
 		for (Iterator/*<IDomainSourceModelType>*/ it = sourceTypes.iterator();it.hasNext();){
 			IDomainSourceModelType sourceType = (IDomainSourceModelType)it.next();
 			IMetaDataLocator locator = sourceType.getLocator();
 			//We MUST set the sourceType here to associate the handler with locator to use for the source models
 			locator.setDomainSourceModelType(sourceType);
 			
 			//set project context in locator for those that care
 			if (locator instanceof IPathSensitiveMetaDataLocator)
 				((IPathSensitiveMetaDataLocator)locator).setProjectContext(model.getModelKey().getProject());
 			
 			List/*<IMetaDataSourceModelProvider>*/ providers = sourceType.getLocator().locateMetaDataModelProviders(model.getModelKey().getUri());
 			if (providers != null && !providers.isEmpty()){
 				for (Iterator mdProviders =providers.iterator();mdProviders.hasNext();){
 					IMetaDataSourceModelProvider provider = (IMetaDataSourceModelProvider)mdProviders.next();
 					//We MUST set the sourceType here to associate the translators to use for the source models
 					provider.setLocator(sourceType.getLocator());
 					sources.add(provider);
 				}
 			}
 			//listen for changes
 			sourceType.getLocator().addObserver(this);
 		}
 		return sources;
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.jst.jsf.common.metadata.internal.IMetaDataObserver#notifyMetadataChanged(org.eclipse.jst.jsf.common.metadata.internal.IMetaDataChangeNotificationEvent)
 	 */
 	public void notifyMetadataChanged(IMetaDataChangeNotificationEvent event) {
 		//for now, if any event occurs, we need to flush the model so that it will rebuild
 		model.setNeedsRefresh();
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.jst.jsf.common.metadata.internal.IDomainLoadingStrategy#cleanup()
 	 */
 	public void cleanup(){
 		removeOldLocatorObservers();
 		sources = null;
 		sourceTypes = null;
 		model = null;
 	}
 	
 	private void removeOldLocatorObservers(){
 		if (sources != null){
 			for (Iterator it= sources.iterator();it.hasNext();){				
 				IMetaDataSourceModelProvider provider = (IMetaDataSourceModelProvider)it.next();
 				IMetaDataLocator locator = provider.getLocator();
 				locator.removeObserver(this);		
 				locator.setDomainSourceModelType(null);
 				provider.setLocator(null);
 			}
 		}
 	}
 
 }
