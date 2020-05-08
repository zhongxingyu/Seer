 /*
  * Copyright (C) 2007 Yusuke Yamamoto
  * Copyright (C) 2011 Twitter, Inc.
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
 
 package moefou4j;
 
 import moefou4j.Wiki.Type;
 import moefou4j.auth.Authorization;
 import moefou4j.auth.OAuthAuthorization;
 import moefou4j.conf.Configuration;
 import moefou4j.http.HttpParameter;
 import moefou4j.http.HttpResponse;
 import moefou4j.internal.util.Moefou4JInternalStringUtil;
 
 /**
  * A java representation of the Moefou API<br>
  * This class is thread safe and can be cached/re-used and used concurrently.<br>
  * Currently this class is not carefully designed to be extended. It is
  * suggested to extend this class only for mock testing purpose.<br>
  * 
  * @author Yusuke Yamamoto - yusuke at mac.com
  */
 final class MoefouImpl extends MoefouBaseImpl implements Moefou {
 
 	private final HttpParameter PARAM_API;
 
 	private final HttpParameter PARAM_API_KEY;
 
 	/* package */
 	MoefouImpl(final Configuration conf, final Authorization auth) {
 		super(conf, auth);
 		PARAM_API = new HttpParameter("api", "json");
 		PARAM_API_KEY = conf.getOAuthConsumerKey() != null ? new HttpParameter("api_key", conf.getOAuthConsumerKey())
 				: null;
 	}
 
 	@Override
 	public Playlist getNextPlaylist(final Playlist.PlaylistInformation info) throws MoefouException {
 		return factory.createPlayist(get(String.valueOf(info.getNextUrl())), "playlist");
 	}
 
 	@Override
 	public Playlist getPlaylist() throws MoefouException {
 		return getPlaylist(new Paging());
 	}
 
 	@Override
 	public Playlist getPlaylist(final Paging paging) throws MoefouException {
 		return factory.createPlayist(
 				get(conf.getMoeFMBaseURL() + "listen/playlist", mergeParameters(paging.toHttpParameters(), PARAM_API)),
 				"playlist");
 	}
 
 	@Override
 	public ResponseList<Wiki> getWikis(final Type[] types, final Paging paging, final String... tags)
 			throws MoefouException {
 		if (tags == null || tags.length == 0) throw new IllegalArgumentException("Tag is required");
 		final HttpParameter[] params = new HttpParameter[2];
 		params[0] = new HttpParameter("wiki_type", Moefou4JInternalStringUtil.join(Type.toTypeStringArray(types), ','));
 		params[1] = new HttpParameter("tag", Moefou4JInternalStringUtil.join(tags, ','));
 		return factory.createWikisList(
 				get(conf.getMoefouBaseURL() + "wikis.json", mergeParameters(paging.toHttpParameters(), params)));
 	}
 
 	@Override
 	public ResponseMessage logListened(long objId) throws MoefouException {
 		final HttpParameter[] params = new HttpParameter[5];
 		params[0] = new HttpParameter("log_obj_type", "sub");
 		params[1] = new HttpParameter("log_type", "listen");
 		params[2] = new HttpParameter("obj_type", "song");		
 		params[3] = new HttpParameter("obj_id", objId);
 		params[4] = PARAM_API;
 		return factory.createResponseMessage(get("http://moe.fm/ajax/log", params));
 	}
 	
 	@Override
 	public ResponseList<Wiki> searchWikis(Wiki.Type[] types, Paging paging, String... keywords) throws MoefouException {
 		if (keywords == null || keywords.length == 0) throw new IllegalArgumentException("Keyword is required");
 		final HttpParameter[] params = new HttpParameter[2];
 		params[0] = new HttpParameter("wiki_type", Moefou4JInternalStringUtil.join(Type.toTypeStringArray(types), ','));
 		params[1] = new HttpParameter("keyword", Moefou4JInternalStringUtil.join(keywords, ' '));
 		return factory.createWikisList(
 			get(conf.getMoefouBaseURL() + "wikis.json", mergeParameters(paging.toHttpParameters(), params)));
 	}
 	
 	@Override
 	public HttpResponse rawGet(final String url, final HttpParameter... params) throws MoefouException {
 		if (url == null) throw new NullPointerException();
		if (!url.startsWith(conf.getMoefouBaseURL()) && !url.startsWith(conf.getMoeFMBaseURL()))
 			throw new IllegalArgumentException("Not a valid API address!");
 		return get(url, params);
 	}
 
 	@Override
 	public HttpResponse rawPost(final String url, final HttpParameter... params) throws MoefouException {
 		if (url == null) throw new NullPointerException();
		if (!url.startsWith(conf.getMoefouBaseURL()) && !url.startsWith(conf.getMoeFMBaseURL()))
 			throw new IllegalArgumentException("Not a valid API address!");
 		return post(url, params);
 	}
 
 	@Override
 	public String toString() {
 		return "MoefouImpl{PARAM_API=" + PARAM_API + ", PARAM_API_KEY=" + PARAM_API_KEY + ", conf=" + conf + ", http="
 				+ http + ", factory=" + factory + ", auth=" + auth + "}";
 	}
 
 	private HttpResponse get(final String url, final HttpParameter... parameters) throws MoefouException {
 		if (auth instanceof OAuthAuthorization)
 			return http.get(url, parameters, auth);
 		else if (PARAM_API_KEY != null) return http.get(url, mergeParameters(parameters, PARAM_API_KEY), auth);
 		return http.get(url, parameters, auth);
 	}
 
 	private HttpResponse post(final String url, final HttpParameter... parameters) throws MoefouException {
 		if (auth instanceof OAuthAuthorization)
 			return http.post(url, parameters, auth);
 		else if (PARAM_API_KEY != null) return http.post(url, mergeParameters(parameters, PARAM_API_KEY), auth);
 		return http.post(url, parameters, auth);
 	}
 
 	private static HttpParameter[] mergeParameters(final HttpParameter[] params1, final HttpParameter... params2) {
 		if (params1 != null && params2 != null) {
 			final HttpParameter[] params = new HttpParameter[params1.length + params2.length];
 			System.arraycopy(params1, 0, params, 0, params1.length);
 			System.arraycopy(params2, 0, params, params1.length, params2.length);
 			return params;
 		}
 		if (null == params1 && null == params2) return new HttpParameter[0];
 		if (params1 != null)
 			return params1;
 		else
 			return params2;
 	}
 }
