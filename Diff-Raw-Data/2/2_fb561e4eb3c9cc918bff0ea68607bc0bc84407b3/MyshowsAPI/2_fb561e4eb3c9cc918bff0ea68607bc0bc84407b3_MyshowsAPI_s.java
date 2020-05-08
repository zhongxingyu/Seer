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
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.security.MessageDigest;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 /** 
  * MyShows API
  * @see <a href="http://api.myshows.ru/">http://api.myshows.ru/</a>
  * @author Ilya Arefiev (arefiev.id@gmail.com)
  */
 public class MyshowsAPI {
 	/**
 	 * auto-generated version number<br>
 	 * based on ant build.xml script and reassigned during every 'ant compile'<br>
 	 *
 	 * <b>do not edit this!</b>
 	 */
 	public static final float VERSION=0.2F;
 
 	/**
 	 * auto-generated full (including build number) version number<br>
 	 * based on ant build.xml script and reassigned during every 'ant compile'<br>
 	 *
 	 * <b>do not edit this!</b>
 	 */
 	public static final String VERSION_FULL="0.2.2";
 
 	final protected String URL_API_LOGIN="http://api.myshows.ru/profile/login?login=%1$s&password=%2$s";
 	final protected String URL_API_SHOWS="http://api.myshows.ru/profile/shows/";
 
 	final protected String URL_API_EPISODES_SEEN="http://api.myshows.ru/profile/shows/%1$d/";
 	final protected String URL_API_EPISODES_UNWATCHED="http://api.myshows.ru/profile/episodes/unwatched/";
 	final protected String URL_API_EPISODES_NEXT="http://api.myshows.ru/profile/episodes/next/";
 
 //	final protected String URL_API_EPISODES_IGNORED="http://api.myshows.ru/profile/episodes/ignored/list/";
 //	final protected String URL_API_EPISODES_IGNORED_ADD="http://api.myshows.ru/profile/episodes/ignored/add/%1$d";
 //	final protected String URL_API_EPISODES_IGNORED_REMOVE="http://api.myshows.ru/profile/episodes/ignored/remove/%1$d";
 
 	final protected String URL_API_EPISODE_CHECK="http://api.myshows.ru/profile/episodes/check/%1$d";
 	final protected String URL_API_EPISODE_CHECK_RATIO="http://api.myshows.ru/profile/episodes/check/%1$d?rating=%2$d";
 	final protected String URL_API_EPISODE_UNCHECK="http://api.myshows.ru/profile/episodes/uncheck/%1$d";
 	final protected String URL_API_EPISODE_RATIO="http://api.myshows.ru/profile/episodes/rate/%1$d/$2%d"; // ratio/episode
 
 	public enum SHOW_STATUS { watching, later, cancelled, remove };
 	final protected String URL_API_SHOW_STATUS="http://api.myshows.ru/profile/shows/%1$d/%2$s";
 //	final protected String URL_API_SHOW_RATIO="http://api.myshows.ru/profile/shows/%1$d/rate/%2$d"; // show/ratio
 
 //	final protected String URL_API_SHOW_FAVORITE_ADD="http://api.myshows.ru/profile/episodes/favorites/add/%1$d";
 //	final protected String URL_API_SHOW_FAVORITE_REMOVE="http://api.myshows.ru/profile/episodes/favorites/remove/%1$d";
 
 //	final protected String URL_API_NEWS="http://api.myshows.ru/profile/news/";
 	
 	/**
 	 * registered username
 	 */
 	protected String user=null;
 	
 	/**
 	 * username's password
 	 */
 	protected String password=null;
 	
 	/**
 	 * main http client<br>
 	 * unique for whole session because of auth cookies
 	 */
 	protected HttpClient httpClient=null;
 	
 	/**
 	 * dummy constructor<br>
 	 * just creates {@link HttpClient}
 	 */
 	protected MyshowsAPI() {
 		httpClient = new DefaultHttpClient();
 	}
 	
 	/**
 	 * common function to execute {@link HttpGet} requests<br>
 	 * @param _request request to execute
 	 * @return {@link String} with response if success<br>
 	 * 			<code>null</code> otherwise
 	 */
 	private String executeRequest(HttpGet _request) {
 		
 		if ( httpClient==null || _request==null ) {
 			System.err.println("--- httpClient || _request = null");
 			
 			return null;
 		}
 		
 		try {
 			HttpResponse response=httpClient.execute(_request);
 			
 			HttpEntity entity=response.getEntity();
 			if ( entity!=null ) {
 				
 				BufferedReader inputStream = new BufferedReader(
 						new InputStreamReader( entity.getContent() )
 						);
 				
 				StringBuffer answer = new StringBuffer();
 				String line;
 				
 				
 
 				while ( (line = inputStream.readLine()) != null ) {
 					answer.append(line).append("\n");
 				}
 				_request.abort();	// ~ close connection (?)
 				
 				// debug
 //				System.out.println("answer: >>>\n" + answer + "<<<");
 				
 				if ( response.getStatusLine().getStatusCode()==HttpURLConnection.HTTP_OK ) {
 					return answer.toString();
 				} else {
 					System.err.println("--- response status: "+response.getStatusLine().getStatusCode());
 					return null;
 				}
 			}
 		} catch (Exception e) {
 			System.err.println("--- oops: "+e.getMessage());
 			e.printStackTrace();
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * login into <code>username</code>'s account
 	 * @param _user username
 	 * @param _password password of username
 	 * @return <code>true</code> if success<br>
 	 * 			<code>false</code> otherwise 
 	 */
 	protected boolean login(String _user, String _password) {
 		
 		if ( httpClient==null ) {
 			System.err.println("--- httpClient=null. create new");
 			
 			httpClient=new DefaultHttpClient();
 			
 			if ( httpClient==null ) {
 				System.err.println("--- httpClient=null again");
 				return false;
 			}
 		}
 		
 		user=_user;
 		
 		// get md5 hash of password
 		// http://www.spiration.co.uk/post/1199/Java%20md5%20example%20with%20MessageDigest
 		try {
 			MessageDigest algorithm=MessageDigest.getInstance("MD5");
 			algorithm.reset();
 			algorithm.update(_password.getBytes());
 			
 			byte[] hashDigest=algorithm.digest();
 			
 			StringBuffer hexString=new StringBuffer();
 			for ( int i=0; i<hashDigest.length; i++ ) {
 				String  hex=Integer.toHexString( 0xFF & hashDigest[i] );
 				if ( hex.length()==1 ) {
 					hex="0"+hex;
 				}
 				hexString.append(hex);
 			}
 			
 			password=hexString.toString();
 			
 			// debug
 //			System.out.println("password: "+password);
 			
 		} catch (Exception e) {
 			System.err.println("--- oops: "+e.toString());
 			e.printStackTrace();
 			
 			password=null;
 
 			return false;
 		}
 		
 		//----------------------
 		
 		String URLs=String.format(URL_API_LOGIN, user, password);
 		
 		HttpGet request = new HttpGet(URLs);
 		
 		if ( executeRequest(request)!=null ) {
 			return true;
 		} else {
 			System.err.println("--- bad executeRequest @ login");
 		}
     	
 		return false;
 	}
 	
 	/**
 	 * <b>currently</b> drops httpclient
 	 * @return currently <b><code>true</code></b>
 	 */
 	protected boolean logout() {
 		user="";
 		password="";
 		
 		// TODO: temporary workaround. rewrite api's logout (without new httpclient) if possible
 		httpClient=null;
 		
 		return true;
 	}
 	
 	/**
 	 * get all shows (watching, canceled, etc) of user<br>
 	 * <code>JSON string</code> format:
 		<pre>{
   "$showId": {
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
 }
 		</pre>
 	 * @return <code>JSON string<code> with shows if success<br>
 	 * 			<code>null</code> otherwise
 	 */
 	protected String getShows() {
 		if ( httpClient==null ) {
 			return null;
 		}
 		
 		HttpGet request=new HttpGet(URL_API_SHOWS);
 		return executeRequest(request);
 	}
 
 	// TODO: docs
 	protected boolean setShowStatus(int _show, SHOW_STATUS _status) {
 
 		if ( httpClient==null ) {
 			return false;
 		}
 
 		System.out.println("api: set show("+_show+") status to "+_status.toString());
 
 		HttpGet request=new HttpGet( String.format(URL_API_SHOW_STATUS, _show, _status.toString()) );
 		return ( executeRequest(request)==null ? false : true );
 	}
 
 	/**
 	 * get all unwatched episodes of all user's shows<br>
 	 * <code>JSON string</code> format:
 		<pre>{
   "$episodeId": {
     "airDate": "$dd.mm.yyyy",
     "episodeId": $episodeId,
     "episodeNumber": $episode_number,
     "seasonNumber": $season_number,
     "showId": $showId,
     "title": "$original_episode_title"
   }
 }
 		</pre>
 	 * @return <code>JSON string<code> with episodes if success<br>
 	 * 			<code>null</code> otherwise
 	 */
 	protected String getUnwatchedEpisodes() {
 		if ( httpClient==null ) {
 			return null;
 		}
 		
 		HttpGet request=new HttpGet(URL_API_EPISODES_UNWATCHED);
 		return executeRequest(request);
 	}
 	
 	/**
 	 * get next (future) episodes of all user's shows<br>
 	 * <code>JSON string</code> format:
 		<pre>{
   "$episodeId": {
     "airDate": "$dd.mm.yyyy",
     "episodeId": $episodeId,
     "episodeNumber": $episode_number,
     "seasonNumber": $season_number,
     "showId": $showId,
     "title": "$original_episode_title"
   }
 }
 		</pre>
 	 * @return <code>JSON string<code> with episodes if success<br>
 	 * 			<code>null</code> otherwise
 	 */
 	protected String getNextEpisodes() {
 		if ( httpClient==null ) {
 			return null;
 		}
 		
 		HttpGet request=new HttpGet(URL_API_EPISODES_NEXT);
 		return executeRequest(request);
 	}
 	
 	/**
 	 * get seen episodes of user's show (given by <code>_show</code>)<br>
 	 * <code>JSON string</code> format:
 		<pre>{
   "$episodeId": {
     "id": $episodeId,
     "watchDate": "$dd.mm.yyyy"
   }
 }
 		</pre>
 	 * @param _show $showId
 	 * @return <code>JSON string<code> with episodes if success<br>
 	 * 			<code>null</code> otherwise
 	 */
 	protected String getSeenEpisodes(int _show) {
 		if ( httpClient==null || _show<0 ) {
 			return null;
 		}
 		
 		String URLs=String.format(URL_API_EPISODES_SEEN, _show);
 		HttpGet request=new HttpGet(URLs);
 		return executeRequest(request);
 	}
 
 	/**
 	 * mark episode as watched<br>
 	 * actually calls <code>checkEpisode(int _episode, -1)</code>
 	 * @param _episode $episodeId
 	 * @return <code>true</code> if success<br>
 	 * 			<code>false</code> otherwise (likely unauthorized)
 	 */
 	protected boolean checkEpisode(int _episode) {
 		return checkEpisode(_episode, -1);
 	}
 	
 	/**
 	 * mark episode as watched with ratio<br>
 	 * @param _episode $episodeId
 	 * @param _ratio if ( _ratio<0 ) { no ratio for http call using }
 	 * @return <code>true</code> if success<br>
 	 * 			<code>false</code> otherwise (likely unauthorized)
 	 */
 	protected boolean checkEpisode(int _episode, int _ratio) {
 		
 		if ( httpClient==null || _episode<0 ) {
 			// debug
 			System.err.println("--- no httpClient || episode");
 			return false;
 		}
 		
 		String URLs=null;
 		if ( _ratio<0 || _ratio>5 ) {
 			URLs=String.format(URL_API_EPISODE_CHECK, _episode);
 		} else {
 			URLs=String.format(URL_API_EPISODE_CHECK_RATIO, _episode, _ratio); // TODO: check if ratio appears @ msh web
 		}
 		
 		HttpGet request = new HttpGet(URLs);
 		return executeRequest(request)==null ? false : true;
 	}
 
 	// TODO: docs
 	protected boolean setEpisodeRatio(int _episode, int _ratio) {
 
 		if ( httpClient==null || _episode<0 ) {
 			// debug
 			System.err.println("--- no httpClient || episode");
 			return false;
 		}
 
 		String URLs=null;
 		if ( _ratio<0 || _ratio>5 ) {
 			System.err.println("--- wrong ratio: "+_ratio);
 		} else {
			URLs=String.format(URL_API_EPISODE_RATIO, _episode, _ratio);
 		}
 
 		HttpGet request = new HttpGet(URLs);
 		return executeRequest(request)==null ? false : true;
 	}
 
 	/**
 	 * mark episode as unwatched
 	 * @param _episode $episodeId
 	 * @return <code>true</code> if success<br>
 	 * 			<code>false</code> otherwise (likely unauthorized)
 	 */
 	protected boolean unCheckEpisode(int _episode) {
 		
 		if ( httpClient==null || _episode<0 ) {
 			// debug
 			System.err.println("--- no httpClient || episode");
 			return false;
 		}
 		
 		HttpGet request = new HttpGet( String.format(URL_API_EPISODE_UNCHECK, _episode) );
 		return executeRequest(request)==null ? false : true;
 	}
 }
