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
 
 import java.io.DataInput;
 import java.io.DataOutput;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.StringTokenizer;
 
 import org.apache.hadoop.hbase.KeyValue;
 import org.apache.hadoop.hbase.filter.Filter;
 import org.apache.hadoop.hbase.filter.FilterList;
 
 import com.bizosys.hsearch.byteutils.SortedBytesArray;
 import com.bizosys.hsearch.federate.FederatedFacade;
 import com.bizosys.hsearch.federate.QueryPart;
 import com.bizosys.hsearch.functions.HSearchReducer;
 import com.bizosys.hsearch.hbase.HbaseLog;
 import com.bizosys.hsearch.treetable.client.HSearchProcessingInstruction;
 import com.bizosys.hsearch.treetable.client.HSearchTableMultiQueryExecutor;
 import com.bizosys.hsearch.treetable.client.HSearchTableMultiQueryProcessor;
 import com.bizosys.hsearch.treetable.client.HSearchTableParts;
 import com.bizosys.hsearch.treetable.client.IHSearchPlugin;
 import com.bizosys.hsearch.treetable.client.L;
 
 /**
  * @author abinash
  *
  */
 public abstract class HSearchGenericFilter implements Filter {
 	
 	public static boolean DEBUG_ENABLED = HbaseLog.l.isDebugEnabled();
 	
 	String multiQuery = null;
 	Map<String, String> queryFilters = null;
 	Map<String,QueryPart> queryPayload = new HashMap<String, QueryPart>(3);
 	Map<String, String> colIdWithType = new HashMap<String, String>(3);
 	boolean hasMatchingIds = false;
 	
 	HSearchProcessingInstruction processingInstructions = new HSearchProcessingInstruction();
 
 	public HSearchGenericFilter(){
 	}
 	
 	public HSearchGenericFilter(HSearchProcessingInstruction outputType, String query, Map<String, String> details){
 		this.multiQuery = query;
 		this.queryFilters = details;
 		this.processingInstructions = outputType;
 	}
 	
 	public int getTotalQueryParts() throws IOException {
 		if ( null != this.queryFilters) return this.queryFilters.size();
 		if ( null != this.queryPayload) return this.queryPayload.size();
 		throw new IOException("Unable to find total queries inside the multi query.");
 	}
 	
 	
 	/**
 	 * output type
 	 * structured:A OR unstructured:B
 	 * structured:A=f|1|1|1|c|*|*
 	 * unstructured:B=*|*|*|*|*|*
 	 */
 	@Override
 	public void write(DataOutput out) throws IOException {
 		StringBuilder sb = new StringBuilder();
 		
 		sb.append(processingInstructions.toString()).append('\n');
 		sb.append(this.multiQuery);
 		
 		if ( null != queryFilters) {
 			for (String queryP : queryFilters.keySet()) {
 				String input = queryFilters.get(queryP);
 				sb.append('\n').append(queryP).append('=').append(input.toString());
 			}
 		}
 		
 		if ( DEBUG_ENABLED ) {
 			HbaseLog.l.debug("Sending to HBase : " + sb.toString());
 		}
 		
 		byte[] ser = sb.toString().getBytes();
 		out.writeInt(ser.length);
 		out.write(ser);
 	}	
 
 	/**
 	 * output type
 	 * structured:A OR unstructured:B
 	 * structured:A=f|1|1|1|c|*|*
 	 * unstructured:B=*|*|*|*|*|*
 	 * TODO:// Replace with Fast Split.
 	 */
 	@Override
 	public void readFields(DataInput in) throws IOException {
 		try {
 			int length = in.readInt();
 			if ( 0 == length) throw new IOException("Invalid Query");
 			
 			byte[] ser = new byte[length];
 			in.readFully(ser, 0, length);
 
 			StringTokenizer stk = new StringTokenizer(new String(ser), "\n");
 			
 			int lineNo = -1;
 			while ( stk.hasMoreTokens() ) {
 				
 				lineNo++;
 				
 				switch ( lineNo ) {
 					case 0:
 						String output = stk.nextToken();
 						if (output.length() == 0  ) throw new IOException("Unknown result output type.");
 						this.processingInstructions = new HSearchProcessingInstruction(output);
 						break;
 						
 					case 1:
 						this.multiQuery = stk.nextToken();
 						this.queryPayload = new HashMap<String, QueryPart>();
 
 						if ( DEBUG_ENABLED ) {
 							HbaseLog.l.debug("HBase Region Server: Multi Query" +  this.multiQuery);
 						}
 						break;
 
 					default:
 						String line = stk.nextToken();
 						int splitIndex = line.indexOf('=');
 						if ( -1 == splitIndex) throw new IOException("Expecting [=] in line " + line);
 						
 						String colNameQuolonId = line.substring(0,splitIndex);
 						String filtersPipeSeparated =  line.substring(splitIndex+1);
 						
 						int colNameAndQIdSplitIndex = colNameQuolonId.indexOf(':');
 						if ( -1 == colNameAndQIdSplitIndex || colNameQuolonId.length() - 1 == colNameAndQIdSplitIndex) {
 							throw new IOException("Sub queries expected as  X:Y eg.\n" + 
 									 "family1:A OR family2:B\nfamily1:A=f|1|1|1|c|*|*\nfamily2:B=*|*|*|*|*|*");
 						}
 						String colName = colNameQuolonId.substring(0,colNameAndQIdSplitIndex);
 						String qId =  colNameQuolonId.substring(colNameAndQIdSplitIndex+1);
 						
 						if ( DEBUG_ENABLED ) {
 							HbaseLog.l.debug("colName:qId = " + colName + "/" + qId);
 						}
 						
 						colIdWithType.put(qId, colName);
 						
 						IHSearchPlugin plugin = createPlugIn(colName) ;
 						plugin.setOutputType(this.processingInstructions);
 						
 						this.queryPayload.put(
 								colNameQuolonId, new QueryPart(filtersPipeSeparated,
 									HSearchTableMultiQueryExecutor.PLUGIN, plugin) );
 						
 
 						if ( DEBUG_ENABLED ) {
 							HbaseLog.l.debug("HBase Region Server: Query Payload " +  line);
 						}
 						break;
 				}
 			}
 		} catch (Exception ex) {
 			L.getInstance().flush();
 		} finally {
 			L.getInstance().clear();
 		}
 	}
 	
	/**
	 * TODO: 
	 * If we have a query as FieldA OR FieldB
	 * FieldA, tableparts should only contain byte[] of family FieldA_*
	 * and FieldB byte[] of family FieldB_*
	 */
 	@Override
 	public void filterRow(List<KeyValue> kvL) {
 		if ( null == kvL) return;
 		int kvT = kvL.size();
 		if ( 0 == kvT) return;
 		
 		if ( DEBUG_ENABLED ) {
 			HbaseLog.l.debug("Processing @ Region Server : filterRow" );
 		}
 		
 		
 		try {
 			byte[] row = null;
 			byte[] firstFamily = null;
 			byte[] firstCol = null;
 
 			//colParts.put("structured:A", bytes);
 			Map<String, HSearchTableParts> colNamesWithPartitionBytes = new HashMap<String, HSearchTableParts>();
 			
 			HSearchTableMultiQueryExecutor intersector = createExecutor();
 
 			//HBase Family Name = schema column name + "_" + partition
 			String columnNameWithParition = null;
 			String colName = null;
 			for (KeyValue kv : kvL) {
 				if ( null == kv) continue;
 
 				byte[] inputData = kv.getValue();
 				if ( null == inputData) continue;
 				columnNameWithParition = new String(kv.getFamily());
 				
 				int partitionIndex = columnNameWithParition.indexOf('_');
 				colName = ( partitionIndex == -1 ) ? columnNameWithParition : 
 					columnNameWithParition.substring(0, partitionIndex);
 				
 				HSearchTableParts tableParts =  null;
 				if ( colNamesWithPartitionBytes.containsKey(colName)) {
 					tableParts = colNamesWithPartitionBytes.get(colName);
 				} else {
 					tableParts = new HSearchTableParts();
 					colNamesWithPartitionBytes.put(colName, tableParts);
 				}
 				tableParts.put(inputData);
 
 				if ( null == row ) {
 					firstFamily = kv.getFamily();
 					firstCol = kv.getQualifier();
 					row = kv.getRow();
 				}
 			}
 			
 			if ( DEBUG_ENABLED ) {
 				HbaseLog.l.debug("queryData HSearchTableParts creation. ");
 			}
 			
 			Map<String, HSearchTableParts> queryData = new HashMap<String, HSearchTableParts>();
 			
 			for (String queryId : colIdWithType.keySet()) { //A
 				String queryType = colIdWithType.get(queryId); //structured
 				HSearchTableParts parts = colNamesWithPartitionBytes.get(queryType);
 				
 				String queryTypeWithId = queryType + ":" + queryId;
 
 				if ( DEBUG_ENABLED ) {
 					HbaseLog.l.debug(queryTypeWithId);
 					HbaseLog.l.debug("Query Parts for " + queryTypeWithId);
 				}
 				
 				queryData.put(queryTypeWithId, parts);
 			}
 			colNamesWithPartitionBytes.clear();
 			colNamesWithPartitionBytes = null;
 
 			if ( DEBUG_ENABLED ) HbaseLog.l.debug("HSearchGenericFilter: Filteration Starts");
 			List<FederatedFacade<String, String>.IRowId> intersectedIds = 
 				federatedQueryExec(row, intersector, queryData);
 			
 			kvL.clear(); //Clear all data
 			byte[] value = serialize(intersectedIds, this.queryPayload);
 			kvL.add(new KeyValue(row, firstFamily, firstCol, value) );
 			
 			if ( null != HSearchTableMultiQueryProcessor.processor) 
 				HSearchTableMultiQueryProcessor.processor.objectFactory.putprimaryKeyRowId(intersectedIds);
 			
 		} catch (Exception ex) {
 			ex.printStackTrace(System.err);
 		
 		} finally {
 			L.getInstance().flush();
 		}
 	}
 
 	private List<FederatedFacade<String, String>.IRowId> federatedQueryExec(byte[] row,
 			HSearchTableMultiQueryExecutor intersector,
 			Map<String, HSearchTableParts> queryData) throws Exception,
 			IOException {
 		
 		List<FederatedFacade<String, String>.IRowId> intersectedIds = null;
 		intersectedIds = intersector.execute(queryData, this.multiQuery, this.queryPayload, processingInstructions);
 
 		hasMatchingIds = ( null != intersectedIds && intersectedIds.size() > 0 );
 
 		if ( DEBUG_ENABLED ) {
 			HbaseLog.l.debug("Generaic filter hasMatchingIds :" + hasMatchingIds);
 			if ( hasMatchingIds ) HbaseLog.l.debug( new String(row) + " has ids of :" + intersectedIds.size());
 		}
 		
 		
 		return intersectedIds;
 	}
 	
 
 	@Override
 	public void reset() {
 		hasMatchingIds = false;
 	}	
 	
 	@Override
 	public boolean hasFilterRow() {
 		return true;
 	}	
 	
 	@Override
 	public KeyValue getNextKeyHint(KeyValue arg0) {
 		return null;
 	}	
 	
 	@Override
 	public boolean filterRowKey(byte[] rowKey, int offset, int length) {
 		return false;
 	}
 	
 	@Override
 	public boolean filterAllRemaining() {
 		return false;
 	}
 	
 	@Override
 	public boolean filterRow() {
 		return false;
 	}
 	
 	@Override
 	public ReturnCode filterKeyValue(KeyValue arg0) {
 		return ReturnCode.INCLUDE;
 	}	
 	
 	/**
 	 * Version 0.94 FIX
 	 */
 	@Override
 	public KeyValue transform(KeyValue arg0) {
 		return arg0;
 	}
 	
 	/**
 	 *******************************************************************************************
 	 * COMPUTATIONS
 	 * Step 1 - HSearch Table merge 
 	 *******************************************************************************************
 	 */
 	
 	/**
 	 * *|*|architect|age
 	 * AND
 	 * *|*|developer|age
 	 * 
 	 * @param matchedIds
 	 * @param queryPayload
 	 * @param processingInstructions
 	 * @return
 	 * @throws IOException
 	 */
 	public byte[] serialize( List<FederatedFacade<String, String>.IRowId> matchedIds, 
 		Map<String, QueryPart> queryPayload) throws IOException {
 		if ( DEBUG_ENABLED ) {
 			int matchedIdsT = ( null == matchedIds) ? 0 : matchedIds.size();
 			HbaseLog.l.debug("HSearchGenericFilter:serialize : with matchedIds " +  matchedIdsT);
 		}
 		
		/**
		 * - Iterate through all the parts and find the values.
		 * - Collect the data for multiple queries
		 */
 		HSearchReducer reducer = getReducer();
 		int totalQueries = queryPayload.values().size();

		Collection<byte[]> merged = new ArrayList<byte[]>();
		Collection<byte[]> append = new ArrayList<byte[]>(1);
 		
 		for (QueryPart part : queryPayload.values()) {
 			Object pluginO = part.getParams().get(HSearchTableMultiQueryExecutor.PLUGIN);
 			IHSearchPlugin plugin = (IHSearchPlugin) pluginO;
 			
 			if ( totalQueries == 1) {
 				plugin.getResultSingleQuery(merged);
 			} else {
				plugin.getResultMultiQuery(matchedIds, append);
				if ( null != reducer) reducer.appendCols(merged, append);
 				append.clear();
 			}
 		}
 		
 		//Put it to Bytes
 		return SortedBytesArray.getInstance().toBytes(merged);
 	}
 
 	public void deserialize(byte[] input, Collection<byte[]> output) throws IOException {
 		SortedBytesArray.getInstance().parse(input).values(output);
 	}
 	
 	public abstract HSearchTableMultiQueryExecutor createExecutor();
 	public abstract IHSearchPlugin createPlugIn(String type) throws IOException ;
 	public abstract HSearchReducer getReducer();
 	
 	/**
 	 * Override this method if you want to set more filters in processing.
 	 * 
 	 	FilterList list = new FilterList(FilterList.Operator.MUST_PASS_ALL);
 		RowFilter filter1 = new RowFilter(CompareOp.EQUAL, new BinaryComparator(Bytes.toBytes("row-22")) );
 		list.addFilter(filter1);
 		list.addFilter(this);
 		return list;
 		
 	 * @return
 	 */
 	public FilterList getFilters() {
 		return null;
 	}
 	
 	/**
 	 * Any information to be configured before starting the filtration process.
 	 */
 	public void configure() {
 	}
 	
 	/**
 	 * At the end release the resources.
 	 */
 	public void close() {
 	}
 	
 }
