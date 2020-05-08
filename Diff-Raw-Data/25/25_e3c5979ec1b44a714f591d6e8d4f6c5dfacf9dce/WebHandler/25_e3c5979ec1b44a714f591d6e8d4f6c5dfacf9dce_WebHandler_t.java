 package com.chiorichan.http;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.IOException;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.TreeMap;
 
 import org.apache.commons.io.filefilter.WildcardFileFilter;
 import org.apache.commons.lang3.StringUtils;
 import org.apache.commons.lang3.exception.ExceptionUtils;
 
 import com.chiorichan.Loader;
 import com.chiorichan.database.SqlConnector;
 import com.chiorichan.event.EventException;
 import com.chiorichan.event.server.RenderEvent;
 import com.chiorichan.event.server.RequestEvent;
 import com.chiorichan.event.server.ServerVars;
 import com.chiorichan.framework.CodeParsingException;
 import com.chiorichan.framework.Evaling;
 import com.chiorichan.framework.Site;
 import com.chiorichan.util.Versioning;
 import com.sun.net.httpserver.HttpExchange;
 import com.sun.net.httpserver.HttpHandler;
 
 public class WebHandler implements HttpHandler
 {
 	protected Map<String, String> rewriteVars = new HashMap<String, String>();
 	protected Map<ServerVars, Object> serverVars = new HashMap<ServerVars, Object>();
 	protected File siteRoot = new File( Loader.webroot );
 	
 	public void handle( HttpExchange t ) throws IOException
 	{
 		HttpRequest request = new HttpRequest( t, this );
 		HttpResponse response = request.getResponse();
 		
 		try
 		{
 			String uri = request.getURI();
 			String domain = request.getDomain();
 			String site = "";
 			
 			if ( uri.startsWith( "/" ) )
 				uri = uri.substring( 1 );
 			
 			if ( domain.equalsIgnoreCase( "localhost" ) || domain.equalsIgnoreCase( "127.0.0.1" ) | domain.equalsIgnoreCase( request.getLocalAddr() ) )
 				domain = "";
 			
 			if ( domain.split( "\\." ).length > 2 )
 			{
 				String[] var1 = domain.split( "\\.", 2 );
 				site = var1[0];
 				domain = var1[1];
 			}
 			
 			Site currentSite = Loader.getPersistenceManager().getSiteManager().getSiteByDomain( domain );
 			
 			if ( currentSite == null )
 				currentSite = new Site( "default", Loader.getConfig().getString( "framework.sites.defaultTitle", "Unnamed Chiori Framework Site" ), domain );
 			
 			siteRoot = new File( Loader.webroot, currentSite.getWebRoot( site ) );
 			
 			if ( !siteRoot.exists() )
 				siteRoot.mkdirs();
 			
 			if ( uri.isEmpty() )
 				uri = "/";
 			
 			currentSite.setSubDomain( site );
 			request.setSite( currentSite );
 			
 			request.initSession();
 			
 			initServerVars( request );
 			
 			RequestEvent event = new RequestEvent( request );
 			
 			Loader.getPluginManager().callEvent( event );
 			
 			if ( event.isCancelled() )
 			{
 				Loader.getLogger().warning( "Navigation was cancelled by a Server Plugin" );
 				
 				int status = event.getStatus();
 				String reason = event.getReason();
 				
 				if ( status < 400 && status > 599 )
 				{
 					status = 502;
 					reason = "Navigation Cancelled by Internal Plugin Event";
 				}
 				
 				response.sendError( status, reason );
 				return;
 			}
 			
 			Map<String, String> result = null;
 			
 			result = rewriteVirtual( domain, site, uri );
 			
 			if ( result == null )
 			{
 				File dest = new File( siteRoot, uri );
 				
 				Loader.getLogger().info( "Requesting uri (" + currentSite.siteId + ") '" + uri + " from " + dest.getAbsolutePath() + "'" );
 				
 				if ( dest.isDirectory() )
 				{
 					// Scan for any file named index. This makes it possible for an image to act as an index page for a directory.
 					FileFilter fileFilter = new WildcardFileFilter( "index.*" );
 					File[] files = dest.listFiles( fileFilter );
 					
 					if ( files.length > 0 && files[0].exists() )
 					{
 						// TODO: Figure out if it's possible to make a priority of htm, groovy and chi over any other ext.
 						
 						uri = uri + "/" + files[0].getName();
 						
 						/*
 						 * if ( new File( dest, "index.html" ).exists() ) uri = uri + "/index.html"; else if ( new File( dest,
 						 * "index.htm" ).exists() ) uri = uri + "/index.htm"; else if ( new File( dest, "index.groovy"
 						 * ).exists() ) uri = uri + "/index.groovy"; else if ( new File( dest, "index.chi" ).exists() ) uri =
 						 * uri + "/index.chi";
 						 */
 					}
 					else if ( Loader.getConfig().getBoolean( "server.allowDirectoryListing" ) )
 					{
 						// TODO: Implement Directory Listings
 						
 						response.sendError( 403, "Sorry, Directory Listing has not been implemented on this Server!" );
 						return;
 					}
 					else
 					{
 						response.sendError( 403, "Directory Listing is Denied on this Server!" );
 						return;
 					}
 				}
 				
 				dest = new File( siteRoot, uri );
 				
 				if ( !dest.exists() )
 				{
 					// Attempt to determine if it's possible that the uri is a name with an extension.
 					// For Example: uri(http://example.com/pages/aboutus) = file([root]/pages/aboutus.html)
 					if ( dest.getParentFile().exists() && dest.getParentFile().isDirectory() )
 					{
 						FileFilter fileFilter = new WildcardFileFilter( dest.getName() + ".*" );
 						File[] files = dest.getParentFile().listFiles( fileFilter );
 						
 						if ( files.length > 0 && files[0].exists() )
 						{
 							dest = files[0];
 						}
 					}
 				}
 				
 				if ( dest.exists() )
 				{
 					result = new HashMap<String, String>();
 					result.put( "site", site );
 					result.put( "domain", domain );
 					result.put( "page", uri );
 					// result.put( "title", "" );
 					result.put( "reqlevel", "-1" );
 					// result.put( "theme", "" );
 					// result.put( "view", "" );
 					result.put( "html", "" );
 					result.put( "file", dest.getAbsolutePath() );
 				}
 			}
 			
 			if ( result == null )
 			{
 				response.sendError( 404 );
 				return;
 			}
 			
 			if ( !loadPage( request, response, result ) )
 				response.sendError( 500 );
 		}
 		catch ( Exception e )
 		{
 			// if ( e.getMessage().equals( "Broken pipe" ) )
 			// Loader.getLogger().warning(
 			// "Broken Pipe: The browser closed the connection before data could be written to it.", e );
 			// else
 			e.printStackTrace();
 			response.sendError( 500, null, "<pre>" + ExceptionUtils.getStackTrace( e ) + "</pre>" );
 			request.getSession().getEvaling().reset(); // XXX There is a bug with the buffer not clearing on exception. This should be a decent fix.
 		}
 	}
 	
 	/**
 	 * @return returns false if the page output was not handled by this method
 	 * @throws IOException
 	 */
 	public boolean loadPage( HttpRequest request, HttpResponse response, Map<String, String> pageData ) throws IOException
 	{
 		Site currentSite = request.getSite();
 		PersistentSession sess = request.getSession();
 		
 		if ( currentSite == null )
 			return false;
 		
 		String file = pageData.get( "file" );
 		String html = pageData.get( "html" );
 		
 		if ( file == null )
 			file = "";
 		
 		if ( html == null )
 			html = "";
 		
 		if ( file.isEmpty() && html.isEmpty() )
 			return false;
 		
 		File requestFile = null;
 		if ( !file.isEmpty() )
 		{
 			if ( currentSite.protectCheck( file ) )
 			{
 				Loader.getLogger().warning( "Loading of page '" + file + "' is not allowed since its hard protected in the site configs." );
 				response.sendError( 401, "Loading of this page is not allowed since its hard protected in the site configs." );
 				return true;
 			}
 			
 			// We expect the the provided file is an absolute file path.
 			requestFile = new File( file );
 			
 			Loader.getLogger().fine( "Requesting file: " + requestFile.getAbsolutePath() );
 			
 			if ( !requestFile.exists() || requestFile.isDirectory() )
 			{
 				Loader.getLogger().warning( "Could not load file '" + requestFile.getAbsolutePath() + "'" );
 				response.sendError( HttpCode.HTTP_NOT_FOUND );
 				return true;
 			}
 			
 			request.getSession().getBinding().setVariable( "__FILE__", requestFile );
 		}
 		
 		serverVars.put( ServerVars.DOCUMENT_ROOT, new File( Loader.getConfig().getString( "settings.webroot", "webroot" ), currentSite.getWebRoot( currentSite.getSubDomain() ) ).getAbsolutePath() );
 		
 		Map<String, Object> server = new HashMap<String, Object>();
 		
 		for ( Entry<ServerVars, Object> en : serverVars.entrySet() )
 		{
 			server.put( en.getKey().getName().toLowerCase(), en.getValue() );
 		}
 		
 		Evaling eval = sess.getEvaling();
 		
 		sess.setGlobal( "_SERVER", server );
 		sess.setGlobal( "_REQUEST", request.getRequestMap() );
 		sess.setGlobal( "_POST", request.getPostMap() );
 		sess.setGlobal( "_GET", request.getGetMap() );
 		sess.setGlobal( "_REWRITE", rewriteVars );
 		
 		FileInterpreter fi = new FileInterpreter( requestFile );
 		
 		if ( fi.get( "title" ) != null )
 			pageData.put( "title", fi.get( "title" ) );
 		
 		if ( fi.get( "reqlevel" ) != null )
 			pageData.put( "reqlevel", fi.get( "reqlevel" ) );
 		
 		if ( fi.get( "theme" ) != null )
 			pageData.put( "theme", fi.get( "theme" ) );
 		
 		if ( fi.get( "view" ) != null )
 			pageData.put( "view", fi.get( "view" ) );
 		
 		for ( Entry<String, String> kv : fi.getOverrides().entrySet() )
 			if ( !kv.getKey().equals( "title" ) && !kv.getKey().equals( "reqlevel" ) && !kv.getKey().equals( "reqlevel" ) && !kv.getKey().equals( "theme" ) && !kv.getKey().equals( "view" ) )
 				pageData.put( kv.getKey(), kv.getValue() );
 		
 		ReqFailureReason result = sess.doReqCheck( pageData.get( "reqlevel" ) );
 		
 		if ( result == ReqFailureReason.ACCEPTED )
 		{
 			if ( !html.isEmpty() )
 				eval.evalCode( html );
 			
 			if ( requestFile != null )
 				try
 				{
 					if ( fi.getOverrides().get( "shell" ).equals( "groovy" ) )
 						eval.evalFileVirtual( fi.getContent(), requestFile.getAbsolutePath() );
 					else
 						eval.write( fi.getContent() );
 				}
 				catch ( CodeParsingException e )
 				{
 					e.printStackTrace();
 					response.sendError( 500, null, "<pre>" + ExceptionUtils.getStackTrace( e ) + "</pre>" );
 					return true;
 					// TODO: Generate proper exception page
 				}
 		}
 		else if ( result == ReqFailureReason.NO_USER )
 		{
 			String loginForm = request.getSite().getYaml().getString( "scripts.login-form", "/login" );
 			Loader.getLogger().warning( "Requester of page '" + file + "' has been redirected to the login page." );
 			response.sendRedirect( loginForm + "?msg=" + result.getReason() + "&target=" + request.getURI() );
 			// TODO: Come up with a better way to handle the URI used in the target. ie. Params are lost.
 			return true;
 		}
 		else
 		{
 			response.sendError( 401, result.getReason() );
 		}
 		
 		// TODO: Possible theme'ing of error pages.
 		if ( response.stage == HttpResponseStage.CLOSED )
 			return true;
 		
 		// Allows scripts to directly override page data. For example: Themes, Views, Titles
 		for ( Entry<String, String> kv : response.pageDataOverrides.entrySet() )
 			pageData.put( kv.getKey(), kv.getValue() );
 		
 		String source = eval.reset();
 		/*
 		 * String source; if ( continueNormally ) { source = eval.reset(); } else { currentSite =
 		 * Loader.getPersistenceManager().getSiteManager().getSiteById( "framework" );
 		 * if ( currentSite instanceof FrameworkSite ) ( (FrameworkSite) currentSite ).setDatabase(
 		 * Loader.getPersistenceManager().getSql() );
 		 * source = alternateOutput; theme = "com.chiorichan.themes.error"; view = ""; }
 		 */
 		
 		RenderEvent event = new RenderEvent( sess, source, pageData );
 		
 		try
 		{
 			Loader.getPluginManager().callEventWithException( event );
 			
 			if ( event.sourceChanged() && !pageData.get( "shell" ).equals( "null" ) )
 				source = event.getSource();
 			
 			response.getOutput().write( source.getBytes( "ISO-8859-1" ) );
 		}
 		catch ( EventException ex )
 		{
 			ex.printStackTrace();
 			// response.getOutput().write( errorPage( ex.getCause() ).getBytes() );
 			// TODO: Generate a proper exception page
 			return false;
 		}
 		
 		/*
 		 * FileInputStream is; try { is = new FileInputStream( target ); } catch ( FileNotFoundException e ) {
 		 * response.sendError( 404 ); return; }
 		 * try { ByteArrayOutputStream buffer = response.getOutput();
 		 * int nRead; byte[] data = new byte[16384];
 		 * while ( ( nRead = is.read( data, 0, data.length ) ) != -1 ) { buffer.write( data, 0, nRead ); }
 		 * buffer.flush();
 		 * is.close(); } catch ( IOException e ) { e.printStackTrace(); response.sendError( 500, e.getMessage() ); return;
 		 * }
 		 */
 		
 		if ( requestFile != null )
 		{
 			response.setContentType( fi.getContentType() );
 		}
 		else
 		{
 			response.setContentType( "text/html" );
 		}
 		
 		response.sendResponse();
 		sess.releaseResources();
 		return true;
 	}
 	
 	/**
 	 * Scans the server database table 'pages' for page rewrite. This is similar to the rewrite module in apache but much
 	 * better.
 	 * 
 	 * @param domain
 	 * @param subdomain
 	 * @param uri
 	 * @return rewrite information
 	 */
 	public Map<String, String> rewriteVirtual( String domain, String subdomain, String uri )
 	{
 		SqlConnector sql = Loader.getPersistenceManager().getSql();
 		
 		try
 		{
 			// TODO: Fix the select issue with blank subdomains. It's not suppose to be 1111 but it is to prevent the
 			// redirect loop.
 			ResultSet rs = sql.query( "SELECT * FROM `pages` WHERE (site = '" + subdomain + "' OR site = '1111') AND domain = '" + domain + "' UNION SELECT * FROM `pages` WHERE (site = '" + subdomain + "' OR site = '1111') AND domain = '';" );
 			
 			if ( sql.getRowCount( rs ) > 0 )
 			{
 				Map<String, HashMap<String, String>> rewrite = new TreeMap<String, HashMap<String, String>>();
 				
 				do
 				{
 					HashMap<String, String> data = new HashMap<String, String>();
 					
 					String prop = rs.getString( "page" );
 					
 					if ( prop.startsWith( "/" ) )
 						prop = prop.substring( 1 );
 					
 					if ( uri.startsWith( "/" ) )
 						uri = uri.substring( 1 );
 					
 					data.put( "page", prop );
 					
 					String[] props = prop.split( "[.//]" );
 					String[] uris = uri.split( "[.//]" );
 					
 					String weight = StringUtils.repeat( "?", Math.max( props.length, uris.length ) );
 					
 					boolean whole_match = true;
 					for ( int i = 0; i < Math.max( props.length, uris.length ); i++ )
 					{
 						try
 						{
 							Loader.getLogger().fine( prop + " --> " + props[i] + " == " + uris[i] );
 							
 							if ( props[i].matches( "\\[([a-zA-Z0-9]+)=\\]" ) )
 							{
 								weight = replaceAt( weight, i, "Z" );
 								
 								String key = props[i].replaceAll( "[\\[\\]=]", "" );
 								String value = uris[i];
 								
 								rewriteVars.put( key, value );
 								
 								// PREP MATCH
 								
 								Loader.getLogger().fine( "Found a PREG match to " + rs.getString( "page" ) );
 							}
 							else if ( props[i].equals( uris[i] ) )
 							{
 								weight = replaceAt( weight, i, "A" );
 								
 								Loader.getLogger().fine( "Found a match to " + rs.getString( "page" ) );
 								// MATCH
 							}
 							else
 							{
 								whole_match = false;
 								Loader.getLogger().fine( "Found no match to " + rs.getString( "page" ) );
 								break;
 								// NO MATCH
 							}
 						}
 						catch ( ArrayIndexOutOfBoundsException e )
 						{
 							whole_match = false;
 							break;
 						}
 						catch ( Exception e )
 						{
 							e.printStackTrace();
 							whole_match = false;
 							break;
 						}
 					}
 					
 					if ( whole_match )
 					{
 						/*
 						 * data.put( "site", rs.getString( "site" ) ); data.put( "domain", rs.getString( "domain" ) );
 						 * data.put( "page", rs.getString( "page" ) ); data.put( "title", rs.getString( "title" ) ); data.put(
 						 * "reqlevel", rs.getString( "reqlevel" ) ); data.put( "theme", rs.getString( "theme" ) ); data.put(
 						 * "view", rs.getString( "view" ) ); data.put( "html", rs.getString( "html" ) ); data.put( "file",
 						 * rs.getString( "file" ) );
 						 */
 						
 						ResultSetMetaData rsmd = rs.getMetaData();
 						
 						int numColumns = rsmd.getColumnCount();
 						
 						for ( int i = 1; i < numColumns + 1; i++ )
 						{
 							String column_name = rsmd.getColumnName( i );
 							data.put( column_name, rs.getString( column_name ) );
 						}
 						
 						if ( data.get( "file" ) != null && !data.get( "file" ).isEmpty() )
 						{
 							File dest = new File( siteRoot, data.get( "file" ) );
 							data.put( "file", dest.getAbsolutePath() );
 						}
 						
 						rewrite.put( weight, data );
 					}
 				}
 				while ( rs.next() );
 				
 				if ( rewrite.size() > 0 )
 				{
 					@SuppressWarnings( "unchecked" )
 					HashMap<String, String> data = (HashMap<String, String>) rewrite.values().toArray()[0];
 					
 					Loader.getLogger().info( "Rewriting page request to " + data );
 					
 					return data;
 				}
 				
 				Loader.getLogger().warning( "Failed to find a page redirect for Framework Rewrite... '" + subdomain + "." + domain + "' '" + uri + "'" );
 				return null;
 			}
 			else
 			{
 				Loader.getLogger().warning( "Failed to find a page redirect for Framework Rewrite... '" + subdomain + "." + domain + "' '" + uri + "'" );
 				return null;
 			}
 		}
 		catch ( SQLException e )
 		{
 			e.printStackTrace(); // TODO: Better this catch
 		}
 		
 		return null;
 	}
 	
 	public String replaceAt( String par, int at, String rep )
 	{
 		StringBuilder sb = new StringBuilder( par );
 		sb.setCharAt( at, rep.toCharArray()[0] );
 		return sb.toString();
 	}
 	
 	public void initServerVars( HttpRequest request )
 	{
 		try
 		{
 			// serverVars.put( ServerVars.PHP_SELF, requestFile.getPath() );
 			serverVars.put( ServerVars.DOCUMENT_ROOT, Loader.getConfig().getString( "settings.webroot", "webroot" ) + request.getSite().getWebRoot( null ) );
 			serverVars.put( ServerVars.HTTP_ACCEPT, request.getHeader( "Accept" ) );
 			serverVars.put( ServerVars.HTTP_USER_AGENT, request.getUserAgent() );
 			serverVars.put( ServerVars.HTTP_CONNECTION, request.getHeader( "Connection" ) );
 			serverVars.put( ServerVars.HTTP_HOST, request.getLocalHost() );
 			serverVars.put( ServerVars.HTTP_ACCEPT_ENCODING, request.getHeader( "Accept-Encoding" ) );
 			serverVars.put( ServerVars.HTTP_ACCEPT_LANGUAGE, request.getHeader( "Accept-Language" ) );
			serverVars.put( ServerVars.HTTP_X_REQUESTED_WITH, request.getHeader( "X-requested-with" ) );
 			serverVars.put( ServerVars.REMOTE_HOST, request.getRemoteHost() );
 			serverVars.put( ServerVars.REMOTE_ADDR, request.getRemoteAddr() );
 			serverVars.put( ServerVars.REMOTE_PORT, request.getRemotePort() );
 			serverVars.put( ServerVars.REQUEST_TIME, request.getRequestTime() );
 			serverVars.put( ServerVars.REQUEST_URI, request.getURI() );
 			serverVars.put( ServerVars.CONTENT_LENGTH, request.getContentLength() );
 			serverVars.put( ServerVars.AUTH_TYPE, request.getAuthType() );
 			serverVars.put( ServerVars.SERVER_NAME, request.getServerName() );
 			serverVars.put( ServerVars.SERVER_PORT, request.getServerPort() );
 			serverVars.put( ServerVars.HTTPS, request.isSecure() );
 			serverVars.put( ServerVars.SESSION, request.getSession() );
 			serverVars.put( ServerVars.SERVER_SOFTWARE, Versioning.getProduct() );
 			serverVars.put( ServerVars.SERVER_ADMIN, Loader.getConfig().getString( "server.admin", "webmaster@" + request.getDomain() ) );
 			serverVars.put( ServerVars.SERVER_SIGNATURE, Versioning.getProduct() + " Version " + Versioning.getVersion() );
 		}
 		catch ( Exception e )
 		{
 			e.printStackTrace();
 		}
 	}
 }
