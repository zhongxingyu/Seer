 /*
  * OpenRemote, the Home of the Digital Home.
  * Copyright 2008-2010, OpenRemote Inc.
  *
  * See the contributors.txt file in the distribution for a
  * full listing of individual contributors.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.openremote.android.console.net;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.SocketTimeoutException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpRequestBase;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.openremote.android.console.Constants;
 import org.openremote.android.console.exceptions.ORConnectionException;
 import org.openremote.android.console.util.SecurityUtil;
 
 import android.content.Context;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 
 /**
  * TODO
  *
  * This is responsible for manage the connection of android console to controller
  * 
  * @author handy 2010-04-27
  *
  */
 
 public class ORConnection
 {
 
   // Constants ------------------------------------------------------------------------------------
 
   /**
    * Log category for this class with a common OpenRemote prefix.
    */
   public final static String LOG_CATEGORY = Constants.LOG_CATEGORY + "HTTPConnection";
 
 
   public static final int SUCCESS = 1;
   public static final int ERROR = 0;
 
 
   // Instance Fields ------------------------------------------------------------------------------
 
   private HttpClient httpClient;
   private HttpRequestBase httpRequest;
   private HttpResponse httpResponse;
   protected ORConnectionDelegate delegate;
   private Context context;
   protected Handler handler;
 
 
   // Constructors ---------------------------------------------------------------------------------
 
   /**
    * Establish the HttpBasicAuthentication httpconnection depend on param <b>isNeedHttpBasicAuth</b>
    * and param <b>isUseSSLfor</b> with url caller, and then the caller can deal with the
    * httprequest result within ORConnectionDelegate instance.
    *
    * @param context               global Android application context
    * @param httpMethod            enum POST or GET
    * @param useHTTPAuth           indicates whether the HTTP 'Authentication' header should be added
    *                              to the HTTP request
    * @param url                   the URL to connect to
    * @param delegateParam         callback delegate to deal with return values, data and exceptions
    */
   public ORConnection (final Context context, ORHttpMethod httpMethod, boolean useHTTPAuth,
                        String url, ORConnectionDelegate delegateParam)
   {
     initHandler(context);
 
     delegate = delegateParam;
     this.context = context;
 
     HttpParams params = new BasicHttpParams();
     HttpConnectionParams.setConnectionTimeout(params, 4 * 1000);
     HttpConnectionParams.setSoTimeout(params, 5 * 1000);
 
     httpClient = new DefaultHttpClient(params);
 
 
     try
     {
        URL targetUrl = new URL(url);
        targetUrl.toURI();
        if ("https".equals(targetUrl.getProtocol()))
        {
           Scheme sch = new Scheme(targetUrl.getProtocol(), new SelfCertificateSSLSocketFactory(context), targetUrl.getPort());
           httpClient.getConnectionManager().getSchemeRegistry().register(sch);
        }
     }
 
     catch (MalformedURLException e)
     {
        Log.e(LOG_CATEGORY, "Create URL fail:" + url);
        return;
     } catch (URISyntaxException e) {
        Log.e(LOG_CATEGORY, "Could not convert " + url + " to a compliant URI");
        return;
    }
 
     if (ORHttpMethod.POST.equals(httpMethod))
     {
        httpRequest = new HttpPost(url);
     }
     else if (ORHttpMethod.GET.equals(httpMethod))
     {
        httpRequest = new HttpGet(url);
     }
     
     if (httpRequest == null)
     {
        Log.e(LOG_CATEGORY, "Create HttpRequest fail:" + url);
        return;
     }
 
     if (useHTTPAuth)
     {
        SecurityUtil.addCredentialToHttpRequest(context, httpRequest);
     }
 
     execute();
   }
 
 
 
    protected void initHandler(final Context context) {
       handler = new Handler() {
          @Override
          public void handleMessage(Message msg) {
             int message = msg.what;
             if (message == ERROR) {
                connectionDidFailWithException(context, new ORConnectionException("Httpclient execute httprequest fail."));
             } else {
                dealWithResponse();
             }
          }
      };
    }
 
    /** Execute the http request.*/
    public void execute() {
       new Thread(new Runnable() {
          public void run() {
             try {
                httpResponse = httpClient.execute(httpRequest);
                handler.sendEmptyMessage(SUCCESS);
             } catch (SocketTimeoutException e) {
                handler.sendEmptyMessage(ERROR);
             } catch (ClientProtocolException e) {
                handler.sendEmptyMessage(ERROR);
             } catch (IOException e) {
                handler.sendEmptyMessage(ERROR);
             } catch (IllegalStateException e) {
                 handler.sendEmptyMessage(ERROR);
            } catch (IllegalArgumentException e) {
                handler.sendEmptyMessage(ERROR);
             }
          }
       }).start(); 
    }
    
    /** Deal with the response while httpconnection of android console to controller success. */
    protected void dealWithResponse() {
       connectionDidReceiveResponse();
       connectionDidReceiveData();
    }
    
    /**  delegate methods of self */
    
    /** 
     * This method is invoked by self while the connection of android console to controller was failed,
     * and sends a notification to delegate with <b>urlConnectionDidFailWithException</b> method calling.
     */
    protected void connectionDidFailWithException(Context context, ORConnectionException e) {
       delegate.urlConnectionDidFailWithException(e);
    }
    
    /** 
     * This method is invoked by self while the connection of android console to controller received response,
     * and sends a notification to delegate with <b>urlConnectionDidReceiveResponse</b> method calling.
     */
    private void connectionDidReceiveResponse() {
       delegate.urlConnectionDidReceiveResponse(httpResponse);
    }
    
    /**
     * This method is invoked by self while the connection of android console to controller received data,
     * and sends a notification to delegate with <b>urlConnectionDidReceiveData</b> method calling.
     */
    private void connectionDidReceiveData() {
       try {
          if (httpResponse.getStatusLine().getStatusCode() == Constants.HTTP_SUCCESS) {
             delegate.urlConnectionDidReceiveData(httpResponse.getEntity().getContent());
          } else {
             Log.e(LOG_CATEGORY, "Get the entity's content of httpresponse fail.");
          }
       } catch (IllegalStateException e) {
          Log.e(LOG_CATEGORY, "Get the entity's content of httpresponse fail.", e);
       } catch (IOException e) {
          Log.e(LOG_CATEGORY, "Get the entity's content of httpresponse fail.", e);
       }
    }
    
   /**
    * TODO
    *
    * Establish the httpconnection with url for caller and then the caller can deal with the
    * httprequest result within ORConnectionDelegate instance. if check failed, return null.
    *
    * @param context         global Android application context
    * @param httpMethod      enum POST or GET
    * @param url             the URL to connect to
    * @param useHTTPAuth     indicates whether the HTTP 'Authentication' header should be added
    *                        to the HTTP request
    *
    * @return TODO
    *
    * @throws IOException  if the URL cannot be resolved by DNS (UnknownHostException),
    *                      if the URL was not correctly formed (MalformedURLException),
    *                      if the connection timed out (SocketTimeoutException),
    *                      there was an error in the HTTP protocol (ClientProtocolException),
    *                      or any other IO error occured (generic IOException)
    */
   public static HttpResponse checkURLWithHTTPProtocol(Context context, ORHttpMethod httpMethod,
                                                       String urlString, boolean useHTTPAuth)
       throws IOException
   {
     // TODO : could move this method to ORNetworkCheck class, no one else is using it.
     //
     // TODO : use URL in the API instead of string
     //
     // Validate the URL by creating a proper URL instance from the string... it will throw
     // an MalformedURLException (IOException) in case the URL was invalid.
     //
     // This can go away when the API is fixed...
 
     URL targetURL = new URL(urlString);
     URI targetURI;
 
     try
     {
       targetURI = targetURL.toURI();
     }
     catch (URISyntaxException e)
     {
       // Not sure if we're ever going to hit this, but in case we do, just convert to
       // MalformedURLException...
 
       throw new MalformedURLException(
           "Could not convert " + urlString + " to a compliant URI: " + e.getMessage()
       );
     }
 
     HttpRequestBase request = null;
     HttpResponse response = null;
 
     HttpParams params = new BasicHttpParams();
 
     // TODO : seems like timeouts ought to be externalized...
 
     HttpConnectionParams.setConnectionTimeout(params, 5 * 1000);
     HttpConnectionParams.setSoTimeout(params, 5 * 1000);
 
     HttpClient client = new DefaultHttpClient(params);
 
     switch (httpMethod)
     {
       case POST:
         request = new HttpPost(targetURI);
         break;
 
       case GET:
         request = new HttpGet(targetURI);
         break;
 
       default:
         throw new IOException("Unsupported HTTP Method: " + httpMethod);
     }
 
 
     if (useHTTPAuth)
     {
        SecurityUtil.addCredentialToHttpRequest(context, request);
     }
 
 
     if ("https".equals(targetURL.getProtocol()))
     {
       Scheme sch = new Scheme(
           targetURL.getProtocol(),
           new SelfCertificateSSLSocketFactory(context),
           targetURL.getPort()
       );
 
       client.getConnectionManager().getSchemeRegistry().register(sch);
     }
 
    try
    {
    	response = client.execute(request);
    }
    catch (IllegalArgumentException e) {
        throw new MalformedURLException(
                "Illegal argument: " + e.getMessage()
            );
	}
 
     return response;
   }
 
 }
