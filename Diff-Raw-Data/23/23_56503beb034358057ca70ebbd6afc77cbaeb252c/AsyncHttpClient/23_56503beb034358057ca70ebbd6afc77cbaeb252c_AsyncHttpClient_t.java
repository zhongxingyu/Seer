 /*
  *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  *  Copyright (c) 2011, Janrain, Inc.
  *
  *  All rights reserved.
  *
  *  Redistribution and use in source and binary forms, with or without modification,
  *  are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice, this
  *    list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimer in the documentation and/or
  *    other materials provided with the distribution.
  *  * Neither the name of the Janrain, Inc. nor the names of its
  *    contributors may be used to endorse or promote products derived from this
  *    software without specific prior written permission.
  *
  *
  *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
  *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
  *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  */
 package com.janrain.android.engage.net.async;
 
 import java.io.IOException;
 import java.io.InputStream;
import java.net.InetAddress;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.zip.GZIPInputStream;
 import org.apache.http.Header;
 import org.apache.http.HeaderElement;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpRequest;
 import org.apache.http.HttpRequestInterceptor;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpResponseInterceptor;
 import org.apache.http.HttpStatus;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.entity.ByteArrayEntity;
 import org.apache.http.entity.HttpEntityWrapper;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.protocol.HttpContext;
 import android.os.Handler;
 import android.util.Log;
 import com.janrain.android.engage.JREngage;
 import com.janrain.android.engage.net.JRConnectionManager;
 import com.janrain.android.engage.utils.IOUtils;
 
 /**
  * @internal
  *
  * @class AsyncHttpClient
  * Utility class which performs HTTP operations asynchronously.
  **/
 public final class AsyncHttpClient {
     private static final String TAG = AsyncHttpClient.class.getSimpleName();
     private static final String USER_AGENT = "Mozilla/5.0 (Linux; U; Android 2.2; en-us; Droid Build/FRG22D) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1";
     private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
     private static final String ENCODING_GZIP = "gzip";
 
     private AsyncHttpClient() {}
 
 	public static class HttpSender extends Thread {
 		private static final String TAG = HttpSender.class.getSimpleName();
 
         private String mUrl;
         private List<NameValuePair> mHeaders;
         private byte[] mPostData;
 		private Handler mHandler;
 		private HttpCallbackWrapper mWrapper;
         private DefaultHttpClient mHttpClient;
 
         private void setupHttpClient() {
             HttpParams connectionParams = new BasicHttpParams();
             HttpConnectionParams.setConnectionTimeout(connectionParams, 10000); // ten second timeout
             HttpConnectionParams.setSoTimeout(connectionParams, 10000);
 
             // From the Google IO app:
             mHttpClient = new DefaultHttpClient(connectionParams);
             mHttpClient.addRequestInterceptor(new HttpRequestInterceptor() {
                 public void process(HttpRequest request, HttpContext context) {
                     // Add header to accept gzip content
                     if (!request.containsHeader(HEADER_ACCEPT_ENCODING)) {
                         request.addHeader(HEADER_ACCEPT_ENCODING, ENCODING_GZIP);
                     }
                 }
             });
 
             mHttpClient.addResponseInterceptor(new HttpResponseInterceptor() {
                 public void process(HttpResponse response, HttpContext context) {
                     // Inflate any responses compressed with gzip
                     final HttpEntity entity = response.getEntity();
                     if (entity == null) return;
                     final Header encoding = entity.getContentEncoding();
                     if (encoding != null) {
                         for (HeaderElement element : encoding.getElements()) {
                             if (element.getName().equalsIgnoreCase(ENCODING_GZIP)) {
                                 response.setEntity(new InflatingEntity(response.getEntity()));
                                 break;
                             }
                         }
                     }
                 }
             });
         }
 
         /**
          * @internal 
          * From the Google IO app:
          * Simple {@link HttpEntityWrapper} that inflates the wrapped
          * {@link HttpEntity} by passing it through {@link GZIPInputStream}.
          */
         private static class InflatingEntity extends HttpEntityWrapper {
             public InflatingEntity(HttpEntity wrapped) {
                 super(wrapped);
             }
 
             @Override
             public InputStream getContent() throws IOException {
                 return new GZIPInputStream(wrappedEntity.getContent());
             }
 
             @Override
             public long getContentLength() {
                 return -1;
             }
         }
 
         public HttpSender(JRConnectionManager.ConnectionData connectionData,
                           Handler handler, HttpCallbackWrapper wrapper) {
             mUrl = connectionData.getRequestUrl();
             mHeaders = connectionData.getRequestHeaders();
             mPostData = connectionData.getPostData();
             mHandler = handler;
             mWrapper = wrapper;
         }
 
 		public void run() {
 			JREngage.logd(TAG, "[run] BEGIN, URL: " + mUrl);
 
             setupHttpClient();
 
             try {
                 //try { Thread.sleep(5000); } catch (InterruptedException e) {}
 
                 HttpUriRequest request;
                 if (mPostData != null) {
                     request = new HttpPost(mUrl);
                     ((HttpPost) request).setEntity(new ByteArrayEntity(mPostData));
                     request.addHeader("Content-Type", "application/x-www-form-urlencoded");
                     request.addHeader("Content-Language", "en-US");
                 } else {
                     request = new HttpGet(mUrl);
                 }
 
                InetAddress ia = InetAddress.getByName(request.getURI().getHost());
                JREngage.logd(TAG, "Connecting to: " + ia.getHostAddress());

                 request.addHeader("User-Agent", USER_AGENT);
                 if (mHeaders == null) mHeaders = new ArrayList<NameValuePair>();
                 for (NameValuePair header : mHeaders) request.addHeader(header.getName(), header.getValue());
 
                 HttpResponse response = mHttpClient.execute(request);
 
                /* Fetching the status code ensures that the response interceptor has a chance to un-gzip the
                 * entity before we fetch it.
                 */
                 response.getStatusLine().getStatusCode();
 
                 HttpResponseHeaders headers = HttpResponseHeaders.fromResponse(response, request);
 
                 HttpEntity entity = response.getEntity();
                 byte[] data = entity == null? new byte[0] : IOUtils.readFromStream(entity.getContent(), true);
                 String dataString = new String(data);
                 if (entity != null) entity.consumeContent();
 
                 switch (response.getStatusLine().getStatusCode()) {
                 case HttpStatus.SC_OK:
                     JREngage.logd(TAG, "[run] HTTP_OK");
                     JREngage.logd(TAG, "[run] headers: " + headers.toString());
                     JREngage.logd(TAG, "[run] data for " + mUrl + ": " +
                             dataString.substring(0, Math.min(dataString.length(), 600)));
                     mWrapper.setResponse(new AsyncHttpResponse(mUrl, headers, data));
                     break;
                 case HttpStatus.SC_NOT_MODIFIED:
                     JREngage.logd(TAG, "[run] HTTP_NOT_MODIFIED");
                     mWrapper.setResponse(new AsyncHttpResponse(mUrl, headers, data));
                     break;
                 case HttpStatus.SC_CREATED:
                     // Response from the Engage trail creation and maybe URL shortening calls
                     JREngage.logd(TAG, "[run] HTTP_CREATED");
                     mWrapper.setResponse(new AsyncHttpResponse(mUrl, headers, data));
                     break;
                 default:
                    // This shouldn't be globbed together, but instead be structured
                     // to allow the error handler to make meaningful use of the web
                     // servers response (contained in String r)
                     String message = "[run] Unexpected HTTP response:  [responseCode: "
                             + response.getStatusLine().getStatusCode() + " | reasonPhrase: "
                             + response.getStatusLine().getReasonPhrase() + " | entity: "
                             + dataString;
 
                     Log.e(TAG, message);
 
                     mWrapper.setResponse(new AsyncHttpResponse(mUrl, new Exception(message)));
                 }
 
                 mHandler.post(mWrapper);
                 return;
             } catch (IOException e) {
                Log.e(TAG, "[run] Problem executing HTTP request. (" + e +")", e);
                 Log.e(TAG, this.toString());
                 mWrapper.setResponse(new AsyncHttpResponse(mUrl, e));
                 mHandler.post(mWrapper);
             }
 		}
 
         public String toString() {
             if (mPostData == null) mPostData = new byte[0];
             return TAG + ": {url: " + mUrl + "\nheaders: " + mHeaders + "\npostData: "
                     + new String(mPostData);
         }
 	}
 
 	/**
      * @internal
 	 * Sends full response (or exception) back to the listener.
 	 */
 	public static class HttpCallbackWrapper implements Runnable {
 		private static final String TAG = HttpCallbackWrapper.class.getSimpleName();
 
 		private AsyncHttpResponseListener mListener;
 		private AsyncHttpResponse mResponse;
         private JRConnectionManager.ConnectionData mConnectionData;
 
 		public HttpCallbackWrapper(AsyncHttpResponseListener listener,
                                    JRConnectionManager.ConnectionData cd) {
             mConnectionData = cd;
 			mListener = listener;
 		}
 
 		public void run() {
 			mListener.onResponseReceived(mResponse);
 		}
 
 		public void setResponse(AsyncHttpResponse holder) {
 			mResponse = holder;
             mResponse.setConnectionData(mConnectionData);
 			JREngage.logd(TAG, "[setResponse] response set.");
 		}
 	}
 
     /**
      * @internal
      *
      * @interface AsyncHttpResponseListener
      * Interface used to define behavior for listening to asynchronous HTTP responses.
      */
     public interface AsyncHttpResponseListener {
 
         /**
          * Method invoked when a response from an asynchronous request is received.
          *
          * @param response
          *      The response object returned from the asynchronous request.  This object will contain either the
          *      the response headers and data or if an issue occurs, an exception detailing the issue encountered.
          */
         void onResponseReceived(AsyncHttpResponse response);
     }
 }
