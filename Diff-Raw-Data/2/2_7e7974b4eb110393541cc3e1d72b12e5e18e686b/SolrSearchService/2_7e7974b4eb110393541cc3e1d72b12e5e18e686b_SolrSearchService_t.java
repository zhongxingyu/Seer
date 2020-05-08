 package uk.ac.ox.oucs.search2.solr;
 
 import org.apache.solr.client.solrj.SolrQuery;
 import org.apache.solr.client.solrj.SolrServer;
 import org.apache.solr.client.solrj.SolrServerException;
 import org.apache.solr.client.solrj.response.QueryResponse;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import uk.ac.ox.oucs.search2.AbstractSearchService;
 import uk.ac.ox.oucs.search2.exception.InvalidSearchQueryException;
 import uk.ac.ox.oucs.search2.filter.SearchFilter;
 import uk.ac.ox.oucs.search2.result.SearchResultList;
 import uk.ac.ox.oucs.search2.solr.result.SolrSearchResultList;
 
 import java.util.Collection;
 import java.util.Iterator;
 
 /**
  * @author Colin Hebert
  */
 public class SolrSearchService extends AbstractSearchService {
    private final static Logger logger = LoggerFactory.getLogger(SolrSearchService.class);
     private final SolrServer solrServer;
 
     public SolrSearchService(SolrServer solrServer) {
         this.solrServer = solrServer;
     }
 
     protected SearchResultList search(String searchQuery, Collection<String> contexts, int start, int length, Iterable<SearchFilter> filterChain) {
         try {
             SolrQuery query = new SolrQuery();
 
             query.setStart(start);
             query.setRows(length);
             query.setFields("*", "score");
 
             query.setHighlight(true).setHighlightSnippets(5);
             query.setParam("hl.useFastVectorHighlighter", true);
             query.setParam("hl.mergeContiguous", true);
             query.setParam("hl.fl", SolrSchemaConstants.CONTENT_FIELD);
 
             query.setParam("tv", true);
             query.setParam("tv.fl", SolrSchemaConstants.CONTENT_FIELD);
             query.setParam("tv.tf", true);
 
             if (!contexts.isEmpty()) {
                 StringBuilder sb = new StringBuilder();
                 sb.append('+').append(SolrSchemaConstants.SITEID_FIELD).append(":");
                 sb.append('(');
                 for (Iterator<String> contextIterator = contexts.iterator(); contextIterator.hasNext(); ) {
                     sb.append('"').append(contextIterator.next()).append('"');
                     if (contextIterator.hasNext())
                         sb.append(" OR ");
                 }
                 sb.append(')');
                 query.setFilterQueries(sb.toString());
             }
 
             logger.debug("Searching with Solr : " + searchQuery);
             query.setQuery(searchQuery);
             QueryResponse rsp = solrServer.query(query);
             return new SolrSearchResultList(rsp, filterChain);
         } catch (SolrServerException e) {
             throw new InvalidSearchQueryException("Failed to parse Query ", e);
         }
     }
 
     @Override
     public String getSuggestion(String searchString) {
         return null;
     }
 }
