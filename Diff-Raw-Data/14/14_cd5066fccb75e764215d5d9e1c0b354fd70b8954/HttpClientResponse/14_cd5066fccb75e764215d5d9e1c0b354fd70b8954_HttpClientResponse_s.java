 package com.atlassian.sal.core.net;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Map;
 import java.util.HashMap;
 
 import org.apache.commons.httpclient.HttpMethod;
 import org.apache.commons.httpclient.Header;
 import com.atlassian.sal.api.net.ResponseException;
 import com.atlassian.sal.api.net.Response;
 
 public class HttpClientResponse implements Response
 {
 
 	private final HttpMethod method;
 
 	public HttpClientResponse(HttpMethod method)
 	{
 		this.method = method;
 	}
 
 	public String getResponseBodyAsString() throws ResponseException
 	{
 		try
 		{
 			return method.getResponseBodyAsString();
 		} catch (IOException e)
 		{
 			throw new ResponseException(e.getMessage(), e);
 		}
 	}
 
 	public InputStream getResponseBodyAsStream() throws ResponseException
 	{
 		try
 		{
 			return method.getResponseBodyAsStream();
 		} catch (IOException e)
 		{
 			throw new ResponseException(e.getMessage(), e);
 		}
 	}
 
     public <T> T getEntity(Class<T> entityClass) throws ResponseException
     {
         throw new UnsupportedOperationException("This SAL request does not support object marshalling. Use the RequestFactory component instead.");
     }
 
     public int getStatusCode()
 	{
 		return method.getStatusCode();
 	}
 
 	public String getStatusText()
 	{
 		return method.getStatusText();
 	}
 
 	public boolean isSuccessful()
 	{
         int codeOrder = method.getStatusCode() / 100;
         return codeOrder == 2 || codeOrder ==  3;
     }
 
     public String getHeader(String name)
     {
        return method.getResponseHeader(name).getValue();
     }
 
     public Map<String, String> getHeaders()
     {
         Map<String, String> map = new HashMap<String, String>();
         for (Header header : method.getResponseHeaders())
         {
             map.put(header.getName(), header.getValue());
         }
         return map;
     }
 }
