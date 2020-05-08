 /**
  *  This file is part of MythTV for Android
  * 
  *  MythTV for Android is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  MythTV for Android is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with MythTV for Android.  If not, see <http://www.gnu.org/licenses/>.
  *   
  * This software can be found at <https://github.com/MythTV-Android/MythTV-Service-API/>
  *
  */
 package org.mythtv.services.api;
 
 import java.net.URI;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 import org.springframework.http.ContentCodingType;
 import org.springframework.http.HttpEntity;
 import org.springframework.http.HttpHeaders;
 import org.springframework.http.MediaType;
 import org.springframework.social.support.URIBuilder;
 import org.springframework.util.LinkedMultiValueMap;
 import org.springframework.util.MultiValueMap;
 
 /**
  * @author Daniel Frey
  * 
  */
 public abstract class AbstractOperations {
 	private static final String TAG = AbstractOperations.class.getSimpleName();
 	private static final String MYTHTV_ETAG = "If-None-Match";
 	
 	protected static final DateTimeFormatter formatter = DateTimeFormat.forPattern( "yyyy-MM-dd'T'HH:mm:ss" );
 
 	private final String apiUrlBase;
 	
 	private final Logger logger;
 	
 	/**
 	 * @param apiUrlBase
 	 */
 	public AbstractOperations( String apiUrlBase ) {
 		this.apiUrlBase = apiUrlBase;
 		logger = Logger.getLogger( AbstractOperations.TAG );
 	}
 
 	public static void setLogLevel(Level lvl){
 		Logger.getLogger( AbstractOperations.TAG ).setLevel(lvl);
 	}
 	
 	/**
 	 * @param path
 	 * @return
 	 */
 	protected URI buildUri( String path ) {
 		if (logger.isLoggable(Level.FINE))
 			logger.fine( buildUri( path, EMPTY_PARAMETERS ).toString() );
 		
 		return buildUri( path, EMPTY_PARAMETERS );
 	}
 
 	/**
 	 * @param path
 	 * @param parameterName
 	 * @param parameterValue
 	 * @return
 	 */
 	protected URI buildUri( String path, String parameterName, String parameterValue ) {
 		MultiValueMap<String, String> parameters = new LinkedMultiValueMap<String, String>();
 		parameters.set( parameterName, parameterValue );
 
 		return buildUri( path, parameters );
 	}
 
 	/**
 	 * @param path
 	 * @param parameters
 	 * @return
 	 */
 	protected URI buildUri( String path, MultiValueMap<String, String> parameters ) {
 		if (logger.isLoggable(Level.FINE))
 			logger.fine( "URI : " + URIBuilder.fromUri( getApiUrlBase() + path ).queryParams( parameters ).build() );
 		
 		return URIBuilder.fromUri( getApiUrlBase() + path ).queryParams( parameters ).build();
 	}
 
 	/**
 	 * @return
 	 */
 	protected String getApiUrlBase() {
 		return apiUrlBase;
 	}
 
 	/**
 	 * @return the requestEntity
 	 */
 	protected HttpEntity<?> getRequestEntity( ETagInfo info, MediaType ...mediaTypes ) {
 		
		if( null == mediaTypes ) {
 			mediaTypes = new MediaType[ 1 ];
 			mediaTypes[ 0 ] = MediaType.APPLICATION_JSON;
 		}
 		
 		if( mediaTypes.length > 1 ) {
 			throw new IllegalArgumentException( "Should only be one MediaType here" );
 		}
 		
 		HttpHeaders requestHeaders = new HttpHeaders();
 		requestHeaders.setAccept( Arrays.asList( mediaTypes ) );
 		if( info != null && !info.isEmptyEtag() ) {
 			// it seems that mythtv uses "If-None-Match" and not "ETag"
 			// we will still add etag the regular way and add it via If-None-Match 
 			requestHeaders.setETag( info.getETag() );
 			requestHeaders.add( MYTHTV_ETAG, info.getETag() );
 
 			// Currently does not work with .26 backends
 			requestHeaders.setAcceptEncoding( ContentCodingType.GZIP );
 		}
 				
 		return new HttpEntity<Object>( requestHeaders );
 	}
 	
 	protected void handleResponseEtag(ETagInfo etagInfo, HttpHeaders headers){
 		if(etagInfo != null){
 			etagInfo.setETag(headers.getETag());
 		}
 	}
 
 	protected String convertUtcAndFormat( DateTime dt ) {
 		return formatter.print( dt.withZone( DateTimeZone.UTC ) );
 	}
 		
 	private static final LinkedMultiValueMap<String, String> EMPTY_PARAMETERS = new LinkedMultiValueMap<String, String>();
 
 }
