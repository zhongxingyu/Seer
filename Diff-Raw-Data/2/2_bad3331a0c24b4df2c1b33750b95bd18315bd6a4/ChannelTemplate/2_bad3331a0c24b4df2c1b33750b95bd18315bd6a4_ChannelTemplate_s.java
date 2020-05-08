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
 package org.mythtv.services.api.channel.impl;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.mythtv.services.api.ETagInfo;
 import org.mythtv.services.api.Int;
 import org.mythtv.services.api.MythServiceApiRuntimeException;
 import org.mythtv.services.api.StringList;
 import org.mythtv.services.api.channel.ChannelInfo;
 import org.mythtv.services.api.channel.ChannelInfoList;
 import org.mythtv.services.api.channel.ChannelInfoWrapper;
 import org.mythtv.services.api.channel.ChannelOperations;
 import org.mythtv.services.api.channel.Lineup;
 import org.mythtv.services.api.channel.LineupList;
 import org.mythtv.services.api.channel.VideoMultiplex;
 import org.mythtv.services.api.channel.VideoMultiplexList;
 import org.mythtv.services.api.channel.VideoMultiplexWrapper;
 import org.mythtv.services.api.channel.VideoSource;
 import org.mythtv.services.api.channel.VideoSourceList;
 import org.mythtv.services.api.channel.VideoSourceWrapper;
 import org.springframework.http.HttpMethod;
 import org.springframework.http.ResponseEntity;
 import org.springframework.util.LinkedMultiValueMap;
 import org.springframework.web.client.RestOperations;
 
 /**
  * @author Daniel Frey
  *
  */
 public class ChannelTemplate extends AbstractChannelOperations implements ChannelOperations {
 
 	private final RestOperations restOperations;
 
 	public ChannelTemplate( RestOperations restOperations, String apiUrlBase ) {
 		super( apiUrlBase );
 		this.restOperations = restOperations;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.mythtv.services.api.channel.ChannelOperations#addDBChannel(int, int, int, java.lang.String, java.lang.String, java.lang.String, int, int, int, boolean, boolean, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
 	 */
 	@Override
 	public boolean addDBChannel( int multiplexId, int sourceId, int channelId, String callSign, String channelName, String channelNumber, int serviceId, int atscMajorChannel, int atscMinorChannel, boolean userEIT, boolean visible, String frequencyId, String icon, String format, String xmltvId, String defaultAuthority ) throws MythServiceApiRuntimeException {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.mythtv.services.api.channel.ChannelOperations#addVideoSource(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, boolean, java.lang.String, int)
 	 */
 	@Override
 	public int addVideoSource( String sourceName, String grabber, String userId, String frequencyTable, String lineupId, String password, boolean useEit, String configPath, int nitId ) throws MythServiceApiRuntimeException {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.mythtv.services.api.channel.ChannelOperations#fetchChannelsFromSource(int, int, boolean)
 	 */
 	@Override
 	public int fetchChannelsFromSource( int sourceId, int cardId, boolean waitForFinish, ETagInfo etag ) throws MythServiceApiRuntimeException {
 		LinkedMultiValueMap<String, String> parameters = new LinkedMultiValueMap<String, String>();
 		parameters.add( "SourceId", "" + sourceId );
 		parameters.add("CardId", "" + cardId );
 		parameters.add("WaitForFinish", Boolean.toString(waitForFinish) );
 
 		ResponseEntity<Int> responseEntity = restOperations.exchange( buildUri( "FetchChannelsFromSource", parameters ), HttpMethod.GET, getRequestEntity(etag), Int.class );
 		handleResponseEtag(etag, responseEntity.getHeaders());
 		return responseEntity.getBody().getInteger().intValue();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.mythtv.services.api.channel.ChannelOperations#getChannelInfo(int)
 	 */
 	@Override
 	public ChannelInfoWrapper getChannelInfo( int channelId, ETagInfo etag ) throws MythServiceApiRuntimeException {
 
 		LinkedMultiValueMap<String, String> parameters = new LinkedMultiValueMap<String, String>();
 		parameters.add( "ChanId", "" + channelId );
 
 		ResponseEntity<ChannelInfoWrapper> responseEntity = restOperations.exchange( buildUri( "GetChannelInfo", parameters ), HttpMethod.GET, getRequestEntity(etag), ChannelInfoWrapper.class );
 		handleResponseEtag(etag, responseEntity.getHeaders());
 		return responseEntity.getBody();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.mythtv.services.api.channel.ChannelOperations#getChannelInfoList(int, int, int)
 	 */
 	@Override
 	public List<ChannelInfo> getChannelInfoList( int sourceId, int startIndex, int count, ETagInfo etag ) throws MythServiceApiRuntimeException {
 		
 		ResponseEntity<ChannelInfoList> responseEntity = getChannelInfoListResponseEntity( sourceId, startIndex, count, etag );
 		ChannelInfoList channelInfoList = responseEntity.getBody();	
 		return channelInfoList.getChannelInfos().getChannelInfos();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.mythtv.services.api.channel.ChannelOperations#getChannelInfoListResponseEntity(int, int, int)
 	 */
 	@Override
 	public ResponseEntity<ChannelInfoList> getChannelInfoListResponseEntity( int sourceId, int startIndex, int count, ETagInfo etag ) throws MythServiceApiRuntimeException {
 
 		LinkedMultiValueMap<String, String> parameters = new LinkedMultiValueMap<String, String>();
 		
 		if( sourceId > 0 ) {
			parameters.add( "SourceId", "" + sourceId );
 		}
 		
 		if( startIndex > 0 ) {
 			parameters.add( "StartIndex", "" + startIndex );
 		}
 
 		if( count > 0 ) {
 			parameters.add( "Count", "" + count );
 		}
 		
 		ResponseEntity<ChannelInfoList> responseEntity = restOperations.exchange( buildUri( "GetChannelInfoList", parameters ), HttpMethod.GET, getRequestEntity(etag), ChannelInfoList.class );
 		handleResponseEtag(etag, responseEntity.getHeaders());
 		return responseEntity;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.mythtv.services.api.channel.ChannelOperations#getChannelInfoList()
 	 */
 	@Override
 	public List<ChannelInfo> getChannelInfoList( ETagInfo etag ) throws MythServiceApiRuntimeException {
 
 		ResponseEntity<ChannelInfoList> responseEntity = getChannelInfoListResponseEntity(etag);
 		ChannelInfoList channelInfoList = responseEntity.getBody();
 		return channelInfoList.getChannelInfos().getChannelInfos();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.mythtv.services.api.channel.ChannelOperations#getChannelInfoListResponseEntity()
 	 */
 	@Override
 	public ResponseEntity<ChannelInfoList> getChannelInfoListResponseEntity(ETagInfo etag) throws MythServiceApiRuntimeException {
 		ResponseEntity<ChannelInfoList>  responseEntity = restOperations.exchange( buildUri( "GetChannelInfoList" ), HttpMethod.GET, getRequestEntity(etag), ChannelInfoList.class );
 		handleResponseEtag(etag, responseEntity.getHeaders());
 		return responseEntity;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.mythtv.services.api.channel.ChannelOperations#getDDLineupList(java.lang.String, java.lang.String, java.lang.String)
 	 */
 	@Override
 	public List<Lineup> getDDLineupList( String source, String userId, String password, ETagInfo etag ) throws MythServiceApiRuntimeException {
 		LinkedMultiValueMap<String, String> parameters = new LinkedMultiValueMap<String, String>();
 		if(source != null)
 			parameters.add("Source", source);
 		if(userId != null)
 			parameters.add("UserId", userId);
 		if(password != null)
 			parameters.add("Password", password);
 		ResponseEntity<LineupList> response = restOperations.exchange( buildUri( "GetDDLineupList", parameters ), HttpMethod.GET, getRequestEntity(etag), LineupList.class );
 		handleResponseEtag(etag, response.getHeaders());
 		return response.getBody().getLineups().getLineups();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.mythtv.services.api.channel.ChannelOperations#getVideoMultiplex(int)
 	 */
 	@Override
 	public VideoMultiplexWrapper getVideoMultiplex( int multiplexId, ETagInfo etag ) throws MythServiceApiRuntimeException {
 		LinkedMultiValueMap<String, String> parameters = new LinkedMultiValueMap<String, String>();
 		if( multiplexId > 0 ) {
 			parameters.add( "MplexID", "" + multiplexId );
 		}
 		ResponseEntity<VideoMultiplexWrapper> response = restOperations.exchange( buildUri( "GetVideoMultiplex", parameters ), HttpMethod.GET, getRequestEntity(etag), VideoMultiplexWrapper.class );
 		handleResponseEtag(etag, response.getHeaders());
 		return response.getBody();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.mythtv.services.api.channel.ChannelOperations#getVideoMultiplexList(int, int, int)
 	 */
 	@Override
 	public List<VideoMultiplex> getVideoMultiplexList( int sourceId, int startIndex, int count, ETagInfo etag ) throws MythServiceApiRuntimeException {
 		LinkedMultiValueMap<String, String> parameters = new LinkedMultiValueMap<String, String>();
 		if( sourceId > 0 ) {
 			parameters.add( "SourceID", "" + sourceId );
 		}
 		if(startIndex >= 0) {
 			parameters.add( "StartIndex", "" + startIndex );
 		}
 		if(count > 0) {
 			parameters.add( "Count", "" + count );
 		}
 		ResponseEntity<VideoMultiplexList> response = restOperations.exchange( buildUri( "GetVideoMultiplexList", parameters ), HttpMethod.GET, getRequestEntity(etag), VideoMultiplexList.class );
 		handleResponseEtag(etag, response.getHeaders());
 		return response.getBody().getVideoMultiplexes().getVideoMultiplexes();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.mythtv.services.api.channel.ChannelOperations#getVideoSource(int)
 	 */
 	@Override
 	public VideoSourceWrapper getVideoSource( int sourceId, ETagInfo etag ) throws MythServiceApiRuntimeException {
 		LinkedMultiValueMap<String, String> parameters = new LinkedMultiValueMap<String, String>();
 		if( sourceId > 0 ) {
 			parameters.add( "SourceID", "" + sourceId );
 		}
 		
 		ResponseEntity<VideoSourceWrapper> response = restOperations.exchange( buildUri( "GetVideoSource", parameters ), HttpMethod.GET, getRequestEntity(etag), VideoSourceWrapper.class );
 		handleResponseEtag(etag, response.getHeaders());
 		return response.getBody();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.mythtv.services.api.channel.ChannelOperations#getVideoSourceList()
 	 */
 	@Override
 	public List<VideoSource> getVideoSourceList(ETagInfo etag) throws MythServiceApiRuntimeException {
 		ResponseEntity<VideoSourceList> responseEntity = restOperations.exchange( buildUri( "GetVideoSourceList" ), HttpMethod.GET, getRequestEntity(etag), VideoSourceList.class );
 		VideoSourceList list = responseEntity.getBody();
 		handleResponseEtag(etag, responseEntity.getHeaders());
 		return list.getVideoSources().getVideoSources();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.mythtv.services.api.channel.ChannelOperations#getXmltvIdList(int)
 	 */
 	@Override
 	public List<String> getXmltvIdList( int sourceId, ETagInfo etag ) throws MythServiceApiRuntimeException {
 		LinkedMultiValueMap<String, String> parameters = new LinkedMultiValueMap<String, String>();
 		if( sourceId > 0 ) {
 			parameters.add( "SourceID", "" + sourceId );
 		}
 		ResponseEntity<StringList> responseEntity = restOperations.exchange( buildUri( "GetXMLTVIdList", parameters ), HttpMethod.GET, getRequestEntity(etag), StringList.class );
 		handleResponseEtag(etag, responseEntity.getHeaders());
 		return Arrays.asList( responseEntity.getBody().getStringList() );
 	}
 
 	/* (non-Javadoc)
 	 * @see org.mythtv.services.api.channel.ChannelOperations#reomveDBChannel(int)
 	 */
 	@Override
 	public boolean removeDBChannel( int channelId ) throws MythServiceApiRuntimeException {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.mythtv.services.api.channel.ChannelOperations#removeVideoSource(int)
 	 */
 	@Override
 	public boolean removeVideoSource( int sourceId ) throws MythServiceApiRuntimeException {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.mythtv.services.api.channel.ChannelOperations#updateDBChannel(int, int, int, java.lang.String, java.lang.String, java.lang.String, int, int, int, boolean, boolean, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
 	 */
 	@Override
 	public boolean updateDBChannel( int multiplexId, int sourceId, int channelId, String callSign, String channelName, String channelNumber, int serviceId, int atscMajorChannel, int atscMinorChannel, boolean useEIT, boolean visible, String frequencyId, String icon, String format, String xmltvId, String defaultAuthority ) throws MythServiceApiRuntimeException {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.mythtv.services.api.channel.ChannelOperations#updateVideoSource(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, boolean, java.lang.String, int)
 	 */
 	@Override
 	public boolean updateVideoSource( int sourceId, String sourceName, String grabber, String userId, String frequencyTable, String lineupId, String password, boolean userEIT, String configPath, int nitId ) throws MythServiceApiRuntimeException {
 		// TODO Auto-generated method stub
 		return false;
 	}
 }
