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
 
 package eu.europeana.uim.plugin.solr.service;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.net.MalformedURLException;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.solr.client.solrj.SolrServer;
 import org.apache.solr.client.solrj.SolrServerException;
 import org.apache.solr.common.SolrException;
 import org.apache.solr.common.SolrInputDocument;
 import org.jibx.runtime.BindingDirectory;
 import org.jibx.runtime.IBindingFactory;
 import org.jibx.runtime.IUnmarshallingContext;
 import org.jibx.runtime.JiBXException;
 
 import com.mongodb.Mongo;
 import com.mongodb.MongoException;
 
 import eu.europeana.corelib.definitions.jibx.RDF;
 import eu.europeana.corelib.definitions.model.EdmLabel;
 import eu.europeana.corelib.definitions.solr.beans.FullBean;
 import eu.europeana.corelib.dereference.impl.Dereferencer;
 import eu.europeana.corelib.dereference.impl.VocabularyMongoServer;
 import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
 import eu.europeana.corelib.solr.exceptions.MongoDBException;
 import eu.europeana.corelib.solr.server.EdmMongoServer;
 import eu.europeana.corelib.solr.server.impl.EdmMongoServerImpl;
 import eu.europeana.corelib.solr.utils.EseEdmMap;
 import eu.europeana.corelib.solr.utils.MongoConstructor;
 import eu.europeana.corelib.solr.utils.MongoUtils;
 import eu.europeana.corelib.solr.utils.SolrConstructor;
 import eu.europeana.corelib.tools.lookuptable.EuropeanaId;
 import eu.europeana.corelib.tools.lookuptable.EuropeanaIdMongoServer;
 import eu.europeana.corelib.tools.utils.HashUtils;
 import eu.europeana.corelib.tools.utils.PreSipCreatorUtils;
 import eu.europeana.corelib.tools.utils.SipCreatorUtils;
 import eu.europeana.uim.api.AbstractIngestionPlugin;
 import eu.europeana.uim.api.CorruptedMetadataRecordException;
 import eu.europeana.uim.api.ExecutionContext;
 import eu.europeana.uim.api.IngestionPluginFailedException;
 import eu.europeana.uim.common.TKey;
 import eu.europeana.uim.model.europeanaspecific.EuropeanaModelRegistry;
 import eu.europeana.uim.model.europeanaspecific.fieldvalues.ControlledVocabularyProxy;
 import eu.europeana.uim.model.europeanaspecific.fieldvalues.EuropeanaRetrievableField;
 import eu.europeana.uim.solr3.Solr3Initializer;
 import eu.europeana.uim.store.Collection;
 import eu.europeana.uim.store.MetaDataRecord;
 import eu.europeana.uim.sugarcrm.QueryResultException;
 import eu.europeana.uim.sugarcrm.SugarCrmRecord;
 import eu.europeana.uim.sugarcrm.SugarCrmService;
 
 /**
  * This is the main class implementing the UIM functionality for the solr
  * workflow plugin exposed as an OSGI service.
  * 
  * @author Georgios Markakis
  * @author Yorgos.Mamakis@ kb.nl
  * 
  */
 public class SolrWorkflowPlugin extends AbstractIngestionPlugin {
 
 	private static String solrUrl;
 	private static Mongo mongo;
 	private static SolrServer solrServer;
 	private static EdmMongoServer mongoServer;
 	private static int recordNumber;
 	private static final int RETRIES = 10;
 
 	private static SugarCrmService sugarCrmService;
 	private static String previewsOnlyInPortal;
 	private static String europeanaID;
 	private static String collections;
 	private static String mongoHost;
 	private static int mongoPort;
 	private static String mongoDB;
 	private static String solrCore;
 	private static String repository;
 	private static String vocabularyDB;
 
 	// GETTERS & SETTERS
 	public SugarCrmService getSugarCrmService() {
 		return sugarCrmService;
 	}
 
 	public void setSugarCrmService(SugarCrmService sugarCrmService) {
 		SolrWorkflowPlugin.sugarCrmService = sugarCrmService;
 	}
 
 	public void setVocabularyDB(String vocabularyDB) {
 		SolrWorkflowPlugin.vocabularyDB = vocabularyDB;
 	}
 
 	public String getVocabularyDB() {
 		return vocabularyDB;
 	}
 
 	public String getSolrUrl() {
 		return solrUrl;
 	}
 
 	public void setSolrUrl(String solrUrl) {
 		SolrWorkflowPlugin.solrUrl = solrUrl;
 	}
 
 	public String getEuropeanaID() {
 		return europeanaID;
 	}
 
 	public void setEuropeanaID(String europeanaID) {
 		SolrWorkflowPlugin.europeanaID = europeanaID;
 	}
 
 	public String getCollections() {
 		return collections;
 	}
 
 	public void setCollections(String collections) {
 		SolrWorkflowPlugin.collections = collections;
 	}
 
 	public String getMongoHost() {
 		return mongoHost;
 	}
 
 	public void setMongoHost(String mongoHost) {
 		SolrWorkflowPlugin.mongoHost = mongoHost;
 	}
 
 	public int getMongoPort() {
 		return mongoPort;
 	}
 
 	public void setMongoPort(int mongoPort) {
 		SolrWorkflowPlugin.mongoPort = mongoPort;
 	}
 
 	public String getMongoDB() {
 		return mongoDB;
 	}
 
 	public void setMongoDB(String mongoDB) {
 		SolrWorkflowPlugin.mongoDB = mongoDB;
 	}
 
 	public String getSolrCore() {
 		return solrCore;
 	}
 
 	public void setSolrCore(String solrCore) {
 		SolrWorkflowPlugin.solrCore = solrCore;
 	}
 
 	public String getRepository() {
 		return repository;
 	}
 
 	public void setRepository(String repository) {
 		SolrWorkflowPlugin.repository = repository;
 	}
 
 	/** Property which allows to overwrite base url from collection/provider */
 	public static final String httpzipurl = "http.overwrite.zip.baseUrl";
 
 	/**
 	 * The parameters used by this WorkflowStart
 	 */
 	private static final List<String> params = new ArrayList<String>() {
 		private static final long serialVersionUID = 1L;
 		{
 			add(httpzipurl);
 		}
 	};
 
 	public SolrWorkflowPlugin() {
 		super("solr_workflow", "Solr Repository Ingestion Plugin");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see eu.europeana.uim.api.IngestionPlugin#processRecord(eu.europeana.uim.
 	 * MetaDataRecord, eu.europeana.uim.api.ExecutionContext)
 	 */
 	public <I> boolean processRecord(MetaDataRecord<I> mdr,
 			ExecutionContext<I> context) throws IngestionPluginFailedException,
 			CorruptedMetadataRecordException {
 
 		try {
 			IBindingFactory bfact = BindingDirectory.getFactory(RDF.class);
 			IUnmarshallingContext uctx = bfact.createUnmarshallingContext();
 
 			String value = mdr.getValues(EuropeanaModelRegistry.EDMRECORD).get(
 					0);
 			MongoConstructor mongoConstructor = new MongoConstructor();
 			RDF rdf = (RDF) uctx.unmarshalDocument(new StringReader(value));
 			mongoConstructor.setMongoServer(mongoServer);
 			FullBean fullBean = mongoConstructor.constructFullBean(rdf);
 
 			String collectionId = (String) mdr.getCollection().getId();
 
 			String fileName = (String) mdr.getCollection().getName();
 			String hash = hashExists(collectionId, fileName, fullBean);
 			fullBean.getAggregations()
 					.get(0)
 					.setEdmPreviewNoDistribute(
 							Boolean.parseBoolean(previewsOnlyInPortal));
 			if (StringUtils.isNotEmpty(hash)) {
 				createLookupEntry(fullBean, hash);
 			}
 
 			if (mongoServer.getDatastore().find(FullBeanImpl.class)
 					.filter("about", fullBean.getAbout()).get() != null) {
 				System.out.println("Updating");
 				MongoUtils.updateFullBean(fullBean, mongoServer);
 			} else {
 				System.out.println("Saving");
 				mongoServer.getDatastore().save(fullBean);
 			}
 
 			int retries = 0;
 			while (retries < RETRIES) {
 				try {
 					SolrInputDocument solrInputDocument = SolrConstructor
 							.constructSolrDocument(rdf);
 					solrInputDocument.addField(
 							EdmLabel.PREVIEW_NO_DISTRIBUTE.toString(),
 							Boolean.parseBoolean(previewsOnlyInPortal));
 					solrServer.add(solrInputDocument, 1000);
 					retries = RETRIES;
 					recordNumber++;
 					return true;
 				} catch (SolrServerException e) {
 					retries++;
 				} catch (IOException e) {
 					retries++;
 				} catch (SolrException e) {
 					retries++;
 				}
 			}
 
 		} catch (JiBXException e) {
 			context.getLoggingEngine().logFailed(
 					Level.SEVERE,
 					this,
 					e,
 					"JiBX unmarshalling has failed with the following error: "
 							+ e.getMessage());
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			context.getLoggingEngine().logFailed(Level.SEVERE, this, e,
 					"Unkwown error: " + e.getMessage());
 
 		} catch (IllegalAccessException e) {
 			context.getLoggingEngine().logFailed(Level.SEVERE, this, e,
 					"Unknown error: " + e.getMessage());
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	private void createLookupEntry(FullBean fullBean, String hash) {
 		EuropeanaIdMongoServer europeanaIdMongoServer = new EuropeanaIdMongoServer(
 				mongo, europeanaID);
 		EuropeanaId europeanaId = europeanaIdMongoServer
 				.retrieveEuropeanaIdFromOld(hash).get(0);
 		europeanaId.setNewId(fullBean.getAbout());
 		europeanaIdMongoServer.saveEuropeanaId(europeanaId);
 
 	}
 
 	private String hashExists(String collectionId, String fileName,
 			FullBean fullBean) {
 		SipCreatorUtils sipCreatorUtils = new SipCreatorUtils();
 		sipCreatorUtils.setRepository(repository);
 		if (sipCreatorUtils.getHashField(collectionId, fileName) != null) {
 			return HashUtils.createHash(EseEdmMap.valueOf(
 					sipCreatorUtils.getHashField(collectionId, fileName))
 					.getEdmValue(fullBean));
 		}
 		PreSipCreatorUtils preSipCreatorUtils = new PreSipCreatorUtils();
 		preSipCreatorUtils.setRepository(repository);
 		if (preSipCreatorUtils.getHashField(collectionId, fileName) != null) {
 			return HashUtils.createHash(EseEdmMap.valueOf(
 					preSipCreatorUtils.getHashField(collectionId, fileName))
 					.getEdmValue(fullBean));
 		}
 		return null;
 	}
 
 	public void initialize() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void shutdown() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public int getPreferredThreadCount() {
 		return 5;
 	}
 
 	public int getMaximumThreadCount() {
 		return 10;
 	}
 
 	public <I> void initialize(ExecutionContext<I> context)
 			throws IngestionPluginFailedException {
 		Solr3Initializer solr3Initializer = new Solr3Initializer(solrUrl,
 				solrCore);
 		solr3Initializer.run();
 		solrServer = solr3Initializer.getServer();
 
 		try {
 			mongo = new Mongo(mongoHost, mongoPort);
 			mongoServer = new EdmMongoServerImpl(mongo, mongoDB);
 			mongoServer.getDatastore();
 
 			Dereferencer.setServer(new VocabularyMongoServer(mongo,
 					vocabularyDB));
 			@SuppressWarnings("rawtypes")
			Collection collection = (Collection) context.getExecution().getDataSet();
 			String sugarCrmId = collection
 					.getValue(ControlledVocabularyProxy.SUGARCRMID);
 
 			SugarCrmRecord sugarCrmRecord = sugarCrmService
 					.retrieveRecord(sugarCrmId);
 			previewsOnlyInPortal = sugarCrmRecord
 					.getItemValue(EuropeanaRetrievableField.PREVIEWS_ONLY_IN_PORTAL);
 
 		} catch (MongoDBException e) {
 			context.getLoggingEngine().logFailed(Level.SEVERE, this, e,
 					"Mongo DB server error: " + e.getMessage());
 			e.printStackTrace();
 		} catch (UnknownHostException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (MongoException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (QueryResultException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 	public <I> void completed(ExecutionContext<I> context)
 			throws IngestionPluginFailedException {
 		try {
 			solrServer.commit();
 			solrServer.optimize();
 
 		} catch (IOException e) {
 			context.getLoggingEngine().logFailed(
 					Level.SEVERE,
 					this,
 					e,
 					"Input/Output exception occured in Solr with the following message: "
 							+ e.getMessage());
 		} catch (SolrServerException e) {
 			context.getLoggingEngine().logFailed(
 					Level.SEVERE,
 					this,
 					e,
 					"Solr server exception occured in Solr with the following message: "
 							+ e.getMessage());
 		}
 	}
 
 	@Override
 	public TKey<?, ?>[] getInputFields() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public TKey<?, ?>[] getOptionalFields() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public TKey<?, ?>[] getOutputFields() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public List<String> getParameters() {
 		return params;
 	}
 
 	public static int getRecords() {
 		return recordNumber;
 	}
 }
