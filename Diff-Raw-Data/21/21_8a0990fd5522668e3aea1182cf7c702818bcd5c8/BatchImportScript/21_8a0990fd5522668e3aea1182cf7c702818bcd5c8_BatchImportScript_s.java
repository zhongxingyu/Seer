 package org.biosemantics.conceptstore.dataimport;
 
 import java.util.Map;
 
 import org.apache.commons.dbcp.BasicDataSource;
 import org.neo4j.helpers.collection.MapUtil;
 import org.neo4j.unsafe.batchinsert.BatchInserter;
 import org.neo4j.unsafe.batchinsert.BatchInserterIndex;
 import org.neo4j.unsafe.batchinsert.BatchInserterIndexProvider;
 import org.neo4j.unsafe.batchinsert.BatchInserters;
 import org.neo4j.unsafe.batchinsert.LuceneBatchInserterIndexProvider;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class BatchImportScript {
 
 	public void init(String dbFolder, Map<String, String> graphConfig) {
 		batchInserter = BatchInserters.inserter(dbFolder, graphConfig);
 		indexProvider = new LuceneBatchInserterIndexProvider(batchInserter);
 		labelIndex = indexProvider.nodeIndex("Label", MapUtil.stringMap("type", "exact"));
 		labelIndex.setCacheCapacity("text", INDEX_CACHE);
 		notationIndex = indexProvider.nodeIndex("Notation", MapUtil.stringMap("type", "exact"));
 		notationIndex.setCacheCapacity("code", INDEX_CACHE);
 		conceptIndex = indexProvider.nodeIndex("Concept", MapUtil.stringMap("type", "exact"));
 		conceptIndex.setCacheCapacity("type", INDEX_CACHE);
 		relationshipTypeIndex = indexProvider.relationshipIndex("Rlsp", MapUtil.stringMap("type", "exact"));
 		relationshipTypeIndex.setCacheCapacity("type", INDEX_CACHE);
 		umlsDataSource = new BasicDataSource();
 		umlsDataSource.setDriverClassName(configReader.getValue("jdbc.driverClassName"));
 		umlsDataSource.setUrl(configReader.getValue("jdbc.url"));
 		umlsDataSource.setUsername(configReader.getValue("jdbc.username"));
 		umlsDataSource.setPassword(configReader.getValue("jdbc.password"));
 
 	}
 
 	public void batchImport() throws Exception {
 		DataImport dataImport = new UmlsDataImport(batchInserter, labelIndex, notationIndex, conceptIndex,
 				relationshipTypeIndex, umlsDataSource);
 		dataImport.importData();
 	}
 
 	public void destroy() {
 		logger.info("shutdown invoked");
 		labelIndex.flush();
 		notationIndex.flush();
 		conceptIndex.flush();
 		indexProvider.shutdown();
 		batchInserter.shutdown();
 		logger.info("shutdown complete");
 	}
 
 	public static void main(String[] args) throws Exception {
 		configReader.init();
 		BatchImportScript batchImportScript = new BatchImportScript();
 		batchImportScript.init(configReader.getValue("graph.db"), getGraphConfig());
 		batchImportScript.batchImport();
 		batchImportScript.destroy();
 
 	}
 
 	private static final Map<String, String> getGraphConfig() {
 		return MapUtil.stringMap("neostore.propertystore.db.index.keys.mapped_memory", "5M",
 				"neostore.propertystore.db.index.mapped_memory", "5M", "neostore.nodestore.db.mapped_memory", "200M",
 				"neostore.relationshipstore.db.mapped_memory", "1000M", "neostore.propertystore.db.mapped_memory",
 				"1000M", "neostore.propertystore.db.strings.mapped_memory", "200M");
 	}
 
 	private static final int INDEX_CACHE = 100000;
 	private BatchInserter batchInserter;
 	private BatchInserterIndexProvider indexProvider;
 	private BatchInserterIndex labelIndex;
 	private BatchInserterIndex notationIndex;
 	private BatchInserterIndex conceptIndex;
 	private BatchInserterIndex relationshipTypeIndex;
 	private BasicDataSource umlsDataSource;
 	private static final DataImportConfigReader configReader = new DataImportConfigReader();
 	private static final Logger logger = LoggerFactory.getLogger(BatchImportScript.class);
 
 }
