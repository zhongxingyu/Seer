 /*
  * Copyright 2007-2012 The Europeana Foundation
  *
  *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
  *  by the European Commission;
  *  You may not use this work except in compliance with the Licence.
  * 
  *  You may obtain a copy of the Licence at:
  *  http://joinup.ec.europa.eu/software/page/eupl
  *
  *  Unless required by applicable law or agreed to in writing, software distributed under
  *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
  *  any kind, either express or implied.
  *  See the Licence for the specific language governing permissions and limitations under
  *  the Licence.
  */
 package eu.europeana.uim.mintclient.workflows;
 
 import java.util.ArrayList;
 import java.util.List;
 import eu.europeana.uim.plugin.ingestion.AbstractIngestionPlugin;
 import eu.europeana.uim.plugin.ingestion.CorruptedDatasetException;
 import eu.europeana.uim.orchestration.ExecutionContext;
 import eu.europeana.uim.plugin.ingestion.IngestionPluginFailedException;
 import eu.europeana.uim.storage.StorageEngineException;
 import eu.europeana.uim.common.TKey;
 import eu.europeana.uim.mintclient.service.MintUIMService;
 import eu.europeana.uim.mintclient.service.exceptions.MintOSGIClientException;
 import eu.europeana.uim.mintclient.service.exceptions.MintRemoteException;
 import eu.europeana.uim.model.europeanaspecific.fieldvalues.EuropeanaDatasetStates;
 import eu.europeana.uim.store.Collection;
 import eu.europeana.uim.store.Provider;
 import eu.europeana.uim.sugar.QueryResultException;
 import eu.europeana.uim.sugar.SugarCrmService;
 
 /**
  * Plugin that handles the import to UIM after completion
  * @author Georgios Markakis <gwarkx@hotmail.com>
  * @since 3 Apr 2012
  */
 public class MintImportPlugin<I> extends AbstractIngestionPlugin<Collection<I>,I>{
 
 	private static MintUIMService mintservice;
 	private static SugarCrmService sugarservice;
 	/** Force provider update */
 	public static final String force_provider_update = "mint.force.poviderupdate";
 	
 	
 	/**
 	 * The parameters used by this Plugin
 	 */
 	private static final List<String> params = new ArrayList<String>() {
 		private static final long serialVersionUID = 1L;
 		{
 			add(force_provider_update);
 
 		}
 	};
 	
 	
 	/**
 	 * @param repoxservice
 	 * @param sugarservice
 	 * @param name
 	 * @param description
 	 */
 	public MintImportPlugin(MintUIMService mintservice,
 			SugarCrmService sugarservice,String name, String description) {
 		super(name, description);
 		MintImportPlugin.mintservice = mintservice;
 		MintImportPlugin.sugarservice = sugarservice;
 	}
 
 
 
 	/* (non-Javadoc)
 	 * @see eu.europeana.uim.plugin.ingestion.IngestionPlugin#getInputFields()
 	 */
 	@Override
 	public TKey<?, ?>[] getInputFields() {
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see eu.europeana.uim.plugin.Plugin#getMaximumThreadCount()
 	 */
 	@Override
 	public int getMaximumThreadCount() {
 		return 10;
 	}
 
 	/* (non-Javadoc)
 	 * @see eu.europeana.uim.plugin.ingestion.IngestionPlugin#getOptionalFields()
 	 */
 	@Override
 	public TKey<?, ?>[] getOptionalFields() {
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see eu.europeana.uim.plugin.ingestion.IngestionPlugin#getOutputFields()
 	 */
 	@Override
 	public TKey<?, ?>[] getOutputFields() {
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see eu.europeana.uim.plugin.Plugin#getParameters()
 	 */
 	@Override
 	public List<String> getParameters() {
 		return params;
 	}
 
 	/* (non-Javadoc)
 	 * @see eu.europeana.uim.plugin.Plugin#getPreferredThreadCount()
 	 */
 	@Override
 	public int getPreferredThreadCount() {
 		return 5;
 	}
 
 	/* (non-Javadoc)
 	 * @see eu.europeana.uim.plugin.Plugin#initialize()
 	 */
 	@Override
 	public void initialize() {
 	}
 
 
 	/* (non-Javadoc)
 	 * @see eu.europeana.uim.plugin.Plugin#shutdown()
 	 */
 	@Override
 	public void shutdown() {
 	}
 
 	/* (non-Javadoc)
 	 * @see eu.europeana.uim.plugin.ingestion.IngestionPlugin#process(eu.europeana.uim.store.UimDataSet, eu.europeana.uim.orchestration.ExecutionContext)
 	 */
 	@Override
 	public boolean process(Collection<I> dataset, ExecutionContext<Collection<I>,I> context)
 			throws IngestionPluginFailedException, CorruptedDatasetException {
 
 		return true;
 	}
 
 	/* (non-Javadoc)
 	 * @see eu.europeana.uim.plugin.ExecutionPlugin#initialize(eu.europeana.uim.orchestration.ExecutionContext)
 	 */
 	@Override
 	public void initialize(ExecutionContext<Collection<I>,I> context)
 			throws IngestionPluginFailedException {		
 	}
 
 
 	
 	/* (non-Javadoc)
 	 * @see eu.europeana.uim.plugin.ExecutionPlugin#completed(eu.europeana.uim.orchestration.ExecutionContext)
 	 */
 	@Override
 	public void completed(ExecutionContext<Collection<I>,I> context)
 			throws IngestionPluginFailedException {
 		Collection<I> collection = (Collection<I>) context.getDataSet();
 		Provider<I> provider = collection.getProvider();
 		
		Provider<I> provider2 =  null;
		
		try {
			provider2 = context.getStorageEngine().findProvider(provider.getMnemonic());
		} catch (StorageEngineException e1) {
			e1.printStackTrace();
		}
				
 		try {
 			
 			String forceupdate = context.getProperties().getProperty(
 					force_provider_update);
 			
			http://www.europeana.eu/schemas/ese mintservice.createMintOrganization(provider2,forceupdate);
 			
 			
 			//TODO:Commented out for the time being. Implement it when user management for Mint is ready
 			//mintservice.createMintAuthorizedUser(provider);
 			
 			mintservice.createMappingSession(collection);
 			/*
 			String sugarID = collection.getValue("sugarCRMID");
 			sugarservice.changeEntryStatus(sugarID, 
 					EuropeanaDatasetStates.MAPPING_AND_NORMALIZATION);
 		    */
 			
 		} catch (MintOSGIClientException e) {
 			throw new IngestionPluginFailedException("Error while creating mapping in Mint",e);
 		} catch (MintRemoteException e) {
 			throw new IngestionPluginFailedException("Error while creating mapping in Mint",e);
 		} catch (StorageEngineException e) {
 			throw new IngestionPluginFailedException("Error while storing data in UIM",e);
 		} 
 		/*
 		catch (QueryResultException e) {
 			throw new IngestionPluginFailedException("Error while updating data in SugarCRM",e);
 		}
 		*/
 		
 	}
 
 
 
 }
