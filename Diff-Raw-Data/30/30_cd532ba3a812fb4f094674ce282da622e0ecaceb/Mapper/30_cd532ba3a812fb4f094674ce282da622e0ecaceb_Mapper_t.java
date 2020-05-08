 /*
  * Copyright 2012 University of Rostock, Institute of Applied Microelectronics and Computer Engineering
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *    http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * 
  * This work has been sponsored by Siemens Corporate Technology. 
  *
  */
 package org.ws4d.coap.proxy;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.Inet4Address;
 import java.net.Inet6Address;
 import java.net.InetAddress;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.Locale;
 import java.util.TimeZone;
 import java.util.concurrent.ArrayBlockingQueue;
 
 import org.apache.http.Header;
 import org.apache.http.HttpRequest;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.HttpVersion;
 import org.apache.http.message.BasicHttpEntityEnclosingRequest;
 import org.apache.http.message.BasicHttpRequest;
 import org.apache.http.message.BasicHttpResponse;
 import org.apache.http.nio.entity.ConsumingNHttpEntityTemplate;
 import org.apache.http.nio.entity.NStringEntity;
 import org.apache.http.util.EntityUtils;
 import org.ws4d.coap.interfaces.CoapRequest;
 import org.ws4d.coap.interfaces.CoapResponse;
 import org.ws4d.coap.messages.BasicCoapRequest;
 import org.ws4d.coap.messages.BasicCoapResponse;
 import org.ws4d.coap.messages.CoapMediaType;
 import org.ws4d.coap.messages.CoapPacketType;
 import org.ws4d.coap.messages.CoapRequestCode;
 import org.ws4d.coap.messages.CoapResponseCode;
 
 /**
  * @author Christian Lerche <christian.lerche@uni-rostock.de>
  * @author Andy Seidel <andy.seidel@uni-rostock.de>
  */
 public class Mapper {
 	
 	static final int DEFAULT_MAX_AGE_MS = 60000; //Max Age Default in ms 
 
 	private static ArrayBlockingQueue<ProxyMessageContext> httpInQueue = new ArrayBlockingQueue<ProxyMessageContext>(100);		
 	private static ArrayBlockingQueue<ProxyMessageContext> httpOutQueue = new ArrayBlockingQueue<ProxyMessageContext>(100);
 	private static ArrayBlockingQueue<ProxyMessageContext> coapInQueue = new ArrayBlockingQueue<ProxyMessageContext>(100);
 	private static ArrayBlockingQueue<ProxyMessageContext> coapOutQueue = new ArrayBlockingQueue<ProxyMessageContext>(100);
 	
 	//introduce other needed classes for communication
 	private CoapClientProxy coapClient;
 	private CoapServerProxy coapServer;
 	private HttpServerNIO httpServerNIO;
 	private HttpClientNIO httpClientNIO;
 	private static ProxyCache cache;	
 	private static HttpRequestHandler httpRequestHandler;
 	private static CoapRequestHandler coapRequestHandler;
 	private static HttpResponseHandler httpResponseHandler;
 	private static CoapResponseHandler coapResponseHandler;
 	
 	private static Mapper instance;
 	
 	/*for statistics*/
 	private int httpRequestCount = 0;
 	private int coapRequestCount = 0;
 	private int servedFromCacheCount = 0;
 	
 
     public synchronized static Mapper getInstance() {
         if (instance == null) {
             instance = new Mapper();
         }
         return instance;
     }
 
     private Mapper() {
 		HttpRequestHandler httprequesthandler = new HttpRequestHandler();
 		httpRequestHandler = httprequesthandler;
 		httprequesthandler.start();
 		
 		CoapResponseHandler coapresponsehandler = new CoapResponseHandler();
 		coapResponseHandler = coapresponsehandler;
 		coapresponsehandler.start();
 		
 		HttpResponseHandler httpresponsehandler = new HttpResponseHandler();
 		httpResponseHandler = httpresponsehandler;
 		httpresponsehandler.start();
 		
 		CoapRequestHandler crh = new CoapRequestHandler();
 		coapRequestHandler = crh;
 		crh.start();
 
 		cache = new ProxyCache();
     }
     
    public void setDefaultCacheTime(int cacheTime){
    	cache.setDefaultCacheTime(cacheTime);
     }
 	
 	//this class handles incoming http-requests; used in cases http-coap, http-http
 	class HttpRequestHandler extends Thread {
 			
 	        public void run() {
 	        	
 	        	this.setName("HttpRequestHandler");
 	        		        	
 	            while (!Thread.interrupted()) {
 	                try {
 	                	
 	                	//wait until there is nothing to do
 	                	synchronized(this) {
 	                        while (httpInQueue.isEmpty())
 	    						try {
 	    							wait();
 	    						} catch (InterruptedException e) {
 	    							e.printStackTrace();
 	    						}
 	                    }	                	
 	                	
 	                	ProxyMessageContext context = httpInQueue.take(); 				//blocking operation
 	                	httpRequestCount++;
 
 					// at this point response is not in cache and should be
 					// mapped to coap-request
 					// do not translate methods: OPTIONS,TRACE,CONNECT
 					// in that case answer "Not Implemented"
 					if (HttpRequestMethodSupported(context.getHttpRequest())) {
 						// perform request-transformation
 						/* try to get from cache */
 						
 						CoapResponse response = cache.getCoapRes(context.getUri());
 						if (response != null){
 							/* answer from cache */
 							context.setCoapResponse(response);
 							context.setFromCache(true); //avoid "rechaching"
 							putCoapResponse(context);
 							servedFromCacheCount++;
 						} else {
 							/* not cached */
 							CoapRequest request = httpRequestToCoapRequest(context.getHttpRequest());
 							
 							if (request != null) {
 								context.setTranslatedCoapRequest(request);
 								coapClient.makeRequest(context);
 							} else {
 								//if something went wrong while translation
 								System.out.println("Error: request transformation error, serving error-message to httpserver");
 								context.setHttpResponse(new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_INTERNAL_SERVER_ERROR, "Internal Server Error"));
 								httpServerNIO.receivedHttpResponse(context);
 							}
 						}
 
 					} else {
 						// if method is unsupported answer with error-code
 						HttpResponse httpResponse = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_NOT_IMPLEMENTED, "Not Implemented");
 						context.setHttpResponse(httpResponse);
 						httpServerNIO.receivedHttpResponse(context);
 					}
 	                } catch (InterruptedException e) {
 	                    e.printStackTrace();
 	                } 
 	            }
 	        }
 	    }
 	
 	//this class handles incoming coap-requests; used in case coap-http
 	class CoapRequestHandler extends Thread {
 			
 	        public void run() {
 	        	this.setName("CoapRequestHandler");
 	            while (!Thread.interrupted()) {
 	                try {
 	                	
 	                	//wait until there is nothing to do
 	                	synchronized(this) {
 	                        while (coapInQueue.isEmpty())
 	    						try {
 	    							wait();
 	    						} catch (InterruptedException e) {
 	    							e.printStackTrace();
 	    						}
 	                    }
 	                    
 	                	ProxyMessageContext context = coapInQueue.take(); 
 	                	coapRequestCount++;
 
 	                	if (context.isTranslate()){
 	                		/* coap to http */
 		                	HttpResponse response = cache.getHttpRes(context.getUri());
 							if (response != null){
 								/* answer from cache */
 								context.setHttpResponse(response);
 								context.setFromCache(true); //avoid "recaching"
 								putHttpResponse(context);
 								System.out.println("served from cache");
 							} else {	                		
 								HttpRequest httpRequest = coapRequestToHttpRequest(context);
 		                		context.setTranslatedHttpRequest(httpRequest);
 		                		httpClientNIO.makeHttpRequest(context);       //send to http-client
 							}
 	                	} else {
 	                		/* coap to coap */
 		                	CoapResponse response = cache.getCoapRes(context.getUri());
 							if (response != null){
 								/* answer from cache */
 								context.setCoapResponse(response);
 								context.setFromCache(true); //avoid "recaching"
 								putCoapResponse(context);
 								System.out.println("served from cache");
 							} else {
 								coapClient.makeRequest(context);				//send to coap-client
 							}	                		
 	                	}
 	                } catch (InterruptedException ex) {
 	                    break;
 	                }
 	            }
 	        }
 	    }
 	
 	//this class handles coap-responses; used in case http-coap
 	class CoapResponseHandler extends Thread {
         
         public void run() {
         	
         	this.setName("CoapResponseHandler");
         	
             while (!Thread.interrupted()) {
                 try {
                 	
                 	synchronized(this) {
                         while (coapOutQueue.isEmpty())
     						try {
     							wait();
     						} catch (InterruptedException e) {    							
     							e.printStackTrace();
     						}
                     }
                     
                 	ProxyMessageContext context = coapOutQueue.take(); 						//blocking	operation 
                 	if(!context.isFromCache()){
                 		/* don't cache already cached elements (loop)*/
                 		cache.put(context);
                 	}
                 	
                 	if (context.isTranslate()){
                 		/* translate to HTTP*/
                 		HttpResponse response;
                 		if (context.getCoapResponse() != null){
                 			/* received no response TODO: implement a better error handling*/
                 			response = coapResponseToHttpResponse(context);	    //translate
                 			if (response == null){                				//error in translation-process, send back error-message
                 				response = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_INTERNAL_SERVER_ERROR, "Internal Server Error");
                 			}
                 		} else {
             				response = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_NOT_FOUND, "Client not reachable");
                 		}
                 		
                 		context.setHttpResponse(response);
                 		httpServerNIO.receivedHttpResponse(context);
                 	} else {
                 		/* just coap to coap */
                 		coapServer.receivedResponse(context);
                 	}
                 } catch (InterruptedException ex) {
                     break;
                 }
             }
         }
     }
 	
 	class HttpResponseHandler extends Thread {
         
         public void run() {
         	
         	this.setName("HttpResponseHandler");
         	
             while (!Thread.interrupted()) {
                 try {
                 	
                 	//wait until there is nothing to do
                 	synchronized(this) {
                         while (httpOutQueue.isEmpty())
     						try {
     							wait();
     						} catch (InterruptedException e) {
     							e.printStackTrace();
     						}
                     }
                 	
                    
                 	ProxyMessageContext context = httpOutQueue.take(); 		//blocking operation
 					CoapResponse coapResponse = httpResponseToCoapResponse(context); // translate
                 	
 					if (coapResponse != null) {
 //						if (cacheEnabled) {
 //							String msgID = new String("" + context.getMsgID());
 //							LinkedList<String> uri = requestToStringlist(req);
 //							if (uri != null) {
 //								cache.put(uri, context);
 //							} else
 //								System.out
 //										.println("Error: uri is null --> nothing cached");
 //						}
 					}
 
 
 					context.setCoapResponse(coapResponse);
 					coapServer.receivedResponse(context);
                 	
                 } catch (InterruptedException ex) {
                     break;
                 }
             }
         }
 	}     
 	
 	//in case of translation from coap-response to http-response
 	//message-code-translation depends on coap-responsecode and http-request-method
 	public static void setHttpMsgCode(CoapResponse coapResponse, String requestMethod, HttpResponse httpResponse) {
 		
 		CoapResponseCode responseCode = coapResponse.getResponseCode();
 		
 		switch(responseCode) {
 //			case OK_200: { //removed from CoAP draft
 //				httpResponse.setStatusCode(HttpStatus.SC_OK);
 //				httpResponse.setReasonPhrase("Ok");
 //				break;
 //			}
 			case Created_201: {
 				if (requestMethod.contains("post") || requestMethod.contains("put")) {				
 					httpResponse.setStatusCode(HttpStatus.SC_CREATED);
 					httpResponse.setReasonPhrase("Created");
 				}
 				else {
 					System.out.println("wrong msgCode for request-method!");
 					httpResponse.setStatusCode(HttpStatus.SC_METHOD_FAILURE);
 					httpResponse.setReasonPhrase("Method Failure");
 				}
 				break;
 			}
 			case Deleted_202: {
 				if (requestMethod.contains("delete")) {				
 					httpResponse.setStatusCode(HttpStatus.SC_NO_CONTENT);
 					httpResponse.setReasonPhrase("No Content");
 				}
 				else {
 					System.out.println("wrong msgCode for request-method!");
 					httpResponse.setStatusCode(HttpStatus.SC_METHOD_FAILURE);
 					httpResponse.setReasonPhrase("Method Failure");
 				}
 				break;
 			}
 			case Valid_203: {
 				httpResponse.setStatusCode(HttpStatus.SC_NOT_MODIFIED);
 				httpResponse.setReasonPhrase("Not Modified");
 				break;
 			}
 			case Changed_204: {
 				if (requestMethod.contains("post") || requestMethod.contains("put")) {				
 					httpResponse.setStatusCode(HttpStatus.SC_NO_CONTENT);
 					httpResponse.setReasonPhrase("No Content");
 				}
 				else {
 					System.out.println("wrong msgCode for request-method!");
 					httpResponse.setStatusCode(HttpStatus.SC_METHOD_FAILURE);
 					httpResponse.setReasonPhrase("Method Failure");
 				}
 				break;
 			}
 			case Content_205: {
 				if (requestMethod.contains("get")) {
 					httpResponse.setStatusCode(HttpStatus.SC_OK);
 					httpResponse.setReasonPhrase("OK");
 				}
 				else {
 					System.out.println("wrong msgCode for request-method!");
 					httpResponse.setStatusCode(HttpStatus.SC_METHOD_FAILURE);
 					httpResponse.setReasonPhrase("Method Failure");
 				}
 				break;
 			}
 			case Bad_Request_400: {
 				httpResponse.setStatusCode(HttpStatus.SC_BAD_REQUEST);
 				httpResponse.setReasonPhrase("Bad Request");
 				break;
 			}
 			case Unauthorized_401: {
 				httpResponse.setStatusCode(HttpStatus.SC_UNAUTHORIZED);
 				httpResponse.setReasonPhrase("Unauthorized");
 				break;
 			}
 			case Bad_Option_402: {
 				httpResponse.setStatusCode(HttpStatus.SC_BAD_REQUEST);
 				httpResponse.setReasonPhrase("Bad Option");
 				break;
 			}
 			case Forbidden_403: {
 				httpResponse.setStatusCode(HttpStatus.SC_FORBIDDEN);
 				httpResponse.setReasonPhrase("Forbidden");
 				break;
 			}
 			case Not_Found_404: {
 				httpResponse.setStatusCode(HttpStatus.SC_NOT_FOUND);
 				httpResponse.setReasonPhrase("Not Found");
 				break;
 			}
 			case Method_Not_Allowed_405: {
 				httpResponse.setStatusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
 				httpResponse.setReasonPhrase("Method Not Allowed");
 				break;
 			}
 			case Precondition_Failed_412: {
 				httpResponse.setStatusCode(HttpStatus.SC_PRECONDITION_FAILED);
 				httpResponse.setReasonPhrase("Precondition Failed");
 				break;
 			}
 			case Request_Entity_To_Large_413: {
 				httpResponse.setStatusCode(HttpStatus.SC_REQUEST_TOO_LONG);
 				httpResponse.setReasonPhrase("Request Too Long : Request entity too large");
 				break;
 			}
 			case Unsupported_Media_Type_415: {
 				httpResponse.setStatusCode(HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE);
 				httpResponse.setReasonPhrase("Unsupported Media Type");
 				break;
 			}
 			case Internal_Server_Error_500: {
 				httpResponse.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
 				httpResponse.setReasonPhrase("Internal Server Error");
 				break;
 			}
 			case Not_Implemented_501: {
 				httpResponse.setStatusCode(HttpStatus.SC_NOT_IMPLEMENTED);
 				httpResponse.setReasonPhrase("Not Implemented");
 				break;
 			}
 			case Bad_Gateway_502: {
 				httpResponse.setStatusCode(HttpStatus.SC_BAD_GATEWAY);
 				httpResponse.setReasonPhrase("Bad Gateway");
 				break;
 			}
 			case Service_Unavailable_503: {
 				httpResponse.setStatusCode(HttpStatus.SC_SERVICE_UNAVAILABLE);
 				httpResponse.setReasonPhrase("Service Unavailable");
 				break;
 			}
 			case Gateway_Timeout_504: {
 				httpResponse.setStatusCode(HttpStatus.SC_GATEWAY_TIMEOUT);
 				httpResponse.setReasonPhrase("Gateway Timeout");
 				break;
 			}
 			case Proxying_Not_Supported_505: {
 				httpResponse.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
 				httpResponse.setReasonPhrase("Internal Server Error : Proxying not supported");
 				break;
 			}
 			case UNKNOWN: {
 				httpResponse.setStatusCode(HttpStatus.SC_BAD_REQUEST);
 				httpResponse.setReasonPhrase("Bad Request : Unknown Coap Message Code");
 				break;
 			}
 			default: {
 				httpResponse.setStatusCode(HttpStatus.SC_BAD_REQUEST);
 				httpResponse.setReasonPhrase("Bad Request : Unknown Coap Message Code");
 				break;
 			}
 		}		
 	}
 
 	//sets the coap-response code under use of http-status code; used in case coap-http
 	public static CoapResponseCode getCoapResponseCode(String requestMethod, HttpResponse httpResponse) {
 		
 		//TODO: add cases in which http-code is the same, but coap-code is different, look at response-code-mapping-table
 		switch(httpResponse.getStatusLine().getStatusCode()) {
 			case HttpStatus.SC_CREATED:	return CoapResponseCode.Created_201;
 			case HttpStatus.SC_NO_CONTENT:				
 				if (requestMethod.toLowerCase() == "delete") {
 					return CoapResponseCode.Deleted_202;
 				} else {
 					return CoapResponseCode.Changed_204;
 				}				
 			case HttpStatus.SC_NOT_MODIFIED: return CoapResponseCode.Valid_203;
 			case HttpStatus.SC_OK: return CoapResponseCode.Content_205;
 			case HttpStatus.SC_UNAUTHORIZED: return CoapResponseCode.Unauthorized_401;
 			case HttpStatus.SC_FORBIDDEN:return CoapResponseCode.Forbidden_403;
 			case HttpStatus.SC_NOT_FOUND:return CoapResponseCode.Not_Found_404;
 			case HttpStatus.SC_METHOD_NOT_ALLOWED:return CoapResponseCode.Method_Not_Allowed_405;
 			case HttpStatus.SC_PRECONDITION_FAILED: return CoapResponseCode.Precondition_Failed_412;
 			case HttpStatus.SC_REQUEST_TOO_LONG: return CoapResponseCode.Request_Entity_To_Large_413;
 			case HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE:return CoapResponseCode.Unsupported_Media_Type_415;
 			case HttpStatus.SC_INTERNAL_SERVER_ERROR:return CoapResponseCode.Internal_Server_Error_500;
 			case HttpStatus.SC_NOT_IMPLEMENTED:return CoapResponseCode.Not_Implemented_501;
 			case HttpStatus.SC_BAD_GATEWAY:return CoapResponseCode.Bad_Gateway_502;
 			case HttpStatus.SC_SERVICE_UNAVAILABLE:return CoapResponseCode.Service_Unavailable_503;
 			case HttpStatus.SC_GATEWAY_TIMEOUT:return CoapResponseCode.Gateway_Timeout_504;
 			default:return CoapResponseCode.Bad_Request_400;
 		}
 	}
 	
 	//translating-functions in case of http-coap
 	public static CoapRequest httpRequestToCoapRequest(HttpRequest request) {		
 
 		//analyze method:
 		CoapRequestCode requestCode = GetCoapMessageCode(request);		//special handling of "head" is performed in that function
 		if (requestCode != null) {
 			switch (requestCode) {
 			case GET: 
 				return handleHttpGETrequest(request);
 			case PUT: 
 				return handleHttpPUTrequest(request);
 			case POST: 
 				return handleHttpPOSTrequest(request);
 			case DELETE: 
 				return handleHttpDELETErequest(request);
 			default:
 				return null;
 			}
 		}
 		else {
 			System.out.println("Error in Mapper-Thread: Message-Code is invalid");
 			return null;			
 		}				
 	}
 	
 	public static HttpResponse coapResponseToHttpResponse(ProxyMessageContext context) {
 		CoapResponse coapResponse = context.getCoapResponse();
 
 		//create a response-object, set http version and assume a default state of ok
 		HttpResponse httpResponse = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "OK");
 		
 		// differ between methods is necessary to set the right status-code
 		String request_method = context.getHttpRequest().getRequestLine().getMethod();
 
 		if (request_method.toLowerCase().contains("get"))
 			handleCoapGETresponse(coapResponse, httpResponse);
 		else if (request_method.toLowerCase().contains("put"))
 			handleCoapPUTresponse(coapResponse, httpResponse);
 		else if (request_method.toLowerCase().contains("post"))
 			handleCoapPOSTresponse(coapResponse, httpResponse);
 		else if (request_method.toLowerCase().contains("delete"))
 			handleCoapDELETEresponse(coapResponse, httpResponse);
 		else {
 			System.out
 					.println("error in translating coap-response to http-response!");
 			httpResponse = new BasicHttpResponse(HttpVersion.HTTP_1_1,
 					HttpStatus.SC_INTERNAL_SERVER_ERROR,
 					"Internal Server Error");
 		}
 
 		return httpResponse;
 	}
 
 	//translating-functions in case of coap-http
 	public static HttpRequest coapRequestToHttpRequest(ProxyMessageContext context) {
 		CoapRequest request = context.getCoapRequest();
 		CoapRequestCode code = request.getRequestCode();
 		switch (code) {
 			case GET:
 				return handleCoapGETrequest(request, context.getUri().toString());
 			case PUT:
 				return handleCoapPUTrequest(request, context.getUri().toString());
 			case POST:
 				return handleCoapPOSTrequest(request, context.getUri().toString());
 			case DELETE:
 				return handleCoapDELETErequest(request, context.getUri().toString());
 			default:
 				return null;
 		}
 	}
 	
 	public static CoapResponse httpResponseToCoapResponse(ProxyMessageContext context) {
 		
 
 		
 		//get the coap-request to extract the method-code
 		String method = "get";
 		CoapRequest request = context.getCoapRequest();
 		if (request != null) {
 			method = request.getRequestCode().toString();
 		}
 		
		//set the response-code according to response-code-mapping-table
		CoapResponse coapResponse = new BasicCoapResponse(CoapPacketType.ACK, getCoapResponseCode(method, context.getHttpResponse()), 0, null);
 
 		//TODO: translate header-options
 		
 		//assume in this case a string-entity
 		//TODO: add more entity-types
 		coapResponse.setContentType(CoapMediaType.text_plain);
 		
 		String entity = "";
 		try {
 			entity = EntityUtils.toString(context.getHttpResponse().getEntity());
 			
 		} catch (org.apache.http.ParseException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	
 		coapResponse.setPayload(entity);
 
 		return coapResponse;
 	}
 		
 	//mediatype-mapping:
 	public static void httpMediaType2coapMediaType(String mediatype, CoapRequest request) {
 		
 		String[] type_subtype = mediatype.split(",");
 		for (String value : type_subtype) {
 			if (value.toLowerCase().contains("text")
 					&& value.toLowerCase().contains("plain")) {
 				request.addAccept(CoapMediaType.text_plain);
 			} else if (value.toLowerCase().contains("application")) { // value is for example "application/xml;q=0.9"
 				String[] subtypes = value.toLowerCase().split("/");
 				String subtype = "";
 
 				if (subtypes.length == 2) {
 					subtype = subtypes[1]; // subtype is for example now "xml;q=0.9"
 				} else {
 					System.out.println("Error in reading Mediatypes!");
 				}
 
 				// extract the subtype-name and remove the quality identifiers:
 				String[] subname = subtype.split(";");
 				String name = "";
 
 				if (subname.length > 0) {
 					name = subname[0]; // name is for example "xml"
 				} else {
 					System.out.println("Error in reading Mediatypes!");
 				}
 
 				if (name.contentEquals("link-format")) {
 					request.addAccept(CoapMediaType.link_format);
 				}
 				if (name.contentEquals("xml")) {
 					request.addAccept(CoapMediaType.xml);
 				}
 				if (name.contentEquals("octet-stream")) {
 					request.addAccept(CoapMediaType.octet_stream);
 				}
 				if (name.contentEquals("exi")) {
 					request.addAccept(CoapMediaType.exi);
 				}
 				if (name.contentEquals("json")) {
 					request.addAccept(CoapMediaType.json);
 				}
 			}
 		}
 	}
 		
 	//translate request-header-options in case of http-coap
 	public static void headerTranslateHttpToCoap(HttpRequest request, CoapRequest coapRequest) {
 		URI uri = null;		
 		
 		//construct uri for later use
 		uri = getHttpRequestUri(request);
 		
 		//Content-Type is in response only
 		
 		//Max-Age is in response only
 
 		//Proxy-Uri doesn't matter for this purpose
 		
 		//ETag:
 		if (request.containsHeader("Etag")) {
 			Header[] headers = request.getHeaders("Etag");
 			if (headers.length > 0) {
 				for (int i=0; i < headers.length; i++) {
 					String etag = headers[i].getValue();
 					coapRequest.addETag(etag.getBytes());
 				}
 			}
 		}
 		
 		//Uri-Host:		
 		//don't needs to be there
 		
 		
 		//Location-Path is in response-only
 		//Location-Query is in response-only
 		
 		//Uri-Path:
 		//this is the implementation according to coap-rfc section 6.4		
 		//first check if uri is absolute and that it has no fragment
 		if (uri.isAbsolute() && uri.getFragment() == null) {
 			coapRequest.setUriPath(uri.getPath());
 		}
 		else {
 			System.out.println("error: uri has wrong format, adding no uri-path-header!");
 		}
 		
 		//Token is the same number as msgID, not needed now
 		//in future development it should be generated here
 		
 		//Accept: possible values are numeric media-types
 		if (request.containsHeader("Accept")) {
 			Header[] headers = request.getHeaders("Accept");
 			if (headers.length > 0) {
 				for (int i=0; i < headers.length; i++) {
 					httpMediaType2coapMediaType(headers[i].getValue(), coapRequest);
 				}
 			}
 		}
 		
 		//TODO: if-match:
 //		if (request.containsHeader("If-Match")) {
 //			Header[] headers = request.getHeaders("If-Match");
 //			if (headers.length > 0) {
 //				for (int i=0; i < headers.length; i++) {
 //					String header_value = headers[i].getValue();
 //					CoapHeaderOption option_ifmatch = new CoapHeaderOption(CoapHeaderOptionType.If_Match, header_value.getBytes());
 //					header.addOption(option_ifmatch );
 //				}
 //			}
 //		}
 		
 		//Uri-Query:
 		//this is the implementation according to coap-rfc section 6.4		
 		//first check if uri is absolute and that it has no fragment
 		if (uri.isAbsolute() && uri.getFragment() == null) {
 			if (uri.getQuery() != null) {						//only add options if there are some
 				coapRequest.setUriQuery(uri.getQuery());
 			}
 		} else {
 			System.out.println("error: uri has wrong format, adding no uri-query-header!");
 		}
 		
 		
 		//TODO: If-None-Match:
 //		if (request.containsHeader("If-None-Match")) {
 //			Header[] headers = request.getHeaders("If-None-Match");
 //			if (headers.length > 0) {
 //				if (headers.length > 1) {
 //					System.out.println("multiple headers in request, ignoring all except the first");
 //				}
 //				String header_value = headers[0].getValue();
 //				CoapHeaderOption option_ifnonematch = new CoapHeaderOption(CoapHeaderOptionType.If_None_Match, header_value.getBytes());
 //				header.addOption(option_ifnonematch);
 //			}
 //		}
 	}
 		
 	// translate response-header-options in case of http-coap
 	public static void headerTranslateCoapToHttp(CoapResponse coapResponse, HttpResponse httpResponse) {
 
 		// investigate all coap-headers and set corresponding http-headers
 		CoapMediaType contentType = coapResponse.getContentType();
 		if (contentType != null) {
 			switch (contentType) {
 			case text_plain:
 				httpResponse.addHeader("Content-Type", "text/plain");
 				break;
 			case link_format:
 				httpResponse.addHeader("Content-Type",
 						"application/link-format");
 				break;
 			case json:
 				httpResponse.addHeader("Content-Type", "application/json");
 				break;
 			case exi:
 				httpResponse.addHeader("Content-Type", "application/exi");
 				break;
 			case octet_stream:
 				httpResponse.addHeader("Content-Type",
 						"application/octet-stream");
 				break;
 			case xml:
 				httpResponse.addHeader("Content-Type", "application/xml");
 				break;
 			default:
 				httpResponse.addHeader("Content-Type", "text/plain");
 				break;
 			}
 		} else {
 			httpResponse.addHeader("Content-Type", "text/plain");
 		}
 		
 		long maxAge = coapResponse.getMaxAge();
 		if (maxAge < 0){
 			maxAge = DEFAULT_MAX_AGE_MS;
 		}
 		long maxAgeMs = maxAge * 1000; 
 		if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_SERVICE_UNAVAILABLE){
 			httpResponse.addHeader("Retry-After", String.valueOf(maxAge));
 		}
 		
 		byte[] etag = coapResponse.getETag();
 		if (etag != null){
 			httpResponse.addHeader("Etag", new String(etag));
 		}
 				
 		//generate content-length-header
 		if (httpResponse.getEntity() != null)
 			httpResponse.addHeader("Content-length", "" + httpResponse.getEntity().getContentLength());
 		
 		//set creation-date for Caching:
 		httpResponse.addHeader("Date", "" + formatDate(new GregorianCalendar().getTime()));
 		
 		//expires-option is option-value (default is 60 secs) + current_date
 		Calendar calendar = new GregorianCalendar();
 		calendar.setTimeInMillis(calendar.getTimeInMillis() + maxAgeMs);
 		String date = formatDate(calendar.getTime());
 		httpResponse.addHeader("Expires", date);
 	}
 	
 	//these functions are used to differentiate between method-codes
 	//special methods need special handling
 	public static CoapRequest handleHttpGETrequest(HttpRequest request) {
 		
 		//create CoapMessage
 		/* FIXME: the request should be created by the corresponding channel*/
 		CoapRequest coapRequest = new BasicCoapRequest(CoapPacketType.CON, CoapRequestCode.GET, 0);
 		//Translate Headers
 		headerTranslateHttpToCoap(request, coapRequest);
 		
 		return coapRequest;
 	}
 	
 	
 	public static CoapRequest handleHttpPUTrequest(HttpRequest request) {
 
 		//create CoapMessage
 		/* FIXME: the request should be created by the corresponding channel*/
 		CoapRequest coaprequest = new BasicCoapRequest(CoapPacketType.CON, CoapRequestCode.PUT, 0);
 		//Translate Headers
 		headerTranslateHttpToCoap(request,coaprequest);
 		
 		//pass-through the payload
 		BasicHttpEntityEnclosingRequest entirequest = (BasicHttpEntityEnclosingRequest) request;		
 		ConsumingNHttpEntityTemplate entity = (ConsumingNHttpEntityTemplate) entirequest.getEntity();
 		ByteContentListener liste = (ByteContentListener)entity.getContentListener();
 		try {
 			byte[] data = liste.getContent();
 			coaprequest.setPayload(data);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 						
 		return coaprequest;
 	}	
 	public static CoapRequest handleHttpPOSTrequest(HttpRequest request) {
 		
 		//create CoapMessage
 		/* FIXME: the request should be created by the corresponding channel*/
 		CoapRequest coapRequest = new BasicCoapRequest(CoapPacketType.CON, CoapRequestCode.POST, 0);
 		//Translate Headers
 		headerTranslateHttpToCoap(request,coapRequest);
 		
 		//pass-through the payload
 		BasicHttpEntityEnclosingRequest entirequest = (BasicHttpEntityEnclosingRequest) request;		
 		ConsumingNHttpEntityTemplate entity = (ConsumingNHttpEntityTemplate) entirequest.getEntity();	//(ConsumingNHttpEntityTemplate)
 		ByteContentListener liste = (ByteContentListener)entity.getContentListener();
 		try {
 			byte[] data = liste.getContent();
 			coapRequest.setPayload(data);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		return coapRequest;
 	}
 	
 	
 	public static CoapRequest handleHttpDELETErequest(HttpRequest request) {
 		
 		//create CoapMessage
 		/* FIXME: the request should be created by the corresponding channel*/
 		CoapRequest coapRequest = new BasicCoapRequest(CoapPacketType.CON, CoapRequestCode.DELETE, 0);
 		//Translate Headers
 		headerTranslateHttpToCoap(request,coapRequest);
 		return coapRequest;
 	}
 		
 	//functions translate coap-response to http-response in case of http-coap
 	public static HttpResponse handleCoapGETresponse(CoapResponse coapResponse, HttpResponse httpResponse) {		
 						
 		//set status code and reason phrase
 		setHttpMsgCode(coapResponse, "get", httpResponse);
 		
 		//pass-through the payload, if we do not answer a head-request
 		boolean isMethodHead = false; 
 		if (isMethodHead) {
 			//FIXME: don't add the payload in case of a HEAD HTTP request
 		}
 		else {
 			NStringEntity entity;
 			try {
 				
 				entity = new NStringEntity(new String(coapResponse.getPayload()),"UTF-8");
 				entity.setContentType("text/plain");
 				
 				httpResponse.setEntity(entity);
 				
 			} catch (UnsupportedEncodingException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		//set Headers
 		headerTranslateCoapToHttp(coapResponse,httpResponse);		
 		return httpResponse;		
 	}
 	public static HttpResponse handleCoapPUTresponse(CoapResponse response, HttpResponse httpResponse) {		
 		
 		//set status code and reason phrase
 		setHttpMsgCode(response, "put", httpResponse);
 		
 		//set Headers
 		headerTranslateCoapToHttp(response, httpResponse);
 					
 		return httpResponse;		
 	}
 	public static HttpResponse handleCoapPOSTresponse(CoapResponse response, HttpResponse httpResponse) {		
 		
 		//set status code and reason phrase
 		setHttpMsgCode(response, "post", httpResponse);
 		
 		//set Headers
 		headerTranslateCoapToHttp(response, httpResponse);
 					
 		return  httpResponse;		
 	}
 	
 	public static HttpResponse handleCoapDELETEresponse(CoapResponse response, HttpResponse httpResponse) {		
 		//set status code and reason phrase
 		setHttpMsgCode(response, "delete", httpResponse);
 		//set Headers
 		headerTranslateCoapToHttp(response, httpResponse);
 		return httpResponse;		
 	}
 		
 	//translation-functions in case of coap-http
 	public static HttpRequest handleCoapGETrequest(CoapRequest request, String uri) {
 		
 		//create HttpRequest
 		HttpRequest httprequest = new BasicHttpRequest("GET", uri);		
 		
 		//TODO:translate header options from coap-request to http-request
 		
 		return httprequest;
 	}
 	public static HttpRequest handleCoapPUTrequest(CoapRequest request, String uri) {
 		
 		//create HttpRequest
 		BasicHttpEntityEnclosingRequest httprequest = new BasicHttpEntityEnclosingRequest("PUT", uri);	
 		
 		//TODO:translate header options from coap-request to http-request
 		
 		//Payload:
 		NStringEntity entity;
 		try {
 			entity = new NStringEntity(""+request.getPayload());
 			httprequest.setEntity(entity);
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 						
 		return httprequest;
 	}
 	public static HttpRequest handleCoapPOSTrequest(CoapRequest request, String uri) {
 	
 		//create HttpRequest
 		BasicHttpEntityEnclosingRequest httprequest = new BasicHttpEntityEnclosingRequest("POST", uri);
 		
 		//TODO:translate header options from coap-request to http-request
 		
 		//Payload:
 		NStringEntity entity;
 		try {
 			entity = new NStringEntity(""+request.getPayload());
 			httprequest.setEntity(entity);
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 		
 		return httprequest;
 	}
 	public static HttpRequest handleCoapDELETErequest(CoapRequest request, String uri) {
 	
 		//create HttpRequest
 		HttpRequest httprequest = new BasicHttpRequest("DELETE", uri);		
 		
 		//TODO:translate header options from coap-request to http-request
 		
 		return httprequest;
 	}
 	
 //	//the mode is used to indicate for which case the proxy is listening to
 //	//mode is unneccessary when proxy is listening to all cases, then there are more threads neccessary
 //	public void setMode(Integer modenumber) {
 //		mode = modenumber;
 //	}
 	
 	//setter-functions to introduce other threads
 	public void setHttpServer(HttpServerNIO server) {
 		httpServerNIO = server;
 	}
 	public void setHttpClient(HttpClientNIO client) {
 		httpClientNIO = client;
 	}
 	public void setCoapServer(CoapServerProxy server) {
 		coapServer = server;
 	}
 	public void setCoapClient(CoapClientProxy client) {
 		coapClient = client;
 	}
 	
 	//these functions offer other classes the ability to pass a message to the mapper; queue-interface-functions
 	public void putCoapResponse(ProxyMessageContext context) {		
 		try {
 			coapOutQueue.put(context);
 			
             synchronized (coapResponseHandler) {
             	coapResponseHandler.notify();
             }
 			
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 	public void putCoapRequest(ProxyMessageContext context) {		
 		try {
 			coapInQueue.put(context);
 			
             synchronized (coapRequestHandler) {
             	coapRequestHandler.notify();
             }
             
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 	public void putHttpRequest(ProxyMessageContext context) {		
 		try {
 			httpInQueue.put(context);
 			
             synchronized (httpRequestHandler) {
             	httpRequestHandler.notify();
             }
             
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 	public void putHttpResponse(ProxyMessageContext context) {
 		try {
 			httpOutQueue.put(context);
 			
             synchronized (httpResponseHandler) {
             	httpResponseHandler.notify();
             }
             
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	//get the coap-message-code from a http-request
 	public static CoapRequestCode GetCoapMessageCode(HttpRequest request) {
 		CoapRequestCode code;
 		String method;
 		
 		method = request.getRequestLine().getMethod().toLowerCase();
 		
 		//only get, put, post, head and delete are accepted
 		if (method.contentEquals("get")) {
 			code = CoapRequestCode.GET;
 			return code;
 		}
 		if (method.contentEquals("put")) {
 			code = CoapRequestCode.PUT;
 			return code;
 		}
 		if (method.contentEquals("post")) {
 			code = CoapRequestCode.POST;
 			return code;
 		}
 		if (method.contentEquals("delete")) {
 			code = CoapRequestCode.DELETE;
 			return code;
 		}
 		//if we have a head request, coap should handle it as a get, but without any message-body
 		if (method.contentEquals("head")) {
 			code = CoapRequestCode.GET;
 			return code;
 		}
 
 		return null;
 	}
 	
 	//exclude methods from processing:OPTIONS/TRACE/CONNECT
 	public static boolean HttpRequestMethodSupported(HttpRequest request) {
 		
 		String method = request.getRequestLine().getMethod().toLowerCase();
 		
 		if (method.contentEquals("options") || method.contentEquals("trace") || method.contentEquals("connect"))		
 			return false;
 
 		return true;
 	}
 		
 	//makes a date to a string; http-header-values (expires, date...) must be a string in most cases
     public static String formatDate(Date date) {
     	
     	final String PATTERN_RFC1123 = "EEE, dd MMM yyyy HH:mm:ss zzz";
     	
     	if (date == null) {
             throw new IllegalArgumentException("date is null");
         }
 
         SimpleDateFormat formatter = new SimpleDateFormat(PATTERN_RFC1123, Locale.US);
         formatter.setTimeZone(TimeZone.getDefault());				//CEST
         String ret = formatter.format(date);
         return ret;
         
     }
     
     public static URI getHttpRequestUri(HttpRequest request){
 			URI uri = null;
 			
 			String uriString = request.getRequestLine().getUri();
 			/* make sure to have the scheme */
 			if (uriString.startsWith("coap://")){
 				/* do nothing */
 			} else if (uriString.startsWith("http://")){
 				uriString = "coap://" + uriString.substring(7);
 			} else {
 				/* not an absolute uri*/
 				Header[] host = request.getHeaders("Host");
 				/* only one is accepted */
 				if (host.length <= 0){
 					/* error, unknown host*/
 					return null;
 				}
 				uriString = "coap://"  + host[0].getValue() + uriString;
 			}
 			
 			try {
 				uri = new URI(uriString);
 			} catch (URISyntaxException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				return null;
 			}				
 				
 			return uri;
 	}
     
 	public static boolean isIPv4Address(InetAddress addr) {
 		try {
 			@SuppressWarnings("unused") //just to check if casting fails
 			Inet4Address addr4 = (Inet4Address) addr;
 			return true;
 		} catch (ClassCastException ex) {
 			return false;
 		}
 	}
 
 	public static boolean isIPv6Address(InetAddress addr) {
 		try {
 			@SuppressWarnings("unused") //just to check if casting fails
 			Inet6Address addr6 = (Inet6Address) addr;
 			return true;
 		} catch (ClassCastException ex) {
 			return false;
 		}
 	}
 
 	public int getHttpRequestCount() {
 		return httpRequestCount;
 	}
 
 	public int getCoapRequestCount() {
 		return coapRequestCount;
 	}
 
 	public int getServedFromCacheCount() {
 		return servedFromCacheCount;
 	}
 	
 	public void resetCounter(){
 		httpRequestCount = 0;
 		coapRequestCount = 0;
 		servedFromCacheCount = 0;
 	}
 	
 }
