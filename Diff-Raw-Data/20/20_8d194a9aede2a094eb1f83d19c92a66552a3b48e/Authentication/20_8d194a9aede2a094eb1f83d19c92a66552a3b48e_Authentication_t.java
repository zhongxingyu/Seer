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
package org.bonsaimind.minecraftmiddleknife.pre16;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
import org.bonsaimind.minecraftmiddleknife.Credentials;
 
 /**
  * Deals with the authentication at the Mojang, or any other server.
  */
 public class Authentication extends Credentials {
 
 	/**
 	 * The default version which will be reported.
 	 */
 	public static final String LAUNCHER_VERSION = "884";
 	/**
 	 * The addressof the Mojang server.
 	 */
 	public static final String MOJANG_SERVER = "https://login.minecraft.net";
 	private long currentVersion;
 	private String deprecated;
 	private boolean keepAliveUsesRealUsername = true;
 	private String realUsername;
 	private String server = MOJANG_SERVER;
 	private String sessionId;
 	private String userId;
 	private String version = LAUNCHER_VERSION;
 
 	public Authentication() {
 	}
 
 	public Authentication(Credentials credentials) {
 		super(credentials.getUsername(), credentials.getPassword());
 	}
 
 	public Authentication(String username, String password) {
 		super(username, password);
 	}
 
 	public Authentication(String server, String version, String username, String password) {
 		super(username, password);
 		this.server = server;
 		this.version = version;
 	}
 
 	/**
 	 * Do the authentication.
 	 * @return
 	 * @throws UnsupportedEncodingException
 	 * @throws MalformedURLException
 	 * @throws IOException
 	 */
 	public AuthenticationResponse authenticate() throws UnsupportedEncodingException, MalformedURLException, IOException {
 		String request = String.format(
 				"user=%s&password=%s&version=%s",
 				URLEncoder.encode(getUsername(), "UTF-8"),
 				URLEncoder.encode(getPassword(), "UTF-8"),
 				URLEncoder.encode(getVersion(), "UTF-8"));
 		String response = httpRequest(getServer(), request);
 		String[] splitted = response.split(":");
 
 		if (splitted.length < 5) {
 			return AuthenticationResponse.getResponse(response);
 		}
 
 		currentVersion = Long.parseLong(splitted[0]);
 		deprecated = splitted[1];
 		realUsername = splitted[2];
 		sessionId = splitted[3];
 		userId = splitted[4];
 
 		return AuthenticationResponse.SUCCESS;
 	}
 
 	/**
 	 * Returns the current version of Minecraft.
 	 * @return The current version.
 	 */
 	public long getCurrentVersion() {
 		return currentVersion;
 	}
 
 	/**
 	 * The DEPRECATED field of the login response, should always be "DEPRECATED".
 	 * @return Nothing useful.
 	 */
 	public String getDeprecated() {
 		return deprecated;
 	}
 
 	/**
 	 * Returns true of the keep-alive will be using the real username returned
 	 * by the authentication server.
 	 * @return If keep-alive uses the real username.
 	 */
 	public boolean isKeepAliveUsesRealUsername() {
 		return keepAliveUsesRealUsername;
 	}
 
 	/**
 	 * Returns the real username (case corrected f.e.).
 	 * @return The real username.
 	 */
 	public String getRealUsername() {
 		return realUsername;
 	}
 
 	/**
 	 * Returns the server which will be used for authentication. Default value
 	 * is the Mojang server.
 	 * @return The server.
 	 */
 	public String getServer() {
 		return server;
 	}
 
 	/**
 	 * Returns the session ID as acquired by the login process.
 	 * @return The Session ID.
 	 */
 	public String getSessionId() {
 		return sessionId;
 	}
 
 	/**
 	 * Returns the user ID as acquired by the login process.
 	 * @return The user ID.
 	 */
 	public String getUserId() {
 		return userId;
 	}
 
 	/**
 	 * Returns the version (of the launcher) which will be reported to
 	 * the server. Default value is the default one.
 	 * @return The launcher version.
 	 */
 	public String getVersion() {
 		return version;
 	}
 
 	/**
 	 * Sends a keep-alive to the authentication server so that the session
 	 * does not expire.
 	 * @throws UnsupportedEncodingException
 	 * @throws MalformedURLException
 	 * @throws IOException
 	 */
 	public void keepAlive() throws UnsupportedEncodingException, MalformedURLException, IOException {
 		String request = String.format(
 				"?name={0}&session={1}",
 				URLEncoder.encode(isKeepAliveUsesRealUsername() ? getRealUsername() : getUsername(), "UTF-8"),
 				URLEncoder.encode(getSessionId(), "UTF-8"));
 
 		httpRequest(getServer(), request);
 	}
 
 	/**
 	 * Determines if the keepa-live uses the real username returned by
 	 * the authentication server or the username set by the user.
 	 * @param keepAliveUsesRealUsername If keep-alive uses the real username.
 	 */
 	public void setKeepAliveUsesRealUsername(boolean keepAliveUsesRealUsername) {
 		this.keepAliveUsesRealUsername = keepAliveUsesRealUsername;
 	}
 
 	/**
 	 * Set the real username, this is most likely returned by the auth server.
 	 * @param realUsername The real username.
 	 */
 	public void setRealUsername(String realUsername) {
 		this.realUsername = realUsername;
 	}
 
 	/**
 	 * Set the session id, used for keep-alive.
 	 * @param sessionId The session id.
 	 */
 	public void setSessionId(String sessionId) {
 		this.sessionId = sessionId;
 	}
 
 	/**
 	 * Set the server which will be used for authentication.
 	 * @param server The (full) address of the server.
 	 */
 	public void setServer(String server) {
 		this.server = server;
 	}
 
 	/**
 	 * Set the version (of the launcher) which will be reported to
 	 * the authentication server. This should be a valid int, even
 	 * though it is a string.
 	 * @param version The version of the launcher, a valid int would be nice.
 	 */
 	public void setVersion(String version) {
 		this.version = version;
 	}
 
 	private static String httpRequest(String url, String content) throws UnsupportedEncodingException, MalformedURLException, IOException {
 		byte[] contentBytes = content.getBytes("UTF-8");
 
 		URLConnection connection = new URL(url).openConnection();
 		connection.setDoInput(true);
 		connection.setDoOutput(true);
 		connection.setRequestProperty("Accept-Charset", "UTF-8");
 		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
 		connection.setRequestProperty("Content-Length", Integer.toString(contentBytes.length));
 
 		OutputStream requestStream = connection.getOutputStream();
 		requestStream.write(contentBytes, 0, contentBytes.length);
 		requestStream.close();
 
 		String response = "";
 
 		BufferedReader responseStream = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
 		response = responseStream.readLine();
 		responseStream.close();
 
 		return response;
 	}
 }
