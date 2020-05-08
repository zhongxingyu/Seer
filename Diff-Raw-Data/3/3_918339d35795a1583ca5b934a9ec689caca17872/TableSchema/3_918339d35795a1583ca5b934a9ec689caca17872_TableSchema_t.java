 /*
  * Copyright 2014 Eediom Inc.
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
 package org.araqne.logstorage;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.araqne.api.CollectionTypeHint;
 import org.araqne.api.FieldOption;
 import org.araqne.confdb.CollectionName;
 import org.araqne.log.api.FieldDefinition;
 
 @CollectionName("table")
 public class TableSchema {
 	/**
 	 * unique table name in a node
 	 */
 	@FieldOption(nullable = false)
 	private String name;
 
 	/**
 	 * table id for path generation (name may contains character which is not
 	 * allowed at target storage filesystem)
 	 */
 	@FieldOption(nullable = false)
 	private int id;
 
 	private StorageConfig primaryStorage;
 
 	private StorageConfig replicaStorage;
 
 	@CollectionTypeHint(StorageConfig.class)
 	private List<StorageConfig> secondaryStorages = new ArrayList<StorageConfig>();
 
 	/**
 	 * Table may contains other field which is not specified here. Query
 	 * designer can use these field definitions for search dialog rendering,
 	 * lookup input/output mapping, etc.
 	 */
 	@CollectionTypeHint(FieldDefinition.class)
 	private List<FieldDefinition> fieldDefinitions;
 
 	private Map<String, String> metadata = new HashMap<String, String>();
 
 	public TableSchema() {
 	}
 
 	public TableSchema(String name, StorageConfig primaryConfig) {
 		this.name = name;
 		this.primaryStorage = primaryConfig;
 	}
 
 	public TableSchema clone() {
 		TableSchema c = new TableSchema();
 		c.setName(name);
 		c.setId(id);
 		c.setPrimaryStorage(primaryStorage.clone());
 
 		if (replicaStorage != null)
 			c.setReplicaStorage(replicaStorage.clone());
 
 		List<StorageConfig> l = new ArrayList<StorageConfig>();
 		for (StorageConfig s : secondaryStorages)
 			l.add(s.clone());
 
 		c.setSecondaryStorages(l);
 
 		c.setFieldDefinitions(cloneFieldDefinitions(fieldDefinitions));
		if (metadata != null)
			c.setMetadata(new HashMap<String, String>(metadata));
 
 		return c;
 	}
 
 	private List<FieldDefinition> cloneFieldDefinitions(List<FieldDefinition> l) {
 		if (l == null)
 			return null;
 
 		List<FieldDefinition> cloned = new ArrayList<FieldDefinition>();
 		for (FieldDefinition d : l)
 			cloned.add(FieldDefinition.parse(d.toString()));
 		return cloned;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public int getId() {
 		return id;
 	}
 
 	public void setId(int id) {
 		this.id = id;
 	}
 
 	public StorageConfig getPrimaryStorage() {
 		return primaryStorage;
 	}
 
 	public void setPrimaryStorage(StorageConfig primaryStorage) {
 		this.primaryStorage = primaryStorage;
 	}
 
 	public StorageConfig getReplicaStorage() {
 		return replicaStorage;
 	}
 
 	public void setReplicaStorage(StorageConfig replicaStorage) {
 		this.replicaStorage = replicaStorage;
 	}
 
 	public List<StorageConfig> getSecondaryStorages() {
 		return secondaryStorages;
 	}
 
 	public void setSecondaryStorages(List<StorageConfig> secondaryStorages) {
 		this.secondaryStorages = secondaryStorages;
 	}
 
 	public List<FieldDefinition> getFieldDefinitions() {
 		if (fieldDefinitions == null)
 			return null;
 		return new ArrayList<FieldDefinition>(fieldDefinitions);
 	}
 
 	/**
 	 * update table field definitions
 	 * 
 	 * @param tableName
 	 *            existing table name
 	 * @param fields
 	 *            field definitions or null
 	 * @since 2.5.1
 	 */
 	public void setFieldDefinitions(List<FieldDefinition> fieldDefinitions) {
 		this.fieldDefinitions = fieldDefinitions;
 	}
 
 	public Map<String, String> getMetadata() {
 		return metadata;
 	}
 
 	public void setMetadata(Map<String, String> metadata) {
 		this.metadata = metadata;
 	}
 }
