 /*
  * Copyright 2013 Eediom Inc.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.araqne.logdb.metadata;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.felix.ipojo.annotations.Component;
 import org.apache.felix.ipojo.annotations.Invalidate;
 import org.apache.felix.ipojo.annotations.Requires;
 import org.apache.felix.ipojo.annotations.Validate;
 import org.araqne.log.api.FieldDefinition;
 import org.araqne.logdb.AccountService;
 import org.araqne.logdb.FieldOrdering;
 import org.araqne.logdb.Row;
 import org.araqne.logdb.QueryContext;
 import org.araqne.logdb.MetadataCallback;
 import org.araqne.logdb.MetadataProvider;
 import org.araqne.logdb.MetadataService;
 import org.araqne.logdb.Privilege;
 import org.araqne.logstorage.*;
 
 @Component(name = "logdb-table-metadata")
 public class TableMetadataProvider implements MetadataProvider, FieldOrdering {
 
 	@Requires
 	private LogTableRegistry tableRegistry;
 
 	@Requires
 	private AccountService accountService;
 
 	@Requires
 	private LogFileServiceRegistry lfsRegistry;
 	
 	@Requires
 	private LogStorage storage;
 
 	@Requires
 	private MetadataService metadataService;
 	
 
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
 	public String getType() {
 		return "tables";
 	}
 
 	@Override
 	public void verify(QueryContext context, String queryString) {
 	}
 
 	@Override
 	public void query(QueryContext context, String queryString, MetadataCallback callback) {
 		if (context.getSession().isAdmin()) {
 			for (String tableName : tableRegistry.getTableNames()) {
 				writeTableInfo(tableName, callback);
 			}
 		} else {
 			List<Privilege> privileges = accountService.getPrivileges(context.getSession(), context.getSession().getLoginName());
 			for (Privilege p : privileges) {
 				if (p.getPermissions().size() > 0 && tableRegistry.exists(p.getTableName())) {
 					writeTableInfo(p.getTableName(), callback);
 				}
 			}
 		}
 	}
 
 	private void writeTableInfo(String tableName, MetadataCallback callback) {
 		Map<String, Object> m = new HashMap<String, Object>();
 		m.put("table", tableName);
 		TableSchema s = tableRegistry.getTableSchema(tableName);
 
 		// primary storage
 		StorageConfig primaryStorage = s.getPrimaryStorage();
 		LogFileService lfs = lfsRegistry.getLogFileService(primaryStorage.getType());
 		for (TableConfigSpec spec: lfs.getConfigSpecs()) {
 			String config = null;
 			TableConfig c = primaryStorage.getConfig(spec.getKey());
 			if (c != null && c.getValues().size() > 1)
 				config = c.getValues().toString();
 			else if (c != null)
 				config = c.getValue();
 			m.put(spec.getKey(), config);
 		}
 
 		// replica storage
 		StorageConfig replicaStorage = s.getReplicaStorage();
 		if (replicaStorage != null) {
 			for (TableConfigSpec spec : lfs.getReplicaConfigSpecs()) {
 				TableConfig c = replicaStorage.getConfig(spec.getKey());
 				String config = null;
 				if (c != null && c.getValues().size() > 1)
 					config = c.getValues().toString();
 				else if (c != null)
 					config = c.getValue();
 
 				m.put(spec.getKey(), config);
 			}
 		}
 
 		// field definitions
 		List<FieldDefinition> fields = s.getFieldDefinitions();
 		if (fields != null) {
 			for (FieldDefinition field : fields) {
 				String line = null;
 				if (field.getLength() > 0)
 					line = field.getName() + "\t" + field.getType() + "(" + field.getLength() + ")";
 				line = field.getName() + "\t" + field.getType();
 
 				m.put("fields", line);
 			}
 		}
 		
 		m.put("metadata", s.getMetadata());
 
 		// retention pollicy
 		LogRetentionPolicy retentionPolicy = storage.getRetentionPolicy(tableName);
 		String retention = null;
 		if (retentionPolicy != null && retentionPolicy.getRetentionDays() > 0)
 			retention = retentionPolicy.getRetentionDays() + "days";
 
 		m.put("retention_policy", retention);
 
 		m.put("data_path", storage.getTableDirectory(tableName).getAbsolutePath());
 		
 		LockStatus status = storage.lockStatus(new LockKey("script", tableName, null));
 		if (status.isLocked()) {
 			m.put("lock_owner", status.getOwner());
			m.put("lock_purpose", status.getPurposes().toArray(new String[0]));
 			m.put("lock_reentcnt", status.getReentrantCount());
 		} else {
 			m.put("lock_owner", null);
 		}
 		
 		callback.onPush(new Row(m));
 	}
 
 	@Override
 	public List<String> getFieldOrder() {
 		return Arrays.asList("table", "compression", "crypto", "metadata", "replication_mode", 
 				"replication_table", "lock_owner", "lock_purpose", "lock_reentcnt",
 				"retention_policy", "data_path");
 	}
 
 }
