 package com.aconex.scrutineer.elasticsearch;
 
 import com.aconex.scrutineer.IdAndVersion;
 import org.elasticsearch.action.search.SearchResponse;
 import org.elasticsearch.action.search.SearchType;
 import org.elasticsearch.client.Client;
 import org.elasticsearch.client.action.search.SearchRequestBuilder;
 import org.elasticsearch.common.unit.TimeValue;
 import org.elasticsearch.search.SearchHit;
 
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 
 import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
 
 public class ElasticSearchDownloader {
 
     private static final int BATCH_SIZE = 100000;
     private static final int SCROLL_TIME_IN_MINUTES = 10;
 
     private final Client client;
     private final String indexName;
 
     public ElasticSearchDownloader(Client client, String indexName) {
         this.client = client;
         this.indexName = indexName;
     }
 
     public void downloadTo(OutputStream outputStream) {
         try {
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
             consumeBatches(objectOutputStream, startScrollAndGetFirstBatch());
             objectOutputStream.close();
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 
     void consumeBatches(ObjectOutputStream objectOutputStream, SearchResponse searchResponse) throws IOException {
         String scrollId = searchResponse.getScrollId();
         SearchResponse batchSearchResponse = null;
         do {
             batchSearchResponse = client.prepareSearchScroll(scrollId).setScroll(TimeValue.timeValueMinutes(SCROLL_TIME_IN_MINUTES)).execute().actionGet();
         } while (writeSearchResponseToOutputStream(objectOutputStream, batchSearchResponse));
     }
 
     boolean writeSearchResponseToOutputStream(ObjectOutputStream objectOutputStream, SearchResponse searchResponse) throws IOException {
         SearchHit[] hits = searchResponse.getHits().hits();
         for (SearchHit hit : hits) {
             new IdAndVersion(hit.getId(), hit.getVersion()).writeToStream(objectOutputStream);
         }
         return hits.length > 0;
     }
 
    @SuppressWarnings("PMD.NcssMethodCount")
     SearchResponse startScrollAndGetFirstBatch() {
         SearchRequestBuilder searchRequestBuilder = client.prepareSearch(indexName);
         searchRequestBuilder.setSearchType(SearchType.SCAN);
         searchRequestBuilder.setQuery(matchAllQuery());
         searchRequestBuilder.setSize(BATCH_SIZE);
         searchRequestBuilder.setExplain(false);
         searchRequestBuilder.setNoFields();
         searchRequestBuilder.setVersion(true);
         searchRequestBuilder.setScroll(TimeValue.timeValueMinutes(SCROLL_TIME_IN_MINUTES));
 
         return searchRequestBuilder.execute().actionGet();
     }
 
 }
