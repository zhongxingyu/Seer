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
 
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.logging.Level;
 
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrResponse;
 import org.apache.solr.client.solrj.SolrServer;
 import org.apache.solr.client.solrj.SolrServerException;
 import org.apache.solr.common.SolrInputDocument;
 import org.jibx.runtime.BindingDirectory;
 import org.jibx.runtime.IBindingFactory;
 import org.jibx.runtime.IUnmarshallingContext;
 import org.jibx.runtime.JiBXException;
 import org.springframework.beans.factory.annotation.Value;
 
 import eu.europeana.corelib.definitions.jibx.RDF;
 import eu.europeana.corelib.definitions.jibx.RDF.Choice;
 import eu.europeana.corelib.definitions.solr.beans.FullBean;
 import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
 import eu.europeana.corelib.solr.entity.AgentImpl;
 import eu.europeana.corelib.solr.entity.AggregationImpl;
 import eu.europeana.corelib.solr.entity.ConceptImpl;
 import eu.europeana.corelib.solr.entity.PlaceImpl;
 import eu.europeana.corelib.solr.entity.ProvidedCHOImpl;
 import eu.europeana.corelib.solr.entity.ProxyImpl;
 import eu.europeana.corelib.solr.entity.TimespanImpl;
 import eu.europeana.corelib.solr.entity.WebResourceImpl;
 import eu.europeana.corelib.solr.exceptions.MongoDBException;
 import eu.europeana.corelib.solr.server.MongoDBServer;
 import eu.europeana.corelib.solr.server.impl.MongoDBServerImpl;
 import eu.europeana.corelib.solr.server.importer.util.AgentFieldInput;
 import eu.europeana.corelib.solr.server.importer.util.AggregationFieldInput;
 import eu.europeana.corelib.solr.server.importer.util.ConceptFieldInput;
 import eu.europeana.corelib.solr.server.importer.util.PlaceFieldInput;
 import eu.europeana.corelib.solr.server.importer.util.ProvidedCHOFieldInput;
 import eu.europeana.corelib.solr.server.importer.util.ProxyFieldInput;
 import eu.europeana.corelib.solr.server.importer.util.TimespanFieldInput;
 import eu.europeana.corelib.solr.server.importer.util.WebResourcesFieldInput;
 import eu.europeana.uim.api.AbstractIngestionPlugin;
 import eu.europeana.uim.api.CorruptedMetadataRecordException;
 import eu.europeana.uim.api.ExecutionContext;
 import eu.europeana.uim.api.IngestionPluginFailedException;
 import eu.europeana.uim.common.TKey;
 import eu.europeana.uim.model.europeanaspecific.EuropeanaModelRegistry;
 import eu.europeana.uim.solr3.Solr3Initializer;
 import eu.europeana.uim.store.MetaDataRecord;
 
 /**
  * This is the main class implementing the UIM functionality for 
  * the solr workflow plugin exposed as an OSGI service.
  * 
  * @author Georgios Markakis
  * @author Yorgos.Mamakis@ kb.nl
  *
  */
 public class SolrWorkflowPlugin extends AbstractIngestionPlugin {
 
 	@Value("#{europeanaProperties['solr.selectUrl']}")
 	private static String solrUrl;
 	@Value("#{europeanaProperties['mongoDB.host']}")
 	private static String mongoDBhost;
 	@Value("#{europeanaProperties['mongoDB.port']}")
 	private static int mongoDBport;
 	private static SolrServer solrServer;
 	private static MongoDBServer mongoServer;
 	public SolrWorkflowPlugin() {
 		super("solr_workflow", "Solr Repository Ingestion Plugin"); 
 	}
 
 
 
 
 	/* (non-Javadoc)
 	 * @see eu.europeana.uim.api.IngestionPlugin#processRecord(eu.europeana.uim.MetaDataRecord, eu.europeana.uim.api.ExecutionContext)
 	 */
 	public  <I> boolean processRecord(MetaDataRecord<I> mdr, ExecutionContext<I> context)	
     throws IngestionPluginFailedException, CorruptedMetadataRecordException{
 
 		SolrInputDocument solrInputDocument = null;
 		
 		try{
 			IBindingFactory bfact = BindingDirectory.getFactory(RDF.class);
 			IUnmarshallingContext uctx = bfact.createUnmarshallingContext();
 			RDF rdf = (RDF) uctx.unmarshalDocument(new ByteArrayInputStream(mdr.getValues(EuropeanaModelRegistry.UNCLASSIFIED, null).get(0).getBytes()), null);
 			List<Choice> rdfElements = rdf.getChoiceList();
 			solrInputDocument = new SolrInputDocument();
 			FullBean fullBean = new FullBeanImpl();
 			List<AggregationImpl> aggregations = Collections.synchronizedList(new ArrayList<AggregationImpl>());
 			List<AgentImpl> agentList = new ArrayList<AgentImpl>();
 			List<ConceptImpl> conceptList = new ArrayList<ConceptImpl>();
 			List<PlaceImpl> placeList = new ArrayList<PlaceImpl>();
 			List<ProvidedCHOImpl> providedCHOList = new ArrayList<ProvidedCHOImpl>();
 			List<ProxyImpl> proxyList = new ArrayList<ProxyImpl>();
 			List<WebResourceImpl> webResourceList = new ArrayList<WebResourceImpl>();
 			List<TimespanImpl> timespanList = new ArrayList<TimespanImpl>();
 			for (Choice rdfElement : rdfElements){
 		
 				if(rdfElement.ifAgent()){
 					solrInputDocument = AgentFieldInput.createAgentSolrFields(rdfElement.getAgent(), solrInputDocument);
 					agentList.add(AgentFieldInput.createAgentMongoEntity(rdfElement.getAgent(), mongoServer));
 				}
 				else if(rdfElement.ifAggregation()){
 					solrInputDocument = AggregationFieldInput.createAggregationSolrFields(rdfElement.getAggregation(), solrInputDocument);
 					AggregationImpl aggregation = AggregationFieldInput.createAggregationMongoFields(rdfElement.getAggregation(),mongoServer);
 					if(webResourceList.size()>0){
 						AggregationFieldInput.appendWebResource(aggregations, webResourceList, mongoServer);
 					}
 					aggregations.add(aggregation);
 				}
 				else if(rdfElement.ifConcept()){
 					solrInputDocument = ConceptFieldInput.createConceptSolrFields(rdfElement.getConcept(),solrInputDocument);
 					conceptList.add(ConceptFieldInput.createConceptMongoFields(rdfElement.getConcept(), mongoServer));
 				}
 				else if(rdfElement.ifPlace()){
 					solrInputDocument = PlaceFieldInput.createPlaceSolrFields(rdfElement.getPlace(),solrInputDocument) ;
 					placeList.add(PlaceFieldInput.createPlaceMongoFields(rdfElement.getPlace(), mongoServer));
 				}
 				else if(rdfElement.ifProvidedCHO()){
 					
 					solrInputDocument = ProvidedCHOFieldInput.createProvidedCHOFields(rdfElement.getProvidedCHO(),solrInputDocument);
 					providedCHOList.add(ProvidedCHOFieldInput.createProvidedCHOMongoFields(rdfElement.getProvidedCHO(), mongoServer));
 					if(proxyList.size()>0){
 						proxyList.set(0, ProxyFieldInput.createProxyMongoFields(new ProxyImpl(),rdfElement.getProvidedCHO(), mongoServer));
 					}
 					else{
 						proxyList.add(ProxyFieldInput.createProxyMongoFields(new ProxyImpl(),rdfElement.getProvidedCHO(), mongoServer));
 					}
 				}
 				
 				else if (rdfElement.ifTimeSpan()) {
 					solrInputDocument = TimespanFieldInput.createTimespanSolrFields(rdfElement.getTimeSpan(), solrInputDocument);
 					timespanList.add(TimespanFieldInput.createTimespanMongoField(rdfElement.getTimeSpan(),mongoServer));
 				}
 				else{
 					solrInputDocument = WebResourcesFieldInput.createWebResourceSolrFields(rdfElement.getWebResource(), solrInputDocument);
 					webResourceList.add(WebResourcesFieldInput.createWebResourceMongoField(rdfElement.getWebResource(), mongoServer));
 					if(aggregations.size()>0){
 						aggregations.set(0, AggregationFieldInput.appendWebResource(aggregations, webResourceList, mongoServer));
 					}
 				}
 			}
 			fullBean.setAggregations(aggregations);
 			fullBean.setProvidedCHOs(providedCHOList);
 			fullBean.setConcepts(conceptList);
 			fullBean.setPlaces(placeList);
 			fullBean.setTimespans(timespanList);
 			fullBean.setProxies(proxyList);
 			fullBean.setAgents(agentList);
 			
 			mongoServer.getDatastore().save(fullBean);
 			List<SolrInputDocument> records = new ArrayList<SolrInputDocument>();
 			records.add(solrInputDocument);
 			solrServer.add(records);
 			return true;
 		}
 		catch (JiBXException e) {
 			context.getLoggingEngine().logFailed(Level.SEVERE, this, e, "JiBX unmarshalling has failed with the following error: "+ e.getMessage());
 		} catch (InstantiationException e) {
 			context.getLoggingEngine().logFailed(Level.SEVERE, this, e, "Unkwown error: "+ e.getMessage());
 			
 		} catch (IllegalAccessException e) {
 			context.getLoggingEngine().logFailed(Level.SEVERE, this, e, "Unknown error: "+ e.getMessage());
 		} catch (SolrServerException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return false;
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
 
 	public void initialize(ExecutionContext context) throws IngestionPluginFailedException {
 		Solr3Initializer solr3Initializer = new Solr3Initializer(solrUrl,"");
 		solrServer = solr3Initializer.getServer();
 		try {
 			mongoServer = new MongoDBServerImpl(mongoDBhost,mongoDBport,"europeana");
 		} catch (MongoDBException e) {
 			context.getLoggingEngine().logFailed(Level.SEVERE, this, e, "Mongo DB server error: "+ e.getMessage());
 		}
 	}
 
 	public void completed(ExecutionContext context)
 			throws IngestionPluginFailedException {
 		try{
 			solrServer.commit();
 			solrServer.optimize();
 		}
 		catch(IOException e){
 			context.getLoggingEngine().logFailed(Level.SEVERE, this, e, "Input/Output exception occured in Solr with the following message: "+ e.getMessage());
 		} catch (SolrServerException e) {
 			context.getLoggingEngine().logFailed(Level.SEVERE, this, e, "Solr server exception occured in Solr with the following message: "+ e.getMessage());
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
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 
 
 
 
 	
 
 }
