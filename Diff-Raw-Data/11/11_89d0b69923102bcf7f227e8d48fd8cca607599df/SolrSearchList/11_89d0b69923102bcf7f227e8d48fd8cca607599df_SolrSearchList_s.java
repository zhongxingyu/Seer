 package org.sakaiproject.search.solr.response;
 
 import com.google.common.collect.ForwardingList;
 import org.apache.solr.client.solrj.response.QueryResponse;
 import org.apache.solr.common.SolrDocument;
 import org.sakaiproject.search.api.SearchList;
 import org.sakaiproject.search.api.SearchResult;
 import org.sakaiproject.search.api.SearchService;
 import org.sakaiproject.search.producer.ContentProducerFactory;
 import org.sakaiproject.search.response.filter.SearchItemFilter;
 
 import java.util.*;
 
 import static org.sakaiproject.search.solr.response.TermVectorExtractor.TermInfo;
 
 /**
  * List of results after a solr search query.
  *
  * @author Colin Hebert
  */
 public class SolrSearchList extends ForwardingList<SearchResult> implements SearchList {
     private final List<SearchResult> solrResults;
     private final QueryResponse rsp;
     private final int start;
 
     /**
      * List of results for a solr query.
      *
      * @param rsp                    raw response from solr.
      * @param start                  position of the first result (overall).
      * @param filter                 filter to apply on each result.
      * @param contentProducerFactory factory to obtain a content producer for each result.
      */
    public SolrSearchList(QueryResponse rsp, int start, SearchItemFilter filter, ContentProducerFactory contentProducerFactory) {
         this.rsp = rsp;
         this.start = start;
 
         List<SearchResult> results = new ArrayList<SearchResult>(rsp.getResults().size());
 
         //Extract TermVector information from the response
         TermVectorExtractor termVectorExtractor = new TermVectorExtractor(rsp);
         Map<String, Map<String, Map<String, TermInfo>>> termsPerDocument = termVectorExtractor.getTermVectorInfo();
 
         //Generate a SolrResult for each document
         for (SolrDocument document : rsp.getResults()) {
             String reference = (String) document.getFieldValue(SearchService.FIELD_REFERENCE);
 
             SolrResult solrResult = new SolrResult();
             solrResult.setIndex(results.size());
             solrResult.setDocument(document);
 
             //Not mandatory highlighting
             Map<String, List<String>> highlights = rsp.getHighlighting().get(reference);
             if (highlights == null)
                 highlights = Collections.emptyMap();
             solrResult.setHighlights(highlights);
 
             //Not mandatory terms counting
             Map<String, Map<String, TermInfo>> terms = termsPerDocument.get(reference);
             if (terms == null)
                 terms = Collections.emptyMap();
             solrResult.setTerms(terms);
 
             solrResult.setContentProducer(contentProducerFactory.getContentProducerForElement(reference));
 
             results.add(filter.filter(solrResult));
         }
         this.solrResults = Collections.unmodifiableList(results);
     }
 
     @Override
     public Iterator<SearchResult> iterator(int startAt) {
         Iterator<SearchResult> iterator = iterator();
         //Skip the fist elements
         for (int i = 0; i < startAt && iterator.hasNext(); i++)
             iterator.next();
         return iterator;
     }
 
     @Override
     public int getFullSize() {
         return (int) rsp.getResults().getNumFound();
     }
 
     @Override
     public int getStart() {
         return start;
     }
 
     @Override
     protected List<SearchResult> delegate() {
         return solrResults;
     }
 }
