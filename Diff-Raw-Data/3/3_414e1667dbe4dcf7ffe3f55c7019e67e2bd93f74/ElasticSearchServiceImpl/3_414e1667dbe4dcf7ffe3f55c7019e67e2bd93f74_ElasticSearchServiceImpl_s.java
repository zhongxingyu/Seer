 package md.frolov.legume.client.elastic;
 
 import java.util.Iterator;
 import java.util.Set;
 import java.util.logging.Logger;
 import javax.inject.Inject;
 import javax.inject.Singleton;
 
 import com.google.common.collect.Sets;
 import com.google.gwt.http.client.Request;
 import com.google.gwt.http.client.RequestBuilder;
 import com.google.gwt.http.client.RequestCallback;
 import com.google.gwt.http.client.Response;
 import com.google.web.bindery.autobean.shared.AutoBean;
 import com.google.web.bindery.autobean.shared.AutoBeanCodex;
 import com.google.web.bindery.autobean.shared.Splittable;
 import com.google.web.bindery.autobean.shared.impl.StringQuoter;
 
 import md.frolov.legume.client.Constants;
 import md.frolov.legume.client.elastic.api.Callback;
 import md.frolov.legume.client.elastic.api.ESRequest;
 import md.frolov.legume.client.elastic.api.ESResponse;
 import md.frolov.legume.client.elastic.model.ModelFactory;
 import md.frolov.legume.client.elastic.model.reply.WrappedMap;
 import md.frolov.legume.client.service.ConfigurationService;
 
 @Singleton
 public class ElasticSearchServiceImpl implements ElasticSearchService, Constants
 {
 
     private final static Logger LOG = Logger.getLogger("ElasticSearchServiceImpl");
 
     private final Set<Request> requests = Sets.newHashSet();
 
     @Inject
     private ConfigurationService configurationService;
 
     @Inject
     private ModelFactory modelFactory;
 
     @Override
     public <REQUEST extends ESRequest<QUERY, REPLY, RESPONSE>, RESPONSE extends ESResponse<REPLY>, QUERY, REPLY> void query(final REQUEST request, final Callback<REQUEST, RESPONSE> callback)
     {
         try
         {
             final String url = configurationService.get(ELASTICSEARCH_SERVER) + request.getUri();
             final RequestBuilder requestBuilder = new RequestBuilder(request.getMethod(), url);
             requestBuilder.setTimeoutMillis(configurationService.getInt(ELASTICSEARCH_TIMEOUT));
 
             final AutoBean<QUERY> queryAutoBean = request.getPayload();
             final String data;
             if(queryAutoBean!=null) {
                 data = AutoBeanCodex.encode(queryAutoBean).getPayload();
             } else {
                 data = null;
             }
 
             Request xhrRequest = requestBuilder.sendRequest(data, new RequestCallback()
             {
                 @Override
                 public void onResponseReceived(final Request xhrRequest, final Response xhrResponse)
                 {
                     if (xhrResponse.getStatusCode() != 200)
                     {
                        throw new IllegalStateException("Invalid response code: " + xhrResponse.getStatusCode());
                     }
 
                     AutoBean<REPLY> replyAutoBean = request.getExpectedAutobeanResponse();
                     final String jsonText;
                     Class superType = replyAutoBean.getType().getSuperclass();
                     if(replyAutoBean.as() instanceof WrappedMap){
                         jsonText = "{\"obj\":" + xhrResponse.getText()+"}";
                     } else {
                         jsonText = xhrResponse.getText();
                     }
                     Splittable json = StringQuoter.split(jsonText);
                     AutoBeanCodex.decodeInto(json, replyAutoBean);
                     RESPONSE response = request.getResponse(replyAutoBean.as());
                     callback.onSuccess(request, response);
                 }
 
                 @Override
                 public void onError(final Request request, final Throwable exception)
                 {
                     callback.onFailure(exception);
                 }
             });
             requests.add(xhrRequest);
         }
         catch (Exception e)
         {
             callback.onFailure(e);
         }
     }
 
     @Override
     public void cancelAllRequests()
     {
         LOG.info("Cancel all requests to elasticsearch");
         for (Iterator<Request> iterator = requests.iterator(); iterator.hasNext(); )
         {
             Request request = iterator.next();
             iterator.remove();
         }
     }
 }
