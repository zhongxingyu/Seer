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
 package org.mythtv.services.api.frontend.impl;
 
 import org.joda.time.DateTime;
 import org.mythtv.services.api.Bool;
 import org.mythtv.services.api.ETagInfo;
 import org.mythtv.services.api.MythServiceApiRuntimeException;
 import org.mythtv.services.api.frontend.FrontendActionList;
 import org.mythtv.services.api.frontend.FrontendOperations;
 import org.mythtv.services.api.frontend.FrontendStatus;
 import org.springframework.http.HttpMethod;
 import org.springframework.http.ResponseEntity;
 import org.springframework.util.LinkedMultiValueMap;
 import org.springframework.web.client.RestOperations;
 
 /**
  * @author Daniel Frey
  * 
  */
 public class FrontendTemplate extends AbstractFrontendOperations implements FrontendOperations {
 
 	private final RestOperations restOperations;
 
 	public FrontendTemplate( RestOperations restOperations, String apiUrlBase ) {
 		super( apiUrlBase );
 		this.restOperations = restOperations;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.mythtv.services.api.frontend.FrontendOperations#getStatus(java.lang
 	 * .String)
 	 */
 	@Override
 	public ResponseEntity<FrontendStatus> getStatus( String frontedApiUrlBase, ETagInfo etag )
 			throws MythServiceApiRuntimeException {
 
 		ResponseEntity<FrontendStatus> responseEntity = restOperations.exchange( frontedApiUrlBase
 				+ "/Frontend/GetStatus", HttpMethod.GET, getRequestEntity( etag ), FrontendStatus.class );
 		handleResponseEtag( etag, responseEntity.getHeaders() );
 		
 		return responseEntity;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.mythtv.services.api.frontend.FrontendOperations#sendMessage(java.
 	 * lang.String, java.lang.String)
 	 */
 	@Override
 	public ResponseEntity<Bool> sendMessage( String frontedApiUrlBase, String message ) throws MythServiceApiRuntimeException {
 
 		LinkedMultiValueMap<String, String> parameters = new LinkedMultiValueMap<String, String>();
 		parameters.add( "Message", message );
 
 		ResponseEntity<Bool> responseEntity = restOperations.exchange(
 				buildUri( frontedApiUrlBase + "/Frontend/SendMessage", parameters ), HttpMethod.GET,
 				getRequestEntity( null ), Bool.class );
 
 		return responseEntity;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.mythtv.services.api.frontend.FrontendOperations#sendAction(java.lang
 	 * .String, java.lang.String, java.lang.String, int, int)
 	 */
 	@Override
 	public ResponseEntity<Bool> sendAction( String frontedApiUrlBase, String action, String file, int width, int height )
 			throws MythServiceApiRuntimeException {
 
 		LinkedMultiValueMap<String, String> parameters = new LinkedMultiValueMap<String, String>();
 		parameters.add( "Action", action );
 		
 		if( null != file ) {
 			parameters.add( "File", file );
 		}
 		
 		if( width > 0 && height > 0 ) {
 			parameters.add( "Width", "" + width );
 			parameters.add( "Height", "" + height );
 		}
 
 		ResponseEntity<Bool> responseEntity = restOperations.exchange(
 				buildUri( frontedApiUrlBase + "/Frontend/SendAction", parameters ),
 				HttpMethod.GET,
 				getRequestEntity( null ),
 				Bool.class );
 		
 		return responseEntity;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.mythtv.services.api.frontend.FrontendOperations#getActionList(java
 	 * .lang.String)
 	 */
 	@Override
 	public ResponseEntity<FrontendActionList> getActionList( String frontedApiUrlBase, ETagInfo etag )
 			throws MythServiceApiRuntimeException {
 
 		ResponseEntity<FrontendActionList> responseEntity = restOperations.exchange( buildUri( frontedApiUrlBase
 				+ "/Frontend/GetActionList" ), HttpMethod.GET, getRequestEntity( etag ), FrontendActionList.class );
 		handleResponseEtag( etag, responseEntity.getHeaders() );
 		
 		return responseEntity;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * - GET
 	 * 
 	 * @param frontedApiUrlBase
 	 * @param channelId - (Required) The database channel id for the recording
 	 * @param startTime - (Required) The recording start time for the item. This should be in MySQL ISO format, eg: 2011-08-29 18:59:00. You can replace the space with %20 or T.
 	 * @return
 	 * @throws MythServiceApiRuntimeException
 	 */
 	@Override
 	public ResponseEntity<Bool> playRecording(String frontedApiUrlBase, Long channelId, DateTime startTime)
 	{
 		LinkedMultiValueMap<String, String> parameters = new LinkedMultiValueMap<String, String>();
 		parameters.add( "ChanId", channelId.toString() );
		parameters.add( "StartTime", startTime.toString());
 		
 		ResponseEntity<Bool> responseEntity = restOperations.exchange(
				buildUri( frontedApiUrlBase + "/Frontend/PlayRecording", parameters ),
 				HttpMethod.GET,
 				getRequestEntity( null ),
 				Bool.class );
 		
 		return responseEntity;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * - Get 
 	 * 
 	 * @param frontedApiUrlBase
 	 * @param id - (Required) The database id for the video file
 	 * @param useBookmark - (Optional) Pass 1 to resume playback at a bookmark
 	 * @return
 	 * @throws MythServiceApiRuntimeException
 	 */
 	@Override
 	public ResponseEntity<Bool> playVideo(String frontedApiUrlBase, String id, boolean useBookmark)
 	{
 		LinkedMultiValueMap<String, String> parameters = new LinkedMultiValueMap<String, String>();
 		parameters.add( "Id", id.toString() );
 		if(useBookmark) parameters.add( "UseBookmark", "1");
 		
 		ResponseEntity<Bool> responseEntity = restOperations.exchange(
				buildUri( frontedApiUrlBase + "/Frontend/PlayVideo", parameters ),
 				HttpMethod.GET,
 				getRequestEntity( null ),
 				Bool.class );
 		
 		return responseEntity;
 	}
 
 }
