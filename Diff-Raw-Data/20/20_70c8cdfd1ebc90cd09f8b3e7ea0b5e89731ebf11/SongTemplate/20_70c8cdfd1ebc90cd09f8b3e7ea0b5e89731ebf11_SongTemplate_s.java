 /*
  * Copyright 2011 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.springframework.social.exfm.api.impl;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.springframework.data.domain.Page;
 import org.springframework.data.domain.PageImpl;
 import org.springframework.data.domain.Pageable;
 import org.springframework.social.ResourceNotFoundException;
 import org.springframework.social.exfm.api.ExFmProfile;
 import org.springframework.social.exfm.api.Song;
 import org.springframework.social.exfm.api.SongOperations;
 import org.springframework.social.exfm.api.impl.json.ExFmSongsResponse;
 import org.springframework.social.exfm.api.impl.json.ExFmUserResponse;
 import org.springframework.social.exfm.api.impl.json.SongList;
 import org.springframework.social.exfm.api.impl.json.SongResponse;
 import org.springframework.web.client.RestTemplate;
 
 /**
  * @author Michael Lavelle
  */
 public class SongTemplate extends
 		AbstractExFmResourceOperations implements SongOperations {
 
 	private String username;
 	private String password;
 	private boolean useOauth;
 	
 	public SongTemplate(String apiBaseUrl, RestTemplate restTemplate,
 			boolean isAuthorizedForUser) {
 		super(apiBaseUrl, restTemplate, isAuthorizedForUser);
 		this.useOauth = true;
 	}
 	
 	public SongTemplate(String apiBaseUrl, RestTemplate restTemplate,String username,String password) {
 		super(apiBaseUrl, restTemplate, true);
 		this.useOauth = true;
 		this.username = username;
 		this.password = password;
 		this.useOauth = false;
 	}
 	
 
 
 	public ExFmProfile getUserProfile() {
 		return restTemplate.getForObject(getApiResourceUrl(""),
 				ExFmUserResponse.class).getNestedResponse();
 	}
 
 	@Override
 	protected String getApiResourceBaseUrl() {
 		return getApiBaseUrl() + "/song";
 	}
 
 	@Override
 	public Song getSongBySourceUrl(String sourceUrl) {
 		Song song = restTemplate.getForObject(
 				getApiResourceUrl("/" + sourceUrl), SongResponse.class).getNestedResponse();
 		if (song.getTitle() == null)
 		{
 			throw new ResourceNotFoundException("No song details available for source url :" + sourceUrl);
 		}	
 		return song;
 	}
 	
 	@Override
 	public Song getSongById(String songId) {
 		Song song = restTemplate.getForObject(
 				getApiResourceUrl("/" + songId), SongResponse.class).getNestedResponse();
 		if (song.getTitle() == null)
 		{
 			throw new ResourceNotFoundException("No song details available for song id :" + songId);
 		}
 		return song;
 	}
 	
	private String getContextParamsString(String contextUrl,String fromUser)
 	{
 		String contextParamsString = "";
 		if (contextUrl != null)
 		{
			contextParamsString = contextParamsString + "&source=" + contextUrl;
 		}
 		if (fromUser != null)
 		{
			contextParamsString = contextParamsString + "&context=" + fromUser;
 		}
 		return contextParamsString;
 	}
 	
 	@Override
 	public void loveSongById(String songId)
 	{
 		loveSongById(songId,null,null);
 	}
 	
 	@Override
 	public void loveSongBySourceUrl(String sourceUrl)
 	{
 		loveSongBySourceUrl(sourceUrl,null,null);
 	}
 	
 	@Override
 	public void loveSongById(String songId,String contextUrl,String fromUser)
 	{
 		requireAuthorization();
		restTemplate.postForObject(getApiResourceUrl("/" + songId + "/love") + (useOauth ? ""
				: ("?username=" + username + "&password=" + password + getContextParamsString(contextUrl,fromUser))),null,String.class);	
 	}
 	
 	@Override
 	public void loveSongBySourceUrl(String sourceUrl,String contextUrl,String fromUser)
 	{
 		requireAuthorization();
 		Song song = getSongBySourceUrl(sourceUrl);
 		
 		restTemplate.postForObject(getApiResourceUrl("/" + song.getId() + "/love") + (useOauth ? ""
				: ("?username=" + username + "&password=" + password + getContextParamsString(contextUrl,fromUser))),null,String.class);	
 	}
 	
 	@Override
 	public Page<Song> search(String searchText) {
 		return search(searchText,null);
 	}
 	
 	@Override
 	public Page<Song> search(String searchText,Pageable pageable) {
 		ExFmSongsResponse songsResponse= restTemplate.getForObject(
 				getApiResourceUrl("/search/" + searchText,pageable), ExFmSongsResponse.class);
 		List<Song> songList = 
 			songsResponse.getNestedResponse();
 		long totalResults = (songsResponse.getTotal() == null) ? songList.size() : songsResponse.getTotal().longValue();
 		return new PageImpl<Song>(songList == null ? new ArrayList<Song>() : songList,songsResponse.getPageable(pageable,songList),totalResults);
 	}
 	
 
 }
