 package eu.europeana.uim.plugin.solr.test;
 
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.apache.commons.io.FileUtils;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import com.google.code.morphia.Datastore;
 import com.google.code.morphia.DatastoreImpl;
 import com.google.code.morphia.Morphia;
 import com.hp.hpl.jena.rdf.model.RDFReaderF;
 import com.hp.hpl.jena.rdf.model.impl.RDFReaderFImpl;
 import com.mongodb.Mongo;
 
 import de.flapdoodle.embed.mongo.MongodExecutable;
 import de.flapdoodle.embed.mongo.MongodStarter;
 import de.flapdoodle.embed.mongo.config.MongodConfig;
 import de.flapdoodle.embed.mongo.distribution.Version;
 
 import eu.europeana.corelib.definitions.model.EdmLabel;
 import eu.europeana.corelib.dereference.impl.ControlledVocabularyImpl;
 import eu.europeana.corelib.dereference.impl.EdmMappedField;
 import eu.europeana.corelib.dereference.impl.VocabularyMongoServerImpl;
 import eu.europeana.uim.logging.LoggingEngine;
 import eu.europeana.uim.logging.LoggingEngineAdapter;
 import eu.europeana.uim.model.europeana.EuropeanaModelRegistry;
 import eu.europeana.uim.model.europeanaspecific.fieldvalues.ControlledVocabularyProxy;
 import eu.europeana.uim.orchestration.ActiveExecution;
 import eu.europeana.uim.plugin.solr.service.SolrWorkflowPlugin;
 import eu.europeana.uim.plugin.solr.service.SolrWorkflowService;
 import eu.europeana.uim.plugin.solr.service.SolrWorkflowServiceImpl;
 import eu.europeana.uim.plugin.solr.utils.OsgiExtractor;
 import eu.europeana.uim.store.Collection;
 import eu.europeana.uim.store.MetaDataRecord;
 import eu.europeana.uim.store.bean.CollectionBean;
 import eu.europeana.uim.store.bean.ExecutionBean;
 import eu.europeana.uim.store.bean.MetaDataRecordBean;
 import eu.europeana.uim.store.bean.ProviderBean;
 import eu.europeana.uim.sugar.SugarCrmRecord;
 import eu.europeana.uim.sugar.SugarCrmService;
 import eu.europeana.uim.sugarcrmclient.plugin.SugarCRMServiceImpl;
 import eu.europeana.uim.sugarcrmclient.plugin.objects.SugarCrmRecordImpl;
 
 public class SolrPluginTest {
  
 	private final String record = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><record><first><item><name>previews_only_on_europeana_por_c</name><type>0</type></item></first></record>";
 	
 	
 	private SolrWorkflowPlugin plugin;
 	private OsgiExtractor extractor;
 	private MongodExecutable mongoExec;
 	Datastore datastore;
 	
 	
 	
 	private void prepareConceptMappings() {
 		ControlledVocabularyImpl voc = new ControlledVocabularyImpl();
 		voc.setName("MIMO-Concepts");
 		voc.setRules(new String[]{"InstrumentsKeywords","HornbostelAndSachs"});
 		voc.setIterations(0);
 		voc.setURI("http://www.mimo-db.eu/");
 		voc.setSuffix(".rdf");
 		extractor.setVocabulary(voc);
 		extractor.readSchema("src/test/resources/MIMO-Concepts");
 		extractor.setMappedField("skos:prefLabel", EdmLabel.CC_SKOS_PREF_LABEL, null);
 		extractor.setMappedField("skos:Concept", EdmLabel.SKOS_CONCEPT, null);
 		extractor.setMappedField("skos:related", EdmLabel.CC_SKOS_RELATED, null);
 		extractor.setMappedField("skos:narrower", EdmLabel.CC_SKOS_NARROWER, null);
 		extractor.setMappedField("skos:Concept_rdf:about", EdmLabel.SKOS_CONCEPT, null);
 		extractor.setMappedField("skos:prefLabel", EdmLabel.CC_SKOS_PREF_LABEL, null);
 		extractor.setMappedField("skos:prefLabel_xml:lang", EdmLabel.CC_SKOS_PREF_LABEL, "xml:lang");
 		extractor.setMappedField("skos:broader", EdmLabel.CC_SKOS_BROADER, null);
 		extractor.setMappedField("skos:inScheme", EdmLabel.CC_SKOS_INSCHEME, null);
 		extractor.saveMapping(0);
 	}
 
 
 	@Test
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	public void testConcept() {
 		String RECORD = null;
 		try {
 			
 			RECORD = FileUtils.readFileToString(new File("src/test/resources/edm_concept.xml"));
 			MongodConfig conf = new MongodConfig(Version.V2_0_7, 10000,
 					false);
 
 			MongodStarter runtime = MongodStarter.getDefaultInstance();
 			SolrWorkflowService solrService = mock(SolrWorkflowServiceImpl.class);
 			mongoExec = runtime.prepare(conf);
 			try {
 				mongoExec.start();
 				Morphia morphia = new Morphia();
 				morphia.map(ControlledVocabularyImpl.class);
 				Mongo mongo = new Mongo("localhost", 10000);
 				datastore = new DatastoreImpl(morphia,mongo,"voc_test");
 				extractor = new OsgiExtractor(solrService);
 				extractor.setDatastore(datastore);
 				extractor.setMongoServer(new VocabularyMongoServerImpl(mongo, "voc_test"));
 				prepareConceptMappings();
 				preparePlaceMappings();
 				prepareAgentMappings();
 				prepareTimeMappings();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 		ActiveExecution context = mock(ActiveExecution.class);
 		Collection collection = new CollectionBean("09431",
 				new ProviderBean<String>("test_provider"));
 		
 		collection.putValue(ControlledVocabularyProxy.SUGARCRMID, "09431");
 		collection.setMnemonic("12345");
 		
 		MetaDataRecord mdr = new MetaDataRecordBean<String>("09431", collection);
 		mdr.addValue(EuropeanaModelRegistry.EDMRECORD, RECORD);
 		SugarCrmService service = mock(SugarCRMServiceImpl.class);
 		
 		
 
 		RDFReaderF reader = new RDFReaderFImpl();
 		when(solrService.getRDFReaderF()).thenReturn(reader);
 		when(solrService.getDatastore()).thenReturn(datastore);
 		when(solrService.getExtractor()).thenReturn(extractor);
 		plugin = new SolrWorkflowPlugin(solrService);
 		
 		
 		ExecutionBean execution = new ExecutionBean();
 		execution.setDataSet(collection);
 		Properties properties = new Properties();
 		LoggingEngine logging = LoggingEngineAdapter.LONG;
 		
 		SugarCrmRecord sugarRecord =  SugarCrmRecordImpl.getInstance(getElement(record));
 		try {
 			when(service.retrieveRecord("09431")).thenReturn(sugarRecord);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		when(context.getExecution()).thenReturn(execution);
 		when(context.getProperties()).thenReturn(properties);
 		when(context.getLoggingEngine()).thenReturn(logging);
 		plugin.initialize(context);
 		plugin.process(mdr, context);
 		//Assert.assertTrue();
 		System.out.println(mdr.getValues(EuropeanaModelRegistry.EDMDEREFERENCEDRECORD).get(0));
 		plugin.completed(context);
		Assert.assertEquals(1, SolrWorkflowPlugin.getRecords());
 		
 		plugin.shutdown();
 		} catch (Exception e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
         
 	}
 
 	private void prepareTimeMappings() {
 		
 	}
 
 
 	private void prepareAgentMappings() {
 		ControlledVocabularyImpl voc = new ControlledVocabularyImpl();
 		voc.setName("MIMO-Agents");
 		voc.setRules(new String[]{"InstrumentMaker"});
 		voc.setIterations(0);
 		voc.setURI("http://www.mimo-db.eu/");
 		voc.setSuffix(".rdf");
 		extractor.setVocabulary(voc);
 		extractor.readSchema("src/test/resources/MIMO-Agents");
 		extractor.setMappedField("skos:Concept", EdmLabel.EDM_AGENT, null);
 		extractor.setMappedField("rdaGr2:preferredNameForThePerson", EdmLabel.AG_SKOS_PREF_LABEL, null);
 		extractor.setMappedField("rdaGr2:dateOfBirth", EdmLabel.AG_RDAGR2_DATEOFBIRTH, null);
 		extractor.setMappedField("rdaGr2:dateOfDeath", EdmLabel.AG_RDAGR2_DATEOFDEATH, null);
 		extractor.setMappedField("foaf:name", EdmLabel.AG_FOAF_NAME, null);
 		extractor.setMappedField("skos:Concept_rdf:about", EdmLabel.EDM_AGENT, null);
 		extractor.saveMapping(0);
 	}
 
 
 	private void preparePlaceMappings() {
 		ControlledVocabularyImpl voc = new ControlledVocabularyImpl();
 		voc.setName("Geonames");
 		voc.setRules(new String[]{"*"});
 		voc.setIterations(0);
 		voc.setURI("http://www.geonames.org/");
 		voc.setReplaceUrl("http://sws.geonames.org/");
 		voc.setSuffix("/about.rdf");
 		extractor.setVocabulary(voc);
 		extractor.readSchema("src/test/resources/Geonames");
 		extractor.setMappedField("gn:Feature", EdmLabel.EDM_PLACE, null);
 		extractor.setMappedField("gn:Feature_rdf:about", EdmLabel.EDM_PLACE, null);
 		extractor.setMappedField("gn:parentADM1", EdmLabel.PL_DCTERMS_ISPART_OF, null);
 		extractor.setMappedField("gn:alternateName", EdmLabel.PL_SKOS_ALT_LABEL,null);
 		extractor.setMappedField("gn:name", EdmLabel.PL_SKOS_PREF_LABEL, null);
 		extractor.setMappedField("gn:alternateName_xml:lang", EdmLabel.PL_SKOS_ALT_LABEL,"xml:lang");
 		extractor.setMappedField("wgs84_pos:long", EdmLabel.PL_WGS84_POS_LONG,null);
 		extractor.setMappedField("wgs84_pos:lat", EdmLabel.PL_WGS84_POS_LAT,null);
 		extractor.saveMapping(0);
 	}
 
 
 	private Element getElement(String record2) {
 		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 		try {
 			Document doc = dbf.newDocumentBuilder().parse(new ByteArrayInputStream(record2.getBytes()));
 			
 			return  (Element)doc.getDocumentElement().getElementsByTagName("first").item(0);
 		}
 		catch(Exception e){
 			e.printStackTrace();
 			return null;
 		}
 		
 	}
 
 	
 }
