 /*
 * Copyright 2010 Bizosys Technologies Limited
 *
 * Licensed to the Bizosys Technologies Limited (Bizosys) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Bizosys licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 package com.bizosys.hsearch.treetable.storage;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import com.bizosys.hsearch.treetable.client.partition.IPartition;
 
 public final class HBaseTableSchemaDefn {
 	
 	private static Map<String, HBaseTableSchemaDefn> repositories = new HashMap<String, HBaseTableSchemaDefn>();
 	public static HBaseTableSchemaDefn getInstance(String tableName) {
 		if ( repositories.containsKey(tableName)) return repositories.get(tableName);
 		else {
 			synchronized (HBaseTableSchemaDefn.class.getName()) {
 				if ( repositories.containsKey(tableName)) return repositories.get(tableName);
 				repositories.put(tableName, new HBaseTableSchemaDefn());
 			}
 		}
 		return repositories.get(tableName);
 	}
 	
 	private HBaseTableSchemaDefn() {
 		
 	}
 
 	private HBaseTableSchemaDefn(String tableName) {
 		this.tableName = tableName;
 	}
 
	private String tableName = "htable";
 	
 	//FamilyName_partition is how the column families are created.
 	public Map<String, IPartition> columnPartions = new HashMap<String, IPartition>();
 
 	public final static char getColumnName() {
 		return getColumnName(1);
 	}
 	
 	public final static char getColumnName(int token) {
 		String tokenStr  = new Integer(token).toString();
 		return tokenStr.charAt(tokenStr.length() - 1);
 	}	
 	
	public String getTableName() {
		return this.tableName;
	}
	
 
 }
