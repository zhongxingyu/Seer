 /*
 * Copyright 2013 Bizosys Technologies Limited
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
 
 package com.bizosys.hsearch.kv;
 
 import java.io.IOException;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.regex.Pattern;
 
 import com.bizosys.hsearch.byteutils.SortedBytesBitset;
 import com.bizosys.hsearch.federate.BitSetOrSet;
 import com.bizosys.hsearch.federate.BitSetWrapper;
 import com.bizosys.hsearch.federate.FederatedSearch;
 import com.bizosys.hsearch.federate.FederatedSearchException;
 import com.bizosys.hsearch.federate.QueryPart;
 import com.bizosys.hsearch.functions.GroupSortedObject;
 import com.bizosys.hsearch.functions.GroupSortedObject.FieldType;
 import com.bizosys.hsearch.functions.GroupSorter;
 import com.bizosys.hsearch.functions.GroupSorter.GroupSorterSequencer;
 import com.bizosys.hsearch.hbase.HReader;
 import com.bizosys.hsearch.kv.dao.DataBlock;
 import com.bizosys.hsearch.kv.dao.MapperKVBaseEmpty;
 import com.bizosys.hsearch.kv.dao.ScalarFilter;
 import com.bizosys.hsearch.kv.impl.Datatype;
 import com.bizosys.hsearch.kv.impl.FieldMapping;
 import com.bizosys.hsearch.kv.impl.FieldMapping.Field;
 import com.bizosys.hsearch.kv.impl.KVDataSchemaRepository;
 import com.bizosys.hsearch.kv.impl.KVDataSchemaRepository.KVDataSchema;
 import com.bizosys.hsearch.kv.impl.StorageReader;
 import com.bizosys.hsearch.kv.impl.TypedObject;
 import com.bizosys.hsearch.treetable.client.HSearchProcessingInstruction;
 import com.bizosys.hsearch.treetable.client.HSearchQuery;
 import com.bizosys.hsearch.treetable.client.IHSearchTable;
 import com.bizosys.hsearch.util.HSearchLog;
 import com.bizosys.hsearch.util.LineReaderUtil;
 import com.bizosys.hsearch.util.ShutdownCleanup;
 import com.bizosys.unstructured.util.IdSearchLog;
 
 public class Searcher {
 
 	public String dataRepository = "";
 	public String schemaRepositoryName = "";
 	
 	public static final Pattern patternExtraWhitespaces = Pattern.compile("\\s+");
 	public static final Pattern patternBooleans = Pattern.compile("( AND | OR | NOT )");
 	public static final Pattern patternComma = Pattern.compile(",");
 	public static final Pattern patternBracketsOutsideQuotes = Pattern.compile("(\\(|\\))(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
 	public static final Pattern patternRemoveQuotes = Pattern.compile("\"");
 	
 	private Set<KVRowI> resultset = null;
 	private Map<String, Set<Object>> facetsMap = null;
 	private Map<String, List<HsearchFacet>> pivotFacetsMap = null;
 	public KVDataSchemaRepository repository = KVDataSchemaRepository.getInstance();
 	
 	public static boolean DEBUG_ENABLED = HSearchLog.l.isDebugEnabled();
 	public static boolean INFO_ENABLED = HSearchLog.l.isInfoEnabled();
 	
 	private boolean checkForAllWords = false;
 	private ISearcherPlugin plugIn = null;	
 
 	private Map<Integer,KVRowI> joinedRows = null;
 	
 	public Searcher(final String schemaName, final FieldMapping fm){
 		this.schemaRepositoryName = schemaName;
 		this.repository.add(schemaRepositoryName, fm);
 		this.resultset = new HashSet<KVRowI>();
 	}
 	
 	public final void setCheckForAllWords(final boolean checkForAllWords) {
 		this.checkForAllWords = checkForAllWords;
 	}
 	
 	public final void setPlugin(ISearcherPlugin plugIn) {
 		this.plugIn = plugIn;
 	}	
 
 	public final Set<String> searchRegex(final String dataRepository,
 			final String mergeIdPattern, final String selectQuery, String whereQuery,
 			KVRowI blankRow, IEnricher... enrichers) 
 		throws IOException, InterruptedException, ExecutionException, FederatedSearchException, NumberFormatException, ParseException  {
 
 		long start = System.currentTimeMillis();
 		this.dataRepository = dataRepository;
 		List<String> rowIds = HReader.getMatchingRowIds(dataRepository, mergeIdPattern);
 		if ( null == rowIds) return null;
 		if ( rowIds.size() == 0 ) return null;
 		
 		Set<String> mergeIds = new HashSet<String>();
 		int lastIndex = -1;
 		for (String mergeIdWithFieldId : rowIds) {
 			lastIndex = mergeIdWithFieldId.lastIndexOf('_');
 			mergeIds.add(  mergeIdWithFieldId.substring(0, lastIndex) );
 		}
 		
 		if ( DEBUG_ENABLED) {
 			long end = System.currentTimeMillis();
 			int mergeIdsT = ( null == mergeIds) ? 0 : mergeIds.size();
 			HSearchLog.l.debug( mergeIdPattern +  " rows regex matched total = " + mergeIdsT + " in Time ms, " + (end - start));
 			start = end;
 		}
 		
 		for (String mergeId : mergeIds) {
 			search(dataRepository, mergeId, selectQuery, whereQuery, blankRow, enrichers);
 			if ( null != joinedRows) joinedRows.clear();
 		}
 
 		if ( DEBUG_ENABLED) {
 			long end = System.currentTimeMillis();
 			HSearchLog.l.debug( mergeIdPattern +  " fields retrieved in Time ms, " + (end - start));
 		}
 		
 		return mergeIds;
 	}
 	
 	public final void search(final String dataRepository, 
 			final String mergeId, final String selectQuery, String whereQuery, 
 			final KVRowI blankRow, final IEnricher... enrichers) 
 			throws IOException, InterruptedException, ExecutionException, FederatedSearchException, NumberFormatException, ParseException {
 		
 		this.search(dataRepository, mergeId, selectQuery, whereQuery, null, blankRow, enrichers);
 	}
 	
 	
 	public final void search(final String dataRepository, final String mergeId,  
 			final String selectQuery, String whereQuery, String facetFields,
 			final KVRowI blankRow, final IEnricher... enrichers) 
 			throws IOException, InterruptedException, ExecutionException, FederatedSearchException, NumberFormatException, ParseException {
 		
 		this.dataRepository = dataRepository;
 		BitSetOrSet matchIds = null;
 		boolean isWhereQueryEmpty = ( null == whereQuery) ? true : (whereQuery.length() == 0);
 		
 		/**
 		 * Find Matching Ids - START
 		 */
 		
 		Map<String, QueryPart> whereParts = new HashMap<String, QueryPart>();
 		if(!isWhereQueryEmpty){
 			whereQuery = parseWhereQuery(mergeId, whereQuery, whereParts);
 			matchIds = getIds(mergeId, whereQuery, whereParts);
 			if(0 == matchIds.size()) return;
 		}
 
 		int matchIdsT = (null == matchIds) ?  64 : matchIds.size();
 		if ( null == this.joinedRows)
 			this.joinedRows = new HashMap<Integer, KVRowI>(matchIdsT);
 		else this.joinedRows.clear();
 		
 		if ( null != matchIds) {
 			if ( null != this.plugIn) this.plugIn.onJoin(
 				mergeId, matchIds.getDocumentSequences(), whereParts, this.joinedRows);			
 		}
 		
 
 		/**
 		 * Find Matching Ids - END
 		 * Find Facets - START
 		 */
		if ( null != this.plugIn) {
			
			int matchIdsFoundT = ( null == matchIds) ? 0 : ( null == matchIds.getDocumentSequences()) ? 0 : matchIds.getDocumentSequences().length();
			if ( isWhereQueryEmpty && ( matchIdsFoundT > 0 ) ) {
 				Map<String, Map<Object, FacetCount>> facets = createFacetCount(
 					matchIds.getDocumentSequences(), mergeId, facetFields);
 				this.plugIn.onFacets(mergeId, facets);
			} else {
				Map<String, Map<Object, FacetCount>> facets = createFacetCount(null, mergeId, facetFields);
				this.plugIn.onFacets(mergeId, facets);
 			}
 		}
 
 		/**
 		 * Value fetch
 		 */
 		if ( null != matchIds) {
 			if ( null != this.plugIn) 	{
 				this.plugIn.beforeSelect(mergeId, matchIds.getDocumentSequences());
 			}
 		}
 		
 		if ( null != selectQuery) 
 			getValues(dataRepository, mergeId, matchIds, selectQuery, blankRow);
 		
 		if ( null != matchIds) {
 			if ( null != this.plugIn) 	{
 				this.plugIn.afterSelect(mergeId, matchIds.getDocumentSequences());
 			}
 		}
 
 		/**
 		 * Apply Enricher
 		 */
 		if ( null != enrichers && null != this.resultset) {
 			for (IEnricher enricher : enrichers) {
 				if ( null != enricher) enricher.enrich(this.resultset);
 			}
 		}		
 		
 	}
 	
 	@SuppressWarnings("unchecked")
 	public final void getValues(final String dataRepository, 
 			final String mergeId, final BitSetOrSet matchIds, final String selectQuery,  
 			final KVRowI blankRow, final IEnricher... enrichers) throws IOException, InterruptedException, ExecutionException, FederatedSearchException {
 
 		Map<String, Object> fieldWithDocIdAndValue = null;
 		Set<Integer> documentIds = null;
 		Set<String> fields = null;
 		Map<Integer, Object> result = null;
 
 		this.dataRepository = dataRepository;
 
 		boolean isSelectQueryEmpty = ( null == selectQuery) ? true : (selectQuery.length() == 0);
 		if(!isSelectQueryEmpty) fieldWithDocIdAndValue = getAllSelectFieldValues(
 			mergeId, matchIds, selectQuery);
 		
 		if(null == fieldWithDocIdAndValue) return;
 		
 		if ( null == this.joinedRows) {
 			int matchIdsT = ( null == matchIds) ? 64 : matchIds.size();
 			this.joinedRows = new HashMap<Integer, KVRowI>(matchIdsT); 
 		}
 		
 		fields = fieldWithDocIdAndValue.keySet();
 		KVDataSchema dataSchema = repository.get(schemaRepositoryName);
 		KVRowI aRow = null;
 		
 		for (String field : fields) {
 
 			result = (Map<Integer, Object>) fieldWithDocIdAndValue.get(field);
 			if ( null == result) continue;
 			documentIds = result.keySet();
 
 			for (Integer id : documentIds) {
 				
 				if (joinedRows.containsKey(id)){
 					aRow = joinedRows.get(id);
 				} else {
 					aRow = blankRow.create(dataSchema, id, mergeId);
 					joinedRows.put(id, aRow);
 				}
 				aRow.setValue(field, result.get(id));
 
 			}
 		}
 
 		resultset.addAll(joinedRows.values());
 	}
 
 	public final BitSetOrSet getIds(final String dataRepository, final String mergeId, String whereQuery) throws IOException {
 		this.dataRepository = dataRepository;
 		Map<String, QueryPart> whereQueryParts = new HashMap<String, QueryPart>();
 		whereQuery = parseWhereQuery(mergeId, whereQuery, whereQueryParts);
 		return getIds(mergeId, whereQuery, whereQueryParts);
 	}
 	
 	private final BitSetOrSet getIds(final String mergeId, String whereQuery, Map<String, QueryPart> whereQueryParts) throws IOException {
 
 		try {
 
 			FederatedSearch ff = createFederatedSearch();
 			BitSetOrSet mixedQueryMatchedIds = ff.execute(whereQuery, whereQueryParts);
 			
 			/**
 			 * Remove the delete Ids
 			 */
 			if (this.repository.get(this.schemaRepositoryName).fm.delete ) {
 				BitSetWrapper deleteRecords = DataBlock.getPinnedBitSets(
 					this.repository.get(this.schemaRepositoryName).fm.tableName, 
 					DataBlock.getDeleteId( mergeId ) );
 				
 				if ( null != deleteRecords) {
 					mixedQueryMatchedIds.getDocumentSequences().andNot(deleteRecords);
 				}
 			}
 
 			return mixedQueryMatchedIds;
 			
 		} catch (Exception e) {
 			HSearchLog.l.fatal("Error in Searcher: could not execute " + e.getMessage(), e);
 			throw new IOException("Federated Query Failure: " + whereQuery + "\n" + e.getMessage());
 		}
 	}
 
 	 protected static ExecutorService ES = null;
 	 private void init(){
 		 if(null == ES) {
 			 ES = Executors.newFixedThreadPool(30);
 			 ShutdownCleanup.getInstance().addExectorService(ES);
 		 }
 	 }
 
 	 /**
 	  * Get all the field values for the select fields
 	  * @param mergeId
 	  * @param selectFields
 	  * @return
 	  * @throws IOException
 	  * @throws InterruptedException
 	  * @throws ExecutionException
 	  */
 	private final Map<String, Object> getAllSelectFieldValues(final String mergeId, 
 			final BitSetOrSet matchIds, final String selectFields) throws IOException, InterruptedException, ExecutionException {
 		
 		String rowId = null;
 		Map<String, Object> fieldWithDocIdAndValue = new HashMap<String, Object>();
 		byte[] matchingIdsB = null;
 		if( null != matchIds )
 			matchingIdsB = SortedBytesBitset.getInstanceBitset().bitSetToBytes(matchIds.getDocumentSequences());
 		
 		String[] selectFieldsA = patternComma.split(selectFields);
 		Map<String, Future<Map<Integer, Object>>> fieldWithfuture = new HashMap<String, Future<Map<Integer, Object>>>();
 		KVDataSchema dataScheme =  repository.get(schemaRepositoryName);
 		Field fld = null;
 		int outputType = -1;
 		String isRepeatable = null;
 		String isCompressed = null;
 		
 		init();
 		
 		for (String field : selectFieldsA) {
 			rowId = mergeId + "_" + field;
 			if ( ! dataScheme.fldWithDataTypeMapping.containsKey(field) ) {
 				throw new IOException("Unknown Field on select clause " + field);
 			}
 			
 			outputType = dataScheme.fldWithDataTypeMapping.get(field);
 			outputType = (outputType == Datatype.FREQUENCY_INDEX) ? Datatype.STRING : outputType;
 			fld = dataScheme.fm.nameWithField.get(field);
 			isRepeatable = fld.isRepeatable ? "true" : "false";
 			isCompressed = fld.isCompressed ? "true" : "false";
 
 			HSearchProcessingInstruction instruction = new HSearchProcessingInstruction(
 				HSearchProcessingInstruction.PLUGIN_CALLBACK_COLS, outputType,
 				isRepeatable + "\t" + isCompressed);
 			
 			String filterQuery = "*|*";
 			
 			StorageReader reader = new StorageReader(dataRepository, rowId, 
 				matchIds, matchingIdsB, field, filterQuery, instruction, fld.isCachable);
 			Future<Map<Integer, Object>> future = ES.submit(reader);
 
 			fieldWithfuture.put(field, future);
 		}
 		Set<String> fields = fieldWithfuture.keySet();
 		Future<Map<Integer, Object>> future = null;
 		for (String field : fields){
 			future = fieldWithfuture.get(field);
 			fieldWithDocIdAndValue.put(field, future.get());
 		}
 		
 		return fieldWithDocIdAndValue;
 	}
 
 	public final Set<KVRowI> sort (final String... sorters) throws ParseException {
 		
 		GroupSorterSequencer[] sortSequencer = new GroupSorterSequencer[sorters.length];
 
 		int index = 0;
 		int fieldSeq = 0;
 		int dataType = -1;
 		FieldType fldType = null;
 		boolean sortType = false;
 
 		KVDataSchema dataSchema = repository.get(schemaRepositoryName); 
 		
 		for (String sorterName : sorters) {
 			
 			int sorterLen = ( null == sorterName ) ? 0 : sorterName.length();
 			if ( sorterLen == 0 ) throw new ParseException("Invalid blank sorter", 0);
 			
 			char firstChar = sorterName.charAt(0);
 			if('^' == firstChar){
 				sortType = true;
 				sorterName = sorterName.substring(1);
 			}
 			else{
 				sortType = false;				
 			}
 			
 			fieldSeq = dataSchema.nameToSeqMapping.get(sorterName);
 			dataType = dataSchema.fldWithDataTypeMapping.get(sorterName);
 			fldType = Datatype.getFieldType(dataType);
 
 			GroupSorterSequencer seq = new GroupSorterSequencer(fldType,fieldSeq,index,sortType);
 			
 			sortSequencer[index++] = seq;
 		}
 		GroupSorter gs = new GroupSorter();
 		
 		for (GroupSorterSequencer seq : sortSequencer) {
 			gs.setSorter(seq);
 		}
 		int resultsetT = resultset.size();
 		GroupSortedObject[] sortedContainer = new GroupSortedObject[resultsetT];
 		resultset.toArray(sortedContainer);
 		gs.sort(sortedContainer);
 		resultset = new LinkedHashSet<KVRowI>();
 		for (int j=0; j < resultsetT; j++) {
 			resultset.add((KVRowI)sortedContainer[j]);
 		}
 
 		return this.resultset;
 	}
 	
 	public final Set<KVRowI> getResult() {
 		return this.resultset;
 	}
 
 	public final Map<String, Map<Object, FacetCount>> createFacetCount(
 		final BitSetWrapper matchedIds, final String mergeId, final String facetFields) 
 		throws NumberFormatException, IOException, ParseException {
 
 		List<String> facetFieldLst = new ArrayList<String>();
 		LineReaderUtil.fastSplit(facetFieldLst, facetFields, ',');
 
 		KVDataSchema dataScheme =  repository.get(schemaRepositoryName);
 		Field fld = null;
 		String isRepeatable = null;
 		String isCompressed = null;
 		int outputType;
 		String filterQuery = "*|*";
 		String rowId = null;
 		
 		Map<String, Map<Object, FacetCount>> facets = new HashMap<String, Map<Object, FacetCount>>();
 		
 		for (String field : facetFieldLst) {
 			
 			rowId = mergeId + "_" + field;
 			fld = dataScheme.fm.nameWithField.get(field);
 			byte[] bytes = DataBlock.getBlock(dataRepository, rowId, true);
 			if(null == bytes) continue;
 			
 			isRepeatable = fld.isRepeatable ? "true" : "false";
 			isCompressed = fld.isCompressed ? "true" : "false";
 			outputType = dataScheme.fldWithDataTypeMapping.get(field);
 			outputType = (outputType == Datatype.FREQUENCY_INDEX) ? Datatype.STRING : outputType;
 
 			HSearchProcessingInstruction instruction = new HSearchProcessingInstruction(
 				HSearchProcessingInstruction.PLUGIN_CALLBACK_COLS, outputType,
 				isRepeatable + "\t" + isCompressed);
 			
 
 			IHSearchTable table = ScalarFilter.createTable(instruction);
 			final Map<Object, FacetCount> facetValues = new HashMap<Object, FacetCount>();
 
 	    	HSearchQuery hq = new HSearchQuery(filterQuery);
 	    	table.get(bytes, hq, new MapperKVBaseEmpty() {
 				
 				@Override
 				public void setMergeId(byte[] mergeId) throws IOException {
 				}
 				
 				@Override
 				public boolean onRowKey(BitSetWrapper ids) {
 					return false;
 				}
 				
 				@Override
 				public boolean onRowCols(BitSetWrapper ids, Object value) {
					if ( null != matchedIds) ids.and(matchedIds);
 					int idsT = ids.cardinality();
 					if ( idsT > 0 ) facetValues.put(value, new FacetCount(idsT));
 					return false;
 				}
 				
 				@Override
 				public boolean onRowKey(int id) {
 					return false;
 				}
 				
 				@Override
 				public boolean onRowCols(int key, Object value) {
					if (null != matchedIds) {
						if ( ! matchedIds.get(key)) return false;
					}
 					if ( facetValues.containsKey(value)) {
 						facetValues.get(value).count++;
 					} else {
 						facetValues.put(value, new FacetCount());
 					}
 					
 					return false;
 				}
 			});
 	    	
 	    	facets.put(field, facetValues);
 		}
 		return facets;
 	}		
 
 	public final void clear() {
 		if(null != resultset)resultset.clear();
 		if(null != facetsMap)facetsMap.clear();
 		if(null != pivotFacetsMap)pivotFacetsMap.clear();
 	}
 	
 	private FederatedSearch createFederatedSearch() {
 		
 		FederatedSearch ff = new FederatedSearch(2) {
 
 			@Override
 			public BitSetOrSet populate(final String type, final String queryId,
 					final String rowId, final Map<String, Object> filterValues) throws IOException {
 				
 				String filterQuery = null;
 				for (String key : filterValues.keySet()) {
 					if ( "query".equals(key) ) filterQuery = filterValues.get(key).toString();
 				}
 				
 				int tableNameLen = ( null == dataRepository) ? 0 : dataRepository.trim().length();
 				
 				if ( 0 == tableNameLen) {
 					IdSearchLog.l.fatal("Unknown data repository for query " + queryId);
 					throw new IOException("Unknown data repository for query " + queryId);
 				}
 				
 				KVDataSchema dataScheme = repository.get(schemaRepositoryName);;
 				String field = rowId.substring(rowId.lastIndexOf('_') + 1);
 				if ( null == field) {
 					String msg = "Field can not be null - " + rowId + "\n" + (dataScheme.fldWithDataTypeMapping + "\t" + field);
 					throw new IOException(msg);
 				}
 				
 				if ( ! dataScheme.fldWithDataTypeMapping.containsKey(field)) {
 					throw new IOException("Field Is not mapped properly - " + field);
 				}
 				int outputType = dataScheme.fldWithDataTypeMapping.get(field);
 				
 				Field fld = dataScheme.fm.nameWithField.get(field);
 				String isRepeatable = fld.isRepeatable ? "true" : "false";
 				String isCompressed = fld.isCompressed ? "true" : "false";
 
 				boolean isTextSearch = fld.isDocIndex && !fld.isStored;
 				
 				
 				HSearchProcessingInstruction instruction = new HSearchProcessingInstruction(HSearchProcessingInstruction.PLUGIN_CALLBACK_ID, 
 					outputType, isRepeatable + "\t" + isCompressed);
 				
 				StorageReader reader = new StorageReader(dataRepository, rowId,
 					filterQuery, instruction, fld.isCachable);
 				
 				BitSetWrapper readingIds = null;
 				
 				readingIds = isTextSearch ? reader.readStorageTextIds(fld, checkForAllWords, field) : 
 					reader.readStorageIds();
 
 				BitSetOrSet rows = new BitSetOrSet();
 				rows.setDocumentSequences(readingIds);
 				return rows;
 			}
 		};
 		return ff;
 	}
 	
 	private String parseWhereQuery(final String mergeId, String whereQuery,
 			Map<String, QueryPart> queryDetails) throws IOException {
 		String skeletonQuery = patternExtraWhitespaces.matcher(whereQuery).replaceAll(" ");
 		skeletonQuery = patternBracketsOutsideQuotes.matcher(whereQuery).replaceAll("");
 		String[] splittedQueries = patternBooleans.split(skeletonQuery);
 		
 		int index = -1;
 		int colonIndex = -1;
 		int totalQueries = 0;
 		String fieldName = "";
 		String fieldQuery = "";
 
 		for (String splittedQuery : splittedQueries) {
 			splittedQuery = splittedQuery.trim();
 			index = whereQuery.indexOf(splittedQuery);
 			String queryId = "q" + totalQueries++; 
 			whereQuery = whereQuery.substring(0, index) + queryId +  whereQuery.substring(index + splittedQuery.length());
 			colonIndex = splittedQuery.indexOf(':');
 			if ( -1 == colonIndex) {
 				throw new IOException("Invalid query fyntax. Expecting FIELD:VALUE and not " + splittedQuery);
 			}
 			fieldName = splittedQuery.substring(0,colonIndex);
 			fieldQuery = "*|" + splittedQuery.substring(colonIndex + 1,splittedQuery.length());
 			QueryPart qpart = new QueryPart(mergeId + "_" + fieldName);
 			qpart.setParam("query", fieldQuery);
 			qpart.setParam("allwords", this.checkForAllWords);
 			queryDetails.put(queryId, qpart);
 		}
 		return whereQuery;
 	}
 
 
 	public final Map<String, List<HsearchFacet>> pivotFacet(final String dataRepository, final String mergeId, final String pivotFacetFields, final String facetQuery, final KVRowI aBlankrow) throws IOException, ParseException, InterruptedException, ExecutionException, FederatedSearchException{
 		String[] pivotFacets = pivotFacetFields.split("\\|");
 		pivotFacetsMap = new HashMap<String, List<HsearchFacet>>();
 		IEnricher enricher = null;
 		for (String aPivotFacet : pivotFacets) {
 			String[] fields = patternComma.split(aPivotFacet);
 			search(dataRepository, mergeId, aPivotFacet, facetQuery, aBlankrow, enricher);
 			Set<KVRowI> result = getResult();
 			HsearchFacet root = new HsearchFacet("root", new TypedObject("root"), new ArrayList<HsearchFacet>());
 			HsearchFacet current = root;
 			for (KVRowI kvRowI : result) {
 				for (String aField : fields) {
 					current = current.getChild(aField, kvRowI.getValueNative(aField));				
 				}
 		        current = root;			
 			}
 			this.pivotFacetsMap.put(aPivotFacet, root.getinternalFacets());
 			resultset.clear();
 		}
 		
 		return pivotFacetsMap;
 	}
 	
 }
