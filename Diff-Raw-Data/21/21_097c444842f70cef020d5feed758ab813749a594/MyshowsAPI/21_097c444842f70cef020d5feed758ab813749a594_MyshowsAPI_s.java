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
 	final protected String URL_API_LOGIN="http://api.myshows.ru/profile/login?login=%1$s&password=%2$s";
 	final protected String URL_API_SHOWS="http://api.myshows.ru/profile/shows/";
 	final protected String URL_API_EPISODES_UNWATCHED="http://api.myshows.ru/profile/episodes/unwatched/";
 	final protected String URL_API_EPISODES_NEXT="http://api.myshows.ru/profile/episodes/next/";
 	final protected String URL_API_EPISODES_SEEN="http://api.myshows.ru/profile/shows/%1$d/";
 	final protected String URL_API_EPISODE_CHECK="http://api.myshows.ru/profile/episodes/check/%1$d";
 	final protected String URL_API_EPISODE_CHECK_RATIO="http://api.myshows.ru/profile/episodes/check/%1$d?rating=%2$d";
 	final protected String URL_API_EPISODE_UNCHECK="http://api.myshows.ru/profile/episodes/uncheck/%1$d";
 	
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
 	 * login into <code>username</code>'s account
 	 * @param _user username
 	 * @param _password password of username
 	 * @return <code>true</code> if success<br>
 	 * 			<code>false</code> otherwise 
 	 */
 	protected boolean login(String _user, String _password) {
 		
 		if ( httpClient==null ) {
 			System.err.println("--- httpClient=null");
 			
 			return false;
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
 			System.out.println("password: "+password);
 			
 		} catch (Exception e) {
 			System.err.println("--- oops: "+e.toString());
 			e.printStackTrace();
 			
 			password=null;
 
 			return false;
 		}
 		
 		//----------------------
 		
 		String URLs=String.format(URL_API_LOGIN, user, password);
     	
 		try {
 			HttpGet request = new HttpGet(URLs);
 			
 			HttpResponse response = httpClient.execute(request);
 			
 			// TODO: rewrite checking logged in (?)
 			if ( response.getStatusLine().getStatusCode()==HttpURLConnection.HTTP_OK ) {
 				request.abort();	// ~ close connection (?)
 				return true;
 			} else {
 				HttpEntity entity=response.getEntity();
 				if ( entity!=null ) {
 					BufferedReader inputStream = new BufferedReader(
 							new InputStreamReader( entity.getContent() )
 							);
 					String answer = "";
 					String line;
 					while ( (line = inputStream.readLine()) != null ) {
 						answer += (line + "\n");
 					}
 					request.abort();	// ~ close connection (?)
 					
 					System.out.println("answer: >>>\n" + answer + "<<<");
 				}
 			}
 			
 		} catch (Exception e) {
 			System.err.println("--- oops: "+e.getMessage());
 			e.printStackTrace();
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * get all shows (watching, canceled, etc) of user<br>
 	 * <code>JSON string</code> format:
 		<pre>{
   "$showId": {
     "rating": 0,
     "ruTitle": "$translated_title",
    "runtime": 43,	// TODO
     "showId": $showId,
    "showStatus": "Returning Series", // TODO: variants
     "title": "$original_title",
     "totalEpisodes": $num_of_totlat_episodes,
    "watchStatus": "$user's_watching_status",	// TODO: variants 
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
 		
 		try {
 			HttpGet request=new HttpGet(URL_API_SHOWS);
 			
 			HttpResponse response=httpClient.execute(request);
 			
 			HttpEntity entity=response.getEntity();
 			if ( entity!=null ) {
 				BufferedReader inputStream = new BufferedReader(
 						new InputStreamReader( entity.getContent() )
 						);
 				String answer = "";
 				String line;
 				while ( (line = inputStream.readLine()) != null ) {
 					answer += (line + "\n");
 				}
 				request.abort();	// ~ close connection (?)
 				
 				// debug
 				System.out.println("answer: >>>\n" + answer + "<<<");
 				
 				if ( response.getStatusLine().getStatusCode()==HttpURLConnection.HTTP_OK ) {
 					return answer;
 				} else {
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
 	 * get all unwatched episodes of all user's shows<br>
 	 * <code>JSON string</code> format:
 		<pre>{
   "$episode_id": {
     "airDate": "$dd.mm.yyyy",
     "episodeId": $episode_id,
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
 		
 		try {
 			HttpGet request=new HttpGet(URL_API_EPISODES_UNWATCHED);
 			
 			HttpResponse response=httpClient.execute(request);
 			
 			HttpEntity entity=response.getEntity();
 			if ( entity!=null ) {
 				BufferedReader inputStream = new BufferedReader(
 						new InputStreamReader( entity.getContent() )
 						);
 				String answer = "";
 				String line;
 				while ( (line = inputStream.readLine()) != null ) {
 					answer += (line + "\n");
 				}
 				request.abort();	// ~ close connection (?)
 				
 				// debug
 				System.out.println("answer: >>>\n" + answer + "<<<");
 				
 				if ( response.getStatusLine().getStatusCode()==HttpURLConnection.HTTP_OK ) {
 					return answer;
 				} else {
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
 	 * get next (future) episodes of all user's shows<br>
 	 * <code>JSON string</code> format:
 		<pre>{
   "$episode_id": {
     "airDate": "$dd.mm.yyyy",
     "episodeId": $episode_id,
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
 		
 		try {
 			HttpGet request=new HttpGet(URL_API_EPISODES_NEXT);
 			
 			HttpResponse response=httpClient.execute(request);
 			
 			HttpEntity entity=response.getEntity();
 			if ( entity!=null ) {
 				BufferedReader inputStream = new BufferedReader(
 						new InputStreamReader( entity.getContent() )
 						);
 				String answer = "";
 				String line;
 				while ( (line = inputStream.readLine()) != null ) {
 					answer += (line + "\n");
 				}
 				request.abort();	// ~ close connection (?)
 				
 				// debug
 				System.out.println("answer: >>>\n" + answer + "<<<");
 				
 				if ( response.getStatusLine().getStatusCode()==HttpURLConnection.HTTP_OK ) {
 					return answer;
 				} else {
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
 	 * get seen episodes of user's show (given by <code>_show</code>)<br>
 	 * <code>JSON string</code> format:
 		<pre>{
   "$episode_id": {
     "id": $episode_id,
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
 		
 		try {
 			String URLs=String.format(URL_API_EPISODES_SEEN, _show);
 			HttpGet request=new HttpGet(URLs);
 			
 			HttpResponse response=httpClient.execute(request);
 			
 			HttpEntity entity=response.getEntity();
 			if ( entity!=null ) {
 				BufferedReader inputStream = new BufferedReader(
 						new InputStreamReader( entity.getContent() )
 						);
 				String answer = "";
 				String line;
 				while ( (line = inputStream.readLine()) != null ) {
 					answer += (line + "\n");
 				}
 				request.abort();	// ~ close connection (?)
 				
 				// debug
 				System.out.println("answer: >>>\n" + answer + "<<<");
 				
 				if ( response.getStatusLine().getStatusCode()==HttpURLConnection.HTTP_OK ) {
 					return answer;
 				} else {
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
 	 * mark episode as watched<br>
 	 * actually calls <code>checkEpisode(int _episode, -1)</code>
 	 * @param _episode $episode_id
 	 * @return <code>true</code> if success<br>
 	 * 			<code>false</code> otherwise (likely unauthorized)
 	 */
 	protected boolean checkEpisode(int _episode) {
 		return checkEpisode(_episode, -1);
 	}
 	
 	/**
 	 * mark episode as watched with ratio<br>
 	 * @param _episode $episode_id
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
     			
 		try {
 			HttpGet request = new HttpGet(URLs);
 			
 			HttpResponse response = httpClient.execute(request);
 			
 			if ( response.getStatusLine().getStatusCode()==HttpURLConnection.HTTP_OK ) {
 				request.abort();	// ~ close connection (?)
 				return true;
 			} else {
 				HttpEntity entity=response.getEntity();
 				if ( entity!=null ) {
 					BufferedReader inputStream = new BufferedReader(
 							new InputStreamReader( entity.getContent() )
 							);
 					String answer = "";
 					String line;
 					while ( (line = inputStream.readLine()) != null ) {
 						answer += (line + "\n");
 					}
 					request.abort();	// ~ close connection (?)
 					
 					System.out.println("answer: >>>\n" + answer + "<<<");
 				}
 			}
 			
 		} catch (Exception e) {
 			System.err.println("--- oops: "+e.getMessage());
 			e.printStackTrace();
 		}
 		
 		return false;
 	}
 
 	/**
 	 * mark episode as unwatched
 	 * @param _episode $episode_id
 	 * @return <code>true</code> if success<br>
 	 * 			<code>false</code> otherwise (likely unauthorized)
 	 */
 	protected boolean unCheckEpisode(int _episode) {
 		
 		if ( httpClient==null || _episode<0 ) {
 			// debug
 			System.err.println("--- no httpClient || episode");
 			return false;
 		}
 
 		try {
 			HttpGet request = new HttpGet( String.format(URL_API_EPISODE_UNCHECK, _episode) );
 			
 			HttpResponse response = httpClient.execute(request);
 			
 			if ( response.getStatusLine().getStatusCode()==HttpURLConnection.HTTP_OK ) {
 				request.abort();	// ~ close connection (?)
 				return true;
 			} else {
 				HttpEntity entity=response.getEntity();
 				if ( entity!=null ) {
 					BufferedReader inputStream = new BufferedReader(
 							new InputStreamReader( entity.getContent() )
 							);
 					String answer = "";
 					String line;
 					while ( (line = inputStream.readLine()) != null ) {
 						answer += (line + "\n");
 					}
 					request.abort();	// ~ close connection (?)
 					
 					System.out.println("answer: >>>\n" + answer + "<<<");
 				}
 			}
 			
 		} catch (Exception e) {
 			System.err.println("--- oops: "+e.getMessage());
 			e.printStackTrace();
 		}
 		
 		return false;
 	}
 }
