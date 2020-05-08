 package org.araqne.logdb.query.engine;
 
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.concurrent.atomic.AtomicLong;
 
 import org.apache.felix.ipojo.annotations.Component;
 import org.apache.felix.ipojo.annotations.Provides;
 import org.apache.felix.ipojo.annotations.Requires;
 import org.apache.felix.ipojo.annotations.Validate;
 import org.araqne.logdb.Query;
 import org.araqne.logdb.QueryResult;
 import org.araqne.logdb.QueryResultConfig;
 import org.araqne.logdb.QueryResultFactory;
 import org.araqne.logdb.QueryResultStorage;
 import org.araqne.logstorage.file.LogFileReader;
 import org.araqne.logstorage.file.LogFileReaderV2;
 import org.araqne.logstorage.file.LogFileWriter;
 import org.araqne.logstorage.file.LogFileWriterV2;
 import org.araqne.storage.api.FilePath;
 import org.araqne.storage.api.StorageManager;
 
 @Component(name = "logdb-query-result-factory")
 @Provides
 public class QueryResultFactoryImpl implements QueryResultFactory {
 	@Requires
 	private StorageManager storageManager;
 	
 	private CopyOnWriteArrayList<QueryResultStorage> storages = new CopyOnWriteArrayList<QueryResultStorage>();
 
 	public QueryResultFactoryImpl() {
 	}
 	
 	// for test
 	public QueryResultFactoryImpl(StorageManager storageManager) {
 		this.storageManager = storageManager;
 	}
 	
 	@Validate
 	public void start() {
		FilePath BASE_DIR = storageManager.resolveFilePath(System.getProperty("araqne.data.dir", "")).newFilePath("araqne-logdb/query/");
 
 		QueryResultStorageV2 embedded = new QueryResultStorageV2(BASE_DIR);
 		storages.add(embedded);
 	}
 
 	@Override
 	public QueryResult createResult(QueryResultConfig config) throws IOException {
 		QueryResultStorage lastStorage = null;
 		for (QueryResultStorage storage : storages)
 			lastStorage = storage;
 
 		return new QueryResultImpl(config, lastStorage);
 	}
 
 	@Override
 	public void registerStorage(QueryResultStorage storage) {
 		storages.add(storage);
 	}
 
 	@Override
 	public void unregisterStorage(QueryResultStorage storage) {
 		storages.remove(storage);
 	}
 
 	private static class QueryResultStorageV2 implements QueryResultStorage {
 		private final FilePath BASE_DIR;
 		
 		public QueryResultStorageV2(FilePath BASE_DIR) {
 			this.BASE_DIR = BASE_DIR;
 		}
 
 		@Override
 		public String getName() {
 			return "v2";
 		}
 
 		@Override
 		public LogFileWriter createWriter(QueryResultConfig config) throws IOException {
 			BASE_DIR.mkdirs();
 
 			String filePrefix = getFileNamePrefix(config);
 			FilePath indexPath = BASE_DIR.newFilePath(filePrefix+".idx");
 			FilePath dataPath = BASE_DIR.newFilePath(filePrefix+".dat");
 			return new LogFileWriterV2(indexPath, dataPath, 1024 * 1024, 1, null, null, null, new AtomicLong(-1));
 		}
 
 		@Override
 		public LogFileReader createReader(QueryResultConfig config) throws IOException {
 			String filePrefix = getFileNamePrefix(config);
 			FilePath indexPath = BASE_DIR.newFilePath(filePrefix+".idx");
 			FilePath dataPath = BASE_DIR.newFilePath(filePrefix+".dat");
 			return new LogFileReaderV2(null, indexPath, dataPath);
 		}
 
 		private String getFileNamePrefix(QueryResultConfig config) {
 			Query query = config.getQuery();
 			String tag = config.getTag();
 
 			SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");
 			if (tag == null || tag.isEmpty())
 				tag = query.getId() + "_" + df.format(config.getCreated());
 
 			return "result_v2_" + tag;
 		}
 
 	}
 }
