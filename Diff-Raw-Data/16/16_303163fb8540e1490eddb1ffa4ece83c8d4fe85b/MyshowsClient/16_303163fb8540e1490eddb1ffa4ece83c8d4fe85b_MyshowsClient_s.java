 /*
 Copyright (c) 2011, Ilya Arefiev <arefiev.id@gmail.com>
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are
 met:
  * Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.
  * Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the
    distribution.
  * Neither the name of the author nor the names of its
    contributors may be used to endorse or promote products derived
    from this software without specific prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 
 package aid.lib.myshows;
 
 import java.util.Iterator;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 /**
  * MyShows API public Client
  * @see <a href="http://api.myshows.ru/">http://api.myshows.ru/</a>
  * @author Ilya Arefiev (arefiev.id@gmail.com)
  *
  */
 public class MyshowsClient {
 	protected MyshowsAPI api=null;
 	
 	/**
 	 * dummy constructor<br>
 	 * just creates {@link MyshowsAPI}
 	 */
 	public MyshowsClient() {
 		api=new MyshowsAPI();
 		
 //		System.out.println("+++ MyshowsClient()");
 		
 		// TODO: check if offline
 	}
 	
 	/**
 	 * login into username's account<br>
 	 * calls <code>MyshowsAPI.login()</code>
 	 * @param _username
 	 * @param _password
 	 * @return <code>true</code> if success<br>
 	 * 			<code>false</code> otherwise 
 	 */
 	public boolean login(String _username, String _password) {
 //		System.out.println("+++ login(String "+_username+", String "+_password+")");
 		
 		return api.login(_username, _password);
 	}
 	
 	/**
 	 * logout from user's account with clearing auth info<br>
 	 * <b>currently does nothing</b>
 	 * @return currently <code>false</code>
 	 */
 	public boolean logout() {
 		// TODO: implement logout @ API && client
 //		System.out.println("+++ logout()");
 		return api.logout();
 	}
 	
 	/**
 	 * get all shows (watching, canceled, etc) of user<br>
 	 * calls <code>MyshowsAPI.getShows()</code><br>
 	 * 
 	 * {@link JSONArray} format:
 		<pre>[
   {
     "rating": 0,
     "ruTitle": "$translated_title",
     "runtime": $episode_duration,
     "showId": $showId,
     "showStatus": "$show_status", // Canceled/Ended || Returning Series
     "title": "$original_title",
     "totalEpisodes": $num_of_totlat_episodes,
     "watchStatus": "$user's_watching_status",	// watching || cancelled
     "watchedEpisodes": $num_of_watched_episodes
   }
 ]
 		</pre>
 	 * @return {@link JSONArray} with shows if success<br>
 	 * 			<code>null</code> otherwise
 	 */
 	public JSONArray getShows() {
 //		System.out.println("+++ getShows()");
 		
 		JSONObject shows=null;
 		JSONArray ret=null;
 		String result=api.getShows();
 		
 		if ( result!=null ) {
 			try {
 				// put shows in jsonobject{ "showid":{"info"} }
 				shows=new JSONObject(result);
 				
 //				System.out.println(shows.toString(2));
 				// get all "showid"
 				Iterator<String> iter = shows.keys();
 				
 				ret=new JSONArray();
 				
 				// add info into jsonarray
 				while ( iter.hasNext() ) {
 					ret.put(
 							shows.getJSONObject( iter.next() )
 							);
 				}
 				
 			} catch (Exception e) {
 				System.err.println("--- oops: "+e.getMessage());
 				e.printStackTrace();
 			}
 		} else {
 			System.err.println("--- null from API call");
 		}
 		
 		return ret;
 	}
 	
 	/**
 	 * get all unwatched episodes of all user's shows or filtered by
 	 * <code>_show</code><br>
 	 * calls <code>MyshowsAPI.getUnwatchedEpisodes()</code> and filter result by
 	 * <code>_show</code> (if <code>_show</code>>0)<br>
 	 * {@link JSONArray} format:
 		<pre>[
   {
     "airDate": "$dd.mm.yyyy",
     "episodeId": $episodeId,
     "episodeNumber": $episode_number,
     "seasonNumber": $season_number,
     "showId": $showId,
     "title": "$original_episode_title"
   }
 ]
 		</pre>
 	 * @param _show $showId to filter unwatched episodes
 	 * @return {@link JSONArray} with episodes if success<br>
 	 * 			<code>null</code> otherwise
 	 */
 	public JSONArray getUnwatchedEpisodes(int _show) {
 //		System.out.println("+++ getUnwatchedEpisodes("+_show+")");
 		
 		JSONObject unwatched=null;
 		JSONArray ret=null;
 		String result=api.getUnwatchedEpisodes();
 		
 		if ( result!=null ) {
 			try {
 				// put episodes in jsonobject{ "showid":{"info"} }
 				unwatched=new JSONObject(result);
 				
 //				System.out.println(unwatched.toString(2));
 				// get all "showid"
 				Iterator<String> iter = unwatched.keys();
 				
 				ret=new JSONArray();
 				
 				JSONObject episode=null;
 				
 				// add info into jsonarray
 				while ( iter.hasNext() ) {
 					episode=unwatched.getJSONObject( iter.next() );
 					
 					// filter by _show if exists
 					if ( _show>0 && episode.getInt("showId")==_show ) {
 						ret.put( episode );
 					} else if ( _show<0 ) {
 						ret.put( episode );
 					} else {
 						continue;
 					}
 				}
 				
 			} catch (Exception e) {
 				System.err.println("--- oops: "+e.getMessage());
 				e.printStackTrace();
 			}
 		} else {
 			System.err.println("--- null from API call");
 		}
 		
 		return ret;
 	}
 	
 	/**
 	 * get next (future) episodes of all user's shows or filtered by
 	 * <code>_show</code><br>
 	 * calls <code>MyshowsAPI.getNextEpisodes()</code> and filter result by
 	 * <code>_show</code> (if <code>_show</code>>0)<br>
 	 * {@link JSONArray} format:
 		<pre>[
   {
     "airDate": "$dd.mm.yyyy",
     "episodeId": $episodeId,
     "episodeNumber": $episode_number,
     "seasonNumber": $season_number,
     "showId": $showId,
     "title": "$original_episode_title"
   }
 ]
 		</pre>
 	 * @param _show $showId to filter next episodes
 	 * @return {@link JSONArray} with episodes if success<br>
 	 * 			<code>null</code> otherwise
 	 */
 	public JSONArray getNextEpisodes(int _show) {
 //		System.out.println("+++ getNextEpisodes("+_show+")");
 		
 		JSONObject next=null;
 		JSONArray ret=null;
 		String result=api.getNextEpisodes();
 		
 		if ( result!=null ) {
 			try {
 				// put episodes in jsonobject{ "showid":{"info"} }
 				next=new JSONObject(result);
 				
 //				System.out.println(next.toString(2));
 				// get all "showid"
 				Iterator<String> iter = next.keys();
 				
 				ret=new JSONArray();
 				
 				JSONObject episode=null;
 				
 				// add info into jsonarray
 				while ( iter.hasNext() ) {
 					episode=next.getJSONObject( iter.next() );
 					
 					// filter by _show if exists
 					if ( _show>0 && episode.getInt("showId")==_show ) {
 						ret.put( episode );
 					} else if ( _show<0 ) {
 						ret.put( episode );
 					} else {
 						continue;
 					}
 				}
 				
 			} catch (Exception e) {
 				System.err.println("--- oops: "+e.getMessage());
 				e.printStackTrace();
 			}
 		} else {
 			System.err.println("--- null from API call");
 		}
 		
 		return ret;
 	}
 	
 	/**
 	 * get seen episodes of user's show (given by <code>_show</code>)<br>
 	 * calls <code>MyshowsAPI.getSeenEpisodes()</code><br>
 	 * {@link JSONArray} format:
 		<pre>[
   {
     "id": $episodeId,
     "watchDate": "$dd.mm.yyyy"
   }
 ]
 		</pre>
 	 * @param _show $showId to filter next episodes
 	 * @return {@link JSONArray} with episodes if success<br>
 	 * 			<code>null</code> otherwise
 	 */
 	public JSONArray getSeenEpisodes(int _show) {
 //		System.out.println("+++ getSeenEpisodes(int "+_show+")");
 		
 		JSONObject seen=null;
 		JSONArray ret=null;
 		String result=api.getSeenEpisodes(_show);
 		
 		if ( result!=null ) {
 			try {
 				// put episodes in jsonobject{ "showid":{"info"} }
 				seen=new JSONObject(result);
 				
 //				System.out.println(seen.toString(2));
 				// get all "showid"
 				Iterator<String> iter = seen.keys();
 				
 				ret=new JSONArray();
 				
 				// add info into jsonarray
 				while ( iter.hasNext() ) {
 					ret.put(
 							seen.getJSONObject( iter.next() )
 							);
 				}
 				
 			} catch (Exception e) {
 				System.err.println("--- oops: "+e.getMessage());
 				e.printStackTrace();
 			}
 		} else {
 			System.err.println("--- null from API call");
 		}
 		
 		return ret;
 	}
 	
 	/**
 	 * mark episode as watched<br>
 	 * calls <code>MyshowsAPI.checkEpisode(_episode)</code><br>
 	 * @param _episode $episodeId
 	 * @return <code>true</code> if success<br>
 	 * 			<code>false</code> otherwise (likely unauthorized)
 	 */
 	public boolean checkEpisode(int _episode) {
 //		System.out.println("+++ checkEpisode(int "+_episode+")");
 		
 		return api.checkEpisode(_episode);
 	}
 	
 	
 	/**
 	 * mark episode as watched with ratio<br>
 	 * calls <code>MyshowsAPI.checkEpisode(_episode, _ratio)</code>
 	 * @param _episode $episodeId
 	 * @param _ratio $ratio // <b>currently useless</b>
 	 * @return <code>true</code> if success<br>
 	 * 			<code>false</code> otherwise (likely unauthorized)
 	 */
 	public boolean checkEpisode(int _episode, int _ratio) {
 //		System.out.println("+++ checkEpisode(int "+_episode+", int "+_ratio+")");
 		
 		return api.checkEpisode(_episode, _ratio);
 	}
 	
 	/**
 	 * mark episode as unwatched<br>
 	 * calls <code>MyshowsAPI.unCheckEpisode(_episode)</code>
 	 * @param _episode $episodeId
 	 * @return <code>true</code> if success<br>
 	 * 			<code>false</code> otherwise (likely unauthorized)
 	 */
 	public boolean unCheckEpisode(int _episode) {
 //		System.out.println("+++ unCheckEpisode(int "+_episode+")");
 		
 		return api.unCheckEpisode(_episode);
 	}
 	
 	public boolean setShowStatus(int _show, String _status) {
 		System.out.println("client.setShowStatus: "+_show+":"+_status);
 		
 		MyshowsAPI.SHOW_STATUS st=MyshowsAPI.SHOW_STATUS.watching;
 		
 		if ( _status.equals("w") ) {
 			st=MyshowsAPI.SHOW_STATUS.watching;
 		} else if ( _status.equals("c") ) {
 			st=MyshowsAPI.SHOW_STATUS.cancelled;
 		} else if ( _status.equals("l") ) {
 			st=MyshowsAPI.SHOW_STATUS.later;
 		} else if ( _status.equals("r") ) {
 			st=MyshowsAPI.SHOW_STATUS.remove;
 		}
 		
 		return api.setShowStatus(_show, st);
 	}
 }
