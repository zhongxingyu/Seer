 /*
  * Copyright 2015 Eediom Inc. All rights reserved.
  */
 package org.araqne.logdb.metadata;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.felix.ipojo.annotations.Component;
 import org.apache.felix.ipojo.annotations.Invalidate;
 import org.apache.felix.ipojo.annotations.Requires;
 import org.apache.felix.ipojo.annotations.Validate;
 import org.araqne.logdb.MetadataCallback;
 import org.araqne.logdb.MetadataProvider;
 import org.araqne.logdb.MetadataService;
 import org.araqne.logdb.QueryContext;
 import org.araqne.logdb.Row;
 import org.araqne.storage.api.RCDirectBufferManager;
 
 @Component(name = "logdb-memory-metadata")
 public class MemoryMetadataProvider implements MetadataProvider {
 	@Requires
 	private MetadataService metadataService;
 	
 	@Requires
 	private RCDirectBufferManager manager;
 
 	@Override
 	public String getType() {
 		return "memory";
 	}
 
 	@Validate
 	public void start() {
 		metadataService.addProvider(this);
 	}
 
 	@Invalidate
 	public void stop() {
 		if (metadataService != null)
 			metadataService.removeProvider(this);
 	}
 
 	@Override
 	public void verify(QueryContext context, String queryString) {
 	}
 
 	@Override
 	public void query(QueryContext context, String queryString, MetadataCallback callback) {
 		Map<String, Object> heap = new HashMap<String, Object>();
 		Runtime runtime = Runtime.getRuntime();
 		heap.put("type", "heap");
 		heap.put("free", runtime.freeMemory());
 		heap.put("total", runtime.totalMemory());
 		callback.onPush(new Row(heap));
 		
 		Map<String, Object> rc = new HashMap<String, Object>();
		rc.put("type", "offheap");
 		rc.put("total_capacity", manager.getTotalCapacity());
 		rc.put("object_count", manager.getObjectCount());
 		callback.onPush(new Row(rc));
 	}
 
 }
