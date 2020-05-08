 package com.amazonaws.services.cloudsearch.service;
 
 import static com.amazonaws.services.cloudsearch.model.enums.CloudSearchQueryParam.*;
 import static com.amazonaws.services.cloudsearch.common.constant.CloudSearchEndpointConstants.*;
 
 import com.amazonaws.services.cloudsearch.model.sdf.SearchDocumentAdd;
 import com.amazonaws.services.cloudsearch.model.sdf.SearchDocumentFormat;
 import com.amazonaws.services.cloudsearch.model.search.SearchResponse;
 import com.amazonaws.services.cloudsearch.model.upload.UploadResponse;
 import org.apache.cxf.interceptor.LoggingInInterceptor;
 import org.apache.cxf.interceptor.LoggingOutInterceptor;
 import org.apache.cxf.jaxrs.client.Client;
 import org.apache.cxf.jaxrs.client.ClientConfiguration;
 import org.apache.cxf.jaxrs.client.WebClient;
 import org.apache.cxf.transport.http.HTTPConduit;
 import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import com.amazonaws.services.cloudsearch.common.exception.ErrorRepresentation;
 import com.amazonaws.services.cloudsearch.rest.CloudSearchReadResource;
 
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.Response;
 import static javax.ws.rs.core.Response.Status.*;
 import java.util.Date;
 import java.util.List;
 
 /**
  * HTTP REST client for consuming AWS Cloud Search services.
  *
  * @author Phillip Read
  */
 @Service("cloudSearchService")
 public class CloudSearchServiceImpl implements CloudSearchService {
 
     /** The Proxy AWS cloud Search. */
     @Autowired(required = true)
     private CloudSearchReadResource proxyCloudSearchRead;
 
     public SearchResponse cloudSearchRead(String q, String returnFields, int start, String facet) {
 
         Date today = new Date();
 
         Client client = WebClient.client(proxyCloudSearchRead)
                 .type("application/json")
                 .accept("application/json")
                 .modified(today, false);
 
         WebClient wc = WebClient.fromClient(client, true);
 
         ClientConfiguration cc = WebClient.getConfig(wc);
         cc.getInFaultInterceptors().add(new LoggingInInterceptor());
         cc.getInInterceptors().add(new LoggingInInterceptor());
         cc.getOutInterceptors().add(new LoggingOutInterceptor());
 
         SearchResponse result = wc.path(ROOT + SEARCH)
                 .query(Q.getName(), q)
                 .query(START.getName(), start)
                 .query(RETURN_FIELDS.getName(), returnFields)
                 .query(FACET.getName(), facet).get(SearchResponse.class);
 
         Response response = wc.getResponse();
         int status = (response == null) ? NO_CONTENT.getStatusCode() : response.getStatus();
 
         if (NO_CONTENT.getStatusCode() == status) {
             return null;
         }
 
         if (OK.getStatusCode() != status) {
             throw new WebApplicationException(Response
                     .status(BAD_REQUEST)
                     .entity(new ErrorRepresentation(status, "Hard Request!"))
                     .build());
         }
 
         return result;
     }
 
     public SearchResponse cloudSearchBooleanQuery(String bq, String returnFields, int start, String facet) {
 
         Date today = new Date();
 
         Client client = WebClient.client(proxyCloudSearchRead)
                 .type("application/json")
                 .accept("application/json")
                 .modified(today, false);
 
         WebClient wc = WebClient.fromClient(client, true);
 
         ClientConfiguration cc = WebClient.getConfig(wc);
         cc.getInFaultInterceptors().add(new LoggingInInterceptor());
         cc.getInInterceptors().add(new LoggingInInterceptor());
         cc.getOutInterceptors().add(new LoggingOutInterceptor());
 
         SearchResponse result = wc.path(ROOT + SEARCH)
                 .query(BQ.getName(), bq)
                 .query(START.getName(), start)
                 .query(RETURN_FIELDS.getName(), returnFields)
                 .query(FACET.getName(), facet).get(SearchResponse.class);
 
         Response response = wc.getResponse();
         int status = (response == null) ? NO_CONTENT.getStatusCode() : response.getStatus();
 
         if (NO_CONTENT.getStatusCode() == status) {
             return null;
         }
 
         if (OK.getStatusCode() != status) {
             throw new WebApplicationException(Response
                     .status(BAD_REQUEST)
                     .entity(new ErrorRepresentation(status, "Hard Request!"))
                     .build());
         }
 
         return result;
     }
 
     public UploadResponse batch(SearchDocumentFormat item) {
         return batch(item.getSearchDocumentAdds());
     }
 
     public UploadResponse batch(List<SearchDocumentAdd> items) {
 
         Date today = new Date();
 
         Client client = WebClient.client(proxyCloudSearchRead)
                 .type("application/json")
                 .accept("application/json")
                 .modified(today, false);
 
         WebClient wc = WebClient.fromClient(client, true);
 
         ClientConfiguration cc = WebClient.getConfig(wc);
         cc.getInFaultInterceptors().add(new LoggingInInterceptor());
         cc.getInInterceptors().add(new LoggingInInterceptor());
         cc.getOutInterceptors().add(new LoggingOutInterceptor());
 
         HTTPConduit http = WebClient.getConfig(wc).getHttpConduit();
         HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
         httpClientPolicy.setConnectionTimeout(36000);
         httpClientPolicy.setAllowChunking(false);
         http.setClient(httpClientPolicy);
 
         Response response = wc.path(ROOT + BATCH).post(items);
 
         int status = (response == null) ? NO_CONTENT.getStatusCode() : response.getStatus();
 
         if (NO_CONTENT.getStatusCode() == status) {
             return null;
         }
 
         return response.readEntity(UploadResponse.class);
     }
 
 }
