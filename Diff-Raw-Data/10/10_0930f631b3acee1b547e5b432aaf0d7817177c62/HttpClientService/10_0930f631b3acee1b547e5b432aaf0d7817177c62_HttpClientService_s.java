 package com.nityankhanna.androidutils.http;
 
 import android.os.AsyncTask;
 
 import org.apache.http.Header;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.BasicHttpParams;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created by Nityan Khanna on 01/07/13.
  */
 
 /**
  * A utility class to execute and handle HTTP requests and responses.
  */
 public final class HttpClientService {
 
 	private List<HttpCookie> cookies;
 	private OnHttpResponseListener delegate;
 	private List<HttpHeader> headers;
 	private List<HttpParameter> params;
 	private HttpRequestMessage requestMessage;
 	private RequestType requestType;
 	private URI url;
 
 	/**
 	 * Initializes a new instance of the HttpClientService class with a specified context, URL, request type and response listener.
 	 *
 	 * @param requestMessage The request message.
 	 * @param response       The response listener used to listen for the HTTP response.
 	 */
 	public HttpClientService(HttpRequestMessage requestMessage, OnHttpResponseListener response) {
 		this.requestMessage = requestMessage;
 
 		try {
 			this.url = new URI(requestMessage.getUrl());
 		} catch (URISyntaxException e) {
 			throw new IllegalArgumentException("Bad URL: " + url);
 		}
 
 		this.requestType = requestMessage.getRequestType();
 
 		if (response == null) {
 			throw new IllegalArgumentException("The response parameter cannot be null");
 		}
 
 		this.delegate = response;
 
 		if (requestMessage.containsCookies()) {
 			cookies = requestMessage.getCookies();
 		} else {
 			cookies = new ArrayList<HttpCookie>();
 		}
 
 		if (requestMessage.containsHeaders()) {
 			headers = requestMessage.getHeaders();
 		} else {
 			headers = new ArrayList<HttpHeader>();
 		}
 
 		if (requestMessage.containsParameters()) {
 			params = requestMessage.getParams();
 		}
 	}
 
 	/**
 	 * Executes an HTTP request on a background thread.
 	 */
 	public void executeRequestAsync() {
 		new HttpClientTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
 	}
 
 	private class HttpClientTask extends AsyncTask<Void, Void, HttpResponse> {
 
 		private DefaultHttpClient client;
 
 		@Override
 		protected HttpResponse doInBackground(Void... voids) {
 
 			client = new DefaultHttpClient();
 			HttpResponse httpResponse;
 
 			switch (requestType) {
 
 				case GET:
 					httpResponse = executeGetRequest();
 					break;
 
 				case POST:
 					httpResponse = executePostRequest();
 					break;
 
 				case PUT:
 					httpResponse = executePutRequest();
 					break;
 
 				case DELETE:
 					httpResponse = executeDeleteRequest();
 					break;
 
 				default:
 					throw new RuntimeException("Invalid request type");
 			}
 
 			return httpResponse;
 		}
 
 		@Override
 		protected void onPostExecute(HttpResponse httpResponse) {
 			super.onPostExecute(httpResponse);
 
			String reasonPhrase = httpResponse.getStatusLine().getReasonPhrase();
			int statusCode = httpResponse.getStatusLine().getStatusCode();
 			HttpEntity entity = httpResponse.getEntity();
 			Header[] basicHeaders = httpResponse.getAllHeaders();
 
 			List<HttpHeader> httpHeaders = new ArrayList<HttpHeader>();
 
 			for (Header header : basicHeaders) {
 				HttpHeader httpHeader = new HttpHeader(header.getName(), header.getValue());
 				httpHeaders.add(httpHeader);
 			}
 
 			HttpResponseMessage responseMessage = new HttpResponseMessage(statusCode, reasonPhrase, entity, httpHeaders);
 			responseMessage.setContentType(new HttpHeader(entity.getContentType().getName(), entity.getContentType().getValue()));
 			responseMessage.setRequestMessage(requestMessage);
 
 			if (statusCode >= 500) {
 				ErrorResponse error = new ErrorResponse();
 
 				error.setMessage(statusCode + " " + reasonPhrase);
 				responseMessage.setError(error);
 
 				delegate.onServerError(responseMessage);
 			} else if (statusCode >= 400) {
 				ErrorResponse error = new ErrorResponse();
 
 				error.setMessage(statusCode + " " + reasonPhrase);
 				responseMessage.setError(error);
 
 				delegate.onClientError(responseMessage);
 			} else {
 
 				switch (requestType) {
 
 					case GET:
 						delegate.onGetCompleted(responseMessage);
 						break;
 
 					case POST:
 						delegate.onPostCompleted(responseMessage);
 						break;
 
 					case PUT:
 						delegate.onPutCompleted(responseMessage);
 						break;
 
 					case DELETE:
 						delegate.onDeleteCompleted(responseMessage);
 						break;
 
 					default:
 						throw new RuntimeException("Invalid request type");
 				}
 			}
 		}
 
 		private HttpResponse executeDeleteRequest() {
 
 			HttpDelete delete = new HttpDelete(url);
 			delete.setHeaders(headers.toArray(new Header[headers.size()]));
 
 			HttpResponse httpResponse = null;
 
 			try {
 				httpResponse = client.execute(delete);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 
 			return httpResponse;
 		}
 
 		private HttpResponse executeGetRequest() {
 
 			HttpGet get = new HttpGet(url);
 			get.setHeaders(headers.toArray(new Header[headers.size()]));
 
 			HttpResponse httpResponse = null;
 
 			try {
 				httpResponse = client.execute(get);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 
 			return httpResponse;
 		}
 
 		private HttpResponse executePostRequest() {
 
 			HttpPost post = new HttpPost(url);
 			post.setHeaders(headers.toArray(new Header[headers.size()]));
 
 			HttpResponse httpResponse = null;
 
 			try {
 
 				BasicHttpParams basicHttpParams = new BasicHttpParams();
 
 				for (HttpParameter parameter : params) {
 					basicHttpParams.setParameter(parameter.getName(), parameter.getValue());
 				}
 
 				post.setParams(basicHttpParams);
 				httpResponse = client.execute(post);
 			} catch (UnsupportedEncodingException e) {
 				e.printStackTrace();
 			} catch (ClientProtocolException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 
 			return httpResponse;
 		}
 
 		private HttpResponse executePutRequest() {
 
 			HttpPut put = new HttpPut(url);
 			put.setHeaders(headers.toArray(new Header[headers.size()]));
 
 			HttpResponse httpResponse = null;
 
 			try {
 
 				BasicHttpParams basicHttpParams = new BasicHttpParams();
 
 				for (HttpParameter parameter : params) {
 					basicHttpParams.setParameter(parameter.getName(), parameter.getValue());
 				}
 
 				put.setParams(basicHttpParams);
 				httpResponse = client.execute(put);
 			} catch (UnsupportedEncodingException e) {
 				e.printStackTrace();
 			} catch (ClientProtocolException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 
 			return httpResponse;
 		}
 	}
 }
