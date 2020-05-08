 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.apache.solr.handler.component;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Deque;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.TermQuery;
 import org.apache.lucene.util.BytesRef;
 import org.apache.solr.common.SolrException;
 import org.apache.solr.common.SolrException.ErrorCode;
 import org.apache.solr.common.params.FacetParams;
 import org.apache.solr.common.params.ModifiableSolrParams;
 import org.apache.solr.common.params.SolrParams;
 import org.apache.solr.common.util.NamedList;
 import org.apache.solr.common.util.SimpleOrderedMap;
 import org.apache.solr.request.SimpleFacets;
 import org.apache.solr.request.SolrQueryRequest;
 import org.apache.solr.schema.FieldType;
 import org.apache.solr.schema.SchemaField;
 import org.apache.solr.search.DocSet;
 import org.apache.solr.search.SolrIndexSearcher;
 import org.apache.solr.util.NamedListHelper;
 import org.apache.solr.util.PivotListEntry;
 
 /**
  * This is thread safe
  * 
  * @since solr 4.0
  */
 public class PivotFacetHelper {
   
   protected NamedListHelper namedListHelper = NamedListHelper.INSTANCE;
   
   protected Comparator<NamedList<Object>> namedListCountComparator = new PivotNamedListCountComparator();
   
   /**
    * Designed to be overridden by subclasses that provide different faceting
    * implementations. TODO: Currently this is returning a SimpleFacets object,
    * but those capabilities would be better as an extracted abstract class or
    * interface.
    */
   protected SimpleFacets getFacetImplementation(SolrQueryRequest req,
       DocSet docs, SolrParams params) {
     return new SimpleFacets(req, docs, params);
   }
   
   public SimpleOrderedMap<List<NamedList<Object>>> process(ResponseBuilder rb,
       SolrParams params, String[] pivots) throws IOException {
     if (!rb.doFacets || pivots == null) return null;
     
     int minMatch = params.getInt(FacetParams.FACET_PIVOT_MINCOUNT, 1);
     boolean distinct = params.getBool( FacetParams.FACET_PIVOT_DISTINCT, false);  // distinct pivot?
     boolean showDistinctCounts = params.getBool( FacetParams.FACET_PIVOT_DISTINCT, false);
     if (showDistinctCounts) {
       // force values in facet query to default values when facet.pivot.distinct = true
       // facet.mincount = 1 ---- distinct count makes no sense if we filter out valid terms
       // facet.limit = -1   ---- distinct count makes no sense if we limit terms
       ModifiableSolrParams v = new ModifiableSolrParams(rb.req.getParams());
       v.set("facet.mincount", 1);
       v.set("facet.limit", -1);
       params = v;
       rb.req.setParams(params);
     }
     
     SimpleOrderedMap<List<NamedList<Object>>> pivotResponse = new SimpleOrderedMap<List<NamedList<Object>>>();
     for (String pivot : pivots) {
       String[] fields = pivot.split(","); // only support two levels for now
       int depth = fields.length;
       
       if (fields.length < 1) {
         throw new SolrException(ErrorCode.BAD_REQUEST,
             "Pivot Facet needs at least one field: " + pivot);
       }
       
       DocSet docs = rb.getResults().docSet;
       String field = fields[0];      
       Deque<String> fnames = new LinkedList<String>();
       for (int i = fields.length - 1; i > 1; i--) {
         fnames.push(fields[i]);
       }
       
       SimpleFacets sf = getFacetImplementation(rb.req, rb.getResults().docSet, params);
       NamedList<Integer> superFacets = sf.getTermCounts(field);
       
       if(fields.length > 1) {
     	  String subField = fields[1];
     	  pivotResponse.add(pivot,
     	          doPivots(superFacets, field, subField, fnames, rb, docs, minMatch, distinct, depth, depth));  
       }
       else {
     	  pivotResponse.add(pivot,
     	      doPivots(superFacets,field,null,fnames,rb,docs, minMatch, distinct, depth, depth));
       }
       
     }
     return pivotResponse;
   }
   
   /**
    * Recursive function to do all the pivots
    */
   protected List<NamedList<Object>> doPivots( NamedList<Integer> superFacets, String field, String subField, Deque<String> fnames, ResponseBuilder rb, DocSet docs, int minMatch, boolean distinct, int maxDepth, int depth) throws IOException
   {    
     SolrIndexSearcher searcher = rb.req.getSearcher();
     // TODO: optimize to avoid converting to an external string and then having to convert back to internal below
     SchemaField sfield = searcher.getSchema().getField(field);
     FieldType ftype = sfield.getType();
 
     String nextField = fnames.poll();
     
     // when distinct and no subs, dont bother
     if (subField == null && distinct == true) {
       return new ArrayList<NamedList<Object>>();
     }
     
     Query baseQuery = rb.getQuery();
     
     List<NamedList<Object>> values = new ArrayList<NamedList<Object>>( superFacets.size() );
     
     for (Map.Entry<String, Integer> kv : superFacets) {
       // Only sub-facet if parent facet has positive count - still may not be any values for the sub-field though
       if (kv.getValue() >= minMatch ) {
         // don't reuse the same BytesRef  each time since we will be constructing Term
         // objects that will most likely be cached.
         BytesRef termval = new BytesRef();
         ftype.readableToIndexed(kv.getKey(), termval);
         
         SimpleOrderedMap<Object> pivot = new SimpleOrderedMap<Object>();
         pivot.add("field", field );
         pivot.add("value", ftype.toObject(sfield, termval) );
         pivot.add("count", kv.getValue() );
         
         // only due stats
         DocSet subset = null;
         SimpleFacets sf = null;
         
         if (maxDepth != depth) {
           Query query = new TermQuery(new Term(field, termval));
           subset = searcher.getDocSet(query, docs);
           sf = getFacetImplementation(rb.req, subset, rb.req.getParams());
           NamedList<Object> subFieldStats = sf.getFacetPercentileCounts();
          // if (subFieldStats != null && subFieldStats.size() > 0) {
         	  pivot.add( "statistics", subFieldStats);
          // }
         }
         
                       
         if( subField == null) {          
           if (distinct == false) {
             values.add( pivot );
           }
         }
         else {
           
           if (sf == null) {
             Query query = new TermQuery(new Term(field, termval));
             subset = searcher.getDocSet(query, docs);
             sf = getFacetImplementation(rb.req, subset, rb.req.getParams());
             NamedList<Object> subFieldStats = sf.getFacetPercentileCounts();
            // if (subFieldStats != null && subFieldStats.size() > 0) {
             	pivot.add( "statistics", subFieldStats);
            // }
           }
           
           NamedList<Integer> nl = sf.getTermCounts(subField);
           if (distinct) {
             pivot.add("distinct", nl.size());
             if (depth > 1) {
               List<NamedList<Object>> list = doPivots( nl, subField, nextField, fnames, rb, subset, minMatch, distinct, maxDepth, depth-1 );
              // if (list.size() > 0) {
             	  pivot.add( "pivot", list);
              // }
               values.add( pivot );
             }
           } else {
             if (nl.size() >= minMatch) {
               List<NamedList<Object>> list = doPivots( nl, subField, nextField, fnames, rb, subset, minMatch, distinct, maxDepth, depth-1 );
              // if (list.size() > 0) {
             	pivot.add( "pivot",  list);
              // }
               values.add( pivot );
             }
           }
         }
         
       }
     }
     
     // put the field back on the list
     fnames.push( nextField );
     return values;
   }
   private void mergeValueToMap(Map<Object,NamedList<Object>> polecatCounts,
 		  String field, Object value, Integer count,
 		  List<NamedList<Object>> subPivot, int pivotsDone, int numberOfPivots) {
 	  if (polecatCounts.containsKey(value)) {
 		  polecatCounts.put(
 				  value,
 				  mergePivots(polecatCounts.get(value), count, subPivot, pivotsDone,
 						  numberOfPivots));
 	  } else {
 		  SimpleOrderedMap<Object> pivot = new SimpleOrderedMap<Object>();
 		  pivot.add(PivotListEntry.FIELD.getName(), field);
 		  pivot.add(PivotListEntry.VALUE.getName(), value);
 		  pivot.add(PivotListEntry.COUNT.getName(), count);
 		  if (subPivot != null) {
 			  pivot.add(PivotListEntry.PIVOT.getName(),
 					  convertPivotsToMaps(subPivot, pivotsDone, numberOfPivots));
 		  }
 		  polecatCounts.put(value, pivot);
 	  }
   }
 
   private NamedList<Object> mergePivots(NamedList<Object> existingNamedList,
 		  Integer countToMerge, List<NamedList<Object>> pivotToMergeList,
 		  int pivotsDone, int numberOfPivots) {
 	  if (countToMerge != null) {
 		  // Cast here, but as we're only putting Integers in above it should be
 		  // fine
 		  existingNamedList.setVal(
 				  PivotListEntry.COUNT.getIndex(),
 				  ((Integer) namedListHelper.getFromPivotList(PivotListEntry.COUNT,
 						  existingNamedList)) + countToMerge);
 	  }
 	  if (pivotToMergeList != null) {
 		  Object existingPivotObj = namedListHelper.getFromPivotList(
 				  PivotListEntry.PIVOT, existingNamedList);
 		  if (existingPivotObj instanceof Map) {
 			  for (NamedList<Object> pivotToMerge : pivotToMergeList) {
 				  String nextFieldToMerge = (String) namedListHelper.getFromPivotList(
 						  PivotListEntry.FIELD, pivotToMerge);
 				  Object nextValueToMerge = namedListHelper.getFromPivotList(
 						  PivotListEntry.VALUE, pivotToMerge);
 				  Integer nextCountToMerge = (Integer) namedListHelper
 						  .getFromPivotList(PivotListEntry.COUNT, pivotToMerge);
 				  Object nextPivotToMergeListObj = namedListHelper.getFromPivotList(
 						  PivotListEntry.PIVOT, pivotToMerge);
 				  List nextPivotToMergeList = null;
 				  if (nextPivotToMergeListObj instanceof List) {
 					  nextPivotToMergeList = (List) nextPivotToMergeListObj;
 				  }
 				  mergeValueToMap((Map) existingPivotObj, nextFieldToMerge,
 						  nextValueToMerge, nextCountToMerge, nextPivotToMergeList,
 						  pivotsDone++, numberOfPivots);
 			  }
 		  } else {
 			  existingNamedList.add(
 					  PivotListEntry.PIVOT.getName(),
 					  convertPivotsToMaps(pivotToMergeList, pivotsDone + 1,
 							  numberOfPivots));
 		  }
 	  }
 	  return existingNamedList;
   }
 
   public Map<Object,NamedList<Object>> convertPivotsToMaps(
 		  List<NamedList<Object>> pivots, int pivotsDone, int numberOfPivots) {
 	  return convertPivotsToMaps(pivots, pivotsDone, numberOfPivots, null);
   }
 
   public Map<Object,NamedList<Object>> convertPivotsToMaps(
 		  List<NamedList<Object>> pivots, int pivotsDone, int numberOfPivots,
 		  Map<Integer,Map<Object,Integer>> fieldCounts) {
 	  Map<Object,NamedList<Object>> pivotMap = new HashMap<Object,NamedList<Object>>();
 	  boolean countFields = (fieldCounts != null);
 	  Map<Object,Integer> thisFieldCountMap = null;
 	  if (countFields) {
 		  thisFieldCountMap = getFieldCountMap(fieldCounts, pivotsDone);
 	  }
 	  for (NamedList<Object> pivot : pivots) {
 		  Object valueObj = namedListHelper.getFromPivotList(PivotListEntry.VALUE,
 				  pivot);
 		  pivotMap.put(valueObj, pivot);
 		  if (countFields) {
 			  Object countObj = namedListHelper.getFromPivotList(
 					  PivotListEntry.COUNT, pivot);
 			  int count = 0;
 			  if (countObj instanceof Integer) {
 				  count = (Integer) countObj;
 			  }
 			  addFieldCounts(valueObj, count, thisFieldCountMap);
 		  }
 		  if (pivotsDone < numberOfPivots) {
 			  Integer pivotIdx = pivot.indexOf(PivotListEntry.PIVOT.getName(), 0);
 			  if (pivotIdx > -1) {
 				  Object pivotObj = pivot.getVal(pivotIdx);
 				  if (pivotObj instanceof List) {
 					  pivot.setVal(
 							  pivotIdx,
 							  convertPivotsToMaps((List) pivotObj, pivotsDone + 1,
 									  numberOfPivots, fieldCounts));
 				  }
 			  }
 		  }
 	  }
 	  return pivotMap;
   }
 
   public List<NamedList<Object>> convertPivotMapToList(
 		  Map<Object,NamedList<Object>> pivotMap, int numberOfPivots) {
 	  return convertPivotMapToList(pivotMap, new InternalPivotLimitInfo(), 0,
 			  numberOfPivots, false);
   }
 
   private List<NamedList<Object>> convertPivotMapToList(
 		  Map<Object,NamedList<Object>> pivotMap,
 		  InternalPivotLimitInfo pivotLimitInfo, int currentPivot,
 		  int numberOfPivots, boolean sortByCount) {
 	  List<NamedList<Object>> pivots = new ArrayList<NamedList<Object>>();
 	  currentPivot++;
 	  List<Object> fieldLimits = null;
 	  InternalPivotLimitInfo nextPivotLimitInfo = new InternalPivotLimitInfo(
 			  pivotLimitInfo);
 	  if (pivotLimitInfo.combinedPivotLimit
 			  && pivotLimitInfo.fieldLimitsList.size() > 0) {
 		  fieldLimits = pivotLimitInfo.fieldLimitsList.get(0);
 		  nextPivotLimitInfo.fieldLimitsList = pivotLimitInfo.fieldLimitsList
 				  .subList(1, pivotLimitInfo.fieldLimitsList.size());
 	  }
 	  for (Entry<Object,NamedList<Object>> pivot : pivotMap.entrySet()) {
 		  if (pivotLimitInfo.limit == 0 || !pivotLimitInfo.combinedPivotLimit
 				  || fieldLimits == null || fieldLimits.contains(pivot.getKey())) {
 			  pivots.add(pivot.getValue());
 			  convertPivotEntryToListType(pivot.getValue(), nextPivotLimitInfo,
 					  currentPivot, numberOfPivots, sortByCount);
 		  }
 	  }
 	  if (sortByCount) {
 		  Collections.sort(pivots, namedListCountComparator);
 	  }
 	  if (!pivotLimitInfo.combinedPivotLimit && pivotLimitInfo.limit > 0
 			  && pivots.size() > pivotLimitInfo.limit) {
 		  pivots = new ArrayList<NamedList<Object>>(pivots.subList(0,
 				  pivotLimitInfo.limit));
 	  }
 	  return pivots;
   }
 
   public SimpleOrderedMap<List<NamedList<Object>>> convertPivotMapsToList(
 		  SimpleOrderedMap<Map<Object,NamedList<Object>>> pivotValues,
 		  PivotLimitInfo pivotLimitInfo, boolean sortByCount) {
 	  SimpleOrderedMap<List<NamedList<Object>>> pivotsLists = new SimpleOrderedMap<List<NamedList<Object>>>();
 	  for (Entry<String,Map<Object,NamedList<Object>>> pivotMapEntry : pivotValues) {
 		  String pivotName = pivotMapEntry.getKey();
 		  Integer numberOfPivots = 1 + StringUtils.countMatches(pivotName, ",");
 		  InternalPivotLimitInfo internalPivotLimitInfo = new InternalPivotLimitInfo(
 				  pivotLimitInfo, pivotName);
 		  pivotsLists.add(
 				  pivotName,
 				  convertPivotMapToList(pivotMapEntry.getValue(),
 						  internalPivotLimitInfo, 0, numberOfPivots, sortByCount));
 	  }
 	  return pivotsLists;
   }
 
   private void convertPivotEntryToListType(NamedList<Object> pivotEntry,
 		  InternalPivotLimitInfo pivotLimitInfo, int pivotsDone,
 		  int numberOfPivots, boolean sortByCount) {
 	  if (pivotsDone < numberOfPivots) {
 		  int pivotIdx = pivotEntry.indexOf(PivotListEntry.PIVOT.getName(), 0);
 		  if (pivotIdx > -1) {
 			  Object subPivotObj = pivotEntry.getVal(pivotIdx);
 			  if (subPivotObj instanceof Map) {
 				  Map<Object,NamedList<Object>> subPivotMap = (Map) subPivotObj;
 				  pivotEntry.setVal(
 						  pivotIdx,
 						  convertPivotMapToList(subPivotMap, pivotLimitInfo, pivotsDone,
 								  numberOfPivots, sortByCount));
 			  }
 		  }
 	  }
   }
 
   public Map<Object,Integer> getFieldCountMap(
 		  Map<Integer,Map<Object,Integer>> fieldCounts, int pivotNumber) {
 	  Map<Object,Integer> fieldCountMap = fieldCounts.get(pivotNumber);
 	  if (fieldCountMap == null) {
 		  fieldCountMap = new HashMap<Object,Integer>();
 		  fieldCounts.put(pivotNumber, fieldCountMap);
 	  }
 	  return fieldCountMap;
   }
 
   public void addFieldCounts(Object name, int count,
 		  Map<Object,Integer> thisFieldCountMap) {
 	  Integer existingFieldCount = thisFieldCountMap.get(name);
 	  if (existingFieldCount == null) {
 		  thisFieldCountMap.put(name, count);
 	  } else {
 		  thisFieldCountMap.put(name, existingFieldCount + count);
 	  }
   }
 
   public static class PivotLimitInfo {
 
 	  public SimpleOrderedMap<List<List<Object>>> fieldLimitsMap = null;
 
 	  public int limit = 0;
 
 	  public boolean combinedPivotLimit = false;
   }
 
   private static class InternalPivotLimitInfo {
 
 	  public List<List<Object>> fieldLimitsList = null;
 
 	  public int limit = 0;
 
 	  public boolean combinedPivotLimit = false;
 
 	  private InternalPivotLimitInfo() {}
 
 	  private InternalPivotLimitInfo(PivotLimitInfo pivotLimitInfo,
 			  String pivotName) {
 		  this.limit = pivotLimitInfo.limit;
 		  this.combinedPivotLimit = pivotLimitInfo.combinedPivotLimit;
 		  if (pivotLimitInfo.fieldLimitsMap != null) {
 			  this.fieldLimitsList = pivotLimitInfo.fieldLimitsMap.get(pivotName);
 		  }
 	  }
 
 	  private InternalPivotLimitInfo(InternalPivotLimitInfo pivotLimitInfo) {
 		  this.fieldLimitsList = pivotLimitInfo.fieldLimitsList;
 		  this.limit = pivotLimitInfo.limit;
 		  this.combinedPivotLimit = pivotLimitInfo.combinedPivotLimit;
 	  }
   }
 // TODO: This is code from various patches to support distributed search.
 //  Some parts may be helpful for whoever implements distributed search.
 //
 //  @Override
 //  public int distributedProcess(ResponseBuilder rb) throws IOException {
 //    if (!rb.doFacets) {
 //      return ResponseBuilder.STAGE_DONE;
 //    }
 //
 //    if (rb.stage == ResponseBuilder.STAGE_GET_FIELDS) {
 //      SolrParams params = rb.req.getParams();
 //      String[] pivots = params.getParams(FacetParams.FACET_PIVOT);
 //      for ( ShardRequest sreq : rb.outgoing ) {
 //        if (( sreq.purpose & ShardRequest.PURPOSE_GET_FIELDS ) != 0
 //            && sreq.shards != null && sreq.shards.length == 1 ) {
 //          sreq.params.set( FacetParams.FACET, "true" );
 //          sreq.params.set( FacetParams.FACET_PIVOT, pivots );
 //          sreq.params.set( FacetParams.FACET_PIVOT_MINCOUNT, 1 ); // keep this at 1 regardless so that it accumulates everything
 //            }
 //      }
 //    }
 //    return ResponseBuilder.STAGE_DONE;
 //  }
 //
 //  @Override
 //  public void handleResponses(ResponseBuilder rb, ShardRequest sreq) {
 //    if (!rb.doFacets) return;
 //
 //
 //    if ((sreq.purpose & ShardRequest.PURPOSE_GET_FACETS)!=0) {
 //      SimpleOrderedMap<List<NamedList<Object>>> tf = rb._pivots;
 //      if ( null == tf ) {
 //        tf = new SimpleOrderedMap<List<NamedList<Object>>>();
 //        rb._pivots = tf;
 //      }
 //      for (ShardResponse srsp: sreq.responses) {
 //        int shardNum = rb.getShardNum(srsp.getShard());
 //
 //        NamedList facet_counts = (NamedList)srsp.getSolrResponse().getResponse().get("facet_counts");
 //
 //        // handle facet trees from shards
 //        SimpleOrderedMap<List<NamedList<Object>>> shard_pivots = 
 //          (SimpleOrderedMap<List<NamedList<Object>>>)facet_counts.get( PIVOT_KEY );
 //        
 //        if ( shard_pivots != null ) {
 //          for (int j=0; j< shard_pivots.size(); j++) {
 //            // TODO -- accumulate the results from each shard
 //            // The following code worked to accumulate facets for an previous 
 //            // two level patch... it is here for reference till someone can upgrade
 //            /**
 //            String shard_tree_name = (String) shard_pivots.getName( j );
 //            SimpleOrderedMap<NamedList> shard_tree = (SimpleOrderedMap<NamedList>)shard_pivots.getVal( j );
 //            SimpleOrderedMap<NamedList> facet_tree = tf.get( shard_tree_name );
 //            if ( null == facet_tree) { 
 //              facet_tree = new SimpleOrderedMap<NamedList>(); 
 //              tf.add( shard_tree_name, facet_tree );
 //            }
 //
 //            for( int o = 0; o < shard_tree.size() ; o++ ) {
 //              String shard_outer = (String) shard_tree.getName( o );
 //              NamedList shard_innerList = (NamedList) shard_tree.getVal( o );
 //              NamedList tree_innerList  = (NamedList) facet_tree.get( shard_outer );
 //              if ( null == tree_innerList ) { 
 //                tree_innerList = new NamedList();
 //                facet_tree.add( shard_outer, tree_innerList );
 //              }
 //
 //              for ( int i = 0 ; i < shard_innerList.size() ; i++ ) {
 //                String shard_term = (String) shard_innerList.getName( i );
 //                long shard_count  = ((Number) shard_innerList.getVal(i)).longValue();
 //                int tree_idx      = tree_innerList.indexOf( shard_term, 0 );
 //
 //                if ( -1 == tree_idx ) {
 //                  tree_innerList.add( shard_term, shard_count );
 //                } else {
 //                  long tree_count = ((Number) tree_innerList.getVal( tree_idx )).longValue();
 //                  tree_innerList.setVal( tree_idx, shard_count + tree_count );
 //                }
 //              } // innerList loop
 //            } // outer loop
 //              **/
 //          } // each tree loop
 //        }
 //      }
 //    } 
 //    return ;
 //  }
 //
 //  @Override
 //  public void finishStage(ResponseBuilder rb) {
 //    if (!rb.doFacets || rb.stage != ResponseBuilder.STAGE_GET_FIELDS) return;
 //    // wait until STAGE_GET_FIELDS
 //    // so that "result" is already stored in the response (for aesthetics)
 //
 //    SimpleOrderedMap<List<NamedList<Object>>> tf = rb._pivots;
 //
 //    // get 'facet_counts' from the response
 //    NamedList facetCounts = (NamedList) rb.rsp.getValues().get("facet_counts");
 //    if (facetCounts == null) {
 //      facetCounts = new NamedList();
 //      rb.rsp.add("facet_counts", facetCounts);
 //    }
 //    facetCounts.add( PIVOT_KEY, tf );
 //    rb._pivots = null;
 //  }
 //
 //  public String getDescription() {
 //    return "Handle Pivot (multi-level) Faceting";
 //  }
 //
 //  public String getSource() {
 //    return "$URL: http://svn.apache.org/repos/asf/lucene/dev/branches/branch_4x/solr/core/src/java/org/apache/solr/handler/component/PivotFacetHelper.java $";
 //  }
 }
