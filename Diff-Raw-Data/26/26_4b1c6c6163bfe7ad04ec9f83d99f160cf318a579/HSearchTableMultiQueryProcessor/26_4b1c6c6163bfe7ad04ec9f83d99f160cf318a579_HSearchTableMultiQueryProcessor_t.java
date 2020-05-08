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
 package com.bizosys.hsearch.treetable.client;
 
 import java.io.IOException;
 import java.util.BitSet;
 import java.util.HashSet;
 import java.util.Map;
 
 import com.bizosys.hsearch.federate.BitSetOrSet;
 import com.bizosys.hsearch.federate.FederatedSearch;
 import com.bizosys.hsearch.hbase.HbaseLog;
 
 public abstract class HSearchTableMultiQueryProcessor implements IHSearchTableMultiQueryProcessor {
 
 	public static boolean DEBUG_ENABLED = HbaseLog.l.isDebugEnabled();
 	
 	public final static BitSetOrSet EMPTY_BITSET = new BitSetOrSet();
 	static {
 		EMPTY_BITSET.setDocumentIds(new HashSet<Object>(0));
 		EMPTY_BITSET.setDocumentSequences(new BitSet());
 	}
 	
 	public abstract IHSearchTableCombiner getCombiner();
 	
 	public HSearchTableMultiQueryProcessor() {
 	}
 	
 	@Override
 	public FederatedSearch getProcessor() { 
		return build();
 	}
 	
 	private final FederatedSearch build() {
 
 		return new FederatedSearch() {
 			
 			@Override
 			public BitSetOrSet populate(
 					String type, String multiQueryPartId, String aStmtOrValue, Map<String, Object> stmtParams) throws IOException {
 
 				if ( DEBUG_ENABLED ) L.getInstance().logDebug(  "HSearchTableMultiQuery.populate ENTER.");
 				long startTime = System.currentTimeMillis();
 				try {
 					IHSearchTableCombiner combiner = getCombiner();
 					HSearchProcessingInstruction outputType = (HSearchProcessingInstruction) stmtParams.get(HSearchTableMultiQueryExecutor.OUTPUT_TYPE);
 					
 					if ( DEBUG_ENABLED ) {
 						startTime = System.currentTimeMillis();
 						HbaseLog.l.debug("Concurrent derer ENTER");
 					}
 					combiner.concurrentDeser(aStmtOrValue, outputType, stmtParams, type);
 
 					if ( DEBUG_ENABLED ) {
 						HbaseLog.l.debug("Concurrent deser EXIT in ms > " + ( System.currentTimeMillis() - startTime ) );
 					}
 
 					IHSearchPlugin plugin = (IHSearchPlugin) stmtParams.get(HSearchTableMultiQueryExecutor.PLUGIN);
 					BitSetOrSet keys = plugin.getUniqueMatchingDocumentIds();
 					
 					if ( null == keys) return EMPTY_BITSET;
 					if ( keys.isEmpty()) return EMPTY_BITSET;
 					
 					if ( DEBUG_ENABLED ) {
 						startTime = System.currentTimeMillis();
 					}
 	
 					if ( DEBUG_ENABLED ) {
 						HbaseLog.l.debug("IRowId Collection EXIT in ms > " + ( System.currentTimeMillis() - startTime ) );
 					}
 					return keys;
 					
 				} catch (Exception ex) {
 					throw new IOException(ex);
 				} finally {
 					if ( DEBUG_ENABLED ) {
 						long endTime = System.currentTimeMillis();
 						L.getInstance().logDebug( Thread.currentThread().getName() + "> " + "populate EXIT ms :" + (endTime - startTime));
 					}
 				}
 			}
 		};
 	}
 }
