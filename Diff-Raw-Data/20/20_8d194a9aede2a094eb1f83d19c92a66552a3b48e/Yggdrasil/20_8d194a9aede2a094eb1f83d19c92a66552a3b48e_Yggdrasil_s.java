 /*
  * Copyright 2013 Robert 'Bobby' Zenz. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are
  * permitted provided that the following conditions are met:
  *
  * 1. Redistributions of source code must retain the above copyright notice, this list of
  * conditions and the following disclaimer.
  *
  * 2. Redistributions in binary form must reproduce the above copyright notice, this list
  * of conditions and the following disclaimer in the documentation and/or other materials
  * provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY Robert 'Bobby' Zenz ''AS IS'' AND ANY EXPRESS OR IMPLIED
  * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
  * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
  * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * The views and conclusions contained in the software and documentation are those of the
  * authors and should not be interpreted as representing official policies, either expressed
  * or implied, of Robert 'Bobby' Zenz.
  */
package org.bonsaimind.minecraftmiddleknife;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.UUID;
 import org.json.simple.JSONObject;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 
 /**
  * Deals with the new authentication system called...Yggdrasil.
  */
 public class Yggdrasil extends Credentials {
 
 	private static final class HttpResponse {
 
 		private int code;
 		private String response;
 
 		public HttpResponse(int code, String response) {
 			this.code = code;
 			this.response = response;
 		}
 
 		public int getCode() {
 			return code;
 		}
 
 		public String getResponse() {
 			return response;
 		}
 	}
 	public static final String AGENT_NAME = "Minecraft";
 	public static final int AGENT_VERSION = 1;
 	public static final String ENDPOINT_AUTHENTICATE = "authenticate";
 	public static final String ENDPOINT_REFRESH = "refresh";
 	public static final String ENDPOINT_VALIDATE = "validate";
 	public static final String MOJANG_SERVER = "https://authserver.mojang.com/";
 	private String accessToken;
 	private String agentName = AGENT_NAME;
 	private int agentVersion = AGENT_VERSION;
 	private String clientToken = UUID.randomUUID().toString();
 	private String endpointAuthenticate = ENDPOINT_AUTHENTICATE;
 	private String endpointRefresh = ENDPOINT_REFRESH;
 	private String endpointValidate = ENDPOINT_VALIDATE;
 	private String error;
 	private String errorCause;
 	private String errorMessage;
 	private String realUsername;
 	private String server = MOJANG_SERVER;
 	private String userId;
 
 	public Yggdrasil() {
 	}
 
 	public Yggdrasil(Credentials credentials) {
 		super(credentials.getUsername(), credentials.getPassword());
 	}
 
 	public Yggdrasil(String username, String password) {
 		super(username, password);
 	}
 
 	public Yggdrasil(String server, String username, String password) {
 		super(username, password);
 
 		this.server = server;
 	}
 
 	/**
 	 * Authenticate and acquire the access token.
 	 * @return True if the authentication was successful.
 	 * @throws UnsupportedEncodingException
 	 * @throws MalformedURLException
 	 * @throws IOException
 	 * @throws ParseException
 	 */
 	public boolean authenticate() throws UnsupportedEncodingException, MalformedURLException, IOException, ParseException {
 		resetError();
 
 		HttpResponse response = httpRequest(server, endpointAuthenticate, createAuthenticationJSON());
 		if (response.getCode() == 200) {
 			parseAuthenticationJSON(response.getResponse());
 			return true;
 		} else {
 			parseErrorJSON(response.getResponse());
 			return false;
 		}
 	}
 
 	/**
 	 * Get the Access-Token.
 	 * @return
 	 */
 	public String getAccessToken() {
 		return accessToken;
 	}
 
 	/**
 	 * Get the currently set Agent-Name
 	 * @return
 	 */
 	public String getAgentName() {
 		return agentName;
 	}
 
 	/**
 	 * Get the currently set Agent-Version.
 	 * @return
 	 */
 	public int getAgentVersion() {
 		return agentVersion;
 	}
 
 	/**
 	 * Get the client token.
 	 * @return
 	 */
 	public String getClientToken() {
 		return clientToken;
 	}
 
 	public String getEndpointAuthenticate() {
 		return endpointAuthenticate;
 	}
 
 	public String getEndpointRefresh() {
 		return endpointRefresh;
 	}
 
 	public String getEndpointValidate() {
 		return endpointValidate;
 	}
 
 	public String getError() {
 		return error;
 	}
 
 	public String getErrorCause() {
 		return errorCause;
 	}
 
 	public String getErrorMessage() {
 		return errorMessage;
 	}
 
 	public String getRealUsername() {
 		return realUsername;
 	}
 
 	public String getServer() {
 		return server;
 	}
 
 	/**
 	 * Get the User-Id as returned from the server.
 	 * @return
 	 */
 	public String getUserId() {
 		return userId;
 	}
 
 	/**
 	 * Refresh the access-token so that it does not expire.
 	 * @return True if token is still valid.
 	 * @throws UnsupportedEncodingException
 	 * @throws MalformedURLException
 	 * @throws IOException
 	 * @throws ParseException
 	 */
 	public boolean refresh() throws UnsupportedEncodingException, MalformedURLException, IOException, ParseException {
 		resetError();
 
 		HttpResponse response = httpRequest(server, endpointRefresh, createRefreshJSON());
 		if (response.getCode() == 200) {
 			parseRefreshJSON(response.getResponse());
 			return true;
 		} else {
 			parseErrorJSON(response.getResponse());
 			return false;
 		}
 	}
 
 	/**
 	 * Set the access-token to be used.
 	 * @param accessToken
 	 */
 	public void setAccessToken(String accessToken) {
 		this.accessToken = accessToken;
 	}
 
 	/**
 	 * Set the agent-name. Setting this to something else then "Minecraft" will
 	 * most likely wreck functionality.
 	 * @param agentName
 	 */
 	public void setAgentName(String agentName) {
 		this.agentName = agentName;
 	}
 
 	/**
 	 * Set the agent-version.
 	 * @param agentVersion
 	 */
 	public void setAgentVersion(int agentVersion) {
 		this.agentVersion = agentVersion;
 	}
 
 	/**
 	 * Set the client-token. The client-token is associated with
 	 * the access-token and vice versa.
 	 * @param clientToken
 	 */
 	public void setClientToken(String clientToken) {
 		this.clientToken = clientToken;
 	}
 
 	public void setEndpointAuthenticate(String endpointAuthenticate) {
 		this.endpointAuthenticate = endpointAuthenticate;
 	}
 
 	public void setEndpointRefresh(String endpointRefresh) {
 		this.endpointRefresh = endpointRefresh;
 	}
 
 	public void setEndpointValidate(String endpointValidate) {
 		this.endpointValidate = endpointValidate;
 	}
 
 	public void setRealUsername(String realUsername) {
 		this.realUsername = realUsername;
 	}
 
 	public void setServer(String server) {
 		this.server = server;
 	}
 
 	public void setUserId(String userId) {
 		this.userId = userId;
 	}
 
 	/**
 	 * Validate the current access-token.
 	 * @return Retuns true if the access-token is valid.
 	 * @throws UnsupportedEncodingException
 	 * @throws MalformedURLException
 	 * @throws IOException
 	 * @throws ParseException
 	 */
 	public boolean validate() throws UnsupportedEncodingException, MalformedURLException, IOException, ParseException {
 		resetError();
 
 		HttpResponse response = httpRequest(server, ENDPOINT_VALIDATE, createValidationJSON());
 		if (response.getCode() != 200) {
 			parseErrorJSON(response.getResponse());
 			return false;
 		}
 
 		return true;
 	}
 
 	private String createAuthenticationJSON() {
 		JSONObject parent = new JSONObject();
 
 		JSONObject agent = new JSONObject();
 		agent.put("name", getAgentName());
 		agent.put("version", getAgentVersion());
 		parent.put("agent", agent);
 
 		parent.put("username", getUsername());
 		parent.put("password", getPassword());
 		parent.put("clientToken", getClientToken());
 
 		return parent.toJSONString();
 	}
 
 	private String createRefreshJSON() {
 		JSONObject parent = new JSONObject();
 
 		parent.put("accessToken", getAccessToken());
 		parent.put("clientToken", getClientToken());
 
 		JSONObject selectedProfile = new JSONObject();
 		selectedProfile.put("id", getUserId());
 		selectedProfile.put("name", getUsername());
 		parent.put("selectedProfile", selectedProfile);
 
 		return parent.toJSONString();
 	}
 
 	private String createValidationJSON() {
 		JSONObject parent = new JSONObject();
 
 		parent.put("accessToken", getAccessToken());
 
 		return parent.toJSONString();
 	}
 
 	private void parseAuthenticationJSON(String response) throws ParseException {
 		JSONParser parser = new JSONParser();
 		JSONObject parent = (JSONObject) parser.parse(response);
 		setAccessToken((String) parent.get("accessToken"));
 
 		if (parent.containsKey("selectedProfile")) {
 			JSONObject selectedProfile = (JSONObject) parent.get("selectedProfile");
 			setUserId((String) selectedProfile.get("id"));
 			setRealUsername((String) selectedProfile.get("name"));
 		}
 	}
 
 	private void parseErrorJSON(String response) throws ParseException {
 		JSONParser parser = new JSONParser();
 		JSONObject parent = (JSONObject) parser.parse(response);
 
 		error = (String) parent.get("error");
 		errorMessage = (String) parent.get("errorMessage");
 		if (parent.containsKey("cause")) {
 			errorCause = (String) parent.get("cause");
 		}
 	}
 
 	private void parseRefreshJSON(String response) throws ParseException {
 		JSONParser parser = new JSONParser();
 		JSONObject parent = (JSONObject) parser.parse(response);
 		setAccessToken((String) parent.get("accessToken"));
 
 		if (parent.containsKey("selectedProfile")) {
 			JSONObject selectedProfile = (JSONObject) parent.get("selectedProfile");
 			setUserId((String) selectedProfile.get("id"));
 			setRealUsername((String) selectedProfile.get("name"));
 		}
 	}
 
 	private void resetError() {
 		error = null;
 		errorCause = null;
 		errorMessage = null;
 	}
 
 	private static HttpResponse httpRequest(String url, String endpoint, String content) throws UnsupportedEncodingException, MalformedURLException, IOException {
 		if (url.endsWith("/")) { // Ugly hack...who cares?
 			url += endpoint;
 		} else {
 			url += "/" + endpoint;
 		}
 
 		byte[] contentBytes = content.getBytes("UTF-8");
 
 		URLConnection connection = new URL(url).openConnection();
 		connection.setDoInput(true);
 		connection.setDoOutput(true);
 		connection.setRequestProperty("Accept-Charset", "UTF-8");
 		connection.setRequestProperty("Content-Type", "application/json");
 		connection.setRequestProperty("Content-Length", Integer.toString(contentBytes.length));
 
 		OutputStream requestStream = connection.getOutputStream();
 		requestStream.write(contentBytes, 0, contentBytes.length);
 		requestStream.close();
 
 		String response = "";
 		BufferedReader responseStream;
 		if (((HttpURLConnection) connection).getResponseCode() == 200) {
 			responseStream = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
 		} else {
 			responseStream = new BufferedReader(new InputStreamReader(((HttpURLConnection) connection).getErrorStream(), "UTF-8"));
 		}
 
 		response = responseStream.readLine();
 		responseStream.close();
 
 		return new HttpResponse(((HttpURLConnection) connection).getResponseCode(), response);
 	}
 }
