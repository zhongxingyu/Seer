 /* Copyright (c) 2010 HomeAway, Inc.
  * All rights reserved.  http://jmonette/github.io/thunderhead
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.homeaway.aws.thunderhead.client;
 
import com.homeaway.aws.thunderhead.model.constants.CloudSearchPath;
 import com.homeaway.aws.thunderhead.model.constants.CloudSearchQueryParam;
 import com.homeaway.aws.thunderhead.model.exceptions.CloudSearchClientException;
 import com.homeaway.aws.thunderhead.model.exceptions.CloudSearchRuntimeException;
 import com.homeaway.aws.thunderhead.model.search.SearchHit;
 import com.homeaway.aws.thunderhead.model.search.SearchHits;
 import com.homeaway.aws.thunderhead.model.search.SearchResponse;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.core.util.MultivaluedMapImpl;
 import org.apache.commons.lang.StringUtils;
 import org.perf4j.aop.Profiled;
 
 import javax.ws.rs.core.MultivaluedMap;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * This class is a client for uploading SDF documents to the cloudsearch domain and querying the cloudsearch domain
  * 
  * @author jmonette
  */
 public class CloudSearchReadClient extends AbstractCloudSearchClient {
 
     /**
      * Default constructor. If this constructor is used then setUpdateWebResource and setQueryWebResource must be called to
      * set the WebResources needed by the client
      */
     public CloudSearchReadClient() {
         super();
     }
 
     /**
      * The WebResource to update the cloudsearch domain with
      *
      * @param updateWebResource the WebResource to update the cloudsearch domain with
      */
     @Override
     public void setUpdateWebResource(final WebResource updateWebResource) {
         throw new CloudSearchRuntimeException("Read client not allowed to update domain", new IllegalAccessException("Read client not allowed to update domain"));
     }
 
     /**
      * The WebResource to query the cloudsearch domain with
      *
      * @param queryWebResource the WebResource to query the cloudsearch domain with
      */
     @Override
     public void setQueryWebResource(final WebResource queryWebResource) {
         this.queryWebResource = queryWebResource;
     }
 
     /**
      * A summarized version of a query that only returns the values that were found and not all the extra CloudSearch
      * metadata
      * 
      * @param queryParams a MultivaluedMap of the query params to use
      * @return a List of MultivaluedMaps that equate to all the entries that were found and their return fields
      * @throws com.homeaway.aws.thunderhead.model.exceptions.CloudSearchClientException if the response did not return a 2XX status code
      */
     public List<MultivaluedMap<String, String>> querySummary(MultivaluedMap<String, String> queryParams) throws CloudSearchClientException {
         return buildSummarizedResponse(query(queryParams));
     }
         
     /**
      * This method queries Amazon and return the results found
      * 
      * @param queryParams a MultivaluedMap of the query params to use
      * @return a SearchResponse object which represents query results
      * @throws CloudSearchClientException if the response did not return a 2XX status code
      */
     @Profiled(tag = "CloudSearchReadClient.query")
     public SearchResponse query(MultivaluedMap<String, String> queryParams) throws CloudSearchClientException {
         ClientResponse clientResponse = null;
         SearchResponse searchResponse = null; 
         
         /* Request xml results from AWS cloudsearch for JAXB conversion */
         MultivaluedMap<String, String> myQueryParams = new MultivaluedMapImpl(queryParams);
         myQueryParams.remove(CloudSearchQueryParam.RESULTS_TYPE);
         myQueryParams.add(CloudSearchQueryParam.RESULTS_TYPE, "xml");
         
         LOGGER.debug("Querying to {} with query params: {}", this.queryWebResource.getURI(), myQueryParams);
         try {
             /* Don't like doing this but apparently jersey has problems with return-fields being in multivalued map */
             List<String> list = myQueryParams.remove(CloudSearchQueryParam.RETURN_FIELDS);
            if (list != null) {
                String[] returnFields = list.toArray(new String[list.size()]);
                // If i don't do the following line the only return field we get is the last one
                myQueryParams.add(CloudSearchQueryParam.RETURN_FIELDS, StringUtils.join(returnFields, ","));
            }
            System.out.println(this.queryWebResource);
            clientResponse = this.queryWebResource//.uri(UriBuilder.fromUri(this.queryWebResource.getURI()).build())
                                                   .path(CLOUDSEARCH_VERSION)
                                                   .path(CloudSearchPath.SEARCH)
                                                   .queryParams(myQueryParams)
                                                   .get(ClientResponse.class);
 
             
             LOGGER.debug("Received a status of {} for query to {}", clientResponse.getStatus(), this.queryWebResource.getURI());            
             checkStatus(clientResponse);
 
             searchResponse = clientResponse.getEntity(SearchResponse.class);
         } catch(RuntimeException re) {
             throw new CloudSearchRuntimeException(re.getMessage(), re);
         } finally {
             if (clientResponse != null) {
                 clientResponse.close();
             }
         }
         
         return searchResponse;
     }
 
     /**
      * Utility method to return summarized response
      * 
      * @param searchResponse The search response from Amazon Cloud Search
      * @return List of MultivaluedMap<String, String> representing the summarized response
      */
     public static List<MultivaluedMap<String, String>> buildSummarizedResponse(SearchResponse searchResponse) {
         List<MultivaluedMap<String, String>> ret = new ArrayList<MultivaluedMap<String, String>>();
         if (searchResponse != null && searchResponse.getFound() != null && searchResponse.getFound().getHits() != null) {
             SearchHits searchHits = searchResponse.getFound();
             for (SearchHit searchHit : searchHits.getHits()) {
                 ret.add(searchHit.getReturnFieldsMap());
             }
         }
         return ret;
     }
 }
