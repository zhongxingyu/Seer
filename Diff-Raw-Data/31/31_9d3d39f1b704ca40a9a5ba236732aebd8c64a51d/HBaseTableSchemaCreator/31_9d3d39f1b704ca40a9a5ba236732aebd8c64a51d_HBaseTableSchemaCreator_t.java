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
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.hadoop.hbase.HColumnDescriptor;
 import org.apache.hadoop.hbase.HConstants;
 import org.apache.hadoop.hbase.io.encoding.DataBlockEncoding;
 import org.apache.hadoop.hbase.io.hfile.Compression;
 import org.apache.hadoop.hbase.io.hfile.Compression.Algorithm;
 import org.apache.hadoop.hbase.regionserver.StoreFile.BloomType;
 import org.apache.log4j.Logger;
 
 import com.bizosys.hsearch.hbase.HDML;
 import com.bizosys.hsearch.util.HSearchConfig;
 import com.bizosys.hsearch.util.conf.Configuration;
 
 public final class HBaseTableSchemaCreator {
 	
 	private static HBaseTableSchemaCreator instance = null;
 	public static Logger l = Logger.getLogger(HBaseTableSchemaCreator.class.getName());
 	
 	Configuration config = HSearchConfig.getInstance().getConfiguration(); 
 	
 	public Algorithm compression = Compression.Algorithm.NONE;
 	public int partitionBlockSize = config.getInt("partition.block.size", 13035596);	
 	public int partitionRepMode = HConstants.REPLICATION_SCOPE_GLOBAL;
 	public  DataBlockEncoding dataBlockEncoding = DataBlockEncoding.NONE;
 	public BloomType bloomType = BloomType.NONE;
 	public boolean inMemory = false;
 	public boolean blockCacheEnabled = config.getBoolean("block.cache.enabled", true);;
 	
 	public static final HBaseTableSchemaCreator getInstance() {
 		if ( null != instance) return instance;
 		synchronized (HBaseTableSchemaCreator.class) {
 			if ( null != instance) return instance;
 			instance = new HBaseTableSchemaCreator();
 		}
 		return instance;
 	}
 	
 	/**
 	 * Default constructor
 	 *
 	 */
 	public HBaseTableSchemaCreator(){
 	}
 	
 	/**
 	 * Checks and Creates all necessary tables required for HSearch index.
 	 */
 	public final boolean init(String tableName) {
 
 		try {
 			
 			List<HColumnDescriptor> colFamilies = new ArrayList<HColumnDescriptor>();
 			
 			HBaseTableSchemaDefn def = HBaseTableSchemaDefn.getInstance(tableName);
 			
 			System.out.println("Compression : " + this.compression.getName());
 			System.out.println("Partition Block Size : " + this.partitionBlockSize);
 			System.out.println("Partition Rep Mode : " + this.partitionRepMode);
 			System.out.println("Partition Block Size : " + this.partitionBlockSize);
 			System.out.println("Partition Block Encoding : " + this.dataBlockEncoding.name());
 			System.out.println("Bloom Type : " + this.bloomType.name());
 			System.out.println("In Memory Table: " + this.inMemory);
 			System.out.println("Block Caching: " + this.blockCacheEnabled);
 			
 			for (String familyName : def.columnPartions.keySet()) {
 				
 				//Partitioned
 				List<String> partitionNames = def.columnPartions.get(familyName).getPartitionNames();
 				for (String partition : partitionNames) {
 					HColumnDescriptor rangeCols = new HColumnDescriptor( (familyName + "_" + partition ).getBytes());
 					configColumn(rangeCols);
 					colFamilies.add(rangeCols);
 				}
 				
 				//No Partition
 				if ( partitionNames.size() == 0 ) {
 					HColumnDescriptor rangeCols = new HColumnDescriptor( familyName.getBytes());
 					configColumn(rangeCols);
 					colFamilies.add(rangeCols);
 				}
 			}
 			
			HDML.create(tableName, colFamilies);
 			return true;
 			
 		} catch (Exception sf) {
 			sf.printStackTrace(System.err);
 			l.fatal(sf);
 			return false;
 		} 
 	}
 
 	/**
 	 * Compression method to HBase compression code.
 	 * @param methodName 
 	 * @return
 	 */
 	public static final String resolveCompression(final String methodName) {
 		String compClazz =  Compression.Algorithm.GZ.getName();
 		if ("gz".equals(methodName)) {
 			compClazz = Compression.Algorithm.GZ.getName();
 		} else if ("lzo".equals(methodName)) {
 			compClazz = Compression.Algorithm.LZO.getName();
 		} else if ("none".equals(methodName)) {
 			compClazz = Compression.Algorithm.NONE.getName();
 		}
 		return compClazz;
 	}
 	
 	public final void configColumn(final HColumnDescriptor col) {
 		col.setMinVersions(1);
 		col.setMaxVersions(1);
 		col.setKeepDeletedCells(false);
 		col.setCompressionType(compression);
 		col.setEncodeOnDisk(false);
 		col.setDataBlockEncoding(dataBlockEncoding);
 		col.setInMemory(inMemory);
 		col.setBlockCacheEnabled(blockCacheEnabled);
 		col.setBlocksize(partitionBlockSize);
 		col.setTimeToLive(HConstants.FOREVER);
 		col.setBloomFilterType(bloomType);
 		col.setScope(partitionRepMode);
 	}
 }
