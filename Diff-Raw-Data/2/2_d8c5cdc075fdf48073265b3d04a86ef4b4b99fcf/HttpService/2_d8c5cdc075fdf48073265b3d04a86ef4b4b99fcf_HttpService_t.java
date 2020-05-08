 package net.meneame.fisgodroid;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.client.params.ClientPNames;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.HttpContext;
 
 public class HttpService implements IHttpService
 {
     private HttpContext mContext = new BasicHttpContext();
     private HttpClient mClient = new DefaultHttpClient();
     
     @Override
     public String get(String uri)
     {
         ByteArrayOutputStream os = new ByteArrayOutputStream();
         performRequest(new HttpGet(uri), os);
         return new String(os.toByteArray());
     }
     
     @Override
     public boolean get(String uri, OutputStream os)
     {
         return performRequest(new HttpGet(uri), os);
     }
 
     private HttpPost buildPostRequest(String uri, Map<String, Object> params)
     {
         HttpPost req = new HttpPost(uri);
 
         // Set the POST parameters in the HTTP request
         if (params.size() > 0)
         {
             // We need to transform the Map given in the parameter to the
             // implementation-specific
             // list of NameValuePair elements.
             List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(params.size());
             for (Map.Entry<String, Object> entry : params.entrySet())
             {
                 nameValuePairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
             }
 
             try
             {
                req.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
             }
             catch (UnsupportedEncodingException e)
             {
                 e.printStackTrace();
             }
         }
 
         return req;
     }
     
     @Override
     public String post(String uri, Map<String, Object> params)
     {
         ByteArrayOutputStream os = new ByteArrayOutputStream();
         performRequest(buildPostRequest(uri, params), os);
         return new String(os.toByteArray());
     }
     
     @Override
     public boolean post(String uri, Map<String, Object> params, OutputStream os)
     {
         return performRequest(buildPostRequest(uri, params), os);
     }
 
     public boolean performRequest(HttpUriRequest req, OutputStream out)
     {
         try
         {
             mClient.getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
             
             // Perform the request
             HttpResponse response = mClient.execute(req, mContext);
             
             // Get the response data and transform it into a String
             if ( out != null )
             {
                 InputStream content = response.getEntity().getContent();
                 byte[] buffer = new byte[512];
                 int bytesRead;
                 while ((bytesRead = content.read(buffer)) != -1)
                 {
                     out.write(buffer, 0, bytesRead);
                 }
             }
             return true;
         }
         catch ( ClientProtocolException e )
         {
             e.printStackTrace();
         }
         catch ( IOException e )
         {
             e.printStackTrace();
         }
         
         return false;
     }
 }
