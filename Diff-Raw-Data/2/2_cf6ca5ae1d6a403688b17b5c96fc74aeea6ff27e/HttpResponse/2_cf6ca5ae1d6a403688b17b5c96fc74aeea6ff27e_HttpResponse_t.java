 package com.nyaruka.http;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.util.Properties;
 
 public class HttpResponse {
 
 	private InputStream m_data = null;
 	private Properties m_headers = new Properties();
 	private Properties m_cookies = new Properties();
 	private String m_status = NanoHTTPD.HTTP_OK;
 	private String m_mimeType = NanoHTTPD.MIME_HTML;
 	
 	public HttpResponse(){
 	}
 	
 	public HttpResponse(String body){
 		this(NanoHTTPD.HTTP_OK, NanoHTTPD.MIME_HTML, body);
 	}
 	
 	public HttpResponse(String status, String mimeType, InputStream data){
 		m_status = status;
 		m_mimeType = mimeType;
 		m_data = data;
 	}
 	
 	public HttpResponse(String status, String mimeType, String data){
 		m_status = status;
 		m_mimeType = mimeType;
 		m_data = new ByteArrayInputStream(data.getBytes());
 	}
 	
 	public HttpResponse(String status, String data){
 		this(status, NanoHTTPD.MIME_HTML, data);
 	}
 	
 	public HttpResponse(String status, InputStream data){
 		this(status, NanoHTTPD.MIME_HTML, data);
 	}
 	
 	public void addHeader(String name, String value){
 		m_headers.setProperty(name, value);
 	}
 	public String getHeader(String name){
 		return m_headers.getProperty(name);
 	}
 	public Properties getHeaders(){ return m_headers; }
 	
 	public void setBody(String body){ m_data = new ByteArrayInputStream(body.getBytes()); }
 	public void setBody(InputStream  body){ m_data = body; }
 	public InputStream getBody(){ return m_data; }
 	
 	public void setStatus(String status){ m_status = status; }
 	public String getStatus(){ return m_status; }
 	
 	public String getMimeType(){ return m_mimeType; }
 	public void setMimeType(String mime){ m_mimeType = mime; }
 	
 	public boolean hasCookies(){ return m_cookies.size() > 0; }
 	public void setCookie(String key, String value){
 		m_cookies.put(key, value);
 	}
 	public Properties getCookies(){ return m_cookies; }
 	public String getCookieString(String key){
 		// cookie format is actually mega simple, there's no encoding, just joining of 
 		// key and value by '=' and ending of values with ';'
 		// we just make all cookies persist forever for now
 		StringBuilder cookie = new StringBuilder();
 		cookie.append(key);
 		cookie.append("=");
 		cookie.append(m_cookies.get(key));
		cookie.append("; Path=/; expires=");
 		cookie.append("Sat, 03 May 2025 17:44:22 GMT");
 		return cookie.toString();
 	}
 }
