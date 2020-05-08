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
 	
 	private static final int DEFAULT_PAGE_SIZE = 20;
 	private int pageSize = DEFAULT_PAGE_SIZE;
 	
 	@Autowired
 	@Qualifier("esClient")
 	private Client client;
 
 	@Override
 	public LimitedResult<List<NameConceptModelIF>> search(String text) {
 		SearchRequestBuilder srb = client.prepareSearch(INDEX_NAME)
 		        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
 		        .setQuery(QueryBuilders.matchQuery("name.ngrams", text))
 		        .setSize(pageSize);
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
 		        .setTypes(TAXON_TYPE)
 		        .setQuery(QueryBuilders.matchQuery("name.ngrams", text))
 		        .setSize(pageSize);
 		SearchResponse response = srb
 		        .execute()
 		        .actionGet();
 		return searchHitsToNameModelList(response.getHits());
 	}
 	
 	@Override
 	public List<NameConceptModelIF> searchVernacular(String text) {
 		SearchRequestBuilder srb = client.prepareSearch(INDEX_NAME)
 		        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
 		        .setTypes(VERNACULAR_TYPE)
 		        .setQuery(QueryBuilders.matchQuery("name.ngrams", text))
 		        .setSize(pageSize);
 		SearchResponse response = srb
 		        .execute()
 		        .actionGet();
 		return searchHitsToNameModelList(response.getHits());
 	}
 	
 	@Override
 	public List<NameConceptModelIF> search(String text, int pageNumber) {
 		
 		SearchRequestBuilder srb = client.prepareSearch(INDEX_NAME)
 		        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
 		        .setFrom(pageNumber)
 		        .setSize(pageSize)
 		        .setQuery(QueryBuilders.matchQuery("name.ngrams", text));
 		SearchResponse response = srb
 		        .execute()
 		        .actionGet();
 		return searchHitsToNameModelList(response.getHits());
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
 		for(SearchHit currHit: hits.hits()){
 			if(currHit.getType().equalsIgnoreCase(TAXON_TYPE)){
 				tNameModel = new NameConceptTaxonModel();
 				tNameModel.setTaxonId(Integer.parseInt(currHit.getId()));
 				tNameModel.setName((String)currHit.sourceAsMap().get("name"));
 				tNameModel.setStatus((String)currHit.sourceAsMap().get("status"));
 				tNameModel.setNamehtml((String)currHit.sourceAsMap().get("namehtml"));
 				tNameModel.setNamehtmlauthor((String)currHit.sourceAsMap().get("namehtmlauthor"));
 				newNameModelList.add(tNameModel);
 			}
 			else if(currHit.getType().equalsIgnoreCase(VERNACULAR_TYPE)){
 				vNameModel = new NameConceptVernacularNameModel();
				vNameModel.setId(Integer.parseInt(currHit.getId()));
 				vNameModel.setTaxonId((Integer)currHit.sourceAsMap().get("taxonid"));
 				vNameModel.setName((String)currHit.sourceAsMap().get("name"));
 				vNameModel.setStatus((String)currHit.sourceAsMap().get("status"));
 				vNameModel.setLang((String)currHit.sourceAsMap().get("language"));
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
 
 	public Client getClient() {
 		return client;
 	}
 	public void setClient(Client client) {
 		this.client = client;
 	}
 	
 }
