 /*
  	org.manalith.ircbot.plugin.twitreader/TwitReaderRunner.java
  	ManalithBot - An open source IRC bot based on the PircBot Framework.
  	Copyright (C) 2011, 2012  Seong-ho, Cho <darkcircle.0426@gmail.com>
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.manalith.ircbot.plugin.twitreader;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.URI;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Locale;
 import java.util.regex.Pattern;
 
 import org.apache.commons.lang3.StringUtils;
 import org.apache.commons.lang3.time.DateFormatUtils;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.utils.URIUtils;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.log4j.Logger;
 import org.json.JSONArray;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 
 public class TwitReader {
 	private Logger logger = Logger.getLogger(getClass());
 	private static final String TWITTER_USER_HOME_PATTERN = "http(s)?\\:\\/\\/twitter\\.com\\/(\\#\\!\\/)?([a-zA-Z0-9\\_]{1,15}(\\/)?){1}";
	private static final String TWITTER_TWIT_URL_PATTERN = "http(s)?\\:\\/\\/twitter\\.com\\/\\#\\!\\/[a-zA-Z0-9\\_]{1,15}\\/status\\/[0-9]+";
 	private static final String SOURCE_DATE_PATTERN = "EEE MMM dd HH:mm:ss ZZZZ yyyy";
 	private static final String TARGET_DATE_PATTERN = "yyyy년 MM월 dd일 E요일 HH:mm:ss";
 
 	private enum UrlType {
 		TwitURL, UserURL
 	}
 
 	public String read(String[] strs) {
 		for (String str : strs) {
 			String result = getText(str, validateUrl(str));
 			if (result != null)
 				return result;
 		}
 
 		return null;
 	}
 
 	private String getText(String twitterurl, UrlType type) {
 		if (type == null)
 			return null;
 
 		String result = null;
 
 		try {
 			String[] PathnQuery = getJSONPathNQuery(twitterurl, type);
 			URI uri = URIUtils.createURI("https", "api.twitter.com", -1,
 					PathnQuery[0], PathnQuery[1], null);
 			HttpGet get = new HttpGet(uri);
 			DefaultHttpClient httpclient = new DefaultHttpClient();
 			JSONTokener tokener = new JSONTokener((new BufferedReader(
 					new InputStreamReader(httpclient.execute(get).getEntity()
 							.getContent()))).readLine());
 
 			switch (type) {
 			case TwitURL: {
 				JSONObject obj = new JSONObject(tokener);
 
 				String written_by = obj.getJSONObject("user").getString("name");
 				String body = obj.getString("text");
 
 				result = "작성자 : " + written_by + ", 본문 : " + body;
 			}
 				break;
 			case UserURL:
 				JSONArray arr = new JSONArray(tokener);
 
 				if (arr.length() == 0) {
 					result = "게시물이 존재하지 않습니다";
 				} else {
 					JSONObject obj = arr.getJSONObject(0);
 
 					String written_datetime = obj.getString("created_at");
 					String body = obj.getString("text");
 
 					result = "작성시각 : "
 							+ getDateTimeinKoreanFormat(written_datetime)
 							+ ", 본문 : " + body;
 				}
 
 				break;
 			}
 		} catch (Exception e) {
 			logger.error(e);
 		}
 
 		return result;
 	}
 
 	private UrlType validateUrl(String url) {
 		UrlType result = null;
 
 		if (StringUtils.isEmpty(url))
 			return null;
 		else if (Pattern.compile(TWITTER_TWIT_URL_PATTERN).matcher(url)
 				.matches())
 			return UrlType.TwitURL;
 		else if (Pattern.compile(TWITTER_USER_HOME_PATTERN).matcher(url)
 				.matches())
 			return UrlType.UserURL;
 
 		return result;
 	}
 
 	private String[] getJSONPathNQuery(String twitterurl, UrlType type) {
 		String[] json_requrl = new String[2];
 		// [0] : path, [1] query, [2] path, [3] query
 
 		String[] url = twitterurl.split("\\/");
 
 		switch (type) {
 		case TwitURL:
 			String twit_id = url[url.length - 1];
 			json_requrl[0] = "1/statuses/show.json";
 			json_requrl[1] = "id=" + twit_id + "&include_entities=false";
 			break;
 		case UserURL:
 			String scrname = url[url.length - 1];
 			json_requrl[0] = "1/statuses/user_timeline.json";
 			json_requrl[1] = "include_entities=false&include_rts=true&screen_name="
 					+ scrname + "&count=1";
 			break;
 		}
 
 		return json_requrl;
 	}
 
 	private String getDateTimeinKoreanFormat(String dateString) {
 		String result = null;
 
 		try {
 			SimpleDateFormat sourceDateFormat = new SimpleDateFormat(
 					SOURCE_DATE_PATTERN, Locale.ENGLISH);
 			result = DateFormatUtils.format(sourceDateFormat.parse(dateString),
 					TARGET_DATE_PATTERN);
 		} catch (ParseException e) {
 			logger.error(e);
 		}
 
 		return result;
 	}
 
 }
