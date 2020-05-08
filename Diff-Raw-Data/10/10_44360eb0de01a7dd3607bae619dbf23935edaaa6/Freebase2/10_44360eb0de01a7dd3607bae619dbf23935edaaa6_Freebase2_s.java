 /*
  * Copyright (c) 2010, Hamish Morgan.
  * All Rights Reserved.
  */
 package com.google.api.services.freebase;
 
 import com.google.api.client.googleapis.GoogleHeaders;
 import com.google.api.client.googleapis.batch.BatchCallback;
 import com.google.api.client.googleapis.batch.BatchRequest;
 import com.google.api.client.googleapis.services.GoogleClient;
 import com.google.api.client.http.HttpMethod;
 import com.google.api.client.http.HttpRequest;
 import com.google.api.client.http.HttpRequestInitializer;
 import com.google.api.client.http.HttpTransport;
 import com.google.api.client.http.json.JsonHttpRequestInitializer;
 import com.google.api.client.json.JsonFactory;
 import com.google.api.client.json.JsonObjectParser;
 import com.google.api.services.freebase.SearchFormat.AbstractResult;
 import com.google.api.services.freebase.SearchFormat.IdsResult;
 import com.google.common.base.Preconditions;
 import com.google.common.base.Throwables;
 import com.google.common.collect.Maps;
 import com.google.common.io.Files;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.annotation.Nullable;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.charset.Charset;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * Extention to the com.google.api.services.freebase.Freebase API which adds support for search.
  * <p/>
  * <
  * p/>
  * <p/>
  * @author hamish
  */
 public class Freebase2 extends Freebase {
 
     private static final Logger LOG = LoggerFactory.getLogger(Freebase2.class);
     public static final int BATCH_FAILURE_RETRIES = 3;
 
     public Freebase2(HttpTransport transport, JsonFactory jsonFactory,
                      HttpRequestInitializer httpRequestInitializer) {
         super(transport, jsonFactory, httpRequestInitializer);
     }
 
     Freebase2(HttpTransport transport,
               JsonHttpRequestInitializer jsonHttpRequestInitializer,
               HttpRequestInitializer httpRequestInitializer,
               JsonFactory jsonFactory, JsonObjectParser jsonObjectParser,
               String rootUrl, String servicePath, String applicationName,
               boolean suppressPatternChecks) {
         super(transport, jsonHttpRequestInitializer, httpRequestInitializer,
               jsonFactory, jsonObjectParser, rootUrl, servicePath,
               applicationName, suppressPatternChecks);
     }
 
     public Freebase2.Search search(String query) throws IOException {
         LOG.debug("Performing search for query {}", query);
         Search search = new Search(query);
         initialize(search);
         return search;
     }
 
     public List<String> searchGetMids(String query) throws IOException {
         LOG.debug("Performing MID search for query {}", query);
         Search search = new Search(query);
         search.setFormat(SearchFormat.MIDS);
         initialize(search);
         return search.executeParseObject(
                 SearchFormat.IdsResult.class).getResults();
     }
 
     public List<String> searchGetIds(String query) throws IOException {
         LOG.debug("Performing ID search for query {}", query);
         Search search = new Search(query);
         search.setFormat(SearchFormat.IDS);
         initialize(search);
         return search.executeParseObject(
                 SearchFormat.IdsResult.class).getResults();
     }
 
     public List<String> searchGetGuids(String query) throws IOException {
         LOG.debug("Performing GUID search for query {}", query);
         Search search = new Search(query);
         search.setFormat(SearchFormat.GUIDS);
         initialize(search);
         return search.executeParseObject(
                 SearchFormat.IdsResult.class).getResults();
     }
 
     /**
      * Convenience method that creates a new query to retrieve the text description for the given
      * entity id, execute the query, then return the result.
      *
      * @param id
      * @return
      * @throws IOException
      */
     public String textGetResult(String id) throws IOException {
         Preconditions.checkNotNull(id, "id");
         return text().get(Arrays.asList(id)).execute().getResult();
     }
 
     /**
      * Search for all of the given queries as a single batch operations, returning the results as
      * map from each query string to the result object. The format of the result object is
      * determined by the second (format) argument.
      *
      * This method is substantially faster when large numbers of queries need to be performed
      * consecutively.
      *
      * @param queries queries to search for
      * @param format result format
      * @return results mapped to each query string
      * @throws IOException if something bad occurs with the lookup
      */
     public Map<String, AbstractResult> batchSearch(
             Set<String> queries, SearchFormat format)
             throws IOException {
         LOG.debug("Performing brach search for queries {}", queries);
         Preconditions.checkNotNull(queries, "queries");
         Preconditions.checkNotNull(format, "queries");
         assert AbstractResult.class.isAssignableFrom(format.getDataClass());
 
         final Map<String, AbstractResult> successes = Maps.newHashMap();
        final Map<String, Void> failures = Maps.newHashMap();
 
         final BatchRequest request = this.batch();
 
         int retryCount = 0;
 
         for (String query : queries) {
             failures.put(query, null);
         }
 
         while (!failures.isEmpty() && retryCount < BATCH_FAILURE_RETRIES) {
 
             try {
                 Thread.sleep(100 * (long) Math.pow(2, retryCount));
             } catch (InterruptedException ex) {
                 Throwables.propagate(ex);
             }
 
             for (String query : failures.keySet()) {
                 final Search search = new Search(query);
                 search.setFormat(format);
                 initialize(search);
 
                 // Need to pre-define and type the arguments or jdk6 gets confused.
                 final HttpRequest httpRequest = search.buildHttpRequest();
                 final Class<AbstractResult> dataClass = (Class<AbstractResult>)format.getDataClass();
                final Class<Void> errorClass = Void.TYPE;
                final BatchCallback<AbstractResult, Void> callback =  newMapPutCallback(query, successes, failures);
                 request.queue(httpRequest, dataClass, errorClass, callback);
             }
             failures.clear();
             request.execute();
 
             if (!failures.isEmpty()) {
                 LOG.warn("One or more queries failed during batch request: "
                         + failures.keySet().toString());
             }
             retryCount++;
         }
 
         return successes;
     }
 
     /**
      * Search for all of the given queries as a single batch operations, returning the results as
      * map from each query string to an IdResult object.
      *
      * @param queries queries to search for
      * @param format result format
      * @return results mapped to each query string
      * @throws IOException if something bad occurs with the lookup
      */
     Map<String, List<String>> batchSearchGetIds0(Set<String> queries, SearchFormat format) throws IOException {
         Preconditions.checkNotNull(queries, "queries");
         Preconditions.checkNotNull(format, "format");
         Preconditions.checkArgument(format.getDataClass().equals(IdsResult.class),
                                     "format must produce an IdsResult type output.");
         final Map<String, AbstractResult> results = batchSearch(queries, format);
         final Map<String, List<String>> ids = Maps.newHashMap();
         for (Map.Entry<String, AbstractResult> result : results.entrySet()) {
             ids.put(result.getKey(), ((IdsResult) result.getValue()).getResults());
         }
         return ids;
     }
 
     /**
      * Search for all of the given queries as a single batch operations, returning the results as
      * map from each query string to an list of id strings.
      *
      * @param queries queries to search for
      * @return results mapped to each query string
      * @throws IOException if something bad occurs with the lookup
      */
     public Map<String, List<String>> batchSearchGetIds(Set<String> queries) throws IOException {
         return batchSearchGetIds0(queries, SearchFormat.IDS);
     }
 
     /**
      * Search for all of the given queries as a single batch operations, returning the results as
      * map from each query string to an list of mid strings.
      *
      * @param queries queries to search for
      * @return results mapped to each query string
      * @throws IOException if something bad occurs with the lookup
      */
     public Map<String, List<String>> batchSearchGetMids(Set<String> queries) throws IOException {
         return batchSearchGetIds0(queries, SearchFormat.MIDS);
     }
 
     /**
      * Search for all of the given queries as a single batch operations, returning the results as
      * map from each query string to an list of guid strings.
      *
      * @param queries queries to search for
      * @return results mapped to each query string
      * @throws IOException if something bad occurs with the lookup
      */
     public Map<String, List<String>> batchSearchGetGuids(Set<String> queries) throws IOException {
         return batchSearchGetIds0(queries, SearchFormat.GUIDS);
     }
 
     /**
      * Factory methods that instantiates a new BatchCallback class, which writes it's results to the
      * the key's location in the given map objects.
      *
      * @param <K> type of key
      * @param <S> type of result object that is produced on success
      * @param <F> type of error object that is produced on failure
      * @param key the location is the maps to write
      * @param successDestination map of keys to result objects
      * @param failureDestination map of keys to error objects
      * @return a new callback instance
      */
     static <K, S, F> BatchCallback<S, F> newMapPutCallback(
             final K key,
             final Map<? super K, ? super S> successDestination,
             final Map<? super K, ? super F> failureDestination) {
         return new BatchCallback<S, F>() {
             @Override
             public void onSuccess(S t, GoogleHeaders responseHeaders) {
                 if (successDestination.containsKey(t)) {
                     LOG.warn("Key already exists in success map; overwriting.");
                 }
                 successDestination.put(key, t);
             }
 
             @Override
             public void onFailure(F e, GoogleHeaders responseHeaders) throws IOException {
                 if (failureDestination.containsKey(e)) {
                     LOG.warn("Key already exists in failure map; overwriting.");
                 }
                 failureDestination.put(key, e);
             }
         };
     }
 
     public static String loadGoogleApiKey(File path) throws IOException {
         final String googleApiKey;
         if (path.exists()) {
             byte[] bytes = Files.toByteArray(path);
             googleApiKey = new String(bytes, Charset.forName("ASCII"));
             LOG.debug("Google API key {} loaded from {}.",
                       googleApiKey, path);
         } else {
             googleApiKey = null;
             LOG.warn("{} doesn't exist.", path);
         }
         return googleApiKey;
     }
 
     public class Search extends FreebaseRequest {
 
         private static final String REST_PATH = "search";
         /**
          * The text you want to match against Freebase entities. (Required)
          */
         @com.google.api.client.util.Key
         private String query;
         /**
          * JS method name for JSONP callbacks.
          */
         @com.google.api.client.util.Key
         @Nullable
         private String callback;
         /**
          * A comma separated list of domain IDs. Search results must include these domains.
          */
         @com.google.api.client.util.Key
         @Nullable
         private List<String> domain;
         /**
          * Matches only the name, and keys 'exactly'. No normalization of any kind is done at
          * indexing and query time. The text is only broken up on space characters. (Default: false)
          */
         @com.google.api.client.util.Key
         @Nullable
         private Boolean exact;
         /**
          * A filter s-expression.
          */
         @com.google.api.client.util.Key
         @Nullable
         private List<String> filter;
         /**
          * The keyword "classic" to return the same information the original search API would have
          */
         @com.google.api.client.util.Key
         @Nullable
         private SearchFormat format;
         /**
          * Whether or not to HTML escape entities' names. (Default: False)
          */
         @com.google.api.client.util.Key
         @Nullable
         private Boolean encode;
         /**
          * Whether to indent the JSON. (Default: False)
          */
         @com.google.api.client.util.Key
         @Nullable
         private Boolean indent;
         /**
          * Return up to this number of results. (Min: 1, Default: 20)
          */
         @com.google.api.client.util.Key
         @Nullable
         private Long limit;
         /**
          * A MQL query that extracts entity information.
          */
         @com.google.api.client.util.Key("mql_output")
         @Nullable
         private String mqlOutput;
         /**
          * Whether or not to match by name prefix. (used for autosuggest) (Default: False)
          */
         @com.google.api.client.util.Key
         @Nullable
         private Boolean prefixed;
         /**
          * Allows paging through results. (Min: 0, Default: 0)
          */
         @com.google.api.client.util.Key
         @Nullable
         private Long start;
         /**
          * A comma separated list of type IDs. Search results must include these types.
          */
         @com.google.api.client.util.Key
         @Nullable
         private List<String> type;
         /**
          * The language you are searching in. Can pass multiple languages.
          */
         @com.google.api.client.util.Key
         @Nullable
         private List<String> lang;
 
         /**
          *
          * <p/>
          * @param query
          */
         Search(String query) {
             super(Freebase2.this, HttpMethod.GET, REST_PATH, null);
             Preconditions.checkNotNull(query, "query");
             this.query = query;
         }
 
         /**
          * @return The text you want to search for.
          */
         public String getQuery() {
             return query;
         }
 
         /**
          *
          * @param query The text you want to search for.
          * @return self (allows method chaining)
          */
         public Search setQuery(String query) {
             Preconditions.checkNotNull(query, "query");
             this.query = query;
             return this;
         }
 
         /**
          *
          * @return
          */
         public String getCallback() {
             return callback;
         }
 
         /**
          * @param callback JS method name for JSONP callbacks.
          * @return self (allows method chaining)
          */
         public Search setCallback(String callback) {
             this.callback = callback;
             return this;
         }
 
         /**
          *
          * @return
          */
         public List<String> getDomain() {
             return domain;
         }
 
         /**
          *
          * @param domain
          * @return self (allows method chaining)
          */
         public Search setDomain(List<String> domain) {
             this.domain = domain;
             return this;
         }
 
         /**
          *
          * @return
          */
         public Boolean getExact() {
             return exact;
         }
 
         /**
          *
          * @param exact
          * @return self (allows method chaining)
          */
         public Search setExact(Boolean exact) {
             this.exact = exact;
             return this;
         }
 
         /**
          *
          * @return complex rules and constraints to apply to the query.
          */
         public List<String> getFilter() {
             return filter;
         }
 
         /**
          * The filter parameter allows you to create more complex rules and constraints to apply to
          * your query.
          * <p/>
          * The filter value is a simple language that supports the following symbols: <ul> <li>the
          * all, any, should and not operators </li> <li>tthe type, domain, name,</li> <li>talias,
          * with and without operands </li> </ul> the ( and ) parenthesis for grouping and precedence
          * <p/>
          * @param filter complex rules and constraints to apply to the query.
          * @return self (allows method chaining)
          * @see "http://wiki.freebase.com/wiki/Search_Cookbook"
          */
         public Search setFilter(List<String> filter) {
             this.filter = filter;
             return this;
         }
 
         /**
          *
          * @return
          */
         public SearchFormat getFormat() {
             return format;
         }
 
         /**
          *
          * @param format
          * @return self (allows method chaining)
          */
         public Search setFormat(SearchFormat format) {
             this.format = format;
             return this;
         }
 
         /**
          *
          * @return
          */
         public Boolean getEncode() {
             return encode;
         }
 
         /**
          * By default search will return data as-is in Freebase. You can turn on html encoding using
          * this parameter.
          * <p/>
          * @param encode
          * @return self (allows method chaining)
          */
         public Search setEncode(Boolean encode) {
             this.encode = encode;
             return this;
         }
 
         /**
          *
          * @return
          */
         public Boolean getIndent() {
             return indent;
         }
 
         /**
          *
          * @param indent
          * @return self (allows method chaining)
          */
         public Search setIndent(Boolean indent) {
             this.indent = indent;
             return this;
         }
 
         /**
          *
          * @return return up to this number of results.
          */
         public Long getLimit() {
             return limit;
         }
 
         /**
          * By default, 20 matches in decreasing order of relevance are returned, if that many exist.
          * Fewer or more matches may be requested by using the limit parameter with a different
          * value.
          * <p/>
          * @param limit return up to this number of results.
          * @return self (allows method chaining)
          * @throws IllegalArgumentException if limit < 1
          */
         public Search setLimit(Long limit) {
             Preconditions.checkArgument(limit >= 1, "limit < 1");
             this.limit = limit;
             return this;
         }
 
         /**
          *
          * @return
          */
         public String getMqlOutput() {
             return mqlOutput;
         }
 
         /**
          * After the query is run, the matching documents' ids are passed to the mql_output MQL
          * query to retrieve actual data about the matches. The MQL results are sorted by decreasing
          * relevance score.
          * <p/>
          * @param mqlOutput
          * @return self (allows method chaining)
          */
         public Search setMqlOutput(String mqlOutput) {
             this.mqlOutput = mqlOutput;
             return this;
         }
 
         /**
          *
          * @return
          */
         public Boolean getPrefixed() {
             return prefixed;
         }
 
         /**
          *
          * @param prefixed
          * @return self (allows method chaining)
          */
         public Search setPrefixed(Boolean prefixed) {
             this.prefixed = prefixed;
             return this;
         }
 
         /**
          *
          * @return
          */
         public Long getStart() {
             return start;
         }
 
         /**
          * Using the start parameter makes it possible to page through limit results at a time. For
          * example, to present 3 pages of successive 10 results, the same query may be used with
          * limit=10 and start=0, then 10, 20.
          * <p/>
          * @param start
          * @return self (allows method chaining)
          * @throws IllegalArgumentException if start < 0
          */
         public Search setStart(Long start) {
             Preconditions.checkArgument(start >= 0, "start < 0");
             this.start = start;
             return this;
         }
 
         /**
          *
          * @return
          */
         public List<String> getType() {
             return type;
         }
 
         /**
          *
          * @param type
          * @return self (allows method chaining)
          */
         public Search setType(List<String> type) {
             this.type = type;
             return this;
         }
 
         /**
          *
          * @return
          */
         public List<String> getLang() {
             return lang;
         }
 
         /**
          * @param lang The language you are searching in. Can pass multiple languages.
          * @return self (allows method chaining)
          */
         public Search setLang(List<String> lang) {
             this.lang = lang;
             return this;
         }
 
         public <T> T executeParseObject(Class<T> dataClass) throws IOException {
 
             final InputStream is = executeAsInputStream();
 
             JsonObjectParser parser = getJsonObjectParser() == null
                     ? getJsonFactory().createJsonObjectParser()
                     : getJsonObjectParser();
 
             // XXX: Not good
             Charset charset = Charset.defaultCharset();
 
             return parser.parseAndClose(is, charset, dataClass);
 
         }
     }
 
     /**
      * Almost identical to {@link Freebase.Builder} but produces a Freebase2 object instead.
      * Deprecated methods have been removed.
      */
     public static final class Builder extends GoogleClient.Builder {
 
         /**
          * Returns an instance of a new builder.
          * <p/>
          * @param transport The transport to use for requests
          * @param jsonFactory A factory for creating JSON parsers and serializers
          * @param httpRequestInitializer The HTTP request initializer or {@code null} for none
          * @since 1.7
          */
         public Builder(HttpTransport transport, JsonFactory jsonFactory,
                        HttpRequestInitializer httpRequestInitializer) {
             super(transport, jsonFactory, DEFAULT_ROOT_URL, DEFAULT_SERVICE_PATH,
                   httpRequestInitializer);
         }
 
         /**
          * Builds a new instance of {@link Freebase}.
          */
         @SuppressWarnings("deprecation")
         @Override
         public Freebase2 build() {
             return new Freebase2(
                     getTransport(),
                     getJsonHttpRequestInitializer(),
                     getHttpRequestInitializer(),
                     getJsonFactory(),
                     getObjectParser(),
                     getRootUrl(),
                     getServicePath(),
                     getApplicationName(),
                     getSuppressPatternChecks());
         }
 
         @Override
         public Builder setRootUrl(String rootUrl) {
             super.setRootUrl(rootUrl);
             return this;
         }
 
         @Override
         public Builder setServicePath(String servicePath) {
             super.setServicePath(servicePath);
             return this;
         }
 
         @Override
         public Builder setJsonHttpRequestInitializer(
                 JsonHttpRequestInitializer jsonHttpRequestInitializer) {
             super.setJsonHttpRequestInitializer(jsonHttpRequestInitializer);
             return this;
         }
 
         @Override
         public Builder setHttpRequestInitializer(
                 HttpRequestInitializer httpRequestInitializer) {
             super.setHttpRequestInitializer(httpRequestInitializer);
             return this;
         }
 
         @Override
         public Builder setApplicationName(String applicationName) {
             super.setApplicationName(applicationName);
             return this;
         }
 
         @Override
         public Builder setObjectParser(JsonObjectParser parser) {
             super.setObjectParser(parser);
             return this;
         }
 
         @Override
         public Builder setSuppressPatternChecks(boolean suppressPatternChecks) {
             super.setSuppressPatternChecks(suppressPatternChecks);
             return this;
         }
     }
 }
