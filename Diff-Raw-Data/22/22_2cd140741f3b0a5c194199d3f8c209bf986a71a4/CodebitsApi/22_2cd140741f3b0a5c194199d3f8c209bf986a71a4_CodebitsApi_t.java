 /*  
  *  Codebits - Comment the talk proposals.
  *  Copyright (C) 2011 Henrique Rocha <hmrocha@gmail.com>
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.androidappdev.codebits;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.util.Log;
 
 /**
  * Methods of the Codebits API https://services.sapo.pt/Codebits
  * 
  * @author Henrique Rocha
  */
 public class CodebitsApi {
 	public static final String TAG = "CodebitsApi";
 
 	/**
 	 * Perform an HTTP request
 	 * 
 	 * @param url
 	 *            the URL for the request
 	 * @return a String with the result of the request
 	 */
 	private static String performHttpRequest(String url) {
 		StringBuilder builder = new StringBuilder();
 		HttpClient client = new DefaultHttpClient();
 		HttpGet request = new HttpGet(url);
 		try {
 			HttpResponse response = client.execute(request);
 			HttpEntity entity = response.getEntity();
 			InputStream content = entity.getContent();
 			BufferedReader reader = new BufferedReader(new InputStreamReader(
 					content));
 			String line = null;
 			while ((line = reader.readLine()) != null) {
 				builder.append(line);
 			}
 		} catch (Exception e) {
 			Log.d(TAG, "Exception", e);
 		}
 		return builder.toString();
 	}
 
 	/**
 	 * Get the auth token. Will be used as a &token= argument with all the
 	 * authenticated methods.
 	 * 
 	 * @param user
 	 *            e-mail of the user
 	 * @param password
 	 *            the user's password
 	 * @return the auth token
 	 */
 	public static String getToken(String user, String password) {
 		try {
 			String url = "https://services.sapo.pt/Codebits/gettoken?"
 					+ "user=" + user + "&" + "password=" + password;
 			JSONObject jsonObject;
 			jsonObject = new JSONObject(performHttpRequest(url));
 			return jsonObject.getString("token");
 		} catch (JSONException e) {
 			Log.d(TAG, "JSONException", e);
 		}
 		return null;
 	}
 
 	/**
 	 * List submissions
 	 * 
 	 * @param token
 	 *            authentication token
 	 * @return Returns the list of the call for talks submissions for this year.
 	 * @throws JSONException
 	 */
 	public static List<Talk> listSubmissions(String token) {
		LinkedList<Talk> result = new LinkedList<Talk>();
 		try {
 			String url = "https://services.sapo.pt/Codebits/calltalks";
 			if (token != null) {
 				url += "?token=" + token;
 			}
 			JSONArray jsonArray = new JSONArray(performHttpRequest(url));
 			for (int i = 0; i < jsonArray.length(); i++) {
 				result.add(new Talk(jsonArray.getJSONObject(i)));
 			}
 		} catch (JSONException e) {
 			Log.d(TAG, "JSONException", e);
 		}
		return result;
 	}
 }
