 package com.chiorichan.http;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.poi.util.IOUtils;
 
 import com.chiorichan.Loader;
 import com.chiorichan.framework.Framework;
 import com.chiorichan.framework.Site;
 import com.chiorichan.util.Common;
 import com.sun.net.httpserver.Headers;
 import com.sun.net.httpserver.HttpExchange;
 
 public class HttpRequest
 {
 	private HttpExchange http;
 	private PersistentSession sess = null;
 	private HttpResponse response;
 	private Map<String, String> getMap, postMap;
 	private int requestTime = 0;
 	private WebHandler handler;
 	
 	protected HttpRequest(HttpExchange _http, WebHandler _handler)
 	{
 		http = _http;
 		handler = _handler;
 		requestTime = Common.getEpoch();
 		
 		response = new HttpResponse( this );
 		
 		try
 		{
 			getMap = queryToMap( http.getRequestURI().getQuery() );
 			
 			if ( http.getRequestBody().available() > 0 )
 			{
 				byte[] queryBytes = new byte[http.getRequestBody().available()];
 				IOUtils.readFully( http.getRequestBody(), queryBytes );
 				
 				postMap = queryToMap( new String( queryBytes ) );
 			}
 		}
 		catch ( IOException e )
 		{
 			Loader.getLogger().severe( "There was a severe error reading the POST query.", e );
 		}
 	}
 	
 	protected void initSession()
 	{
 		sess = Loader.getPersistenceManager().find( this );
 		sess.handleUserProtocols();
 	}
 	
 	protected Map<String, String> queryToMap( String query ) throws UnsupportedEncodingException
 	{
 		Map<String, String> result = new HashMap<String, String>();
 		
 		if ( query == null )
 			return result;
 		
 		for ( String param : query.split( "&" ) )
 		{
 			String pair[] = param.split( "=" );
 			if ( pair.length > 1 )
 			{
 				result.put( URLDecoder.decode( pair[0], "ISO-8859-1" ), URLDecoder.decode( pair[1], "ISO-8859-1" ) );
 			}
 			else
 			{
 				result.put( URLDecoder.decode( pair[0], "ISO-8859-1" ), "" );
 			}
 		}
 		return result;
 	}
 	
 	public String getArgument( String key )
 	{
 		return getArgument( key, "" );
 	}
 	
 	public String getArgument( String key, String def )
 	{
 		return getArgument( key, "", false );
 	}
 	
 	public String getArgument( String key, String def, boolean rtnNull )
 	{
 		String val = getMap.get( key );
 		
 		if ( val == null && postMap != null )
 			val = postMap.get( key );
 		
 		if ( val == null && rtnNull )
 			return null;
 		
 		if ( val == null || val.isEmpty() )
 			return def;
 		
 		return val.trim();
 	}
 	
 	public Collection<Candy> getCandies()
 	{
 		return getSession().candies.values();
 	}
 	
 	public Headers getHeaders()
 	{
 		return http.getRequestHeaders();
 	}
 	
 	public Framework getFramework()
 	{
 		return getSession().getFramework();
 	}
 	
 	public PersistentSession getSession()
 	{
 		if ( sess == null )
 			Loader.getLogger().severe( "The Session is NULL! This usually happens because initSession() was not called at the proper time." );
 		
 		return sess;
 	}
 	
 	public HttpResponse getResponse()
 	{
 		return response;
 	}
 	
 	public String getURI()
 	{
 		return http.getRequestURI().getPath();
 	}
 	
 	public String getDomain()
 	{
 		try
 		{
 			String host = http.getRequestHeaders().get( "Host" ).get( 0 );
 			return host.split( "\\:" )[0];
 		}
 		catch ( Exception e )
 		{
 			return "";
 		}
 	}
 	
 	public String getMethod()
 	{
 		return http.getRequestMethod();
 	}
 	
 	public String getLocalAddr()
 	{
 		return http.getLocalAddress().getHostName();
 	}
 	
 	public String getHeader( String key )
 	{
 		try
 		{
 			return http.getRequestHeaders().get( key ).get( 0 );
 		}
 		catch ( NullPointerException | IndexOutOfBoundsException e )
 		{
 			return "";
 		}
 	}
 	
 	/**
 	 * Not a guaranteed method to determined if a request was made with AJAX since this header is not always set.
 	 */
 	public boolean isAjaxRequest()
 	{
		return ( getHeader("X-Requested-With") == "XMLHttpRequest" );
 	}
 	
 	public String getRemoteHost()
 	{
 		return http.getRemoteAddress().getHostName();
 	}
 	
 	public String getRemoteAddr()
 	{
 		// This is a checker that makes it possible for our server to get the correct remote IP even if using it with
 		// CloudFlare.
 		// https://support.cloudflare.com/hc/en-us/articles/200170786-Why-do-my-server-logs-show-CloudFlare-s-IPs-using-CloudFlare-
 		if ( http.getRequestHeaders().containsKey( "CF-Connecting-IP" ) )
 		{
 			return http.getRequestHeaders().get( "CF-Connecting-IP" ).get( 0 );
 		}
 		else
 		{
 			return http.getRemoteAddress().getAddress().getHostAddress();
 		}
 	}
 	
 	public int getRemotePort()
 	{
 		return http.getRemoteAddress().getPort();
 	}
 	
 	public int getContentLength()
 	{
 		try
 		{
 			http.getRequestBody().reset();
 			return http.getRequestBody().available();
 		}
 		catch ( IOException e )
 		{
 			return -1;
 		}
 	}
 	
 	public Object getAuthType()
 	{
 		return "";
 	}
 	
 	/**
 	 * Update once https is available
 	 */
 	public boolean isSecure()
 	{
 		return false;
 	}
 	
 	public int getServerPort()
 	{
 		return http.getLocalAddress().getPort();
 	}
 	
 	public String getServerName()
 	{
 		return http.getLocalAddress().getHostName();
 	}
 	
 	public String getParameter( String key )
 	{
 		return null;
 	}
 	
 	protected Site currentSite;
 	
 	protected void setSite( Site site )
 	{
 		currentSite = site;
 	}
 	
 	public Site getSite()
 	{
 		if ( currentSite == null )
 			return Loader.getPersistenceManager().getSiteManager().getSiteById( "framework" );
 		
 		return currentSite;
 	}
 	
 	public String getAttribute( String string )
 	{
 		return null;
 	}
 	
 	public void setHeader( String key, String value )
 	{
 		http.getResponseHeaders().set( key, value );
 	}
 	
 	protected HttpExchange getOriginal()
 	{
 		return http;
 	}
 	
 	public Map<String, String> getRequestMap()
 	{
 		Map<String, String> requestMap = new HashMap<String, String>();
 		
 		if ( getMap != null )
 			requestMap.putAll( getMap );
 		
 		if ( postMap != null )
 			requestMap.putAll( postMap );
 		
 		return requestMap;
 	}
 	
 	public Map<String, String> getPostMap()
 	{
 		if ( postMap == null )
 			postMap = new HashMap<String, String>();
 		
 		return postMap;
 	}
 	
 	public Map<String, String> getGetMap()
 	{
 		if ( getMap == null )
 			getMap = new HashMap<String, String>();
 		
 		return getMap;
 	}
 	
 	public int getRequestTime()
 	{
 		return requestTime;
 	}
 	
 	public String getLocalHost()
 	{
 		return getHeader( "Host" );
 	}
 	
 	public String getUserAgent()
 	{
 		return getHeader( "User-Agent" );
 	}
 }
