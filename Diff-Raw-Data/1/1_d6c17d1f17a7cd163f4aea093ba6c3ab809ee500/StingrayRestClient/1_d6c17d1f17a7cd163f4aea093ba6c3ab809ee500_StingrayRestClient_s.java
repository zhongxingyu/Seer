 package org.rackspace.stingray.client;
 
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.UniformInterfaceException;
 import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
 import com.sun.jersey.client.apache.ApacheHttpClient;
 import org.rackspace.stingray.client.config.ClientConfigKeys;
 import org.rackspace.stingray.client.config.Configuration;
 import org.rackspace.stingray.client.config.StingrayRestClientConfiguration;
 import org.rackspace.stingray.client.manager.util.StingrayRestClientUtil;
import org.rackspace.stingray.client.pool.PoolUpdate;
 
 import javax.ws.rs.core.MediaType;
 import java.net.URI;
 
 public class StingrayRestClient {
     private ApacheHttpClient client;
     private Configuration config;
 
     public StingrayRestClient() {
         config = new StingrayRestClientConfiguration();
     }
 
     public ClientResponse getResource(String path) throws Exception {
         //Path will be in the client methods. This method should be generic ...
 
         ClientResponse response = null;
         Client client = StingrayRestClientUtil.ClientHelper.createClient();
 
         URI endpoint = URI.create(config.getString(ClientConfigKeys.stingray_rest_endpoint) + config.getString(ClientConfigKeys.stingray_base_uri));
         try {
             client.addFilter(new HTTPBasicAuthFilter(config.getString(ClientConfigKeys.stingray_admin_user), config.getString(ClientConfigKeys.stingray_admin_key)));
             response = client.resource(endpoint + path)
                     .accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
         } catch (UniformInterfaceException ux) {
             throw ux;
         }
 
         return response;
     }
 
     public ClientResponse updatePool(String path, PoolUpdate pool) throws Exception {
         //Path will be in the client methods. This method should be generic possibly ...
         ClientResponse response = null;
         Client client = StingrayRestClientUtil.ClientHelper.createClient();
 
         URI endpoint = URI.create(config.getString(ClientConfigKeys.stingray_rest_endpoint) + config.getString(ClientConfigKeys.stingray_base_uri));
         try {
             client.addFilter(new HTTPBasicAuthFilter(config.getString(ClientConfigKeys.stingray_admin_user), config.getString(ClientConfigKeys.stingray_admin_key)));
             response = client.resource(endpoint + path).type(MediaType.APPLICATION_JSON)
                     .accept(MediaType.APPLICATION_JSON).entity(pool).put(ClientResponse.class);
         } catch (UniformInterfaceException ux) {
             throw ux;
         }
 
         return response;
     }
 }
