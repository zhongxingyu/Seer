 package net.canadensys.dataportal.vascan.dao.impl;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import net.canadensys.dataportal.vascan.dao.NameDAO;
 import net.canadensys.dataportal.vascan.model.NameConceptModelIF;
 import net.canadensys.dataportal.vascan.model.NameConceptTaxonModel;
 import net.canadensys.dataportal.vascan.model.NameConceptVernacularNameModel;
 import net.canadensys.dataportal.vascan.model.TaxonLookupModel;
 import net.canadensys.query.LimitedResult;
 
 import org.apache.commons.lang3.StringUtils;
 import org.elasticsearch.action.index.IndexResponse;
 import org.elasticsearch.action.search.SearchRequestBuilder;
 import org.elasticsearch.action.search.SearchResponse;
 import org.elasticsearch.action.search.SearchType;
 import org.elasticsearch.client.Client;
 import org.elasticsearch.index.query.QueryBuilders;
 import org.elasticsearch.search.SearchHit;
 import org.elasticsearch.search.SearchHits;
 import org.elasticsearch.search.sort.SortBuilders;
 import org.elasticsearch.search.sort.SortOrder;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Repository;
 
 /**
  * Optional feature implementation of the data access layer.
  * Add ElasticSearch to your classpath to enable it.
  * @author canadensys
  *
  */
 @Repository("nameDAO")
 public class ElasticSearchNameDAO implements NameDAO{
 	
 	private static final String INDEX_NAME = "vascan";
 	
 	private static final String TAXON_TYPE = "taxon";
 	private static final String VERNACULAR_TYPE = "vernacular";
 	
 	private static final String TAXON_NAME_FIELD = "taxonname";
 	private static final String VERNACULAR_NAME_FIELD = "vernacularname";
 	
 	private static final String TAXON_NAME_NGRAM_FIELD = "taxonname.ngrams";
 	private static final String VERNACULAR_NAME_NGRAM_FIELD = "vernacularname.ngrams";
 	
 	private static final int DEFAULT_PAGE_SIZE = 50;
 	private int pageSize = DEFAULT_PAGE_SIZE;
 	
 	@Autowired
 	@Qualifier("esClient")
 	private Client client;
 
 	@Override
 	public LimitedResult<List<NameConceptModelIF>> search(String text){
 		SearchRequestBuilder srb = buildSearchRequestBuilderFromText(text,pageSize);
 		SearchResponse response = srb
 		        .execute()
 		        .actionGet();
 		LimitedResult<List<NameConceptModelIF>> qr = new LimitedResult<List<NameConceptModelIF>>();
 		qr.setRows(searchHitsToNameModelList(response.getHits()));
 		qr.setTotal_rows(response.getHits().getTotalHits());
 		return qr;
 	}
 	
 	@Override
 	public LimitedResult<List<NameConceptModelIF>> search(String text, int pageNumber) {
 		SearchRequestBuilder srb = buildSearchRequestBuilderFromText(text,pageSize);
 		srb.setFrom(pageNumber*pageSize);
 		
 		SearchResponse response = srb
 		        .execute()
 		        .actionGet();
 		LimitedResult<List<NameConceptModelIF>> qr = new LimitedResult<List<NameConceptModelIF>>();
 		qr.setRows(searchHitsToNameModelList(response.getHits()));
 		qr.setTotal_rows(response.getHits().getTotalHits());
 		return qr;
 	}
 	
 	@Override
 	public List<NameConceptModelIF> searchTaxon(String text) {
 		SearchRequestBuilder srb = client.prepareSearch(INDEX_NAME)
 		        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
 		        .setQuery(QueryBuilders
 	                .boolQuery()
 	                .should(QueryBuilders.matchQuery(TAXON_NAME_FIELD,text))
 	                .should(QueryBuilders.matchQuery(TAXON_NAME_NGRAM_FIELD,text)))
 	            .setSize(pageSize)
 	            .addSort(SortBuilders.scoreSort())
 	            .addSort(SortBuilders.fieldSort(TAXON_NAME_FIELD).order(SortOrder.ASC))
 	            .setTypes(TAXON_TYPE);
 
 		SearchResponse response = srb
 		        .execute()
 		        .actionGet();
 		return searchHitsToNameModelList(response.getHits());
 	}
 	
 	@Override
 	public List<NameConceptModelIF> searchVernacular(String text) {
 		SearchRequestBuilder srb = client.prepareSearch(INDEX_NAME)
 		        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
 		        .setQuery(QueryBuilders
 	                .boolQuery()
 	                .should(QueryBuilders.matchQuery(VERNACULAR_NAME_FIELD,text))
	                .should(QueryBuilders.matchQuery(VERNACULAR_NAME_NGRAM_FIELD,text)))
 	            .setSize(pageSize)
 	            .addSort(SortBuilders.scoreSort())
 	            .addSort(SortBuilders.fieldSort(VERNACULAR_NAME_FIELD).order(SortOrder.ASC))
 	            .setTypes(VERNACULAR_TYPE);
 
 		SearchResponse response = srb
 		        .execute()
 		        .actionGet();
 		return searchHitsToNameModelList(response.getHits());
 	}
 	
 	/**
 	 * Build a bool query using the name and name.ngrams fields.
 	 * SHOULD match the name
 	 * SHOULD match the name.ngrams
 	 * Sorted by score then name (maybe it should be provided by the caller?)
 	 * @param text
 	 * @param pageSize
 	 * @return
 	 */
 	private SearchRequestBuilder buildSearchRequestBuilderFromText(String text, int pageSize){
 		return client.prepareSearch(INDEX_NAME)
 		        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
 		        .setQuery(QueryBuilders
 	                .boolQuery()
 	                .should(QueryBuilders.multiMatchQuery(text, TAXON_NAME_FIELD,VERNACULAR_NAME_FIELD))
	                .should(QueryBuilders.multiMatchQuery(text, TAXON_NAME_NGRAM_FIELD,VERNACULAR_NAME_NGRAM_FIELD)))
 	                .setSize(pageSize)
 	                .addSort(SortBuilders.scoreSort())
 	                .addSort(SortBuilders.fieldSort(TAXON_NAME_FIELD).order(SortOrder.ASC))
 	                .addSort(SortBuilders.fieldSort(VERNACULAR_NAME_FIELD).order(SortOrder.ASC));
 	}
 	
 	/**
 	 * Creates a List<NameModel> from ES SearchHits object.
 	 * @param hits
 	 * @return
 	 */
 	private List<NameConceptModelIF>searchHitsToNameModelList(SearchHits hits){
 		List<NameConceptModelIF> newNameModelList = new ArrayList<NameConceptModelIF>();
 		NameConceptTaxonModel tNameModel;
 		NameConceptVernacularNameModel vNameModel;
 		Map<String,Object> esHitData = null;
 		for(SearchHit currHit: hits.hits()){
 			esHitData = currHit.sourceAsMap();
 			if(currHit.getType().equalsIgnoreCase(TAXON_TYPE)){
 				tNameModel = new NameConceptTaxonModel();
 				tNameModel.setTaxonId(Integer.valueOf(currHit.getId()));
 				tNameModel.setName((String)esHitData.get("taxonname"));
 				tNameModel.setStatus((String)esHitData.get("status"));
 				tNameModel.setNamehtml((String)esHitData.get("namehtml"));
 				tNameModel.setNamehtmlauthor((String)esHitData.get("namehtmlauthor"));
 				tNameModel.setRankname((String)esHitData.get("rankname"));
 				tNameModel.setParentid((Integer)esHitData.get("parentid"));
 				tNameModel.setParentnamehtml((String)esHitData.get("parentnamehtml"));
 				newNameModelList.add(tNameModel);
 			}
 			else if(currHit.getType().equalsIgnoreCase(VERNACULAR_TYPE)){
 				vNameModel = new NameConceptVernacularNameModel();
 				vNameModel.setId(Integer.valueOf(currHit.getId()));
 				vNameModel.setName((String)esHitData.get("vernacularname"));
 				vNameModel.setStatus((String)esHitData.get("status"));
 				vNameModel.setLang((String)esHitData.get("language"));
 				vNameModel.setTaxonId((Integer)esHitData.get("taxonid"));
 				vNameModel.setTaxonnamehtml((String)esHitData.get("taxonnamehtml"));
 				newNameModelList.add(vNameModel);
 			}
 		}
 		return newNameModelList;
 	}
 	
 	/**
 	 * Index a taxon
 	 * @param tlm
 	 * @return
 	 */
 	public boolean indexTaxon(TaxonLookupModel tlm){
 		Map<String,Object> sourceData = new HashMap<String,Object>();
 		sourceData.put("name", tlm.getCalname());
 		sourceData.put("status", tlm.getStatus());
 		sourceData.put("rank", tlm.getRank());
 		IndexResponse response = client.prepareIndex(INDEX_NAME, TAXON_TYPE)
 				.setSource(sourceData)
 				.setId(Integer.toString(tlm.getTaxonId()))
 		        .execute()
 		        .actionGet();
 		return (StringUtils.equals(response.getId(),Integer.toString(tlm.getTaxonId())));
 	}
 	
 	@Override
 	public void setPageSize(int pageSize) {
 		this.pageSize = pageSize;
 	}
 	@Override
 	public int getPageSize() {
 		return pageSize;
 	}
 
 	public Client getClient() {
 		return client;
 	}
 	public void setClient(Client client) {
 		this.client = client;
 	}
 	
 }
