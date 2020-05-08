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
 package eu.europeana.dedup.osgi.service;
 
 import java.io.StringWriter;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;

 import eu.europeana.corelib.definitions.jibx.AggregatedCHO;
 import eu.europeana.corelib.definitions.jibx.Aggregation;
 import eu.europeana.corelib.definitions.jibx.ProvidedCHOType;
 import eu.europeana.corelib.definitions.jibx.ProxyType;
 import eu.europeana.corelib.definitions.jibx.RDF;
 import eu.europeana.corelib.tools.lookuptable.EuropeanaIdRegistry;
 import eu.europeana.corelib.tools.lookuptable.EuropeanaIdRegistryMongoServer;
 import eu.europeana.corelib.tools.lookuptable.FailedRecord;
 import eu.europeana.corelib.tools.lookuptable.LookupResult;
 import eu.europeana.corelib.tools.lookuptable.LookupState;
import eu.europeana.corelib.tools.lookuptable.impl.EuropeanaIdRegistryMongoServerImpl;
 import eu.europeana.dedup.osgi.service.exceptions.DeduplicationException;
 import eu.europeana.dedup.utils.Decoupler;
 import eu.europeana.dedup.utils.PropertyReader;
 import eu.europeana.dedup.utils.UimConfigurationProperty;
 import eu.europeana.uim.common.BlockingInitializer;
 
 /**
  * 
  * @author Georgios Markakis <gwarkx@hotmail.com>
  * @since 27 Sep 2012
  */
 public class DeduplicationServiceImpl implements DeduplicationService {
 
 	/**
 	 * Set the Logging variable to use logging within this class
 	 */
 	private static final Logger log = Logger
 			.getLogger(DeduplicationServiceImpl.class.getName());
 
 	EuropeanaIdRegistryMongoServer mongoserver;
 
 	private IBindingFactory bfact;
 	
 	
 	
 	/**
 	 * Default Constructor
 	 */
 	public DeduplicationServiceImpl() {
 
 		try {
 			
 			bfact = BindingDirectory.getFactory(RDF.class);
 			final Mongo mongo = new Mongo(
 					PropertyReader
 							.getProperty(UimConfigurationProperty.MONGO_HOSTURL),
 					Integer.parseInt(PropertyReader
 							.getProperty(UimConfigurationProperty.MONGO_HOSTPORT)));
 
 			BlockingInitializer initializer = new BlockingInitializer() {
 				@Override
 				public void initializeInternal() {
 					try {
 						status = STATUS_BOOTING;
						mongoserver = new EuropeanaIdRegistryMongoServerImpl(mongo,
 								PropertyReader.getProperty(UimConfigurationProperty.MONGO_DB_EUROPEANAIDREGISTRY));
 
 						boolean test = mongoserver.oldIdExists("something");
 						boolean test2 = mongoserver.getDatastore()
 								.find(FailedRecord.class)
 								.filter("collectionId", "test").asList() != null;
 						log.log(java.util.logging.Level.INFO, "OK");
 						status = STATUS_INITIALIZED;
 					} catch (Throwable t) {
 						log.log(java.util.logging.Level.SEVERE,
 								"Failed to initialize Deduplication Service.",
 								t);
 						status = STATUS_FAILED;
 					}
 				}
 			};
 			initializer.initialize(EuropeanaIdRegistryMongoServer.class
 					.getClassLoader());
 
 		} catch (UnknownHostException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (MongoException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (JiBXException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 	
 	
 	/* (non-Javadoc)
 	 * @see eu.europeana.dedup.osgi.service.DeduplicationService#retrieveEuropeanaIDFromOld(java.lang.String)
 	 */
 	@Override
 	public List<String> retrieveEuropeanaIDFromOld(String oldID,String collectionID) {
 		
 		List<String> retlist = new ArrayList<String>();
 		
 		List<EuropeanaIdRegistry> results = mongoserver.retrieveEuropeanaIdFromOriginal(oldID, collectionID);
 		
 		for(EuropeanaIdRegistry result: results){
 			retlist.add(result.getEid());
 		}
 		return retlist;
 	}
 
 	
 	
 	/* (non-Javadoc)
 	 * @see eu.europeana.dedup.osgi.service.DeduplicationService#deleteEuropeanaID(java.lang.String)
 	 */
 	@Override
 	public void deleteEuropeanaID(String newEuropeanaID) {
 		mongoserver.deleteEuropeanaIdFromNew(newEuropeanaID);
 	}
 	
 	
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * eu.europeana.dedup.osgi.service.DeduplicationService#deduplicateRecord
 	 * (java.lang.String, java.lang.String)
 	 */
 	@Override
 	public List<DeduplicationResult> deduplicateRecord(String collectionID,
 			String sessionid, String edmRecord) throws DeduplicationException,JiBXException {
 
 		List<DeduplicationResult> deduplist = new ArrayList<DeduplicationResult>();
 		Decoupler dec = new Decoupler();
 		List<RDF> decoupledResults = dec.decouple(edmRecord);
 		for (RDF result : decoupledResults) {
 			DeduplicationResult dedupres = new DeduplicationResult();
 			dedupres.setEdm(unmarshall(result));
 			List<ProxyType> proxylist = result.getProxyList();
 			String nonUUID = null;
 			for (ProxyType proxy : proxylist) {
 					nonUUID = proxy.getAbout();
 				}
 			
 			LookupResult lookup = mongoserver.lookupUiniqueId(nonUUID,
 					collectionID, dedupres.getEdm(), sessionid);
 			updateInternalReferences(result, lookup.getEuropeanaID());
 			dedupres.setEdm(unmarshall(result));
 			dedupres.setLookupresult(lookup);
 			dedupres.setDerivedRecordID(lookup.getEuropeanaID());
 			deduplist.add(dedupres);
 		}
 
 		return deduplist;
 
 	}
 
 	/**
 	 * Updates europeanaID references in the provided EDM JIBX representation
 	 * 
 	 * @param edm
 	 * @param newID
 	 */
 	private void updateInternalReferences(RDF edm, String newID) {
 		 List<ProxyType> proxylist = edm.getProxyList();
 		 List<Aggregation> aggregationlist = edm.getAggregationList();
 		 List<ProvidedCHOType> prcholist = edm.getProvidedCHOList();
 
 		for (ProxyType proxy : proxylist) {
 			proxy.setAbout(newID);
 		}
 
 		for (ProvidedCHOType cho : prcholist) {
 			String origId = cho.getAbout();
 			cho.setAbout(newID);
 			
 			for (Aggregation aggregation : aggregationlist) {
 				 AggregatedCHO aggrcho = aggregation.getAggregatedCHO();
 				 if(aggrcho != null && origId.equals(aggrcho.getResource())){
 					 aggregation.getAggregatedCHO().setResource(newID);
 				 }				 
 			}
 		}
 		
 	}
 
 	/**
 	 * @param edm
 	 * @return
 	 * @throws JiBXException
 	 */
 	private String unmarshall(RDF edm) throws JiBXException {
 		IMarshallingContext mctx = bfact.createMarshallingContext();
 		mctx.setIndent(2);
 		StringWriter stringWriter = new StringWriter();
 		mctx.setOutput(stringWriter);
 		mctx.marshalDocument(edm);
 		String edmstring = stringWriter.toString();
 
 		return edmstring;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * eu.europeana.dedup.osgi.service.DeduplicationService#getFailedRecords
 	 * (java.lang.String)
 	 */
 	@Override
 	public List<Map<String, String>> getFailedRecords(String collectionId) {
 
 		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
 		ExtendedBlockingInitializer initializer = new ExtendedBlockingInitializer(
 				collectionId, list) {
 
 			@Override
 			protected void initializeInternal() {
 				list = mongoserver.getFailedRecords(str);
 			}
 		};
 		initializer.initialize(EuropeanaIdRegistryMongoServer.class
 				.getClassLoader());
 
 		return initializer.getList();
 	}
 
 
 
 	/* (non-Javadoc)
 	 * @see eu.europeana.dedup.osgi.service.DeduplicationService#createUpdateIdStatus(java.lang.String, java.lang.String, eu.europeana.corelib.tools.lookuptable.LookupState)
 	 */
 	@Override
 	public void createUpdateIdStatus(String oldEuropeanaID,String newEuropeanaID,String collectionID,String xml,LookupState state) {
 		mongoserver.createFailedRecord(state, collectionID, oldEuropeanaID, newEuropeanaID, xml);
 	}
 
 
 
 	@Override
 	public void deleteFailedRecord(String europeanaId,String collectionID) {
 		mongoserver.deleteFailedRecord(europeanaId,collectionID);
 	}
 
 
 
 }
