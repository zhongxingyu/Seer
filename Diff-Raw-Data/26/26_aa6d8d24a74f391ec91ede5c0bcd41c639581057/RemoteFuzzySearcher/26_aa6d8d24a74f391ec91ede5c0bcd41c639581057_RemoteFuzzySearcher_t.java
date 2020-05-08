 package org.atlasapi.query.content.fuzzy;
 
 import org.atlasapi.remotesite.HttpClients;
 import org.atlasapi.search.ContentSearcher;
 import org.atlasapi.search.model.SearchQuery;
 import org.atlasapi.search.model.SearchResults;
 import org.atlasapi.search.model.SearchResultsError;
 
 import com.google.common.base.Joiner;
 import com.google.gson.Gson;
 import com.metabroadcast.common.http.HttpResponse;
 import com.metabroadcast.common.http.HttpStatusCode;
 import com.metabroadcast.common.http.SimpleHttpClient;
 import com.metabroadcast.common.url.UrlEncoding;
 
 public class RemoteFuzzySearcher implements ContentSearcher {
 
     private static final Joiner CSV = Joiner.on(',');
 
     private final SimpleHttpClient client = HttpClients.webserviceClient();
     private final Gson gson = new Gson();
     private final String remoteHost;
 
     public RemoteFuzzySearcher(String remoteHost) {
         this.remoteHost = remoteHost;
     }
 
     @Override
     public SearchResults search(SearchQuery query) {
        String queryString = remoteHost + String.format("/titles?title=%s&%s&publishers=%s&titleWeighting=%s&broadcastWeighting=%s&catchupWeighting=%s",
                    UrlEncoding.encode(query.getTerm()), 
                    query.getSelection().asQueryParameters(), 
                    CSV.join(query.getIncludedPublishers()), 
                    query.getTitleWeighting(), 
                    query.getBroadcastWeighting(), 
                    query.getCatchupWeighting());
         try {
            HttpResponse response = client.get(queryString);
             if (HttpStatusCode.OK.is(response.statusCode())) {
                 return gson.fromJson(response.body(), SearchResults.class);
             }
             throw new RemoteFuzzySearcherException(gson.fromJson(response.body(), SearchResultsError.class));
        } catch (Exception e) {
            throw new RuntimeException("Query: " + queryString, e);
         }
     }
 
     static class RemoteFuzzySearcherException extends RuntimeException {
 
         private static final long serialVersionUID = 1L;
 
         public RemoteFuzzySearcherException(SearchResultsError error) {
             super(error.getMessage());
         }
     }
 }
