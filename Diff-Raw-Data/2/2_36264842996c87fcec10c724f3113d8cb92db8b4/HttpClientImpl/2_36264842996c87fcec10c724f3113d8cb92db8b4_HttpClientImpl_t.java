 package no.nlb.http;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.util.Map;
 
 import no.nlb.utils.XML;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.restlet.Client;
 import org.restlet.data.MediaType;
 import org.restlet.data.Protocol;
 import org.restlet.data.Status;
 import org.restlet.representation.Representation;
 import org.restlet.resource.ClientResource;
 import org.restlet.resource.ResourceException;
 import org.w3c.dom.Document;
 
 /**
  * Implementation of HttpClient that uses Restlet as the underlying HTTP client.
  * 
  * @author jostein
  */
 public class HttpClientImpl implements HttpClient {
 	
	private Client client = new Client(Protocol.HTTP); // TODO: add support for HTTPS WS ?
 	
 	/**
 	 * Implementation of HttpClient.get(String)
 	 */
 	public HttpResponse get(String url) throws HttpException {
 		ClientResource resource = new ClientResource(url);
 		resource.setNext(client);
 		Representation representation = null;
 		InputStream in = null;
 		boolean error = false;
 		try {
 			representation = resource.get();
 			if (representation != null) {
 				in = representation.getStream();
 			}
 			
 		} catch (ResourceException e) {
 			// Unauthorized etc.
 			error = true;
 		} catch (IOException e) {
 			e.printStackTrace();
 			error = true;
 		}
 		
 		Status status = resource.getStatus();
 		
 		if (error) {
 			try {
 				if (status != null && status.getCode() >= 1000) {
 					in = new ByteArrayInputStream(("Could not communicate with the remote server: "+url).getBytes("utf-8"));
 				} else {
 					in = new ByteArrayInputStream(("Could not communicate with the remote server: "+url).getBytes("utf-8"));
 				}
 	        } catch(UnsupportedEncodingException e) {
 	            throw new HttpException("Unable to create error body string as stream", e);
 	        }
 		}
 		
 		HttpResponse response = new HttpResponse(status.getCode(), status.getReasonPhrase(), status.getDescription(), representation==null?null:representation.getMediaType()==null?null:representation.getMediaType().toString(), in);
 		if (Http.debug()) {
 			try {
     			if (representation == null) {
     				System.err.println("---- Received: null ----\n");
                 } else if (representation.getMediaType() == MediaType.APPLICATION_ALL_XML) {
                     System.err.println("---- Received: ----\n"+response.asText());
     			} else {
     				System.err.println("---- Received: "+representation.getMediaType()+" ("+representation.getSize()+" bytes) ----");
     			}
 			} catch (Exception e) {
 				if (Http.debug()) System.err.print("---- Received: ["+e.getClass()+": "+e.getMessage()+"] ----");
 				if (Http.debug()) e.printStackTrace();
 			}
 		}
 		
 		return response;
 	}
 	
 	/**
 	 * Implementation of HttpClient.post(String,Document)
 	 */
 	public HttpResponse post(final String url, final Document xml) throws HttpException {
 		if (Http.debug()) {
 			System.err.println("URL: ["+url+"]");
 			System.err.println(XML.toString(xml));
 		}
 		
 		ClientResource resource = new ClientResource(url);
 		resource.setNext(client);
 		Representation representation = null;
 		try {
 			representation = resource.post(XML.toString(xml));
 		} catch (org.restlet.resource.ResourceException e) {
 			throw new HttpException(e.getMessage(), e);
 		}
 		
 		InputStream in = null;
 		if (representation != null) {
 			try {
 				in = representation.getStream();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		Status status = resource.getStatus();
 		
 		return new HttpResponse(status.getCode(), status.getReasonPhrase(), status.getDescription(), representation==null?null:representation.getMediaType().toString(), in);
 	}
 	
 	/**
 	 * Implementation of HttpClient.post(String,String)
 	 */
 	public HttpResponse post(final String url, final String text) throws HttpException {
 		ClientResource resource = new ClientResource(url);
 		resource.setNext(client);
 		Representation representation = null;
 		try {
 			representation = resource.post(text);
 		} catch (org.restlet.resource.ResourceException e) {
 			throw new HttpException(e.getMessage(), e);
 		}
 		
 		InputStream in = null;
 		if (representation != null) {
 			try {
 				in = representation.getStream();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		Status status = resource.getStatus();
 		
 		return new HttpResponse(status.getCode(), status.getReasonPhrase(), status.getDescription(), representation==null?null:representation.getMediaType().toString(), in);
 	}
 	
 	/**
 	 * Implementation of HttpClient.post(String,Map<String,File>)
 	 */
 	public HttpResponse post(final String url, final Map<String,File> parts) throws HttpException {
 		org.apache.http.client.HttpClient httpclient = new DefaultHttpClient();
 		HttpPost httppost = new HttpPost(url);
 		
 		MultipartEntity reqEntity = new MultipartEntity();
 		for (String partName : parts.keySet()) { 
 			reqEntity.addPart(partName, new FileBody(parts.get(partName)));
 		}
 		httppost.setEntity(reqEntity);
 		
 		org.apache.http.HttpResponse response = null;
 		try {
 			response = httpclient.execute(httppost);
 		} catch (ClientProtocolException e) {
 			throw new HttpException("Error while POSTing.", e);
 		} catch (IOException e) {
 			throw new HttpException("Error while POSTing.", e);
 		}
 		HttpEntity resEntity = response.getEntity();
 		
 		InputStream bodyStream = null;
 		try {
 			bodyStream = resEntity.getContent();
 		} catch (IOException e) {
 			throw new HttpException("Error while reading response body", e); 
 		}
 		
 		Status status = Status.valueOf(response.getStatusLine().getStatusCode());
 		
 		return new HttpResponse(status.getCode(), status.getReasonPhrase(), status.getDescription(), response.getFirstHeader("Content-Type").getValue(), bodyStream);
 	}
 	
 }
