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
 
 import java.util.Map;
 
 import com.bizosys.hsearch.federate.BitSetOrSet;
 import com.bizosys.hsearch.federate.FederatedSearch;
 import com.bizosys.hsearch.federate.QueryPart;
 import com.bizosys.hsearch.hbase.HbaseLog;
 
 /**
  * Concurrent: 
  * 	Process each filter in threads - De-serialization is parallel
  *  Process each parts in threads
  *  
  *  Final Joiner. 
  * @author abinash
  */
 public final class HSearchTableMultiQueryExecutor {
 
 	public static boolean DEBUG_ENABLED = HbaseLog.l.isDebugEnabled();
 	
 	public static final String OUTPUT_TYPE = "output";
 	public static final String PLUGIN = "plugin";
 	public static final String TABLE_PARTS = "ranges";
 	
 	IHSearchTableMultiQueryProcessor processor = null;
 	FederatedSearch ff = null;
 	
 	public HSearchTableMultiQueryExecutor(final IHSearchTableMultiQueryProcessor processor) {
 		this.processor = processor;
 	}
 	
 	public BitSetOrSet execute (
 			final Map<String, HSearchTableParts> tableParts, final String multiQueryStmt, 
 			final Map<String,QueryPart> multiQueryParts, final HSearchProcessingInstruction resultType) throws Exception {
 		
 		long start = 0L;
 		
 		if ( null == tableParts) {
 			String msg = "Warning : TableParts is not found. Input bytes are not set.";
 			HbaseLog.l.warn(msg);
 			return null;
 		}
 		
 		for (String queryId : multiQueryParts.keySet()) {
 			HSearchTableParts part = tableParts.get(queryId); 
 			
 			if ( null == part) {
 				String msg = ("Warning : Null table part bytes for " + queryId + "\n" + 
 						queryId + " is not in the supplied set :" + tableParts.keySet().toString());
 				HbaseLog.l.error(msg);
 				continue;
 			}
 			
 			multiQueryParts.get(queryId).setParam(HSearchTableMultiQueryExecutor.TABLE_PARTS, part);
 			multiQueryParts.get(queryId).setParam(HSearchTableMultiQueryExecutor.OUTPUT_TYPE, resultType);
 		}
 		
 		if ( DEBUG_ENABLED ) {
 			HbaseLog.l.debug("HSearchTestMultiQuery : getProcessor ENTER ");
 		}
 		
 		if ( null == ff) {
 			ff = processor.getProcessor();
 			ff.initialize(multiQueryStmt);
 		} 
 		
 		if ( DEBUG_ENABLED ) {
 			HbaseLog.l.debug("HSearchTestMultiQuery : ff.execute ENTER ");
 			start = System.currentTimeMillis();
 		}
 		
		BitSetOrSet matchingIds = ff.execute(multiQueryParts);
 
 		if  ( DEBUG_ENABLED ) {
 			long end = System.currentTimeMillis();
 			
 			int size = matchingIds.size();
 			
 			if ( size < 10 ) { 
 				HbaseLog.l.debug("HSearchTableMultiQuery ff.execute: [" + 
 						matchingIds.toString() + "]" + " in ms " + (end - start));
 			} else {
 				HbaseLog.l.debug("HSearchTableMultiQuery ff.execute: Output Ids Total : [" +  
 					matchingIds.size() + "]" + " in ms " + (end - start));
 			}
 		}
 		
 		return matchingIds;
 	}
 }
