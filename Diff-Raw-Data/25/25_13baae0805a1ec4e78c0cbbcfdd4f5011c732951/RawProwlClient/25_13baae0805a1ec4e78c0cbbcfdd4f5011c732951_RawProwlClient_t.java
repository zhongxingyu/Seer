 package org.antbear.jprowl;
 
 import com.google.common.collect.ImmutableList;
 import org.antbear.jprowl.model.ProwlResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.validation.constraints.NotNull;
 import java.util.List;
 
 public class RawProwlClient implements ProwlClient {
 
     private Logger log = LoggerFactory.getLogger(RawProwlClient.class);
 
     @NotNull
    private final ProwlContext context;
 
     public RawProwlClient(@NotNull final ProwlContext prowlContext) {
        if (null == prowlContext) {
            throw new IllegalArgumentException("ProwlContext may not be null");
        }
        this.context = prowlContext;
     }
 
     @NotNull
     public ProwlContext getContext() {
         return context;
     }
 
     @NotNull
     private static HttpClient newHttpClientInstance() {
         return new DefaultHttpClient();
     }
 
     @NotNull
     private ProwlResponse execute(@NotNull final String method, @NotNull List<ProwlRequestParameter> parameters) {
         try {
             final HttpClient client = newHttpClientInstance();
             final HttpGet get = new HttpGet(this.context.buildRequestURI(method, parameters));
             log.debug("Executing URL " + get.getURI());
             final ResponseHandler<ProwlResponse> responseHandler = new ProwlResponseHandler();
             final ProwlResponse response = client.execute(get, responseHandler);
             return response;
         } catch (Exception ex) {
             throw new RuntimeException(ex);
         }
     }
 
     @Override
     @NotNull
     public ProwlResponse verifyApiKey(final String apiKey) {
         return execute("verify", ImmutableList.of(new ProwlRequestParameter("apikey", apiKey)));
     }
 
     @Override
     @NotNull
     public ProwlResponse postNotification(@NotNull final ProwlNotification notification) {
         return execute("add", notification.toRequestParameterList());
     }
 }
