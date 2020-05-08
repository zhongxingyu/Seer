 package org.atlasapi.remotesite.redux;
 
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.TimeUnit;
 
import org.atlasapi.remotesite.FetchException;
 import org.atlasapi.remotesite.HttpClients;
 import org.atlasapi.remotesite.redux.model.BaseReduxProgramme;
 import org.atlasapi.remotesite.redux.model.FullReduxProgramme;
 import org.atlasapi.remotesite.redux.model.PaginatedBaseProgrammes;
 import org.atlasapi.remotesite.redux.model.ReduxMedia;
 import org.joda.time.LocalDate;
 import org.joda.time.format.ISODateTimeFormat;
 
 import com.google.common.cache.CacheBuilder;
 import com.google.common.cache.CacheLoader;
 import com.google.common.cache.LoadingCache;
 import com.google.common.collect.ImmutableList;
 import com.google.common.net.HostSpecifier;
 import com.google.gson.FieldNamingPolicy;
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.reflect.TypeToken;
 import com.metabroadcast.common.http.HttpException;
 import com.metabroadcast.common.http.HttpResponsePrologue;
 import com.metabroadcast.common.http.HttpResponseTransformer;
 import com.metabroadcast.common.http.HttpStatusCode;
 import com.metabroadcast.common.http.SimpleHttpClient;
 import com.metabroadcast.common.http.SimpleHttpClientBuilder;
 import com.metabroadcast.common.http.SimpleHttpRequest;
 import com.metabroadcast.common.media.MimeType;
 import com.metabroadcast.common.query.Selection;
 import com.metabroadcast.common.security.UsernameAndPassword;
 
 public class HttpBackedReduxClient implements ReduxClient {
 
     private static final String MEDIA_TYPE_TV = "tv";
    private static final String MEDIA_TYPE_RADIO = "radio";
     private static final int MEDIA_MAP_CACHE_TIMEOUT_HOURS = 13;
 
     public static final Builder reduxClientForHost(HostSpecifier host) {
         return new Builder(host);
     }
     
     public static final class Builder {
         
         private HostSpecifier host;
         private UsernameAndPassword credentials;
         private String basePath = "/";
        
         public Builder(HostSpecifier host) {
             this.host = host;
         }
         
         public Builder withCredentials(UsernameAndPassword credentials) {
             this.credentials = credentials;
             return this;
         }
         
         public Builder withBasePath(String basePath) {
             this.basePath = basePath;
             return this;
         }
         
         public HttpBackedReduxClient build() {
         	SimpleHttpClientBuilder httpClientBuilder = new SimpleHttpClientBuilder()
         			.withUserAgent(HttpClients.ATLAS_USER_AGENT)
         			.withAcceptHeader(MimeType.APPLICATION_JSON);
         	
         	if (credentials != null) {
         		httpClientBuilder.withPreemptiveBasicAuth(credentials);
         	}
             return new HttpBackedReduxClient(httpClientBuilder.build(), "http://" + host.toString() + basePath);
         }
     }
     
     private final SimpleHttpClient httpClient;
     private final String requestBase;
     
     private final Gson gson;
     
     private LoadingCache<String, Map<String,ReduxMedia>> mediaMapCache = CacheBuilder.newBuilder()
             .expireAfterWrite(MEDIA_MAP_CACHE_TIMEOUT_HOURS, TimeUnit.HOURS)
             .build(
                 new CacheLoader<String, Map<String,ReduxMedia>>() {
     
                     @Override
                     public Map<String, ReduxMedia> load(String key) throws Exception {
                        TypeToken<Map<String, ReduxMedia>> type = new TypeToken<Map<String, ReduxMedia>>() {};

                         if (MEDIA_TYPE_TV.equals(key)) {
                            return getAsType(String.format("%sformats/tv.json", requestBase), type);
                        } else if (MEDIA_TYPE_RADIO.equals(key)) {
                            return getAsType(String.format("%sformats/radio.json", requestBase), type);
                        } else {
                            throw new FetchException("Could not retrieve media map for redux type '"+key+"'");
                         }
                     }
                     });
             
     public HttpBackedReduxClient(SimpleHttpClient httpClient, String requestBase) {
         this.httpClient = httpClient;
         this.requestBase = requestBase;
         this.gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
     }
     
     Map<String, ReduxMedia> getCachedMedia(String forType) throws ExecutionException {
         return mediaMapCache.get(forType);
     }
     
     @Override
     public FullReduxProgramme programmeFor(final String diskRef) throws HttpException, Exception {
         FullReduxProgramme result = getAsType(String.format("%sprogramme/%s/cached.json", requestBase, diskRef), TypeToken.get(FullReduxProgramme.class));
         result.copyMedia(mediaMapCache.get(result.getType()));
         return result;
     }
 
     private <T> T getAsType(final String url, final TypeToken<T> type) throws HttpException, Exception {
         return httpClient.get(SimpleHttpRequest.httpRequestFrom(url, 
             new HttpResponseTransformer<T>() {
                 @Override
                 public T transform(HttpResponsePrologue prologue, InputStream body) throws HttpException, Exception {
                     if(HttpStatusCode.OK.code() == prologue.statusCode()) {
                         return gson.fromJson(new InputStreamReader(body), type.getType());
                     }
                     throw new HttpException(String.format("Request %s failed: %s %s", url, prologue.statusCode(), prologue.statusLine()), prologue);
                 }
             })
         );
     }
 
     @Override
     public List<BaseReduxProgramme> programmesForDay(LocalDate date) throws HttpException, Exception {
         String formattedDate = date.toString(ISODateTimeFormat.date());
         List<BaseReduxProgramme> programmes = getAsType(String.format("%sday/%s", requestBase, formattedDate), new TypeToken<List<BaseReduxProgramme>>() {});
         return ImmutableList.copyOf(programmes);
     }
 
     @Override
     public PaginatedBaseProgrammes latest(Selection selection) throws HttpException, Exception {
         return getAsType(selection.appendToUrl(requestBase + "latest"), TypeToken.get(PaginatedBaseProgrammes.class));
     }
     
 }
