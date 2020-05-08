 package no.guttab.raven.search.query;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.solr.client.solrj.SolrQuery;
 
 import static java.util.Arrays.asList;
 
 public class QueryBuilder {
    private List<QueryProcessor> queryProcessors = new ArrayList<QueryProcessor>();
 
    public QueryBuilder() {
       this(asList(
             new FacetFieldQueryProcessor(),
             new FilterQueryProcessor(),
             new QueryStringQueryProcessor()
       ));
    }
 
    private QueryBuilder(List<QueryProcessor> queryProcessors) {
       this.queryProcessors.addAll(queryProcessors);
    }
 
    public QueryBuilder withQueryProcessors(List<QueryProcessor> queryProcessors) {
       return new QueryBuilder(queryProcessors);
    }
 
    public QueryBuilder withAddedQueryProcessors(QueryProcessor... queryProcessors) {
       QueryBuilder defaultQueryBuilder = new QueryBuilder(this.queryProcessors);
       defaultQueryBuilder.queryProcessors.addAll(asList(queryProcessors));
       return defaultQueryBuilder;
    }
 
    public SolrQuery buildQuery(Object queryInput) {
       final SolrQuery solrQuery = new SolrQuery();
       for (QueryProcessor queryProcessor : queryProcessors) {
          queryProcessor.buildQuery(queryInput, solrQuery);
       }
       return solrQuery;
    }
 
 }
