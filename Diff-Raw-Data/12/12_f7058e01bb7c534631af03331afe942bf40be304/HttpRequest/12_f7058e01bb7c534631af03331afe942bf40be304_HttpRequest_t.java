 package com.redshape.servlet.core;
 
 import com.redshape.utils.Commons;
 import org.apache.log4j.Logger;
 
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
 import java.util.*;
 
 /**
  * Created by IntelliJ IDEA.
  * User: nikelin
  * Date: 10/10/10
  * Time: 11:56 PM
  * To change this template use File | Settings | File Templates.
  */
 public class HttpRequest extends HttpServletRequestWrapper implements IHttpRequest {
     @SuppressWarnings("unused")
     private static final Logger log = Logger.getLogger( HttpRequest.class );
 
 	public static final String MULTIPART_TYPE = "multipart/form-data";
 
     private String controller;
     private String action;
     private String requestData;
 
     private IMultipartRequest multipart;
 
     private Map<String, Object> parameters = new HashMap<String, Object>();
     private Map<String, Cookie> cookiesMap = new HashMap<String, Cookie>();
 
     public HttpRequest( HttpServletRequest request ) {
         super(request);
 
 		this.init();
         this.initCookies();
     }
 
 	protected void init() {
 		try {
 			if ( this.isMultiPart() ) {
 				this.multipart = new MultipartRequest(this);
 			}
 
             this.copyBaseParams();
 		} catch ( Throwable e ) {
 			throw new IllegalStateException( e.getMessage(), e);
 		}
 	}
 
     protected void initCookies() {
         if ( this.getCookies() == null ) {
             return;
         }
 
         for ( Cookie cookie : this.getCookies() ) {
             this.cookiesMap.put( cookie.getName(), cookie );
         }
     }
 
 	@Override
 	public void setParameter(String name, Object value) {
 		this.parameters.put( name, value );
 	}
 
 	@Override
     public boolean hasParameter(String name) {
         return this.parameters.containsKey(name);
     }
 
     @Override
 	public boolean isMultiPart() {
 		return this.getContentType() != null && this.getContentType().contains( MULTIPART_TYPE );
 	}
 
 	protected void copyBaseParams() {
 		Enumeration<String> parameterNames = super.getParameterNames();
 		while ( parameterNames.hasMoreElements() ) {
 			String parameterName = parameterNames.nextElement();
 			String[] values = super.getParameterValues(parameterName);
 			if ( values.length == 1 && !parameterName.endsWith("[]") ) {
 				this.parameters.put( parameterName, values[0] );
 			} else {
 				this.parameters.put( parameterName, Arrays.asList(values) );
 			}
 		}
 	}
 
 	@Override
 	public Long getLongParameter(String name) {
         try {
 		    return Long.valueOf( this.getParameter(name) );
         } catch ( NumberFormatException e ) {
             return null;
         }
 	}
 
 	@Override
 	public Integer getIntegerParameter(String name) {
         try {
 	        return Integer.valueOf( this.getParameter(name) );
         } catch ( NumberFormatException e ) {
             return null;
         }
 	}
 
     @Override
     public <T> List<T> getListParameter(String name) {
         return (List<T>) this.parameters.get(name);
     }
 
     @Override
 	public Boolean getBooleanParameter(String name) {
         return Boolean.valueOf( this.getRequest().getParameter(name) );
 	}
 
 	@Override
 	public Boolean getCheckboxParameter(String name) {
 		return this.getParameter(name).equals("on");
 	}
 
 	@Override
 	public Float getFloatParameter(String name) {
 		return Float.valueOf( this.getParameter(name) );
 	}
 
 	@Override
     public <T> T getObjectParameter( String name ) {
 		if ( name == null ) {
 			throw new IllegalArgumentException("<null>");
 		}
 
 		if ( !name.endsWith("[]") ) {
 			String data = super.getParameter(name);
 			if ( data != null ) {
 				return (T) data;
 			}
 		}
 
         return (T) this.parameters.get( name );
     }
 
     @Override
     public String getParameter(String name ) {
         return String.valueOf( this.<Object>getObjectParameter(name) );
     }
 
     @Override
     public String getCookie( String name ) {
         Cookie cookie = this.cookiesMap.get( name );
         if ( cookie == null ) {
             return null;
         }
 
         return cookie.getValue();
     }
 
     protected synchronized  String readRequest() throws IOException {
         if ( this.requestData != null ) {
             return this.requestData;
         }
 
         StringBuffer data = new StringBuffer();
         String buff;
         InputStreamReader reader = new InputStreamReader( this.getInputStream() );
 
         BufferedReader buffer = new BufferedReader( reader );
         buffer.skip(0);
         while (null != (buff = buffer.readLine())) {
             data.append(buff);
         }
 
		return this.requestData = data.toString();
     }
 
     @Override
     public synchronized IMultipartRequest getMultipart() {
 		if ( !this.isMultiPart() ) {
 			throw new IllegalStateException("Request is not multipart type");
 		}
 
 		return this.multipart;
     }
 
     @Override
     public synchronized byte[] getFileContent( String name )
             throws IOException {
         IMultipartRequest multiPart = this.getMultipart();
         MultipartRequest.IFileInfo fileInfo = multiPart.getFileInfo(name);
         if ( null == fileInfo ) {
             return new byte[0];
         }
 
         return fileInfo.getContent();
     }
 
     public boolean isPost() {
         return this.getMethod().equals("POST");
     }
 
     public void setController( String name ) {
         this.controller = name;
     }
 
     public String getController() {
         return this.controller;
     }
 
     public void setAction( String name ) {
         this.action = name;
     }
 
     public String getAction() {
         return this.action;
     }
 
     @Override
     public Object getAttribute( String attribute ) {
         return Commons.select( super.getAttribute(attribute), this.getSession().getAttribute(attribute) );
     }
 
     @Override
     public void setParameters(Map<String, Object> parameters) {
         this.parameters = parameters;
     }
 
     @Override
     public Map<String, Object> getParameters() {
         return this.parameters;
     }
 
     @Override
     public String getBody() throws IOException {
         return this.readRequest();
     }
 
 
 }
