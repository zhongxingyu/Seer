 package utils.pactas;
 
 import com.google.common.base.Charsets;
 import com.google.common.base.Strings;
 import com.google.common.util.concurrent.ListenableFuture;
 import com.ning.http.client.AsyncCompletionHandler;
 import com.ning.http.client.AsyncHttpClient;
 import com.ning.http.client.Response;
 import io.sphere.client.SphereException;
 import io.sphere.internal.request.RequestHolder;
 import io.sphere.internal.request.RequestHolderImpl;
 import io.sphere.internal.util.Log;
 import io.sphere.internal.util.Util;
 import org.codehaus.jackson.map.DeserializationConfig;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.map.ObjectWriter;
 import org.codehaus.jackson.type.TypeReference;
 
 import java.io.IOException;
 import java.util.concurrent.ExecutionException;
 
 public class PactasClient {
     private final ObjectMapper jsonMapper = new ObjectMapper().configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
     private final AsyncHttpClient httpClient = new AsyncHttpClient();
     private final String baseUrl = "http://itero.proto.pactas.com/api/v1";
 
     /** Sets OAuth authorization header for a request. */
     public static AsyncHttpClient.BoundRequestBuilder basicAuth(AsyncHttpClient.BoundRequestBuilder builder) {
         return builder.
                 setHeader("Authorization", "Basic ZG9udXRzQGVtcGhlc3MubmV0OmhhY2sx").  // donuts@emphess.net, hack1
                 setHeader("Content-Type", "application/json");
     }
 
     public Id createCustomer(String paymillToken) {
         return execute(this.<Id>createPost(
                 baseUrl + "/contacts",
                 new NewCustomer(paymillToken)), new TypeReference<Id>() {});
     }
 
     public Id createBillingGroup() {
         return execute(this.<Id>createPost(
                 baseUrl + "/billingGroups",
                 new NewBillingGroup()), new TypeReference<Id>() {});
     }
 
     public Id createContract(String billingGroupdId, String customerId) {
         return execute(this.<Id>createPost(
                baseUrl + "/contact/" + customerId + "/contracts",
                 new NewContract(billingGroupdId, customerId)), new TypeReference<Id>() {});
     }
 
     public Id createUsageData(String contractId, String productId, String variantId, double quantity) {
         return execute(this.<Id>createPost(
                 baseUrl + "/contracts/" + contractId + "/usage",
                 new NewUsageData(new NewUsageData.Usage(productId, variantId, quantity))), new TypeReference<Id>() {});
     }
 
     public void lockContract(String contractId) {
         execute(this.<Object>createPost(
                 baseUrl + "/contracts/" + contractId + "/lock",
                 new Object()), new TypeReference<Object>() {});
     }
 
     public <T> RequestHolder<T> createGet(String url) {
         return new RequestHolderImpl<T>(basicAuth(httpClient.prepareGet(url)));
     }
 
     public <T> RequestHolder<T> createPost(String url, Object payload) {
         ObjectWriter jsonWriter = new ObjectMapper().writer();
         try {
             RequestHolder<T> request = new RequestHolderImpl<T>(basicAuth(httpClient.preparePost(url)));
             return request.setBody(jsonWriter.writeValueAsString(payload));
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 
     public <T> T execute(RequestHolder<T> request, TypeReference<T> jsonParserTypeRef) {
         try {
             return executeAsync(request, jsonParserTypeRef).get();
         } catch(ExecutionException e) {
             throw new RuntimeException(e);
         } catch (InterruptedException e) {
             throw new RuntimeException(e);
         }
     }
 
     public <T> ListenableFuture<T> executeAsync(final RequestHolder<T> request, final TypeReference<T> jsonParserTypeRef) {
         try {
             return request.executeRequest(new AsyncCompletionHandler<T>() {
                 public T onCompleted(Response response) throws Exception {
                     if (Log.isDebugEnabled()) {
                         Log.debug(requestHolderToString(request) + "=> " +
                                   response.getStatusCode() + "\n" +
                                   response.getResponseBody(Charsets.UTF_8.name()));
                                   //Util.prettyPrintJsonStringSecure(response.getResponseBody(Charsets.UTF_8.name())));
                     }
                     return jsonMapper.readValue(response.getResponseBody(Charsets.UTF_8.name()), jsonParserTypeRef);
                 }
             });
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
 
     private static <T> String requestHolderToString(RequestHolder<T> requestHolder) {
         try {
             return requestHolder.getMethod() + " " +
                    requestHolder.getUrl() +
                    (Strings.isNullOrEmpty(requestHolder.getBody()) ?
                            "" :
                            "\n" + Util.prettyPrintJsonStringSecure(requestHolder.getBody())) +
                    "\n";
         } catch(IOException e) {
             throw new SphereException(e);
         }
     }
 }
