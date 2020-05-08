 package eu.europeana.uim.plugin.solr.test;
 
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 import java.io.ByteArrayInputStream;
 import java.util.Properties;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import eu.europeana.uim.api.ActiveExecution;
 import eu.europeana.uim.api.LoggingEngine;
 import eu.europeana.uim.api.LoggingEngineAdapter;
 import eu.europeana.uim.model.europeanaspecific.EuropeanaModelRegistry;
 import eu.europeana.uim.model.europeanaspecific.fieldvalues.ControlledVocabularyProxy;
 import eu.europeana.uim.plugin.solr.service.SolrWorkflowPlugin;
 import eu.europeana.uim.store.Collection;
 import eu.europeana.uim.store.ControlledVocabularyKeyValue;
 import eu.europeana.uim.store.MetaDataRecord;
 import eu.europeana.uim.store.bean.CollectionBean;
 import eu.europeana.uim.store.bean.ExecutionBean;
 import eu.europeana.uim.store.bean.MetaDataRecordBean;
 import eu.europeana.uim.store.bean.ProviderBean;
 import eu.europeana.uim.sugarcrm.QueryResultException;
 import eu.europeana.uim.sugarcrm.SugarCrmRecord;
 import eu.europeana.uim.sugarcrm.SugarCrmService;
 import eu.europeana.uim.sugarcrmclient.plugin.SugarCRMServiceImpl;
 import eu.europeana.uim.sugarcrmclient.plugin.objects.SugarCrmRecordImpl;
 
 public class SolrPluginTest {
 	private static final String RECORD = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
 			+ "<rdf:RDF xmlns:dcterms=\"http://purl.org/dc/terms/\""
 			+ "xmlns:edm=\"http://www.europeana.eu/schemas/edm/\""
 			+ "xmlns:enrichment=\"http://www.europeana.eu/schemas/edm/enrichment/\""
 			+ "xmlns:owl=\"http://www.w3.org/2002/07/owl#\""
 			+ "xmlns:wgs84=\"http://www.w3.org/2003/01/geo/wgs84_pos#\""
 			+ "xmlns:skos=\"http://www.w3.org/2004/02/skos/core\""
 			+ "xmlns:oai=\"http://www.openarchives.org/OAI/2.0/\""
 			+ "xmlns:ore=\"http://www.openarchives.org/ore/terms/\""
 			+ "xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\""
 			+ "xmlns:dc=\"http://purl.org/dc/elements/1.1/\""
 			+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
 			+ "xsi:schemaLocation=\"http://www.w3.org/1999/02/22-rdf-syntax-ns# EDM.xsd\">"
 			+ "<edm:ProvidedCHO rdf:about=\"#GNM:728331\">"
 			+ "<dc:identifier>#GNM:728331</dc:identifier>"
 			+ "<dc:coverage></dc:coverage>"
 			+ "<dc:description xml:lang=\"de\">Stoßmechanik.</dc:description>"
 			+ "<dc:title xml:lang=\"de\">Modell einer primitiven Stoßmechanik</dc:title>"
 			+ "<edm:type>IMAGE</edm:type>"
 			+ "</edm:ProvidedCHO>"
 			+ "<edm:WebResource rdf:about=\"http://www.mimo-db.eu/media/GNM/IMAGE/MINe298_H_1304604690701_2.jpg\">"
 			+ "<edm:rights rdf:resource=\"http://creativecommons.org/licenses/by-nc-sa/3.0/\"></edm:rights>"
 			+ "</edm:WebResource>"
 			+ "<ore:Aggregation rdf:about=\"http://www.mimo-db.eu/GNM/728331\">"
 			+ "<edm:aggregatedCHO rdf:resource=\"#GNM:728331\"></edm:aggregatedCHO>"
 			+ "<edm:hasView rdf:resource=\"http://www.mimo-db.eu/media/GNM/IMAGE/MINe298_H_1304604690701_2.jpg\"></edm:hasView>"
 			+ "<edm:dataProvider>Germanisches Nationalmuseum</edm:dataProvider>"
 			+ "<edm:provider>MIMO - Musical Instrument Museums Online</edm:provider>"
 			+ "<edm:rights rdf:resource=\"http://creativecommons.org/licenses/by-nc-sa/3.0/\"></edm:rights>"
 			+ "<edm:isShownBy rdf:resource=\"http://www.mimo-db.eu/media/GNM/IMAGE/MINe298_H_1304604690701_2.jpg\"></edm:isShownBy>"
 			+ "<edm:isShownAt rdf:resource=\"http://www.mimo-db.eu/GNM/728331\"></edm:isShownAt>"
 			+ "<edm:object rdf:resource=\"http://www.mimo-db.eu/media/GNM/IMAGE/MINe298_H_1304604690701_2.jpg\"></edm:object>"
 			+ "</ore:Aggregation>" + "</rdf:RDF>";
 
 	private final String record = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><record><first><item><name>previews_only_on_europeana_por_c</name><type>0</type></item></first></record>";
 	
 	
 	private SolrWorkflowPlugin plugin;
 
 	@Test
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	public void store() {
 		
 		ActiveExecution context = mock(ActiveExecution.class);
 		Collection collection = new CollectionBean("09431",
 				new ProviderBean<String>("test_provider"));
 		
 		collection.putValue(ControlledVocabularyProxy.SUGARCRMID, "09431");
		collection.setMnemonic("12345");
		
 		MetaDataRecord mdr = new MetaDataRecordBean<String>("09431", collection);
 		mdr.addValue(EuropeanaModelRegistry.EDMRECORD, RECORD);
 		SugarCrmService service = mock(SugarCRMServiceImpl.class);
 		plugin = new SolrWorkflowPlugin();
 		plugin.setSolrUrl("http://127.0.0.1:8282/");
 		plugin.setSolrCore("apache-solr-3.5.0");
 		plugin.setSugarCrmService(service);
 		plugin.setMongoHost("127.0.0.1");
 		plugin.setMongoPort(27017);
 		plugin.setMongoDB("europeana_test");
 		plugin.setVocabularyDB("vocabulary");
 		plugin.setRepository("/export/repository");
 		plugin.setCollections("collections");
 		plugin.setEuropeanaID("EuropeanaID");
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
 
 		Assert.assertTrue(plugin.processRecord(mdr, context));
 
 		plugin.completed(context);
 		Assert.assertEquals(1, SolrWorkflowPlugin.getRecords());
 		plugin.shutdown();
 
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
