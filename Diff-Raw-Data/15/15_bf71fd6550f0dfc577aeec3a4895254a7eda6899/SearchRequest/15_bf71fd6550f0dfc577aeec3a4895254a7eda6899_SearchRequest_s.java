 package no.guttab.raven.search.config;
 
 import java.util.List;
 
 import no.guttab.raven.search.query.QueryBuilder;
 import no.guttab.raven.search.response.DefaultConstructorSearchResponseFactory;
 import no.guttab.raven.search.response.ResponseProcessor;
 import no.guttab.raven.search.response.ResponseProcessors;
 import no.guttab.raven.search.response.SearchResponse;
 import no.guttab.raven.search.response.SearchResponseFactory;
 
 public class SearchRequest<T extends SearchResponse> {
    private SearchResponseFactory<T> searchResponseFactory;
    private QueryBuilder queryBuilder;
    private Object searchRequest;
    private ResponseProcessors<T> responseProcessors;
 
    public SearchRequest(Object searchRequest, Class<T> searchResponseType) {
       this(searchRequest, new DefaultConstructorSearchResponseFactory<T>(searchResponseType));
    }
 
    public SearchRequest(Object searchRequest, SearchResponseFactory<T> searchResponseFactory) {
       this(searchRequest,
             searchResponseFactory,
             new QueryBuilder(),
             ResponseProcessors.<T>defaultProcessors(searchRequest.getClass()));
    }
 
    private SearchRequest(
          Object searchRequest,
          SearchResponseFactory<T> searchResponseFactory,
          QueryBuilder queryBuilder,
          ResponseProcessors<T> responseProcessors) {
       this.searchRequest = searchRequest;
       this.searchResponseFactory = searchResponseFactory;
       this.queryBuilder = queryBuilder;
       this.responseProcessors = responseProcessors;
    }
 
    public SearchRequest<T> withQueryBuilder(QueryBuilder queryBuilder) {
       return new SearchRequest<T>(searchRequest, searchResponseFactory, queryBuilder, responseProcessors);
    }
 
    public SearchRequest<T> withSearchResponseFactory(SearchResponseFactory<T> searchResponseFactory) {
       return new SearchRequest<T>(searchRequest, searchResponseFactory, queryBuilder, responseProcessors);
    }
 
    public SearchRequest<T> withResponseProcessors(List<ResponseProcessor<T>> responseProcessors) {
       return new SearchRequest<T>(
             searchRequest, searchResponseFactory, queryBuilder, new ResponseProcessors<T>(responseProcessors));
    }
 
    public QueryBuilder getQueryBuilder() {
       return queryBuilder;
    }
 
    public SearchResponseFactory<T> getSearchResponseFactory() {
       return searchResponseFactory;
    }
 
    public ResponseProcessors<T> getResponseProcessors() {
       return responseProcessors;
    }
 }
