 package com.pocketsunited.facebook.service;
 
 import com.pocketsunited.facebook.data.AbstractOpenGraphObject;
 import com.pocketsunited.facebook.protocol.data.IdResponse;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.util.EntityUtils;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author Michael Duergner <michael@pocketsunited.com>
  */
 public class FacebookOpenGraphService extends AbstractFacebookGraphService implements IFacebookOpenGraphService {
 
     @Override
     public String publish(String accessToken, AbstractOpenGraphObject openGraphObject) {
         try {
             HttpPost request = new HttpPost(buildRequestUrl(new String[] {"me",openGraphObject.namespace()+":"+openGraphObject.name()},null));
             List<NameValuePair> data = new ArrayList<NameValuePair>();
             data.add(new BasicNameValuePair("access_token",accessToken));
             if (openGraphObject.explicitlyShared()) {
                 data.add(new BasicNameValuePair("fb:explicitly_shared","true"));
             }
             if (null != openGraphObject.message()) {
                 data.add(new BasicNameValuePair("message",openGraphObject.message()));
             }
             if (null != openGraphObject.ref()) {
                 data.add(new BasicNameValuePair("ref",openGraphObject.ref()));
             }
             if (0 < openGraphObject.tags().length) {
                 StringBuilder tags = null;
                 for (String tag : openGraphObject.tags()) {
                     if (null == tags) {
                         tags = new StringBuilder();
                     }
                     else {
                         tags.append(",");
                     }
                     tags.append(tag);
                 }
                 data.add(new BasicNameValuePair("tags",tags.toString()));
             }
             if (0 < openGraphObject.images().length) {
                 boolean userGenerated = false;
                 for (int i=0; i< openGraphObject.images().length; i++) {
                     data.add(new BasicNameValuePair("image["+i+"][url]",openGraphObject.images()[i].url()));
                     data.add(new BasicNameValuePair("image[\"+i+\"][user_generated]",openGraphObject.images()[i].userGenerated() ? "true" : "false"));
                     userGenerated |= openGraphObject.images()[i].userGenerated();
                 }
                 if (userGenerated) {
                     data.add(new BasicNameValuePair("user_generated","true"));
                 }
             }
             for (Map.Entry<String,String> entry : openGraphObject.getPostParameters().entrySet()) {
                 data.add(new BasicNameValuePair(entry.getKey(),entry.getValue()));
             }
            HttpEntity entity = new UrlEncodedFormEntity(data);
             System.out.println(EntityUtils.toString(entity));
             request.setEntity(entity);
 
             HttpResponse response = httpClient.execute(request);
             if (200 == response.getStatusLine().getStatusCode()) {
                 IdResponse idResponse = objectMapper.readValue(response.getEntity().getContent(),IdResponse.class);
                 if (null != idResponse) {
                     return idResponse.getId();
                 }
             }
             else {
                 logger.warn("Got unexpected response code {} with status line {} for request with url {}", new Object[] {response.getStatusLine().getStatusCode(),response.getStatusLine(),request.getURI().toString()});
                 logger.warn(EntityUtils.toString(response.getEntity()));
             }
         }
         catch (Throwable t) {
             logger.error(t.getMessage(),t);
         }
         return null;
     }
 }
