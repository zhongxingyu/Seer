 package org.jcouchdb.db;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpVersion;
 import org.apache.http.auth.AuthScope;
 import org.apache.http.auth.Credentials;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.client.methods.HttpRequestBase;
 import org.apache.http.client.params.HttpClientParams;
 import org.apache.http.conn.ClientConnectionManager;
 import org.apache.http.conn.params.ConnManagerPNames;
 import org.apache.http.conn.params.ConnPerRouteBean;
 import org.apache.http.conn.scheme.PlainSocketFactory;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.scheme.SchemeRegistry;
 import org.apache.http.conn.scheme.SocketFactory;
 import org.apache.http.entity.ByteArrayEntity;
 import org.apache.http.entity.InputStreamEntity;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.params.HttpProtocolParams;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.HttpContext;
 import org.jcouchdb.exception.CouchDBException;
 import org.jcouchdb.util.Assert;
 import org.jcouchdb.util.ExceptionWrapper;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.svenson.JSON;
 
 /**
  * Default implementation of the {@link Server} interface.
  * 
  * @author fforw at gmx dot de
  */
 public class ServerImpl
     implements Server
 {
     private static final String CHARSET = "UTF-8";
 
     protected static Logger log = LoggerFactory.getLogger(ServerImpl.class);
 
     private ClientConnectionManager clientConnectionManager;
 
     private AuthScope authScope;
 
     private Credentials credentials;
 
     private HttpContext context;
 
     private String serverURI;
 
     private volatile DefaultHttpClient httpClient;
 
     private int maxConnectionsPerRoute = 10;
 
     private int maxTotalConnections = 25;
 
     private volatile boolean shutdown;
     
     public void setMaxConnectionsPerRoute(int maxConnectionsPerRoute)
     {
         this.maxConnectionsPerRoute = maxConnectionsPerRoute;
     }
     
     public void setMaxTotalConnections(int maxTotalConnections)
     {
         this.maxTotalConnections = maxTotalConnections;
     }
     
     public ServerImpl(String host)
     {
        this(host, DEFAULT_PORT);
     }
 
     public ServerImpl(String host, int port)
     {
        this.serverURI = "http://" + host + ":" + port;
     }
 
     private DefaultHttpClient getHttpClient()
     {
         if (httpClient == null)
         {
             synchronized(this)
             {
                 if (httpClient == null)
                 {
                     SchemeRegistry supportedSchemes = new SchemeRegistry();
                     SocketFactory sf = PlainSocketFactory.getSocketFactory();
                     supportedSchemes.register(new Scheme("http", sf, 80));
             
                     HttpParams params = new BasicHttpParams();
                     HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
                     HttpProtocolParams.setUseExpectContinue(params, false);
                     HttpClientParams.setRedirecting(params, false);
                     params.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE, new ConnPerRouteBean(maxConnectionsPerRoute));
             
                     params.setParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, maxTotalConnections);
             
                     context = new BasicHttpContext();
                     clientConnectionManager = new ThreadSafeClientConnManager( params, supportedSchemes);
                     httpClient = new DefaultHttpClient(clientConnectionManager, params);
                     if (authScope != null)
                     {
                         httpClient.getCredentialsProvider().setCredentials(authScope, credentials);
                     }
                 }
             }
         }
         return httpClient;
     }
 
     private final Response execute( HttpRequestBase request ) throws ClientProtocolException, IOException 
     {
         HttpResponse res = getHttpClient().execute( request, context );
         return new Response( res );
     }
 
 
     private Response executePut(HttpPut put)
     {
         try
         {
             return execute(put);
         }
         catch (IOException e)
         {
             put.abort();
             throw ExceptionWrapper.wrap(e);
         }
     }
 
     
     /**
      * {@inheritDoc}
      */
     public List<String> listDatabases()
     {
         Response resp = null;
         try
         {
             resp = get("/_all_dbs");
             if (!resp.isOk())
             {
                 throw new CouchDBException("Error listing databases: " + resp);
             }
             return resp.getContentAsList();
         }
         finally
         {
             if (resp != null)
             {
                 resp.destroy();
             }
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean createDatabase(String name)
     {
         Response resp = null;
         try
         {
             resp = put("/" + name + "/");
             if (resp.isOk())
             {
                 return true;
             }
             else
             {
                 if (resp.getCode() == 412 || resp.getCode() == 500)
                 {
                     return false;
                 }
                 else
                 {
                     throw new CouchDBException("Error creating database: " + resp);
                 }
             }
         }
         finally
         {
             if (resp != null)
             {
                 resp.destroy();
             }
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void deleteDatabase(String name)
     {
         Response resp = null;
         try
         {
             resp = delete("/" + name + "/");
             if (!resp.isOk())
             {
                 throw new CouchDBException("Cannot delete database " + name + ": " + resp);
             }
         }
         finally
         {
             if (resp != null)
             {
                 resp.destroy();
             }
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public Response get(String uri)
     {
         if (log.isDebugEnabled())
         {
             log.debug("GET " + uri);
         }
 
         HttpGet get = new HttpGet( serverURI + uri );
         
         try
         {
             return execute( get );
         }
         catch (IOException e) {
             get.abort();
             throw ExceptionWrapper.wrap(e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public Response put(String uri)
     {
         return put(uri, null);
     }
 
     /**
      * {@inheritDoc}
      */
     public Response put(String uri, String body)
     {
         if (log.isDebugEnabled())
         {
             log.debug("PUT " + uri + ", body = " + body);
         }
         HttpPut put = new HttpPut( serverURI + uri );
         if (body != null) {
             try {
                 StringEntity reqEntity = new StringEntity( body , "UTF-8");
                 reqEntity.setContentType("application/json");
                 reqEntity.setContentEncoding( CHARSET );
                 put.setEntity( reqEntity );
             }
             catch (UnsupportedEncodingException e) {
                 throw ExceptionWrapper.wrap(e);
             }
         }
 
         return executePut( put );        
     }
 
     /**
      * {@inheritDoc}
      */
     public Response put(String uri, byte[] body, String contentType)
     {
         if (log.isDebugEnabled())
         {
             log.debug("PUT " + uri + ", body = " + body);
         }
 
         HttpPut put = new HttpPut(serverURI + uri);
         if (body != null)
         {
             ByteArrayEntity reqEntity = new ByteArrayEntity(body);
             reqEntity.setContentType(contentType);
             put.setEntity(reqEntity);
         }
 
         return executePut(put);
     }
 
     
     public Response put(String uri, InputStream inputStream, String contentType, long length) throws CouchDBException
     {
         Assert.notNull(inputStream, "inputStream can't be null");
 
         if (log.isDebugEnabled())
         {
             log.debug("PUT " + uri + ", inputStream = " + inputStream);
         }
 
         HttpPut put = new HttpPut(serverURI + uri);
         InputStreamEntity entity = new InputStreamEntity( inputStream, length);
         entity.setContentType(contentType);
         put.setEntity(entity);
         return executePut(put);
     }
 
     /**
      * {@inheritDoc}
      */
     public Response post(String uri, String body)
     {
         if (log.isDebugEnabled())
         {
             log.debug("POST " + uri + ", body = " + body);
         }
 
         HttpPost post = new HttpPost( serverURI + uri );
 
         try
         {
             StringEntity reqEntity = new StringEntity(body, "UTF-8");
             reqEntity.setContentType("application/json");
             reqEntity.setContentEncoding(CHARSET);
             post.setEntity(reqEntity);
 
             return execute(post);
         }
         catch (IOException e)
         {
             post.abort();
             throw ExceptionWrapper.wrap(e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public Response delete(String uri)
     {
         if (log.isDebugEnabled())
         {
             log.debug("DELETE " + uri);
         }
 
         HttpDelete delete = new HttpDelete( serverURI + uri );
 
         try
         {
             return execute(delete);
         }
         catch (IOException e)
         {
             delete.abort();
             throw ExceptionWrapper.wrap(e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void setCredentials(AuthScope authScope, Credentials credentials)
     {
         this.authScope      = authScope;
         this.credentials    = credentials;
         httpClient = null;
     }
 
     public void shutDown()
     {
         if (clientConnectionManager != null)
         {
             clientConnectionManager.shutdown();
         }
         httpClient = null;
         clientConnectionManager = null;
         shutdown = true;
     }
     
     public boolean isShutdown()
     {
         return shutdown;
     }
 
     /**
      * {@inheritDoc}
      */
     public Map<String,Map<String,Object>> getStats(String filter)
     {
         String uri = "/_stats";
         
         if (filter != null)
         {
             uri += filter;
         }
         
         Response resp = get(uri);
         return resp.getContentAsMap();
     }
 
     public ReplicationInfo replicate(String source, String target, boolean continuous)
     {
         Response response = null;
         try
         {
             Map<String,Object> map = new HashMap<String, Object>();
             map.put("source", source);
             map.put("target", target);
             
             if (continuous)
             {
                 map.put("continuous", true);
             }
             
             response = post("/_replicate", JSON.defaultJSON().forValue(map) );
             return response.getContentAsBean(ReplicationInfo.class);
         }
         finally
         {
             if (response != null)
             {
                 response.destroy();
             }
         }
     }
     
     /**
      * Requests a list of uuids from the CouchDB server
      * @param count     number of uuids to request
      * @return
      */
     public List<String> getUUIDs(int count)
     {
         Response response = null;
         try
         {
             response = get("/_uuids?count=" + count );
             Map content = response.getContentAsMap();
             return (List<String>)content.get("uuids");
         }
         finally
         {
             if (response != null)
             {
                 response.destroy();
             }
         }
     }
 }
