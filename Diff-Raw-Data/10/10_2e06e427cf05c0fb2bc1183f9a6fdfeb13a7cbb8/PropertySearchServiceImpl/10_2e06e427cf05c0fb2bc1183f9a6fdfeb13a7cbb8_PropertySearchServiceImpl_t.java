 package com.rightmove.es.service.impl;
 
 import com.rightmove.es.domain.Property;
 import com.rightmove.es.domain.PropertyQueryParams;
 import com.rightmove.es.domain.PropertySearchResult;
 import com.rightmove.es.service.PropertySearchService;
 import org.apache.commons.lang.StringUtils;
 import org.elasticsearch.client.Client;
 import org.elasticsearch.index.query.FilterBuilder;
 import org.elasticsearch.index.query.FilterBuilders;
 import org.elasticsearch.index.query.QueryBuilders;
 import org.elasticsearch.index.query.TermFilterBuilder;
 import org.elasticsearch.search.sort.FieldSortBuilder;
 import org.elasticsearch.search.sort.SortOrder;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.domain.PageRequest;
 import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
 import org.springframework.data.elasticsearch.core.FacetedPage;
 import org.springframework.data.elasticsearch.core.FacetedPageImpl;
 import org.springframework.data.elasticsearch.core.facet.FacetResult;
 import org.springframework.data.elasticsearch.core.facet.request.TermFacetRequestBuilder;
 import org.springframework.data.elasticsearch.core.facet.result.Term;
 import org.springframework.data.elasticsearch.core.facet.result.TermResult;
 import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
 import org.springframework.data.elasticsearch.core.query.SearchQuery;
 import org.springframework.stereotype.Component;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 
 @Component
 public class PropertySearchServiceImpl implements PropertySearchService {
 
 	@Autowired
 	private ElasticsearchTemplate esTemplate;
 
 	@Override
 	public PropertySearchResult search(String searchPhrase) {
 		return search(searchPhrase, null);
 	}
 
 	@Override
 	public PropertySearchResult search(String searchPhrase, PropertyQueryParams propertyQueryParams) {
 		NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder(); 
 		queryBuilder.withQuery(QueryBuilders.multiMatchQuery(searchPhrase)
 				.field("incode", 100.0f)
 				.field("outcode", 80.0f)
 				.field("city", 90.0f)
 				.field("summary", 0.3f)
 				.field("description", 0.2f)
 				.field("propertyType", 1.5f)
 				.field("propertySubType", 2.0f)
 				.field("features", 1.0f));
 
 		if(propertyQueryParams != null) {
 			applyFilters(queryBuilder, propertyQueryParams);
 			defineFacets(queryBuilder);
 			applyOrderBy(queryBuilder, propertyQueryParams);
 			applyPaging(propertyQueryParams, queryBuilder);
 		}
 		
 		SearchQuery searchQuery = queryBuilder.build();
 
 		long start = System.nanoTime();
 		FacetedPage<Property> results = esTemplate.queryForPage(searchQuery, Property.class);
 		long millisSpent = (System.nanoTime() - start) / 1000000;
 
 		results = removeFilteredTermsFromFacet(results, propertyQueryParams);
 
 		return new PropertySearchResult(searchPhrase, millisSpent, results, propertyQueryParams);
 	}
 
 	private FacetedPage<Property> removeFilteredTermsFromFacet(FacetedPage<Property> results, PropertyQueryParams propertyQueryParams) {
 
 		if(propertyQueryParams != null && propertyQueryParams.getFilters() != null && propertyQueryParams.getFilters().size() > 0 && results.hasFacets()) {
 
 			List<FacetResult> facetResultList = new ArrayList<>();
 
 			for (FacetResult facetResult : results.getFacets()) {
 				if ( propertyQueryParams.getFilters(facetResult.getName()) != null && propertyQueryParams.getFilters(facetResult.getName()).size() > 0) {
 					List<Term> termsToRemove = ((TermResult) facetResult).getTerms();
 					List<Term> termsToKeep = new ArrayList<>();
 					for (String selectedFilter : propertyQueryParams.getFilters(facetResult.getName())) {
 						for (Term term : termsToRemove) {
 							if ( term.getTerm().equals(selectedFilter) ) {
 								termsToKeep.add(term);
 							}
 						}
 					}
 					facetResultList.add(new TermResult(facetResult.getName(), termsToKeep));
 				}
 				else {
 					facetResultList.add(facetResult);
 				}
 			}
 
 			return new FacetedPageImpl<Property>(results.getContent(), results.nextPageable(), results.getTotalElements(), facetResultList);
 		}
 		return results;
 	}
 
 	private void applyPaging(PropertyQueryParams propertyQueryParams,
 			NativeSearchQueryBuilder queryBuilder) {
 		if(propertyQueryParams.getPageNumber() != null && propertyQueryParams.getPageSize() != null
 				&& propertyQueryParams.getPageNumber() >= 0 && propertyQueryParams.getPageSize() >= 1) {
 			queryBuilder.withPageable(new PageRequest(propertyQueryParams.getPageNumber(), propertyQueryParams.getPageSize()));
 		}
 	}
 
 	private void applyOrderBy(NativeSearchQueryBuilder queryBuilder, PropertyQueryParams propertyQueryParams) {
 		if(!StringUtils.isEmpty(propertyQueryParams.getFieldOrderBy())) {
 			FieldSortBuilder fieldSortBuilder = new FieldSortBuilder(propertyQueryParams.getFieldOrderBy());
 			if("DESC".equals(propertyQueryParams.getDirectionOrderBy())) {
 				fieldSortBuilder.order(SortOrder.DESC);
 			}
 			queryBuilder.withSort(fieldSortBuilder);
 		}
 	}
 
 	private void defineFacets(NativeSearchQueryBuilder queryBuilder) {
 		queryBuilder.withFacet(new TermFacetRequestBuilder("outcode").fields("outcode").build());
 		queryBuilder.withFacet(new TermFacetRequestBuilder("incode").fields("incode").build());
 		queryBuilder.withFacet(new TermFacetRequestBuilder("city").fields("city").build());
 		queryBuilder.withFacet(new TermFacetRequestBuilder("propertyType").fields("propertyType").build());
 		queryBuilder.withFacet(new TermFacetRequestBuilder("propertySubType").fields("propertySubType").build());
 	}
 
 	// there must be an easier way to do this
 	private void applyFilters(NativeSearchQueryBuilder queryBuilder, PropertyQueryParams propertyQueryParams) {
 		if(propertyQueryParams == null) {
 			return;
 		}
 
 		Collection<FilterBuilder> filterBuilders = new ArrayList<FilterBuilder>(); 
 
 		filterBuilders.addAll(applyFilter(queryBuilder, propertyQueryParams, "incode"));
 		filterBuilders.addAll(applyFilter(queryBuilder, propertyQueryParams, "outcode"));
 		filterBuilders.addAll(applyFilter(queryBuilder, propertyQueryParams, "city"));
 		filterBuilders.addAll(applyFilter(queryBuilder, propertyQueryParams, "propertyType"));
 		filterBuilders.addAll(applyFilter(queryBuilder, propertyQueryParams, "propertySubType"));
 		
 		FilterBuilder priceFilter = applyFilterPriceRange(queryBuilder, propertyQueryParams);
 		if(priceFilter != null) {
 			filterBuilders.add(priceFilter);
 		}
 		
 		if(filterBuilders.size() == 0) {
 			return;
 		}
 		
 		// this needs refactoring, there must be an easier way to add one filter after the other
 		Object[] array = filterBuilders.toArray();
 		FilterBuilder[] filterBuilderArray= new FilterBuilder[array.length];
 		
 		for (int i = 0; i < array.length; i++) {
 			FilterBuilder filter = (FilterBuilder) array[i];
 			filterBuilderArray[i] = filter;
 		}
 		
 		queryBuilder.withFilter(FilterBuilders.orFilter(filterBuilderArray));
 	}
 
 	private FilterBuilder applyFilterPriceRange(
 			NativeSearchQueryBuilder queryBuilder,
 			PropertyQueryParams propertyQueryParams) {
 		
 		Double priceMin = propertyQueryParams.getPriceMin();
 		Double priceMax = propertyQueryParams.getPriceMax();
 		
 		if(priceMin == null || priceMax == null || priceMax < priceMin) {
 			return null;
 		}
 		
 		FilterBuilder filter = FilterBuilders
 				.rangeFilter("price")
 				.from(priceMin)
 				.to(priceMax)
 				.includeLower(true)
 				.includeUpper(true);
 		
 		return filter;
 	}
 
 	// this needs refactoring, there must be an easier way to add one filter after the other
 	private List<FilterBuilder> applyFilter(
 			NativeSearchQueryBuilder queryBuilder,
 			PropertyQueryParams propertyQueryParams, String fieldName) {
 		Collection<String> filterValues = propertyQueryParams.getFilters(fieldName);
 		List<FilterBuilder> filterBuilders = new ArrayList<FilterBuilder>(filterValues.size());
 
 		for (String value : propertyQueryParams.getFilters(fieldName)) {
 			TermFilterBuilder filter = FilterBuilders.termFilter(fieldName, value);
 			filterBuilders.add(filter);
 		}
 		return filterBuilders;
 	}
 }
