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
  * This software can be found at <https://github.com/MythTV-Android/mythtv-for-android/>
  *
  */
 package org.mythtv.services.api;
 
 import java.net.URI;
 import java.util.Collections;
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
 	
	protected static final DateTimeFormatter formatter = DateTimeFormat.forPattern( "yyyy-MM-dd'T'HH:mm:ss" );
 
 	private final String apiUrlBase;
 
 	private HttpEntity<?> requestEntity;
 	
 	private final Logger logger;
 	
 	/**
 	 * @param apiUrlBase
 	 */
 	public AbstractOperations( String apiUrlBase ) {
 		this.apiUrlBase = apiUrlBase;
 		logger = Logger.getLogger(AbstractOperations.TAG);
 		
 		HttpHeaders requestHeaders = new HttpHeaders();
 		requestHeaders.setAccept( Collections.singletonList( MediaType.APPLICATION_JSON ) );
 		requestHeaders.setAcceptEncoding( Collections.singletonList( ContentCodingType.GZIP ) );
 		
 		requestEntity = new HttpEntity<Object>( requestHeaders );
 	}
 
 	/**
 	 * @param path
 	 * @return
 	 */
 	protected URI buildUri( String path ) {
 		logger.info( buildUri( path, EMPTY_PARAMETERS ).toString() );
 		
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
 		logger.warning( "URI : " + URIBuilder.fromUri( getApiUrlBase() + path ).queryParams( parameters ).build() );
 		
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
 	protected HttpEntity<?> getRequestEntity() {
 		return requestEntity;
 	}
 
 	protected String convertUtcAndFormat( DateTime dt ) {
 		return formatter.print( dt.withZone( DateTimeZone.UTC ) );
 	}
 	
 	private static final LinkedMultiValueMap<String, String> EMPTY_PARAMETERS = new LinkedMultiValueMap<String, String>();
 
 }
