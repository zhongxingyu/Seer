 package org.commacq;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import org.springframework.jmx.export.annotation.ManagedAttribute;
 import org.springframework.jmx.export.annotation.ManagedOperation;
 import org.springframework.jmx.export.annotation.ManagedResource;
 
 /**
  * Defines a set of CsvDataSources. Sources within the same layer
  * can be updated together (transactionally). An example of the use
  * of layers is creating a cache CsvDataSource for each "real"
  * data source in a layer.
  */
 @ManagedResource
 public class CsvDataSourceLayer {
 	
 	private ThreadLocal<CsvLineCallbackSingleImpl> csvLineCallbackSingleImplLocal = new ThreadLocal<CsvLineCallbackSingleImpl>() {
 		protected CsvLineCallbackSingleImpl initialValue() {
 			return new CsvLineCallbackSingleImpl();
 		};
 	};
 
 	private final SortedSet<String> entityIds = new TreeSet<String>();
 	//Unmodifiable wrapper to give to clients
 	private final SortedSet<String> entityIdsUnmodifiable = Collections.unmodifiableSortedSet(entityIds);
 	
 	private final Map<String, CsvDataSource> csvDataSourceMap = new HashMap<String, CsvDataSource>();
 	//Unmodifiable wrapper to give to clients
 	private final Map<String, CsvDataSource> csvDataSourceMapUnmodifiable = Collections.unmodifiableMap(csvDataSourceMap);	
 	
 	public CsvDataSourceLayer(Collection<CsvDataSource> csvDataSources) {
 		for(CsvDataSource source : csvDataSources) {
 			boolean newEntry = entityIds.add(source.getEntityId());
 			if(!newEntry) {
 				throw new RuntimeException("Duplicate entityId in layer: " + source.getEntityId());
 			}
 			csvDataSourceMap.put(source.getEntityId(), source);
 		}
 	}
 	
 	@ManagedAttribute
 	public SortedSet<String> getEntityIds() {
 		return entityIdsUnmodifiable;
 	}
 	
 	@ManagedOperation
 	public String getCsvEntry(String entityId, String id) {
 		CsvLineCallbackSingleImpl callback = csvLineCallbackSingleImplLocal.get();  
 	    getCsvDataSource(entityId).getCsvLine(id, callback);
 	    return callback.getCsvLineAndClear().getCsvLine();
 	}
 	
 	@ManagedOperation
 	//TODO Move this into an update manager where the updates are synchronised
 	public String pokeCsvEntry(String entityId, String id) {
 		CsvDataSource source = getCsvDataSource(entityId);
 		if(!(source instanceof CsvUpdatableDataSource)) {
 			throw new RuntimeException("Not an updatable data source: " + entityId);
 		}
 		
 		CsvUpdatableDataSource updatableDataSource = (CsvUpdatableDataSource)source;
 		try {
			updatableDataSource.startUpdateBlock(source.getColumnNamesCsv());
 			updatableDataSource.updateUntrusted(id);
			updatableDataSource.finishUpdateBlock();
 		} catch (CsvUpdateBlockException ex) {
 			return null;
 		}
 		
 		return getCsvEntry(entityId, id);
 	}
 	
 	
 	public CsvDataSource getCsvDataSource(String entityId) {
 		return csvDataSourceMap.get(entityId);
 	}
 	
 	public Map<String, CsvDataSource> getMap() {
 		return csvDataSourceMapUnmodifiable;
 	}
 	
 }
