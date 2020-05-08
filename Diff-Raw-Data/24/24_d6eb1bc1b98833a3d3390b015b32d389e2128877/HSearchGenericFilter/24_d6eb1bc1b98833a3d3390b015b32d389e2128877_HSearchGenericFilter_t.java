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
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.StringTokenizer;
 
 import org.apache.hadoop.hbase.KeyValue;
 import org.apache.hadoop.hbase.filter.Filter;
 import org.apache.hadoop.hbase.filter.FilterList;
 
 import com.bizosys.hsearch.byteutils.SortedBytesArray;
 import com.bizosys.hsearch.byteutils.SortedBytesBase.Reference;
 import com.bizosys.hsearch.federate.BitSetOrSet;
 import com.bizosys.hsearch.federate.QueryPart;
 import com.bizosys.hsearch.functions.HSearchReducer;
 import com.bizosys.hsearch.functions.StatementWithOutput;
 import com.bizosys.hsearch.hbase.HbaseLog;
 import com.bizosys.hsearch.treetable.client.HSearchProcessingInstruction;
 import com.bizosys.hsearch.treetable.client.HSearchTableMultiQueryExecutor;
 import com.bizosys.hsearch.treetable.client.HSearchTableParts;
 import com.bizosys.hsearch.treetable.client.IHSearchPlugin;
 
 /**
  * @author abinash
  *
  */
 public abstract class HSearchGenericFilter implements Filter {
 	
 	public static boolean DEBUG_ENABLED = HbaseLog.l.isDebugEnabled();
 	public static boolean INFO_ENABLED = HbaseLog.l.isInfoEnabled();
 
 	
 	
 	/**
 	 * Client side variables
 	 */
 	Map<String, String> queryFilters = null;
 	
 	/**
 	 * Input Variables
 	 */
 	String multiQuery = null;
 	Map<String,QueryPart> queryPayload = new HashMap<String, QueryPart>(3);
 	Map<String, String> colIdWithType = new HashMap<String, String>(3);
 	
 	public long pluginExecutionTime = 0L;
 	public long overallExecutionTime = 0L;
 	
 	
 	HSearchProcessingInstruction inputMapperInstructions = new HSearchProcessingInstruction();
 	byte[] inputRowsToIncludeB = null;
 	List<byte[]>  inputRowsList = null;
 	SortedBytesArray rowsToInclude = null;
 
 	Map<String, HSearchTableParts> queryIdWithParts = new HashMap<String, HSearchTableParts>();
 	Map<String, HSearchTableParts> colNamesWithPartitionBytes = new HashMap<String, HSearchTableParts>();
 	List<byte[]> columnsOfOneRowAfterJoin = new ArrayList<byte[]>();	
 	List<Collection<byte[]>> stmtOutputContainers = new LinkedList<Collection<byte[]>>();
 	SortedBytesArray rowBytesPacker = SortedBytesArray.getInstanceArr();
 	
 	HSearchTableMultiQueryExecutor intersector = null;
 	
 	public HSearchGenericFilter(){
 	}
 
 	public HSearchGenericFilter(final HSearchProcessingInstruction outputType, 
 		final String query, final  Map<String, String> details) {
 		this(outputType, query, details, null);
 	}
 	
 	public HSearchGenericFilter(final HSearchProcessingInstruction outputType, 
 		final String query, final  Map<String, String> details, List<byte[]> scopedToRows) {
 		
 		this.multiQuery = query;
 		this.queryFilters = details;
 		this.inputMapperInstructions = outputType;
 		this.inputRowsList = scopedToRows;
 	}
 	
 	public boolean clientSideAPI_IsSingleQuery() throws IOException {
 		if ( null == this.queryFilters) throw new IOException("Genric Filter is not initalized");
 		if ( 1 == this.queryFilters.size()) return true;
 		return false;
 	}
 	
 	public String clientSideAPI_getSingleQueryWithScope() throws IOException {
 		if ( null == this.queryFilters) throw new IOException("Genric Filter is not initalized");
 		if ( 1 != this.queryFilters.size()) throw new IOException("Genric Filter has multi queries");
 		return this.getClass().getName() + "/" + this.queryFilters.values().iterator().next();
 	}
 
 	
 	/**
 	 * output type
 	 * structured:A OR unstructured:B
 	 * structured:A=f|1|1|1|c|*|*
 	 * unstructured:B=*|*|*|*|*|*
 	 */
 	@Override
 	public final void write(final DataOutput out) throws IOException {
 
 		if ( null != inputRowsList) {
 			if ( inputRowsList.size() > 0 ) {
 				inputRowsToIncludeB = SortedBytesArray.getInstanceArr().toBytes(inputRowsList);
 			}
 		}
 		
 		StringBuilder querySection = new StringBuilder();
 		querySection.append(inputMapperInstructions.toString()).append('\n');
 		querySection.append(this.multiQuery);
 		if ( null != queryFilters) {
 			for (String queryP : queryFilters.keySet()) {
 				String input = queryFilters.get(queryP);
 				querySection.append('\n').append(queryP).append('=').append(input.toString());
 			}
 		}
 		
 		if ( DEBUG_ENABLED ) {
 			HbaseLog.l.debug("Sending to HBase : " + querySection.toString() + ", Rows to include:" + inputRowsToIncludeB);
 		}
 		SortedBytesArray sendToRSData = SortedBytesArray.getInstanceArr();
 		byte[] ser = ( null == inputRowsToIncludeB) ?
 			sendToRSData.toBytes(querySection.toString().getBytes())
 			: 
 			sendToRSData.toBytes( querySection.toString().getBytes(), inputRowsToIncludeB);
 		
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
 	public final void readFields(final DataInput in) throws IOException {
 		try {
 			int length = in.readInt();
 			if ( 0 == length) throw new IOException("Invalid Query");
 			
 			byte[] deser = new byte[length];
 			in.readFully(deser, 0, length);
 			
 			if ( DEBUG_ENABLED) {
 				HbaseLog.l.debug("Total bytes Received @ Generic Filter:" + length);
 			}
 			
 			SortedBytesArray receiveRSData = SortedBytesArray.getInstanceArr();
 			receiveRSData.parse(deser);
 
 			int packedDataSectionsT = receiveRSData.getSize();
 			if ( DEBUG_ENABLED) {
 				HbaseLog.l.debug("Reading bytes sections of total :" + packedDataSectionsT);
 			}
 			if ( packedDataSectionsT == 0 ) {
 				throw new IOException("Unknown number of fields :" + packedDataSectionsT);
 			}
 			
 			//Filter Row Section
 			if ( packedDataSectionsT == 2) {
 				Reference ref = new Reference();
 				receiveRSData.getValueAtReference(1, ref);
 				rowsToInclude = SortedBytesArray.getInstanceArr();
 				rowsToInclude.parse(deser, ref.offset, ref.length);
 			}
 			
 			//Query Section
 			Reference ref = new Reference();
 			receiveRSData.getValueAtReference(0, ref);
 			StringTokenizer stk = new StringTokenizer(new String(deser, ref.offset, ref.length), "\n");
 			
 			int lineNo = -1;
 			while ( stk.hasMoreTokens() ) {
 				
 				lineNo++;
 				
 				switch ( lineNo ) {
 					case 0:
 						String output = stk.nextToken();
 						if (output.length() == 0  ) throw new IOException("Unknown result output type.");
 						this.inputMapperInstructions = new HSearchProcessingInstruction(output);
 						break;
 						
 					case 1:
 						this.multiQuery = stk.nextToken();
 
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
 						plugin.setOutputType(this.inputMapperInstructions);
 						
 						this.queryPayload.put(
 								colNameQuolonId, new QueryPart(filtersPipeSeparated,
 									HSearchTableMultiQueryExecutor.PLUGIN, plugin) );
 						
 
 						if ( DEBUG_ENABLED ) {
 							HbaseLog.l.debug("HBase Region Server: Query Payload " +  line);
 						}
 						break;
 				}
 			}
 			for (int i=0; i<this.queryPayload.size(); i++) {
 				this.stmtOutputContainers.add( new ArrayList<byte[]>() );
 			}
 			
 			
 		} catch (Exception ex) {
 			HbaseLog.l.fatal(ex);
 			throw new IOException(ex);
 		}
 	}
 	
 	/**
 	 * TODO: 
 	 * If we have a query as FieldA OR FieldB
 	 * FieldA, tableparts should only contain byte[] of family FieldA_*
 	 * and FieldB byte[] of family FieldB_*
 	 */
 	@Override
 	public final  void filterRow(final List<KeyValue> kvL) {
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
 			colNamesWithPartitionBytes.clear();
 			
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
 			
 			queryIdWithParts.clear();
 			
 			for (String queryId : colIdWithType.keySet()) { //A
 				String queryType = colIdWithType.get(queryId); //structured
 				HSearchTableParts parts = colNamesWithPartitionBytes.get(queryType);
 				
 				String queryTypeWithId = queryType + ":" + queryId;
 
 				if ( DEBUG_ENABLED ) {
 					HbaseLog.l.debug(queryTypeWithId);
 					HbaseLog.l.debug("Query Parts for " + queryTypeWithId);
 				}
 				
 				queryIdWithParts.put(queryTypeWithId, parts);
 			}
 			colNamesWithPartitionBytes.clear();
 
 			if ( DEBUG_ENABLED ) HbaseLog.l.debug("HSearchGenericFilter: Filteration Starts");
 			
 			long monitorStartTime = 0L; 
 			if ( INFO_ENABLED ) {
 				monitorStartTime = System.currentTimeMillis();
 			}	
 			
 			if ( null == intersector ) intersector = createExecutor();
 			
 			BitSetOrSet intersectedIds = federatedQueryExec(row, intersector, queryIdWithParts);
 
 			if ( INFO_ENABLED ) {
 				this.pluginExecutionTime += System.currentTimeMillis() - monitorStartTime;
 			}
 			
 			kvL.clear(); //Clear all data
 			byte[] value = getOneRowBytes(intersectedIds, this.queryPayload);
 			kvL.add(new KeyValue(row, firstFamily, firstCol, value) );
 			
 			
 		} catch (Exception ex) {
 			ex.printStackTrace(System.err);
 			HbaseLog.l.fatal(ex);
 		}
 	}
 
 	private final  BitSetOrSet federatedQueryExec(final byte[] row,
 			final HSearchTableMultiQueryExecutor intersector,
 			final Map<String, HSearchTableParts> queryData) throws Exception, IOException {
 		
 		BitSetOrSet intersectedIds = intersector.execute(
 			queryData, this.multiQuery, this.queryPayload, inputMapperInstructions);
 
 		if ( DEBUG_ENABLED ) {
			boolean hasMatchingIds = false;
 			hasMatchingIds = ( null != intersectedIds && intersectedIds.size() > 0 );
			HbaseLog.l.debug("Generaic filter hasMatchingIds :" + hasMatchingIds + " objectid=" + intersectedIds.hashCode());
 			if ( hasMatchingIds ) HbaseLog.l.debug( new String(row) + " has ids of :" + intersectedIds.size());
 		}
 		
 		return intersectedIds;
 	}
 	
 
 	@Override
 	public final void reset() {
 	}	
 	
 	@Override
 	public final boolean hasFilterRow() {
 		return true;
 	}	
 	
 	@Override
 	public final KeyValue getNextKeyHint(final KeyValue arg0) {
 		return null;
 	}	
 	
 	@Override
 	public final boolean filterRowKey(final byte[] rowKey, final int offset, final int length) {
 		
 		if (DEBUG_ENABLED) {
 			int scopeToTheseRowsT = ( null == inputRowsToIncludeB) ? 0 : inputRowsToIncludeB.length;
 			HbaseLog.l.debug("Analyzing row for processing: " + new String(rowKey + " , From a matching set of " + scopeToTheseRowsT));
 		}
 		
 		if ( null == inputRowsToIncludeB) return false;
 		
 		byte[] exactRowBytes = new byte[length];
 		try {
 			System.arraycopy(rowKey, offset, exactRowBytes, 0, length);
 			if ( rowsToInclude.getEqualToIndex(exactRowBytes) == -1) return false;
 			return true;
 			
 		} catch (IOException ex) {
 			int scopeToTheseRowsT = ( null == rowsToInclude) ? 0 : rowsToInclude.getSize();
 			String rowKeyStr = ( null == rowKey) ? "Null row key" : new String(rowKey);
 			String errMsg = "Error while finding fileration criteria for the row , " + rowKeyStr 
 				+ "\n" + ex.getMessage() + "\n" + 
 				"With search scope inside id count : " + scopeToTheseRowsT;
 			System.err.println(errMsg);
 			HbaseLog.l.fatal(errMsg, ex);
 			
 			return false;
 		}
 	}
 	
 	@Override
 	public final boolean filterAllRemaining() {
 		return false;
 	}
 	
 	@Override
 	public final boolean filterRow() {
 		return false;
 	}
 	
 	@Override
 	public final ReturnCode filterKeyValue(final KeyValue arg0) {
 		return ReturnCode.INCLUDE;
 	}	
 	
 	/**
 	 * Version 0.94 FIX
 	 */
 	@Override
 	public final KeyValue transform(final KeyValue arg0) {
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
 	 * @param inputMapperInstructions
 	 * @return
 	 * @throws IOException
 	 */
 	public final byte[] getOneRowBytes( final BitSetOrSet matchedIds, final Map<String, QueryPart> queryPayload) throws IOException {
 		
 		if ( DEBUG_ENABLED ) {
 			int matchedIdsT = ( null == matchedIds) ? 0 : matchedIds.size();
			HbaseLog.l.debug("HSearchGenericFilter:serialize : with matchedIds " +  matchedIdsT + ", Object:" + matchedIds.hashCode());
			if ( null != matchedIds.getDocumentIds()) {
				HbaseLog.l.debug("HSearchGenericFilter: DocumentIds size " +  matchedIds.getDocumentIds().size() + " and matchedId size " + matchedIds.size());
			} else if ( null != matchedIds.getDocumentSequences()) {
				HbaseLog.l.debug("HSearchGenericFilter: DocumentSequences cardinality " +  matchedIds.getDocumentSequences().cardinality());
			}
 		}
 		
 		/**
 		 * - Iterate through all the parts and find the values.
 		 * - Collect the data for multiple queries
 		 */
 		HSearchReducer reducer = getReducer();
 		int totalQueries = queryPayload.size();
 		
 		columnsOfOneRowAfterJoin.clear();
 		long monitorStartTime = 0L; 
 
 		if ( totalQueries == 1) {
 			
 			Object pluginO = queryPayload.values().iterator().next().getParams().get(
 				HSearchTableMultiQueryExecutor.PLUGIN);
 			IHSearchPlugin plugin = (IHSearchPlugin) pluginO;
 			
 			if ( INFO_ENABLED ) {
 				monitorStartTime = System.currentTimeMillis();
 			}
 			
 			plugin.getResultSingleQuery(columnsOfOneRowAfterJoin);
 			
 			if ( INFO_ENABLED ) {
 				this.pluginExecutionTime += System.currentTimeMillis() - monitorStartTime;
 			}
 			
 			
 		} else {
 			StatementWithOutput[] stmtWithOutputs = new StatementWithOutput[totalQueries];
 			int seq = 0;
 			
 			for (QueryPart part : queryPayload.values()) {
 				
 				Object pluginO = part.getParams().get(HSearchTableMultiQueryExecutor.PLUGIN);
 				IHSearchPlugin plugin = (IHSearchPlugin) pluginO;
 				
 				if ( INFO_ENABLED ) {
 					monitorStartTime = System.currentTimeMillis();
 				}	
 				
 				Collection<byte[]> queryOutput = this.stmtOutputContainers.get(seq);
 				queryOutput.clear(); //Clear to reuse
 				plugin.getResultMultiQuery(matchedIds, queryOutput);
 				
 				if ( INFO_ENABLED ) {
 					this.pluginExecutionTime += System.currentTimeMillis() - monitorStartTime;
 				}
 				
 				stmtWithOutputs[seq] = new StatementWithOutput(part.aStmtOrValue, queryOutput);
 				seq++;
 			}
 			
 			if ( INFO_ENABLED ) {
 				monitorStartTime = System.currentTimeMillis();
 			}	
 
 			reducer.appendCols(stmtWithOutputs, columnsOfOneRowAfterJoin);
 			
 			if ( INFO_ENABLED ) {
 				this.pluginExecutionTime += System.currentTimeMillis() - monitorStartTime;
 			}
 			
 			for (StatementWithOutput stmtWithOutput : stmtWithOutputs) {
 				if ( null != stmtWithOutput.cells ) stmtWithOutput.cells.clear();
 			}
 		}
 
 		//Put it to Bytes
 		byte[] processedRowBytes = rowBytesPacker.toBytes(columnsOfOneRowAfterJoin);
 		columnsOfOneRowAfterJoin.clear();
 		
 		return processedRowBytes;
 	}
 	
 	public final void deserialize(final byte[] input, final Collection<byte[]> output) throws IOException {
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
 	public final FilterList getFilters() {
 		return null;
 	}
 	
 	/**
 	 * Any information to be configured before starting the filtration process.
 	 */
 	public final void configure() {
 	}
 	
 	/**
 	 * At the end release the resources.
 	 */
 	public final void close() {
 		if ( null != queryFilters)  queryFilters.clear();
 		if ( null != queryPayload)  queryPayload.clear();
 		if ( null != colIdWithType)  colIdWithType.clear();
 		if ( null != queryIdWithParts)  queryIdWithParts.clear();
 		if ( null != colNamesWithPartitionBytes)  colNamesWithPartitionBytes.clear();
 		if ( null != columnsOfOneRowAfterJoin)  columnsOfOneRowAfterJoin.clear();
 		if ( null != stmtOutputContainers)  stmtOutputContainers.clear();
 	}
 	
 	
 }
