 package org.geoserver.ows.http;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.AbstractController;
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserFactory;
 
 public class Dispatcher extends AbstractController {
 
 	protected ModelAndView handleRequestInternal(
 		HttpServletRequest httpRequest, HttpServletResponse httpResponse
 	) throws Exception {
 	
 		//step 1: parse kvp set
 		Map kvp = parseKVP( httpRequest );
 		
 		//step 2: determine which operation is being called
 		String method = httpRequest.getMethod();
 		String service = null;
 		String request = null;
 		File cache = cacheInputStream( httpRequest );
 		
 		if ( "get".equalsIgnoreCase( method ) ) {
 			//lookup in query string
 			service = (String) kvp.get( "service" );
 			request = (String) kvp.get( "request" );
 		}
 		else if ( "post".equalsIgnoreCase( method ) ) {
 			InputStream input = input( cache );
 			if ( input != null ) {
 				try {
 					Map xml = readOpPost( input );
 					service = (String) xml.get( "service" );
 					request = (String) xml.get( "request" );
 				}
 				finally {
 					input.close();
 				}	
 			}
 		}
 		
 		if ( service == null || request == null) {
 			Map map = readOpContext( httpRequest );
 			if ( service == null ) {
 				service = (String) map.get( "service" );
 			}
 			if ( request == null ) {
 				request = (String) map.get( "request" );
 			}
 			
 		}
 		
 		if ( request == null ) {
 			String msg = "Could not determine request.";
 			throw new RuntimeException( msg );
 		}
 		
 		//step 2: lookup the operation
 		Collection operations = getApplicationContext()
 			.getBeansOfType( Operation.class ).values();
 		List matches = new ArrayList();
 		for ( Iterator itr = operations.iterator(); itr.hasNext(); ) {
 			Operation bean = (Operation) itr.next();
 			if (bean.getId().equalsIgnoreCase( request ) ) {
 				if ( service == null || service.equalsIgnoreCase( bean.getServiceId() ) ) {
 					matches.add( bean );
 				}
 			}
 		}
 		
 		if ( matches.isEmpty() ) {
 			String msg = "No operation: (" + service + "," + request + ",)"; 
 			throw new RuntimeException( msg );
 		}
 		
 		if ( matches.size() > 1 ) {
 			String msg = "Multiple operation: (" + service + "," + request + ",)"; 
 			throw new RuntimeException( msg );
 		}
 		
 		Operation opBean = (Operation) matches.get( 0 );
 		Object op = opBean.getOperation().newInstance();
 		
 		//step 3: set the params
 		for ( Iterator itr = kvp.entrySet().iterator(); itr.hasNext(); ) {
 			Map.Entry entry = (Map.Entry) itr.next();
 			String key = (String) entry.getKey();
 			Object val = entry.getValue();
 			
 			opBean.set( op, key, val );
 		}
 		
 		//step 4: execute
 		Object result = null;
 		if ( cache == null ) {
 			result = opBean.run( op, null );
 		}
 		else {
 			Object input = parseXML( cache );
 			result = opBean.run( op, input );
 		}
 		
 		//step 5: write response
 		if ( result == null ) {
 			//look up the response for the result
 			Collection responses = getApplicationContext()
 				.getBeansOfType( Response.class ).values();
 			for( Iterator itr = responses.iterator(); itr.hasNext(); ) {
 				Response response = (Response) itr.next();
 				
 			}
 		}
 		
 		if (cache != null) {
 			cache.deleteOnExit();	
 		}
 		
 		return null;
 	}
 
 	File cacheInputStream( HttpServletRequest request ) throws IOException  {
 		InputStream input = request.getInputStream();
 		if ( input == null )
 			return null;
 		
 		File cache = File.createTempFile("geoserver","req");
 		BufferedOutputStream output = 
 			new BufferedOutputStream( new FileOutputStream( cache ) );
 		
 		byte[] buffer = new byte[1024];
 		int nread = 0;
 		
 		while (( nread = input.read( buffer ) ) > 0) {
 			output.write( buffer, 0, nread );
 		}
 		
 		output.flush();
 		output.close();
 		
 		return cache;
 	}
 	
 	BufferedInputStream input( File cache ) throws IOException {
 		return cache == null ? null :
 			new BufferedInputStream( new FileInputStream( cache ) );
 	}
 	
 	Map parseKVP( HttpServletRequest request ) {
 		//unparsed kvp set
 		Map kvp = request.getParameterMap();
 		
 		//look up parser objects
 		Collection parsers = getApplicationContext().getBeansOfType(KVPParser.class).values();
 		Map parsedKvp = new HashMap();
 		
 		for ( Iterator itr = kvp.entrySet().iterator(); itr.hasNext(); ) {
 			Map.Entry entry = (Map.Entry) itr.next();
 			String key = (String) entry.getKey();
 			String value = (String) entry.getValue();
 			
 			//find the parser for this key value pair
 			Object parsed = null;
 			for ( Iterator pitr = parsers.iterator(); pitr.hasNext(); ) {
 				KVPParser parser = (KVPParser) pitr.next();
 				if ( key.equals( parser.getKey() ) ) {
 					try {
 						parsed = parser.parse( value );
 					}
 					catch(Throwable t) {
 						//TODO: log
 					}
 				}
 			}
 			
 			//if noone could parse, just set to string value
 			if ( parsed == null ) {
 				parsed = value;
 			}
 			
 			//convert key to lowercase 
 			parsedKvp.put( key.toLowerCase(), parsed );
 		}
 		
 		return parsedKvp;
 	}
 	
 	Object parseXML( File cache ) throws Exception {
 		InputStream input = input( cache );
 		
 		//create stream parser
 		XmlPullParserFactory factory = 
 			XmlPullParserFactory.newInstance();
 		factory.setNamespaceAware(true);
 		factory.setValidating(false);
 			
 		//parse root element
 		XmlPullParser parser = factory.newPullParser();
 		parser.setInput( input, "UTF-8" );
 		parser.nextTag();
 		
 		String ns = parser.getNamespace() != null ? parser.getNamespace() : "";
 		String local = parser.getName();
 		
 		parser.setInput(null);
 		
 		//reset input stream
 		input.close();
 		input = input( cache );
 		
 		Collection xmlParsers = 
 			getApplicationContext().getBeansOfType( XMLParser.class ).values();
 		
 		for ( Iterator itr = xmlParsers.iterator(); itr.hasNext(); ) {
 			XMLParser xmlParser = (XMLParser) itr.next();
 			
 			String pns = xmlParser.getNamespace() != null ? 
 					xmlParser.getNamespace() : "";
 			String plocal = xmlParser.getElement();
 			
 			if ( pns.equals( ns ) && plocal.equals( local )) {
 				return xmlParser.parse( input );
 			}
 		}
 		
 		return null;
 		
 	}
 	
 	Map readOpContext( HttpServletRequest request ) {
 		//try to get from request url
 		String ctxPath = request.getContextPath();
 		String reqPath = request.getRequestURI();
 		reqPath = reqPath.substring( ctxPath.length() );
 		
 		if ( reqPath.startsWith("/") ) {
 			reqPath = reqPath.substring( 1, reqPath.length() );
 		}
 		
 		if ( reqPath.endsWith("/") ) {
 			reqPath = reqPath.substring( 0, reqPath.length()-1 );
 		}
 		
 		Map map = new HashMap();
 		int index = reqPath.indexOf('/');
 		if (index != -1) {
 			
 			map.put( "service", reqPath.substring( 0, index ) );
 			map.put( "request", reqPath.substring( index + 1 ) );
 		}
 		else {
 			
 			map.put( "service", reqPath );
 			
 		}
 		
 		return map;
 	}
 	
 	Map readOpPost( InputStream input ) throws Exception {
 	
 		//create stream parser
 		XmlPullParserFactory factory = 
 			XmlPullParserFactory.newInstance();
 		factory.setNamespaceAware(true);
 		factory.setValidating(false);
 			
 		//parse root element
 		XmlPullParser parser = factory.newPullParser();
 		parser.setInput( input, "UTF-8" );
 		parser.nextTag();
 		
 		Map map = new HashMap();
 		map.put( "request", parser.getName() );
 		for ( int i = 0; i < parser.getAttributeCount(); i++ ) {
 			if ( "service".equals( parser.getAttributeName( i ) ) ) {
 				map.put( "service", parser.getAttributeValue(i) );
 				break;
 			}
 		}
 		
 		//close parser + release resources
 		parser.setInput(null);
 		input.close();
 		
 		return map;
 	}
 }
