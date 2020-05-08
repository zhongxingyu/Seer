 package com.olivelabs.util;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.simpleframework.http.Address;
 import org.simpleframework.http.ContentType;
 import org.simpleframework.http.Cookie;
 import org.simpleframework.http.Form;
 import org.simpleframework.http.Path;
 import org.simpleframework.http.Query;
 import org.simpleframework.http.Request;
 import org.simpleframework.http.session.Session;
 
 import com.ning.http.client.RequestBuilder;
 import com.ning.http.client.RequestType;
 
 public class RequestConvertor {
 
 	public static com.ning.http.client.Request copyRequest(Request serverRequest,
 			String host, String port) throws Throwable {
 		
 		Map<String, String> attributes = serverRequest.getAttributes();
 		String content = serverRequest.getContent();
 		InetSocketAddress clientAddress = serverRequest.getClientAddress();
 		int contentLength = serverRequest.getContentLength();
 		ContentType contentTypeData = serverRequest.getContentType();
 		List<Cookie> cookies = serverRequest.getCookies();
 		String requestMethod = serverRequest.getMethod();
 		List<String> headers = serverRequest.getNames();
 		Session session = serverRequest.getSession();
 		Path path = serverRequest.getPath();
 		Form form = serverRequest.getForm();
 
 		
 
 		RequestBuilder requestBuilder = new RequestBuilder(getRequestType(requestMethod));
 		if((RequestType.PUT).equals(getRequestType(requestMethod)) || (RequestType.POST).equals(getRequestType(requestMethod)) )
 			requestBuilder.setBody(content);
 		for(String header : headers){
 			requestBuilder.addHeader(header, serverRequest.getValue(header));
 		}
 		for(Cookie cookie : cookies){
 			com.ning.http.client.Cookie cookieToSend = new com.ning.http.client.Cookie(cookie.getDomain(),cookie.getName(),cookie.getValue(), cookie.getPath(), cookie.getExpiry(),false); 
 			requestBuilder.addCookie(cookieToSend);
 		}
 		//requestBuilder.setParameters(attributes);
 		Set sessionKeys = session.entrySet();
 		for(Object key : sessionKeys){
 			requestBuilder.addParameter((String) key,(String) session.get(key));
 		}
 		
 		if(port!=null && !("").equals(port)){
 			port=":"+port;
 		}
 		else{
 			port = "";
 		}
 		
 		String protocol =serverRequest.getAddress().getScheme();// +"://";
 		if( protocol==null || ("").equals(protocol)){
 			protocol = "http://";
 		}
 		else{
 			protocol = protocol + "://";
 		}
 		String queryString = "";
 		Query query = serverRequest.getAddress().getQuery();
		if(query.keySet().size()!=0){
 			for(String key : query.keySet()){
				requestBuilder.addQueryParameter(key, query.get(key));
 			}
 		}
 		
		String url = protocol + host + port + serverRequest.getPath();
 		requestBuilder.setUrl(url);
 		return requestBuilder.build();
 		
 	}
 	
 	public static RequestType getRequestType(String requestMethod){
 		RequestType requestType = RequestType.GET;
 		if (("POST".equals(requestMethod)))
 			requestType = RequestType.POST;
 		if (("PUT".equals(requestMethod)))
 			requestType = RequestType.PUT;
 		if (("DELETE".equals(requestMethod)))
 			requestType = RequestType.DELETE;
 		if (("HEAD".equals(requestMethod)))
 			requestType = RequestType.HEAD;
 		if (("OPTIONS".equals(requestMethod)))
 			requestType = RequestType.OPTIONS;
 		return requestType;
 	}
 }
